/**
 * $Id$
 * $URL$
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.providers.model;

import java.util.Properties;
import java.util.Set;

import org.azeckoski.reflectutils.annotations.ReflectIgnoreClassFields;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.sakaiproject.tool.api.Tool;

/**
 * EntityTool
 * 
 * This entity represents a sakai Tool configuration
 *
 * @author Earle Nietzel
 * Created on Sep 5, 2013
 * 
 */
@ReflectIgnoreClassFields({"accessSecurity"})
public class EntityTool implements Tool, Comparable<Tool> {

    private static final String ACCESS_SECURITY_PORTAL = "portal";
    private static final String ACCESS_SECURITY_TOOL = "tool";

    private transient Tool tool;

    @EntityId
    private String id;
    private String home;
    private String title;
    private String description;
    private Properties registeredConfig;
    private Properties mutableConfig;
    private Properties finalConfig;
    private Set<String> keywords;
    private Set<String> categories;
    private String access;

    public EntityTool(Tool tool) {
        if (tool != null) {
            this.tool = tool;

            this.id = tool.getId();
            this.home = tool.getHome();
            this.title = tool.getTitle();
            this.description = tool.getDescription();
            this.registeredConfig = tool.getRegisteredConfig();
            this.mutableConfig = tool.getMutableConfig();
            this.finalConfig = tool.getFinalConfig();
            this.keywords = tool.getKeywords();
            this.categories = tool.getCategories();
            this.access = getAccessSecurity() == Tool.AccessSecurity.PORTAL ? ACCESS_SECURITY_PORTAL : ACCESS_SECURITY_TOOL;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public EntityTool() {
    }

    @Override
    @EntityId
    public String getId() {
        return this.id;
    }

    @Override
    public String getHome() {
        return this.home;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Properties getRegisteredConfig() {
        return this.registeredConfig;
    }

    @Override
    public Properties getMutableConfig() {
        return this.mutableConfig;
    }

    @Override
    public Properties getFinalConfig() {
        return this.finalConfig;
    }

    @Override
    public Set<String> getKeywords() {
        return this.keywords;
    }

    @Override
    public Set<String> getCategories() {
        return this.categories;
    }

    @Override
    public AccessSecurity getAccessSecurity() {
        if (tool != null) {
            return tool.getAccessSecurity();
        }
        throw new UnsupportedOperationException();
    }

    public String getAccess() {
        return access;
    }

    @Override
    public int compareTo(Tool tool) {
        return getId().compareTo(tool.getId());
    }

}
