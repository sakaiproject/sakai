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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.objectautocomplete.AutoCompletionChoicesProvider;
import org.wicketstuff.objectautocomplete.ObjectAutoCompleteBuilder;
import org.wicketstuff.objectautocomplete.ObjectAutoCompleteField;
import org.wicketstuff.objectautocomplete.ObjectAutoCompleteRenderer;

import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileMessagingLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.tool.components.ResourceReferences;
import org.sakaiproject.profile2.tool.models.NewMessageModel;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

@Slf4j
public class ComposeNewMessage extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileMessagingLogic")
	protected ProfileMessagingLogic messagingLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	protected ProfileConnectionsLogic connectionsLogic;
	
	private TextField<String> toField;
	private Label formFeedback;
	
	public ComposeNewMessage(String id) {
		super(id);
		
		//current user
		final String userId = sakaiProxy.getCurrentUserId();
		
		//setup model
		NewMessageModel newMessage = new NewMessageModel();
		newMessage.setFrom(userId);
		
		//feedback for form submit action
		formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		add(formFeedback);
		
		
		//setup form	
		final Form<NewMessageModel> form = new Form<NewMessageModel>("form", new Model<NewMessageModel>(newMessage));
		
		//close button
		/*
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
		*/
		
		
		//to label
		form.add(new Label("toLabel", new ResourceModel("message.to")));
		
		//get connections
		final List<Person> connections = connectionsLogic.getConnectionsForUser(userId);
		Collections.sort(connections);

		// list provider
		AutoCompletionChoicesProvider<Person> provider = new AutoCompletionChoicesProvider<Person>() {
			private static final long serialVersionUID = 1L;

			public Iterator<Person> getChoices(String input) {
            	return connectionsLogic.getConnectionsSubsetForSearch(connections, input, true).iterator();
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
		builder.preselect();
		
		//autocompletefield
		final ObjectAutoCompleteField<Person, String> autocompleteField = builder.build("toField", new PropertyModel<String>(newMessage, "to"));
		toField = autocompleteField.getSearchTextField();
		toField.setMarkupId("messagerecipientinput");
		toField.setOutputMarkupId(true);
		toField.add(new AttributeModifier("class", true, new Model<String>("formInputField")));
		toField.setRequired(true);
		form.add(autocompleteField);
		
		//subject
		form.add(new Label("subjectLabel", new ResourceModel("message.subject")));
		final TextField<String> subjectField = new TextField<String>("subjectField", new PropertyModel<String>(newMessage, "subject"));
		subjectField.setMarkupId("messagesubjectinput");
		subjectField.setOutputMarkupId(true);
		subjectField.add(new RecipientEventBehavior("onfocus"));
		form.add(subjectField);
		
		//body
		form.add(new Label("messageLabel", new ResourceModel("message.message")));
		final TextArea<String> messageField = new TextArea<String>("messageField", new PropertyModel<String>(newMessage, "message"));
		messageField.setMarkupId("messagebodyinput");
		messageField.setOutputMarkupId(true);
		messageField.setRequired(true);
		messageField.add(new RecipientEventBehavior("onfocus"));
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
				if(newMessage.getTo()!=null && messagingLogic.sendNewMessage(newMessage.getTo(), newMessage.getFrom(), threadId, newMessage.getSubject(), newMessage.getMessage())) {
					
					//success
					formFeedback.setDefaultModel(new ResourceModel("success.message.send.ok"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("success")));
					
					//target.appendJavascript("$('#" + form.getMarkupId() + "').slideUp();");
					target.appendJavaScript("setMainFrameHeight(window.name);");
					
					//PRFL-797 all fields when successful, to prevent multiple messages.
					//User can just click Compose message again to get a new form
					this.setEnabled(false);
					autocompleteField.setEnabled(false);
					subjectField.setEnabled(false);
					messageField.setEnabled(false);
					target.add(this);
					target.add(autocompleteField);
					target.add(subjectField);
					target.add(messageField);
					
				} else {
					//error
					formFeedback.setDefaultModel(new ResourceModel("error.message.send.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("alertMessage")));
				}
				
				formFeedback.setVisible(true);
				target.add(formFeedback);
				
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

				target.add(formFeedback);
			}
		};
		form.add(sendButton);
		sendButton.setModel(new ResourceModel("button.message.send"));
		
		add(form);
		
	}
	
	
	/**
	 * Inner class to provide simple validation of the to field and print an error message
	 * if it's not set. This is a bit of a hack since it should be on the toField, 
	 * but we can't overload the onchange/onblur event of the toField itself as that event is already taken.
	 * 
	 * Would need to reimplement autocompletebox as plain javascript.
	 * 
	 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
	 *
	 */
	class RecipientEventBehavior extends AjaxEventBehavior {

		private static final long serialVersionUID = 1L;

		public RecipientEventBehavior(String event) {
			super(event);
		}

		@Override
		protected void onEvent(AjaxRequestTarget target) {
			if(StringUtils.isBlank(toField.getValue())) {
        		formFeedback.setDefaultModel(new ResourceModel("error.message.required.to"));
        		formFeedback.add(new AttributeModifier("class", true, new Model<String>("alertMessage")));
        	} else {
        		formFeedback.setVisible(false);
        	}
    		target.add(formFeedback);

			
		}
		
		
	}
	
	
}
