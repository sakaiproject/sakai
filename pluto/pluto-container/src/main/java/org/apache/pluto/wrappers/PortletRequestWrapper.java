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
package org.apache.pluto.wrappers;

import java.util.Enumeration;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;

public class PortletRequestWrapper
    extends javax.servlet.http.HttpServletRequestWrapper
    implements PortletRequest {

    /**
     * Creates a ServletRequest adaptor wrapping the given request object.
     * @throws java.lang.IllegalArgumentException
     *          if the request is null.
     */
    public PortletRequestWrapper(PortletRequest portletRequest) {
        super((javax.servlet.http.HttpServletRequest) portletRequest);

        if (portletRequest == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
    }

    // javax.portlet.PortletRequest implementation -------------------------------------------------
    public boolean isWindowStateAllowed(WindowState state) {
        return this.getPortletRequest().isWindowStateAllowed(state);
    }

    public boolean isPortletModeAllowed(PortletMode mode) {
        return this.getPortletRequest().isPortletModeAllowed(mode);
    }

    public PortletMode getPortletMode() {
        return this.getPortletRequest().getPortletMode();
    }

    public WindowState getWindowState() {
        return this.getPortletRequest().getWindowState();
    }

    public PortletPreferences getPreferences() {
        return this.getPortletRequest().getPreferences();
    }

    public PortletSession getPortletSession() {
        return this.getPortletRequest().getPortletSession();
    }

    public PortletSession getPortletSession(boolean create) {
        return this.getPortletRequest().getPortletSession(create);
    }

    public String getProperty(String name) {
        return this.getPortletRequest().getProperty(name);
    }

    public Enumeration getProperties(String name) {
        return this.getPortletRequest().getProperties(name);
    }

    public Enumeration getPropertyNames() {
        return this.getPortletRequest().getPropertyNames();
    }

    public PortalContext getPortalContext() {
        return this.getPortletRequest().getPortalContext();
    }

    public java.lang.String getAuthType() {
        return this.getPortletRequest().getAuthType();
    }

    public String getContextPath() {
        return this.getPortletRequest().getContextPath();
    }

    public java.lang.String getRemoteUser() {
        return this.getPortletRequest().getRemoteUser();
    }

    public java.security.Principal getUserPrincipal() {
        return this.getPortletRequest().getUserPrincipal();
    }

    public boolean isUserInRole(java.lang.String role) {
        return this.getPortletRequest().isUserInRole(role);
    }

    public Object getAttribute(String name) {
        return this.getPortletRequest().getAttribute(name);
    }

    public java.util.Enumeration getAttributeNames() {
        return this.getPortletRequest().getAttributeNames();
    }

    public String getParameter(String name) {
        return this.getPortletRequest().getParameter(name);
    }

    public java.util.Enumeration getParameterNames() {
        return this.getPortletRequest().getParameterNames();
    }

    public String[] getParameterValues(String name) {
        return this.getPortletRequest().getParameterValues(name);
    }

    public java.util.Map getParameterMap() {
        return this.getPortletRequest().getParameterMap();
    }

    public boolean isSecure() {
        return this.getPortletRequest().isSecure();
    }

    public void setAttribute(String name, Object o) {
        this.getPortletRequest().setAttribute(name, o);
    }

    public void removeAttribute(String name) {
        this.getPortletRequest().removeAttribute(name);
    }

    public String getRequestedSessionId() {
        return this.getPortletRequest().getRequestedSessionId();
    }

    public boolean isRequestedSessionIdValid() {
        return this.getPortletRequest().isRequestedSessionIdValid();
    }

    public String getResponseContentType() {
        return this.getPortletRequest().getResponseContentType();
    }

    public java.util.Enumeration getResponseContentTypes() {
        return this.getPortletRequest().getResponseContentTypes();
    }

    public java.util.Locale getLocale() {
        return this.getPortletRequest().getLocale();
    }

    public java.util.Enumeration getLocales() {
        return this.getPortletRequest().getLocales();
    }

    public String getScheme() {
        return this.getPortletRequest().getScheme();
    }

    public String getServerName() {
        return this.getPortletRequest().getServerName();
    }

    public int getServerPort() {
        return this.getPortletRequest().getServerPort();
    }

    // --------------------------------------------------------------------------------------------
    
    // additional methods -------------------------------------------------------------------------
    /**
     * Return the wrapped ServletRequest object.
     */
    public PortletRequest getPortletRequest() {
        return (PortletRequest) super.getRequest();
    }

    /**
     * Sets the request being wrapped.
     * @throws java.lang.IllegalArgumentException
     *          if the request is null.
     */
    public void setRequest(PortletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        setRequest((javax.servlet.http.HttpServletRequest) request);
    }
    // --------------------------------------------------------------------------------------------
}

