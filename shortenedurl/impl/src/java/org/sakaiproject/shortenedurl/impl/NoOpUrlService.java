package org.sakaiproject.shortenedurl.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;

/**
 * This is the default implementation of {@link org.sakaiproject.shortenedurl.api.ShortenedUrlService}. It returns the original URL unchanged.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class NoOpUrlService implements ShortenedUrlService {

	private static Logger log = LoggerFactory.getLogger(NoOpUrlService.class);
	
	public String shorten(String url) {
		log.info("NoOpUrlService returning original url");
		return url;
	}

	public String shorten(String url, boolean secure) {
		log.info("NoOpUrlService returning original url");
		return url;
	}

	public String resolve(String key) {
		log.info("NoOpUrlService - no implementation, returning null");
		return null;
	}
	
	public void init() {
  		log.debug("Sakai NoOpUrlService init().");
  	}

	/**
	 * since the short URL is the same as the original, there's no
	 * point in adding it to the transversal map along with the original
	 */
	public boolean shouldCopy(String url) {
		return false;
	}
}
