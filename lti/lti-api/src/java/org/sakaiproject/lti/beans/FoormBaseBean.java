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

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;

/**
 * Base class for beans with {@link FoormField} annotations and type conversion utilities.
 * Provides robust conversion of Object types from database results to strongly-typed fields,
 * and reflection-based access by canonical field name for Foorm-annotated subclasses.
 * <p>
 * Named in homage to the someday long-gone Foorm code.
 */
@Slf4j
public abstract class FoormBaseBean {

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

    // --- FoormField reflection (for archive, etc.) ---

    private static final Map<Class<?>, Map<String, Field>> FIELDS_CACHE = new ConcurrentHashMap<>();

    protected static Map<String, Field> getFieldsForClass(Class<?> clazz) {
        return FIELDS_CACHE.computeIfAbsent(clazz, c -> {
            Map<String, Field> map = new ConcurrentHashMap<>();
            for (Field f : c.getDeclaredFields()) {
                FoormField ann = f.getAnnotation(FoormField.class);
                if (ann != null) {
                    f.setAccessible(true);
                    map.put(ann.value(), f);
                }
            }
            return map;
        });
    }

    /**
     * Returns the value of the property identified by its canonical field name.
     *
     * @param fieldName Canonical name from {@link FoormField} (e.g. {@code deployment_id}, {@code SITE_ID})
     * @return The property value, or null if the field is unknown or its value is null
     */
    public Object getValueByFieldName(String fieldName) {
        Field f = getFieldsForClass(getClass()).get(fieldName);
        if (f == null) {
            return null;
        }
        try {
            return f.get(this);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Returns the {@link FoormField} annotation for a field name, or null if unknown.
     */
    public FoormField getFoormFieldByFieldName(String fieldName) {
        Field f = getFieldsForClass(getClass()).get(fieldName);
        return f != null ? f.getAnnotation(FoormField.class) : null;
    }

    /**
     * Field names to omit from archive output. Override in subclasses (e.g. to exclude
     * computed fields like checksum that are appended separately).
     */
    protected Set<String> getExcludedArchiveFieldNames() {
        return Collections.emptySet();
    }

    /**
     * Produces an archive XML element for this bean. Iterates over fields with
     * {@link FoormField#archive()}{@code == true}, excluding any in
     * {@link #getExcludedArchiveFieldNames()}, and appends child elements.
     *
     * @param doc     The document to create elements in
     * @param tagName Root element tag name (e.g. {@code sakai-lti-tool})
     * @return The root element with archivable fields as children, or null if doc is null
     */
    public Element toArchiveElement(Document doc, String tagName) {
        if (doc == null) {
            return null;
        }
        Set<String> excluded = getExcludedArchiveFieldNames();
        Element root = doc.createElement(tagName);
        for (Field f : getClass().getDeclaredFields()) {
            FoormField ann = f.getAnnotation(FoormField.class);
            if (ann == null || !ann.archive() || excluded.contains(ann.value())) {
                continue;
            }
            String field = ann.value();
            Object o = getValueByFieldName(field);
            if (o == null) {
                continue;
            }
            String text = formatArchiveValue(o, ann.type());
            if (text == null) {
                continue;
            }
            Element child = doc.createElement(field);
            child.setTextContent(text);
            root.appendChild(child);
        }
        return root;
    }

    /**
     * Formats a value for archive XML based on its FoormType.
     */
    protected static String formatArchiveValue(Object o, FoormType type) {
        if (o == null) {
            return null;
        }
        if (type == FoormType.CHECKBOX
                || type == FoormType.RADIO
                || type == FoormType.INTEGER
                || type == FoormType.KEY) {
            if (o instanceof Boolean) {
                return Boolean.TRUE.equals(o) ? "1" : "0";
            }
        }
        if (o instanceof Date) {
            return String.valueOf(((Date) o).getTime());
        }
        return o.toString();
    }

    /**
     * Populates this bean from an archive XML element. Iterates over child elements,
     * and for each tag name matching an archivable {@link FoormField}, parses the
     * text content and sets the corresponding field. Skips excluded fields and
     * children that are not archivable (e.g. nested {@code sakai-lti-tool}).
     *
     * @param element The archive element (e.g. {@code sakai-lti-tool}) containing children
     */
    public void populateFromArchiveElement(Element element) {
        if (element == null) {
            return;
        }
        Map<String, Field> fieldsByName = getFieldsForClass(getClass());
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element child = (Element) n;
            String tagName = child.getTagName();
            Field f = fieldsByName.get(tagName);
            if (f == null) {
                continue;
            }
            FoormField ann = f.getAnnotation(FoormField.class);
            if (ann == null || !ann.archive()) {
                continue;
            }
            String text = child.getTextContent();
            if (text == null || text.isEmpty()) {
                continue;
            }
            Object value = parseArchiveValue(text.trim(), f);
            if (value == null) {
                continue;
            }
            try {
                f.set(this, value);
            } catch (IllegalAccessException e) {
                log.debug("Could not set field {}: {}", tagName, e.getMessage());
            }
        }
    }

    /**
     * Parses archive XML text content into the correct type for the given field.
     */
    protected static Object parseArchiveValue(String text, Field f) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        Class<?> type = f.getType();
        try {
            if (type == Boolean.class || type == boolean.class) {
                return "1".equals(text) || "true".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text);
            }
            if (type == Integer.class || type == int.class) {
                if (text.contains(".")) {
                    return Integer.valueOf((int) Double.parseDouble(text));
                }
                return Integer.valueOf(text);
            }
            if (type == Long.class || type == long.class) {
                if (text.contains(".")) {
                    return Long.valueOf((long) Double.parseDouble(text));
                }
                return Long.valueOf(text);
            }
            if (type == Date.class) {
                return new Date(Long.parseLong(text));
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return text;
    }
}
