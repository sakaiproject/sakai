package uk.ac.lancs.e_science.profile2.tool.pages;


import org.apache.log4j.Logger;


public class ViewProfile extends BasePage {

	private transient Logger log = Logger.getLogger(MyProfile.class);
	private transient byte[] profileImageBytes;
	
	public ViewProfile(String userUuid)   {
		
		if(log.isDebugEnabled()) log.debug("ViewProfile()");
		
		//get current user Id
		String currentUserId = sakaiProxy.getCurrentUserId();
		
		System.out.println("currentUserId: " + currentUserId);
		System.out.println("userUuid: " + userUuid);

		//friend?
		boolean friend = profile.isUserFriendOfCurrentUser(userUuid, currentUserId);
		
		
		
		//is this user allowed to view this person's profile?
		boolean isProfileAllowed = profile.isUserProfileVisibleByCurrentUser(userUuid, currentUserId, friend);
		//get SakaiPerson for the person who's profile we are viewing
		
		
		
	}
	
		
	
	
	
}
