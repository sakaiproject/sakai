package org.sakaiproject.profile2.service;

import java.util.Date;

import org.apache.log4j.Logger;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * <p>This is the implementation of {@link ProfileStatusService}; see that interface for usage details.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileStatusServiceImpl implements ProfileStatusService {

	private static final Logger log = Logger.getLogger(ProfileStatusServiceImpl.class);
	
	/**
	 * {@inheritDoc}
	 */
	public ProfileStatus getPrototype() {
		return new ProfileStatus();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ProfileStatus getProfileStatusRecord(String userId) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("Must be logged in.");
		}
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}
		
		//get record for the user, will be null if none
		ProfileStatus status = profileLogic.getUserStatus(userUuid);
		if(status == null) {
			return null;
		}
		
		//get privacy on the status (if no privacy, return based on default)
		ProfilePrivacy privacy = profileLogic.getPrivacyRecordForUser(userUuid);
		if (privacy == null) {
			return (ProfileConstants.DEFAULT_MYSTATUS_VISIBILITY ? status : null);
		}
		
		//check if friends
		boolean friend = profileLogic.isUserXFriendOfUserY(userUuid, currentUserUuid);
		
		//now check if allowed, return if ok, null if not
		return (profileLogic.isUserXStatusVisibleByUserY(userUuid, privacy, currentUserUuid, friend) ? status : null);
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean save(ProfileStatus obj) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("Must be logged in.");
		}
		
		//check currentUser and object uuid match
		if(!currentUserUuid.equals(obj.getUserUuid())) {
			throw new SecurityException("Not allowed to save.");
		}
		
		//reset the date to be now
		obj.setDateAdded(new Date());
		
		//save and return response
		return persistProfileStatus(obj);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean create(String userId, String message) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("Must be logged in.");
		}
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return false;
		}
		
		//check currentUser and object uuid match
		if(!currentUserUuid.equals(userUuid)) {
			throw new SecurityException("Not allowed to save.");
		}
		
		//create status object for user with message and date for now
		ProfileStatus status = new ProfileStatus(userUuid,message,new Date());

		//save and return response
		return persistProfileStatus(status);
	}
	
	

	/**
	 * {@inheritDoc}
	 */
	public boolean checkProfileStatusExists(String userId) {
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return false;
		}
		
		//check if we have a persisted object already
		if(profileLogic.getUserStatus(userUuid) == null) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Helper method to take care of persisting a ProfileStatus object to the database.
	 * 
	 * @param ProfileStatus object
	 * @return true/false for success
	 */
	private boolean persistProfileStatus(ProfileStatus obj) {

		if(profileLogic.setUserStatus(obj)) {
			return true;
		} 
		return false;
	}
	
		
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private ProfileLogic profileLogic;
	public void setProfileLogic(ProfileLogic profileLogic) {
		this.profileLogic = profileLogic;
	}
	

	
	

}
