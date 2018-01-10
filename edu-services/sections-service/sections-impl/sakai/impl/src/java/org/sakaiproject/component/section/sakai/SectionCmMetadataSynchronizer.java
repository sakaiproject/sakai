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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.section.sakai;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * Updates sakai's section metadata with the latest section information from the CM service.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">jholtzman@berkeley.edu</a>
 *
 */
@Slf4j
public class SectionCmMetadataSynchronizer implements Job {
	public static final int PAGE_SIZE = 100;
	
	protected SiteService siteService;
	protected CourseManagementService cmService;
	protected GroupProvider groupProvider;
	protected AuthzGroupService authzGroupService;
	public void setCmService(CourseManagementService cmService) {
		this.cmService = cmService;
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void init() {
		if(log.isInfoEnabled()) log.info("init()");
		// A group provider may not exist, so we can't use spring to inject it
		groupProvider = (GroupProvider)ComponentManager.get(GroupProvider.class);
	}
    
	public void updateSakaiSectionMetadata() {
		loginToSakai();
		
		int numCourseSites = siteService.countSites(SelectionType.NON_USER, "course", null, null);
		int numPages = (int)Math.ceil((double) numCourseSites / PAGE_SIZE);
		
		if(log.isInfoEnabled()) log.info("Synchronizing " + numCourseSites +
				" course sites in " + numPages + " chunks of " + PAGE_SIZE + " sites");

		for(int page = 0; page < numPages; page++) {
			int pageStart = (page*PAGE_SIZE) + 1;
			PagingPosition pager = new PagingPosition(pageStart, PAGE_SIZE);
			if(log.isDebugEnabled()) log.debug("Paging from " + pageStart + " with page size = " + PAGE_SIZE);
			List<Site> pagedSites = siteService.getSites(SelectionType.NON_USER,
				"course", null, null, SortType.NONE, pager);
			for(Iterator<Site> siteIter = pagedSites.iterator(); siteIter.hasNext();) {
				syncSite(siteIter.next());
			}
		}

		logoutFromSakai();
	}

	private void syncSite(Site site) {
		if(log.isDebugEnabled()) log.debug("synching site " + site.getId());

		// Ensure that the site is fully initialized.
		site.loadAll();
		
		Collection<Group> groups = site.getGroups();
		String complexProviderId = site.getProviderGroupId();
		String[] sectionEids = groupProvider.unpackId(complexProviderId);
		for(int i=0; i<sectionEids.length; i++) {
			// Get the section
			String sectionEid = sectionEids[i];
			Section section;
			try {
				section = cmService.getSection(sectionEid);
			} catch (IdNotFoundException ide) {
				log.error("Section " + sectionEid + " is not defined in CM");
				// remove the group?  remove the providerId from the group?
				continue;
			}
			
			// Get the group with a providerId equal to this sectionEid
			for(Iterator<Group> groupIter = groups.iterator(); groupIter.hasNext();) {
				Group group = groupIter.next();
				if(sectionEid.equals(group.getProviderGroupId())) {
					// Update the group
					if(log.isDebugEnabled()) log.debug("Updating sakai group " + group.getId() + " from section " + section.getEid());
					CourseSectionUtil.decorateGroupWithCmSection(group, section);
					break;
				}
			}
		}
		try {
			siteService.save(site);
		} catch (IdUnusedException | PermissionException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	protected void loginToSakai() {
	    Session sakaiSession = SessionManager.getCurrentSession();
		sakaiSession.setUserId("admin");
		sakaiSession.setUserEid("admin");

		// establish the user's session
		UsageSessionService.startSession("admin", "127.0.0.1", "CMSync");
		
		// update the user's externally provided realm definitions
		authzGroupService.refreshUser("admin");

		// post the login event
		EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGIN, null, true));
	}

	protected void logoutFromSakai() {
		// post the logout event
		EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGOUT, null, true));
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		if(log.isDebugEnabled()) log.debug("Updating sakai section metadata from CM via quartz job");
		updateSakaiSectionMetadata();
	}
}
