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
package org.apache.pluto.core;

import org.apache.pluto.spi.optional.PortalAdministrationService;
import org.apache.pluto.spi.optional.AdministrativeRequestListener;
import org.apache.pluto.spi.optional.PortletInvocationListener;

import java.util.List;
import java.util.ArrayList;

public class DefaultPortalAdministrationService implements PortalAdministrationService {

    private List administrativeRequestListeners =
        new ArrayList();

    private List portletInvocationListeners =
        new ArrayList();

    public List getAdministrativeRequestListeners() {
        return administrativeRequestListeners;
    }

    public void setAdministrativeRequestListeners(List administrativeRequestListeners) {
        this.administrativeRequestListeners = administrativeRequestListeners;
    }

    public void addAdministrativeRequestListener(AdministrativeRequestListener listener) {
        administrativeRequestListeners.add(listener);
    }

    public List getPortletInvocationListeners() {
        return portletInvocationListeners;
    }

    public void setPortletInvocationListeners(List portletInvocationListeners) {
        this.portletInvocationListeners = portletInvocationListeners;
    }

    public void addPortletInvocationListener(PortletInvocationListener listener) {
        portletInvocationListeners.add(listener);
    }
}
