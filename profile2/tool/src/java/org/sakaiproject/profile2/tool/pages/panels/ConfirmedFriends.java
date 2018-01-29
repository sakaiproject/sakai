/**
 * Copyright (c) 2008-2012 The Sakai Foundation
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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfilePrivacyLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.tool.components.ProfileImage;
import org.sakaiproject.profile2.tool.components.ProfileStatusRenderer;
import org.sakaiproject.profile2.tool.dataproviders.ConfirmedFriendsDataProvider;
import org.sakaiproject.profile2.tool.models.FriendAction;
import org.sakaiproject.profile2.tool.pages.MySearch;
import org.sakaiproject.profile2.tool.pages.ViewFriends;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.tool.pages.windows.RemoveFriend;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

@Slf4j
public class ConfirmedFriends extends Panel {
	
	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	protected ProfileConnectionsLogic connectionsLogic;
    
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	protected ProfilePrivacyLogic privacyLogic;
	
	private Integer numConfirmedFriends = 0;
	private boolean ownList = false;
	
	
	public ConfirmedFriends(final String id, final String userUuid) {
		super(id);
		
		log.debug("ConfirmedFriends()");
		
		//setup model to store the actions in the modal windows
		final FriendAction friendActionModel = new FriendAction();
		
		//get info for user viewing this page (will be the same if user is viewing own list, different if viewing someone else's)
		final String currentUserUuid = sakaiProxy.getCurrentUserId();
		//User currentUser = sakaiProxy.getUserQuietly(currentUserUuid);
		//final String currentUserType = currentUser.getType(); //to be used for checking if connection between users is allowed, when this is added
		
		//if viewing own friends, you can manage them.
		if(userUuid.equals(currentUserUuid)) {
			ownList = true;
		}
		
		//get our list of confirmed friends as an IDataProvider
		ConfirmedFriendsDataProvider provider = new ConfirmedFriendsDataProvider(userUuid);
		
		//init number of friends
		numConfirmedFriends = (int) provider.size();
		
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
		
		// actions
		Form<Void> confirmedFriendsButtonForm = new Form<Void>("confirmedFriendsButtonForm");
		add(confirmedFriendsButtonForm);
		
		//create worksite panel
		final CreateWorksitePanel createWorksitePanel = 
			new CreateWorksitePanel("createWorksitePanel", connectionsLogic.getConnectionsForUser(userUuid));
		//create placeholder and set invisible initially
		createWorksitePanel.setOutputMarkupPlaceholderTag(true);
		createWorksitePanel.setVisible(false);
				
		confirmedFriendsButtonForm.add(createWorksitePanel);
		
		final AjaxButton createWorksiteButton = new AjaxButton("createWorksiteButton", confirmedFriendsButtonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				createWorksitePanel.setVisible(true);
				target.add(createWorksitePanel);
				target.appendJavaScript("fixWindowVertical();");
			}

		};
		createWorksiteButton.setModel(new ResourceModel("link.worksite.create"));
		createWorksiteButton.add(new AttributeModifier("title", true, new ResourceModel("link.title.worksite.create")));
		createWorksiteButton.setVisible(sakaiProxy.isUserAllowedAddSite(userUuid));
		confirmedFriendsButtonForm.add(createWorksiteButton);
		
		//search for connections
		AjaxButton searchConnectionsButton = new AjaxButton("searchConnectionsButton", confirmedFriendsButtonForm) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				setResponsePage(new MySearch());
			}
    	};
		searchConnectionsButton.setModel(new ResourceModel("link.my.friends.search"));    	
		confirmedFriendsButtonForm.add(searchConnectionsButton);
		
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
				final String personUuid = person.getUuid();
		    			    	
		    	//setup values
		    	String displayName = person.getDisplayName();
		    	boolean friend;
		    	
		    	//get friend status
		    	if(ownList) {
		    		friend = true; //viewing own page of conenctions, must be friend!
		    	} else {
		    		friend = connectionsLogic.isUserXFriendOfUserY(userUuid, personUuid); //other person viewing, check if they are friends
		    	}
		    	
				//get other objects
				ProfilePrivacy privacy = person.getPrivacy();
				ProfilePreferences prefs = person.getPreferences();
				
				//image wrapper, links to profile
		    	Link<String> friendItem = new Link<String>("connectionPhotoWrap", new Model<String>(personUuid)) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new ViewProfile(getModelObject()));
					}
				};
				
				//image				
				ProfileImage connectionPhoto = new ProfileImage("connectionPhoto", new Model<String>(personUuid));
				connectionPhoto.setSize(ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
				friendItem.add(connectionPhoto);

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
		    	
				WebMarkupContainer c1 = new WebMarkupContainer("removeConnectionContainer");
				c1.setOutputMarkupId(true);
				
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
				            		target.appendJavaScript("$('#" + item.getMarkupId() + "').slideUp();");
				            		
				            		//update label
				            		target.add(confirmedFriendsHeading);
				            		
				            		//if none left, hide whole thing
				            		if(numConfirmedFriends==0) {
				            			target.appendJavaScript("$('#" + confirmedFriendsContainer.getMarkupId() + "').fadeOut();");
				            		}
				            	}
							}
				        });	
						
						connectionWindow.show(target);
						target.appendJavaScript("fixWindowVertical();"); 
					}
				};
				//ContextImage removeConnectionIcon = new ContextImage("removeConnectionIcon",new Model<String>(ProfileConstants.DELETE_IMG));
				removeConnectionLink.add(new AttributeModifier("alt", true, new StringResourceModel("accessibility.connection.remove", null, new Object[]{ displayName } )));
				//removeConnectionLink.add(removeConnectionIcon);
				removeConnectionLink.add(new AttributeModifier("title", true,new ResourceModel("link.title.removefriend")));
				removeConnectionLink.add(new Label("removeConnectionLabel", new ResourceModel("button.friend.remove")).setOutputMarkupId(true));
				c1.add(removeConnectionLink);
				item.add(c1);
				
				//can only delete if own connections
				if(!ownList) {
					removeConnectionLink.setEnabled(false);
					removeConnectionLink.setVisible(false);
				}
				
				WebMarkupContainer c2 = new WebMarkupContainer("viewFriendsContainer");
		    	c2.setOutputMarkupId(true);
		    	
		    	final AjaxLink<String> viewFriendsLink = new AjaxLink<String>("viewFriendsLink") {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						// always ViewFriends because a user isn't connected to himself
						setResponsePage(new ViewFriends(personUuid));
					}
				};
				final Label viewFriendsLabel = new Label("viewFriendsLabel", new ResourceModel("link.view.friends"));
				viewFriendsLink.add(viewFriendsLabel);
				
				//hide if not allowed
				if(!privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_MYFRIENDS)) {
					viewFriendsLink.setEnabled(false);
					c2.setVisible(false);
				}
				viewFriendsLink.setOutputMarkupId(true);
				c2.add(viewFriendsLink);
				item.add(c2);
				
				WebMarkupContainer c3 = new WebMarkupContainer("emailContainer");
		    	c3.setOutputMarkupId(true);
		    	
		    	ExternalLink emailLink = new ExternalLink("emailLink",
						"mailto:" + person.getProfile().getEmail(),
						new ResourceModel("profile.email").getObject());
		    	
				c3.add(emailLink);
				
				if (StringUtils.isBlank(person.getProfile().getEmail()) || 
						false == privacyLogic.isActionAllowed(
								person.getUuid(), currentUserUuid, PrivacyType.PRIVACY_OPTION_CONTACTINFO)) {
					
					c3.setVisible(false);
				}
				item.add(c3);
				
				WebMarkupContainer c4 = new WebMarkupContainer("websiteContainer");
		    	c4.setOutputMarkupId(true);
		    	
		    	// TODO home page, university profile URL or academic/research URL (see PRFL-35)
		    	ExternalLink websiteLink = new ExternalLink("websiteLink", person.getProfile()
						.getHomepage(), new ResourceModel(
						"profile.homepage").getObject()).setPopupSettings(new PopupSettings());
		    	
		    	c4.add(websiteLink);
		    	
				if (StringUtils.isBlank(person.getProfile().getHomepage()) || 
						false == privacyLogic.isActionAllowed(
								person.getUuid(), currentUserUuid, PrivacyType.PRIVACY_OPTION_CONTACTINFO)) {
					
					c4.setVisible(false);
				}
				item.add(c4);
				
				// basic info can be set to 'only me' so still need to check
				if (true == privacyLogic.isActionAllowed(
						person.getUuid(), currentUserUuid, PrivacyType.PRIVACY_OPTION_BASICINFO)) {
					
					item.add(new Label("connectionSummary",
							StringUtils.abbreviate(ProfileUtils.stripHtml(
									person.getProfile().getPersonalSummary()), 200)));
				} else {
					item.add(new Label("connectionSummary", ""));
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
	
}
