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

/**
 * Portlet Application Configuration.
 *
 * @version $Id: PortletAppDD.java 157475 2005-03-14 22:13:18Z ddewolf $
 * @since Mar 6, 2005
 */
public class PortletAppDD {

    /** PortletApplication descriptor version */
    private String version;

    /** The defined portlets within the system. */
    private List portlets = new ArrayList();

    /** The defined custom modes within the application. */
    private List customPortletModes = new ArrayList();

    /** The defined custom states within the application. */
    private List customWindowStates = new ArrayList();

    /** The defined userAttributes within the app. */
    private List userAttributes = new ArrayList();

    /** The defined constraints */
    private List securityConstraints = new ArrayList();

    /**
     * Default Constructor.
     */
    public PortletAppDD() {
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Retrieve the portlets which exist within this application.
     * @return
     */
    public List getPortlets() {
        return portlets;
    }

    /**
     * Set the portlets that exist within this application.
     * @param portlets
     */
    public void setPortlets(List portlets) {
        this.portlets = portlets;
    }

    public List getCustomPortletModes() {
        return customPortletModes;
    }

    public void setCustomPortletModes(List customPortletModes) {
        this.customPortletModes = customPortletModes;
    }

    public List getCustomWindowStates() {
        return customWindowStates;
    }

    public void setCustomWindowStates(List customWindowStates) {
        this.customWindowStates = customWindowStates;
    }

    public List getUserAttributes() {
        return userAttributes;
    }

    public void setUserAttributes(List userAttributes) {
        this.userAttributes = userAttributes;
    }

    public List getSecurityConstraints() {
        return securityConstraints;
    }

    public void setSecurityConstraints(List securityConstraints) {
        this.securityConstraints = securityConstraints;
    }
}
