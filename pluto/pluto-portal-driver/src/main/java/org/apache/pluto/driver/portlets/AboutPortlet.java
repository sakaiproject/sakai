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
package org.apache.pluto.driver.portlets;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * The pluto portal driver about portlet.
 * @since 2006-02-09
 */
public class AboutPortlet extends GenericPortlet {

	private static final String VIEW_PAGE = "/WEB-INF/fragments/about/view.jsp";
	private static final String EDIT_PAGE = "/WEB-INF/fragments/about/edit.jsp";
	private static final String HELP_PAGE = "/WEB-INF/fragments/about/help.jsp";

	// GenericPortlet Impl -----------------------------------------------------

    public void doView(RenderRequest request, RenderResponse response)
    throws PortletException, IOException {
        PortletContext context = getPortletContext();
        PortletRequestDispatcher requestDispatcher =
        		context.getRequestDispatcher(VIEW_PAGE);
        requestDispatcher.include(request, response);
    }

    protected void doEdit(RenderRequest request, RenderResponse response)
    throws PortletException, IOException {
        PortletContext context = getPortletContext();
        PortletRequestDispatcher requestDispatcher =
        		context.getRequestDispatcher(EDIT_PAGE);
        requestDispatcher.include(request, response);
    }

    protected void doHelp(RenderRequest request, RenderResponse response)
    throws PortletException, IOException {
    	PortletContext context = getPortletContext();
    	PortletRequestDispatcher requestDispatcher =
    			context.getRequestDispatcher(HELP_PAGE);
    	requestDispatcher.include(request, response);
    }


}
