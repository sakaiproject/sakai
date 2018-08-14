package org.sakaiproject.sitestats.api.event.detailed.lessons;

/**
 * Data for a comments section. There are two variations on a comments section, normal comments a user adds,
 * and "forced comments" for student pages which are added automatically.
 * 
 * @author plukasew
 */
public class CommentsSectionItemData implements LessonsData
{
	public static final ForcedComments FORCED = new ForcedComments();

	public final PageData parent;

	/**
	 * Constructor
	 * @param parent the page the comments section is on
	 */
	public CommentsSectionItemData(final PageData parent)
	{
		this.parent = parent;
	}

	public static class ForcedComments implements LessonsData
	{
		// Due to Lessons design choices, forced comments cannot be tied back to their student page and therefore will have no page hierarchy
	}
}
