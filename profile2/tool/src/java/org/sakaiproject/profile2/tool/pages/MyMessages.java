package org.sakaiproject.profile2.tool.pages;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.profile2.tool.pages.panels.ComposeNewMessage;

public class MyMessages extends BasePage {

	private static final Logger log = Logger.getLogger(MyMessages.class); 
	
	public MyMessages() {
		
		log.debug("MyMessages()");
		
		
		//new message panel
		final ComposeNewMessage newMessagePanel = new ComposeNewMessage("newMessagePanel");
		newMessagePanel.setOutputMarkupPlaceholderTag(true);
		newMessagePanel.setVisible(false);
		add(newMessagePanel);
		
		
		//new message button
		Form form = new Form("form");
		IndicatingAjaxButton newMessageButton = new IndicatingAjaxButton("newMessage", form) {
		
			public void onSubmit(AjaxRequestTarget target, Form form) {
				//show panel
				newMessagePanel.setVisible(true);
				target.addComponent(newMessagePanel);
				
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
		
		
		
		
	}

	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MyMessages has been deserialized.");
		//re-init our transient objects
		//profileLogic = getProfileLogic();
		//sakaiProxy = getSakaiProxy();
	}
	
	
}




