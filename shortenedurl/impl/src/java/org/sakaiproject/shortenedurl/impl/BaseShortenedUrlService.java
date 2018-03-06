/**
 * Copyright (c) 2009-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.shortenedurl.impl;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;

/**
 * This is the base implementing class which delegates all methods to the configured implementation.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class BaseShortenedUrlService implements ShortenedUrlService {

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
