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

public interface ProfileService {

    /**
     * Get a UserProfile for the given userUuid
     *
     * <p>All users have profiles, even if they haven't filled it in yet.
     * At a very minimum it will contain their name. Privacy checks will determine visibility of other fields</p>
     *
     * <p>You must be logged-in in order to make requests to this method as the content returned will be tailored
     * to be visible for the currently logged in user.</p>
     *
     *
     * @param userUuid      uuid of the user to retrieve the profile for
     * @return UserProfile  for the user, that is visible to the requesting user, or null if the user does not exist.
     */
    ProfileTransferBean getUserProfile(String userUuid);

    /**
     * Get a UserProfile for the given userUuid
     *
     * <p>All users have profiles, even if they haven't filled it in yet.
     * At a very minimum it will contain their name. Privacy checks will determine visibility of other fields</p>
     *
     * <p>You must be logged-in in order to make requests to this method as the content returned will be tailored
     * to be visible for the currently logged in user.</p>
     *
     *
     * @param userUuid      uuid of the user to retrieve the profile for
     * @param siteId        a site id to check permissions against. Occasionally, site persmissions like roster.viewemail
     *                      need to override profile2 permissions.
     * @return UserProfile  for the user, that is visible to the requesting user, or null if the user does not exist.
     */
    ProfileTransferBean getUserProfile(String userUuid, String siteId);

    /**
     * Persist a SakaiPerson object and send an email notification, if required.
     *
     * <p>Note that this may eventually change to UserProfile, when SakaiPerson is reimplemented.
     *
     * @param sp    SakaiPerson obj
     * @return
     */
    boolean saveUserProfile(ProfileTransferBean profileBean);

    /**
     * Check if a user has a pronunciation recording. This call does not get the actual bytes, it
     * just checks if the resource exists.
     *
     * @param userId The user to check for
     * */
    boolean hasPronunciationRecording(String userId);

    boolean removePronunciationRecording(String userId);

    /**
     * Get the bytes of user pronunciation ogg audio
     * @param uuid The user id
     * @return MimeTypeByteArray of ogg audio
     */
    MimeTypeByteArray getUserNamePronunciation(String uuid);

    /**
     * Get the blank profile image, the one a user sees if there is
     * no other image available.
     */
    ProfileImage getBlankProfileImage();

    /**
     * Get the profile image for a user. Takes into account all global settings, user preferences .
     *
     * <p>If making a request for your own image</p>
     * <ul>
     *  <li>You should provide 'prefs' (if available) otherwise it will be looked up.</li>
     * </ul>
     *
     * <p>If making a request for someone else's image</p>
     * <ul>
     *  <li>You should provide the preferences object for that user (if available), otherwise it will be looked up.</li>
     *  <li>If preferences is still null, the global preference will be used, which may not exist and therefore be default.</li>
     * </ul>
     *
     * <p>The returned ProfileImage object is a wrapper around all of the types of image that can be set. use the getBinarty and getUrl() methods on this object to get the data.
     * See the docs on ProfileImage for how to use this.
     *
     * @param userUuid
     * @param prefs
     * @param size
     * @return
     */
    ProfileImage getProfileImage(String userUuid, int size);

    /**
     * Gets the official profile image for a user.
     * @param userUuid
     * @param siteId siteId to check that the requesting user has roster.viewofficialphoto permission
     * @return The ProfileImage object, populated with either a url or binary data.
     */
    ProfileImage getOfficialProfileImage(String userUuid, String siteId);

    /**
     * Get the profile image for a user. Takes into account all global settings, user preferences and permissions in the given site.
     * @param userUuid
     * @param prefs
     * @param size
     * @param siteId - optional siteid to check if the current user has permissions in this site to see the target user's image (PRFL-411)
     * @return
     */
    ProfileImage getProfileImage(String userUuid, int size, String siteId);

    /**
     * Get the profile image for a user.
     * @param person    Person object that contains all info about a user
     * @param size      size of image to return.
     * @return
     */
    ProfileImage getProfileImage(Person person, int size);

    /**
     * Get the profile image for a user.
     * @param person    Person object that contains all info about a user
     * @param size      size of image to return.
     * @param siteId - optional siteid to check if the current user has permissions in this site to see the target user's image (PRFL-411)
     * @return
     */
    ProfileImage getProfileImage(Person person, int size, String siteId);

    /**
     * Update the profile image for a user given the byte[] of the image.
     * <p>Will work, but not have visible effect if the setting for the image type used in sakai.properties is not upload. ie its using URL instead
     *
     * @param userUuid - uuid for the user
     * @param imageBytes - byte[] of image
     * @param mimeType - mimetype of image, ie image/jpeg
     * @param filename - name of file, used by ContentHosting, optional.
     * @return
     */
    boolean setProfileImage(String userUuid, byte[] imageBytes, String mimeType, String fileName);

    /**
     * Save the official image url that institutions can set.
     * @param userUuid      uuid of the user
     * @param url           url to image
     * @return
     */
    boolean saveOfficialImageUrl(final String userUuid, final String url);

    /**
     * Get the entity url to a profile image for a user.
     *
     * It can be added to any profile without checks as the retrieval of the image does the checks, and a default image
     * is used if not allowed or none available.
     *
     * @param userUuid  uuid for the user
     * @param size      size of image, from ProfileConstants
     */
    String getProfileImageEntityUrl(String userUuid, int size);

    /**
     * Remove the profile image for a user
     *
     * @param userUuid uuid for the user
     * @return
     */
    boolean removeProfileImage(String userUuid);

    /**
     * Does this use have a default profile image?
     *
     * @param userUuid uuid for the user
     * @return
     */
    boolean profileImageIsDefault(final String userUuid);

    /**
     * Generate a profile image for a user with his name/last name initials
     * @param userUuid uuid for the user
     * @return The ProfileImage object, populated with either a url or binary data.
     */
    ProfileImage getProfileAvatarInitials(final String userUuid);

    String getProfileImageURL(String userId, boolean thumbnail);
}
