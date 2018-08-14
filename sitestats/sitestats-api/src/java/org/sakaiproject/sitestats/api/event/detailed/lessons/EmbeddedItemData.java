package org.sakaiproject.sitestats.api.event.detailed.lessons;

import org.apache.commons.lang3.StringUtils;

/**
 * Data for a Lessons item which has its content embedded in the page as opposed to presented as an external content link.
 */
public class EmbeddedItemData implements LessonsData
{
	public final String desc;
	public final PageData parentPage;

	/**
	 * Constructor
	 * @param description an optional description for the items (as shown on the page in Lessons)
	 * @param parentPage the page the item is embedded into
	 */
	public EmbeddedItemData(String description, PageData parentPage)
	{
		desc = StringUtils.trimToEmpty(description);
		this.parentPage = parentPage;
	}
}
