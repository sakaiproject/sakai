package org.sakaiproject.sitestats.api.event.detailed.announcements;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Data for an announcement
 * @author plukasew
 */
public class AnnouncementData implements ResolvedEventData
{
	public final String title;

	/**
	 * Constructor
	 * @param title the title of the announcement
	 */
	public AnnouncementData(String title)
	{
		this.title = title;
	}
}
