/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/tinyurl/trunk/api/src/java/org/sakaiproject/tinyurl/api/TinyUrlService.java $
 * $Id: TinyUrlService.java 64400 2009-11-03 13:21:08Z steve.swinsburg@gmail.com $
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

package org.sakaiproject.shortenedurl.api;


/**
 * This is the base service which all URL shortening services must implement
 * 
 * You can specify the URL shortening scheme in sakai.properties
 * 
 * @author Steve Swinsburg (s.swinsburg@gmail.com)
 *
 */
public interface ShortenedUrlService {

	/**
	 * Event fired if URL translation is ok
	 */
	public final String EVENT_GET_URL_OK = "shortenedurl.get.ok";
	
	/**
	 * Event fired if URL does not exist
	 */
	public final String EVENT_GET_URL_BAD = "shortenedurl.get.bad";
	
	/**
	 * Event fired if shortened URL is created ok
	 */
	public final String EVENT_CREATE_OK = "shortenedurl.create.ok";
	
	/**
	 * Event fired if a shortened URL is requested to be created, but one for this URL already exists
	 */
	public final String EVENT_CREATE_EXISTS = "shortenedurl.create.exists";
	
	/**
	 * Event fired if a shortened URL is created but it collides with an existing one for a different URL
	 */
	public final String EVENT_CREATE_COLLISION = "shortenedurl.create.collision";
	
	/**
	 * The name of the property in sakai.properties. THe value of this is the implementation which is used preferentially
	 */
	public final String IMPLEMENTATION_PROP_NAME = "shortenedurl.implementation";
	
	/**
	 * The default implementation of this service as a Spring bean reference 
	 */
	public final String DEFAULT_IMPLEMENTATION = "org.sakaiproject.shortenedurl.api.NoOpUrlService";

	/**
	 * Provide an implementation of this which generates a shortened URL given some other URL
	 * @param url
	 * @return the full shortened URL
	 */
	public String shorten(String url);
	
	
	/**
	 * Provide an implementation of this which generates a shortened URL given some other URL.
	 * <p>This method also provides an optional flag which you can use to make your URL more secure.
	 * @param url
	 * @return the full shortened URL
	 */
	public String shorten(String url, boolean secure);
	
	
	/**
	 * Provide an implementation of this to get the original URL for the given shortened URL.
	 * 
	 * @param key - the shortened key
	 * @return the original URL that maps to this key
	 */
	public String resolve(String key);
	
	/**
	 * Specify whether the short form of the URL should be placed in the transversal map
	 * when duplicating sites. If so, SiteAction will call shorten on the URL. Depending
	 * upon the implementation, it may not be desirable to call shorten once for each
	 * tool, so implementations may choose to return false all the time, or return true
	 * only if there is reason to think that this specific URL has been shortened.
	 *
	 * @param url
	 * @return true if this URL should be shortened by SiteAction in duplicating sites
	 */
	public boolean shouldCopy(String url);	
	
}
