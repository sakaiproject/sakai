package org.sakaiproject.scorm.ui.console.pages;

import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;
import org.sakaiproject.wicket.markup.html.link.NavIntraLink;
import org.sakaiproject.wicket.markup.html.repeater.data.toolbar.NavIntraToolbar;

public class ConsoleBasePage extends SakaiPortletWebPage {

	private static final long serialVersionUID = 1L;
	
	public ConsoleBasePage() {
		
		add(new NavIntraLink("listLink", new ResourceModel("link.list"), PackageListPage.class));
		add(new NavIntraLink("uploadLink", new ResourceModel("link.upload"), PackageUploadPage.class));
		add(new NavIntraLink("validateLink", new ResourceModel("link.validate"), ValidationPage.class));

	}
	
}
