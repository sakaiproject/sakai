/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.osedu.org/licenses/ECL-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.impl;

import java.util.Optional;

import org.sakaiproject.profile2.api.ProfileDao;
import org.sakaiproject.profile2.api.model.ProfileImageOfficial;
import org.sakaiproject.profile2.api.model.ProfileImageUploaded;
import org.sakaiproject.profile2.api.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.api.repository.ProfileImageOfficialRepository;
import org.sakaiproject.profile2.api.repository.ProfileImageUploadedRepository;
import org.sakaiproject.profile2.api.repository.SocialNetworkingInfoRepository;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Internal DAO Interface for Profile2
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Setter
@Slf4j
public class ProfileDaoImpl implements ProfileDao {

    @Autowired private ProfileImageUploadedRepository profileImageUploadedRespository;
    @Autowired private ProfileImageOfficialRepository profileImageOfficialRespository;
    @Autowired private SocialNetworkingInfoRepository socialNetworkingInfoRespository;

    @Override
    public ProfileImageUploaded getProfileImage(String userId) {

        return profileImageUploadedRespository.findById(userId).orElse(null);
    }

    @Override
    public boolean saveProfileImage(ProfileImageUploaded profileImage) {

        // Delete all the non current images for this user. Really, there should only be one
        // image per user in the profile images table but this will clean them up for now. We
        // can make useruuid a unique index in the future.
        profileImageUploadedRespository.delete(profileImage);

        // now save the single new one
        profileImageUploadedRespository.save(profileImage);
        return true;
    }

    @Override
    public boolean removeProfileImage(String userId) {

        profileImageUploadedRespository.deleteById(userId);
        return true;
    }

    @Override
    public ProfileImageOfficial getOfficialImage(String userId) {

        return profileImageOfficialRespository.findById(userId).orElse(null);
    }

    @Override
    public boolean saveOfficialImage(ProfileImageOfficial officialImage) {

        profileImageOfficialRespository.save(officialImage);
        return true;
    }

    @Override
    public Optional<SocialNetworkingInfo> getSocialNetworkingInfo(String userId) {

        return socialNetworkingInfoRespository.findById(userId);
    }

    @Override
    public boolean saveSocialNetworkingInfo(SocialNetworkingInfo socialNetworkingInfo) {

        socialNetworkingInfoRespository.save(socialNetworkingInfo);
        return true;
    }
}
