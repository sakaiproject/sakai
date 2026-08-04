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
package org.apache.pluto.driver.services.portal;

import java.util.Set;

/**
 * Service interface which defines the methods required
 * for a provider wishing to provide property management.
 *
 * @since Aug 10, 2005
 */
public interface PropertyConfigService extends DriverConfigurationService {

    /**
     * Name of the portal driver.
     * @return the name of this portal implementation
     */
    String getPortalName();

    /**
     * Portal driver version.
     * @return version information
     */
    String getPortalVersion();

    /**
     * Unique name of the Portlet Container
     * used to service this portal instance.
     * @return container name
     */
    String getContainerName();

    /**
     * Set of unique Portlet Modes.
     * The set must include
     * {@link javax.portlet.PortletMode#VIEW},
     * {@link javax.portlet.PortletMode#EDIT}, and
     * {@link javax.portlet.PortletMode#HELP}
     * @return set of unique portlet modes.
     */
    Set getSupportedPortletModes();

    /**
     * Set of unique Window States.
     * The set must include:
     * {@link javax.portlet.WindowState#NORMAL},
     * {@link javax.portlet.WindowState#MAXIMIZED}, and
     * {@link javax.portlet.WindowState#MINIMIZED}
     * @return set of unique window states.
     */
    Set getSupportedWindowStates();
}
