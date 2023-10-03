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

import javax.annotation.Nonnull;
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

        private final String shortLabel;
        private final String longLabel;

        Snapshot() {
            this("", "");
        }

        Snapshot(@Nonnull final String shortLabel, @Nonnull final String longLabel) {
            this.shortLabel = shortLabel;
            this.longLabel = longLabel;
        }

        public final String getShortLabel() {
            return shortLabel;
        }

        public final String getLongLabel() {
            return longLabel;
        }

        @Override
        public String toString() {
            return longLabel;
        }
    }

    private int major;
    private int minor;
    private int patch;
    @Deprecated(since = "3.0.0", forRemoval = true)
    private int build;
    @Deprecated(since = "3.0.0", forRemoval = true)
    private boolean beta;
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
        super();

        this.major = major;
        this.minor = minor;
        this.patch = revision;
        this.build = build;
        this.beta = beta;
        this.snapshot = (beta) ? Snapshot.BETA : Snapshot.NONE;
    }

    public Version(final int major, final int minor, final int patch, @Nonnull final Snapshot snapshot) {
        super();

        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.build = 0;
        this.beta = false;
        this.snapshot = snapshot;
    }

    /**
     * @deprecated Use {@link #fromString(String)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public Version(@Nonnull final String version) {
        this(0, 0, 0, Snapshot.NONE);

        final String lower = version.toLowerCase(Locale.ROOT);

        if (lower.contains(Snapshot.ALPHA.getShortLabel()) || lower.contains(Snapshot.ALPHA.getLongLabel()))
            snapshot = Snapshot.ALPHA;
        else if (lower.contains(Snapshot.BETA.getShortLabel()) || lower.contains(Snapshot.BETA.getLongLabel()))
            snapshot = Snapshot.BETA;
        else if (lower.contains(Snapshot.RELEASE_CANDIDATE.getShortLabel()) || lower.contains(Snapshot.RELEASE_CANDIDATE.getLongLabel()))
            snapshot = Snapshot.RELEASE_CANDIDATE;

        final String[] values = version.replaceAll("[a-zA-Z \\-]", "").split("\\.");

        if ((values.length > 0) && (!StringUtil.isEmpty(values[0], true)))
            major = Integer.parseInt(values[0]);
        if ((values.length > 1) && (!StringUtil.isEmpty(values[1], true)))
            major = Integer.parseInt(values[1]);
        if ((values.length > 2) && (!StringUtil.isEmpty(values[2], true)))
            patch = Integer.parseInt(values[2]);
    }

    public static Version fromString(@Nonnull final String version) {
        final String lower = version.toLowerCase(Locale.ROOT);

        final Snapshot snapshot;
        if (lower.contains(Snapshot.ALPHA.getShortLabel()) || lower.contains(Snapshot.ALPHA.getLongLabel()))
            snapshot = Snapshot.ALPHA;
        else if (lower.contains(Snapshot.BETA.getShortLabel()) || lower.contains(Snapshot.BETA.getLongLabel()))
            snapshot = Snapshot.BETA;
        else if (lower.contains(Snapshot.RELEASE_CANDIDATE.getShortLabel()) || lower.contains(Snapshot.RELEASE_CANDIDATE.getLongLabel()))
            snapshot = Snapshot.RELEASE_CANDIDATE;
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

    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setMajor(final int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setMinor(final int minor) {
        this.minor = minor;
    }

    public int getPatch() {
        return patch;
    }

    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setPatch(final int patch) {
        this.patch = patch;
    }

    /**
     * @deprecated Use {@link #getPatch()} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public int getRevision() {
        return patch;
    }

    /**
     * @deprecated Use {@link #setPatch(int)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setRevision(final int revision) {
        this.patch = revision;
    }

    @Deprecated(since = "3.0.0", forRemoval = true)
    public int getBuild() {
        return build;
    }

    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setBuild(final int build) {
        this.build = build;
    }

    /**
     * @deprecated Use {@link #isSnapshot()} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean isBeta() {
        return beta;
    }

    /**
     * @deprecated Use {@link #setSnapshot(Snapshot)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setBeta(final boolean beta) {
        this.beta = beta;
    }

    public boolean isSnapshot() {
        return snapshot != Snapshot.NONE;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setSnapshot(@Nonnull final Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * special isEmpty case (0.0.0.0)
     */
    public boolean isEmpty() {
        //return (major == 0) && (minor == 0) && (revision == 0) && (build == 0) && !beta;
        return (major == 0) && (minor == 0) && (patch == 0) && (snapshot == Snapshot.NONE);
    }

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
    public boolean equals(final Object obj) {
        if (obj instanceof Version)
            return compareTo((Version) obj) == 0;

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        // assume 8 bits for each number (0-255 range)
        return (major << 0) | (minor << 8) | (patch << 16) | (snapshot.ordinal() << 24);
    }

    /**
     * Compares this version with the specified version for order.
     * Returns a negative integer, zero, or a positive integer as this version is less than, equal to, or greater than the specified version.
     *
     * @param version the object to be compared.
     * @return negative if lower, zero if equal or positive if greater.
     */
    @Override
    public int compareTo(@Nonnull final Version version) {
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

    public boolean isGreater(@Nonnull final Version version) {
        return compareTo(version) > 0;
    }

    public boolean isGreaterOrEqual(@Nonnull final Version version) {
        return compareTo(version) >= 0;
    }

    public boolean isLower(@Nonnull final Version version) {
        return compareTo(version) < 0;
    }

    public boolean isLowerOrEqual(@Nonnull final Version version) {
        return compareTo(version) <= 0;
    }

    /**
     * @deprecated Use {@link #isGreater(Version)}
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean isNewer(final Version version) {
        return isGreater(version);
    }

    /**
     * @deprecated Use {@link #isGreaterOrEqual(Version)}
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean isNewerOrEqual(final Version version) {
        return isGreaterOrEqual(version);
    }

    /**
     * @deprecated Use {@link #isLower(Version)}
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean isOlder(final Version version) {
        return isLower(version);
    }

    /**
     * @deprecated Use {@link #isLowerOrEqual(Version)}
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean isOlderOrEqual(final Version version) {
        return isLowerOrEqual(version);
    }
}
