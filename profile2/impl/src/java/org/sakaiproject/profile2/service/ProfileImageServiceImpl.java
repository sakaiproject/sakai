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
