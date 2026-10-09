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
package org.apache.myfaces.webapp.filter;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;


/**
 * This class is used to hold some utilities used by tomahawk in portlet
 * environments. The idea is that all calls to portlet api methods
 * should be done here, to avoid ClassNotFoundException error in
 * servlet environments.
 * 
 * The public methods should not use classes on portlet api, so the
 * other classes calling this class does not have dependencies to this api.
 * 
 * @since 1.1.8
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 954965 $ $Date: 2010-06-15 11:58:31 -0500 (Tue, 15 Jun 2010) $
 */
public class PortletUtils
{

    public static boolean isDisabledTomahawkFacesContextWrapper(Object contextOrConfig)
    {
        PortletContext portletContext = null;
        if (contextOrConfig instanceof PortletConfig)
        {
            portletContext = ((PortletConfig)contextOrConfig).getPortletContext();
        }
        else
        {
            portletContext = (PortletContext)contextOrConfig;
        }
        
        return getBooleanValue(portletContext.getInitParameter(
                TomahawkFacesContextFactory.DISABLE_TOMAHAWK_FACES_CONTEXT_WRAPPER),
                TomahawkFacesContextFactory.DISABLE_TOMAHAWK_FACES_CONTEXT_WRAPPER_DEFAULT);
    }
    
    private static boolean getBooleanValue(String initParameter, boolean defaultVal)
    {
        if(initParameter == null || initParameter.trim().length()==0)
            return defaultVal;

        return (initParameter.equalsIgnoreCase("on") || initParameter.equals("1") || initParameter.equalsIgnoreCase("true"));
    }
    
    public static String getContextInitParameter(Object context, String paramName)
    {
        PortletContext portletContext = (PortletContext) context;
        return portletContext.getInitParameter(paramName);
    }
    
    public static Object getAttribute(Object context, String key)
    {
        PortletContext portletContext = (PortletContext) context;
        return portletContext.getAttribute(key);
    }
    
    public static void setAttribute(Object context, String key, Object value)
    {
        PortletContext portletContext = (PortletContext) context;
        portletContext.setAttribute(key, value);
    }
}
