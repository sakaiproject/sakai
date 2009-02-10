package uk.ac.lancs.e_science.profile2.impl;


import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;

import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.ProfileUtilityManager;
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
	
	/**
 	* {@inheritDoc}
 	*/
	public SakaiPerson createSakaiPerson(String userId) {
		
		SakaiPerson sakaiPerson = null;
		
		try {
			sakaiPerson = sakaiPersonManager.create(userId, sakaiPersonManager.getUserMutableType());
		} catch (Exception e) {
			log.error("SakaiProxy.createSakaiPerson(): Couldn't create SakaiPerson: " + e.getClass() + " : " + e.getMessage());
		}
		return sakaiPerson;
	}


	
	/**
 	* {@inheritDoc}
 	*/
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
	
	
	/**
 	* {@inheritDoc}
 	*/
	public String getSakaiConfigurationParameterAsString(String parameter, String defaultValue) {
		return(serverConfigurationService.getString(parameter, defaultValue));
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public int getSakaiConfigurationParameterAsInt(String parameter, int defaultValue) {
		return serverConfigurationService.getInt(parameter, defaultValue);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean getSakaiConfigurationParameterAsBoolean(String parameter, boolean defaultValue) {
		return serverConfigurationService.getBoolean(parameter, defaultValue);
	}

	/**
 	* {@inheritDoc}
 	*/
	public int getMaxProfilePictureSize() {
		return getSakaiConfigurationParameterAsInt("profile.picture.max", ProfileImageManager.MAX_PROFILE_IMAGE_UPLOAD_SIZE);
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
	private void disableSecurityAdvisor(){
		securityService.popAdvisor();
	}

	
	/**
 	* {@inheritDoc}
 	*/
	public String getProfileImageResourcePath(String userId, int type, String fileName) {
		
		//this needs to come from a sakai property perhaps?
		//may break on windows unless use File.separator?
		
		//String fullResourceId = "/private/profileImages/" + userId + "/" + type + "/" + fileName;
		
		String fullResourceId = "/private/profileImages/" + userId + "/" + type + "/" + idManager.createUuid();
		
		return fullResourceId;
		
	}
	
	
	/**
 	* {@inheritDoc}
 	*/
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
	
	
	/**
 	* {@inheritDoc}
 	*/
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
 	* {@inheritDoc}
 	*/
	public List<String> searchUsers(String search) {
		
		List<String> userUuids = new ArrayList<String>();
		
		//search for users
		List<User> results = new ArrayList<User>(userDirectoryService.searchUsers(search, ProfileUtilityManager.FIRST_RECORD, ProfileUtilityManager.MAX_RECORDS));
		
		for(Iterator<User> i = results.iterator(); i.hasNext();){
			User user = (User)i.next();
			//get id
			userUuids.add(user.getId());	
	  	}
		
		return userUuids;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public void postEvent(String event,String reference,boolean modify) {
		eventTrackingService.post(eventTrackingService.newEvent(event,reference,modify));
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public void sendEmail(final String userId, final String subject, final String message) {
		
		class EmailSender implements Runnable{
			private Thread runner;
			private String emailTo;
			private String emailFrom;
			private String subject;
			private String message;

			public EmailSender(String emailTo, String emailFrom, String subject, String message) {
				this.emailTo = emailTo;
				this.emailFrom = emailFrom;
				this.subject = subject;
				this.message = message;
				runner = new Thread(this,"Profile2 EmailSender thread");
				runner.start();
			}

			//do it!
			public synchronized void run() {
				try {
					StringBuffer emailText = new StringBuffer();
					emailText.append("<html><body>");
					emailText.append(message);
					emailText.append("</body></html>");

					List<String> additionalHeaders = new ArrayList<String>();
					additionalHeaders.add("Content-Type: text/html; charset=ISO-8859-1");
				
					//do it
					emailService.send(emailFrom, emailTo, subject, emailText.toString(), emailTo, emailFrom, additionalHeaders);
					
					log.info("Email sent to: " + userId);
				} catch (Exception e) {
					log.error("SakaiProxy.sendEmail() failed for userId: " + userId + " : " + e.getClass() + " : " + e.getMessage());
				}
				
			}
		}
		
		//get email address of the user
		String emailTo = getUserEmail(userId);
		if("".equals(emailTo)) {
			//log 
			log.error("SakaiProxy.sendEmail() failed. No email for userId: " + userId);
			return;
		}
		
		//get address to send the message from
		String emailFrom = "no-reply@" + getServerName();
		
		//instantiate class to send the email
		new EmailSender(emailTo, emailFrom, subject, message);
		
	}

	
	/**
 	* {@inheritDoc}
 	*/
	public String getServerName() {
		return serverConfigurationService.getServerName();
	}
	
	public String getPortalUrl() {
		return serverConfigurationService.getPortalUrl();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getServiceName() {
		return getSakaiConfigurationParameterAsString("ui.service", "Sakai");
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public void updateEmailForUser(String userId, String email) {
		
		//are they allowed to update their email?
		if(!isEmailUpdateAllowed(userId)) {
			return;
		}
		
		try {
			UserEdit userEdit = null;
			userEdit = userDirectoryService.editUser(userId);
			userEdit.setEmail(email);
			userDirectoryService.commitEdit(userEdit);
			
			log.info("User email updated for: " + userId);
		}
		catch (Exception e) {  
			log.error("Profile.updateEmailForUser() failed for userId: " + userId);
		}
	}


	
	/**
 	* {@inheritDoc}
 	*/
	public String getDirectUrlToUserProfile(final String userId, final String extraParams) {
		String portalUrl = getPortalUrl();
		
		String siteId = getUserMyWorkspace(userId);
		
		ToolConfiguration toolConfig = getFirstInstanceOfTool(siteId, ProfileUtilityManager.TOOL_ID);
		if(toolConfig == null) {
			log.error("Profile.getDirectUrlToUserProfile() failed for userId: " + userId);
			return null;
		}
		
		String pageId = toolConfig.getPageId();
		String placementId = toolConfig.getId();
				
		try {
			StringBuilder url = new StringBuilder();
			url.append(portalUrl);
			url.append("/site/");
			url.append(siteId);
			url.append("/page/");
			url.append(pageId);
			url.append("?toolstate-");
			url.append(placementId);
			url.append("=");
			url.append(URLEncoder.encode(extraParams,"UTF-8"));
		
			return url.toString();
		}
		catch(Exception e) {
			log.error("SakaiProxy.getDirectUrl():" + e.getClass() + ":" + e.getMessage());
			return null;
		}
		
	}

	
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isEmailUpdateAllowed(String userId) {
		return userDirectoryService.allowUpdateUserEmail(userId);
	}

	/*
	public String getCurrentPageId() {
		Placement placement = toolManager.getCurrentPlacement();
		
		if(placement instanceof ToolConfiguration) {
			return ((ToolConfiguration) placement).getPageId();
		}
		return null;
	}
	
	
	private String getCurrentToolId(){
		return toolManager.getCurrentPlacement().getId();
	}
		
	public String getDirectUrl(String string){
		String portalUrl = getPortalUrl();
		String pageId = getCurrentPageId();
		String siteId = getCurrentSiteId();
		String toolId = getCurrentToolId();
				
		try {
			String url = portalUrl
						+ "/site/" + siteId
						+ "/page/" + pageId
						+ "?toolstate-" + toolId + "="
							+ URLEncoder.encode(string,"UTF-8");
		
			return url;
		}
		catch(Exception e) {
			log.error("SakaiProxy.getDirectUrl():" + e.getClass() + ":" + e.getMessage());
			return null;
		}
	}
	*/
	
	
	
	
	
	
	
	/**
	 * Gets the siteId of the given user's My Workspace
	 * Generally ~userId but may not be
	 * 
	 * @param userId
	 * @return
	 */
	private String getUserMyWorkspace(String userId) {
		return siteService.getUserSiteId(userId);
	}
	
		
	/**
	 * Gets the ToolConfiguration of a page in a site containing a given tool
	 * 
	 * @param siteId	siteId
	 * @param toolId	toolId ie sakai.profile2
	 * @return
	 */
	private ToolConfiguration getFirstInstanceOfTool(String siteId, String toolId) {
		try {
			return siteService.getSite(siteId).getToolForCommonId(toolId);
		}
		catch (IdUnusedException e){
			log.error("Profile.getFirstInstanceOfTool() failed for siteId: " + siteId + " and toolId: " + toolId);
			return null;
		}
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
	
	private EmailService emailService;
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void init() {
		log.debug("Profile2 SakaiProxy init()");
	}

	

	
}
