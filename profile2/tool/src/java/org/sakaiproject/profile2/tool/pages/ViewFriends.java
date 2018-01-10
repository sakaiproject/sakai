/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.pages;


import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.markup.html.panel.Panel;

import org.sakaiproject.profile2.exception.ProfileFriendsIllegalAccessException;
import org.sakaiproject.profile2.tool.pages.panels.ConfirmedFriends;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileConstants;

@Slf4j
public class ViewFriends extends BasePage {

	public ViewFriends(final String userUuid) {
		
		log.debug("ViewFriends()");
		
		//get user viewing this page
		final String currentUserUuid = sakaiProxy.getCurrentUserId();
				
		//check person viewing this page (currentuserId) is allowed to view userId's friends - unless admin
		if(!sakaiProxy.isSuperUser()){
			boolean isFriendsListVisible = privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_MYFRIENDS);
			if(!isFriendsListVisible) {
				throw new ProfileFriendsIllegalAccessException("User: " + currentUserUuid + " is not allowed to view the friends list for: " + userUuid);
			}
		}
		
		//show confirmed friends panel for the given user
		Panel confirmedFriends = new ConfirmedFriends("confirmedFriends", userUuid);
		confirmedFriends.setOutputMarkupId(true);
		add(confirmedFriends);
		
		//post view event
		sakaiProxy.postEvent(ProfileConstants.EVENT_FRIENDS_VIEW_OTHER, "/profile/"+userUuid, false);
		
	}
	
}



