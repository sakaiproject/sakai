/**
 * Copyright (c) 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.lti.impl;

import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.tsugi.lti.LTIConstants;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.lti.api.UserPictureSetter;
import org.sakaiproject.profile2.api.ProfileService;
import org.sakaiproject.user.api.User;

/**
 *  @author Adrian Fish <a.fish@lancaster.ac.uk>
 */
@Slf4j
public class UserPictureSetterImpl implements UserPictureSetter {

    @Setter
    private ProfileService profileService;

    /**
     * LTI-155. If Profile2 is installed, set the profile picture to the user_image url, if supplied.
     *
     * @param payload The LTI launch parameters in a Map
     * @param user The provisioned user who MUST be already logged in.
     * @param isTrustedConsumer If this is true, do nothing as we assume that a local
     * 							user corresponding to the consumer user already exists
     * @param isEmailTrustedConsumer If this is true, do nothing as we assume that a local
     * 							user corresponding to the consumer user already exists
     */
    public void setupUserPicture(Map payload, User user, boolean isTrustedConsumer, boolean isEmailTrustedConsumer) {

        if (isTrustedConsumer) return;
        if (isEmailTrustedConsumer) return;

    	String imageUrl = (String) payload.get(LTIConstants.USER_IMAGE);

        if (StringUtils.isNotBlank(imageUrl)) {
    		log.debug("User image supplied by consumer: {}", imageUrl);

            try {
                profileService.saveOfficialImageUrl(user.getId(), imageUrl);
            } catch(Exception e) {
                log.error("Failed to setup launcher's Profile2 picture.", e.toString());
            }
    	}
    }
}
