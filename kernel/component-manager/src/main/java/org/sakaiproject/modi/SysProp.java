package org.sakaiproject.modi;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enumerates and encapsulates essential system properties for launching Sakai.
 *
 * This is a better alternative to using the strings everywhere for specific, core properties.
 * They are used by reference, so the compiler can trace them. With managed access, there is
 * also a possibility of moving them out of mutable global state.
 *
 * This interface also wraps return values in Optionals for more convenient handling of missing
 * values or chaining/cascading.
 */
public enum SysProp {
    /**
     * The home directory for Sakai. Used for countless pieces of configuration or transient storage. It is
     * typically directly within the Tomcat directory as "sakai/".
     */
    sakaiHome("sakai.home"),

    /** The "base" directory for Tomcat/Catalina. This is essentially where Sakai is installed. */
    catalinaBase("catalina.base");

    /** The name/key of this property. */
    private String key;

    SysProp(String key) {
        this.key = key;
    }

    /** The name/key of this property. */
    public String getKey() {
        return key;
    }

    /** Get the value of this property; currently implemented over System properties. */
    Optional<String> getValue() {
        return Optional.ofNullable(System.getProperty(key));
    }

    /** Get the value of this property, with a default value; currently implemented over System properties. */
    Optional<String> getValue(String def) {
        return Optional.ofNullable(System.getProperty(key, def));
    }

    /** Set the value of this property; currently implemented over System properties. */
    void setValue(String value) {
        System.setProperty(key, value);
    }

    /** Look up a property enum object by its key. */
    public static Optional<SysProp> get(String key) {
        return Arrays.stream(SysProp.values())
                .filter(prop -> prop.key.equalsIgnoreCase(key))
                .findFirst();
    }
}