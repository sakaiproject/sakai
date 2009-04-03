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

package org.sakaiproject.news.impl;

import java.util.List;

import org.sakaiproject.news.api.NewsItem;

/***********************************************************************************
 * NewsItem implementation
 **********************************************************************************/

public class BasicNewsItem implements NewsItem
{
	/** The title of this NewsItem. */
	protected String m_title = null;

	/** The URL for the complete story. */
	protected String m_link = null;

	/** The publication date of the NewsItem. */
	protected String m_pubdate = null;

	/** The description (or body) of the news item */
	protected String m_description = null;

	/** The list of NewsItemEnclosures for this item. */
	protected List m_enclosures = null;
	
	/**
	 * Construct.
	 * 
	 * @param title
	 *        The headline of the item
	 * @param description
	 *        The body of the item
	 * @param link
	 *        The URL for a longer version of the item
	 * @param pubdate
	 *        The date/time at which the item was published
	 * @param enclosure
	 *        The list of NewsItemEnclosures for this item
	 */
	public BasicNewsItem(String title, String description, String link, String pubdate, List enclosures)
	{
		m_title = title;
		m_description = description;
		m_link = link;
		m_pubdate = pubdate;
		m_enclosures = enclosures;

	} // BasicNewsItem

	/**
	 * Construct.
	 * 
	 * @param title
	 *        The headline of the item
	 * @param description
	 *        The body of the item
	 * @param link
	 *        The URL for a longer version of the item
	 * @param pubdate
	 *        The date/time at which the item was published
	 */
	public BasicNewsItem(String title, String description, String link, String pubdate)
	{
		m_title = title;
		m_description = description;
		m_link = link;
		m_pubdate = pubdate;

	} // BasicNewsItem

	
	/**
	 * Access the title of the NewsItem.
	 * 
	 * @return The title of the NewsItem.
	 */
	public String getTitle()
	{
		return m_title;

	} // getTitle

	/**
	 * Access the time when the NewsItem was updated.
	 * 
	 * @return The time when the NewsItem was updated.
	 */
	public String getPubdate()
	{
		return m_pubdate;

	} // getPubdate

	/**
	 * Access the URL where the complete story can be found.
	 * 
	 * @return The URL where the complete story can be found.
	 */
	public String getLink()
	{
		return m_link;

	} // getLink
	
	/**
	 * Access the List of Enclosures for the item
	 * 
	 * @return the List of Enclosures for the item
	 */
	public List getEnclosures()
	{
		return m_enclosures;
	} // getEnclosures

	/**
	 * Access the description (or body) of the NewsItem.
	 * 
	 * @return The description (or body) of the NewsItem.
	 */
	public String getDescription()
	{
		return m_description;
	}

	/**
	 * Set the title of the NewsItem.
	 * 
	 * @param title
	 *        The title of the NewsItem.
	 */
	public void setTitle(String title)
	{
		m_title = title;

	} // setTitle

	/**
	 * Set the time when the NewsItem was updated.
	 * 
	 * @param pubdate
	 *        The time when the NewsItem was updated.
	 */
	public void setPubdate(String pubdate)
	{
		m_pubdate = pubdate;

	} // setPubdate

	/**
	 * Set the URL where the complete story can be found.
	 * 
	 * @param link
	 *        The URL where the complete story can be found.
	 */
	public void setLink(String link)
	{
		m_link = link;

	} // setLink

	/**
	 * Set the description (or body) of the NewsItem.
	 * 
	 * @param description
	 *        The description (or body) of the NewsItem.
	 */
	public void setDescription(String description)
	{
		m_description = description;
	}

} // class BasicNewsItem

