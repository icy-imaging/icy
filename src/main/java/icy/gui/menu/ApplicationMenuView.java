package icy.gui.menu;

import com.formdev.flatlaf.FlatLaf;
import icy.action.GeneralActions;
import icy.action.WindowActions;
import icy.gui.util.LookAndFeelUtil;
import icy.main.Icy;
import jiconfont.IconCode;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import javax.swing.*;
import java.util.List;

public final class ApplicationMenuView extends AbstractApplicationMenu {
    private final JMenu menuAppearance;
    private final JMenu menuOrganizeWindows;

    private final JCheckBoxMenuItem checkboxitemDetachedMode;
    private final JCheckBoxMenuItem checkboxitemStayOnTop;
    private final JMenuItem itemSwimmingPoolViewer;
    private final JMenuItem itemOrganizeGrid;
    private final JMenuItem itemOrganizeHorizontal;
    private final JMenuItem itemOrganizeVertical;
    private final JMenuItem itemOrganizeCascade;

    public ApplicationMenuView() {
        super("View");

        menuAppearance = new JMenu("Appearance");
        setIcon(menuAppearance, GoogleMaterialDesignIcons.BRIGHTNESS_MEDIUM);
        add(menuAppearance);

        final List<FlatLaf> skins = LookAndFeelUtil.getSkins();
        final String skinName = LookAndFeelUtil.getCurrentSkinName();
        final ButtonGroup groupSkin = new ButtonGroup();
        for (final FlatLaf skin : skins) {
            final JRadioButtonMenuItem itemLaf = new JRadioButtonMenuItem(skin.getName());
            final IconCode iconCode = (skin.isDark()) ? GoogleMaterialDesignIcons.BRIGHTNESS_2 : GoogleMaterialDesignIcons.WB_SUNNY;
            setIcon(itemLaf, iconCode);
            if (skinName.equals(skin.getName()))
                itemLaf.setSelected(true);
            itemLaf.addActionListener(e -> LookAndFeelUtil.setSkin(skin));
            groupSkin.add(itemLaf);
            menuAppearance.add(itemLaf);
        }

        addSeparator();

        checkboxitemDetachedMode = new JCheckBoxMenuItem("Detached Mode");
        setIcon(checkboxitemDetachedMode, GoogleMaterialDesignIcons.DASHBOARD);
        checkboxitemDetachedMode.setSelected(Icy.getMainInterface().isDetachedMode());
        checkboxitemDetachedMode.addActionListener(GeneralActions.detachedModeAction);
        add(checkboxitemDetachedMode);

        checkboxitemStayOnTop = new JCheckBoxMenuItem("Stay on Top");
        setIcon(checkboxitemStayOnTop, GoogleMaterialDesignIcons.VERTICAL_ALIGN_TOP);
        checkboxitemStayOnTop.setSelected(Icy.getMainInterface().isAlwaysOnTop());
        checkboxitemStayOnTop.addActionListener(WindowActions.stayOnTopAction);
        add(checkboxitemStayOnTop);

        addSeparator();

        itemSwimmingPoolViewer = new JMenuItem("Swimming Pool Viewer...");
        setIcon(itemSwimmingPoolViewer, GoogleMaterialDesignIcons.GROUP_WORK);
        itemSwimmingPoolViewer.addActionListener(WindowActions.swimmingPoolAction);
        add(itemSwimmingPoolViewer);

        addSeparator();

        menuOrganizeWindows = new JMenu("Organize Windows");
        add(menuOrganizeWindows);

        itemOrganizeGrid = new JMenuItem("Grid View");
        setIcon(itemOrganizeGrid, GoogleMaterialDesignIcons.VIEW_MODULE);
        itemOrganizeGrid.setAccelerator(KeyStroke.getKeyStroke("shift G"));
        itemOrganizeGrid.addActionListener(WindowActions.gridTileAction);
        menuOrganizeWindows.add(itemOrganizeGrid);

        itemOrganizeHorizontal = new JMenuItem("Horizontal View");
        setIcon(itemOrganizeHorizontal, GoogleMaterialDesignIcons.VIEW_AGENDA);
        itemOrganizeHorizontal.setAccelerator(KeyStroke.getKeyStroke("shift H"));
        itemOrganizeHorizontal.addActionListener(WindowActions.horizontalTileAction);
        menuOrganizeWindows.add(itemOrganizeHorizontal);

        itemOrganizeVertical = new JMenuItem("Vertical View");
        setIcon(itemOrganizeVertical, GoogleMaterialDesignIcons.VIEW_COLUMN);
        itemOrganizeVertical.setAccelerator(KeyStroke.getKeyStroke("shift V"));
        itemOrganizeVertical.addActionListener(WindowActions.verticalTileAction);
        menuOrganizeWindows.add(itemOrganizeVertical);

        itemOrganizeCascade = new JMenuItem("Cascade View");
        setIcon(itemOrganizeCascade, GoogleMaterialDesignIcons.VIEW_QUILT);
        itemOrganizeCascade.addActionListener(WindowActions.cascadeAction);
        add(itemOrganizeCascade);

        addSkinChangeListener();
    }
}
