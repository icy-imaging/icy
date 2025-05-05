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

package org.bioimageanalysis.icy.common;

import org.bioimageanalysis.icy.common.string.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Version class.<br>
 * This class is used to describe a version number encoded on 3 digits.<br>
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class Version implements Comparable<Version> {
    public enum DevelopmentStage {
        RELEASE,
        ALPHA("a", "alpha"),
        BETA("b", "beta"),
        RELEASE_CANDIDATE("rc", "release-candidate");

        private final @NotNull String shortLabel;
        private final @NotNull String longLabel;

        @Contract(pure = true)
        DevelopmentStage() {
            this("", "");
        }

        @Contract(pure = true)
        DevelopmentStage(final @NotNull String shortLabel, final @NotNull String longLabel) {
            this.shortLabel = shortLabel;
            this.longLabel = longLabel;
        }

        @Contract(pure = true)
        public final @NotNull String getShortLabel() {
            return shortLabel;
        }

        @Contract(pure = true)
        public final @NotNull String getLongLabel() {
            return longLabel;
        }

        @Contract(pure = true)
        @Override
        public String toString() {
            return longLabel;
        }
    }

    private final int major;
    private final int minor;
    private final int patch;
    private final int revision;

    private final @NotNull Version.DevelopmentStage developmentStage;

    @Contract(pure = true)
    public Version() {
        this(0, 0, 0, DevelopmentStage.RELEASE, 0);
    }

    @Contract(pure = true)
    public Version(final int major) {
        this(major, 0, 0, DevelopmentStage.RELEASE, 0);
    }

    @Contract(pure = true)
    public Version(final int major, final int minor) {
        this(major, minor, 0, DevelopmentStage.RELEASE, 0);
    }

    @Contract(pure = true)
    public Version(final int major, final int minor, final int patch) {
        this(major, minor, patch, DevelopmentStage.RELEASE, 0);
    }

    @Contract(pure = true)
    public Version(final int major, final int minor, final int patch, final @NotNull DevelopmentStage developmentStage) {
        this(major, minor, patch, developmentStage, 0);
    }

    @Contract(pure = true)
    public Version(final int major, final int minor, final int patch, final @NotNull Version.DevelopmentStage developmentStage, final int revision) {
        super();

        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.developmentStage = developmentStage;
        this.revision = revision;
    }

    /**
     * Return a new Version from a String with this format: 1.2.3-a.5 (major: 1, minor: 2, patch: 3, development stage: alpha, revision: 5), it's like 1.2.3.5 alpha.<br>
     * There are 4 different development stages :
     * <ul>
     *     <li>Release (x.x.x)</li>
     *     <li>Alpha (x.x.x-a.x)</li>
     *     <li>Beta (x.x.x-b.x)</li>
     *     <li>Release candidate (x.x.x-rc.x)</li>
     * </ul>
     */
    @Contract("_ -> new")
    public static @NotNull Version fromString(final @NotNull String version) {
        final String lower = version.toLowerCase(Locale.ROOT).replaceAll("[0-9 .\\-_]", "");

        final DevelopmentStage developmentStage;
        if (lower.equals(DevelopmentStage.RELEASE_CANDIDATE.getShortLabel()) || lower.equals(DevelopmentStage.RELEASE_CANDIDATE.getLongLabel()))
            developmentStage = DevelopmentStage.RELEASE_CANDIDATE;
        else if (lower.equals(DevelopmentStage.BETA.getShortLabel()) || lower.equals(DevelopmentStage.BETA.getLongLabel()))
            developmentStage = DevelopmentStage.BETA;
        else if (lower.equals(DevelopmentStage.ALPHA.getShortLabel()) || lower.equals(DevelopmentStage.ALPHA.getLongLabel()))
            developmentStage = DevelopmentStage.ALPHA;
        else
            developmentStage = DevelopmentStage.RELEASE;

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

        int stage = 0;
        if ((values.length > 3) && (!StringUtil.isEmpty(values[3], true)))
            stage = Integer.parseInt(values[3]);

        return new Version(major, minor, patch, developmentStage, stage);
    }

    @Contract(pure = true)
    public int getMajor() {
        return major;
    }

    @Contract(pure = true)
    public int getMinor() {
        return minor;
    }

    @Contract(pure = true)
    public int getPatch() {
        return patch;
    }

    @Contract(pure = true)
    public boolean isNotRelease() {
        return developmentStage != DevelopmentStage.RELEASE;
    }

    @Contract(pure = true)
    public boolean isRelease() {
        return developmentStage == DevelopmentStage.RELEASE;
    }

    @Contract(pure = true)
    public @NotNull Version.DevelopmentStage getDevelopmentStage() {
        return developmentStage;
    }

    @Contract(pure = true)
    public int getRevision() {
        return revision;
    }

    /**
     * @return True if equals '0.0.0' and not a snapshot.
     */
    @Contract(pure = true)
    public boolean isEmpty() {
        return (major == 0) && (minor == 0) && (patch == 0) && (developmentStage == DevelopmentStage.RELEASE);
    }

    public @NotNull String toShortString() {
        if (isEmpty())
            return "0";

        String version = String.format(
                "%d.%d.%d",
                major,
                minor,
                patch
        );

        if (isNotRelease()) {
            version += String.format("-%s", developmentStage.getShortLabel());
            if (revision > 0)
                version += String.format(".%d", revision);
        }

        return version;
    }

    @Override
    public @NotNull String toString() {
        if (isEmpty())
            return "0";

        String version = String.format(
                "%d.%d.%d",
                major,
                minor,
                patch
        );

        if (isNotRelease()) {
            version += String.format(" %s", developmentStage.getLongLabel());
            if (revision > 0)
                version += String.format(" %d", revision);
        }

        return version;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj instanceof Version)
            return compareTo((Version) obj) == 0;

        return super.equals(obj);
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        // assume 8 bits for each number (0-255 range)
        return (major/* << 0*/) | (minor << 8) | (patch << 16) | (developmentStage.ordinal() << 24);
    }

    /**
     * Compares this version with the specified version for order.
     * Returns a negative integer, zero, or a positive integer as this version is less than, equal to, or greater than the specified version.
     *
     * @param version the object to be compared.
     * @return negative if lower, zero if equal or positive if greater.
     */
    @Override
    public int compareTo(final @NotNull Version version) {
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
        else if (version.developmentStage.ordinal() < developmentStage.ordinal())
            return 1;
        else if (version.developmentStage.ordinal() > developmentStage.ordinal())
            return -1;
        else if (version.revision < revision)
            return 1;
        else if (version.revision > revision)
            return -1;
        else
            return 0;
    }

    @Contract(pure = true)
    public boolean isGreater(final @NotNull Version version) {
        return compareTo(version) > 0;
    }

    @Contract(pure = true)
    public boolean isGreaterOrEqual(final @NotNull Version version) {
        return compareTo(version) >= 0;
    }

    @Contract(pure = true)
    public boolean isLower(final @NotNull Version version) {
        return compareTo(version) < 0;
    }

    @Contract(pure = true)
    public boolean isLowerOrEqual(final @NotNull Version version) {
        return compareTo(version) <= 0;
    }
}
