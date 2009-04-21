package uk.ac.lancs.e_science.profile2.impl;

import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.api.ProfileService;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.api.entity.model.UserProfile;
import uk.ac.lancs.e_science.profile2.api.exception.ProfileMismatchException;

/**
 * <p>This is the implementation of {@link ProfileService}; see that interface for usage details.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileServiceImpl implements ProfileService {

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
	public UserProfile getPrototype(String userUuid) {
		UserProfile userProfile = getPrototype();
		userProfile.setUserUuid(userUuid);
		
		return userProfile;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public UserProfile getUserProfile(String userUuid) {
		
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
		
		if(sakaiPerson == null) {
			return getPrototype(userUuid);
		}
		
		UserProfile userProfile = transformSakaiPersonToUserProfile(userUuid, sakaiPerson);
		return userProfile;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean checkUserProfileExists(String userUuid) {
		return sakaiProxy.checkForUser(userUuid);
	}

	
	
	
	
	/**
	 * Convenience method to map a SakaiPerson object onto a UserProfile object
	 * @param sp 	input SakaiPerson
	 * @return		returns a UserProfile representation of the SakaiPerson object
	 */
	private UserProfile transformSakaiPersonToUserProfile(String userUuid, SakaiPerson sp) {
		
		//double check the userUuid matches the SakaiPerson
		if(!userUuid.equals(sp.getAgentUuid())) {
			throw new ProfileMismatchException("Mismatch between SakaiPerson: " + sp.getAgentUuid() + " and userUuid: " + userUuid);
		}
				
		UserProfile userProfile = new UserProfile();
		
		userProfile.setUserUuid(userUuid);
		userProfile.setNickname(sp.getNickname());
		userProfile.setDateOfBirth(sp.getDateOfBirth());
		/*userProfile.setDisplayName(userDisplayName);
		userProfile.setEmail(userEmail);
		userProfile.setHomepage(sp.getLabeledURI());
		userProfile.setHomephone(sp.getHomePhone());
		userProfile.setWorkphone(sp.getTelephoneNumber());
		userProfile.setMobilephone(sp.getMobile());
		userProfile.setFavouriteBooks(sp.getFavouriteBooks());
		userProfile.setFavouriteTvShows(sp.getFavouriteTvShows());
		userProfile.setFavouriteMovies(sp.getFavouriteMovies());
		userProfile.setFavouriteQuotes(sp.getFavouriteQuotes());
		userProfile.setOtherInformation(sp.getNotes());*/
		
		return userProfile;
		
	}
	
	
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}

}
