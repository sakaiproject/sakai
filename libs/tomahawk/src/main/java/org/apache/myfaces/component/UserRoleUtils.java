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

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import java.util.StringTokenizer;

/**
 * @author Manfred Geiler (latest modification by $Author: skitching $)
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (Thu, 03 Jul 2008) $
 */
public final class UserRoleUtils
{
    //private static final Log log = LogFactory.getLog(UserRoleUtils.class);

    /**
     * Constructor (private one)
     */
    private UserRoleUtils(){
        
    }
    
    /**
     * Gets the comma separated list of visibility user roles from the given component
     * and checks if current user is in one of these roles.
     * @param component a user role aware component
     * @return true if no user roles are defined for this component or
     *              user is in one of these roles, false otherwise
     */
    public static boolean isVisibleOnUserRole(UIComponent component)
    {
        String userRole;
        if (component instanceof UserRoleAware)
        {
            userRole = ((UserRoleAware)component).getVisibleOnUserRole();
        }
        else
        {
            userRole = (String)component.getAttributes().get(UserRoleAware.VISIBLE_ON_USER_ROLE_ATTR);
        }

        if (userRole == null)
        {
            // no restriction
            return true;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        StringTokenizer st = new StringTokenizer(userRole, ",");
        while (st.hasMoreTokens())
        {
            if (facesContext.getExternalContext().isUserInRole(st.nextToken().trim()))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Gets the comma separated list of enabling user roles from the given component
     * and checks if current user is in one of these roles.
     * @param component a user role aware component
     * @return true if no user roles are defined for this component or
     *              user is in one of these roles, false otherwise
     */
    public static boolean isEnabledOnUserRole(UIComponent component)
    {
        String userRole;
        if (component instanceof UserRoleAware)
        {
            userRole = ((UserRoleAware)component).getEnabledOnUserRole();
        }
        else
        {
            userRole = (String)component.getAttributes().get(UserRoleAware.ENABLED_ON_USER_ROLE_ATTR);
        }

        if (userRole == null)
        {
            // no restriction
            return true;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        StringTokenizer st = new StringTokenizer(userRole, ",");
        while (st.hasMoreTokens())
        {
            if (facesContext.getExternalContext().isUserInRole(st.nextToken().trim()))
            {
                return true;
            }
        }
        return false;
    }

}
