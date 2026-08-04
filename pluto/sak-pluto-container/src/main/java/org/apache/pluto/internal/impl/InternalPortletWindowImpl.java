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
package org.apache.pluto.internal.impl;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.ServletContext;

import org.apache.pluto.PortletWindow;
import org.apache.pluto.PortletWindowID;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.internal.PortletEntity;

/**
 * Implementation of <code>InternalPortletWindow</code> interface.
 *
 */
public class InternalPortletWindowImpl implements InternalPortletWindow {

    /** The underlying portlet window instance. */
    private final PortletWindow portletWindow;

    /** The servlet context of the portlet. */
    private final ServletContext servletContext;

    /** The portlet entity associated with the portlet window. */
    private PortletEntity entity;


    // Constructor -------------------------------------------------------------

    /**
     * Constructs an internal portlet window that wraps a portlet window.
     * An internal portlet window instance is created everytime when the portlet
     * container's <code>doRender()</code> or <code>doAction()</code> method is
     * invoked.
     *
     * @param context  the servlet context from which this window is
     *        being invoked.
     * @param portletWindow  the underlying portlet window instance.
     */
    public InternalPortletWindowImpl(ServletContext context,
                             PortletWindow portletWindow) {
        this.servletContext = context;
        this.portletWindow = portletWindow;
    }


    // PortletWindow Impl ------------------------------------------------------

    public String getContextPath() {
        return portletWindow.getContextPath();
    }

    public String getPortletName() {
        return portletWindow.getPortletName();
    }

    public WindowState getWindowState() {
        return portletWindow.getWindowState();
    }

    public PortletMode getPortletMode() {
        return portletWindow.getPortletMode();
    }

    public PortletWindowID getId() {
        return portletWindow.getId();
    }


    // InternalPortletWindow Impl ----------------------------------------------

    public ServletContext getServletContext() {
        return servletContext;
    }

    public PortletEntity getPortletEntity() {
        if (entity == null) {
            entity = new PortletEntityImpl(servletContext, getPortletName());
        }
        return entity;
    }

    public PortletWindow getOriginalPortletWindow() {
        return portletWindow;
    }
}
