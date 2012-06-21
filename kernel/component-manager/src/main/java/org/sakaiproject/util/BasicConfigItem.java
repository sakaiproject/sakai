/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 Sakai Foundation
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

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigHistory;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigItem;

/**
 * Provides an easy way for someone to create a {@link ConfigItem} which is valid without having to build their own implementation
 * 
 * Use the static methods to easily generate the {@link ConfigItem} for use with the methods in the {@link ServerConfigurationService}
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class BasicConfigItem implements ConfigItem {
    /**
     * the name/key for this configuration value
     */
    protected String name;
    /**
     * the actual stored value for this config (null indicates it is not set)
     */
    protected Object value = null;
    /**
     * the default value for this config (null indicates it is not set)
     */
    protected Object defaultValue = null;
    /**
     * the human readable description for this configuration value (null if not set)
     */
    protected String description = null;
    /**
     * the name of the most recent source for this config value (e.g. sakai/sakai.properties)
     */
    protected String source = ServerConfigurationService.UNKNOWN;
    /**
     * Indicates if this config item is dynamic (true) or static (false).
     * Default false, static config items cannot be changed at runtime, dynamic items can be changed at runtime
     */
    protected boolean dynamic = false;

    /**
     * Do NOT use this - INTERNAL ONLY
     */
    protected BasicConfigItem() {}

    /**
     * Constructor for when you know the source,
     * leave defaultValue null if you do not know have one 
     * OR if this is a registration for a defaultValue then fill in the defaultValue and leave the value null
     * (either value OR defaultValue MUST be set)
     * 
     * Recommend use of the static make methods unless you know what you are doing
     * 
     * @param name the config name key (the ID of this configuration setting)
     * @param value [OPTIONAL] the config value (this IS the configurations setting)
     * @param defaultValue [OPTIONAL] the default value for this config
     * @param description [OPTIONAL] the human readable description of this config setting
     * @param source [OPTIONAL] the name of the origin for this config setting (defaults to UNKNOWN)
     * @param dynamic default false, static config items cannot be changed at runtime, dynamic items can be changed at runtime
     */
    public BasicConfigItem(String name, Object value, Object defaultValue, String description, String source, boolean dynamic) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name must be set");
        }
        this.name = name;
        if (value == null && defaultValue == null) {
            throw new IllegalArgumentException("value OR defaultValue must be set");
        }
        this.value = value;
        this.defaultValue = defaultValue;
        this.description = description;
        if (source != null && !"".equals(source)) {
            this.source = source;
        }
        this.dynamic = dynamic;
    }

    public int requested() {
        return 0;
    }

    public int changed(Object value, String source) {
        return 0;
    }

    public ConfigItem copy() {
        return new BasicConfigItem(this.name, this.value, this.defaultValue, this.description, this.source, this.dynamic);
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public String getType() {
        return ServerConfigurationService.UNKNOWN;
    }

    public String getDescription() {
        return description;
    }

    public String getSource() {
        return source;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public int getRequested() {
        return 0;
    }

    public int getChanged() {
        return 0;
    }

    public int getVersion() {
        return 1;
    }

    public ConfigHistory[] getHistory() {
        return new ConfigHistory[0];
    }

    public boolean isRegistered() {
        return false;
    }

    public boolean isDefaulted() {
        return defaultValue != null;
    }

    public boolean isSecured() {
        return false;
    }

    public boolean isDynamic() {
        return dynamic;
    }


    /**
     * Create a config item which stores a default value but does not have an actual value
     * @param name the config name key
     * @param defaultValue the default value for this config
     * @param source the origin of this config setting
     * @return the {@link ConfigItem} which can be registered with the {@link ServerConfigurationService}
     */
    public static ConfigItem makeDefaultedConfigItem(String name, Object defaultValue, String source) {
        return new BasicConfigItem(name, null, defaultValue, null, source, false);
    }

    /**
     * Create a basic config item which stores a value
     * @param name the config name key
     * @param value the default value for this config
     * @param source the origin of this config setting
     * @return the {@link ConfigItem} which can be registered with the {@link ServerConfigurationService}
     */
    public static ConfigItem makeConfigItem(String name, Object value, String source) {
        return new BasicConfigItem(name, value, null, null, source, false);
    }

    /**
     * Create a basic config item which can be dynamic
     * @param name the config name key
     * @param value the default value for this config
     * @param source the origin of this config setting
     * @param dynamic default false, static config items cannot be changed at runtime, dynamic items can be changed at runtime
     * @return the {@link ConfigItem} which can be registered with the {@link ServerConfigurationService}
     */
    public static ConfigItem makeConfigItem(String name, Object value, String source, boolean dynamic) {
        return new BasicConfigItem(name, value, null, null, source, dynamic);
    }

    /**
     * Create a basic config item with description which can be dynamic
     * @param name the config name key
     * @param value the default value for this config
     * @param description the human readable description of this configuration value
     * @param source the origin of this config setting
     * @param dynamic default false, static config items cannot be changed at runtime, dynamic items can be changed at runtime
     * @return the {@link ConfigItem} which can be registered with the {@link ServerConfigurationService}
     */
    public static ConfigItem makeConfigItem(String name, Object value, String description, String source, boolean dynamic) {
        return new BasicConfigItem(name, value, null, description, source, dynamic);
    }

    /**
     * Create a complete config item with default value and a description which can be dynamic
     * @param name the config name key
     * @param value the default value for this config
     * @param defaultValue the default value for this config
     * @param description the human readable description of this configuration value
     * @param source the origin of this config setting
     * @param dynamic default false, static config items cannot be changed at runtime, dynamic items can be changed at runtime
     * @return the {@link ConfigItem} which can be registered with the {@link ServerConfigurationService}
     */
    public static ConfigItem makeConfigItem(String name, Object value, Object defaultValue, String description, String source, boolean dynamic) {
        return new BasicConfigItem(name, value, defaultValue, description, source, dynamic);
    }

}
