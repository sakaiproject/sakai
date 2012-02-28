package org.sakaiproject.scorm.ui.console.components;

import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

public class SakaiFeedbackPanel extends FeedbackPanel {

	public SakaiFeedbackPanel(String id, IFeedbackMessageFilter filter) {
		super(id, filter);
	}

	public SakaiFeedbackPanel(String id) {
		super(id);
	}

}
