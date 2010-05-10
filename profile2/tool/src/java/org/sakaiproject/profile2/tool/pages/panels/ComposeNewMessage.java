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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfileMessagingLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.tool.components.ResourceReferences;
import org.sakaiproject.profile2.tool.models.NewMessageModel;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.wicketstuff.objectautocomplete.AutoCompletionChoicesProvider;
import org.wicketstuff.objectautocomplete.ObjectAutoCompleteBuilder;
import org.wicketstuff.objectautocomplete.ObjectAutoCompleteField;
import org.wicketstuff.objectautocomplete.ObjectAutoCompleteRenderer;

public class ComposeNewMessage extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ComposeNewMessage.class);
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileMessagingLogic")
	protected ProfileMessagingLogic messagingLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	protected ProfileConnectionsLogic connectionsLogic;
	
	public ComposeNewMessage(String id) {
		super(id);
		
		//this panel
		final Component thisPanel = this;
		
		//current user
		final String userId = sakaiProxy.getCurrentUserId();
		
		//setup model
		NewMessageModel newMessage = new NewMessageModel();
		newMessage.setFrom(userId);
		
		//feedback for form submit action
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		add(formFeedback);
		
		
		//setup form	
		final Form<NewMessageModel> form = new Form<NewMessageModel>("form", new Model<NewMessageModel>(newMessage));
		
		//close button
		WebMarkupContainer closeButton = new WebMarkupContainer("closeButton");
		closeButton.add(new AjaxFallbackLink<Void>("link") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				if(target != null) {
					target.prependJavascript("$('#" + thisPanel.getMarkupId() + "').slideUp();");
					target.appendJavascript("setMainFrameHeight(window.name);");
				}
			}
		}.add(new ContextImage("img",new Model<String>(ProfileConstants.CLOSE_IMAGE))));
		form.add(closeButton);
		
		
		//to label
		form.add(new Label("toLabel", new ResourceModel("message.to")));
		
		//get connections
		final List<Person> connections = connectionsLogic.getConnectionsForUser(userId);
		Collections.sort(connections);

		// list provider
		AutoCompletionChoicesProvider<Person> provider = new AutoCompletionChoicesProvider<Person>() {
			private static final long serialVersionUID = 1L;

			public Iterator<Person> getChoices(String input) {
            	return connectionsLogic.getConnectionsSubsetForSearch(connections, input).iterator();
            }
        };
        
        //renderer
        ObjectAutoCompleteRenderer<Person> renderer = new ObjectAutoCompleteRenderer<Person>(){
			private static final long serialVersionUID = 1L;
			
        	protected String getIdValue(Person p) {
            	return p.getUuid();
            }
        	protected String getTextValue(Person p) {
            	return p.getDisplayName();
            }
        };
        	
        
		//autocompletefield builder
		ObjectAutoCompleteBuilder<Person,String> builder = new ObjectAutoCompleteBuilder<Person,String>(provider);
		builder.autoCompleteRenderer(renderer);
		builder.searchLinkImage(ResourceReferences.CROSS_IMG_LOCAL);
		
		//autocompletefield
		ObjectAutoCompleteField<Person, String> autocompleteField = builder.build("toField", new PropertyModel<String>(newMessage, "to"));
		final TextField<String> toField = autocompleteField.getSearchTextField();
		toField.add(new AttributeModifier("class", true, new Model<String>("formInputField")));
		toField.setRequired(true);
		form.add(autocompleteField);

		
		//subject
		form.add(new Label("subjectLabel", new ResourceModel("message.subject")));
		final TextField<String> subjectField = new TextField<String>("subjectField", new PropertyModel<String>(newMessage, "subject"));
		form.add(subjectField);
		
		//body
		form.add(new Label("messageLabel", new ResourceModel("message.message")));
		final TextArea<String> messageField = new TextArea<String>("messageField", new PropertyModel<String>(newMessage, "message"));
		messageField.setRequired(true);
		form.add(messageField);
		
		
		//send button
		IndicatingAjaxButton sendButton = new IndicatingAjaxButton("sendButton", form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
								
				//get the backing model
				NewMessageModel newMessage = (NewMessageModel) form.getModelObject();
				
				//generate the thread id
				String threadId = ProfileUtils.generateUuid();
				
				//save it, it will be abstracted into its proper parts and email notifications sent
				if(messagingLogic.sendNewMessage(newMessage.getTo(), newMessage.getFrom(), threadId, newMessage.getSubject(), newMessage.getMessage())) {
					
					//post event
					sakaiProxy.postEvent(ProfileConstants.EVENT_MESSAGE_SENT, "/profile/" + newMessage.getFrom(), true);
					
					//success
					formFeedback.setDefaultModel(new ResourceModel("success.message.send.ok"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("success")));
					
					target.appendJavascript("$('#" + form.getMarkupId() + "').slideUp();");
					target.appendJavascript("setMainFrameHeight(window.name);");
					
				} else {
					//error
					formFeedback.setDefaultModel(new ResourceModel("error.message.send.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("alertMessage")));
				}
				
				target.addComponent(formFeedback);
            }
			
			protected void onError(AjaxRequestTarget target, Form form) {
				
				//check which item didn't validate and update the feedback model
				if(!toField.isValid()) {
					formFeedback.setDefaultModel(new ResourceModel("error.message.required.to"));
				}
				if(!messageField.isValid()) {
					formFeedback.setDefaultModel(new ResourceModel("error.message.required.body"));
				}
				formFeedback.add(new AttributeModifier("class", true, new Model<String>("alertMessage")));	

				target.addComponent(formFeedback);
			}
		};
		form.add(sendButton);
		sendButton.setModel(new ResourceModel("button.message.send"));
		
		add(form);
		
	}
	
}
