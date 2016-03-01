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

package org.sakaiproject.user.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * UserDirectoryService is a static Cover for the {@link org.sakaiproject.user.api.UserDirectoryService UserDirectoryService}; see that interface for usage details.
 * </p>
 * 
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class UserDirectoryService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.user.api.UserDirectoryService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.user.api.UserDirectoryService) ComponentManager
						.get(org.sakaiproject.user.api.UserDirectoryService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.user.api.UserDirectoryService) ComponentManager
					.get(org.sakaiproject.user.api.UserDirectoryService.class);
		}
	}

	private static org.sakaiproject.user.api.UserDirectoryService m_instance = null;

	public static java.lang.String APPLICATION_ID = org.sakaiproject.user.api.UserDirectoryService.APPLICATION_ID;

	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.user.api.UserDirectoryService.REFERENCE_ROOT;

	public static java.lang.String SECURE_ADD_USER = org.sakaiproject.user.api.UserDirectoryService.SECURE_ADD_USER;

	public static java.lang.String SECURE_REMOVE_USER = org.sakaiproject.user.api.UserDirectoryService.SECURE_REMOVE_USER;

	public static java.lang.String SECURE_UPDATE_USER_OWN = org.sakaiproject.user.api.UserDirectoryService.SECURE_UPDATE_USER_OWN;

	public static java.lang.String SECURE_UPDATE_USER_ANY = org.sakaiproject.user.api.UserDirectoryService.SECURE_UPDATE_USER_ANY;

	public static java.lang.String ADMIN_ID = org.sakaiproject.user.api.UserDirectoryService.ADMIN_ID;

	public static java.lang.String ADMIN_EID = org.sakaiproject.user.api.UserDirectoryService.ADMIN_EID;

	public static org.sakaiproject.user.api.User getUser(java.lang.String param0)
			throws org.sakaiproject.user.api.UserNotDefinedException
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.getUser(param0);
	}

	public static org.sakaiproject.user.api.User getUserByEid(java.lang.String param0)
			throws org.sakaiproject.user.api.UserNotDefinedException
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.getUserByEid(param0);
	}

	public static org.sakaiproject.user.api.User getUserByAid(java.lang.String param0) throws org.sakaiproject.user.api.UserNotDefinedException
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.getUserByAid(param0);
	}

	public static java.util.List getUsers(java.util.Collection param0)
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.getUsers(param0);
	}

	public static java.util.List<org.sakaiproject.user.api.User> getUsersByEids(java.util.Collection<String> param0)
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.getUsersByEids(param0);
	}

	public static java.util.List getUsers()
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.getUsers();
	}

	public static java.util.List getUsers(int param0, int param1)
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.getUsers(param0, param1);
	}

	public static org.sakaiproject.user.api.User getCurrentUser()
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.getCurrentUser();
	}

	public static java.util.Collection findUsersByEmail(java.lang.String param0)
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.findUsersByEmail(param0);
	}

	public static boolean allowUpdateUser(java.lang.String param0)
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return false;

		return service.allowUpdateUser(param0);
	}

	public static boolean updateUserId(java.lang.String param0,java.lang.String param1)
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return false;

		return service.updateUserId(param0,param1);
	}

	public static org.sakaiproject.user.api.UserEdit editUser(java.lang.String param0)
			throws org.sakaiproject.user.api.UserNotDefinedException, org.sakaiproject.user.api.UserPermissionException,
			org.sakaiproject.user.api.UserLockedException
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.editUser(param0);
	}

	public static void commitEdit(org.sakaiproject.user.api.UserEdit param0)
			throws org.sakaiproject.user.api.UserAlreadyDefinedException
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return;

		service.commitEdit(param0);
	}

	public static void cancelEdit(org.sakaiproject.user.api.UserEdit param0)
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return;

		service.cancelEdit(param0);
	}

	public static org.sakaiproject.user.api.User getAnonymousUser()
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.getAnonymousUser();
	}

	public static int countUsers()
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return 0;

		return service.countUsers();
	}

	public static java.util.List searchUsers(java.lang.String param0, int param1, int param2)
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.searchUsers(param0, param1, param2);
	}

	public static int countSearchUsers(java.lang.String param0)
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return 0;

		return service.countSearchUsers(param0);
	}

	public static boolean allowAddUser()
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return false;

		return service.allowAddUser();
	}
	
	public static boolean checkDuplicatedEmail(org.sakaiproject.user.api.User param0)
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return false;

		return service.checkDuplicatedEmail(param0);
	}


	public static org.sakaiproject.user.api.UserEdit addUser(java.lang.String param0, java.lang.String param1)
			throws org.sakaiproject.user.api.UserIdInvalidException, org.sakaiproject.user.api.UserAlreadyDefinedException,
			org.sakaiproject.user.api.UserPermissionException
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.addUser(param0, param1);
	}

	public static org.sakaiproject.user.api.User addUser(java.lang.String param0, java.lang.String param1, java.lang.String param2,
			java.lang.String param3, java.lang.String param4, java.lang.String param5, java.lang.String param6,
			org.sakaiproject.entity.api.ResourceProperties param7) throws org.sakaiproject.user.api.UserIdInvalidException,
			org.sakaiproject.user.api.UserAlreadyDefinedException, org.sakaiproject.user.api.UserPermissionException
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.addUser(param0, param1, param2, param3, param4, param5, param6, param7);
	}

	public static org.sakaiproject.user.api.UserEdit mergeUser(org.w3c.dom.Element param0)
			throws org.sakaiproject.user.api.UserIdInvalidException, org.sakaiproject.user.api.UserAlreadyDefinedException,
			org.sakaiproject.user.api.UserPermissionException
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.mergeUser(param0);
	}

	public static boolean allowRemoveUser(java.lang.String param0)
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return false;

		return service.allowRemoveUser(param0);
	}

	public static void removeUser(org.sakaiproject.user.api.UserEdit param0)
			throws org.sakaiproject.user.api.UserPermissionException
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return;

		service.removeUser(param0);
	}

	public static org.sakaiproject.user.api.User authenticate(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.authenticate(param0, param1);
	}

	/**
	 * @deprecated Unused; will likely be removed from the interface
	 */
	public static void destroyAuthentication()
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return;

		service.destroyAuthentication();
	}

	public static java.lang.String userReference(java.lang.String param0)
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.userReference(param0);
	}

	public static java.lang.String getUserEid(java.lang.String param0) throws org.sakaiproject.user.api.UserNotDefinedException
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.getUserEid(param0);
	}

	public static java.lang.String getUserId(java.lang.String param0) throws org.sakaiproject.user.api.UserNotDefinedException
	{
		org.sakaiproject.user.api.UserDirectoryService service = getInstance();
		if (service == null) return null;

		return service.getUserId(param0);
	}
}
