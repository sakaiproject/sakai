/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.news.api.NewsChannel;
import org.sakaiproject.news.api.NewsConnectionException;
import org.sakaiproject.news.api.NewsFormatException;
import org.sakaiproject.news.api.NewsItem;
import org.sakaiproject.news.api.NewsItemEnclosure;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.cover.SessionManager;

import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndImage;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;

/***********************************************************************************
 * NewsChannel implementation
 **********************************************************************************/

public class BasicNewsChannel implements NewsChannel
{
	protected String m_source = null;

	protected String m_link = null;

	protected String m_title = null;

	protected String m_description = null;

	protected String m_language = null;

	protected String m_copyright = null;

	protected String m_pubdate = null;

	protected String m_lastbuilddate = null;

	protected String m_imageLink = null;

	protected String m_imageTitle = null;

	protected String m_imageUrl = null;

	protected String m_imageHeight = "31";

	protected String m_imageWidth = "88";

	protected String m_imageDescription = null;

	protected List<NewsItem> m_items = null;

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(BasicNewsService.class);
	
	private ResourceLoader rl = new ResourceLoader("news-impl");

	
	/**
	 * Construct.
	 * 
	 * @param source
	 *        The URL from which the feed can be obtained
	 * @exception NewsConnectionException,
	 *            for errors making the connection.
	 * @exception NewsFormatException,
	 *            for errors in the URL or errors parsing the feed.
	 */
	public BasicNewsChannel(String source) throws NewsConnectionException, NewsFormatException
	{
		if (m_items == null)
		{
			m_items = new Vector<NewsItem>();
		}

		// get the file, parse it and cache it
		// throw NewsConnectionException if unable to get file
		// throw NewsFormatException if file is in wrong format
		initChannel(source);
	}


