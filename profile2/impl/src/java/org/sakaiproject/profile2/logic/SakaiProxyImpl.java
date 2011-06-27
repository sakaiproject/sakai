/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.profile2.logic;


import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;

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
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.ActivityService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.profile2.model.MimeTypeByteArray;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.Validator;

/**
 * Implementation of SakaiProxy for Profile2.
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
	public User getCurrentUser() {
		return userDirectoryService.getCurrentUser();
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
	public boolean isSuperUser() {
		return securityService.isSuperUser();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isAdminUser() {
		return StringUtils.equals(sessionManager.getCurrentSessionUserId(), UserDirectoryService.ADMIN_ID);
	}

	
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isSuperUserAndProxiedToUser(String userId) {
		return (isSuperUser() && !StringUtils.equals(userId, getCurrentUserId()));
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getUserType(String userId) {
		String type = null;
		try {
			type = userDirectoryService.getUser(userId).getType();
		} catch (UserNotDefinedException e) {
			log.info("User with eid: " + userId + " does not exist : " + e.getClass() + " : " + e.getMessage());
		}
		return type;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public User getUserById(String userId) {
		User u = null;
		try {
			u = userDirectoryService.getUser(userId);
		} catch (UserNotDefinedException e) {
			log.info("User with id: " + userId + " does not exist : " + e.getClass() + " : " + e.getMessage());
		}
		
		return u;
	}
	
	
	/**
 	* {@inheritDoc}
 	*/
	public User getUserQuietly(String userId) {
		User u = null;
		try {
			u = userDirectoryService.getUser(userId);
		} catch (UserNotDefinedException e) {
			//carry on, no log output. see javadoc for reason.
		}
		return u;
	}

	
	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentToolTitle() {
		Tool tool = toolManager.getCurrentTool();
		if(tool != null)
			return tool.getTitle();
		else
			return "Profile";
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public List<User> getUsers(List<String> userIds) {
		return userDirectoryService.getUsers(userIds);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public List<String> getUuids(List<User> users) {
		List<String> uuids = new ArrayList<String>();
		for(User u: users){
			uuids.add(u.getId());
		}
		return uuids;
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
	public byte[] getSakaiPersonJpegPhoto(String userId) {
		
		SakaiPerson sakaiPerson = null;
		byte[] image = null;
    
		try {
			//try normal user type
			sakaiPerson = sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getUserMutableType());
			if(sakaiPerson != null) {
				image = sakaiPerson.getJpegPhoto();
			}
			//if null try system user type as a profile might have been created with this type
			if(image == null) {
				sakaiPerson = sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getSystemMutableType());
				if(sakaiPerson != null) {
					image = sakaiPerson.getJpegPhoto();
				}
			}
			
		} catch (Exception e) {
			log.error("SakaiProxy.getSakaiPersonJpegPhoto(): Couldn't get SakaiPerson Jpeg photo for: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return image;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getSakaiPersonImageUrl(String userId) {
		
		SakaiPerson sakaiPerson = null;
		String url = null;
    
		try {
			//try normal user type
			sakaiPerson = sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getUserMutableType());
			if(sakaiPerson != null) {
				url = sakaiPerson.getPictureUrl();
			}
			//if null try system user type as a profile might have been created with this type
			if(StringUtils.isBlank(url)) {
				sakaiPerson = sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getSystemMutableType());
				if(sakaiPerson != null) {
					url = sakaiPerson.getPictureUrl();
				}
			}
			
		} catch (Exception e) {
			log.error("SakaiProxy.getSakaiPersonImageUrl(): Couldn't get SakaiPerson image URL for: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return url;
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
		return serverConfigurationService.getInt("profile2.picture.max", ProfileConstants.MAX_IMAGE_UPLOAD_SIZE);
	}
	
	private String getProfileGalleryPath(String userId) {
		String slash = Entity.SEPARATOR;

		StringBuilder path = new StringBuilder();
		path.append(slash);
		path.append("private");
		path.append(slash);
		path.append("profileGallery");
		path.append(slash);
		path.append(userId);
		path.append(slash);

		return path.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProfileGalleryImagePath(String userId, String imageId) {
		
		StringBuilder path = new StringBuilder(getProfileGalleryPath(userId));

		path.append(ProfileConstants.GALLERY_IMAGE_MAIN);
		path.append(Entity.SEPARATOR);
		path.append(imageId);

		return path.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProfileGalleryThumbnailPath(String userId, String imageId) {
		StringBuilder path = new StringBuilder(getProfileGalleryPath(userId));
		path.append(ProfileConstants.GALLERY_IMAGE_THUMBNAILS);
		path.append(Entity.SEPARATOR);
		path.append(imageId);

		return path.toString();
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
		path.append(ProfileUtils.generateUuid());
		
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
	public MimeTypeByteArray getResource(String resourceId) {
		
		MimeTypeByteArray mtba = new MimeTypeByteArray();
		
		if(StringUtils.isBlank(resourceId)) {
			return null;
		}
		
		try {
			enableSecurityAdvisor();
			try {
				ContentResource resource = contentHostingService.getResource(resourceId);
				if(resource == null){
					return null;
				}
				mtba.setBytes(resource.getContent());
				mtba.setMimeType(resource.getContentType());
				return mtba;
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
		
		return null;
	}
	
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean removeResource(String resourceId) {
		
		boolean result = false;
		
		try {
			enableSecurityAdvisor();

			contentHostingService.removeResource(resourceId);
			
			result = true;
		} catch (Exception e) {
			log.error("SakaiProxy.removeResource() failed for resourceId "
					+ resourceId + ": " + e.getMessage());
			return false;
		} finally {
			disableSecurityAdvisor();
		}
		
		return result;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public List<User> searchUsers(String search) {
		return userDirectoryService.searchUsers(search, ProfileConstants.FIRST_RECORD, ProfileConstants.MAX_RECORDS);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public List<User> searchExternalUsers(String search) {
		return userDirectoryService.searchExternalUsers(search, ProfileConstants.FIRST_RECORD, ProfileConstants.MAX_RECORDS);
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
		
		class EmailSender {
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
			}

			//do it!
			public void send() {
				try {

					//get User to send to
					User user = userDirectoryService.getUser(userId);
					
					if (StringUtils.isBlank(user.getEmail())){
						log.error("SakaiProxy.sendEmail() failed. No email for userId: " + userId);
						return;
					}
					
					//do it
					emailService.sendToUsers(Collections.singleton(user), getHeaders(user.getEmail(), subject), formatMessage(subject, message));
					
					log.info("Email sent to: " + userId);
				} catch (UserNotDefinedException e) {
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
		new EmailSender(userId, subject, message).send();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public void sendEmail(List<String> userIds, final String emailTemplateKey, final Map<String,String> replacementValues) {
		
		//get list of Users
		final List<User> users = new ArrayList<User>(getUsers(userIds));
		
		//only ever use one thread whether sending to one user or many users
		Thread sendMailThread = new Thread() {
			
			public void run() {
				//get the rendered template for each user
				RenderedTemplate template = null;
				
				for(User user : users) {
					log.info("SakaiProxy.sendEmail() attempting to send email to: " + user.getId());
					try { 
						template = emailTemplateService.getRenderedTemplateForUser(emailTemplateKey, user.getReference(), replacementValues); 
						if (template == null) {
							log.error("SakaiProxy.sendEmail() no template with key: " + emailTemplateKey);
							return;	//no template
						}
					}
					catch (Exception e) {
						log.error("SakaiProxy.sendEmail() error retrieving template for user: " + user.getId() + " with key: " + emailTemplateKey + " : " + e.getClass() + " : " + e.getMessage());
						continue; //try next user
					}
					
					//send
					sendEmail(user.getId(), template.getRenderedSubject(), template.getRenderedHtmlMessage());
				}
			}
		};
		sendMailThread.start();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public void sendEmail(String userId, String emailTemplateKey, Map<String,String> replacementValues) {
		sendEmail(Collections.singletonList(userId), emailTemplateKey, replacementValues);
	}


	
	/**
 	* {@inheritDoc}
 	*/
	public String getServerName() {
		return serverConfigurationService.getServerName();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getPortalUrl() {
		return serverConfigurationService.getPortalUrl();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getServerUrl() {
		return serverConfigurationService.getServerUrl();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getFullPortalUrl() {
		return getServerUrl() + getPortalPath(); 
	}

	/**
 	* {@inheritDoc}
 	*/
	public String getPortalPath() {
		return serverConfigurationService.getString("portalPath", "/portal");
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isUsingNormalPortal() {
		return StringUtils.equals(getPortalPath(), "/portal");
	}

	/**
 	* {@inheritDoc}
 	*/
	public String getUserHomeUrl() {
		return serverConfigurationService.getUserHomeUrl();
	}

	
	/**
 	* {@inheritDoc}
 	*/
	public String getServiceName() {
		return serverConfigurationService.getString("ui.service", ProfileConstants.SAKAI_PROP_SERVICE_NAME);
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
		String portalUrl = getFullPortalUrl();
		String siteId = getUserMyWorkspace(userId);
		
		ToolConfiguration toolConfig = getFirstInstanceOfTool(siteId, ProfileConstants.TOOL_ID);
		if(toolConfig == null) {
			//if the user doesn't have the Profile2 tool installed in their My Workspace,
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
			//only if we have params to add
			if(StringUtils.isNotBlank(extraParams)) {
				url.append("?toolstate-");
				url.append(placementId);
				url.append("=");
				url.append(URLEncoder.encode(extraParams,"UTF-8"));
			}
		
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
	public boolean isBusinessProfileEnabled() {
		return serverConfigurationService.getBoolean(
				"profile2.profile.business.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PROFILE_BUSINESS_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isWallEnabledGlobally() {
		return serverConfigurationService.getBoolean(
				"profile2.wall.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_WALL_ENABLED);		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isWallDefaultProfilePage() {
		return serverConfigurationService.getBoolean(
				"profile2.wall.default",
				ProfileConstants.SAKAI_PROP_PROFILE2_WALL_DEFAULT);	
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isProfileConversionEnabled() {
		return serverConfigurationService.getBoolean("profile2.convert", ProfileConstants.SAKAI_PROP_PROFILE2_CONVERSION_ENABLED);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isTwitterIntegrationEnabledGlobally() {
		return serverConfigurationService.getBoolean("profile2.integration.twitter.enabled", ProfileConstants.SAKAI_PROP_PROFILE2_TWITTER_INTEGRATION_ENABLED);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getTwitterSource() {
		return serverConfigurationService.getString("profile2.integration.twitter.source", ProfileConstants.SAKAI_PROP_PROFILE2_TWITTER_INTEGRATION_SOURCE);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isProfileGalleryEnabledGlobally() {
		return serverConfigurationService.getBoolean("profile2.gallery.enabled", ProfileConstants.SAKAI_PROP_PROFILE2_GALLERY_ENABLED);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isProfilePictureChangeEnabled() {
		// PRFL-395: Ability to enable/disable profile picture change per user type
		boolean globallyEnabled = serverConfigurationService.getBoolean("profile2.picture.change.enabled", ProfileConstants.SAKAI_PROP_PROFILE2_PICTURE_CHANGE_ENABLED);
		String userType = getUserType(getCurrentUserId());
		// return user type specific setting, defaulting to global one
		return serverConfigurationService.getBoolean("profile2.picture.change." + userType + ".enabled", globallyEnabled);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public int getProfilePictureType() {
		String pictureType = serverConfigurationService.getString("profile2.picture.type", ProfileConstants.PICTURE_SETTING_UPLOAD_PROP);
				
		//if 'upload'
		if(pictureType.equals(ProfileConstants.PICTURE_SETTING_UPLOAD_PROP)) {
			return ProfileConstants.PICTURE_SETTING_UPLOAD;
		}
		//if 'url'
		else if(pictureType.equals(ProfileConstants.PICTURE_SETTING_URL_PROP)) {
			return ProfileConstants.PICTURE_SETTING_URL;
		}
		//if 'official'
		else if(pictureType.equals(ProfileConstants.PICTURE_SETTING_OFFICIAL_PROP)) {
			return ProfileConstants.PICTURE_SETTING_OFFICIAL;
		}
		//gravatar is not an enforceable setting, hence no block here. it is purely a user preference.
		//but can be disabled
		
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
	public String ensureUuid(String userId) {
		
		//check for userId
		try {
			User u = userDirectoryService.getUser(userId);
			if(u != null){
				return userId;
			}
		} catch (UserNotDefinedException e) {
			//do nothing, this is fine, cotninue to next check
		}
		
		//check for eid
		try {
			User u = userDirectoryService.getUserByEid(userId);
			if(u != null){
				return u.getId();
			}
		} catch (UserNotDefinedException e) {
			//do nothing, this is fine, continue
		}
		
		log.error("User: " + userId + " could not be found in any lookup by either id or eid");
		return null;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean currentUserMatchesRequest(String userUuid) {
		
		//get current user
		String currentUserUuid = getCurrentUserId();
		
		//check match
		if(StringUtils.equals(currentUserUuid, userUuid)) {
			return true;
		}
		
		return false;
	}

	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isPrivacyChangeAllowedGlobally() {
		return serverConfigurationService.getBoolean("profile2.privacy.change.enabled", ProfileConstants.SAKAI_PROP_PROFILE2_PRIVACY_CHANGE_ENABLED);
	}
	
	
	/**
 	* {@inheritDoc}
 	*/
	public HashMap<String, Object> getOverriddenPrivacySettings() {
		
		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put("profileImage", serverConfigurationService.getInt("profile2.privacy.default.profileImage", ProfileConstants.DEFAULT_PRIVACY_OPTION_PROFILEIMAGE));
		props.put("basicInfo", serverConfigurationService.getInt("profile2.privacy.default.basicInfo", ProfileConstants.DEFAULT_PRIVACY_OPTION_BASICINFO));
		props.put("contactInfo", serverConfigurationService.getInt("profile2.privacy.default.contactInfo", ProfileConstants.DEFAULT_PRIVACY_OPTION_CONTACTINFO));
		props.put("staffInfo", serverConfigurationService.getInt("profile2.privacy.default.staffInfo", ProfileConstants.DEFAULT_PRIVACY_OPTION_STAFFINFO));
		props.put("studentInfo", serverConfigurationService.getInt("profile2.privacy.default.studentInfo", ProfileConstants.DEFAULT_PRIVACY_OPTION_STUDENTINFO));
		props.put("personalInfo", serverConfigurationService.getInt("profile2.privacy.default.personalInfo", ProfileConstants.DEFAULT_PRIVACY_OPTION_PERSONALINFO));
		props.put("birthYear", serverConfigurationService.getBoolean("profile2.privacy.default.birthYear", ProfileConstants.DEFAULT_BIRTHYEAR_VISIBILITY));
		props.put("myFriends", serverConfigurationService.getInt("profile2.privacy.default.myFriends", ProfileConstants.DEFAULT_PRIVACY_OPTION_MYFRIENDS));
		props.put("myStatus", serverConfigurationService.getInt("profile2.privacy.default.myStatus", ProfileConstants.DEFAULT_PRIVACY_OPTION_MYSTATUS));
		props.put("myPictures", serverConfigurationService.getInt("profile2.privacy.default.myPictures", ProfileConstants.DEFAULT_PRIVACY_OPTION_MYPICTURES));
		props.put("messages", serverConfigurationService.getInt("profile2.privacy.default.messages", ProfileConstants.DEFAULT_PRIVACY_OPTION_MESSAGES));
		props.put("businessInfo", serverConfigurationService.getInt("profile2.privacy.default.businessInfo", ProfileConstants.DEFAULT_PRIVACY_OPTION_BUSINESSINFO));
		props.put("myKudos", serverConfigurationService.getInt("profile2.privacy.default.myKudos", ProfileConstants.DEFAULT_PRIVACY_OPTION_MYKUDOS));
		props.put("myWall", serverConfigurationService.getInt("profile2.privacy.default.myWall", ProfileConstants.DEFAULT_PRIVACY_OPTION_MYWALL));
		
		return props;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public List<String> getInvisibleUsers() {
		String config = serverConfigurationService.getString("profile2.invisible.users", ProfileConstants.SAKAI_PROP_INVISIBLE_USERS);
		return ProfileUtils.getListFromString(config, ProfileConstants.SAKAI_PROP_LIST_SEPARATOR);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isConnectionAllowedBetweenUserTypes(String requestingUserType, String targetUserType) {
		
		if(isSuperUser()){
			return true;
		}
		
		String configuration = serverConfigurationService.getString("profile2.allowed.connection.usertypes." + requestingUserType);
		if(StringUtils.isBlank(configuration)) {
			return true;
		}
		
		String[] values = StringUtils.split(configuration, ',');
		List<String> valueList = Arrays.asList(values);

		if(valueList.contains(targetUserType)){
			return true;
		}
		
		return false;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean toggleProfileLocked(String userId, boolean locked) {
		SakaiPerson sp = getSakaiPerson(userId);
		if(sp == null) {
			return false;
		}
		sp.setLocked(locked);
		if(updateSakaiPerson(sp)){
			return true;
		}
		return false;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isOfficialImageEnabledGlobally() {
		return serverConfigurationService.getBoolean("profile2.official.image.enabled", ProfileConstants.SAKAI_PROP_PROFILE2_OFFICIAL_IMAGE_ENABLED);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isUsingOfficialImageButAlternateSelectionEnabled() {
		
		if(isOfficialImageEnabledGlobally() && 
			getProfilePictureType() != ProfileConstants.PICTURE_SETTING_OFFICIAL &&
			isProfilePictureChangeEnabled()) {
			return true;
		}
		return false;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getOfficialImageSource() {
		return serverConfigurationService.getString("profile2.official.image.source", ProfileConstants.OFFICIAL_IMAGE_SETTING_DEFAULT);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getOfficialImageAttribute() {
		return serverConfigurationService.getString("profile2.official.image.attribute", ProfileConstants.USER_PROPERTY_JPEG_PHOTO);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getSkinRepoProperty(){
		return serverConfigurationService.getString("skin.repo");
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getToolSkinCSS(String skinRepo){
		
		String skin = siteService.findTool(sessionManager.getCurrentToolSession().getPlacementId()).getSkin();			
		
		if(skin == null) {
			skin = serverConfigurationService.getString("skin.default");
		}
		
		return skinRepo + "/" + skin + "/tool.css";
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String createUuid() {
		return idManager.createUuid();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isUserActive(String userUuid) {
		return activityService.isUserActive(userUuid);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public List<String> getActiveUsers(List<String> userUuids){
		return activityService.getActiveUsers(userUuids);
	}

	/**
 	* {@inheritDoc}
 	*/
	public Long getLastEventTimeForUser(String userUuid) {
		return activityService.getLastEventTimeForUser(userUuid);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public Map<String, Long> getLastEventTimeForUsers(List<String> userUuids) {
		return activityService.getLastEventTimeForUsers(userUuids);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getServerConfigurationParameter(String key, String def) {
		return serverConfigurationService.getString(key, def);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isUserMyWorkspace(String siteId) {
		return siteService.isUserSite(siteId);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isUserAllowedInSite(String userId, String permission, String siteId) {
		if(securityService.isSuperUser()) {
			return true;
		}
		String siteRef = siteId;
		if(siteId != null && !siteId.startsWith(SiteService.REFERENCE_ROOT)) {
			siteRef = SiteService.REFERENCE_ROOT + Entity.SEPARATOR + siteId;
		}
		if(securityService.unlock(userId, permission, siteRef)) {
			return true;
		}
		return false;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean checkForSite(String siteId) {
		return siteService.siteExists(siteId);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getMaxSearchResults() {
		return serverConfigurationService.getInt(
				"profile2.search.maxSearchResults",
				ProfileConstants.DEFAULT_MAX_SEARCH_RESULTS);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxSearchResultsPerPage() {
		return serverConfigurationService.getInt(
				"profile2.search.maxSearchResultsPerPage",
				ProfileConstants.DEFAULT_MAX_SEARCH_RESULTS_PER_PAGE);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isGravatarImageEnabledGlobally() {
		return serverConfigurationService.getBoolean("profile2.gravatar.image.enabled", ProfileConstants.SAKAI_PROP_PROFILE2_GRAVATAR_IMAGE_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isUserAllowedAddSite(String userUuid) {
		return siteService.allowAddSite(userUuid);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Site addSite(String id, String type) {
		Site site = null;
		try {
			site = siteService.addSite(id, type);
		} catch (IdInvalidException e) {
			e.printStackTrace();
		} catch (IdUsedException e) {
			e.printStackTrace();
		} catch (PermissionException e) {
			e.printStackTrace();
		}
		
		return site;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean saveSite(Site site) {
		try {
			siteService.save(site);
		} catch (IdUnusedException e) {
			e.printStackTrace();
			return false;
		} catch (PermissionException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Tool getTool(String id) {
		return toolManager.getTool(id);
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
		log.info("Profile2 SakaiProxy init()");
		
		//process the email templates
		//the list is injected via Spring
		emailTemplateService.processEmailTemplates(emailTemplates);
		
	}

	
	
	@Setter
	private ToolManager toolManager;
	
	@Setter
	private SecurityService securityService;
	
	@Setter
	private SessionManager sessionManager;
	
	@Setter
	private SiteService siteService;
	
	@Setter
	private UserDirectoryService userDirectoryService;
	
	@Setter
	private SakaiPersonManager sakaiPersonManager;
	
	@Setter
	private ContentHostingService contentHostingService;
	
	@Setter
	private EventTrackingService eventTrackingService;
	
	@Setter
	private EmailService emailService;
	
	@Setter
	private ServerConfigurationService serverConfigurationService;
	
	@Setter
	private EmailTemplateService emailTemplateService;
	
	@Setter
	private IdManager idManager;
	
	@Setter
	private ActivityService activityService;
	

	//INJECT OTHER RESOURCES
	@Setter
	private ArrayList<String> emailTemplates;
	
	
}
