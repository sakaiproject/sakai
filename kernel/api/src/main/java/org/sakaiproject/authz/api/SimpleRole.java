/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2014 Apereo Foundation
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

package org.sakaiproject.authz.api;

import java.io.Serializable;
import java.util.Set;

/**
 * SimpleRole is a class that allows BaseRole (Role) to be serializable for distribution
 * This class should only have getters/setters and should be serializable.
 * KNL-1184
 */
public class SimpleRole implements Serializable {

    static final long serialVersionUID = 1L;

    private String id;
    private Set locks;
    private String description;
    private boolean providerOnly;
    private boolean active;

    public SimpleRole() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set getLocks() {
        return this.locks;
    }

    public void setLocks(Set locks) {
        this.locks = locks;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isProviderOnly() {
        return this.providerOnly;
    }

    public void setProviderOnly(boolean providerOnly) {
        this.providerOnly = providerOnly;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
