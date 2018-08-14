package org.sakaiproject.sitestats.api.event.detailed.lessons;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Data for a Lessons page
 * @author plukasew
 */
public class PageData implements LessonsData
{
	public final String title;
	public final List<String> pageHierarchy;

	public static final DeletedPage DELETED_PAGE = new PageData.DeletedPage();
	public static final String DELETED_HIERARCHY_PAGE = "DHP";

	/**
	 * Constructor
	 * @param title the title of the page
	 * @param pageHierarchy the path to this page, as a list of page titles
	 */
	public PageData(String title, List<String> pageHierarchy)
	{
		this.title = StringUtils.trimToEmpty(title);
		this.pageHierarchy = Collections.unmodifiableList(pageHierarchy);
	}

	// a deleted page (no further info is available)
	public static final class DeletedPage implements LessonsData { }
}
