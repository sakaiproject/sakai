/******************************************************************************
 * $URL: $
 * $Id: $
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/

package org.sakaiproject.config.api;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigItem;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * KNL-1063
 * HibernateConfigItem
 * <p/>
 * This is strictly a Hibernate POJO for persisting ConfigItem(s).
 * <p/>
 * Please DO NOT USE THIS use ServerConfigurationService for adding or updating
 * sakai config properties.
 *
 * @author Earle Nietzel
 *         Created on Mar 8, 2013
 */
public class HibernateConfigItem implements Serializable {

    private static final long serialVersionUID = -5970590246550034440L;
    public static final String ARRAY_SEPARATOR = "::";

    private Long id;

    /**
     * the name/key for this configuration value
     */
    private String name;

    /**
     * The node associated with this configuration item,
     * null indicates that it applies to the complete system
     */
    private String node;

    /**
     * the actual stored string value for this config (null indicates it is not set)
     */
    private String value;

    /**
     * the raw stored string (before being expanded)
     */
    private String rawValue;

    /**
     * the type of the value (string, int, boolean, array)
     * ServerConfigurationService.TYPE_STRING;
     * ServerConfigurationService.TYPE_INT;
     * ServerConfigurationService.TYPE_BOOLEAN;
     * ServerConfigurationService.TYPE_ARRAY;
     */
    private String type;

    /**
     * indicates is this config is registered (true) or if it is only requested (false)
     * (requested means someone asked for it but the setting has not been stored in the config service)
     */
    private boolean registered;

    /**
     * indicates is this config is has a default value defined
     * (this can only be known for items which are requested)
     */
    private boolean defaulted;

    /**
     * indicates is this config value should not be revealed because there are security implications
     */
    private boolean secured;

    /**
     * the default value for this config item (null indicates it is not set)
     */
    private String defaultValue;

    /**
     * where this item was registered
     */
    private String source;

    private boolean dynamic;

    private String description;

    private Date created;

    private Date modified;

    private Date pollOn;

    public HibernateConfigItem() {
    }

    public HibernateConfigItem(String node, String name, String value, String rawValue, String type, String description, String source, String defaultValue,
                               boolean registered, boolean defaulted, boolean secured, boolean dynamic) {
        this.node = node;
        this.name = name;
        this.value = value;
        this.rawValue = rawValue;
        this.type = type;
        this.description = description;
        this.source = source;
        this.defaultValue = defaultValue;
        this.registered = registered;
        this.defaulted = defaulted;
        this.secured = secured;
        this.dynamic = dynamic;
        this.created = Calendar.getInstance().getTime();
        this.modified = this.created;
    }

    public HibernateConfigItem(String node, String name, String value, String type) {
        this(node, name, value, null, type, null, null, null, false, false, false, false);
    }

    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public boolean isDefaulted() {
        return defaulted;
    }

    public void setDefaulted(boolean defaulted) {
        this.defaulted = defaulted;
    }

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Date getPollOn() {
        return pollOn;
    }

    public void setPollOn(Date date) {
        this.pollOn = date;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((node == null) ? 0 : node.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HibernateConfigItem other = (HibernateConfigItem) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (node == null) {
            if (other.node != null) {
                return false;
            }
        } else if (!node.equals(other.node)) {
            return false;
        }
        return true;
    }

    public boolean similar(ConfigItem item) {
        if (item == null) {
            return false;
        }

        if (name == null) {
            if (item.getName() != null) {
                return false;
            }
        } else if (!name.equals(item.getName())) {
            return false;
        }

        if (type == null) {
            if (item.getType() != null) {
                return false;
            }
        } else if (!type.equals(item.getType())) {
            return false;
        }

        if (value == null) {
            if (item.getValue() != null) {
                return false;
            }
        } else {
            if (ServerConfigurationService.TYPE_STRING.equals(type)) {
                if (!value.equals(item.getValue())) {
                    return false;
                }
            } else if (ServerConfigurationService.TYPE_BOOLEAN.equals(type)) {
                if (!value.equals(String.valueOf(item.getValue()))) {
                    return false;
                }
            } else if (ServerConfigurationService.TYPE_INT.equals(type)) {
                if (!value.equals(String.valueOf(item.getValue()))) {
                    return false;
                }
            } else if (ServerConfigurationService.TYPE_ARRAY.equals(type)) {
                if (!value.equals(join((String[]) item.getValue(), ARRAY_SEPARATOR))) {
                    return false;
                }
            }
        }

        if (defaultValue == null) {
            if (item.getDefaultValue() != null) {
                return false;
            }
        } else {
            if (ServerConfigurationService.TYPE_STRING.equals(type)) {
                if (!defaultValue.equals(item.getDefaultValue())) {
                    return false;
                }
            } else if (ServerConfigurationService.TYPE_BOOLEAN.equals(type)) {
                if (!defaultValue.equals(String.valueOf(item.getDefaultValue()))) {
                    return false;
                }
            } else if (ServerConfigurationService.TYPE_INT.equals(type)) {
                if (!defaultValue.equals(String.valueOf(item.getDefaultValue()))) {
                    return false;
                }
            } else if (ServerConfigurationService.TYPE_ARRAY.equals(type)) {
                if (!defaultValue.equals(join((String[]) item.getDefaultValue(), ARRAY_SEPARATOR))) {
                    return false;
                }
            }
        }

        if (source == null) {
            if (item.getSource() != null) {
                return false;
            }
        } else if (!source.equals(item.getSource())) {
            return false;
        }

        if (description == null) {
            if (item.getDescription() != null) {
                return false;
            }
        } else if (!description.equals(item.getDescription())) {
            return false;
        }

        if (defaulted != item.isDefaulted()) {
            return false;
        }

        if (secured != item.isSecured()) {
            return false;
        }

        if (registered != item.isRegistered()) {
            return false;
        }

        if (dynamic != item.isDynamic()) {
            return false;
        }

        return true;
    }

    private String join(final String[] array, final String separator) {
        if (array == null) {
            return null;
        }
        final int noOfItems = array.length;
        if (noOfItems <= 0) {
            return "";
        }
        final StringBuilder buf = new StringBuilder(noOfItems * 16);
        for (int i = 0; i < noOfItems; i++) {
            if (i > 0) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    @Override
    public String toString() {
        return "HibernateConfigItem [" + (id != null ? "id=" + id + ", " : "") +
                       (name != null ? "name=" + name + ", " : "") +
                       (node != null ? "node=" + node + ", " : "") +
                       (value != null ? "value=" + value + ", " : "") +
                       (rawValue != null ? "rawValue=" + rawValue + ", " : "") +
                       (type != null ? "type=" + type + ", " : "") +
                       "registered=" + registered + ", " +
                       "defaulted=" + defaulted + ", " +
                       "secured=" + secured + ", " +
                       (defaultValue != null ? "defaultValue=" + defaultValue + ", " : "") +
                       (source != null ? "source=" + source + ", " : "") +
                       "dynamic=" + dynamic + ", " +
                       (description != null ? "description=" + description + ", " : "") +
                       "created=" + created.toString() + ", " +
                       "modified=" + modified.toString() + "]";
    }
}

