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

import java.util.Locale;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * PreferencesService is a static Cover for the {@link org.sakaiproject.user.api.PreferencesService PreferencesService}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class PreferencesService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.user.api.PreferencesService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.user.api.PreferencesService) ComponentManager
						.get(org.sakaiproject.user.api.PreferencesService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.user.api.PreferencesService) ComponentManager
					.get(org.sakaiproject.user.api.PreferencesService.class);
		}
	}

	private static org.sakaiproject.user.api.PreferencesService m_instance = null;

	public static java.lang.String APPLICATION_ID = org.sakaiproject.user.api.PreferencesService.APPLICATION_ID;

	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.user.api.PreferencesService.REFERENCE_ROOT;

	public static java.lang.String SECURE_ADD_PREFS = org.sakaiproject.user.api.PreferencesService.SECURE_ADD_PREFS;

	public static java.lang.String SECURE_EDIT_PREFS = org.sakaiproject.user.api.PreferencesService.SECURE_EDIT_PREFS;

	public static java.lang.String SECURE_REMOVE_PREFS = org.sakaiproject.user.api.PreferencesService.SECURE_REMOVE_PREFS;

	public static void commit(org.sakaiproject.user.api.PreferencesEdit param0)
	{
		org.sakaiproject.user.api.PreferencesService service = getInstance();
		if (service == null) return;

		service.commit(param0);
	}

	public static org.sakaiproject.user.api.Preferences getPreferences(java.lang.String param0)
	{
		org.sakaiproject.user.api.PreferencesService service = getInstance();
		if (service == null) return null;

		return service.getPreferences(param0);
	}

	public static boolean allowUpdate(java.lang.String param0)
	{
		org.sakaiproject.user.api.PreferencesService service = getInstance();
		if (service == null) return false;

		return service.allowUpdate(param0);
	}

	public static org.sakaiproject.user.api.PreferencesEdit add(java.lang.String param0)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUsedException
	{
		org.sakaiproject.user.api.PreferencesService service = getInstance();
		if (service == null) return null;

		return service.add(param0);
	}

	public static void remove(org.sakaiproject.user.api.PreferencesEdit param0)
	{
		org.sakaiproject.user.api.PreferencesService service = getInstance();
		if (service == null) return;

		service.remove(param0);
	}

	public static void cancel(org.sakaiproject.user.api.PreferencesEdit param0)
	{
		org.sakaiproject.user.api.PreferencesService service = getInstance();
		if (service == null) return;

		service.cancel(param0);
	}

	public static org.sakaiproject.user.api.PreferencesEdit edit(java.lang.String param0)
			throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.InUseException,
			org.sakaiproject.exception.IdUnusedException
	{
		org.sakaiproject.user.api.PreferencesService service = getInstance();
		if (service == null) return null;

		return service.edit(param0);
	}
	
	public static Locale getLocale(String userId) {
		org.sakaiproject.user.api.PreferencesService service = getInstance();
		if (service == null) return null;

		return service.getLocale(userId);
	}
}
