/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.query.Query;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.BooleanType;
import org.hibernate.type.DateType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
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
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.model.WallItemComment;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;

/**
 * Internal DAO Interface for Profile2
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ProfileDaoImpl extends HibernateDaoSupport implements ProfileDao {

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<String> getRequestedConnectionUserIdsForUser(final String userId) {
				
		//get friends of this user [and map it automatically to the Friend object]
		//updated: now just returns a List of Strings
		final HibernateCallback<List<String>> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_FRIEND_REQUESTS_FOR_USER);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setBoolean("false", Boolean.FALSE);
            //q.setResultTransformer(Transformers.aliasToBean(Friend.class));

            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getOutgoingConnectionUserIdsForUser(final String userId) {

		//get friends of this user [and map it automatically to the Friend object]
		//updated: now just returns a List of Strings
		final HibernateCallback<List<String>> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_OUTGOING_FRIEND_REQUESTS_FOR_USER);
            q.setString(USER_UUID, userId);
            q.setBoolean("false", Boolean.FALSE);

            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<String> getConfirmedConnectionUserIdsForUser(final String userId) {
				
		//get 
		final HibernateCallback<List<String>> hcb = session -> {
	  			final Query q = session.getNamedQuery(QUERY_GET_CONFIRMED_FRIEND_USERIDS_FOR_USER);
	  			q.setParameter(USER_UUID, userId, StringType.INSTANCE);
	  			q.setBoolean("true", Boolean.TRUE); 
	  			return q.list();
	  	};
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<String> findSakaiPersonsByNameOrEmail(final String search) {
				
		//get 
		final HibernateCallback<List<String>> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_NAME_OR_EMAIL);
            q.setParameter(SEARCH, '%' + search + '%', StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<String> findSakaiPersonsByInterest(final String search, final boolean includeBusinessBio) {
		
		//get 
		final HibernateCallback<List<String>> hcb = session -> {
            Query q;
            if (false == includeBusinessBio) {
                q = session.getNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_INTEREST);
            } else {
                q = session.getNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_INTEREST_AND_BUSINESS_BIO);
            }
            q.setParameter(SEARCH, '%' + search + '%', StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<ProfileImageUploaded> getCurrentProfileImageRecords(final String userId) {
				
		//get 
		final HibernateCallback<List<ProfileImageUploaded>> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileImageUploaded getCurrentProfileImageRecord(final String userId) {
		
		final HibernateCallback<ProfileImageUploaded> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ProfileImageUploaded) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<ProfileImageUploaded> getOtherProfileImageRecords(final String userId) {
				
		//get 
		final HibernateCallback<List<ProfileImageUploaded>> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_OTHER_PROFILE_IMAGE_RECORDS);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileFriend getConnectionRecord(final String userId, final String friendId) {
		
		//this particular query checks for records when userId/friendId is in either column
		final HibernateCallback<ProfileFriend> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_FRIEND_RECORD);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setParameter(FRIEND_UUID, friendId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ProfileFriend) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public CompanyProfile getCompanyProfile(final String userId, final long companyProfileId) {

		final HibernateCallback<CompanyProfile> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_COMPANY_PROFILE);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setParameter(ID, companyProfileId, LongType.INSTANCE);
            q.setMaxResults(1);
            return (CompanyProfile) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public GalleryImage getGalleryImageRecord(final String userId, final long imageId) {
		
		final HibernateCallback<GalleryImage> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_GALLERY_RECORD);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setParameter(ID, imageId, LongType.INSTANCE);
            q.setMaxResults(1);
            return (GalleryImage) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileImageOfficial getOfficialImageRecordForUser(final String userUuid) {
		
		final HibernateCallback<ProfileImageOfficial> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_OFFICIAL_IMAGE_RECORD);
            q.setParameter(USER_UUID, userUuid, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ProfileImageOfficial) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean addNewConnection(final ProfileFriend profileFriend) {
		
		try {
			getHibernateTemplate().save(profileFriend);
			return true;
			
		} catch (final Exception e) {
			log.error("requestFriend failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
		
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean updateConnection(final ProfileFriend profileFriend) {
		
		try {
			getHibernateTemplate().update(profileFriend);
			return true;
		} catch (final Exception e) {
			log.error("confirmFriendRequest failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean removeConnection(final ProfileFriend profileFriend) {
		
		//delete
		try {
			getHibernateTemplate().delete(profileFriend, LockMode.NONE);
			return true;
		} catch (final Exception e) {
			log.error("removeConnection failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileFriend getPendingConnection(final String userId, final String friendId) {
		
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null Argument in getPendingConnection"); 
	  	}
		
		final HibernateCallback<ProfileFriend> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_FRIEND_REQUEST);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setParameter(FRIEND_UUID, friendId, StringType.INSTANCE);
            q.setParameter(CONFIRMED, false, BooleanType.INSTANCE);
            q.setMaxResults(1);
            return (ProfileFriend) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileStatus getUserStatus(final String userId, final Date oldestDate) {
		
		final HibernateCallback<ProfileStatus> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_USER_STATUS);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setParameter(OLDEST_STATUS_DATE, oldestDate, DateType.INSTANCE);
            q.setMaxResults(1);
            return (ProfileStatus) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean setUserStatus(final ProfileStatus profileStatus) {
		
		try {
			//only allowing one status object per user, hence saveOrUpdate
			getHibernateTemplate().saveOrUpdate(profileStatus);
			return true;
		} catch (final Exception e) {
			log.error("ProfileLogic.setUserStatus() failed. " + e.getClass() + ": " + e.getMessage()); 
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean clearUserStatus(final ProfileStatus profileStatus) {
				
		try {
			getHibernateTemplate().delete(getHibernateTemplate().merge(profileStatus));
			return true;
		} catch (final Exception e) {
			log.error("ProfileLogic.clearUserStatus() failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getStatusUpdatesCount(final String userId) {
		
		final HibernateCallback<Number> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_STATUS_UPDATES_COUNT);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfilePrivacy addNewPrivacyRecord(final ProfilePrivacy privacy) {
		
		try {
			getHibernateTemplate().save(privacy);
			return privacy;
		} catch (final Exception e) {
			log.error("addPrivacyRecord failed. " + e.getClass() + ": " + e.getMessage());  
			return null;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfilePrivacy getPrivacyRecord(final String userId) {
		
		final HibernateCallback<ProfilePrivacy> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_PRIVACY_RECORD);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ProfilePrivacy) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean updatePrivacyRecord(final ProfilePrivacy privacy) {

		try {
			getHibernateTemplate().saveOrUpdate(privacy);
			return true;
		} catch (final Exception e) {
			log.error("updatePrivacyRecord failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addNewCompanyProfile(final CompanyProfile companyProfile) {
		
		try {
			getHibernateTemplate().save(companyProfile);
			return true;
		} catch (final Exception e) {
			log.error("addNewCompanyProfile failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean updateCompanyProfile(final CompanyProfile companyProfile) {

		try {
			getHibernateTemplate().saveOrUpdate(companyProfile);
			return true;
		} catch (final Exception e) {
			log.error("updateCompanyProfile failed. "+ e.getClass() + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<CompanyProfile> getCompanyProfiles(final String userId) {
		
		final HibernateCallback<List<CompanyProfile>> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_COMPANY_PROFILES);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean removeCompanyProfile(final CompanyProfile companyProfile) {

		try {
			getHibernateTemplate().delete(companyProfile);
			return true;
		} catch (final Exception e) {
			log.error("ProfileLogicImpl.removeCompanyProfile() failed. "+ e.getClass() + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean addNewGalleryImage(final GalleryImage galleryImage) {
		
		try {
			getHibernateTemplate().save(galleryImage);
			return true;
		} catch (final Exception e) {
			log.error("addNewGalleryImage failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<GalleryImage> getGalleryImages(final String userId) {
		
		final HibernateCallback<List<GalleryImage>> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_GALLERY_IMAGE_RECORDS);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean removeGalleryImage(final GalleryImage galleryImage) {
		
		try {
			getHibernateTemplate().delete(getHibernateTemplate().merge(galleryImage));
			return true;
		} catch (final Exception e) {
			log.error("removeGalleryImage failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getGalleryImagesCount(final String userId) {
		
		final HibernateCallback<Number> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_GALLERY_IMAGE_RECORDS_COUNT);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public SocialNetworkingInfo getSocialNetworkingInfo(final String userId) {
		
		final HibernateCallback<SocialNetworkingInfo> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_SOCIAL_NETWORKING_INFO);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (SocialNetworkingInfo) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean saveSocialNetworkingInfo(final SocialNetworkingInfo socialNetworkingInfo) {

		try {
			getHibernateTemplate().saveOrUpdate(socialNetworkingInfo);
			return true;
		} catch (final Exception e) {
			log.error("saveSocialNetworkingInfo failed. "+ e.getClass() + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean addNewProfileImage(final ProfileImageUploaded profileImage) {
		
		final Boolean success = getHibernateTemplate().execute(session -> {
            try {
                //first get the current ProfileImage records for this user
                final List<ProfileImageUploaded> currentImages = new ArrayList<>(getCurrentProfileImageRecords(profileImage.getUserUuid()));

                for(final ProfileImageUploaded currentImage : currentImages){
                    //invalidate each
                    currentImage.setCurrent(false);

                    //save
                    session.update(currentImage);
                }

                //now save the new one
                session.save(profileImage);

                // flush session
                session.flush();

            } catch(final Exception e) {
                log.error("addNewProfileImage failed. " + e.getClass() + ": " + e.getMessage());
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        });
		return success;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<String> getAllSakaiPersonIds() {
				
		//get 
		final HibernateCallback<List<String>> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_ALL_SAKAI_PERSON_IDS);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getAllSakaiPersonIdsCount() {
		
		//get 
		final HibernateCallback<Number> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_ALL_SAKAI_PERSON_IDS_COUNT);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<UserProfile> getUserProfiles(final int start, final int count) {
		
		//get fields directly from the sakaiperson table and use Transformers.aliasToBean to transform into UserProfile pojo
		//the idea is we *dont* want a SakaiPerson object
		final HibernateCallback<List<UserProfile>> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_SAKAI_PERSON);
            //see scalars in the hbm
            q.setFirstResult(start);
            q.setMaxResults(count);
            q.setResultTransformer(Transformers.aliasToBean(UserProfile.class));
            q.setCacheMode(CacheMode.GET);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileImageExternal getExternalImageRecordForUser(final String userId) {
		
		final HibernateCallback<ProfileImageExternal> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_EXTERNAL_IMAGE_RECORD);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ProfileImageExternal) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean saveExternalImage(final ProfileImageExternal externalImage) {
	
		try {
			getHibernateTemplate().saveOrUpdate(externalImage);
			return true;
		} catch (final Exception e) {
			log.error("saveExternalImage failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfilePreferences addNewPreferencesRecord(final ProfilePreferences prefs) {
		
		try {
			getHibernateTemplate().save(prefs);
			return prefs;
		} catch (final Exception e) {
			log.error("ProfileLogic.createDefaultPreferencesRecord() failed. " + e.getClass() + ": " + e.getMessage());  
			return null;
		}
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfilePreferences getPreferencesRecordForUser(final String userId) {
		
		final HibernateCallback<ProfilePreferences> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_PREFERENCES_RECORD);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ProfilePreferences) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}

	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean savePreferencesRecord(final ProfilePreferences prefs) {
		
		try {
			getHibernateTemplate().saveOrUpdate(prefs);
			return true;
		} catch (final Exception e) {
			log.error("savePreferencesRecord failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getAllUnreadMessagesCount(final String userId) {
		
		final HibernateCallback<Number> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_ALL_UNREAD_MESSAGES_COUNT);
            q.setParameter(UUID, userId, StringType.INSTANCE);
            q.setBoolean("false", Boolean.FALSE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getThreadsWithUnreadMessagesCount(final String userId) {
		
		final HibernateCallback<Number> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_THREADS_WITH_UNREAD_MESSAGES_COUNT);
            q.setParameter(UUID, userId, StringType.INSTANCE);
            q.setBoolean("false", Boolean.FALSE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<MessageThread> getMessageThreads(final String userId) {
		
		final HibernateCallback<List<MessageThread>> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_MESSAGE_THREADS);
            q.setParameter(UUID, userId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getMessageThreadsCount(final String userId) {
		
		final HibernateCallback<Number> hcb = session -> {

            final Query q = session.getNamedQuery(QUERY_GET_MESSAGE_THREADS_COUNT);
            q.setParameter(UUID, userId, StringType.INSTANCE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getSentMessagesCount(final String userId) {
		
		final HibernateCallback<Number> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_SENT_MESSAGES_COUNT);
            q.setParameter(UUID, userId, StringType.INSTANCE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<Message> getMessagesInThread(final String threadId) {
		
		final HibernateCallback<List<Message>> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_MESSAGES_IN_THREAD);
            q.setParameter(THREAD, threadId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getMessagesInThreadCount(final String threadId) {
		
		final HibernateCallback<Number> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_MESSAGES_IN_THREAD_COUNT);
            q.setParameter(THREAD, threadId, StringType.INSTANCE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public Message getMessage(final String id) {
		
		final HibernateCallback<Message> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_MESSAGE);
            q.setParameter(ID, id, StringType.INSTANCE);
            q.setMaxResults(1);
            return (Message) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public MessageThread getMessageThread(final String threadId) {
		
		final HibernateCallback<MessageThread> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_MESSAGE_THREAD);
            q.setParameter(ID, threadId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (MessageThread) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public Message getLatestMessageInThread(final String threadId) {
		
		final HibernateCallback<Message> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_LATEST_MESSAGE_IN_THREAD);
            q.setParameter(THREAD, threadId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (Message) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean toggleMessageRead(final MessageParticipant participant, final boolean read) {
		
		try {
			participant.setRead(read);
			getHibernateTemplate().saveOrUpdate(participant);
			return true;
		} catch (final Exception e) {
			log.error("toggleMessageRead failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public MessageParticipant getMessageParticipant(final String messageId, final String userUuid) {
		
		final HibernateCallback<MessageParticipant> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_MESSAGE_PARTICIPANT_FOR_MESSAGE_AND_UUID);
            q.setParameter(MESSAGE_ID, messageId, StringType.INSTANCE);
            q.setParameter(UUID, userUuid, StringType.INSTANCE);
            q.setMaxResults(1);
            return (MessageParticipant) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<String> getThreadParticipants(final String threadId) {
		
		//get
		final HibernateCallback<List<String>> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_THREAD_PARTICIPANTS);
            q.setParameter(THREAD, threadId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public void saveNewThread(final MessageThread thread) {
		
		try {
			getHibernateTemplate().save(thread);
			log.info("MessageThread saved with id= " + thread.getId());  
		} catch (final Exception e) {
			log.error("saveNewThread failed. " + e.getClass() + ": " + e.getMessage());  
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public void saveNewMessage(final Message message) {
		
		try {
			getHibernateTemplate().save(message);			
			log.info("Message saved with id= " + message.getId());  
		} catch (final Exception e) {
			log.error("saveNewMessage failed. " + e.getClass() + ": " + e.getMessage());  
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public void saveNewMessageParticipant(final MessageParticipant participant) {
		
		try {
			getHibernateTemplate().save(participant);
			log.info("MessageParticipant saved with id= " + participant.getId());  
		} catch (final Exception e) {
			log.error("saveNewMessageParticipant failed. " + e.getClass() + ": " + e.getMessage());  
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public void saveNewMessageParticipants(final List<MessageParticipant> participants) {
		
		for(final MessageParticipant participant : participants) {
		
			try {
				getHibernateTemplate().save(participant);
				log.info("MessageParticipant saved with id= " + participant.getId());  
			} catch (final Exception e) {
				log.error("saveNewMessageParticipant failed. " + e.getClass() + ": " + e.getMessage());  
			}
		}
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean saveOfficialImageUrl(final ProfileImageOfficial officialImage) {
		
		try {
			getHibernateTemplate().saveOrUpdate(officialImage);
			return true;
		} catch (final Exception e) {
			log.error("saveOfficialImageUrl failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileKudos getKudos(final String userUuid) {
				
		final HibernateCallback<ProfileKudos> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_KUDOS_RECORD);
            q.setParameter(USER_UUID, userUuid, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ProfileKudos) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean updateKudos(final ProfileKudos kudos) {
		try {
			getHibernateTemplate().saveOrUpdate(kudos);
			return true;
		} catch (final Exception e) {
			log.error("updateKudos failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ExternalIntegrationInfo getExternalIntegrationInfo(final String userUuid) {
				
		final HibernateCallback<ExternalIntegrationInfo> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_EXTERNAL_INTEGRATION_INFO);
            q.setParameter(USER_UUID, userUuid, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ExternalIntegrationInfo) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean updateExternalIntegrationInfo(final ExternalIntegrationInfo info) {
		try {
			getHibernateTemplate().saveOrUpdate(info);
			return true;
		} catch (final Exception e) {
			log.error("updateExternalIntegrationInfo failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean addNewWallItemForUser(final String userUuid, final WallItem item) {
		
		try {
			getHibernateTemplate().save(item);
			return true;
		} catch (final Exception e) {
			log.error("addNewWallItemForUser failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeWallItemFromWall(final WallItem item) {
		
		try {
			getHibernateTemplate().delete(item);
			return true;
		} catch (final Exception e) {
			log.error("removeWallItemFromWall failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WallItem getWallItem(final long wallItemId) {

		final HibernateCallback<List<WallItem>> hcb = session -> {

            final Query q = session.getNamedQuery(QUERY_GET_WALL_ITEM);
            q.setParameter(ID, wallItemId, LongType.INSTANCE);
            return q.list();
        };

		return getHibernateTemplate().execute(hcb).get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WallItemComment getWallItemComment(final long wallItemCommentId) {

		final HibernateCallback<List<WallItemComment>> hcb = session -> session.createCriteria(WallItemComment.class)
                .add(Restrictions.eq(ID, wallItemCommentId))
                .setFetchMode("wallItem", FetchMode.JOIN)
                .list();

		final List<WallItemComment> comments = getHibernateTemplate().execute(hcb);

		if (comments.size() > 0) {
		    return comments.get(0);
		} else {
			return null;
		}
	}

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<WallItem> getWallItemsForUser(final String userUuid) {
		
		final HibernateCallback<List<WallItem>> hcb = session -> {
            final Query q = session.getNamedQuery(QUERY_GET_WALL_ITEMS);
            q.setParameter(USER_UUID, userUuid, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean addNewCommentToWallItem(final WallItemComment wallItemComment) {
		try {
			getHibernateTemplate().save(wallItemComment);
			return true;
		} catch (final Exception e) {
			log.error("addNewWallItemComment failed. "+ e.getClass() + ": " + e.getMessage());
			return false;
		}
	}	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean invalidateCurrentProfileImage(final String userUuid) {
		try {
			final ProfileImageUploaded currentImage = getCurrentProfileImageRecord(userUuid);
			if (currentImage != null) {
				currentImage.setCurrent(false);
				getHibernateTemplate().save(currentImage);
				return true;
			} else {
				return false;
			}
		} catch (final Exception e) {
			log.error("invalidateCurrentProfileImage failed. "+ e.getClass() + ": " + e.getMessage());
			return false;
		}
	}
	
	public void init() {
	      log.debug("init");
	}
	
}
