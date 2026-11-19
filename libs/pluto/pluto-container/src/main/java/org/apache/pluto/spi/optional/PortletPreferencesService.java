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
package org.apache.pluto.spi.optional;

import javax.portlet.PortletRequest;

import org.apache.pluto.PortletWindow;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.internal.InternalPortletPreference;
import org.apache.pluto.spi.ContainerService;

/**
 * Portlet preferences service that should be implemented by the portal.
 * This is an optional container service.
 *
 */
public interface PortletPreferencesService extends ContainerService {

	/**
	 * Returns the stored portlet preferences.
	 * @param portletWindow  the portlet window.
	 * @param request  the portlet request.
	 * @return the stored portlet preferences.
	 * @throws PortletContainerException  if fail to get stored preferences.
	 */
    public InternalPortletPreference[] getStoredPreferences(
    		PortletWindow portletWindow,
    		PortletRequest request)
    throws PortletContainerException;

    /**
     * Stores the portlet references to the persistent storage.
     * @param portletWindow  the portlet window.
     * @param request  the portlet request.
     * @param preferences  the portlet preferences to store.
     * @throws PortletContainerException  if fail to store preferences.
     */
    public void store(PortletWindow portletWindow,
                      PortletRequest request,
                      InternalPortletPreference[] preferences)
    throws PortletContainerException;

}
