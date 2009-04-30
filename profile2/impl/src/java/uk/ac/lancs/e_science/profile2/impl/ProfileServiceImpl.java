package uk.ac.lancs.e_science.profile2.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.user.api.UserNotDefinedException;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.ProfilePreferencesManager;
import uk.ac.lancs.e_science.profile2.api.ProfilePrivacyManager;
import uk.ac.lancs.e_science.profile2.api.ProfileService;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.api.entity.model.Connection;
import uk.ac.lancs.e_science.profile2.api.entity.model.UserProfile;
import uk.ac.lancs.e_science.profile2.api.exception.ProfileMismatchException;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePreferences;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePrivacy;
import uk.ac.lancs.e_science.profile2.hbm.ProfileStatus;

/**
 * <p>This is the implementation of {@link ProfileService}; see that interface for usage details.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileServiceImpl implements ProfileService {

	private static final Logger log = Logger.getLogger(ProfileServiceImpl.class);
	
	/**
	 * {@inheritDoc}
	 */
	public boolean save(UserProfile userProfile) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public UserProfile getPrototype() {
		return new UserProfile();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public UserProfile getPrototype(String userId) {
		String userUuid = getUuidForUserId(userId);
		
		UserProfile userProfile = getPrototype();
		userProfile.setUserUuid(userUuid);
		userProfile.setDisplayName(sakaiProxy.getUserDisplayName(userUuid));
		
		return userProfile;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public UserProfile getFullUserProfile(String userId) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to make a request for a user's profile.");
		}
		
		//convert userId into uuid
		String userUuid = getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}
		
		//get SakaiPerson
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
		if(sakaiPerson == null) {
			return getPrototype(userUuid);
		}
		UserProfile userProfile = transformSakaiPersonToUserProfile(userUuid, sakaiPerson);
		
		//if person requested own profile, no need for privacy checks
		if(userUuid.equals(currentUserUuid)) {
			log.debug("userId is current user");
			addStatusToProfile(userProfile);
			return userProfile;
		}
		
		//get privacy record
		ProfilePrivacy privacy = profile.getPrivacyRecordForUser(userUuid);

		//get preferences record
		ProfilePreferences preferences = profile.getPreferencesRecordForUser(userUuid);
				
		//check friend status
		boolean friend = profile.isUserXFriendOfUserY(userUuid, currentUserUuid);
		
		//unset basic info if not allowed
		if(!profile.isUserXBasicInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			log.debug("basic info not allowed");
			userProfile.setNickname(null);
			userProfile.setDateOfBirth(null);
		}
		
		//unset contact info if not allowed
		if(!profile.isUserXContactInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			log.debug("contact info not allowed");
			userProfile.setEmail(null);
			userProfile.setHomepage(null);
			userProfile.setHomephone(null);
			userProfile.setWorkphone(null);
			userProfile.setMobilephone(null);
		}
		
		//unset personal info if not allowed
		if(!profile.isUserXPersonalInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			log.debug("personal info not allowed");
			userProfile.setFavouriteBooks(null);
			userProfile.setFavouriteTvShows(null);
			userProfile.setFavouriteMovies(null);
			userProfile.setFavouriteQuotes(null);
			userProfile.setOtherInformation(null);
		}
		
		//profile status
		if(profile.isUserXStatusVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			addStatusToProfile(userProfile);
		}
		
		//properties
		addPropertiesToProfile(userProfile, privacy, preferences);
		
		return userProfile;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public UserProfile getMinimalUserProfile(String userId) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to make a request for a user's profile.");
		}
		
		//convert userId into uuid
		String userUuid = getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}
				
		//create base profile
		UserProfile userProfile = getPrototype(userUuid);
		
		//get privacy record for the user - will be done in the method so not necessary here unless we add more fields that may use it
		//ProfilePrivacy profilePrivacy = profile.getPrivacyRecordForUser(userUuid);
		
		//check friend status
		boolean friend = profile.isUserXFriendOfUserY(userUuid, currentUserUuid);
		
		//add status if allowed
		if(profile.isUserXStatusVisibleByUserY(userUuid, currentUserUuid, friend)) {
			addStatusToProfile(userProfile);
		}
		
		return userProfile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean checkUserProfileExists(String userId) {
		return sakaiProxy.checkForUser(getUuidForUserId(userId));
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getProfileImage(String userId, int imageType) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to make a request for a user's profile image.");
		}
		
		//convert userId into uuid
		String userUuid = getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}
		
		//check that the environment is configured to be using these types of images.
		//you should not be able to get the info if not as it will be incorrect/out of date
		if(sakaiProxy.getProfilePictureType() != ProfileImageManager.PICTURE_SETTING_UPLOAD) {
			log.error("Requested profile image but environment not configured for this. Check sakai.properties if this is intentional.");
			return null;
		}
		
		//check friend status
		boolean friend = profile.isUserXFriendOfUserY(userUuid, currentUserUuid);
		
		//check if photo is allowed
		if(!profile.isUserXProfileImageVisibleByUserY(userUuid, currentUserUuid, friend)) {
			return null;
		}
		return profile.getCurrentProfileImageForUser(userUuid, imageType);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getConnectionIdsForUser(String userId) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to make a request for a user's connections.");
		}
		
		//convert userId into uuid
		String userUuid = getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}
				
		//check friend status
		boolean friend = profile.isUserXFriendOfUserY(userUuid, currentUserUuid);
		
		List<String> connectionIds = new ArrayList<String>();
		
		if(profile.isUserXFriendsListVisibleByUserY(userUuid, currentUserUuid, friend)) {
			connectionIds = profile.getConfirmedFriendUserIdsForUser(userUuid);
		}
		return connectionIds;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public List<Connection> getConnectionsForUser(String userId) {
		
		//pass off to get the list of uuids. Checks done in above method
		List<String> connectionIds = new ArrayList<String>();
		connectionIds = getConnectionIdsForUser(userId);
		
		if(connectionIds == null) {
			return null;
		}
		
		//convert userIds to Connections
		List<Connection> connections = new ArrayList<Connection>();
		for(String connectionId: connectionIds) {
			connections.add(new Connection(connectionId, sakaiProxy.getUserDisplayName(connectionId)));
		}
		
		return connections;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getExternalProfileImageUrl(String userId, int imageType, boolean fallback) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to make a request for a user's external image.");
		}
		
		//convert userId into uuid
		String userUuid = getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}
		
		//check that the environment is configured to be using these types of images.
		//you should not be able to get the info if not as it will be incorrect/out of date
		if(sakaiProxy.getProfilePictureType() != ProfileImageManager.PICTURE_SETTING_URL) {
			log.error("Requested external image but environment not configured for this. Check sakai.properties if this is intentional.");
			return null;
		}
		
		//check friend status
		boolean friend = profile.isUserXFriendOfUserY(userUuid, currentUserUuid);
		
		//check if photo is allowed
		if(!profile.isUserXProfileImageVisibleByUserY(userUuid, currentUserUuid, friend)) {
			return null;
		}
		
		return profile.getExternalImageUrl(userUuid, imageType, fallback);
		
	}
	
	
	/**
	 * This is a helper method to take care of getting the status and adding it to the profile.
	 * It is only called after any necessary privacy checks have been made
	 * @param userProfile	- UserProfile object for the person
	 */
	private void addStatusToProfile(UserProfile userProfile) {
		ProfileStatus profileStatus = profile.getUserStatus(userProfile.getUserUuid());
		if(profileStatus != null) {
			userProfile.setStatusMessage(profileStatus.getMessage());
			userProfile.setStatusDate(profileStatus.getDateAdded());
		}
	}
	
	/**
	 * This is a helper method to take care of setting the various relevant properties into a user's profile.
	 * @param userProfile	- UserProfile object for the person
	 * @param privacy		- Privacy object for the person
	 */
	private void addPropertiesToProfile(UserProfile userProfile, ProfilePrivacy privacy, ProfilePreferences preferences) {
		
		userProfile.setProperty(ProfilePrivacyManager.PROP_BIRTH_YEAR_VISIBLE, String.valueOf(privacy.isShowBirthYear()));
		userProfile.setProperty(ProfilePreferencesManager.PROP_EMAIL_CONFIRM_ENABLED, String.valueOf(preferences.isConfirmEmailEnabled()));
		userProfile.setProperty(ProfilePreferencesManager.PROP_EMAIL_REQUEST_ENABLED, String.valueOf(preferences.isRequestEmailEnabled()));
	
		//check the type of profileimage in use by the system (sakaiProxy) and set properties
		int imageType = sakaiProxy.getProfilePictureType();
		if(imageType == ProfileImageManager.PICTURE_SETTING_UPLOAD) {
			boolean hasImage = profile.hasUploadedProfileImage(userProfile.getUserUuid());
			userProfile.setProperty(ProfileImageManager.PROP_HAS_IMAGE, String.valueOf(hasImage));
			if(hasImage) {
				userProfile.setProperty(ProfileImageManager.PROP_IMAGE_TYPE, ProfileImageManager.ENTITY_IMAGE);
			}
		}
		if(imageType == ProfileImageManager.PICTURE_SETTING_URL) {
			boolean hasImage = profile.hasExternalProfileImage(userProfile.getUserUuid());
			userProfile.setProperty(ProfileImageManager.PROP_HAS_IMAGE, String.valueOf(hasImage));
			if(hasImage) {
				userProfile.setProperty(ProfileImageManager.PROP_IMAGE_TYPE, ProfileImageManager.ENTITY_IMAGE_URL);
			}
		}
		//based on that type, check if they have an image, and if so, what type is it. the value for this should match the entitynames
		//ie image or imageurl
	
	
	}
	
	
	/**
	 * Convenience method to convert the given userId input (internal id or eid) to a uuid. 
	 * 
	 * There is a small risk that an eid could be created that matches the uuid of another user.
	 * 
	 * Since 99% of the time requests will be made with uuid as the param, to speed things up this checks for that first.
	 * If the above risk manifests itself, we will need to swap the order so usernames are checked first.
	 * 
	 * @param userId
	 * @return
	 * @throws UserNotDefinedException 
	 */
	private String getUuidForUserId(String userId) {
		
		String userUuid = null;

		if(sakaiProxy.checkForUser(userId)) {
			userUuid = userId;
		} else if (sakaiProxy.checkForUserByEid(userId)) {
			userUuid = sakaiProxy.getUserIdForEid(userId);
			
			if(userUuid == null) {
				log.error("Could not translate eid to uuid for: " + userId);
			}
		} else {
			log.error("User: " + userId + " could not be found in any lookup by either id or eid");
		}
		
		return userUuid;
	}
	
	
	
	/**
	 * Convenience method to map a SakaiPerson object onto a UserProfile object
	 * 
	 * @param userUuid 	uuid for owner of this profile, for double checking.
	 * @param sp 		input SakaiPerson
	 * @return			returns a UserProfile representation of the SakaiPerson object
	 */
	private UserProfile transformSakaiPersonToUserProfile(String userUuid, SakaiPerson sp) {
		
		//double check the userUuid matches the SakaiPerson
		if(!userUuid.equals(sp.getAgentUuid())) {
			throw new ProfileMismatchException("Mismatch between SakaiPerson: " + sp.getAgentUuid() + " and userUuid: " + userUuid);
		}
				
		UserProfile userProfile = new UserProfile();
		
		//minimum info
		userProfile.setUserUuid(userUuid);
		userProfile.setDisplayName(sakaiProxy.getUserDisplayName(userUuid));

		//basic info
		userProfile.setNickname(sp.getNickname());
		userProfile.setDateOfBirth(sp.getDateOfBirth());
		
		//contact info
		userProfile.setEmail(sakaiProxy.getUserEmail(userUuid));
		userProfile.setHomepage(sp.getLabeledURI());
		userProfile.setHomephone(sp.getHomePhone());
		userProfile.setWorkphone(sp.getTelephoneNumber());
		userProfile.setMobilephone(sp.getMobile());
		
		//personal info
		userProfile.setFavouriteBooks(sp.getFavouriteBooks());
		userProfile.setFavouriteTvShows(sp.getFavouriteTvShows());
		userProfile.setFavouriteMovies(sp.getFavouriteMovies());
		userProfile.setFavouriteQuotes(sp.getFavouriteQuotes());
		userProfile.setOtherInformation(sp.getNotes());
		
		return userProfile;
		
	}
	
	
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private Profile profile;
	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	
	

}
