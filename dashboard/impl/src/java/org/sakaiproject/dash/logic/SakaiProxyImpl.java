package org.sakaiproject.dash.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observer;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Implementation of our SakaiProxy API
 * 
 * Need to avoid circular references.  This class should not reference 
 * any classes/interfaces in org.sakaiproject.dash.*.
 * (is that true?) 
 * 
 */
public class SakaiProxyImpl implements SakaiProxy {

	private static final Logger logger = Logger.getLogger(SakaiProxyImpl.class);
    
	/************************************************************************
	 * SakaiProxy methods
	 ************************************************************************/
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#addLocalEventListener(java.util.Observer)
	 */
	public void addLocalEventListener(Observer observer) {
		this.eventTrackingService.addLocalObserver(observer);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#getConfigParam(java.lang.String, boolean)
	 */
	public boolean getConfigParam(String param, boolean dflt) {
		return serverConfigurationService.getBoolean(param, dflt);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#getConfigParam(java.lang.String, java.lang.String)
	 */
	public String getConfigParam(String param, String dflt) {
		return serverConfigurationService.getString(param, dflt);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#getCurrentSiteId()
	 */
	public String getCurrentSiteId(){
		return toolManager.getCurrentPlacement().getContext();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#getCurrentUserDisplayName()
	 */
	public String getCurrentUserDisplayName() {
	   return userDirectoryService.getCurrentUser().getDisplayName();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#getCurrentUserId()
	 */
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#getEntity(java.lang.String)
	 */
	public Entity getEntity(String entityReference) {
		return this.entityManager.newReference(entityReference).getEntity();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#getRealmId(java.lang.String, java.lang.String)
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
	 * @see org.sakaiproject.dash.logic.SakaiProxy#getSite(java.lang.String)
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
	 * @see org.sakaiproject.dash.logic.SakaiProxy#getSkinRepoProperty()
 	*/
	public String getSkinRepoProperty(){
		return serverConfigurationService.getString("skin.repo");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#getToolSkinCSS(java.lang.String)
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
	 * @see org.sakaiproject.dash.logic.SakaiProxy#getUser(java.lang.String)
	 */
	public User getUser(String sakaiId) {
		
		try {
			return this.userDirectoryService.getUser(sakaiId);
		} catch (UserNotDefinedException e) {
			logger.warn("Unable to retrieve user for sakaiId: " + sakaiId);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#getUsersWithReadAccess(java.lang.String)
 	*/
	public List<String> getUsersWithReadAccess(String entityReference, String accessPermission) {
		List<String> users = new ArrayList<String>();
		Reference ref = this.entityManager.newReference(entityReference);
		Collection<String> azGroups = ref.getEntityProducer().getEntityAuthzGroups(ref, null);
		
		Set<String> sakaiIds = this.authzGroupService.getUsersIsAllowed(accessPermission, azGroups );
		
		return new ArrayList<String>(sakaiIds);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#isSuperUser()
	 */
	public boolean isSuperUser() {
		return securityService.isSuperUser();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#isWorksite(java.lang.String)
	 */
	public boolean isWorksite(String siteId) {
		return this.siteService.isUserSite(siteId);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#postEvent(java.lang.String, java.lang.String, boolean)
	 */
	public void postEvent(String event,String reference,boolean modify) {
		eventTrackingService.post(eventTrackingService.newEvent(event,reference,modify));
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#pushSecurityAdvisor(org.sakaiproject.authz.api.SecurityAdvisor)
	 */
	public void pushSecurityAdvisor(SecurityAdvisor securityAdvisor) {
		this.securityService.pushAdvisor(securityAdvisor);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.SakaiProxy#popSecurityAdvisor(org.sakaiproject.authz.api.SecurityAdvisor)
	 */
	public void popSecurityAdvisor(SecurityAdvisor securityAdvisor) {
		this.securityService.popAdvisor(securityAdvisor);
		
	}
	
	/************************************************************************
	 * Spring-injected classes
	 ************************************************************************/
	
	@Getter @Setter
	private ToolManager toolManager;
	
	@Getter @Setter
	private SessionManager sessionManager;
	
	@Getter @Setter
	private UserDirectoryService userDirectoryService;
	
	@Getter @Setter
	private SecurityService securityService;
	
	@Getter @Setter
	private EventTrackingService eventTrackingService;
	
	@Getter @Setter
	private ServerConfigurationService serverConfigurationService;
	
	@Getter @Setter
	private SiteService siteService;
	
	@Getter @Setter
	private EntityManager entityManager;
	
	@Getter @Setter
	protected AuthzGroupService authzGroupService;

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
