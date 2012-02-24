/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright 2008 Sakai Foundation
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

package org.sakaiproject.coursemanagement.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiDependencyInjectionTests;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class ConfigurationTestBase extends SakaiDependencyInjectionTests {
	static final Log log = LogFactory.getLog(ConfigurationTestBase.class);
	
	protected AuthzGroupService authzGroupService;
	protected SiteService siteService;
	protected CourseManagementAdministration courseManagementAdmin;
	private UserDirectoryService userDirectoryService;
	private SessionManager sessionManager;

	// Neither SiteService nor AuthzGroupService provides an API to add or remove
	// authorization group provider EIDs. As a result, client application code
	// needs to call the correct GroupProvider service's "packID" method directly.
	// That in turn is the only reason we can't rely on autowiring-by-type to
	// handle all of our dependencies.
	protected GroupProvider groupProvider;

	protected void onSetUp() throws Exception {
		super.onSetUp();
		
		// Any deployment might include multiple provider implementations, and so
		// autowiring by type is not safe when client code needs to call providers.
		groupProvider = (GroupProvider)applicationContext.getBean(GroupProvider.class.getName());
		
		actAsUserEid("admin");
	}
	
	protected void addUser(String userEid) throws Exception {
		userDirectoryService.addUser(userEid, userEid);
	}

	/**
	 * Convenience routine to support the frequent testing need to switch authn/authz identities.
	 * TODD Find some central place for this frequently-needed helper logic. It can easily be made
	 * static.
	 *
	 * @param userEid
	 */
	public void actAsUserEid(String userEid) {
		if (log.isDebugEnabled()) log.debug("actAsUserEid=" + userEid);
		String userId;
		try {
			userId = userDirectoryService.getUserId(userEid);
		} catch (UserNotDefinedException e) {
			log.error("Could not act as user EID=" + userEid, e);
			return;
		}
		Session session = sessionManager.getCurrentSession();
		session.setUserEid(userEid);
		session.setUserId(userId);
		authzGroupService.refreshUser(userId);
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setCourseManagementAdmin(CourseManagementAdministration courseManagementAdmin) {
		this.courseManagementAdmin = courseManagementAdmin;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

}
