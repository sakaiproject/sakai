package uk.ac.lancs.e_science.profile2.tool.pages;


import org.apache.log4j.Logger;

import uk.ac.lancs.e_science.profile2.api.exception.ProfileIllegalAccessException;


public class ViewProfile extends BasePage {

	private transient Logger log = Logger.getLogger(MyProfile.class);
	private transient byte[] profileImageBytes;
	
	public ViewProfile(String userUuid)   {
		
		if(log.isDebugEnabled()) log.debug("ViewProfile()");
		
		//get current user Id
		String currentUserId = sakaiProxy.getCurrentUserId();
		
		//friend?
		boolean friend = profile.isUserFriendOfCurrentUser(userUuid, currentUserId);
		

		//is this user allowed to view this person's profile?
		boolean isProfileAllowed = profile.isUserProfileVisibleByCurrentUser(userUuid, currentUserId, friend);
		
		if(!isProfileAllowed) {
			throw new ProfileIllegalAccessException("User: " + currentUserId + " is not allowed to view profile for: " + userUuid);
		}
		
		//get SakaiPerson for the person who's profile we are viewing
		
		
		
	}
	
		
	
	
	
}
