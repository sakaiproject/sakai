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

import java.net.URI;
import java.util.UUID;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.profile2.api.MimeTypeByteArray;
import org.sakaiproject.profile2.api.ProfileImage;
import org.sakaiproject.profile2.api.ProfileService;
import org.sakaiproject.profile2.api.ProfileTransferBean;
import org.sakaiproject.profile2.api.ProfileConstants;
import org.sakaiproject.serialization.MapperFactory;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.simple.JSONObject;

import com.github.fge.jsonpatch.JsonPatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ProfileController extends AbstractSakaiApiController {

    @Autowired(required = false)
    private CandidateDetailProvider candidateDetailProvider;

    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private ProfileService profileService;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private ToolManager toolManager;

    // Permission constants for Roster tool
    private static final String ROSTER_PERMISSION_VIEW_PROFILE = "roster.viewprofile";
    private static final String ROSTER_PERMISSION_VIEW_EMAIL = "roster.viewemail";
    private static final String ROSTER_PERMISSION_VIEW_CANDIDATE_DETAILS = "roster.viewcandidatedetails";

    private ObjectMapper jsonMapper;

    public ProfileController() {

        jsonMapper = MapperFactory.jsonBuilder()
            .includeEmpty()
            .registerJavaTimeModule()
            .build();
    }

    @GetMapping(value = "/users/{userId}/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileTransferBean> getUserProfile(
            @PathVariable String userId,
            @RequestParam(required = false) String siteId) throws UserNotDefinedException {

        if (StringUtils.equals(userId, "blank")) {
            return ResponseEntity.noContent().build();
        }

        String currentUserId = checkSakaiSession().getUserId();

        if (StringUtils.equals(userId, "me")) {
            userId = currentUserId;
        }

        ProfileTransferBean bean = profileService.getUserProfile(userId, siteId);

        if (bean == null) {
            return ResponseEntity.badRequest().build();
        }

        if (bean.hasPronunciationRecording) {
            bean.nameRecordingUrl = "/api/users/" + userId + "/profile/pronunciation";
        }

        return ResponseEntity.ok(bean);
    }

    @PatchMapping(value = "/users/{userId}/profile", consumes = "application/json-patch+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileTransferBean> patchProfile(@PathVariable String userId, @RequestBody JsonPatch patch) throws Exception {

        checkSakaiSession();

        ProfileTransferBean profile = profileService.getUserProfile(userId);

        try {
            JsonNode patched = patch.apply(jsonMapper.convertValue(profile, JsonNode.class));
            ProfileTransferBean patchedProfile = jsonMapper.treeToValue(patched, ProfileTransferBean.class);
            profileService.saveUserProfile(patchedProfile);

            patchedProfile = profileService.getUserProfile(userId);

            if (patchedProfile.hasPronunciationRecording) {
                patchedProfile.nameRecordingUrl = "/api/users/" + userId + "/profile/pronunciation";
            }

            return ResponseEntity.ok(patchedProfile);
        } catch (Exception e) {
            log.error("Failed to patch profile", e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(path = { "/users/{userId}/profile/image", "/users/{userId}/profile/image/{imageType}" }, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getProfileImage(@PathVariable String userId, @PathVariable(required = false) String imageType, @RequestParam(required = false) String siteId) {

        String currentUserId = checkSakaiSession().getUserId();

        if (StringUtils.isNotBlank(imageType)
                && !StringUtils.equals("thumb", imageType)
                && !StringUtils.equals("official", imageType)) {
            return ResponseEntity.badRequest().build();
        }

        ProfileImage image = StringUtils.equals("official", imageType)
                ? profileService.getOfficialProfileImage(userId, siteId)
                : profileService.getProfileImage(userId,
                        StringUtils.equals("thumb", imageType) ? ProfileConstants.PROFILE_IMAGE_THUMBNAIL : ProfileConstants.PROFILE_IMAGE_MAIN,
                        siteId);
        
        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        if (!StringUtils.equals(currentUserId, userId)) {
		    eventTrackingService.post(this.eventTrackingService.newEvent(ProfileConstants.EVENT_IMAGE_REQUEST, "/profile/" + currentUserId + "/imagerequest/", false));
        }

        // check for binary
        final byte[] bytes = image.getBinary();
        if (bytes != null && bytes.length > 0) {
            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        }

        // 302 for images that are URL-backed, not binary-backed.
        final String url = image.getUrl();
        if (StringUtils.isNotBlank(url)) {
            try {
                URI redirectUri = URI.create(url);
                HttpHeaders headers = new HttpHeaders();
                headers.setCacheControl(CacheControl.noStore().getHeaderValue());
                headers.setLocation(redirectUri);
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            } catch (Exception e) {
                log.warn("Invalid profile image URL for user {}: {}", userId, e.getMessage());
            }
        }
        
        return ResponseEntity.badRequest().build();
    }


    @PostMapping(value = "/users/{userId}/profile/image")
    public ResponseEntity<String> putProfileImage(@PathVariable String userId, @RequestParam String base64) {

        JSONObject result = new JSONObject();

        result.put("status", "ERROR");

        String currentUserId = checkSakaiSession().getUserId();

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String mimeType = "image/png";
        String fileName = UUID.randomUUID().toString();
        byte[] imageBytes = Base64.decodeBase64(base64.getBytes());

        if (profileService.setProfileImage(userId, imageBytes, mimeType, fileName)) {
            result.put("status", "SUCCESS");
        }

        return ResponseEntity.ok(result.toJSONString());
    }

    @GetMapping(value = "/users/{userId}/profile/image/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProfileImageDetails(@PathVariable String userId) {

        JSONObject result = new JSONObject();

        result.put("status", "ERROR");

        Session session = checkSakaiSession();
        String currentUserId = session.getUserId();

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String imageUrl = profileService.getProfileImageEntityUrl(userId, ProfileConstants.PROFILE_IMAGE_MAIN);

        result.put("url", imageUrl);
        result.put("isDefault", profileService.profileImageIsDefault(userId));
        result.put("csrf_token", session.getAttribute("sakai.csrf.token"));
        result.put("status", "SUCCESS");

        return ResponseEntity.ok(result.toJSONString());
    }

    @DeleteMapping(value = "/users/{userId}/profile/image")
    public ResponseEntity<String> removeProfileImage(@PathVariable String userId) {

        String currentUserId = checkSakaiSession().getUserId();

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        profileService.removeProfileImage(userId);

        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/users/{userId}/profile/pronunciation", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getNamePronunciation(@PathVariable String userId) {

        MimeTypeByteArray data = profileService.getUserNamePronunciation(userId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(data.getMimeType())).body(data.getBytes());
    }

    @DeleteMapping(path = "/users/{userId}/profile/pronunciation")
    public ResponseEntity removeNamePronunciation(@PathVariable String userId) {

        profileService.removePronunciationRecording(userId);
        return ResponseEntity.ok().build();
    }

}
