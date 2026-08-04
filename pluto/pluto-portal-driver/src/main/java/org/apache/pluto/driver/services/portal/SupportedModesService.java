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

/**
 * Allows clients to determine if a particular PortletMode is supported
 * by the portal, a particular portlet, or both.
 *
 * @version $Id: SupportedModesService.java 516329 2007-03-09 08:43:46Z cziegeler $
 * @since September 9, 2006
 * @see javax.portlet.PortletMode
 */
public interface SupportedModesService extends DriverConfigurationService {

    /**
     * Returns true if the portlet and the portal support the supplied mode.
     * @param portletId the id uniquely identifiying the portlet
     * @param mode the portlet mode
     * @return true if the portlet and portal both support the supplied mode
     */
    boolean isPortletModeSupported(String portletId, String mode);

    /**
     * Returns true if the portal supports the supplied mode.
     * @param mode the portlet mode
     * @return true if the portal supports the supplied mode
     */
    boolean isPortletModeSupportedByPortal(String mode);

    /**
     * Returns true if the portlet supports the supplied mode.
     * @param portletId the id uniquely identifying the portlet
     * @param mode the portlet mode
     * @return true if the portlet supports the supplied mode.
     */
    boolean isPortletModeSupportedByPortlet(String portletId, String mode);

}
