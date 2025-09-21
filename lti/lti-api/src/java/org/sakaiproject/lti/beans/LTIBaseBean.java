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

/**
 * Base class providing common type conversion utilities for LTI POJOs.
 * These methods handle the robust conversion of Object types from database results
 * to strongly-typed POJO fields, similar to the LTIUtil conversion methods.
 * 
 * Implementation assistance provided by Claude AI for comprehensive type conversion
 * and robust database number handling across SQLite, Oracle, and MySQL systems.
 */
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
}
