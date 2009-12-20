package org.sakaiproject.profile2.tool.pages.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.tool.Locator;

public class ComposeNewMessage extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ComposeNewMessage.class);
	private transient SakaiProxy sakaiProxy;
	
	public ComposeNewMessage(String id) {
		super(id);
		
		//get API's
		sakaiProxy = getSakaiProxy();
		
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
		
		final List list = new ArrayList();
		list.add("item 1");
		list.add("item 2");
		list.add("item 3");
		
		
		final AutoCompleteTextField toField = new AutoCompleteTextField("toField", new PropertyModel(message, "to")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected Iterator<String> getChoices(String input) {
				if (Strings.isEmpty(input)) {
					List<String> emptyList = Collections.emptyList();
					return emptyList.iterator();
				}

				List<String> keyMatches = new ArrayList<String>(10);

				return list.iterator();
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
	}
	*/
	
	private SakaiProxy getSakaiProxy() {
		return Locator.getSakaiProxy();
	}

}
