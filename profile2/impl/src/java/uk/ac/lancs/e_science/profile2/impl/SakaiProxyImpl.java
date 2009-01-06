package uk.ac.lancs.e_science.profile2.impl;


import java.io.File;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;

import uk.ac.lancs.e_science.profile2.api.SakaiProxy;




public class SakaiProxyImpl implements SakaiProxy {

	private transient Logger log = Logger.getLogger(SakaiProxyImpl.class);
    
	
	private final int MAX_PROFILE_IMAGE_SIZE = 2; //default if not specified in sakai.properties as profile.picture.max (megs)
	
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
			log.warn("Cannot get eid for id: " + userId + ":" + e.getClass() + ":" + e.getMessage());
		}
		return eid;
	}

	public String getUserDisplayName(String userId) {
	   String name = null;
		try {
			name = userDirectoryService.getUser(userId).getDisplayName();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get displayname for id: " + userId + ":" + e.getClass() + ":" + e.getMessage());
		}
		return name;
	}
	
	public String getUserEmail(String userId) {
	   String email = null;
		try {
			email = userDirectoryService.getUser(userId).getEmail();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get email for id: " + userId + ":" + e.getClass() + ":" + e.getMessage());
		}
		return email;
	}
	
	public boolean isUserAdmin(String userId) {
		return securityService.isSuperUser(userId);
	}
	
	//we use SakaiProxy to get us the SakaiPerson object for a given user
	//then we use the SakaiPerson object to get attributes about the person.
	public SakaiPerson getSakaiPerson(String userId) {
		
		SakaiPerson sakaiPerson = null;
		
		try {
			sakaiPerson = sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getUserMutableType());
		} catch (Exception e) {
			log.error("Couldn't get SakaiPerson for: " + userId + ":" + e.getClass() + ":" + e.getMessage());
		}
		return sakaiPerson;
	}
	
	//deprecated as its not persistable. use createSakaiPerson instead
	public SakaiPerson getSakaiPersonPrototype() {
		
		SakaiPerson sakaiPerson = null;
		
		try {
			sakaiPerson = sakaiPersonManager.getPrototype();
		} catch (Exception e) {
			log.error("Couldn't get SakaiPerson prototype:" + e.getClass() + ":" + e.getMessage());
		}
		return sakaiPerson;
	}
	
	
	public SakaiPerson createSakaiPerson(String userId) {
		
		SakaiPerson sakaiPerson = null;
		
		try {
			sakaiPerson = sakaiPersonManager.create(userId, sakaiPersonManager.getUserMutableType());
		} catch (Exception e) {
			log.error("Couldn't create SakaiPerson:" + e.getClass() + ":" + e.getMessage());
		}
		return sakaiPerson;
	}


	
	
	public boolean updateSakaiPerson(SakaiPerson sakaiPerson) {
		//the save is void, so unless it throws an exception, its ok (?)
		//I'd prefer a return value from sakaiPersonManager. this wraps it.
		try {
			sakaiPersonManager.save(sakaiPerson);
			return true;
		} catch (Exception e) {
			log.error("Couldn't update SakaiPerson: " + e.getClass() + ":" + e.getMessage());
		}
		return false;
	}
	
	
	
	private String getSakaiConfigurationParameterAsString(String parameter, String defaultValue) {
		return(ServerConfigurationService.getString(parameter, defaultValue));
	}
	
	private int getSakaiConfigurationParameterAsInt(String parameter, int defaultValue) {
		return ServerConfigurationService.getInt(parameter, defaultValue);
	}
	
	private boolean getSakaiConfigurationParameterAsBoolean(String parameter, boolean defaultValue) {
		return ServerConfigurationService.getBoolean(parameter, defaultValue);
	}

	
	public int getMaxProfilePictureSize() {
		return getSakaiConfigurationParameterAsInt("profile.picture.max", MAX_PROFILE_IMAGE_SIZE);
	}
	
	public LinkedHashMap<String, String> getSiteListForUser(int limitSites) {
		LinkedHashMap sites = new LinkedHashMap();
		//we need a good method to get a site for a specific user. screw the session
		sites.put("site1", "test site 1");
		sites.put("site2", "test site 2");
		
		return sites;
	}

	public String cleanString(String input) {
		//this could do something with the error messages in the StringBuilder...SS
		return(FormattedText.processFormattedText(input, new StringBuilder()));
	}

	
	public void registerSecurityAdvisor(SecurityAdvisor securityAdvisor){
		securityService.pushAdvisor(securityAdvisor);
	}
	
	private void enableSecurityAdvisor() {
        registerSecurityAdvisor(new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                return SecurityAdvice.ALLOWED;
            }
        });
    }

	
	
	//wrapper method to get the CHS resourceID for the main profile image for the user
	private String getResourceIdForProfileImage(String userId, int type) {
		
		
		String resourceId = "";
		
		String fullResourceId = "/profilepics/" + userId + "/" + resourceId;

		/*
		 * put constants in the Image API and use these inplace of the numbers below
		 * public static final int MAIN_PROFILE_IMAGE = 1;
		 * public static final int SMALL_PROFILE_IMAGE = 2;
		 * public staticfinal int TINY_PROFILE_IMAGE = 3;
		 * 
		 * is use Image.MAIN_PROFILE_IMAGE
		 * 
		 */
		
		
		
		
		switch(type) {
			//full image
			case 1:
			{
				//lookup in db to get the id
				
				
				break;
			}
			//small image
			case 2:
			{
				
				break;
			}
			//tiny image
			case 3:
			{
				
				break;
			}
			default:
				log.warn("SakaiProxy.getResourceIdForProfileImage(): Invalid image type supplied: " + type);
		}
		
		return fullResourceId;
		
	}
	
	
	
	// we need a few separate methods to get different profile pics/sizes
	public byte[] getResource(String fullResourceId) {
		byte[] data = null;
		
		try {
			
			//this needs to come from a sakai property perhaps?
			//String fullResourceId = "/profilepics/" + contextId + "/" + resourceId;
			
			ContentResource resource = contentHostingService.getResource(fullResourceId);
			data = resource.getContent();
		}
		catch(Exception e){
			log.error("SakaiProxy.fileExists() failed. " + e.getClass() + ": " + e.getMessage());
		}
		
		return data;
	}
	
	
	

	public String saveFile(String title, String mimeType, byte[] data) {
		// TODO Auto-generated method stub
		return null;
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
	
	private ContentHostingService contentHostingService;
	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
		
		
	public void init() {
		log.debug("init()");
	}

	

	
}
