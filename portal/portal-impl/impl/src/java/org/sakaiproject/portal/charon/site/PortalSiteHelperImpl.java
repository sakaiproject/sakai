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

package org.sakaiproject.portal.charon.site;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.cover.AliasService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntitySummary;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.Summary;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.PageFilter;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalSiteHelper;
import org.sakaiproject.portal.api.SiteView;
import org.sakaiproject.portal.api.SiteView.View;
import org.sakaiproject.portal.charon.ToolHelperImpl;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ArrayUtil;
import org.sakaiproject.util.MapUtil;
import org.sakaiproject.util.Web;

/**
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */

public class PortalSiteHelperImpl implements PortalSiteHelper
{
	// Alias prefix for page aliases. Use Entity.SEPARATOR as IDs shouldn't contain it.
	private static final String PAGE_ALIAS = Entity.SEPARATOR+ "pagealias"+ Entity.SEPARATOR;

	private static final Log log = LogFactory.getLog(PortalSiteHelper.class);

	private final String PROP_PARENT_ID = SiteService.PROP_PARENT_ID;

	protected final static String CURRENT_PLACEMENT = "sakai:ToolComponent:current.placement";

	private Portal portal;

	// 2.3 back port
	// private final String PROP_PARENT_ID = "sakai:parent-id";

	private ToolHelperImpl toolHelper = new ToolHelperImpl();

