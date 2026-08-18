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
package org.sakaiproject.pluto.core;

import javax.portlet.PortletContext;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.sakaiproject.pluto.PortletContainer;
import org.sakaiproject.pluto.internal.InternalActionRequest;
import org.sakaiproject.pluto.internal.InternalActionResponse;
import org.sakaiproject.pluto.internal.InternalPortletWindow;
import org.sakaiproject.pluto.internal.InternalRenderRequest;
import org.sakaiproject.pluto.internal.InternalRenderResponse;
import org.sakaiproject.pluto.internal.impl.ActionRequestImpl;
import org.sakaiproject.pluto.internal.impl.ActionResponseImpl;
import org.sakaiproject.pluto.internal.impl.PortletSessionImpl;
import org.sakaiproject.pluto.internal.impl.RenderRequestImpl;
import org.sakaiproject.pluto.internal.impl.RenderResponseImpl;
import org.sakaiproject.pluto.spi.optional.PortletEnvironmentService;

/**
 *
 */
public class DefaultPortletEnvironmentService implements PortletEnvironmentService {


    public InternalActionRequest createActionRequest(PortletContainer container,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     InternalPortletWindow internalPortletWindow) {
        return new ActionRequestImpl(container, internalPortletWindow, request);
    }

    public InternalActionResponse createActionResponse(PortletContainer container,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response,
                                                       InternalPortletWindow internalPortletWindow) {
        return new ActionResponseImpl(container, internalPortletWindow, request, response);
    }

    public InternalRenderRequest createRenderRequest(PortletContainer container,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     InternalPortletWindow internalPortletWindow) {
        return new RenderRequestImpl(container, internalPortletWindow, request);
    }

    public InternalRenderResponse createRenderResponse(PortletContainer container,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response,
                                                       InternalPortletWindow internalPortletWindow) {
        return new RenderResponseImpl(container, internalPortletWindow, request, response);
    }

    public PortletSession createPortletSession(PortletContainer container, 
                                               HttpServletRequest servletRequest,
                                               PortletContext portletContext,
                                               HttpSession httpSession,
                                               InternalPortletWindow internalPortletWindow) {

        return new PortletSessionImpl(portletContext, internalPortletWindow, httpSession);
    }
}
