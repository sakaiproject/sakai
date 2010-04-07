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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.components.ProfileStatusRenderer;
import org.sakaiproject.profile2.tool.dataproviders.RequestedFriendsDataProvider;
import org.sakaiproject.profile2.tool.models.FriendAction;
import org.sakaiproject.profile2.tool.pages.MyFriends;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.tool.pages.windows.ConfirmFriend;
import org.sakaiproject.profile2.tool.pages.windows.IgnoreFriend;
import org.sakaiproject.profile2.util.ProfileConstants;

public class RequestedFriends extends Panel {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(RequestedFriends.class);
	private Integer numRequestedFriends = 0;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;
	
	public RequestedFriends(final String id, final String userUuid) {
		super(id);
		
		log.debug("RequestedFriends()");
		
		//setup model to store the actions in the modal windows
		final FriendAction friendActionModel = new FriendAction();
	
		//get our list of friend requests as an IDataProvider
		RequestedFriendsDataProvider provider = new RequestedFriendsDataProvider(userUuid);
		
		//init number of requests
		numRequestedFriends = provider.size();
		
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
		DataView<Person> requestedFriendsDataView = new DataView<Person>("results-list", provider) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item<Person> item) {
		        
				Person person = (Person)item.getDefaultModelObject();
				String personUuid = person.getUuid();
		    			    	
		    	//get name
		    	String displayName = person.getDisplayName();
		    	
		    	//get privacy record for the friend
		    	ProfilePrivacy privacy = profileLogic.getPrivacyRecordForUser(personUuid);
		    	
		    	//is profile image allowed to be viewed by this user/friend?
				final boolean isProfileImageAllowed = profileLogic.isUserXProfileImageVisibleByUserY(personUuid, privacy, userUuid, false);
				
				//image wrapper, links to profile
		    	Link<String> friendItem = new Link<String>("friendPhotoWrap", new Model<String>(personUuid)) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new ViewProfile(getModelObject()));
					}
				};
				
				//image
				friendItem.add(new ProfileImageRenderer("result-photo", personUuid, isProfileImageAllowed, ProfileConstants.PROFILE_IMAGE_THUMBNAIL, true));
				item.add(friendItem);
		    			    	
		    	//name and link to profile
		    	Link<String> profileLink = new Link<String>("result-profileLink", new Model<String>(personUuid)) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new ViewProfile(getModelObject()));
					}
				};
				profileLink.add(new Label("result-name", displayName));
		    	item.add(profileLink);
		    	
		    	//status component
				ProfileStatusRenderer status = new ProfileStatusRenderer("result-status", personUuid, privacy, userUuid, false, "friendsListInfoStatusMessage", "friendsListInfoStatusDate");
				status.setOutputMarkupId(true);
				item.add(status);
				
				//IGNORE FRIEND LINK AND WINDOW
		    	final AjaxLink<String> ignoreFriendLink = new AjaxLink<String>("ignoreFriendLink", new Model<String>(personUuid)) {
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
						
						connectionWindow.show(target);
						target.appendJavascript("fixWindowVertical();"); 
					}
				};
				ContextImage ignoreFriendIcon = new ContextImage("ignoreFriendIcon",new Model<String>(ProfileConstants.CANCEL_IMG));
				ignoreFriendLink.add(ignoreFriendIcon);
				ignoreFriendLink.add(new AttributeModifier("title", true,new ResourceModel("link.title.ignorefriend")));
				item.add(ignoreFriendLink);
				
				//CONFIRM FRIEND LINK AND WINDOW
		    	final AjaxLink<String> confirmFriendLink = new AjaxLink<String>("confirmFriendLink", new Model<String>(personUuid)) {
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
				            		target.appendJavascript("$('#" + item.getMarkupId() + "').slideUp();");
				            		
				            		//update label
				            		target.addComponent(requestedFriendsHeading);
				            		
				            		//get parent panel and repaint ConfirmedFriends panel via helper method in MyFriends 
				            		findParent(MyFriends.class).updateConfirmedFriends(target, userUuid);
				            		
				            		//if none left, hide everything
				            		if(numRequestedFriends==0) {
				            			target.appendJavascript("$('#" + requestedFriendsHeading.getMarkupId() + "').fadeOut();");
				            			target.appendJavascript("$('#" + requestedFriendsContainer.getMarkupId() + "').fadeOut();");
				            		}
				            	}
							}
				        });	
						
						connectionWindow.show(target);
						target.appendJavascript("fixWindowVertical();"); 
					}
				};
				ContextImage confirmFriendIcon = new ContextImage("confirmFriendIcon",new Model<String>(ProfileConstants.ACCEPT_IMG));
				confirmFriendLink.add(confirmFriendIcon);
				confirmFriendLink.add(new AttributeModifier("title", true,new ResourceModel("link.title.confirmfriend")));
				item.add(confirmFriendLink);

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
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("RequestedFriends has been deserialized");
		//re-init our transient objects
		
	}
	
	
}
