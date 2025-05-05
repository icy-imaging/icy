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

package org.bioimageanalysis.icy.io;

import loci.formats.gui.ExtensionFileFilter;

/**
 * Define some default image file format for Icy.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public enum ImageFileFormat {
    TIFF {
        @Override
        public String[] getExtensions() {
            return new String[]{"tif", "tiff"};
        }

        @Override
        public String getDescription() {
            return "TIFF images";
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public boolean canWrite() {
            return true;
        }
    },
    PNG {
        @Override
        public String[] getExtensions() {
            return new String[]{"png"};
        }

        @Override
        public String getDescription() {
            return "PNG images";
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public boolean canWrite() {
            return true;
        }
    },
    LSM {
        @Override
        public String[] getExtensions() {
            return new String[]{"lsm"};
        }

        @Override
        public String getDescription() {
            return "LSM images";
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public boolean canWrite() {
            return false;
        }
    },
    JPG {
        @Override
        public String[] getExtensions() {
            return new String[]{"jpg", "jpeg"};
        }

        @Override
        public String getDescription() {
            return "JPG images";
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public boolean canWrite() {
            return true;
        }
    },
    AVI {
        @Override
        public String[] getExtensions() {
            return new String[]{"avi"};
        }

        @Override
        public String getDescription() {
            return "AVI sequences";
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public boolean canWrite() {
            return true;
        }
    };

    /**
     * Returns true if the image file format supports read operation.
     */
    public abstract boolean canRead();

    /**
     * Returns true if the image file format supports write operation.
     */
    public abstract boolean canWrite();

    /**
     * Returns the image file format description.
     */
    public String getDescription() {
        return "unknow";
    }

    /**
     * Returns the image file format extensions.
     */
    public String[] getExtensions() {
        return new String[]{""};
    }

    /**
     * Returns the associated {@link ExtensionFileFilter}
     */
    public ExtensionFileFilter getExtensionFileFilter() {
        return new ExtensionFileFilter(getExtensions(), getDescription());
    }

    /**
     * Return true if the specified extension matches this format.<br>
     * <code>defaultValue</code> is returned if no matching format is found (it can be null).
     */
    public boolean matches(final String ext) {
        if (ext == null)
            return false;

        // always consider lower case extension
        final String extLC = ext.toLowerCase();

        for (final String e : getExtensions())
            if (e.equals(extLC))
                return true;

        return false;
    }

    /**
     * Returns the FileFormat corresponding to specified extension.<br>
     * <code>defaultValue</code> is returned if no matching format is found.
     */
    public static ImageFileFormat getFormat(final String ext, final ImageFileFormat defaultValue) {
        for (final ImageFileFormat iff : values())
            if (iff.matches(ext))
                return iff;

        return defaultValue;
    }

    /**
     * Returns the {@link ImageFileFormat} corresponding to the specified extension and which
     * support read operation.<br>
     * <code>defaultValue</code> is returned if no matching format is found.
     */
    public static ImageFileFormat getReadFormat(final String ext, final ImageFileFormat defaultValue) {
        for (final ImageFileFormat iff : values())
            if (iff.canRead() && iff.matches(ext))
                return iff;

        return defaultValue;
    }

    /**
     * Returns the {@link ImageFileFormat} corresponding to the specified extension and which
     * support write operation.<br>
     * <code>defaultValue</code> is returned if no matching format is found.
     */
    public static ImageFileFormat getWriteFormat(final String ext, final ImageFileFormat defaultValue) {
        for (final ImageFileFormat iff : values())
            if (iff.canWrite() && iff.matches(ext))
                return iff;

        return defaultValue;
    }
}
