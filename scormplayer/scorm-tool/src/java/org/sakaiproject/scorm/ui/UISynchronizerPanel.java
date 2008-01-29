package org.sakaiproject.scorm.ui;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.scorm.ui.player.components.ActivityTree;

public abstract class UISynchronizerPanel extends Panel implements UISynchronizer {

	public UISynchronizerPanel(String id) {
		super(id);
	}
	
	public UISynchronizerPanel(String id, IModel model) {
		super(id, model);
	}
	
	public abstract WebMarkupContainer getContentPanel();

	public abstract ActivityTree getTree();
	
}
