package org.sakaiproject.entity.api;

/**
 * Services which implement ContentExistsAware declare that they are able to determine if they contain content for the site they are in.
 * Note that services must also be registered EntityProducers.
 *
 * @since 11.0
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public interface ContentExistsAware {

	/**
	 * Does this tool contain content in this site?
	 * 
	 * @param siteId the siteId to use when checking if content exists
	 */
	public boolean hasContent(String siteId);
	
}
