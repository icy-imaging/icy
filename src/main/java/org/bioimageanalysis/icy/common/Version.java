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

package org.bioimageanalysis.icy.common;

import org.bioimageanalysis.icy.common.string.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents a semantic versioning object consisting of major, minor, patch numbers,
 * development stage, revision, and snapshot state.
 * <p>
 * The {@code Version} class implements the {@code Comparable} interface to allow
 * comparison between different version instances.
 * <p>
 * Instances of this class are immutable and thread-safe.
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public final class Version implements Comparable<Version> {
    /**
     * Represents a stage in the development lifecycle of a software version.
     * The stages include predefined phases such as RELEASE, ALPHA, BETA,
     * and RELEASE_CANDIDATE, each of which may include a short and long label
     * for identification purposes.
     */
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
    private final boolean isSnapshot;

    private final @NotNull Version.DevelopmentStage developmentStage;

    /**
     * Constructs a new {@link Version} object using default values.
     * <p>
     * Creates a {@link Version} instance with the following default configurations:
     * <ul>
     *  <li>Major number set to 0.</li>
     *  <li>Minor number set to 0.</li>
     *  <li>Patch number set to 0.</li>
     *  <li>Development stage set to {@link DevelopmentStage#RELEASE}.</li>
     *  <li>Revision number set to 0.</li>
     *  <li>Snapshot status set to {@code false}.</li>
     * </ul>
     */
    @Contract(pure = true)
    public Version() {
        this(0, 0, 0, DevelopmentStage.RELEASE, 0, false);
    }

    /**
     * Constructs a new {@link Version} object with the specified major version number.
     * <p>
     * Other version details will be initialized with the following default values:
     * <ul>
     *  <li>Minor number set to 0.</li>
     *  <li>Patch number set to 0.</li>
     *  <li>Development stage set to {@link DevelopmentStage#RELEASE}.</li>
     *  <li>Revision number set to 0.</li>
     *  <li>Snapshot status set to {@code false}.</li>
     * </ul>
     *
     * @param major the major version number.
     */
    @Contract(pure = true)
    public Version(final int major) {
        this(major, 0, 0, DevelopmentStage.RELEASE, 0, false);
    }

    /**
     * Constructs a new {@link Version} object with the specified major and minor version numbers.
     * <p>
     * Other version details will be initialized with the following default values:
     * <ul>
     *  <li>Patch number set to 0.</li>
     *  <li>Development stage set to {@link DevelopmentStage#RELEASE}.</li>
     *  <li>Revision number set to 0.</li>
     *  <li>Snapshot status set to {@code false}.</li>
     * </ul>
     *
     * @param major the major version number.
     * @param minor the minor version number.
     */
    @Contract(pure = true)
    public Version(final int major, final int minor) {
        this(major, minor, 0, DevelopmentStage.RELEASE, 0, false);
    }

    /**
     * Constructs a new {@link Version} object with the specified major, minor, and patch version numbers.
     * <p>
     * Other version details will be initialized with the following default values:
     * <ul>
     *  <li>Development stage set to {@link DevelopmentStage#RELEASE}.</li>
     *  <li>Revision number set to 0.</li>
     *  <li>Snapshot status set to {@code false}.</li>
     * </ul>
     *
     * @param major the major version number.
     * @param minor the minor version number.
     * @param patch the patch version number.
     */
    @Contract(pure = true)
    public Version(final int major, final int minor, final int patch) {
        this(major, minor, patch, DevelopmentStage.RELEASE, 0, false);
    }

    /**
     * Constructs a new {@link Version} object with the specified major, minor, patch version numbers and whether it is a snapshot version.
     * <p>
     * Other version details will be initialized with the following default values:
     * <ul>
     *  <li>Development stage set to {@link DevelopmentStage#RELEASE}.</li>
     *  <li>Revision number set to 0.</li>
     * </ul>
     *
     * @param major      the major version number.
     * @param minor      the minor version number.
     * @param patch      the patch version number.
     * @param isSnapshot a boolean indicating whether this version is a snapshot.
     */
    @Contract(pure = true)
    public Version(final int major, final int minor, final int patch, final boolean isSnapshot) {
        this(major, minor, patch, DevelopmentStage.RELEASE, 0, isSnapshot);
    }

    /**
     * Constructs a new {@link Version} object with the specified major, minor, patch version numbers and {@link DevelopmentStage}.
     * <p>
     * Other version details will be initialized with the following default values:
     * <ul>
     *  <li>Revision number set to 0.</li>
     *  <li>Snapshot status set to {@code false}.</li>
     * </ul>
     *
     * @param major            the major version number.
     * @param minor            the minor version number.
     * @param patch            the patch version number.
     * @param developmentStage the development stage of the version, must not be {@code null}.
     */
    @Contract(pure = true)
    public Version(final int major, final int minor, final int patch, final @NotNull DevelopmentStage developmentStage) {
        this(major, minor, patch, developmentStage, 0, false);
    }

    /**
     * Constructs a new {@link Version} object with the specified major, minor, patch, revision version numbers and {@link DevelopmentStage}.
     * <p>
     * Other version details will be initialized with the following default values:
     * <ul>
     *  <li>Revision number set to 0.</li>
     *  <li>Snapshot status set to {@code false}.</li>
     * </ul>
     *
     * @param major            the major version number.
     * @param minor            the minor version number.
     * @param patch            the patch version number.
     * @param developmentStage the development stage of the version, must not be {@code null}.
     * @param revision         the revision number associated with the development stage.
     */
    @Contract(pure = true)
    public Version(final int major, final int minor, final int patch, final @NotNull DevelopmentStage developmentStage, final int revision) {
        this(major, minor, patch, developmentStage, revision, false);
    }

    /**
     * Constructs a new {@link Version} object with the specified version details.
     *
     * @param major            the major version number.
     * @param minor            the minor version number.
     * @param patch            the patch version number.
     * @param developmentStage the development stage of the version, must not be {@code null}.
     * @param revision         the revision number associated with the development stage.
     * @param isSnapshot       a boolean indicating whether this version is a snapshot.
     */
    @Contract(pure = true)
    public Version(final int major, final int minor, final int patch, final @NotNull Version.DevelopmentStage developmentStage, final int revision, final boolean isSnapshot) {
        super();

        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.developmentStage = developmentStage;
        this.revision = revision;
        this.isSnapshot = isSnapshot;
    }

    /**
     * Parses a version string and constructs a {@link Version} object from it.
     * <p>
     * The input string is analyzed to extract the major, minor, and patch versions,
     * along with additional metadata, such as whether the version is a snapshot
     * and the associated development stage (e.g., {@link DevelopmentStage#ALPHA}, {@link DevelopmentStage#BETA},
     * {@link DevelopmentStage#RELEASE_CANDIDATE}, or {@link DevelopmentStage#RELEASE}).
     * <p>
     * The version string is expected to follow a certain format, with optional
     * labels to specify the development stage. A "-SNAPSHOT" suffix will be
     * used to indicate whether the version is a snapshot version.
     *
     * @param version the version string to be parsed, must not be {@code null}.
     *                It should follow the format: "{major}.{minor}.{patch}-{stage}-{revision}".
     *                Stages may include {@link DevelopmentStage#ALPHA}, {@link DevelopmentStage#BETA},
     *                {@link DevelopmentStage#RELEASE_CANDIDATE}, or {@link DevelopmentStage#RELEASE}.
     *                A "-SNAPSHOT" suffix indicates a snapshot version.
     * @return a {@link Version} object representing the parsed version string. The object
     * includes information about the major, minor, and patch versions, the development
     * stage, the stage revision, and whether it is a snapshot.
     * @throws NumberFormatException if the numeric values in the version string cannot
     *                               be correctly parsed.
     * @throws NullPointerException  if the {@code version} parameter is {@code null}.
     */
    @Contract("_ -> new")
    public static @NotNull Version fromString(final @NotNull String version) throws NumberFormatException, NullPointerException {
        final boolean isSnapshot;
        final String s;
        if (version.endsWith("-SNAPSHOT")) {
            isSnapshot = true;
            s = version.substring(0, version.length() - "-SNAPSHOT".length());
        }
        else {
            s = version;
            isSnapshot = false;
        }
        final String lower = s.toLowerCase(Locale.getDefault()).replaceAll("[0-9 .\\-_]", "");

        final DevelopmentStage developmentStage;
        // Determines development stage by normalized label matching
        if (lower.equals(DevelopmentStage.RELEASE_CANDIDATE.getShortLabel()) || lower.equals(DevelopmentStage.RELEASE_CANDIDATE.getLongLabel()))
            developmentStage = DevelopmentStage.RELEASE_CANDIDATE;
        else if (lower.equals(DevelopmentStage.BETA.getShortLabel()) || lower.equals(DevelopmentStage.BETA.getLongLabel()))
            developmentStage = DevelopmentStage.BETA;
        else if (lower.equals(DevelopmentStage.ALPHA.getShortLabel()) || lower.equals(DevelopmentStage.ALPHA.getLongLabel()))
            developmentStage = DevelopmentStage.ALPHA;
        else
            developmentStage = DevelopmentStage.RELEASE;

        final String[] values = s.replaceAll("[a-zA-Z \\-]", "").split("\\.");

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

        return new Version(major, minor, patch, developmentStage, stage, isSnapshot);
    }

    /**
     * Retrieves the major version number.
     *
     * @return the major version as an integer.
     */
    @Contract(pure = true)
    public int getMajor() {
        return major;
    }

    /**
     * Retrieves the minor version number.
     *
     * @return the minor version as an integer.
     */
    @Contract(pure = true)
    public int getMinor() {
        return minor;
    }

    /**
     * Retrieves the patch version number.
     *
     * @return the patch version as an integer.
     */
    @Contract(pure = true)
    public int getPatch() {
        return patch;
    }

    /**
     * Retrieves the patch version number.
     *
     * @return the patch version as an integer.
     */
    @Contract(pure = true)
    public @NotNull Version.DevelopmentStage getDevelopmentStage() {
        return developmentStage;
    }

    /**
     * Retrieves the revision version number.
     *
     * @return the revision version as an integer.
     */
    @Contract(pure = true)
    public int getRevision() {
        return revision;
    }

    /**
     * Checks whether the current state is a snapshot.
     *
     * @return {@code true} if the current state represents a snapshot, {@code false} otherwise.
     */
    @Contract(pure = true)
    public boolean isSnapshot() {
        return isSnapshot;
    }

    /**
     * Determines if the current version is not a release version.
     * <p>
     * The method checks whether the current state is either a snapshot
     * or a development stage other than {@link DevelopmentStage#RELEASE}.
     *
     * @return {@code true} if the current version is a snapshot or not in the {@link DevelopmentStage#RELEASE} stage; {@code false} otherwise.
     */
    @Contract(pure = true)
    public boolean isNotRelease() {
        if (isSnapshot)
            return true;

        return developmentStage != DevelopmentStage.RELEASE;
    }

    /**
     * Determines if the current version is a release version.
     * <p>
     * The method checks whether the current state is either a snapshot
     * or a development stage other than {@link DevelopmentStage#RELEASE}.
     *
     * @return {@code true} if the current version is not a snapshot and in the {@link DevelopmentStage#RELEASE} stage; {@code false} otherwise.
     */
    @Contract(pure = true)
    public boolean isRelease() {
        if (isSnapshot)
            return false;

        return developmentStage == DevelopmentStage.RELEASE;
    }

    /**
     * Checks if the current version object represents an empty version.
     * <p>
     * An empty version is defined as having all version components set to
     * their default values:
     * <ul>
     *  <li>major, minor, patch, and revision are all 0.</li>
     *  <li>the development stage is set to RELEASE.</li>
     *  <li>snapshot flag is false.</li>
     * </ul>
     *
     * @return {@code true} if the version is empty, {@code false} otherwise.
     */
    @Contract(pure = true)
    public boolean isEmpty() {
        return (major == 0) && (minor == 0) && (patch == 0) && (developmentStage == DevelopmentStage.RELEASE) && (revision == 0) && (!isSnapshot);
    }

    /**
     * Converts the version information of the object into a concise string representation.
     * The format includes the major, minor, and patch version numbers, and optionally,
     * additional details such as development stage, revision, and snapshot status,
     * if applicable.
     *
     * @return a non-null string representing the version in a shortened format.
     */
    public @NotNull String toShortString() {
        if (isEmpty())
            return "0";

        String version = String.format(
                "%d.%d.%d",
                major,
                minor,
                patch
        );

        // Appends development qualifiers to non-release version string
        if (isNotRelease()) {
            version += String.format("-%s", developmentStage.getShortLabel());
            if (revision > 0)
                version += String.format(".%d", revision);

            if (isSnapshot)
                version += "-SNAPSHOT";
        }

        return version;
    }

    /**
     * Converts the version information into a formatted string representation.
     * The string will include the major, minor, and patch numbers.
     * If the version is not a release version, additional indicators such as
     * the development stage, revision number, and "SNAPSHOT" will be appended
     * to the string.
     *
     * @return A non-null string representation of the version.
     */
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

        // Appends development qualifiers to version string
        if (isNotRelease()) {
            version += String.format(" %s", developmentStage.getLongLabel());
            if (revision > 0)
                version += String.format(" %d", revision);

            if (isSnapshot)
                version += " SNAPSHOT";
        }

        return version;
    }

    /**
     * Compares this object with the specified object for equality.
     *
     * @param obj the object to be compared for equality with this object. Can be null.
     * @return {@code true} if the specified object is equal to this object,
     *         otherwise {@code false}.
     */
    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj instanceof Version)
            return compareTo((Version) obj) == 0;

        return super.equals(obj);
    }

    /**
     * Computes a hash code for the object based on its fields.
     *
     * @return the hash code value for this object.
     */
    @Contract(pure = true)
    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, developmentStage.ordinal(), revision, (isSnapshot ? 1 : 0));
    }

    /**
     * Compares this version object with the specified version for order.
     * The comparison is based on the following hierarchical order:
     * major, minor, patch, development stage, revision, and snapshot status.
     *
     * @param version the version object to be compared with the current instance. Must not be null.
     * @return a negative integer, zero, or a positive integer as this version
     *         is less than, equal to, or greater than the specified version.
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
        else if (version.isSnapshot && !isSnapshot)
            return 1;
        else if (!version.isSnapshot && isSnapshot)
            return -1;
        return 0;
    }

    /**
     * Compares the current object with the specified version and determines
     * if the current object is greater.
     *
     * @param version the version to compare with. Must not be null.
     * @return {@code true} if the current object is greater than the specified version,
     *         {@code false} otherwise.
     */
    @Contract(pure = true)
    public boolean isGreater(final @NotNull Version version) {
        return compareTo(version) > 0;
    }

    /**
     * Compares the current object with the specified version and determines
     * if the current object is greater or equal.
     *
     * @param version the version to compare with. Must not be null.
     * @return {@code true} if the current object is greater or equal than the specified version,
     *         {@code false} otherwise.
     */
    @Contract(pure = true)
    public boolean isGreaterOrEqual(final @NotNull Version version) {
        return compareTo(version) >= 0;
    }

    /**
     * Compares the current object with the specified version and determines
     * if the current object is lower.
     *
     * @param version the version to compare with. Must not be null.
     * @return {@code true} if the current object is lower than the specified version,
     *         {@code false} otherwise.
     */
    @Contract(pure = true)
    public boolean isLower(final @NotNull Version version) {
        return compareTo(version) < 0;
    }

    /**
     * Compares the current object with the specified version and determines
     * if the current object is lower or equal.
     *
     * @param version the version to compare with. Must not be null.
     * @return {@code true} if the current object is lower or equal than the specified version,
     *         {@code false} otherwise.
     */
    @Contract(pure = true)
    public boolean isLowerOrEqual(final @NotNull Version version) {
        return compareTo(version) <= 0;
    }
}
