/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/web/trunk/web-impl/impl/src/java/org/sakaiproject/web/impl/WebServiceImpl.java$
 * $Id: WebServiceImpl.java 39315 2007-12-15 18:08:26Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.web.impl;

import static org.sakaiproject.tool.api.ToolManager.PORTAL_VISIBLE;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.web.api.WebService;

@Slf4j
public class WebServiceImpl implements WebService, EntityTransferrer
{

	private static final String TOOL_ID = "sakai.iframe";
	
	private static final String WEB_CONTENT = "web_content";
	private static final String REDIRECT_TAB = "redirect_tab";
	private static final String WEB_CONTENT_TITLE = "title";
	private static final String WEB_CONTENT_URL = "url";
	private static final String PAGE_TITLE = "page_title";
	private static final String NEW_WINDOW = "open_in_new_window";
	private static final String ARCHIVE_VERSION = "2.4"; // in case new features are added in future exports
	
	private static final String VERSION_ATTR = "version";
	private static final String WEB_CONTENT_URL_PROP = "source";
	private static final String HEIGHT_PROP = "height";
	private static final String CUSTOM_ICON_PROP = "imsti.fa_icon";
	private static final String SPECIAL_PROP = "special";
	
	public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh";
	
	public void init()
	{
		log.debug("init()");

		// register as an entity producer
		EntityManager.registerEntityProducer(this, REFERENCE_ROOT);

	} // init
	
	
	@Override
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		StringBuilder results = new StringBuilder();

