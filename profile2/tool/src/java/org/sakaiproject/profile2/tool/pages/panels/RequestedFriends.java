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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
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

import org.sakaiproject.profile2.logic.ProfilePrivacyLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.tool.components.ProfileImage;
import org.sakaiproject.profile2.tool.components.ProfileStatusRenderer;
import org.sakaiproject.profile2.tool.dataproviders.RequestedFriendsDataProvider;
import org.sakaiproject.profile2.tool.models.FriendAction;
import org.sakaiproject.profile2.tool.pages.MyFriends;
import org.sakaiproject.profile2.tool.pages.ViewFriends;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.tool.pages.windows.ConfirmFriend;
import org.sakaiproject.profile2.tool.pages.windows.IgnoreFriend;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

@Slf4j
public class RequestedFriends extends Panel {
	
	private static final long serialVersionUID = 1L;
	private Integer numRequestedFriends = 0;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
		
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	protected ProfilePrivacyLogic privacyLogic;
	
	public RequestedFriends(final String id, final String userUuid) {
		super(id);
		
		log.debug("RequestedFriends()");
		
		final String currentUserUuid = sakaiProxy.getCurrentUserId();
		
		//setup model to store the actions in the modal windows
		final FriendAction friendActionModel = new FriendAction();
	
		//get our list of friend requests as an IDataProvider
		RequestedFriendsDataProvider provider = new RequestedFriendsDataProvider(userUuid);
		
		//init number of requests
		numRequestedFriends = (int) provider.size();
		
		//model so we can update the number of requests
		IModel<Integer> numRequestedFriendsModel = new Model<Integer>() {
			private static final long serialVersionUID = 1L;

			public Integer getObject() {
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
		
		//connection window
		final ModalWindow connectionWindow = new ModalWindow("connectionWindow");
		
		//search results
		DataView<Person> requestedFriendsDataView = new DataView<Person>("connections", provider) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item<Person> item) {
		        
				Person person = (Person)item.getDefaultModelObject();
				final String personUuid = person.getUuid();
		    			    	
		    	//get name
		    	String displayName = person.getDisplayName();
		    	
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
				
				//CONFIRM FRIEND LINK AND WINDOW
				
				WebMarkupContainer c1 = new WebMarkupContainer("confirmConnectionContainer");
				c1.setOutputMarkupId(true);
				
		    	final AjaxLink<String> confirmConnectionLink = new AjaxLink<String>("confirmConnectionLink", new Model<String>(personUuid)) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						
						//get this item, and set content for modalwindow
				    	String personUuid = getModelObject();
						connectionWindow.setContent(new ConfirmFriend(connectionWindow.getContentId(), connectionWindow, friendActionModel, userUuid, personUuid)); 

						//modalwindow handler 
						connectionWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
							private static final long serialVersionUID = 1L;
							public void onClose(AjaxRequestTarget target){
								if(friendActionModel.isConfirmed()) { 
				            		
				            		//decrement number of requests
				            		numRequestedFriends--;
				            		
				            		//remove friend item from display
				            		target.appendJavaScript("$('#" + item.getMarkupId() + "').slideUp();");
				            		
				            		//update label
				            		target.add(requestedFriendsHeading);
				            		
				            		//get parent panel and repaint ConfirmedFriends panel via helper method in MyFriends 
				            		findParent(MyFriends.class).updateConfirmedFriends(target, userUuid);
				            		
				            		//if none left, hide everything
				            		if(numRequestedFriends==0) {
				            			target.appendJavaScript("$('#" + requestedFriendsHeading.getMarkupId() + "').fadeOut();");
				            			target.appendJavaScript("$('#" + requestedFriendsContainer.getMarkupId() + "').fadeOut();");
				            		}
				            	}
							}
				        });	
						
						connectionWindow.show(target);
						target.appendJavaScript("fixWindowVertical();"); 
					}
				};
				//ContextImage confirmConnectionIcon = new ContextImage("confirmConnectionIcon",new Model<String>(ProfileConstants.ACCEPT_IMG));
				//confirmConnectionLink.add(confirmConnectionIcon);
				confirmConnectionLink.add(new AttributeModifier("title", true,new ResourceModel("link.title.confirmfriend")));
				confirmConnectionLink.add(new AttributeModifier("alt", true, new StringResourceModel("accessibility.connection.confirm", null, new Object[]{ displayName } )));
				confirmConnectionLink.add(new Label("confirmConnectionLabel", new ResourceModel("link.friend.confirm")).setOutputMarkupId(true));
				c1.add(confirmConnectionLink);
				item.add(c1);
				
				//IGNORE FRIEND LINK AND WINDOW
				
				WebMarkupContainer c2 = new WebMarkupContainer("ignoreConnectionContainer");
				c2.setOutputMarkupId(true);
				
		    	final AjaxLink<String> ignoreConnectionLink = new AjaxLink<String>("ignoreConnectionLink", new Model<String>(personUuid)) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						
						//get this item, and set content for modalwindow
				    	String personUuid = getModelObject();
						connectionWindow.setContent(new IgnoreFriend(connectionWindow.getContentId(), connectionWindow, friendActionModel, userUuid, personUuid)); 

						//modalwindow handler 
						connectionWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
							private static final long serialVersionUID = 1L;
							public void onClose(AjaxRequestTarget target){
								if(friendActionModel.isIgnored()) { 
				            		
				            		//decrement number of requests
				            		numRequestedFriends--;
				            		
				            		//remove friend item from display
				            		target.appendJavaScript("$('#" + item.getMarkupId() + "').slideUp();");
				            		
				            		//update label
				            		target.add(requestedFriendsHeading);
				            				            		
				            		//if none left, hide everything
				            		if(numRequestedFriends==0) {
				            			target.appendJavaScript("$('#" + requestedFriendsHeading.getMarkupId() + "').fadeOut();");
				            			target.appendJavaScript("$('#" + requestedFriendsContainer.getMarkupId() + "').fadeOut();");
				            		}
				            	}
							}
				        });	
						
						connectionWindow.show(target);
						target.appendJavaScript("fixWindowVertical();"); 
					}
				};
				//ContextImage ignoreConnectionIcon = new ContextImage("ignoreConnectionIcon",new Model<String>(ProfileConstants.CANCEL_IMG));
				//ignoreConnectionLink.add(ignoreConnectionIcon);
				ignoreConnectionLink.add(new AttributeModifier("title", true,new ResourceModel("link.title.ignorefriend")));
				ignoreConnectionLink.add(new AttributeModifier("alt", true, new StringResourceModel("accessibility.connection.ignore", null, new Object[]{ displayName } )));
				ignoreConnectionLink.add(new Label("ignoreConnectionLabel", new ResourceModel("link.friend.ignore")).setOutputMarkupId(true));
				c2.add(ignoreConnectionLink);
				item.add(c2);
				
				WebMarkupContainer c3 = new WebMarkupContainer("viewFriendsContainer");
		    	c3.setOutputMarkupId(true);
		    	
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
					c3.setVisible(false);
				}
				viewFriendsLink.setOutputMarkupId(true);
				c3.add(viewFriendsLink);
				item.add(c3);
				
				WebMarkupContainer c4 = new WebMarkupContainer("emailContainer");
		    	c4.setOutputMarkupId(true);
		    	
		    	ExternalLink emailLink = new ExternalLink("emailLink",
						"mailto:" + person.getProfile().getEmail(),
						new ResourceModel("profile.email").getObject());
		    	
				c4.add(emailLink);
				
				// friend=false
				if (StringUtils.isBlank(person.getProfile().getEmail()) || 
						false == privacyLogic.isActionAllowed(
								person.getUuid(), currentUserUuid, PrivacyType.PRIVACY_OPTION_CONTACTINFO)) {
					
					c4.setVisible(false);
				}
				item.add(c4);
				
				WebMarkupContainer c5 = new WebMarkupContainer("websiteContainer");
		    	c5.setOutputMarkupId(true);
		    	
		    	// TODO home page, university profile URL or academic/research URL (see PRFL-35)
		    	ExternalLink websiteLink = new ExternalLink("websiteLink", person.getProfile()
						.getHomepage(), new ResourceModel(
						"profile.homepage").getObject()).setPopupSettings(new PopupSettings());
		    	
		    	c5.add(websiteLink);

				// friend=false
				if (StringUtils.isBlank(person.getProfile().getHomepage()) || 
						false == privacyLogic.isActionAllowed(
								person.getUuid(), currentUserUuid, PrivacyType.PRIVACY_OPTION_CONTACTINFO)) {
					
					c5.setVisible(false);
				}
				item.add(c5);
				
				// not a friend yet, so friend=false
				if (true == privacyLogic.isActionAllowed(
						person.getUuid(), sakaiProxy.getCurrentUserId(), PrivacyType.PRIVACY_OPTION_BASICINFO)) {
					
					item.add(new Label("connectionSummary",
							StringUtils.abbreviate(ProfileUtils.stripHtml(
									person.getProfile().getPersonalSummary()), 200)));
				} else {
					item.add(new Label("connectionSummary", ""));
				}
				
				item.setOutputMarkupId(true);
		    }
			
		};
		requestedFriendsDataView.setOutputMarkupId(true);
		requestedFriendsContainer.add(requestedFriendsDataView);

		//add results container
		add(requestedFriendsContainer);
		
		//add window
		add(connectionWindow);
		
		//initially, if no requests, hide everything
		if(numRequestedFriends == 0) {
			this.setVisible(false);
		}
		
	}
	
}
