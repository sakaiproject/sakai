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

package org.sakaiproject.email.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * DigestService is a static Cover for the {@link org.sakaiproject.email.api.DigestService DigestService}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class DigestService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.email.api.DigestService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.email.api.DigestService) ComponentManager
						.get(org.sakaiproject.email.api.DigestService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.email.api.DigestService) ComponentManager.get(org.sakaiproject.email.api.DigestService.class);
		}
	}

	private static org.sakaiproject.email.api.DigestService m_instance = null;

	public static java.lang.String SERVICE_NAME = org.sakaiproject.email.api.DigestService.SERVICE_NAME;

	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.email.api.DigestService.REFERENCE_ROOT;

	public static java.lang.String SECURE_ADD_DIGEST = org.sakaiproject.email.api.DigestService.SECURE_ADD_DIGEST;

	public static java.lang.String SECURE_EDIT_DIGEST = org.sakaiproject.email.api.DigestService.SECURE_EDIT_DIGEST;

	public static java.lang.String SECURE_REMOVE_DIGEST = org.sakaiproject.email.api.DigestService.SECURE_REMOVE_DIGEST;

	public static void commit(org.sakaiproject.email.api.DigestEdit param0)
	{
		org.sakaiproject.email.api.DigestService service = getInstance();
		if (service == null) return;

		service.commit(param0);
	}

	public static org.sakaiproject.email.api.Digest getDigest(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException
	{
		org.sakaiproject.email.api.DigestService service = getInstance();
		if (service == null) return null;

		return service.getDigest(param0);
	}

	public static java.util.List getDigests()
	{
		org.sakaiproject.email.api.DigestService service = getInstance();
		if (service == null) return null;

		return service.getDigests();
	}

	public static org.sakaiproject.email.api.DigestEdit add(java.lang.String param0)
			throws org.sakaiproject.exception.IdUsedException
	{
		org.sakaiproject.email.api.DigestService service = getInstance();
		if (service == null) return null;

		return service.add(param0);
	}

	public static void remove(org.sakaiproject.email.api.DigestEdit param0)
	{
		org.sakaiproject.email.api.DigestService service = getInstance();
		if (service == null) return;

		service.remove(param0);
	}

	public static void digest(java.lang.String param0, java.lang.String param1, java.lang.String param2)
	{
		org.sakaiproject.email.api.DigestService service = getInstance();
		if (service == null) return;

		service.digest(param0, param1, param2);
	}

	public static void cancel(org.sakaiproject.email.api.DigestEdit param0)
	{
		org.sakaiproject.email.api.DigestService service = getInstance();
		if (service == null) return;

		service.cancel(param0);
	}

	public static org.sakaiproject.email.api.DigestEdit edit(java.lang.String param0)
			throws org.sakaiproject.exception.InUseException
	{
		org.sakaiproject.email.api.DigestService service = getInstance();
		if (service == null) return null;

		return service.edit(param0);
	}
}
