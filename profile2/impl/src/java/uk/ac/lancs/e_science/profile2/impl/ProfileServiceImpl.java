package uk.ac.lancs.e_science.profile2.impl;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileService;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.api.entity.model.UserProfile;
import uk.ac.lancs.e_science.profile2.api.exception.ProfileMismatchException;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePrivacy;

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
	public UserProfile getUserProfile(String userId, String currentUser) {
		
		//convert userId into uuid
		String userUuid = getUuidForUserId(userId);
		
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
		if(sakaiPerson == null) {
			return getPrototype(userId);
		}
		UserProfile userProfile = transformSakaiPersonToUserProfile(userUuid, sakaiPerson);
		
		//if person requested own profile, no need for privacy checks
		if(userUuid.equals(currentUser)) {
			System.out.println("userId is current user");
			return userProfile;
		}
		
		//get privacy record for the user
		ProfilePrivacy profilePrivacy = profile.getPrivacyRecordForUser(userUuid);
		
		//TODO get preferences
		//TODO need to check if their basicinfo is allowed, but birth year is not, 
		//to remove that from the Date by foramtting it to the hidden SimpleDateFormat in ProfileUtilityManager perhaps?
		
		//check friend status
		boolean friend = profile.isUserXFriendOfUserY(userUuid, currentUser);
		
		//unset basic info if not allowed
		if(!profile.isUserXBasicInfoVisibleByUserY(userUuid, profilePrivacy, currentUser, friend)) {
			System.out.println("basic info not allowed");
			userProfile.setNickname(null);
			userProfile.setDateOfBirth(null);
		}
		
		//unset basic info if not allowed
		if(!profile.isUserXContactInfoVisibleByUserY(userUuid, profilePrivacy, currentUser, friend)) {
			System.out.println("contact info not allowed");
			userProfile.setEmail(null);
			userProfile.setHomepage(null);
			userProfile.setHomephone(null);
			userProfile.setWorkphone(null);
			userProfile.setMobilephone(null);
		}
		
		//unset personal info if not allowed
		if(!profile.isUserXPersonalInfoVisibleByUserY(userUuid, profilePrivacy, currentUser, friend)) {
			System.out.println("personal info not allowed");
			userProfile.setFavouriteBooks(null);
			userProfile.setFavouriteTvShows(null);
			userProfile.setFavouriteMovies(null);
			userProfile.setFavouriteQuotes(null);
			userProfile.setOtherInformation(null);
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
	 * Convenience method to convert the given userId input (internal id or eid) to a uuid. 
	 * @param userId
	 * @return
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
			log.error("User " + userId + " could not be found in any lookup by either id or eid");
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
