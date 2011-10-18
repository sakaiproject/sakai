package org.sakaiproject.scorm.ui.reporting.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.scorm.model.api.Learner;


public class LearnerDetailsPanel extends Panel {

	/**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

	public LearnerDetailsPanel(String id, IModel<Learner> model) {
	    super(id, model);
	    initPanel(model.getObject());
    }

	private void initPanel(Learner object) {
		add(new Label("id", object.getDisplayId()));
		add(new Label("name", object.getDisplayName()));
    }

}
