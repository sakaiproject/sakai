package org.sakaiproject.entitybroker.util.devhelper;

/**
 * $Id$
 * $URL$
 */

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;

import lombok.extern.slf4j.Slf4j;

/**
 * Lightweight bean manipulation helpers that replace the legacy reflectutils dependency.
 */
@Slf4j
public final class DeveloperBeanUtils {

    private DeveloperBeanUtils() {
    }

    public static <T> T cloneBean(T bean, int maxDepth, String[] propertiesToSkip) {
        if (bean == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) bean.getClass();
        T copy = instantiate(type);
        copyBean(bean, copy, maxDepth, propertiesToSkip, false);
        return copy;
    }

    public static void copyBean(Object source, Object target, int maxDepth, String[] fieldNamesToSkip, boolean ignoreNulls) {
        if (source == null || target == null) {
            return;
        }
        Set<String> skips = toSkipSet(fieldNamesToSkip);
        BeanWrapper sourceWrapper = new BeanWrapperImpl(source);
        BeanWrapper targetWrapper = new BeanWrapperImpl(target);
        int allowedDepth = normalizeDepth(maxDepth);
        for (PropertyDescriptor descriptor : sourceWrapper.getPropertyDescriptors()) {
            String name = descriptor.getName();
            if ("class".equals(name) || skips.contains(name)) {
                continue;
            }
            if (!sourceWrapper.isReadableProperty(name) || !targetWrapper.isWritableProperty(name)) {
                continue;
            }
            Object value = sourceWrapper.getPropertyValue(name);
            if (value == null && ignoreNulls) {
                continue;
            }
            Object converted = convertValue(value, descriptor.getPropertyType(), allowedDepth - 1);
            try {
                targetWrapper.setPropertyValue(name, converted);
            } catch (BeansException e) {
                log.debug("Failed to copy property {} from {} to {}", name, source.getClass(), target.getClass(), e);
            }
        }
    }

    public static List<String> populate(Object bean, Map<String, ?> properties) {
        if (bean == null || properties == null || properties.isEmpty()) {
            return Collections.emptyList();
        }
        BeanWrapper wrapper = new BeanWrapperImpl(bean);
        List<String> applied = new ArrayList<>();
        int allowedDepth = normalizeDepth(0) - 1;
        for (Map.Entry<String, ?> entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            if (propertyName == null || !wrapper.isWritableProperty(propertyName)) {
                continue;
            }
            Class<?> targetType = wrapper.getPropertyType(propertyName);
            Object converted = convertValue(entry.getValue(), targetType, allowedDepth);
            try {
                wrapper.setPropertyValue(propertyName, converted);
                applied.add(propertyName);
            } catch (BeansException e) {
                log.debug("Unable to populate property {} on {}", propertyName, bean.getClass(), e);
            }
        }
        return applied;
    }

    public static <T> T convert(Object value, Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Target type must be provided");
        }
        Object converted = convertValue(value, type, normalizeDepth(0) - 1);
        return type.cast(converted);
    }

    private static int normalizeDepth(int maxDepth) {
        return maxDepth <= 0 ? Integer.MAX_VALUE : maxDepth;
    }

    private static Set<String> toSkipSet(String[] names) {
        if (names == null || names.length == 0) {
            return Collections.emptySet();
        }
        return java.util.Arrays.stream(names)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private static <T> T instantiate(Class<T> type) {
        try {
            return BeanUtils.instantiateClass(type);
        } catch (BeansException e) {
            throw new IllegalStateException("Unable to instantiate bean of type " + type, e);
        }
    }

    private static Object convertValue(Object value, Class<?> targetType, int depthRemaining) {
        if (value == null || targetType == null) {
            return value;
        }
        if (targetType.isInstance(value)) {
            return cloneNestedIfNeeded(value, depthRemaining);
        }
        if (value instanceof String[] stringArray) {
            if (targetType.isArray()) {
                return convertArray(stringArray, targetType.getComponentType());
            }
            value = stringArray.length == 0 ? null : (stringArray.length == 1 ? stringArray[0] : String.join(",", stringArray));
            if (value == null) {
                return null;
            }
        } else if (value.getClass().isArray() && !targetType.isArray()) {
            int length = Array.getLength(value);
            value = length == 0 ? null : Array.get(value, 0);
            if (value == null) {
                return null;
            }
        }
        BeanWrapperImpl converter = new BeanWrapperImpl();
        try {
            Object converted = converter.convertIfNecessary(value, targetType);
            return cloneNestedIfNeeded(converted, depthRemaining);
        } catch (BeansException e) {
            log.debug("Conversion of value {} to type {} failed", value, targetType, e);
            return null;
        }
    }

    private static Object cloneNestedIfNeeded(Object value, int depthRemaining) {
        if (value == null || depthRemaining <= 0) {
            return value;
        }
        if (value instanceof Collection<?> collection) {
            Collection<Object> copy = instantiateCollection(collection);
            for (Object element : collection) {
                copy.add(cloneNestedIfNeeded(element, depthRemaining - 1));
            }
            return copy;
        }
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                copy.put(entry.getKey(), cloneNestedIfNeeded(entry.getValue(), depthRemaining - 1));
            }
            return copy;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            Object copy = Array.newInstance(value.getClass().getComponentType(), length);
            for (int i = 0; i < length; i++) {
                Array.set(copy, i, cloneNestedIfNeeded(Array.get(value, i), depthRemaining - 1));
            }
            return copy;
        }
        if (isSimpleValue(value.getClass())) {
            return value;
        }
        Object nested = instantiate(value.getClass());
        copyBean(value, nested, depthRemaining, null, false);
        return nested;
    }

    private static boolean isSimpleValue(Class<?> type) {
        return BeanUtils.isSimpleValueType(type) || Enum.class.isAssignableFrom(type);
    }

    private static Object convertArray(String[] source, Class<?> componentType) {
        Object array = Array.newInstance(componentType, source.length);
        for (int i = 0; i < source.length; i++) {
            Array.set(array, i, convertValue(source[i], componentType, 0));
        }
        return array;
    }

    private static Collection<Object> instantiateCollection(Collection<?> template) {
        try {
            @SuppressWarnings("unchecked")
            Collection<Object> copy = (Collection<Object>) template.getClass().getDeclaredConstructor().newInstance();
            return copy;
        } catch (ReflectiveOperationException e) {
            return new ArrayList<>();
        }
    }
}
