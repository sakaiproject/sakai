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
import uk.ac.lancs.e_science.profile2.hbm.Friend;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.dataproviders.FriendDataProvider;
import uk.ac.lancs.e_science.profile2.tool.pages.MyFriends;
import uk.ac.lancs.e_science.profile2.tool.pages.MySearch;


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
	
	public FriendsFeed(String id, String ownerUserId, String viewingUserId) {
		super(id);
		
		//get SakaiProxy
		final SakaiProxy sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile
		final Profile profile = ProfileApplication.get().getProfile();
		

		//get randomised set of friends for this user, in future, list may be according to some rules		
		Label heading = new Label("heading", new ResourceModel("heading.feed.my.friends"));
		add(heading);
		
		
		//get our list of friends as an IDataProvider
		//the FriendDataProvider takes care of the privacy associated with the associated list
		//so what it returns will always be clean
		FriendDataProvider provider = new FriendDataProvider(ownerUserId, viewingUserId, profile);

		
		GridView dataView = new GridView("rows", provider) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateEmptyItem(Item item)
			{
				WebMarkupContainer c = new WebMarkupContainer("friendsFeedItem");
				c.add(new ContextImage("friendPhoto",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
				c.add(new Label("friendName","empty"));
				item.add(c);
				c.setVisible(false);
			}
			
			protected void populateItem(Item item)
			{
				Friend friend = (Friend)item.getModelObject();
				
				//setup info
				String friendId = friend.getUserUuid();
				String displayName = sakaiProxy.getUserDisplayName(friendId);
		    	final byte[] imageBytes = profile.getCurrentProfileImageForUser(friendId, ProfileImageManager.PROFILE_IMAGE_THUMBNAIL);
			
		    	WebMarkupContainer c = new WebMarkupContainer("friendsFeedItem");
		    	
		    	//photo
		    	if(imageBytes != null && imageBytes.length > 0){
					BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource(){
						protected byte[] getImageData() {
							return imageBytes;
						}
					};
					c.add(new Image("friendPhoto",photoResource));
				} else {
					c.add(new ContextImage("friendPhoto",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
				}
		    	
		    	//name
		    	Label nameLabel = new Label("friendName", displayName);
		    	c.add(nameLabel);
		
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
    	Link viewAllFriendsLink = new Link("viewAllFriendsLink") {
			public void onClick() {
				//this could come from a bookmarkablelink, but this works for now
				if(numFriends == 0) {
					setResponsePage(new MySearch());
				} else {
					setResponsePage(new MyFriends());
				}
			}
		};
		Label viewAllFriendsLabel = new Label("viewAllFriendsLabel");
		viewAllFriendsLink.add(viewAllFriendsLabel);
		add(viewAllFriendsLink);

		
		/* TESTS FOR THE ABOVE to change labels etc */
		if(numFriends == 0) {
			numFriendsLabel.setVisible(false);
			viewAllFriendsLabel.setModel(new ResourceModel("link.friend.feed.search"));
		} else if (numFriends == 1) {
			numFriendsLabel.setModel(new ResourceModel("link.friend.feed.num.one"));
			viewAllFriendsLabel.setModel(new ResourceModel("link.friend.feed.view"));
		} else {
			numFriendsLabel.setModel(new StringResourceModel("link.friend.feed.num.many", null, new Object[]{ numFriends }));
			viewAllFriendsLabel.setModel(new ResourceModel("link.friend.feed.view"));
		}
		
		
		
		
		
		
		
	}
	
	
	
}
