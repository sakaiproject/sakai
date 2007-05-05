package org.sakaiproject.scorm.client.pages;

import wicket.ajax.AjaxRequestTarget;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.PropertyModel;

public class ContentPanel extends Panel {
	Label messageLabel;
	String message = "default";
	
	public ContentPanel(String id, String message) {
		super(id);
		this.message = message;
		messageLabel = new Label("message", new PropertyModel(this, "message"));
		messageLabel.setOutputMarkupId(true);
		add(messageLabel);
		
		//messageLabel = new AjaxEditableLabel("message");
		//messageModel = new Model(message);
		//messageLabel.setLabel(messageModel);	
		//add(messageLabel);
	}
	
	public String getMessage() {
		return this.message;
	}

	public void updateMessage(AjaxRequestTarget target, String message) {
		this.message = message;
		//PropertyModel model = (PropertyModel)messageLabel.getModel();
		//model.setObject(messageLabel, message);
		target.addComponent(messageLabel);
	}
	

	
}
