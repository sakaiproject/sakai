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
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.core.PortletWindowImpl;
import org.apache.pluto.driver.services.portal.PortletWindowConfig;
import org.apache.pluto.driver.url.PortalURL;

import javax.portlet.PortletException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The controller filter used to drive static portlet pages.
 *
 * @version 1.0
 * @since March 28, 2006
 */
public class PortalDriverFilter implements Filter {

    /**
     * Internal Logger.
     */
    private static final Log LOG = LogFactory.getLog(PortalDriverFilter.class);

    /**
     * The Portal Driver sServlet Context
     */
    private ServletContext servletContext;

    /**
     * The portlet container to which we
     * will forward all portlet requests.
     */
    protected PortletContainer container;


    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Initialize the Portal Driver.
     * This method retrieves the portlet container instance
     * from the servlet context scope.
     *
     * @see org.apache.pluto.PortletContainer
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();
        container = (PortletContainer) servletContext.getAttribute(
            AttributeKeys.PORTLET_CONTAINER);
    }


    /**
     * Release the container and the context.
     */
    public void destroy() {
        container = null;
        servletContext = null;
    }

    /**
     * Intercept requests in order to perform any actions.
     *
     * @param request  the incoming ServletRequest.
     * @param response the incoming ServletResponse.
     * @throws javax.servlet.ServletException if an internal error occurs.
     * @throws java.io.IOException            if an error occurs writing to the response.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest) request;
            // Since we must support a 2.3 environment, we can't use
            //  filter dispatchers.  B/C of this, we make sure we haven't
            //  allready processed this request. No infinite loops for us!!!!
            if (PortalRequestContext.getContext(req) == null) {
                PortalRequestContext ctx = doPortletPrepare(req, (HttpServletResponse) response);
                LOG.debug("Render Path: " + ctx.getRequestedPortalURL().getRenderPath());
                LOG.debug("Servlet Path: " + ctx.getRequestedPortalURL().getServletPath());
                PortalURL url = ctx.getRequestedPortalURL();
                if (url.getActionWindow() != null) {
                    return;
                }

            }

            String path = req.getServletPath();
            int idx = path.indexOf(".jsp");
            if (!path.endsWith(".jsp") && idx > 0) {
                String realPath = path.substring(0, idx + ".jsp".length());
                if (realPath.startsWith(req.getContextPath())) {
                    realPath = realPath.substring(req.getContextPath().length());
                }
                LOG.info("Forwarding to realPath: " + realPath);
                request.getRequestDispatcher(realPath).forward(request, response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Perform Portlet Preparation
     *
     * @param request
     * @param response
     * @throws java.io.IOException            if an io exception occurs
     * @throws javax.servlet.ServletException if a servlet exception occurs
     */
    public PortalRequestContext doPortletPrepare(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

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

        return portalRequestContext;
    }
}
