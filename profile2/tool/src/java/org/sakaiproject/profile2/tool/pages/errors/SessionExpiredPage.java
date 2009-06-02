package org.sakaiproject.profile2.tool.pages.errors;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;

import org.sakaiproject.profile2.tool.pages.BasePage;

public class SessionExpiredPage extends BasePage {

	public SessionExpiredPage() {
		
		
		add(new Label("heading", new ResourceModel("exception.heading.session.expired")));
		add(new Label("text", new ResourceModel("exception.text.session.expired")));

	}
}
