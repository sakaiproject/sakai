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

import org.apache.pluto.PortletWindow;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.spi.optional.PortletInfoService;


public class DefaultPortletInfoService implements PortletInfoService {

    public String getTitle(PortletWindow window) {
        if (window instanceof InternalPortletWindow) {
            return getPortletDefinition((InternalPortletWindow) window)
                .getPortletInfo().getTitle();
        }
        return null;
    }

    public String getShortTitle(PortletWindow window) {
        if (window instanceof InternalPortletWindow) {
            return getPortletDefinition((InternalPortletWindow) window)
                .getPortletInfo().getShortTitle();
        }
        return null;
    }

    public String getKeywords(PortletWindow window) {
        if (window instanceof InternalPortletWindow) {
            return getPortletDefinition((InternalPortletWindow) window)
                .getPortletInfo().getKeywords();
        }
        return null;
    }

    private PortletDD getPortletDefinition(InternalPortletWindow window) {
        return window.getPortletEntity().getPortletDefinition();
    }
}
