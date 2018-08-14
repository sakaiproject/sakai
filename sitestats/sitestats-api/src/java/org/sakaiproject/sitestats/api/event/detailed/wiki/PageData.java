package org.sakaiproject.sitestats.api.event.detailed.wiki;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Data for a wiki page
 * @author plukasew
 */
public class PageData implements ResolvedEventData
{
	public final String name;
	public final String url;

	/**
	 * Constructor
	 * @param name the name of the page
	 * @param url the url to the page
	 */
	public PageData(String name, String url)
	{
		this.name = name;
		this.url = url;
	}
}
