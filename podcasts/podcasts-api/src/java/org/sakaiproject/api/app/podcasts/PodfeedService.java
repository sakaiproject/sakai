package org.sakaiproject.api.app.podcasts;

import org.sakaiproject.exception.InconsistentException;


public interface PodfeedService {

	public static final String PODFEED_CATEGORY = "Podcast";
	
	/**
	 * Method to generate the XML file for the specific category of feed with the name passed in.
	 * 
	 * @param category What category of feed is to be generated.
	 * @param name What is the name of the feed.
	 */
	public String generatePodcastRSS(String category, String name);

	public String generatePodcastRSS(String category, String name, String siteID, String feedType);

	public String generatePodcastRSS(String Category);

	public boolean getPodcastAccessPublic(String siteId) throws InconsistentException;
	
}
