/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.portal.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntitySummary;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.Summary;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ArrayUtil;
import org.sakaiproject.util.MapUtil;

/**
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */

public class PortalSiteHelper
{
	private static final Log log = LogFactory.getLog(PortalSiteHelper.class);

	public static final String TOOLCONFIG_REQUIRED_PERMISSIONS = "functions.require";

	private final String PROP_PARENT_ID = SiteService.PROP_PARENT_ID;
	// 2.3 back port
	// private final String PROP_PARENT_ID = "sakai:parent-id";

	// Determine if we are to do multiple tabs for the anonymous view (Gateway)
	public boolean doGatewaySiteList()
	{
		String gatewaySiteListPref = ServerConfigurationService
				.getString("gatewaySiteList");
		if (gatewaySiteListPref == null) return false;
		return (gatewaySiteListPref.trim().length() > 0);
	}

	// Return the list of tabs for the anonymous view (Gateway)
	// If we have a list of sites, return that - if not simply pull in the
	// single
	// Gateway site
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

	// Get the sites which are to be displayed for the gateway
	private List<Site> getGatewaySites()
	{
		List<Site> mySites = new ArrayList<Site>();
		String[] gatewaySiteIds = getGatewaySiteList();
		if (gatewaySiteIds == null)
		{
			return mySites; // An empty list - deal with this higher up in the
			// food chain
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
				mySites.add(site);
			}
		}

