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

import javax.portlet.PortalContext;

import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.RequiredContainerServices;
import org.apache.pluto.driver.config.DriverConfiguration;
import org.apache.pluto.spi.PortalCallbackService;
import org.apache.pluto.spi.optional.PortalAdministrationService;
import org.apache.pluto.spi.optional.PortletEnvironmentService;
import org.apache.pluto.spi.optional.PortletInfoService;
import org.apache.pluto.spi.optional.PortletInvokerService;
import org.apache.pluto.spi.optional.PortletPreferencesService;
import org.apache.pluto.spi.optional.PortletRegistryService;
import org.apache.pluto.spi.optional.RequestAttributeService;
import org.apache.pluto.spi.optional.UserInfoService;

/**
 * The Portal Driver's <code>PortletContainerServices</code> implementation. The
 * <code>PortletContainerServices</code> interface is the main integration point
 * between the pluto container and the surrounding portal.
 * @version 1.0
 * @since Sep 21, 2004
 */
public class ContainerServicesImpl
    implements RequiredContainerServices, OptionalContainerServices {


    private PortalContextImpl context;
    private DriverConfiguration driverConfig;


    /**
     * Default Constructor.
     */
    public ContainerServicesImpl(PortalContextImpl context,
                                 DriverConfiguration driverConfig) {
        this.context = context;
        this.driverConfig = driverConfig;
    }

    /**
     * Standard Getter.
     * @return the portal context for the portal which we service.
     */
    public PortalContext getPortalContext() {
        return context;
    }

    /**
     * The PortletPreferencesService provides access to the portal's
     * PortletPreference persistence mechanism.
     * @return a PortletPreferencesService instance.
     */
    public PortletPreferencesService getPortletPreferencesService() {
        return driverConfig.getPortletPreferencesService();
    }

    /**
     * The PortalCallbackService allows the container to communicate
     * actions back to the portal.
     * @return a PortalCallbackService implementation.
     */
    public PortalCallbackService getPortalCallbackService() {
        return driverConfig.getPortalCallbackService();
    }


    /**
     * Returns null to use pluto's default
     * @return
     */
    public PortletRegistryService getPortletRegistryService() {
        return null;
    }

    public PortletEnvironmentService getPortletEnvironmentService() {
        return null;
    }

    public PortletInvokerService getPortletInvokerService() {
        return null;
    }

    public PortletInfoService getPortletInfoService() {
        return null;
    }

    public PortalAdministrationService getPortalAdministrationService() {
        return null;
    }

    public UserInfoService getUserInfoService() {
        return null;
    }

    public RequestAttributeService getRequestAttributeService() {
        return null;
    }
}

