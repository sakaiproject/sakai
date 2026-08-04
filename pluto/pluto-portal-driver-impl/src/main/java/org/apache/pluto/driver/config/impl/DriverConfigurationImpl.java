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
package org.apache.pluto.driver.config.impl;

import java.util.Collection;

import javax.servlet.ServletContext;

import org.apache.pluto.driver.config.DriverConfiguration;
import org.apache.pluto.driver.services.portal.PageConfig;
import org.apache.pluto.driver.services.portal.PropertyConfigService;
import org.apache.pluto.driver.services.portal.RenderConfigService;
import org.apache.pluto.driver.services.portal.SupportedModesService;
import org.apache.pluto.driver.services.portal.SupportedWindowStateService;
import org.apache.pluto.driver.url.PortalURLParser;
import org.apache.pluto.spi.PortalCallbackService;
import org.apache.pluto.spi.optional.PortletPreferencesService;

/**
 * Encapsulation of the Pluto Driver ResourceConfig.
 *
 * @version 1.0
 * @since Sep 23, 2004
 */
public class DriverConfigurationImpl
    implements DriverConfiguration {

    private PortalURLParser portalUrlParser;
    private PropertyConfigService propertyService;
    private RenderConfigService renderService;
    private SupportedModesService supportedModesService;
    private SupportedWindowStateService supportedWindowStateService;

    // Container Services
    private PortalCallbackService portalCallbackService;
    private PortletPreferencesService portletPreferencesService;

    public DriverConfigurationImpl(PortalURLParser portalUrlParser,
                                   PropertyConfigService propertyService,
                                   RenderConfigService renderService,
                                   SupportedModesService supportedModesService,
                                   SupportedWindowStateService supportedWindowStateService,
                                   PortalCallbackService portalCallback) {

        this.portalUrlParser = portalUrlParser;
        this.propertyService = propertyService;
        this.renderService = renderService;
        this.portalCallbackService = portalCallback;
        this.supportedModesService = supportedModesService;
        this.supportedWindowStateService = supportedWindowStateService;
    }

    /**
     * Standard Getter.
     * @return the name of the portal.
     */
    public String getPortalName() {
        return propertyService.getPortalName();
    }

    /**
     * Standard Getter.
     * @return the portal version.
     */
    public String getPortalVersion() {
        return propertyService.getPortalVersion();
    }

    /**
     * Standard Getter.
     * @return the name of the container.
     */
    public String getContainerName() {
        return propertyService.getContainerName();
    }

    /**
     * Standard Getter.
     * @return the names of the supported portlet modes.
     */
    public Collection getSupportedPortletModes() {
        return propertyService.getSupportedPortletModes();
    }

    /**
     * Standard Getter.
     * @return the names of the supported window states.
     */
    public Collection getSupportedWindowStates() {
        return propertyService.getSupportedWindowStates();
    }

    /**
     * Standard Getter.
     * @return the render configuration.
     */
    public Collection getPages() {
        return renderService.getPages();
    }

    public PageConfig getPageConfig(String pageId) {
        return renderService.getPage(pageId);
    }

    public boolean isPortletModeSupportedByPortal(String mode) {
        return supportedModesService.isPortletModeSupportedByPortal(mode);
    }

    public boolean isPortletModeSupportedByPortlet(String portletId, String mode) {
        return supportedModesService.isPortletModeSupportedByPortlet(portletId, mode);
    }

    public boolean isPortletModeSupported(String portletId, String mode) {
        return supportedModesService.isPortletModeSupported(portletId, mode);
    }

    public void init(ServletContext context) {
        this.propertyService.init(context);
        this.renderService.init(context);
        this.supportedModesService.init(context);
        this.supportedWindowStateService.init(context);
    }

    public void destroy() {
        if(propertyService != null)
            propertyService.destroy();

        if(renderService != null)
            renderService.destroy();

        if (supportedModesService != null)
            supportedModesService.destroy();

        if (supportedWindowStateService != null)
            supportedWindowStateService.destroy();
    }

//
// Portal Driver Services
//

    public PortalURLParser getPortalUrlParser() {
        return portalUrlParser;
    }

    public void setPortalUrlParser(PortalURLParser portalUrlParser) {
        this.portalUrlParser = portalUrlParser;
    }

//
// Container Services
//
    public PortalCallbackService getPortalCallbackService() {
        return portalCallbackService;
    }

    public void setPortalCallbackService(PortalCallbackService portalCallbackService) {
        this.portalCallbackService = portalCallbackService;
    }

    public PortletPreferencesService getPortletPreferencesService() {
        return portletPreferencesService;
    }

    public void setPortletPreferencesService(PortletPreferencesService portletPreferencesService) {
        this.portletPreferencesService = portletPreferencesService;
    }

    public boolean isWindowStateSupported(String portletId, String windowState)
    {
        return supportedWindowStateService.isWindowStateSupported(portletId, windowState);
    }

    public boolean isWindowStateSupportedByPortal(String windowState)
    {
        return supportedWindowStateService.isWindowStateSupportedByPortal(windowState);
    }

    public boolean isWindowStateSupportedByPortlet(String portletId, String windowState)
    {
        return supportedWindowStateService.isWindowStateSupportedByPortlet(portletId, windowState);
    }
    
    public RenderConfigService getRenderConfigService(){
    	return renderService;
    }
}

