/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.tool.components.OnlinePresenceIndicator;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.components.ProfileStatusRenderer;
import org.sakaiproject.profile2.tool.models.FriendAction;
import org.sakaiproject.profile2.tool.pages.panels.FriendsFeed;
import org.sakaiproject.profile2.tool.pages.panels.GalleryFeed;
import org.sakaiproject.profile2.tool.pages.panels.KudosPanel;
import org.sakaiproject.profile2.tool.pages.panels.ViewProfilePanel;
import org.sakaiproject.profile2.tool.pages.panels.ViewWallPanel;
import org.sakaiproject.profile2.tool.pages.windows.AddFriend;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.user.api.User;


public class ViewProfile extends BasePage {

	private static final Logger log = Logger.getLogger(ViewProfile.class);
	
	public ViewProfile(final String userUuid, final String tab)   {
				
		log.debug("ViewProfile()");

		//setup model to store the actions in the modal windows
		final FriendAction friendActionModel = new FriendAction();
		
		//get current user info
		User currentUser = sakaiProxy.getUserQuietly(sakaiProxy.getCurrentUserId());
		final String currentUserId = currentUser.getId();
		String currentUserType = currentUser.getType();
		
		//double check, if somehow got to own ViewPage, redirect to MyProfile instead
		if(userUuid.equals(currentUserId)) {
			log.warn("ViewProfile: user " + userUuid + " accessed ViewProfile for self. Redirecting...");
			throw new RestartResponseException(new MyProfile());
		}
		
		//check if super user, to grant editing rights to another user's profile
		if(sakaiProxy.isSuperUser()) {
			log.warn("ViewProfile: superUser " + currentUserId + " accessed ViewProfile for " + userUuid + ". Redirecting to allow edit.");
			throw new RestartResponseException(new MyProfile(userUuid));
		}
		
		//post view event
		sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_VIEW_OTHER, "/profile/"+userUuid, false);
		
		/* DEPRECATED via PRFL-24 when privacy was relaxed
		if(!isProfileAllowed) {
			throw new ProfileIllegalAccessException("User: " + currentUserId + " is not allowed to view profile for: " + userUuid);
		}
		*/
			
		//get some values from User
		User user = sakaiProxy.getUserQuietly(userUuid);
		String userDisplayName = user.getDisplayName();
		String userType = user.getType();
		
		//init
		final boolean friend;
		boolean friendRequestToThisPerson = false;
		boolean friendRequestFromThisPerson = false;

		//friend?
		friend = connectionsLogic.isUserXFriendOfUserY(userUuid, currentUserId);

		//if not friend, has a friend request already been made to this person?
		if(!friend) {
			friendRequestToThisPerson = connectionsLogic.isFriendRequestPending(currentUserId, userUuid);
		}
		
		//if not friend and no friend request to this person, has a friend request been made from this person to the current user?
		if(!friend && !friendRequestToThisPerson) {
			friendRequestFromThisPerson = connectionsLogic.isFriendRequestPending(userUuid, currentUserId);
		}
		
		//privacy checks
		final ProfilePrivacy privacy = privacyLogic.getPrivacyRecordForUser(userUuid);
		
		boolean isFriendsListVisible = privacyLogic.isActionAllowed(userUuid, currentUserId, PrivacyType.PRIVACY_OPTION_MYFRIENDS);
		boolean isKudosVisible = privacyLogic.isActionAllowed(userUuid, currentUserId, PrivacyType.PRIVACY_OPTION_MYKUDOS);
		boolean isGalleryVisible = privacyLogic.isActionAllowed(userUuid, currentUserId, PrivacyType.PRIVACY_OPTION_MYPICTURES);
		boolean isConnectionAllowed = sakaiProxy.isConnectionAllowedBetweenUserTypes(currentUserType, userType);
		boolean isOnlineStatusVisible = privacyLogic.isActionAllowed(userUuid, currentUserId, PrivacyType.PRIVACY_OPTION_ONLINESTATUS);
		
		final ProfilePreferences prefs = preferencesLogic.getPreferencesRecordForUser(userUuid);

		/* IMAGE */
		add(new ProfileImageRenderer("photo", userUuid, prefs, privacy, ProfileConstants.PROFILE_IMAGE_MAIN, true));
		
