package uk.ac.lancs.e_science.profile2.impl;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import org.apache.log4j.Logger;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;




public class SakaiProxyImpl implements SakaiProxy {

	private transient Logger log = Logger.getLogger(SakaiProxyImpl.class);
	
	public String getCurrentSiteId(){
		return toolManager.getCurrentPlacement().getContext();
	}
	
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}
	
	public String getUserEid(String userId){
		String eid = null;
		try {
			eid = userDirectoryService.getUser(userId).getEid();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get eid for id: " + userId);
		}
		return eid;
	}

	public String getUserDisplayName(String userId) {
	   String name = null;
		try {
			name = userDirectoryService.getUser(userId).getDisplayName();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get displayname for id: " + userId);
		}
		return name;
	}
	
	public String getUserEmail(String userId) {
	   String email = null;
		try {
			email = userDirectoryService.getUser(userId).getEmail();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get email for id: " + userId);
		}
		return email;
	}
	
	public boolean isUserAdmin(String userId) {
		return securityService.isSuperUser(userId);
	}
	
	//we use SakaiProxy to get us the SakaiPerson object for a given user
	//then we use that object to get attributes about the person.
	public SakaiPerson getSakaiPerson(String userId) {
		
		SakaiPerson sakaiPerson = null;
		
		try {
			sakaiPerson = sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getUserMutableType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sakaiPerson;
	}
	
	
	
	
	
	
	
	
	//setup Sakai API's
	private ToolManager toolManager;
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
	
	private SakaiPersonManager sakaiPersonManager;
	public void setSakaiPersonManager(SakaiPersonManager sakaiPersonManager) {
		this.sakaiPersonManager = sakaiPersonManager;
	}
	
	
	
		
	public void init() {
		log.debug("init()");
	}

	

	
}
