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

package icy.gui.viewer;

import icy.gui.component.IcySlider;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.util.ComponentUtil;
import icy.gui.util.GuiUtil;
import icy.resource.icon.SVGIcon;
import icy.resource.icon.SVGIconPack;
import icy.system.thread.ThreadUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class TNavigationPanel extends JPanel {
    private static final int DEFAULT_FRAME_RATE = 15;

    final JSlider slider;
    final JLabel leftLabel;
    final JLabel rightLabel;

    final IcyToggleButton play;
    final IcyToggleButton loop;
    final JSpinner frameRate;

    final Timer timer;

    public TNavigationPanel() {
        super(true);

        slider = new IcySlider(SwingConstants.HORIZONTAL);
        slider.setFocusable(false);
        slider.setMaximum(0);
        slider.setMinimum(0);
        slider.setToolTipText("Move cursor or use left/right key to navigate in T dimension");
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                rightLabel.setText(Integer.toString(slider.getMaximum()));
                leftLabel.setText(Integer.toString(slider.getValue()));
                validate();
            }
        });

        ComponentUtil.setFixedHeight(slider, 22);

        timer = new Timer(1000 / DEFAULT_FRAME_RATE, e -> {
            // only if slider is not adjusting T position
            if (!slider.getValueIsAdjusting()) {
                final int oldT = getTPosition();

                incTPosition();

                // end reached ?
                if (oldT == getTPosition()) {
                    // loop mode --> reset
                    if (isRepeat())
                        setTPosition(0);
                    else {
                        // end play
                        stopPlay();
                        // and reset position
                        setTPosition(0);
                    }
                }
            }
        });

        play = new IcyToggleButton(new SVGIconPack(SVGIcon.PLAY_CIRCLE, SVGIcon.STOP_CIRCLE));
        //play.setFlat(true);
        play.setToolTipText("play");
        play.addActionListener(e -> {
            if (isPlaying())
                stopPlay();
            else
                startPlay();
        });

        loop = new IcyToggleButton(SVGIcon.REPEAT);
        //loop.setFlat(true);
        loop.setToolTipText("Enable loop playback");
        loop.addActionListener(e -> setRepeat(!isRepeat()));
        // default
        setRepeat(true);

        frameRate = new JSpinner(new SpinnerNumberModel(DEFAULT_FRAME_RATE, 1, 60, 1));
        frameRate.setFocusable(false);
        // no manual edition and edition focus
        final JTextField tf = ((JSpinner.DefaultEditor) frameRate.getEditor()).getTextField();
        tf.setEditable(false);
        tf.setFocusable(false);
        frameRate.setToolTipText("Change playback frame rate");
        frameRate.addChangeListener(e -> {
            final int f = ((Integer) frameRate.getValue()).intValue();
            // adjust timer delay
            setTimerDelay(1000 / f);
        });
        ComponentUtil.setFixedSize(frameRate, new Dimension(50, 22));

        leftLabel = new JLabel("0");
        leftLabel.setToolTipText("T position");
        rightLabel = new JLabel("0");
        rightLabel.setToolTipText("T sequence size");

        final JPanel leftPanel = GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(8), GuiUtil.createBoldLabel("T"),
                Box.createHorizontalStrut(10), leftLabel);

        rightLabel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                // so left label is adjusted to right label size
                ComponentUtil.setFixedWidth(leftLabel, rightLabel.getWidth());
                leftPanel.revalidate();
            }
        });

        final JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.LINE_AXIS));

        final JLabel frameRateLabel = new JLabel("FPS");
        frameRateLabel.setToolTipText("Frames Per Second");

        rightPanel.add(rightLabel);
        rightPanel.add(Box.createHorizontalStrut(12));
        rightPanel.add(play);
        rightPanel.add(loop);
        rightPanel.add(Box.createHorizontalStrut(8));
        rightPanel.add(frameRate);
        rightPanel.add(Box.createHorizontalStrut(4));
        rightPanel.add(frameRateLabel);
        rightPanel.add(Box.createHorizontalStrut(4));

        // setBorder(BorderFactory.createLineBorder(BorderFactory.createTitledBorder("").getTitleColor()));
        setBorder(BorderFactory.createTitledBorder("").getBorder());
        setLayout(new BorderLayout());

        add(leftPanel, BorderLayout.WEST);
        add(slider, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        validate();
    }

    protected void incTPosition() {
        setTPosition(getTPosition() + 1);
    }

    protected void decTPosition() {
        setTPosition(Math.max(0, getTPosition() - 1));
    }

    protected void setTimerDelay(final int delay) {
        timer.setDelay(delay);
    }

    protected int getTPosition() {
        return slider.getValue();
    }

    protected void setTPosition(final int t) {
        // we want to be sure the T position is changed in EDT
        ThreadUtil.invokeNow(() -> slider.setValue(t));
    }

    /**
     * Returns the frame rate (given in frame per second) for play command.
     */
    public int getFrameRate() {
        return ((Integer) frameRate.getValue()).intValue();
    }

    /**
     * Sets the frame rate (given in frame per second) for play command.
     */
    public void setFrameRate(final int fps) {
        frameRate.setValue(Integer.valueOf(fps));
    }

    /**
     * Returns true if <code>repeat</code> is enabled for play command.
     */
    public boolean isRepeat() {
        return loop.getToolTipText().startsWith("Disable");
    }

    /**
     * Set <code>repeat</code> mode for play command.
     */
    public void setRepeat(final boolean value) {
        if (value) {
            //loop.setIcon(new IcyIcon(ResourceUtil.ICON_RELOAD, 16));
            loop.setSelected(true);
            loop.setToolTipText("Disable loop playback");
        }
        else {
            //loop.setIcon(new IcyIcon(ResourceUtil.ICON_ARROW_RIGHT, 16));
            loop.setSelected(false);
            loop.setToolTipText("Enable loop playback");
        }
        fireLoopingStateChange();
    }

    private final List<ActionListener> loopingStateChangeListeners = Collections.synchronizedList(new ArrayList<>());

    public void addLoopingStateChangeListener(final ActionListener actionListener) {
        loopingStateChangeListeners.add(actionListener);
    }

    public void removeLoopingStateChangeListener(final ActionListener actionListener) {
        loopingStateChangeListeners.remove(actionListener);
    }

    protected void fireLoopingStateChange() {
        for (final ActionListener l : loopingStateChangeListeners) {
            l.actionPerformed(null);
        }
    }

    /**
     * Returns true if currently playing.
     */
    public boolean isPlaying() {
        return timer.isRunning();
    }

    /**
     * Start sequence play.
     *
     * @see #stopPlay()
     * @see #setRepeat(boolean)
     */
    public void startPlay() {
        timer.start();
        //play.setIcon(new IcyIcon(ResourceUtil.ICON_PAUSE));
        play.setSelected(true);
        play.setToolTipText("pause");
    }

    /**
     * Stop sequence play.
     *
     * @see #startPlay()
     */
    public void stopPlay() {
        timer.stop();
        //play.setIcon(new IcyIcon(ResourceUtil.ICON_PLAY));
        play.setSelected(false);
        play.setToolTipText("play");
    }

    /**
     * @see icy.gui.component.IcySlider#setPaintLabels(boolean)
     */
    public void setPaintLabels(final boolean b) {
        slider.setPaintLabels(b);
    }

    /**
     * @see icy.gui.component.IcySlider#setPaintTicks(boolean)
     */
    public void setPaintTicks(final boolean b) {
        slider.setPaintTicks(b);
    }

    /**
     * @see JSlider#addChangeListener(ChangeListener)
     */
    public void addChangeListener(final ChangeListener l) {
        slider.addChangeListener(l);
    }

    /**
     * @see JSlider#removeChangeListener(ChangeListener)
     */
    public void removeChangeListener(final ChangeListener l) {
        slider.removeChangeListener(l);
    }

    /**
     * Remove all change listener
     */
    public void removeAllChangeListener() {
        for (final ChangeListener l : slider.getListeners(ChangeListener.class))
            slider.removeChangeListener(l);
    }

    /**
     * @see JSlider#getValue()
     */
    public int getValue() {
        return slider.getValue();
    }

    /**
     * @see JSlider#setValue(int)
     */
    public void setValue(final int n) {
        slider.setValue(n);
    }

    /**
     * @see JSlider#getMinimum()
     */
    public int getMinimum() {
        return slider.getMinimum();
    }

    /**
     * @see JSlider#setMinimum(int)
     */
    public void setMinimum(final int minimum) {
        slider.setMinimum(minimum);
    }

    /**
     * @see JSlider#getMaximum()
     */
    public int getMaximum() {
        return slider.getMaximum();
    }

    /**
     * @see JSlider#setMaximum(int)
     */
    public void setMaximum(final int maximum) {
        slider.setMaximum(maximum);
    }

    /**
     * @see JSlider#getPaintTicks()
     */
    public boolean getPaintTicks() {
        return slider.getPaintTicks();
    }

    /**
     * @see JSlider#getPaintTrack()
     */
    public boolean getPaintTrack() {
        return slider.getPaintTrack();
    }

    /**
     * @see JSlider#setPaintTrack(boolean)
     */
    public void setPaintTrack(final boolean b) {
        slider.setPaintTrack(b);
    }

    /**
     * @see JSlider#getPaintLabels()
     */
    public boolean getPaintLabels() {
        return slider.getPaintLabels();
    }
}