	// get the file, parse it and cache it
	// throw NewsConnectionException if unable to get file
	// throw NewsFormatException if file is in wrong format
	// updated to use ROME syndication api
	private void initChannel(String source) throws NewsConnectionException, NewsFormatException
	{
		SyndFeed feed = null;

		try
		{
			// if feedUrl is on this sakai server, add session parameter
			if ( source.startsWith( ServerConfigurationService.getServerUrl() ) )
			{
				String sessionId = SessionManager.getCurrentSession().getId();
				if ( source.indexOf("?")!= -1 )
					source = source+"&session="+sessionId;
				else
					source = source+"?session="+sessionId;
			}
			
			URL feedUrl = new URL(source);
			FeedFetcher feedFetcher = new HttpURLFeedFetcher();
			feed = feedFetcher.retrieveFeed(feedUrl);
		}
		catch (MalformedURLException e)
		{
			if (M_log.isDebugEnabled()) M_log.debug("initChannel(" + source + ") bad url: " + e.getMessage());
			throw new NewsFormatException("\"" + source + "\" " + rl.getString("is_not_a_valid_url"));
		}
		catch (IOException e)
		{
			if (M_log.isDebugEnabled())
				M_log.debug("initChannel(" + source + ") constructor: couldn't connect: " + e.getMessage());
			throw new NewsConnectionException( rl.getString("unable_to_obtain_news_feed") + " " + source);
		}
		catch (Exception e)
		{
			M_log.info("initChannel(" + source + ") constructor: couldn't parse: " + e.getMessage());
			throw new NewsConnectionException(rl.getString("unable_to_interpret") +" " + source);
		}

		m_title = FormattedText.processEscapedHtml(feed.getTitle());
		m_source = source;
		m_description = FormattedText.processEscapedHtml(feed.getDescription());
		m_description = Validator.stripAllNewlines(m_description);

		m_lastbuilddate = "";
		m_pubdate = "";
		Date pubdate = feed.getPublishedDate();
		if (pubdate != null)
		{
			m_pubdate = FormattedText.processEscapedHtml(DateFormat.getDateInstance().format(pubdate));
			m_lastbuilddate = m_pubdate;
		}
		m_pubdate = Validator.stripAllNewlines(m_pubdate);
		m_lastbuilddate = Validator.stripAllNewlines(m_lastbuilddate);

		m_copyright = FormattedText.processEscapedHtml(feed.getCopyright());
		m_copyright = Validator.stripAllNewlines(m_copyright);

		m_language = FormattedText.processEscapedHtml(feed.getLanguage());
		m_language = Validator.stripAllNewlines(m_language);

		m_link = FormattedText.processEscapedHtml(feed.getLink());
		m_link = Validator.stripAllNewlines(m_link);

		SyndImage image = feed.getImage();
		if (image != null)
		{
			m_imageLink = FormattedText.processEscapedHtml(image.getLink());
			m_imageLink = Validator.stripAllNewlines(m_imageLink);

			m_imageTitle = FormattedText.processEscapedHtml(image.getTitle());
			m_imageTitle = Validator.stripAllNewlines(m_imageTitle);

			m_imageUrl = FormattedText.processEscapedHtml(image.getUrl());
			m_imageUrl = Validator.stripAllNewlines(m_imageUrl);

			m_imageHeight = "";

			m_imageWidth = "";

			m_imageDescription = FormattedText.processEscapedHtml(image.getDescription());
			m_imageDescription = Validator.stripAllNewlines(m_imageDescription);

		}
		// others??
		m_items = new Vector<NewsItem>();

		List items = feed.getEntries();

		for (int i = 0; i < items.size(); ++i)
		{
			SyndEntry entry = (SyndEntry) items.get(i);

			String iTitle = FormattedText.processEscapedHtml(entry.getTitle());
			iTitle = Validator.stripAllNewlines(iTitle);

			String iDescription = null;
			try
			{
				if (entry.getDescription() != null)
				{
					iDescription = FormattedText.processEscapedHtml(
						entry.getDescription().getValue());
					iDescription = Validator.stripAllNewlines(iDescription);
				}
			}
			catch (Exception e)
			{
				M_log.warn(e);
			}

			String iLink = FormattedText.processEscapedHtml(entry.getLink());
			iLink = Validator.stripAllNewlines(iLink);
			String iPubDate = "";
			Date entrydate = entry.getPublishedDate();
			if (entrydate != null)
			{
				iPubDate = FormattedText.processEscapedHtml(
				             DateFormat.getDateInstance().format(entrydate));
			}
			
			List<NewsItemEnclosure> enclosures = new Vector<NewsItemEnclosure>();
			List syndEnclosures = entry.getEnclosures();

			for (int j = 0; j < syndEnclosures.size(); j++)
			{
				SyndEnclosure syndEnclosure = (SyndEnclosure) syndEnclosures.get(j);

				enclosures.add(new BasicNewsItemEnclosure(
								FormattedText.processEscapedHtml(syndEnclosure.getUrl()), 
								syndEnclosure.getType(), syndEnclosure.getLength()));

			}
			iPubDate = Validator.stripAllNewlines(iPubDate);
			m_items.add(new BasicNewsItem(iTitle, iDescription, iLink, iPubDate, enclosures));
		}
	} // initChannel

	/**
	 * A .
	 * 
	 * @return the NewsItem that has the specified id.
	 */
	public List getNewsitems()
	{
		List<NewsItem> rv = new Vector<NewsItem>();
		rv.addAll(m_items);

		return rv;

	} // getNewsitems

	/**
	 * A .
	 * 
	 * @param filter
	 *        A filtering object to accept messages, or null if no filtering is desired.
	 * @return a list of NewsItems objects (may be empty).
	 */
	public List getNewsitems(Filter filter)
	{
		List items = new Vector<NewsItem>(m_items);
		if (filter != null)
		{
			List<NewsItem> accepted = new Vector<NewsItem>();
			Iterator it = items.iterator();
			while (it.hasNext())
			{
				NewsItem item = (NewsItem) it.next();
				if (filter.accept(item))
				{
					accepted.add(item);
				}
			}
			items = accepted;
		}
		return items;
	}

	public String getSource()
	{
		return m_source;
	}

	public String getLink()
	{
		return m_link;
	}

	public String getTitle()
	{
		return m_title;
	}

	public String getDescription()
	{
		return m_description;
	}

