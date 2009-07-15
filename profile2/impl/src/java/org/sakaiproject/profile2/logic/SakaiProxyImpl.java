package org.sakaiproject.profile2.logic;


import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.profile2.model.ResourceWrapper;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.Validator;

/**
 * This is the Implementation of the helper API used by the Profile2 tool only. 
 * 
 * DO NOT USE THIS YOURSELF
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class SakaiProxyImpl implements SakaiProxy {

	private static final Logger log = Logger.getLogger(SakaiProxyImpl.class);
    
	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentSiteId(){
		return toolManager.getCurrentPlacement().getContext();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getUserEid(String userId){
		String eid = null;
		try {
			eid = userDirectoryService.getUser(userId).getEid();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get eid for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return eid;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getUserIdForEid(String eid) {
		String userUuid = null;
		try {
			userUuid = userDirectoryService.getUserByEid(eid).getId();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get id for eid: " + eid + " : " + e.getClass() + " : " + e.getMessage());
		}
		return userUuid;
	}


	/**
 	* {@inheritDoc}
 	*/
	public String getUserDisplayName(String userId) {
	   String name = null;
		try {
			name = userDirectoryService.getUser(userId).getDisplayName();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get displayname for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return name;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getUserFirstName(String userId) {
		String email = null;
		try {
			email = userDirectoryService.getUser(userId).getFirstName();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get first name for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return email;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getUserLastName(String userId) {
		String email = null;
		try {
			email = userDirectoryService.getUser(userId).getLastName();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get last name for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return email;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getUserEmail(String userId) {
		String email = null;
		try {
			email = userDirectoryService.getUser(userId).getEmail();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get email for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return email;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean checkForUser(String userId) {
		User u = null;
		try {
			u = userDirectoryService.getUser(userId);
			if (u != null) {
				return true;
			} 
		} catch (UserNotDefinedException e) {
			log.info("User with id: " + userId + " does not exist : " + e.getClass() + " : " + e.getMessage());
		}
		return false;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean checkForUserByEid(String eid) {
		User u = null;
		try {
			u = userDirectoryService.getUserByEid(eid);
			if (u != null) {
				return true;
			} 
		} catch (UserNotDefinedException e) {
			log.info("User with eid: " + eid + " does not exist : " + e.getClass() + " : " + e.getMessage());
		}
		return false;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public SakaiPerson getSakaiPerson(String userId) {
		
		SakaiPerson sakaiPerson = null;
		
		try {
			//try normal user type
			sakaiPerson = sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getUserMutableType());
			
			//if null try system user type as a profile might have been created with this type
			if(sakaiPerson == null) {
				sakaiPerson = sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getSystemMutableType());
			}
			
		} catch (Exception e) {
			log.error("SakaiProxy.getSakaiPerson(): Couldn't get SakaiPerson for: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return sakaiPerson;
	}
	
	/**
 	* {@inheritDoc}
 	*/
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
	public int getMaxProfilePictureSize() {
		return serverConfigurationService.getInt("profile2.picture.max", ProfileConstants.MAX_PROFILE_IMAGE_UPLOAD_SIZE);
	}
	
	
	/**
 	* {@inheritDoc}
 	*/
	public String getProfileImageResourcePath(String userId, int type) {
		
		String slash = Entity.SEPARATOR;
		
		StringBuilder path = new StringBuilder();
		path.append(slash);
		path.append("private");
		path.append(slash);
		path.append("profileImages");
		path.append(slash);
		path.append(userId);
		path.append(slash);
		path.append(type);
		path.append(slash);
		path.append(idManager.createUuid());
		
		return path.toString();
		
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
		
		if(StringUtils.isBlank(resourceId)) {
			return null;
		}
		
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
	public ResourceWrapper getResourceWrapped(String resourceId) {
		
		ResourceWrapper wrapper = new ResourceWrapper();
		
		try {
			
			enableSecurityAdvisor();
		
			try {
				ContentResource resource = contentHostingService.getResource(resourceId);
				wrapper.setBytes(resource.getContent());
				wrapper.setMimeType(resource.getContentType());
				wrapper.setResourceID(resourceId);
				wrapper.setLength(resource.getContentLength());
			}
			catch(Exception e){
				log.error("SakaiProxy.getResourceWrapped() failed for resourceId: " + resourceId + " : " + e.getClass() + " : " + e.getMessage());
			}
		} catch (Exception e) {
			log.error("SakaiProxy.getResourceWrapped():" + e.getClass() + ":" + e.getMessage());
		}
		finally	{
			disableSecurityAdvisor();
		}
		return wrapper;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public List<String> searchUsers(String search) {
		
		List<String> userUuids = new ArrayList<String>();
		
		//search for users
		List<User> results = new ArrayList<User>(userDirectoryService.searchUsers(search, ProfileConstants.FIRST_RECORD, ProfileConstants.MAX_RECORDS));
		
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
	public void sendEmail(final String userId, final String subject, String message) {
		
		class EmailSender implements Runnable{
			private Thread runner;
			private String userId;
			private String subject;
			private String message;
			
			public final String MULTIPART_BOUNDARY = "======sakai-multi-part-boundary======";
			public final String BOUNDARY_LINE = "\n\n--"+MULTIPART_BOUNDARY+"\n";
			public final String TERMINATION_LINE = "\n\n--"+MULTIPART_BOUNDARY+"--\n\n";
			public final String MIME_ADVISORY = "This message is for MIME-compliant mail readers.";
			public final String PLAIN_TEXT_HEADERS= "Content-Type: text/plain\n\n";
			public final String HTML_HEADERS = "Content-Type: text/html; charset=ISO-8859-1\n\n";
			public final String HTML_END = "\n  </body>\n</html>\n";

			public EmailSender(String userId, String subject, String message) {
				this.userId = userId;
				this.subject = subject;
				this.message = message;
				runner = new Thread(this,"Profile2 EmailSender thread");
				runner.start();
			}

			//do it!
			public synchronized void run() {
				try {

					//get User to send to
					User user = userDirectoryService.getUser(userId);
					
					if (StringUtils.isBlank(user.getEmail())){
						log.error("SakaiProxy.sendEmail() failed. No email for userId: " + userId);
						return;
					}
					
					List<User> receivers = new ArrayList<User>();
					receivers.add(user);
					
					//do it
					emailService.sendToUsers(receivers, getHeaders(user.getEmail(), subject), formatMessage(subject, message));

					
					log.info("Email sent to: " + userId);
				} catch (Exception e) {
					log.error("SakaiProxy.sendEmail() failed for userId: " + userId + " : " + e.getClass() + " : " + e.getMessage());
				}
			}
			
			/** helper methods for formatting the message */
			private String formatMessage(String subject, String message) {
				StringBuilder sb = new StringBuilder();
				sb.append(MIME_ADVISORY);
				sb.append(BOUNDARY_LINE);
				sb.append(PLAIN_TEXT_HEADERS);
				sb.append(Validator.escapeHtmlFormattedText(message));
				sb.append(BOUNDARY_LINE);
				sb.append(HTML_HEADERS);
				sb.append(htmlPreamble(subject));
				sb.append(message);
				sb.append(HTML_END);
				sb.append(TERMINATION_LINE);
				
				return sb.toString();
			}
			
			private String htmlPreamble(String subject) {
				StringBuilder sb = new StringBuilder();
				sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n");
				sb.append("\"http://www.w3.org/TR/html4/loose.dtd\">\n");
				sb.append("<html>\n");
				sb.append("<head><title>");
				sb.append(subject);
				sb.append("</title></head>\n");
				sb.append("<body>\n");
				
				return sb.toString();
			}
			
			private List<String> getHeaders(String emailTo, String subject){
				List<String> headers = new ArrayList<String>();
				headers.add("MIME-Version: 1.0");
				headers.add("Content-Type: multipart/alternative; boundary=\""+MULTIPART_BOUNDARY+"\"");
				headers.add(formatSubject(subject));
				headers.add(getFrom());
				if (StringUtils.isNotBlank(emailTo)) {
					headers.add("To: " + emailTo);
				}
				
				return headers;
			}
			
			private String getFrom(){
				StringBuilder sb = new StringBuilder();
				sb.append("From: ");
				sb.append(getServiceName());
				sb.append(" <no-reply@");
				sb.append(getServerName());
				sb.append(">");
				
				return sb.toString();
			}
			
			private String formatSubject(String subject) {
				StringBuilder sb = new StringBuilder();
				sb.append("Subject: ");
				sb.append(subject);
				
				return sb.toString();
			}
			
			
		}
		
		//instantiate class to format, then send the mail
		new EmailSender(userId, subject, message);
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
	
	public String getServerUrl() {
		return serverConfigurationService.getServerUrl();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getServiceName() {
		return serverConfigurationService.getString("ui.service", "Sakai");
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public void updateEmailForUser(String userId, String email) {
		try {
			UserEdit userEdit = null;
			userEdit = userDirectoryService.editUser(userId);
			userEdit.setEmail(email);
			userDirectoryService.commitEdit(userEdit);
			
			log.info("User email updated for: " + userId);
		}
		catch (Exception e) {  
			log.error("SakaiProxy.updateEmailForUser() failed for userId: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
	}

	
	/**
 	* {@inheritDoc}
 	*/
	public void updateNameForUser(String userId, String firstName, String lastName) {
		try {
			UserEdit userEdit = null;
			userEdit = userDirectoryService.editUser(userId);
			userEdit.setFirstName(firstName);
			userEdit.setLastName(lastName);
			userDirectoryService.commitEdit(userEdit);
			
			log.info("User name details updated for: " + userId);
		}
		catch (Exception e) {  
			log.error("SakaiProxy.updateNameForUser() failed for userId: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
	}

	
	/**
 	* {@inheritDoc}
 	*/
	public String getDirectUrlToUserProfile(final String userId, final String extraParams) {
		String portalUrl = getPortalUrl();
		
		String siteId = getUserMyWorkspace(userId);
		
		ToolConfiguration toolConfig = getFirstInstanceOfTool(siteId, ProfileConstants.TOOL_ID);
		if(toolConfig == null) {
			//if the user doesn't have the profile2 tool installed in their My Workspace,
			log.warn("SakaiProxy.getDirectUrlToUserProfile() failed to find " + ProfileConstants.TOOL_ID + " installed in My Workspace for userId: " + userId);
			
			//just return a link to their My Workspace
			StringBuilder url = new StringBuilder();
			url.append(portalUrl);
			url.append("/site/");
			url.append(siteId);
			return url.toString();
			
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
	public boolean isAccountUpdateAllowed(String userId) {
		try {
			UserEdit edit = userDirectoryService.editUser(userId);
			userDirectoryService.cancelEdit(edit);
			return true;
		}
		catch (Exception e) {
			log.info("SakaiProxy.isAccountUpdateAllowed() false for userId: " + userId);
			return false;
		}
	}

	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isProfileConversionEnabled() {
		return serverConfigurationService.getBoolean("profile2.convert", false);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isTwitterIntegrationEnabledGlobally() {
		return serverConfigurationService.getBoolean("profile2.integration.twitter.enabled", true);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getTwitterSource() {
		return serverConfigurationService.getString("profile2.integration.twitter.source", "profile2");
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isProfilePictureChangeEnabled() {
		return serverConfigurationService.getBoolean("profile2.picture.change.enabled", true);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public int getProfilePictureType() {
		String pictureType = serverConfigurationService.getString("profile2.picture.type");
		
		//if 'upload'
		if(pictureType.equals(ProfileConstants.PICTURE_SETTING_UPLOAD_PROP)) {
			return ProfileConstants.PICTURE_SETTING_UPLOAD;
		}
		//if 'url'
		else if(pictureType.equals(ProfileConstants.PICTURE_SETTING_URL_PROP)) {
			return ProfileConstants.PICTURE_SETTING_URL;
		}
		//otherwise return default
		else {
			return ProfileConstants.PICTURE_SETTING_DEFAULT;
		}
	}
	
	
	/**
 	* {@inheritDoc}
 	*/
	public List<String> getAcademicEntityConfigurationSet() {
		
		String configuration = serverConfigurationService.getString("profile2.profile.entity.set.academic", ProfileConstants.ENTITY_SET_ACADEMIC);
		String[] parameters = StringUtils.split(configuration, ',');
		
		List<String> tempList = Arrays.asList(parameters);
		List<String> list = new ArrayList<String>(tempList);
		
		return list;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public List<String> getMinimalEntityConfigurationSet() {
		String configuration = serverConfigurationService.getString("profile2.profile.entity.set.minimal", ProfileConstants.ENTITY_SET_MINIMAL);
		String[] parameters = StringUtils.split(configuration, ',');
		
		List<String> tempList = Arrays.asList(parameters);
		List<String> list = new ArrayList<String>(tempList);
		
		return list;
	}
	
	
	/**
 	* {@inheritDoc}
 	*/
	public String getUuidForUserId(String userId) {
		
		String userUuid = null;

		if(checkForUser(userId)) {
			userUuid = userId;
		} else if (checkForUserByEid(userId)) {
			userUuid = getUserIdForEid(userId);
			
			if(userUuid == null) {
				log.error("Could not translate eid to uuid for: " + userId);
			}
		} else {
			log.error("User: " + userId + " could not be found in any lookup by either id or eid");
		}
		
		return userUuid;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isPrivacyChangeAllowedGlobally() {
		return serverConfigurationService.getBoolean("profile2.privacy.change.enabled", true);
	}
	
	
	/**
 	* {@inheritDoc}
 	*/
	public HashMap<String, Object> getOverriddenPrivacySettings() {
		
		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put("profileImage", serverConfigurationService.getInt("profile2.privacy.default.profileImage", ProfileConstants.DEFAULT_PRIVACY_OPTION_PROFILEIMAGE));
		props.put("basicInfo", serverConfigurationService.getInt("profile2.privacy.default.basicInfo", ProfileConstants.DEFAULT_PRIVACY_OPTION_BASICINFO));
		props.put("contactInfo", serverConfigurationService.getInt("profile2.privacy.default.contactInfo", ProfileConstants.DEFAULT_PRIVACY_OPTION_CONTACTINFO));
		props.put("academicInfo", serverConfigurationService.getInt("profile2.privacy.default.academicInfo", ProfileConstants.DEFAULT_PRIVACY_OPTION_ACADEMICINFO));
		props.put("personalInfo", serverConfigurationService.getInt("profile2.privacy.default.personalInfo", ProfileConstants.DEFAULT_PRIVACY_OPTION_PERSONALINFO));
		props.put("birthYear", serverConfigurationService.getBoolean("profile2.privacy.default.birthYear", ProfileConstants.DEFAULT_BIRTHYEAR_VISIBILITY));
		props.put("search", serverConfigurationService.getInt("profile2.privacy.default.search", ProfileConstants.DEFAULT_PRIVACY_OPTION_SEARCH));
		props.put("myFriends", serverConfigurationService.getInt("profile2.privacy.default.myFriends", ProfileConstants.DEFAULT_PRIVACY_OPTION_MYFRIENDS));
		props.put("myStatus", serverConfigurationService.getInt("profile2.privacy.default.myStatus", ProfileConstants.DEFAULT_PRIVACY_OPTION_MYSTATUS));

		return props;
	}
	
	
	// PRIVATE METHODS FOR SAKAIPROXY
	
	/**
	 * Setup a security advisor for this transaction
	 */
	private void enableSecurityAdvisor() {
		securityService.pushAdvisor(new SecurityAdvisor(){
			public SecurityAdvice isAllowed(String userId, String function, String reference){
				  return SecurityAdvice.ALLOWED;
			}
		});
	}

	/**
	 * Remove security advisor from the stack
	 */
	private void disableSecurityAdvisor(){
		securityService.popAdvisor();
	}
	
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
			log.error("SakaiProxy.getFirstInstanceOfTool() failed for siteId: " + siteId + " and toolId: " + toolId);
			return null;
		}
	}
	
	
	
	
	
	
	
	
	/**
	 * init
	 */
	public void init() {
		log.debug("Profile2 SakaiProxy init()");
	}

	
	
	
	// SETUP SAKAI API'S
	
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

	
}
