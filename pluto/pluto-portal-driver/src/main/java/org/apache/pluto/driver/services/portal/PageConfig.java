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
package org.apache.pluto.driver.services.portal;

import java.util.ArrayList;
import java.util.Collection;

/**
 */
public class PageConfig {

    private String name;
    private String uri;
    private Collection portletIds;
    private int orderNumber;

    public PageConfig() {
        this.portletIds = new ArrayList();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Collection getPortletIds() {
        return portletIds;
    }

    public void setPortletIds(Collection ids) {
        this.portletIds = ids;
    }

    public void addPortlet(String contextPath, String portletName) {
        synchronized(portletIds) {
            portletIds.add(PortletWindowConfig.createPortletId(contextPath, portletName, createPlacementId()));
        }
    }

    public void removePortlet(String portletId) {
        portletIds.remove(portletId);
    }

    void setOrderNumber(int number) {
        this.orderNumber = number;
    }

    int getOrderNumber() {
        return orderNumber;
    }

    private String createPlacementId() {
        return getName().hashCode() + "|"+portletIds.size();
    }

}
