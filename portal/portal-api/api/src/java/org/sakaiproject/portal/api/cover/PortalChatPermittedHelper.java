/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/portal/tags/portal-base-2.9.1/portal-api/api/src/java/org/sakaiproject/portal/api/cover/PortalService.java $
 * $Id: PortalService.java 110562 2012-07-19 23:00:20Z ottenhoff@longsight.com $
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

public class PortalChatPermittedHelper {

	private static org.sakaiproject.portal.api.PortalChatPermittedHelper m_instance = null;

	public static org.sakaiproject.portal.api.PortalChatPermittedHelper getInstance() {
		
		if (ComponentManager.CACHE_COMPONENTS) {
			if (m_instance == null) {
				m_instance = (org.sakaiproject.portal.api.PortalChatPermittedHelper) ComponentManager
						.get(org.sakaiproject.portal.api.PortalChatPermittedHelper.class);
			}
			return m_instance;
		}
		else {
			return (org.sakaiproject.portal.api.PortalChatPermittedHelper) ComponentManager
					.get(org.sakaiproject.portal.api.PortalChatPermittedHelper.class);
		}
	}
}
