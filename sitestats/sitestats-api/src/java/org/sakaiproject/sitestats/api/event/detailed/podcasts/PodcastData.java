package org.sakaiproject.sitestats.api.event.detailed.podcasts;

import java.time.Instant;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Data for a podcast
 * @author plukasew
 */
public class PodcastData implements ResolvedEventData
{
	public final String title;
	public final Instant publishTime;
	public final String parentUrl;

	/**
	 * Constructor
	 * @param title the title of the podcast
	 * @param publishTime the date/time the podcast was published
	 * @param parentUrl the url to the podcast's parent container
	 */
	public PodcastData(String title, Instant publishTime, String parentUrl)
	{
		this.title = title;
		this.publishTime = publishTime;
		this.parentUrl = parentUrl;
	}
}
