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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.pluto.PortletWindow;
import org.apache.pluto.driver.AttributeKeys;

/**
 * The portlet title tag is used to print the dynamic portlet title to the page.
 * @version 1.0
 * @since Oct 4, 2004
 */
public class PortletTitleTag extends TagSupport {

	// TagSupport Impl ---------------------------------------------------------

	/**
     * Method invoked when the start tag is encountered. This method retrieves
     * the portlet title and print it to the page.
     *
	 * @see org.apache.pluto.services.PortalCallbackService#setTitle(HttpServletRequest, PortletWindow, String)
	 * @see org.apache.pluto.driver.services.container.PortalCallbackServiceImpl#setTitle(HttpServletRequest, PortletWindow, String)
	 *
	 * @throws JspException
	 */
    public int doStartTag() throws JspException {

    	// Ensure that the portlet title tag resides within a portlet tag.
        PortletTag parentTag = (PortletTag) TagSupport.findAncestorWithClass(
        		this, PortletTag.class);
        if (parentTag == null) {
            throw new JspException("Portlet title tag may only reside "
            		+ "within a pluto:portlet tag.");
        }

        // Print out the portlet title to page.
        try {
            pageContext.getOut().print(pageContext.getRequest().getAttribute(
            		AttributeKeys.PORTLET_TITLE));
        } catch (IOException ex) {
            throw new JspException(ex);
        }
        return SKIP_BODY;
    }
}

