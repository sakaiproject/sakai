package org.sakaiproject.dashboard.tool;

import org.sakaiproject.dashboard.tool.pages.BasePage;
import org.sakaiproject.dashboard.tool.pages.Items;

public class Dispatcher extends BasePage {
	
	public Dispatcher() {
		super();
		
		setResponsePage(new Items());
		
	}
}
