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
import java.util.Date;

import org.sakaiproject.javax.Filter;

/**
 * <p>
 * NewsChannel is the interface for a Sakai News service News channel. Messages in the NewsChannel are NewsItems.
 * </p>
 */
public interface NewsChannel
{
	/**
	 * Accesses a list of all news items from this rss news feed.
	 * 
	 * @return a list of NewsItem objects (may be empty).
	 */
	public List getNewsitems();

	/**
	 * Accesses a filtered list of news items from this rss news feed.
	 * 
	 * @param filter
	 *        A filtering object to accept messages, or null if no filtering is desired.
	 * @return a list of NewsItem objects (may be empty).
	 */
	public List getNewsitems(Filter filter);

	/**
	 *
	 */
	public String getLink();

	/**
	 *
	 */
	public String getSource();

	/**
	 *
	 */
	public String getTitle();

	/**
	 *
	 */
	public String getDescription();

	/**
	 *
	 */
	public String getLanguage();

	/**
	 *
	 */
	public String getCopyright();

	/**
	 *
	 */
	public String getPubdate();
	public Date getPubdateInDateFormat();

	/**
	 *
	 */
	public String getLastbuilddate();
	public Date getLastbuilddateInDateFormat();

	/**
	 *
	 */
	public void setNewsitems(List items);

	/**
	 *
	 */
	public void addNewsitem(NewsItem item);

	/**
	 *
	 */
	public void setLink(String link);

	/**
	 *
	 */
	public void setSource(String source) throws NewsConnectionException, NewsFormatException;

	/**
	 *
	 */
	public void setTitle(String set);

	/**
	 *
	 */
	public void setDescription(String description);

	/**
	 *
	 */
	public void setLanguage(String language);

	/**
	 *
	 */
	public void setCopyright(String copyright);

	/**
	 *
	 */
	public void setPubdate(String pubdate);

	/**
	 *
	 */
	public void setLastbuilddate(String builddate);

	/**
	 * Checks whether an update is available for the rss news feed.
	 * 
	 * @return true if update is available, false otherwise
	 */
	public boolean isUpdateAvailable();

	/**
	 * Checks the relative ordering of the String url's of two Channels. Same response pattern as compareTo method for Strings--negative if "this" object is greater than parameter, zero if the objects are equal, and positive if "this" object is less than
	 * the parameter. The parameter can be a String reference or a MessageChannel object (otherwise method throws ClassCastException).
	 * 
	 * @return A negative integer if "this" object is greater than parameter, zero if the objects are equal, and a positive integer if "this" object is less than the parameter
	 */
	public int compareTo(Object obj) throws ClassCastException;

	/**
	 * Checks whether the parameter obj refers to the same channel as "this" channel. The parameter can be a String URL or a NewsChannel object (otherwise method throws ClassCastException).
	 * 
	 * @return true if the channels are the same, false otherwise
	 */
	public boolean equals(Object obj) throws ClassCastException;

	/**
	 * Calculates a hash code for the channel object's URL.
	 * 
	 * @return The hash-code for the String URL to the channel.
	 */
	public int hashCode();
}