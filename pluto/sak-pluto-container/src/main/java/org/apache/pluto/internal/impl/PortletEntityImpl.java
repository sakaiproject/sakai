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
package org.apache.pluto.internal.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.portlet.PreferencesValidator;
import javax.portlet.ValidatorException;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.portlet.PortletPreferenceDD;
import org.apache.pluto.descriptors.portlet.PortletPreferencesDD;
import org.apache.pluto.descriptors.servlet.ServletDD;
import org.apache.pluto.internal.InternalPortletPreference;
import org.apache.pluto.internal.PortletDescriptorRegistry;
import org.apache.pluto.internal.PortletEntity;
import org.apache.pluto.internal.PreferencesValidatorRegistry;

/**
 * The PortletEntity encapsulates all data pertaining to a single portlet
 * instance.  This instance may appear zero or more times per user. The
 * PortletEntity consists of two primary peices of information, the Portlet
 * Definition as defined by the {@link PortletDD} and the Wrapping Servlet
 * information as defined by the{@link ServletDD}
 *
 */
public class PortletEntityImpl implements PortletEntity {

	/** Logger. */
    private static final Log LOG = LogFactory.getLog(PortletEntityImpl.class);

    /** URI prefix of the portlet invoker servlet. */
    private static final String PREFIX = "/PlutoInvoker/";


    // Private Member Variables ------------------------------------------------

    /** The servlet context. */
    private final ServletContext servletContext;

    /** The portlet window. */
    private final String portletName;

    /** The cached PortletDD retrieved from the portlet descriptor registry. */
    private PortletDD portletDefinition;

    /** Default portlet preferences defined for this portlet. */
    private InternalPortletPreference[] defaultPreferences ;


    // Constructor -------------------------------------------------------------

    PortletEntityImpl(ServletContext servletContext, String portletName) {
        this.servletContext = servletContext;
        this.portletName = portletName;
    }


    // PortletEntity Impl ------------------------------------------------------

    /**
     * Returns the URI to the controller servlet that wraps this portlet.
     * @return the URI to the controller servlet that wraps this portlet.
     * @deprecated
     */
    public String getControllerServletUri() {
        // this method is deprecated as of pluto 1.1.2, so that
        // we can remove it in the future.  The PortletInvokerService
        // will be responsible for resolving the URI used to invoke
        // the portlet.
        return PREFIX + portletName;
    }

    /**
     * Returns an array of default preferences of this portlet. The default
     * preferences are retrieved from the portlet application descriptor.
     * <p>
     * Data retrieved from <code>portlet.xml</code> are injected into the domain
     * object <code>PortletPreferenceDD</code>. This method converts the domain
     * objects into <code>PortletPreference</code> objects.
     * </p>
     * <p>
     * Note that if no value is bound to a given preference key,
     * <code>PortletPreferenceDD.getValues()</code> will return an empty string
     * list, but the value array of <code>PortletPreference</code> should be set
     * to null (instead of an empty array).
     * </p>
     * <p>
     * This method never returns null, but the values held by PortletPreference
     * may be null.
     * </p>
     * @return the preference set
     *
     * @see org.apache.pluto.descriptors.portlet.PortletPreferenceDD
     */
    public InternalPortletPreference[] getDefaultPreferences() {
        if (defaultPreferences == null) {
            PortletDD portletDD = getPortletDefinition();
            PortletPreferencesDD prefsDD = portletDD.getPortletPreferences();
            if (prefsDD != null) {
            	List prefs = new ArrayList();
            	for (Iterator it = prefsDD.getPortletPreferences().iterator();
            			it.hasNext(); ) {
            		PortletPreferenceDD prefDD = (PortletPreferenceDD) it.next();
            		String[] values = null;
            		if (prefDD.getValues() != null && prefDD.getValues().size() > 0) {
            			values = (String[]) prefDD.getValues().toArray(
            					new String[prefDD.getValues().size()]);
            		}
            		PortletPreferenceImpl pref = new PortletPreferenceImpl(
            				prefDD.getName(), values, prefDD.isReadOnly());
            		prefs.add(pref);
            	}
            	defaultPreferences = (InternalPortletPreference[])
            			prefs.toArray(new InternalPortletPreference[prefs.size()]);
            }
        }
        return defaultPreferences;
    }

    /**
     * Returns the portlet description. The return value cannot be NULL.
     * @return the portlet description.
     */
    public PortletDD getPortletDefinition() {
        if (portletDefinition == null) {
            load();
        }
        return portletDefinition;
    }

    /**
     * Returns the preferences validator instance for this portlet.
     * One validator instance is created per portlet definition.
     * @return the preferences validator instance for this portlet.
     * @throws ValidatorException  if fail to instantiate the validator.
     */
    public PreferencesValidator getPreferencesValidator()
    throws ValidatorException {
    	PreferencesValidator validator = PreferencesValidatorRegistry
    			.getRegistry()
    			.getPreferencesValidator(getPortletDefinition());
    	return validator;
    }


    // Private Methods ---------------------------------------------------------

    /**
     * Loads the portlet definition.
     */
    private void load() {

    	// Retrieve the cross servlet context for the portlet.
        ServletContext crossContext = servletContext;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieved cross context: " + crossContext);
        }

        // Load PortletAppDD and find out the portlet definition.
        try {
            PortletAppDD appDD = PortletDescriptorRegistry.getRegistry()
            		.getPortletAppDD(crossContext);
            for (Iterator it = appDD.getPortlets().iterator(); it.hasNext(); ) {
                PortletDD portletDD = (PortletDD) it.next();
                if (portletDD.getPortletName().equals(portletName)) {
                	portletDefinition = portletDD;
                	break;
                }
            }
        } catch (PortletContainerException ex) {
        	String message = "Unable to load Portlet App Deployment Descriptor:"
        			+ ex.getMessage();
        	LOG.error(message, ex);
        	// FIXME: should this be a NullPointerException?
            throw new NullPointerException(message);
        }
    }

}
