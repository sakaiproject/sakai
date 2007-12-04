package org.sakaiproject.scorm.ui.console.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.sakaiproject.scorm.ui.console.wizard.PackageManagementWizard;

public class PackageUploadPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;

	public PackageUploadPage(PageParameters params) {
		add(new PackageManagementWizard("upload-wizard"));
	}
	
	
}
