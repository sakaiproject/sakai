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
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;

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
	public SakaiPerson getSakaiPerson(String userId) {
		
		SakaiPerson sakaiPerson = null;
		
		try {
			sakaiPerson = sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getUserMutableType());
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
		return serverConfigurationService.getInt("profile.picture.max", ProfileImageManager.MAX_PROFILE_IMAGE_UPLOAD_SIZE);
	}
	
	
	/**
 	* {@inheritDoc}
 	*/
	public String getProfileImageResourcePath(String userId, int type) {
		
		//this needs to come from a sakai property perhaps?
		//may break on windows unless use File.separator?
		
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
					
					if (StringUtil.trimToNull(user.getEmail()) == null){
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
				if (StringUtil.trimToNull(emailTo) != null) {
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

	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isProfileConversionEnabled() {
		return serverConfigurationService.getBoolean("profile.convert", false);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getTwitterSource() {
		return serverConfigurationService.getString("profile2.integration.twitter.source", "profile2");
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
			log.error("Profile.getFirstInstanceOfTool() failed for siteId: " + siteId + " and toolId: " + toolId);
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
