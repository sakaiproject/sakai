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
package org.apache.pluto.core;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.internal.InternalPortletPreference;
import org.apache.pluto.spi.optional.PortletPreferencesService;

/**
 * Default implementation of the portlet preferences service. The portlet
 * preferences service is a singleton held by the pluto portlet container.
 *
 * @see javax.portlet.PortletPreferences
 * @see org.apache.pluto.internal.impl.PortletPreferencesImpl
 * @see javax.portlet.PortletPreferences
 *
 */
public class DefaultPortletPreferencesService
implements PortletPreferencesService {

	/** Logger. */
	private static final Log LOG = LogFactory.getLog(
			DefaultPortletPreferencesService.class);


	// Private Member Variables ------------------------------------------------

	/**
	 * The in-memory portlet preferences storage: key is the preference name as
	 * a string, value is an array of PortletPreference objects.
	 */
	private final Map storage = new HashMap();


	// Constructor -------------------------------------------------------------

	/**
	 * Default no-arg constructor.
	 */
	public DefaultPortletPreferencesService() {
		// Do nothing.
	}


	// PortletPreferencesService Impl ------------------------------------------

	/**
	 * Returns the stored portlet preferences array. The preferences managed by
	 * this service should be protected from being directly accessed, so this
	 * method returns a cloned copy of the stored preferences.
	 *
	 * @param portletWindow  the portlet window.
	 * @param request  the portlet request from which the remote user is retrieved.
	 * @return a copy of the stored portlet preferences array.
	 * @throws PortletContainerException
	 */
	public InternalPortletPreference[] getStoredPreferences(
			PortletWindow portletWindow,
			PortletRequest request)
	throws PortletContainerException {
        String key = getFormattedKey(portletWindow, request);
        InternalPortletPreference[] preferences = (InternalPortletPreference[])
        		storage.get(key);
        if (preferences == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No portlet preferences found for: " + key);
            }
            return new InternalPortletPreference[0];
        } else {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("Got " + preferences.length + " stored preferences.");
        	}
        	return clonePreferences(preferences);
        }
    }

	/**
	 * Stores the portlet preferences to the in-memory storage. This method
	 * should be invoked after the portlet preferences are validated by the
	 * preference validator (if defined).
	 * <p>
	 * The preferences managed by this service should be protected from being
	 * directly accessed, so this method clones the passed-in preferences array
	 * and saves it.
	 * </p>
	 *
	 * @see javax.portlet.PortletPreferences#store()
	 *
	 * @param portletWindow  the portlet window
	 * @param request  the portlet request from which the remote user is retrieved.
	 * @param preferences  the portlet preferences to store.
	 * @throws PortletContainerException
	 */
    public void store(PortletWindow portletWindow,
                      PortletRequest request,
                      InternalPortletPreference[] preferences)
    throws PortletContainerException {
        String key = getFormattedKey(portletWindow, request);
        storage.put(key, clonePreferences(preferences));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Portlet preferences stored for: " + key);
        }
    }


    // Private Methods ---------------------------------------------------------

    /**
     * Formats the preference key for the portlet preference using specified
     * portlet window and remote user.
     * @param portletWindow  the portlet window.
     * @param request  the remote request.
     */
    private String getFormattedKey(PortletWindow portletWindow,
                                   PortletRequest request) {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("user=").append(request.getRemoteUser()).append(";");
    	buffer.append("portletName=").append(portletWindow.getPortletName());
    	return buffer.toString();
    }

    /**
     * Clones a PortletPreference array. This method performs a deep clone on
     * the passed-in portlet preferences array. Every PortletPreference object
     * in the array are cloned (via the <code>PortletPreference.clone()</code>
     * method) and injected into the new array.
     *
     * @param preferences  the portlet preferences array to clone.
     * @return a deep-cloned copy of the portlet preferences array.
     */
    private InternalPortletPreference[] clonePreferences(
    		InternalPortletPreference[] preferences) {
    	if (preferences == null) {
    		return null;
    	}
    	InternalPortletPreference[] copy =
    			new InternalPortletPreference[preferences.length];
    	for (int i = 0; i < preferences.length; i++) {
    		if (preferences[i] != null) {
    			copy[i] = (InternalPortletPreference) preferences[i].clone();
    		} else {
    			copy[i] = null;
    		}
    	}
    	return copy;
    }

}


