package org.sakaiproject.profile2.tool.pages.panels;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.tool.ProfileApplication;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.components.ProfileStatusRenderer;
import org.sakaiproject.profile2.tool.dataproviders.ConfirmedFriendsDataProvider;
import org.sakaiproject.profile2.tool.models.FriendAction;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.tool.pages.windows.RemoveFriend;
import org.sakaiproject.profile2.util.ProfileConstants;

public class ConfirmedFriends extends Panel {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ConfirmedFriends.class);
    private transient SakaiProxy sakaiProxy;
    private transient ProfileLogic profileLogic;
	private int numConfirmedFriends = 0;
	private boolean ownList = false;
	
	
	public ConfirmedFriends(final String id, final String userUuid) {
		super(id);
		
		log.debug("ConfirmedFriends()");
		
		//get SakaiProxy
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get ProfileLogic
		profileLogic = ProfileApplication.get().getProfileLogic();
			
		//setup model to store the actions in the modal windows
		final FriendAction friendActionModel = new FriendAction();
		
		//get id of user viewing this page (will be the same if user is viewing own list, different if viewing someone else's)
		final String currentUserUuid = sakaiProxy.getCurrentUserId();
		
		//if viewing own friends, you can manage them.
		if(userUuid.equals(currentUserUuid)) {
			ownList = true;
		}
		
		//get our list of confirmed friends as an IDataProvider
		ConfirmedFriendsDataProvider provider = new ConfirmedFriendsDataProvider(userUuid);
		
		//init number of friends
		numConfirmedFriends = provider.size();
		
		//model so we can update the number of friends
		IModel numConfirmedFriendsModel = new Model() {
			private static final long serialVersionUID = 1L;

			public Object getObject() {
				return numConfirmedFriends;
			} 
		};
		
		//heading
		final WebMarkupContainer confirmedFriendsHeading = new WebMarkupContainer("confirmedFriendsHeading");
		Label confirmedFriendsLabel = new Label("confirmedFriendsLabel");
		//if viewing own list, "my friends", else, "their name's friends"
		if(ownList) {
			confirmedFriendsLabel.setModel(new ResourceModel("heading.friends.my"));
		} else {
			String displayName = sakaiProxy.getUserDisplayName(userUuid);
			confirmedFriendsLabel.setModel(new StringResourceModel("heading.friends.view", null, new Object[]{ displayName } ));
		}
		confirmedFriendsHeading.add(confirmedFriendsLabel);
		confirmedFriendsHeading.add(new Label("confirmedFriendsNumber", numConfirmedFriendsModel));
		confirmedFriendsHeading.setOutputMarkupId(true);
		add(confirmedFriendsHeading);
		
		//no friends message (only show if viewing own list)
		/*
		final WebMarkupContainer noFriendsContainer = new WebMarkupContainer("noFriendsContainer");
		noFriendsContainer.setOutputMarkupId(true);
		
		final Link noFriendsLink = new Link("noFriendsLink", new ResourceModel("link.friend.search")) {
			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new MySearch());
			}

		};
		noFriendsContainer.add(noFriendsLink);
		noFriendsContainer.setVisible(false);
		add(noFriendsContainer);
		*/
		
	
		
		//container which wraps list
		final WebMarkupContainer confirmedFriendsContainer = new WebMarkupContainer("confirmedFriendsContainer");
		confirmedFriendsContainer.setOutputMarkupId(true);
		
		//results
		DataView confirmedFriendsDataView = new DataView("results-list", provider) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item item) {
		        
		    	//get friendUuid
		    	final String friendUuid = (String)item.getModelObject();
		    			    	
		    	//setup values
		    	String displayName = sakaiProxy.getUserDisplayName(friendUuid);
		    	boolean friend;
		    	
		    	//get friend status
		    	if(ownList) {
		    		friend = true; //viewing own page of conenctions, must be friend!
		    	} else {
		    		friend = profileLogic.isUserXFriendOfUserY(userUuid, friendUuid); //other person viewing, check if they are friends
		    	}
		    	
		    	//get privacy record for the friend
		    	ProfilePrivacy privacy = profileLogic.getPrivacyRecordForUser(friendUuid);
	    		
		    	//is profile image allowed to be viewed by this user/friend?
				final boolean isProfileImageAllowed = profileLogic.isUserXProfileImageVisibleByUserY(friendUuid, privacy, currentUserUuid, friend);
				
				//image
				item.add(new ProfileImageRenderer("result-photo", friendUuid, isProfileImageAllowed, ProfileConstants.PROFILE_IMAGE_THUMBNAIL, true));
			
		    	//name and link to profile
		    	Link profileLink = new Link("result-profileLink") {
					private static final long serialVersionUID = 1L;

					public void onClick() {
						setResponsePage(new ViewProfile(friendUuid));
					}
					
				};
				profileLink.add(new Label("result-name", displayName));
		    	item.add(profileLink);
		    	
		    	//status component
				ProfileStatusRenderer status = new ProfileStatusRenderer("result-status", friendUuid, privacy, currentUserUuid, friend, "friendsListInfoStatusMessage", "friendsListInfoStatusDate");
				status.setOutputMarkupId(true);
				item.add(status);
		    	
		    	
		    	/* ACTIONS */
		    	
		    	//REMOVE FRIEND MODAL WINDOW
				final ModalWindow removeFriendWindow = new ModalWindow("removeFriendWindow");
				removeFriendWindow.setContent(new RemoveFriend(removeFriendWindow.getContentId(), removeFriendWindow, friendActionModel, userUuid, friendUuid)); 
				
				//REMOVE FRIEND LINK
		    	final AjaxLink removeFriendLink = new AjaxLink("removeFriendLink") {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						
						//target.appendJavascript("Wicket.Window.get().window.style.width='800px';");
						removeFriendWindow.show(target);
						target.appendJavascript("fixWindowVertical();"); 

					}
				};
				ContextImage removeFriendIcon = new ContextImage("removeFriendIcon",new Model(ProfileConstants.DELETE_IMG));
				removeFriendLink.add(removeFriendIcon);
				removeFriendLink.add(new AttributeModifier("title", true,new ResourceModel("link.title.removefriend")));
				item.add(removeFriendLink);
				
				//can only delete if own friends
				if(!ownList) {
					removeFriendLink.setEnabled(true);
					removeFriendLink.setVisible(false);
				}
				
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
		            		target.addComponent(confirmedFriendsHeading);
		            		
		            		//if none left, hide whole thing
		            		if(numConfirmedFriends==0) {
		            			target.appendJavascript("$('#" + confirmedFriendsContainer.getMarkupId() + "').fadeOut();");
		            		}
		            		
		            	}
		            }
		        });
				item.add(removeFriendWindow);
				
				item.setOutputMarkupId(true);
				
		    }
			
		};
		confirmedFriendsDataView.setOutputMarkupId(true);
		confirmedFriendsDataView.setItemsPerPage(ProfileConstants.MAX_CONNECTIONS_PER_PAGE);
		
		confirmedFriendsContainer.add(confirmedFriendsDataView);

		//add results container
		add(confirmedFriendsContainer);
		
		//add pager
		PagingNavigator pager = new PagingNavigator("navigator", confirmedFriendsDataView);
		add(pager);

		//initially, if no friends, hide container
		if(numConfirmedFriends == 0) {
			confirmedFriendsContainer.setVisible(false);
			pager.setVisible(false);
		}
	}
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("ConfirmedFriends has been deserialized.");
		//re-init our transient objects
		profileLogic = ProfileApplication.get().getProfileLogic();
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
	}
	
}
