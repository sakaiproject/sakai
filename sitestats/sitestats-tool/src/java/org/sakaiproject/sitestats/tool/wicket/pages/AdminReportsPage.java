package org.sakaiproject.sitestats.tool.wicket.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.ResourceModel;

public class AdminReportsPage extends ReportsPage {
	
	public AdminReportsPage(PageParameters pageParameters) {
		super(pageParameters);
	}
	
	public String getPageTitle() {
		return (String) new ResourceModel("menu_adminreports").getObject();
	}
}
