package uk.ac.lancs.e_science.profile2.tool.pages.panels;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.dataproviders.RequestedFriendsDataProvider;
import uk.ac.lancs.e_science.profile2.tool.models.FriendAction;
import uk.ac.lancs.e_science.profile2.tool.pages.ViewProfile;
import uk.ac.lancs.e_science.profile2.tool.pages.windows.RemoveFriend;

public class RequestedFriends extends Panel {
	
	private static final long serialVersionUID = 1L;
	private transient Logger log = Logger.getLogger(RequestedFriends.class);
	private transient SakaiProxy sakaiProxy;
	private transient Profile profile; 
	private int numRequestedFriends = 0;
	
	public RequestedFriends(final String id, final String userX) {
		super(id);
		
		//get SakaiProxy
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile
		profile = ProfileApplication.get().getProfile();
			
		//setup model to store the actions in the modal windows
		final FriendAction friendActionModel = new FriendAction();
	
		//get our list of friend requests as an IDataProvider
		RequestedFriendsDataProvider provider = new RequestedFriendsDataProvider(userX);
		
		//init number of requests
		numRequestedFriends = provider.size();
		
		//model so we can update the number of requests
		IModel numRequestedFriendsModel = new Model() {
			private static final long serialVersionUID = 1L;

			public Object getObject() {
				return numRequestedFriends;
			} 
		};
		
		//heading
		final WebMarkupContainer requestedFriendsHeading = new WebMarkupContainer("requestedFriendsHeading");
		requestedFriendsHeading.add(new Label("requestedFriendsLabel", new ResourceModel("heading.friend.requests")));
		requestedFriendsHeading.add(new Label("requestedFriendsNumber", numRequestedFriendsModel));
		requestedFriendsHeading.setOutputMarkupId(true);
		add(requestedFriendsHeading);
		
		//container which wraps list
		final WebMarkupContainer requestedFriendsContainer = new WebMarkupContainer("requestedFriendsContainer");
		requestedFriendsContainer.setOutputMarkupId(true);
		
		//search results
		DataView requestedFriendsDataView = new DataView("results-list", provider) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item item) {
		        
		    	//get friendId
		    	final String friendId = (String)item.getModelObject();
		    			    	
		    	//setup basic values
		    	/*
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
		    	*/
		    	/* ACTIONS */
		    	/*
		    	//REMOVE FRIEND MODAL WINDOW
				final ModalWindow removeFriendWindow = new ModalWindow("removeFriendWindow");
				removeFriendWindow.setContent(new RemoveFriend(removeFriendWindow.getContentId(), removeFriendWindow, friendActionModel, userX, friendId)); 
				
				//REMOVE FRIEND LINK
		    	final AjaxLink removeFriendLink = new AjaxLink("removeFriendLink") {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						
						//target.appendJavascript("Wicket.Window.get().window.style.width='800px';");
						removeFriendWindow.show(target);
						target.appendJavascript("fixWindowVertical();"); 

					}
				};
				ContextImage removeFriendIcon = new ContextImage("removeFriendIcon",new Model(ProfileImageManager.REMOVE_IMG));
				removeFriendLink.add(removeFriendIcon);
				removeFriendLink.add(new AttributeModifier("title", true,new ResourceModel("link.title.removefriend")));
				removeFriendLink.add(removeFriendIcon);
				item.add(removeFriendLink);
				
				// REMOVE FRIEND MODAL WINDOW HANDLER 
				removeFriendWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
					private static final long serialVersionUID = 1L;
					public void onClose(AjaxRequestTarget target){
		            	if(friendActionModel.isRemoved()) { 
		            		
		            		//decrement number of friends
		            		numConfirmedFriends--;
		            		
		            		//remove friend item from display
		            		target.appendJavascript("$('#" + item.getMarkupId() + "').slideUp();");
		            		
		            		//update label
		            		target.addComponent(numConfirmedFriendsContainer);
		            		
		            		//if none left, hide whole thing
		            		if(numConfirmedFriends==0) {
		            			target.appendJavascript("$('#" + confirmedFriendsContainer.getMarkupId() + "').fadeOut();");
		            			//confirmedFriendsContainer.setVisible(false);
		            			//target.addComponent(confirmedFriendsContainer);
		            		}
		            		
		            	}
		            }
		        });
				item.add(removeFriendWindow);
				*/
				item.setOutputMarkupId(true);
				
		    }
			
		};
		requestedFriendsDataView.setOutputMarkupId(true);
		requestedFriendsContainer.add(requestedFriendsDataView);

		//add results container
		add(requestedFriendsContainer);
		
		//initially, if no requests, hide container
		if(numRequestedFriends == 0) {
			requestedFriendsHeading.setVisible(false);
			requestedFriendsContainer.setVisible(false);
		}
		
	}
	
}
