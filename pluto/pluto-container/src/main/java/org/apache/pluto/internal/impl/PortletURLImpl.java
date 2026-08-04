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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.portlet.SupportsDD;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.spi.PortletURLProvider;
import org.apache.pluto.util.StringManager;
import org.apache.pluto.util.StringUtils;

public class PortletURLImpl implements PortletURL {

	private static final Log LOG = LogFactory.getLog(PortletURLImpl.class);
	
    private static final StringManager EXCEPTIONS =
        StringManager.getManager(PortletURLImpl.class.getPackage().getName());


    private PortletContainer container;
    protected PortletMode mode = null;

    protected Map parameters = new HashMap();

    protected InternalPortletWindow internalPortletWindow;

    protected boolean secure;
    protected javax.servlet.http.HttpServletRequest servletRequest;
    protected javax.servlet.http.HttpServletResponse servletResponse;
    protected WindowState state;
    protected boolean isAction;

    private PortalContext context;

    public PortletURLImpl(PortletContainer container,
                          InternalPortletWindow internalPortletWindow,
                          javax.servlet.http.HttpServletRequest servletRequest,
                          javax.servlet.http.HttpServletResponse servletResponse,
                          boolean isAction) {
        this.container = container;
        this.internalPortletWindow = internalPortletWindow;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        secure = servletRequest.isSecure();
        this.isAction = isAction;
        this.context = container.getRequiredContainerServices().getPortalContext();
    }

    // javax.portlet.PortletURL -------------------------------------------------------------------
    public void setWindowState(WindowState windowState)
        throws WindowStateException {
        if (windowState != null && isWindowStateAllowed(windowState)) {
            state = windowState;
            return;
        }

        throw new WindowStateException(
            "unsupported Window State used: " + windowState, windowState);
    }

    public void setPortletMode(PortletMode portletMode)
        throws PortletModeException {
        // Test and throw exception if not allowed.
        isPortletModeAllowed(portletMode);
        mode = portletMode;
    }

    public void setParameter(String name, String value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException(
                "name and value must not be null");
        }

        parameters.put(name, new String[]{value});
    }

    public void setParameter(String name, String[] values) {
        if (name == null || values == null || values.length == 0) {
            throw new IllegalArgumentException(
                "name and values must not be null or values be an empty array");
        }

        parameters.put(name, StringUtils.copy(values));
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletURL#setParameters(Map)
     */
    public void setParameters(Map parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        for (Iterator it = parameters.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            if (!(entry.getKey() instanceof String)) {
                throw new IllegalArgumentException(
                    "Key must not be null and of type java.lang.String.");
            }
            if (!(entry.getValue() instanceof String[])) {
                throw new IllegalArgumentException(
                    "Value must not be null and of type java.lang.String[].");
            }
        }

        this.parameters = StringUtils.copyParameters(parameters);
    }

    public void setSecure(boolean secure) throws PortletSecurityException {
        if (!secure) {
            return;
        }
        PortletURLProvider urlProvider = container
        		.getRequiredContainerServices()
        		.getPortalCallbackService()
        		.getPortletURLProvider(servletRequest, internalPortletWindow);
        if (urlProvider.isSecureSupported()) {
            urlProvider.setSecure();
        } else {
        	LOG.info("Secure URLs not supported.");
        }
    }

    public String toString() {
        StringBuffer url = new StringBuffer(200);

        PortletURLProvider urlProvider = container
        		.getRequiredContainerServices()
        		.getPortalCallbackService()
        		.getPortletURLProvider(servletRequest, internalPortletWindow);

        if (mode != null) {
            urlProvider.setPortletMode(mode);
        }
        if (state != null) {
            urlProvider.setWindowState(state);
        }
        if (isAction) {
            urlProvider.setAction(true);
        }
        if (secure && urlProvider.isSecureSupported()) {
            try {
                urlProvider.setSecure();
            } catch (PortletSecurityException e) {
                throw new IllegalStateException("URL Provider is misconfigured." +
                    "  It claims to support secure urls," +
                    " yet it threw a PortletSecurityException");
            }
        }
        urlProvider.clearParameters();
        urlProvider.setParameters(parameters);

        url.append(urlProvider.toString());

        return url.toString();
    }
    // --------------------------------------------------------------------------------------------


    // additional methods -------------------------------------------------------------------------
    public String getParameter(String name) {
        return (String) parameters.get(name);
    }

    public String[] getParameters(String name) {
        return (String[]) parameters.get(name);
    }

    public PortletMode getPortletMode() {
        return mode;
    }

    public WindowState getWindowState() {
        return state;
    }
    // --------------------------------------------------------------------------------------------


    private boolean isPortletModeAllowed(PortletMode mode)
        throws PortletModeException {
        if (mode == null) {
            throw new PortletModeException(
                EXCEPTIONS.getString("javax.portlet.PortletModeException.null"),
                null);
        }

        return isPortletModeAllowedByPortlet(mode)
               && isPortletModeAllowedByPortal(mode);
    }

    private boolean isPortletModeAllowedByPortlet(PortletMode mode)
        throws PortletModeException {
        // PLT 8.1: View Portlet Mode should always be
        // supported by a portlet, even if not defined in the descriptor
        if (mode.equals(PortletMode.VIEW)) {
            return true;
        }

        PortletDD dd = internalPortletWindow.getPortletEntity()
            .getPortletDefinition();
        Iterator supports = dd.getSupports().iterator();
        while(supports.hasNext()) {
            SupportsDD support = (SupportsDD)supports.next();
            Iterator modes = support.getPortletModes().iterator();
            while(modes.hasNext()) {
                String md = (String)modes.next();
                if (md.toUpperCase().equals(
                    mode.toString().toUpperCase())) {
                    return true;
                }
            }
        }
        String message = EXCEPTIONS.getString(
            "javax.portlet.PortletModeException.portlet", mode.toString());

        throw new PortletModeException(message, mode);
    }

    private boolean isPortletModeAllowedByPortal(PortletMode mode)
        throws PortletModeException {
        Enumeration supportedModes = context.getSupportedPortletModes();
        while (supportedModes.hasMoreElements()) {
            if (supportedModes.nextElement().toString().toUpperCase().equals(
                (mode.toString().toUpperCase()))) {
                return true;
            }
        }
        String message = EXCEPTIONS.getString(
            "javax.portlet.PortletModeException.portal", mode.toString());

        throw new PortletModeException(message, mode);
    }

    private boolean isWindowStateAllowed(WindowState state) {
        Enumeration supportedStates = context.getSupportedWindowStates();
        while (supportedStates.hasMoreElements()) {
            if (supportedStates.nextElement().toString().toUpperCase().equals(
                (state.toString().toUpperCase()))) {
                return true;
            }
        }
        return false;
    }
}
