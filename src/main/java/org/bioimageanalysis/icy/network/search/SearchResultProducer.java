/*
 * Copyright (c) 2010-2024. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */
package org.bioimageanalysis.icy.network.search;

import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.thread.SingleProcessor;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The SearchResultProducer create {@link SearchResult} objects from given search keywords.<br>
 * These {@link SearchResult} are then consumed by a {@link SearchResultConsumer}.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public abstract class SearchResultProducer implements Comparable<SearchResultProducer> {
    private class SearchRunner implements Runnable {
        private final String text;
        private final SearchResultConsumer consumer;

        public SearchRunner(final String text, final SearchResultConsumer consumer) {
            super();

            this.text = text;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            // perform search if text is not empty
            if (!StringUtil.isEmpty(text)) {
                try {
                    doSearch(text, consumer);
                }
                catch (final Throwable t) {
                    // just display the exception and continue
                    IcyLogger.error(SearchResultProducer.class, t, t.getLocalizedMessage());
                }
            }
            else {
                final boolean notEmpty;

                synchronized (results) {
                    // clear the list if necessary
                    notEmpty = !results.isEmpty();
                    if (notEmpty)
                        results.clear();
                }

                // avoid death lock by sending event after synchronization
                if (notEmpty)
                    consumer.resultsChanged(SearchResultProducer.this);
            }

            // search completed (do it after searching set to false)
            consumer.searchCompleted(SearchResultProducer.this);
        }
    }

    public static class SearchWord {
        public final String word;
        public final boolean mandatory;
        public final boolean reject;

        /**
         * Constructor
         */
        public SearchWord(final String word) {
            super();

            if (word.startsWith("+")) {
                mandatory = true;
                reject = false;
                if (word.length() > 1)
                    this.word = word.substring(1);
                else
                    this.word = "";
            }
            else if (word.startsWith("-")) {
                mandatory = false;
                reject = true;
                if (word.length() > 1)
                    this.word = word.substring(1);
                else
                    this.word = "";
            }
            else {
                mandatory = false;
                reject = false;
                this.word = word;
            }
        }

        /**
         * @return empty state
         */
        public boolean isEmpty() {
            return StringUtil.isEmpty(word);
        }

        /**
         * @return length
         */
        public int length() {
            return word.length();
        }
    }

    /**
     * Take an input text and return a list of search/key word
     *
     * @param text
     *        input text
     * @return list of search work (key word)
     */
    public static List<SearchWord> getSearchWords(final String text) {
        final List<String> words = StringUtil.split(text);
        final List<SearchWord> result = new ArrayList<>();

        for (final String w : words) {
            final SearchWord sw = new SearchWord(w);
            if (!sw.isEmpty())
                result.add(sw);
        }

        return result;
    }

    public static boolean getShortSearch(final List<SearchWord> words) {
        return (words.size() == 1) && (words.get(0).length() <= 2);
    }

    /**
     * Result list
     */
    protected List<SearchResult> results;

    /**
     * Internals
     */
    protected final SingleProcessor processor;

    public SearchResultProducer() {
        super();

        results = new ArrayList<>();
        processor = new SingleProcessor(true, this.getClass().getSimpleName());
    }

    /**
     * @return Returns the result producer order
     */
    public int getOrder() {
        // default
        return 10;
    }

    /**
     * @return Returns the result producer name
     */
    public abstract String getName();

    /**
     * @return Returns the tooltip displayed on the menu (in small under the label).
     */
    public String getTooltipText() {
        return "Click to run";
    }

    /**
     * @return Returns the result list
     */
    public List<SearchResult> getResults() {
        return results;
    }

    /**
     * Performs the search request (asynchronous), mostly build the search result list.<br>
     * Only one search request should be processed at one time so take care of waiting for previous
     * search request completion.
     *
     * @param text
     *        Search text, it can contains several words and use operators.<br>
     *        Examples:<br>
     *        <ul>
     *        <li><i>spot detector</i> : any of word should be present</li>
     *        <li><i>+spot +detector</i> : both words should be present</li>
     *        <li><i>"spot detector"</i> : the exact expression should be present</li>
     *        <li><i>+"spot detector" -tracking</i> : <i>spot detector</i> should be present and <i>tracking</i> absent</li>
     *        </ul>
     * @param consumer
     *        Search result consumer for this search request.<br>
     *        The consumer should be notified of new results by using the
     *        {@link SearchResultConsumer#resultsChanged(SearchResultProducer)} method.
     */
    public void search(final String text, final SearchResultConsumer consumer) {
        processor.submit(new SearchRunner(text, consumer));
    }

    /**
     * Performs the search request (internal).<br>
     * The method is responsible for filling the <code>results</code> list :<br>
     * - If no result correspond to the requested search then <code>results</code> should be
     * cleared.<br>
     * - otherwise it should contains the founds results.<br>
     * <code>results</code> variable access should be synchronized as it can be externally accessed.<br>
     * The method could return earlier if {@link #hasWaitingSearch()} returns true.
     *
     * @param text
     *        Search text, it can contains several words and use operators.<br>
     *        Examples:<br>
     *        <ul>
     *        <li><i>spot detector</i> : any of word should be present</li>
     *        <li><i>+spot +detector</i> : both words should be present</li>
     *        <li><i>"spot detector"</i> : the exact expression should be present</li>
     *        <li><i>+"spot detector" -tracking</i> : <i>spot detector</i> should be present and <i>tracking</i> absent</li>
     *        </ul>
     * @param consumer
     *        Search result consumer for this search request.<br>
     *        The consumer should be notified of new results by using the
     *        {@link SearchResultConsumer#resultsChanged(SearchResultProducer)} method.
     * @see #hasWaitingSearch()
     */
    public abstract void doSearch(String text, SearchResultConsumer consumer);

    /**
     * Wait for the search request to complete.
     */
    public void waitSearchComplete() {
        while (isSearching())
            ThreadUtil.sleep(1);
    }

    /**
     * @return Returns true if the search result producer is currently processing a search request.
     */
    public boolean isSearching() {
        return processor.isProcessing();
    }

    /**
     * @return Returns true if there is a waiting search pending.<br>
     *         This method should be called during search process to cancel it if another search is waiting.
     */
    public boolean hasWaitingSearch() {
        return processor.hasWaitingTasks();
    }

    /**
     * Add the SearchResult to the result list.
     *
     * @param result
     *        Result to add to the result list.
     * @param consumer
     *        If not null then consumer is notified about result change
     */
    public void addResult(final SearchResult result, final SearchResultConsumer consumer) {
        if (result != null) {
            synchronized (results) {
                results.add(result);
            }

            // notify change to consumer
            if (consumer != null)
                consumer.resultsChanged(this);
        }
    }

    @Override
    public int compareTo(final SearchResultProducer o) {
        // sort on order
        return getOrder() - o.getOrder();
    }
}
