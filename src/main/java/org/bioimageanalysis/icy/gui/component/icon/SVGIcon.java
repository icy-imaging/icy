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

package org.bioimageanalysis.icy.gui.component.icon;

import org.bioimageanalysis.icy.io.ResourceUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * List all the Icy's SVG icons.
 * @author Thomas Musset
 */
public class SVGIcon {
    private static final String MONO = ResourceUtil.SVG_ICON_PATH + "mono/";
    private static final String COLOR = ResourceUtil.SVG_ICON_PATH + "color/";

    // Monochrome icons
    public static final SVGIcon AXIS_3D = new SVGIcon(MONO + "3d_axis.svg", false);
    public static final SVGIcon ADD = new SVGIcon(MONO + "add.svg", false);
    public static final SVGIcon ADD_PHOTO_ALTERNATE = new SVGIcon(MONO + "add_photo_alternate.svg", false);
    public static final SVGIcon ARROW_DROP_DOWN = new SVGIcon(MONO + "arrow_drop_down.svg", false);
    public static final SVGIcon ARROW_DROP_UP = new SVGIcon(MONO + "arrow_drop_up.svg", false);
    public static final SVGIcon ARROW_LEFT = new SVGIcon(MONO + "arrow_left.svg", false);
    public static final SVGIcon ARROW_RIGHT = new SVGIcon(MONO + "arrow_right.svg", false);
    public static final SVGIcon ART_TRACK = new SVGIcon(MONO + "art_track.svg", false);
    public static final SVGIcon ASPECT_RATIO = new SVGIcon(MONO + "aspect_ratio.svg", false);
    public static final SVGIcon BOLT = new SVGIcon(MONO + "bolt.svg", false);
    public static final SVGIcon BOOLEAN = new SVGIcon(MONO + "boolean.svg", false);
    public static final SVGIcon BOOLEAN_AND = new SVGIcon(MONO + "boolean_and.svg", false);
    public static final SVGIcon BOOLEAN_NOT = new SVGIcon(MONO + "boolean_not.svg", false);
    public static final SVGIcon BOOLEAN_OR = new SVGIcon(MONO + "boolean_or.svg", false);
    public static final SVGIcon BOOLEAN_SUBSTRACT = new SVGIcon(MONO + "boolean_substract.svg", false);
    public static final SVGIcon BOOLEAN_XOR = new SVGIcon(MONO + "boolean_xor.svg", false);
    public static final SVGIcon BOX_BOUNDS = new SVGIcon(MONO + "box_bounds.svg", false);
    public static final SVGIcon BRACKET_RIGHT = new SVGIcon(MONO + "bracket_right.svg", false);
    public static final SVGIcon BRIGHTNESS_MEDIUM = new SVGIcon(MONO + "brightness_medium.svg", false);
    public static final SVGIcon BROKEN_IMAGE = new SVGIcon(MONO + "broken_image.svg", false);
    public static final SVGIcon BUG_REPORT = new SVGIcon(MONO + "bug_report.svg", false);
    public static final SVGIcon CAMERA = new SVGIcon(MONO + "camera.svg", false);
    public static final SVGIcon CAMERA_ROLL = new SVGIcon(MONO + "camera_roll.svg", false);
    public static final SVGIcon CENTER_FOCUS_STRONG = new SVGIcon(MONO + "center_focus_strong.svg", false);
    public static final SVGIcon CHECK = new SVGIcon(MONO + "check.svg", false);
    public static final SVGIcon CHECK_CIRCLE = new SVGIcon(MONO + "check_circle.svg", false);
    public static final SVGIcon CIRCLE = new SVGIcon(MONO + "circle.svg", false);
    public static final SVGIcon CIRCLE_FILL = new SVGIcon(MONO + "circle_fill.svg", false);
    public static final SVGIcon CLEAR_ALL = new SVGIcon(MONO + "clear_all.svg", false);
    public static final SVGIcon CLOSE = new SVGIcon(MONO + "close.svg", false);
    public static final SVGIcon CONTENT_COPY = new SVGIcon(MONO + "content_copy.svg", false);
    public static final SVGIcon CONVERT_SHAPE = new SVGIcon(MONO + "convert_shape.svg", false);
    public static final SVGIcon CROP = new SVGIcon(MONO + "crop.svg", false);
    public static final SVGIcon CUBE_SLICE = new SVGIcon(MONO + "cube_slice.svg", false);
    public static final SVGIcon CUT = new SVGIcon(MONO + "cut.svg", false);
    public static final SVGIcon DARK_MODE = new SVGIcon(MONO + "dark_mode.svg", false);
    public static final SVGIcon DASHBOARD = new SVGIcon(MONO + "dashboard.svg", false);
    public static final SVGIcon DATABASE = new SVGIcon(MONO + "database.svg", false);
    public static final SVGIcon DELETE = new SVGIcon(MONO + "delete.svg", false);
    public static final SVGIcon DELETE_SWEEP = new SVGIcon(MONO + "delete_sweep.svg", false);
    public static final SVGIcon DEPLOYED_CODE = new SVGIcon(MONO + "deployed_code.svg", false);
    public static final SVGIcon DESCRIPTION = new SVGIcon(MONO + "description.svg", false);
    public static final SVGIcon DEVICE_RESET = new SVGIcon(MONO + "device_reset.svg", false);
    public static final SVGIcon DRAW_ABSTRACT = new SVGIcon(MONO + "draw_abstract.svg", false);
    public static final SVGIcon EAST = new SVGIcon(MONO + "east.svg", false);
    public static final SVGIcon EDIT = new SVGIcon(MONO + "edit.svg", false);
    public static final SVGIcon ERROR = new SVGIcon(MONO + "error.svg", false);
    public static final SVGIcon EXPORT_NOTES = new SVGIcon(MONO + "export_notes.svg", false);
    public static final SVGIcon EXTENSION = new SVGIcon(MONO + "extension.svg", false);
    public static final SVGIcon FILE_OPEN = new SVGIcon(MONO + "file_open.svg", false);
    public static final SVGIcon FILE_SAVE = new SVGIcon(MONO + "file_save.svg", false);
    public static final SVGIcon FILTER_1 = new SVGIcon(MONO + "filter_1.svg", false);
    public static final SVGIcon FILTER_2 = new SVGIcon(MONO + "filter_2.svg", false);
    public static final SVGIcon FILTER_3 = new SVGIcon(MONO + "filter_3.svg", false);
    public static final SVGIcon FILTER_4 = new SVGIcon(MONO + "filter_4.svg", false);
    public static final SVGIcon FILTER_5 = new SVGIcon(MONO + "filter_5.svg", false);
    public static final SVGIcon FILTER_6 = new SVGIcon(MONO + "filter_6.svg", false);
    public static final SVGIcon FILTER_7 = new SVGIcon(MONO + "filter_7.svg", false);
    public static final SVGIcon FILTER_8 = new SVGIcon(MONO + "filter_8.svg", false);
    public static final SVGIcon FILTER_9 = new SVGIcon(MONO + "filter_9.svg", false);
    public static final SVGIcon FILTER_9_PLUS = new SVGIcon(MONO + "filter_9_plus.svg", false);
    public static final SVGIcon FILTER_NONE = new SVGIcon(MONO + "filter_none.svg", false);
    public static final SVGIcon FIRST_PAGE = new SVGIcon(MONO + "first_page.svg", false);
    public static final SVGIcon FLAG = new SVGIcon(MONO + "flag.svg", false);
    public static final SVGIcon FLASH_OFF = new SVGIcon(MONO + "flash_off.svg", false);
    public static final SVGIcon FLASH_ON = new SVGIcon(MONO + "flash_on.svg", false);
    public static final SVGIcon FOLDER = new SVGIcon(MONO + "folder.svg", false);
    public static final SVGIcon FOLDER_OPEN = new SVGIcon(MONO + "folder_open.svg", false);
    public static final SVGIcon FORMAT_ALIGN_CENTER = new SVGIcon(MONO + "format_align_center.svg", false);
    public static final SVGIcon FORMAT_ALIGN_JUSTIFY = new SVGIcon(MONO + "format_align_justify.svg", false);
    public static final SVGIcon FORMAT_ALIGN_LEFT = new SVGIcon(MONO + "format_align_left.svg", false);
    public static final SVGIcon FORMAT_ALIGN_RIGHT = new SVGIcon(MONO + "format_align_right.svg", false);
    public static final SVGIcon GESTURE_SELECT = new SVGIcon(MONO + "gesture_select.svg", false);
    public static final SVGIcon GRAIN = new SVGIcon(MONO + "grain.svg", false);
    public static final SVGIcon GRID_OFF = new SVGIcon(MONO + "grid_off.svg", false);
    public static final SVGIcon GRID_ON = new SVGIcon(MONO + "grid_on.svg", false);
    public static final SVGIcon GROUP_WORK = new SVGIcon(MONO + "group_work.svg", false);
    public static final SVGIcon HANDYMAN = new SVGIcon(MONO + "handyman.svg", false);
    public static final SVGIcon HELP = new SVGIcon(MONO + "help.svg", false);
    public static final SVGIcon HISTORY = new SVGIcon(MONO + "history.svg", false);
    public static final SVGIcon HOURGLASS = new SVGIcon(MONO + "hourglass.svg", false);
    public static final SVGIcon IMAGE = new SVGIcon(MONO + "image.svg", false);
    public static final SVGIcon IMAGE_ASPECT_RATIO = new SVGIcon(MONO + "image_aspect_ratio.svg", false);
    public static final SVGIcon INDETERMINATE_QUESTION = new SVGIcon(MONO + "indeterminate_question.svg", false);
    public static final SVGIcon INFO = new SVGIcon(MONO + "info.svg", false);
    public static final SVGIcon INVERT_COLORS = new SVGIcon(MONO + "invert_colors.svg", false);
    public static final SVGIcon KEYBOARD = new SVGIcon(MONO + "keyboard.svg", false);
    public static final SVGIcon KEYBOARD_ARROW_DOWN = new SVGIcon(MONO + "keyboard_arrow_down.svg", false);
    public static final SVGIcon KEYBOARD_ARROW_LEFT = new SVGIcon(MONO + "keyboard_arrow_left.svg", false);
    public static final SVGIcon KEYBOARD_ARROW_RIGHT = new SVGIcon(MONO + "keyboard_arrow_right.svg", false);
    public static final SVGIcon KEYBOARD_ARROW_UP = new SVGIcon(MONO + "keyboard_arrow_up.svg", false);
    public static final SVGIcon LAPS = new SVGIcon(MONO + "laps.svg", false);
    public static final SVGIcon LAST_PAGE = new SVGIcon(MONO + "last_page.svg", false);
    public static final SVGIcon LAYERS = new SVGIcon(MONO + "layers.svg", false);
    public static final SVGIcon LAYERS_CLEAR = new SVGIcon(MONO + "layers_clear.svg", false);
    public static final SVGIcon LIGHT_MODE = new SVGIcon(MONO + "light_mode.svg", false);
    public static final SVGIcon LINE = new SVGIcon(MONO + "line.svg", false);
    public static final SVGIcon LOCK = new SVGIcon(MONO + "lock.svg", false);
    public static final SVGIcon LOCK_OPEN = new SVGIcon(MONO + "lock_open.svg", false);
    public static final SVGIcon MAGIC_WAND = new SVGIcon(MONO + "magic_wand.svg", false);
    public static final SVGIcon MEASURE_CENTIMETER = new SVGIcon(MONO + "measure_centimeter.svg", false);
    public static final SVGIcon MY_LOCATION = new SVGIcon(MONO + "my_location.svg", false);
    public static final SVGIcon NORTH = new SVGIcon(MONO + "north.svg", false);
    public static final SVGIcon NORTH_EAST = new SVGIcon(MONO + "north_east.svg", false);
    public static final SVGIcon NORTH_WEST = new SVGIcon(MONO + "north_west.svg", false);
    public static final SVGIcon NOTE_ADD = new SVGIcon(MONO + "note_add.svg", false);
    public static final SVGIcon OPEN_IN_NEW = new SVGIcon(MONO + "open_in_new.svg", false);
    public static final SVGIcon PAUSE_CIRCLE = new SVGIcon(MONO + "pause_circle.svg", false);
    public static final SVGIcon PENTAGON = new SVGIcon(MONO + "pentagon.svg", false);
    public static final SVGIcon POINT = new SVGIcon(MONO + "point.svg", false);
    public static final SVGIcon PHOTO_CAMERA = new SVGIcon(MONO + "photo_camera.svg", false);
    public static final SVGIcon PHOTO_LIBRARY = new SVGIcon(MONO + "photo_library.svg", false);
    public static final SVGIcon PICTURE_IN_PICTURE = new SVGIcon(MONO + "picture_in_picture.svg", false);
    public static final SVGIcon PLAY_CIRCLE = new SVGIcon(MONO + "play_circle.svg", false);
    public static final SVGIcon POINT_SCAN = new SVGIcon(MONO + "point_scan.svg", false);
    public static final SVGIcon POWER_SETTINGS_NEW = new SVGIcon(MONO + "power_settings_new.svg", false);
    public static final SVGIcon RADIO_BUTTON_CHECKED = new SVGIcon(MONO + "radio_button_checked.svg", false);
    public static final SVGIcon RADIO_BUTTON_PARTIAL = new SVGIcon(MONO + "radio_button_partial.svg", false);
    public static final SVGIcon RADIO_BUTTON_UNCHECKED = new SVGIcon(MONO + "radio_button_unchecked.svg", false);
    public static final SVGIcon RECENTER = new SVGIcon(MONO + "recenter.svg", false);
    public static final SVGIcon RECTANGLE = new SVGIcon(MONO + "rectangle.svg", false);
    public static final SVGIcon REDO = new SVGIcon(MONO + "redo.svg", false);
    public static final SVGIcon REMOVE = new SVGIcon(MONO + "remove.svg", false);
    public static final SVGIcon REPEAT = new SVGIcon(MONO + "repeat.svg", false);
    public static final SVGIcon REPEAT_ON = new SVGIcon(MONO + "repeat_on.svg", false);
    public static final SVGIcon REPLAY = new SVGIcon(MONO + "replay.svg", false);
    public static final SVGIcon ROI_DILATE = new SVGIcon(MONO + "roi_dilate.svg", false);
    public static final SVGIcon ROI_DISTANCE_MAP = new SVGIcon(MONO + "roi_distance_map.svg", false);
    public static final SVGIcon ROI_ERODE = new SVGIcon(MONO + "roi_erode.svg", false);
    public static final SVGIcon ROI_EXTERIOR = new SVGIcon(MONO + "roi_exterior.svg", false);
    public static final SVGIcon ROI_INTERIOR = new SVGIcon(MONO + "roi_interior.svg", false);
    public static final SVGIcon ROI_SPLIT = new SVGIcon(MONO + "roi_split.svg", false);
    public static final SVGIcon ROTATE_LEFT = new SVGIcon(MONO + "rotate_left.svg", false);
    public static final SVGIcon ROTATE_RIGHT = new SVGIcon(MONO + "rotate_right.svg", false);
    public static final SVGIcon RULER = new SVGIcon(MONO + "ruler.svg", false);
    public static final SVGIcon SAVE = new SVGIcon(MONO + "save.svg", false);
    public static final SVGIcon SAVE_AS = new SVGIcon(MONO + "save_as.svg", false);
    public static final SVGIcon SEARCH = new SVGIcon(MONO + "search.svg", false);
    public static final SVGIcon SETTINGS = new SVGIcon(MONO + "settings.svg", false);
    public static final SVGIcon SETTINGS_PHOTO_CAMERA = new SVGIcon(MONO + "settings_photo_camera.svg", false);
    public static final SVGIcon SETTINGS_VIDEO_CAMERA = new SVGIcon(MONO + "settings_video_camera.svg", false);
    public static final SVGIcon SHADING = new SVGIcon(MONO + "shading.svg", false);
    public static final SVGIcon SKIP_NEXT = new SVGIcon(MONO + "skip_next.svg", false);
    public static final SVGIcon SKIP_PREVIOUS = new SVGIcon(MONO + "skip_previous.svg", false);
    public static final SVGIcon SOUTH = new SVGIcon(MONO + "south.svg", false);
    public static final SVGIcon SOUTH_EAST = new SVGIcon(MONO + "south_east.svg", false);
    public static final SVGIcon SOUTH_WEST = new SVGIcon(MONO + "south_west.svg", false);
    public static final SVGIcon STAR = new SVGIcon(MONO + "star.svg", false);
    public static final SVGIcon STOP_CIRCLE = new SVGIcon(MONO + "stop_circle.svg", false);
    public static final SVGIcon STROKE_FULL = new SVGIcon(MONO + "stroke_full.svg", false);
    public static final SVGIcon SWITCH_ACCESS_2 = new SVGIcon(MONO + "switch_access_2.svg", false);
    public static final SVGIcon TIMELINE = new SVGIcon(MONO + "timeline.svg", false);
    public static final SVGIcon TV_OPTIONS_INPUT_SETTINGS = new SVGIcon(MONO + "tv_options_input_settings.svg", false);
    public static final SVGIcon UNDO = new SVGIcon(MONO + "undo.svg", false);
    public static final SVGIcon UNFOLD_LESS = new SVGIcon(MONO + "unfold_less.svg", false);
    public static final SVGIcon UNFOLD_MORE = new SVGIcon(MONO + "unfold_more.svg", false);
    public static final SVGIcon UNION = new SVGIcon(MONO + "union.svg", false);
    public static final SVGIcon UPDATE = new SVGIcon(MONO + "update.svg", false);
    public static final SVGIcon UPDATE_DISABLED = new SVGIcon(MONO + "update_disabled.svg", false);
    public static final SVGIcon VERTICAL_ALIGN_TOP = new SVGIcon(MONO + "vertical_align_top.svg", false);
    public static final SVGIcon VIDEOCAM = new SVGIcon(MONO + "videocam.svg", false);
    public static final SVGIcon VIEW_COLUMN = new SVGIcon(MONO + "view_column.svg", false);
    public static final SVGIcon VIEW_MODULE = new SVGIcon(MONO + "view_module.svg", false);
    public static final SVGIcon VIEW_QUILT = new SVGIcon(MONO + "view_quilt.svg", false);
    public static final SVGIcon VIEW_STREAM = new SVGIcon(MONO + "view_stream.svg", false);
    public static final SVGIcon VISIBILITY = new SVGIcon(MONO + "visibility.svg", false);
    public static final SVGIcon VISIBILITY_OFF = new SVGIcon(MONO + "visibility_off.svg", false);
    public static final SVGIcon WARNING = new SVGIcon(MONO + "warning.svg", false);
    public static final SVGIcon WEST = new SVGIcon(MONO + "west.svg", false);
    public static final SVGIcon WIDGETS = new SVGIcon(MONO + "widgets.svg", false);
    public static final SVGIcon ZOOM_IN = new SVGIcon(MONO + "zoom_in.svg", false);
    public static final SVGIcon ZOOM_OUT = new SVGIcon(MONO + "zoom_out.svg", false);
    public static final SVGIcon ZOOM_OUT_MAP = new SVGIcon(MONO + "zoom_out_map.svg", false);

