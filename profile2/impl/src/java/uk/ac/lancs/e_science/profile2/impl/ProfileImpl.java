package uk.ac.lancs.e_science.profile2.impl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.ProfilePrivacyManager;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.hbm.Friend;
import uk.ac.lancs.e_science.profile2.hbm.ProfileFriend;
import uk.ac.lancs.e_science.profile2.hbm.ProfileImage;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePrivacy;
import uk.ac.lancs.e_science.profile2.hbm.ProfileStatus;
import uk.ac.lancs.e_science.profile2.hbm.SearchResult;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;


public class ProfileImpl extends HibernateDaoSupport implements Profile {

	private transient Logger log = Logger.getLogger(ProfileImpl.class);
	
	//surely this is in the Calendar API somewhere
	private static final String[] DAY_OF_WEEK_MAPPINGS = { "", "Sunday", "Monday",
		"Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
	
	
	/*
	 * Eventually may come from the database. For now, only FRIEND is used.
	 */
	private final int RELATIONSHIP_FRIEND = 1;
	private final int RELATIONSHIP_COLLEAGUE = 2;
	
	private static final String QUERY_GET_FRIENDS_FOR_USER = "getFriendsForUser";
	private static final String QUERY_GET_FRIEND_REQUESTS_FOR_USER = "getFriendRequestsForUser";
	private static final String QUERY_GET_CONFIRMED_FRIEND_USERIDS_FOR_USER = "getConfirmedFriendUserIdsForUser";
	private static final String QUERY_GET_FRIEND_REQUEST = "getFriendRequest";
	private static final String QUERY_GET_FRIEND_RECORD = "getFriendRecord";
	private static final String QUERY_GET_USER_STATUS = "getUserStatus";
	private static final String QUERY_GET_PRIVACY_RECORD = "getPrivacyRecord";
	private static final String QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD = "getCurrentProfileImageRecord";
	private static final String QUERY_OTHER_PROFILE_IMAGE_RECORDS = "getOtherProfileImageRecords";
	private static final String QUERY_FIND_SAKAI_PERSONS_BY_NAME_OR_EMAIL = "findSakaiPersonsByNameOrEmail";
	private static final String QUERY_FIND_SAKAI_PERSONS_BY_INTEREST = "findSakaiPersonsByInterest";
	private static final String QUERY_LIST_ALL_SAKAI_PERSONS = "listAllSakaiPersons";
	
	//Hibernate object fields
	private static final String USER_UUID = "userUuid";
	private static final String FRIEND_UUID = "friendUuid";
	private static final String CONFIRMED = "confirmed";
	private static final String OLDEST_STATUS_DATE = "oldestStatusDate";
	private static final String SEARCH = "search";
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#checkContentTypeForProfileImage()
	 */
	public boolean checkContentTypeForProfileImage(String contentType) {
		
		ArrayList<String> allowedTypes = new ArrayList<String>();
		allowedTypes.add("image/jpeg");
		allowedTypes.add("image/gif");
		allowedTypes.add("image/png");

		if(allowedTypes.contains(contentType)) {
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#scaleImage()
	 */
	public byte[] scaleImage (byte[] imageData, int maxSize) {
	
	    if (log.isDebugEnabled()) {
	    	log.debug("Scaling image...");
	    }
	    // Get the image from a file.
	    Image inImage = new ImageIcon(imageData).getImage();
	
	    // Determine the scale (we could change this to only determine scale from one dimension, ie the width only?)
	    double scale = (double) maxSize / (double) inImage.getHeight(null);
	    if (inImage.getWidth(null) > inImage.getHeight(null)) {
	        scale = (double) maxSize / (double) inImage.getWidth(null);
	    }
	    
	    System.out.println("===========Image scaling============");
	    System.out.println("WIDTH: " + inImage.getWidth(null));
	    System.out.println("HEIGHT: " + inImage.getHeight(null));
	    System.out.println("SCALE: " + scale);
	    System.out.println("========End of image scaling========");

	    //if image is smaller than desired image size (ie scale is larger) just return the original image bytes
	    if (scale >= 1.0d) {
	    	return imageData;
	    }
	    
	    
	
	    // Determine size of new image.
	    // One of the dimensions should equal maxSize.
	    int scaledW = (int) (scale * inImage.getWidth(null));
	    int scaledH = (int) (scale * inImage.getHeight(null));
	
	    // Create an image buffer in which to paint on.
	    BufferedImage outImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_RGB);
	
	    // Set the scale.
	    AffineTransform tx = new AffineTransform();
	
	    //scale
	    tx.scale(scale, scale);
	
	    // Paint image.
	    Graphics2D g2d = outImage.createGraphics();
	    g2d.setRenderingHint(
	            RenderingHints.KEY_ANTIALIASING,
	            RenderingHints.VALUE_ANTIALIAS_ON
	        );
	    g2d.drawImage(inImage, tx, null);
	    g2d.dispose();
	
	    // JPEG-encode the image
	    // and write to file.
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    try { 
	    	JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
	    	encoder.encode(outImage);
	    	os.close();
	    	if (log.isDebugEnabled()) {
	    		log.debug("Scaling done.");
	    	}
	    } catch (IOException e) {
	    	log.error("Scaling image failed.");
	    }
	    return os.toByteArray();
	}
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#convertDateToString()
	 */
	public String convertDateToString(Date date, String format) {
		
		if(date == null || "".equals(format)) {
			throw new IllegalArgumentException("Null Argument in Profile.convertDateToString()");	
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        StringBuilder dateStr = new StringBuilder(dateFormat.format(date));
        
        System.out.println("convertDateToString(), Input date: " + date.toString());
        System.out.println("convertDateToString(), Converted date string: " + dateStr);

		return dateStr.toString();
		
	}
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#convertStringToDate()
	 */
	public Date convertStringToDate(String dateStr, String format) {
		
		if("".equals(dateStr) || "".equals(format)) {
			throw new IllegalArgumentException("Null Argument in Profile.convertStringToDate()");	
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		 
		try {
			Date date = dateFormat.parse(dateStr);
			
			System.out.println("convertStringToDate(), Input date string: " + dateStr);
			System.out.println("convertStringToDate(), Converted date: " + date.toString());
			
			return date;
		} catch (Exception e) {
			log.error("convertStringToDate() failed. " + e.getClass() + ": " + e.getMessage());
			return null;
		}            
        
	}


	

	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getFriendsForUser()
	 */
	public List<Friend> getFriendsForUser(final String userId, final int limit) {
		if(userId == null){
	  		throw new IllegalArgumentException("Null Argument in getFriendsForUser");
	  	}
		
		List<Friend> friends = new ArrayList();
			
		//get friends of this user and map it automatically to the Friend object
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			/* returns too much data when getting the photos as well.
	  			Query q = session.createSQLQuery("select PROFILE_FRIENDS_T.FRIEND_UUID as userUuid, PROFILE_FRIENDS_T.CONFIRMED as confirmed, PROFILE_STATUS_T.MESSAGE as statusMessage, PROFILE_STATUS_T.DATE_ADDED as statusDate, SAKAI_PERSON_T.JPEG_PHOTO as photo from PROFILE_FRIENDS_T left join PROFILE_STATUS_T on PROFILE_FRIENDS_T.FRIEND_UUID=PROFILE_STATUS_T.USER_UUID left join SAKAI_PERSON_T on PROFILE_FRIENDS_T.FRIEND_UUID=SAKAI_PERSON_T.AGENT_UUID where PROFILE_FRIENDS_T.USER_UUID = :userUuid")
	  				.addScalar("photo", Hibernate.BYTE);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setResultTransformer(Transformers.aliasToBean(Friend.class));
	  			*/
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_FRIENDS_FOR_USER);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setResultTransformer(Transformers.aliasToBean(Friend.class));
	  			
	  			//limit of 0 = unlimited, else set limit
	  			if(limit > 0) {
	  				q.setMaxResults(limit);
	  			}
	  			
	  			return q.list();
	  		}
	  	};
	  	
	  	friends = (List<Friend>) getHibernateTemplate().executeFind(hcb);
	  	
	  	return friends;

	}
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getFriendsForUser()
	 */
	public List<Friend> getFriendRequestsForUser(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null Argument in getFriendsForUser");
	  	}
		
		List<Friend> requests = new ArrayList();
		
		//get friends of this user and map it automatically to the Friend object
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_FRIEND_REQUESTS_FOR_USER);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setResultTransformer(Transformers.aliasToBean(Friend.class));
	  			
