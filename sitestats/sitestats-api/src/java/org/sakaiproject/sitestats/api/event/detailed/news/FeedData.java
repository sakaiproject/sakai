package org.sakaiproject.sitestats.api.event.detailed.news;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Data for a news (rss) feed
 * @author plukasew
 */
public class FeedData implements ResolvedEventData
{
	public final String title;

	/**
	 * Constructor
	 * @param title the title of the feed
	 */
	public FeedData(String title)
	{
		this.title = title;
	}
}
