package org.sakaiproject.shortenedurl.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;

/**
 * This is the base implementing class which delegates all methods to the configured implementation.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class BaseShortenedUrlService implements ShortenedUrlService {

	private static Logger log = LoggerFactory.getLogger(BaseShortenedUrlService.class.getName());
	private ShortenedUrlService service;
	
	/**
	 * This init method sets up the implementing class that will be used.
	 */
	public void init() {
		log.info("BaseShortenedUrlService init()");
		String implementingClass = serverConfigurationService.getString(ShortenedUrlService.IMPLEMENTATION_PROP_NAME, ShortenedUrlService.DEFAULT_IMPLEMENTATION);
		service = (ShortenedUrlService) ComponentManager.get(implementingClass);
		log.info("BaseShortenedUrlService init(): Registered implementation: " + implementingClass);
	}
	
	/**
	 * @{inheritDoc}
	 */
	public String shorten(String url) {
		return service.shorten(url);
	}

	/**
	 * @{inheritDoc}
	 */
	public String shorten(String url, boolean secure) {
		return service.shorten(url, secure);
	}

	/**
	 * @{inheritDoc}
	 */
	public String resolve(String key) {
		return service.resolve(key);
	}
	
	/**
	 * @{inheritDoc}
	 */
	public boolean shouldCopy(String url) {
		return service.shouldCopy(url);
	}

	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

}
