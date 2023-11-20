/*
 * Copyright (c) 2010-2023. Institut Pasteur.
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

package icy.common;

import icy.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Version class.<br>
 * This class is used to describe a version number encoded on 3 digits.<br>
 *
 * @author Stephane
 * @author Thomas MUSSET
 */
public class Version implements Comparable<Version> {
    public enum Snapshot {
        NONE,
        ALPHA("a", "alpha"),
        BETA("b", "beta"),
        RELEASE_CANDIDATE("rc", "release-candidate");

        @NotNull
        private final String shortLabel;
        @NotNull
        private final String longLabel;

        Snapshot() {
            this("", "");
        }

        Snapshot(@NotNull final String shortLabel, @NotNull final String longLabel) {
            this.shortLabel = shortLabel;
            this.longLabel = longLabel;
        }

        @NotNull
        public final String getShortLabel() {
            return shortLabel;
        }

        @NotNull
        public final String getLongLabel() {
            return longLabel;
        }

        @Override
        public String toString() {
            return longLabel;
        }
    }

    private int major; // TODO: 02/11/2023 Make major final
    private int minor; // TODO: 02/11/2023 Make minor final
    private int patch; // TODO: 02/11/2023 Make patch final

    @NotNull
    private Snapshot snapshot;

    public Version() {
        this(0, 0, 0, Snapshot.NONE);
    }

    public Version(final int major) {
        this(major, 0, 0, Snapshot.NONE);
    }

    public Version(final int major, final int minor) {
        this(major, minor, 0, Snapshot.NONE);
    }

    public Version(final int major, final int minor, final int patch) {
        this(major, minor, patch, Snapshot.NONE);
    }

    /**
     * @deprecated Use {@link #Version(int, int, int)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public Version(final int major, final int minor, final int revision, final int build) {
        this(major, minor, revision, build, false);
    }

    /**
     * @deprecated Use {@link #Version(int, int, int, Snapshot)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public Version(final int major, final int minor, final int revision, final int build, final boolean beta) {
        this(major, minor, revision, (beta) ? Snapshot.BETA : Snapshot.NONE);
    }

    public Version(final int major, final int minor, final int patch, @NotNull final Snapshot snapshot) {
        super();

        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.snapshot = snapshot;
    }

    /**
     * @deprecated Use {@link #fromString(String)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public Version(@NotNull final String version) {
        this();

        final String lower = version.toLowerCase(Locale.ROOT).replaceAll("[0-9 \\-_]", "");

        if (lower.equals(Snapshot.RELEASE_CANDIDATE.getShortLabel()) || lower.equals(Snapshot.RELEASE_CANDIDATE.getLongLabel()))
            snapshot = Snapshot.RELEASE_CANDIDATE;
        else if (lower.equals(Snapshot.BETA.getShortLabel()) || lower.equals(Snapshot.BETA.getLongLabel()))
            snapshot = Snapshot.BETA;
        else if (lower.equals(Snapshot.ALPHA.getShortLabel()) || lower.equals(Snapshot.ALPHA.getLongLabel()))
            snapshot = Snapshot.ALPHA;

        final String[] values = version.replaceAll("[a-zA-Z \\-]", "").split("\\.");

        if ((values.length > 0) && (!StringUtil.isEmpty(values[0], true)))
            major = Integer.parseInt(values[0]);
        if ((values.length > 1) && (!StringUtil.isEmpty(values[1], true)))
            minor = Integer.parseInt(values[1]);
        if ((values.length > 2) && (!StringUtil.isEmpty(values[2], true)))
            patch = Integer.parseInt(values[2]);
    }

    @NotNull
    public static Version fromString(@NotNull final String version) {
        final String lower = version.toLowerCase(Locale.ROOT).replaceAll("[0-9 \\-_]", "");

        final Snapshot snapshot;
        if (lower.equals(Snapshot.RELEASE_CANDIDATE.getShortLabel()) || lower.equals(Snapshot.RELEASE_CANDIDATE.getLongLabel()))
            snapshot = Snapshot.RELEASE_CANDIDATE;
        else if (lower.equals(Snapshot.BETA.getShortLabel()) || lower.equals(Snapshot.BETA.getLongLabel()))
            snapshot = Snapshot.BETA;
        else if (lower.equals(Snapshot.ALPHA.getShortLabel()) || lower.equals(Snapshot.ALPHA.getLongLabel()))
            snapshot = Snapshot.ALPHA;
        else
            snapshot = Snapshot.NONE;

        final String[] values = version.replaceAll("[a-zA-Z \\-]", "").split("\\.");

        int major = 0;
        if ((values.length > 0) && (!StringUtil.isEmpty(values[0], true)))
            major = Integer.parseInt(values[0]);

        int minor = 0;
        if ((values.length > 1) && (!StringUtil.isEmpty(values[1], true)))
            minor = Integer.parseInt(values[1]);

        int patch = 0;
        if ((values.length > 2) && (!StringUtil.isEmpty(values[2], true)))
            patch = Integer.parseInt(values[2]);

        return new Version(major, minor, patch, snapshot);
    }

    public int getMajor() {
        return major;
    }

    /**
     * @deprecated No replacement.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setMajor(final int major) {
        // Version is write protected
    }

    public int getMinor() {
        return minor;
    }

    /**
     * @deprecated No replacement.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setMinor(final int minor) {
        // Version is write protected
    }

    public int getPatch() {
        return patch;
    }

    /**
     * @deprecated Use {@link #getPatch()} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public int getRevision() {
        return patch;
    }

    /**
     * @deprecated No replacement.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setRevision(final int revision) {
        // Version is write protected
    }

    /**
     * @deprecated No replacement.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public int getBuild() {
        return 0;
    }

    /**
     * @deprecated No replacement.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setBuild(final int build) {
        // Version is write protected
    }

    /**
     * @deprecated Use {@link #isSnapshot()} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean isBeta() {
        return isSnapshot();
    }

    /**
     * @deprecated No replacement.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setBeta(final boolean beta) {
        // Version is write protected
    }

    public boolean isSnapshot() {
        return snapshot != Snapshot.NONE;
    }

    @NotNull
    public Snapshot getSnapshot() {
        return snapshot;
    }

    /**
     * @return True if equals '0.0.0' and not a snapshot.
     */
    public boolean isEmpty() {
        return (major == 0) && (minor == 0) && (patch == 0) && (snapshot == Snapshot.NONE);
    }

