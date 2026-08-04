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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import org.apache.pluto.driver.url.PortalURL;
import org.apache.pluto.driver.url.PortalURLParser;
import org.apache.pluto.driver.AttributeKeys;
import org.apache.pluto.driver.config.DriverConfiguration;

/**
 * Defines the context of the currentl portal request.
 * Allows for the retrieval of the original request
 * and response throughout the lifetime of the request.
 *
 * Provides a consistent interface for parsing/creating
 * PortalURLs to the outside world.
 *
 */
public class PortalRequestContext {

    /**
     * The attribute key to bind the portal environment instance to servlet
     * request.
     */
    public final static String REQUEST_KEY =
            PortalRequestContext.class.getName();

    /** The servletContext of execution. **/
    private ServletContext servletContext;

    /** The incoming servlet request. */
    private HttpServletRequest request;

    /** The incoming servlet response. */
    private HttpServletResponse response;

    /** The requested portal URL. */
    private PortalURL requestedPortalURL;


    // Constructor -------------------------------------------------------------

    /**
     * Creates a PortalRequestContext instance.
     * @param request  the incoming servlet request.
     * @param response  the incoming servlet response.
     */
    public PortalRequestContext(ServletContext servletContext,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        this.servletContext = servletContext;
        this.request = request;
        this.response = response;

        // Bind the instance to servlet request for later use.
        request.setAttribute(REQUEST_KEY, this);
    }

    /**
     * Returns the portal environment from the servlet request. The portal
     * envirionment instance is saved in the request scope.
     * @param request  the servlet request.
     * @return the portal environment.
     */
    public static PortalRequestContext getContext(
            HttpServletRequest request) {
        return (PortalRequestContext) request.getAttribute(REQUEST_KEY);
    }

    /**
     * Returns the servlet request.
     * @return the servlet request.
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Returns the servlet response.
     * @return the servlet response.
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * Returns the requested portal URL.
     * @return the requested portal URL.
     */
    public PortalURL getRequestedPortalURL() {
        if(requestedPortalURL == null) {
            DriverConfiguration config = (DriverConfiguration)
                servletContext.getAttribute(AttributeKeys.DRIVER_CONFIG);
            PortalURLParser parser = config.getPortalUrlParser();
            requestedPortalURL = parser.parse(request);
        }
        return requestedPortalURL;
    }

    public PortalURL createPortalURL() {
        return (PortalURL)getRequestedPortalURL().clone();
    }
}
