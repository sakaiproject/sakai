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

import javax.portlet.GenericPortlet;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequestDispatcher;
import java.io.IOException;


public abstract class GenericPlutoPortlet extends GenericPortlet {

    public abstract String getViewPage();
    public abstract String getEditPage();
    public abstract String getHelpPage(RenderRequest request);

    public void doView(RenderRequest request, RenderResponse response)
    throws PortletException, IOException {
        PortletContext context = getPortletContext();
        PortletRequestDispatcher requestDispatcher =
        		context.getRequestDispatcher(getViewPage());
        requestDispatcher.include(request, response);
    }

    protected void doEdit(RenderRequest request, RenderResponse response)
    throws PortletException, IOException {
        PortletContext context = getPortletContext();
        PortletRequestDispatcher requestDispatcher =
        		context.getRequestDispatcher(getEditPage());
        requestDispatcher.include(request, response);
    }

    protected void doHelp(RenderRequest request, RenderResponse response)
    throws PortletException, IOException {
    	PortletContext context = getPortletContext();
    	PortletRequestDispatcher requestDispatcher =
    			context.getRequestDispatcher(getHelpPage(request));
    	requestDispatcher.include(request, response);
    }
}