		/* NAME */
		Label profileName = new Label("profileName", userDisplayName);
		add(profileName);
		
		/* ONLINE PRESENCE INDICATOR */
		if(prefs.isShowOnlineStatus() && isOnlineStatusVisible){
			add(new OnlinePresenceIndicator("online", userUuid));
		} else {
			add(new EmptyPanel("online"));
		}
		
		/*STATUS PANEL */
		if(sakaiProxy.isProfileStatusEnabled()) {
			add(new ProfileStatusRenderer("status", userUuid, privacy, null, "tiny"));
		} else {
			add(new EmptyPanel("status"));
		}
		
		/* TABS */
		List<ITab> tabs = new ArrayList<ITab>();
		
		AjaxTabbedPanel tabbedPanel = new AjaxTabbedPanel("viewProfileTabs", tabs) {
			
			private static final long serialVersionUID = 1L;

			// overridden so we can add tooltips to tabs
			@Override
			protected WebMarkupContainer newLink(String linkId, final int index) {
				WebMarkupContainer link = super.newLink(linkId, index);
				
				if (ProfileConstants.TAB_INDEX_PROFILE == index) {
					link.add(new AttributeModifier("title", true,
							new ResourceModel("link.tab.profile.tooltip")));
					
				} else if (ProfileConstants.TAB_INDEX_WALL == index) {
					link.add(new AttributeModifier("title", true,
							new ResourceModel("link.tab.wall.tooltip")));	
				}
				return link;
			}
		};
		
		Cookie tabCookie = getWebRequestCycle().getWebRequest().getCookie(ProfileConstants.TAB_COOKIE);
		
		if (sakaiProxy.isProfileFieldsEnabled()) {
			tabs.add(new AbstractTab(new ResourceModel("link.tab.profile")) {
	
				private static final long serialVersionUID = 1L;
	
				@Override
				public Panel getPanel(String panelId) {
	
					setTabCookie(ProfileConstants.TAB_INDEX_PROFILE);
					return new ViewProfilePanel(panelId, userUuid, currentUserId,
							privacy, friend);
				}
			});
		}
		
		if (true == sakaiProxy.isWallEnabledGlobally()) {
			
			tabs.add(new AbstractTab(new ResourceModel("link.tab.wall")) {

				private static final long serialVersionUID = 1L;

				@Override
				public Panel getPanel(String panelId) {

					setTabCookie(ProfileConstants.TAB_INDEX_WALL);
					return new ViewWallPanel(panelId, userUuid);
				}
			});
			
			if (true == sakaiProxy.isWallDefaultProfilePage() && null == tabCookie) {
				
				tabbedPanel.setSelectedTab(ProfileConstants.TAB_INDEX_WALL);
			}
		}
		
		
		if (null != tab) {
			tabbedPanel.setSelectedTab(Integer.parseInt(tab));
		} else if (null != tabCookie) {
			try {
				tabbedPanel.setSelectedTab(Integer.parseInt(tabCookie.getValue()));
			} catch (IndexOutOfBoundsException e) {
				//do nothing. This will be thrown if the cookie contains a value > the number of tabs but thats ok.
			}
		}
		
		add(tabbedPanel);
		
		/* SIDELINKS */
		WebMarkupContainer sideLinks = new WebMarkupContainer("sideLinks");
		int visibleSideLinksCount = 0;
		
		WebMarkupContainer addFriendContainer = new WebMarkupContainer("addFriendContainer");
		
		//ADD FRIEND MODAL WINDOW
		final ModalWindow addFriendWindow = new ModalWindow("addFriendWindow");

