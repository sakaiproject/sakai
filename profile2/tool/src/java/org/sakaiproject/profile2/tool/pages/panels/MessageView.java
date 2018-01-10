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
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
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
import org.sakaiproject.profile2.tool.dataproviders.MessagesDataProvider;
import org.sakaiproject.profile2.tool.models.StringModel;
import org.sakaiproject.profile2.tool.pages.MyMessages;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

@Slf4j
public class MessageView extends Panel {

	private static final long serialVersionUID = 1L;

	private DataView<Message> messageList = null;
	private WebMarkupContainer messageListContainer = null;
	private boolean lastUnreadSet = false;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePreferencesLogic")
	protected ProfilePreferencesLogic preferencesLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileMessagingLogic")
	protected ProfileMessagingLogic messagingLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	protected ProfilePrivacyLogic privacyLogic;
	
	/**
	 * Constructor for an incoming link with a threadId as part of the PageParameters
	 * @param parameters
	 */
	public MessageView(final String id, PageParameters parameters) {
		super(id);
		log.debug("MyMessageView(" + parameters.toString() +")");

		MessageThread thread = messagingLogic.getMessageThread(parameters.get("thread").toString());
		
		//check user is a thread participant
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(!messagingLogic.isThreadParticipant(thread.getId(), currentUserUuid)) {
			//this would only ever happen if the user has access to the other user's workspace because the link is a direct link to their site
			//so they won't even reach this part if they don't have access - so it would need to be a very special case.
			log.error("MyMessageView: user " + currentUserUuid + " attempted to access restricted thread: " + thread.getId());
			throw new RestartResponseException(new MyMessages());
		}
		
		renderMyMessagesView(sakaiProxy.getCurrentUserId(), thread.getId(), thread.getSubject());
	}
	
	/**
	 * Constructor for normal viewing
	 * @param currentUserUuid
	 * @param threadId
	 */
	public MessageView(final String id, final String currentUserUuid, final String threadId) {
		super(id);
		log.debug("MyMessageView(" + currentUserUuid + ", " + threadId + ")");
		
		//get subject
		String threadSubject = messagingLogic.getThreadSubject(threadId);
		
		renderMyMessagesView(currentUserUuid, threadId, threadSubject);
	}
	
