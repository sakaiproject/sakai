package org.sakaiproject.scorm.helper;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadWebRequest;
import org.apache.wicket.markup.html.AjaxServerAndClientTimeFilter;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.io.IObjectStreamFactory;
import org.apache.wicket.util.lang.Objects;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.helper.pages.UploadPage;
import org.sakaiproject.scorm.helper.pages.UploadContentPackage;
import org.apache.wicket.util.file.Folder;

import org.apache.wicket.settings.ISecuritySettings;
import org.apache.wicket.util.crypt.ClassCryptFactory;
import org.apache.wicket.util.crypt.NoCrypt;

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
