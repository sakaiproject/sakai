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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.spi.optional.UserInfoService;

/**
 * UserInfo
 *
 */
public class DefaultUserInfoService implements UserInfoService {
    private Map userInfoMap = new HashMap();

    /**
     * @deprecated
     */
    public Map getUserInfo(PortletRequest request) throws PortletContainerException {
        if ( request.getRemoteUser() != null ) {
        	final Map info = (Map)this.userInfoMap.get(request.getRemoteUser());
            if ( info == null ) {
                return Collections.EMPTY_MAP;
            }
            
            return info;
        }

        return null;
    }

    public Map getUserInfo(PortletRequest request, PortletWindow window) throws PortletContainerException {
        return getUserInfo(request);
    }

    public void setUserInfo(String userName, Map userInfoMap) {
        this.userInfoMap.put(userName, userInfoMap);
    }

    public void removeUserInfo(String userName) {
        this.userInfoMap.remove(userName);
    }
    
}
