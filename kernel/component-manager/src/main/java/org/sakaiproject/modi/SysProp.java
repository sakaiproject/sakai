/*
 * Copyright (c) 2003-2022 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.modi;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/**
 * Enumerates and encapsulates essential system properties for launching Sakai.
 * <p>
 * This is a better alternative to using the strings everywhere for specific, core properties. They are used by
 * reference, so the compiler can trace them. With managed access, there is also a possibility of moving them out of
 * mutable global state.
 * <p>
 * This interface also wraps return values in Optionals for more convenient handling of missing values or
 * chaining/cascading.
 * <p>
 * We use underscores (but not uppercase) to distinguish them and make it practical to do a static import of SysProp.*
 * and have easy access when you have camelCase variables or fields.
 */
public enum SysProp {
    /**
     * The home directory for Sakai. Used for countless pieces of configuration or transient storage. It is typically
     * directly within the Tomcat directory as "sakai/".
     */
    sakai_home("sakai.home"),

    /** The "base" directory for Tomcat/Catalina. This is essentially where Sakai is installed. */
    catalina_base("catalina.base"),
    sakai_components_root("sakai.components.root"),
    sakai_security("sakai.security");

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
    Optional<String> get() {
        return Optional.ofNullable(System.getProperty(key));
    }

    /** Get the value of this property as a Path; currently implemented over System properties. */
    Optional<Path> getPath() {
        return get().map(Path::of);
    }

    /**
     * Get the value of this property as a Path, with a resolved extension; currently implemented over System
     * properties.
     */
    Optional<Path> getPathPlus(String other) {
        return getPath().map(p -> p.resolve(other));
    }

    /** Get the value of this property, with a default value; currently implemented over System properties. */
    Optional<String> get(String def) {
        return Optional.ofNullable(System.getProperty(key, def));
    }

    /** Get the unchecked value of this property; currently implemented over System properties. */
    String getRaw() {
        return System.getProperty(key);
    }

    /** Get the unchecked value of this property, with a default value; currently implemented over System properties. */
    String getRaw(String def) {
        return System.getProperty(key, def);
    }

    /** Get the unchecked value of this property as a Path; currently implemented over System properties. */
    Path getRawPath() {
        return get().map(Path::of).orElse(null);
    }

    /**
     * Get this property as a Path, with a resolved extension; currently implemented over System properties.
     * <p>
     * The extension is applied if the property is set. This returns the combined path if so, null otherwise.
     */
    Path getRawPathPlus(String other) {
        return getPath().map(p -> p.resolve(other)).orElse(null);
    }

    /** Set the value of this property; currently implemented over System properties. */
    void set(String value) {
        System.setProperty(key, value);
    }

    /** Set the value of this property to a Path; currently implemented over System properties. */
    void set(Path value) {
        set(value.toString());
    }

    /** Look up a property enum object by its key. */
    public static Optional<SysProp> lookup(String key) {
        return Arrays.stream(SysProp.values())
                .filter(prop -> prop.key.equalsIgnoreCase(key))
                .findFirst();
    }
}
