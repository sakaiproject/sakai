/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.news.api.NewsChannel;
import org.sakaiproject.news.api.NewsConnectionException;
import org.sakaiproject.news.api.NewsFormatException;
import org.sakaiproject.news.api.NewsService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
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

			m_storage = m_memoryService
					.newCache("org.sakaiproject.news.api.NewsService.cache");
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
		return ch.getNewsitems();
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
		return ((NewsChannel) m_storage.get(source)).getNewsitems(filter);

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
		Base64 codec = new Base64();
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
												byte[] decoded = codec.decode(trimBody.getBytes("UTF-8"));
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
												byte[] decoded = codec.decode(trimBody.getBytes("UTF-8"));
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
												byte[] decoded = codec.decode(trimBody.getBytes("UTF-8"));
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
		StringBuilder results = new StringBuilder();
        Base64 codec = new Base64();
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
									String encoded = new String(codec.encode(newsUrl.getBytes("UTF-8")),"UTF-8");
									newsData.setAttribute(NEWS_URL, encoded);
								}
								catch(Exception e) 
								{
									M_log.warn("Encode News URL - " + e);
								}

								try 
								{
									String encoded = new String(codec.encode(toolTitle.getBytes("UTF-8")),"UTF-8");
									newsData.setAttribute(TOOL_TITLE, encoded);
								}
								catch(Exception e) 
								{
									M_log.warn("Encode News Tool Title - " + e);
								}
								
								try 
								{
									String encoded = new String(codec.encode(pageTitle.getBytes("UTF-8")),"UTF-8");
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

	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup)
	{	
		try
		{
			if(cleanup == true)
			{
				// retrieve all of the news tools to remove
				Site toSite = SiteService.getSite(toContext);
		
				List toSitePages = toSite.getPages();
				if (toSitePages != null && !toSitePages.isEmpty()) 
				{
					Vector removePageIds = new Vector();
					Iterator pageIter = toSitePages.iterator();
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
								removePageIds.add(toolConfig.getPageId());
							}	
						}	
					}
					for (int i = 0; i < removePageIds.size(); i++)
					{
						String removeId = (String) removePageIds.get(i);
						SitePage sitePage = toSite.getPage(removeId);
						toSite.removePage(sitePage);
					}
				}
				SiteService.save(toSite);
				ToolSession session = SessionManager.getCurrentToolSession();

				if (session.getAttribute(ATTR_TOP_REFRESH) == null)	
				{
					session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
				}
			}
		}
		catch (Exception e)
		{
			M_log.info("News transferCopyEntities Error" + e);
		}
		transferCopyEntities(fromContext, toContext, ids);
	}
}
