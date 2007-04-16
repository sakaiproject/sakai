package org.sakaiproject.scorm.client;

//import org.sakaiproject.user.api.UserDirectoryService;

import org.sakaiproject.scorm.client.api.ScormClientService;

import wicket.markup.html.AjaxServerAndClientTimeFilter;
import wicket.protocol.http.WebApplication;

public class ScormWicketApplication extends WebApplication {

	private ScormClientService scormClientService;
	
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
	
	public ScormClientService getScormClientService() {
		return scormClientService;
	}

	public void setScormClientService(ScormClientService scormClientService) {
		this.scormClientService = scormClientService;
	}
		
}
