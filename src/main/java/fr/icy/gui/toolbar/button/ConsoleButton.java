/*
 * Copyright (c) 2010-2026. Institut Pasteur.
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

package fr.icy.gui.toolbar.button;

import fr.icy.gui.component.button.IcyToggleButton;
import fr.icy.gui.component.icon.IcySVG;
import fr.icy.gui.component.icon.IcySVGIcon;
import fr.icy.gui.toolbar.container.StatusPanel;
import fr.icy.shared.logging.CustomLevel;
import fr.icy.shared.logging.LogManager;
import fr.icy.shared.logging.formatter.ConsoleFormatter;
import fr.icy.shared.logging.gui.ConsolePanel;
import fr.icy.shared.logging.gui.ConsolePanelImpl;
import fr.icy.shared.logging.handler.GUIConsoleHandler;
import org.jetbrains.annotations.Contract;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Thomas Musset
 */
public final class ConsoleButton extends IcyToggleButton implements ActionListener {
    //private final OutputConsolePanel consolePanel = OutputConsolePanel.getInstance();
    private final ConsolePanel consolePanel = new ConsolePanelImpl();

    public ConsoleButton() {
        super(IcySVG.TERMINAL);

        final Handler bttnHandler = new Handler() {
            @Override
            public void publish(final LogRecord record) {
                if (record == null || !isLoggable(record)) return;
                if (record.getLevel().intValue() >= Level.SEVERE.intValue())
                    setBadge(IcySVGIcon.ERROR);
                else if (record.getLevel().intValue() >= Level.WARNING.intValue())
                    setBadge(IcySVGIcon.WARNING);
            }

            @Contract(pure = true)
            @Override
            public void flush() {

            }

            @Contract(pure = true)
            @Override
            public void close() {

            }
        };

        setFocusable(false);
        addActionListener(this);

        final GUIConsoleHandler handler = new GUIConsoleHandler(consolePanel);
        LogManager.addHandler(handler, new ConsoleFormatter(), Level.CONFIG, null);
        LogManager.addHandler(bttnHandler, new ConsoleFormatter(), CustomLevel.WARNING, null);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final StatusPanel panel = StatusPanel.getInstance();
        if (isSelected())
            panel.show(consolePanel);
        else
            panel.close();
    }
}
