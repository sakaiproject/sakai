package org.sakaiproject.sitestats.api.event.detailed.lessons;

/**
 * Data for a Lessons item which is presented on the page as an external content link as opposed
 * to having its content embedded in the page.
 */
public class ContentLinkItemData implements LessonsData
{
	public final String name;
	public final PageData parentPage;

	/**
	 * Constructor
	 * @param name the name of the item as it appears on the Lessons page
	 * @param parentPage the page the item appears on
	 */
	public ContentLinkItemData(String name, PageData parentPage)
	{
		this.name = name;
		this.parentPage = parentPage;
	}
}