		//FRIEND LINK/STATUS
		final AjaxLink<Void> addFriendLink = new AjaxLink<Void>("addFriendLink") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
    			addFriendWindow.show(target);
			}
		};
		
		final Label addFriendLabel = new Label("addFriendLabel");
		addFriendLink.add(addFriendLabel);
		
		addFriendContainer.add(addFriendLink);
		
		//setup link/label and windows
		if(friend) {
			addFriendLabel.setDefaultModel(new ResourceModel("text.friend.confirmed"));
    		addFriendLink.add(new AttributeModifier("class", true, new Model<String>("instruction icon connection-confirmed")));
			addFriendLink.setEnabled(false);
		} else if (friendRequestToThisPerson) {
			addFriendLabel.setDefaultModel(new ResourceModel("text.friend.requested"));
    		addFriendLink.add(new AttributeModifier("class", true, new Model<String>("instruction icon connection-request")));
			addFriendLink.setEnabled(false);
		} else if (friendRequestFromThisPerson) {
			//TODO (confirm pending friend request link)
			//could be done by setting the content off the addFriendWindow.
			//will need to rename some links to make more generic and set the onClick and setContent in here for link and window
			addFriendLabel.setDefaultModel(new ResourceModel("text.friend.pending"));
    		addFriendLink.add(new AttributeModifier("class", true, new Model<String>("instruction icon connection-request")));
			addFriendLink.setEnabled(false);
		}  else {
			addFriendLabel.setDefaultModel(new StringResourceModel("link.friend.add.name", null, new Object[]{ user.getFirstName() } ));
			addFriendWindow.setContent(new AddFriend(addFriendWindow.getContentId(), addFriendWindow, friendActionModel, currentUserId, userUuid)); 
		}
		sideLinks.add(addFriendContainer);
		
		
		//ADD FRIEND MODAL WINDOW HANDLER 
		addFriendWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
			private static final long serialVersionUID = 1L;

			public void onClose(AjaxRequestTarget target){
            	if(friendActionModel.isRequested()) { 
            		//friend was successfully requested, update label and link
            		addFriendLabel.setDefaultModel(new ResourceModel("text.friend.requested"));
            		addFriendLink.add(new AttributeModifier("class", true, new Model<String>("instruction icon connection-request")));
            		addFriendLink.setEnabled(false);
            		target.addComponent(addFriendLink);
            	}
            }
        });
		
		add(addFriendWindow);
		
		//hide connection link if not allowed
		if(!isConnectionAllowed) {
			addFriendContainer.setVisible(false);
		} else {
			visibleSideLinksCount++;
		}
		
		//hide entire list if no links to show
		if(visibleSideLinksCount == 0) {
			sideLinks.setVisible(false);
		}
		
		add(sideLinks);
		
		
		/* KUDOS PANEL */
		if(isKudosVisible) {
			add(new AjaxLazyLoadPanel("myKudos"){
				private static final long serialVersionUID = 1L;
	
				@Override
				public Component getLazyLoadComponent(String markupId) {
					if(prefs.isShowKudos()){
											
						int score = kudosLogic.getKudos(userUuid);
						if(score > 0) {
							return new KudosPanel(markupId, userUuid, currentUserId, score);
						}
					} 
					return new EmptyPanel(markupId);
				}
			});
		} else {
			add(new EmptyPanel("myKudos").setVisible(false));
		}
		
		
		/* FRIENDS FEED PANEL */
		if(isFriendsListVisible) {
			add(new AjaxLazyLoadPanel("friendsFeed") {
				private static final long serialVersionUID = 1L;

				@Override
	            public Component getLazyLoadComponent(String markupId) {
	            	return new FriendsFeed(markupId, userUuid, currentUserId);
	            }
				
	        });
		} else {
			add(new EmptyPanel("friendsFeed").setVisible(false));
		}
		
		/* GALLERY FEED PANEL */
		if (sakaiProxy.isProfileGalleryEnabledGlobally() && isGalleryVisible && prefs.isShowGalleryFeed()) {
			add(new AjaxLazyLoadPanel("galleryFeed") {
				private static final long serialVersionUID = 1L;
	
				@Override
				public Component getLazyLoadComponent(String markupId) {
					return new GalleryFeed(markupId, userUuid, currentUserId).setOutputMarkupId(true);
				}
			});
		} else {
			add(new EmptyPanel("galleryFeed").setVisible(false));
		}
	}
	
	/**
	 * This constructor is called if we have a pageParameters object containing the userUuid as an id parameter
	 * Just redirects to normal ViewProfile(String userUuid)
	 * @param parameters
	 */
	public ViewProfile(PageParameters parameters) {
		this(parameters.getString(ProfileConstants.WICKET_PARAM_USERID), parameters.getString(ProfileConstants.WICKET_PARAM_TAB));
	}
	
	public ViewProfile(final String userUuid) {
		this(userUuid, null);
	}
}
