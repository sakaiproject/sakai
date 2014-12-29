package org.sakaiproject.site.api;

/**
 * Allows handlers to know just before a site is removed.
 * This allows tools to do cleanup or something else before a site is removed.
 * It does not allow the deletion to be cancelled or aborted.
 * The advantage of using this over listening for events is that the event is only generated once the site has been
 * removed so you can no longer find it.
 *
 * @author Matthew Buckett
 */
public interface SiteRemovalAdvisor {

	/**
	 * Called just before a site is removed.
	 * @param site The site being removed, this will never be <code>null</code>.
	 */
	public void removed(Site site);

}
