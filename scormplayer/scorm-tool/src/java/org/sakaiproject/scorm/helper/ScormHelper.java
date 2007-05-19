package org.sakaiproject.scorm.helper;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.file.Folder;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.helper.pages.UploadContentPackage;

public class ScormHelper extends WebApplication {

	private ScormClientFacade clientFacade;

	private Folder uploadFolder = null;

	protected void init()
	{
		addComponentInstantiationListener(new SpringComponentInjector(this));
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		//getDebugSettings().setAjaxDebugModeEnabled(true);
		//Objects.setObjectStreamFactory(new IObjectStreamFactory.DefaultObjectStreamFactory());

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
	
	/*protected WebRequest newWebRequest(HttpServletRequest servletRequest) {
		return new UploadWebRequest(servletRequest);
	}*/

	public Folder getUploadFolder() {
		return uploadFolder;
	}
}
