/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2018 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
 
package org.sakaiproject.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Optional;

import lombok.Setter;

/**
 * Utilities based on double and integers such as validation formats.
 */
public class NumberUtil {

    @Setter
    private static ResourceLoader resourceLoader = new ResourceLoader();

    /**
     * @param origin origin number that is needed to validate on the default user's locale
     * @return true if number format is valid for user's locale
     */
    public static boolean isValidLocaleDouble(final String origin) {
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(resourceLoader.getLocale());
        final DecimalFormatSymbols fs = df.getDecimalFormatSymbols();
        final String doublePattern =
                new StringBuilder()
                        .append("\\d{1,3}(\\")
                        .append(fs.getGroupingSeparator())
                        .append("\\d{3})+")
                        .append(fs.getDecimalSeparator())
                        .append("\\d+|\\d*\\")
                        .append(fs.getDecimalSeparator())
                        .append("\\d+|\\d{1,3}(\\")
                        .append(fs.getGroupingSeparator())
                        .append("\\d{3})+|\\d+")
                        .toString();
        return origin.matches(doublePattern);
    }

    /**
     * Returns the provided number string formatted for the supplied locale. Supports both dot and comma decimal
     * separators regardless of the locale passed.
     *
     * @param origin the number to normalise
     * @param locale the locale to format the number for
     * @return the number formatted using the locale's separators, or the original string when parsing fails
     */
    public static String normalizeLocaleDouble(final String origin, final Locale locale) {
        if (origin == null) {
            return null;
        }

        final String trimmed = origin.trim();
        if (trimmed.isEmpty()) {
            return origin;
        }

        final Double numericValue = parseLocaleDouble(trimmed, locale);
        if (numericValue == null) {
            return origin;
        }

        final NumberFormat format = NumberFormat.getInstance(locale);
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(0);
        // Allow enough precision to avoid losing decimals without introducing scientific notation
        format.setMaximumFractionDigits(15);
        return format.format(numericValue);
    }

    /**
     * Returns the provided number string formatted for the current user's locale.
     *
     * @param origin the number to normalise
     * @return the number formatted using the current locale's separators
     */
    public static String normalizeLocaleDouble(final String origin) {
        return normalizeLocaleDouble(origin, resourceLoader.getLocale());
    }

    /**
     * Parses a number string using either standard dot decimal notation or the separators for the provided locale.
     *
     * @param origin the number to parse
     * @param locale the locale whose separators should be considered
     * @return the parsed double, or {@code null} when parsing fails
     */
    public static Double parseLocaleDouble(final String origin, final Locale locale) {
        if (origin == null) {
            return null;
        }

        final String trimmed = origin.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        try {
            return Double.valueOf(trimmed);
        } catch (NumberFormatException nfe) {
            try {
                final NumberFormat parseFormat = NumberFormat.getInstance(locale);
                parseFormat.setGroupingUsed(true);
                return parseFormat.parse(trimmed).doubleValue();
            } catch (ParseException pe) {
                return null;
            }
        }
    }

    /**
     * Parses a number string using either standard dot decimal notation or the separators for the current user's
     * locale.
     *
     * @param origin the number to parse
     * @return the parsed double, or {@code null} when parsing fails
     */
    public static Double parseLocaleDouble(final String origin) {
        return parseLocaleDouble(origin, resourceLoader.getLocale());
    }

    /**
     * Parse a string value into an optional Integer. Directly uses {@link Integer#valueOf(String)}.
     * <p>
     * These conversions are only here because commons-lang3 does not support Optional.
     *
     * @param value the string to parse
     * @return the Integer value or an empty Optional if unable to convert
     */
    public static Optional<Integer> toInteger(final String value) {
        try {
            return Optional.of(Integer.valueOf(value));
        } catch (final NumberFormatException nfe) {
            return Optional.empty();
        }
    }

    /**
     * Parse a string value into a Integer with default value. Directly uses {@link Integer#valueOf(String)}.
     * <p>
     * These conversions are only here because commons-lang3 does not support Optional.
     *
     * @param value the string to parse
     * @return the Integer value or the supplied default if unable to convert
     */
    public static Integer toInteger(final String value, final Integer defaultValue) {
        try {
            return Integer.valueOf(value);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * Parse a string value into an optional Long. Directly uses {@link Long#valueOf(String)}.
     * <p>
     * These conversions are only here because commons-lang3 does not support Optional.
     *
     * @param value the string to parse
     * @return the Long value or an empty Optional if unable to convert
     */
    public static Optional<Long> toLong(final String value) {
        try {
            return Optional.of(Long.valueOf(value));
        } catch (final NumberFormatException nfe) {
            return Optional.empty();
        }
    }

    /**
     * Parse a string value into a Long with default value. Directly uses {@link Long#valueOf(String)}.
     * <p>
     * These conversions are only here because commons-lang3 does not support Optional.
     *
     * @param value the string to parse
     * @return the Long value or the supplied default if unable to convert
     */
    public static Long toLong(final String value, final Long defaultValue) {
        try {
            return Long.valueOf(value);
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }


}
