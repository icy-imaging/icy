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

package org.bioimageanalysis.icy.gui.component.field;

import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.gui.component.icon.IcySVGIcon;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author Thomas Musset
 */
public class IcyTextFieldHint extends JTextField implements FocusListener {
    private final IcySVGIcon icon;
    private final String hint;
    private final Insets insets;

    public IcyTextFieldHint(final @NotNull SVGIcon icon, final @NotNull String hint) {
        super();
        this.icon = new IcySVGIcon(icon, LookAndFeelUtil.ColorType.UI_BUTTON_DEFAULT);
        this.hint = hint;

        final Border border = UIManager.getBorder("TextField.border");
        insets = border.getBorderInsets(this);

        addFocusListener(this);
    }

    public IcyTextFieldHint(final @NotNull SVGIcon icon) {
        super();
        this.icon = new IcySVGIcon(icon, LookAndFeelUtil.ColorType.UI_BUTTON_DEFAULT);
        this.hint = "";

        final Border border = UIManager.getBorder("TextField.border");
        insets = border.getBorderInsets(this);

        addFocusListener(this);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        int textX = 2;

        if (icon != null) {
            final int iconWidth = icon.getIconWidth();
            final int iconHeight = icon.getIconHeight();
            final int x = insets.left;
            textX = x + iconWidth + 2;
            final int y = (this.getHeight() - iconHeight) / 2;
            icon.paintIcon(this, g, x, y);
        }

        setMargin(new Insets(2, textX, 2, 2));

        if (getText().isEmpty()) {
            final int height = this.getHeight();
            final Font prev = g.getFont();
            final Font italic = prev.deriveFont(Font.ITALIC);
            final Color prevColor = g.getColor();
            g.setFont(italic);
            g.setColor(UIManager.getColor("textInactiveText"));
            final int h = g.getFontMetrics().getHeight();
            final int textBottom = (height - h) / 2 + h - 4;
            final int x = this.getInsets().left;
            final Graphics2D g2d = (Graphics2D) g;
            final RenderingHints hints = g2d.getRenderingHints();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.drawString(hint, x, textBottom);
            g2d.setRenderingHints(hints);
            g.setFont(prev);
            g.setColor(prevColor);
        }
    }

    @Override
    public void focusGained(final FocusEvent e) {
        repaint();
    }

    @Override
    public void focusLost(final FocusEvent e) {
        repaint();
    }
}
