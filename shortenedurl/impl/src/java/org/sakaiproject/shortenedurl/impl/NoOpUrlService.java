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

import org.sakaiproject.shortenedurl.api.ShortenedUrlService;

/**
 * This is the default implementation of {@link org.sakaiproject.shortenedurl.api.ShortenedUrlService}. It returns the original URL unchanged.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class NoOpUrlService implements ShortenedUrlService {

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
