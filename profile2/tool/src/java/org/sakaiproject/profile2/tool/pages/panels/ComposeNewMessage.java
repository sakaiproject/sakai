package org.sakaiproject.profile2.tool.pages.panels;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.Response;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteRenderer;
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
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.tool.components.AbstractAutoCompleteTextField;
import org.sakaiproject.user.api.User;

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
		message.setFrom(userId);		
		
		
		//setup form	
		Form<Message> form = new Form<Message>("form", new Model(message)) {
			private static final long serialVersionUID = 1L;

			public void onSubmit(){
			}
		};
		
		
		//to label
		form.add(new Label("toLabel", new ResourceModel("message.to")));
		
		//get connections
		final List<User> connections = profileLogic.getConnectionsForUser(userId);

		//toField autocompleterenderer - required when not using simple strings
		AbstractAutoCompleteRenderer autoCompleteRenderer = new AbstractAutoCompleteRenderer() {
			protected final String getTextValue(final Object object) {
				User user = (User) object;
				//workaround to track the ID so we can use it.
				setToValue(user.getId());
				return user.getDisplayName();
			}
			protected final void renderChoice(final Object object, final Response response, final String criteria) {
				response.write(getTextValue(object));
			}
			
		};
		
		// toField
		final AbstractAutoCompleteTextField<User> toField = new AbstractAutoCompleteTextField<User>("toField", new PropertyModel(message, "to"), autoCompleteRenderer) {
			protected final List<User> getChoiceList(final String input) {
				return profileLogic.getConnectionsSubsetForSearch(connections, input);
			}

			protected final String getChoiceValue(final User choice) throws Throwable {
				//this is never called when renderer is being used
				return choice.getId();
			}
		};
		form.add(toField);

		
		//subject label
		form.add(new Label("subjectLabel", new ResourceModel("message.subject")));
		TextField<String> subjectField = new TextField<String>("subjectField", new PropertyModel(message, "subject"));
		form.add(subjectField);
		
		//subject label
		form.add(new Label("messageLabel", new ResourceModel("message.message")));
		TextArea<String> messageField = new TextArea<String>("messageField", new PropertyModel(message, "message"));
		form.add(messageField);
		
		
		//send button
		AjaxFallbackButton sendButton = new AjaxFallbackButton("sendButton", new ResourceModel("button.message.send"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				Message message = (Message) form.getModelObject();
				
				System.out.println("from: " + message.getFrom());
				System.out.println("to: " + message.getTo());
				System.out.println("toValue: " + getToValue());
				System.out.println("findChoice: " + toField.findChoice().getId());
				
				System.out.println(message.getSubject());
				System.out.println(message.getMessage());

				
            }
		};
		form.add(sendButton);
		
		
		
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
	
	public void setToValue(String toValue) {
		this.toValue = toValue;
	}


	public String getToValue() {
		return toValue;
	}


	private SakaiProxy getSakaiProxy() {
		return Locator.getSakaiProxy();
	}
	private ProfileLogic getProfileLogic() {
		return Locator.getProfileLogic();
	}

}
