package org.sakaiproject.scorm.ui.console.pages;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;

public class ConsoleBasePage extends SakaiPortletWebPage {

	private static final long serialVersionUID = 1L;
	
	public ConsoleBasePage() {
		add(new BookmarkablePageLink("listLink", PackageListPage.class));
		add(new BookmarkablePageLink("zipListLink", ZipListPage.class));
		add(new BookmarkablePageLink("uploadLink", PackageUploadPage.class));
		//add(new BookmarkablePageLink("dataManagerListLink", DataManagerListPage.class));
	}

	
}
