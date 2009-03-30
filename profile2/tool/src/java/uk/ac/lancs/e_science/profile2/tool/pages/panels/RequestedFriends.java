package uk.ac.lancs.e_science.profile2.tool.pages.panels;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.components.ProfileImageRenderer;
import uk.ac.lancs.e_science.profile2.tool.dataproviders.RequestedFriendsDataProvider;
import uk.ac.lancs.e_science.profile2.tool.models.FriendAction;
import uk.ac.lancs.e_science.profile2.tool.pages.MyFriends;
import uk.ac.lancs.e_science.profile2.tool.pages.ViewProfile;
import uk.ac.lancs.e_science.profile2.tool.pages.windows.ConfirmFriend;
import uk.ac.lancs.e_science.profile2.tool.pages.windows.IgnoreFriend;

public class RequestedFriends extends Panel {
	
	private static final long serialVersionUID = 1L;
	private transient Logger log = Logger.getLogger(RequestedFriends.class);
	private transient SakaiProxy sakaiProxy;
	private transient Profile profile; 
	private int numRequestedFriends = 0;
	
	public RequestedFriends(final String id, final MyFriends parent, final String userId) {
		super(id);
		
		//get SakaiProxy
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile
		profile = ProfileApplication.get().getProfile();
		
		//setup model to store the actions in the modal windows
		final FriendAction friendActionModel = new FriendAction();
	
		//get our list of friend requests as an IDataProvider
		RequestedFriendsDataProvider provider = new RequestedFriendsDataProvider(userId);
		
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
		    			    	
		    	//get name
		    	String displayName = sakaiProxy.getUserDisplayName(friendId);
		    	
		    	//is this user allowed to view this person's profile image?
				boolean isProfileImageAllowed = profile.isUserXProfileImageVisibleByUserY(friendId, userId, false);
				
				//image
				item.add(new ProfileImageRenderer("result-photo", friendId, isProfileImageAllowed, ProfileImageManager.PROFILE_IMAGE_THUMBNAIL, true));
		    			    	
		    	//name and link to profile
		    	Link profileLink = new Link("result-profileLink") {
					private static final long serialVersionUID = 1L;

					public void onClick() {
						setResponsePage(new ViewProfile(friendId));
					}
					
				};
				profileLink.add(new Label("result-name", displayName));
		    	item.add(profileLink);
		    	
		    	
		    	//is status allowed to be viewed by this user?
				final boolean isProfileStatusAllowed = profile.isUserXStatusVisibleByUserY(friendId, userId, false);
				
				Label statusMsgLabel = new Label("result-statusMsg");
				Label statusDateLabel = new Label("result-statusDate");
				
				if(isProfileStatusAllowed) {
					String profileStatusMessage = profile.getUserStatusMessage(friendId);
					Date profileStatusDate = profile.getUserStatusDate(friendId);
					if(profileStatusMessage == null) {
						statusMsgLabel.setVisible(false);
						statusDateLabel.setVisible(false);
					} else {
						//message
						statusMsgLabel.setModel(new Model(profileStatusMessage));
						
						//now date
						if(profileStatusDate == null) {
							statusDateLabel.setVisible(false);
						} else {
							statusDateLabel.setModel(new Model(profile.convertDateForStatus(profileStatusDate)));
						}
					}
				}
				
				item.add(statusMsgLabel);
				item.add(statusDateLabel);
		    	
		    	
		    	
		    	
		    	
		    	
		    	/* ACTIONS */
		    	
		    	//CONFIRM FRIEND MODAL WINDOW
				final ModalWindow confirmFriendWindow = new ModalWindow("confirmFriendWindow");
				confirmFriendWindow.setContent(new ConfirmFriend(confirmFriendWindow.getContentId(), confirmFriendWindow, friendActionModel, userId, friendId)); 
				
				//IGNORE FRIEND MODAL WINDOW
				final ModalWindow ignoreFriendWindow = new ModalWindow("ignoreFriendWindow");
				ignoreFriendWindow.setContent(new IgnoreFriend(ignoreFriendWindow.getContentId(), ignoreFriendWindow, friendActionModel, userId, friendId)); 
				
				
				
				//IGNORE FRIEND LINK
		    	final AjaxLink ignoreFriendLink = new AjaxLink("ignoreFriendLink") {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						
						ignoreFriendWindow.show(target);
						target.appendJavascript("fixWindowVertical();"); 

					}
				};
				ContextImage ignoreFriendIcon = new ContextImage("ignoreFriendIcon",new Model(ProfileImageManager.CANCEL_IMG));
				ignoreFriendLink.add(ignoreFriendIcon);
				ignoreFriendLink.add(new AttributeModifier("title", true,new ResourceModel("link.title.ignorefriend")));
				item.add(ignoreFriendLink);
				
