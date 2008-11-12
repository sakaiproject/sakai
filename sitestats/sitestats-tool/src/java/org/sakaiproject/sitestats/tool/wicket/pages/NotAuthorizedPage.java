package org.sakaiproject.sitestats.tool.wicket.pages;

import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.sitestats.tool.wicket.components.CSSFeedbackPanel;


/**
 * @author Nuno Fernandes
 */
public class NotAuthorizedPage extends BasePage {

	public NotAuthorizedPage() {
		add(new CSSFeedbackPanel("messages"));
		
		error((String) new ResourceModel("unauthorized").getObject());
	}
}

