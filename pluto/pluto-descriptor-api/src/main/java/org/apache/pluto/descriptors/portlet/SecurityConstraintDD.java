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
package org.apache.pluto.descriptors.portlet;

import java.util.ArrayList;
import java.util.List;

import org.apache.pluto.descriptors.common.UserDataConstraintDD;


public class SecurityConstraintDD {

    private List displayNames = new ArrayList();
    private PortletCollectionDD portletCollection;
    private UserDataConstraintDD userDataConstraint;

    public List getDisplayNames() {
        return displayNames;
    }

    public void setDisplayNames(List displayNames) {
        this.displayNames = displayNames;
    }

    public PortletCollectionDD getPortletCollection() {
        return portletCollection;
    }

    public void setPortletCollection(PortletCollectionDD portletCollection) {
        this.portletCollection = portletCollection;
    }

    public UserDataConstraintDD getUserDataConstraint() {
        return userDataConstraint;
    }

    public void setUserDataConstraint(UserDataConstraintDD userDataConstraint) {
        this.userDataConstraint = userDataConstraint;
    }
}
