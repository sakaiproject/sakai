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
package org.sakaiproject.profile2.service;

import lombok.Setter;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.hbm.model.ProfileImageUploaded;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.tool.api.SessionManager;

public class ProfileImageServiceImpl implements ProfileImageService {

    @Setter
    private ProfileDao dao;

    @Setter
    private SessionManager sessionManager;

    public Long getCurrentProfileImageId(final String userId) {

        ProfileImageUploaded profileImage = dao.getCurrentProfileImageRecord(userId);
        if (profileImage == null) {
            return null;
        }
        return profileImage.getId();
    }

    public String getProfileImageURL(final String userId, final String eid, final boolean thumbnail) {

        if (userId == null) {
            if (thumbnail) {
                return ProfileConstants.UNAVAILABLE_IMAGE_THUMBNAIL;
            } else {
                return ProfileConstants.UNAVAILABLE_IMAGE_FULL;
            }
        }

        String url = "/direct/profile/"+eid+"/image";

        if (thumbnail) {
            url += "/thumb";
        }

        if (sessionManager.getCurrentSession().getAttribute("profileImageId") == null) {
            resetCachedProfileImageId(userId);
        }

        url += "?_=" + ((Long) sessionManager.getCurrentSession().getAttribute("profileImageId")).toString();

        return url;
    }

    public String resetCachedProfileImageId(final String userId) {

        Long profileImageId = getCurrentProfileImageId(userId);
        if (profileImageId == null) {
            profileImageId = Long.valueOf(0);
        }
        sessionManager.getCurrentSession().setAttribute("profileImageId", profileImageId);

        return profileImageId.toString();
    }

}
