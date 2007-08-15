package org.sakaiproject.ziparchive.helper;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.file.Folder;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.helper.pages.UploadContentPackage;
import org.sakaiproject.ziparchive.helper.pages.UploadZipArchive;
import org.sakaiproject.ziparchive.helper.pages.UploadZipEntry;

public class ZipEntryHelper extends WebApplication {

	private ScormClientFacade clientFacade;

	private Folder uploadFolder = null;

	protected void init()
	{
		addComponentInstantiationListener(new SpringComponentInjector(this));
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		getDebugSettings().setAjaxDebugModeEnabled(false);

		mountBookmarkablePage("/upload", UploadContentPackage.class);
	}
	
	public Class getHomePage() {
		return UploadZipEntry.class;
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
