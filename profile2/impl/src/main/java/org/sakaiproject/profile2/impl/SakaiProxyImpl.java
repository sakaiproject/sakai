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
package org.sakaiproject.profile2.impl;

import java.util.ArrayList;
import java.util.List;

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
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.profile2.api.MimeTypeByteArray;
import org.sakaiproject.profile2.api.SakaiProxy;
import org.sakaiproject.profile2.api.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of SakaiProxy for Profile2.
 *
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
@Slf4j
public class SakaiProxyImpl implements SakaiProxy {

    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SiteService siteService;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private SakaiPersonManager sakaiPersonManager;
    @Autowired private ContentHostingService contentHostingService;
    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private ServerConfigurationService serverConfigurationService;

    @Override
    public String getCurrentUserId() {
        return sessionManager.getCurrentSessionUserId();
    }

    @Override
    public String getUserIdForEid(String eid) {

        try {
            return userDirectoryService.getUserByEid(eid).getId();
        } catch (UserNotDefinedException e) {
            log.warn("No user for eid {}", eid);
        }
        return null;
    }

    @Override
    public String getUserDisplayName(String userId) {

        try {
            return userDirectoryService.getUser(userId).getDisplayName();
        } catch (UserNotDefinedException e) {
            log.warn("Cannot get displayname for id {}: {}", userId, e.toString());
        }
        return null;
    }

    @Override
    public String getUserFirstName(String userId) {

        try {
            return this.userDirectoryService.getUser(userId).getFirstName();
        } catch (UserNotDefinedException e) {
            log.warn("Cannot get first name for id {} : {}", userId, e.toString());
        }
        return null;
    }

    @Override
    public String getUserLastName(String userId) {

        try {
            return userDirectoryService.getUser(userId).getLastName();
        } catch (UserNotDefinedException e) {
            log.warn("Cannot get last name for id {} : {}", userId, e.toString());
        }
        return null;
    }

    @Override
    public String getUserEmail(String userId) {

        try {
            return this.userDirectoryService.getUser(userId).getEmail();
        } catch (UserNotDefinedException e) {
            log.warn("Cannot get email for id {}: {}", userId, e.toString());
        }
        return null;
    }

    @Override
    public boolean isSuperUser() {
        return this.securityService.isSuperUser();
    }

    @Override
    public boolean isSuperUserAndProxiedToUser(String userId) {
        return (isSuperUser() && !StringUtils.equals(userId, getCurrentUserId()));
    }

    @Override
    public boolean isUserRoleSwapped() {
        return this.securityService.isUserRoleSwapped();
    }

    @Override
    public String getUserType(String userId) {

        try {
            return userDirectoryService.getUser(userId).getType();
        } catch (UserNotDefinedException e) {
            log.debug("User with eid {}: {}", userId, e.toString());
        }

        return null;
    }

    @Override
    public User getUserById(String userId) {

        try {
            return userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException e) {
            log.debug("User with id {}: {}", userId, e.toString());
        }

        return null;
    }

    @Override
    public User getUserQuietly(String userId) {

        try {
            return userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException e) {
            // carry on, no log output. see javadoc for reason.
        }
        return null;
    }

    @Override
    public SakaiPerson getSakaiPerson(String userId) {

        try {
            return sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getUserMutableType());
        } catch (Exception e) {
            log.error("Couldn't get SakaiPerson for userId {}: {}", userId, e.toString());
        }

