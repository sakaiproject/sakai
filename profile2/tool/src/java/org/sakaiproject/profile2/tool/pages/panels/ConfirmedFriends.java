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

package org.sakaiproject.profile2.tool.pages.panels;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
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
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.components.ProfileStatusRenderer;
import org.sakaiproject.profile2.tool.dataproviders.ConfirmedFriendsDataProvider;
import org.sakaiproject.profile2.tool.models.FriendAction;
import org.sakaiproject.profile2.tool.pages.MySearch;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.tool.pages.windows.RemoveFriend;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.user.api.User;

public class ConfirmedFriends extends Panel {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ConfirmedFriends.class);
   
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
    @SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;
	
    
	private Integer numConfirmedFriends = 0;
	private boolean ownList = false;
	
	
	public ConfirmedFriends(final String id, final String userUuid) {
		super(id);
		
		log.debug("ConfirmedFriends()");
		
		//setup model to store the actions in the modal windows
		final FriendAction friendActionModel = new FriendAction();
		
		//get info for user viewing this page (will be the same if user is viewing own list, different if viewing someone else's)
		final String currentUserUuid = sakaiProxy.getCurrentUserId();
		User currentUser = sakaiProxy.getUserQuietly(currentUserUuid);
		final String currentUserType = currentUser.getType(); //to be used for checking if connection between users is allowed, when this is added
		
		//if viewing own friends, you can manage them.
		if(userUuid.equals(currentUserUuid)) {
			ownList = true;
		}
		
		//get our list of confirmed friends as an IDataProvider
		ConfirmedFriendsDataProvider provider = new ConfirmedFriendsDataProvider(userUuid);
		
		//init number of friends
		numConfirmedFriends = provider.size();
		
		//model so we can update the number of friends
		IModel<Integer> numConfirmedFriendsModel = new Model<Integer>() {
			private static final long serialVersionUID = 1L;
			public Integer getObject() {
				return numConfirmedFriends;
			} 
		};
		
		//heading
		final WebMarkupContainer confirmedFriendsHeading = new WebMarkupContainer("confirmedFriendsHeading");
		Label confirmedFriendsLabel = new Label("confirmedFriendsLabel");
		//if viewing own list, "my friends", else, "their name's friends"
		if(ownList) {
			confirmedFriendsLabel.setDefaultModel(new ResourceModel("heading.friends.my"));
		} else {
			String displayName = sakaiProxy.getUserDisplayName(userUuid);
			confirmedFriendsLabel.setDefaultModel(new StringResourceModel("heading.friends.view", null, new Object[]{ displayName } ));
		}
		confirmedFriendsHeading.add(confirmedFriendsLabel);
		confirmedFriendsHeading.add(new Label("confirmedFriendsNumber", numConfirmedFriendsModel));
		confirmedFriendsHeading.setOutputMarkupId(true);
		add(confirmedFriendsHeading);
		
		//search for friends
		WebMarkupContainer searchFriends = new WebMarkupContainer("searchFriends");
    	AjaxLink searchFriendsLink = new AjaxLink("searchFriendsLink") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				setResponsePage(new MySearch());
			}
    	};
		Label searchFriendsLabel = new Label("searchFriendsLabel",
				new ResourceModel("link.my.friends.search"));
		searchFriendsLink.add(searchFriendsLabel);
    	searchFriends.add(searchFriendsLink);
    	add(searchFriends);
    	
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
		
		//connection window
		final ModalWindow connectionWindow = new ModalWindow("connectionWindow");

		//results
		DataView<Person> confirmedFriendsDataView = new DataView<Person>("connections", provider) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item<Person> item) {
		        
				Person person = (Person)item.getDefaultModelObject();
				String personUuid = person.getUuid();
		    			    	
		    	//setup values
		    	String displayName = person.getDisplayName();
		    	boolean friend;
		    	
		    	//get friend status
		    	if(ownList) {
		    		friend = true; //viewing own page of conenctions, must be friend!
		    	} else {
		    		friend = profileLogic.isUserXFriendOfUserY(userUuid, personUuid); //other person viewing, check if they are friends
		    	}
		    	
		    	//REMOVE THIS WHEN WE FLESH OUT THE PERSON OBJECT RETURNED
				//ensure privacy
				ProfilePrivacy privacy = person.getPrivacy();
				if(privacy == null){
					 privacy = profileLogic.getPrivacyRecordForUser(personUuid);
				}
				
				//ensure preferences
				ProfilePreferences prefs = person.getPreferences();
				if(prefs == null){
					prefs = profileLogic.getPreferencesRecordForUser(personUuid);
				}
		    	
		    	
				//image wrapper, links to profile
		    	Link<String> friendItem = new Link<String>("connectionPhotoWrap", new Model<String>(personUuid)) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new ViewProfile(getModelObject()));
					}
				};
				
				//image
				friendItem.add(new ProfileImageRenderer("connectionPhoto", personUuid, prefs, privacy, ProfileConstants.PROFILE_IMAGE_THUMBNAIL, true));
				item.add(friendItem);
				
		    	//name and link to profile
		    	Link<String> profileLink = new Link<String>("connectionLink", new Model<String>(personUuid)) {
					private static final long serialVersionUID = 1L;

					public void onClick() {
						setResponsePage(new ViewProfile(getModelObject()));
					}
					
				};
				profileLink.add(new Label("connectionName", displayName));
		    	item.add(profileLink);
		    	
		    	//status component
				ProfileStatusRenderer status = new ProfileStatusRenderer("connectionStatus", person, "connection-status-msg", "connection-status-date");
				status.setOutputMarkupId(true);
				item.add(status);
		    	
		    	
		    	/* ACTIONS */
		    	
				//REMOVE FRIEND LINK AND WINDOW
		    	final AjaxLink<String> removeConnectionLink = new AjaxLink<String>("removeConnectionLink", new Model<String>(personUuid)) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						
						//get this item, and set content for modalwindow
				    	String friendUuid = getModelObject();				    	
						connectionWindow.setContent(new RemoveFriend(connectionWindow.getContentId(), connectionWindow, friendActionModel, userUuid, friendUuid)); 
						
						//modalwindow handler 
						connectionWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
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
						
						connectionWindow.show(target);
						target.appendJavascript("fixWindowVertical();"); 
					}
				};
				ContextImage removeConnectionIcon = new ContextImage("removeConnectionIcon",new Model<String>(ProfileConstants.DELETE_IMG));
				removeConnectionLink.add(removeConnectionIcon);
				removeConnectionLink.add(new AttributeModifier("title", true,new ResourceModel("link.title.removefriend")));
				item.add(removeConnectionLink);
				
				//can only delete if own connections
				if(!ownList) {
					removeConnectionLink.setEnabled(false);
					removeConnectionLink.setVisible(false);
				}
				
				item.setOutputMarkupId(true);
		    }
			
		};
		confirmedFriendsDataView.setOutputMarkupId(true);
		confirmedFriendsDataView.setItemsPerPage(ProfileConstants.MAX_CONNECTIONS_PER_PAGE);
		
		confirmedFriendsContainer.add(confirmedFriendsDataView);

		//add results container
		add(confirmedFriendsContainer);
		
		//add window
		add(connectionWindow);
		
		//add pager
		AjaxPagingNavigator pager = new AjaxPagingNavigator("navigator", confirmedFriendsDataView);
		add(pager);

		//initially, if no friends, hide container and pager
		if(numConfirmedFriends == 0) {
			confirmedFriendsContainer.setVisible(false);
			pager.setVisible(false);
		}
		
		//also, if num less than num required for pager, hide it
		if(numConfirmedFriends <= ProfileConstants.MAX_CONNECTIONS_PER_PAGE) {
			pager.setVisible(false);
		}
		
	}
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("ConfirmedFriends has been deserialized.");
		//re-init our transient objects
		
	}
	
	
}
