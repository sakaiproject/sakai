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
package org.apache.pluto.driver.url;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import java.util.Collection;
import java.util.Map;

public interface PortalURL extends Cloneable {
    void setRenderPath(String renderPath);

    String getRenderPath();

    void addParameter(PortalURLParameter param);

    Collection getParameters();

    void setActionWindow(String actionWindow);

    String getActionWindow();

    Map getPortletModes();

    PortletMode getPortletMode(String windowId);

    void setPortletMode(String windowId, PortletMode portletMode);

    Map getWindowStates();

    WindowState getWindowState(String windowId);

    void setWindowState(String windowId, WindowState windowState);

    void clearParameters(String windowId);

    String toString();

    /**
     * @deprecated no longer used. will be removed in 1.1.x
     * @return
     */
    String getServerURI();

    String getServletPath();

    Object clone();
}
