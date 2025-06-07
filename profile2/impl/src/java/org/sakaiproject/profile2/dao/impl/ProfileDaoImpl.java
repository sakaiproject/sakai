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
package org.sakaiproject.profile2.dao.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.hbm.model.ProfileImageExternal;
import org.sakaiproject.profile2.hbm.model.ProfileImageOfficial;
import org.sakaiproject.profile2.hbm.model.ProfileImageUploaded;
import org.sakaiproject.profile2.model.ExternalIntegrationInfo;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Internal DAO Interface for Profile2
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Setter
@Slf4j
public class ProfileDaoImpl implements ProfileDao {

    private SessionFactory sessionFactory;

    @Override
    public List<ProfileImageUploaded> getCurrentProfileImageRecords(final String userId) {
        TypedQuery<ProfileImageUploaded> query = getSession().createNamedQuery(QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD, ProfileImageUploaded.class);
        query.setParameter(USER_UUID, userId);
        return query.getResultList();
    }

    @Override
    public ProfileImageUploaded getCurrentProfileImageRecord(final String userId) {
        TypedQuery<ProfileImageUploaded> query = getSession().createNamedQuery(QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD, ProfileImageUploaded.class);
        query.setParameter(USER_UUID, userId);
        return query.getResultStream().findAny().orElse(null);
    }

    @Override
    public List<ProfileImageUploaded> getOtherProfileImageRecords(final String userId) {
        TypedQuery<ProfileImageUploaded> query = getSession().createNamedQuery(QUERY_OTHER_PROFILE_IMAGE_RECORDS, ProfileImageUploaded.class);
        query.setParameter(USER_UUID, userId);
        return query.getResultList();
    }

    @Override
    public ProfileImageOfficial getOfficialImageRecordForUser(final String userUuid) {
        TypedQuery<ProfileImageOfficial> query = getSession().createNamedQuery(QUERY_GET_OFFICIAL_IMAGE_RECORD, ProfileImageOfficial.class);
        query.setParameter(USER_UUID, userUuid);
        return query.getResultStream().findAny().orElse(null);
    }

    @Override
    public SocialNetworkingInfo getSocialNetworkingInfo(final String userId) {
        return getSession().get(SocialNetworkingInfo.class, userId);
    }

    @Override
    public boolean saveSocialNetworkingInfo(final SocialNetworkingInfo socialNetworkingInfo) {
        try {
            getSession().merge(socialNetworkingInfo);
            return true;
        } catch (Exception e) {
            log.warn("save social networking info failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public boolean addNewProfileImage(final ProfileImageUploaded profileImage) {
        Session session = getSession();
        try {
            // first get the current ProfileImage records for this user
            List<ProfileImageUploaded> currentImages = getCurrentProfileImageRecords(profileImage.getUserUuid());

            for (ProfileImageUploaded currentImage : currentImages) {
                currentImage.setCurrent(false);
                session.merge(currentImage);
            }

            // now save the new one
            session.persist(profileImage);
            return true;
        } catch (Exception e) {
            log.warn("add new profile image failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public List<String> getAllSakaiPersonIds() {
        TypedQuery<String> query = getSession().createNamedQuery(QUERY_GET_ALL_SAKAI_PERSON_IDS, String.class);
        return query.getResultList();
    }

    @Override
    public int getAllSakaiPersonIdsCount() {
        TypedQuery<Number> query = getSession().createNamedQuery(QUERY_GET_ALL_SAKAI_PERSON_IDS_COUNT, Number.class);
        return query.getSingleResult().intValue();
    }

    @Override
    public ProfileImageExternal getExternalImageRecordForUser(final String userId) {
        return getSession().get(ProfileImageExternal.class, userId);
    }

    @Override
    public boolean saveExternalImage(final ProfileImageExternal externalImage) {
        try {
            getSession().merge(externalImage);
            return true;
        } catch (Exception e) {
            log.warn("save external image failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public boolean saveOfficialImageUrl(final ProfileImageOfficial officialImage) {
        try {
            getSession().merge(officialImage);
            return true;
        } catch (Exception e) {
            log.warn("save official image url failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public ExternalIntegrationInfo getExternalIntegrationInfo(final String userUuid) {
        return getSession().get(ExternalIntegrationInfo.class, userUuid);
    }

    @Override
    public boolean updateExternalIntegrationInfo(final ExternalIntegrationInfo info) {
        try {
            getSession().merge(info);
            return true;
        } catch (Exception e) {
            log.warn("update external integration info failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public boolean invalidateCurrentProfileImage(final String userUuid) {
        try {
            ProfileImageUploaded currentImage = getCurrentProfileImageRecord(userUuid);
            if (currentImage != null) {
                currentImage.setCurrent(false);
                getSession().merge(currentImage);
                return true;
            }
        } catch (Exception e) {
            log.warn("invalidate current profile image failed, {}", e.toString());
        }
        return false;
    }

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }
}
