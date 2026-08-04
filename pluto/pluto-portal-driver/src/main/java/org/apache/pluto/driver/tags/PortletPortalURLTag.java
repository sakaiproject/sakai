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

import java.io.IOException;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.pluto.driver.url.PortalURL;
import org.apache.pluto.driver.core.PortalRequestContext;

/**
 * The portlet URL tag is used to generate portal URL pointing to the current
 * portlet with specified portlet mode and window state.
 *
 * @version 1.0
 * @since Oct 4, 2004
 */
public class PortletPortalURLTag extends BodyTagSupport {

    // Private Member Variables ------------------------------------------------

    /** The window state to be encoded in the portal URL. */
    private String windowState;

    /** The portlet mode to be encoded in the portal URL. */
    private String portletMode;


    // Tag Attribute Accessors -------------------------------------------------

    public String getWindowState() {
        return windowState;
    }

    public void setWindowState(String windowState) {
        this.windowState = windowState;
    }

    public String getPortletMode() {
        return portletMode;
    }

    public void setPortletMode(String portletMode) {
        this.portletMode = portletMode;
    }


    // BodyTagSupport Impl -----------------------------------------------------

    /**
     * Creates the portal URL pointing to the current portlet with specified
     * portlet mode and window state, and print the URL to the page.
     * @see PortletTag
     */
    public int doStartTag() throws JspException {

        // Ensure that the portlet render tag resides within a portlet tag.
        PortletTag parentTag = (PortletTag) TagSupport.findAncestorWithClass(
                this, PortletTag.class);
        if (parentTag == null) {
            throw new JspException("Portlet window controls may only reside "
                    + "within a pluto:portlet tag.");
        }

        // Create portal URL.
        HttpServletRequest request = (HttpServletRequest)
                pageContext.getRequest();

        HttpServletResponse response = (HttpServletResponse)
                pageContext.getResponse();

        PortalRequestContext ctx = (PortalRequestContext)
            request.getAttribute(PortalRequestContext.REQUEST_KEY);

        PortalURL portalUrl =  ctx.createPortalURL();

        // Encode window state of the current portlet in the portal URL.
        String portletId = parentTag.getEvaluatedPortletId();
        if (windowState != null) {
            portalUrl.setWindowState(portletId, new WindowState(windowState));
        }

        // Encode portlet mode of the current portlet in the portal URL.
        if (portletMode != null) {
            portalUrl.setPortletMode(portletId, new PortletMode(portletMode));
        }

        // Print the portal URL as a string to the page.
        try {
            pageContext.getOut().print(response.encodeURL(portalUrl.toString()));
        } catch (IOException ex) {
            throw new JspException(ex);
        }

        // Skip the tag body.
        return SKIP_BODY;
    }


}

