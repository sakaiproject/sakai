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
package org.apache.pluto.driver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.driver.config.DriverConfiguration;
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.core.PortletWindowImpl;
import org.apache.pluto.driver.services.portal.PageConfig;
import org.apache.pluto.driver.services.portal.PortletWindowConfig;
import org.apache.pluto.driver.url.PortalURL;

import javax.portlet.PortletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The controller servlet used to drive the Portal Driver. All requests mapped
 * to this servlet will be processed as Portal Requests.
 *
 * @version 1.0
 * @since Sep 22, 2004
 */
public class PortalDriverServlet extends HttpServlet {

    /** Internal Logger. */
    private static final Log LOG = LogFactory.getLog(PortalDriverServlet.class);

    /** The Portal Driver sServlet Context */
    private ServletContext servletContext;

    public static final String DEFAULT_PAGE_URI =
    		"/WEB-INF/themes/pluto-default-theme.jsp";

    /** The portlet container to which we will forward all portlet requests. */
    protected PortletContainer container;

    /** Character encoding and content type of the response */
    private String contentType = "";


    // HttpServlet Impl --------------------------------------------------------

    public String getServletInfo() {
        return "Pluto Portal Driver Servlet";
    }

    /**
     * Initialize the Portal Driver. This method retrieves the portlet container
     * instance from the servlet context scope.
     * @see PortletContainer
     */
    public void init() {
        servletContext = getServletContext();
        container = (PortletContainer) servletContext.getAttribute(
        		AttributeKeys.PORTLET_CONTAINER);
        String charset = getServletConfig().getInitParameter("charset");
        if (charset != null && charset.length() > 0) {
            contentType = "text/html; charset=" + charset;
        }

    }


    /**
     * Handle all requests. All POST requests are passed to this method.
     * @param request  the incoming HttpServletRequest.
     * @param response  the incoming HttpServletResponse.
     * @throws ServletException  if an internal error occurs.
     * @throws IOException  if an error occurs writing to the response.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        if ( contentType != "" ) {
            response.setContentType( contentType );
        }

        PortalRequestContext portalRequestContext =
            new PortalRequestContext(getServletContext(), request, response);

        PortalURL portalURL = portalRequestContext.getRequestedPortalURL();
        String actionWindowId = portalURL.getActionWindow();

        PortletWindowConfig actionWindowConfig =
            actionWindowId == null
                ? null
                : PortletWindowConfig.fromId(actionWindowId);

        // Action window config will only exist if there is an action request.
        if (actionWindowConfig != null) {
            PortletWindowImpl portletWindow = new PortletWindowImpl(
            		actionWindowConfig, portalURL);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing action request for window: "
                		+ portletWindow.getId().getStringId());
            }
            try {
                container.doAction(portletWindow, request, response);
            } catch (PortletContainerException ex) {
                throw new ServletException(ex);
            } catch (PortletException ex) {
                throw new ServletException(ex);
            }
            if (LOG.isDebugEnabled()) {
            	LOG.debug("Action request processed.\n\n");
            }
        }

        // Otherwise (actionWindowConfig == null), handle the render request.
        else {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("Processing render request.");
        	}
            PageConfig pageConfig = getPageConfig(portalURL);
            if (pageConfig == null)
            {
                // TODO Shouldn't we throw an exception here?
                LOG.error("PageConfig for render path [" + portalURL.getRenderPath() + "] could not be found.");
            }

            request.setAttribute(AttributeKeys.CURRENT_PAGE, pageConfig);
            String uri = (pageConfig.getUri() != null)
            		? pageConfig.getUri() : DEFAULT_PAGE_URI;
            if (LOG.isDebugEnabled()) {
            	LOG.debug("Dispatching to: " + uri);
            }
            RequestDispatcher dispatcher = request.getRequestDispatcher(uri);
            dispatcher.forward(request, response);
            if (LOG.isDebugEnabled()) {
            	LOG.debug("Render request processed.\n\n");
            }
        }
    }

    /**
     * Pass all POST requests to {@link #doGet(HttpServletRequest, HttpServletResponse)}.
     * @param request  the incoming servlet request.
     * @param response  the incoming servlet response.
     * @throws ServletException if an exception occurs.
     * @throws IOException if an exception occurs writing to the response.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doGet(request, response);
    }


    // Private Methods ---------------------------------------------------------

    /**
     * Returns the config of the portal page to be rendered.
     * @param currentURL  the current portal URL.
     * @return the config of the portal page to be rendered.
     */
    private PageConfig getPageConfig(PortalURL currentURL) {
        String requestedPageId = currentURL.getRenderPath();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Rendering Portal: Requested Page: " + requestedPageId);
        }
        return getDriverConfiguration().getPageConfig(requestedPageId);
    }

    /**
     * Returns the portal driver configuration object.
     * @return the portal driver configuration object.
     */
    private DriverConfiguration getDriverConfiguration() {
        return (DriverConfiguration) getServletContext().getAttribute(
        		AttributeKeys.DRIVER_CONFIG);
    }

}