				//CONFIRM FRIEND LINK
		    	final AjaxLink confirmFriendLink = new AjaxLink("confirmFriendLink") {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						
						confirmFriendWindow.show(target);
						target.appendJavascript("fixWindowVertical();"); 

					}
				};
				ContextImage confirmFriendIcon = new ContextImage("confirmFriendIcon",new Model(ProfileImageManager.ACCEPT_IMG));
				confirmFriendLink.add(confirmFriendIcon);
				confirmFriendLink.add(new AttributeModifier("title", true,new ResourceModel("link.title.confirmfriend")));
				item.add(confirmFriendLink);

				
				
				
				
				// CONFIRM FRIEND MODAL WINDOW HANDLER 
				confirmFriendWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
					private static final long serialVersionUID = 1L;
					public void onClose(AjaxRequestTarget target){
		            	if(friendActionModel.isConfirmed()) { 
		            		
		            		//decrement number of requests
		            		numRequestedFriends--;
		            		
		            		//remove friend item from display
		            		target.appendJavascript("$('#" + item.getMarkupId() + "').slideUp();");
		            		
		            		//update label
		            		target.addComponent(requestedFriendsHeading);
		            		
		            		//TODO repaint confirmed friends panel by calling method in MyFriends to repaint it for us
		            		parent.updateConfirmedFriends(target, userId);
		            		
		            		//if none left, hide everything
		            		if(numRequestedFriends==0) {
		            			target.appendJavascript("$('#" + requestedFriendsHeading.getMarkupId() + "').fadeOut();");
		            			target.appendJavascript("$('#" + requestedFriendsContainer.getMarkupId() + "').fadeOut();");
		            		}
		            		
		            	}
		            }
		        });
				item.add(confirmFriendWindow);
				
				// IGNORE FRIEND MODAL WINDOW HANDLER 
				ignoreFriendWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
					private static final long serialVersionUID = 1L;
					public void onClose(AjaxRequestTarget target){
		            	if(friendActionModel.isIgnored()) { 
		            		
		            		//decrement number of requests
		            		numRequestedFriends--;
		            		
		            		//remove friend item from display
		            		target.appendJavascript("$('#" + item.getMarkupId() + "').slideUp();");
		            		
		            		//update label
		            		target.addComponent(requestedFriendsHeading);
		            				            		
		            		//if none left, hide everything
		            		if(numRequestedFriends==0) {
		            			target.appendJavascript("$('#" + requestedFriendsHeading.getMarkupId() + "').fadeOut();");
		            			target.appendJavascript("$('#" + requestedFriendsContainer.getMarkupId() + "').fadeOut();");
		            		}
		            		
		            	}
		            }
		        });
				item.add(ignoreFriendWindow);
				
				
				item.setOutputMarkupId(true);
				
		    }
			
		};
		requestedFriendsDataView.setOutputMarkupId(true);
		requestedFriendsContainer.add(requestedFriendsDataView);

		//add results container
		add(requestedFriendsContainer);
		
		//initially, if no requests, hide everything
		if(numRequestedFriends == 0) {
			this.setVisible(false);
		}
		
	}
	
}