		if (mySites.size() < 1)
		{
			log.warn("No suitable gateway sites found, gatewaySiteList preference had "
					+ gatewaySiteIds.length + " sites.");
		}
		return mySites;
	}

	/*
	 * Get All Sites which indicate the current site as their parent
	 */
	// TODO: Move into Site

	public List<Site> getSubSites(Site site)
	{
		if ( site == null ) return null;
		Map<String,String> propMap = new HashMap<String,String>();
		propMap.put(PROP_PARENT_ID,site.getId());
		List<Site> mySites = SiteService.getSites(
					org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null,
					null, propMap, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC,
					null);
		return mySites;
	}

	/*
	 * Get All Sites for the current user. If the user is not logged in we
	 * return the list of publically viewable gateway sites.
	 *
	 * @param includeMyWorkspace When this is true - include the user's My Workspace as
	 * the first parameter. If false, do not include the MyWorkspace anywhere in
	 * the list. Some uses - such as the portlet styled portal or the rss styled
	 * portal simply want all of the sites with the MyWorkspace first. Other
	 * portals like the basic tabbed portal treats My Workspace separately from
	 * all of the rest of the workspaces.
	 */

	public List<Site> getAllSites(HttpServletRequest req, Session session,
			boolean includeMyWorkspace) throws IOException
	{

		boolean loggedIn = session.getUserId() != null;
		List<Site> mySites;

		// collect the Publically Viewable Sites
		if (!loggedIn)
		{
			mySites = getGatewaySites();
			return mySites;
		}

		// collect the user's sites
		mySites = SiteService.getSites(
				org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null,
				null, null, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC,
				null);

		// collect the user's preferences
		List prefExclude = new ArrayList();
		List prefOrder = new ArrayList();
		if (session.getUserId() != null)
		{
			Preferences prefs = PreferencesService
					.getPreferences(session.getUserId());
			ResourceProperties props = prefs.getProperties("sakai:portal:sitenav");

			List l = props.getPropertyList("exclude");
			if (l != null)
			{
				prefExclude = l;
			}

			l = props.getPropertyList("order");
			if (l != null)
			{
				prefOrder = l;
			}
		}

		// remove all in exclude from mySites
		mySites.removeAll(prefExclude);

		// Prepare to put sites in the right order
		Vector<Site> ordered = new Vector<Site>();
		Set<String> added = new HashSet<String>();

		// First, place or remove MyWorkspace as requested
		Site myWorkspace = getMyWorkspace(session);
		if (myWorkspace != null)
		{
			if (includeMyWorkspace)
			{
				ordered.add(myWorkspace);
				added.add(myWorkspace.getId());
			}
			else
			{
				int pos = listIndexOf(myWorkspace.getId(), mySites);
				if (pos != -1) mySites.remove(pos);
			}
		}

		// re-order mySites to have order first, the rest later
		for (Iterator i = prefOrder.iterator(); i.hasNext();)
		{
			String id = (String) i.next();

			// find this site in the mySites list
			int pos = listIndexOf(id, mySites);
			if (pos != -1)
			{
				// move it from mySites to order, ignoring child sites
				Site s = mySites.get(pos);
				ResourceProperties rp = s.getProperties();
				String ourParent = rp.getProperty(PROP_PARENT_ID);
				// System.out.println("Pref Site:"+s.getTitle()+" parent="+ourParent);
				if ( ourParent == null && ! added.contains(s.getId()) )
				{
					ordered.add(s);
					added.add(s.getId());
				}
			}
		}

		// We only do the child processing if we have less than 200 sites
		boolean haveChildren = false;
		int siteCount = mySites.size();

		// pick up the rest of the top-level-sites
		for(int i=0; i< mySites.size(); i++)
		{
			Site s = mySites.get(i);
			if ( added.contains(s.getId()) ) continue;
			ResourceProperties rp = s.getProperties();
			String ourParent = rp.getProperty(PROP_PARENT_ID);
			// System.out.println("Top Site:"+s.getTitle()+" parent="+ourParent);
			if ( siteCount > 200 || ourParent == null )
			{
				// System.out.println("Added at root");
				ordered.add(s);
				added.add(s.getId());
			}
			else
			{
				haveChildren = true;
			}
		}

		// If and only if we have some child nodes, we repeatedly
		// pull up children nodes to be behind their parents
		// This is O N**2 - so if we had thousands of sites it
		// it would be costly - hence we only do it for < 200 sites
		// and limited depth - that makes it O(N) not O(N**2)
		boolean addedSites = true;
		int depth = 0;
		while ( depth < 20 && addedSites && haveChildren )
		{
			depth++;
			addedSites = false;
			haveChildren = false;
			for(int i=mySites.size()-1; i>=0; i--)
			{
				Site s = mySites.get(i);
				if ( added.contains(s.getId()) ) continue;
				ResourceProperties rp = s.getProperties();
				String ourParent = rp.getProperty(PROP_PARENT_ID);
				if ( ourParent == null ) continue;
				haveChildren = true;
				// System.out.println("Child Site:"+s.getTitle()+" parent="+ourParent);
				// Search the already added pages for a parent
				// or sibling node
				boolean found = false;
				int j = -1;
				for (j=ordered.size()-1; j>=0; j--) {
					Site ps = ordered.get(j);
					// See if this site is our parent
					if ( ourParent.equals(ps.getId()) )
					{
						found = true;
						break;
					}
					// See if this site is our sibling
					rp = ps.getProperties();
					String peerParent = rp.getProperty(PROP_PARENT_ID);
					if ( ourParent.equals(peerParent) )
					{
						found = true;
						break;
					}
					}

				// We want to insert *after* the identified node
				j = j + 1;
				if ( found && j >= 0 && j < ordered.size())
				{
					// System.out.println("Added after parent");
					ordered.insertElementAt(s,j);
					added.add(s.getId());
					addedSites = true;  // Worth going another level deeper
				}
			}
		} // End while depth

		// If we still have children drop them at the end
		if ( haveChildren ) for(int i=0; i<mySites.size(); i++)
		{
			Site s = mySites.get(i);
			if ( added.contains(s.getId()) ) continue;
			// System.out.println("Orphan Site:"+s.getId()+" "+s.getTitle());
			ordered.add(s);
		}

		// All done
		mySites = ordered;
		return mySites;
	}

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

	protected final static String CURRENT_PLACEMENT = "sakai:ToolComponent:current.placement";

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

	/*
	 * There must be a better way of doing this as this hard codes the services
	 * in surely there should be some whay of looking up the serivce and making
	 * the getSummary part of an interface. TODO: Add an interface beside
	 * EntityProducer to generate summaries Make this discoverable
	 */
	public boolean summarizeTool(Map m, Site site, String toolIdentifier)
	{
		if (site == null) return false;

		setTemporaryPlacement(site);
		Map newMap = null;

		/*
		 * This is a new, cooler way to do this (I hope) chmaurer...
		 * (ieb) Yes:) All summaries now through this interface
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
								.getId(),toolIdentifier);
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
			if ( modDate != null ) 
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

			// re-throw if that didn't work
			throw e;
		}
	}

	/**
	 * Find the site in the list that has this id - return the position.
	 *
	 * @param value
	 *        The site id to find.
	 * @param siteList
	 *        The list of Site objects.
	 * @return The index position in siteList of the site with site id = value,
	 *         or -1 if not found.
	 */
	private int listIndexOf(String value, List siteList)
	{
		for (int i = 0; i < siteList.size(); i++)
		{
			Site site = (Site) siteList.get(i);
			if (site.equals(value))
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * The optional tool configuration tag "functions.require" describes a
	 * set of permission lists which decide the visibility of the tool link
	 * for this site user. Lists are separated by "|" and permissions within a
	 * list are separated by ",". Users must have all the permissions included in
	 * at least one of the permission lists.
	 *
	 * For example, a value like "section.role.student,annc.new|section.role.ta"
	 * would let a user with "section.role.ta" see the tool, and let a user with
	 * both "section.role.student" AND "annc.new" see the tool, but not let a user
	 * who only had "section.role.student" see the tool.
	 *
	 * If the configuration tag is not set or is null, then all users see the tool.
	 */
	public boolean allowTool(Site site, Placement placement)
	{
		// No way to render an opinion
		if (placement == null || site == null) return true;

		String requiredPermissionsString = placement.getConfig().getProperty(TOOLCONFIG_REQUIRED_PERMISSIONS);
		if (log.isDebugEnabled()) log.debug("requiredPermissionsString=" + requiredPermissionsString + " for " + placement.getToolId());
		if (requiredPermissionsString == null)
			return true;
		requiredPermissionsString = requiredPermissionsString.trim();
		if (requiredPermissionsString.length() == 0)
			return true;

		String[] allowedPermissionSets = requiredPermissionsString.split("\\|");
		for (int i = 0; i < allowedPermissionSets.length; i++)
		{
			String[] requiredPermissions = allowedPermissionSets[i].split(",");
			if (log.isDebugEnabled()) log.debug("requiredPermissions=" + Arrays.asList(requiredPermissions));
			boolean gotAllInList = true;
			for (int j = 0; j < requiredPermissions.length; j++)
			{
				if (!SecurityService.unlock(requiredPermissions[j].trim(), site.getReference()))
				{
					gotAllInList = false;
					break;
				}
			}
			if (gotAllInList)
			{
				return true;
			}
		}

		// No permission sets were matched.
		return false;
	}

	/*
	 * Retrieve the list of pages in this site, checking to see if the user has
	 * permission to see the page - by checking the permissions of tools on the
	 * page.
	 */

	// TODO: Move this into Site
	public List getPermittedPagesInOrder(Site site)
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
		return newPages;
	}

	/*
	 * Make sure that we have a proper page selected in the site
	 * pageid is generally the last page used in the site.
	 * pageId must be in the site and the user must have
	 * permission for the page as well.
         */

	public SitePage lookupSitePage(String pageId, Site site)
	{
		// Make sure we have some permitted pages
                List pages = getPermittedPagesInOrder(site);
		if (pages.isEmpty() ) return null;
                SitePage page = site.getPage(pageId);
		if ( page == null )
		{
			page = (SitePage) pages.get(0);
			return page;
		}

		// Make sure that they user has permission for the page.
		// If the page is not in the permitted list go to the first
		// page.
		boolean found = false;
  		for (Iterator i = pages.iterator(); i.hasNext();)
                {
                        SitePage p = (SitePage) i.next();
			if (p.getId().equals(page.getId()) ) return page;
		}

		return (SitePage) pages.get(0);
	}
}
