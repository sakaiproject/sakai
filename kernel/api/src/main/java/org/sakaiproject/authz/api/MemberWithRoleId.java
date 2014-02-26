/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.authz.api;

import java.io.Serializable;

/**
 * An class used in the membership cache in RealmRoleGroupCache
 * The cache key is userId String, 
 * and cache value is of MemberWithRoleId object, which contains information of roleId, provided, active 
 * KNL-1037
 *
 * Moved this to API (shared) so it can be serializable for distribution
 * KNL-1184
 * 
 * @author zqian
 */
public class MemberWithRoleId implements Serializable {

    static final long serialVersionUID = 1L;

    protected String roleId = null;
    protected boolean provided = false;
    protected boolean active = true;

    public MemberWithRoleId(String roleId, boolean active, boolean provided)
    {
        this.roleId = roleId;
        this.active = active;
        this.provided = provided;
    }

    public MemberWithRoleId(Member m)
    {
        this.roleId = m.getRole() != null? m.getRole().getId():null;
        this.active = m.isActive();
        this.provided = m.isProvided();
    }

    public String getRoleId()
    {
        return roleId;
    }

    /**
     * whether the member is provided or not
     */
    public boolean isProvided()
    {
        return provided;
    }

    /**
     * whether the member is active or not
     */
    public boolean isActive()
    {
        return active;
    }

    /**
     * set the active attribute
     */
    public void setActive(boolean active)
    {
        this.active = active;
    }

}