	public String getLanguage()
	{
		return m_language;
	}

	public String getCopyright()
	{
		return m_copyright;
	}

	public String getPubdate()
	{
		return m_pubdate;
	}

	public String getLastbuilddate()
	{
		return m_lastbuilddate;
	}

	public String getImageUrl()
	{
		return m_imageUrl;
	}

	public String getImageTitle()
	{
		return m_imageTitle;
	}

	public String getImageLink()
	{
		return m_imageLink;
	}

	public String getImageWidth()
	{
		return m_imageWidth;
	}

	public String getImageHeight()
	{
		return m_imageHeight;
	}

	public String getImageDescription()
	{
		return m_imageDescription;
	}

	public void setNewsitems(List items)
	{
		m_items = new Vector(items);
	}

	public void addNewsitem(NewsItem item)
	{
		m_items.add(item);
	}

	public void setSource(String source) throws NewsConnectionException, NewsFormatException
	{
		m_source = source;
		initChannel(source);
	}

	public void setLink(String link)
	{
		m_link = link;
	}

	public void setTitle(String title)
	{
		m_title = title;
	}

	public void setDescription(String description)
	{
		m_description = description;
	}

	public void setLanguage(String language)
	{
		m_language = language;
	}

	public void setCopyright(String copyright)
	{
		m_copyright = copyright;
	}

	public void setPubdate(String pubdate)
	{
		m_pubdate = pubdate;
	}

	public void setLastbuilddate(String builddate)
	{
		m_lastbuilddate = builddate;
	}

	public void setImageUrl(String imageUrl)
	{
		m_imageUrl = imageUrl;
	}

	public void setImageTitle(String imageTitle)
	{
		m_imageTitle = imageTitle;
	}

	public void setImageLink(String imageLink)
	{
		m_imageLink = imageLink;
	}

	public void setImageWidth(String imageWidth)
	{
		m_imageWidth = imageWidth;
	}

	public void setImageHeight(String imageHeight)
	{
		m_imageHeight = imageHeight;
	}

	public void setImageDescription(String imageDescription)
	{
		m_imageDescription = imageDescription;
	}

	/**
	 * Checks whether an update is available for the rss news feed.
	 * 
	 * @return true if update is available, false otherwise
	 */
	public boolean isUpdateAvailable()
	{
		// %%%%%%%%
		return true;
	}

	/**
	 * Checks the relative ordering of the String url's of two Channels. Same 
	 * response pattern as compareTo method for Strings--negative if "this" 
	 * object is greater than parameter, zero if the objects are equal, and 
	 * positive if "this" object is less than the parameter. The parameter 
	 * can be a String reference or a NewsChannel object (otherwise method 
	 * throws ClassCastException).
	 * 
	 * @return A negative integer if "this" object is greater than parameter, 
	 * zero if the objects are equal, and a positive integer if "this" object 
	 * is less than the parameter
	 */
	public int compareTo(Object obj) throws ClassCastException
	{
		int rv = 0;
		if (m_source == null)
		{
			if (obj != null)
			{
				rv = -1;
			}
		}
		else if (obj == null)
		{
			rv = 1;
		}
		else if (obj instanceof String)
		{
			rv = m_source.compareTo((String) obj);
		}
		else
		{
			NewsChannel other = (NewsChannel) obj;
			rv = m_source.compareTo(other.getLink());
		}
		return rv;
	}

	/**
	 * Checks whether the parameter obj refers to the same channel as "this" channel.
	 *  The parameter can be a String URL or a NewsChannel object (otherwise method 
	 *  throws ClassCastException).
	 * 
	 * @return true if the channels are the same, false otherwise
	 */
	public boolean equals(Object obj) throws ClassCastException
	{
		return (compareTo(obj) == 0);
	}

	/**
	 * Calculates a hash code for the channel object's URL.
	 * 
	 * @return The hash-code for the String URL to the channel.
	 */
	public int hashCode()
	{
		String hval = "";
		if (m_source != null)
		{
			hval = m_source;
		}

		return hval.hashCode();
	}

} // BasicNewsChannel
