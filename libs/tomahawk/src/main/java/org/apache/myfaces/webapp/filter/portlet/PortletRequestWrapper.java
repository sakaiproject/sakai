/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.webapp.filter.portlet;

import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;
import jakarta.servlet.ServletResponse;

/**
 * <p>
 * NOTE: This class should be used(instantiated) only by 
 * TomahawkFacesContextWrapper. By that reason, it could change
 * in the future.
 * </p>
 * 
 * @since 1.1.8
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 782179 $ $Date: 2009-06-05 20:35:54 -0500 (Fri, 05 Jun 2009) $
 */
public class PortletRequestWrapper implements PortletRequest
{

    public PortletRequestWrapper(PortletRequest request)
    {
        if(request == null)
        {
            throw new IllegalArgumentException("Request cannot be null");
        } else
        {
            this.request = request;
            return;
        }
    }
    public PortletRequest getRequest()
    {
        return request;
    }

    public void setRequest(PortletRequest response)
    {
        if(response == null)
        {
            throw new IllegalArgumentException("Response cannot be null");
        } else
        {
            this.request = response;
            return;
        }
    }
    
    private PortletRequest request;

    public Object getAttribute(String s)
    {
        return request.getAttribute(s);
    }

    public Enumeration getAttributeNames()
    {
        return request.getAttributeNames();
    }

    public String getAuthType()
    {
        return request.getAuthType();
    }

    public String getContextPath()
    {
        return request.getContextPath();
    }

    public Locale getLocale()
    {
        return request.getLocale();
    }

    public Enumeration getLocales()
    {
        return request.getLocales();
    }

    public String getParameter(String s)
    {
        return request.getParameter(s);
    }

    public Map getParameterMap()
    {
        return request.getParameterMap();
    }

    public Enumeration getParameterNames()
    {
        return request.getParameterNames();
    }

    public String[] getParameterValues(String s)
    {
        return request.getParameterValues(s);
    }

    public PortalContext getPortalContext()
    {
        return request.getPortalContext();
    }

    public PortletMode getPortletMode()
    {
        return request.getPortletMode();
    }

    public PortletSession getPortletSession()
    {
        return request.getPortletSession();
    }

    public PortletSession getPortletSession(boolean flag)
    {
        return request.getPortletSession(flag);
    }

    public PortletPreferences getPreferences()
    {
        return request.getPreferences();
    }

    public Enumeration getProperties(String s)
    {
        return request.getProperties(s);
    }

    public String getProperty(String s)
    {
        return request.getProperty(s);
    }

    public Enumeration getPropertyNames()
    {
        return request.getPropertyNames();
    }

    public String getRemoteUser()
    {
        return request.getRemoteUser();
    }

    public String getRequestedSessionId()
    {
        return request.getRequestedSessionId();
    }

    public String getResponseContentType()
    {
        return request.getResponseContentType();
    }

    public Enumeration getResponseContentTypes()
    {
        return request.getResponseContentTypes();
    }

    public String getScheme()
    {
        return request.getScheme();
    }

    public String getServerName()
    {
        return request.getServerName();
    }

    public int getServerPort()
    {
        return request.getServerPort();
    }

    public Principal getUserPrincipal()
    {
        return request.getUserPrincipal();
    }

    public WindowState getWindowState()
    {
        return request.getWindowState();
    }

    public boolean isPortletModeAllowed(PortletMode portletmode)
    {
        return request.isPortletModeAllowed(portletmode);
    }

    public boolean isRequestedSessionIdValid()
    {
        return request.isRequestedSessionIdValid();
    }

    public boolean isSecure()
    {
        return request.isSecure();
    }

    public boolean isUserInRole(String s)
    {
        return request.isUserInRole(s);
    }

    public boolean isWindowStateAllowed(WindowState windowstate)
    {
        return request.isWindowStateAllowed(windowstate);
    }

    public void removeAttribute(String s)
    {
        request.removeAttribute(s);
    }

    public void setAttribute(String s, Object obj)
    {
        request.setAttribute(s, obj);
    }
}
