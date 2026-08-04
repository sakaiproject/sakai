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
package org.apache.pluto.driver.services.container;

import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.portlet.PortletSecurityException;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.PortletWindow;
import org.apache.pluto.driver.url.PortalURLParameter;
import org.apache.pluto.driver.url.PortalURL;
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.spi.PortletURLProvider;

/**
 *
 */
public class PortletURLProviderImpl implements PortletURLProvider {

    private PortalURL url;
    private String window;

    public PortletURLProviderImpl(HttpServletRequest request,
                                  PortletWindow internalPortletWindow) {
        PortalRequestContext ctx = (PortalRequestContext)
            request.getAttribute(PortalRequestContext.REQUEST_KEY);
        url = ctx.createPortalURL();

        this.window = internalPortletWindow.getId().getStringId();
    }

    public void setPortletMode(PortletMode mode) {
        url.setPortletMode(window, mode);
    }

    public void setWindowState(WindowState state) {
        url.setWindowState(window, state);
    }

    public void setAction(boolean action) {
        if (action) {
            url.setActionWindow(window);
        } else {
            url.setActionWindow(null);
        }
    }

    public void setSecure() throws PortletSecurityException {
        throw new PortletSecurityException("No Supported");
    }

    public boolean isSecureSupported() {
        return false;
    }

    public void clearParameters() {
        url.clearParameters(window);
    }

    public void setParameters(Map parameters) {
        Iterator it = parameters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            PortalURLParameter param = new PortalURLParameter(
            		window,
            		(String) entry.getKey(),
            		(String[]) entry.getValue());
            url.addParameter(param);
        }
    }

    public String toString() {
        return url.toString();
    }

}
