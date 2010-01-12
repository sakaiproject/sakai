package org.sakaiproject.profile2.tool.pages;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.model.MessageThread;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.dataproviders.MessageThreadsDataProvider;
import org.sakaiproject.profile2.tool.pages.panels.ConfirmedFriends;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

public class MyMessageThreads extends BasePage {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ConfirmedFriends.class);
    
	public MyMessageThreads() {
		
		log.debug("MessageThreads()");
		
		//get API's
		sakaiProxy = getSakaiProxy();
		profileLogic = getProfileLogic();
		
		//get current user
		final String userUuid = sakaiProxy.getCurrentUserId();
		
		//new message panel
		/*
		final ComposeNewMessage newMessagePanel = new ComposeNewMessage("newMessagePanel");
		newMessagePanel.setOutputMarkupPlaceholderTag(true);
		newMessagePanel.setVisible(false);
		add(newMessagePanel);
		*/
		add(new EmptyPanel("newMessagePanel"));
		
		
		//new message button
		Form form = new Form("form");
		IndicatingAjaxButton newMessageButton = new IndicatingAjaxButton("newMessage", form) {
		
			public void onSubmit(AjaxRequestTarget target, Form form) {
				//show panel
				//newMessagePanel.setVisible(true);
				//target.addComponent(newMessagePanel);
				
				//disable this button
				this.setEnabled(false);
				target.addComponent(this);
				
				//resize iframe
				target.prependJavascript("setMainFrameHeight(window.name);");
			}
		};
		newMessageButton.setModel(new ResourceModel("button.message.new"));
		newMessageButton.setDefaultFormProcessing(false);
		form.add(newMessageButton);
		add(form);
		
		
		
		//container which wraps list
		final WebMarkupContainer messageThreadListContainer = new WebMarkupContainer("messageThreadListContainer");
		messageThreadListContainer.setOutputMarkupId(true);
		
		//get our list of messages
		final MessageThreadsDataProvider provider = new MessageThreadsDataProvider(userUuid);
		int numMessages = provider.size();
		
		//message list
		DataView<MessageThread> messageThreadList = new DataView<MessageThread>("messageThreadList", provider) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item<MessageThread> item) {
		        
				final MessageThread thread = (MessageThread)item.getDefaultModelObject();
				
				final Message message = thread.getMostRecentMessage();
				final String messageFromUuid = message.getFrom();
				
				//friend?
				boolean friend = profileLogic.isUserXFriendOfUserY(messageFromUuid, userUuid);
				
				//photo link
				AjaxLink<Void> photoLink = new AjaxLink<Void>("photoLink") {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						setResponsePage(new ViewProfile(messageFromUuid));
					}
					
				};
				
				//photo
				photoLink.add(new ProfileImageRenderer("messagePhoto", messageFromUuid, profileLogic.isUserXProfileImageVisibleByUserY(messageFromUuid, userUuid, friend), ProfileConstants.PROFILE_IMAGE_THUMBNAIL, false));
				item.add(photoLink);
				
				
				//name link
				AjaxLink<Void> messageFromLink = new AjaxLink<Void>("messageFromLink") {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						setResponsePage(new ViewProfile(messageFromUuid));
					}
					
				};
				messageFromLink.add(new Label("messageFromName", new Model<String>(sakaiProxy.getUserDisplayName(messageFromUuid))));
				item.add(messageFromLink);
			
				//date
				item.add(new Label("messageDate", ProfileUtils.convertDateToString(message.getDatePosted(), ProfileConstants.MESSAGE_DISPLAY_DATE_FORMAT)));
				
				//subject link
				AjaxLink<Void> messageSubjectLink = new AjaxLink<Void>("messageSubjectLink") {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						//load messageview panel
						setResponsePage(new MyMessageView(userUuid, message.getThread(), thread.getSubject()));
					}
					
				};
				messageSubjectLink.add(new Label("messageSubject", new Model<String>(thread.getSubject())));
				item.add(messageSubjectLink);
				
				//message body
				item.add(new Label("messageBody", new Model<String>(StringUtils.abbreviate(message.getMessage(), ProfileConstants.MESSAGE_PREVIEW_MAX_LENGTH))));
				
				//highlight if new
				/*
				if(!message.isRead()) {
					item.add(new AttributeAppender("class", true, new Model<String>("unread-message"), " "));
				}
				*/
				
				
				
				
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
	
		//initially, if no message threads to show, hide container and pager
		if(numMessages == 0) {
			messageThreadListContainer.setVisible(false);
			pager.setVisible(false);
		}
		
		//also, if num less than num required for pager, hide it
		if(numMessages <= ProfileConstants.MAX_MESSAGES_PER_PAGE) {
			pager.setVisible(false);
		}
		
		
	}
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MessageList has been deserialized.");
		//re-init our transient objects
		profileLogic = getProfileLogic();
		sakaiProxy = getSakaiProxy();
	}
	
}
