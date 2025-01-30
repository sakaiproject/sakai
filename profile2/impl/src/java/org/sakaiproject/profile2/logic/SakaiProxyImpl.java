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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
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
import org.sakaiproject.util.ResourceLoader;

/**
 * Implementation of SakaiProxy for Profile2.
 *
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
@Slf4j
public class SakaiProxyImpl implements SakaiProxy {

	private static ResourceLoader rb = new ResourceLoader("ProfileApplication");

	@Override
	public String getCurrentSiteId() {
		return this.toolManager.getCurrentPlacement().getContext();
	}

	@Override
	public String getCurrentUserId() {
		return this.sessionManager.getCurrentSessionUserId();
	}

	@Override
	public User getCurrentUser() {
		return this.userDirectoryService.getCurrentUser();
	}

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

	@Override
	public boolean isSuperUser() {
		return this.securityService.isSuperUser();
	}

	@Override
	public boolean isSuperUserAndProxiedToUser(final String userId) {
		return (isSuperUser() && !StringUtils.equals(userId, getCurrentUserId()));
	}

	@Override
	public boolean isUserRoleSwapped() {
		return this.securityService.isUserRoleSwapped();
	}

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

	@Override
	public String getCurrentToolTitle() {
		final Tool tool = this.toolManager.getCurrentTool();
		if (tool != null) {
			return tool.getTitle();
		} else {
			return "Profile";
		}
	}

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

	@Override
	public List<String> getUuids(final List<User> users) {
		final List<String> uuids = new ArrayList<String>();
		for (final User u : users) {
			uuids.add(u.getId());
		}
		return uuids;
	}

	@Override
	public SakaiPerson getSakaiPerson(final String userId) {

		SakaiPerson sakaiPerson = null;

		try {
			sakaiPerson = this.sakaiPersonManager.getSakaiPerson(userId, this.sakaiPersonManager.getUserMutableType());
		} catch (Exception e) {
			log.error("Couldn't get SakaiPerson for userId {}: ", userId, e.toString());
		}
		return sakaiPerson;
	}

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

	@Override
	public boolean updateSakaiPerson(final SakaiPerson sakaiPerson) {
		// the save is void, so unless it throws an exception, its ok (?)
		// I'd prefer a return value from sakaiPersonManager. this wraps it.

		try {
			this.sakaiPersonManager.save(sakaiPerson);
			return true;
		} catch (final Exception e) {
			log.error("SakaiProxy.updateSakaiPerson(): Couldn't update SakaiPerson: " + e.getClass() + " : " + e.getMessage());
		}
		return false;
	}

	@Override
	public int getMaxProfilePictureSize() {
		return this.serverConfigurationService.getInt("profile2.picture.max", ProfileConstants.MAX_IMAGE_UPLOAD_SIZE);
	}

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
				log.debug("SakaiProxy.getResource() failed for resourceId: {} : {} : {}", resourceId, e.getClass(), e.getMessage());
			}
		} catch (final Exception e) {
			log.debug("SakaiProxy.getResource(): {} : {}", e.getClass(), e.getMessage());
		} finally {
			disableSecurityAdvisor();
		}

		return null;
	}

	@Override
	public boolean removeResource(final String resourceId) {
		try {
			enableSecurityAdvisor();
			this.contentHostingService.removeResource(resourceId);
			return true;
		} catch (Exception e) {
			log.debug("Could not retrieve resource {}, {}", resourceId, e.getMessage());
			return false;
		} finally {
			disableSecurityAdvisor();
		}
	}

	@Override
	public void postEvent(final String event, final String reference, final boolean modify) {
		this.eventTrackingService.post(this.eventTrackingService.newEvent(event, reference, modify));
	}

	@Override
	public String getServerName() {
		return this.serverConfigurationService.getServerName();
	}

	@Override
	public String getPortalUrl() {
		return this.serverConfigurationService.getPortalUrl();
	}

	@Override
	public String getServerUrl() {
		return this.serverConfigurationService.getServerUrl();
	}

	@Override
	public String getFullPortalUrl() {
		return getServerUrl() + getPortalPath();
	}

	@Override
	public String getPortalPath() {
		return this.serverConfigurationService.getString("portalPath", "/portal");
	}

	@Override
	public boolean isUsingNormalPortal() {
		return StringUtils.equals(getPortalPath(), "/portal");
	}

	@Override
	public String getUserHomeUrl() {
		return this.serverConfigurationService.getUserHomeUrl();
	}

	@Override
	public String getServiceName() {
		return this.serverConfigurationService.getString("ui.service", ProfileConstants.SAKAI_PROP_SERVICE_NAME);
	}

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

	@Override
	public String getDirectUrlToProfileComponent(final String userId, final String component, final Map<String, String> extraParams) {
	    return getDirectUrlToProfileComponent(getCurrentUserId(), userId, component, extraParams);
	}

	@Override
	public String getDirectUrlToProfileComponent(final String viewerUuid, final String viewedUuid, final String component, final Map<String, String> extraParams) {

		final String portalUrl = getFullPortalUrl();

		// this is for the viewer
		final String siteId = getUserMyWorkspace(viewerUuid);
		final ToolConfiguration toolConfig = getFirstInstanceOfTool(siteId, ProfileConstants.TOOL_ID);
		if (toolConfig == null) {
			// if the user doesn't have the Profile2 tool installed in their My Workspace,
			log.warn("SakaiProxy.getDirectUrlToProfileComponent() failed to find " + ProfileConstants.TOOL_ID
					+ " installed in My Workspace for userId: " + viewerUuid);

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
					url.append(viewedUuid);
					break;
				}
			}

			return url.toString();
		} catch (final Exception e) {
			log.error("SakaiProxy.getDirectUrlToProfileComponent():" + e.getClass() + ":" + e.getMessage());
			return null;
		}
	}

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

	@Override
	public boolean isSocialProfileEnabled() {
		return this.serverConfigurationService.getBoolean(
				"profile2.profile.social.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PROFILE_SOCIAL_ENABLED);
	}

	@Override
	public boolean isNamePronunciationProfileEnabled() {
		return this.serverConfigurationService.getBoolean(
				"profile2.profile.name.pronunciation.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PROFILE_PRONUNCIATION_ENABLED);
	}

	@Override
	public boolean isProfilePictureChangeEnabled() {
		// PRFL-395: Ability to enable/disable profile picture change per user type
		final boolean globallyEnabled = this.serverConfigurationService.getBoolean("profile2.picture.change.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PICTURE_CHANGE_ENABLED);
		final String userType = getUserType(getCurrentUserId());
		// return user type specific setting, defaulting to global one
		return this.serverConfigurationService.getBoolean("profile2.picture.change." + userType + ".enabled", globallyEnabled);
	}

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

	@Override
	public List<String> getAcademicEntityConfigurationSet() {

		final String configuration = this.serverConfigurationService.getString("profile2.profile.entity.set.academic",
				ProfileConstants.ENTITY_SET_ACADEMIC);
		final String[] parameters = StringUtils.split(configuration, ',');

		final List<String> tempList = Arrays.asList(parameters);
		final List<String> list = new ArrayList<String>(tempList);

		return list;
	}

	@Override
	public List<String> getMinimalEntityConfigurationSet() {
		final String configuration = this.serverConfigurationService.getString("profile2.profile.entity.set.minimal",
				ProfileConstants.ENTITY_SET_MINIMAL);
		final String[] parameters = StringUtils.split(configuration, ',');

		final List<String> tempList = Arrays.asList(parameters);
		final List<String> list = new ArrayList<String>(tempList);

		return list;
	}

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

	@Override
	public List<String> getInvisibleUsers() {
		final String config = this.serverConfigurationService.getString("profile2.invisible.users",
				ProfileConstants.SAKAI_PROP_INVISIBLE_USERS);
		return ProfileUtils.getListFromString(config, ProfileConstants.SAKAI_PROP_LIST_SEPARATOR);
	}

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

	@Override
	public boolean isOfficialImageEnabledGlobally() {
		return this.serverConfigurationService.getBoolean("profile2.official.image.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_OFFICIAL_IMAGE_ENABLED);
	}

	@Override
	public boolean isUsingOfficialImage() {

		return getProfilePictureType() == ProfileConstants.PICTURE_SETTING_OFFICIAL;
	}

	@Override
	public boolean isUsingOfficialImageButAlternateSelectionEnabled() {

		if (isOfficialImageEnabledGlobally() &&
				getProfilePictureType() != ProfileConstants.PICTURE_SETTING_OFFICIAL &&
				isProfilePictureChangeEnabled()) {
			return true;
		}
		return false;
	}

	@Override
	public String getOfficialImageSource() {
		return this.serverConfigurationService.getString("profile2.official.image.source", ProfileConstants.OFFICIAL_IMAGE_SETTING_DEFAULT);
	}

	@Override
	public String getOfficialImagesDirectory() {
		return this.serverConfigurationService.getString("profile2.official.image.directory", "/official-photos");
	}

	@Override
	public String getOfficialImagesFileSystemPattern() {
		return this.serverConfigurationService.getString("profile2.official.image.directory.pattern", "TWO_DEEP");
	}

	@Override
	public String getOfficialImageAttribute() {
		return this.serverConfigurationService.getString("profile2.official.image.attribute", ProfileConstants.USER_PROPERTY_JPEG_PHOTO);
	}

	@Override
	public String createUuid() {
		return this.idManager.createUuid();
	}

	@Override
	public boolean isUserActive(final String userUuid) {
		return this.activityService.isUserActive(userUuid);
	}

	@Override
	public List<String> getActiveUsers(final List<String> userUuids) {
		return this.activityService.getActiveUsers(userUuids);
	}

	@Override
	public Long getLastEventTimeForUser(final String userUuid) {
		return this.activityService.getLastEventTimeForUser(userUuid);
	}

	@Override
	public Map<String, Long> getLastEventTimeForUsers(final List<String> userUuids) {
		return this.activityService.getLastEventTimeForUsers(userUuids);
	}

	@Override
	public String getServerConfigurationParameter(final String key, final String def) {
		return this.serverConfigurationService.getString(key, def);
	}

	@Override
	public boolean isUserMyWorkspace(final String siteId) {
		return this.siteService.isUserSite(siteId);
	}

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

	@Override
	public boolean checkForSite(final String siteId) {
		return this.siteService.siteExists(siteId);
	}

	@Override
	public boolean isGravatarImageEnabledGlobally() {
		return this.serverConfigurationService.getBoolean("profile2.gravatar.image.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_GRAVATAR_IMAGE_ENABLED);
	}

	@Override
	public boolean isUserAllowedAddSite(final String userUuid) {
		return this.siteService.allowAddSite(userUuid);
	}

	@Override
	public Site addSite(final String id, final String type) {
		Site site = null;
		try {
			site = this.siteService.addSite(id, type);
		} catch (final IdInvalidException e) {
			log.error(e.getMessage(), e);
		} catch (final IdUsedException e) {
			log.error(e.getMessage(), e);
		} catch (final PermissionException e) {
			log.error(e.getMessage(), e);
		}

		return site;
	}

	@Override
	public boolean saveSite(final Site site) {
		try {
			this.siteService.save(site);
		} catch (final IdUnusedException e) {
			log.error(e.getMessage(), e);
			return false;
		} catch (final PermissionException e) {
			log.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	@Override
	public Site getSite(final String siteId) {
		try {
			return this.siteService.getSite(siteId);
		} catch (final IdUnusedException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public List<Site> getUserSites() {
		return this.siteService.getSites(SelectionType.ACCESS, null, null, null, SortType.TITLE_ASC, null);
	}

	@Override
	public Tool getTool(final String id) {
		return this.toolManager.getTool(id);
	}

	@Override
	public List<String> getToolsRequired(final String category) {
		return this.serverConfigurationService.getToolsRequired(category);
	}

	@Override
	public boolean isGoogleIntegrationEnabledGlobally() {
		return this.serverConfigurationService.getBoolean("profile2.integration.google.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_GOOGLE_INTEGRATION_ENABLED);
	}

	@Override
	public boolean isLoggedIn() {
		return StringUtils.isNotBlank(getCurrentUserId());
	}

	@Override
	public boolean isProfileFieldsEnabled() {
		return this.serverConfigurationService.getBoolean("profile2.profile.fields.enabled",
				ProfileConstants.SAKAI_PROP_PROFILE2_PROFILE_FIELDS_ENABLED);
	}

	@Override
	public boolean isMenuEnabledGlobally() {
		return this.serverConfigurationService.getBoolean("profile2.menu.enabled", ProfileConstants.SAKAI_PROP_PROFILE2_MENU_ENABLED);
	}

	@Override
	public boolean isPreferenceEnabledGlobally() {
		if (!isMenuEnabledGlobally()) {
			return false;
		} else {
			return this.serverConfigurationService.getBoolean("profile2.preference.enabled",
					ProfileConstants.SAKAI_PROP_PROFILE2_PREFERENCE_ENABLED);
		}
	}

	@Override
	public SiteService.SiteTitleValidationStatus validateSiteTitle(String orig, String stripped) {
		return this.siteService.validateSiteTitle(orig, stripped);
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

	@Override
	public String getNamePronunciationExamplesLink() {
		return this.serverConfigurationService.getString("profile2.profile.name.pronunciation.examples.link", "");
	}

	@Override
	public int getNamePronunciationDuration() {
		return this.serverConfigurationService.getInt("profile2.profile.name.pronunciation.duration", 10);
	}

	@Override
	public boolean isUserMemberOfSite(final String userId, final String siteId){
		try {
			return this.siteService.getSite(siteId).getUserRole(userId) != null;
		} catch (IdUnusedException e) {
			return false;
		}
	}

	@Override
	public boolean areUsersMembersOfSameSite(final String userId1, final String userId2){
		if (StringUtils.equals(userId1, userId2)) {
			return true;
		}
		
		try {
			List<Site> sitesUser1 = siteService.getUserSites(false, userId1);
			List<Site> sitesUser2 = siteService.getUserSites(false, userId2);
			List<Site> coincidences = new ArrayList<>(sitesUser1);
			coincidences.retainAll(sitesUser2);
			return coincidences.size() > 0;
		} catch (Exception ex) {
			return false;
		}
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
	private ServerConfigurationService serverConfigurationService;

	@Setter
	private IdManager idManager;

	@Setter
	private ActivityService activityService;
}