	/**
	 * @param portal
	 */
	public PortalSiteHelperImpl(Portal portal)
	{
		this.portal = portal;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#doGatewaySiteList()
	 */
	public boolean doGatewaySiteList()
	{
		String gatewaySiteListPref = ServerConfigurationService
		.getString("gatewaySiteList");

		if (gatewaySiteListPref == null) return false;
		return (gatewaySiteListPref.trim().length() > 0);
	}

	// Determine if we are to do multiple tabs for the anonymous view (Gateway)
	/**
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#doGatewaySiteList()
	 */
	public String getGatewaySiteId()
	{
		String gatewaySiteListPref = ServerConfigurationService
				.getString("gatewaySiteList");
		
		if (gatewaySiteListPref == null) return null;					
		
		String[] gatewaySiteIds = getGatewaySiteList();
		if (gatewaySiteIds == null)
		{
			return null; 
		}

		// Loop throught the sites making sure they exist and are visitable
		for (int i = 0; i < gatewaySiteIds.length; i++)
		{
			String siteId = gatewaySiteIds[i];

			Site site = null;
			try
			{
				site = getSiteVisit(siteId);
			}
			catch (IdUnusedException e)
			{
				continue;
			}
			catch (PermissionException e)
			{
				continue;
			}

			if (site != null)
			{
				return siteId;
			}
		}

		log.warn("No suitable gateway sites found, gatewaySiteList preference had "
					+ gatewaySiteIds.length + " sites.");
		return null;
	}

	// Return the list of tabs for the anonymous view (Gateway)
	// If we have a list of sites, return that - if not simply pull in the
	// single
	// Gateway site
	/**
	 * @return
	 */
	private String[] getGatewaySiteList()
	{
		String gatewaySiteListPref = ServerConfigurationService
				.getString("gatewaySiteList");

		if (gatewaySiteListPref == null || gatewaySiteListPref.trim().length() < 1)
		{
			gatewaySiteListPref = ServerConfigurationService.getGatewaySiteId();
		}
		if (gatewaySiteListPref == null || gatewaySiteListPref.trim().length() < 1)
			return null;

		String[] gatewaySites = gatewaySiteListPref.split(",");
		if (gatewaySites.length < 1) return null;

		return gatewaySites;
	}


	/*
	 * Get All Sites which indicate the current site as their parent
	 */

	// TODO: Move into SiteStructureProvider
	/**
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#getSubSites(org.sakaiproject.site.api.Site)
	 */
	public List<Site> getSubSites(Site site)
	{
		if (site == null) return null;
		Map<String, String> propMap = new HashMap<String, String>();
		propMap.put(PROP_PARENT_ID, site.getId());
		List<Site> mySites = SiteService.getSites(
				org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, null,
				propMap, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null);
		return mySites;
	}

	public List<Map> getSitesInContext(String context, String userId)
	{

		return null;
	}

	/**
	 * This method takes a list of sites and organizes it into a list of maps of
	 * properties. There is an additional complication that the depth contains
	 * informaiton arround.
	 * 
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#convertSitesToMaps(javax.servlet.http.HttpServletRequest,
	 *      java.util.List, java.lang.String, java.lang.String,
	 *      java.lang.String, boolean, boolean, boolean, boolean,
	 *      java.lang.String, boolean)
	 */
	public List<Map> convertSitesToMaps(HttpServletRequest req, List mySites,
			String prefix, String currentSiteId, String myWorkspaceSiteId,
			boolean includeSummary, boolean expandSite, boolean resetTools,
			boolean doPages, String toolContextPath, boolean loggedIn)
	{
		List<Map> l = new ArrayList<Map>();
		Map<String, Integer> depthChart = new HashMap<String, Integer>();
		boolean motdDone = false;

		// We only compute the depths if there is no user chosen order
		boolean computeDepth = true;
		Session session = SessionManager.getCurrentSession();
		if ( session != null )
                { 
                        Preferences prefs = PreferencesService.getPreferences(session.getUserId());
                        ResourceProperties props = prefs.getProperties("sakai:portal:sitenav");

                        List propList = props.getPropertyList("order");
                        if (propList != null)
                        {
                                computeDepth = false; 
                        }
                }

		// Determine the depths of the child sites if needed
		for (Iterator i = mySites.iterator(); i.hasNext();)
		{
			Site s = (Site) i.next();

			// The first site is the current site
			if (currentSiteId == null) currentSiteId = s.getId();

			Integer cDepth = new Integer(0);
			if ( computeDepth )
			{
				ResourceProperties rp = s.getProperties();
				String ourParent = rp.getProperty(PROP_PARENT_ID);
				// System.out.println("Depth Site:"+s.getTitle()+
				// "parent="+ourParent);
				if (ourParent != null)
				{
					Integer pDepth = depthChart.get(ourParent);
					if (pDepth != null)
					{
						cDepth = pDepth + 1;
					}
				}
				depthChart.put(s.getId(), cDepth);
				// System.out.println("Depth = "+cDepth);
			}

			Map m = convertSiteToMap(req, s, prefix, currentSiteId, myWorkspaceSiteId,
					includeSummary, expandSite, resetTools, doPages, toolContextPath,
					loggedIn);

			// Add the Depth of the site
			m.put("depth", cDepth);

			if (includeSummary && m.get("rssDescription") == null)
			{
				if (!motdDone)
				{
					summarizeTool(m, s, "sakai.motd");
					motdDone = true;
				}
				else
				{
					summarizeTool(m, s, "sakai.announcements");
				}

			}
			l.add(m);
		}
		return l;
	}

	/**
	 * Explode a site into a map suitable for use in the map
	 * 
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#convertSiteToMap(javax.servlet.http.HttpServletRequest,
	 *      org.sakaiproject.site.api.Site, java.lang.String, java.lang.String,
	 *      java.lang.String, boolean, boolean, boolean, boolean,
	 *      java.lang.String, boolean)
	 */
	public Map convertSiteToMap(HttpServletRequest req, Site s, String prefix,
			String currentSiteId, String myWorkspaceSiteId, boolean includeSummary,
			boolean expandSite, boolean resetTools, boolean doPages,
			String toolContextPath, boolean loggedIn)
	{
		if (s == null) return null;
		Map<String, Object> m = new HashMap<String, Object>();

		// In case the effective is different than the actual site
		String effectiveSite = getSiteEffectiveId(s);

		boolean isCurrentSite = currentSiteId != null
				&& (s.getId().equals(currentSiteId) || effectiveSite
						.equals(currentSiteId));
		m.put("isCurrentSite", Boolean.valueOf(isCurrentSite));
		m.put("isMyWorkspace", Boolean.valueOf(myWorkspaceSiteId != null
				&& (s.getId().equals(myWorkspaceSiteId) || effectiveSite
						.equals(myWorkspaceSiteId))));
		m.put("siteTitle", Web.escapeHtml(s.getTitle()));
		m.put("siteDescription", Web.escapeHtml(s.getDescription()));
		String siteUrl = Web.serverUrl(req)
				+ ServerConfigurationService.getString("portalPath") + "/";
		if (prefix != null) siteUrl = siteUrl + prefix + "/";
		// siteUrl = siteUrl + Web.escapeUrl(siteHelper.getSiteEffectiveId(s));
		m.put("siteUrl", siteUrl + Web.escapeUrl(getSiteEffectiveId(s)));

		// TODO: This should come from the site neighbourhood.
		ResourceProperties rp = s.getProperties();
		String ourParent = rp.getProperty(PROP_PARENT_ID);
		boolean isChild = ourParent != null;
		m.put("isChild", Boolean.valueOf(isChild));
		m.put("parentSite", ourParent);

		// Get the current site hierarchy
		if (isChild && isCurrentSite)
		{
			List<Site> pwd = getPwd(s, ourParent);
			if (pwd != null)
			{
				List<Map> l = new ArrayList<Map>();
				for (int i = 0; i < pwd.size(); i++)
				{
					Site site = pwd.get(i);
					// System.out.println("PWD["+i+"]="+site.getId()+"
					// "+site.getTitle());
					Map<String, Object> pm = new HashMap<String, Object>();
					pm.put("siteTitle", Web.escapeHtml(site.getTitle()));
					pm.put("siteUrl", siteUrl + Web.escapeUrl(getSiteEffectiveId(site)));
					l.add(pm);
				}
				m.put("pwd", l);
			}
		}

		if (includeSummary)
		{
			summarizeTool(m, s, "sakai.announce");
		}
		if (expandSite)
		{
			Map pageMap = pageListToMap(req, loggedIn, s, /* SitePage */null,
					toolContextPath, prefix, doPages, resetTools, includeSummary);
			m.put("sitePages", pageMap);
		}

		return m;
	}

	/**
	 * Gets the path of sites back to the root of the tree.
	 * @param s
	 * @param ourParent
	 * @return
	 */
	private List<Site> getPwd(Site s, String ourParent)
	{
		if (ourParent == null) return null;

		// System.out.println("Getting Current Working Directory for
		// "+s.getId()+" "+s.getTitle());

		int depth = 0;
		Vector<Site> pwd = new Vector<Site>();
		Set<String> added = new HashSet<String>();

		// Add us to the list at the top (will become the end)
		pwd.add(s);
		added.add(s.getId());

		// Make sure we don't go on forever
		while (ourParent != null && depth < 8)
		{
			depth++;
			Site site = null;
			try
			{
				site = SiteService.getSiteVisit(ourParent);
			}
			catch (Exception e)
			{
				break;
			}
			// We have no patience with loops
			if (added.contains(site.getId())) break;

			// System.out.println("Adding Parent "+site.getId()+"
			// "+site.getTitle());
			pwd.insertElementAt(site, 0); // Push down stack
			added.add(site.getId());

			ResourceProperties rp = site.getProperties();
			ourParent = rp.getProperty(PROP_PARENT_ID);
		}

		// PWD is only defined for > 1 site
		if (pwd.size() < 2) return null;
		return pwd;
	}

	/**
	 * Produce a page and/or a tool list doPage = true is best for the
	 * tabs-based portal and for RSS - these think in terms of pages doPage =
	 * false is best for the portlet-style - it unrolls all of the tools unless
	 * a page is marked as a popup. If the page is a popup - it is left a page
	 * and marked as such. restTools = true - generate resetting tool URLs.
	 * 
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#pageListToMap(javax.servlet.http.HttpServletRequest,
	 *      boolean, org.sakaiproject.site.api.Site,
	 *      org.sakaiproject.site.api.SitePage, java.lang.String,
	 *      java.lang.String, boolean, boolean, boolean)
	 */
	public Map pageListToMap(HttpServletRequest req, boolean loggedIn, Site site,
			SitePage page, String toolContextPath, String portalPrefix, boolean doPages,
			boolean resetTools, boolean includeSummary)
	{

		Map<String, Object> theMap = new HashMap<String, Object>();

		String pageUrl = Web.returnUrl(req, "/" + portalPrefix + "/"
				+ Web.escapeUrl(getSiteEffectiveId(site)) + "/page/");
		String toolUrl = Web.returnUrl(req, "/" + portalPrefix + "/"
				+ Web.escapeUrl(getSiteEffectiveId(site)));
		if (resetTools)
		{
			toolUrl = toolUrl + "/tool-reset/";
		}
		else
		{
			toolUrl = toolUrl + "/tool/";
		}

		String pagePopupUrl = Web.returnUrl(req, "/page/");
		boolean showHelp = ServerConfigurationService.getBoolean("display.help.menu",
				true);
		String iconUrl = site.getIconUrlFull();
		boolean published = site.isPublished();
		String type = site.getType();

		theMap.put("pageNavPublished", Boolean.valueOf(published));
		theMap.put("pageNavType", type);
		theMap.put("pageNavIconUrl", iconUrl);
		// theMap.put("pageNavSitToolsHead",
		// Web.escapeHtml(rb.getString("sit_toolshead")));

		// order the pages based on their tools and the tool order for the
		// site type
		// List pages = site.getOrderedPages();
		List pages = getPermittedPagesInOrder(site);

		List<Map> l = new ArrayList<Map>();

		for (Iterator i = pages.iterator(); i.hasNext();)
		{

			SitePage p = (SitePage) i.next();
			// check if current user has permission to see page
			// we will draw page button if it have permission to see at least
			// one tool on the page
			List pTools = p.getTools();
			ToolConfiguration firstTool = null;
			if (pTools != null && pTools.size() > 0)
			{
				firstTool = (ToolConfiguration) pTools.get(0);
			}
			String toolsOnPage = null;

			boolean current = (page != null && p.getId().equals(page.getId()) && !p
					.isPopUp());
			String alias = lookupPageToAlias(site.getId(), p);
			String pagerefUrl = pageUrl + Web.escapeUrl((alias != null)?alias:p.getId());

			if (doPages || p.isPopUp())
			{
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("isPage", Boolean.valueOf(true));
				m.put("current", Boolean.valueOf(current));
				m.put("ispopup", Boolean.valueOf(p.isPopUp()));
				m.put("pagePopupUrl", pagePopupUrl);
				m.put("pageTitle", Web.escapeHtml(p.getTitle()));
				m.put("jsPageTitle", Web.escapeJavascript(p.getTitle()));
				m.put("pageId", Web.escapeUrl(p.getId()));
				m.put("jsPageId", Web.escapeJavascript(p.getId()));
				m.put("pageRefUrl", pagerefUrl);

				Iterator tools = pTools.iterator();
				//get the tool descriptions for this page, typically only one per page, execpt for the Home page
				StringBuffer desc = new StringBuffer();
				int tCount = 0;
				while(tools.hasNext()){
					ToolConfiguration t = (ToolConfiguration)tools.next();
					if (tCount > 0){
						desc.append(" | ");
					}
					desc.append(t.getTool().getDescription());
					tCount++;
				}
				
				// Just make sure no double quotes...
				String description = desc.toString().replace('"','-');
				m.put("description", desc.toString());
				if (toolsOnPage != null) m.put("toolsOnPage", toolsOnPage);
				if (includeSummary) summarizePage(m, site, p);
				if (firstTool != null)
				{
					String menuClass = firstTool.getToolId();
					menuClass = "icon-" + menuClass.replace('.', '-');
					m.put("menuClass", menuClass);
				}
				else
				{
					m.put("menuClass", "icon-default-tool");
				}
				m.put("pageProps", createPageProps(p));
				// this is here to allow the tool reorder to work
				m.put("_sitePage", p);
				l.add(m);
				continue;
			}

			// Loop through the tools again and Unroll the tools
			Iterator iPt = pTools.iterator();

			while (iPt.hasNext())
			{
				ToolConfiguration placement = (ToolConfiguration) iPt.next();

				String toolrefUrl = toolUrl + Web.escapeUrl(placement.getId());

				Map<String, Object> m = new HashMap<String, Object>();
				m.put("isPage", Boolean.valueOf(false));
				m.put("toolId", Web.escapeUrl(placement.getId()));
				m.put("jsToolId", Web.escapeJavascript(placement.getId()));
				m.put("toolRegistryId", placement.getToolId());
				m.put("toolTitle", Web.escapeHtml(placement.getTitle()));
				m.put("jsToolTitle", Web.escapeJavascript(placement.getTitle()));
				m.put("toolrefUrl", toolrefUrl);
				String menuClass = placement.getToolId();
				menuClass = "icon-" + menuClass.replace('.', '-');
				m.put("menuClass", menuClass);
				// this is here to allow the tool reorder to work if requried.
				m.put("_placement", placement);
				l.add(m);
			}

		}
		PageFilter pageFilter = portal.getPageFilter();
		if (pageFilter != null)
		{
			l = pageFilter.filterPlacements(l, site);
		}

		theMap.put("pageNavTools", l);
		theMap.put("pageMaxIfSingle", ServerConfigurationService.getBoolean(
				"portal.experimental.maximizesinglepage", false));
		theMap.put("pageNavToolsCount", Integer.valueOf(l.size()));

		String helpUrl = ServerConfigurationService.getHelpUrl(null);
		theMap.put("pageNavShowHelp", Boolean.valueOf(showHelp));
		theMap.put("pageNavHelpUrl", helpUrl);
		theMap.put("helpMenuClass", "icon-sakai-help");
		theMap.put("subsiteClass", "icon-sakai-subsite");

		// theMap.put("pageNavSitContentshead",
		// Web.escapeHtml(rb.getString("sit_contentshead")));

		// Display presence? Global property display.users.present may be always / never / true / false
		// If true or false, the value may be overriden by the site property display-users-present
		// which may be true or false.
				
		boolean showPresence;
		String globalShowPresence = ServerConfigurationService.getString("display.users.present");
				
		if ("never".equals(globalShowPresence)) {
			showPresence = false;
		} else if ("always".equals(globalShowPresence)) {
			showPresence = true;
		} else {
			String showPresenceSite = site.getProperties().getProperty("display-users-present");
				
			if (showPresenceSite == null)
			{
				showPresence = Boolean.valueOf(globalShowPresence).booleanValue();  
			}
			else 
			{
				showPresence = Boolean.valueOf(showPresenceSite).booleanValue();
			}	
		}
		
		String presenceUrl = Web.returnUrl(req, "/presence/"
				+ Web.escapeUrl(site.getId()));

		// theMap.put("pageNavSitPresenceTitle",
		// Web.escapeHtml(rb.getString("sit_presencetitle")));
		// theMap.put("pageNavSitPresenceFrameTitle",
		// Web.escapeHtml(rb.getString("sit_presenceiframetit")));
		theMap.put("pageNavShowPresenceLoggedIn", Boolean.valueOf(showPresence
				&& loggedIn));
		theMap.put("pageNavPresenceUrl", presenceUrl);

		return theMap;
	}

	/**
	 * @param p
	 * @return
	 */
	private Map createPageProps(SitePage p)
	{
		Map properties = new HashMap();
		for (Iterator<String> i = p.getProperties().getPropertyNames(); i.hasNext();)
		{
			String propName = i.next();
			properties.put(propName, p.getProperties().get(propName));
		}

		return properties;
	}


	/**
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#getMyWorkspace(org.sakaiproject.tool.api.Session)
	 */
	public Site getMyWorkspace(Session session)
	{
		String siteId = SiteService.getUserSiteId(session.getUserId());

		// Make sure we can visit
		Site site = null;
		try
		{
			site = getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			site = null;
		}
		catch (PermissionException e)
		{
			site = null;
		}

		return site;
	}

	/*
	 * Temporarily set a placement with the site id as the context - we do not
	 * set a tool ID this will not be a rich enough placement to do *everything*
	 * but for those services which call
	 * ToolManager.getCurrentPlacement().getContext() to contextualize their
	 * information - it wil be sufficient.
	 */

	public boolean setTemporaryPlacement(Site site)
	{
		if (site == null) return false;

		Placement ppp = ToolManager.getCurrentPlacement();
		if (ppp != null && site.getId().equals(ppp.getContext()))
		{
			return true;
		}

		// Create a site-only placement
		Placement placement = new org.sakaiproject.util.Placement("portal-temporary", /* toolId */
		null, /* tool */null,
		/* config */null, /* context */site.getId(), /* title */null);

		ThreadLocalManager.set(CURRENT_PLACEMENT, placement);

		// Debugging
		ppp = ToolManager.getCurrentPlacement();
		if (ppp == null)
		{
			System.out.println("WARNING portal-temporary placement not set - null");
		}
		else
		{
			String cont = ppp.getContext();
			if (site.getId().equals(cont))
			{
				return true;
			}
			else
			{
				System.out.println("WARNING portal-temporary placement mismatch site="
						+ site.getId() + " context=" + cont);
			}
		}
		return false;
	}

	public boolean summarizePage(Map m, Site site, SitePage page)
	{
		List pTools = page.getTools();
		Iterator iPt = pTools.iterator();
		while (iPt.hasNext())
		{
			ToolConfiguration placement = (ToolConfiguration) iPt.next();

			if (summarizeTool(m, site, placement.getToolId()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * There must be a better way of doing this as this hard codes the services
	 * in surely there should be some whay of looking up the serivce and making
	 * the getSummary part of an interface. TODO: Add an interface beside
	 * EntityProducer to generate summaries Make this discoverable
	 * 
	 * @param m
	 * @param site
	 * @param toolIdentifier
	 * @return
	 */
	private boolean summarizeTool(Map m, Site site, String toolIdentifier)
	{
		if (site == null) return false;

		setTemporaryPlacement(site);
		Map newMap = null;

		/*
		 * This is a new, cooler way to do this (I hope) chmaurer... (ieb) Yes:)
		 * All summaries now through this interface
		 */

		// offer to all EntityProducers
		for (Iterator i = EntityManager.getEntityProducers().iterator(); i.hasNext();)
		{
			EntityProducer ep = (EntityProducer) i.next();
			if (ep instanceof EntitySummary)
			{
				try
				{
					EntitySummary es = (EntitySummary) ep;

					// if this producer claims this tool id
					if (ArrayUtil.contains(es.summarizableToolIds(), toolIdentifier))
					{
						String summarizableReference = es.getSummarizableReference(site
								.getId(), toolIdentifier);
						newMap = es.getSummary(summarizableReference, 5, 30);
					}
				}
				catch (Throwable t)
				{
					log.warn(
							"Error encountered while asking EntitySummary to getSummary() for: "
									+ toolIdentifier, t);
				}
			}
		}

		if (newMap != null)
		{
			return (MapUtil.copyHtml(m, "rssDescription", newMap,
					Summary.PROP_DESCRIPTION) && MapUtil.copy(m, "rssPubdate", newMap,
					Summary.PROP_PUBDATE));
		}
		else
		{
			Time modDate = site.getModifiedTime();
			// Yes, some sites have never been modified
			if (modDate != null)
			{
				m.put("rssPubDate", (modDate.toStringRFC822Local()));
			}
			return false;
		}

	}

	/**
	 * If this is a user site, return an id based on the user EID, otherwise
	 * just return the site id.
	 * 
	 * @param site
	 *        The site.
	 * @return The effective site id.
	 */
	public String getSiteEffectiveId(Site site)
	{
		if (SiteService.isUserSite(site.getId()))
		{
			try
			{
				String userId = SiteService.getSiteUserId(site.getId());
				String eid = UserDirectoryService.getUserEid(userId);
				return SiteService.getUserSiteId(eid);
			}
			catch (UserNotDefinedException e)
			{
				log.warn("getSiteEffectiveId: user eid not found for user site: "
						+ site.getId());
			}
		}
		else
		{
			String displayId = portal.getSiteNeighbourhoodService().lookupSiteAlias(site.getReference(), null);
			if (displayId != null)
			{
				return displayId;
			}
		}

		return site.getId();
	}

	/**
	 * Do the getSiteVisit, but if not found and the id is a user site, try
	 * translating from user EID to ID.
	 * 
	 * @param siteId
	 *        The Site Id.
	 * @return The Site.
	 * @throws PermissionException
	 *         If not allowed.
	 * @throws IdUnusedException
	 *         If not found.
	 */
	public Site getSiteVisit(String siteId) throws PermissionException, IdUnusedException
	{
		try
		{
			return SiteService.getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			if (SiteService.isUserSite(siteId))
			{
				try
				{
					String userEid = SiteService.getSiteUserId(siteId);
					String userId = UserDirectoryService.getUserId(userEid);
					String alternateSiteId = SiteService.getUserSiteId(userId);
					return SiteService.getSiteVisit(alternateSiteId);
				}
				catch (UserNotDefinedException ee)
				{
				}
			}
			else
			{
				String reference = portal.getSiteNeighbourhoodService().parseSiteAlias(siteId);
				Reference ref = EntityManager.getInstance().newReference(reference);
				try 
				{
					return SiteService.getSiteVisit(ref.getId());
				}
				catch (IdUnusedException iue)
				{
				}
			}

			// re-throw if that didn't work
			throw e;
		}
	}



	/**
	 * Retrieve the list of pages in this site, checking to see if the user has
	 * permission to see the page - by checking the permissions of tools on the
	 * page.
	 * 
	 * @param site
	 * @return
	 */
	private List getPermittedPagesInOrder(Site site)
	{
		// Get all of the pages
		List pages = site.getOrderedPages();

		List newPages = new ArrayList();

		for (Iterator i = pages.iterator(); i.hasNext();)
		{
			// check if current user has permission to see page
			SitePage p = (SitePage) i.next();
			List pTools = p.getTools();
			Iterator iPt = pTools.iterator();

			boolean allowPage = false;
			while (iPt.hasNext())
			{
				ToolConfiguration placement = (ToolConfiguration) iPt.next();

				boolean thisTool = allowTool(site, placement);
				if (thisTool) allowPage = true;
			}
			if (allowPage) newPages.add(p);
		}

		PageFilter pageFilter = portal.getPageFilter();

		if (pageFilter != null)
		{
			newPages = pageFilter.filter(newPages, site);
		}
		return newPages;
	}

	/**
	 * Make sure that we have a proper page selected in the site pageid is
	 * generally the last page used in the site. pageId must be in the site and
	 * the user must have permission for the page as well.
	 * 
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#lookupSitePage(java.lang.String,
	 *      org.sakaiproject.site.api.Site)
	 */
	public SitePage lookupSitePage(String pageId, Site site)
	{
		// Make sure we have some permitted pages
		List pages = getPermittedPagesInOrder(site);
		if (pages.isEmpty()) return null;
		SitePage page = site.getPage(pageId);
		if (page == null)
		{
			page = lookupAliasToPage(pageId, site);
			if (page == null)
			{
				page = (SitePage) pages.get(0);
				return page;
			}
		}

		// Make sure that they user has permission for the page.
		// If the page is not in the permitted list go to the first
		// page.
		boolean found = false;
		for (Iterator i = pages.iterator(); i.hasNext();)
		{
			SitePage p = (SitePage) i.next();
			if (p.getId().equals(page.getId())) return page;
		}

		return (SitePage) pages.get(0);
	}

	public SitePage lookupAliasToPage(String alias, Site site)
	{
		SitePage page = null;
		if (alias != null && alias.length() > 0)
		{
			try
			{
				// Use page#{siteId}:{pageAlias} So we can scan for fist colon and alias can contain any character 
				String refString = AliasService.getTarget(buildAlias(alias, site));
				String aliasPageId = EntityManager.newReference(refString).getId();
				page = (SitePage) site.getPage(aliasPageId);
			}
			catch (IdUnusedException e)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Alias does not resolve " + e.getMessage());
				}
			}
		}
		return page;
	}

	public String lookupPageToAlias(String siteId, SitePage page)
	{
		String alias = null;
		List<Alias> aliases = AliasService.getAliases(page.getReference());
		if (aliases.size() > 0)
		{	
			if (aliases.size() > 1 && log.isWarnEnabled())
			{
				log.warn("More than one alias for: "+siteId+ ":"+ page.getId());
				// Sort on ID so it is consistent in the alias it uses.
				Collections.sort(aliases, getAliasComparator());
			}
			alias = aliases.get(0).getId();
			alias = parseAlias(alias, siteId);
		}
		return alias;
	}

	/**
	 * Find the short alias.
	 * @param alias
	 * @return
	 */
	private String parseAlias(String aliasId, String siteId)
	{
		String prefix = PAGE_ALIAS+ siteId+ Entity.SEPARATOR;
		String alias = null;
		if (aliasId.startsWith(prefix))
		{
			alias = aliasId.substring(prefix.length());
		}
		return alias;
	}

	private String buildAlias(String alias, Site site)
	{
		return PAGE_ALIAS+site.getId()+Entity.SEPARATOR+alias;
	}

	private Comparator<Alias> getAliasComparator()
	{
		return new Comparator<Alias>() {
			public int compare(Alias o1, Alias o2)
			{
				// Sort by date, then by ID to assure consistent order.
				return o1.getCreatedTime().compareTo(o2.getCreatedTime()) * 10 +
					o1.getId().compareTo(o2.getId());
			}
			
		};
	}
	
	/**
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#allowTool(org.sakaiproject.site.api.Site,
	 *      org.sakaiproject.tool.api.Placement)
	 */
	public boolean allowTool(Site site, Placement placement)
	{
		return toolHelper.allowTool(site, placement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#getSitesView(org.sakaiproject.portal.api.SiteView.View,
	 *      javax.servlet.http.HttpServletRequest,
	 *      org.sakaiproject.tool.api.Session, java.lang.String)
	 */
	public SiteView getSitesView(View view, HttpServletRequest request, Session session,
			String siteId)
	{
		switch (view)
		{
			case CURRENT_SITE_VIEW:
				return new CurrentSiteViewImpl(this,  portal.getSiteNeighbourhoodService(), request, session, siteId, SiteService
						.getInstance(), ServerConfigurationService.getInstance(),
						PreferencesService.getInstance());
			case ALL_SITES_VIEW:
				return new AllSitesViewImpl(this,  portal.getSiteNeighbourhoodService(), request, session, siteId, SiteService
						.getInstance(), ServerConfigurationService.getInstance(),
						PreferencesService.getInstance());
			case DEFAULT_SITE_VIEW:
				return new DefaultSiteViewImpl(this, portal.getSiteNeighbourhoodService(), request, session, siteId, SiteService
						.getInstance(), ServerConfigurationService.getInstance(),
						PreferencesService.getInstance());
			case DHTML_MORE_VIEW:
				return new MoreSiteViewImpl(this,portal.getSiteNeighbourhoodService(), request, session, siteId, SiteService
						.getInstance(), ServerConfigurationService.getInstance(),
						PreferencesService.getInstance());
			case SUB_SITES_VIEW:
				return new SubSiteViewImpl(this, portal.getSiteNeighbourhoodService(), request, session, siteId, SiteService
						.getInstance(), ServerConfigurationService.getInstance(),
						PreferencesService.getInstance());
		}
		return null;
	}


}
