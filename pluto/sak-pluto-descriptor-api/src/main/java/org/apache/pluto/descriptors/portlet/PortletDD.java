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

import java.util.List;
import java.util.ArrayList;

/**
 * Bare bones implementation of the Portlet descriptor.
 *
 * FIXME: Hmmm... What do you mean, David?  --ZHENG Zhong
 *
 * Eventually this should be flushed out :), but for the sake of timing I'll be lazy for now.
 *
 *
 * @since Mar 6, 2005
 */
public class PortletDD {
    /**
     * The value of the expirationCache property when no expiration cache was configured
     * in portlet.xml for this portlet descriptor.
     */
    public static final int EXPIRATION_CACHE_UNSET = Integer.MIN_VALUE;

	// Private Member Variables ------------------------------------------------

    /** The unique name of the portlet. */
    private String portletName = null;

    /** The display name of the portlet. */
    private List displayNames = new ArrayList();

    /** The descriptions of the portlet. */
    private List descriptions = new ArrayList();

    private int expirationCache = EXPIRATION_CACHE_UNSET;

    /** The class which implements the portlet interface. */
    private String portletClass = null;

    private String resourceBundle = null;

    private PortletInfoDD portletInfo = null;

    private PortletPreferencesDD portletPreferences = new PortletPreferencesDD();

    private List initParams = new ArrayList();

    private List supports = new ArrayList();

    private List supportedLocales = new ArrayList();

    /** All security role references. */
    private List securityRoleRefs = new ArrayList();


    // Constructor -------------------------------------------------------------

    /**
     * Default no-arg constructor.
     */
    public PortletDD() {
    	// Do nothing.
    }


    // Public Methods ----------------------------------------------------------

    /**
     * Retrieve the unique name of the portlet.
     * @return
     */
    public String getPortletName() {
        return portletName;
    }

    /**
     * Set the unique name of the portlet.
     * @param portletName Value of the portlet-name element for this portlet in portlet.xml.
     * @throws IllegalArgumentException if the name has a period since it is used
     * to create the portlet ID in <code>PortletWindowConfig.createPortletId()</code>
     * using a dot to separate the context path from the portlet name.
     */
    public void setPortletName(String portletName) {
    	if (portletName.indexOf('.') != -1) {
    		throw new IllegalArgumentException("Portlet name must not have a dot(period). Please remove the dot from the value of the portlet-name element ("+ portletName + ") in portlet.xml");
    	}
        this.portletName = portletName;
    }

    public List getDisplayNames() {
        return displayNames;
    }

    public void setDisplayNames(List displayNames) {
        this.displayNames = displayNames;
    }

    public List getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List descriptions) {
        this.descriptions = descriptions;
    }

    public int getExpirationCache() {
        return expirationCache;
    }

    public void setExpirationCache(int expirationCache) {
        this.expirationCache = expirationCache;
    }

    /**
     * Retrieve the name of the portlet class.
     * @return the fully qualified portlet class name.
     */
    public String getPortletClass() {
        return portletClass;
    }

    /**
     * Set the name of the portlet class.
     * @param portletClass
     */
    public void setPortletClass(String portletClass) {
        this.portletClass = portletClass;
    }

    public String getResourceBundle() {
        return resourceBundle;
    }

    public void setResourceBundle(String resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public PortletInfoDD getPortletInfo() {
        return portletInfo;
    }

    public void setPortletInfo(PortletInfoDD portletInfo) {
        this.portletInfo = portletInfo;
    }

    public List getSupports() {
        return supports;
    }

    public void setSupports(List supports) {
        this.supports = supports;
    }

    public List getSupportedLocales() {
        return supportedLocales;
    }

    public void setSupportedLocales(List supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    public List getInitParams() {
        return initParams;
    }

    public void setInitParams(List initParams) {
        this.initParams = initParams;
    }

    public PortletPreferencesDD getPortletPreferences() {
        return portletPreferences;
    }

    public void setPortletPreferences(PortletPreferencesDD portletPreferences) {
        this.portletPreferences = portletPreferences;
    }

    /**
     * Retrieve the security role references for this portlet.
     * @return
     */
    public List getSecurityRoleRefs() {
        return securityRoleRefs;
    }

    /**
     * Set the security role references for this portlet.
     * @param securityRoleRefs
     */
    public void setSecurityRoleRefs(List securityRoleRefs) {
        this.securityRoleRefs = securityRoleRefs;
    }


    // Object Methods ----------------------------------------------------------

    /**
     * Returns a string representation of this instance.
     * FIXME: more info!
     * @return a string representation of this instance.
     */
    public String toString() {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(getClass().getName());
    	buffer.append("[portletName=").append(portletName);
    	buffer.append(",portletClass=").append(portletClass);
    	// TODO:
    	return buffer.toString();
    }

    /**
     * Returns the hash code for this instance.
     * @return the hash code for this instance.
     */
    public int hashCode() {
    	return toString().hashCode();
    }

}

