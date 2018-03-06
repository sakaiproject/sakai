/**
 * Copyright (c) 2008-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.entityprovider;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.sakaiproject.profile2.service.ProfileImageService;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;

import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

@Setter @Slf4j
public class ProfileImageEntityProvider extends AbstractEntityProvider implements EntityProvider, AutoRegisterEntityProvider, Outputable, Describeable, ActionsExecutable {

    private ProfileImageLogic imageLogic;
    private ProfileImageService profileImageService;
    private SessionManager sessionManager;

    @Override
    public String[] getHandledOutputFormats() {
        return new String[] { Formats.JSON };
    }

    @Override
    public String getEntityPrefix() {
        return "profile-image";
    }

    @EntityCustomAction(action = "upload", viewKey = EntityView.VIEW_NEW)
    public String upload(EntityView view, Map<String, Object> params) {

        JSONObject result = new JSONObject();

        result.put("status", "ERROR");

        if (!checkCSRFToken(params)) {
            return result.toJSONString();
        }

        User currentUser = UserDirectoryService.getCurrentUser();
        String currentUserId = currentUser.getId();

        if (currentUserId == null) {
            log.warn("Access denied");
            return result.toJSONString();
        }

        String mimeType = "image/png";
        String fileName = UUID.randomUUID().toString();
        String base64 = (String) params.get("base64");
        byte[] imageBytes = Base64.decodeBase64(base64.getBytes());

        if (imageLogic.setUploadedProfileImage(currentUserId, imageBytes, mimeType, fileName)) {
            profileImageService.resetCachedProfileImageId(currentUserId);
            result.put("status", "SUCCESS");
        }

        return result.toJSONString();
    }

    @EntityCustomAction(action = "details", viewKey = EntityView.VIEW_LIST)
    public Object getProfileImage(OutputStream out, EntityView view, Map<String,Object> params) {

        JSONObject result = new JSONObject();

        result.put("status", "ERROR");

        User currentUser = UserDirectoryService.getCurrentUser();
        String currentUserId = currentUser.getId();

        if (currentUserId == null) {
            log.warn("Access denied");
            return result.toJSONString();
        }

        String imageUrl = imageLogic.getProfileImageEntityUrl(currentUserId, ProfileConstants.PROFILE_IMAGE_MAIN);

        result.put("url", imageUrl);
        result.put("isDefault", imageLogic.profileImageIsDefault(currentUserId));
        result.put("csrf_token", sessionManager.getCurrentSession().getAttribute("sakai.csrf.token"));
        result.put("status", "SUCCESS");

        return result.toJSONString();
    }

    @EntityCustomAction(action = "remove", viewKey = EntityView.VIEW_NEW)
    public String remove(EntityView view, Map<String, Object> params) {

        JSONObject result = new JSONObject();

        result.put("status", "ERROR");

        if (!checkCSRFToken(params)) {
            return result.toJSONString();
        }

        User currentUser = UserDirectoryService.getCurrentUser();
        String currentUserId = currentUser.getId();

        if (currentUserId == null) {
            log.warn("Access denied");
            return result.toJSONString();
        }

        if (imageLogic.resetProfileImage(currentUserId)) {
            profileImageService.resetCachedProfileImageId(currentUserId);
            result.put("status", "SUCCESS");
        }

        return result.toJSONString();
    }

    private boolean checkCSRFToken(Map<String, Object> params) {

        Object sessionToken = sessionManager.getCurrentSession().getAttribute("sakai.csrf.token");

        if (sessionToken == null || !sessionToken.equals(params.get("sakai_csrf_token"))) {
            log.warn("CSRF token validation failed");
            return false;
        }

        return true;
    }
}
