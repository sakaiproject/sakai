package uk.ac.lancs.e_science.profile2.tool.pages.panels;



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
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.dataproviders.FriendsFeedDataProvider;
import uk.ac.lancs.e_science.profile2.tool.pages.MyFriends;
import uk.ac.lancs.e_science.profile2.tool.pages.MySearch;
import uk.ac.lancs.e_science.profile2.tool.pages.ViewProfile;


/*
 * id = markup id
 * ownerUserId = userId of the page that this friends feed panel is on
 * viewinUserId = userId of the person who is viewing this friends feed panel
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
		Label heading = new Label("heading", new ResourceModel("heading.feed.my.friends"));
		add(heading);
		
		
		//get our list of friends as an IDataProvider
		//the FriendDataProvider takes care of the privacy associated with the associated list
		//so what it returns will always be clean
		FriendsFeedDataProvider provider = new FriendsFeedDataProvider(ownerUserId, viewingUserId);
		
		GridView dataView = new GridView("rows", provider) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateEmptyItem(Item item)
			{
				WebMarkupContainer c = new WebMarkupContainer("friendsFeedItem");
				c.add(new ContextImage("friendPhoto",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
				Link friendLink = new Link("friendLink") {
					private static final long serialVersionUID = 1L;

					public void onClick() {}
				};
				friendLink.add(new Label("friendName","empty"));
				c.add(friendLink);
				item.add(c);
				c.setVisible(false);
			}
			
			protected void populateItem(Item item)
			{
				final String friendId = (String)item.getModelObject();
				
				//setup info
				String displayName = sakaiProxy.getUserDisplayName(friendId);
		    	final byte[] imageBytes = profile.getCurrentProfileImageForUser(friendId, ProfileImageManager.PROFILE_IMAGE_THUMBNAIL);
			
		    	WebMarkupContainer c = new WebMarkupContainer("friendsFeedItem");
		    	
		    	//photo
		    	if(imageBytes != null && imageBytes.length > 0){
					BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource(){
						private static final long serialVersionUID = 1L;

						protected byte[] getImageData() {
							return imageBytes;
						}
					};
					c.add(new Image("friendPhoto",photoResource));
				} else {
					c.add(new ContextImage("friendPhoto",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
				}
		    	
		    	//name link - the list is already cleaned by FriendsFeedDataProvider so we can safely link without worrying about privacy restrictions
		    	Link friendLink = new Link("friendLink") {
					private static final long serialVersionUID = 1L;
		    		public void onClick() {
						setResponsePage(new ViewProfile(friendId));
					}
				};
		    	Label friendLinkLabel = new Label("friendName", displayName);
		    	friendLink.add(friendLinkLabel);
		    	c.add(friendLink);
		
		    	item.add(c);
		    	
			}
		};
		
		dataView.setColumns(3);
		add(dataView);
		
		/* NUM FRIENDS LABEL */
		final int numFriends = profile.getConfirmedFriendUserIdsForUser(ownerUserId).size();
		Label numFriendsLabel = new Label("numFriendsLabel");
		add(numFriendsLabel);
		
		
		/* VIEW ALL FRIENDS LINK */
    	Link viewFriendsLink = new Link("viewFriendsLink") {
			
			private static final long serialVersionUID = 1L;

			public void onClick() {
				//this could come from a bookmarkablelink, but this works for now
				if(numFriends == 0) {
					setResponsePage(new MySearch());
				} else {
					//if own FriendsFeed, link to own MyFriends, otherwise link to ViewFriends
					if(viewingUserId.equals(ownerUserId)) {
						setResponsePage(new MyFriends());
					} else {
						//setResponsePage(new ViewFriends());
					}
				}
			}
		};
		Label viewFriendsLabel = new Label("viewFriendsLabel");
		viewFriendsLink.add(viewFriendsLabel);
		add(viewFriendsLink);

		
		/* TESTS FOR THE ABOVE to change labels and links */
		if(numFriends == 0) {
			numFriendsLabel.setVisible(false);
			//if own FriendsFeed, show search link, otherwise hide
			if(viewingUserId.equals(ownerUserId)) {
				viewFriendsLabel.setModel(new ResourceModel("link.friend.feed.search"));
			} else {
				viewFriendsLink.setVisible(false);
			}
		} else if (numFriends == 1) {
			numFriendsLabel.setModel(new ResourceModel("link.friend.feed.num.one"));
			viewFriendsLink.setVisible(false);
		} else {
			numFriendsLabel.setModel(new StringResourceModel("link.friend.feed.num.many", null, new Object[]{ numFriends }));
			viewFriendsLabel.setModel(new ResourceModel("link.friend.feed.view"));
		}
		
		
		
		
		
		
		
	}
	
	
	
}
