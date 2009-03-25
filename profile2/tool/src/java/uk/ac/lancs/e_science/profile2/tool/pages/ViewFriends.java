package uk.ac.lancs.e_science.profile2.tool.pages;


import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.panel.Panel;

import uk.ac.lancs.e_science.profile2.api.ProfileUtilityManager;
import uk.ac.lancs.e_science.profile2.api.exception.ProfileFriendsIllegalAccessException;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.ConfirmedFriends;

public class ViewFriends extends BasePage {

	private transient Logger log = Logger.getLogger(MyFriends.class);
	
	public ViewFriends(final String userId) {
		
		log.debug("ViewFriends()");
		
		//get user viewing this page
		final String currentUserId = sakaiProxy.getCurrentUserId();
		
		//double check they are friends
		boolean friend = profile.isUserXFriendOfUserY(userId, currentUserId);
		
		//double check person viewing this page (currentuserId) is allowed to view userId's friends
		boolean isFriendsListVisible = profile.isUserXFriendsListVisibleByUserY(userId, currentUserId, friend);
		if(!isFriendsListVisible) {
			throw new ProfileFriendsIllegalAccessException("User: " + currentUserId + " is not allowed to view the friends list for: " + userId);
		}
		
		//show confirmed friends panel for the given user
		Panel confirmedFriends = new ConfirmedFriends("confirmedFriends", userId);
		confirmedFriends.setOutputMarkupId(true);
		add(confirmedFriends);
		
		//post view event
		sakaiProxy.postEvent(ProfileUtilityManager.EVENT_FRIENDS_VIEW_OTHER, "/profile/"+userId, false);
		
	}
	
}



