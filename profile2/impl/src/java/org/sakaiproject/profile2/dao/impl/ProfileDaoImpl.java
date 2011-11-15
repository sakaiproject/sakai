package org.sakaiproject.profile2.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
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
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Internal DAO Interface for Profile2
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfileDaoImpl extends HibernateDaoSupport implements ProfileDao {

	private static final Logger log = Logger.getLogger(ProfileDaoImpl.class);

	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> getRequestedConnectionUserIdsForUser(final String userId) {
				
		//get friends of this user [and map it automatically to the Friend object]
		//updated: now just returns a List of Strings
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_FRIEND_REQUESTS_FOR_USER);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setBoolean("false", Boolean.FALSE); 
	  			//q.setResultTransformer(Transformers.aliasToBean(Friend.class));
	  			
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<String>) getHibernateTemplate().executeFind(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> getConfirmedConnectionUserIdsForUser(final String userId) {
				
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  		
	  			Query q = session.getNamedQuery(QUERY_GET_CONFIRMED_FRIEND_USERIDS_FOR_USER);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setBoolean("true", Boolean.TRUE); 
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<String>) getHibernateTemplate().executeFind(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> findSakaiPersonsByNameOrEmail(final String search) {
				
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_NAME_OR_EMAIL);
	  			q.setParameter(SEARCH, '%' + search + '%', Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<String>) getHibernateTemplate().executeFind(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> findSakaiPersonsByInterest(final String search, final boolean includeBusinessBio) {
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  	
	  			Query q;
	  			if (false == includeBusinessBio) {
	  				q = session.getNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_INTEREST);
	  			} else {
	  				q = session.getNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_INTEREST_AND_BUSINESS_BIO);
	  			}
	  			q.setParameter(SEARCH, '%' + search + '%', Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<String>) getHibernateTemplate().executeFind(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<ProfileImageUploaded> getCurrentProfileImageRecords(final String userId) {
				
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<ProfileImageUploaded>) getHibernateTemplate().executeFind(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfileImageUploaded getCurrentProfileImageRecord(final String userId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileImageUploaded) getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<ProfileImageUploaded> getOtherProfileImageRecords(final String userId) {
				
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_OTHER_PROFILE_IMAGE_RECORDS);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<ProfileImageUploaded>) getHibernateTemplate().executeFind(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfileFriend getConnectionRecord(final String userId, final String friendId) {
		
		//this particular query checks for records when userId/friendId is in either column
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_FRIEND_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setParameter(FRIEND_UUID, friendId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileFriend) getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public CompanyProfile getCompanyProfile(final String userId, final long companyProfileId) {

		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_COMPANY_PROFILE);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setParameter(ID, companyProfileId, Hibernate.LONG);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (CompanyProfile) getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public GalleryImage getGalleryImageRecord(final String userId, final long imageId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_GALLERY_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setParameter(ID, imageId, Hibernate.LONG);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (GalleryImage) getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfileImageOfficial getOfficialImageRecordForUser(final String userUuid) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_OFFICIAL_IMAGE_RECORD);
	  			q.setParameter(USER_UUID, userUuid, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileImageOfficial) getHibernateTemplate().execute(hcb);
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
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_FRIEND_REQUEST);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setParameter(FRIEND_UUID, friendId, Hibernate.STRING);
	  			q.setParameter(CONFIRMED, false, Hibernate.BOOLEAN);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileFriend) getHibernateTemplate().execute(hcb);
	}

	/**
 	 * {@inheritDoc}
 	 */
	public ProfileStatus getUserStatus(final String userId, final Date oldestDate) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_USER_STATUS);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setParameter(OLDEST_STATUS_DATE, oldestDate, Hibernate.DATE);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileStatus) getHibernateTemplate().execute(hcb);
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
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_STATUS_UPDATES_COUNT);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			return q.uniqueResult();
	  		}
	  	};
	  	
	  	return ((Integer)getHibernateTemplate().execute(hcb)).intValue();
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
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_PRIVACY_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfilePrivacy) getHibernateTemplate().execute(hcb);
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
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_COMPANY_PROFILES);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<CompanyProfile>) getHibernateTemplate().executeFind(hcb);
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
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_GALLERY_IMAGE_RECORDS);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<GalleryImage>) getHibernateTemplate().executeFind(hcb);
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
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_GALLERY_IMAGE_RECORDS_COUNT);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			return q.uniqueResult();
	  		}
	  	};
	  	
	  	return ((Integer)getHibernateTemplate().execute(hcb)).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public SocialNetworkingInfo getSocialNetworkingInfo(final String userId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_SOCIAL_NETWORKING_INFO);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (SocialNetworkingInfo) getHibernateTemplate().execute(hcb);
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
		
		Boolean success = (Boolean) getHibernateTemplate().execute(new HibernateCallback() {			
				public Object doInHibernate(Session session){
					try {
						//first get the current ProfileImage records for this user
						List<ProfileImageUploaded> currentImages = new ArrayList<ProfileImageUploaded>(getCurrentProfileImageRecords(profileImage.getUserUuid()));
            
						for(Iterator<ProfileImageUploaded> i = currentImages.iterator(); i.hasNext();){
							ProfileImageUploaded currentImage = (ProfileImageUploaded)i.next();
              
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
				}			
		});
		return success.booleanValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> getAllSakaiPersonIds() {
				
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_ALL_SAKAI_PERSON_IDS);
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<String>) getHibernateTemplate().executeFind(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getAllSakaiPersonIdsCount() {
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_ALL_SAKAI_PERSON_IDS_COUNT);
	  			return q.uniqueResult();
	  		}
	  	};
	  	
	  	return ((Integer)getHibernateTemplate().execute(hcb)).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<UserProfile> getUserProfiles(final int start, final int count) {
		
		//get fields directly from the sakaiperson table and use Transformers.aliasToBean to transform into UserProfile pojo
		//the idea is we *dont* want a SakaiPerson object
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  		
	  			Query q = session.getNamedQuery(QUERY_GET_SAKAI_PERSON);

	  			//see scalars in the hbm
	  			q.setFirstResult(start);
	  			q.setMaxResults(count);
	  			q.setResultTransformer(Transformers.aliasToBean(UserProfile.class));
	  			q.setCacheMode(CacheMode.GET);
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<UserProfile>) getHibernateTemplate().executeFind(hcb);
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfileImageExternal getExternalImageRecordForUser(final String userId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_EXTERNAL_IMAGE_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileImageExternal) getHibernateTemplate().execute(hcb);
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
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_PREFERENCES_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfilePreferences) getHibernateTemplate().execute(hcb);
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
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_ALL_UNREAD_MESSAGES_COUNT);
	  			q.setParameter(UUID, userId, Hibernate.STRING);
	  			q.setBoolean("false", Boolean.FALSE);
	  			return q.uniqueResult();
	  		}
	  	};
	  	
	  	return ((Integer)getHibernateTemplate().execute(hcb)).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getThreadsWithUnreadMessagesCount(final String userId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_THREADS_WITH_UNREAD_MESSAGES_COUNT);
	  			q.setParameter(UUID, userId, Hibernate.STRING);
	  			q.setBoolean("false", Boolean.FALSE);
	  			return q.uniqueResult();
	  		}
	  	};
	  	
	  	return ((Integer)getHibernateTemplate().execute(hcb)).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<MessageThread> getMessageThreads(final String userId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  		
	  			Query q = session.getNamedQuery(QUERY_GET_MESSAGE_THREADS);
	  			q.setParameter(UUID, userId, Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<MessageThread>) getHibernateTemplate().executeFind(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getMessageThreadsCount(final String userId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_MESSAGE_THREADS_COUNT);
	  			q.setParameter(UUID, userId, Hibernate.STRING);
	  			return q.uniqueResult();
	  		}
	  	};
	  	
	  	return ((Integer)getHibernateTemplate().execute(hcb)).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getSentMessagesCount(final String userId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_SENT_MESSAGES_COUNT);
	  			q.setParameter(UUID, userId, Hibernate.STRING);
	  			return q.uniqueResult();
	  		}
	  	};
	  	
	  	return ((Integer)getHibernateTemplate().execute(hcb)).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<Message> getMessagesInThread(final String threadId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  		
	  			Query q = session.getNamedQuery(QUERY_GET_MESSAGES_IN_THREAD);
	  			q.setParameter(THREAD, threadId, Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<Message>) getHibernateTemplate().executeFind(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getMessagesInThreadCount(final String threadId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  		
	  			Query q = session.getNamedQuery(QUERY_GET_MESSAGES_IN_THREAD_COUNT);
	  			q.setParameter(THREAD, threadId, Hibernate.STRING);
	  			return q.uniqueResult();
	  		}
	  	};
	  	
	  	return ((Integer)getHibernateTemplate().execute(hcb)).intValue();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public Message getMessage(final String id) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_MESSAGE);
	  			q.setParameter(ID, id, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (Message) getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public MessageThread getMessageThread(final String threadId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_MESSAGE_THREAD);
	  			q.setParameter(ID, threadId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (MessageThread)getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public Message getLatestMessageInThread(final String threadId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_LATEST_MESSAGE_IN_THREAD);
	  			q.setParameter(THREAD, threadId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (Message) getHibernateTemplate().execute(hcb);
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
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_MESSAGE_PARTICIPANT_FOR_MESSAGE_AND_UUID);
	  			q.setParameter(MESSAGE_ID, messageId, Hibernate.STRING);
	  			q.setParameter(UUID, userUuid, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (MessageParticipant) getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> getThreadParticipants(final String threadId) {
		
		//get
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_THREAD_PARTICIPANTS);
	  			q.setParameter(THREAD, threadId, Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<String>) getHibernateTemplate().executeFind(hcb);
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
				
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_KUDOS_RECORD);
	  			q.setParameter(USER_UUID, userUuid, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
	  		}
	  	};
	  	
	  	return (ProfileKudos) getHibernateTemplate().execute(hcb);
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
				
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_EXTERNAL_INTEGRATION_INFO);
	  			q.setParameter(USER_UUID, userUuid, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
	  		}
	  	};
	  	
	  	return (ExternalIntegrationInfo) getHibernateTemplate().execute(hcb);
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
	public List<WallItem> getWallItemsForUser(final String userUuid) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_WALL_ITEMS);
	  			q.setParameter(USER_UUID, userUuid, Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	return (List<WallItem>) getHibernateTemplate().executeFind(hcb);
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
	
	public void init() {
	      log.debug("init");
	}
	
}
