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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.ProfileMessagingLogic;
import org.sakaiproject.profile2.logic.ProfilePreferencesLogic;
import org.sakaiproject.profile2.logic.ProfilePrivacyLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.model.MessageParticipant;
import org.sakaiproject.profile2.model.MessageThread;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.tool.components.ProfileImage;
import org.sakaiproject.profile2.tool.dataproviders.MessageThreadsDataProvider;
import org.sakaiproject.profile2.tool.pages.MyMessages;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

@Slf4j
public class MessageThreadsView extends Panel {
	
	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePreferencesLogic")
	protected ProfilePreferencesLogic preferencesLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileMessagingLogic")
	protected ProfileMessagingLogic messagingLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	protected ProfilePrivacyLogic privacyLogic;
	
	public MessageThreadsView(final String id) {
		super(id);
		
		log.debug("MessageThreads()");
		
		//get current user
		final String currentUserUuid = sakaiProxy.getCurrentUserId();
		
		//heading
		/*
		Label heading = new Label("messageThreadListHeading", new ResourceModel("heading.messages"));
		add(heading);
		*/
		
		//no messages label
		Label noMessagesLabel = new Label("noMessagesLabel");
		noMessagesLabel.setOutputMarkupPlaceholderTag(true);
		add(noMessagesLabel);
		
		//container which wraps list
		final WebMarkupContainer messageThreadListContainer = new WebMarkupContainer("messageThreadListContainer");
		messageThreadListContainer.setOutputMarkupId(true);
		
		//get our list of messages
		final MessageThreadsDataProvider provider = new MessageThreadsDataProvider(currentUserUuid);
		int numMessages = (int) provider.size();
		
		//message list
		DataView<MessageThread> messageThreadList = new DataView<MessageThread>("messageThreadList", provider) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item<MessageThread> item) {
		        
				final MessageThread thread = (MessageThread)item.getDefaultModelObject();
				Message message = thread.getMostRecentMessage();
				String messageFromUuid = message.getFrom();
				
				//we need to know if this message has been read or not so we can style it accordingly
				//we only need this if we didn't send the message
				MessageParticipant participant = null;
				
				boolean messageOwner = false;
				if(StringUtils.equals(messageFromUuid, currentUserUuid)) {
					messageOwner = true;
				}
				if(!messageOwner) {
					participant = messagingLogic.getMessageParticipant(message.getId(), currentUserUuid);
				}
				
				//prefs and privacy
				ProfilePreferences prefs = preferencesLogic.getPreferencesRecordForUser(messageFromUuid);
				ProfilePrivacy privacy = privacyLogic.getPrivacyRecordForUser(messageFromUuid);
				
				//photo link
				AjaxLink<String> photoLink = new AjaxLink<String>("photoLink", new Model<String>(messageFromUuid)) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						setResponsePage(new ViewProfile(getModelObject()));
					}
					
				};
				
				//photo
				ProfileImage messagePhoto = new ProfileImage("messagePhoto", new Model<String>(messageFromUuid));
				messagePhoto.setSize(ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
				photoLink.add(messagePhoto);
				item.add(photoLink);
				
				//name link
				AjaxLink<String> messageFromLink = new AjaxLink<String>("messageFromLink", new Model<String>(messageFromUuid)) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						setResponsePage(new ViewProfile(getModelObject()));
					}
					
				};
				messageFromLink.add(new Label("messageFromName", new Model<String>(sakaiProxy.getUserDisplayName(messageFromUuid))));
				item.add(messageFromLink);
			
				//date
				item.add(new Label("messageDate", ProfileUtils.convertDateToString(message.getDatePosted(), ProfileConstants.MESSAGE_DISPLAY_DATE_FORMAT)));
				
				//subject link
				AjaxLink<MessageThread> messageSubjectLink = new AjaxLink<MessageThread>("messageSubjectLink", new Model<MessageThread>(thread)) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						//load messageview panel
						//setResponsePage(new MyMessageView(id, currentUserUuid, getModelObject().getId(), getModelObject().getSubject()));
						
						//load MyMessages with some params that will then load a diff tab panel and show this message panel.
						setResponsePage(new MyMessages(thread.getId()));
						
						
					}
					
				};
				messageSubjectLink.add(new Label("messageSubject", new Model<String>(thread.getSubject())));
				item.add(messageSubjectLink);
				
				//message body
				item.add(new Label("messageBody", new Model<String>(StringUtils.abbreviate(message.getMessage(), ProfileConstants.MESSAGE_PREVIEW_MAX_LENGTH))));
				
				//unread notice for accessibility, off unless its new.
				Label messageUnreadNotice = new Label("messageUnreadNotice", new ResourceModel("accessibility.messages.unread"));
				messageUnreadNotice.setVisible(false);
				item.add(messageUnreadNotice);
				
				//highlight if new, also render accessibility notice
				if(!messageOwner && !participant.isRead()) {
					item.add(new AttributeAppender("class", true, new Model<String>("unread-message"), " "));
					messageUnreadNotice.setVisible(true);
				}
				
				
				
				
				
				item.setOutputMarkupId(true);
		    }
			
		};
		messageThreadList.setOutputMarkupId(true);
		messageThreadList.setItemsPerPage(ProfileConstants.MAX_MESSAGES_PER_PAGE);
		messageThreadListContainer.add(messageThreadList);
		add(messageThreadListContainer);
		
		//pager
		AjaxPagingNavigator pager = new AjaxPagingNavigator("navigator", messageThreadList);
		add(pager);
	
		//initially, if no message threads to show, hide container and pager, set and show label
		if(numMessages == 0) {
			messageThreadListContainer.setVisible(false);
			pager.setVisible(false);
			
			noMessagesLabel.setDefaultModel(new ResourceModel("text.messages.none"));
			noMessagesLabel.setVisible(true);
		}
		
		//also, if num less than num required for pager, hide it
		if(numMessages <= ProfileConstants.MAX_MESSAGES_PER_PAGE) {
			pager.setVisible(false);
		}
	}
}
