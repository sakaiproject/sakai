package org.sakaiproject.component.api;

import java.util.List;
import java.util.Optional;

/**
 * Simplified alternative to {@link ServerConfigurationService} for getting
 * configuration values from the application context. These values may be
 * available through normal property/value injection, but this interface is
 * useful while untangling the Spring context, ComponentManager, and handling
 * of dynamic (database-stored) values.
 */
public interface ConfiguredContext {
    /**
     * Get an optional string property from the configured context.
     *
     * @param name the property to read
     * @return the property value, if present, or an empty optional
     */
    Optional<String> getString(String name);

    /**
     * Get a string property from the configured context, with a default value.
     *
     * @param name the property to read
     * @param defaultValue the default to return if missing
     * @return the property value, if present, or the supplied default
     */
    String getString(String name, String defaultValue);

    /**
     * Get a string property that can have multiple values. Values that are
     * comma-separated on the named property take precedence over "Sakai-style"
     * properties that have .count and additional ordinal keys. Never null.
     *
     * @param name the property to read
     * @return the property values, if present, or an empty list
     */
    List<String> getStrings(String name);

    /**
     * Get an optional Integer property from the configured context.
     *
     * @param name the property to read
     * @return the property value, if present and well-formed, otherwise an empty optional
     */
    Optional<Integer> getInt(String name);

    /**
     * Get an Integer property from the configured context, with a default value.
     *
     * @param name the property to read
     * @param defaultValue the default to return if missing
     * @return the property value, if present, or the supplied default
     */
    Integer getInt(String name, Integer defaultValue);

    /**
     * Get an optional Long property from the configured context.
     *
     * @param name the property to read
     * @return the property value, if present and well-formed, otherwise an empty optional
     */
    Optional<Long> getLong(String name);

    /**
     * Get a Long property from the configured context, with a default value.
     *
     * @param name the property to read
     * @param defaultValue the default to return if missing
     * @return the property value, if present, or the supplied default
     */
    Long getLong(String name, Long defaultValue);

    /**
     * Get an optional Boolean property from the configured context.
     *
     * @param name the property to read
     * @return Optional.of(true) if present and matches "true" case-insensitive,
     *         Optional.of(false) if present and any other value,
     *         otherwise Optional.empty()
     */
    Optional<Boolean> getBoolean(String name);

    /**
     * Get a Boolean property from the configured context, with a default value.
     *
     * @param name the property to read
     * @param defaultValue the default to return if missing
     * @return true if present and matches "true" case-insensitive, false if present
     *         and any other value, otherwise the supplied default
     */
    Boolean getBoolean(String name, Boolean defaultValue);

    /**
     * Get an optional property from the configured context as an Object.
     *
     * @param name the property to read
     * @return the property value, if present and well-formed, otherwise an empty optional
     */
    Optional<Object> get(String name);

    /**
     * Get a property from the configured context as an Object.
     *
     * @param name the property to read
     * @param defaultValue the default to return if missing
     * @return the property value, if present and well-formed, otherwise an empty optional
     */
    Object get(String name, Object defaultValue);
}