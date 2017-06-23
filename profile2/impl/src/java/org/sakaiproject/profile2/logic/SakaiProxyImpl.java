/**
 * Copyright (c) 2008-2012 The Sakai Foundation
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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
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
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Setter;

/**
 * Implementation of SakaiProxy for Profile2.
 *
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class SakaiProxyImpl implements SakaiProxy {

	private static final Logger log = LoggerFactory.getLogger(SakaiProxyImpl.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCurrentSiteId() {
		return this.toolManager.getCurrentPlacement().getContext();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCurrentUserId() {
		return this.sessionManager.getCurrentSessionUserId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getCurrentUser() {
		return this.userDirectoryService.getCurrentUser();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUserEid(final String userId) {
		String eid = null;
		try {
			eid = this.userDirectoryService.getUser(userId).getEid();
		} catch (final UserNotDefinedException e) {
			log.warn("Cannot get eid for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return eid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUserIdForEid(final String eid) {
		String userUuid = null;
		try {
			userUuid = this.userDirectoryService.getUserByEid(eid).getId();
		} catch (final UserNotDefinedException e) {
			log.warn("Cannot get id for eid: " + eid + " : " + e.getClass() + " : " + e.getMessage());
		}
		return userUuid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUserDisplayName(final String userId) {
		String name = null;
		try {
			name = this.userDirectoryService.getUser(userId).getDisplayName();
		} catch (final UserNotDefinedException e) {
			log.warn("Cannot get displayname for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUserFirstName(final String userId) {
		String email = null;
		try {
			email = this.userDirectoryService.getUser(userId).getFirstName();
		} catch (final UserNotDefinedException e) {
			log.warn("Cannot get first name for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return email;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUserLastName(final String userId) {
		String email = null;
		try {
			email = this.userDirectoryService.getUser(userId).getLastName();
		} catch (final UserNotDefinedException e) {
			log.warn("Cannot get last name for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return email;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUserEmail(final String userId) {
		String email = null;
		try {
			email = this.userDirectoryService.getUser(userId).getEmail();
		} catch (final UserNotDefinedException e) {
			log.warn("Cannot get email for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return email;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean checkForUser(final String userId) {
		User u = null;
		try {
			u = this.userDirectoryService.getUser(userId);
			if (u != null) {
				return true;
			}
		} catch (final UserNotDefinedException e) {
			log.info("User with id: " + userId + " does not exist : " + e.getClass() + " : " + e.getMessage());
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean checkForUserByEid(final String eid) {
		User u = null;
		try {
			u = this.userDirectoryService.getUserByEid(eid);
			if (u != null) {
				return true;
			}
		} catch (final UserNotDefinedException e) {
			log.info("User with eid: " + eid + " does not exist : " + e.getClass() + " : " + e.getMessage());
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSuperUser() {
		return this.securityService.isSuperUser();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAdminUser() {
		return StringUtils.equals(this.sessionManager.getCurrentSessionUserId(), UserDirectoryService.ADMIN_ID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSuperUserAndProxiedToUser(final String userId) {
		return (isSuperUser() && !StringUtils.equals(userId, getCurrentUserId()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUserType(final String userId) {
		String type = null;
		try {
			type = this.userDirectoryService.getUser(userId).getType();
		} catch (final UserNotDefinedException e) {
			log.debug("User with eid: " + userId + " does not exist : " + e.getClass() + " : " + e.getMessage());
		}
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getUserById(final String userId) {
		User u = null;
		try {
			u = this.userDirectoryService.getUser(userId);
		} catch (final UserNotDefinedException e) {
			log.debug("User with id: " + userId + " does not exist : " + e.getClass() + " : " + e.getMessage());
		}

		return u;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getUserQuietly(final String userId) {
		User u = null;
		try {
			u = this.userDirectoryService.getUser(userId);
		} catch (final UserNotDefinedException e) {
			// carry on, no log output. see javadoc for reason.
		}
		return u;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCurrentToolTitle() {
		final Tool tool = this.toolManager.getCurrentTool();
		if (tool != null) {
			return tool.getTitle();
		} else {
			return "Profile";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<User> getUsers(final List<String> userIds) {
		List<User> rval = new ArrayList<>();
		try {
			rval = this.userDirectoryService.getUsers(userIds);
		} catch (final Exception e) {
			// I've seen an NPE in the logs from this call...
		}
		return rval;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getUuids(final List<User> users) {
		final List<String> uuids = new ArrayList<String>();
		for (final User u : users) {
			uuids.add(u.getId());
		}
		return uuids;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SakaiPerson getSakaiPerson(final String userId) {

		SakaiPerson sakaiPerson = null;

		try {
			sakaiPerson = this.sakaiPersonManager.getSakaiPerson(userId, this.sakaiPersonManager.getUserMutableType());
		} catch (final Exception e) {
			log.error(
					"SakaiProxy.getSakaiPerson(): Couldn't get SakaiPerson for: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
		return sakaiPerson;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getSakaiPersonJpegPhoto(final String userId) {

		SakaiPerson sakaiPerson = null;
		byte[] image = null;

		try {
			// try normal user type
			sakaiPerson = this.sakaiPersonManager.getSakaiPerson(userId, this.sakaiPersonManager.getUserMutableType());
			if (sakaiPerson != null) {
				image = sakaiPerson.getJpegPhoto();
			}
			// if null try system user type as a profile might have been created with this type
			if (image == null) {
				sakaiPerson = this.sakaiPersonManager.getSakaiPerson(userId, this.sakaiPersonManager.getSystemMutableType());
				if (sakaiPerson != null) {
					image = sakaiPerson.getJpegPhoto();
				}
			}

		} catch (final Exception e) {
			log.error("SakaiProxy.getSakaiPersonJpegPhoto(): Couldn't get SakaiPerson Jpeg photo for: " + userId + " : " + e.getClass()
					+ " : " + e.getMessage());
		}
		return image;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSakaiPersonImageUrl(final String userId) {

		SakaiPerson sakaiPerson = null;
		String url = null;

		try {
			// try normal user type
			sakaiPerson = this.sakaiPersonManager.getSakaiPerson(userId, this.sakaiPersonManager.getUserMutableType());
			if (sakaiPerson != null) {
				url = sakaiPerson.getPictureUrl();
			}
			// if null try system user type as a profile might have been created with this type
			if (StringUtils.isBlank(url)) {
				sakaiPerson = this.sakaiPersonManager.getSakaiPerson(userId, this.sakaiPersonManager.getSystemMutableType());
				if (sakaiPerson != null) {
					url = sakaiPerson.getPictureUrl();
				}
			}

		} catch (final Exception e) {
			log.error("SakaiProxy.getSakaiPersonImageUrl(): Couldn't get SakaiPerson image URL for: " + userId + " : " + e.getClass()
					+ " : " + e.getMessage());
		}
		return url;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SakaiPerson getSakaiPersonPrototype() {

		SakaiPerson sakaiPerson = null;

		try {
			sakaiPerson = this.sakaiPersonManager.getPrototype();
		} catch (final Exception e) {
			log.error("SakaiProxy.getSakaiPersonPrototype(): Couldn't get SakaiPerson prototype: " + e.getClass() + " : " + e.getMessage());
		}
		return sakaiPerson;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SakaiPerson createSakaiPerson(final String userId) {

		SakaiPerson sakaiPerson = null;

		try {
			sakaiPerson = this.sakaiPersonManager.create(userId, this.sakaiPersonManager.getUserMutableType());
		} catch (final Exception e) {
			log.error("SakaiProxy.createSakaiPerson(): Couldn't create SakaiPerson: " + e.getClass() + " : " + e.getMessage());
		}
		return sakaiPerson;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean updateSakaiPerson(final SakaiPerson sakaiPerson) {
		// the save is void, so unless it throws an exception, its ok (?)
		// I'd prefer a return value from sakaiPersonManager. this wraps it.

		try {
			this.sakaiPersonManager.save(sakaiPerson);
			return true;
		} catch (final Exception e) {
			log.error("SakaiProxy.updateSakaiPerson(): Couldn't update SakaiPerson: " + e.getClass() + " : " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxProfilePictureSize() {
		return this.serverConfigurationService.getInt("profile2.picture.max", ProfileConstants.MAX_IMAGE_UPLOAD_SIZE);
	}

	private String getProfileGalleryPath(final String userId) {
		final String slash = Entity.SEPARATOR;

		final StringBuilder path = new StringBuilder();
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
	@Override
	public String getProfileGalleryImagePath(final String userId, final String imageId) {

		final StringBuilder path = new StringBuilder(getProfileGalleryPath(userId));

		path.append(ProfileConstants.GALLERY_IMAGE_MAIN);
		path.append(Entity.SEPARATOR);
		path.append(imageId);

		return path.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProfileGalleryThumbnailPath(final String userId, final String imageId) {
		final StringBuilder path = new StringBuilder(getProfileGalleryPath(userId));
		path.append(ProfileConstants.GALLERY_IMAGE_THUMBNAILS);
		path.append(Entity.SEPARATOR);
		path.append(imageId);

		return path.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProfileImageResourcePath(final String userId, final int type) {

		final String slash = Entity.SEPARATOR;

		final StringBuilder path = new StringBuilder();
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
	@Override
	public boolean saveFile(final String fullResourceId, final String userId, final String fileName, final String mimeType,
			final byte[] fileData) {

		ContentResourceEdit resource = null;
		boolean result = true;

		try {

			enableSecurityAdvisor();

			try {

				resource = this.contentHostingService.addResource(fullResourceId);
				resource.setContentType(mimeType);
				resource.setContent(fileData);
				final ResourceProperties props = resource.getPropertiesEdit();
				props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, mimeType);
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
				props.addProperty(ResourceProperties.PROP_CREATOR, userId);
				resource.getPropertiesEdit().set(props);
				this.contentHostingService.commitResource(resource, NotificationService.NOTI_NONE);
				result = true;
			} catch (final IdUsedException e) {
				this.contentHostingService.cancelResource(resource);
				log.error("SakaiProxy.saveFile(): id= " + fullResourceId + " is in use : " + e.getClass() + " : " + e.getMessage());
				result = false;
			} catch (final Exception e) {
				this.contentHostingService.cancelResource(resource);
				log.error("SakaiProxy.saveFile(): failed: " + e.getClass() + " : " + e.getMessage());
				result = false;
			}

		} catch (final Exception e) {
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
	@Override
	public MimeTypeByteArray getResource(final String resourceId) {

		final MimeTypeByteArray mtba = new MimeTypeByteArray();

		if (StringUtils.isBlank(resourceId)) {
			return null;
		}

		try {
			enableSecurityAdvisor();
			try {
				final ContentResource resource = this.contentHostingService.getResource(resourceId);
				if (resource == null) {
					return null;
				}
				mtba.setBytes(resource.getContent());
				mtba.setMimeType(resource.getContentType());
				return mtba;
			} catch (final Exception e) {
				log.error("SakaiProxy.getResource() failed for resourceId: " + resourceId + " : " + e.getClass() + " : " + e.getMessage());
			}
		} catch (final Exception e) {
			log.error("SakaiProxy.getResource():" + e.getClass() + ":" + e.getMessage());
		} finally {
			disableSecurityAdvisor();
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeResource(final String resourceId) {

		boolean result = false;

		try {
			enableSecurityAdvisor();

			this.contentHostingService.removeResource(resourceId);

			result = true;
		} catch (final Exception e) {
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
	@Override
	public List<User> searchUsers(final String search) {
		return this.userDirectoryService.searchUsers(search, ProfileConstants.FIRST_RECORD, ProfileConstants.MAX_RECORDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<User> searchExternalUsers(final String search) {
		return this.userDirectoryService.searchExternalUsers(search, ProfileConstants.FIRST_RECORD, ProfileConstants.MAX_RECORDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postEvent(final String event, final String reference, final boolean modify) {
		this.eventTrackingService.post(this.eventTrackingService.newEvent(event, reference, modify));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendEmail(final String userId, final String subject, final String message) {

		class EmailSender {
			private final String userId;
			private final String subject;
			private final String message;

			public final String MULTIPART_BOUNDARY = "======sakai-multi-part-boundary======";
			public final String BOUNDARY_LINE = "\n\n--" + this.MULTIPART_BOUNDARY + "\n";
			public final String TERMINATION_LINE = "\n\n--" + this.MULTIPART_BOUNDARY + "--\n\n";
			public final String MIME_ADVISORY = "This message is for MIME-compliant mail readers.";
			public final String PLAIN_TEXT_HEADERS = "Content-Type: text/plain\n\n";
			public final String HTML_HEADERS = "Content-Type: text/html; charset=ISO-8859-1\n\n";
			public final String HTML_END = "\n  </body>\n</html>\n";

			public EmailSender(final String userId, final String subject, final String message) {
				this.userId = userId;
				this.subject = subject;
				this.message = message;
			}

			// do it!
			public void send() {
				try {

					// get User to send to
					final User user = SakaiProxyImpl.this.userDirectoryService.getUser(this.userId);

					if (StringUtils.isBlank(user.getEmail())) {
						log.error("SakaiProxy.sendEmail() failed. No email for userId: " + this.userId);
						return;
					}

					// do it
					SakaiProxyImpl.this.emailService.sendToUsers(Collections.singleton(user), getHeaders(user.getEmail(), this.subject),
							formatMessage(this.subject, this.message));

					log.info("Email sent to: " + this.userId);
				} catch (final UserNotDefinedException e) {
					log.error("SakaiProxy.sendEmail() failed for userId: " + this.userId + " : " + e.getClass() + " : " + e.getMessage());
				}
			}

			/** helper methods for formatting the message */
			private String formatMessage(final String subject, final String message) {
				final StringBuilder sb = new StringBuilder();
				sb.append(this.MIME_ADVISORY);
				sb.append(this.BOUNDARY_LINE);
				sb.append(this.PLAIN_TEXT_HEADERS);
				sb.append(StringEscapeUtils.escapeHtml(message));
				sb.append(this.BOUNDARY_LINE);
				sb.append(this.HTML_HEADERS);
				sb.append(htmlPreamble(subject));
				sb.append(message);
				sb.append(this.HTML_END);
				sb.append(this.TERMINATION_LINE);

				return sb.toString();
			}

			private String htmlPreamble(final String subject) {
				final StringBuilder sb = new StringBuilder();
				sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n");
				sb.append("\"http://www.w3.org/TR/html4/loose.dtd\">\n");
				sb.append("<html>\n");
				sb.append("<head><title>");
				sb.append(subject);
				sb.append("</title></head>\n");
				sb.append("<body>\n");

				return sb.toString();
			}

			private List<String> getHeaders(final String emailTo, final String subject) {
				final List<String> headers = new ArrayList<String>();
				headers.add("MIME-Version: 1.0");
				headers.add("Content-Type: multipart/alternative; boundary=\"" + this.MULTIPART_BOUNDARY + "\"");
				headers.add(formatSubject(subject));
				headers.add(getFrom());
				if (StringUtils.isNotBlank(emailTo)) {
					headers.add("To: " + emailTo);
				}

				return headers;
			}

			private String getFrom() {
				final StringBuilder sb = new StringBuilder();
				sb.append("From: ");
				sb.append(getServiceName());
				sb.append(" <");
				sb.append(SakaiProxyImpl.this.serverConfigurationService.getString("setup.request", "no-reply@" + getServerName()));
				sb.append(">");

				return sb.toString();
			}

			private String formatSubject(final String subject) {
				final StringBuilder sb = new StringBuilder();
				sb.append("Subject: ");
				sb.append(subject);

				return sb.toString();
			}

		}

		// instantiate class to format, then send the mail
		new EmailSender(userId, subject, message).send();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendEmail(final List<String> userIds, final String emailTemplateKey, final Map<String, String> replacementValues) {

		// get list of Users
		final List<User> users = new ArrayList<User>(getUsers(userIds));

		// only ever use one thread whether sending to one user or many users
		final Thread sendMailThread = new Thread() {

			@Override
			public void run() {
				// get the rendered template for each user
				RenderedTemplate template = null;

				for (final User user : users) {
					log.info("SakaiProxy.sendEmail() attempting to send email to: " + user.getId());
					try {
						template = SakaiProxyImpl.this.emailTemplateService.getRenderedTemplateForUser(emailTemplateKey,
								user.getReference(), replacementValues);
						if (template == null) {
							log.error("SakaiProxy.sendEmail() no template with key: " + emailTemplateKey);
							return; // no template
						}
					} catch (final Exception e) {
						log.error("SakaiProxy.sendEmail() error retrieving template for user: " + user.getId() + " with key: "
								+ emailTemplateKey + " : " + e.getClass() + " : " + e.getMessage());
						continue; // try next user
					}

					// send
					sendEmail(user.getId(), template.getRenderedSubject(), template.getRenderedHtmlMessage());
				}
			}
		};
		sendMailThread.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendEmail(final String userId, final String emailTemplateKey, final Map<String, String> replacementValues) {
		sendEmail(Collections.singletonList(userId), emailTemplateKey, replacementValues);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getServerName() {
		return this.serverConfigurationService.getServerName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPortalUrl() {
		return this.serverConfigurationService.getPortalUrl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getServerUrl() {
		return this.serverConfigurationService.getServerUrl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFullPortalUrl() {
		return getServerUrl() + getPortalPath();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPortalPath() {
		return this.serverConfigurationService.getString("portalPath", "/portal");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUsingNormalPortal() {
		return StringUtils.equals(getPortalPath(), "/portal");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUserHomeUrl() {
		return this.serverConfigurationService.getUserHomeUrl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getServiceName() {
		return this.serverConfigurationService.getString("ui.service", ProfileConstants.SAKAI_PROP_SERVICE_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateEmailForUser(final String userId, final String email) {
		try {
			UserEdit userEdit = null;
			userEdit = this.userDirectoryService.editUser(userId);
			userEdit.setEmail(email);
			this.userDirectoryService.commitEdit(userEdit);

			log.info("User email updated for: " + userId);
		} catch (final Exception e) {
			log.error("SakaiProxy.updateEmailForUser() failed for userId: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateNameForUser(final String userId, final String firstName, final String lastName) {
		try {
			UserEdit userEdit = null;
			userEdit = this.userDirectoryService.editUser(userId);
			userEdit.setFirstName(firstName);
			userEdit.setLastName(lastName);
			this.userDirectoryService.commitEdit(userEdit);

			log.info("User name details updated for: " + userId);
		} catch (final Exception e) {
			log.error("SakaiProxy.updateNameForUser() failed for userId: " + userId + " : " + e.getClass() + " : " + e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public String getDirectUrlToUserProfile(final String userId, final String extraParams) {
		final String portalUrl = getFullPortalUrl();
		final String siteId = getUserMyWorkspace(userId);

		final ToolConfiguration toolConfig = getFirstInstanceOfTool(siteId, ProfileConstants.TOOL_ID);
		if (toolConfig == null) {
			// if the user doesn't have the Profile2 tool installed in their My Workspace,
			log.warn("SakaiProxy.getDirectUrlToUserProfile() failed to find " + ProfileConstants.TOOL_ID
					+ " installed in My Workspace for userId: " + userId);

			// just return a link to their My Workspace
			final StringBuilder url = new StringBuilder();
			url.append(portalUrl);
			url.append("/site/");
			url.append(siteId);
			return url.toString();

		}

		final String pageId = toolConfig.getPageId();
		final String placementId = toolConfig.getId();

		try {
			final StringBuilder url = new StringBuilder();
			url.append(portalUrl);
			url.append("/site/");
			url.append(siteId);
			url.append("/page/");
			url.append(pageId);
			// only if we have params to add
			if (StringUtils.isNotBlank(extraParams)) {
				url.append("?toolstate-");
				url.append(placementId);
				url.append("=");
				url.append(URLEncoder.encode(extraParams, "UTF-8"));
			}

			return url.toString();
		} catch (final Exception e) {
			log.error("SakaiProxy.getDirectUrl():" + e.getClass() + ":" + e.getMessage());
			return null;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDirectUrlToProfileComponent(final String userId, final String component, final Map<String, String> extraParams) {
		final String portalUrl = getFullPortalUrl();

		// this is for current user
		final String siteId = getUserMyWorkspace(getCurrentUserId());
		final ToolConfiguration toolConfig = getFirstInstanceOfTool(siteId, ProfileConstants.TOOL_ID);
		if (toolConfig == null) {
			// if the user doesn't have the Profile2 tool installed in their My Workspace,
			log.warn("SakaiProxy.getDirectUrlToProfileComponent() failed to find " + ProfileConstants.TOOL_ID
					+ " installed in My Workspace for userId: " + userId);

			// just return a link to their My Workspace
			final StringBuilder url = new StringBuilder();
			url.append(portalUrl);
			url.append("/site/");
			url.append(siteId);
			return url.toString();

		}

		final String placementId = toolConfig.getId();

		try {
			final StringBuilder url = new StringBuilder();
			url.append(portalUrl);
			url.append("/site/");
			url.append(siteId);
			url.append("/tool/");
			url.append(placementId);

			switch (component) {
				case "profile": {
					url.append("/profile/");
					break;
				}
				case "viewprofile": {
					url.append("/viewprofile/");
					url.append(userId);
					break;
				}
			}

			return url.toString();
		} catch (final Exception e) {
			log.error("SakaiProxy.getDirectUrlToProfileComponent():" + e.getClass() + ":" + e.getMessage());
			return null;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAccountUpdateAllowed(final String userId) {
		try {
			final UserEdit edit = this.userDirectoryService.editUser(userId);
			this.userDirectoryService.cancelEdit(edit);
			return true;
		} catch (final Exception e) {
			log.info("SakaiProxy.isAccountUpdateAllowed() false for userId: " + userId);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBusinessProfileEnabled() {
		return this.serverConfigurationService.getBoolean(
				"profile2.profile.business.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PROFILE_BUSINESS_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSocialProfileEnabled() {
		return this.serverConfigurationService.getBoolean(
				"profile2.profile.social.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PROFILE_SOCIAL_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInterestsProfileEnabled() {
		return this.serverConfigurationService.getBoolean(
				"profile2.profile.interests.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PROFILE_INTERESTS_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isStaffProfileEnabled() {
		return this.serverConfigurationService.getBoolean(
				"profile2.profile.staff.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PROFILE_STAFF_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isStudentProfileEnabled() {
		return this.serverConfigurationService.getBoolean(
				"profile2.profile.student.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PROFILE_STUDENT_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWallEnabledGlobally() {
		return this.serverConfigurationService.getBoolean(
				"profile2.wall.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_WALL_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWallDefaultProfilePage() {
		return this.serverConfigurationService.getBoolean(
				"profile2.wall.default",
				ProfileConstants.SAKAI_PROP_PROFILE2_WALL_DEFAULT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isProfileConversionEnabled() {
		return this.serverConfigurationService.getBoolean("profile2.convert", ProfileConstants.SAKAI_PROP_PROFILE2_CONVERSION_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isProfileImageImportEnabled() {
		return this.serverConfigurationService.getBoolean("profile2.import.images",
				ProfileConstants.SAKAI_PROP_PROFILE2_IMPORT_IMAGES_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isProfileImportEnabled() {
		return this.serverConfigurationService.getBoolean("profile2.import", ProfileConstants.SAKAI_PROP_PROFILE2_IMPORT_ENABLED);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProfileImportCsvPath() {
		return this.serverConfigurationService.getString("profile2.import.csv", null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTwitterIntegrationEnabledGlobally() {
		return this.serverConfigurationService.getBoolean("profile2.integration.twitter.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_TWITTER_INTEGRATION_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTwitterSource() {
		return this.serverConfigurationService.getString("profile2.integration.twitter.source",
				ProfileConstants.SAKAI_PROP_PROFILE2_TWITTER_INTEGRATION_SOURCE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isProfileGalleryEnabledGlobally() {
		if (!isMenuEnabledGlobally()) {
			return false;
		} else {
			return this.serverConfigurationService.getBoolean("profile2.gallery.enabled",
					ProfileConstants.SAKAI_PROP_PROFILE2_GALLERY_ENABLED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isProfilePictureChangeEnabled() {
		// PRFL-395: Ability to enable/disable profile picture change per user type
		final boolean globallyEnabled = this.serverConfigurationService.getBoolean("profile2.picture.change.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PICTURE_CHANGE_ENABLED);
		final String userType = getUserType(getCurrentUserId());
		// return user type specific setting, defaulting to global one
		return this.serverConfigurationService.getBoolean("profile2.picture.change." + userType + ".enabled", globallyEnabled);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getProfilePictureType() {
		final String pictureType = this.serverConfigurationService.getString("profile2.picture.type",
				ProfileConstants.PICTURE_SETTING_UPLOAD_PROP);

		// if 'upload'
		if (StringUtils.equals(pictureType, ProfileConstants.PICTURE_SETTING_UPLOAD_PROP)) {
			return ProfileConstants.PICTURE_SETTING_UPLOAD;
		}
		// if 'url'
		else if (StringUtils.equals(pictureType, ProfileConstants.PICTURE_SETTING_URL_PROP)) {
			return ProfileConstants.PICTURE_SETTING_URL;
		}
		// if 'official'
		else if (StringUtils.equals(pictureType, ProfileConstants.PICTURE_SETTING_OFFICIAL_PROP)) {
			return ProfileConstants.PICTURE_SETTING_OFFICIAL;
		}
		// gravatar is not an enforceable setting, hence no block here. it is purely a user preference.
		// but can be disabled

		// otherwise return default
		else {
			return ProfileConstants.PICTURE_SETTING_DEFAULT;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getAcademicEntityConfigurationSet() {

		final String configuration = this.serverConfigurationService.getString("profile2.profile.entity.set.academic",
				ProfileConstants.ENTITY_SET_ACADEMIC);
		final String[] parameters = StringUtils.split(configuration, ',');

		final List<String> tempList = Arrays.asList(parameters);
		final List<String> list = new ArrayList<String>(tempList);

		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getMinimalEntityConfigurationSet() {
		final String configuration = this.serverConfigurationService.getString("profile2.profile.entity.set.minimal",
				ProfileConstants.ENTITY_SET_MINIMAL);
		final String[] parameters = StringUtils.split(configuration, ',');

		final List<String> tempList = Arrays.asList(parameters);
		final List<String> list = new ArrayList<String>(tempList);

		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String ensureUuid(final String userId) {

		// check for userId
		try {
			final User u = this.userDirectoryService.getUser(userId);
			if (u != null) {
				return userId;
			}
		} catch (final UserNotDefinedException e) {
			// do nothing, this is fine, cotninue to next check
		}

		// check for eid
		try {
			final User u = this.userDirectoryService.getUserByEid(userId);
			if (u != null) {
				return u.getId();
			}
		} catch (final UserNotDefinedException e) {
			// do nothing, this is fine, continue
		}

		log.error("User: " + userId + " could not be found in any lookup by either id or eid");
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean currentUserMatchesRequest(final String userUuid) {

		// get current user
		final String currentUserUuid = getCurrentUserId();

		// check match
		if (StringUtils.equals(currentUserUuid, userUuid)) {
			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPrivacyChangeAllowedGlobally() {
		return this.serverConfigurationService.getBoolean("profile2.privacy.change.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PRIVACY_CHANGE_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HashMap<String, Object> getOverriddenPrivacySettings() {

		final HashMap<String, Object> props = new HashMap<String, Object>();
		props.put("profileImage", this.serverConfigurationService.getInt("profile2.privacy.default.profileImage",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_PROFILEIMAGE));
		props.put("basicInfo", this.serverConfigurationService.getInt("profile2.privacy.default.basicInfo",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_BASICINFO));
		props.put("contactInfo", this.serverConfigurationService.getInt("profile2.privacy.default.contactInfo",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_CONTACTINFO));
		props.put("staffInfo", this.serverConfigurationService.getInt("profile2.privacy.default.staffInfo",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_STAFFINFO));
		props.put("studentInfo", this.serverConfigurationService.getInt("profile2.privacy.default.studentInfo",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_STUDENTINFO));
		props.put("personalInfo", this.serverConfigurationService.getInt("profile2.privacy.default.personalInfo",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_PERSONALINFO));
		props.put("birthYear", this.serverConfigurationService.getBoolean("profile2.privacy.default.birthYear",
				ProfileConstants.DEFAULT_BIRTHYEAR_VISIBILITY));
		props.put("myFriends", this.serverConfigurationService.getInt("profile2.privacy.default.myFriends",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_MYFRIENDS));
		props.put("myStatus", this.serverConfigurationService.getInt("profile2.privacy.default.myStatus",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_MYSTATUS));
		props.put("myPictures", this.serverConfigurationService.getInt("profile2.privacy.default.myPictures",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_MYPICTURES));
		props.put("messages", this.serverConfigurationService.getInt("profile2.privacy.default.messages",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_MESSAGES));
		props.put("businessInfo", this.serverConfigurationService.getInt("profile2.privacy.default.businessInfo",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_BUSINESSINFO));
		props.put("myKudos", this.serverConfigurationService.getInt("profile2.privacy.default.myKudos",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_MYKUDOS));
		props.put("myWall",
				this.serverConfigurationService.getInt("profile2.privacy.default.myWall", ProfileConstants.DEFAULT_PRIVACY_OPTION_MYWALL));
		props.put("socialInfo", this.serverConfigurationService.getInt("profile2.privacy.default.socialInfo",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_SOCIALINFO));
		props.put("onlineStatus", this.serverConfigurationService.getInt("profile2.privacy.default.onlineStatus",
				ProfileConstants.DEFAULT_PRIVACY_OPTION_ONLINESTATUS));

		return props;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getInvisibleUsers() {
		final String config = this.serverConfigurationService.getString("profile2.invisible.users",
				ProfileConstants.SAKAI_PROP_INVISIBLE_USERS);
		return ProfileUtils.getListFromString(config, ProfileConstants.SAKAI_PROP_LIST_SEPARATOR);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isConnectionAllowedBetweenUserTypes(final String requestingUserType, final String targetUserType) {

		if (isSuperUser()) {
			return true;
		}

		final String configuration = this.serverConfigurationService
				.getString("profile2.allowed.connection.usertypes." + requestingUserType);
		if (StringUtils.isBlank(configuration)) {
			return true;
		}

		final String[] values = StringUtils.split(configuration, ',');
		final List<String> valueList = Arrays.asList(values);

		if (valueList.contains(targetUserType)) {
			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean toggleProfileLocked(final String userId, final boolean locked) {
		final SakaiPerson sp = getSakaiPerson(userId);
		if (sp == null) {
			return false;
		}
		sp.setLocked(locked);
		if (updateSakaiPerson(sp)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOfficialImageEnabledGlobally() {
		return this.serverConfigurationService.getBoolean("profile2.official.image.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_OFFICIAL_IMAGE_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUsingOfficialImageButAlternateSelectionEnabled() {

		if (isOfficialImageEnabledGlobally() &&
				getProfilePictureType() != ProfileConstants.PICTURE_SETTING_OFFICIAL &&
				isProfilePictureChangeEnabled()) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOfficialImageSource() {
		return this.serverConfigurationService.getString("profile2.official.image.source", ProfileConstants.OFFICIAL_IMAGE_SETTING_DEFAULT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOfficialImagesDirectory() {
		return this.serverConfigurationService.getString("profile2.official.image.directory", "/official-photos");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOfficialImagesFileSystemPattern() {
		return this.serverConfigurationService.getString("profile2.official.image.directory.pattern", "TWO_DEEP");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOfficialImageAttribute() {
		return this.serverConfigurationService.getString("profile2.official.image.attribute", ProfileConstants.USER_PROPERTY_JPEG_PHOTO);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createUuid() {
		return this.idManager.createUuid();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUserActive(final String userUuid) {
		return this.activityService.isUserActive(userUuid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getActiveUsers(final List<String> userUuids) {
		return this.activityService.getActiveUsers(userUuids);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getLastEventTimeForUser(final String userUuid) {
		return this.activityService.getLastEventTimeForUser(userUuid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Long> getLastEventTimeForUsers(final List<String> userUuids) {
		return this.activityService.getLastEventTimeForUsers(userUuids);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getServerConfigurationParameter(final String key, final String def) {
		return this.serverConfigurationService.getString(key, def);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUserMyWorkspace(final String siteId) {
		return this.siteService.isUserSite(siteId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUserAllowedInSite(final String userId, final String permission, final String siteId) {
		if (this.securityService.isSuperUser()) {
			return true;
		}
		String siteRef = siteId;
		if (siteId != null && !siteId.startsWith(SiteService.REFERENCE_ROOT)) {
			siteRef = SiteService.REFERENCE_ROOT + Entity.SEPARATOR + siteId;
		}
		if (this.securityService.unlock(userId, permission, siteRef)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean checkForSite(final String siteId) {
		return this.siteService.siteExists(siteId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxSearchResults() {
		return this.serverConfigurationService.getInt(
				"profile2.search.maxSearchResults",
				ProfileConstants.DEFAULT_MAX_SEARCH_RESULTS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxSearchResultsPerPage() {
		return this.serverConfigurationService.getInt(
				"profile2.search.maxSearchResultsPerPage",
				ProfileConstants.DEFAULT_MAX_SEARCH_RESULTS_PER_PAGE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isGravatarImageEnabledGlobally() {
		return this.serverConfigurationService.getBoolean("profile2.gravatar.image.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_GRAVATAR_IMAGE_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUserAllowedAddSite(final String userUuid) {
		return this.siteService.allowAddSite(userUuid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Site addSite(final String id, final String type) {
		Site site = null;
		try {
			site = this.siteService.addSite(id, type);
		} catch (final IdInvalidException e) {
			e.printStackTrace();
		} catch (final IdUsedException e) {
			e.printStackTrace();
		} catch (final PermissionException e) {
			e.printStackTrace();
		}

		return site;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean saveSite(final Site site) {
		try {
			this.siteService.save(site);
		} catch (final IdUnusedException e) {
			e.printStackTrace();
			return false;
		} catch (final PermissionException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Site getSite(final String siteId) {
		try {
			return this.siteService.getSite(siteId);
		} catch (final IdUnusedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Site> getUserSites() {
		return this.siteService.getSites(SelectionType.ACCESS, null, null, null, SortType.TITLE_ASC, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tool getTool(final String id) {
		return this.toolManager.getTool(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getToolsRequired(final String category) {
		return this.serverConfigurationService.getToolsRequired(category);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isGoogleIntegrationEnabledGlobally() {
		return this.serverConfigurationService.getBoolean("profile2.integration.google.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_GOOGLE_INTEGRATION_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLoggedIn() {
		return StringUtils.isNotBlank(getCurrentUserId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isProfileFieldsEnabled() {
		return this.serverConfigurationService.getBoolean("profile2.profile.fields.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PROFILE_FIELDS_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isProfileStatusEnabled() {
		return this.serverConfigurationService.getBoolean("profile2.profile.status.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PROFILE_STATUS_ENABLED);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMenuEnabledGlobally() {
		return this.serverConfigurationService.getBoolean("profile2.menu.enabled", ProfileConstants.SAKAI_PROP_PROFILE2_MENU_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isConnectionsEnabledGlobally() {
		if (!isMenuEnabledGlobally()) {
			return false;
		} else {
			return this.serverConfigurationService.getBoolean("profile2.connections.enabled",
					ProfileConstants.SAKAI_PROP_PROFILE2_CONNECTIONS_ENABLED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMessagingEnabledGlobally() {
		if (isConnectionsEnabledGlobally()) {
			return this.serverConfigurationService.getBoolean("profile2.messaging.enabled",
					ProfileConstants.SAKAI_PROP_PROFILE2_MESSAGING_ENABLED);
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSearchEnabledGlobally() {
		if (!isMenuEnabledGlobally()) {
			return false;
		} else {
			return this.serverConfigurationService.getBoolean("profile2.search.enabled",
					ProfileConstants.SAKAI_PROP_PROFILE2_SEARCH_ENABLED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPrivacyEnabledGlobally() {
		if (!isMenuEnabledGlobally()) {
			return false;
		} else {
			return this.serverConfigurationService.getBoolean("profile2.privacy.enabled",
					ProfileConstants.SAKAI_PROP_PROFILE2_PRIVACY_ENABLED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPreferenceEnabledGlobally() {
		if (!isMenuEnabledGlobally()) {
			return false;
		} else {
			return this.serverConfigurationService.getBoolean("profile2.preference.enabled",
					ProfileConstants.SAKAI_PROP_PROFILE2_PREFERENCE_ENABLED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMyKudosEnabledGlobally() {
		return this.serverConfigurationService.getBoolean("profile2.myKudos.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_MY_KUDOS_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOnlineStatusEnabledGlobally() {
		return this.serverConfigurationService.getBoolean("profile2.onlineStatus.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_ONLINE_STATUS_ENABLED);
	}

	// PRIVATE METHODS FOR SAKAIPROXY

	/**
	 * Setup a security advisor for this transaction
	 */
	private void enableSecurityAdvisor() {
		this.securityService.pushAdvisor(new SecurityAdvisor() {
			@Override
			public SecurityAdvice isAllowed(final String userId, final String function, final String reference) {
				return SecurityAdvice.ALLOWED;
			}
		});
	}

	/**
	 * Remove security advisor from the stack
	 */
	private void disableSecurityAdvisor() {
		this.securityService.popAdvisor();
	}

	/**
	 * Gets the siteId of the given user's My Workspace Generally ~userId but may not be
	 *
	 * @param userId
	 * @return
	 */
	private String getUserMyWorkspace(final String userId) {
		return this.siteService.getUserSiteId(userId);
	}

	/**
	 * Gets the ToolConfiguration of a page in a site containing a given tool
	 *
	 * @param siteId siteId
	 * @param toolId toolId ie sakai.profile2
	 * @return
	 */
	private ToolConfiguration getFirstInstanceOfTool(final String siteId, final String toolId) {
		try {
			return this.siteService.getSite(siteId).getToolForCommonId(toolId);
		} catch (final IdUnusedException e) {
			log.error("SakaiProxy.getFirstInstanceOfTool() failed for siteId: " + siteId + " and toolId: " + toolId);
			return null;
		}
	}

	/**
	 * init
	 */
	public void init() {
		log.info("Profile2 SakaiProxy init()");

		// process the email templates
		// the list is injected via Spring
		this.emailTemplateService.processEmailTemplates(this.emailTemplates);

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

	// INJECT OTHER RESOURCES
	@Setter

	private ArrayList<String> emailTemplates;

}
