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

package fr.icy.gui.component.icon;

import fr.icy.Icy;
import fr.icy.gui.LookAndFeelUtil;
import fr.icy.io.ResourceUtil;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thomas Musset
 *
 * Wrapper for SVG images / icons.
 */
public final class IcySVG {
    private static final Logger LOGGER = Logger.getLogger(IcySVG.class.getName());

    private static final String MONO = ResourceUtil.SVG_ICON_PATH + "mono/";
    private static final String COLOR = ResourceUtil.SVG_ICON_PATH + "color/";

    // Monochrome icons
    public static final IcySVG AXIS_3D = new IcySVG(MONO + "3d_axis.svg", false);
    public static final IcySVG BOX_BOUNDS_3D = new IcySVG(MONO + "3d_box_bounds.svg", false);
    public static final IcySVG ADD = new IcySVG(MONO + "add.svg", false);
    public static final IcySVG ARROW_DOWN = new IcySVG(MONO + "arrow_down.svg", false);
    public static final IcySVG ARROW_LEFT = new IcySVG(MONO + "arrow_left.svg", false);
    public static final IcySVG ARROW_RIGHT = new IcySVG(MONO + "arrow_right.svg", false);
    public static final IcySVG ARROW_UP = new IcySVG(MONO + "arrow_up.svg", false);
    public static final IcySVG BOLT = new IcySVG(MONO + "bolt.svg", false);
    public static final IcySVG BRACKET_LEFT = new IcySVG(MONO + "bracket_left.svg", false);
    public static final IcySVG BRACKET_RIGHT = new IcySVG(MONO + "bracket_right.svg", false);
    public static final IcySVG BUG_REPORT = new IcySVG(MONO + "bug_report.svg", false);
    //public static final IcySVG CAMERA = new IcySVG(MONO + "camera.svg", false);
    //public static final IcySVG CAMERA_ROLL = new IcySVG(MONO + "camera_roll.svg", false);
    //public static final IcySVG CENTER_FOCUS_STRONG = new IcySVG(MONO + "center_focus_strong.svg", false);
    public static final IcySVG CHECK = new IcySVG(MONO + "check.svg", false);
    public static final IcySVG CHECK_CIRCLE = new IcySVG(MONO + "check_circle.svg", false);
    public static final IcySVG CIRCLE = new IcySVG(MONO + "circle.svg", false);
    public static final IcySVG CIRCLE_FILL = new IcySVG(MONO + "circle_fill.svg", false);
    public static final IcySVG CLEAR_ALL = new IcySVG(MONO + "clear_all.svg", false);
    public static final IcySVG CLOSE = new IcySVG(MONO + "close.svg", false);
    public static final IcySVG CONTENT_COPY = new IcySVG(MONO + "content_copy.svg", false);
    public static final IcySVG CONVERT_SHAPE = new IcySVG(MONO + "convert_shape.svg", false);
    public static final IcySVG CROP = new IcySVG(MONO + "crop.svg", false);
    public static final IcySVG CUBE_SLICE = new IcySVG(MONO + "cube_slice.svg", false);
    public static final IcySVG CUT = new IcySVG(MONO + "cut.svg", false);
    public static final IcySVG DASHBOARD = new IcySVG(MONO + "dashboard.svg", false);
    public static final IcySVG DATABASE = new IcySVG(MONO + "database.svg", false);
    public static final IcySVG DELETE = new IcySVG(MONO + "delete.svg", false);
    public static final IcySVG DELETE_SWEEP = new IcySVG(MONO + "delete_sweep.svg", false);
    public static final IcySVG DEPLOYED_CODE = new IcySVG(MONO + "deployed_code.svg", false);
    public static final IcySVG DESCRIPTION = new IcySVG(MONO + "description.svg", false);
    public static final IcySVG DEVICE_RESET = new IcySVG(MONO + "device_reset.svg", false);
    public static final IcySVG DRAG_PAN = new IcySVG(MONO + "drag_pan.svg", false);
    public static final IcySVG DRAW_ABSTRACT = new IcySVG(MONO + "draw_abstract.svg", false);
    public static final IcySVG EAST = new IcySVG(MONO + "east.svg", false);
    public static final IcySVG EDIT = new IcySVG(MONO + "edit.svg", false);
    public static final IcySVG ERROR = new IcySVG(MONO + "error.svg", false);
    public static final IcySVG EXPORT_NOTES = new IcySVG(MONO + "export_notes.svg", false);
    public static final IcySVG EXTENSION = new IcySVG(MONO + "extension.svg", false);
    public static final IcySVG FILE_OPEN = new IcySVG(MONO + "file_open.svg", false);
    public static final IcySVG FILE_SAVE = new IcySVG(MONO + "file_save.svg", false);
    public static final IcySVG FILTER_1 = new IcySVG(MONO + "filter_1.svg", false);
    public static final IcySVG FILTER_2 = new IcySVG(MONO + "filter_2.svg", false);
    public static final IcySVG FILTER_3 = new IcySVG(MONO + "filter_3.svg", false);
    public static final IcySVG FILTER_4 = new IcySVG(MONO + "filter_4.svg", false);
    public static final IcySVG FILTER_5 = new IcySVG(MONO + "filter_5.svg", false);
    public static final IcySVG FILTER_6 = new IcySVG(MONO + "filter_6.svg", false);
    public static final IcySVG FILTER_7 = new IcySVG(MONO + "filter_7.svg", false);
    public static final IcySVG FILTER_8 = new IcySVG(MONO + "filter_8.svg", false);
    public static final IcySVG FILTER_9 = new IcySVG(MONO + "filter_9.svg", false);
    public static final IcySVG FILTER_9_PLUS = new IcySVG(MONO + "filter_9_plus.svg", false);
    public static final IcySVG FILTER_NONE = new IcySVG(MONO + "filter_none.svg", false);
    public static final IcySVG FIRST_PAGE = new IcySVG(MONO + "first_page.svg", false);
    public static final IcySVG FLAG = new IcySVG(MONO + "flag.svg", false);
    public static final IcySVG FLASH_OFF = new IcySVG(MONO + "flash_off.svg", false);
    public static final IcySVG FLASH_ON = new IcySVG(MONO + "flash_on.svg", false);
    public static final IcySVG FOLDER = new IcySVG(MONO + "folder.svg", false);
    public static final IcySVG FOLDER_OPEN = new IcySVG(MONO + "folder_open.svg", false);
    public static final IcySVG FORMAT_ALIGN_CENTER = new IcySVG(MONO + "format_align_center.svg", false);
    public static final IcySVG FORMAT_ALIGN_JUSTIFY = new IcySVG(MONO + "format_align_justify.svg", false);
    public static final IcySVG FORMAT_ALIGN_LEFT = new IcySVG(MONO + "format_align_left.svg", false);
    public static final IcySVG FORMAT_ALIGN_RIGHT = new IcySVG(MONO + "format_align_right.svg", false);
    public static final IcySVG GESTURE_SELECT = new IcySVG(MONO + "gesture_select.svg", false);
    public static final IcySVG GRAIN = new IcySVG(MONO + "grain.svg", false);
    public static final IcySVG GRID_OFF = new IcySVG(MONO + "grid_off.svg", false);
    public static final IcySVG GRID_ON = new IcySVG(MONO + "grid_on.svg", false);
    public static final IcySVG GROUP_WORK = new IcySVG(MONO + "group_work.svg", false);
    public static final IcySVG HANDYMAN = new IcySVG(MONO + "handyman.svg", false);
    public static final IcySVG HELP = new IcySVG(MONO + "help.svg", false);
    public static final IcySVG HISTORY = new IcySVG(MONO + "history.svg", false);
    public static final IcySVG HOURGLASS = new IcySVG(MONO + "hourglass.svg", false);
    public static final IcySVG IMAGE = new IcySVG(MONO + "image.svg", false);
    public static final IcySVG IMAGE_ASPECT_RATIO = new IcySVG(MONO + "image_aspect_ratio.svg", false);
    public static final IcySVG IMAGE_BROKEN = new IcySVG(MONO + "image_broken.svg", false);
    public static final IcySVG IMAGE_RESIZE = new IcySVG(MONO + "image_resize.svg", false);
    public static final IcySVG INDETERMINATE_QUESTION = new IcySVG(MONO + "indeterminate_question.svg", false);
    public static final IcySVG INFO = new IcySVG(MONO + "info.svg", false);
    public static final IcySVG INVERT_COLORS = new IcySVG(MONO + "invert_colors.svg", false);
    public static final IcySVG KEYBOARD = new IcySVG(MONO + "keyboard.svg", false);
    public static final IcySVG KEYBOARD_ARROW_DOWN = new IcySVG(MONO + "keyboard_arrow_down.svg", false);
    public static final IcySVG KEYBOARD_ARROW_LEFT = new IcySVG(MONO + "keyboard_arrow_left.svg", false);
    public static final IcySVG KEYBOARD_ARROW_RIGHT = new IcySVG(MONO + "keyboard_arrow_right.svg", false);
    public static final IcySVG KEYBOARD_ARROW_UP = new IcySVG(MONO + "keyboard_arrow_up.svg", false);
    public static final IcySVG LAPS = new IcySVG(MONO + "laps.svg", false);
    public static final IcySVG LAST_PAGE = new IcySVG(MONO + "last_page.svg", false);
    public static final IcySVG LAYERS = new IcySVG(MONO + "layers.svg", false);
    public static final IcySVG LAYERS_CLEAR = new IcySVG(MONO + "layers_clear.svg", false);
    public static final IcySVG LINE = new IcySVG(MONO + "line.svg", false);
    public static final IcySVG LOCK = new IcySVG(MONO + "lock.svg", false);
    public static final IcySVG LOCK_OPEN = new IcySVG(MONO + "lock_open.svg", false);
    public static final IcySVG MAGIC_WAND = new IcySVG(MONO + "magic_wand.svg", false);
    public static final IcySVG MEASURE_CENTIMETER = new IcySVG(MONO + "measure_centimeter.svg", false);
    public static final IcySVG MY_LOCATION = new IcySVG(MONO + "my_location.svg", false);
    public static final IcySVG NORTH = new IcySVG(MONO + "north.svg", false);
    public static final IcySVG NORTH_EAST = new IcySVG(MONO + "north_east.svg", false);
    public static final IcySVG NORTH_WEST = new IcySVG(MONO + "north_west.svg", false);
    public static final IcySVG NOTE_ADD = new IcySVG(MONO + "note_add.svg", false);
    public static final IcySVG NOTIFICATIONS = new IcySVG(MONO + "notifications.svg", false);
    public static final IcySVG NOTIFICATIONS_IMPORTANT = new IcySVG(MONO + "notifications_important.svg", false);
    public static final IcySVG NOTIFICATIONS_UNREAD = new IcySVG(MONO + "notifications_unread.svg", false);
    public static final IcySVG NULL = new IcySVG(MONO + "null.svg", false);
    public static final IcySVG OPEN_IN_NEW = new IcySVG(MONO + "open_in_new.svg", false);
    public static final IcySVG PAUSE_CIRCLE = new IcySVG(MONO + "pause_circle.svg", false);
    public static final IcySVG PENTAGON = new IcySVG(MONO + "pentagon.svg", false);
    //public static final IcySVG POINT = new IcySVG(MONO + "point.svg", false);
    public static final IcySVG PHOTO_CAMERA = new IcySVG(MONO + "photo_camera.svg", false);
    public static final IcySVG PHOTO_EXPAND = new IcySVG(MONO + "photo_expand.svg", false);
    public static final IcySVG PHOTO_LIBRARY = new IcySVG(MONO + "photo_library.svg", false);
    public static final IcySVG PICTURE_ADD = new IcySVG(MONO + "picture_add.svg", false);
    public static final IcySVG PICTURE_IN_PICTURE = new IcySVG(MONO + "picture_in_picture.svg", false);
    public static final IcySVG PICTURE_METADATA = new IcySVG(MONO + "picture_metadata.svg", false);
    public static final IcySVG PLAY_CIRCLE = new IcySVG(MONO + "play_circle.svg", false);
    public static final IcySVG POINT_SCAN = new IcySVG(MONO + "point_scan.svg", false);
    public static final IcySVG POWER_SETTINGS_NEW = new IcySVG(MONO + "power_settings_new.svg", false);
    public static final IcySVG RADIO_BUTTON_CHECKED = new IcySVG(MONO + "radio_button_checked.svg", false);
    public static final IcySVG RADIO_BUTTON_PARTIAL = new IcySVG(MONO + "radio_button_partial.svg", false);
    public static final IcySVG RADIO_BUTTON_UNCHECKED = new IcySVG(MONO + "radio_button_unchecked.svg", false);
    public static final IcySVG RECENTER = new IcySVG(MONO + "recenter.svg", false);
    //public static final IcySVG RECTANGLE = new IcySVG(MONO + "rectangle.svg", false);
    public static final IcySVG REDO = new IcySVG(MONO + "redo.svg", false);
    public static final IcySVG REMOVE = new IcySVG(MONO + "remove.svg", false);
    public static final IcySVG REPEAT = new IcySVG(MONO + "repeat.svg", false);
    public static final IcySVG REPEAT_ON = new IcySVG(MONO + "repeat_on.svg", false);
    public static final IcySVG REPLAY = new IcySVG(MONO + "replay.svg", false);
    public static final IcySVG ROI_AREA = new IcySVG(MONO + "roi_area.svg", false);
    public static final IcySVG ROI_BOOLEAN = new IcySVG(MONO + "roi_boolean.svg", false);
    public static final IcySVG ROI_BOOLEAN_AND = new IcySVG(MONO + "roi_boolean_and.svg", false);
    public static final IcySVG ROI_BOOLEAN_NOT = new IcySVG(MONO + "roi_boolean_not.svg", false);
    public static final IcySVG ROI_BOOLEAN_OR = new IcySVG(MONO + "roi_boolean_or.svg", false);
    public static final IcySVG ROI_BOOLEAN_SUBSTRACT = new IcySVG(MONO + "roi_boolean_substract.svg", false);
    public static final IcySVG ROI_BOOLEAN_XOR = new IcySVG(MONO + "roi_boolean_xor.svg", false); // TODO redo the SVG to match the others roi_boolean
    public static final IcySVG ROI_DILATE = new IcySVG(MONO + "roi_dilate.svg", false);
    public static final IcySVG ROI_DISTANCE_MAP = new IcySVG(MONO + "roi_distance_map.svg", false);
    public static final IcySVG ROI_ELLIPSE = new IcySVG(MONO + "roi_ellipse.svg", false);
    public static final IcySVG ROI_ERODE = new IcySVG(MONO + "roi_erode.svg", false);
    public static final IcySVG ROI_EXTERIOR = new IcySVG(MONO + "roi_exterior.svg", false);
    public static final IcySVG ROI_INTERIOR = new IcySVG(MONO + "roi_interior.svg", false);
    public static final IcySVG ROI_LINE = new IcySVG(MONO + "roi_line.svg", false);
    public static final IcySVG ROI_POINT = new IcySVG(MONO + "roi_point.svg", false);
    public static final IcySVG ROI_POLYGON = new IcySVG(MONO + "roi_polygon.svg", false);
    public static final IcySVG ROI_POLYLINE = new IcySVG(MONO + "roi_polyline.svg", false);
    public static final IcySVG ROI_RECTANGLE = new IcySVG(MONO + "roi_rectangle.svg", false);
    public static final IcySVG ROI_SPLIT = new IcySVG(MONO + "roi_split.svg", false);
    public static final IcySVG ROTATE_LEFT = new IcySVG(MONO + "rotate_left.svg", false);
    public static final IcySVG ROTATE_RIGHT = new IcySVG(MONO + "rotate_right.svg", false);
    public static final IcySVG RULER = new IcySVG(MONO + "ruler.svg", false);
    public static final IcySVG SAVE = new IcySVG(MONO + "save.svg", false);
    public static final IcySVG SAVE_AS = new IcySVG(MONO + "save_as.svg", false);
    public static final IcySVG SEARCH = new IcySVG(MONO + "search.svg", false);
    public static final IcySVG SETTINGS = new IcySVG(MONO + "settings.svg", false);
    public static final IcySVG SETTINGS_PHOTO_CAMERA = new IcySVG(MONO + "settings_photo_camera.svg", false);
    public static final IcySVG SETTINGS_VIDEO_CAMERA = new IcySVG(MONO + "settings_video_camera.svg", false);
    public static final IcySVG SHADING = new IcySVG(MONO + "shading.svg", false);
    public static final IcySVG SKIP_NEXT = new IcySVG(MONO + "skip_next.svg", false);
    public static final IcySVG SKIP_PREVIOUS = new IcySVG(MONO + "skip_previous.svg", false);
    public static final IcySVG SOUTH = new IcySVG(MONO + "south.svg", false);
    public static final IcySVG SOUTH_EAST = new IcySVG(MONO + "south_east.svg", false);
    public static final IcySVG SOUTH_WEST = new IcySVG(MONO + "south_west.svg", false);
    public static final IcySVG STAR = new IcySVG(MONO + "star.svg", false);
    public static final IcySVG STOP_CIRCLE = new IcySVG(MONO + "stop_circle.svg", false);
    //public static final IcySVG STROKE_FULL = new IcySVG(MONO + "stroke_full.svg", false);
    public static final IcySVG SWITCH_ACCESS_2 = new IcySVG(MONO + "switch_access_2.svg", false);
    public static final IcySVG TERMINAL = new IcySVG(MONO + "terminal.svg", false);
    public static final IcySVG THEME = new IcySVG(MONO + "theme.svg", false);
    public static final IcySVG THEME_DARK = new IcySVG(MONO + "theme_dark.svg", false);
    public static final IcySVG THEME_LIGHT = new IcySVG(MONO + "theme_light.svg", false);
    @Deprecated
    public static final IcySVG TIMELINE = new IcySVG(MONO + "timeline.svg", false);
    public static final IcySVG TV_OPTIONS_INPUT_SETTINGS = new IcySVG(MONO + "tv_options_input_settings.svg", false);
    public static final IcySVG UNDO = new IcySVG(MONO + "undo.svg", false);
    public static final IcySVG UNFOLD_LESS = new IcySVG(MONO + "unfold_less.svg", false);
    public static final IcySVG UNFOLD_MORE = new IcySVG(MONO + "unfold_more.svg", false);
    public static final IcySVG UNION = new IcySVG(MONO + "union.svg", false);
    public static final IcySVG UPDATE = new IcySVG(MONO + "update.svg", false);
    public static final IcySVG UPDATE_DISABLED = new IcySVG(MONO + "update_disabled.svg", false);
    public static final IcySVG VERTICAL_ALIGN_TOP = new IcySVG(MONO + "vertical_align_top.svg", false);
    public static final IcySVG VIDEOCAM = new IcySVG(MONO + "videocam.svg", false);
    public static final IcySVG VIEW_COLUMN = new IcySVG(MONO + "view_column.svg", false);
    public static final IcySVG VIEW_MODULE = new IcySVG(MONO + "view_module.svg", false);
    public static final IcySVG VIEW_QUILT = new IcySVG(MONO + "view_quilt.svg", false);
    public static final IcySVG VIEW_STREAM = new IcySVG(MONO + "view_stream.svg", false);
    public static final IcySVG VISIBILITY = new IcySVG(MONO + "visibility.svg", false);
    public static final IcySVG VISIBILITY_OFF = new IcySVG(MONO + "visibility_off.svg", false);
    public static final IcySVG WARNING = new IcySVG(MONO + "warning.svg", false);
    public static final IcySVG WEST = new IcySVG(MONO + "west.svg", false);
    public static final IcySVG WIDGETS = new IcySVG(MONO + "widgets.svg", false);
    public static final IcySVG ZOOM_IN = new IcySVG(MONO + "zoom_in.svg", false);
    public static final IcySVG ZOOM_OUT = new IcySVG(MONO + "zoom_out.svg", false);
    public static final IcySVG ZOOM_OUT_MAP = new IcySVG(MONO + "zoom_out_map.svg", false);

