/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.news.api;

import java.util.List;

import org.sakaiproject.javax.Filter;
import org.sakaiproject.entity.api.EntityProducer;

/**
 * <p>
 * NewsService is the interface for retrieving and caching news items from a rss news feed.
 * </p>
 */
public interface NewsService extends EntityProducer
{
	/** This string can be used to find the service in the service manager. */
	public static final String SERVICE_NAME = NewsService.class.getName();

	/** This string starts the references to resources in this service. */
	public static final String REFERENCE_ROOT = "/news";

	/**
	 * Retrieves a list of rss feeds that are being used.
	 * 
	 * @return A list of NewsChannel objects (possibly empty).
	 */
	public List getChannels();

	/**
	 * Retrieves a NewsChannel object indexed by a URL.
	 * 
	 * @param source
	 *        The url for the channel.
	 * @return A NewsChannel object (possibly null).
	 * @throws NewsConnectionException
	 * @throws NewsFormatException
	 */
	public NewsChannel getChannel(String source) throws NewsConnectionException, NewsFormatException;

	/**
	 * Retrieves a list of rss feeds that are being used.
	 * 
	 * @return A list of NewsChannel objects (possibly empty).
	 * @throws ?
	 *         if param channel is not a valid url.
	 */
	public void removeChannel(String channel);

	/**
	 * Retrieves a list of items from an rss feed.
	 * 
	 * @param channel
	 *        The url for the feed.
	 * @return A list of NewsItem objects retrieved from the feed.
	 * @throws ?
	 *         if param feed is not a valid url.
	 */
	public List getNewsitems(String channel) throws NewsConnectionException, NewsFormatException;

	/**
	 * Retrieves a list of items from an rss feed.
	 * 
	 * @param channel
	 *        The url for the feed.
	 * @param filter
	 *        A filtering object to accept NewsItems, or null if no filtering is desired.
	 * @return A list of NewsItem objects retrieved from the feed.
	 * @throws ?
	 *         if param feed is not a valid url.
	 */
	public List getNewsitems(String channel, Filter filter) throws NewsConnectionException, NewsFormatException;

	/**
	 * Checks whether an update is available for the rss news feed.
	 * 
	 * @param feed
	 *        The url for the feed.
	 * @return true if update is available, false otherwise
	 * @throws ?
	 *         if param feed is not a valid url.
	 */
	public boolean isUpdateAvailable(String channel);
}
