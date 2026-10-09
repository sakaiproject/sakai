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
package org.apache.pluto.util.impl;

import org.apache.pluto.PortletWindowID;
import org.apache.pluto.util.NamespaceMapper;

/**
 * Default implementation of <code>NamespaceMapper</code> interface.
 */
public class NamespaceMapperImpl implements NamespaceMapper {

    public NamespaceMapperImpl() {
    	// Do nothing.
    }


    // NamespaceMapper Impl ----------------------------------------------------

    public String encode(PortletWindowID portletWindowId, String name) {
        StringBuffer buffer = new StringBuffer(50);
        buffer.append("Pluto_");
        buffer.append(portletWindowId.getStringId());
        buffer.append('_');
        buffer.append(name);
        return buffer.toString();
    }

    public String encode(PortletWindowID portletWindowId1,
                         PortletWindowID portletWindowId2,
                         String name) {
        StringBuffer buffer = new StringBuffer(50);
        buffer.append("Pluto_");
        buffer.append(portletWindowId1.getStringId());
        buffer.append('_');
        buffer.append(portletWindowId2.getStringId());
        buffer.append('_');
        buffer.append(name);
        return buffer.toString();
    }

    public String decode(PortletWindowID portletWindowId, String name) {
        if (!name.startsWith("Pluto_")) {
            return null;
        }
        StringBuffer buffer = new StringBuffer(50);
        buffer.append("Pluto_");
        buffer.append(portletWindowId.getStringId());
        buffer.append('_');
        if (!name.startsWith(buffer.toString())) {
            return null;
        }
        return name.substring(buffer.length());
    }

}
