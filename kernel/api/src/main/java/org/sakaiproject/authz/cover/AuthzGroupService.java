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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.authz.cover;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * AuthzGroupService is a static Cover for the {@link org.sakaiproject.authz.api.AuthzGroupService AuthzGroupService}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class AuthzGroupService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.authz.api.AuthzGroupService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.authz.api.AuthzGroupService) ComponentManager
						.get(org.sakaiproject.authz.api.AuthzGroupService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.authz.api.AuthzGroupService) ComponentManager
					.get(org.sakaiproject.authz.api.AuthzGroupService.class);
		}
	}

	private static org.sakaiproject.authz.api.AuthzGroupService m_instance = null;

	public static java.lang.String APPLICATION_ID = org.sakaiproject.authz.api.AuthzGroupService.APPLICATION_ID;

	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.authz.api.AuthzGroupService.REFERENCE_ROOT;

	public static java.lang.String SECURE_ADD_AUTHZ_GROUP = org.sakaiproject.authz.api.AuthzGroupService.SECURE_ADD_AUTHZ_GROUP;

	public static java.lang.String SECURE_REMOVE_AUTHZ_GROUP = org.sakaiproject.authz.api.AuthzGroupService.SECURE_REMOVE_AUTHZ_GROUP;

	public static java.lang.String SECURE_UPDATE_AUTHZ_GROUP = org.sakaiproject.authz.api.AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP;

	public static java.lang.String SECURE_UPDATE_OWN_AUTHZ_GROUP = org.sakaiproject.authz.api.AuthzGroupService.SECURE_UPDATE_OWN_AUTHZ_GROUP;

	public static java.lang.String ANON_ROLE = org.sakaiproject.authz.api.AuthzGroupService.ANON_ROLE;

	public static java.lang.String AUTH_ROLE = org.sakaiproject.authz.api.AuthzGroupService.AUTH_ROLE;

	public static java.util.List getAuthzGroups(java.lang.String param0, org.sakaiproject.javax.PagingPosition param1)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.getAuthzGroups(param0, param1);
	}

	public static java.util.List getAuthzUserGroupIds(java.util.ArrayList param0, java.lang.String param1)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.getAuthzUserGroupIds(param0, param1);
	}

	public static int countAuthzGroups(java.lang.String param0)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return 0;

		return service.countAuthzGroups(param0);
	}

	public static Set getAuthzGroupIds(String providerId)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return new HashSet();
		
		return service.getAuthzGroupIds(providerId);
	}

	public Set getProviderIds(String authzGroupId)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return new HashSet();
		
		return service.getProviderIds(authzGroupId);
	}

	public static org.sakaiproject.authz.api.AuthzGroup getAuthzGroup(java.lang.String param0)
			throws org.sakaiproject.authz.api.GroupNotDefinedException
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.getAuthzGroup(param0);
	}

	public static boolean allowUpdate(java.lang.String param0)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return false;

		return service.allowUpdate(param0);
	}

	public static void save(org.sakaiproject.authz.api.AuthzGroup param0)
			throws org.sakaiproject.authz.api.GroupNotDefinedException, org.sakaiproject.authz.api.AuthzPermissionException
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return;

		service.save(param0);
	}

	public static boolean allowAdd(java.lang.String param0)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return false;

		return service.allowAdd(param0);
	}

	public static org.sakaiproject.authz.api.AuthzGroup addAuthzGroup(java.lang.String param0)
			throws org.sakaiproject.authz.api.GroupIdInvalidException, org.sakaiproject.authz.api.GroupAlreadyDefinedException,
			org.sakaiproject.authz.api.AuthzPermissionException
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.addAuthzGroup(param0);
	}

	public static org.sakaiproject.authz.api.AuthzGroup addAuthzGroup(java.lang.String param0,
			org.sakaiproject.authz.api.AuthzGroup param1, java.lang.String param2)
			throws org.sakaiproject.authz.api.GroupIdInvalidException, org.sakaiproject.authz.api.GroupAlreadyDefinedException,
			org.sakaiproject.authz.api.AuthzPermissionException
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.addAuthzGroup(param0, param1, param2);
	}

	public static org.sakaiproject.authz.api.AuthzGroup newAuthzGroup(java.lang.String param0,
			org.sakaiproject.authz.api.AuthzGroup param1, java.lang.String param2)
			throws org.sakaiproject.authz.api.GroupAlreadyDefinedException
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.newAuthzGroup(param0, param1, param2);
	}

	public static boolean allowRemove(java.lang.String param0)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return false;

		return service.allowRemove(param0);
	}

	public static void removeAuthzGroup(org.sakaiproject.authz.api.AuthzGroup param0)
			throws org.sakaiproject.authz.api.AuthzPermissionException
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return;

		service.removeAuthzGroup(param0);
	}

	public static void removeAuthzGroup(java.lang.String param0) throws org.sakaiproject.authz.api.AuthzPermissionException
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return;

		service.removeAuthzGroup(param0);
	}

	public static java.lang.String authzGroupReference(java.lang.String param0)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.authzGroupReference(param0);
	}

	public static void joinGroup(java.lang.String param0, java.lang.String param1)
			throws org.sakaiproject.authz.api.GroupNotDefinedException, org.sakaiproject.authz.api.AuthzPermissionException
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return;

		service.joinGroup(param0, param1);
	}

	public static void joinGroup(java.lang.String param0, java.lang.String param1, int param2)
	throws org.sakaiproject.authz.api.GroupNotDefinedException, org.sakaiproject.authz.api.AuthzPermissionException, org.sakaiproject.authz.api.GroupFullException
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return;

		service.joinGroup(param0, param1, param2);
	}

	
	public static void unjoinGroup(java.lang.String param0) throws org.sakaiproject.authz.api.GroupNotDefinedException,
			org.sakaiproject.authz.api.AuthzPermissionException
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return;

		service.unjoinGroup(param0);
	}

	public static boolean allowJoinGroup(java.lang.String param0)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return false;

		return service.allowJoinGroup(param0);
	}

	public static boolean allowUnjoinGroup(java.lang.String param0)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return false;

		return service.allowUnjoinGroup(param0);
	}

	public static java.util.Set getUsersIsAllowed(java.lang.String param0, java.util.Collection param1)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.getUsersIsAllowed(param0, param1);
	}

	public static java.util.Set<String[]> getUsersIsAllowedByGroup(java.lang.String param0, java.util.Collection<String> param1)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.getUsersIsAllowedByGroup(param0, param1);		
	}

	public static java.util.Map<String,Integer> getUserCountIsAllowed(java.lang.String param0, java.util.Collection<String> param1)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.getUserCountIsAllowed(param0, param1);
	}

	
	public static java.util.Set getAuthzGroupsIsAllowed(java.lang.String param0, java.lang.String param1,
			java.util.Collection param2)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.getAuthzGroupsIsAllowed(param0, param1, param2);
	}

	public static java.util.Set getAllowedFunctions(java.lang.String param0, java.util.Collection param1)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.getAllowedFunctions(param0, param1);
	}

	public static void refreshUser(java.lang.String param0)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return;

		service.refreshUser(param0);
	}

	public static boolean isAllowed(java.lang.String param0, java.lang.String param1, java.util.Collection param2)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return false;

		return service.isAllowed(param0, param1, param2);
	}

	public static boolean isAllowed(java.lang.String param0, java.lang.String param1, java.lang.String param2)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return false;

		return service.isAllowed(param0, param1, param2);
	}

	public static java.lang.String getUserRole(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.getUserRole(param0, param1);
	}

	public static java.util.Map<java.lang.String, java.lang.String> getUserRoles(java.lang.String param0, java.util.Collection<java.lang.String> param1)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.getUserRoles(param0, param1);
	}

	public static java.util.Map getUsersRole(java.util.Collection param0, java.lang.String param1)
	{
		org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
		if (service == null) return null;

		return service.getUsersRole(param0, param1);
	}

    public static Collection<String> getAuthzUsersInGroups(Set<String> groupIds)
    {
        org.sakaiproject.authz.api.AuthzGroupService service = getInstance();
        if (service == null) return null;

        return service.getAuthzUsersInGroups(groupIds);
    }

}
