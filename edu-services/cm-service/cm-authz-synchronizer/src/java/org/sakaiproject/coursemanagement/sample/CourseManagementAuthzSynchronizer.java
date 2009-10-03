/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.coursemanagement.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 *
 */
public class CourseManagementAuthzSynchronizer {
	private static final Log log = LogFactory.getLog(CourseManagementAuthzSynchronizer.class);
 
	private CourseManagementService courseManagementService;
	private AuthzGroupService authzGroupService;
	private SiteService siteService;
	private SessionManager sessionManager;
	
	private String courseSiteType;
	private String termEidPropertyName;
	
	private String termEid; 
	
	public void execute() {
		actAsAdmin();

		if (termEid == null) {
			// Default is to synchronize sites connected to all academics sessions
			// which are marked as current.
			List<AcademicSession> academicSessions = courseManagementService.getCurrentAcademicSessions();
			for (AcademicSession academicSession : academicSessions) {
				refreshSitesForAcademicSession(academicSession.getEid());
			}
		} else {
			refreshSitesForAcademicSession(termEid);
		}
	}
	
	protected void refreshSitesForAcademicSession(String academicSessionEid) {
		if (log.isInfoEnabled()) log.info("Synchronizing site groups for term=" + academicSessionEid);
		Map<String, String> propertyCriteria = new HashMap<String, String>();
		propertyCriteria.put(termEidPropertyName, academicSessionEid);
		List<Site> sites = siteService.getSites(SiteService.SelectionType.NON_USER, courseSiteType, null, propertyCriteria, SiteService.SortType.NONE, null);
		for (Site site : sites) {
			// Currently there's no exposed way to refresh provided groups for a site. Instead,
			// it occurs as a side-effect of calling "save" on the site's associated AuthzGroup.
			try {
				AuthzGroup authzGroup = authzGroupService.getAuthzGroup(siteService.siteReference(site.getId()));
				authzGroupService.save(authzGroup);
			} catch (GroupNotDefinedException e) {
				log.warn("AuthzGroup for site " + site.getId() + " not found", e);
				continue;
			} catch (AuthzPermissionException e) {
				log.warn("Unable to synchronize AuthzGroup for site " + site.getId(), e);
				continue;
			}
		}
	}

	/**
	 * Convenience routine to support the frequent testing need to switch authn/authz identities.
	 * TODD Find some central place for this frequently-needed helper logic. It can easily be made
	 * static.
	 */
	public void actAsAdmin() {
		String userId = "admin";
		Session session = sessionManager.getCurrentSession();
		session.setUserEid(userId);
		session.setUserId(userId);
		authzGroupService.refreshUser(userId);
	}

	public void setCourseManagementService(CourseManagementService courseManagementService) {
		this.courseManagementService = courseManagementService;
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	/**
	 * @param courseSiteType
	 *            site type to search for
	 */
	public void setCourseSiteType(String courseSiteType) {
		this.courseSiteType = courseSiteType;
	}
	
	/**
	 * @param termEidPropertyName
	 *            site property to match against an academic session ID; THIS IS
	 *            NOT CURRENTLY PART OF A DOCUMENTED SERVICE API
	 */
	public void setTermEidPropertyName(String termEidPropertyName) {
		this.termEidPropertyName = termEidPropertyName;
	}

	/**
	 * @param termEid
	 *            academic session to synchronize against; if left null, all
	 *            current academic sessions are checked
	 */
	public void setTermEid(String termEid) {
		this.termEid = termEid;
	}
}
