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

package org.bioimageanalysis.icy.gui.component.icon;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.io.ResourceUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * List all the Icy's SVG icons.
 *
 * @author Thomas Musset
 */
public class SVGResource {
    private static final String MONO = ResourceUtil.SVG_ICON_PATH + "mono/";
    private static final String COLOR = ResourceUtil.SVG_ICON_PATH + "color/";

    // Monochrome icons
    public static final SVGResource AXIS_3D = new SVGResource(MONO + "3d_axis.svg", false);
    public static final SVGResource BOX_BOUNDS_3D = new SVGResource(MONO + "3d_box_bounds.svg", false);
    public static final SVGResource ADD = new SVGResource(MONO + "add.svg", false);
    public static final SVGResource ARROW_DOWN = new SVGResource(MONO + "arrow_down.svg", false);
    public static final SVGResource ARROW_LEFT = new SVGResource(MONO + "arrow_left.svg", false);
    public static final SVGResource ARROW_RIGHT = new SVGResource(MONO + "arrow_right.svg", false);
    public static final SVGResource ARROW_UP = new SVGResource(MONO + "arrow_up.svg", false);
    public static final SVGResource BOLT = new SVGResource(MONO + "bolt.svg", false);
    public static final SVGResource BRACKET_LEFT = new SVGResource(MONO + "bracket_left.svg", false);
    public static final SVGResource BRACKET_RIGHT = new SVGResource(MONO + "bracket_right.svg", false);
    public static final SVGResource BUG_REPORT = new SVGResource(MONO + "bug_report.svg", false);
    //public static final SVGResource CAMERA = new SVGResource(MONO + "camera.svg", false);
    //public static final SVGResource CAMERA_ROLL = new SVGResource(MONO + "camera_roll.svg", false);
    //public static final SVGResource CENTER_FOCUS_STRONG = new SVGResource(MONO + "center_focus_strong.svg", false);
    public static final SVGResource CHECK = new SVGResource(MONO + "check.svg", false);
    public static final SVGResource CHECK_CIRCLE = new SVGResource(MONO + "check_circle.svg", false);
    public static final SVGResource CIRCLE = new SVGResource(MONO + "circle.svg", false);
    public static final SVGResource CIRCLE_FILL = new SVGResource(MONO + "circle_fill.svg", false);
    public static final SVGResource CLEAR_ALL = new SVGResource(MONO + "clear_all.svg", false);
    public static final SVGResource CLOSE = new SVGResource(MONO + "close.svg", false);
    public static final SVGResource CONTENT_COPY = new SVGResource(MONO + "content_copy.svg", false);
    public static final SVGResource CONVERT_SHAPE = new SVGResource(MONO + "convert_shape.svg", false);
    public static final SVGResource CROP = new SVGResource(MONO + "crop.svg", false);
    public static final SVGResource CUBE_SLICE = new SVGResource(MONO + "cube_slice.svg", false);
    public static final SVGResource CUT = new SVGResource(MONO + "cut.svg", false);
    public static final SVGResource DASHBOARD = new SVGResource(MONO + "dashboard.svg", false);
    public static final SVGResource DATABASE = new SVGResource(MONO + "database.svg", false);
    public static final SVGResource DELETE = new SVGResource(MONO + "delete.svg", false);
    public static final SVGResource DELETE_SWEEP = new SVGResource(MONO + "delete_sweep.svg", false);
    public static final SVGResource DEPLOYED_CODE = new SVGResource(MONO + "deployed_code.svg", false);
    public static final SVGResource DESCRIPTION = new SVGResource(MONO + "description.svg", false);
    public static final SVGResource DEVICE_RESET = new SVGResource(MONO + "device_reset.svg", false);
    public static final SVGResource DRAG_PAN = new SVGResource(MONO + "drag_pan.svg", false);
    public static final SVGResource DRAW_ABSTRACT = new SVGResource(MONO + "draw_abstract.svg", false);
    public static final SVGResource EAST = new SVGResource(MONO + "east.svg", false);
    public static final SVGResource EDIT = new SVGResource(MONO + "edit.svg", false);
    public static final SVGResource ERROR = new SVGResource(MONO + "error.svg", false);
    public static final SVGResource EXPORT_NOTES = new SVGResource(MONO + "export_notes.svg", false);
    public static final SVGResource EXTENSION = new SVGResource(MONO + "extension.svg", false);
    public static final SVGResource FILE_OPEN = new SVGResource(MONO + "file_open.svg", false);
    public static final SVGResource FILE_SAVE = new SVGResource(MONO + "file_save.svg", false);
    public static final SVGResource FILTER_1 = new SVGResource(MONO + "filter_1.svg", false);
    public static final SVGResource FILTER_2 = new SVGResource(MONO + "filter_2.svg", false);
    public static final SVGResource FILTER_3 = new SVGResource(MONO + "filter_3.svg", false);
    public static final SVGResource FILTER_4 = new SVGResource(MONO + "filter_4.svg", false);
    public static final SVGResource FILTER_5 = new SVGResource(MONO + "filter_5.svg", false);
    public static final SVGResource FILTER_6 = new SVGResource(MONO + "filter_6.svg", false);
    public static final SVGResource FILTER_7 = new SVGResource(MONO + "filter_7.svg", false);
    public static final SVGResource FILTER_8 = new SVGResource(MONO + "filter_8.svg", false);
    public static final SVGResource FILTER_9 = new SVGResource(MONO + "filter_9.svg", false);
    public static final SVGResource FILTER_9_PLUS = new SVGResource(MONO + "filter_9_plus.svg", false);
    public static final SVGResource FILTER_NONE = new SVGResource(MONO + "filter_none.svg", false);
    public static final SVGResource FIRST_PAGE = new SVGResource(MONO + "first_page.svg", false);
    public static final SVGResource FLAG = new SVGResource(MONO + "flag.svg", false);
    public static final SVGResource FLASH_OFF = new SVGResource(MONO + "flash_off.svg", false);
    public static final SVGResource FLASH_ON = new SVGResource(MONO + "flash_on.svg", false);
    public static final SVGResource FOLDER = new SVGResource(MONO + "folder.svg", false);
    public static final SVGResource FOLDER_OPEN = new SVGResource(MONO + "folder_open.svg", false);
    public static final SVGResource FORMAT_ALIGN_CENTER = new SVGResource(MONO + "format_align_center.svg", false);
    public static final SVGResource FORMAT_ALIGN_JUSTIFY = new SVGResource(MONO + "format_align_justify.svg", false);
    public static final SVGResource FORMAT_ALIGN_LEFT = new SVGResource(MONO + "format_align_left.svg", false);
    public static final SVGResource FORMAT_ALIGN_RIGHT = new SVGResource(MONO + "format_align_right.svg", false);
    public static final SVGResource GESTURE_SELECT = new SVGResource(MONO + "gesture_select.svg", false);
    public static final SVGResource GRAIN = new SVGResource(MONO + "grain.svg", false);
    public static final SVGResource GRID_OFF = new SVGResource(MONO + "grid_off.svg", false);
    public static final SVGResource GRID_ON = new SVGResource(MONO + "grid_on.svg", false);
    public static final SVGResource GROUP_WORK = new SVGResource(MONO + "group_work.svg", false);
    public static final SVGResource HANDYMAN = new SVGResource(MONO + "handyman.svg", false);
    public static final SVGResource HELP = new SVGResource(MONO + "help.svg", false);
    public static final SVGResource HISTORY = new SVGResource(MONO + "history.svg", false);
    public static final SVGResource HOURGLASS = new SVGResource(MONO + "hourglass.svg", false);
    public static final SVGResource IMAGE = new SVGResource(MONO + "image.svg", false);
    public static final SVGResource IMAGE_ASPECT_RATIO = new SVGResource(MONO + "image_aspect_ratio.svg", false);
    public static final SVGResource IMAGE_BROKEN = new SVGResource(MONO + "image_broken.svg", false);
    public static final SVGResource IMAGE_RESIZE = new SVGResource(MONO + "image_resize.svg", false);
    public static final SVGResource INDETERMINATE_QUESTION = new SVGResource(MONO + "indeterminate_question.svg", false);
    public static final SVGResource INFO = new SVGResource(MONO + "info.svg", false);
    public static final SVGResource INVERT_COLORS = new SVGResource(MONO + "invert_colors.svg", false);
    public static final SVGResource KEYBOARD = new SVGResource(MONO + "keyboard.svg", false);
    public static final SVGResource KEYBOARD_ARROW_DOWN = new SVGResource(MONO + "keyboard_arrow_down.svg", false);
    public static final SVGResource KEYBOARD_ARROW_LEFT = new SVGResource(MONO + "keyboard_arrow_left.svg", false);
    public static final SVGResource KEYBOARD_ARROW_RIGHT = new SVGResource(MONO + "keyboard_arrow_right.svg", false);
    public static final SVGResource KEYBOARD_ARROW_UP = new SVGResource(MONO + "keyboard_arrow_up.svg", false);
    public static final SVGResource LAPS = new SVGResource(MONO + "laps.svg", false);
    public static final SVGResource LAST_PAGE = new SVGResource(MONO + "last_page.svg", false);
    public static final SVGResource LAYERS = new SVGResource(MONO + "layers.svg", false);
    public static final SVGResource LAYERS_CLEAR = new SVGResource(MONO + "layers_clear.svg", false);
    public static final SVGResource LINE = new SVGResource(MONO + "line.svg", false);
    public static final SVGResource LOCK = new SVGResource(MONO + "lock.svg", false);
    public static final SVGResource LOCK_OPEN = new SVGResource(MONO + "lock_open.svg", false);
    public static final SVGResource MAGIC_WAND = new SVGResource(MONO + "magic_wand.svg", false);
    public static final SVGResource MEASURE_CENTIMETER = new SVGResource(MONO + "measure_centimeter.svg", false);
    public static final SVGResource MY_LOCATION = new SVGResource(MONO + "my_location.svg", false);
    public static final SVGResource NORTH = new SVGResource(MONO + "north.svg", false);
    public static final SVGResource NORTH_EAST = new SVGResource(MONO + "north_east.svg", false);
    public static final SVGResource NORTH_WEST = new SVGResource(MONO + "north_west.svg", false);
    public static final SVGResource NOTE_ADD = new SVGResource(MONO + "note_add.svg", false);
    public static final SVGResource NOTIFICATIONS = new SVGResource(MONO + "notifications.svg", false);
    public static final SVGResource NOTIFICATIONS_IMPORTANT = new SVGResource(MONO + "notifications_important.svg", false);
    public static final SVGResource NOTIFICATIONS_UNREAD = new SVGResource(MONO + "notifications_unread.svg", false);
    public static final SVGResource NULL = new SVGResource(MONO + "null.svg", false);
    public static final SVGResource OPEN_IN_NEW = new SVGResource(MONO + "open_in_new.svg", false);
    public static final SVGResource PAUSE_CIRCLE = new SVGResource(MONO + "pause_circle.svg", false);
    public static final SVGResource PENTAGON = new SVGResource(MONO + "pentagon.svg", false);
    //public static final SVGResource POINT = new SVGResource(MONO + "point.svg", false);
    public static final SVGResource PHOTO_CAMERA = new SVGResource(MONO + "photo_camera.svg", false);
    public static final SVGResource PHOTO_LIBRARY = new SVGResource(MONO + "photo_library.svg", false);
    public static final SVGResource PICTURE_ADD = new SVGResource(MONO + "picture_add.svg", false);
    public static final SVGResource PICTURE_IN_PICTURE = new SVGResource(MONO + "picture_in_picture.svg", false);
    public static final SVGResource PICTURE_METADATA = new SVGResource(MONO + "picture_metadata.svg", false);
    public static final SVGResource PLAY_CIRCLE = new SVGResource(MONO + "play_circle.svg", false);
    public static final SVGResource POINT_SCAN = new SVGResource(MONO + "point_scan.svg", false);
    public static final SVGResource POWER_SETTINGS_NEW = new SVGResource(MONO + "power_settings_new.svg", false);
    public static final SVGResource RADIO_BUTTON_CHECKED = new SVGResource(MONO + "radio_button_checked.svg", false);
    public static final SVGResource RADIO_BUTTON_PARTIAL = new SVGResource(MONO + "radio_button_partial.svg", false);
    public static final SVGResource RADIO_BUTTON_UNCHECKED = new SVGResource(MONO + "radio_button_unchecked.svg", false);
    public static final SVGResource RECENTER = new SVGResource(MONO + "recenter.svg", false);
    //public static final SVGResource RECTANGLE = new SVGResource(MONO + "rectangle.svg", false);
    public static final SVGResource REDO = new SVGResource(MONO + "redo.svg", false);
    public static final SVGResource REMOVE = new SVGResource(MONO + "remove.svg", false);
    public static final SVGResource REPEAT = new SVGResource(MONO + "repeat.svg", false);
    public static final SVGResource REPEAT_ON = new SVGResource(MONO + "repeat_on.svg", false);
    public static final SVGResource REPLAY = new SVGResource(MONO + "replay.svg", false);
    public static final SVGResource ROI_AREA = new SVGResource(MONO + "roi_area.svg", false);
    public static final SVGResource ROI_BOOLEAN = new SVGResource(MONO + "roi_boolean.svg", false);
    public static final SVGResource ROI_BOOLEAN_AND = new SVGResource(MONO + "roi_boolean_and.svg", false);
    public static final SVGResource ROI_BOOLEAN_NOT = new SVGResource(MONO + "roi_boolean_not.svg", false);
    public static final SVGResource ROI_BOOLEAN_OR = new SVGResource(MONO + "roi_boolean_or.svg", false);
    public static final SVGResource ROI_BOOLEAN_SUBSTRACT = new SVGResource(MONO + "roi_boolean_substract.svg", false);
    public static final SVGResource ROI_BOOLEAN_XOR = new SVGResource(MONO + "roi_boolean_xor.svg", false); // TODO redo the SVG to match the others roi_boolean
    public static final SVGResource ROI_DILATE = new SVGResource(MONO + "roi_dilate.svg", false);
    public static final SVGResource ROI_DISTANCE_MAP = new SVGResource(MONO + "roi_distance_map.svg", false);
    public static final SVGResource ROI_ELLIPSE = new SVGResource(MONO + "roi_ellipse.svg", false);
    public static final SVGResource ROI_ERODE = new SVGResource(MONO + "roi_erode.svg", false);
    public static final SVGResource ROI_EXTERIOR = new SVGResource(MONO + "roi_exterior.svg", false);
    public static final SVGResource ROI_INTERIOR = new SVGResource(MONO + "roi_interior.svg", false);
    public static final SVGResource ROI_LINE = new SVGResource(MONO + "roi_line.svg", false);
    public static final SVGResource ROI_POINT = new SVGResource(MONO + "roi_point.svg", false);
    public static final SVGResource ROI_POLYGON = new SVGResource(MONO + "roi_polygon.svg", false);
    public static final SVGResource ROI_POLYLINE = new SVGResource(MONO + "roi_polyline.svg", false);
    public static final SVGResource ROI_RECTANGLE = new SVGResource(MONO + "roi_rectangle.svg", false);
    public static final SVGResource ROI_SPLIT = new SVGResource(MONO + "roi_split.svg", false);
    public static final SVGResource ROTATE_LEFT = new SVGResource(MONO + "rotate_left.svg", false);
    public static final SVGResource ROTATE_RIGHT = new SVGResource(MONO + "rotate_right.svg", false);
    public static final SVGResource RULER = new SVGResource(MONO + "ruler.svg", false);
    public static final SVGResource SAVE = new SVGResource(MONO + "save.svg", false);
    public static final SVGResource SAVE_AS = new SVGResource(MONO + "save_as.svg", false);
    public static final SVGResource SEARCH = new SVGResource(MONO + "search.svg", false);
    public static final SVGResource SETTINGS = new SVGResource(MONO + "settings.svg", false);
    public static final SVGResource SETTINGS_PHOTO_CAMERA = new SVGResource(MONO + "settings_photo_camera.svg", false);
    public static final SVGResource SETTINGS_VIDEO_CAMERA = new SVGResource(MONO + "settings_video_camera.svg", false);
    public static final SVGResource SHADING = new SVGResource(MONO + "shading.svg", false);
    public static final SVGResource SKIP_NEXT = new SVGResource(MONO + "skip_next.svg", false);
    public static final SVGResource SKIP_PREVIOUS = new SVGResource(MONO + "skip_previous.svg", false);
    public static final SVGResource SOUTH = new SVGResource(MONO + "south.svg", false);
    public static final SVGResource SOUTH_EAST = new SVGResource(MONO + "south_east.svg", false);
    public static final SVGResource SOUTH_WEST = new SVGResource(MONO + "south_west.svg", false);
    public static final SVGResource STAR = new SVGResource(MONO + "star.svg", false);
    public static final SVGResource STOP_CIRCLE = new SVGResource(MONO + "stop_circle.svg", false);
    //public static final SVGResource STROKE_FULL = new SVGResource(MONO + "stroke_full.svg", false);
    public static final SVGResource SWITCH_ACCESS_2 = new SVGResource(MONO + "switch_access_2.svg", false);
    public static final SVGResource TERMINAL = new SVGResource(MONO + "terminal.svg", false);
    public static final SVGResource THEME = new SVGResource(MONO + "theme.svg", false);
    public static final SVGResource THEME_DARK = new SVGResource(MONO + "theme_dark.svg", false);
    public static final SVGResource THEME_LIGHT = new SVGResource(MONO + "theme_light.svg", false);
    @Deprecated
    public static final SVGResource TIMELINE = new SVGResource(MONO + "timeline.svg", false);
    public static final SVGResource TV_OPTIONS_INPUT_SETTINGS = new SVGResource(MONO + "tv_options_input_settings.svg", false);
    public static final SVGResource UNDO = new SVGResource(MONO + "undo.svg", false);
    public static final SVGResource UNFOLD_LESS = new SVGResource(MONO + "unfold_less.svg", false);
    public static final SVGResource UNFOLD_MORE = new SVGResource(MONO + "unfold_more.svg", false);
    public static final SVGResource UNION = new SVGResource(MONO + "union.svg", false);
    public static final SVGResource UPDATE = new SVGResource(MONO + "update.svg", false);
    public static final SVGResource UPDATE_DISABLED = new SVGResource(MONO + "update_disabled.svg", false);
    public static final SVGResource VERTICAL_ALIGN_TOP = new SVGResource(MONO + "vertical_align_top.svg", false);
    public static final SVGResource VIDEOCAM = new SVGResource(MONO + "videocam.svg", false);
    public static final SVGResource VIEW_COLUMN = new SVGResource(MONO + "view_column.svg", false);
    public static final SVGResource VIEW_MODULE = new SVGResource(MONO + "view_module.svg", false);
    public static final SVGResource VIEW_QUILT = new SVGResource(MONO + "view_quilt.svg", false);
    public static final SVGResource VIEW_STREAM = new SVGResource(MONO + "view_stream.svg", false);
    public static final SVGResource VISIBILITY = new SVGResource(MONO + "visibility.svg", false);
    public static final SVGResource VISIBILITY_OFF = new SVGResource(MONO + "visibility_off.svg", false);
    public static final SVGResource WARNING = new SVGResource(MONO + "warning.svg", false);
    public static final SVGResource WEST = new SVGResource(MONO + "west.svg", false);
    public static final SVGResource WIDGETS = new SVGResource(MONO + "widgets.svg", false);
    public static final SVGResource ZOOM_IN = new SVGResource(MONO + "zoom_in.svg", false);
    public static final SVGResource ZOOM_OUT = new SVGResource(MONO + "zoom_out.svg", false);
    public static final SVGResource ZOOM_OUT_MAP = new SVGResource(MONO + "zoom_out_map.svg", false);

