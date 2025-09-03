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
package org.sakaiproject.profile2.api;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

/**
 * An interface for abstracting Sakai specific parts away from the main logic.
 *
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public interface SakaiProxy {

	/**
	 * Get current user id
	 *
	 * @return
	 */
	public String getCurrentUserId();

	/**
	 * Convert eid to internal userid
	 *
	 * @return
	 */
	public String getUserIdForEid(String eid);

	/**
	 * Get displayname of a given userid (internal id)
	 *
	 * @return
	 */
	public String getUserDisplayName(String userId);

	/**
	 * Get firstname of a given userid (internal id)
	 *
	 * @return
	 */
	public String getUserFirstName(String userId);

	/**
	 * Get lastname of a given userid (internal id)
	 *
	 * @return
	 */
	public String getUserLastName(String userId);

	/**
	 * Get email address for a given userid (internal id)
	 *
	 * @return
	 */
	public String getUserEmail(String userId);

	/**
	 * Is the current user a superUser? (anyone in admin realm)
	 *
	 * @return
	 */
	public boolean isSuperUser();

	/**
	 * Is the current user a superUser and are they performing an action on another user's profile?
	 *
	 * @param userId - userId of other user
	 * @return
	 */
	public boolean isSuperUserAndProxiedToUser(String userId);

	/**
	 * Is the current user viewing the site as another role via View Site As
	 *
	 * @return
	 */
	public boolean isUserRoleSwapped();

	/**
	 * Get the type of this user's account
	 *
	 * @param userId
	 * @return
	 */
	public String getUserType(String userId);

	/**
	 * Get a user
	 *
	 * @param userId internal user id
	 * @return
	 */
	public User getUserById(String userId);

	/**
	 * Get the User object for the given userId.
	 * <p>
	 * This will not log errors so that we can quietly use it to check if a User exists for a given profile, ie in a search result, for
	 * example.
	 * </p>
	 *
	 * @param userId
	 * @return
	 */
	public User getUserQuietly(String userId);

	/**
	 * Get a SakaiPerson for a user
	 *
	 * @param userId
	 * @return
	 */
	public SakaiPerson getSakaiPerson(String userId);

	/**
	 * Get a SakaiPerson prototype if they don't have a profile.
	 * <p>
	 * This is not persistable so should only be used for temporary views. Use createSakaiPerson if need persistable object for saving a
	 * profile.
	 *
	 * @param userId
	 * @return
	 */
	public SakaiPerson getSakaiPersonPrototype();

	/**
	 * Create a new persistable SakaiPerson object for a user
	 *
	 * @param userId
	 * @return
	 */
	public SakaiPerson createSakaiPerson(String userId);

	/**
	 * Update a SakaiPerson object in the db.
	 * <p>
	 * If you are doing this from the UI you should use the {@link ProfileLogic.updateUserProfile} method instead as it will also handle any
	 * email notifications that may be required.
	 *
	 * <p>
	 * We keep this because for direct actions like converting profiles etc we dont need email notifications.
	 *
	 * @param sakaiPerson
	 * @return
	 */
	public boolean updateSakaiPerson(SakaiPerson sakaiPerson);

	/**
	 * Get the location for a profileImage given the user and type
	 *
	 * @param userId
	 * @param type
	 * @return
	 */
	public String getProfileImageResourcePath(String userId, int type);

	/**
	 * Save a file to CHS
	 *
	 * @param fullResourceId
	 * @param userId
	 * @param fileName
	 * @param mimeType
	 * @param fileData
	 * @return
	 */
	public boolean saveFile(String fullResourceId, String userId, String fileName, String mimeType, byte[] fileData);

  	public boolean resourceExists(String resourceId);

	/**
	 * Retrieve a resource from ContentHosting with byte[] and mimetype
	 *
	 * @param resourceId the full resourceId of the file
	 */
	public MimeTypeByteArray getResource(String resourceId);

	/**
	 * Removes the specified resource.
	 *
	 * @param resourceId the ID of the resource to remove.
	 * @return <code>true</code> if the resource is successfully removed, <code>false</code> if the remove operation fails.
	 */
	public boolean removeResource(String resourceId);

	/**
	 * Post an event to Sakai
	 *
	 * @param event name of event
	 * @param reference reference
	 * @param modify true if something changed, false if just access
	 *
	 */
	public void postEvent(String event, String reference, boolean modify);

	/**
	 * Gets the serverUrl configuration parameter (http://sakai.lancs.ac.uk:8080)
	 *
	 * @return
	 */
	public String getServerUrl();

	/**
	 * Gets the full portal url by adding getServerUrl() and getPortalPath() together This WILL work outside the portal context so safe to
	 * use from an entityprovider
	 *
	 * @return
	 */
	public String getFullPortalUrl();

	/**
	 * Updates a user's email address If the user is a provided user (ie from LDAP) this will probably fail so only internal accounts can be
	 * updated.
	 *
	 * @param userId uuid of the user
	 * @param email
	 */
	public void updateEmailForUser(String userId, String email);

	/**
	 * Updates a user's name If the user is a provided user (ie from LDAP) this will probably fail so only internal accounts can be updated.
	 *
	 * @param userId uuid of the user
	 * @param email
	 */
	public void updateNameForUser(String userId, String firstName, String lastName);

	/**
	 * Check if a user is allowed to update their account. The User could come from LDAP so updates not allowed. This will check if any
	 * updates are allowed.
	 *
	 * Note userDirectoryService.allowUpdateUserEmail etc are NOT the right methods to use as they don't check if account updates are
	 * allowed, just if the user doing the update is allowed.
	 *
	 * @param userId
	 * @return
	 */
	public boolean isAccountUpdateAllowed(String userId);

	/**
	 * Is the profile2.picture.change.enabled flag set in sakai.properties? If not set, defaults to true
	 *
	 * <p>
	 * Depending on this setting, users will be able to upload their own image. This can be useful to disable for institutions which may
	 * provide official photos for students.
	 * </p>
	 *
	 * @return
	 */
	public boolean isProfilePictureChangeEnabled();

	/**
	 * Get the profile2.picture.type setting in sakai.properties
	 * <p>
	 * Possible values for the sakai property are 'upload', 'url' and 'official'. If not set, defaults to 'upload'.
	 * </p>
	 * <p>
	 * This returns an int which matches one of: ProfileConstants.PICTURE_SETTING_UPLOAD, ProfileConstants.PICTURE_SETTING_URL,
	 * ProfileConstants.PICTURE_SETTING_OFFICIAL.
	 * </p>
	 *
	 * <p>
	 * Depending on this setting, Profile2 will decide how it retrieves a user's profile image, and the method by which users can add their
	 * own image. ie by uploading their own image, providing a URL, or not at all (for official)
	 * </p>
	 *
	 * @return
	 */
	public int getProfilePictureType();

	/**
	 * Convenience method to ensure the given userId(eid or internal id) is returned as a valid uuid.
	 *
	 * <p>
	 * External integrations must pass input through this method, where the input can be either form, since all data is keyed on the
	 * internal user id.
	 *
	 * @param userId
	 * @return uuid or null
	 */
	public String ensureUuid(String userId);

	/**
	 * Toggle a profile's locked status.
	 *
	 * @param userId
	 * @param locked
	 * @return
	 */
	public boolean toggleProfileLocked(String userId, boolean locked);

	/**
	 * Is profile2.official.image.enabled true? If so, allow use of this image and preference.
	 *
	 * @return
	 */
	public boolean isOfficialImageEnabledGlobally();

	/**
	 * Checks if profile2.picture.type=official 
	 *
	 * @return
	 */
	public boolean isUsingOfficialImage();

	/**
	 * Gets the value of the profile2.official.image.source attribute from sakai.properties. If not set, defaults to
	 * ProfileConstants.OFFICIAL_SETTING_DEFAULT
	 *
	 * This should be specified if profile2.picture.type=official
	 *
	 * @return
	 */
	public String getOfficialImageSource();

	/**
	 * Gets the value of the profile2.official.image.directory attribute from sakai.properties. If not set, defaults to /official-photos
	 *
	 * This should be specified if profile2.picture.type=official
	 *
	 * @return The root directory where official images are stored
	 */
	public String getOfficialImagesDirectory();

	/**
	 * Get the value of the profile2.official.image.directory.pattern attribute from sakai.properties. If not set, defaults to TWO_DEEP.
	 *
	 * <br />
	 * Options:
	 * <ul>
	 * <li>ONE_DEEP = first letter of a user's eid, then a slash, then the eid suffixed by '.jpg'. For example BASEDIR/a/adrian.jpg,
	 * BASEDIR/s/steve.jpg</li>
	 * <li>TWO_DEEP = first letter of a user's eid, then a slash, then the second letter of the eid followed by a slash and finally the eid
	 * suffixed by '.jpg'. For example BASEDIR/a/d/adrian.jpg, BASEDIR/s/t/steve.jpg</li>
	 * <li>ALL_IN_ONE = all files in the one directory. For example BASEDIR/adrian.jpg, BASEDIR/steve.jpg</li>
	 * </ul>
	 *
	 * This is optional but if you have your images on the filesystem in a pattern that isnt default, you need to set a pattern.
	 *
	 * @return
	 */
	public String getOfficialImagesFileSystemPattern();

	/**
	 * Gets the value of the profile2.official.image.attribute from sakai.properties If not set, defaults to
	 * ProfileConstants.USER_PROPERTY_JPEG_PHOTO
	 *
	 * This should be specified if profile2.official.image.source=provided
	 *
	 * @return
	 */
	public String getOfficialImageAttribute();

	/**
	 * Generic method to get a configuration parameter from sakai.properties
	 *
	 * @param key key of property
	 * @param def default value
	 * @return value or default if none
	 */
	public String getServerConfigurationParameter(String key, String def);

	/**
	 * Check if specified site is a My Workspace site
	 *
	 * @param siteId id of site
	 * @return true if site is a My Workspace site, false otherwise.
	 */
	public boolean isUserMyWorkspace(String siteId);

	/**
	 * Generic method to check if user has permission in site
	 *
	 * @param userId userId of user to check permission
	 * @param permission the permission to check in site
	 * @param siteId site id
	 * @return
	 */
	public boolean isUserAllowedInSite(String userId, String permission, String siteId);

	/**
	 * Is the profile2.profile.social.enabled flag set in sakai.properties? If not set, defaults to true.
	 *
	 * @return <code>true</code> if the profile2.profile.social.enabled flag is set, otherwise returns <code>false</code>.
	 */
	public boolean isSocialProfileEnabled();

	/**
	 * Is the profile2.profile.name.pronunciation.enabled flag set in sakai.properties? If not set, defaults to true
	 *
	 * @return <code>true</code> if the profile2.profile.name.pronunciation.enabled flag is set, otherwise returns <code>false</code>.
	 */
	public boolean isNamePronunciationProfileEnabled();

	/**
	 * Returns the name pronunciation examples link
	 * @return the name pronunciation examples link, empty if it is not configured in sakai.properties
	 */
	public String getNamePronunciationExamplesLink();

	/**
	 * Returns the name pronunciation duration in seconds
	 * @return the name pronunciation duration in seconds. 10 seconds if it is not configured in sakai.properties
	 */
	public int getNamePronunciationDuration();

	/**
	 * Check if a user is member of a site
	 *
	 * @param userId userId of user to check membership
	 * @param siteId id of site
	 * @return true if the user is member of that site
	 */
	public boolean isUserMemberOfSite(String userId, String siteId);

	/**
	 * Check if two users have any site membership in common
	 *
	 * @param userId1 userId of user to check membership
	 * @param userId2 userId of user to check membership
	 * @return true if both users are members of one common site
	 */
	public boolean areUsersMembersOfSameSite(String userId1, String userId2);
}
