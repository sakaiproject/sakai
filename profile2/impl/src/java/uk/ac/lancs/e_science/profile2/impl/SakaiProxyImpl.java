package uk.ac.lancs.e_science.profile2.impl;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;

/**
 * This is the Implementation of the helper API used by the Profile2 tool only. 
 * 
 * DO NOT USE THIS YOURSELF
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class SakaiProxyImpl implements SakaiProxy {

	private transient final Logger log = Logger.getLogger(SakaiProxyImpl.class);
    	
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
			log.warn("Cannot get eid for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return eid;
	}

	public String getUserDisplayName(String userId) {
	   String name = null;
		try {
			name = userDirectoryService.getUser(userId).getDisplayName();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get displayname for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return name;
	}
	
	public String getUserEmail(String userId) {
	   String email = null;
		try {
			email = userDirectoryService.getUser(userId).getEmail();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get email for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
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
			log.error("SakaiProxy.getSakaiPerson(): Couldn't get SakaiPerson for: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return sakaiPerson;
	}
	
	//this is not persistable so should only be used for temporary views. use createSakaiPerson if need persistable object for saving a profile.
	public SakaiPerson getSakaiPersonPrototype() {
		
		SakaiPerson sakaiPerson = null;
		
		try {
			sakaiPerson = sakaiPersonManager.getPrototype();
		} catch (Exception e) {
			log.error("SakaiProxy.getSakaiPersonPrototype(): Couldn't get SakaiPerson prototype: " + e.getClass() + " : " + e.getMessage());
		}
		return sakaiPerson;
	}
	
	
	public SakaiPerson createSakaiPerson(String userId) {
		
		SakaiPerson sakaiPerson = null;
		
		try {
			sakaiPerson = sakaiPersonManager.create(userId, sakaiPersonManager.getUserMutableType());
		} catch (Exception e) {
			log.error("SakaiProxy.createSakaiPerson(): Couldn't create SakaiPerson: " + e.getClass() + " : " + e.getMessage());
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
			log.error("SakaiProxy.updateSakaiPerson(): Couldn't update SakaiPerson: " + e.getClass() + " : " + e.getMessage());
		}
		return false;
	}
	
	
	
	public String getSakaiConfigurationParameterAsString(String parameter, String defaultValue) {
		return(ServerConfigurationService.getString(parameter, defaultValue));
	}
	
	public int getSakaiConfigurationParameterAsInt(String parameter, int defaultValue) {
		return ServerConfigurationService.getInt(parameter, defaultValue);
	}
	
	public boolean getSakaiConfigurationParameterAsBoolean(String parameter, boolean defaultValue) {
		return ServerConfigurationService.getBoolean(parameter, defaultValue);
	}

	
	public int getMaxProfilePictureSize() {
		return getSakaiConfigurationParameterAsInt("profile.picture.max", ProfileImageManager.MAX_PROFILE_IMAGE_UPLOAD_SIZE);
	}
	
	public LinkedHashMap<String, String> getSiteListForUser(int limitSites) {
		LinkedHashMap sites = new LinkedHashMap();
		//we need a good method to get a site for a specific user. screw the session
		sites.put("site1", "test site 1");
		sites.put("site2", "test site 2");
		
		return sites;
	}

	
	/**
	 * Setup a security advisor.
	 */
	private void enableSecurityAdvisor() {
		securityService.pushAdvisor(new SecurityAdvisor(){
			public SecurityAdvice isAllowed(String userId, String function, String reference){
				  return SecurityAdvice.ALLOWED;
			}
		});
	}

	/**
	 * Remove our security advisor.
	 */
	public void disableSecurityAdvisor(){
		securityService.popAdvisor();
	}

	
	
	public String getProfileImageResourcePath(String userId, int type, String fileName) {
		
		//this needs to come from a sakai property perhaps?
		//may break on windows unless use File.separator?
		
		//String fullResourceId = "/private/profileImages/" + userId + "/" + type + "/" + fileName;
		
		String fullResourceId = "/private/profileImages/" + userId + "/" + type + "/" + idManager.createUuid();
		
		return fullResourceId;
		
	}
	
	public boolean saveFile(String fullResourceId, String userId, String fileName, String mimeType, byte[] fileData) {
		
		ContentResourceEdit resource = null;
		boolean result = true;
		
		try {
			
			enableSecurityAdvisor();
			
			try {
								
				resource = contentHostingService.addResource(fullResourceId);
				resource.setContentType(mimeType);
				resource.setContent(fileData);
				ResourceProperties props = resource.getPropertiesEdit();
				props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, mimeType);
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
				props.addProperty(ResourceProperties.PROP_CREATOR, userId);
				resource.getPropertiesEdit().set(props);
				contentHostingService.commitResource(resource, NotificationService.NOTI_NONE);
				result = true;
			}
			catch (IdUsedException e){
				contentHostingService.cancelResource(resource);
				log.error("SakaiProxy.saveFile(): id= " + fullResourceId + " is in use : " + e.getClass() + " : " + e.getMessage());
				result = false;
			}
			catch (Exception e){
				contentHostingService.cancelResource(resource);
				log.error("SakaiProxy.saveFile(): failed: " + e.getClass() + " : " + e.getMessage());
				result = false;
			}
			
		} catch (Exception e) {
			log.error("SakaiProxy.saveFile():" + e.getClass() + ":" + e.getMessage());
			result = false;
		} finally {
			disableSecurityAdvisor();
		}
		return result;
		
	}
	
	
	
	public byte[] getResource(String resourceId) {
		
		byte[] data = null;
		
		try {
			
			enableSecurityAdvisor();
		
			try {
				ContentResource resource = contentHostingService.getResource(resourceId);
				data = resource.getContent();
			}
			catch(Exception e){
				log.error("SakaiProxy.getResource() failed for resourceId: " + resourceId + " : " + e.getClass() + " : " + e.getMessage());
			}
		} catch (Exception e) {
			log.error("SakaiProxy.getResource():" + e.getClass() + ":" + e.getMessage());
		}
		finally	{
			disableSecurityAdvisor();
		}
		return data;
	}
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.SakaiProxy#findUsersByNameOrEmailFromUserDirectory(String search)
	 */
	public List<String> searchUsers(String search) {
		
		List<String> userUuids = new ArrayList<String>();
		
		//search for users
		List<User> results = new ArrayList<User>(userDirectoryService.searchUsers(search, SakaiProxy.FIRST_RECORD, SakaiProxy.MAX_RECORDS));
		
		for(Iterator<User> i = results.iterator(); i.hasNext();){
			User user = (User)i.next();
			//get id
			userUuids.add(user.getId());	
	  	}
		
		return userUuids;
	}
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.SakaiProxy#postEvent(String event,String reference,boolean modify)
	 */	
	public void postEvent(String event,String reference,boolean modify) {
		eventTrackingService.post(eventTrackingService.newEvent(event,reference,modify));
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
	
	private IdManager idManager;
	public void setIdManager(IdManager idManager) {
		this.idManager = idManager;
	}
	
	private EventTrackingService eventTrackingService;
	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	public void init() {
		log.debug("Profile2 SakaiProxy init()");
	}

	

	
}
