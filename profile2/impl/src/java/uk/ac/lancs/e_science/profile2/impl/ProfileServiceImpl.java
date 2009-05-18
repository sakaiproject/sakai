package uk.ac.lancs.e_science.profile2.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.user.api.UserNotDefinedException;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.ProfilePreferencesManager;
import uk.ac.lancs.e_science.profile2.api.ProfilePrivacyManager;
import uk.ac.lancs.e_science.profile2.api.ProfileService;
import uk.ac.lancs.e_science.profile2.api.ProfileUtilityManager;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.api.entity.model.Connection;
import uk.ac.lancs.e_science.profile2.api.entity.model.UserProfile;
import uk.ac.lancs.e_science.profile2.api.exception.ProfileMismatchException;
import uk.ac.lancs.e_science.profile2.api.exception.ProfileNotDefinedException;
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
		UserProfile userProfile = transformSakaiPersonToUserProfile(sakaiPerson);
				
		//if person requested own profile, no need for privacy checks
		//add the additional information and return
		if(userUuid.equals(currentUserUuid)) {
			log.debug("userId is current user");
			addStatusToProfile(userProfile);
			addImageUrlToProfile(userProfile);
			addThumbnailImageUrlToProfile(userProfile);
			
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
			userProfile.setFacsimile(null);
		}
		
		//unset contact info if not allowed
		if(!profile.isUserXAcademicInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			log.debug("academic info not allowed");
			userProfile.setPosition(null);
			userProfile.setDepartment(null);
			userProfile.setSchool(null);
			userProfile.setRoom(null);
			userProfile.setCourse(null);
			userProfile.setSubjects(null);
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
		
		//add image urls
		addImageUrlToProfile(userProfile);
		addThumbnailImageUrlToProfile(userProfile);
		
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
		
		//add thumbnail image url
		addThumbnailImageUrlToProfile(userProfile);
		
		return userProfile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public UserProfile getAcademicUserProfile(String userId) {
		
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
		
		//get privacy record for the user
		ProfilePrivacy privacy = profile.getPrivacyRecordForUser(userUuid);
		
		//check friend status
		
		//check if the academic fields are allowed to be viewed by this user.
		
		//add thumbnail image url
		addImageUrlToProfile(userProfile);
		
		return userProfile;
	}

	/**
	 * {@inheritDoc}
	 */
	public UserProfile getCustomUserProfile(String userId, int profileType) {
		return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean checkUserExists(String userId) {
		return sakaiProxy.checkForUser(getUuidForUserId(userId));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean checkUserProfileExists(String userId) {
		
		//convert userId into uuid
		String userUuid = getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return false;
		}
		
		//check if we have a persisted profile
		if(sakaiProxy.getSakaiPerson(userUuid) == null) {
			return false;
		}
		return true;
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
		
		//check friend status
		boolean friend = profile.isUserXFriendOfUserY(userUuid, currentUserUuid);
		
		//check if photo is allowed, if not return default
		if(!profile.isUserXProfileImageVisibleByUserY(userUuid, currentUserUuid, friend)) {
			return returnDefaultImage();
		}
		
		//check environment configuration (will be url or upload) and get image accordingly
		//fall back by default. there is no real use case for not doing it.
		if(sakaiProxy.getProfilePictureType() == ProfileImageManager.PICTURE_SETTING_URL) {
			String url = profile.getExternalImageUrl(userUuid, imageType, true);
			if(url == null) {
				return returnDefaultImage();
			} else {
				return profile.getURLResourceAsBytes(url);
			}
		} else {
			byte[] image = profile.getCurrentProfileImageForUser(userUuid, imageType, true);
			if(image == null) {
				return returnDefaultImage();
			} else {
				return image;
			}
		} 
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
	public String getUserProfileAsHTML(UserProfile userProfile) {
		
		//note there is no birthday in this field. we need a good way to get the birthday without the year. 
		//maybe it needs to be stored in a separate field and treated differently. Or returned as a localised string.
		
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"profile2-profile\">");
		
		boolean useThumbnail = true;
		
		if(StringUtils.isNotBlank(userProfile.getImageUrl())) {
			sb.append("<div class=\"profile2-profile-image\">");
			sb.append("<img src=\"");
			sb.append(userProfile.getImageUrl());
			sb.append("\" />");
			sb.append("</div>");
			useThumbnail = false;
		}
		
		//only add thumbnail if no main image has been used
		if(useThumbnail && StringUtils.isNotBlank(userProfile.getImageThumbUrl())) {
			sb.append("<div class=\"profile2-profile-image-thumb\">");
			sb.append("<img src=\"");
			sb.append(userProfile.getImageThumbUrl());
			sb.append("\" />");
			sb.append("</div>");
		}
		
		//diff styles depending on if thumb was used or not, for diff widths in formatted profile view.
		if(useThumbnail) {
			sb.append("<div class=\"profile2-profile-content-thumb\">");
		} else {
			sb.append("<div class=\"profile2-profile-content\">");
		}
		
		if(StringUtils.isNotBlank(userProfile.getUserUuid())) {
			sb.append("<div class=\"profile2-profile-userUuid\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.userUuid"));
			sb.append("</span>");
			sb.append(userProfile.getUserUuid());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getDisplayName())) {
			sb.append("<div class=\"profile2-profile-displayName\">");
			sb.append(userProfile.getDisplayName());
			sb.append("</div>");
		}
		
		//status
		if(StringUtils.isNotBlank(userProfile.getStatusMessage())) {
			sb.append("<div class=\"profile2-profile-statusMessage\">");
			sb.append(userProfile.getStatusMessage());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getStatusDateFormatted())) {
			sb.append("<div class=\"profile2-profile-statusDate\">");
			sb.append(userProfile.getStatusDateFormatted());
			sb.append("</div>");
		}
		
		//basic info
		if(StringUtils.isNotBlank(userProfile.getNickname())) {
			sb.append("<div class=\"profile2-profile-nickname\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.nickname"));
			sb.append("</span>");
			sb.append(userProfile.getNickname());
			sb.append("</div>");
		}
		
		
		
		//contact info
		if(StringUtils.isNotBlank(userProfile.getEmail())) {
			sb.append("<div class=\"profile2-profile-email\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.email"));
			sb.append("</span>");
			sb.append(userProfile.getEmail());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getHomepage())) {
			sb.append("<div class=\"profile2-profile-homepage\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.homepage"));
			sb.append("</span>");
			sb.append(userProfile.getHomepage());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getHomephone())) {
			sb.append("<div class=\"profile2-profile-homephone\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.homephone"));
			sb.append("</span>");
			sb.append(userProfile.getHomephone());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getWorkphone())) {
			sb.append("<div class=\"profile2-profile-workphone\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.workphone"));
			sb.append("</span>");
			sb.append(userProfile.getWorkphone());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getMobilephone())) {
			sb.append("<div class=\"profile2-profile-mobilephone\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.mobilephone"));
			sb.append("</span>");
			sb.append(userProfile.getMobilephone());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getFacsimile())) {
			sb.append("<div class=\"profile2-profile-facsimile\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.facsimile"));
			sb.append("</span>");
			sb.append(userProfile.getFacsimile());
			sb.append("</div>");
		}
		
		
		
		//academic info
		if(StringUtils.isNotBlank(userProfile.getPosition())) {
			sb.append("<div class=\"profile2-profile-position\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.position"));
			sb.append("</span>");
			sb.append(userProfile.getPosition());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getDepartment())) {
			sb.append("<div class=\"profile2-profile-department\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.department"));
			sb.append("</span>");
			sb.append(userProfile.getDepartment());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getSchool())) {
			sb.append("<div class=\"profile2-profile-school\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.school"));
			sb.append("</span>");
			sb.append(userProfile.getSchool());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getRoom())) {
			sb.append("<div class=\"profile2-profile-room\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.room"));
			sb.append("</span>");
			sb.append(userProfile.getRoom());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getCourse())) {
			sb.append("<div class=\"profile2-profile-course\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.course"));
			sb.append("</span>");
			sb.append(userProfile.getCourse());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getSubjects())) {
			sb.append("<div class=\"profile2-profile-subjects\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.subjects"));
			sb.append("</span>");
			sb.append(userProfile.getSubjects());
			sb.append("</div>");
		}
		
		
		//personal info
		if(StringUtils.isNotBlank(userProfile.getFavouriteBooks())) {
			sb.append("<div class=\"profile2-profile-favouriteBooks\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.favouriteBooks"));
			sb.append("</span>");
			sb.append(userProfile.getFavouriteBooks());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getFavouriteTvShows())) {
			sb.append("<div class=\"profile2-profile-favouriteTvShows\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.favouriteTvShows"));
			sb.append("</span>");
			sb.append(userProfile.getFavouriteTvShows());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getFavouriteMovies())) {
			sb.append("<div class=\"profile2-profile-favouriteMovies\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.favouriteMovies"));
			sb.append("</span>");
			sb.append(userProfile.getFavouriteMovies());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getFavouriteQuotes())) {
			sb.append("<div class=\"profile2-profile-favouriteQuotes\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.favouriteQuotes"));
			sb.append("</span>");

			sb.append(userProfile.getFavouriteQuotes());
			sb.append("</div>");
		}
		if(StringUtils.isNotBlank(userProfile.getOtherInformation())) {
			sb.append("<div class=\"profile2-profile-otherInformation\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("ProfileServiceImpl.otherInformation"));
			sb.append("</span>");
			sb.append(userProfile.getOtherInformation());
			sb.append("</div>");
		}
		
		sb.append("</div>");
		sb.append("</div>");
		
		//add the stylesheet
		sb.append("<link href=\"");
		sb.append(ProfileUtilityManager.ENTITY_CSS_PROFILE);
		sb.append("\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />");
		
		return sb.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String save(UserProfile userProfile) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to update your profile.");
		}
		
		//check currentUser and profile uuid match
		if(!currentUserUuid.equals(userProfile.getUserUuid())) {
			throw new SecurityException("You are not allowed to update this user's profile.");
		}
		
		//translate, save and return the response.
		return persistUserProfile(userProfile);
		
	}

	/**
	 * {@inheritDoc}
	 */
	public String create(String userId) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to create your profile.");
		}
		
		//convert userId into uuid
		String userUuid = getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}
		
		//check currentUser and profile uuid match
		if(!currentUserUuid.equals(userUuid)) {
			throw new SecurityException("You are not allowed to create this user's profile.");
		}
		
		//does this user already have a persisted profile?
		if(checkUserProfileExists(userUuid)) {
			log.info("userId: " + userId + " already has a profile. Cannot create another.");
			return userUuid;
		}
			
		//no existing profile, setup a prototype
		UserProfile userProfile = getPrototype(userUuid);
		
		//translate, save and return the response.
		return persistUserProfile(userProfile);
				
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String create(UserProfile userProfile) {
		return save(userProfile);
	}
	
	
	/**
	 * This is a helper method to take care of translating a UserProfile to a SakaiPerson, doing anything else
	 * then saving it.
	 * 
	 * @param userProfile
	 * @return userUuid if ok, null otherwise
	 */
	private String persistUserProfile(UserProfile userProfile) {
		
		//translate main fields
		SakaiPerson sakaiPerson = transformUserProfileToSakaiPerson(userProfile);

		//update SakaiPerson obj
		if(sakaiProxy.updateSakaiPerson(sakaiPerson)) {
			log.info("Saved profile for: " + userProfile.getUserUuid());
			return userProfile.getUserUuid();
		} else {
			log.error("Couldn't save profile for: " + userProfile.getUserUuid());
			return null;
		}
		
	}
	
	
	
	// TODO
	private void applyPrivacyChecksToUserProfile(UserProfile userProfile, ProfilePrivacy privacy, boolean friend) {
		
		//go over the various sections of the profile, see if a user is allowed to see them or not, and null out if not.
		//this should replace the checks above
		
	}
	

	/**
	 * These are two helper methods to simply add the URL to a user's profile image or thumbnail to the UserProfile. 
	 * It can be added to any profile without checks as the retrieval of the image does the checks
	 * 
	 * @param userProfile
	 */
	private void addImageUrlToProfile(UserProfile userProfile) {
		userProfile.setImageUrl(sakaiProxy.getServerUrl() + "/direct/profile/" + userProfile.getUserUuid() + "/image/");
	}
	
	private void addThumbnailImageUrlToProfile(UserProfile userProfile) {
		userProfile.setImageThumbUrl(sakaiProxy.getServerUrl() + "/direct/profile/" + userProfile.getUserUuid() + "/image/thumb/");
	}
	
	/**
	 * This is a helper method to take care of getting the default unavailable image and returning it.
	 * @return
	 */
	private byte[] returnDefaultImage() {
		return profile.getURLResourceAsBytes(profile.getUnavailableImageURL());
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
			
			userProfile.setStatusDateFormatted(profile.convertDateForStatus(userProfile.getStatusDate()));
		}
	}
	
	/**
	 * This is a helper method to take care of setting the various relevant properties into a user's profile.
	 * If a user has requested their own profile, they dont need these properties (?)
	 * 
	 * @param userProfile		- UserProfile object for the person
	 * @param privacy			- Privacy object for the person
	 * @param preferences	- Preferences object for the person
	 */
	private void addPropertiesToProfile(UserProfile userProfile, ProfilePrivacy privacy, ProfilePreferences preferences) {
		
		if(privacy == null) {
			userProfile.setProperty(ProfilePrivacyManager.PROP_BIRTH_YEAR_VISIBLE, String.valueOf(ProfilePrivacyManager.DEFAULT_BIRTHYEAR_VISIBILITY));
		} else {
			userProfile.setProperty(ProfilePrivacyManager.PROP_BIRTH_YEAR_VISIBLE, String.valueOf(privacy.isShowBirthYear()));
		}
		
		if(preferences == null) {
			userProfile.setProperty(ProfilePreferencesManager.PROP_EMAIL_CONFIRM_ENABLED, String.valueOf(ProfilePreferencesManager.DEFAULT_EMAIL_NOTIFICATION_SETTING));
			userProfile.setProperty(ProfilePreferencesManager.PROP_EMAIL_REQUEST_ENABLED, String.valueOf(ProfilePreferencesManager.DEFAULT_EMAIL_NOTIFICATION_SETTING));
		} else {
			userProfile.setProperty(ProfilePreferencesManager.PROP_EMAIL_CONFIRM_ENABLED, String.valueOf(preferences.isConfirmEmailEnabled()));
			userProfile.setProperty(ProfilePreferencesManager.PROP_EMAIL_REQUEST_ENABLED, String.valueOf(preferences.isRequestEmailEnabled()));
		}

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
	 * @param sp 		input SakaiPerson
	 * @return			returns a UserProfile representation of the SakaiPerson object
	 */
	private UserProfile transformSakaiPersonToUserProfile(SakaiPerson sp) {
		
		String userUuid = sp.getAgentUuid();
		
		UserProfile userProfile = new UserProfile();
		
		//map fields from SakaiPerson to UserProfile
		
		//minimum info
		userProfile.setUserUuid(userUuid);
		userProfile.setDisplayName(sakaiProxy.getUserDisplayName(userUuid));

		//basic info
		userProfile.setNickname(sp.getNickname());
		userProfile.setDateOfBirth(sp.getDateOfBirth());
		
		//contact info
		userProfile.setEmail(sakaiProxy.getUserEmail(userUuid));
		userProfile.setHomepage(sp.getLabeledURI());
		userProfile.setWorkphone(sp.getTelephoneNumber());
		userProfile.setHomephone(sp.getHomePhone());
		userProfile.setMobilephone(sp.getMobile());
		userProfile.setFacsimile(sp.getFacsimileTelephoneNumber());
		
		//academic info
		userProfile.setDepartment(sp.getOrganizationalUnit());
		userProfile.setPosition(sp.getTitle());
		userProfile.setSchool(sp.getCampus());
		userProfile.setRoom(sp.getRoomNumber());
		userProfile.setCourse(sp.getEducationCourse());
		userProfile.setSubjects(sp.getEducationSubjects());
		
		//personal info
		userProfile.setFavouriteBooks(sp.getFavouriteBooks());
		userProfile.setFavouriteTvShows(sp.getFavouriteTvShows());
		userProfile.setFavouriteMovies(sp.getFavouriteMovies());
		userProfile.setFavouriteQuotes(sp.getFavouriteQuotes());
		userProfile.setOtherInformation(sp.getNotes());
		
		return userProfile;
		
	}
	
	/**
	 * Convenience method to map a UserProfile object onto a SakaiPerson object for persisting
	 * 
	 * @param up 		input SakaiPerson
	 * @return			returns a SakaiPerson representation of the UserProfile object
	 */
	private SakaiPerson transformUserProfileToSakaiPerson(UserProfile up) {
	
		String userUuid = up.getUserUuid();
		
		//get SakaiPerson
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
		
		//if null, create one 
		if(sakaiPerson == null) {
			sakaiPerson = sakaiProxy.createSakaiPerson(userUuid);
			//if its still null, throw exception
			if(sakaiPerson == null) {
				throw new ProfileNotDefinedException("Couldn't create a SakaiPerson for " + userUuid);
			}
		} 
		
		//map fields from UserProfile to SakaiPerson
		
		//basic info
		sakaiPerson.setNickname(up.getNickname());
		sakaiPerson.setDateOfBirth(up.getDateOfBirth());
		
		//contact info
		sakaiPerson.setLabeledURI(up.getHomepage());
		sakaiPerson.setTelephoneNumber(up.getWorkphone());
		sakaiPerson.setHomePhone(up.getHomephone());
		sakaiPerson.setMobile(up.getMobilephone());
		sakaiPerson.setFacsimileTelephoneNumber(up.getFacsimile());
		
		//academic info
		sakaiPerson.setOrganizationalUnit(up.getDepartment());
		sakaiPerson.setTitle(up.getPosition());
		sakaiPerson.setCampus(up.getSchool());
		sakaiPerson.setRoomNumber(up.getRoom());
		sakaiPerson.setEducationCourse(up.getCourse());
		sakaiPerson.setEducationSubjects(up.getSubjects());
		
		//personal info
		sakaiPerson.setFavouriteBooks(up.getFavouriteBooks());
		sakaiPerson.setFavouriteTvShows(up.getFavouriteTvShows());
		sakaiPerson.setFavouriteMovies(up.getFavouriteMovies());
		sakaiPerson.setFavouriteQuotes(up.getFavouriteQuotes());
		sakaiPerson.setNotes(up.getOtherInformation());

		return sakaiPerson;
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
