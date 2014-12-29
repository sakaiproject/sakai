package org.sakaiproject.shortenedurl.entityprovider;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * EntityProvider interface for the ShortenedUrlService to allow URL shortening via GET requests.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface ShortenedUrlServiceEntityProvider extends EntityProvider {

	public final static String ENTITY_PREFIX = "url";
	
}