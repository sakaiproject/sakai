package uk.ac.lancs.e_science.profile2.tool.pages.panels;



import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.api.exception.ProfileBadConfigurationException;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.components.ExternalImage;
import uk.ac.lancs.e_science.profile2.tool.dataproviders.FriendsFeedDataProvider;
import uk.ac.lancs.e_science.profile2.tool.pages.MyFriends;
import uk.ac.lancs.e_science.profile2.tool.pages.MyProfile;
import uk.ac.lancs.e_science.profile2.tool.pages.MySearch;
import uk.ac.lancs.e_science.profile2.tool.pages.ViewFriends;
import uk.ac.lancs.e_science.profile2.tool.pages.ViewProfile;


/*
 * id = markup id
 * ownerUserId = userId of the page that this friends feed panel is on
 * viewingUserId = userId of the person who is viewing this friends feed panel
 * 	(this might be the same or might be different if viewing someone else's profile and is passed to the DataProvider to decide)
 * 
 */

public class FriendsFeed extends Panel {
	
	private static final long serialVersionUID = 1L;
	//private transient Logger log = Logger.getLogger(FriendsFeed.class);
	private transient Profile profile;
	private transient SakaiProxy sakaiProxy;
	
	public FriendsFeed(String id, final String ownerUserId, final String viewingUserId) {
		super(id);
		
		//get SakaiProxy
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile
		profile = ProfileApplication.get().getProfile();

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
				friendItem.add(new ContextImage("friendPhoto",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
				friendItem.add(new Label("friendName","empty"));
				item.add(friendItem);
				friendItem.setVisible(false);
			}
			
			protected void populateItem(Item item)
			{
				final String friendId = (String)item.getModelObject();
				
				//setup info
				String displayName = sakaiProxy.getUserDisplayName(friendId);
				final byte[] imageBytes;
		    	boolean friend;
		    	
		    	
		    	//get friend status
		    	if(ownerUserId.equals(viewingUserId)) {
		    		friend = true; //viewing own list of confirmed fiends so must be a friend
		    	} else {
		    		friend = profile.isUserXFriendOfUserY(viewingUserId, friendId); //other person viewing, check if they are friends
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
				
				//is profile image allowed to be viewed by this user/friend?
				final boolean isProfileImageAllowed = profile.isUserXProfileImageVisibleByUserY(friendId, viewingUserId, friend);
				
		    	if(isProfileImageAllowed) {
		    		
		    		//what type of picture should we use?
		    		int profilePictureType = sakaiProxy.getProfilePictureType();
		    		
		    		if(profilePictureType == ProfileImageManager.PICTURE_SETTING_UPLOAD) {
			    		//upload

		    			imageBytes = profile.getCurrentProfileImageForUser(friendId, ProfileImageManager.PROFILE_IMAGE_THUMBNAIL);
		    			
		    			//add uplaoded image or default
						if(imageBytes != null && imageBytes.length > 0){
							BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource(){
								private static final long serialVersionUID = 1L;
								protected byte[] getImageData() {
									return imageBytes;
								}
							};
							friendItem.add(new Image("friendPhoto",photoResource));
						} else {
							friendItem.add(new ContextImage("friendPhoto",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
						}
						
		    		} else if (profilePictureType == ProfileImageManager.PICTURE_SETTING_URL) {
		    			//url
		    			
						String externalUrl = profile.getExternalImageUrl(friendId, ProfileImageManager.PROFILE_IMAGE_MAIN, true);
						
						//add uploaded iamge or default
						if(externalUrl != null) {
							friendItem.add(new ExternalImage("friendPhoto",externalUrl));
						} else {
							friendItem.add(new ContextImage("friendPhoto",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
						}
		    		} else {
						//no valid option, exception. This is an error in Profile2, not the external configuration.
						throw new ProfileBadConfigurationException("Invalid picture type returned: " + profilePictureType);
						//perhaps we should just include the unavailable image?
					}
		    	} 
				
				
				//name (will be linked also)
		    	Label friendLinkLabel = new Label("friendName", displayName);
		    	friendItem.add(friendLinkLabel);
		
		    	item.add(friendItem);
		    	
			}
		};
		
		dataView.setColumns(3);
		add(dataView);
		
		/* NUM FRIENDS LABEL (can't just use provider as it only ever returns 6, unless we modify it to return a slice somehow. */
		final int numFriends = profile.countConfirmedFriendUserIdsForUser(ownerUserId);
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
	
	
	
}
