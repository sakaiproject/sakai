/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.sakaiproject.profile2.logic.ProfileLinkLogic;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.webapi.beans.ProfileRestBean;

import org.apache.commons.lang3.RandomStringUtils;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
@RestController
public class ProfileController extends AbstractSakaiApiController {

    @Autowired private CandidateDetailProvider candidateDetailProvider;
    @Autowired private ProfileLinkLogic profileLinkLogic;
    @Autowired private ProfileLogic profileLogic;
    @Autowired private UserDirectoryService userDirectoryService;

    @GetMapping(value = "/users/{userId}/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileRestBean> getUserProfile(@PathVariable String userId) throws UserNotDefinedException {

        checkSakaiSession();

        UserProfile userProfile = (UserProfile) profileLogic.getUserProfile(userId);

        if (userProfile == null) {
            return ResponseEntity.badRequest().build();
        }

        ProfileRestBean bean = new ProfileRestBean();
        bean.name = userProfile.getDisplayName();
        bean.email = userProfile.getEmail();
        bean.pronouns = userProfile.getPronouns();
        bean.pronunciation = userProfile.getPhoneticPronunciation();
        bean.profileUrl = profileLinkLogic.getInternalDirectUrlToUserProfile(userId);
        bean.hasPronunciationRecording = profileLogic.getUserNamePronunciation(userId) != null;

        if (candidateDetailProvider != null) {
            try {
                User user = userDirectoryService.getUser(userId);
                candidateDetailProvider.getInstitutionalNumericId(user, null).ifPresent(id -> {
                    bean.studentNumber = id;
                });
            } catch (UserNotDefinedException unde) {
                log.error("No user for id {}", userId);
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok(bean);
    }
}
