package org.sakaiproject.sitestats.api.event.detailed.forums;

/**
 * Data for a forum
 * @author plukasew
 */
public final class ForumData implements MsgForumsData
{
	public final String title;

	/**
	 * Constructor
	 * @param title the title of the forum
	 */
	public ForumData(String title)
	{
		this.title = title;
	}
}
