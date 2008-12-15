package uk.ac.lancs.e_science.profile2.impl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.hbm.ProfileFriend;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePrivacy;
import uk.ac.lancs.e_science.profile2.hbm.ProfileStatus;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;


public class ProfileImpl extends HibernateDaoSupport implements Profile {

	private transient Logger log = Logger.getLogger(ProfileImpl.class);
	
	//surely this is in a calendar API somewhere
	private static final String[] DAY_OF_WEEK_MAPPINGS = { "Sunday", "Monday",
		"Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
	
	
	/*
	 * Eventually may come from the database. For now, only FRIEND is used.
	 */
	private final int RELATIONSHIP_FRIEND = 1;
	private final int RELATIONSHIP_COLLEAGUE = 2;
	
	private static final String QUERY_GET_FRIENDS_FOR_USER = "getFriendsForUser";
	private static final String QUERY_GET_FRIEND_REQUEST = "getFriendRequest";
	private static final String QUERY_GET_USER_STATUS = "getUserStatus";
	private static final String QUERY_GET_PRIVACY_RECORD = "getPrivacyRecord";


	//Hibernate object fields
	private static final String USER_UUID = "userUuid";
	private static final String FRIEND_UUID = "friendUuid";
	private static final String CONFIRMED = "confirmed";
	
	
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
	
	
	
	public byte[] scaleImage (byte[] imageData, int maxSize) {
	
	    if (log.isDebugEnabled()) {
	    	log.debug("Scaling image...");
	    }
	    // Get the image from a file.
	    Image inImage = new ImageIcon(imageData).getImage();
	
	    // Determine the scale.
	    double scale = (double) maxSize / (double) inImage.getHeight(null);
	    if (inImage.getWidth(null) > inImage.getHeight(null)) {
	        scale = (double) maxSize / (double) inImage.getWidth(null);
	    }
	
	    // Determine size of new image.
	    // One of the dimensions should equal maxSize.
	    int scaledW = (int) (scale * inImage.getWidth(null));
	    int scaledH = (int) (scale * inImage.getHeight(null));
	
	    // Create an image buffer in which to paint on.
	    BufferedImage outImage = new BufferedImage(
	            scaledW, scaledH, BufferedImage.TYPE_INT_RGB
	        );
	
	    // Set the scale.
	    AffineTransform tx = new AffineTransform();
	
	    // If the image is smaller than the desired image size,
	    // don't bother scaling.
	    if (scale < 1.0d) {
	        tx.scale(scale, scale);
	    }
	
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
	    	log.error("failed");
	    }
	    return os.toByteArray();
	}
	

	public Date convertStringToDate(String dateStr) {
		Date date = new Date();
		return date;
	}
	
	public String convertDateToString(Date date) {
		String dateStr = "";
		return dateStr;
	}
	
	
	/*
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getFriendsForUser()
	 */
	public List getFriendsForUser(final String userId, final boolean confirmed) {
		if(userId == null){
	  		throw new IllegalArgumentException("Null Argument in getFriendsForUser");
	  	}
		List resultsList = new ArrayList(); 
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_FRIENDS_FOR_USER);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setParameter(CONFIRMED, confirmed, Hibernate.BOOLEAN);
	  			return q.list();
	  		}
	  	};
	  	
	  	resultsList = (List) getHibernateTemplate().executeFind(hcb);
	  	
	  	return resultsList;

	}
	
	/*
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#addFriend()
	 */	
	public boolean addFriend(String userId, String friendId) {
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
	
	/*
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#confirmFriend()
	 */
	public boolean confirmFriend(final String friendId, final String userId) {
		if(friendId == null || userId == null){
	  		throw new IllegalArgumentException("Null Argument in confirmFriend");
	  	}
		
		//get pending ProfileFriend object request for the given details
		ProfileFriend profileFriend = getPendingFriendRequest(friendId, userId);
		
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
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileFriend) getHibernateTemplate().execute(hcb);
	
	}
	
	/*
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getUnreadMessagesCount()
	 */
	public int getUnreadMessagesCount(String userId) {
		int unreadMessages = 0;
		return unreadMessages;
		
	}
	
	/*
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#createDefaultPrivacyRecord()
	 */
	public ProfilePrivacy createDefaultPrivacyRecord(String userId) {
		
		ProfilePrivacy profilePrivacy = new ProfilePrivacy(userId,0,0,0,0,0);
		
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
	
	/*
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#getLatestUserStatus()
	 */
	public ProfileStatus getLatestUserStatus(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null Argument in getUserStatus");
	  	}
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_USER_STATUS);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileStatus) getHibernateTemplate().execute(hcb);
	}
	
	/*
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#setUserStatus()
	 */
	public boolean setUserStatus(String userId, String status) {
		
		//validate userId here - TODO
		
		ProfileStatus profileStatus = new ProfileStatus(userId,status,new Date());
		
		//save (always inserting, never updating, unless its being cleared which is a different process)
		try {
			getHibernateTemplate().save(profileStatus);
			log.info("Updated status for user: " + userId);
			return true;
		} catch (Exception e) {
			log.error("setUserStatus() failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
		
	}

	
	

	/*
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#convertDateForStatus()
	 */
	public String convertDateForStatus(Date date) {
		
		//current time (cal also specify timezome and local here, see API)
		Calendar currentCal = Calendar.getInstance();
		long currentTimeMillis = currentCal.getTimeInMillis();
		
		//posting time (set calendar time to be this)
		//Calendar postingDate = Calendar.getInstance();
		//postingDate.setTimeInMillis(date.getTime());
		long postingTimeMillis = date.getTime();
		
		//difference
		int diff = (int)(currentTimeMillis - postingTimeMillis);
		
		System.out.println("currentDate:" + currentTimeMillis);
		System.out.println("postingDate:" + postingTimeMillis);
		System.out.println("diff:" + diff);
		
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
			
			//now calculate which day it was
			if(numDays == 1) {
				message = "yesterday";
			} else {
				//copy calendar, then subtract number of days to find posting day
				Calendar postingCal = currentCal;
				postingCal.add(Calendar.DAY_OF_WEEK, numDays);
				int postingDay = postingCal.get(Calendar.DAY_OF_WEEK);
					System.out.println("day of week of post = " + postingDay);
					System.out.println("day of week of post = " + DAY_OF_WEEK_MAPPINGS[postingDay]);

				//use calendar API to get name of day here, for now using array at top
				message = "on " + DAY_OF_WEEK_MAPPINGS[postingDay];
			}
			
		}

		return message;
	}
	
	/*
	 * @see uk.ac.lancs.e_science.profile2.api.Profile#truncateAndPadStringToSize()
	 */
	public String truncateAndPadStringToSize(String string, int size) {
		
		String returnStr = string.substring(0, size);
		return (returnStr.concat("..."));
		
	}
	
	/*
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
	  			return q.uniqueResult();
			}
		};
	
		return (ProfilePrivacy) getHibernateTemplate().execute(hcb);

	}
	
	public boolean savePrivacyRecordForUser(ProfilePrivacy profilePrivacy) {

		try {
			getHibernateTemplate().update(profilePrivacy);
			log.info("Updated privacy record for user: " + profilePrivacy.getUserUuid());
			return true;
		} catch (Exception e) {
			log.error("savePrivacyRecordForUser() failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
		
	}


		
	/*
	private void saveFriendRecord(ProfileFriends profileFriends)
	  {
	  	getHibernateTemplate().saveOrUpdate(profileFriends);
	  }
	*/
	
	
	
	
}
