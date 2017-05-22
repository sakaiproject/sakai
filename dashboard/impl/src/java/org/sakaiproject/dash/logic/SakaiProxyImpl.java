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
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 


package org.sakaiproject.dash.logic;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.dash.app.SakaiProxy;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.Validator;

/**
 * Implementation of our SakaiProxy API
 * 
 * Need to avoid circular references.  This class should not reference 
 * any classes/interfaces in org.sakaiproject.dash.*.
 * (is that true?) 
 * 
 */
public class SakaiProxyImpl implements SakaiProxy {

	private static final Logger logger = LoggerFactory.getLogger(SakaiProxyImpl.class);
	
	private static final String SCHEDULE_TOOL_ID = "sakai.schedule";
    
	/************************************************************************
	 * SakaiProxy methods
	 ************************************************************************/
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#addLocalEventListener(java.util.Observer)
	 */
	public void addLocalEventListener(Observer observer) {
		this.eventTrackingService.addLocalObserver(observer);
	}
	
	public List<ContentResource> getAllContentResources(String contentCollectionId) {
		
		return this.contentHostingService.getAllResources(contentCollectionId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getAuthorizedUsers(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Collection<String> getAuthorizedUsers(String permission, String entityReference) {
		List<User> users = null;
		if(entityReference != null && permission != null) {
			users = this.securityService.unlockUsers(permission, entityReference);
		} 
		Set<String> userIds = new TreeSet<String>();
		if(users != null) {
			for(User user : users) {
				userIds.add(user.getId());
			}
		}
		return userIds;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getConfigParam(java.lang.String, boolean)
	 */
	public boolean getConfigParam(String param, boolean dflt) {
		return serverConfigurationService.getBoolean(param, dflt);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getConfigParam(java.lang.String, java.lang.String)
	 */
	public String getConfigParam(String param, String dflt) {
		return serverConfigurationService.getString(param, dflt);
	}
	
	public String getContentTypeImageUrl(String contenttype) {
		return this.contentTypeImageService.getContentTypeImage(contenttype);
	}

	public String getCurrentSessionId() {
		String sessionId = null;
		Session session = this.sessionManager.getCurrentSession();
		if(session != null) {
			sessionId = session.getId();
		}
		return sessionId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getCurrentSiteId()
	 */
	public String getCurrentSiteId(){
		return toolManager.getCurrentPlacement().getContext();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getCurrentUserDisplayName()
	 */
	public String getCurrentUserDisplayName() {
	   return userDirectoryService.getCurrentUser().getDisplayName();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getCurrentUserId()
	 */
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getEntity(java.lang.String)
	 */
	public Entity getEntity(String entityReference) {
		return this.entityManager.newReference(entityReference).getEntity();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getRealmId(java.lang.String, java.lang.String)
	 */
	public Collection<String> getRealmId(String entityReference, String contextId) {
		String realmId = null;
		Collection<String> authzGroups =  null;
		if(entityReference != null && ! entityReference.trim().equals("")) {
			Reference ref = this.entityManager.newReference(entityReference);
			authzGroups = this.authzGroupService.getEntityAuthzGroups(ref , null);
		} 
		
		if((authzGroups == null || authzGroups.isEmpty()) && contextId != null) {
			String siteReference = siteService.siteReference(contextId);
			Reference ref = this.entityManager.newReference(siteReference);
			authzGroups = this.authzGroupService.getEntityAuthzGroups(ref , null);
		}
		
		return authzGroups;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getSite(java.lang.String)
	 */
	public Site getSite(String siteId) {
		Site site = null;
		try {
			site = this.siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			logger.warn("Unable to get site for siteId: " + siteId, e);
		}
		return site;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#isSitePublished(java.lang.String)
	 */
	public boolean isSitePublished(String siteId) {
		Site site = getSite(siteId);
		return site != null? site.isPublished(): false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getSkinRepoProperty()
 	*/
	public String getSkinRepoProperty(){
		return serverConfigurationService.getString("skin.repo");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getTargetForMimetype(java.lang.String)
	 */
	public String getTargetForMimetype(String mimetype) {
		return Validator.getResourceTarget(mimetype);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getToolSkinCSS(java.lang.String)
 	*/
	public String getToolSkinCSS(String skinRepo){
		
		String skin = siteService.findTool(sessionManager.getCurrentToolSession().getPlacementId()).getSkin();			
		
		if(skin == null) {
			skin = serverConfigurationService.getString("skin.default");
		}
		
		return skinRepo + "/" + skin + "/tool.css";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getUser(java.lang.String)
	 */
	public User getUser(String sakaiId) {
		
		try {
			return this.userDirectoryService.getUser(sakaiId);
		} catch (UserNotDefinedException e) {
			logger.warn("Unable to retrieve user for sakaiId: " + sakaiId);
		}
		return null;
	}

	public boolean isDropboxResource(String resourceId) {
		return contentHostingService.isInDropbox(resourceId);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#isSuperUser()
	 */
	public boolean isSuperUser() {
		return securityService.isSuperUser();
	}
	
	/**
	 * 
	 */
	public boolean isUserPermitted(String sakaiUserId, String accessPermission,
			String entityReference) {
		
		return this.securityService.unlock(sakaiUserId, accessPermission, entityReference);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#isWorksite(java.lang.String)
	 */
	public boolean isWorksite(String siteId) {
		return this.siteService.isUserSite(siteId);
	}
	
	/**
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#isAttachmentResource(java.lang.String)
	 */
	public boolean isAttachmentResource(String resourceId) {
		return this.contentHostingService.isAttachmentResource(resourceId);
	}
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#postEvent(java.lang.String, java.lang.String, boolean)
	 */
	public void postEvent(String event,String reference,boolean modify) {
		eventTrackingService.post(eventTrackingService.newEvent(event,reference,modify));
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#pushSecurityAdvisor(org.sakaiproject.authz.api.SecurityAdvisor)
	 */
	public void pushSecurityAdvisor(SecurityAdvisor securityAdvisor) {
		this.securityService.pushAdvisor(securityAdvisor);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#popSecurityAdvisor(org.sakaiproject.authz.api.SecurityAdvisor)
	 */
	public void popSecurityAdvisor(SecurityAdvisor securityAdvisor) {
		this.securityService.popAdvisor(securityAdvisor);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#startAdminSession()
	 */
	public void startAdminSession() {
		logger.debug("Creating session: DashboardEventProcessor");
		Session session = this.sessionManager.startSession("DashboardEventProcessor");
		session.setUserId("admin");
		session.setUserEid("admin");
		this.sessionManager.setCurrentSession(session);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#clearThreadLocalCache()
	 */
	public void clearThreadLocalCache() {
		this.threadLocalManager.clear();
	}

	/**
	 * get the deep link url of schedule event
	 * @param eventRef
	 * @return
	 */
	public String getScheduleEventUrl(String eventRef) {
		
		Reference ref = entityManager.newReference(eventRef);
		
		Site site = this.getSite(ref.getContext());
		if (site != null)
		{
			StringBuilder url = new StringBuilder();
			ToolConfiguration tc = site.getToolForCommonId(SCHEDULE_TOOL_ID);
			if(tc != null) {
				// found schedule tool configuration
				url.append(this.serverConfigurationService.getPortalUrl());
				url.append("/directtool/");
				url.append(tc.getId());
				url.append("?eventReference=");
				url.append(eventRef);
				url.append("&panel=Main&sakai_action=doDescription&sakai.state.reset=true");		
				return url.toString();
			}
		}
		
		// no site or no schedule tool found
		return null;
	}
	
	public boolean isEventProcessingThreadDisabled()
	{
		return serverConfigurationService.getBoolean(CONFIG_DISABLE_DASHBOARD_EVENTPROCESSING, false);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String getSiteReference(String siteId)
	{
		return siteService.siteReference(siteId);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.SakaiProxy#getServerId()
	 */
	public String getServerId() {
		return this.serverConfigurationService.getServerId();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getServerUrl()
	{
		return serverConfigurationService.getServerUrl();
	}
	
	public void registerFunction(String functionName) {
		this.functionManager.registerFunction(functionName);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<User> unlockUsers(String lock, String reference)
	{
		return securityService.unlockUsers(lock, reference);
	}
	
	public boolean isOfDashboardRelatedPermissions(String function)
	{
		boolean rv = false;
		
		ArrayList<String> permissionPrefixes = new ArrayList<String>(Arrays.asList(PERMIT_PREFIX));
		if (permissionPrefixes != null)
		{
			for (String prefix : permissionPrefixes)
			{
				if (function.startsWith(prefix))
				{
					// returns true if the function begins with know prefix
					rv = true;
				}
			}
		}
		return rv;
	}

	/************************************************************************
	 * Spring-injected classes
	 ************************************************************************/
	
	//@Getter @Setter
	private ToolManager toolManager;
	
	//@Getter @Setter
	private SessionManager sessionManager;
	
	//@Getter @Setter
	private UserDirectoryService userDirectoryService;
	
	//@Getter @Setter
	private SecurityService securityService;
	
	//@Getter @Setter
	private EventTrackingService eventTrackingService;
	
	//@Getter @Setter
	private ServerConfigurationService serverConfigurationService;
	
	//@Getter @Setter
	private SiteService siteService;
	
	//@Getter @Setter
	private EntityManager entityManager;
	
	//@Getter @Setter
	protected AuthzGroupService authzGroupService;
	
	//@Getter @Setter
	protected ContentHostingService contentHostingService;

	//@Getter @Setter
	protected AssignmentService assignmentService;

	//@Getter @Setter
	protected PublishedAssessmentServiceAPI publishedAssessmentServiceAPI;
	
	protected ContentTypeImageService contentTypeImageService;
	
	protected ThreadLocalManager threadLocalManager;
	
	protected FunctionManager functionManager;
	
	/**
	 * @param toolManager the toolManager to set
	 */
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	/**
	 * @param sessionManager the sessionManager to set
	 */
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	/**
	 * @param userDirectoryService the userDirectoryService to set
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	/**
	 * @param securityService the securityService to set
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	/**
	 * @param eventTrackingService the eventTrackingService to set
	 */
	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	/**
	 * @param serverConfigurationService the serverConfigurationService to set
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	/**
	 * @param siteService the siteService to set
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	/**
	 * @param entityManager the entityManager to set
	 */
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * @param authzGroupService the authzGroupService to set
	 */
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}
	
	/**
	 * @param contentHostingService the contentHostingService to set
	 */
	public void setContentHostingService( ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
	
	/**
	 * @param assignmentService the AssignmentService to set
	 */
	public void setAssignmentService( AssignmentService assignmentService) {
		this.assignmentService = assignmentService;
	}

	/**
	 * @param setPublishedAssessmentServiceAPI the PublishedAssessmentServiceAPI to set
	 */
	public void setPublishedAssessmentServiceAPI( PublishedAssessmentServiceAPI publishedAssessmentServiceAPI) {
	    this.publishedAssessmentServiceAPI = publishedAssessmentServiceAPI;
	}

	/**
	 * @param contentTypeImageService the contentTypeImageService to set
	 */
	public void setContentTypeImageService(ContentTypeImageService contentTypeImageService) {
		this.contentTypeImageService = contentTypeImageService;
	}

	/**
	 * @param threadLocalManager
	 */
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		this.threadLocalManager = threadLocalManager;
	}
	
	/**
	 * @param functionManager the functionManager to set
	 */
	public void setFunctionManager(FunctionManager functionManager) {
		this.functionManager = functionManager;
	}

	/************************************************************************
	 * init() and destroy()
	 ************************************************************************/

	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
		logger.info("init");
	}

}