	  			return q.list();
	  		}
	  	};
	  	
	  	requests = (List<Friend>) getHibernateTemplate().executeFind(hcb);
	  	
	  	return requests;
	}
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getConfirmedFriendUserIdsForUser(String userId)
	 */	
	public List<String> getConfirmedFriendUserIdsForUser(final String userId) {
		
		List<String> userUuids = new ArrayList<String>();
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_CONFIRMED_FRIEND_USERIDS_FOR_USER);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	userUuids = (List<String>) getHibernateTemplate().executeFind(hcb);
	
	  	return userUuids;

	
	}
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#requestFriend(String userId, String friendId)
	 */	
	public boolean requestFriend(String userId, String friendId) {
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null Argument in getFriendsForUser");
	  	}
		
		//check values are valid, ie userId, friendId etc
		
		try {
			//make a ProfileFriend object with 'Friend Request' constructor
			ProfileFriend profileFriend = new ProfileFriend(userId, friendId, RELATIONSHIP_FRIEND);
			getHibernateTemplate().save(profileFriend);
			log.info("User: " + userId + " requested friend: " + friendId);
			return true;
		} catch (Exception e) {
			log.error("requestFriend() failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
	
	}
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#isFriendRequestPending(String userId, String friendId)
	 */	
	public boolean isFriendRequestPending(String userId, String friendId) {
		
		ProfileFriend profileFriend = getPendingFriendRequest(userId, friendId);

		if(profileFriend == null) {
			log.warn("No pending friend request from userId: " + userId + " to friendId: " + friendId + " found.");
			return false;
		}
		return true;
	}
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#confirmFriend(final String friendId, final String userId)
	 */
	public boolean confirmFriend(final String userId, final String friendId) {
		
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null Argument in confirmFriend");
	  	}
		
		//get pending ProfileFriend object request for the given details
		ProfileFriend profileFriend = getPendingFriendRequest(userId, friendId);

		if(profileFriend == null) {
			log.warn("confirmFriend() failed. No pending friend request from userId: " + userId + " to friendId: " + friendId + " found.");
			return false;
		}
		
	  	//make necessary changes to the ProfileFriend object.
	  	profileFriend.setConfirmed(true);
	  	profileFriend.setConfirmedDate(new Date());
		
		//save
		try {
			getHibernateTemplate().update(profileFriend);
			log.info("User: " + friendId + " confirmed friend request from: " + userId);
			return true;
		} catch (Exception e) {
			log.error("confirmFriend() failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
	
	}
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#removeFriend(String userId, String friendId)
	 */
	public boolean removeFriend(String userId, String friendId) {
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null Argument in removeFriend");
	  	}
		
		//get the friend object for this connection pair (could be any way around)
		ProfileFriend profileFriend = getFriendRecord(userId, friendId);
		
		if(profileFriend == null){
			log.error("ProfileFriend null for userId: " + userId + ", friendId: " + friendId);
			return false;
		}
				
		//if ok, delete it
		try {
			getHibernateTemplate().delete(profileFriend);
			log.info("User: " + userId + " deleted friend: " + friendId);
			return true;
		} catch (Exception e) {
			log.error("removeFriend() failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
		
		
	}
	
	//only gets a pending request
	private ProfileFriend getPendingFriendRequest(final String userId, final String friendId) {
		
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null Argument in getPendingFriendRequest");
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
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getUnreadMessagesCount()
	 */
	public int getUnreadMessagesCount(String userId) {
		int unreadMessages = 0;
		return unreadMessages;
		
	}
	
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getUserStatus()
	 */
	public ProfileStatus getUserStatus(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null Argument in getUserStatus");
	  	}
		
		// compute oldest date for status 
		Calendar cal = Calendar.getInstance(); 
		cal.add(Calendar.DAY_OF_YEAR, -7); 
		final Date oldestStatusDate = cal.getTime(); 

		//System.out.println("oldest time is: " + cal.getTimeInMillis());
		
				
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_USER_STATUS);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setParameter(OLDEST_STATUS_DATE, oldestStatusDate, Hibernate.DATE);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileStatus) getHibernateTemplate().execute(hcb);
	}
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getUserStatusMessage()
	 */
	public String getUserStatusMessage(String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null Argument in getUserStatusMessage");
	  	}
		
		ProfileStatus profileStatus = getUserStatus(userId);
		if(profileStatus == null) {
			return null;
		}
		return profileStatus.getMessage();
	}
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getUserStatusDate()
	 */
	public Date getUserStatusDate(String userId) {
		if(userId == null){
	  		throw new IllegalArgumentException("Null Argument in getUserStatusDate");
	  	}
		
		ProfileStatus profileStatus = getUserStatus(userId);
		if(profileStatus == null) {
			return null;
		}
		return profileStatus.getDateAdded();
	}
	
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#setUserStatus()
	 */
	public boolean setUserStatus(String userId, String status) {
		
		//validate userId here - TODO
		
		ProfileStatus profileStatus = new ProfileStatus(userId,status,new Date());
		
		//this now uses saveOrUpdate as we are only allowing single status records
		//so that we can get the friends/statuses more easily via single SQL statements
		try {
			getHibernateTemplate().saveOrUpdate(profileStatus);
			log.info("Updated status for user: " + userId);
			return true;
		} catch (Exception e) {
			log.error("setUserStatus() failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
		
	}
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#clearUserStatus()
	 */
	public boolean clearUserStatus(String userId) {
		
		//validate userId here - TODO
		
		ProfileStatus profileStatus = getUserStatus(userId);
		
		if(profileStatus == null){
			log.error("ProfileStatus null for userId: " + userId);
			return false;
		}
				
		//if ok, delete it
		try {
			getHibernateTemplate().delete(profileStatus);
			log.info("User: " + userId + " cleared status");
			return true;
		} catch (Exception e) {
			log.error("clearUserStatus() failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
		
	}

	
	

	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#convertDateForStatus()
	 */
	public String convertDateForStatus(Date date) {
		
		//current time (can also specify timezome and local here, see API)
		Calendar currentCal = Calendar.getInstance();
		long currentTimeMillis = currentCal.getTimeInMillis();
		
		//posting time (set calendar time to be this)
		//Calendar postingDate = Calendar.getInstance();
		//postingDate.setTimeInMillis(date.getTime());
		long postingTimeMillis = date.getTime();
		
		//difference
		int diff = (int)(currentTimeMillis - postingTimeMillis);
		
		
		//System.out.println("currentDate:" + currentTimeMillis);
		//System.out.println("postingDate:" + postingTimeMillis);
		//System.out.println("diff:" + diff);
		
		
		int MILLIS_IN_SECOND = 1000;
		int MILLIS_IN_MINUTE = 1000 * 60;
		int MILLIS_IN_HOUR = 1000 * 60 * 60;
		int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
		int MILLIS_IN_WEEK = 1000 * 60 * 60 * 24 * 7;

		String message="";
		
		if(diff < MILLIS_IN_SECOND) {
			//less than a second
			message = "just then";
		} else if (diff < MILLIS_IN_MINUTE) {
			//less than a minute, calc seconds
			int numSeconds = diff/MILLIS_IN_SECOND;
			message = numSeconds + " seconds ago";
		} else if (diff < MILLIS_IN_HOUR) {
			//less than an hour, calc minutes
			int numMinutes = diff/MILLIS_IN_MINUTE;
			message = numMinutes + " minutes ago";
		} else if (diff < MILLIS_IN_DAY) {
			//less than a day, calc hours
			int numHours = diff/MILLIS_IN_HOUR;
			message = numHours + " hours ago";
		} else if (diff < MILLIS_IN_WEEK) {
			//less than a week, calculate days
			int numDays = diff/MILLIS_IN_DAY;
			
			//System.out.println("day diff = " + numDays);
			
			//now calculate which day it was
			if(numDays == 1) {
				message = "yesterday";
			} else {
				//copy calendar, then subtract number of days to find posting day
				Calendar postingCal = currentCal;
				postingCal.add(Calendar.DATE, -numDays);
				int postingDay = postingCal.get(Calendar.DAY_OF_WEEK);
				//System.out.println("day of week of post = " + postingDay);
				//System.out.println("day of week of post = " + DAY_OF_WEEK_MAPPINGS[postingDay]);

				//use calendar API to get name of day here, for now using array at top
				message = "on " + DAY_OF_WEEK_MAPPINGS[postingDay];
			}
			
		} 

		return message;
	}
	
	
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#truncateAndPadStringToSize()
	 */
	public String truncateAndPadStringToSize(String string, int size) {
		
		String returnStr = string.substring(0, size);
		return (returnStr.concat("..."));
		
	}
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#createDefaultPrivacyRecord()
	 */
	public ProfilePrivacy createDefaultPrivacyRecord(String userId) {
		
		//see ProfilePrivacy for this constructor and what it all means
		ProfilePrivacy profilePrivacy = new ProfilePrivacy(
				userId,
				ProfilePrivacyManager.DEFAULT_PRIVACY_OPTION_PROFILE,
				ProfilePrivacyManager.DEFAULT_PRIVACY_OPTION_BASICINFO,
				ProfilePrivacyManager.DEFAULT_PRIVACY_OPTION_CONTACTINFO,
				ProfilePrivacyManager.DEFAULT_PRIVACY_OPTION_PERSONALINFO,
				ProfilePrivacyManager.DEFAULT_BIRTHYEAR_VISIBILITY,
				ProfilePrivacyManager.DEFAULT_PRIVACY_OPTION_SEARCH);
		
		//save
		try {
			getHibernateTemplate().save(profilePrivacy);
			log.info("Created default privacy record for user: " + userId);
			return profilePrivacy;
		} catch (Exception e) {
			log.error("createDefaultPrivacyRecord() failed. " + e.getClass() + ": " + e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getPrivacyRecordForUser()
	 */
	public ProfilePrivacy getPrivacyRecordForUser(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null Argument in getPrivacyRecordForUser");
	  	}
		
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
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#savePrivacyRecord()
	 */
	public boolean savePrivacyRecord(ProfilePrivacy profilePrivacy) {

		try {
			getHibernateTemplate().update(profilePrivacy);
			log.info("Updated privacy record for user: " + profilePrivacy.getUserUuid());
			return true;
		} catch (Exception e) {
			log.error("savePrivacyRecordForUser() failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
		
	}
	

	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#addNewProfileImage()
	 */
	public boolean addNewProfileImage(String userId, String mainResource, String thumbnailResource) {
		
			
		//first get the current ProfileImage records
		List<ProfileImage> currentImages = new ArrayList<ProfileImage>(getCurrentProfileImageRecords(userId));
		for(Iterator<ProfileImage> i = currentImages.iterator(); i.hasNext();){
			ProfileImage currentImage = (ProfileImage)i.next();
			
			//invalidate each
			currentImage.setCurrent(false);
			
			//save
			try {
				getHibernateTemplate().update(currentImage);
				log.info("saveProfileImageRecord(): Invalidated profileImage: " + currentImage.getId() + " for user: " + currentImage.getUserUuid());
			} catch (Exception e) {
				log.error("saveProfileImageRecord(): Couldn't invalidate profileImage: " + e.getClass() + ": " + e.getMessage());
			}
		}
				
		//now create a new ProfileImage object with the new data - this is our new current ProfileImage
		ProfileImage newProfileImage = new ProfileImage(userId, mainResource, thumbnailResource, true);
		
		//save the new ProfileImage to the db
		try {
			getHibernateTemplate().save(newProfileImage);
			log.info("saveProfileImageRecord(): Saved new profileImage for user: " + newProfileImage.getUserUuid());
			return true;
		} catch (Exception e) {
			log.error("saveProfileImageRecord() failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
	}


	
	
	
	
	

	
	
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#findUsersByNameOrEmail(String search, String userId)
	 */
	public List<SearchResult> findUsersByNameOrEmail(String search, String userId) {
		
		//perform search (uses private method to wrap the two searches into one)
		List<String> userUuids = new ArrayList<String>(findUsersByNameOrEmail(search));

		//format into SearchResult records (based on friend status, privacy status etc)
		List<SearchResult> results = new ArrayList<SearchResult>(createSearchResultRecordsFromSearch(userUuids, userId));
		
		return results;
		
	}
	
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#findUsersByNameOrEmail(String search, String userId)
	 */
	public List<SearchResult> findUsersByInterest(String search, String userId) {
		
		//perform search (uses private method to wrap the search)
		List<String> userUuids = new ArrayList<String>(findSakaiPersonsByInterest(search));
		
		//format into SearchResult records (based on friend status, privacy status etc)
		List<SearchResult> results = new ArrayList<SearchResult>(createSearchResultRecordsFromSearch(userUuids, userId));
		
		return results;
		
	}
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#listAllSakaiPersons()
	 */
	public List<String> listAllSakaiPersons() {
		
		List<String> userUuids = new ArrayList<String>();
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_LIST_ALL_SAKAI_PERSONS);
	  			return q.list();
	  		}
	  	};
	  	
	  	userUuids = (List<String>) getHibernateTemplate().executeFind(hcb);
	
	  	return userUuids;
	}
	
	
	
	
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#isUserFriendOfCurrentUser(String userId, String currentUserId)
	 */
	public boolean isUserFriendOfCurrentUser(String userId, String currentUserId) {
		
		//get friends of current user
		//TODO change this to be a single lookup rather than iterating over a list
		List<String> friendUuids = new ArrayList<String>(getConfirmedFriendUserIdsForUser(currentUserId));
		
		//if list of confirmed friends contains this user, they are a friend
		if(friendUuids.contains(userId)) {
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#isUserVisibleInSearchesByCurrentUser(String userId, String currentUserId, boolean friend)
	 */
	public boolean isUserVisibleInSearchesByCurrentUser(String userId, String currentUserId, boolean friend) {
				
		//get ProfilePrivacy record for user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userId);
    	//if none, return whatever the flag is set as by default
    	if(profilePrivacy == null) {
    		if (log.isDebugEnabled()) log.debug("SEARCH VISIBILITY: no record, returning default visibility");
    		return ProfilePrivacyManager.DEFAULT_SEARCH_VISIBILITY;
    	}
    	
    	//if user is the current user (ie they foumd themself in a search)
    	if(currentUserId.equals(userId)) {
    		if (log.isDebugEnabled()) log.debug("SEARCH VISIBILITY: user is current user");
    		return ProfilePrivacyManager.SELF_SEARCH_VISIBILITY;
    	}
    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getSearch() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYME) {
    		if (log.isDebugEnabled()) log.debug("SEARCH VISIBILITY: only me");
    		return false;
    	}
    	
    	//if friend and set to friends only
    	if(friend && profilePrivacy.getSearch() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYFRIENDS) {
    		if (log.isDebugEnabled()) log.debug("SEARCH VISIBILITY: only friends and is friend");
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getSearch() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYFRIENDS) {
    		if (log.isDebugEnabled()) log.debug("SEARCH VISIBILITY: only friends and not friend");
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getSearch() == ProfilePrivacyManager.PRIVACY_OPTION_EVERYONE) {
    		if (log.isDebugEnabled()) log.debug("SEARCH VISIBILITY: everyone");
    		return true;
    	}
    	
    	//uncaught rule, return false
    	log.error("SEARCH VISIBILITY: Uncaught rule");
    	return false;
		
	}
	
	
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#isUserProfileVisibleByCurrentUser(String userId, String currentUserId, boolean friend)
	 */
	public boolean isUserProfileVisibleByCurrentUser(String userId, String currentUserId, boolean friend) {
		
		
		//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userId);
    	
    	//if none, return whatever the flag is set as by default
    	if(profilePrivacy == null) {
    		return ProfilePrivacyManager.DEFAULT_PROFILE_VISIBILITY;
    	}
    	
    	//if user is the current user, they ARE allowed to view their own picture!
    	if(currentUserId.equals(userId)) {
    		return true;
    	}
    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getProfile() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getProfile() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getProfile() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getProfile() == ProfilePrivacyManager.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	
    	//uncaught rule, return false
    	log.error("isUserProfileVisibleByCurrentUser: Uncaught rule");
    	return false;
	}
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#isBasicInfoVisibleByCurrentUser(String userId, String currentUserId, boolean friend)
	 */
	public boolean isBasicInfoVisibleByCurrentUser(String userId, String currentUserId, boolean friend) {
		
		//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userId);
    	
    	//if none, return whatever the flag is set as by default
    	if(profilePrivacy == null) {
    		return ProfilePrivacyManager.DEFAULT_BASICINFO_VISIBILITY;
    	}
    	
    	//if user is the current user, they ARE allowed to view their own picture!
    	//but this will never be called as the current user cannot access ViewProfile.
    	//so this has been removed from this function and all other privacy checks
    	/*
    	if(currentUserId.equals(userId)) {
    		return true;
    	}
    	*/
    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getBasicInfo() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getBasicInfo() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getBasicInfo() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getBasicInfo() == ProfilePrivacyManager.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	
    	//uncaught rule, return false
    	log.error("isBasicInfoVisibleByCurrentUser: Uncaught rule");
    	return false;
	}
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#isContactInfoVisibleByCurrentUser(String userId, String currentUserId, boolean friend)
	 */
	public boolean isContactInfoVisibleByCurrentUser(String userId, String currentUserId, boolean friend) {
		
		//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userId);
    	
    	//if none, return whatever the flag is set as by default
    	if(profilePrivacy == null) {
    		return ProfilePrivacyManager.DEFAULT_CONTACTINFO_VISIBILITY;
    	}
    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getContactInfo() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getContactInfo() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getContactInfo() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getContactInfo() == ProfilePrivacyManager.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	
    	//uncaught rule, return false
    	log.error("isContactInfoVisibleByCurrentUser: Uncaught rule");
    	return false;
	}
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#isPersonalInfoVisibleByCurrentUser(String userId, String currentUserId, boolean friend)
	 */
	public boolean isPersonalInfoVisibleByCurrentUser(String userId, String currentUserId, boolean friend) {
		
		//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userId);
    	
    	//if none, return whatever the flag is set as by default
    	if(profilePrivacy == null) {
    		return ProfilePrivacyManager.DEFAULT_PERSONALINFO_VISIBILITY;
    	}
    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getPersonalInfo() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getPersonalInfo() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getPersonalInfo() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getPersonalInfo() == ProfilePrivacyManager.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	    	
    	//uncaught rule, return false
    	log.error("isPersonalInfoVisibleByCurrentUser: Uncaught rule");
    	return false;
	}
	
	

	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#isBirthYearVisible(String userId)
	 */
	public boolean isBirthYearVisible(String userId) {
		
		//get privacy record for this user
		ProfilePrivacy profilePrivacy = this.getPrivacyRecordForUser(userId);
		
		//return value or whatever the flag is set as by default
    	if(profilePrivacy == null) {
    		return ProfilePrivacyManager.DEFAULT_BIRTHYEAR_VISIBILITY;
    	} else {
    		return profilePrivacy.isShowBirthYear();
    	}
		
	}

	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getCurrentProfileImageForUser(String userId, int imageType)
	 */
	public byte[] getCurrentProfileImageForUser(String userId, int imageType) {
		
		byte[] image = null;
		
		//get record from db
		ProfileImage profileImage = getCurrentProfileImageRecord(userId);
		
		if(profileImage == null) {
			log.warn("Profile.getCurrentProfileImageForUser() null for userId: " + userId);
			return null;
		}
		
		//get main image
		if(imageType == ProfileImageManager.PROFILE_IMAGE_MAIN) {
			image = sakaiProxy.getResource(profileImage.getMainResource());
		}
		
		//or get thumbnail
		if(imageType == ProfileImageManager.PROFILE_IMAGE_THUMBNAIL) {
			image = sakaiProxy.getResource(profileImage.getThumbnailResource());
		}
		
		return image;
	}

	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#hasProfileImage(String userId)
	 */
	public boolean hasProfileImage(String userId) {
		
		//get record from db
		ProfileImage profileImage = getCurrentProfileImageRecord(userId);
		
		if(profileImage == null) {
			return false;
		}
		return true;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//private method to query SakaiPerson for matches
	//this should go in the profile ProfilePersistence API
	private List<String> findSakaiPersonsByNameOrEmail(final String search) {
		
		List<String> userUuids = new ArrayList<String>();
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_NAME_OR_EMAIL);
	  			q.setParameter(SEARCH, '%' + search + '%', Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	userUuids = (List<String>) getHibernateTemplate().executeFind(hcb);
	
	  	return userUuids;
	}
	
	
	//private method to query SakaiPerson for matches
	//this should go in the profile ProfilePersistence API
	private List<String> findSakaiPersonsByInterest(final String search) {
		
		List<String> userUuids = new ArrayList<String>();
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_INTEREST);
	  			q.setParameter(SEARCH, '%' + search + '%', Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	userUuids = (List<String>) getHibernateTemplate().executeFind(hcb);
	
	  	return userUuids;
	}


	
	
	
	/**
	 * Get the current ProfileImage records from the database.
	 * There should only ever be one, but in case things get out of sync this returns all.
	 * This method is only used when we are adding a new image as we need to invalidate all of the others
	 * If you are just wanting to retrieve the latest image, see getCurrentProfileImageRecord()
	 *
	 * @param userId		userId of the user
	 */
	private List<ProfileImage> getCurrentProfileImageRecords(final String userId) {
		
		List<ProfileImage> images = new ArrayList();
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	images = (List<ProfileImage>) getHibernateTemplate().executeFind(hcb);
	  	
	  	return images;
	}


	/**
	 * Get the current ProfileImage record from the database.
	 * There should only ever be one, but if there are more this will return the latest. 
	 * This is called when retrieving a profile image for a user. When adding a new image, there is a call
	 * to a private method called getCurrentProfileImageRecords() which should invalidate any multiple current images
	 *
	 * @param userId		userId of the user
	 */
	private ProfileImage getCurrentProfileImageRecord(final String userId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileImage) getHibernateTemplate().execute(hcb);
	}
	
	
	/**
	 * Get old ProfileImage records from the database. TODO: Used for displaying old the profile pictures album
	 *
	 * @param userId		userId of the user
	 */
	private List<ProfileImage> getOtherProfileImageRecords(final String userId) {
		
		List<ProfileImage> images = new ArrayList();
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_OTHER_PROFILE_IMAGE_RECORDS);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	images = (List<ProfileImage>) getHibernateTemplate().executeFind(hcb);
	  	
	  	return images;
	}


	//gets a friend record and tries both column arrangements
	private ProfileFriend getFriendRecord(final String userId, final String friendId) {
		
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null Argument in getFriendRecord");
	  	}
		
		ProfileFriend profileFriend = null;
		
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
	
		profileFriend = (ProfileFriend) getHibernateTemplate().execute(hcb);
	
		return profileFriend;
	}
	
	
	//private method to back the search methods. returns only uuids which then need to be checked for privacy settings etc.
	private List<String> findUsersByNameOrEmail(String search) {
		
		//get users from SakaiPerson
		List<String> userUuids = new ArrayList<String>(findSakaiPersonsByNameOrEmail(search));

		//get users from UserDirectoryService
		List<String> usersUuidsFromUserDirectoryService = new ArrayList<String>(sakaiProxy.searchUsers(search));
		
		//combine with no duplicates
		userUuids.removeAll(usersUuidsFromUserDirectoryService);
		userUuids.addAll(usersUuidsFromUserDirectoryService);
		
		return userUuids;
	
	}
	
	
	//private utility method used by findUsersByNameOrEmail() and findUsersByInterest() to format results from
	//the supplied userUuids to SearchResult records based on friend status and the privacy settings for each user
	//that was in the initial search results
	private List<SearchResult> createSearchResultRecordsFromSearch(List<String> userUuids, String userId) {

		List<SearchResult> results = new ArrayList<SearchResult>();
				
		//for each userUuid, is userId a friend?
		//also, get privacy record for the userUuid. if searches not allowed for this user pair, skip to next
		//otherwise create SearchResult record and add to list
		for(Iterator<String> i = userUuids.iterator(); i.hasNext();){
			String userUuid = (String)i.next();
			
			System.out.println("======================== START");
			System.out.println("userUuid: " + userUuid);
			System.out.println("userId: " + userId);
						
			
			boolean friend = this.isUserFriendOfCurrentUser(userUuid, userId);
			
			System.out.println("friend: " + friend);

			
			
			if(!isUserVisibleInSearchesByCurrentUser(userUuid, userId, friend)) {
				System.out.println("visible in searches: " + false);
				System.out.println("======================== END");
				continue; //not visible, skip
			}
			
			System.out.println("visible: " + true);

			
			boolean profileAllowed = this.isUserProfileVisibleByCurrentUser(userUuid, userId, friend);
			
			System.out.println("profileAllowed: " + profileAllowed);
			System.out.println("======================== END");

			//make object
			SearchResult searchResult = new SearchResult(
					userUuid,
					friend,
					profileAllowed);
			
			results.add(searchResult);
		}
		
		return results;
	}
	
	
	//init method called when Tomcat starts up
	public void init() {
		
		log.info("Profile2: init()");
		
		//do we need to run the conversion utility?
		if(sakaiProxy.getSakaiConfigurationParameterAsBoolean("profile.convert", false)) {
			convertProfile();
		}
	}
	
	
	
	//method to convert profileImages
	private void convertProfile() {
		log.info("Profile2: ===============================");
		log.info("Profile2: Conversion utility starting up.");
		log.info("Profile2: ===============================");

		//get list of users
		List<String> allUsers = new ArrayList<String>(listAllSakaiPersons());
		
		if(allUsers.isEmpty()){
			log.info("Profile2: No SakaiPersons to process.");
			return;
		}
		//for each, do they have a profile image record. if so, skip (perhaps null the SakaiPerson JPEG_PHOTO bytes?)
		for(Iterator<String> i = allUsers.iterator(); i.hasNext();) {
			String userUuid = (String)i.next();
			
			//if already have a current ProfileImage record, skip to next user
			if(hasProfileImage(userUuid)) {
				log.info("Profile2: valid record already exists for " + userUuid + ". Skipping...");
				continue;
			}

			//get SakaiPerson
			SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
			
			if(sakaiPerson == null) {
				log.error("Profile2: no SakaiPerson exists for " + userUuid + ". There should be one. Skipping...");
				continue;
			}
			
			//get photo from SakaiPerson
			byte[] image = null;
			image = sakaiPerson.getJpegPhoto();
			
			//if none, nothing to do
			if(image == null) {
				log.info("Profile2: nothing to convert for " + userUuid + ". Skipping...");
				continue;
			}
			
			//set some defaults for the image we are adding to ContentHosting
			String fileName = "Profile Image";
			String mimeType = "image/jpeg";
			
			//scale the main image
			byte[] imageMain = scaleImage(image, ProfileImageManager.MAX_IMAGE_XY);
			
			//create resource ID
			String mainResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileImageManager.PROFILE_IMAGE_MAIN, fileName);
			log.info("Profile2: mainResourceId: " + mainResourceId);
			
			//save, if error, log and return.
			if(!sakaiProxy.saveFile(mainResourceId, userUuid, fileName, mimeType, imageMain)) {
				log.error("Saving main profile image failed.");
				continue;
			}

			/*
			 * THUMBNAIL PROFILE IMAGE
			 */
			//scale image
			byte[] imageThumbnail = scaleImage(image, ProfileImageManager.MAX_THUMBNAIL_IMAGE_XY);
			 
			//create resource ID
			String thumbnailResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileImageManager.PROFILE_IMAGE_THUMBNAIL, fileName);

			log.info("Profile2: thumbnailResourceId:" + thumbnailResourceId);
			
			//save, if error, log and return.
			if(!sakaiProxy.saveFile(thumbnailResourceId, userUuid, fileName, mimeType, imageThumbnail)) {
				log.error("Saving thumbnail profile image failed.");
				continue;
			}

			/*
			 * SAVE IMAGE RESOURCE IDS
			 */
			if(addNewProfileImage(userUuid, mainResourceId, thumbnailResourceId)) {
				log.info("Profile2: image converted for " + userUuid);
			} else {
				log.error("Profile2: image conversion failed for " + userUuid);
				continue;
			}
			
		}
		
		
		
		
		return;
		
		
	}
	
	
	//setup SakaiProxy API
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}


	

	
}