    // Colored icons
    public static final SVGIcon ICY_TRANSPARENT = new SVGIcon(COLOR + "icy_transparent.svg", true);
    public static final SVGIcon ICY_WHITE_BG = new SVGIcon(COLOR + "icy_white_bg.svg", true);
    public static final SVGIcon ARGB_IMAGE = new SVGIcon(COLOR + "argb_image.svg", true);
    public static final SVGIcon GRAYSCALE_IMAGE = new SVGIcon(COLOR + "grayscale_image.svg", true);
    public static final SVGIcon RGB_IMAGE = new SVGIcon(COLOR + "rgb_image.svg", true);

    private @Nullable URI uri;
    private final boolean colored;

    /**
     * Create an SVG icon from resource folder.
     * @param path the resource path in String.
     * @param colored false if the SVG is monochrome, allow the sytem to change the color automatically according to the theme.
     */
    public SVGIcon(final @NotNull String path, final boolean colored) {
        this(Objects.requireNonNull(SVGIcon.class.getResource(path)), colored);
    }

    /**
     * Create an SVG icon from resource folder.
     * @param url the resource path in URL.
     * @param colored false if the SVG is monochrome, allow the sytem to change the color automatically according to the theme.
     */
    public SVGIcon(final @NotNull URL url, final boolean colored) {
        try {
            uri = url.toURI();
        }
        catch (final URISyntaxException e) {
            IcyLogger.warn(SVGIcon.class, e, "Unable to get URI from resource: " + url);
            uri = null;
        }
        finally {
            this.colored = colored;
        }
    }

    public final @Nullable URI getURI() {
        return uri;
    }

    public final boolean isColored() {
        return colored;
    }
}
