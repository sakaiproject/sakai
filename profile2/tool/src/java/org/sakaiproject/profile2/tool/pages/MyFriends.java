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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;

import org.sakaiproject.profile2.tool.pages.panels.ConfirmedFriends;
import org.sakaiproject.profile2.tool.pages.panels.RequestedFriends;
import org.sakaiproject.profile2.util.ProfileConstants;

@Slf4j
public class MyFriends extends BasePage {

	private Panel confirmedFriends;
	private Panel requestedFriends;
	
	public MyFriends() {
		
		log.debug("MyFriends()");
		
		disableLink(myFriendsLink);
				
		//get current user
		final String userId = sakaiProxy.getCurrentUserId();
    	
		//friend requests panel
		requestedFriends = new RequestedFriends("requestedFriends", userId);
		requestedFriends.setOutputMarkupId(true);
		add(requestedFriends);
		
		
		//confirmed friends panel
		confirmedFriends = new ConfirmedFriends("confirmedFriends", userId);
		confirmedFriends.setOutputMarkupId(true);
		add(confirmedFriends);
		
		//post view event
		sakaiProxy.postEvent(ProfileConstants.EVENT_FRIENDS_VIEW_OWN, "/profile/"+userId, false);
		
	}
	
	//method to allow us to update the confirmedFriends panel
	public void updateConfirmedFriends(AjaxRequestTarget target, String userId) {
		
		ConfirmedFriends newPanel = new ConfirmedFriends("confirmedFriends", userId);
		newPanel.setOutputMarkupId(true);
		confirmedFriends.replaceWith(newPanel);
		confirmedFriends=newPanel; //keep reference up to date!
		if(target != null) {
			target.add(newPanel);
			//resize iframe
			target.appendJavaScript("setMainFrameHeight(window.name);");
		}
		
	}
	
}



