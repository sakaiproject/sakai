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
import org.sakaiproject.profile2.hbm.model.ProfileFriend;
import org.sakaiproject.profile2.hbm.model.ProfileImageExternal;
import org.sakaiproject.profile2.hbm.model.ProfileImageOfficial;
import org.sakaiproject.profile2.hbm.model.ProfileImageUploaded;
import org.sakaiproject.profile2.hbm.model.ProfileKudos;
import org.sakaiproject.profile2.model.CompanyProfile;
import org.sakaiproject.profile2.model.ExternalIntegrationInfo;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.model.MessageParticipant;
import org.sakaiproject.profile2.model.MessageThread;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.model.WallItemComment;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.util.Date;
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
    public List<String> getRequestedConnectionUserIdsForUser(final String userId) {
        TypedQuery<String> query = getSession().createNamedQuery(QUERY_GET_FRIEND_REQUESTS_FOR_USER, String.class);
        query.setParameter(USER_UUID, userId);
        query.setParameter("false", Boolean.FALSE);
        return query.getResultList();
    }

    @Override
    public List<String> getOutgoingConnectionUserIdsForUser(final String userId) {
        TypedQuery<String> query = getSession().createNamedQuery(QUERY_GET_OUTGOING_FRIEND_REQUESTS_FOR_USER, String.class);
        query.setParameter(USER_UUID, userId);
        query.setParameter("false", Boolean.FALSE);
        return query.getResultList();
    }

    @Override
    public List<String> getConfirmedConnectionUserIdsForUser(final String userId) {
        TypedQuery<String> query = getSession().createNamedQuery(QUERY_GET_CONFIRMED_FRIEND_USERIDS_FOR_USER, String.class);
        query.setParameter(USER_UUID, userId);
        query.setParameter("true", Boolean.TRUE);
        return query.getResultList();
    }

    @Override
    public List<String> findSakaiPersonsByNameOrEmail(final String search) {
        TypedQuery<String> query = getSession().createNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_NAME_OR_EMAIL, String.class);
        query.setParameter(SEARCH, '%' + search + '%');
        return query.getResultList();
    }

    @Override
    public List<String> findSakaiPersonsByInterest(final String search, final boolean includeBusinessBio) {
        TypedQuery<String> query;
        if (includeBusinessBio) {
            query = getSession().createNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_INTEREST_AND_BUSINESS_BIO, String.class);
        } else {
            query = getSession().createNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_INTEREST, String.class);
        }
        query.setParameter(SEARCH, '%' + search + '%');
        return query.getResultList();
    }

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
    public ProfileFriend getConnectionRecord(final String userId, final String friendId) {
        TypedQuery<ProfileFriend> query = getSession().createNamedQuery(QUERY_GET_FRIEND_RECORD, ProfileFriend.class);
        query.setParameter(USER_UUID, userId);
        query.setParameter(FRIEND_UUID, friendId);
        return query.getResultStream().findAny().orElse(null);
    }

    @Override
    public CompanyProfile getCompanyProfile(final String userId, final long companyProfileId) {
        TypedQuery<CompanyProfile> query = getSession().createNamedQuery(QUERY_GET_COMPANY_PROFILE, CompanyProfile.class);
        query.setParameter(USER_UUID, userId);
        query.setParameter(ID, companyProfileId);
        return query.getResultStream().findAny().orElse(null);
    }

    @Override
    public GalleryImage getGalleryImageRecord(final String userId, final long imageId) {
        TypedQuery<GalleryImage> query = getSession().createNamedQuery(QUERY_GET_GALLERY_RECORD, GalleryImage.class);
        query.setParameter(USER_UUID, userId);
        query.setParameter(ID, imageId);
        return query.getResultStream().findAny().orElse(null);
    }

    @Override
    public ProfileImageOfficial getOfficialImageRecordForUser(final String userUuid) {
        TypedQuery<ProfileImageOfficial> query = getSession().createNamedQuery(QUERY_GET_OFFICIAL_IMAGE_RECORD, ProfileImageOfficial.class);
        query.setParameter(USER_UUID, userUuid);
        return query.getResultStream().findAny().orElse(null);
    }

    @Override
    public boolean addNewConnection(final ProfileFriend profileFriend) {
        try {
            getSession().persist(profileFriend);
            return true;
        } catch (Exception e) {
            log.warn("request friend failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public boolean updateConnection(final ProfileFriend profileFriend) {
        try {
            getSession().merge(profileFriend);
            return true;
        } catch (Exception e) {
            log.warn("confirm friend request failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public boolean removeConnection(final ProfileFriend profileFriend) {
        try {
            Session session = getSession();
            session.remove(session.merge(profileFriend));
            return true;
        } catch (Exception e) {
            log.warn("remove connection failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public ProfileFriend getPendingConnection(final String userId, final String friendId) {

        if (userId == null || friendId == null) {
            throw new IllegalArgumentException("Null Argument in getPendingConnection");
        }

        TypedQuery<ProfileFriend> query = getSession().createNamedQuery(QUERY_GET_FRIEND_REQUEST, ProfileFriend.class);
        query.setParameter(USER_UUID, userId);
        query.setParameter(FRIEND_UUID, friendId);
        query.setParameter(CONFIRMED, false);
        return query.getResultStream().findAny().orElse(null);
    }

    @Override
    public ProfileStatus getUserStatus(final String userId, final Date oldestDate) {
        TypedQuery<ProfileStatus> query = getSession().createNamedQuery(QUERY_GET_USER_STATUS, ProfileStatus.class);
        query.setParameter(USER_UUID, userId);
        query.setParameter(OLDEST_STATUS_DATE, oldestDate, TemporalType.TIMESTAMP);
        return query.getResultStream().findAny().orElse(null);
    }

    @Override
    public boolean setUserStatus(final ProfileStatus profileStatus) {
        try {
            getSession().merge(profileStatus);
            return true;
        } catch (Exception e) {
            log.warn("update User Status failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public boolean clearUserStatus(final ProfileStatus profileStatus) {
        try {
            Session session = getSession();
            session.remove(session.merge(profileStatus));
            return true;
        } catch (final Exception e) {
            log.error("clear the Users Status failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public int getStatusUpdatesCount(final String userId) {
        TypedQuery<Number> query = getSession().createNamedQuery(QUERY_GET_STATUS_UPDATES_COUNT, Number.class);
        query.setParameter(USER_UUID, userId);
        return query.getSingleResult().intValue();
    }

    @Override
    public ProfilePrivacy addNewPrivacyRecord(final ProfilePrivacy privacy) {
        return (ProfilePrivacy) getSession().merge(privacy);
    }

    @Override
    public ProfilePrivacy getPrivacyRecord(final String userId) {
        return getSession().get(ProfilePrivacy.class, userId);
    }

    @Override
    public boolean updatePrivacyRecord(final ProfilePrivacy privacy) {
        try {
            getSession().merge(privacy);
            return true;
        } catch (Exception e) {
            log.warn("update privacy record failed, {}", e.toString());
        }
        return false;
    }


    @Override
    public boolean addNewCompanyProfile(final CompanyProfile companyProfile) {
        try {
            getSession().persist(companyProfile);
            return true;
        } catch (Exception e) {
            log.warn("add new company profile failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public boolean updateCompanyProfile(final CompanyProfile companyProfile) {
        try {
            getSession().merge(companyProfile);
            return true;
        } catch (Exception e) {
            log.warn("update company profile failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public List<CompanyProfile> getCompanyProfiles(final String userId) {
        TypedQuery<CompanyProfile> query = getSession().createNamedQuery(QUERY_GET_COMPANY_PROFILES, CompanyProfile.class);
        query.setParameter(USER_UUID, userId);
        return query.getResultList();
    }

    @Override
    public boolean removeCompanyProfile(final CompanyProfile companyProfile) {
        try {
            Session session = getSession();
            session.remove(session.merge(companyProfile));
            return true;
        } catch (Exception e) {
            log.warn("remove company profile failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public boolean addNewGalleryImage(final GalleryImage galleryImage) {
        try {
            getSession().persist(galleryImage);
            return true;
        } catch (Exception e) {
            log.warn("add gallery image failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public List<GalleryImage> getGalleryImages(final String userId) {
        TypedQuery<GalleryImage> query = getSession().createNamedQuery(QUERY_GET_GALLERY_IMAGE_RECORDS, GalleryImage.class);
        query.setParameter(USER_UUID, userId);
        return query.getResultList();
    }

    @Override
    public boolean removeGalleryImage(final GalleryImage galleryImage) {
        try {
            Session session = getSession();
            session.remove(session.merge(galleryImage));
            return true;
        } catch (Exception e) {
            log.warn("remove gallery image failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public int getGalleryImagesCount(final String userId) {
        TypedQuery<Number> query = getSession().createNamedQuery(QUERY_GET_GALLERY_IMAGE_RECORDS_COUNT, Number.class);
        query.setParameter(USER_UUID, userId);
        return query.getSingleResult().intValue();
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
    public ProfilePreferences addNewPreferencesRecord(final ProfilePreferences prefs) {
        try {
            getSession().persist(prefs);
        } catch (Exception e) {
            log.warn("create default preferences record failed, {}", e.toString());
        }
        return prefs;
    }

    @Override
    public ProfilePreferences getPreferencesRecordForUser(final String userId) {
        return getSession().get(ProfilePreferences.class, userId);
    }

    @Override
    public boolean savePreferencesRecord(final ProfilePreferences prefs) {
        try {
            getSession().merge(prefs);
            return true;
        } catch (Exception e) {
            log.warn("save preferences record failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public int getAllUnreadMessagesCount(final String userId) {
        final TypedQuery<Number> query = getSession().createNamedQuery(QUERY_GET_ALL_UNREAD_MESSAGES_COUNT, Number.class);
        query.setParameter(UUID, userId);
        query.setParameter("false", Boolean.FALSE);
        return query.getSingleResult().intValue();
    }

    @Override
    public int getThreadsWithUnreadMessagesCount(final String userId) {
        final TypedQuery<Number> query = getSession().createNamedQuery(QUERY_GET_THREADS_WITH_UNREAD_MESSAGES_COUNT, Number.class);
        query.setParameter(UUID, userId);
        query.setParameter("false", Boolean.FALSE);
        return query.getSingleResult().intValue();
    }

    @Override
    public List<MessageThread> getMessageThreads(final String userId) {
        final TypedQuery<MessageThread> query = getSession().createNamedQuery(QUERY_GET_MESSAGE_THREADS, MessageThread.class);
        query.setParameter(UUID, userId);
        return query.getResultList();
    }

    @Override
    public int getMessageThreadsCount(final String userId) {
        final TypedQuery<Number> query = getSession().createNamedQuery(QUERY_GET_MESSAGE_THREADS_COUNT, Number.class);
        query.setParameter(UUID, userId);
        return query.getSingleResult().intValue();
    }

    @Override
    public int getSentMessagesCount(final String userId) {
        final TypedQuery<Number> query = getSession().createNamedQuery(QUERY_GET_SENT_MESSAGES_COUNT, Number.class);
        query.setParameter(UUID, userId);
        return query.getSingleResult().intValue();
    }

    @Override
    public List<Message> getMessagesInThread(final String threadId) {
        final TypedQuery<Message> query = getSession().createNamedQuery(QUERY_GET_MESSAGES_IN_THREAD, Message.class);
        query.setParameter(THREAD, threadId);
        return query.getResultList();
    }

    @Override
    public int getMessagesInThreadCount(final String threadId) {
        final TypedQuery<Number> query = getSession().createNamedQuery(QUERY_GET_MESSAGES_IN_THREAD_COUNT, Number.class);
        query.setParameter(THREAD, threadId);
        return query.getSingleResult().intValue();
    }

    @Override
    public Message getMessage(final String id) {
        return getSession().get(Message.class, id);
    }

    @Override
    public MessageThread getMessageThread(final String threadId) {
        return getSession().get(MessageThread.class, threadId);
    }

    @Override
    public Message getLatestMessageInThread(final String threadId) {
        final TypedQuery<Message> query = getSession().createNamedQuery(QUERY_GET_LATEST_MESSAGE_IN_THREAD, Message.class);
        query.setParameter(THREAD, threadId);
        List<Message> results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public boolean toggleMessageRead(final MessageParticipant participant, final boolean read) {
        try {
            participant.setRead(read);
            getSession().merge(participant);
            return true;
        } catch (final Exception e) {
            log.warn("toggle message read failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public MessageParticipant getMessageParticipant(final String messageId, final String userUuid) {
        final TypedQuery<MessageParticipant> query = getSession().createNamedQuery(QUERY_GET_MESSAGE_PARTICIPANT_FOR_MESSAGE_AND_UUID, MessageParticipant.class);
        query.setParameter(MESSAGE_ID, messageId);
        query.setParameter(UUID, userUuid);
        List<MessageParticipant> results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public List<String> getThreadParticipants(final String threadId) {
        final TypedQuery<String> query = getSession().createNamedQuery(QUERY_GET_THREAD_PARTICIPANTS, String.class);
        query.setParameter(THREAD, threadId);
        return query.getResultList();
    }

    @Override
    public void saveNewThread(final MessageThread thread) {
        try {
            getSession().persist(thread);
            log.debug("Message thread saved with id [{}]", thread.getId());
        } catch (Exception e) {
            log.warn("save new thread failed, {}", e.toString());
        }
    }

    @Override
    public void saveNewMessage(final Message message) {
        try {
            getSession().persist(message);
            log.debug("Message saved with id [{}]", message.getId());
        } catch (Exception e) {
            log.warn("save new message failed, {}", e.toString());
        }
    }

    @Override
    public void saveNewMessageParticipant(final MessageParticipant participant) {
        try {
            getSession().persist(participant);
            log.debug("Message participant saved with id [{}]", participant.getId());
        } catch (Exception e) {
            log.warn("save new message participant failed, {}", e.toString());
        }
    }

    @Override
    public void saveNewMessageParticipants(final List<MessageParticipant> participants) {
        participants.forEach(this::saveNewMessageParticipant);
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
    public ProfileKudos getKudos(final String userUuid) {
        return getSession().get(ProfileKudos.class, userUuid);
    }


    @Override
    public boolean updateKudos(final ProfileKudos kudos) {
        try {
            getSession().merge(kudos);
            return true;
        } catch (Exception e) {
            log.warn("update Kudos failed, {}", e.toString());
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
    public boolean addNewWallItemForUser(final String userUuid, final WallItem item) {
        try {
            getSession().persist(item);
            return true;
        } catch (Exception e) {
            log.warn("add new wall item for user [{}] failed, {}", userUuid, e.toString());
        }
        return false;
    }

    @Override
    public boolean removeWallItemFromWall(final WallItem item) {
        try {
            Session session = getSession();
            session.remove(session.merge(item));
            return true;
        } catch (final Exception e) {
            log.warn("delete wall item from wall failed, {}", e.toString());
        }
        return false;
    }

    @Override
    public WallItem getWallItem(final long wallItemId) {
        return getSession().get(WallItem.class, wallItemId);
    }

    @Override
    public WallItemComment getWallItemComment(final long wallItemCommentId) {
        return getSession().get(WallItemComment.class, wallItemCommentId);
    }

    @Override
    public List<WallItem> getWallItemsForUser(final String userUuid) {
        TypedQuery<WallItem> query = getSession().createNamedQuery(QUERY_GET_WALL_ITEMS, WallItem.class);
        query.setParameter(USER_UUID, userUuid);
        return query.getResultList();
    }

    @Override
    public boolean addNewCommentToWallItem(final WallItemComment wallItemComment) {
        try {
            getSession().persist(wallItemComment);
            return true;
        } catch (Exception e) {
            log.warn("add new wall item comment failed, {}", e.toString());
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