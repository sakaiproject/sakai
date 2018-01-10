/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.dao.impl;

import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;

import org.quartz.JobExecutionException;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.delegatedaccess.jobs.DelegatedAccessSiteHierarchyJob;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;

@Slf4j
public class DelegatedAccessSampleDataLoader {
	private SiteService siteService;
	private DelegatedAccessSiteHierarchyJob delegatedAccessSiteHierarchyJob;
	private SecurityService securityService;
	private AuthzGroupService authzGroupService;
	private EventTrackingService eventTrackingService;
	private UsageSessionService usageSessionService;
	private SessionManager sessionManager;
	private UserDirectoryService userDirectoryService;

	private List<String> schools = Arrays.asList("MUSIC", "MEDICINE", "EDUCATION");
	private List<String> depts = Arrays.asList("DEPT1", "DEPT2", "DEPT3");
	private List<String> subjs = Arrays.asList("SUBJ1", "SUBJ2","SUBJ3");
	
	public void init(){
		log.info("init()");
		
		if(siteService == null || securityService == null || delegatedAccessSiteHierarchyJob == null){
			return;
		}
		// Cheating to become admin in order to add sites
		SecurityAdvisor yesMan = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				return SecurityAdvice.ALLOWED;
			}
		};
		
		try{
			loginToSakai();
			securityService.pushAdvisor(yesMan);
			AuthzGroup templateGroup = authzGroupService.getAuthzGroup("!site.template.course");
		
			DateTime date = new DateTime();
			String term = "Spring " + date.getYear();
			for(String school : schools){
				for(String dept : depts){
					for(String subject : subjs){
						for(int courseNum = 101; courseNum < 600; courseNum += 25){
							String siteid = "DAC-" + school + "-" + dept + "-" + subject + "-" + courseNum; 
							String title = siteid;
							String description = siteid;
							String shortdesc = siteid;

							Site siteEdit = null;
							try {
								siteEdit = siteService.addSite(siteid, "course");
								siteEdit.setTitle(title);
								siteEdit.setDescription(description);
								siteEdit.setShortDescription(shortdesc);
								siteEdit.setPublished(true);
								siteEdit.setType("course");
								
								//for some reason the course template may not work (prob missed some code somewhere)
								if(siteEdit.getTool("sakai.siteinfo") == null){
									//T&Q
									SitePage page = siteEdit.addPage();
									page.setTitle("Tests & Quizzes");
									page.addTool("sakai.samigo");
									
									//Assignments
									page = siteEdit.addPage();
									page.setTitle("Assignments");
									page.addTool("sakai.assignment.grades");
									
									//Forums
									page = siteEdit.addPage();
									page.setTitle("Forums");
									page.addTool("sakai.forums");
									
									//Messages
									page = siteEdit.addPage();
									page.setTitle("Messages");
									page.addTool("sakai.messages");

									//Syllabus
									page = siteEdit.addPage();
									page.setTitle("Syllabus");
									page.addTool("sakai.syllabus");
									
									//Announcements
									page = siteEdit.addPage();
									page.setTitle("Announcements");
									page.addTool("sakai.announcements");
									
									//Gradebook
									page = siteEdit.addPage();
									page.setTitle("Gradebook");
									page.addTool("sakai.gradebookng");
									
									//Schedule
									page = siteEdit.addPage();
									page.setTitle("Schedule");
									page.addTool("sakai.schedule");

									//Resources
									page = siteEdit.addPage();
									page.setTitle("Resources");
									page.addTool("sakai.resources");
									
									//Roster
									page = siteEdit.addPage();
									page.setTitle("Roster");
									page.addTool("sakai.site.roster2");
									
									//Lessons
									page = siteEdit.addPage();
									page.setTitle("Lessons");
									page.addTool("sakai.lessonbuildertool");

									//Site Info
									page = siteEdit.addPage();
									page.setTitle("Site Info");
									page.addTool("sakai.siteinfo");
									
								}
								
								ResourcePropertiesEdit propEdit = siteEdit.getPropertiesEdit();
								propEdit.addProperty("School", school);
								propEdit.addProperty("Department", dept);
								propEdit.addProperty("Subject", subject);
								
								propEdit.addProperty("term", term);
								propEdit.addProperty("term_eid", term);
								
								siteService.save(siteEdit);
								
								//Make sure roles exist:
								AuthzGroup group = authzGroupService.getAuthzGroup(siteEdit.getReference());
								group.removeMembers();
								group.removeRoles();
								for(Role role : templateGroup.getRoles()){
									group.addRole(role.getId(), role);
								}
								group.addMember("datest", group.getMaintainRole(), true, false);
								authzGroupService.save(group);

							} catch (IdInvalidException | PermissionException e) {
								log.warn(e.getMessage(), e);
							} catch (IdUsedException e) {
								log.debug("IdUsedException: " + e.getId(), e);
								return;
							} catch (Exception e) {
								log.warn(e.getMessage(), e);
								return;
							}
						}
					}
				}
			}

			//now that the sites have been added, lets run the hierarhcy job
			try {
				delegatedAccessSiteHierarchyJob.execute(null);
			} catch (JobExecutionException e) {
				log.warn(e.getMessage(), e);
			}
		} catch(Exception e){
			log.warn(e.getMessage(), e);
		}finally{
			securityService.popAdvisor(yesMan);
			logoutFromSakai();
		}
	}
	
	private void loginToSakai() {
		User user = null;
		try {
			user = userDirectoryService.getUserByEid("datest");
		} catch (UserNotDefinedException e) {
			//user doesn't exist, lets make it:
			try {
				//String id, String eid, String firstName, String lastName, String email, String pw, String type, ResourceProperties properties
				user = userDirectoryService.addUser("datest", "datest", "DA", "Test", "", "datest", "", null);
			} catch (UserIdInvalidException e1) {
				log.error(e1.getMessage(), e1);
			} catch (UserAlreadyDefinedException e1) {
				log.error(e1.getMessage(), e1);
			} catch (UserPermissionException e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		if(user != null){
			Session sakaiSession = sessionManager.getCurrentSession();
			sakaiSession.setUserId(user.getId());
			sakaiSession.setUserEid(user.getEid());

			// establish the user's session
			usageSessionService.startSession(user.getId(), "127.0.0.1", "DAtest");

			// update the user's externally provided realm definitions
			authzGroupService.refreshUser(user.getId());

			// post the login event
			eventTrackingService.post(eventTrackingService.newEvent(UsageSessionService.EVENT_LOGIN, null, true));
		}
	}

	private void logoutFromSakai() {
	    Session sakaiSession = sessionManager.getCurrentSession();
		sakaiSession.invalidate();

		// post the logout event
		eventTrackingService.post(eventTrackingService.newEvent(UsageSessionService.EVENT_LOGOUT, null, true));
	}
	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public DelegatedAccessSiteHierarchyJob getDelegatedAccessSiteHierarchyJob() {
		return delegatedAccessSiteHierarchyJob;
	}

	public void setDelegatedAccessSiteHierarchyJob(
			DelegatedAccessSiteHierarchyJob delegatedAccessSiteHierarchyJob) {
		this.delegatedAccessSiteHierarchyJob = delegatedAccessSiteHierarchyJob;
	}

	public SecurityService getSecurityService() {
		return securityService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public AuthzGroupService getAuthzGroupService() {
		return authzGroupService;
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public EventTrackingService getEventTrackingService() {
		return eventTrackingService;
	}

	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	public UsageSessionService getUsageSessionService() {
		return usageSessionService;
	}

	public void setUsageSessionService(UsageSessionService usageSessionService) {
		this.usageSessionService = usageSessionService;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public UserDirectoryService getUserDirectoryService() {
		return userDirectoryService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
}
