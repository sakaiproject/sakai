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
package org.apache.pluto.driver.config;

import java.util.Collection;

import javax.servlet.ServletContext;

import org.apache.pluto.driver.services.portal.PageConfig;
import org.apache.pluto.driver.services.portal.RenderConfigService;
import org.apache.pluto.driver.url.PortalURLParser;
import org.apache.pluto.spi.PortalCallbackService;
import org.apache.pluto.spi.optional.PortletPreferencesService;

/**
 * Interface defining a means for retrieving driver services
 * based upon configuration information. Within the portal,
 * an implementation of this interface should be bound to
 * the portal's ServletContext.
 *
 *
 * @since Sep 2, 2005
 *
 */
public interface DriverConfiguration {

//
// Lifecycle Methods
//

    /**
     * Initialization method used to place the driver
     * configuration into service.
     * @throws DriverConfigurationException when an error occurs during startup.
     * @param context
     */
    void init(ServletContext context) throws DriverConfigurationException;

    /**
     * Shutdown method used to remove the driver
     * configuration from service;
     * @throws DriverConfigurationException when an error occurs during shutdown.
     */
    void destroy() throws DriverConfigurationException;

//
// Service / Configuration Methods
//

    /**
     * Retrieve the name of the portal
     * as should be returned in
     * {@link javax.portlet.PortalContext#getPortalInfo()}
     * @return the name of the portal.
     */
    String getPortalName();

    /**
     * Retrieve the version of the portal
     * as should be returned in
     * {@link javax.portlet.PortalContext#getPortalInfo()}
     * @return the portal version.
     */
    String getPortalVersion();

    /**
     * Retrieves the name of the container which
     * pluto should create and embed.
     * @return the container name.
     */
    String getContainerName();

    Collection getSupportedPortletModes();

    Collection getSupportedWindowStates();

    Collection getPages();

    PageConfig getPageConfig(String pageId);

    boolean isPortletModeSupportedByPortal(String mode);

    boolean isPortletModeSupportedByPortlet(String portletId, String mode);

    boolean isPortletModeSupported(String portletId, String mode);

    boolean isWindowStateSupportedByPortal(String windowState);

    boolean isWindowStateSupportedByPortlet(String portletId, String windowState);

    boolean isWindowStateSupported(String portletId, String windowState);

//
// Utility methods for the container
//
    PortalCallbackService getPortalCallbackService();

    PortletPreferencesService getPortletPreferencesService();

    PortalURLParser getPortalUrlParser();
    
    public RenderConfigService getRenderConfigService();
}