        return null;
    }

    @Override
    public SakaiPerson getSakaiPersonPrototype() {

        try {
            return sakaiPersonManager.getPrototype();
        } catch (Exception e) {
            log.error("SakaiProxy.getSakaiPersonPrototype(): Couldn't get SakaiPerson prototype: {}", e.toString());
        }

        return null;
    }

    @Override
    public boolean updateSakaiPerson(SakaiPerson sakaiPerson) {
        // the save is void, so unless it throws an exception, its ok (?)
        // I'd prefer a return value from sakaiPersonManager. this wraps it.

        try {
            this.sakaiPersonManager.save(sakaiPerson);
            return true;
        } catch (Exception e) {
            log.error("SakaiProxy.updateSakaiPerson(): Couldn't update SakaiPerson: " + e.getClass() + " : " + e.getMessage());
        }
        return false;
    }

    @Override
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
            } catch (IdUsedException e) {
                this.contentHostingService.cancelResource(resource);
                log.error("SakaiProxy.saveFile(): id= {} is in use : {}", fullResourceId, e.toString());
                result = false;
            } catch (Exception e) {
                this.contentHostingService.cancelResource(resource);
                log.error("SakaiProxy.saveFile(): failed: {}", e.toString());
                result = false;
            }

        } catch (Exception e) {
            log.error("SakaiProxy.saveFile(): {}", e.toString());
            result = false;
        } finally {
            disableSecurityAdvisor();
        }
        return result;

    }

    @Override
    public boolean resourceExists(String resourceId) {

        try {
            enableSecurityAdvisor();
            return contentHostingService.getResource(resourceId) != null;
        } catch (IdUnusedException e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check if resource exists: {} {}", resourceId, e.toString());
        } finally {
            disableSecurityAdvisor();
        }

        return false;
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
                ContentResource resource = this.contentHostingService.getResource(resourceId);
                if (resource == null) {
                    return null;
                }
                mtba.setBytes(resource.getContent());
                mtba.setMimeType(resource.getContentType());
                return mtba;
            } catch (Exception e) {
                log.debug("SakaiProxy.getResource() failed for resourceId: {} : {}", resourceId, e.toString());
            }
        } catch (Exception e) {
            log.debug("SakaiProxy.getResource(): {}", e.toString());
        } finally {
            disableSecurityAdvisor();
        }

        return null;
    }

    @Override
    public boolean removeResource(String resourceId) {

        try {
            enableSecurityAdvisor();
            this.contentHostingService.removeResource(resourceId);
            return true;
        } catch (Exception e) {
            log.debug("Could not retrieve resource {}, {}", resourceId, e.toString());
            return false;
        } finally {
            disableSecurityAdvisor();
        }
    }

    @Override
    public void postEvent(String event, String reference, boolean modify) {
        this.eventTrackingService.post(this.eventTrackingService.newEvent(event, reference, modify));
    }

    @Override
    public String getServerUrl() {
        return this.serverConfigurationService.getServerUrl();
    }

    @Override
    public String getFullPortalUrl() {
        return getServerUrl() + this.serverConfigurationService.getString("portalPath", "/portal");
    }

    @Override
    public void updateEmailForUser(final String userId, final String email) {

        try {
            UserEdit userEdit = null;
            userEdit = this.userDirectoryService.editUser(userId);
            userEdit.setEmail(email);
            this.userDirectoryService.commitEdit(userEdit);

            log.info("User email updated for: {}", userId);
        } catch (Exception e) {
            log.error("SakaiProxy.updateEmailForUser() failed for userId {}: {}", userId, e.toString());
        }
    }

    @Override
    public void updateNameForUser(String userId, String firstName, String lastName) {

        try {
            UserEdit userEdit = null;
            userEdit = this.userDirectoryService.editUser(userId);
            userEdit.setFirstName(firstName);
            userEdit.setLastName(lastName);
            this.userDirectoryService.commitEdit(userEdit);

            log.info("User name details updated for: {}", userId);
        } catch (Exception e) {
            log.error("SakaiProxy.updateNameForUser() failed for userId {} : {}", userId, e.toString());
        }
    }

    @Override
    public boolean isAccountUpdateAllowed(String userId) {

        try {
            UserEdit edit = userDirectoryService.editUser(userId);
            userDirectoryService.cancelEdit(edit);
            return true;
        } catch (Exception e) {
            log.info("SakaiProxy.isAccountUpdateAllowed() false for userId {}", userId);
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
        boolean globallyEnabled = this.serverConfigurationService.getBoolean("profile2.picture.change.enabled",
                ProfileConstants.SAKAI_PROP_PROFILE2_PICTURE_CHANGE_ENABLED);
        String userType = getUserType(getCurrentUserId());
        // return user type specific setting, defaulting to global one
        return this.serverConfigurationService.getBoolean("profile2.picture.change." + userType + ".enabled", globallyEnabled);
    }

    @Override
    public int getProfilePictureType() {
        String pictureType = this.serverConfigurationService.getString("profile2.picture.type",
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
        // otherwise return default
        else {
            return ProfileConstants.PICTURE_SETTING_DEFAULT;
        }
    }

    @Override
    public String ensureUuid(final String userId) {

        // check for userId
        try {
            final User u = this.userDirectoryService.getUser(userId);
            if (u != null) {
                return userId;
            }
        } catch (UserNotDefinedException e) {
            // do nothing, this is fine, cotninue to next check
        }

        // check for eid
        try {
            return userDirectoryService.getUserByEid(userId).getId();
        } catch (UserNotDefinedException e) {
            // do nothing, this is fine, continue
        }

        log.error("User {} could not be found in any lookup by either id or eid", userId);
        return null;
    }

    @Override
    public boolean toggleProfileLocked(String userId, boolean locked) {

        SakaiPerson sp = getSakaiPerson(userId);
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
    public String getServerConfigurationParameter(final String key, final String def) {
        return this.serverConfigurationService.getString(key, def);
    }

    @Override
    public boolean isUserMyWorkspace(final String siteId) {
        return this.siteService.isUserSite(siteId);
    }

    @Override
    public boolean isUserAllowedInSite(String userId, String permission, String siteId) {
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

    // PRIVATE METHODS FOR SAKAIPROXY

    /**
     * Setup a security advisor for this transaction
     */
    private void enableSecurityAdvisor() {

        securityService.pushAdvisor(new SecurityAdvisor() {
            @Override
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
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

    @Override
    public String getNamePronunciationExamplesLink() {
        return serverConfigurationService.getString("profile2.profile.name.pronunciation.examples.link", "");
    }

    @Override
    public int getNamePronunciationDuration() {
        return serverConfigurationService.getInt("profile2.profile.name.pronunciation.duration", 10);
    }

    @Override
    public boolean isUserMemberOfSite(String userId, String siteId){

        try {
            return siteService.getSite(siteId).getUserRole(userId) != null;
        } catch (IdUnusedException e) {
            return false;
        }
    }

    @Override
    public boolean areUsersMembersOfSameSite(String userId1, String userId2) {

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
}
