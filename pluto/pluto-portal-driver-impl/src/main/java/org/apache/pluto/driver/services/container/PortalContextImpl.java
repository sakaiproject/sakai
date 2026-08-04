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
package org.apache.pluto.driver.services.container;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.driver.config.DriverConfiguration;

/**
 * <code>PortalContext</code> implementation for the Pluto Portal Driver.
 */
public class PortalContextImpl implements PortalContext {

    /**
     * The <code>DriverConfigurationImpl</code> from which this
     * <code>PortalContext</code> recieves it's configuration information.
     */
    private DriverConfiguration config;

    /**
     * Portal information.
     */
    private String info = null;

    /**
     * Portal Properties
     */
    private HashMap properties = new HashMap();

    /**
     * Supported PortletModes.
     */
    private Vector portletModes;

    /**
     * Supported WindowStates.
     */
    private Vector windowStates;


    /**
     * Default Constructor.
     * @param config
     */
    public PortalContextImpl(DriverConfiguration config) {
        this.config = config;
        reset();
    }

    /**
     * Get a dynamic portal property.
     * @param name
     * @return the property value associated with the given key.
     * @throws IllegalArgumentException if the specified name is null.
     */
    public String getProperty(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Property name == null");
        }

        return (String) properties.get(name);
    }


    /**
     * Get an enumeration containing all names of the portal properties.
     * @return an enumeration of all keys to which properties are bound.
     */
    public Enumeration getPropertyNames() {
        Vector names = new Vector(properties.keySet());
        return names.elements();
    }


    /**
     * Get an enumeration of all <code>PortletMode</code>s supported by this
     * portal.
     * @return enumeration of all supported portlet modes.
     */
    public Enumeration getSupportedPortletModes() {
        if (portletModes == null) {
            portletModes = new Vector();
            Enumeration enumeration = new Vector(config.getSupportedPortletModes()).elements();
            while (enumeration.hasMoreElements()) {
                portletModes.add(
                    new PortletMode(enumeration.nextElement().toString()));
            }
        }
        return portletModes.elements();
    }

    /**
     * Get an enumeration of all <code>WindowState</code>s supported by this
     * portal.
     * @return an enumeration of all supported window states.
     */
    public Enumeration getSupportedWindowStates() {
        if (windowStates == null) {
            windowStates = new Vector();
            Enumeration enumeration = new Vector(config.getSupportedWindowStates()).elements();
            while (enumeration.hasMoreElements()) {
                windowStates.add(
                    new WindowState(enumeration.nextElement().toString()));
            }
        }
        return windowStates.elements();
    }

    /**
     * Get the portal info for this portal.
     * @return the portal information for this context.
     */
    public String getPortalInfo() {
        if(info == null) {
            info = config.getPortalName() + "/" + config.getPortalVersion();
        }
        return info;
    }


    // additional methods.
    // methods used container internally to set

    public void setProperty(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Property name == null");
        }

        properties.put(name, value);
    }


    /**
     * reset all values to default portlet modes and window states; delete all
     * properties and set the given portlet information as portlet info string.
     */
    public void reset() {
        info = null;
        properties.clear();
    }

    public DriverConfiguration getDriverConfiguration() {
        return config;
    }

}
