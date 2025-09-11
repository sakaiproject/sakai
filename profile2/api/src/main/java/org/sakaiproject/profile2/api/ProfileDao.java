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

import java.util.List;
import java.util.Optional;

import org.sakaiproject.profile2.api.model.ProfileImageOfficial;
import org.sakaiproject.profile2.api.model.ProfileImageUploaded;
import org.sakaiproject.profile2.api.model.SocialNetworkingInfo;

/**
 * Internal DAO Interface for Profile2.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface ProfileDao {

    /**
     * Get the current ProfileImage record from the database.
     * There should only ever be one, but if there are more this will return the latest.
     * This is called when retrieving a profile image for a user. When adding a new image, there is a call
     * to a private method called getCurrentProfileImageRecords() which should invalidate any multiple current images
     *
     * @param userId        userId of the user
     */
    ProfileImageUploaded getProfileImage(String userId);

    /**
     * @param profileImage  ProfileImageUploaded obj
     */
    boolean saveProfileImage(ProfileImageUploaded profileImage);

    /**
     * Invalidate the current profile image for a user.
     *
     * @param userUuid  the uuid for the user
     */
    boolean removeProfileImage(String userUuid);

    /**
     * Get the ProfileImageOfficial record from the database for the given user
     * @param userUuid      uuid of the user
     * @return
     */
    ProfileImageOfficial getOfficialImage(String userUuid);

    /**
     * Save the ProfileImageOfficial record the database
     * @param officialImage     ProfileImageOfficial object
     * @return
     */
    boolean saveOfficialImage(ProfileImageOfficial officialImage);

    /**
     * Get a SocialNetworkingInfo record for a user
     * @param userId        uuid of the user
     * @return
     */
    Optional<SocialNetworkingInfo> getSocialNetworkingInfo(String userId);

    /**
     * Save a SocialNetworkingInfo record
     * @param socialNetworkingInfo  SocialNetworkingInfo object
     * @return
     */
    boolean saveSocialNetworkingInfo(SocialNetworkingInfo socialNetworkingInfo);
}
