/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.component;

/**
 * Behavioral interface.
 * Components that support user role checking should implement this interface
 * to optimize property access.
 *
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 659874 $ $Date: 2008-05-24 15:59:15 -0500 (Sat, 24 May 2008) $
 */
public interface UserRoleAware
{
    static final String ENABLED_ON_USER_ROLE_ATTR = "enabledOnUserRole";
    static final String VISIBLE_ON_USER_ROLE_ATTR = "visibleOnUserRole";

    /**
     * If user is in given role, this component will be rendered 
     * normally. If not, no hyperlink is rendered but all nested 
     * tags (=body) are rendered.
     * 
     * @JSFProperty
     * @return
     */
    String getEnabledOnUserRole();
    
    void setEnabledOnUserRole(String userRole);

    /**
     *  If user is in given role, this component will be rendered 
     *  normally. If not, nothing is rendered and the body of this 
     *  tag will be skipped.
     * 
     * @JSFProperty
     * @return
     */
    String getVisibleOnUserRole();
    
    void setVisibleOnUserRole(String userRole);
}