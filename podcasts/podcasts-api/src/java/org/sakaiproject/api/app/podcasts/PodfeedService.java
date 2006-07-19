package org.sakaiproject.api.app.podcasts;

public interface PodfeedService {

	/**
	 * This method generates the RSS feeds for podcasting based on category (i.e. Podcast)
	 * @param Category The category to run as containing podcast content.
	 * @param Name The filename to write out the format to.
	 */
     public void generatePodcastRSS(String Category, String Name);
     

}
