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
package org.bioimageanalysis.icy.common.math;

import org.bioimageanalysis.icy.common.string.StringUtil;

import java.util.concurrent.TimeUnit;

/**
 * Unit conversion utilities class.
 *
 * @author Thomas Provoost
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class UnitUtil {
    /**
     * Constants for special characters
     */
    // public static final char MICRO_CHAR = 'µ';
    // public static final String MICRO_STRING = "µ";
    // for the sake of easy compatibility with others softwares
    public static final char MICRO_CHAR = 'u';
    public static final String MICRO_STRING = "u";

    public enum UnitPrefix {
        PETA, TERA, GIGA, MEGA, KILO, NONE, MILLI, MICRO, NANO, PICO;

        @Override
        public String toString() {
            return switch (this) {
                case PETA -> "P";
                case TERA -> "T";
                case GIGA -> "G";
                case MEGA -> "M";
                case KILO -> "k";
                case NONE -> "";
                case MILLI -> "m";
                case MICRO -> MICRO_STRING;
                case NANO -> "n";
                case PICO -> "p";
            };
        }
    }

    /**
     * Return the specified value as "bytes" string :<br>
     * 1024 --&gt; "1 KB"<br>
     * 1024*1000 --&gt; "1 MB"<br>
     * 1024*1000*1000 --&gt; "1 GB"<br>
     * ...<br>
     */
    public static String getBytesString(final double value) {
        final double absValue = Math.abs(value);

        // TB
        if (absValue > (512d * 1024d * 1000d * 1000d))
            return MathUtil.round(value / (1024d * 1000d * 1000d * 1000d), 1) + " TB";
            // GB
        else if (absValue > (512d * 1024d * 1000d))
            return MathUtil.round(value / (1024d * 1000d * 1000d), 1) + " GB";
            // MB
        else if (absValue > (512d * 1024d))
            return MathUtil.round(value / (1024d * 1000d), 1) + " MB";
            // KB
        else if (absValue > 512d)
            return MathUtil.round(value / 1024d, 1) + " KB";
        // B
        return MathUtil.round(value, 1) + " B";
    }

    /**
     * Get the best unit with the given value and {@link UnitPrefix}.<br>
     * By best unit we adapt the output unit so the value stay between 0.1 --&gt; 100 range (for
     * dimension 1).<br>
     * Be careful, this method is supposed to be used with unit in <b>decimal</b>
     * system. For sexagesimal system, please use {@link #getBestTimeUnit(double)} or {@link TimeUnit} methods.<br>
     * <br>
     * Example: <code>getBestUnit(0.01, UnitPrefix.MILLI, 1)</code> will return <code>UnitPrefix.MICRO</code><br>
     *
     * @param value
     *        : value used to get the best unit.
     * @param currentUnit
     *        : current unit of the value.
     * @param dimension
     *        : current unit dimension.
     * @return Return the best unit
     * @see #getValueInUnit(double, UnitPrefix, UnitPrefix)
     */
    public static UnitPrefix getBestUnit(final double value, final UnitPrefix currentUnit, final int dimension) {
        // special case
        if (value == 0d)
            return currentUnit;

        int typeInd = currentUnit.ordinal();
        double v = value;
        final int maxInd = UnitPrefix.values().length - 1;
        final double factor = Math.pow(1000d, dimension);
        final double midFactor = Math.pow(100d, dimension);

        while (((int) v == 0) && (typeInd < maxInd)) {
            v *= factor;
            typeInd++;
        }
        while (((int) (v / midFactor) != 0) && (typeInd > 0)) {
            v /= factor;
            typeInd--;
        }

        return UnitPrefix.values()[typeInd];
    }

    /**
     * Get the best unit with the given value and {@link UnitPrefix}. By best unit we adapt the
     * output unit so the value stay between 0.1 --&gt; 100 range (for dimension 1).<br>
     * Be careful, this method is supposed to be used with unit in <b>decimal</b>
     * system. For sexagesimal system, please use {@link #getBestTimeUnit(double)} or {@link TimeUnit} methods.<br>
     * Example: <code>getBestUnit(0.01, UnitPrefix.MILLI, 1)</code> will return <code>UnitPrefix.MICRO</code><br>
     *
     * @param value
     *        : value used to get the best unit.
     * @param currentUnit
     *        : current unit of the value.
     * @return Return the best unit
     * @see #getValueInUnit(double, UnitPrefix, UnitPrefix)
     */
    public static UnitPrefix getBestUnit(final double value, final UnitPrefix currentUnit) {
        return getBestUnit(value, currentUnit, 1);
    }

    /**
     * Return the value from a specific unit to another unit.<br>
     * Be careful, this method is supposed to be used with unit in <b>decimal</b>
     * system. For sexagesimal system, please use {@link #getBestTimeUnit(double)} or {@link TimeUnit} methods.<br>
     * <b>Example:</b><br>
     * <ul>
     * <li>value = 0.01</li>
     * <li>currentUnit = {@link UnitPrefix#MILLI}</li>
     * <li>wantedUnit = {@link UnitPrefix#MICRO}</li>
     * <li>returns: 10</li>
     * </ul>
     *
     * @param value
     *        : Original value.
     * @param currentUnit
     *        : current unit
     * @param wantedUnit
     *        : wanted unit
     * @param dimension
     *        : unit dimension.
     * @return Return a double value in the <code>wantedUnit</code> unit.
     * @see #getBestUnit(double, UnitPrefix)
     */
    public static double getValueInUnit(final double value, final UnitPrefix currentUnit, final UnitPrefix wantedUnit, final int dimension) {
        int currentOrdinal = currentUnit.ordinal();
        final int wantedOrdinal = wantedUnit.ordinal();
        double result = value;
        final double factor = Math.pow(1000d, dimension);

        while (currentOrdinal < wantedOrdinal) {
            result *= factor;
            currentOrdinal++;
        }
        while (currentOrdinal > wantedOrdinal) {
            result /= factor;
            currentOrdinal--;
        }

        return result;
    }

    /**
     * Return the value from a specific unit to another unit.<br>
     * Be careful, this method is supposed to be used with unit in <b>decimal</b>
     * system. For sexagesimal system, please use {@link #getBestTimeUnit(double)} or {@link TimeUnit} methods.<br>
     * <b>Example:</b><br>
     * <ul>
     * <li>value = 0.01</li>
     * <li>currentUnit = {@link UnitPrefix#MILLI}</li>
     * <li>wantedUnit = {@link UnitPrefix#MICRO}</li>
     * <li>returns: 10</li>
     * </ul>
     *
     * @param value
     *        : Original value.
     * @param currentUnit
     *        : current unit
     * @param wantedUnit
     *        : wanted unit
     * @return Return a double value in the <code>wantedUnit</code> unit.
     * @see #getBestUnit(double, UnitPrefix)
     */
    public static double getValueInUnit(final double value, final UnitPrefix currentUnit, final UnitPrefix wantedUnit) {
        return getValueInUnit(value, currentUnit, wantedUnit, 1);
    }

    /**
     * This method returns a string containing the value rounded to a specified
     * number of decimals and its best unit prefix. This method is supposed to
     * be used with meters only.
     *
     * @param value
     *        : value to display
     * @param decimals
     *        : number of decimals to keep
     * @param currentUnit
     *        : current unit prefix (Ex: {@link UnitPrefix#MILLI})
     */
    public static String getBestUnitInMeters(final double value, final int decimals, final UnitPrefix currentUnit) {
        final UnitPrefix unitPxSize = getBestUnit(value, currentUnit);
        final double distanceMeters = getValueInUnit(value, currentUnit, unitPxSize);

        return StringUtil.toString(distanceMeters, decimals) + unitPxSize + "m";
    }

    /**
     * Return the best unit to display the value. The best unit is chosen
     * according to the precision. <br>
     * <b>Example:</b>
     * <ul>
     * <li>62001 ms -&gt; {@link TimeUnit#MILLISECONDS}</li>
     * <li>62000 ms -&gt; {@link TimeUnit#SECONDS}</li>
     * <li>60000 ms -&gt; {@link TimeUnit#MINUTES}</li>
     * </ul>
     *
     * @param valueInMs
     *        : value in milliseconds.
     * @return Return a {@link TimeUnit} enumeration value.
     */
    public static TimeUnit getBestTimeUnit(final double valueInMs) {
        if (valueInMs % 1000 != 0)
            return TimeUnit.MILLISECONDS;
        if (valueInMs % 60000 != 0)
            return TimeUnit.SECONDS;
        if (valueInMs % 3600000 != 0)
            return TimeUnit.MINUTES;

        return TimeUnit.HOURS;
    }

    /**
     * Display the time with a comma and a given precision.
     *
     * @param valueInMs
     *        : value in milliseconds
     * @param precision
     *        : number of decimals after comma
     * @return <b>Example:</b> "2.5 h", "1.543 min", "15 ms".
     */
    public static String displayTimeAsStringWithComma(final double valueInMs, final int precision, final TimeUnit unit) {
        final String result;
        double v = valueInMs;

        result = switch (unit) {
            case DAYS -> {
                v /= 24d * 60d * 60d * 1000d;
                yield StringUtil.toString(v, precision) + " d";
            }
            case HOURS -> {
                v /= 60d * 60d * 1000d;
                yield StringUtil.toString(v, precision) + " h";
            }
            default -> {
                v /= 60d * 1000d;
                yield StringUtil.toString(v, precision) + " min";
            }
            case SECONDS -> {
                v /= 1000d;
                yield StringUtil.toString(v, precision) + " sec";
            }
            case MILLISECONDS -> StringUtil.toString(v, precision) + " ms";
            case NANOSECONDS -> {
                v *= 1000d;
                yield StringUtil.toString(v, precision) + " ns";
            }
        };

        return result;
    }

    /**
     * Display the time with a comma and a given precision.
     *
     * @param valueInMs
     *        : value in milliseconds
     * @param precision
     *        : number of decimals after comma
     * @return <b>Example:</b> "2.5 h", "1.543 min", "15 ms".
     */
    public static String displayTimeAsStringWithComma(final double valueInMs, final int precision) {
        final double v = Math.abs(valueInMs);

        if (v >= 24d * 60d * 60d * 1000d)
            return displayTimeAsStringWithComma(valueInMs, precision, TimeUnit.DAYS);
        if (v >= 60d * 60d * 1000d)
            return displayTimeAsStringWithComma(valueInMs, precision, TimeUnit.HOURS);
        else if (v >= 60d * 1000d)
            return displayTimeAsStringWithComma(valueInMs, precision, TimeUnit.MINUTES);
        else if (v >= 1000d)
            return displayTimeAsStringWithComma(valueInMs, precision, TimeUnit.SECONDS);
        else if (v < 1d)
            return displayTimeAsStringWithComma(valueInMs, precision, TimeUnit.MILLISECONDS);
        else
            return displayTimeAsStringWithComma(valueInMs, precision, TimeUnit.NANOSECONDS);
    }

    /**
     * Display the time with all the units.
     *
     * @param valueInMs
     *        : value in milliseconds
     * @param displayZero
     *        : Even if a unit is not relevant (equals to zero), it will be displayed.
     * @return <b>Example:</b> "2h 3min 40sec 350ms".
     */
    public static String displayTimeAsStringWithUnits(final double valueInMs, final boolean displayZero) {
        String result = "";
        double v = Math.abs(valueInMs);

        if (v >= 24d * 60d * 60d * 1000d) {
            result += (int) (v / (24d * 60d * 60d * 1000d)) + "d ";
            v %= 24d * 60d * 60d * 1000d;
        }
        else if (displayZero)
            result += "0d ";
        if (v >= 60d * 60d * 1000d) {
            result += (int) (v / (60d * 60d * 1000d)) + "h ";
            v %= 60d * 60d * 1000d;
        }
        else if (displayZero)
            result += "00h ";
        if (v >= 60d * 1000d) {
            result += (int) (v / (60d * 1000d)) + "min ";
            v %= 60d * 1000d;
        }
        else if (displayZero)
            result += "00min ";
        if (v >= 1000d) {
            result += (int) (v / 1000d) + "sec ";
            v %= 1000d;
        }
        else if (displayZero)
            result += "00sec ";
        if (v >= 0d)
            result += StringUtil.toString(v, 3) + "ms";
        else if (displayZero)
            result += "000ms";

        return result;
    }
}
