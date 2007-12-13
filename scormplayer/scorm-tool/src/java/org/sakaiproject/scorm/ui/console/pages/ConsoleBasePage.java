package org.sakaiproject.scorm.ui.console.pages;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;

public class ConsoleBasePage extends SakaiPortletWebPage {

	private static final long serialVersionUID = 1L;
	
	public ConsoleBasePage() {
		add(new ConsoleLink("listLink", PackageListPage.class));
		add(new ConsoleLink("zipListLink", ZipListPage.class));
		add(new ConsoleLink("uploadLink", PackageUploadPage.class));
			
		//add(new BookmarkablePageLink("dataManagerListLink", DataManagerListPage.class));
	}

	public class ConsoleLink extends BookmarkablePageLink {

		private static final long serialVersionUID = 1L;

		public ConsoleLink(String id, Class pageClass) {
			super(id, pageClass);
			setAutoEnable(true);
			setBeforeDisabledLink("");
			setAfterDisabledLink("");
		}
		
		protected void disableLink(final ComponentTag tag)
		{
			// if the tag is an anchor proper
			if (tag.getName().equalsIgnoreCase("a") || tag.getName().equalsIgnoreCase("link")
					|| tag.getName().equalsIgnoreCase("area"))
			{
				// Change anchor link to span tag
				tag.setName("span");

				tag.put("class", "margin-right:1em;white-space:nowrap;");
				
				// Remove any href from the old link
				tag.remove("href");

				tag.remove("onclick");
			}
			// if the tag is a button or input
			else if ("button".equalsIgnoreCase(tag.getName())
					|| "input".equalsIgnoreCase(tag.getName()))
			{
				tag.put("disabled", "disabled");
			}
		}
		
	}
	
}
