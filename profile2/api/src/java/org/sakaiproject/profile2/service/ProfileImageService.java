package org.sakaiproject.profile2.service;

public interface ProfileImageService {

    public abstract Long getCurrentProfileImageId(final String userUuid);
    public abstract String getProfileImageURL(final String userUuid, final String eid, final boolean thumbnail);
    public abstract String resetCachedProfileImageId(final String userId);
}
