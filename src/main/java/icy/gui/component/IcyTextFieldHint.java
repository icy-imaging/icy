package icy.gui.component;

import icy.gui.util.LookAndFeelUtil;
import icy.resource.icon.IcyIconFont;
import jiconfont.IconCode;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class IcyTextFieldHint extends JTextField implements FocusListener {
    private final IcyIconFont icon;
    private final String hint;
    private final Insets insets;

    public IcyTextFieldHint(final IconCode code, final String hint) {
        super();
        this.icon = new IcyIconFont(code, LookAndFeelUtil.getDefaultIconSizeAsFloat(), LookAndFeelUtil.ColorType.UI_BUTTON_DEFAULT);
        this.hint = hint;

        final Border border = UIManager.getBorder("TextField.border");
        insets = border.getBorderInsets(this);

        addFocusListener(this);
    }

    public void updateIconFont() {
        final IcyIconFont i = getIcon();
        if (i != null)
            i.updateIcon();
    }

    public IcyIconFont getIcon() {
        return icon;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        updateIconFont();
        repaint();
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        int textX = 2;

        if(icon!=null) {
            final int iconWidth = icon.getIconWidth();
            final int iconHeight = icon.getIconHeight();
            final int x = insets.left;
            textX = x+iconWidth+2;
            final int y = (this.getHeight() - iconHeight) / 2;
            icon.paintIcon(this, g, x, y);
        }

        setMargin(new Insets(2, textX, 2, 2));

        if (getText().equals("")) {
            //int width = this.getWidth();
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
