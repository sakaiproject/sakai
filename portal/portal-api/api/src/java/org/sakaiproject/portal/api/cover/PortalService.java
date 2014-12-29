/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.api.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * A cover to the portal service. This only covers the getting of the
 * implementation and not all the methjods.
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
public class PortalService
{

	/** Possibly cached component instance. */
	private static org.sakaiproject.portal.api.PortalService m_instance = null;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.portal.api.PortalService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.portal.api.PortalService) ComponentManager
						.get(org.sakaiproject.portal.api.PortalService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.portal.api.PortalService) ComponentManager
					.get(org.sakaiproject.portal.api.PortalService.class);
		}
	}

}
