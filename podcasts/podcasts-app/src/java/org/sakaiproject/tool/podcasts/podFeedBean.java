package org.sakaiproject.tool.podcasts;

import org.sakaiproject.util.ResourceLoader;

public class podFeedBean {

	// static reference to access messages in bundle
	private static ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.podcasts.bundle.Messages");

	// Used by podFeedRevise for global feed info
	private String podFeedTitle;
	private String podFeedDescription;
	
	/**
	 * @return Returns the podFeedTitle.
	 */
	public String getPodFeedTitle() {
		// TODO: if (podcastService.getFeedTitle == null) {
		//			podFeedTitle = rb.getMessage(podfeed_global_desc1) + department code + course number + rb.getMessage(podfeed_global_desc2;
		//       }
		//		else {
		//			podFeedTitle = podcastService.getGlobalDescription();
		//		}
		podFeedTitle = rb.getString("podfeed_global_title") + " TestPodcast";
		
		return podFeedTitle;
	}

	/**
	 * @param podFeedTitle The podFeedTitle to set.
	 */
	public void setPodFeedTitle(String podFeedTitle) {
		this.podFeedTitle = podFeedTitle;
		
		//TODO: store it somewhere for later
	}

	/**
	 * @return Returns the podFeedDescription.
	 */
	public String getPodFeedDescription() {
		// TODO: if (podcastService.getFeedDescription == null) {
		//			podFeedDescription = rb.getMessage(podfeed_global_desc1) + department code + course number + rb.getMessage(podfeed_global_desc2;
		//       }
		//		else {
		//			podFeedDescription = podcastService.getGlobalDescription();
		//		}
		podFeedDescription = rb.getString("podfeed_global_desc1") + "TestPodcast" + rb.getString("podfeed_global_desc2"); 
		
		return podFeedDescription;
	}

	/**
	 * @param podFeedDescription The podFeedDescription to set.
	 */
	public void setPodFeedDescription(String podFeedDescription) {
		this.podFeedDescription = podFeedDescription;
		
		//TODO: store it somewhere for later
	}


	/**
	 * Determine if revisions made to global feed info and apply changes.
	 * 
	 * @return String to control navigation
	 */
	public String processPodfeedRevise() {
		if (! (podFeedTitle.equals("") || podFeedTitle.equals(""/* old title */))) {
			// Replace with this title
		}
		
		if (! (podFeedDescription.equals("") || podFeedDescription.equals(""/* old description */))) {
			// Replace with this description
		}
		
		return "cancel";
	}
	
	/**
	 * Cancels revising global podfeed information
	 * 
	 * @return String Sent to return to the main page
	 */
	public String processCancelPodfeedRevise() {
		return "cancel";
	}

}
