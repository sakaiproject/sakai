/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.news.api.NewsChannel;
import org.sakaiproject.news.api.NewsConnectionException;
import org.sakaiproject.news.api.NewsFormatException;
import org.sakaiproject.news.api.NewsItem;
import org.sakaiproject.news.api.NewsService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.Validator;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndImage;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;


/**
 * <p>
 * BasicNewsService implements the NewsService using the Rome RSS package.
 * </p>
 */
public class BasicNewsService implements NewsService, EntityTransferrer
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(BasicNewsService.class);
	
	private static final String TOOL_ID = "sakai.news";
	
	private static final String NEWS = "news";
	private static final String NEWS_ITEM = "news_item";
	private static final String NEWS_URL = "url";
	private static final String TOOL_TITLE = "tool_title";
	private static final String PAGE_TITLE = "page_title";
	private static final String ARCHIVE_VERSION = "2.4"; // in case new features are added in future exports
	
	private static final String VERSION_ATTR = "version";
	private static final String NEWS_URL_PROP = "channel-url";
	
	public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh";

	// default expiration is 10 minutes (expressed in seconds)
	protected static final int DEFAULT_EXPIRATION = 10 * 60;

	// The cache in which channels and news-items are held
	protected Cache m_storage = null;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Dependency: MemoryService. */
	protected MemoryService m_memoryService = null;

	/**
	 * Dependency: MemoryService.
	 * 
	 * @param service
	 *        The MemoryService.
	 */
	public void setMemoryService(MemoryService service)
	{
		m_memoryService = service;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			M_log.info("init()");

			m_storage = m_memoryService.newCache();
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
		
		// register as an entity producer
		EntityManager.registerEntityProducer(this, REFERENCE_ROOT);

	} // init

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		m_storage.destroy();
		m_storage = null;

		M_log.info("destroy()");
	}

	/**
	 * <p>
	 * Permits safe tags but escapes other tags.
	 * </p>
	 * 
	 * @param in
	 *        The String to process.
	 * @return text that is safe to display in a browser window.
	 */
	protected String getSafeHtml(String in)
	{
		String safeHtml = "";
		if (in == null) return safeHtml;
		if (in.equals("")) return safeHtml;
		try
		{
			safeHtml = FormattedText.processEscapedHtml(in);
		}
		catch (Exception e)
		{
			M_log.warn("getSafeHtml() " + e);
		}
		return safeHtml;
	}

	/**
	 * <p>
	 * Checks whether channel is cached. If not or if it's expired, retrieve the feed and update the cache
	 * </p>
	 * 
	 * @param source
	 *        The url for the news feed
	 * @exception NewsConnectionException,
	 *            for errors making the connection.
	 * @exception NewsFormatException,
	 *            for errors in the URL or errors parsing the feed.
	 */
	protected void updateChannel(String source) throws NewsConnectionException, NewsFormatException
	{
		// if channel is expired or not in cache, attempt to update
		// synchronize this part??? %%%%%%
		if (!m_storage.containsKey(source))
		{
			BasicNewsChannel channel = new BasicNewsChannel(source);
			m_storage.put(source, channel, DEFAULT_EXPIRATION);
		}

	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * NewsItem implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

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

	} // class BasicNewsItemHeader

	/**********************************************************************************************************************************************************************************************************************************************************
	 * NewsChannel implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

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

		protected List m_items = null;

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
				m_items = new Vector();
			}

			// get the file, parse it and cache it
			// throw NewsConnectionException if unable to get file
			// throw NewsFormatException if file is in wrong format
			initChannel(source);
		}

		// from org.apache.jetspeed.portal.portlets.NewRSSPortlet
		private Node getNode(Node start, String name)
		{
			NodeList list = start.getChildNodes();

			for (int i = 0; i < list.getLength(); ++i)
			{

				Node node = list.item(i);

				if (node.getNodeName().equals(name))
				{
					return node;
				}
			}
			return null;
		}

		private String getNodeValue(Node start, String nodename, String defval)
		{
			Node thenode = getNode(start, nodename);
			String rv = defval;
			if (thenode != null)
			{
				try
				{
					Node nugget = thenode.getFirstChild();
					if (nugget != null)
					{
						rv = nugget.getNodeValue();
					}
				}
				catch (Exception ignore)
				{
				}
			}
			return rv;
		}

		// get the file, parse it and cache it
		// throw NewsConnectionException if unable to get file
		// throw NewsFormatException if file is in wrong format
		// updated to use ROME syndication api
		private void initChannel(String source) throws NewsConnectionException, NewsFormatException
		{
			SyndFeed feed = null;

			// Document document = null;
			try
			{
				URL feedUrl = new URL(source);
				FeedFetcher feedFetcher = new HttpURLFeedFetcher();
				feed = feedFetcher.retrieveFeed(feedUrl);
			    
			}
			catch (MalformedURLException e)
			{
				if (M_log.isDebugEnabled()) M_log.debug("initChannel(" + source + ") bad url: " + e.getMessage());
				throw new NewsFormatException("\"" + source + "\" is not a complete and valid URL.");
			}
			catch (IOException e)
			{
				if (M_log.isDebugEnabled())
					M_log.debug("initChannel(" + source + ") constructor: couldn't connect: " + e.getMessage());
				throw new NewsConnectionException("Unable to obtain news feed from " + source);
			}
			catch (Exception e)
			{
				M_log.info("initChannel(" + source + ") constructor: couldn't parse: " + e.getMessage());
				throw new NewsConnectionException("Unable to interpret news feed from " + source);
			}

			m_title = feed.getTitle();
			m_source = source;
			m_description = feed.getDescription();
			m_description = Validator.stripAllNewlines(m_description);

			m_lastbuilddate = "";
			m_pubdate = "";
			Date pubdate = feed.getPublishedDate();
			if (pubdate != null)
			{
				m_pubdate = DateFormat.getDateInstance().format(pubdate);
				m_lastbuilddate = m_pubdate;
			}
			m_pubdate = Validator.stripAllNewlines(m_pubdate);
			m_lastbuilddate = Validator.stripAllNewlines(m_lastbuilddate);

			m_copyright = feed.getCopyright();
			m_copyright = Validator.stripAllNewlines(m_copyright);

			m_language = feed.getLanguage();
			m_language = Validator.stripAllNewlines(m_language);

			m_link = feed.getLink();
			m_link = Validator.stripAllNewlines(m_link);

			SyndImage image = feed.getImage();
			if (image != null)
			{
				m_imageLink = image.getLink();
				m_imageLink = Validator.stripAllNewlines(m_imageLink);

				m_imageTitle = image.getTitle();
				m_imageTitle = Validator.stripAllNewlines(m_imageTitle);

				m_imageUrl = image.getUrl();
				m_imageUrl = Validator.stripAllNewlines(m_imageUrl);

				m_imageHeight = "";

				m_imageWidth = "";

				m_imageDescription = image.getDescription();
				m_imageDescription = Validator.stripAllNewlines(m_imageDescription);

			}
			// others??
			m_items = new Vector();

			List items = feed.getEntries();

			for (int i = 0; i < items.size(); ++i)
			{
				SyndEntry entry = (SyndEntry) items.get(i);

				String iTitle = entry.getTitle();
				iTitle = Validator.stripAllNewlines(iTitle);

				String iDescription = entry.getDescription().getValue();
				iDescription = getSafeHtml(iDescription);
				iDescription = Validator.stripAllNewlines(iDescription);

				String iLink = entry.getLink();
				iLink = Validator.stripAllNewlines(iLink);
				String iPubDate = "";
				Date entrydate = entry.getPublishedDate();
				if (entrydate != null)
				{
					iPubDate = DateFormat.getDateInstance().format(entrydate);
				}
				
				List enclosures = new Vector();
				List syndEnclosures = entry.getEnclosures();

				for (int j = 0; j < syndEnclosures.size(); j++) {
					SyndEnclosure syndEnclosure = (SyndEnclosure) syndEnclosures.get(j);

					enclosures.add(new BasicNewsItemEnclosure(syndEnclosure.getUrl(), 
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
			List rv = new Vector();
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
			List items = new Vector(m_items);
			if (filter != null)
			{
				List accepted = new Vector();
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
		 * Checks the relative ordering of the String url's of two Channels. Same response pattern as compareTo method for Strings--negative if "this" object is greater than parameter, zero if the objects are equal, and positive if "this" object is less
		 * than the parameter. The parameter can be a String reference or a NewsChannel object (otherwise method throws ClassCastException).
		 * 
		 * @return A negative integer if "this" object is greater than parameter, zero if the objects are equal, and a positive integer if "this" object is less than the parameter
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
		 * Checks whether the parameter obj refers to the same channel as "this" channel. The parameter can be a String URL or a NewsChannel object (otherwise method throws ClassCastException).
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

	/**********************************************************************************************************************************************************************************************************************************************************
	 * NewsService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Retrieves a list of items from an rss feed.
	 * 
	 * @param source
	 *        The url for the feed.
	 * @return A list of NewsItem objects retrieved from the feed.
	 * @exception NewsConnectionException,
	 *            for errors making the connection.
	 * @exception NewsFormatException,
	 *            for errors in the URL or errors parsing the feed.
	 */
	public List getNewsitems(String source) throws NewsConnectionException, NewsFormatException
	{
		// if channel is expired or not in cache, attempt to update
		updateChannel(source);

		// return a list of items from the channel
		Object obj = m_storage.get(source);
		NewsChannel ch = (NewsChannel) obj;
		List items = ch.getNewsitems();
		return items;
	}

	/**
	 * Retrieves a list of items from an rss feed.
	 * 
	 * @param source
	 *        The url for the feed.
	 * @param filter
	 *        A filtering object to accept NewsItems, or null if no filtering is desired.
	 * @return A list of NewsItem objects retrieved from the feed.
	 * @exception NewsConnectionException,
	 *            for errors making the connection.
	 * @exception NewsFormatException,
	 *            for errors in the URL or errors parsing the feed.
	 */
	public List getNewsitems(String source, Filter filter) throws NewsConnectionException, NewsFormatException
	{
		// if channel is expired or not in cache, attempt to update
		updateChannel(source);

		// 
		List items = ((NewsChannel) m_storage.get(source)).getNewsitems(filter);
		return items;

	} // getNewsitems

	/**
	 * Checks whether an update is available for the rss news feed.
	 * 
	 * @param source
	 *        The url for the feed.
	 * @return true if update is available, false otherwise
	 * @exception NewsConnectionException,
	 *            for errors making the connection.
	 * @exception NewsFormatException,
	 *            for errors in the URL or errors parsing the feed.
	 */
	public boolean isUpdateAvailable(String source)
	{
		// %%%%%
		return true;
	}

	/**
	 * Retrieves a list of URLs for NewsChannel objects currently indexed by the service.
	 * 
	 * @return A list of NewsChannel objects (possibly empty).
	 */
	public List getChannels()
	{
		return m_storage.getKeys();
	}

	/**
	 * Retrieves a NewsChannel object indexed by a URL.
	 * 
	 * @param source
	 *        The url for the channel.
	 * @return A NewsChannel object (possibly null).
	 * @exception NewsConnectionException,
	 *            for errors making the connection.
	 * @exception NewsFormatException,
	 *            for errors in the URL or errors parsing the feed.
	 */
	public NewsChannel getChannel(String source) throws NewsConnectionException, NewsFormatException
	{
		updateChannel(source);
		return (NewsChannel) m_storage.get(source);
	}

	/**
	 * Removes a NewsChannel object from the service.
	 * 
	 * @param source
	 *        The url for the channel.
	 */
	public void removeChannel(String source)
	{
		m_storage.remove(source);
	}
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Import/Export
	 *********************************************************************************************************************************************************************************************************************************************************/
	/**
	 * {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref)
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willImport()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entity getEntity(Reference ref)
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport)
	{
		M_log.info("merge starts for News...");
		if (siteId != null && siteId.trim().length() > 0)
		{
			try
			{
				Site site = SiteService.getSite(siteId);
				NodeList allChildrenNodes = root.getChildNodes();
				int length = allChildrenNodes.getLength();
				for (int i = 0; i < length; i++)
				{
					Node siteNode = allChildrenNodes.item(i);
					if (siteNode.getNodeType() == Node.ELEMENT_NODE)
					{
						Element siteElement = (Element) siteNode;
						if (siteElement.getTagName().equals(NEWS))
						{
							NodeList allContentNodes = siteElement.getChildNodes();
							int lengthContent = allContentNodes.getLength();
							for (int j = 0; j < lengthContent; j++)
							{
								Node child1 = allContentNodes.item(j);
								if (child1.getNodeType() == Node.ELEMENT_NODE)
								{
									Element contentElement = (Element) child1;
									if (contentElement.getTagName().equals(NEWS_ITEM))
									{
										String toolTitle = contentElement.getAttribute(TOOL_TITLE);
										String trimBody = null;
										if(toolTitle != null && toolTitle.length() >0)
										{
											trimBody = trimToNull(toolTitle);
											if (trimBody != null && trimBody.length() >0)
											{
												byte[] decoded = Base64.decode(trimBody);
												toolTitle = new String(decoded, "UTF-8");
											}
										}

										String pageTitle = contentElement.getAttribute(PAGE_TITLE);
										trimBody = null;
										if(pageTitle != null && pageTitle.length() >0)
										{
											trimBody = trimToNull(pageTitle);
											if (trimBody != null && trimBody.length() >0)
											{
												byte[] decoded = Base64.decode(trimBody);
												pageTitle = new String(decoded, "UTF-8");
											}
										}

										String contentUrl = contentElement.getAttribute(NEWS_URL);
										trimBody = null;
										if(contentUrl != null && contentUrl.length() >0)
										{
											trimBody = trimToNull(contentUrl);
											if (trimBody != null && trimBody.length() >0)
											{
												byte[] decoded = Base64.decode(trimBody);
												contentUrl = new String(decoded, "UTF-8");
											}
										}

										if(toolTitle != null && contentUrl != null && toolTitle.length() >0 && contentUrl.length() >0
												&& pageTitle !=null && pageTitle.length() > 0)
										{
											Tool tr = ToolManager.getTool(TOOL_ID);
											SitePage page = site.addPage(); 
											page.setTitle(pageTitle);
											ToolConfiguration tool = page.addTool();
											tool.setTool(TOOL_ID, tr);
											tool.setTitle(toolTitle);
											tool.getPlacementConfig().setProperty(NEWS_URL_PROP, contentUrl);
										}
									}
								}
							}
						}
					}
				}
				SiteService.save(site);
				ToolSession session = SessionManager.getCurrentToolSession();

				if (session.getAttribute(ATTR_TOP_REFRESH) == null)	
				{
					session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
				}
			}
			catch(Exception e)
			{
				M_log.error("errors in merge for BasicNewsService");
				e.printStackTrace();
			}
		}

		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String archive(String siteId, Document doc, Stack stack, String arg3,
		      List attachments)
	{
		StringBuffer results = new StringBuffer();

		try
		{
			int count = 0;
			results.append("archiving " + getLabel() + " context "
					+ Entity.SEPARATOR + siteId + Entity.SEPARATOR
					+ SiteService.MAIN_CONTAINER + ".\n");
			
			// get the default news url
			String defaultUrl = ServerConfigurationService.getString("news.feedURL");
			
			// start with an element with our very own (service) name
			Element element = doc.createElement(SERVICE_NAME);
			element.setAttribute(VERSION_ATTR, ARCHIVE_VERSION);
			((Element) stack.peek()).appendChild(element);
			stack.push(element);
			if (siteId != null && siteId.trim().length() > 0)
			{
				Element newsEl = doc.createElement(NEWS);

				Site site = SiteService.getSite(siteId);
				List sitePages = site.getPages();

				if (sitePages != null && !sitePages.isEmpty()) 
				{
					Iterator pageIter = sitePages.iterator();
					while (pageIter.hasNext()) 
					{
						SitePage currPage = (SitePage) pageIter.next();

						List toolList = currPage.getTools();
						Iterator toolIter = toolList.iterator();
						while (toolIter.hasNext()) 
						{
							ToolConfiguration toolConfig = (ToolConfiguration)toolIter.next();

							if (toolConfig.getToolId().equals(TOOL_ID)) 
							{
								Element newsData = doc.createElement(NEWS_ITEM);
								count++;
								//There will only be a url property if the user updated the default URL
								String newsUrl = toolConfig.getPlacementConfig().getProperty(NEWS_URL_PROP);		
								if (newsUrl == null || newsUrl.length() <= 0) 
								{
									// news item is using default url
									newsUrl = defaultUrl;
								}
								String toolTitle = toolConfig.getTitle();
								String pageTitle = currPage.getTitle();

								try 
								{
									String encoded = Base64.encode(newsUrl.getBytes());
									newsData.setAttribute(NEWS_URL, encoded);
								}
								catch(Exception e) 
								{
									M_log.warn("Encode News URL - " + e);
								}

								try 
								{
									String encoded = Base64.encode(toolTitle.getBytes());
									newsData.setAttribute(TOOL_TITLE, encoded);
								}
								catch(Exception e) 
								{
									M_log.warn("Encode News Tool Title - " + e);
								}
								
								try 
								{
									String encoded = Base64.encode(pageTitle.getBytes());
									newsData.setAttribute(PAGE_TITLE, encoded);
								}
								catch(Exception e) 
								{
									M_log.warn("Encode News Page Title - " + e);
								}

								newsEl.appendChild(newsData);
							}
						}		  
					}
					results.append("archiving " + getLabel() + ": (" + count
							+ ") news items archived successfully.\n");
				}

				else 
				{
					results.append("archiving " + getLabel()
							+ ": empty news archived.\n");
				}

				((Element) stack.peek()).appendChild(newsEl);
				stack.push(newsEl);
			}
			stack.pop();

		}
		catch (DOMException e)	
		{
			M_log.error(e.getMessage(), e);
		}
		catch (IdUnusedException e)	
		{
			M_log.error(e.getMessage(), e);
		}
		catch (Exception e)	
		{
			M_log.error(e.getMessage(), e);
		}
		return results.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void transferCopyEntities(String fromContext, String toContext, List ids) 
	{
		M_log.debug("news transferCopyEntities");
		try
		{				
			// retrieve all of the news tools to copy
			Site fromSite = SiteService.getSite(fromContext);
			Site toSite = SiteService.getSite(toContext);
			
			List fromSitePages = fromSite.getPages();

			if (fromSitePages != null && !fromSitePages.isEmpty()) 
			{
				Iterator pageIter = fromSitePages.iterator();
				while (pageIter.hasNext()) 
				{
					SitePage currPage = (SitePage) pageIter.next();

					List toolList = currPage.getTools();
					Iterator toolIter = toolList.iterator();
					while (toolIter.hasNext()) 
					{
						ToolConfiguration toolConfig = (ToolConfiguration)toolIter.next();
						String toolId =  toolConfig.getToolId();

						if (toolId.equals(TOOL_ID)) 
						{
							String newsUrl = toolConfig.getPlacementConfig().getProperty(NEWS_URL_PROP);
							String toolTitle = toolConfig.getTitle();
							String pageTitle = currPage.getTitle();

							if(toolTitle != null && toolTitle.length() >0 && pageTitle !=null && pageTitle.length() > 0) 
							{
								Tool tr = ToolManager.getTool(TOOL_ID);
								SitePage page = toSite.addPage(); 
								page.setTitle(pageTitle);
								ToolConfiguration tool = page.addTool();
								tool.setTool(TOOL_ID, tr);
								tool.setTitle(toolTitle);
								if (newsUrl != null) 
								{   
									tool.getPlacementConfig().setProperty(NEWS_URL_PROP, newsUrl);
								}
							}
						}
					}
				}
			}
			SiteService.save(toSite);
			ToolSession session = SessionManager.getCurrentToolSession();

			if (session.getAttribute(ATTR_TOP_REFRESH) == null)	
			{
				session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
			}
		}
		catch (Exception any) 
		{
			M_log.warn("transferCopyEntities(): exception in handling news data: ", any);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
	    return "news";
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref)
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String[] myToolIds()
	{
		String[] toolIds = { TOOL_ID };
		return toolIds;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		return false;
	}
	
	public String trimToNull(String value)
	{
		if (value == null) return null;
		value = value.trim();
		if (value.length() == 0) return null;
		return value;
	}
}
