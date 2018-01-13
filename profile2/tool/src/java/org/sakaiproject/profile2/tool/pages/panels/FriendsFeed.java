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
package org.sakaiproject.profile2.tool.pages.panels;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfilePreferencesLogic;
import org.sakaiproject.profile2.logic.ProfilePrivacyLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.tool.components.ProfileImage;
import org.sakaiproject.profile2.tool.dataproviders.FriendsFeedDataProvider;
import org.sakaiproject.profile2.tool.pages.MyFriends;
import org.sakaiproject.profile2.tool.pages.MyProfile;
import org.sakaiproject.profile2.tool.pages.MySearch;
import org.sakaiproject.profile2.tool.pages.ViewFriends;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

/*
 * id = markup id
 * ownerUserId = userId of the page that this friends feed panel is on
 * viewingUserId = userId of the person who is viewing this friends feed panel
 * 	(this might be the same or might be different if viewing someone else's profile and is passed to the DataProvider to decide)
 * 
 */
@Slf4j
public class FriendsFeed extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	protected ProfileConnectionsLogic connectionsLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePreferencesLogic")
	protected ProfilePreferencesLogic preferencesLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	protected ProfilePrivacyLogic privacyLogic;
	
	
	public FriendsFeed(String id, final String ownerUserId, final String viewingUserId) {
		super(id);
		
		log.debug("FriendsFeed()");
		
		//heading	
		Label heading = new Label("heading");
		
		if(viewingUserId.equals(ownerUserId)) {
			heading.setDefaultModel(new ResourceModel("heading.widget.my.friends"));
		} else {
			String displayName = sakaiProxy.getUserDisplayName(ownerUserId);
			heading.setDefaultModel(new StringResourceModel("heading.widget.view.friends", null, new Object[]{ displayName } ));
		}
		add(heading);
		
		
		//get our list of friends as an IDataProvider
		//the FriendDataProvider takes care of the privacy associated with the associated list
		//so what it returns will always be clean
		FriendsFeedDataProvider provider = new FriendsFeedDataProvider(ownerUserId);
		
		GridView<Person> dataView = new GridView<Person>("rows", provider) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateEmptyItem(Item<Person> item)
			{
				Link friendItem = new Link("friendsFeedItem") {
					private static final long serialVersionUID = 1L;
					public void onClick() {}
				};
				
				ProfileImage friendPhoto = new ProfileImage("friendPhoto", new Model<String>(null));
				friendPhoto.setSize(ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
				friendItem.add(friendPhoto);

				friendItem.add(new Label("friendName","empty"));
				item.add(friendItem);
				friendItem.setVisible(false);
			}
			
			protected void populateItem(Item<Person> item)
			{
				final Person person = (Person)item.getDefaultModelObject();
				final String friendId = person.getUuid();
				
				//setup info
				String displayName = person.getDisplayName();
		    	boolean friend;
		    	
		    	
		    	//get friend status
		    	if(ownerUserId.equals(viewingUserId)) {
		    		friend = true; //viewing own list of friends so must be a friend
		    	} else {
		    		friend = connectionsLogic.isUserXFriendOfUserY(viewingUserId, friendId); //other person viewing, check if they are friends
		    	}
	    		
		    	//link to their profile
		    	Link<String> friendItem = new Link<String>("friendsFeedItem") {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						//link to own profile if link will point to self
		    			if(viewingUserId.equals(friendId)) {
							setResponsePage(new MyProfile());
						} else {
							setResponsePage(new ViewProfile(friendId));
						}
						
					}
				};
				
				/* IMAGE */
				ProfileImage friendPhoto = new ProfileImage("friendPhoto", new Model<String>(friendId));
				friendPhoto.setSize(ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
				friendItem.add(friendPhoto);
				
				//name (will be linked also)
		    	Label friendLinkLabel = new Label("friendName", displayName);
		    	friendItem.add(friendLinkLabel);
		
		    	item.add(friendItem);
		    	
			}
		};
		
		dataView.setColumns(3);
		add(dataView);
		
		/* NUM FRIENDS LABEL (can't just use provider as it only ever returns the number in the grid */
		final int numFriends = connectionsLogic.getConnectionsForUserCount(ownerUserId);
		Label numFriendsLabel = new Label("numFriendsLabel");
		add(numFriendsLabel);
		
		
		/* VIEW ALL FRIENDS LINK */
    	Link<String> viewFriendsLink = new Link<String>("viewFriendsLink") {
			private static final long serialVersionUID = 1L;

			public void onClick() {
				//this could come from a bookmarkablelink, but this works for now
				if(numFriends == 0) {
					setResponsePage(new MySearch());
				} else {
					//if own FriendsFeed, link to own MyFriends, otherwise link to ViewFriends
					if (sakaiProxy.isSuperUserAndProxiedToUser(ownerUserId)) {
						setResponsePage(new ViewFriends(ownerUserId));
					} else if (viewingUserId.equals(ownerUserId)) {
						setResponsePage(new MyFriends());
					} else {
						setResponsePage(new ViewFriends(ownerUserId));
					}
				}
			}
		};
		Label viewFriendsLabel = new Label("viewFriendsLabel");
		viewFriendsLink.add(viewFriendsLabel);
		add(viewFriendsLink);

		
		/* TESTS FOR THE ABOVE to change labels and links */
		if(numFriends == 0) {
			numFriendsLabel.setDefaultModel(new ResourceModel("text.friend.feed.num.none"));
			//numFriendsLabel.setVisible(false);
			//if own FriendsFeed, show search link, otherwise hide
			if(viewingUserId.equals(ownerUserId)) {
				if (sakaiProxy.isSearchEnabledGlobally()) {
					viewFriendsLabel.setDefaultModel(new ResourceModel("link.friend.feed.search"));
				}
			} else {
				viewFriendsLink.setVisible(false);
			}
		} else if (numFriends == 1) {
			numFriendsLabel.setDefaultModel(new ResourceModel("text.friend.feed.num.one"));
			viewFriendsLink.setVisible(false);
		} else {
			numFriendsLabel.setDefaultModel(new StringResourceModel("text.friend.feed.num.many", null, new Object[]{ numFriends }));
			viewFriendsLabel.setDefaultModel(new ResourceModel("link.friend.feed.view"));
		}
	
	}
	
	
}
