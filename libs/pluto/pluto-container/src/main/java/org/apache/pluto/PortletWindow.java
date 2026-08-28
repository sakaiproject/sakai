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
package org.apache.pluto;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

/**
 * Thin representation of the portlet window for which the container
 * request should be processed.  The PortletWindow is used internally
 * to map the request to the configured Portlet Application and Portlet.
 *
 * @see org.apache.pluto.internal.impl.InternalPortletWindowImpl
 * @see org.apache.pluto.descriptors.portlet.PortletDD
 *
 * @version 1.0
 * @since Sep 22, 2004
 */
public interface PortletWindow {

    /**
     * Retrieve this windows unique id which will be
     *  used to communicate back to the referencing portal.
     * @return unique id.
     */
    public PortletWindowID getId();

    /**
     * Retrieve the context path in which the Portlet resides.
     * @return context path
     */
    public String getContextPath();

    /**
     * Retrieve the name of the portlet as configured in the
     * <code>portlet.xml</code>.
     * @return the name of the portlet.
     */
    public String getPortletName();

    /**
     * Retrieve the current window state for this window.
     * @return the current window state.
     */
    public WindowState getWindowState();

    /**
     * Retrieve the current portlet mode for this window.
     * @return the current portlet mode.
     */
    public PortletMode getPortletMode();

}
