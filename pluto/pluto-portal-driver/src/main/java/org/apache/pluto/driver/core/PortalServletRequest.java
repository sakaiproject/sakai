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
package org.apache.pluto.driver.core;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.pluto.PortletWindow;
import org.apache.pluto.driver.url.PortalURLParameter;
import org.apache.pluto.driver.url.PortalURL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PortalServletRequest extends HttpServletRequestWrapper {

    private static final Log LOG = LogFactory.getLog(PortalServletRequest.class);

    private PortletWindow portletWindow = null;

    private PortalURL url;
    private Map portletParameters;

    public PortalServletRequest(HttpServletRequest request,
                                PortletWindow window) {
        super(request);
        this.portletWindow = window;

        url =
        PortalRequestContext.getContext(request).getRequestedPortalURL();
    }


// HttpServletRequestWrapper overlay

    public java.lang.String getContentType() {
        String contentType = super.getContentType();
        return contentType;
    }

// ServletRequestWrapper overlay

    public String getParameter(String name) {
        String[] values = (String[]) this.getParameterMap().get(name);
        if (values != null) {
            return values[0];
        }
        return null;
    }

    /**
     * Retreive the Parameters.
     * @return Map of parameters targeted to the window associated with this
     *         request.
     */
    public Map getParameterMap() {
        if (portletParameters == null) {
            initParameterMap();
        }
        return Collections.unmodifiableMap(portletParameters);
    }

    /**
     * Initialize parameters for this request.  We must be careful to make sure
     * that render parameters are only made available if they were targeted for
     * this specific window.
     */
    private void initParameterMap() {
        portletParameters = new HashMap();

        Iterator iterator = url.getParameters().iterator();
        while (iterator.hasNext()) {
            PortalURLParameter param = (PortalURLParameter) iterator.next();
            String name = param.getName();
            String[] values = param.getValues();
            if (param.getWindowId().equals(portletWindow.getId().getStringId())) {
                portletParameters.put(name, values);
            }
        }

        // Currently this request is only used for rendering.
        // Because of that, this will never be used, however, it's
        // being left in since this request's scope may expand at some
        // point!
        String id = url.getActionWindow();
        if (portletWindow.getId().getStringId().equals(id)) {
            Enumeration params = super.getParameterNames();
            while (params.hasMoreElements()) {
                String name = params.nextElement().toString();
                String[] values = super.getParameterValues(name);
                if (portletParameters.containsKey(name)) {
                    String[] temp = (String[]) portletParameters.get(name);
                    String[] all = new String[values.length + temp.length];
                    System.arraycopy(values, 0, all, 0, values.length);
                    System.arraycopy(temp, 0, all, values.length, temp.length);
                }
                portletParameters.put(name, values);
            }
        }

        if(LOG.isDebugEnabled()) {
            LOG.debug("Initialized request parameter map for window: '"
                +portletWindow.getId().getStringId()+"'");
        }
    }

    /**
     * Get an enumeration which contains each of the names for which parameters
     * exist.
     * @return an enumeration of all names bound as parameters.
     */
    public Enumeration getParameterNames() {
        return Collections.enumeration(getParameterMap().keySet());
    }

    /**
     * Get the values associated with the given parameter key.
     * @param name the Parameter name used to key the parameter.
     * @return a String[] of all values bound to the given name as a parameter.
     */
    public String[] getParameterValues(String name) {
        return (String[]) getParameterMap().get(name);
    }

}


