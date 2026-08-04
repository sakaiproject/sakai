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
package org.apache.pluto.driver.tags;

import java.util.Iterator;
import java.util.Map;

import javax.portlet.WindowState;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.tags.el.ExpressionEvaluatorProxy;
import org.apache.pluto.driver.AttributeKeys;
import org.apache.pluto.driver.services.portal.PortletWindowConfig;
import org.apache.pluto.driver.url.PortalURL;
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.core.PortalServletRequest;
import org.apache.pluto.driver.core.PortalServletResponse;
import org.apache.pluto.driver.core.PortletWindowImpl;
import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;

/**
 * The portlet tag is used to render a portlet specified by the portlet ID.
 *
 * @see javax.portlet.Portlet#render(javax.portlet.RenderRequest,javax.portlet.RenderResponse)
 * @see org.apache.pluto.PortletContainer#doRender(PortletWindow, HttpServletRequest, HttpServletResponse)
 *
 */
public class PortletTag extends BodyTagSupport {

	/** Logger. */
    private static final Log LOG = LogFactory.getLog(PortletTag.class);

    /** Status constant for failed rendering. */
    public static final int FAILED = 0;

    /** Status constant for successful rendering. */
    public static final int SUCCESS = 1;


    // Private Member Variables ------------------------------------------------

    /** The portlet ID attribute passed into this tag. */
    private String portletId = null;

    /** The evaluated value of the portlet ID attribute. */
    private String evaluatedPortletId = null;

    /** The cached portal servlet response holding rendering result. */
    private PortalServletResponse response = null;

    /** The cached rendering status: SUCCESS or FAILED. */
    private int status;

    /** The cached Throwable instance when fail to render the portlet. */
    private Throwable throwable = null;


    // Tag Attribute Accessors -------------------------------------------------

    /**
     * Returns the portlet ID attribute.
     * @return the portlet ID attribute.
     */
    public String getPortletId() {
        return portletId;
    }

    /**
     * Sets the portlet ID attribute.
     * @param portletId  the portlet ID attribute.
     */
    public void setPortletId(String portletId) {
        this.portletId = portletId;
    }


    // BodyTagSupport Impl -----------------------------------------------------

    /**
     * Method invoked when the start tag is encountered.
     * @throws JspException  if an error occurs.
     */
    public int doStartTag() throws JspException {

    	// Evaluate portlet ID attribute.
    	evaluatePortletId();

    	// Retrieve the portlet window config for the evaluated portlet ID.
        ServletContext servletContext = pageContext.getServletContext();

        PortletWindowConfig windowConfig =
            PortletWindowConfig.fromId(evaluatedPortletId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Rendering Portlet Window: " + windowConfig);
        }

        // Retrieve the current portal URL.
        PortalRequestContext portalEnv = PortalRequestContext.getContext(
                (HttpServletRequest) pageContext.getRequest());
        PortalURL portalURL = portalEnv.getRequestedPortalURL();

        // Create the portlet window to render.
        PortletWindow window = new PortletWindowImpl(windowConfig, portalURL);

        // Check if someone else is maximized. If yes, don't show content.
        Map windowStates = portalURL.getWindowStates();
        for (Iterator it = windowStates.keySet().iterator(); it.hasNext(); ) {
            String windowId = (String) it.next();
            WindowState windowState = (WindowState) windowStates.get(windowId);
            if (WindowState.MAXIMIZED.equals(windowState)
            		&& !window.getId().getStringId().equals(windowId)) {
                return SKIP_BODY;
            }
        }

        // Create portal servlet request and response to wrap the original
        // HTTP servlet request and response.
        PortalServletRequest portalRequest = new PortalServletRequest(
        		(HttpServletRequest) pageContext.getRequest(), window);
        PortalServletResponse portalResponse = new PortalServletResponse(
                (HttpServletResponse) pageContext.getResponse());

        // Retrieve the portlet container from servlet context.
        PortletContainer container = (PortletContainer)
            	servletContext.getAttribute(AttributeKeys.PORTLET_CONTAINER);

        // Render the portlet and cache the response.
        try {
            container.doRender(window, portalRequest, portalResponse);
            response = portalResponse;
            status = SUCCESS;
        } catch (Throwable th) {
            status = FAILED;
            throwable = th;
        }

        // Continue to evaluate the tag body.
        return EVAL_BODY_INCLUDE;
    }


    // Package Methods ---------------------------------------------------------

    /**
     * Returns the rendering status.
     * @return the rendering status.
     */
    int getStatus() {
        return status;
    }

    /**
     * Returns the portal servlet response holding rendering result.
     * @return the portal servlet response holding rendering result.
     */
    PortalServletResponse getPortalServletResponse() {
        return response;
    }

    /**
     * Returns the error that has occurred when rendering portlet.
     * @return the error that has occurred when rendering portlet.
     */
    Throwable getThrowable() {
        return throwable;
    }

    /**
     * Returns the evaluated portlet ID.
     * @return the evaluated portlet ID.
     */
    String getEvaluatedPortletId() {
        return evaluatedPortletId;
    }



    // Private Methods ---------------------------------------------------------

    /**
     * Evaluates the portlet ID attribute passed into this tag. This method
     * evaluates the member variable <code>portletId</code> and saves the
     * evaluated result to <code>evaluatedPortletId</code>
     * @throws JspException  if an error occurs.
     */
    private void evaluatePortletId() throws JspException {
        ExpressionEvaluatorProxy proxy = ExpressionEvaluatorProxy.getProxy();
        Object obj = proxy.evaluate(portletId, pageContext);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Evaluated portletId to: " + obj);
        }
        evaluatedPortletId = (String) obj;
    }

}
