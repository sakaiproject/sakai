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

import org.apache.pluto.PortletContainer;
import org.apache.pluto.driver.services.portal.PageConfig;
import org.apache.pluto.driver.config.AdminConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * TCK Driver Servlet.
 *
 * @version 1.0
 * @since Dec 11, 2005
 */
public class TCKDriverServlet extends PortalDriverServlet {

	/** Logger. */
    private static final Log LOG = LogFactory.getLog(TCKDriverServlet.class);

    private int pageCounter = 0;

    public String getServletInfo() {
        return "Pluto TCK Driver Servlet";
    }

    public void init() {
        super.init();
        ServletContext servletContext = getServletContext();
        container = (PortletContainer) servletContext.getAttribute(
                AttributeKeys.PORTLET_CONTAINER);
    }

    /**
     * Overwrites <code>super.doGet(..)</code>. If <code>portletName</code>
     * (multiple occurrences) parameter is received, the driver is attempting
     * to create a new page. This page must be setup and then redirected to the
     * actual page. Otherwise, the driver calls <code>super.doGet(..)</code>
     * to continue as normal.
     * @param request  the incoming servlet request.
     * @param response  the incoming servlet response.
     * @throws IOException
     * @throws ServletException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        String[] portletNames = request.getParameterValues("portletName");
        if (portletNames != null && portletNames.length > 0) {
        	debugWithName("Initializing new TCK page...");
            doSetup(request, response);
        } else {
        	debugWithName("No portlet names specified. Continue as normal.");
        	super.doGet(request, response);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse response)
    throws IOException, ServletException {
        super.doGet(req, response);
    }


    // Private Methods ---------------------------------------------------------

    private void doSetup(HttpServletRequest request,
                         HttpServletResponse response)
    throws IOException, ServletException {
        String[] portletNames = request.getParameterValues("portletName");
        String pageName = request.getParameter("pageName");
        if (pageName != null) {
        	debugWithName("Retrieved page name from request: " + pageName);
        } else {
        	debugWithName("Creating page name...");
        	AdminConfiguration adminConfig = (AdminConfiguration)
            		getServletContext()
            		.getAttribute(AttributeKeys.DRIVER_ADMIN_CONFIG);
            if (adminConfig == null) {
                throw new ServletException("Invalid configuration: "
                		+ "an AdminConfiguration must be specified "
                		+ "to run the TCK.");
            }

            pageName = (new DecimalFormat("TCK00000")).format(pageCounter++);
            PageConfig pageConfig = new PageConfig();
            pageConfig.setName(pageName);
            pageConfig.setUri(DEFAULT_PAGE_URI);
            for (int i = 0; i < portletNames.length; i++) {
            	debugWithName("Processing portlet name: " + portletNames[i]);
                int index = portletNames[i].indexOf("/");
                String contextPath = "/" + portletNames[i].substring(0, index);
                String portletName = portletNames[i].substring(index + 1);
                pageConfig.addPortlet(contextPath, portletName);
                adminConfig.getPortletRegistryAdminService()
                		.addPortletApplication(contextPath);
            }

            adminConfig.getRenderConfigAdminService().addPage(pageConfig);
            debugWithName("Created TCK Page: " + pageName);
        }

        // The other possibility would be to redirect to the actual portal.
        //   I'm not sure which is better at this point.
        StringBuffer buffer = new StringBuffer();
        buffer.append(request.getRequestURL().toString());
        if (!request.getRequestURL().toString().endsWith("/")) {
        	buffer.append("/");
        }
        buffer.append(pageName);
        debugWithName("Sending redirect to: " + buffer.toString());
        response.sendRedirect(buffer.toString());
    }

    /**
     * Prints debug message with a <code>[Pluto TCK Driver]</code> prefix.
     * @param message  message to debug.
     */
    private void debugWithName(String message) {
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("[Pluto TCK Driver] " + message);
    	}
    }

}