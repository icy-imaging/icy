/*
 * Copyright (c) 2010-2025. Institut Pasteur.
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

import org.bioimageanalysis.icy.extension.ExtensionLoader;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginSearchProvider;
import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SearchEngine for Icy.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class SearchEngine implements SearchResultConsumer, ExtensionLoader.ExtensionLoaderListener {
    public interface SearchEngineListener {
        void resultChanged(SearchEngine source, SearchResult result);

        void resultsChanged(SearchEngine source);

        void searchStarted(SearchEngine source);

        void searchCompleted(SearchEngine source);
    }

    /**
     * Search result producer list
     */
    final ArrayList<SearchResultProducer> producers;

    /**
     * Listener list
     */
    private final List<SearchEngineListener> listeners;

    /**
     * Internals
     */
    final Runnable searchProviderSetter;
    String lastSearch;

    public SearchEngine() {
        super();

        producers = new ArrayList<>();
        listeners = new ArrayList<>();
        lastSearch = "";

        searchProviderSetter = new Runnable() {
            @Override
            public void run() {
                final String savedSearch = lastSearch;

                // cancel current search
                cancelSearch();

                synchronized (producers) {
                    producers.clear();
                }

                // get search providers from plugin
                for (final PluginDescriptor plugin : ExtensionLoader.getPlugins(PluginSearchProvider.class)) {
                    try {
                        final PluginSearchProvider psp = (PluginSearchProvider) plugin.getPluginClass().getDeclaredConstructor().newInstance();
                        final SearchResultProducer producer = psp.getSearchProviderClass().getDeclaredConstructor().newInstance();

                        synchronized (producers) {
                            producers.add(producer);
                        }
                    }
                    catch (final Throwable t) {
                        IcyExceptionHandler.handleException(plugin, t, true);
                    }
                }

                synchronized (producers) {
                    Collections.sort(producers);
                }

                // restore last search
                search(savedSearch);
            }
        };

        ExtensionLoader.addListener(this);

        updateSearchProducers();
    }

    private void updateSearchProducers() {
        ThreadUtil.runSingle(searchProviderSetter);
    }

    /**
     * Cancel the previous search request
     */
    public void cancelSearch() {
        search("");
    }

    /**
     * Performs the search request, mostly build the search result list.<br>
     * Previous search is automatically canceled and replaced by the new one.
     *
     * @param text Text used for the search request, it can contains several words and use operators.<br>
     *             Examples:<br>
     *             <ul>
     *             <li><i>spot detector</i> : any of word should be present</li>
     *             <li><i>+spot +detector</i> : both words should be present</li>
     *             <li><i>"spot detector"</i> : the exact expression should be present</li>
     *             <li><i>+"spot detector" -tracking</i> : <i>spot detector</i> should be present and <i>tracking</i> absent</li>
     *             </ul>
     * @see #cancelSearch()
     */
    public void search(final String text) {
        // save search string
        lastSearch = text;

        // notify search started
        fireSearchStartedEvent();

        // launch new search
        synchronized (producers) {
            for (final SearchResultProducer producer : producers)
                producer.search(text, this);
        }
    }

    /**
     * Returns {@link SearchResultProducer} attached to the search engine.
     */
    public List<SearchResultProducer> getSearchResultProducers() {
        synchronized (producers) {
            return new ArrayList<>(producers);
        }
    }

    /**
     * Returns the number of currently producer processing a search request.
     */
    public int getSearchingProducerCount() {
        int result = 0;

        synchronized (producers) {
            for (final SearchResultProducer producer : producers)
                if (producer.isSearching())
                    result++;
        }

        return result;
    }

    /**
     * Returns true if the search engine is currently processing a search request.
     */
    public boolean isSearching() {
        synchronized (producers) {
            for (final SearchResultProducer producer : producers)
                if (producer.isSearching())
                    return true;
        }

        return false;
    }

    // /**
    // * Set the list of provider classes.
    // *
    // * @param providers
    // * : list of provider.
    // */
    // public void setProducer(List<SearchResultProducer> providers)
    // {
    // synchronized (producers)
    // {
    // producers.clear();
    // producers.addAll(providers);
    // }
    // }
    //
    // /**
    // * This method will register the provider class into the list of provider
    // * classes. The {@link SearchResultProducer} object will not be used except for its
    // * class.
    // *
    // * @param providerClass
    // * : provider used to get the Class<?> from.
    // */
    // public void addProducer(Class<? extends SearchResultProducer> providerClass)
    // {
    // if (!providerClasses.contains(providerClass))
    // providerClasses.add(providerClass);
    // }
    //
    // /**
    // * This method will unregister the provider class from the list of provider
    // * class.
    // *
    // * @param providerClass
    // * : provider used to get the Class<?> from.
    // */
    // public void removeProducer(Class<? extends SearchResultProducer> providerClass)
    // {
    // providerClasses.remove(providerClass);
    // }

    /**
     * Returns the last search text.
     */
    public String getLastSearch() {
        return lastSearch;
    }

    /**
     * Returns SearchResult at specified index.
     */
    public SearchResult getResult(final int index) {
        final List<SearchResult> results = getResults();

        if ((index >= 0) && (index < results.size()))
            return results.get(index);

        return null;
    }

    /**
     * Return all current results from all {@link SearchResultProducer}.
     */
    public List<SearchResult> getResults() {
        final List<SearchResult> results = new ArrayList<SearchResult>();

        synchronized (producers) {
            for (final SearchResultProducer producer : producers) {
                final List<SearchResult> producerResults = producer.getResults();

                // prevent modification of results while adding it
                synchronized (producerResults) {
                    // sort producer results
                    Collections.sort(producerResults);
                    // and add
                    results.addAll(producerResults);
                }
            }
        }

        return results;
    }

    @Override
    public void extensionLoaderChanged(final ExtensionLoader.ExtensionLoaderEvent e) {
        // refresh producer list
        updateSearchProducers();
    }

    @Override
    public void resultChanged(final SearchResultProducer producer, final SearchResult result) {
        // notify listeners about results change
        fireResultChangedEvent(result);
    }

    @Override
    public void resultsChanged(final SearchResultProducer producer) {
        // notify listeners about results change
        fireResultsChangedEvent();
    }

    @Override
    public void searchCompleted(final SearchResultProducer producer) {
        // last producer search completed ? --> notify listeners about it
        if (getSearchingProducerCount() == 1)
            fireSearchCompletedEvent();
    }

    public List<SearchEngineListener> getListeners() {
        synchronized (listeners) {
            return new ArrayList<SearchEngineListener>(listeners);
        }
    }

    public void addListener(final SearchEngineListener listener) {
        synchronized (listener) {
            if (!listeners.contains(listener))
                listeners.add(listener);
        }
    }

    public void removeListener(final SearchEngineListener listener) {
        synchronized (listener) {
            listeners.remove(listener);
        }
    }

    protected void fireResultChangedEvent(final SearchResult result) {
        for (final SearchEngineListener listener : getListeners())
            listener.resultChanged(this, result);
    }

    protected void fireResultsChangedEvent() {
        for (final SearchEngineListener listener : getListeners())
            listener.resultsChanged(this);
    }

    protected void fireSearchStartedEvent() {
        for (final SearchEngineListener listener : getListeners())
            listener.searchStarted(this);
    }

    protected void fireSearchCompletedEvent() {
        for (final SearchEngineListener listener : getListeners())
            listener.searchCompleted(this);
    }
}
