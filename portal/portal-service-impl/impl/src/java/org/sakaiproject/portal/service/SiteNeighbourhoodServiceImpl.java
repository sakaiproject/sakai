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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ieb
 */
@Slf4j
public class SiteNeighbourhoodServiceImpl implements SiteNeighbourhoodService
{

	private static final String SITE_ALIAS = "/sitealias/";

	private SiteService siteService;

	private PreferencesService preferencesService;

	private UserDirectoryService userDirectoryService;

	private ServerConfigurationService serverConfigurationService;
	
	private AliasService aliasService;

	private ThreadLocalManager threadLocalManager;
	
	/** Should all site aliases have a prefix */
	private boolean useAliasPrefix = false;
	
	private boolean useSiteAliases = false; 

	public void init()
	{
		useSiteAliases = serverConfigurationService.getBoolean("portal.use.site.aliases", false);
	}

	public void destroy()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.SiteNeighbourhoodService#getSitesAtNode(javax.servlet.http.HttpServletRequest,
	 *      org.sakaiproject.tool.api.Session, boolean)
	 */
	public List<Site> getSitesAtNode(HttpServletRequest request, Session session,
			boolean includeMyWorkspace)
	{
		return getAllSites(request, session, includeMyWorkspace);
	}

	/**
	 * Get All Sites for the current user. If the user is not logged in we
	 * return the list of publically viewable gateway sites.
	 * 
	 * @param includeMyWorkspace
	 *        When this is true - include the user's My Workspace as the first
	 *        parameter. If false, do not include the MyWorkspace anywhere in
	 *        the list. Some uses - such as the portlet styled portal or the rss
	 *        styled portal simply want all of the sites with the MyWorkspace
	 *        first. Other portals like the basic tabbed portal treats My
	 *        Workspace separately from all of the rest of the workspaces.
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#getAllSites(javax.servlet.http.HttpServletRequest,
	 *      org.sakaiproject.tool.api.Session, boolean)
	 */
	public List<Site> getAllSites(HttpServletRequest req, Session session,
			boolean includeMyWorkspace)
	{

		boolean loggedIn = session.getUserId() != null;
		List<Site> mySites;

		// collect the Publically Viewable Sites
		if (!loggedIn)
		{
			mySites = getGatewaySites();
			return mySites;
		}

		// collect the user's preferences
		List prefExclude = new ArrayList();
		List prefOrder = new ArrayList();
		if (session.getUserId() != null)
		{
			Preferences prefs = preferencesService.getPreferences(session.getUserId());
			ResourceProperties props = prefs.getProperties(PreferencesService.SITENAV_PREFS_KEY);

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
		
		// collect the user's sites - don't care whether long descriptions are loaded
		// don't load excluded sites
		mySites = siteService.getUserSites(false, false, prefExclude);

		// Prepare to put sites in the right order
		Vector<Site> ordered = new Vector<Site>();
		Set<String> added = new HashSet<String>();
		
		List<String> actualOrder = new ArrayList<String>(mySites.size());
		for (Site site : mySites) {
			actualOrder.add(site.getId());
		}

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
				int pos = actualOrder.indexOf(myWorkspace.getId());
				if (pos != -1) {
					mySites.remove(pos);
					actualOrder.remove(pos);
				};
			}
		}

		// re-order mySites to have order first, the rest later
		for (Iterator i = prefOrder.iterator(); i.hasNext();)
		{
			String id = (String) i.next();

			// find this site in the mySites list
			int pos = actualOrder.indexOf(id);
			if (pos != -1)
			{
				Site s = mySites.get(pos);
				if (!added.contains(s.getId()))
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
		for (int i = 0; i < mySites.size(); i++)
		{
			Site s = mySites.get(i);
			if (added.contains(s.getId())) continue;
			// Once the user takes over the order, 
			// ignore parent/child sorting put all the sites
			// at the top
			String ourParent = null;
			if ( prefOrder.size() == 0 ) 
			{
				ResourceProperties rp = s.getProperties();
				ourParent = rp.getProperty(SiteService.PROP_PARENT_ID);
			}
			log.debug("Top Site:{} parent={}", s.getTitle(), ourParent);
			if (siteCount > 200 || ourParent == null)
			{
				log.debug("Added at root");
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
		while (depth < 20 && addedSites && haveChildren)
		{
			depth++;
			addedSites = false;
			haveChildren = false;
			for (int i = mySites.size() - 1; i >= 0; i--)
			{
				Site s = mySites.get(i);
				if (added.contains(s.getId())) continue;
				ResourceProperties rp = s.getProperties();
				String ourParent = rp.getProperty(SiteService.PROP_PARENT_ID);
				if (ourParent == null) continue;
				haveChildren = true;
				log.debug("Child Site:{} parent={}", s.getTitle(), ourParent);
				// Search the already added pages for a parent
				// or sibling node
				boolean found = false;
				int j = -1;
				for (j = ordered.size() - 1; j >= 0; j--)
				{
					Site ps = ordered.get(j);
					// See if this site is our parent
					if (ourParent.equals(ps.getId()))
					{
						found = true;
						break;
					}
					// See if this site is our sibling
					rp = ps.getProperties();
					String peerParent = rp.getProperty(SiteService.PROP_PARENT_ID);
					if (ourParent.equals(peerParent))
					{
						found = true;
						break;
					}
				}

				// We want to insert *after* the identified node
				j = j + 1;
				if (found && j >= 0 && j < ordered.size())
				{
					log.debug("Added after parent");
					ordered.insertElementAt(s, j);
					added.add(s.getId());
					addedSites = true; // Worth going another level deeper
				}
			}
		} // End while depth

		// If we still have children drop them at the end
		if (haveChildren) for (int i = 0; i < mySites.size(); i++)
		{
			Site s = mySites.get(i);
			if (added.contains(s.getId())) continue;
			log.debug("Orphan Site:{} {}", s.getId(), s.getTitle());
			ordered.add(s);
		}

		// All done
		mySites = ordered;
		return mySites;
	}

	// Get the sites which are to be displayed for the gateway
	/**
	 * @return
	 */
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

	/**
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#getMyWorkspace(org.sakaiproject.tool.api.Session)
	 */
	private Site getMyWorkspace(Session session)
	{
		String siteId = siteService.getUserSiteId(session.getUserId());

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

	// Return the list of tabs for the anonymous view (Gateway)
	// If we have a list of sites, return that - if not simply pull in the
	// single
	// Gateway site
	/**
	 * @return
	 */
	private String[] getGatewaySiteList()
	{
		String gatewaySiteListPref = serverConfigurationService
				.getString("gatewaySiteList");

		if (gatewaySiteListPref == null || gatewaySiteListPref.trim().length() < 1)
		{
			gatewaySiteListPref = serverConfigurationService.getGatewaySiteId();
		}
		if (gatewaySiteListPref == null || gatewaySiteListPref.trim().length() < 1)
			return null;

		String[] gatewaySites = gatewaySiteListPref.split(",");
		if (gatewaySites.length < 1) return null;

		return gatewaySites;
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
			return siteService.getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			if (siteService.isUserSite(siteId))
			{
				try
				{
					String userEid = siteService.getSiteUserId(siteId);
					String userId = userDirectoryService.getUserId(userEid);
					String alternateSiteId = siteService.getUserSiteId(userId);
					return siteService.getSiteVisit(alternateSiteId);
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
	 * @return the preferencesService
	 */
	public PreferencesService getPreferencesService()
	{
		return preferencesService;
	}

	/**
	 * @param preferencesService
	 *        the preferencesService to set
	 */
	public void setPreferencesService(PreferencesService preferencesService)
	{
		this.preferencesService = preferencesService;
	}

	/**
	 * @return the serverConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService()
	{
		return serverConfigurationService;
	}

	/**
	 * @param serverConfigurationService
	 *        the serverConfigurationService to set
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	/**
	 * @return the siteService
	 */
	public SiteService getSiteService()
	{
		return siteService;
	}

	/**
	 * @param siteService
	 *        the siteService to set
	 */
	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	/**
	 * @return the userDirectoryService
	 */
	public UserDirectoryService getUserDirectoryService()
	{
		return userDirectoryService;
	}

	/**
	 * @param userDirectoryService
	 *        the userDirectoryService to set
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

	public void setThreadLocalManager(ThreadLocalManager threadLocalManager)
	{
		this.threadLocalManager = threadLocalManager;
	}

	public String lookupSiteAlias(String id, String context)
	{
		// TODO Constant extraction
		if ("/site/!error".equals(id)) {
			Object originalId =  threadLocalManager.get(PortalService.SAKAI_PORTAL_ORIGINAL_SITEID);
			if (originalId instanceof String) {
				return (String)originalId;
			}
		}
		if (!useSiteAliases)
		{
			return null;
		}
		List<Alias> aliases = aliasService.getAliases(id);
		if (aliases.size() > 0)
		{
			if (aliases.size() > 1 && log.isInfoEnabled())
			{
				log.debug("More than one alias for {} sorting.", id);
				Collections.sort(aliases, new Comparator<Alias>()
				{
					public int compare(Alias o1, Alias o2)
					{
						return o1.getId().compareTo(o2.getId());
					}
					
				});
			}
			for (Alias alias : aliases)
			{
				String aliasId = alias.getId();
				boolean startsWithPrefix = aliasId.startsWith(SITE_ALIAS);
				if (startsWithPrefix)
				{
					if (useAliasPrefix)
					{
						return aliasId.substring(SITE_ALIAS.length());
					}
				}
				else
				{
					if (!useAliasPrefix)
					{
						return aliasId;
					}
				}
			}
		}
		return null;
	}

	public String parseSiteAlias(String alias)
	{
		if (alias == null)
		{
			return null;
		}
		// Prepend site alias prefix if it's being used.
		String id = ((useAliasPrefix)?SITE_ALIAS:"")+alias;

		try
		{
			String reference = aliasService.getTarget(id);
			return reference;
		}
		catch (IdUnusedException e)
		{
			log.debug("No alias found for {}", id);
		}
		return null;
	}

	public void setAliasService(AliasService aliasService) {
		this.aliasService = aliasService;
	}

	public boolean isUseAliasPrefix()
	{
		return useAliasPrefix;
	}

	public void setUseAliasPrefix(boolean useAliasPrefix)
	{
		this.useAliasPrefix = useAliasPrefix;
	}

}