    @NotNull
    public String toShortString() {
        if (isEmpty())
            return "0";

        String version = String.format(
                "%d.%d.%d",
                major,
                minor,
                patch
        );

        if (isSnapshot())
            version += String.format("%s", snapshot.getShortLabel());

        return version;
    }

    @NotNull
    @Override
    public String toString() {
        if (isEmpty())
            return "0";

        String version = String.format(
                "%d.%d.%d",
                major,
                minor,
                patch
        );

        if (isSnapshot())
            version += String.format("-%s", snapshot.getLongLabel());

        return version;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj instanceof Version)
            return compareTo((Version) obj) == 0;

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        // assume 8 bits for each number (0-255 range)
        return (major/* << 0*/) | (minor << 8) | (patch << 16) | (snapshot.ordinal() << 24);
    }

    /**
     * Compares this version with the specified version for order.
     * Returns a negative integer, zero, or a positive integer as this version is less than, equal to, or greater than the specified version.
     *
     * @param version the object to be compared.
     * @return negative if lower, zero if equal or positive if greater.
     */
    @Override
    public int compareTo(@NotNull final Version version) {
        if (version.isEmpty() || isEmpty())
            return 0;
        else if (version.major < major)
            return 1;
        else if (version.major > major)
            return -1;
        else if (version.minor < minor)
            return 1;
        else if (version.minor > minor)
            return -1;
        else if (version.patch < patch)
            return 1;
        else if (version.patch > patch)
            return -1;
        else if (version.isSnapshot() && !isSnapshot())
            return 1;
        else if (!version.isSnapshot() && isSnapshot())
            return -1;
        else
            return 0;
    }

    public boolean isGreater(@NotNull final Version version) {
        return compareTo(version) > 0;
    }

    public boolean isGreaterOrEqual(@NotNull final Version version) {
        return compareTo(version) >= 0;
    }

    public boolean isLower(@NotNull final Version version) {
        return compareTo(version) < 0;
    }

    public boolean isLowerOrEqual(@NotNull final Version version) {
        return compareTo(version) <= 0;
    }

    /**
     * @deprecated Use {@link #isGreater(Version)}
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean isNewer(@NotNull final Version version) {
        return isGreater(version);
    }

    /**
     * @deprecated Use {@link #isGreaterOrEqual(Version)}
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean isNewerOrEqual(@NotNull final Version version) {
        return isGreaterOrEqual(version);
    }

    /**
     * @deprecated Use {@link #isLower(Version)}
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean isOlder(@NotNull final Version version) {
        return isLower(version);
    }

    /**
     * @deprecated Use {@link #isLowerOrEqual(Version)}
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean isOlderOrEqual(@NotNull final Version version) {
        return isLowerOrEqual(version);
    }
}
