package org.sakaiproject.api.app.podcasts;

import com.sun.syndication.feed.synd.SyndFeed;

public interface PodfeedService {

	public static final String PODFEED_CATEGORY = "Podcast";
	
	/**
	 * Method to generate the XML file for the specific category of feed with the name passed in.
	 * 
	 * @param category What category of feed is to be generated.
	 * @param name What is the name of the feed.
	 */
	public SyndFeed generatePodcastRSS(String category, String name);

	public SyndFeed generatePodcastRSS(String category, String name, String siteID);

	public SyndFeed generatePodcastRSS(String Category);

}
