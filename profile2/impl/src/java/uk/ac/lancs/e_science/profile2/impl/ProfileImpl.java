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
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.hbm.Friend;
import uk.ac.lancs.e_science.profile2.hbm.ProfileFriend;
import uk.ac.lancs.e_science.profile2.hbm.ProfileImage;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePrivacy;
import uk.ac.lancs.e_science.profile2.hbm.ProfileStatus;

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
	private static final String QUERY_GET_FRIEND_REQUEST = "getFriendRequest";
	private static final String QUERY_GET_FRIEND_RECORD = "getFriendRecord";
	private static final String QUERY_GET_USER_STATUS = "getUserStatus";
	private static final String QUERY_GET_PRIVACY_RECORD = "getPrivacyRecord";
	private static final String QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD = "getCurrentProfileImageRecord";
	private static final String QUERY_OTHER_PROFILE_IMAGE_RECORDS = "getOtherProfileImageRecords";
	private static final String QUERY_FIND_SAKAI_PERSONS_BY_NAME_OR_EMAIL = "findSakaiPersonsByNameOrEmail";
	private static final String QUERY_FIND_SAKAI_PERSONS_BY_INTEREST = "findSakaiPersonsByInterest";
	
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
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#requestFriend()
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
			log.error("addFriend() failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
	
	}
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#confirmFriend()
	 */
	public boolean confirmFriend(final String friendId, final String userId) {
		if(friendId == null || userId == null){
	  		throw new IllegalArgumentException("Null Argument in confirmFriend");
	  	}
		
		//get pending ProfileFriend object request for the given details
		ProfileFriend profileFriend = getPendingFriendRequest(friendId, userId);
		
		//FIX THIS as the columns can be the other way around - is this an issue?
		
	  	//make necessary changes to the ProfileFriend object.
	  	profileFriend.setConfirmed(true);
	  	profileFriend.setConfirmedDate(new Date()); //now
		
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
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#removeFriend()
	 */
	public boolean removeFriend(String userId, String friendId) {
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null Argument in removeFriend");
	  	}
		
		//get the friend object for this connection pair
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
	private ProfileFriend getPendingFriendRequest(final String friendId, final String userId) {
		
		if(friendId == null || userId == null){
	  		throw new IllegalArgumentException("Null Argument in getPendingFriendRequest");
	  	}
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_FRIEND_REQUEST);
	  			q.setParameter(FRIEND_UUID, friendId, Hibernate.STRING);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
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
		ProfilePrivacy profilePrivacy = new ProfilePrivacy(userId,0,0,0,0,true,0);
		
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
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getCurrentProfileImageRecord()
	 */
	public ProfileImage getCurrentProfileImageRecord(final String userId) {
		
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
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getOtherProfileImageRecords(final String userId)
	 */
	public List<ProfileImage> getOtherProfileImageRecords(final String userId) {
		
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
	
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#findUsersByNameOrEmail(String search)
	 */
	public List<String> findUsersByNameOrEmail(String search) {
		
		//get users from SakaiPerson
		List<String> userUuids = new ArrayList<String>(findSakaiPersonsByNameOrEmail(search));

		//get users from UserDirectoryService
		List<String> usersUuidsFromUserDirectoryService = new ArrayList<String>(sakaiProxy.searchUsers(search));
		
		//combine with no duplicates
		userUuids.removeAll(usersUuidsFromUserDirectoryService);
		userUuids.addAll(usersUuidsFromUserDirectoryService);

		return userUuids;
	
	}
	
	/**
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#findUsersByNameOrEmail(String search)
	 */
	public List<String> findUsersByInterest(String search) {
		
		//get users from SakaiPerson
		List<String> userUuids = new ArrayList<String>(findSakaiPersonsByInterest(search));
		
		return userUuids;
		
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
	 * Get the current ProfileImages record from the database.
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
	
	
	//setup SakaiProxy API
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}


	
}
