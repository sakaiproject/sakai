/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.mock.example;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

public class ExampleSakaiCode {
	
	// Collaborators, or dependencies, or services, or...
	UserDirectoryService uds;
	public void setUds(UserDirectoryService uds) {this.uds = uds;}
	
	ToolManager toolManager;
	public void setToolManager(ToolManager toolManager){this.toolManager = toolManager;}

	SiteService siteService;
	public void setSiteService(SiteService siteService) {this.siteService = siteService;}

	
	public String getStringStartingWithContext() {
		// Get our current context
		String context = toolManager.getCurrentPlacement().getContext();
		return context + " -- that's our context, all right!";
	}
	
	public String getStringStartingWithSiteTitle() {
		// Get a site
		String context = toolManager.getCurrentPlacement().getContext();
		Site site = null;
		try {
			site = siteService.getSite(context);
			return site.getTitle() + " -- that's our site title, all right!";
		} catch (IdUnusedException ide) {
			return null;
		}
	}
	
	public String getStringStartingWithCurrentUserDisplayName() {
		User user = uds.getCurrentUser();
		return user.getDisplayName() + " -- that's our current user's name, all right!"; 
	}
}