    // Colored icons
    public static final SVGResource ICY_TRANSPARENT = new SVGResource(COLOR + "icy_transparent.svg", true);
    public static final SVGResource ICY_WHITE_BG = new SVGResource(COLOR + "icy_white_bg.svg", true);
    public static final SVGResource ICY_MACOS = new SVGResource(COLOR + "icy_macos.svg", true);
    public static final SVGResource ARGB_IMAGE = new SVGResource(COLOR + "argb_image.svg", true);
    public static final SVGResource GRAYSCALE_IMAGE = new SVGResource(COLOR + "grayscale_image.svg", true);
    public static final SVGResource RGB_IMAGE = new SVGResource(COLOR + "rgb_image.svg", true);
    public static final SVGResource EXTENSION_DEFAULT = new SVGResource(COLOR + "extension_default.svg", true);

    private final boolean colored;
    private final byte @NotNull [] data;

    /**
     * Create an SVG icon from resource folder.
     *
     * @param path    the resource path in String.
     * @param colored false if the SVG is monochrome, allow the sytem to change the color automatically according to the theme.
     */
    SVGResource(final @NotNull String path, final boolean colored) {
        this.colored = colored;

        byte @NotNull [] data = new byte[0];
        try (final InputStream is = Icy.class.getResourceAsStream(path)) {
            if (is != null)
                data = is.readAllBytes();
        }
        catch (final IOException e) {
            //
        }
        this.data = data;
    }

    public final byte @NotNull [] toByteArray() {
        return data;
    }

    public final boolean isColored() {
        return colored;
    }
}
