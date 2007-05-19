package org.sakaiproject.scorm.client;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

public abstract class ClientPanel extends Panel {

	public ClientPanel(String id) {
		super(id);
	}
	
	public ClientPanel(String id, IModel model) {
		super(id, model);
	}

	protected Label newResourceLabel(String id, Component component) {
		return new Label(id, new StringResourceModel(id, component, null));
	}
}
