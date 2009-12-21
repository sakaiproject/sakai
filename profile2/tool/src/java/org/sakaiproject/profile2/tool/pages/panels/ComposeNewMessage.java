package org.sakaiproject.profile2.tool.pages.panels;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.Response;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteRenderer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
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
		Form form = new Form("form") {
			private static final long serialVersionUID = 1L;

			public void onSubmit(){
			}
		};
		
		
		//to label
		form.add(new Label("toLabel", new ResourceModel("message.to")));
		
		//get connections
		final List<User> connections = profileLogic.getConnectionsForUser(userId);

		//renderer - required when not using simple strings
		AbstractAutoCompleteRenderer autoCompleteRenderer = new AbstractAutoCompleteRenderer() {
			protected final String getTextValue(final Object object) {
				User user = (User) object;
				return user.getDisplayName();
			}
			protected final void renderChoice(final Object object, final Response response, final String criteria) {
				response.write(getTextValue(object));
			}
			
		};
		
		
		// textfield
		AbstractAutoCompleteTextField<User> toField = new AbstractAutoCompleteTextField<User>("toField", new PropertyModel(message, "to"), autoCompleteRenderer) {
			protected final List<User> getChoiceList(final String input) {
				return profileLogic.getConnectionsSubsetForSearch(connections, input);
			}

			protected final String getChoiceValue(final User choice) throws Throwable {
				return choice.getEid();
			}
		};
		
		
		form.add(toField);
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
