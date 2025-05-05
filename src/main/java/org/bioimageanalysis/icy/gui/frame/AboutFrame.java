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
package org.bioimageanalysis.icy.gui.frame;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;

public class AboutFrame extends IcyFrame
{
    final JTabbedPane tabbedPane;
    final JEditorPane aboutEditorPane;
    final JEditorPane changeLogEditorPane;
    final JEditorPane licenseEditorPane;

    public AboutFrame(int defaultTab)
    {
        super("About ICY", true, true, true, false);

        aboutEditorPane = new JEditorPane("text/html", "");
        aboutEditorPane.setEditable(false);
        aboutEditorPane.setCaretPosition(0);

        changeLogEditorPane = new JEditorPane("text/html", "");
        changeLogEditorPane.setEditable(false);
        changeLogEditorPane.setCaretPosition(0);

        licenseEditorPane = new JEditorPane("text/html", "");
        licenseEditorPane.setEditable(false);
        licenseEditorPane.setCaretPosition(0);

        tabbedPane = new JTabbedPane();
        tabbedPane.add("About", new JScrollPane(aboutEditorPane));
        tabbedPane.add("ChangeLog", new JScrollPane(changeLogEditorPane));
        tabbedPane.add("License", new JScrollPane(licenseEditorPane));

        // select the default tab
        tabbedPane.setSelectedIndex(defaultTab);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        add(tabbedPane);
        setSize(680, 480);
        setVisible(true);
        addToDesktopPane();
        center();
        requestFocus();

        loadInfos();
    }

    private void loadInfos()
    {
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                final String about = "<html><pre>" + Icy.getReadMe() + "</pre></html>";
                final String changelog = "<html><pre>" + Icy.getChangeLog() + "</pre></html>";
                final String license = "<html><pre>" + Icy.getLicense() + "</pre></html>";

                aboutEditorPane.setText(about);
                changeLogEditorPane.setText(changelog);
                licenseEditorPane.setText(license);
            }
        });
    }

    public AboutFrame()
    {
        this(0);
    }
}
