package org.sakaiproject.scorm.ui.helper;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.ui.helper.pages.RemoveContentPackage;

public class ScormRemoveHelper extends WebApplication {

	private ScormClientFacade clientFacade;

	protected void init()
	{
		addComponentInstantiationListener(new SpringComponentInjector(this));
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		getDebugSettings().setAjaxDebugModeEnabled(false);
		getRequestCycleSettings().setRenderStrategy(IRequestCycleSettings.ONE_PASS_RENDER);
		mountBookmarkablePage("/remove", RemoveContentPackage.class);
	}
	
	public Class getHomePage() {
		return RemoveContentPackage.class;
	}
	
	public ScormClientFacade getClientFacade() {
		return clientFacade;
	}

	public void setClientFacade(ScormClientFacade clientFacade) {
		this.clientFacade = clientFacade;
	}
}
