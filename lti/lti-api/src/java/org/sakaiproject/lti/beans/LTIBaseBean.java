/*
 *
 * $URL$
 * $Id$
 *
 * Copyright (c) 2025 Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.lti.beans;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Base class providing common type conversion utilities for LTI Beans.
 * These methods handle the robust conversion of Object types from database results
 * to strongly-typed Bean fields, similar to the LTIUtil conversion methods.
 *
 * Implementation assistance provided by Claude AI for comprehensive type conversion
 * and robust database number handling across SQLite, Oracle, and MySQL systems.
 */
@Slf4j
public abstract class LTIBaseBean {

    /**
     * Safely converts a map value to a String.
     *
     * @param map The map containing the value
     * @param key The key to look up
     * @return The string value, or null if not found or null
     */
    protected static String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Safely converts a map value to a Long, handling all database number types.
     * This method is as robust as LTIUtil.toLong() and handles:
     * - String representations of numbers (including decimals)
     * - All Number types (Integer, Long, Double, Float, Short, Byte, BigDecimal, etc.)
     * - Decimal truncation (like LTIUtil)
     * - Invalid strings return null
     *
     * @param map The map containing the value
     * @param key The key to look up
     * @return The Long value, or null if not found, null, or invalid
     */
    protected static Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }

        // Handle String types (including database string representations)
        if (value instanceof String) {
            String str = (String) value;
            if (str.length() == 0) {
                return null;
            }
            try {
                // Handle decimal strings by truncating (like LTIUtil)
                if (str.contains(".")) {
                    // Convert to double first, then to long (truncates decimal)
                    return Long.valueOf((long) Double.parseDouble(str));
                }
                return Long.valueOf(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Handle all Number types (Integer, Long, Double, Float, Short, Byte, BigDecimal, etc.)
        if (value instanceof Number) {
            return Long.valueOf(((Number) value).longValue());
        }

        // Fallback: try toString() conversion
        try {
            String str = value.toString();
            if (str.contains(".")) {
                return Long.valueOf((long) Double.parseDouble(str));
            }
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Safely converts a map value to an Integer, handling all database number types.
     * This method is as robust as LTIUtil.toInteger() and handles:
     * - String representations of numbers (including decimals)
     * - All Number types (Integer, Long, Double, Float, Short, Byte, BigDecimal, etc.)
     * - Decimal truncation (like LTIUtil)
     * - Invalid strings return null
     *
     * @param map The map containing the value
     * @param key The key to look up
     * @return The Integer value, or null if not found, null, or invalid
     */
    protected static Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }

        // Handle String types (including database string representations)
        if (value instanceof String) {
            String str = (String) value;
            if (str.length() == 0) {
                return null;
            }
            try {
                // Handle decimal strings by truncating (like LTIUtil)
                if (str.contains(".")) {
                    // Convert to double first, then to int (truncates decimal)
                    return Integer.valueOf((int) Double.parseDouble(str));
                }
                return Integer.valueOf(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Handle all Number types (Integer, Long, Double, Float, Short, Byte, BigDecimal, etc.)
        if (value instanceof Number) {
            return Integer.valueOf(((Number) value).intValue());
        }

        // Fallback: try toString() conversion
        try {
            String str = value.toString();
            if (str.contains(".")) {
                return Integer.valueOf((int) Double.parseDouble(str));
            }
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Safely converts a map value to a Boolean, handling common boolean representations.
     *
     * @param map The map containing the value
     * @param key The key to look up
     * @return The Boolean value, or null if not found or null
     */
    protected static Boolean getBooleanValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        // Handle common boolean representations
        String str = value.toString().toLowerCase();
        return "true".equals(str) || "1".equals(str) || "yes".equals(str) || "on".equals(str);
    }

    /**
     * Safely converts a map value to a Date, handling various timestamp representations.
     *
     * @param map The map containing the value
     * @param key The key to look up
     * @return The Date value, or null if not found or null
     */
    protected static Date getDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof Calendar) {
            return ((Calendar) value).getTime();
        }
        if (value instanceof Long) {
            return new Date((Long) value);
        }
        // Try to parse as timestamp
        try {
            long timestamp = Long.parseLong(value.toString());
            return new Date(timestamp);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Safely converts a map value to a three-state Integer (0=off, 1=on, 2=content, null=off).
     * Validates that the value is in the valid set and logs warnings for invalid values.
     *
     * @param map The map containing the value
     * @param key The key to look up
     * @param fieldName The name of the field for logging purposes (e.g., "newpage", "debug")
     * @return The Integer value (0, 1, 2, or null), with null treated as 0
     */
    protected static Integer getThreeStateValue(Map<String, Object> map, String key, String fieldName) {
        Object value = map.get(key);
        if (value == null) {
            return null; // null is valid and represents "off" (0)
        }

        Integer intValue = null;

        // Convert to Integer using existing logic
        if (value instanceof String) {
            String str = (String) value;
            if (str.length() == 0) {
                return null;
            }
            try {
                if (str.contains(".")) {
                    intValue = Integer.valueOf((int) Double.parseDouble(str));
                } else {
                    intValue = Integer.valueOf(str);
                }
            } catch (NumberFormatException e) {
                // Invalid string - will be caught by validation below
            }
        } else if (value instanceof Number) {
            intValue = Integer.valueOf(((Number) value).intValue());
        } else {
            try {
                String str = value.toString();
                if (str.contains(".")) {
                    intValue = Integer.valueOf((int) Double.parseDouble(str));
                } else {
                    intValue = Integer.parseInt(str);
                }
            } catch (NumberFormatException e) {
                // Invalid conversion - will be caught by validation below
            }
        }

        // Validate the three-state value
        if (intValue != null && (intValue < 0 || intValue > 2)) {
            log.warn("Invalid three-state value for {}: {} (expected 0, 1, 2, or null). Treating as null (off).",
                     fieldName, value);
            return null;
        }

        return intValue;
    }

    /**
     * Helper method to safely put a three-state value in a map with validation.
     * Validates that the value is in the valid set (0, 1, 2, or null) and logs warnings for invalid values.
     *
     * @param map The map to put the value in
     * @param key The key
     * @param value The three-state Integer value (0, 1, 2, or null)
     * @param fieldName The name of the field for logging purposes (e.g., "newpage", "debug")
     */
    protected static void putThreeStateIfNotNull(Map<String, Object> map, String key, Integer value, String fieldName) {
        if (value != null) {
            // Validate the three-state value
            if (value < 0 || value > 2) {
                log.warn("Invalid three-state value for {}: {} (expected 0, 1, 2, or null). Treating as null (off).",
                         fieldName, value);
                return; // Don't put invalid values in the map
            }
            map.put(key, value);
        }
    }

    /**
     * Helper method to safely put a value in a map only if it's not null.
     *
     * @param map The map to put the value in
     * @param key The key
     * @param value The value (only added if not null)
     */
    protected static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    /**
     * Helper method to safely put a Boolean value in a map as an Integer.
     * Converts Boolean values to Integer: true → 1, false → 0, null → null
     *
     * @param map The map to put the value in
     * @param key The key
     * @param value The Boolean value to convert and add (only added if not null)
     */
    protected static void putBooleanAsInteger(Map<String, Object> map, String key, Boolean value) {
        if (value != null) {
            map.put(key, value ? Integer.valueOf(1) : Integer.valueOf(0));
        }
    }
}