    // Colored icons
    public static final IcySVG ICY_TRANSPARENT = new IcySVG(COLOR + "icy_transparent.svg", false);
    public static final IcySVG ICY_WHITE_BG = new IcySVG(COLOR + "icy_white_bg.svg", false);
    public static final IcySVG ICY_MACOS = new IcySVG(COLOR + "icy_macos.svg", false);
    public static final IcySVG ARGB_IMAGE = new IcySVG(COLOR + "argb_image.svg", false);
    public static final IcySVG GRAYSCALE_IMAGE = new IcySVG(COLOR + "grayscale_image.svg", false);
    public static final IcySVG RGB_IMAGE = new IcySVG(COLOR + "rgb_image.svg", false);
    public static final IcySVG EXTENSION_DEFAULT = new IcySVG(COLOR + "extension_default.svg", false);

    private final byte @NonNull [] data;
    /**
     * Only for Icon
     */
    private final boolean monochrome;

    public IcySVG(final @NonNull String resourcePath, final boolean monochrome) {
        this.monochrome = monochrome;

        byte [] data = new byte[0];
        try (final InputStream is = Icy.class.getResourceAsStream(resourcePath)) {
            if (is != null)
                data = is.readAllBytes();
        }
        catch (final IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, "Cannot open SVG resource: " + resourcePath + ".", e);
        }
        this.data = data;
    }

    public IcySVG(final @NonNull Path path, final boolean monochrome) throws IllegalArgumentException {
        this.monochrome = monochrome;

        byte [] data = new byte[0];
        if (!path.toFile().exists())
            throw new IllegalArgumentException("The path " + path + " does not exist");
        try (final InputStream is = new FileInputStream(path.toFile())) {
            //if (is != null)
            data = is.readAllBytes();
        }
        catch (final IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, "Cannot open SVG file: " + path + ".", e);
        }
        this.data = data;
    }

    public IcySVG(final byte @NonNull [] data, final boolean monochrome) {
        this.data = data.clone();
        this.monochrome = monochrome;
    }

    public IcySVG(final byte @NonNull [] data) {
        this(data, false);
    }

    // SVG Icon
    @Contract("_, _ -> new")
    public @NonNull IcySVGIcon getIcon(final int width, final int height) {
        return IcySVGIcon.fromBytes(data, width, height);
    }

    @Contract("_ -> new")
    public @NonNull IcySVGIcon getIcon(final int size) {
        return getIcon(size, size);
    }

    public @NonNull IcySVGIcon getIcon(final int width, final int height, final @NonNull Color color) {
        if (monochrome)
            return IcySVGIcon.fromBytes(data, width, height, color);
        else
            return getIcon(width, height);
    }

    public @NonNull IcySVGIcon getIcon(final int size, final @NonNull Color color) {
        return getIcon(size, size, color);
    }

    public @NonNull IcySVGIcon getIcon(final int width, final int height, final LookAndFeelUtil. @NonNull ColorType colorType) {
        if (monochrome)
            return IcySVGIcon.fromBytes(data, width, height, colorType);
        else
            return getIcon(width, height);
    }

    public @NonNull IcySVGIcon getIcon(final int size, final LookAndFeelUtil. @NonNull ColorType colorType) {
        return getIcon(size, size, colorType);
    }

    public @NonNull IcySVGIcon getIcon(final int size, final LookAndFeelUtil. @NonNull ColorType colorType, final IcySVGIcon.Badge badge) {
        //return getIcon(size, size, colorType);
        if (monochrome)
            return IcySVGIcon.fromBytes(data, size, size, colorType, badge);
        else
            return IcySVGIcon.fromBytes(data, size, size, badge);
    }

    @Contract("_, _ -> new")
    public @NonNull IcySVGImage getImage(final int width, final int height) {
        return IcySVGImage.fromBytes(data, width, height);
    }

    @Contract("_ -> new")
    public @NonNull IcySVGImage getImage(final int size) {
        return getImage(size, size);
    }

    @Contract("_, _, _ -> new")
    public @NonNull IcySVGImage getImage(final int width, final int height, final @NonNull Color color) {
        return IcySVGImage.fromBytes(data, width, height, color);
    }

    @Contract("_, _ -> new")
    public @NonNull IcySVGImage getImage(final int size, final @NonNull Color color) {
        return getImage(size, size, color);
    }
}