	/**
	 * Constructor for normal viewing
	 * @param currentUserUuid
	 * @param threadId
	 */
	public MessageView(final String id, final String currentUserUuid, final String threadId, final String threadSubject) {
		super(id);
		log.debug("MyMessageView(" + currentUserUuid + ", " + threadId + ", " + threadSubject + ")");
		
		renderMyMessagesView(currentUserUuid, threadId, threadSubject);
	}
	
	
	/**
	 * Does the actual rendering of the page
	 * @param currentUserUuid
	 * @param threadId
	 * @param threadSubject
	 */
	private void renderMyMessagesView(final String currentUserUuid, final String threadId, final String threadSubject) {
				
		//details container
		WebMarkupContainer messageDetailsContainer = new WebMarkupContainer("messageDetailsContainer");
		messageDetailsContainer.setOutputMarkupId(true);
		
		//thread subject
		Label threadSubjectLabel = new Label("threadSubject", new Model<String>(threadSubject));
		messageDetailsContainer.add(threadSubjectLabel);
		
		add(messageDetailsContainer);
		
		//list container
		messageListContainer = new WebMarkupContainer("messageListContainer");
		messageListContainer.setOutputMarkupId(true);
		
		//get our list of messages
		final MessagesDataProvider provider = new MessagesDataProvider(threadId);
		
		
		messageList = new DataView<Message>("messageList", provider) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item<Message> item) {
		        
				final Message message = (Message)item.getDefaultModelObject();
				final String messageFromUuid = message.getFrom();
				
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
				/* disabled for now
				ProfileImage messagePhoto = new ProfileImage("messagePhoto", new Model<String>(messageFromUuid));
				messagePhoto.setSize(ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
				photoLink.add(messagePhoto);
				*/
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
				
				//message body
				item.add(new Label("messageBody", new Model<String>(message.getMessage())));
				
				//highlight if new, then mark it as read
				if(!messageOwner && !participant.isRead()) {
					item.add(new AttributeAppender("class", true, new Model<String>("unread-message"), " "));
					messagingLogic.toggleMessageRead(participant, true);
					
					//set param for first unread message in the thread
					if(!lastUnreadSet) {
						lastUnreadSet=true;
						item.add(new AttributeModifier("rel", true, new Model<String>("lastUnread")));
					}
					
				}
				
				item.setOutputMarkupId(true);
				
		    }
			
		};
		messageList.setOutputMarkupId(true);
		messageListContainer.add(messageList);
		add(messageListContainer);
		
		
		//reply form
		StringModel stringModel = new StringModel();
		Form<StringModel> replyForm = new Form<StringModel>("replyForm", new Model<StringModel>(stringModel));
		
		//form feedback
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		add(formFeedback);
		
		//reply field
		replyForm.add(new Label("replyLabel", new ResourceModel("message.reply")));
		final TextArea<StringModel> replyField = new TextArea<StringModel>("replyField", new PropertyModel<StringModel>(stringModel, "string"));
		replyField.setRequired(true);
		replyField.setOutputMarkupId(true);
		replyForm.add(replyField);
		
		//reply button
		IndicatingAjaxButton replyButton = new IndicatingAjaxButton("replyButton", replyForm) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
        		StringModel stringModel = (StringModel) form.getModelObject();
        		String reply = stringModel.getString();
				
        		//create a direct link to view this message thread
		        //String messageLink = sakaiProxy.getDirectUrlToUserProfile(newMessage.getTo(), urlFor(MyMessageView.class, new PageParameters("thread=" + threadId)).toString());
        		
        		//send it, get Message back so we can add it to the list
        		Message message = messagingLogic.replyToThread(threadId, reply, currentUserUuid);
        		if(message != null) {
        			//clear this field
        			replyField.setModelObject(null);
        			target.add(replyField);
        			
        			//create new item and add it to the list
        			//do we need to register this with the listview?
        			Component item = buildItem(message);
        			target.prependJavaScript(String.format(
                                    "var item=document.createElement('%s');item.id='%s';Wicket.$('%s').appendChild(item);",
                                    "tr", item.getMarkupId(), messageListContainer.getMarkupId()));
        			target.add(item);

        			//repaint the list of messages in this thread
        			//target.addComponent(messageListContainer);
        			
        			//resize
    				target.appendJavaScript("setMainFrameHeight(window.name);");
        		}
        		
            }
			
			protected void onError(AjaxRequestTarget target, Form form) {
				
				//validate
				if(!replyField.isValid()) {
					formFeedback.setDefaultModel(new ResourceModel("error.message.required.body"));
				}
				formFeedback.add(new AttributeModifier("class", true, new Model<String>("alertMessage")));	
				target.add(formFeedback);
			}
		};
		replyForm.add(replyButton);
		replyButton.setModel(new ResourceModel("button.message.send"));
		
		add(replyForm);

		
		
		
	}
	
	//build a single item in the list so we can add it to the end
	private Component buildItem(Message message){
        WebMarkupContainer item = new WebMarkupContainer(messageList.newChildId());
        item.setOutputMarkupId(true);
        messageList.add(item);
       
        ProfilePreferences prefs = preferencesLogic.getPreferencesRecordForUser(message.getFrom());
        
        //photo and link
		item.add(new AjaxLink<String>("photoLink", new Model<String>(message.getFrom())) {
			private static final long serialVersionUID = 1L;
			public void onClick(AjaxRequestTarget target) {
				setResponsePage(new ViewProfile(getModelObject()));
			}
			
		});
		
		/* disabled for now
		//image
		ProfileImage messagePhoto = new ProfileImage("messagePhoto", new Model<String>(message.getFrom()));
		messagePhoto.setSize(ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
		item.add(messagePhoto);
		*/
		
		//name link
		item.add(new AjaxLink<String>("messageFromLink", new Model<String>(message.getFrom())) {
			private static final long serialVersionUID = 1L;
			public void onClick(AjaxRequestTarget target) {
				setResponsePage(new ViewProfile(getModelObject()));
			}
		}.add(new Label("messageFromName", new Model<String>(sakaiProxy.getUserDisplayName(message.getFrom())))));
	
		//date
		item.add(new Label("messageDate", ProfileUtils.convertDateToString(message.getDatePosted(), ProfileConstants.MESSAGE_DISPLAY_DATE_FORMAT)));
		
		//message body
		item.add(new Label("messageBody", new Model<String>(message.getMessage())));
		
        return item;
    }
	
}
