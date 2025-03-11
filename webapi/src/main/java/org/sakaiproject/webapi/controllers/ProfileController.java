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

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.profile2.logic.ProfileLinkLogic;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.webapi.beans.ProfileRestBean;

import org.apache.commons.lang3.StringUtils;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
@RestController
public class ProfileController extends AbstractSakaiApiController {

    @Autowired(required = false)
    private CandidateDetailProvider candidateDetailProvider;

    @Autowired private ProfileLinkLogic profileLinkLogic;
    @Autowired private ProfileLogic profileLogic;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SecurityService securityService;
    @Autowired private SiteService siteService;
    @Autowired private ToolManager toolManager;

    // Permission constants for Roster tool
    private static final String ROSTER_PERMISSION_VIEW_PROFILE = "roster.viewprofile";
    private static final String ROSTER_PERMISSION_VIEW_EMAIL = "roster.viewemail";
    private static final String ROSTER_PERMISSION_VIEW_CANDIDATE_DETAILS = "roster.viewcandidatedetails";

    @GetMapping(value = "/users/{userId}/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileRestBean> getUserProfile(
            @PathVariable String userId,
            @RequestParam(required = false) String siteId) throws UserNotDefinedException {

        Session session = checkSakaiSession();
        String currentUserId = session.getUserId();

        if (StringUtils.equals(userId, "blank")) {
            return ResponseEntity.noContent().build();
        }

        UserProfile userProfile = (UserProfile) profileLogic.getUserProfile(userId);

        if (userProfile == null) {
            return ResponseEntity.badRequest().build();
        }

        ProfileRestBean bean = new ProfileRestBean();
        bean.name = userProfile.getDisplayName();
        
        // Users can always view their own full profile
        boolean isSelf = StringUtils.equals(currentUserId, userId);
        
        // If no siteId is provided and it's not the user viewing their own profile,
        // only return the basic profile info
        if (StringUtils.isBlank(siteId) && !isSelf) {
            log.debug("No siteId provided for permission check, returning basic profile for userId: {}", userId);
            return ResponseEntity.ok(bean);
        }
        
        String siteRef = "/site/" + siteId;
        
        boolean canViewProfile = isSelf || securityService.unlock(ROSTER_PERMISSION_VIEW_PROFILE, siteRef);
        boolean canViewEmail = isSelf || securityService.unlock(ROSTER_PERMISSION_VIEW_EMAIL, siteRef);
        boolean canViewCandidateDetails = isSelf || securityService.unlock(ROSTER_PERMISSION_VIEW_CANDIDATE_DETAILS, siteRef);
        
        // Only add these fields if the user has permission to view the profile
        if (canViewProfile) {
            bean.nickname = userProfile.getNickname();
            bean.pronouns = userProfile.getPronouns();
            bean.pronunciation = userProfile.getPhoneticPronunciation();
            bean.profileUrl = profileLinkLogic.getInternalDirectUrlToUserProfile(userId);
            bean.hasPronunciationRecording = profileLogic.getUserNamePronunciation(userId) != null;
        }
        
        // Only add email if the user has permission to view email
        if (canViewEmail) {
            bean.email = userProfile.getEmail();
        }

        // Only add candidate details if the user has permission to view them
        if (canViewCandidateDetails && candidateDetailProvider != null) {
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
