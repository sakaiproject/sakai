/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.descriptors.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Security Role Reference Configuration.
 *
 * @version $Id: SecurityRoleRefDD.java 157038 2005-03-11 03:44:40Z ddewolf $
 * @since Feb 28, 2005
 */
public class SecurityRoleRefDD {

    /** The name of the role reference. */
    private String roleName;

    /** The role to which the reference is linked. */
    private String roleLink;

    /** The descriptions of what the role is utilized for. */
    private List descriptions = new ArrayList();

    /**
     * Default Constructor.
     */
    public SecurityRoleRefDD() {

    }

    /**
     * Retrieve the name of the role reference.
     * @return
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Set the name of the role reference.
     * @param roleName
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * Retrieve then role to which the reference is linked.
     * @return
     */
    public String getRoleLink() {
        return roleLink;
    }

    /**
     * Set the role to which the reference is linked.
     * @param roleLink
     */
    public void setRoleLink(String roleLink) {
        this.roleLink = roleLink;
    }

    public List getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List descriptions) {
        this.descriptions = descriptions;
    }
}


