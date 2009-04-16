/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.alias.cover;

import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * AliasService is a static Cover for the {@link org.sakaiproject.alias.api.AliasService AliasService}; see that interface for usage details.
 * </p>
 */
public class AliasService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.alias.api.AliasService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.alias.api.AliasService) ComponentManager
						.get(org.sakaiproject.alias.api.AliasService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.alias.api.AliasService) ComponentManager.get(org.sakaiproject.alias.api.AliasService.class);
		}
	}

	private static org.sakaiproject.alias.api.AliasService m_instance = null;

	public static java.lang.String APPLICATION_ID = org.sakaiproject.alias.api.AliasService.APPLICATION_ID;

	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.alias.api.AliasService.REFERENCE_ROOT;

	public static java.lang.String SECURE_ADD_ALIAS = org.sakaiproject.alias.api.AliasService.SECURE_ADD_ALIAS;

	public static java.lang.String SECURE_UPDATE_ALIAS = org.sakaiproject.alias.api.AliasService.SECURE_UPDATE_ALIAS;

	public static java.lang.String SECURE_REMOVE_ALIAS = org.sakaiproject.alias.api.AliasService.SECURE_REMOVE_ALIAS;

	public static boolean allowSetAlias(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return false;

		return service.allowSetAlias(param0, param1);
	}

	public static void setAlias(java.lang.String param0, java.lang.String param1)
			throws org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return;

		service.setAlias(param0, param1);
	}

	public static boolean allowRemoveAlias(java.lang.String param0)
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return false;

		return service.allowRemoveAlias(param0);
	}

	public static void removeAlias(java.lang.String param0) throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.InUseException
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return;

		service.removeAlias(param0);
	}

	public static boolean allowRemoveTargetAliases(java.lang.String param0)
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return false;

		return service.allowRemoveTargetAliases(param0);
	}

	public static void removeTargetAliases(java.lang.String param0) throws org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return;

		service.removeTargetAliases(param0);
	}

	public static java.util.List<Alias> getAliases(int param0, int param1)
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return null;

		return service.getAliases(param0, param1);
	}

	public static java.util.List<Alias> getAliases(java.lang.String param0)
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return null;

		return service.getAliases(param0);
	}

	public static java.util.List<Alias> getAliases(java.lang.String param0, int param1, int param2)
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return null;

		return service.getAliases(param0, param1, param2);
	}

	public static int countAliases()
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return 0;

		return service.countAliases();
	}

	public static java.util.List<Alias> searchAliases(java.lang.String param0, int param1, int param2)
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return null;

		return service.searchAliases(param0, param1, param2);
	}

	public static int countSearchAliases(java.lang.String param0)
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return 0;

		return service.countSearchAliases(param0);
	}

	public static java.lang.String aliasReference(java.lang.String param0)
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return null;

		return service.aliasReference(param0);
	}

	public static boolean allowAdd()
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return false;

		return service.allowAdd();
	}

	public static boolean allowEdit(java.lang.String param0)
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return false;

		return service.allowEdit(param0);
	}

	public static void commit(org.sakaiproject.alias.api.AliasEdit param0)
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return;

		service.commit(param0);
	}

	public static org.sakaiproject.alias.api.AliasEdit add(java.lang.String param0)
			throws org.sakaiproject.exception.IdInvalidException, org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return null;

		return service.add(param0);
	}

	public static void remove(org.sakaiproject.alias.api.AliasEdit param0) throws org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return;

		service.remove(param0);
	}

	public static void cancel(org.sakaiproject.alias.api.AliasEdit param0)
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return;

		service.cancel(param0);
	}

	public static java.lang.String getTarget(java.lang.String param0) throws org.sakaiproject.exception.IdUnusedException
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return null;

		return service.getTarget(param0);
	}

	public static org.sakaiproject.alias.api.AliasEdit edit(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.InUseException
	{
		org.sakaiproject.alias.api.AliasService service = getInstance();
		if (service == null) return null;

		return service.edit(param0);
	}
}
