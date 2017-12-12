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

import static sun.security.krb5.Confounder.intValue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.type.BooleanType;
import org.hibernate.type.DateType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.CacheMode;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

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
	public List<String> getRequestedConnectionUserIdsForUser(final String userId) {
				
		//get friends of this user [and map it automatically to the Friend object]
		//updated: now just returns a List of Strings
		HibernateCallback<List<String>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_FRIEND_REQUESTS_FOR_USER);
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
	public List<String> getOutgoingConnectionUserIdsForUser(final String userId) {

		//get friends of this user [and map it automatically to the Friend object]
		//updated: now just returns a List of Strings
		HibernateCallback<List<String>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_OUTGOING_FRIEND_REQUESTS_FOR_USER);
            q.setString(USER_UUID, userId);
            q.setBoolean("false", Boolean.FALSE);

            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> getConfirmedConnectionUserIdsForUser(final String userId) {
				
		//get 
		HibernateCallback<List<String>> hcb = session -> {
	  			Query q = session.getNamedQuery(QUERY_GET_CONFIRMED_FRIEND_USERIDS_FOR_USER);
	  			q.setParameter(USER_UUID, userId, StringType.INSTANCE);
	  			q.setBoolean("true", Boolean.TRUE); 
	  			return q.list();
	  	};
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> findSakaiPersonsByNameOrEmail(final String search) {
				
		//get 
		HibernateCallback<List<String>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_NAME_OR_EMAIL);
            q.setParameter(SEARCH, '%' + search + '%', StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> findSakaiPersonsByInterest(final String search, final boolean includeBusinessBio) {
		
		//get 
		HibernateCallback<List<String>> hcb = session -> {
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
	public List<ProfileImageUploaded> getCurrentProfileImageRecords(final String userId) {
				
		//get 
		HibernateCallback<List<ProfileImageUploaded>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfileImageUploaded getCurrentProfileImageRecord(final String userId) {
		
		HibernateCallback<ProfileImageUploaded> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ProfileImageUploaded) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<ProfileImageUploaded> getOtherProfileImageRecords(final String userId) {
				
		//get 
		HibernateCallback<List<ProfileImageUploaded>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_OTHER_PROFILE_IMAGE_RECORDS);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfileFriend getConnectionRecord(final String userId, final String friendId) {
		
		//this particular query checks for records when userId/friendId is in either column
		HibernateCallback<ProfileFriend> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_FRIEND_RECORD);
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
	public CompanyProfile getCompanyProfile(final String userId, final long companyProfileId) {

		HibernateCallback<CompanyProfile> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_COMPANY_PROFILE);
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
	public GalleryImage getGalleryImageRecord(final String userId, final long imageId) {
		
		HibernateCallback<GalleryImage> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_GALLERY_RECORD);
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
	public ProfileImageOfficial getOfficialImageRecordForUser(final String userUuid) {
		
		HibernateCallback<ProfileImageOfficial> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_OFFICIAL_IMAGE_RECORD);
            q.setParameter(USER_UUID, userUuid, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ProfileImageOfficial) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean addNewConnection(ProfileFriend profileFriend) {
		
		try {
			getHibernateTemplate().save(profileFriend);
			return true;
			
		} catch (Exception e) {
			log.error("requestFriend failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
		
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean updateConnection(ProfileFriend profileFriend) {
		
		try {
			getHibernateTemplate().update(profileFriend);
			return true;
		} catch (Exception e) {
			log.error("confirmFriendRequest failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean removeConnection(ProfileFriend profileFriend) {
		
		//delete
		try {
			getHibernateTemplate().delete(profileFriend);
			return true;
		} catch (Exception e) {
			log.error("removeConnection failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfileFriend getPendingConnection(final String userId, final String friendId) {
		
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null Argument in getPendingConnection"); 
	  	}
		
		HibernateCallback<ProfileFriend> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_FRIEND_REQUEST);
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
	public ProfileStatus getUserStatus(final String userId, final Date oldestDate) {
		
		HibernateCallback<ProfileStatus> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_USER_STATUS);
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
	public boolean setUserStatus(ProfileStatus profileStatus) {
		
		try {
			//only allowing one status object per user, hence saveOrUpdate
			getHibernateTemplate().saveOrUpdate(profileStatus);
			return true;
		} catch (Exception e) {
			log.error("ProfileLogic.setUserStatus() failed. " + e.getClass() + ": " + e.getMessage()); 
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean clearUserStatus(ProfileStatus profileStatus) {
				
		try {
			getHibernateTemplate().delete(profileStatus);
			return true;
		} catch (Exception e) {
			log.error("ProfileLogic.clearUserStatus() failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getStatusUpdatesCount(final String userId) {
		
		HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_STATUS_UPDATES_COUNT);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePrivacy addNewPrivacyRecord(ProfilePrivacy privacy) {
		
		try {
			getHibernateTemplate().save(privacy);
			return privacy;
		} catch (Exception e) {
			log.error("addPrivacyRecord failed. " + e.getClass() + ": " + e.getMessage());  
			return null;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePrivacy getPrivacyRecord(final String userId) {
		
		HibernateCallback<ProfilePrivacy> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_PRIVACY_RECORD);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ProfilePrivacy) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean updatePrivacyRecord(final ProfilePrivacy privacy) {

		try {
			getHibernateTemplate().saveOrUpdate(privacy);
			return true;
		} catch (Exception e) {
			log.error("updatePrivacyRecord failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean addNewCompanyProfile(final CompanyProfile companyProfile) {
		
		try {
			getHibernateTemplate().save(companyProfile);
			return true;
		} catch (Exception e) {
			log.error("addNewCompanyProfile failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean updateCompanyProfile(final CompanyProfile companyProfile) {

		try {
			getHibernateTemplate().saveOrUpdate(companyProfile);
			return true;
		} catch (Exception e) {
			log.error("updateCompanyProfile failed. "+ e.getClass() + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<CompanyProfile> getCompanyProfiles(final String userId) {
		
		HibernateCallback<List<CompanyProfile>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_COMPANY_PROFILES);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean removeCompanyProfile(final CompanyProfile companyProfile) {

		try {
			getHibernateTemplate().delete(companyProfile);
			return true;
		} catch (Exception e) {
			log.error("ProfileLogicImpl.removeCompanyProfile() failed. "+ e.getClass() + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean addNewGalleryImage(final GalleryImage galleryImage) {
		
		try {
			getHibernateTemplate().save(galleryImage);
			return true;
		} catch (Exception e) {
			log.error("addNewGalleryImage failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<GalleryImage> getGalleryImages(final String userId) {
		
		HibernateCallback<List<GalleryImage>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_GALLERY_IMAGE_RECORDS);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean removeGalleryImage(final GalleryImage galleryImage) {
		
		try {
			getHibernateTemplate().delete(galleryImage);
			return true;
		} catch (Exception e) {
			log.error("removeGalleryImage failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getGalleryImagesCount(final String userId) {
		
		HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_GALLERY_IMAGE_RECORDS_COUNT);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public SocialNetworkingInfo getSocialNetworkingInfo(final String userId) {
		
		HibernateCallback<SocialNetworkingInfo> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_SOCIAL_NETWORKING_INFO);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (SocialNetworkingInfo) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean saveSocialNetworkingInfo(SocialNetworkingInfo socialNetworkingInfo) {

		try {
			getHibernateTemplate().saveOrUpdate(socialNetworkingInfo);
			return true;
		} catch (Exception e) {
			log.error("saveSocialNetworkingInfo failed. "+ e.getClass() + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean addNewProfileImage(final ProfileImageUploaded profileImage) {
		
		Boolean success = getHibernateTemplate().execute(session -> {
            try {
                //first get the current ProfileImage records for this user
                List<ProfileImageUploaded> currentImages = new ArrayList<>(getCurrentProfileImageRecords(profileImage.getUserUuid()));

                for(ProfileImageUploaded currentImage : currentImages){
                    //invalidate each
                    currentImage.setCurrent(false);

                    //save
                    session.update(currentImage);
                }

                //now save the new one
                session.save(profileImage);

                // flush session
                session.flush();

            } catch(Exception e) {
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
	public List<String> getAllSakaiPersonIds() {
				
		//get 
		HibernateCallback<List<String>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_ALL_SAKAI_PERSON_IDS);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getAllSakaiPersonIdsCount() {
		
		//get 
		HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_ALL_SAKAI_PERSON_IDS_COUNT);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<UserProfile> getUserProfiles(final int start, final int count) {
		
		//get fields directly from the sakaiperson table and use Transformers.aliasToBean to transform into UserProfile pojo
		//the idea is we *dont* want a SakaiPerson object
		HibernateCallback<List<UserProfile>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_SAKAI_PERSON);
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
	public ProfileImageExternal getExternalImageRecordForUser(final String userId) {
		
		HibernateCallback<ProfileImageExternal> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_EXTERNAL_IMAGE_RECORD);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ProfileImageExternal) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean saveExternalImage(ProfileImageExternal externalImage) {
	
		try {
			getHibernateTemplate().saveOrUpdate(externalImage);
			return true;
		} catch (Exception e) {
			log.error("saveExternalImage failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePreferences addNewPreferencesRecord(ProfilePreferences prefs) {
		
		try {
			getHibernateTemplate().save(prefs);
			return prefs;
		} catch (Exception e) {
			log.error("ProfileLogic.createDefaultPreferencesRecord() failed. " + e.getClass() + ": " + e.getMessage());  
			return null;
		}
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId) {
		
		HibernateCallback<ProfilePreferences> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_PREFERENCES_RECORD);
            q.setParameter(USER_UUID, userId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ProfilePreferences) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}

	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean savePreferencesRecord(ProfilePreferences prefs) {
		
		try {
			getHibernateTemplate().saveOrUpdate(prefs);
			return true;
		} catch (Exception e) {
			log.error("savePreferencesRecord failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getAllUnreadMessagesCount(final String userId) {
		
		HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_ALL_UNREAD_MESSAGES_COUNT);
            q.setParameter(UUID, userId, StringType.INSTANCE);
            q.setBoolean("false", Boolean.FALSE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getThreadsWithUnreadMessagesCount(final String userId) {
		
		HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_THREADS_WITH_UNREAD_MESSAGES_COUNT);
            q.setParameter(UUID, userId, StringType.INSTANCE);
            q.setBoolean("false", Boolean.FALSE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<MessageThread> getMessageThreads(final String userId) {
		
		HibernateCallback<List<MessageThread>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_MESSAGE_THREADS);
            q.setParameter(UUID, userId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getMessageThreadsCount(final String userId) {
		
		HibernateCallback<Number> hcb = session -> {

            Query q = session.getNamedQuery(QUERY_GET_MESSAGE_THREADS_COUNT);
            q.setParameter(UUID, userId, StringType.INSTANCE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getSentMessagesCount(final String userId) {
		
		HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_SENT_MESSAGES_COUNT);
            q.setParameter(UUID, userId, StringType.INSTANCE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<Message> getMessagesInThread(final String threadId) {
		
		HibernateCallback<List<Message>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_MESSAGES_IN_THREAD);
            q.setParameter(THREAD, threadId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getMessagesInThreadCount(final String threadId) {
		
		HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_MESSAGES_IN_THREAD_COUNT);
            q.setParameter(THREAD, threadId, StringType.INSTANCE);
            return (Number) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public Message getMessage(final String id) {
		
		HibernateCallback<Message> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_MESSAGE);
            q.setParameter(ID, id, StringType.INSTANCE);
            q.setMaxResults(1);
            return (Message) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public MessageThread getMessageThread(final String threadId) {
		
		HibernateCallback<MessageThread> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_MESSAGE_THREAD);
            q.setParameter(ID, threadId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (MessageThread) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public Message getLatestMessageInThread(final String threadId) {
		
		HibernateCallback<Message> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_LATEST_MESSAGE_IN_THREAD);
            q.setParameter(THREAD, threadId, StringType.INSTANCE);
            q.setMaxResults(1);
            return (Message) q.uniqueResult();
      };
	
		return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean toggleMessageRead(MessageParticipant participant, final boolean read) {
		
		try {
			participant.setRead(read);
			getHibernateTemplate().saveOrUpdate(participant);
			return true;
		} catch (Exception e) {
			log.error("toggleMessageRead failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public MessageParticipant getMessageParticipant(final String messageId, final String userUuid) {
		
		HibernateCallback<MessageParticipant> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_MESSAGE_PARTICIPANT_FOR_MESSAGE_AND_UUID);
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
	public List<String> getThreadParticipants(final String threadId) {
		
		//get
		HibernateCallback<List<String>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_THREAD_PARTICIPANTS);
            q.setParameter(THREAD, threadId, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public void saveNewThread(MessageThread thread) {
		
		try {
			getHibernateTemplate().save(thread);
			log.info("MessageThread saved with id= " + thread.getId());  
		} catch (Exception e) {
			log.error("saveNewThread failed. " + e.getClass() + ": " + e.getMessage());  
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public void saveNewMessage(Message message) {
		
		try {
			getHibernateTemplate().save(message);			
			log.info("Message saved with id= " + message.getId());  
		} catch (Exception e) {
			log.error("saveNewMessage failed. " + e.getClass() + ": " + e.getMessage());  
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public void saveNewMessageParticipant(MessageParticipant participant) {
		
		try {
			getHibernateTemplate().save(participant);
			log.info("MessageParticipant saved with id= " + participant.getId());  
		} catch (Exception e) {
			log.error("saveNewMessageParticipant failed. " + e.getClass() + ": " + e.getMessage());  
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public void saveNewMessageParticipants(List<MessageParticipant> participants) {
		
		for(MessageParticipant participant : participants) {
		
			try {
				getHibernateTemplate().save(participant);
				log.info("MessageParticipant saved with id= " + participant.getId());  
			} catch (Exception e) {
				log.error("saveNewMessageParticipant failed. " + e.getClass() + ": " + e.getMessage());  
			}
		}
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean saveOfficialImageUrl(ProfileImageOfficial officialImage) {
		
		try {
			getHibernateTemplate().saveOrUpdate(officialImage);
			return true;
		} catch (Exception e) {
			log.error("saveOfficialImageUrl failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfileKudos getKudos(final String userUuid) {
				
		HibernateCallback<ProfileKudos> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_KUDOS_RECORD);
            q.setParameter(USER_UUID, userUuid, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ProfileKudos) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean updateKudos(ProfileKudos kudos) {
		try {
			getHibernateTemplate().saveOrUpdate(kudos);
			return true;
		} catch (Exception e) {
			log.error("updateKudos failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public ExternalIntegrationInfo getExternalIntegrationInfo(final String userUuid) {
				
		HibernateCallback<ExternalIntegrationInfo> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_EXTERNAL_INTEGRATION_INFO);
            q.setParameter(USER_UUID, userUuid, StringType.INSTANCE);
            q.setMaxResults(1);
            return (ExternalIntegrationInfo) q.uniqueResult();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean updateExternalIntegrationInfo(ExternalIntegrationInfo info) {
		try {
			getHibernateTemplate().saveOrUpdate(info);
			return true;
		} catch (Exception e) {
			log.error("updateExternalIntegrationInfo failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean addNewWallItemForUser(final String userUuid, final WallItem item) {
		
		try {
			getHibernateTemplate().save(item);
			return true;
		} catch (Exception e) {
			log.error("addNewWallItemForUser failed. " + e.getClass() + ": " + e.getMessage());  
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean removeWallItemFromWall(final WallItem item) {
		
		try {
			getHibernateTemplate().delete(item);
			return true;
		} catch (Exception e) {
			log.error("removeWallItemFromWall failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public WallItem getWallItem(final long wallItemId) {

		HibernateCallback<List<WallItem>> hcb = session -> {

            Query q = session.getNamedQuery(QUERY_GET_WALL_ITEM);
            q.setParameter(ID, wallItemId, LongType.INSTANCE);
            return q.list();
        };

		return getHibernateTemplate().execute(hcb).get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public WallItemComment getWallItemComment(final long wallItemCommentId) {

		HibernateCallback<List<WallItemComment>> hcb = session -> session.createCriteria(WallItemComment.class)
                .add(Restrictions.eq(ID, wallItemCommentId))
                .setFetchMode("wallItem", FetchMode.JOIN)
                .list();

		List<WallItemComment> comments = getHibernateTemplate().execute(hcb);

		if (comments.size() > 0) {
		    return comments.get(0);
		} else {
			return null;
		}
	}

	/**
 	 * {@inheritDoc}
 	 */
	public List<WallItem> getWallItemsForUser(final String userUuid) {
		
		HibernateCallback<List<WallItem>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_WALL_ITEMS);
            q.setParameter(USER_UUID, userUuid, StringType.INSTANCE);
            return q.list();
        };
	  	
	  	return getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean addNewCommentToWallItem(WallItemComment wallItemComment) {
		try {
			getHibernateTemplate().save(wallItemComment);
			return true;
		} catch (Exception e) {
			log.error("addNewWallItemComment failed. "+ e.getClass() + ": " + e.getMessage());
			return false;
		}
	}	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean invalidateCurrentProfileImage(final String userUuid) {
		try {
			ProfileImageUploaded currentImage = getCurrentProfileImageRecord(userUuid);
			currentImage.setCurrent(false);
			getHibernateTemplate().save(currentImage);
			return true;
		} catch (Exception e) {
			log.error("invalidateCurrentProfileImage failed. "+ e.getClass() + ": " + e.getMessage());
			return false;
		}
	}
	
	public void init() {
	      log.debug("init");
	}
	
}
