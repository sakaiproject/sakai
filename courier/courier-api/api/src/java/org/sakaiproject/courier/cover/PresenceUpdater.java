/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

package org.sakaiproject.courier.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
* <p>PresenceUpdater is a static Cover for the {@link org.sakaiproject.courier.api.PresenceUpdater PresenceUpdater};
* see that interface for usage details.</p>
*/
public class PresenceUpdater
{
	/** Service Bean ID for PresenceService, which implements PresenceUpdater to break cross-dependency. */
	public static final String PRESENCE_SERVICE = "org.sakaiproject.presence.api.PresenceService";

	private static org.sakaiproject.courier.api.PresenceUpdater m_instance = null;

	/**
	 * Access the component instance: special cover only method.
	 * @return the component instance.
	 */
	public static org.sakaiproject.courier.api.PresenceUpdater getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null) m_instance = (org.sakaiproject.courier.api.PresenceUpdater) ComponentManager.get(PRESENCE_SERVICE);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.courier.api.PresenceUpdater) ComponentManager.get(PRESENCE_SERVICE);
		}
	}

	public static void setPresence(String locationId)
	{
		org.sakaiproject.courier.api.PresenceUpdater service = getInstance();
		if (service == null)
			return;

		service.setPresence(locationId);
	}
}


