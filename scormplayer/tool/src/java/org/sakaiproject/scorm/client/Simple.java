package org.sakaiproject.scorm.client;

import org.sakaiproject.scorm.client.pages.Index;

import wicket.markup.html.AjaxServerAndClientTimeFilter;
import wicket.protocol.http.WebApplication;

public class Simple  extends WebApplication {

	
	public Simple() {
	
	}
	
	protected void init()
	{
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		getRequestCycleSettings().addResponseFilter(new AjaxServerAndClientTimeFilter());
		getDebugSettings().setAjaxDebugModeEnabled(true);
	}

	@Override
	public Class getHomePage() {
		return Index.class;
	}

	
	
}
