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
package org.apache.pluto.spi.optional;

import org.apache.pluto.PortletWindow;
import org.apache.pluto.Constants;

import javax.portlet.PortletRequest;


public class PortletInvocationEvent {

    public static int LOAD = Constants.METHOD_NOOP.intValue();

    public static int ACTION = Constants.METHOD_ACTION.intValue();

    public static int RENDER = Constants.METHOD_RENDER.intValue();

    public static int ADMIN = Constants.METHOD_ADMIN.intValue();

    private PortletRequest portletRequest;

    private PortletWindow portletWindow;

    private int invocationMethod;

    public PortletInvocationEvent(PortletRequest portletRequest, PortletWindow portletWindow, int invocationMethod) {
        this.portletRequest = portletRequest;
        this.portletWindow = portletWindow;
        this.invocationMethod = invocationMethod;
    }

    public PortletRequest getPortletRequest() {
        return portletRequest;
    }

    public int getInvocationMethod() {
        return invocationMethod;
    }

    public PortletWindow getPortletWindow() {
        return portletWindow;
    }

}
