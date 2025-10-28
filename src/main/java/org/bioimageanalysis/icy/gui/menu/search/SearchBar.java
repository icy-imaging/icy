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

package org.bioimageanalysis.icy.gui.menu.search;

import org.bioimageanalysis.icy.gui.component.field.IcyTextField;
import org.bioimageanalysis.icy.gui.component.icon.IcySVG;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.bioimageanalysis.icy.network.search.SearchEngine;
import org.bioimageanalysis.icy.network.search.SearchEngine.SearchEngineListener;
import org.bioimageanalysis.icy.network.search.SearchResult;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.jdesktop.swingx.painter.BusyPainter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Thomas Provoost
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class SearchBar extends IcyTextField implements SearchEngineListener {
    private static final int DELAY = 20;

    private static final int BUSY_PAINTER_SIZE = 15;
    private static final int BUSY_PAINTER_POINTS = 40;
    private static final int BUSY_PAINTER_TRAIL = 20;

    /** Internal search engine */
    final SearchEngine searchEngine;

    /**
     * GUI
     */
    final SearchResultPanel resultsPanel;
    private final Icon searchIcon;

    /**
     * Internals
     */
    private Timer busyPainterTimer;
    final BusyPainter busyPainter;
    int frame;
    boolean lastSearchingState;
    boolean initialized;

    public SearchBar() {
        super();

        initialized = false;

        searchEngine = new SearchEngine();
        searchEngine.addListener(this);

        resultsPanel = new SearchResultPanel(this);
        searchIcon = new IcySVG(SVGResource.SEARCH).getIcon(16);

        // modify margin so we have space for icon
        final Insets margin = getMargin();
        setMargin(new Insets(margin.top, margin.left, margin.bottom, margin.right + 20));

        // focusable only when hit Ctrl + F or clicked at the beginning
        setFocusable(false);

        // SET THE BUSY PAINTER
        busyPainter = new BusyPainter(BUSY_PAINTER_SIZE);
        busyPainter.setFrame(0);
        busyPainter.setPoints(BUSY_PAINTER_POINTS);
        busyPainter.setTrailLength(BUSY_PAINTER_TRAIL);
        busyPainter.setPointShape(new Rectangle2D.Float(0, 0, 2, 1));
        frame = 0;

        lastSearchingState = false;
        busyPainterTimer = new Timer("Search animation timer");

        // ADD LISTENERS
        addTextChangeListener((source, validate) -> searchInternal(getText()));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                setFocus();
            }
        });

        addFocusListener(new FocusListener() {
            @Override
            public void focusLost(final FocusEvent e) {
                removeFocus();
            }

            @Override
            public void focusGained(final FocusEvent e) {
                searchInternal(getText());
            }
        });

        // global key listener to catch Ctrl+F in every case (not elegant)
        // getToolkit().addAWTEventListener(new AWTEventListener()
        // {
        // @Override
        // public void eventDispatched(AWTEvent event)
        // {
        // if (event instanceof KeyEvent)
        // {
        // final KeyEvent key = (KeyEvent) event;
        //
        // if (key.getID() == KeyEvent.KEY_PRESSED)
        // {
        // // Handle key presses
        // switch (key.getKeyCode())
        // {
        // case KeyEvent.VK_F:
        // if (EventUtil.isControlDown(key))
        // {
        // setFocus();
        // key.consume();
        // }
        // break;
        // }
        // }
        // }
        // }
        // }, AWTEvent.KEY_EVENT_MASK);

        // global mouse listener to simulate focus lost (not elegant)
        getToolkit().addAWTEventListener(event -> {
            if (!initialized || !hasFocus())
                return;

            if (event instanceof final MouseEvent evt) {
                if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
                    final Point pt = evt.getLocationOnScreen();

                    // user clicked outside search panel --> close it
                    if (!isInsideSearchComponents(pt))
                        removeFocus();
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);

        buildActionMap();

        initialized = true;
    }

    void buildActionMap() {
        final InputMap imap = getInputMap(JComponent.WHEN_FOCUSED);
        final ActionMap amap = getActionMap();

        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "MoveDown");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "MoveUp");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Execute");

        amap.put("Cancel", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (initialized)
                    cancelSearch();
            }
        });
        getActionMap().put("MoveDown", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (initialized)
                    moveDown();
            }
        });
        getActionMap().put("MoveUp", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (initialized)
                    moveUp();
            }
        });
        getActionMap().put("Execute", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (initialized)
                    execute();
            }
        });
    }

    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    protected boolean isInsideSearchComponents(final Point pt) {
        final Rectangle bounds = new Rectangle();

        bounds.setLocation(getLocationOnScreen());
        bounds.setSize(getSize());

        if (bounds.contains(pt))
            return true;

        if (initialized) {
            if (resultsPanel.isVisible()) {
                bounds.setLocation(resultsPanel.getLocationOnScreen());
                bounds.setSize(resultsPanel.getSize());

                return bounds.contains(pt);
            }
        }

        return false;
    }

    public void setFocus() {
        if (!hasFocus()) {
            setFocusable(true);
            requestFocus();
        }
    }

    public void removeFocus() {
        if (initialized) {
            resultsPanel.close(true);
            setFocusable(false);
        }
    }

    public void cancelSearch() {
        setText("");
    }

    // public void search(String text)
    // {
    // final String filter = text.trim();
    //
    // if (StringUtil.isEmpty(filter))
    // searchEngine.cancelSearch();
    // else
    // searchEngine.search(filter);
    // }
    //

    /**
     * Request search for the specified text.
     * @param text string
     *
     * @see SearchEngine#search(String)
     */
    public void search(final String text) {
        setText(text);
    }

    protected void searchInternal(final @NotNull String text) {
        final String filter = text.trim();

        if (StringUtil.isEmpty(filter))
            searchEngine.cancelSearch();
        else
            searchEngine.search(filter);
    }

    protected void execute() {
        // result displayed --> launch selected result
        if (resultsPanel.isShowing())
            resultsPanel.executeSelected();
        else
            searchInternal(getText());
    }

    protected void moveDown() {
        resultsPanel.moveSelection(1);
    }

    protected void moveUp() {
        resultsPanel.moveSelection(-1);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        final Graphics2D g2 = (Graphics2D) g.create();
        final int w = getWidth();
        final int h = getHeight();

        // set rendering presets
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        if (StringUtil.isEmpty(getText()) && !hasFocus()) {
            // draw "Search" if no focus
            final Insets insets = getMargin();
            final Color fg = getForeground();

            g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 100));
            g2.drawString("Search", insets.left + 2, h - g2.getFontMetrics().getHeight() / 2 + 2);
        }

        if (searchEngine.isSearching()) {
            // draw loading icon
            g2.translate(w - (BUSY_PAINTER_SIZE + 5), 3);
            busyPainter.paint(g2, this, BUSY_PAINTER_SIZE, BUSY_PAINTER_SIZE);
        }
        else {
            // draw search icon
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            searchIcon.paintIcon(this, g2, w - h, 2);
        }

        g2.dispose();
    }

    @Override
    public void resultChanged(final SearchEngine source, final SearchResult result) {
        if (initialized)
            resultsPanel.resultChanged(result);
    }

    @Override
    public void resultsChanged(final SearchEngine source) {
        if (initialized)
            resultsPanel.resultsChanged();
    }

    @Override
    public void searchStarted(final SearchEngine source) {
        if (!initialized)
            return;

        // make sure the animation timer for the busy icon is stopped
        busyPainterTimer.cancel();

        // ... and restart it
        final Timer newTimer = new Timer("Search animation timer");
        newTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                frame = (frame + 1) % BUSY_PAINTER_POINTS;
                busyPainter.setFrame(frame);

                final boolean searching = searchEngine.isSearching();

                // this permit to get rid of the small delay between the searchCompleted
                // event and when isSearching() actually returns false
                if (searching || (searching != lastSearchingState))
                    repaint();

                lastSearchingState = searching;
            }
        }, DELAY, DELAY);
        busyPainterTimer = newTimer;

        // for the busy loop animation
        repaint();
    }

    @Override
    public void searchCompleted(final SearchEngine source) {
        // stop the animation timer for the rotating busy icon
        busyPainterTimer.cancel();

        // for the busy loop animation
        repaint();
    }
}
