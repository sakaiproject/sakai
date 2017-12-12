/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/tinyurl/trunk/impl/src/java/org/sakaiproject/tinyurl/impl/TinyUrlServiceImpl.java $
 * $Id: TinyUrlServiceImpl.java 64964 2009-12-01 00:05:12Z steve.swinsburg@gmail.com $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.shortenedurl.impl;

import java.net.URI;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.type.StringType;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;
import org.sakaiproject.shortenedurl.model.RandomisedUrl;

/**
 * An implementation of {@link org.sakaiproject.shortenedurl.api.ShortenedUrlService} to provide randomised URLs
 * 
 * <p>This implementation stores the shortened key and original URL in a local database table, and uses the resolver servlet to
 * translate the key back to it's original URL.</p>
 * 
 * <p>URLs created are of the form: http://your.sakai.server/x/1w2Kb8
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
@Slf4j
public class RandomisedUrlService extends HibernateDaoSupport implements ShortenedUrlService {

	//Hibernate stored queries
	private static final String QUERY_GET_URL = "getUrl";
	private static final String QUERY_GET_KEY = "getKey";
	
	//Hibernate object fields
	private static final String KEY = "key";
	private static final String URL = "url";
	
	/**
	 * The prefix for URLs created
	 */
	public final String PREFIX = "/x/";
	
	/**
	 * Length of a short key
	 */
	public static final int SHORT = 6;
	
	/**
	 * length of a secure key
	 */
	public static final int SECURE = 22;
	
	private Cache cache;
	private final String CACHE_NAME = "org.sakaiproject.shortenedurl.cache";	
	
	
	/**
	 * Generate a randomised URL for the given URL
	 * Store it and returns it or null if errors
	 * 
	 * <p>
	 * Defaults to short mode where keys are 6 characters long. This should be sufficient for most since the authentication is handled by the container.<br />
	 * If you are passing sensitive information on an unauthenticated URL, you can use {@link #shorten(String url, boolean secure)} to create a longer key.
	 * </p>
	 * 
	 * @param url - the long URL
	 * @return	the shortened URL, or null if errors.
	 */
	public String shorten(String url) {
		
		return shorten(url, false);
	}
	
	/**
	 * Generate a randomised URL for the given URL but with a much longer key (22 chars vs 6 chars).
	 * Store it and return it or null if errors.
	 * 
	 * @param url - the long URL
	 * @param secure - if a longer key is required.
	 * @return the shortened URL, or null if errors.
	 */
	public String shorten(String url, boolean secure) {
		
		//check values
		if(StringUtils.isBlank(url)){
			log.warn("URL was empty, aborting...");
			return null;
		}
		
		
		//check if a key already exists for this url
		String key = getExistingKey(url);
		if(key != null) {
			//log
			log.debug("Returning existing key: " + key);
			
			//post event
			postEvent(ShortenedUrlService.EVENT_CREATE_EXISTS, PREFIX+key, false);
			
			//make actual url and return it
			return generateActualUrl(key);
		}
		
		//or generate a new one
		String newKey = generateKey(secure);
		
		//if not unique, recalculate
		int attempts = 0;
		while (!isKeyUnique(newKey)) {
			//if this is the second or greater pass through, we had a collision. log it.
			if(attempts > 0){
				log.warn("Collision detected for record: " + newKey + " and attempt: " + attempts + ". Regenerating...");
				postEvent(ShortenedUrlService.EVENT_CREATE_COLLISION, newKey, false);
			}
			newKey = generateKey(secure);
			attempts++;
		}
		
		log.debug("Created:" + newKey + " for URL: " + url);
		postEvent(ShortenedUrlService.EVENT_CREATE_OK, PREFIX + newKey, true);
		
		//save 
		if(!saveNewShortenedUrl(newKey, url)) {
			return null;
		}
		
		//make actual url and return it
		return generateActualUrl(newKey);
	}
	
	
	/**
	 * Gets the encoded URL for the given shortened URL.
	 * This is used by the RandomisedUrlService servlet to translate short URLs back into their original URLs. 
	 * 
	 * @param key - the key value, eg 6whjq
	 * @return the original encoded URL mapped to this record or null if errors
	 */
	public String resolve(final String key) {
		
		if (StringUtils.isBlank(key)) {
			return null;
		}

		//first check cache
		String value = (String) cache.get(key);
		if (value != null) {
			return encodeUrl(value);
		}
		
		//then check db
		RandomisedUrl randomisedUrl = null;
		
		HibernateCallback<RandomisedUrl> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_URL);
            q.setParameter(KEY, key, StringType.INSTANCE);
            q.setMaxResults(1);
            return (RandomisedUrl) q.uniqueResult();
      };
	
		//will be either a RandomisedUrl or null
		randomisedUrl = getHibernateTemplate().execute(hcb);
		if(randomisedUrl == null) {
			//log
			log.warn("Request for invalid record: " + key);
			
			//post failure event
			postEvent(ShortenedUrlService.EVENT_GET_URL_BAD, PREFIX+key, false);
			
			return null;
		}
		
		//log
		log.debug("Request for valid record: " + key);
		
		//post success event
		postEvent(ShortenedUrlService.EVENT_GET_URL_OK, PREFIX+key, false);
		
		//add to cache
		String url = randomisedUrl.getUrl();
		addToCache(key, url);

		String encodedUrl = encodeUrl(url);
		if(StringUtils.isBlank(encodedUrl)) {
			return null;
		}
		
		log.debug("URL: " + encodedUrl);
		
		return encodedUrl;
	}
	


	
	
	/**
	 * Checks if a key already exists for a given url, if so returns it else returns null
	 * @param url
	 * @return
	 */
	private String getExistingKey(final String url) {

		if (StringUtils.isBlank(url)) {
			return null;
		}
		
		//first check cache
		String value = (String) cache.get(url);
		if (value != null) {
			return value;
		}
		
		//then check db
		RandomisedUrl randomisedUrl = null;
		
		HibernateCallback<RandomisedUrl> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_KEY);
            q.setParameter(URL, url, StringType.INSTANCE);
            q.setMaxResults(1);
            return (RandomisedUrl) q.uniqueResult();
      };
	
		//will be either a RandomisedUrl or null
		randomisedUrl = getHibernateTemplate().execute(hcb);
		if(randomisedUrl == null) {
			return null;
		}
		
		//add to cache
		String key = randomisedUrl.getKey();
		addToCache(url, key);
	
		return key;
	}
	
	/**
	 * See if we should include this in the transversal map. Do so only
	 * if a mapping exists, since otherwise there can't be any references
	 * to it.
	 */
	public boolean shouldCopy(String url) {
		return (getExistingKey(url) != null);
	}

	/**
	 * Checks if a given key is unique by checking for its existence
	 * @param key
	 * @return
	 */
	private boolean isKeyUnique(final String key) {
		
		RandomisedUrl randomisedUrl = null;
		
		HibernateCallback<RandomisedUrl> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_URL);
            q.setParameter(KEY, key, StringType.INSTANCE);
            q.setMaxResults(1);
            return (RandomisedUrl) q.uniqueResult();
      };
	
		//if null then it doesn't exist
		randomisedUrl = getHibernateTemplate().execute(hcb);
		if(randomisedUrl == null) {
			return true;
		}
		return false;
	}
	
	/**
	 * Generate a random string
	 * @param secure	 if secure, makes it much longer
	 * @return
	 */
	private String generateKey(boolean secure) {
		if(secure){
			return generateSecure();
		} else {
			return generateShort();
		}
	}
	
	/**
	 * Generate a random of RandomisedUrlService.SHORT length
	 * @return
	 */
	private String generateShort() {
		return RandomStringUtils.random(SHORT, true, true);
	}
	
	/**
	 * Generate a random of RandomisedUrlService.SECURE length
	 * @return
	 */
	private String generateSecure() {
		return RandomStringUtils.random(SECURE, true, true);
	}
	
	
	/**
	 * Helper method to generate the final URL that we can use
	 * @param key
	 * @return
	 */
	private String generateActualUrl(final String key) {
		
		//make actual url
		StringBuffer linkUrl = new StringBuffer();
		linkUrl.append(getServerBase());
		linkUrl.append(PREFIX);
		linkUrl.append(key);
		
		//return it
		return linkUrl.toString();
	}
	
	/**
	 * Save entry
	 * @param key
	 * @param url
	 * @return
	 */
	private boolean saveNewShortenedUrl(final String key, final String url) {
		
		try {
			//add to db
			RandomisedUrl randomisedUrl = new RandomisedUrl(key, url);
			getHibernateTemplate().save(randomisedUrl);
			log.debug("RandomisedUrl saved as: " + key);
			
			//and put it in the cache, both ways
			addToCache(key, url);
			addToCache(url, key);
			
			return true;
		} catch (Exception e) {
			log.error("RandomisedUrl save failed. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
	}

  
	/**
	 * get server base URL from sakai.properties
	 * @return
	 */
  	private String getServerBase() {
		return serverConfigurationService.getServerUrl();
	}
  	
  	/**
  	 * Post an event
  	 * 
  	 * @param event		- event id
  	 * @param reference	- reference for event
  	 * @param modify	- true if a modification event, false if not
  	 */
  	public void postEvent(String event,String reference,boolean modify) {
		eventTrackingService.post(eventTrackingService.newEvent(event,reference,modify));
	}
  	
  	/**
  	 * Add data to the cache
  	 * @param k	key
  	 * @param v value
  	 */
  	private void addToCache(String k, String v){
  		log.debug("Added entry to cache, key: " + k +", value: " + v);
		cache.put(k, v);
  	}

  	/**
  	 * Encodes a full URL.
  	 * 
  	 * @param rawUrl the URL to encode.
  	 */
  	private String encodeUrl(String rawUrl) {
  		if (StringUtils.isBlank(rawUrl)) {
  			return null;
  		}
  		String encodedUrl = null;
  		
  		try {
	  		URL url = new URL(rawUrl);
	  		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
	  		encodedUrl = uri.toASCIIString();
  		} catch (Exception e) {
  			log.warn("encoding url: " + rawUrl + ", " + e.getMessage(), e);
		}
  		
  		return encodedUrl;
  	}

  	
  	public void init() {
  		log.debug("Sakai RandomisedUrlService init().");
  		
  		//setup cache
  		cache = memoryService.getCache(CACHE_NAME);
  	}

  	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	private EventTrackingService eventTrackingService;
	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}
	
	private MemoryService memoryService;
	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}

}
