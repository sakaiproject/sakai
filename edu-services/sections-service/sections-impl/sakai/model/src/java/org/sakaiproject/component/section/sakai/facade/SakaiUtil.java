/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.component.section.sakai.facade;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.component.section.sakai.UserImpl;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

@Slf4j
public class SakaiUtil {
	/**
	 * Gets a User from Sakai's UserDirectory (legacy) service.
	 * 
	 * @param userUid The user uuid
	 * @return
	 */
	public static final User getUserFromSakai(String userUid) {
		final org.sakaiproject.user.api.User sakaiUser;
		try {
			sakaiUser = UserDirectoryService.getUser(userUid);
		} catch (UserNotDefinedException e) {
			log.warn("User not found: " + userUid);
			return null;
		}
		return convertUser(sakaiUser);
	}

	/**
	 * Converts a sakai user object into a user object suitable for use in the section
	 * manager tool and in section awareness.
	 * 
	 * @param sakaiUser The sakai user, as returned by Sakai's legacy SecurityService.
	 * 
	 * @return
	 */
	public static final User convertUser(final org.sakaiproject.user.api.User sakaiUser) {
		UserImpl user = new UserImpl(sakaiUser.getDisplayId(), sakaiUser.getDisplayName(),
				sakaiUser.getSortName(), sakaiUser.getId());
		return user;
	}
	
	/**
	 * @return The current sakai authz reference
	 */
	public static final String getSiteReference() {
		Placement placement = ToolManager.getCurrentPlacement();
		String context = placement.getContext();
		return SiteService.siteReference(context);
	}
}