		Base64 codec = new Base64();
		try
		{
			int count = 0;
			results.append("archiving " + getLabel() + " context "
					+ Entity.SEPARATOR + siteId + Entity.SEPARATOR
					+ SiteService.MAIN_CONTAINER + ".\n");
			// start with an element with our very own (service) name
			Element element = doc.createElement(SERVICE_NAME);
			element.setAttribute(VERSION_ATTR, ARCHIVE_VERSION);
			((Element) stack.peek()).appendChild(element);
			stack.push(element);
			if (siteId != null && siteId.trim().length() > 0)
			{
				Element webContentEl = doc.createElement(WEB_CONTENT);

				Site site = SiteService.getSite(siteId);
				List sitePages = site.getPages();

				if (sitePages != null && !sitePages.isEmpty()) {
					Iterator pageIter = sitePages.iterator();
					while (pageIter.hasNext()) {
						SitePage currPage = (SitePage) pageIter.next();

						List toolList = currPage.getTools();
						Iterator toolIter = toolList.iterator();
						while (toolIter.hasNext()) {
							ToolConfiguration toolConfig = (ToolConfiguration)toolIter.next();
							
							// we do not want to archive "special" uses of sakai.iframe, such as worksite info
							String special = toolConfig.getPlacementConfig().getProperty(SPECIAL_PROP);

							if (toolConfig.getToolId().equals(TOOL_ID) && special == null) {
								Element webContentData = doc.createElement(REDIRECT_TAB);
								count++;

								String contentUrl = toolConfig.getPlacementConfig().getProperty(WEB_CONTENT_URL_PROP);
								String toolTitle = toolConfig.getTitle();
								String pageTitle = currPage.getTitle();
								String height = toolConfig.getPlacementConfig().getProperty(HEIGHT_PROP);

								webContentData.setAttribute(NEW_WINDOW, new Boolean(currPage.isPopUp()).toString());

								try {
									String encoded = new String(codec.encode(contentUrl.getBytes("UTF-8")),"UTF-8");
									webContentData.setAttribute(WEB_CONTENT_URL, encoded);
								}
								catch(Exception e) {
									log.warn("Encode Web Content URL - " + e);
								}

								try {
									String encoded = new String(codec.encode(toolTitle.getBytes("UTF-8")),"UTF-8");
									webContentData.setAttribute(WEB_CONTENT_TITLE, encoded);
								}
								catch(Exception e) {
									log.warn("Encode Web Content Tool Title - " + e);
								}

								if (height != null) {
									webContentData.setAttribute(HEIGHT_PROP, height);
								}

								try {
									String encoded = new String(codec.encode(pageTitle.getBytes("UTF-8")),"UTF-8");
									webContentData.setAttribute(PAGE_TITLE, encoded);
								}
								catch(Exception e) {
									log.warn("Encode Web Content Page Title - " + e);
								}

								if (height != null) {
									webContentData.setAttribute(HEIGHT_PROP, height);
								}

								webContentEl.appendChild(webContentData);
							}
						}		  
					}
					results.append("archiving " + getLabel() + ": (" + count
							+ ") web content items archived successfully.\n");
				}

				else {
					results.append("archiving " + getLabel()
							+ ": empty web content archived.\n");
				}

				((Element) stack.peek()).appendChild(webContentEl);
				stack.push(webContentEl);
			}
			stack.pop();

		}
		catch (DOMException e)
		{
			log.error(e.getMessage(), e);
		}
		catch (IdUnusedException e)
		{
			log.error(e.getMessage(), e);
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
		}
		return results.toString();
	}

	@Override
	public String getLabel()
	{
		return "web";
	}

	// As of SAK-50943, this is not used as the import from site.xml captures the page, tool, and tool properties
	// In a sense, looking at the code - this is reading the Site data structures and doing a bad job of it
	// We will still put web.xml into exports in case thye are valuable in some use case - but import
	// will skip web.xml (see org.sakaiproject.archive.impl.SiteMerger.java)
	@Override
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport)
	{
		log.info("merge starts for Web Content...");
		Base64 codec = new Base64();
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
						if (siteElement.getTagName().equals(WEB_CONTENT))
						{
							NodeList allContentNodes = siteElement.getChildNodes();
							int lengthContent = allContentNodes.getLength();
							for (int j = 0; j < lengthContent; j++)
							{
								Node child1 = allContentNodes.item(j);
								if (child1.getNodeType() == Node.ELEMENT_NODE)
								{
									Element contentElement = (Element) child1;
									if (contentElement.getTagName().equals(REDIRECT_TAB))
									{
										String toolTitle = contentElement.getAttribute(WEB_CONTENT_TITLE);
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
										
										// if either tool or page title is missing, use the same for both
										if ((toolTitle != null && toolTitle.length() > 0) && (pageTitle == null || pageTitle.length() == 0))
											pageTitle = toolTitle;
										if ((pageTitle != null && pageTitle.length() > 0) && (toolTitle == null || toolTitle.length() == 0))
											toolTitle = pageTitle;

										String contentUrl = contentElement.getAttribute(WEB_CONTENT_URL);
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

										String height = contentElement.getAttribute(HEIGHT_PROP);
										String openInNewWindow = contentElement.getAttribute(NEW_WINDOW);

										if(toolTitle != null && contentUrl != null && toolTitle.length() >0 && contentUrl.length() >0
												&& pageTitle !=null && pageTitle.length() > 0)
										{
											Tool tr = ToolManager.getTool(TOOL_ID);
											SitePage page = null;
											List<SitePage> pages = site.getPages();
											for(SitePage p : pages)
											{
												String pTitle = p.getTitle();
												if(pageTitle.equals(pTitle))
												{
													page = p;
													break;
												}
											}
											// Page is already there, do not add again.
											if(page != null) {
												log.warn("Web content page '" + pageTitle + "' not added because it is already present in Site ");
												continue;
											}
											page = site.addPage(); 
											page.setTitle(pageTitle);
											ToolConfiguration tool = page.addTool();
											tool.setTool(TOOL_ID, tr);
											tool.setTitle(toolTitle);
											tool.getPlacementConfig().setProperty(WEB_CONTENT_URL_PROP, contentUrl);

											if (height != null) {
												tool.getPlacementConfig().setProperty(HEIGHT_PROP, height);
											}

											if (openInNewWindow.equalsIgnoreCase("true")) 
												page.setPopup(true);
											else
												page.setPopup(false);
										}
										else
										{
											log.warn("Web content item not imported because page_title and title missing or url missing: " + "title: " + toolTitle + " page_title: " + pageTitle + " url: " + contentUrl);
										}
									}
								}
							}
						}
					}
				}
				SiteService.save(site);
				ToolSession session = SessionManager.getCurrentToolSession();

				// During site import, ToolSession may not yet be established
				if (session != null && session.getAttribute(ATTR_TOP_REFRESH) == null)
				{
					session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
				}
			}
			catch(Exception e)
			{
				log.error("errors in merge for WebServiceImpl");
			}
		}

		return null;
	}

	@Override
	public boolean willArchiveMerge()
	{
		return true;
	}

	@Override
	public String[] myToolIds()
	{
		String[] toolIds = { TOOL_ID };
		return toolIds;
	}

	@Override
	public List<Map<String, String>> getEntityMap(String fromContext) {

		try {
			Site fromSite = SiteService.getSite(fromContext);
			List<SitePage> fromSitePages = fromSite.getOrderedPages();

			if (CollectionUtils.isNotEmpty(fromSitePages)) {
				return fromSitePages.stream().filter(sp -> {

					List<ToolConfiguration> toolList = sp.getTools();
					for (ToolConfiguration toolConfig : toolList) {
						 // we do not want to import "special" uses of sakai.iframe, such as worksite info
						String special = toolConfig.getPlacementConfig().getProperty(SPECIAL_PROP);

						if (toolConfig.getToolId().equals(TOOL_ID) && special == null) return true;
					}
					return false;
				}).map(sp -> Map.of("id", sp.getId(), "title", sp.getTitle())).collect(Collectors.toList());
			}
		} catch (IdUnusedException idue) {
			log.warn("No site for id {}", fromContext);
		}

		return Collections.EMPTY_LIST;
	}

	@Override
	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions)
	{
		log.debug("web content transferCopyEntities");
		try
		{
			// retrieve all of the web content tools to copy
			Site fromSite = SiteService.getSite(fromContext);
			Site toSite = SiteService.getSite(toContext);
			
			List<SitePage> fromSitePages = fromSite.getOrderedPages();

			if (CollectionUtils.isNotEmpty(ids)) {
				fromSitePages = fromSitePages.stream().filter(sp -> ids.contains(sp.getId())).collect(Collectors.toList());
			}

			if (CollectionUtils.isNotEmpty(fromSitePages)) {
				for (SitePage currPage : fromSitePages) {
					List<ToolConfiguration> toolList = currPage.getTools();
					for (ToolConfiguration toolConfig : toolList) {
						 // we do not want to import "special" uses of sakai.iframe, such as worksite info
						String special = toolConfig.getPlacementConfig().getProperty(SPECIAL_PROP);

						if (toolConfig.getToolId().equals(TOOL_ID) && special == null) {
							String contentUrl = toolConfig.getPlacementConfig().getProperty(WEB_CONTENT_URL_PROP);
							String toolTitle = toolConfig.getTitle();
							final String pageTitle = currPage.getTitle();
							final int pagePosition = currPage.getPosition();
							final boolean pagePopup = currPage.isPopUp();
							final String height = toolConfig.getPlacementConfig().getProperty(HEIGHT_PROP);
							final String customIcon = toolConfig.getPlacementConfig().getProperty(CUSTOM_ICON_PROP);
							final String visibility = toolConfig.getPlacementConfig().getProperty(PORTAL_VISIBLE);

							// in some cases the new site already has all of this. so make
							// sure we don't make a duplicate
							boolean skip = false;

							Collection<ToolConfiguration> toolConfs = toSite.getTools(TOOL_ID);
							if (CollectionUtils.isNotEmpty(toolConfs))  {
							    for (ToolConfiguration config: toolConfs) {
									if (config.getToolId().equals(TOOL_ID)) {
									    SitePage p = config.getContainingPage();
									    if (pageTitle != null && pageTitle.equals(p.getTitle()) &&
											contentUrl != null && contentUrl.equals(config.getPlacementConfig().getProperty(WEB_CONTENT_URL_PROP))) {
											skip = true;
											break;
									    }
									}
							    }
							}

							if(!skip && toolTitle != null && toolTitle.length() >0 && pageTitle !=null && pageTitle.length() > 0) {
								Tool tr = ToolManager.getTool(TOOL_ID);
								SitePage page = toSite.addPage(); 
								page.setTitle(pageTitle);
								page.setPosition(pagePosition);
								page.setPopup(pagePopup);
								ToolConfiguration tool = page.addTool();
								tool.setTool(TOOL_ID, tr);
								tool.setTitle(toolTitle);
								if (contentUrl != null) {
									// Replace references to the site we're copying from.
									contentUrl = contentUrl.replace(fromContext, toContext);
									tool.getPlacementConfig().setProperty(WEB_CONTENT_URL_PROP, contentUrl);
								}
								if (height != null) {
									tool.getPlacementConfig().setProperty(HEIGHT_PROP, height);
								}
								if (customIcon != null) {
									tool.getPlacementConfig().setProperty(CUSTOM_ICON_PROP, customIcon);
								}
								if (visibility != null) {
									tool.getPlacementConfig().setProperty(PORTAL_VISIBLE, visibility);
								}
							}
						}
					}
				}
			}
			SiteService.save(toSite);
			ToolSession session = SessionManager.getCurrentToolSession();

			if (session != null && session.getAttribute(ATTR_TOP_REFRESH) == null)
			{
				session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
			}
		}

		catch (Exception any)
		{
			log.warn("transferCopyEntities(): exception in handling webcontent data: ", any);
		}

		return null;
	}

	public String trimToNull(String value)
	{
		if (value == null) return null;
		value = value.trim();
		if (value.length() == 0) return null;
		return value;
	}

	@Override
	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions, boolean cleanup) {
		try {
			if (cleanup) {
				Vector<String> removePageIds = new Vector<>();
				Site toSite = SiteService.getSite(toContext);

				List<SitePage> toSitePages = toSite.getOrderedPages();
				if (toSitePages != null && !toSitePages.isEmpty()) {
					for (SitePage currPage : toSitePages) {
						List<ToolConfiguration> toolList = currPage.getTools();
						for (ToolConfiguration toolConfig : toolList) {
							 // we do not want to import "special" uses of sakai.iframe, such as worksite info
							String special = toolConfig.getPlacementConfig().getProperty(SPECIAL_PROP);

							if (toolConfig.getToolId().equals(TOOL_ID) && special == null) {
								removePageIds.add(toolConfig.getPageId());
							}
						}
					}

					for (String removeId : removePageIds) {
						SitePage sitePage = toSite.getPage(removeId);
						toSite.removePage(sitePage);
					}
				}

				// Only save if pages were actually removed
				if (!removePageIds.isEmpty()) {
					SiteService.save(toSite);
					ToolSession session = SessionManager.getCurrentToolSession();

					if (session != null && session.getAttribute(ATTR_TOP_REFRESH) == null) {
						session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
					}
				}
			}
			return transferCopyEntities(fromContext, toContext, ids, transferOptions);
		}
		catch (Exception e) {
			log.info("WebContent transferCopyEntities Error" + e);
		}

		return null;
	}
}
