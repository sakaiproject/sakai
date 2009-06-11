package org.sakaiproject.profile2.tool.pages;


import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.profile2.exception.ProfileFriendsIllegalAccessException;
import org.sakaiproject.profile2.tool.pages.panels.ConfirmedFriends;
import org.sakaiproject.profile2.util.ProfileConstants;

public class ViewFriends extends BasePage {

	private static final Logger log = Logger.getLogger(MyFriends.class);
	
	public ViewFriends(final String userUuid) {
		
		log.debug("ViewFriends()");
		
		//get user viewing this page
		final String currentUserUuid = sakaiProxy.getCurrentUserId();
		
		//double check they are friends
		boolean friend = profileLogic.isUserXFriendOfUserY(userUuid, currentUserUuid);
		
		//double check person viewing this page (currentuserId) is allowed to view userId's friends
		boolean isFriendsListVisible = profileLogic.isUserXFriendsListVisibleByUserY(userUuid, currentUserUuid, friend);
		if(!isFriendsListVisible) {
			throw new ProfileFriendsIllegalAccessException("User: " + currentUserUuid + " is not allowed to view the friends list for: " + userUuid);
		}
		
		//show confirmed friends panel for the given user
		Panel confirmedFriends = new ConfirmedFriends("confirmedFriends", userUuid);
		confirmedFriends.setOutputMarkupId(true);
		add(confirmedFriends);
		
		//post view event
		sakaiProxy.postEvent(ProfileConstants.EVENT_FRIENDS_VIEW_OTHER, "/profile/"+userUuid, false);
		
	}
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("ViewFriends has been deserialized.");
		//re-init our transient objects
		//profile = getProfile();
		//sakaiProxy = getSakaiProxy();
	}
	
}



