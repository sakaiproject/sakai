package org.sakaiproject.entity.api;

/**
 * Services which implement HardDeleteAware declare that they are able to purge themselves. Note that services must also be registered EntityProducers.
 *
 * @since 10.0
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public interface HardDeleteAware {

	/**
	 * Hard delete the content for the implementing service in the given site
	 * 
	 * @param siteId the siteId to use when finding content to delete
	 */
	public void hardDelete(String siteId);
	
}
