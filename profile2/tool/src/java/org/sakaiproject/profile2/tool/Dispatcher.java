package org.sakaiproject.profile2.tool;

import org.sakaiproject.profile2.tool.pages.BasePage;
import org.sakaiproject.profile2.tool.pages.MyProfile;

public class Dispatcher extends BasePage {
	
	public Dispatcher() {
		super();
		
		setResponsePage(new MyProfile());
		
	}
}
