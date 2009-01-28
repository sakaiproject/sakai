package uk.ac.lancs.e_science.profile2.tool.pages.panels;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.dataproviders.ConfirmedFriendsDataProvider;
import uk.ac.lancs.e_science.profile2.tool.pages.ViewProfile;

public class ConfirmedFriends extends Panel {
	
	private static final long serialVersionUID = 1L;
	private transient Logger log = Logger.getLogger(ConfirmedFriends.class);
	private transient SakaiProxy sakaiProxy;
	private transient Profile profile; 
	
	public ConfirmedFriends(final String id, final String userX) {
		super(id);
		
		//get SakaiProxy
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile
		profile = ProfileApplication.get().getProfile();
		
		//get id of user viewing this page (will be the same if user is viewing own list, different if viewing someone else's)
		final String userY = sakaiProxy.getCurrentUserId();
		
		//get our list of confirmed friends as an IDataProvider
		//this provider takes care of cleaning the list so what we return is always safe to print
		//ie the privacy settings for each user is already taken care of
		ConfirmedFriendsDataProvider provider = new ConfirmedFriendsDataProvider(userX, userY);
		
		Label heading = new Label("confirmedFriendsHeading");
		//if viewing own list, "my friends", else, "their name's friends"
		if(userX.equals(userY)) {
			heading.setModel(new ResourceModel("heading.friends.my"));
		} else {
			String displayName = sakaiProxy.getUserDisplayName(userX);
			heading.setModel(new StringResourceModel("heading.friends.view", null, new Object[]{ displayName } ));
		}
		add(heading);
		
		//container which wraps list
		final WebMarkupContainer confirmedFriendsContainer = new WebMarkupContainer("confirmedFriendsContainer");
		confirmedFriendsContainer.setOutputMarkupId(true);
		
		//search results
		DataView resultsDataView = new DataView("results-list", provider) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(Item item) {
		        
		    	//get friendId
		    	final String friendId = (String)item.getModelObject();
		    			    	
		    	//setup basic values
		    	String displayName = sakaiProxy.getUserDisplayName(friendId);
		    	final byte[] photo = profile.getCurrentProfileImageForUser(friendId, ProfileImageManager.PROFILE_IMAGE_THUMBNAIL);
		    			    	
		    	//photo/default photo
		    	if(photo != null && photo.length > 0){
		    		
					BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource(){
						private static final long serialVersionUID = 1L;

						protected byte[] getImageData() {
							return photo;
						}
					};
				
					item.add(new Image("result-photo",photoResource));
				} else {
					item.add(new ContextImage("result-photo",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
				}
		    	
		    	//name and link to profile
		    	Link profileLink = new Link("result-profileLink") {
					private static final long serialVersionUID = 1L;

					public void onClick() {
						setResponsePage(new ViewProfile(friendId));
					}
					
				};
				

				profileLink.add(new Label("result-name", displayName));
		    	item.add(profileLink);
		    	
		    	
				
		    }

			
		};
		confirmedFriendsContainer.add(resultsDataView);
		
		
		
		//add results container
		add(confirmedFriendsContainer);
		
		
		
	}
	
}
