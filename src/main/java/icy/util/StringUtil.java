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

package icy.util;

import icy.math.MathUtil;
import icy.system.logging.IcyLogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class StringUtil {
    /*
     * The Alphanum Algorithm is an improved sorting algorithm for strings
     * containing numbers. Instead of sorting numbers in ASCII order like
     * a standard sort, this algorithm sorts numbers in numeric order.
     *
     * The Alphanum Algorithm is discussed at http://www.DaveKoelle.com
     *
     * This library is free software; you can redistribute it and/or
     * modify it under the terms of the GNU Lesser General Public
     * License as published by the Free Software Foundation; either
     * version 2.1 of the License, or any later version.
     *
     * This library is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
     * Lesser General Public License for more details.
     *
     * You should have received a copy of the GNU Lesser General Public
     * License along with this library; if not, write to the Free Software
     * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
     */

    /**
     * This is an updated version of Alphanum Algorithm Comparator
     * with enhancements made by Daniel Migowski, Andre Bogus, and David Koelle
     */
    public static final class AlphanumComparator implements Comparator<String> {
        /** Length of string is passed in for improved efficiency (only need to calculate it once) **/
        private static String getChunk(final String s, final int slength, final int index) {
            int marker = index;
            final StringBuilder chunk = new StringBuilder();
            char c = s.charAt(marker);
            chunk.append(c);
            marker++;
            if (Character.isDigit(c)) {
                while (marker < slength) {
                    c = s.charAt(marker);
                    if (!Character.isDigit(c))
                        break;
                    chunk.append(c);
                    marker++;
                }
            }
            else {
                while (marker < slength) {
                    c = s.charAt(marker);
                    if (Character.isDigit(c))
                        break;
                    chunk.append(c);
                    marker++;
                }
            }
            return chunk.toString();
        }

        @Override
        public int compare(final String s1, final String s2) {
            int thisMarker = 0;
            int thatMarker = 0;
            final int s1Length = s1.length();
            final int s2Length = s2.length();

            while (thisMarker < s1Length && thatMarker < s2Length) {
                final String thisChunk = getChunk(s1, s1Length, thisMarker);
                thisMarker += thisChunk.length();

                final String thatChunk = getChunk(s2, s2Length, thatMarker);
                thatMarker += thatChunk.length();

                // If both chunks contain numeric characters, sort them numerically
                int result; // = 0;
                if (Character.isDigit(thisChunk.charAt(0)) && Character.isDigit(thatChunk.charAt(0))) {
                    // Simple chunk comparison by length.
                    final int thisChunkLength = thisChunk.length();
                    result = thisChunkLength - thatChunk.length();
                    // If equal, the first different number counts
                    if (result == 0) {
                        for (int i = 0; i < thisChunkLength; i++) {
                            result = thisChunk.charAt(i) - thatChunk.charAt(i);

                            if (result != 0)
                                return result;
                        }
                    }
                }
                else
                    result = thisChunk.compareTo(thatChunk);

                if (result != 0)
                    return result;
            }

            return s1Length - s2Length;
        }
    }

    /**
     * Return defaultValue if value is empty
     */
    public static String getValue(final String value, final String defaultValue) {
        if (StringUtil.isEmpty(value))
            return defaultValue;

        return value;
    }

    /**
     * Returns the next number found from specified <code>startIndex</code> in specified string.<br>
     * Returns an empty string if no number was found.
     */
    public static CharSequence getNextNumber(final CharSequence text, final int index) {
        final int len = text.length();

        // get starting digit char index
        final int st = getNextDigitCharIndex(text, index);

        // we find a digit char ?
        if (st >= 0) {
            // get ending digit char index
            int end = StringUtil.getNextNonDigitCharIndex(text, st);
            if (end < 0)
                end = len;

            // get value
            return text.subSequence(st, end);
        }

        return "";
    }

    /**
     * Return the index of previous digit char from specified index in specified string<br>
     * return -1 if not found
     */
    public static int getPreviousDigitCharIndex(final CharSequence value, final int from) {
        final int len = value.length();

        if (from >= len)
            return -1;

        int index = from;
        while (index >= 0) {
            if (Character.isDigit(value.charAt(index)))
                return index;
            index--;
        }

        return -1;
    }

    /**
     * Return the index of previous letter char from specified index in specified string<br>
     * return -1 if not found
     */
    public static int getPreviousLetterCharIndex(final CharSequence value, final int from) {
        final int len = value.length();

        if (from >= len)
            return -1;

        int index = from;
        while (index >= 0) {
            if (Character.isLetter(value.charAt(index)))
                return index;
            index--;
        }

        return -1;
    }

    /**
     * Return the index of previous non digit char from specified index in specified string<br>
     * return -1 if not found
     */
    public static int getPreviousNonDigitCharIndex(final CharSequence value, final int from) {
        final int len = value.length();

        if (from >= len)
            return -1;

        int index = from;
        while (index >= 0) {
            if (!Character.isDigit(value.charAt(index)))
                return index;
            index--;
        }

        return -1;
    }

    /**
     * Return the index of previous non letter char from specified index in specified string<br>
     * Return -1 if not found.
     */
    public static int getPreviousNonLetterCharIndex(final CharSequence value, final int from) {
        final int len = value.length();

        if (from >= len)
            return -1;

        int index = from;
        while (index >= 0) {
            if (!Character.isLetter(value.charAt(index)))
                return index;
            index--;
        }

        return -1;
    }

    /**
     * Return the index of next digit char from specified index in specified string<br>
     * return -1 if not found
     */
    public static int getNextDigitCharIndex(final CharSequence value, final int from) {
        final int len = value.length();

        if (from < 0)
            return -1;

        int index = from;
        while (index < len) {
            if (Character.isDigit(value.charAt(index)))
                return index;
            index++;
        }

        return -1;
    }

    /**
     * Return the index of next letter char from specified index in specified string<br>
     * return -1 if not found
     */
    public static int getNextLetterCharIndex(final CharSequence value, final int from) {
        final int len = value.length();

        if (from < 0)
            return -1;

        int index = from;
        while (index < len) {
            if (Character.isDigit(value.charAt(index)))
                return index;
            index++;
        }

        return -1;
    }

    /**
     * Return the index of next non digit char from specified index in specified string<br>
     * return -1 if not found
     */
    public static int getNextNonDigitCharIndex(final CharSequence value, final int from) {
        final int len = value.length();

        if (from < 0)
            return -1;

        int index = from;
        while (index < len) {
            if (!Character.isDigit(value.charAt(index)))
                return index;
            index++;
        }

        return -1;
    }

    /**
     * Return the index of next non letter char from specified index in specified string<br>
     * return -1 if not found
     */
    public static int getNextNonLetterCharIndex(final CharSequence value, final int from) {
        final int len = value.length();

        if (from < 0)
            return -1;

        int index = from;
        while (index < len) {
            if (!Character.isLetter(value.charAt(index)))
                return index;
            index++;
        }

        return -1;
    }

    /**
     * Return the index of next control char from specified <code>startIndex</code> in specified
     * string.<br>
     * return -1 if no control character found.
     */
    public static int getNextCtrlCharIndex(final CharSequence value, final int startIndex) {
        final int len = value.length();

        if (startIndex < 0)
            return -1;

        int index = startIndex;
        while (index < len) {
            if (Character.isISOControl(value.charAt(index)))
                return index;
            index++;
        }

        return -1;
    }

    /**
     * Limit the length of the specified string to maxlen.
     */
    public static String limit(final String value, final int maxlen, final boolean tailLimit) {
        if (value == null)
            return null;

        final int len = value.length();

        if (len > maxlen) {
            // simple truncation
            if (tailLimit || (maxlen <= 8))
                return value.substring(0, maxlen - 2).trim() + "...";

            // cut center
            final int cut = (maxlen - 3) / 2;
            return value.substring(0, cut).trim() + "..." + value.substring(len - cut).trim();
        }

        return value;
    }

    /**
     * Limit the length of the specified string to maxlen.
     */
    public static String limit(final String value, final int maxlen) {
        return limit(value, maxlen, false);
    }

    /**
     * Truncate the text to a specific size, according a keyword.<br>
     * The text will be truncated around the place where the keyword is found.<br>
     * If the string is found at the beginning, the text will be like this:<br>
     * <b>Lorem ipsum dolor sit amet, consec...</b>
     *
     * @param fullText
     *        : text to be truncated.
     * @param keyword
     *        : string to be found in the text and truncated around.
     * @param maxSize
     *        : max size of the string
     */
    public static String trunc(final String fullText, final String keyword, final int maxSize) {
        final int idx = fullText.toLowerCase().indexOf(keyword.toLowerCase());

        // key not found
        if (idx == -1)
            return "";

        String toReturn = fullText;
        final int fullTextSize = fullText.length();

        if (fullTextSize > maxSize) {
            int firstSpaceAfter;
            final String textBeforeWord;
            int lastSpaceBefore;

            // extract the full word from the text
            firstSpaceAfter = fullText.indexOf(' ', idx);
            firstSpaceAfter = firstSpaceAfter == -1 ? fullTextSize : firstSpaceAfter;

            textBeforeWord = fullText.substring(0, idx);
            lastSpaceBefore = textBeforeWord.lastIndexOf(' ');
            lastSpaceBefore = lastSpaceBefore == -1 ? 0 : lastSpaceBefore;

            // determine if we are at the beginning, the end, or at the middle
            if (idx <= maxSize / 2) {
                toReturn = fullText.substring(0, maxSize);
                toReturn = toReturn.trim() + "...";
            }
            else if ((fullTextSize - idx) <= maxSize / 2) {
                toReturn = fullText.substring(fullTextSize - maxSize, fullTextSize);
                toReturn = "..." + toReturn.trim();
            }
            else {
                final int beginIndex = idx - maxSize / 2;
                final int endIndex = idx + maxSize / 2;
                if (endIndex > fullTextSize)
                    IcyLogger.trace(StringUtil.class, "End index superior to full text size: " + endIndex);
                // beginIndex = beginIndex < 0 ? 0 : beginIndex;
                // endIndex = endIndex > fullTextSize ? fullTextSize : endIndex;
                toReturn = "..." + fullText.substring(beginIndex, endIndex).trim() + "...";
            }
        }

        return toReturn;
    }

    /**
     * Return true if the specified String are exactly the same.
     *
     * @param trim
     *        if true then string are trimmed before comparison
     */
    public static boolean equals(final String s1, final String s2, final boolean trim) {
        if (isEmpty(s1, trim))
            return isEmpty(s2, trim);
        else if (isEmpty(s2, trim))
            return false;

        if (trim)
            return s1.trim().equals(s2.trim());

        return s1.equals(s2);
    }

    /**
     * Return true if the specified String are exactly the same
     */
    public static boolean equals(final String s1, final String s2) {
        return equals(s1, s2, false);
    }

    /**
     * Return true if the specified String is empty.
     *
     * @param trim
     *        trim the String before doing the empty test
     */
    public static boolean isEmpty(final String value, final boolean trim) {
        if (value != null) {
            if (trim)
                return value.trim().length() == 0;

            return value.length() == 0;
        }

        return true;
    }

    /**
     * Return true if the specified String is empty.
     * The String is trimed by default before doing the test
     */
    public static boolean isEmpty(final String value) {
        return isEmpty(value, true);
    }

    /**
     * Try to parse a boolean from the specified String and return it.
     * Return 'def' is we can't parse any boolean from the string.
     */
    public static boolean parseBoolean(final String s, final boolean def) {
        if (s == null)
            return def;

        final String value = s.toLowerCase();

        if (value.equals(Boolean.toString(true)))
            return true;
        if (value.equals(Boolean.toString(false)))
            return false;

        return def;
    }

    /**
     * Try to parse a integer from the specified String and return it.
     * Return 'def' is we can't parse any integer from the string.
     */
    public static int parseInt(final String s, final int def) {
        try {
            return Integer.parseInt(s);
        }
        catch (final NumberFormatException E) {
            return def;
        }
    }

    /**
     * Try to parse a long integer from the specified String and return it.
     * Return 'def' is we can't parse any integer from the string.
     */
    public static long parseLong(final String s, final long def) {
        try {
            return Long.parseLong(s);
        }
        catch (final NumberFormatException E) {
            return def;
        }
    }

    /**
     * Try to parse a float from the specified String and return it.
     * Return 'def' is we can't parse any float from the string.
     */
    public static float parseFloat(final String s, final float def) {
        try {
            return Float.parseFloat(s);
        }
        catch (final NumberFormatException E) {
            return def;
        }
    }

    /**
     * Try to parse a double from the specified String and return it.
     * Return 'def' is we can't parse any double from the string.
     */
    public static double parseDouble(final String s, final double def) {
        try {
            return Double.parseDouble(s);
        }
        catch (final NumberFormatException E) {
            return def;
        }
    }

    /**
     * Try to parse a array of byte from the specified String and return it.
     * Return 'def' is we can't parse any array of byte from the string.
     */
    public static byte[] parseBytes(final String s, final byte[] def) {
        if (s == null)
            return def;

        return s.getBytes();
    }

    /**
     * Returns a <i>String</i> object representing the specified
     * boolean. If the specified boolean is <code>true</code>, then
     * the string {@code "true"} will be returned, otherwise the
     * string {@code "false"} will be returned.
     */
    public static String toString(final boolean value) {
        return Boolean.toString(value);
    }

    /**
     * Returns a <code>String</code> object representing the specified integer.
     */
    public static String toString(final int value) {
        return Integer.toString(value);
    }

    /**
     * Returns a <code>String</code> object representing the specified integer.<br>
     * If the returned String is shorter than specified length<br>
     * then leading '0' are added to the string.
     */
    public static String toString(final int value, final int minSize) {
        final StringBuilder result = new StringBuilder(Integer.toString(value));

        while (result.length() < minSize)
            result.insert(0, "0");

        return result.toString();
    }

    /**
     * Returns a <code>String</code> object representing the specified <code>long</code>.
     */
    public static String toString(final long value) {
        return Long.toString(value);
    }

    /**
     * Returns a string representation of the <code>float</code> argument.
     */
    public static String toString(final float value) {
        return Float.toString(value);
    }

    /**
     * Returns a string representation of the <code>double</code> argument.
     */
    public static String toString(final double value) {
        final int i = (int) value;

        if (i == value)
            return toString(i);

        return Double.toString(value);
    }

    /**
     * Returns a string representation of the <code>double</code> argument
     * with specified number of decimal.
     */
    public static String toString(final double value, final int numDecimal) {
        return Double.toString(MathUtil.round(value, numDecimal));
    }

    /**
     * Returns a string representation of the <code>double</code> argument with specified size :<br>
     * <code>toString(1.23456, 5)</code> --&gt; <code>"1.2345"</code><br>
     * <code>toString(123.4567, 4)</code> --&gt; <code>"123.4"</code><br>
     * <code>toString(1234.567, 2)</code> --&gt; <code>"1234"</code> as we never trunk integer part.<br>
     * <code>toString(1234.5, 10)</code> --&gt; <code>"1234.5"</code> as we never trunk integer part.<br>
     */
    public static String toStringEx(final double value, final int size) {
        final int i = (int) value;

        if (i == value)
            return toString(i);

        return Double.toString(MathUtil.roundSignificant(value, size, true));
    }

    /**
     * Return a string representation of the byte array argument.
     */
    public static String toString(final byte[] value) {
        return new String(value);
    }

    /**
     * Returns a string representation of the integer argument as an
     * unsigned integer in base 16.
     */
    public static String toHexaString(final int value) {
        return Integer.toHexString(value);
    }

    /**
     * Returns a string representation of the integer argument as an
     * unsigned integer in base 16.<br>
     * Force the returned string to have the specified size :<br>
     * If the string is longer then only last past is kept.<br>
     * If the string is shorter then leading 0 are added to the string.
     */
    public static String toHexaString(final int value, final int size) {
        final StringBuilder result = new StringBuilder(Integer.toHexString(value));

        if (result.length() > size)
            return result.substring(result.length() - size);

        while (result.length() < size)
            result.insert(0, "0");
        return result.toString();
    }

    /**
     * Remove <code>count</code> characters from the end of specified string.
     */
    public static String removeLast(final String value, final int count) {
        if (value == null)
            return null;

        final int l = value.length();

        if (l < 2)
            return "";

        return value.substring(0, l - count);
    }

    /**
     * Creates a flattened version of the provided String. The flattening operation splits the
     * string by inserting spaces between words starting with an upper case letter, and converts
     * upper case letters to lower case (with the exception of the first word). Note that
     * <b>consecutive upper case letters will remain grouped</b>, as they are considered to
     * represent an acronym.<br>
     * <br>
     * <u>NOTE:</u> This method is optimized for class names that follow the Java naming convention. <br>
     * Examples:<br>
     * MyGreatClass -&gt; "My great class"<br>
     * MyXYZClass -&gt; "My XYZ class"
     *
     * @param string
     *        the string to flatten
     * @return a flattened (i.e. pretty-printed) String based on the name of the string
     */
    public static String getFlattened(final String string) {
        final String[] words = string.split("(?=[A-Z])");

        StringBuilder output = new StringBuilder(words[0]);
        if (words.length > 1) {
            // words[0] is always empty here
            output = new StringBuilder(words[1]);

            for (int i = 2; i < words.length; i++) {
                final String word = words[i];
                if (word.length() == 1) {
                    // single letter
                    if (words[i - 1].length() == 1) {
                        // append to the previous letter (acronym)
                        output.append(word);
                    }
                    else {
                        // new isolated letter or acronym
                        output.append(" ").append(word);
                    }
                }
                else
                    output.append(" ").append(word.toLowerCase());
            }
        }

        return output.toString();
    }

    /**
     * Replace all C line break sequence : <code>"\n", "\r", "\r\n"</code><br>
     * from the specified <code>text</code> by <code>str</code>.
     */
    public static String replaceCR(final String text, final String str) {
        return text.replaceAll("(\r\n|\n\r|\r|\n)", str);
    }

    /**
     * Remove all C line break sequence : <code>"\n", "\r", "\r\n"</code><br>
     * from the specified text.
     */
    public static String removeCR(final String text) {
        return replaceCR(text, "");
    }

    /**
     * Convert the C line break sequence : <code>"\n", "\r", "\r\n"</code><br>
     * to HTML line break sequence.
     */
    public static String toHtmlCR(final String text) {
        return replaceCR(text, "<br>").replaceAll("(<BR>|<br/>|<BR/>)", "<br>");
    }

    /**
     * Return true if the specified text contains HTML line break sequence.
     */
    public static boolean containHtmlCR(final String text) {
        return (text.contains("<br>")) || (text.contains("<BR>")) || (text.contains("<br/>")) || (text.contains("<BR/>"));
    }

    /**
     * Bold (inserting HTML bold tag) the specified keyword in the text.
     */
    public static String htmlBoldSubstring(final String text, final String keyword, final boolean ignoreCase) {
        // right now we just ignore 'b' keyword with produce error because of the <b> sequence.
        if (!isEmpty(text) && !isEmpty(keyword) && !keyword.equalsIgnoreCase("b")) {
            final int keywordLen = keyword.length();
            final String key;

            if (ignoreCase)
                key = keyword.toLowerCase();
            else
                key = keyword;

            String result = text;
            int index;

            if (ignoreCase)
                index = result.toLowerCase().indexOf(key);
            else
                index = result.indexOf(key);

            while (index != -1) {
                result = result.substring(0, index) + "<b>" + result.substring(index, index + keywordLen) + "</b>"
                        + result.substring(index + keywordLen);

                if (ignoreCase)
                    index = result.toLowerCase().indexOf(key, index + keywordLen + 6);
                else
                    index = result.indexOf(key, index + keywordLen + 6);
            }

            return result;
        }

        return text;
    }

    /**
     * Split a text into word based on space character while preserving quoted sentences.
     *
     * @param text
     *        text to split into word.<br>
     *        Example:<br>
     *        <i>this book is named "the red cat"</i> --&gt; <br>
     *        <ul>
     *        <li>this</li>
     *        <li>book</li>
     *        <li>is</li>
     *        <li>named</li>
     *        <li>the red cat</li>
     *        </ul>
     * @return String array representing words
     */
    public static List<String> split(final String text) {
        // want to preserve quoted string as single words
        final List<String> result = new ArrayList<>();
        final Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(text);

        while (m.find())
            result.add(m.group(1).replace("\"", ""));

        return result;
    }

    /**
     * Converts wildcard to regular expression.
     *
     * @return regex
     */
    public static String wildcardToRegex(final String wildcard) {
        final StringBuilder s = new StringBuilder(wildcard.length());

        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            final char c = wildcard.charAt(i);
            switch (c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case '^':
                case '.':
                case '{':
                case '}':
                case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');

        return (s.toString());
    }
}
