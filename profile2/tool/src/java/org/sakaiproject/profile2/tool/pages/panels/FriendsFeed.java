package org.sakaiproject.profile2.tool.pages.panels;



import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.tool.ProfileApplication;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
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

public class FriendsFeed extends Panel {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ChangeProfilePictureUrl.class);
    private transient SakaiProxy sakaiProxy;
    private transient ProfileLogic profileLogic;
	
	public FriendsFeed(String id, final String ownerUserId, final String viewingUserId) {
		super(id);
		
		log.debug("FriendsFeed()");
		
		//get SakaiProxy
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get ProfileLogic
		profileLogic = ProfileApplication.get().getProfileLogic();

		//heading	
		Label heading = new Label("heading");
		
		if(viewingUserId.equals(ownerUserId)) {
			heading.setModel(new ResourceModel("heading.feed.my.friends"));
		} else {
			String displayName = sakaiProxy.getUserDisplayName(ownerUserId);
			heading.setModel(new StringResourceModel("heading.feed.view.friends", null, new Object[]{ displayName } ));
		}
		add(heading);
		
		
		//get our list of friends as an IDataProvider
		//the FriendDataProvider takes care of the privacy associated with the associated list
		//so what it returns will always be clean
		FriendsFeedDataProvider provider = new FriendsFeedDataProvider(ownerUserId);
		
		GridView dataView = new GridView("rows", provider) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateEmptyItem(Item item)
			{
				Link friendItem = new Link("friendsFeedItem") {
					private static final long serialVersionUID = 1L;
					public void onClick() {}
				};
				
				friendItem.add(new ProfileImageRenderer("friendPhoto", null, false, ProfileConstants.PROFILE_IMAGE_THUMBNAIL, true));

				friendItem.add(new Label("friendName","empty"));
				item.add(friendItem);
				friendItem.setVisible(false);
			}
			
			protected void populateItem(Item item)
			{
				final String friendId = (String)item.getModelObject();
				
				//setup info
				String displayName = sakaiProxy.getUserDisplayName(friendId);
		    	boolean friend;
		    	
		    	
		    	//get friend status
		    	if(ownerUserId.equals(viewingUserId)) {
		    		friend = true; //viewing own list of confirmed fiends so must be a friend
		    	} else {
		    		friend = profileLogic.isUserXFriendOfUserY(viewingUserId, friendId); //other person viewing, check if they are friends
		    	}
	    		
		    	//link to their profile
		    	AjaxLink friendItem = new AjaxLink("friendsFeedItem") {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						//link to own profile if link will point to self
		    			if(viewingUserId.equals(friendId)) {
							setResponsePage(new MyProfile());
						} else {
							setResponsePage(new ViewProfile(friendId));
						}
						
					}
				};
				
				//get privacy
				ProfilePrivacy privacy = profileLogic.getPrivacyRecordForUser(friendId);
				
				//is profile image allowed to be viewed by this user/friend?
				final boolean isProfileImageAllowed = profileLogic.isUserXProfileImageVisibleByUserY(friendId, privacy, viewingUserId,friend);
				
				/* IMAGE */
				friendItem.add(new ProfileImageRenderer("friendPhoto", friendId, isProfileImageAllowed, ProfileConstants.PROFILE_IMAGE_THUMBNAIL, true));
				
				//name (will be linked also)
		    	Label friendLinkLabel = new Label("friendName", displayName);
		    	friendItem.add(friendLinkLabel);
		
		    	item.add(friendItem);
		    	
			}
		};
		
		dataView.setColumns(3);
		add(dataView);
		
		/* NUM FRIENDS LABEL (can't just use provider as it only ever returns 6, unless we modify it to return a slice. */
		final int numFriends = profileLogic.countConfirmedFriendUserIdsForUser(ownerUserId);
		Label numFriendsLabel = new Label("numFriendsLabel");
		add(numFriendsLabel);
		
		
		/* VIEW ALL FRIENDS LINK */
    	AjaxLink viewFriendsLink = new AjaxLink("viewFriendsLink") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				//this could come from a bookmarkablelink, but this works for now
				if(numFriends == 0) {
					setResponsePage(new MySearch());
				} else {
					//if own FriendsFeed, link to own MyFriends, otherwise link to ViewFriends
					if(viewingUserId.equals(ownerUserId)) {
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
			numFriendsLabel.setModel(new ResourceModel("text.friend.feed.num.none"));
			//numFriendsLabel.setVisible(false);
			//if own FriendsFeed, show search link, otherwise hide
			if(viewingUserId.equals(ownerUserId)) {
				viewFriendsLabel.setModel(new ResourceModel("link.friend.feed.search"));
			} else {
				viewFriendsLink.setVisible(false);
			}
		} else if (numFriends == 1) {
			numFriendsLabel.setModel(new ResourceModel("text.friend.feed.num.one"));
			viewFriendsLink.setVisible(false);
		} else {
			numFriendsLabel.setModel(new StringResourceModel("text.friend.feed.num.many", null, new Object[]{ numFriends }));
			viewFriendsLabel.setModel(new ResourceModel("link.friend.feed.view"));
		}
	
	}
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("FriendsFeed has been deserialized.");
		//re-init our transient objects
		profileLogic = ProfileApplication.get().getProfileLogic();
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
	}
	
	
	
}
