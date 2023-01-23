package icy.resource.icon;

import icy.gui.util.LookAndFeelUtil;
import jiconfont.IconCode;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;

public class IcyIconFont implements Icon {
    public enum State {
        FOREGROUND,
        DISABLED,
        SELECTED
    }

    final IconCode ic;
    float size;
    State state;

    Icon internalIcon;

    public IcyIconFont(final IconCode iconCode, final float size, final State state) {
        this.ic = iconCode;
        this.size = size;
        this.state = state;

        updateIcon();
    }

    public IcyIconFont(final IconCode iconCode, final State state) {
        this.ic = iconCode;
        this.size = LookAndFeelUtil.getDefaultIconSize();
        this.state = state;

        updateIcon();
    }

    private Color getColor(State state) {
        Color color;
        switch (state) {
            default:
            case FOREGROUND:
                color = LookAndFeelUtil.getForeground();
                break;
            case DISABLED:
                color = LookAndFeelUtil.getDisabledForeground();
                break;
            case SELECTED:
                color = LookAndFeelUtil.getSelectionForeground();
                break;
        }
        return color;
    }

    public IconCode getIconCode() {
        return ic;
    }

    public void updateIcon(final float size, final State state) {
        this.size = size;
        this.state = state;
        updateIcon();
    }

    public void updateIcon(final float size) {
        this.size = size;
        updateIcon();
    }

    public void updateIcon(final State state) {
        this.state = state;
        updateIcon();
    }

    public void updateIcon() {
        internalIcon = IconFontSwing.buildIcon(ic, size, getColor(state));
    }

    @Override
    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
        internalIcon.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return internalIcon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return internalIcon.getIconHeight();
    }
}
