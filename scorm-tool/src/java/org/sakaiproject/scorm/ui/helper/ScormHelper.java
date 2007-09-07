package org.sakaiproject.scorm.ui.helper;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.file.Folder;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.ui.helper.pages.UploadContentPackage;

public class ScormHelper extends WebApplication {

	private ScormClientFacade clientFacade;

	private Folder uploadFolder = null;

	protected void init()
	{
		addComponentInstantiationListener(new SpringComponentInjector(this));
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		getDebugSettings().setAjaxDebugModeEnabled(false);
		getRequestCycleSettings().setRenderStrategy(IRequestCycleSettings.ONE_PASS_RENDER);
		mountBookmarkablePage("/upload", UploadContentPackage.class);
	}
	
	public Class getHomePage() {
		return UploadContentPackage.class;
	}
	
	public ScormClientFacade getClientFacade() {
		return clientFacade;
	}

	public void setClientFacade(ScormClientFacade clientFacade) {
		this.clientFacade = clientFacade;
	}
	
	public Folder getUploadFolder() {
		return uploadFolder;
	}
}
