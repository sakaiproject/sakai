package org.sakaiproject.scorm.helper.components;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.sakaiproject.scorm.client.ClientPanel;

public class UserCopyrightPanel extends ClientPanel {
	private static final long serialVersionUID = 1L;

	private Form form;
	
	public UserCopyrightPanel(String id, Form form, IModel model) {
		super(id, model);
		FormComponent userCopyrightInput = new TextArea("userCopyright");
		
		add(newResourceLabel("userCopyrightLabel", this));
		add(userCopyrightInput);
	}
}
