package org.sakaiproject.profile2.tool.pages.panels;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
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
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.tool.components.CloseButton;
import org.sakaiproject.profile2.tool.components.ResourceReferences;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.wicketstuff.objectautocomplete.AutoCompletionChoicesProvider;
import org.wicketstuff.objectautocomplete.ObjectAutoCompleteBuilder;
import org.wicketstuff.objectautocomplete.ObjectAutoCompleteField;
import org.wicketstuff.objectautocomplete.ObjectAutoCompleteRenderer;

public class ComposeNewMessage extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ComposeNewMessage.class);
	private transient SakaiProxy sakaiProxy;
	private transient ProfileLogic profileLogic;
	private String toValue;
	
	public ComposeNewMessage(String id) {
		super(id);
		
		//get API's
		sakaiProxy = getSakaiProxy();
		profileLogic = getProfileLogic();
		
		//get userId
		final String userId = sakaiProxy.getCurrentUserId();
		
		//setup model
		Message message = new Message();
		
		//feedback for form submit action
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		final String formFeedbackId = formFeedback.getMarkupId();
		add(formFeedback);
		
		
		//setup form	
		Form<Message> form = new Form<Message>("form", new Model<Message>(message));
		
		//to label
		form.add(new Label("toLabel", new ResourceModel("message.to")));
		
		//get connections
		final List<Person> connections = profileLogic.getConnectionsForUser(userId);

		// list provider
		AutoCompletionChoicesProvider<Person> provider = new AutoCompletionChoicesProvider<Person>() {
			private static final long serialVersionUID = 1L;

			public Iterator<Person> getChoices(String input) {
            	return profileLogic.getConnectionsSubsetForSearch(connections, input).iterator();
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
		ObjectAutoCompleteField<Person, String> autocompleteField = builder.build("toField", new PropertyModel<String>(message, "to"));
		final TextField<String> toField = autocompleteField.getSearchTextField();
		toField.add(new AttributeModifier("class", true, new Model<String>("formInputField")));
		toField.setRequired(true);
		form.add(autocompleteField);

		
		//subject
		form.add(new Label("subjectLabel", new ResourceModel("message.subject")));
		TextField<String> subjectField = new TextField<String>("subjectField", new PropertyModel<String>(message, "subject"));
		form.add(subjectField);
		
		//body
		form.add(new Label("messageLabel", new ResourceModel("message.message")));
		final TextArea<String> messageField = new TextArea<String>("messageField", new PropertyModel<String>(message, "message"));
		messageField.setRequired(true);
		form.add(messageField);
		
		
		//send button
		IndicatingAjaxButton sendButton = new IndicatingAjaxButton("sendButton", form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				//get the backing model
				Message message = (Message) form.getModelObject();
				
				//add other info
				//message.setThread(sakaiProxy.generateUuid());
				message.setFrom(userId);	
				
				//send the message
				if(profileLogic.sendPrivateMessage(message)) {
					
					//post event
					sakaiProxy.postEvent(ProfileConstants.EVENT_MESSAGE_SENT, "/profile/"+message.getFrom(), true);
					
					//if email is enabled for this message type, send email
					/*
					if(profileLogic.isEmailEnabledForThisMessageType(message.getTo(), ProfileConstants.EMAIL_NOTIFICATION_PRIVATE_MESSAGE)) {
						//message sending goes here, use the ETS
					}
					*/
					
					//success
					formFeedback.setDefaultModel(new ResourceModel("success.message.send.ok"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("success")));
					
				} else {
					//error
					formFeedback.setDefaultModel(new ResourceModel("error.message.send.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("alert")));
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
	
	
	/* reinit for deserialisation (ie back button) */
	/*
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("ComposeNewMessage has been deserialized.");
		//re-init our transient objects
		sakaiProxy = getSakaiProxy();
		profileLogic = getProfileLogic();
	}
	*/
	

	private SakaiProxy getSakaiProxy() {
		return Locator.getSakaiProxy();
	}
	private ProfileLogic getProfileLogic() {
		return Locator.getProfileLogic();
	}

}
