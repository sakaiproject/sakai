/*
 * Copyright (c) 2003-2023 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.portal.charon.site;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntitySummary;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.Summary;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.PortalSiteHelper;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.portal.api.SiteView;
import org.sakaiproject.portal.api.SiteView.View;
import org.sakaiproject.portal.charon.PortalStringUtil;
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ArrayUtil;
import org.sakaiproject.util.MapUtil;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.Web;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.comparator.AliasCreatedTimeComparator;

import lombok.extern.slf4j.Slf4j;

/**
* @author ieb
* @since Sakai 2.4
* @version $Rev$
*/
@SuppressWarnings("deprecation")
@Slf4j
public class PortalSiteHelperImpl implements PortalSiteHelper
{
	// namespace for sakai icons see _icons.scss
	public static final String ICON_SAKAI = "icon-sakai--";

	// Alias prefix for page aliases. Use Entity.SEPARATOR as IDs shouldn't contain it.
	private static final String PAGE_ALIAS = Entity.SEPARATOR+ "pagealias"+ Entity.SEPARATOR;

	private final String PROP_PARENT_ID = SiteService.PROP_PARENT_ID;

	private static final String PROP_HTML_INCLUDE = "sakai:htmlInclude";

	private static final String PROP_MENU_CLASS = "sakai:menuClass";

	protected final static String CURRENT_PLACEMENT = "sakai:ToolComponent:current.placement";

	private static final String OVERVIEW_TOOL_TITLE = "overview";
	private static final String SAK_PROP_FORCE_OVERVIEW_TO_TOP = "portal.forceOverviewToTop";
	private static final boolean SAK_PROP_FORCE_OVERVIEW_TO_TOP_DEFAULT = false;

	private Portal portal;

	private AliasService aliasService;
	private EntityManager entityManager;
	private PortalService portalService;
	private PreferencesService preferencesService;
	private SecurityService securityService;
	private ServerConfigurationService serverConfigurationService;
	private SessionManager sessionManager;
	private SiteNeighbourhoodService siteNeighbourhoodService;
	private SiteService siteService;
	private SqlService sqlService;
	private ThreadLocalManager threadLocalManager;
	private UserDirectoryService userDirectoryService;

	private boolean lookForPageAliases;

	// 2.3 back port
	// private final String PROP_PARENT_ID = "sakai:parent-id";

	private ToolManager toolManager;
	private FormattedText formattedText;

	public ToolManager getToolManager() {
		//To work around injection for test case
		if (toolManager==null) {
			toolManager = (ToolManager) ComponentManager.get(ToolManager.class.getName());
		}
		return toolManager;
	}

	public FormattedText getFormattedText() {
		if (formattedText == null) {
			formattedText = ComponentManager.get(FormattedText.class);
		}
		return formattedText;
	}

	private static AuthzGroupService getAuthzGroupService() {
		return (AuthzGroupService) ComponentManager.get(AuthzGroupService.class.getName());
	}

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	/**
	* @param portal
	*/
	public PortalSiteHelperImpl(Portal portal, boolean lookForPageAliases)
	{
		this.portal = portal;
		this.lookForPageAliases = lookForPageAliases;
		aliasService = ComponentManager.get(AliasService.class);
		entityManager = ComponentManager.get(EntityManager.class);
		portalService = ComponentManager.get(PortalService.class);
		siteService = ComponentManager.get(SiteService.class);
		securityService = ComponentManager.get(SecurityService.class);
		serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
		preferencesService = ComponentManager.get(PreferencesService.class);
		sessionManager = ComponentManager.get(SessionManager.class);
		siteNeighbourhoodService = ComponentManager.get(SiteNeighbourhoodService.class);
		sqlService = ComponentManager.get(SqlService.class);
		threadLocalManager = ComponentManager.get(ThreadLocalManager.class);
		userDirectoryService = ComponentManager.get(UserDirectoryService.class);
	}

	/* (non-Javadoc)
	* @see org.sakaiproject.portal.api.PortalSiteHelper#doGatewaySiteList()
	*/
	public boolean doGatewaySiteList()
	{
		String gatewaySiteListPref = serverConfigurationService.getString("gatewaySiteList");

		if (gatewaySiteListPref == null) return false;
		return (gatewaySiteListPref.trim().length() > 0);
	}

	// Determine if we are to do multiple tabs for the anonymous view (Gateway)
	/**
	* @see org.sakaiproject.portal.api.PortalSiteHelper#doGatewaySiteList()
	*/
	public String getGatewaySiteId()
	{
		String gatewaySiteListPref = serverConfigurationService.getString("gatewaySiteList");

		if (gatewaySiteListPref == null) return null;					

		String[] gatewaySiteIds = getGatewaySiteList();
		if (gatewaySiteIds == null)
		{
			return null; 
		}

		// Loop throught the sites making sure they exist and are visitable
		for (String siteId : gatewaySiteIds)
		{
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

		log.warn("No suitable gateway sites found, gatewaySiteList preference had {} sites", gatewaySiteIds.length);
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
		String gatewaySiteListPref = serverConfigurationService.getString("gatewaySiteList");

		if (gatewaySiteListPref == null || gatewaySiteListPref.trim().length() < 1)
		{
			gatewaySiteListPref = serverConfigurationService.getGatewaySiteId();
		}
		if (gatewaySiteListPref == null || gatewaySiteListPref.trim().length() < 1) {
			return null;
		}

		String[] gatewaySites = gatewaySiteListPref.split(",");
		if (gatewaySites.length < 1) return null;

		return gatewaySites;
	}

	public Site getSite(String siteId) {

		if (siteId != null) {
			try {
				return siteService.getSite(siteId);
			} catch (IdUnusedException e) {
				// Attempt to lookup by alias.
				String reference = siteNeighbourhoodService.parseSiteAlias(siteId);
				if (reference != null) {
					Reference ref = entityManager.newReference(reference);
					try {
						return siteService.getSite(ref.getId());
					} catch (IdUnusedException e2) {
						log.error("Site with Id {} not found: {}", siteId, e.toString());
					}
				}
			}
		}
		return null;
	}

	private String getPageDescription(SitePage page) {

		return String.join(" | ", page.getTools().stream().map(tc -> tc.getTool())
			.filter(Objects::nonNull)
			.map(t -> t.getDescription().replace("\"","&quot;")).collect(Collectors.toList()));
	}

	private List<String> getExcludedSiteIds(String userId) {
		Preferences preferences = preferencesService.getPreferences(userId);
		ResourceProperties props = preferences.getProperties(PreferencesService.SITENAV_PREFS_KEY);
        return Optional.ofNullable(props.getPropertyList("exclude")).orElseGet(Collections::emptyList);
	}

	private Map<String, Object> getSiteMap(Site site, String currentSiteId, String userId, boolean pinned, boolean hidden, boolean includePages) {

		Map<String, Object> siteMap = new HashMap<>();
		siteMap.put("id", site.getId());
		siteMap.put("title", site.getTitle());
		siteMap.put("url", site.getUrl());
		siteMap.put("type", site.getType());
		siteMap.put("description", site.getDescription());
		siteMap.put("shortDescription", site.getShortDescription());
		siteMap.put("isPinned", pinned);
        siteMap.put("isHome", userId != null && site.getId().equals(siteService.getUserSiteId(userId)));
        siteMap.put("isCurrent", site.getId().equals(currentSiteId));
        siteMap.put("isHidden", hidden);
        siteMap.put("currentSiteId", currentSiteId);
		if (includePages) {
			List<SitePage> pageList = getPermittedPagesInOrder(site);
			siteMap.put("pages", getPageMaps(pageList, site));
		}
		return siteMap;
	}

	private List<Map<String, Object>> getSiteMaps(Collection<Site> sites, String currentSiteId, String userId, boolean pinned, boolean hidden, boolean includePages) {

		return sites.stream()
				.map(site -> getSiteMap(site, currentSiteId, userId, pinned, hidden, includePages))
				.collect(Collectors.toList());
	}

	private Map<String, Object> getPageMap(SitePage page) {

		Map<String, Object> pageMap = new HashMap<>();
		List<ToolConfiguration> toolList = page.getTools();
		if (toolList != null && toolList.size() != 0) {
			if (toolList.size() == 1) {
				String toolId = toolList.get(0).getId();
				String toolUrl = page.getUrl().replaceFirst("page.*", "tool/".concat(toolId));
				pageMap.put("url", toolUrl);
				pageMap.put("resetUrl", toolUrl.replaceFirst("tool", "tool-reset"));
				pageMap.put("toolId", toolId);
			} else {
				pageMap.put("url", page.getUrl());
				pageMap.put("resetUrl", page.getUrl().replaceFirst("page", "page-reset"));
			}
			pageMap.put("id", page.getId());

			ToolConfiguration firstTool = toolList.get(0);
			String icon = "si si-" + firstTool.getToolId().replace('.', '-');
			Properties tmp = firstTool.getConfig();
			if ( tmp != null ) {
				String fa = tmp.getProperty("imsti.fa_icon");
				if (StringUtils.isNotBlank(fa)) {
					icon = "fa fa-tool-menu-icon " + collapseToVariable(fa);
				}
			}
			pageMap.put("icon", icon);
		} else {
			pageMap.put("icon", "si-default-tool");
		}
		pageMap.put("hidden", toolList.size() > 0 && toolManager.isHidden(toolList.get(0)));
		pageMap.put("locked", !toolManager.isFirstToolVisibleToAnyNonMaintainerRole(page));
		pageMap.put("isPopup", page.isPopUp());
		pageMap.put("title", page.getTitle());
		pageMap.put("description", getPageDescription(page));
		return pageMap;
	}

	private List<Map<String, Object>> getPageMaps(Collection<SitePage> pages, Site site) {

		final boolean siteUpdater = securityService.unlock("site.upd", site.getReference());

		return pages.stream().map(this::getPageMap)
			.filter(m -> !((Boolean) m.get("hidden")) || siteUpdater).collect(Collectors.toList());
	}

	public Map<String, Object> getContextSitesWithPages(HttpServletRequest req, String currentSiteId, String toolContextPath, boolean loggedIn) {

		Map<String, Object> contextSites = new HashMap<>();
		if (loggedIn) {
            // Put Home site in context
			String userId = sessionManager.getCurrentSessionUserId();
			contextSites.put("homeSite", getSiteMap(getSite(siteService.getUserSiteId(userId)), currentSiteId, userId,false, false, true));

			List<String> excludedSiteIds = getExcludedSiteIds(userId);
			// Get pinned sites, excluded sites never appear in the pinned list including current site
            Collection<String> pinnedSiteIds = portalService.getPinnedSites();
            Collection<Site> pinnedSites = pinnedSiteIds.stream()
					.filter(Predicate.not(excludedSiteIds::contains))
					.map(this::getSite)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
            List<Map<String, Object>> pinnedSiteMaps = getSiteMaps(pinnedSites, currentSiteId, userId,true, false, true);
            contextSites.put("pinnedSites", pinnedSiteMaps);

			// Get most recent sites
			Collection<String> recentSiteIds = portalService.getRecentSites();
			// The current site is added to recent sites, except when it:
			// is in recents, is in pinned, is excluded, is a user site
			if (!recentSiteIds.contains(currentSiteId)
					&& !pinnedSiteIds.contains(currentSiteId)
					&& !excludedSiteIds.contains(currentSiteId)
					&& !siteService.isUserSite(currentSiteId)) {
				portalService.addRecentSite(currentSiteId);
				recentSiteIds = portalService.getRecentSites();
			}

			Collection<String> filteredRecentSiteIds = recentSiteIds.stream()
					.filter(Predicate.not(pinnedSiteIds::contains))
					.filter(Predicate.not(excludedSiteIds::contains))
					.collect(Collectors.toSet());

			Collection<Site> recentSites = filteredRecentSiteIds.stream()
					.map(this::getSite)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
            List<Map<String, Object>> recentSitesMaps = getSiteMaps(recentSites, currentSiteId, userId, false, false, true);

			// If the current site is excluded it should appear in recent as hidden
			if (excludedSiteIds.contains(currentSiteId)) {
				recentSitesMaps.add(getSiteMap(getSite(currentSiteId), currentSiteId, userId, false, true, true));
			}
            contextSites.put("recentSites", recentSitesMaps);

            // We need a way to only mark one site as the current. We don't want two sites to
            // be highlighted because the site is pinned and recent.
            if (pinnedSiteMaps.stream().anyMatch(m -> (Boolean) m.get("isCurrent"))) {
                recentSitesMaps.stream().filter(m -> (Boolean) m.get("isCurrent")).findAny().ifPresent(m -> m.remove("isCurrent"));
            }

		} else {
			//Get gateway site
			Site gatewaySite = getSite(serverConfigurationService.getGatewaySiteId());
			if (!gatewaySite.isEmpty()) {
				contextSites.put("gatewaySite", getSiteMap(gatewaySite, currentSiteId, null,false, false, true));
			}
		}
		return contextSites;
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
	public List<Map> convertSitesToMaps(HttpServletRequest req, List<Site> mySites,
	String prefix, String currentSiteId, String myWorkspaceSiteId,
	boolean includeSummary, boolean expandSite, boolean resetTools,
	boolean doPages, String toolContextPath, boolean loggedIn)
	{
		List<Map> l = new ArrayList<>();
		Map<String, Integer> depthChart = new HashMap<>();
		boolean motdDone = false;

		// We only compute the depths if there is no user chosen order
		boolean computeDepth = true;
		Session session = sessionManager.getCurrentSession();

		List<String> pinned = portalService.getPinnedSites();

		// Determine the depths of the child sites if needed
		Map<String, List<String>> realmProviderMap = getProviderIDsForSites(mySites);
		for (Site s : mySites)
		{
			// The first site is the current site
			if (currentSiteId == null) currentSiteId = s.getId();

			Integer cDepth =  Integer.valueOf(0);
			if ( computeDepth )
			{
				ResourceProperties rp = s.getProperties();
				String ourParent = rp.getProperty(PROP_PARENT_ID);
				log.debug("Depth Site:{} parent={}", s.getTitle(), ourParent);
				if (ourParent != null)
				{
					Integer pDepth = depthChart.get(ourParent);
					if (pDepth != null)
					{
						cDepth = pDepth + 1;
					}
				}
				depthChart.put(s.getId(), cDepth);
				log.debug("Depth = {}", cDepth);
			}

			Map<String, Object> m = convertSiteToMap(req, s, prefix, currentSiteId, myWorkspaceSiteId,
			includeSummary, expandSite, resetTools, doPages, toolContextPath,
			loggedIn, realmProviderMap.get(s.getReference()));

			// Add the Depth of the site
			m.put("depth", cDepth);

			// And indicate whether it's pinned or not
			m.put("isPinned", pinned.contains(s.getId()));

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
	* Get all provider IDs for the given site.
	*
	* @param site the site to retrieve all provider IDs
	* @return a List of Strings of provider IDs for the given site
	*/
	public static List<String> getProviderIDsForSite(Site site)
	{
		List<String> providers = new ArrayList<>();
		if (site != null)
		{
			providers.addAll(getAuthzGroupService().getProviderIds(site.getReference()));
		}

		return providers;
	}

	/**
	* Get all provider IDs for all sites given.
	*
	* @param sites the list of sites to retrieve all provider IDs
	* @return a Map, where the key is the realm ID, and the value is a list of provider IDs for that site
	*/
	public static Map<String, List<String>> getProviderIDsForSites(List<Site> sites) {

		if (sites.isEmpty()) {
			return Collections.EMPTY_MAP;
		}

		List<String> realmIDs
		= sites.stream().map(s -> s.getReference()).collect(Collectors.toList());

		return getAuthzGroupService().getProviderIDsForRealms(realmIDs);
	}

	/**
	* {@inheritDoc}
	*/
	public String getUserSpecificSiteTitle( Site site, boolean escaped )
	{
		return getUserSpecificSiteTitle( site, true, escaped, null );
	}

	public String getUserSpecificSiteTitle( Site site, boolean truncated, boolean escaped )
	{
		return getUserSpecificSiteTitle(site, truncated, escaped, null);
	}

	public String getUserSpecificSiteTitle(Site site, boolean truncated, boolean escaped, List<String> siteProviders)
	{
		String retVal = siteService.getUserSpecificSiteTitle( site, userDirectoryService.getCurrentUser().getId(), siteProviders );
		if( truncated )
		{
			retVal = getFormattedText().makeShortenedText( retVal, null, null, null );
		}

		if( escaped )
		{
			retVal = getFormattedText().escapeHtml( retVal );
		}

		return retVal;
	}

	/**
	* Explode a site into a map suitable for use in the map
	* 
	* @see org.sakaiproject.portal.api.PortalSiteHelper#convertSiteToMap(javax.servlet.http.HttpServletRequest,
	*      org.sakaiproject.site.api.Site, java.lang.String, java.lang.String,
	*      java.lang.String, boolean, boolean, boolean, boolean,
	*      java.lang.String, boolean, java.util.List<java.lang.String>)
	*/
	public Map<String, Object> convertSiteToMap(HttpServletRequest req, Site s, String prefix,
	String currentSiteId, String myWorkspaceSiteId, boolean includeSummary,
	boolean expandSite, boolean resetTools, boolean doPages,
	String toolContextPath, boolean loggedIn, List<String> siteProviders)
	{
		if (s == null) return null;
		Map<String, Object> m = new HashMap<>();

		// In case the effective is different than the actual site
		String effectiveSite = getSiteEffectiveId(s);

		boolean isCurrentSite = currentSiteId != null
                        && (s.getId().equals(currentSiteId) || effectiveSite.equals(currentSiteId));
		m.put("isCurrentSite", Boolean.valueOf(isCurrentSite));
		m.put("isPublished", s.isPublished());
		m.put("isMyWorkspace", Boolean.valueOf(myWorkspaceSiteId != null
		&& (s.getId().equals(myWorkspaceSiteId) || effectiveSite
		.equals(myWorkspaceSiteId))));

		String siteTitleRaw = getUserSpecificSiteTitle(s, false, false, siteProviders);
		String siteTitle = getFormattedText().escapeHtml(siteTitleRaw);
		String siteTitleTruncated = getFormattedText().escapeHtml(getFormattedText().makeShortenedText(siteTitleRaw, null, null, null));
		m.put("siteTitle", siteTitle);
		m.put("siteTitleTrunc", siteTitleTruncated);
		m.put("fullTitle", siteTitle);

		m.put("siteDescription", s.getHtmlDescription());

		if (s.getShortDescription() != null && s.getShortDescription().trim().length() > 0) {
			// SAK-23895:  Allow display of site description in the tab instead of site title
			String shortDesc = s.getShortDescription(); 
			String shortDesc_trimmed = getFormattedText().makeShortenedText(shortDesc, null, null, null);
			m.put("shortDescription", getFormattedText().escapeHtml(shortDesc_trimmed));
		}

		String siteUrl = RequestFilter.serverUrl(req)
		+ serverConfigurationService.getString("portalPath") + "/";
		if (prefix != null) siteUrl = siteUrl + prefix + "/";
		// siteUrl = siteUrl + Web.escapeUrl(siteHelper.getSiteEffectiveId(s));
		m.put("siteUrl", siteUrl + getFormattedText().escapeUrl(getSiteEffectiveId(s)));
		m.put("siteType", s.getType());
		m.put("siteId", s.getId());

		// TODO: This should come from the site neighbourhood.
		ResourceProperties rp = s.getProperties();
		String ourParent = rp.getProperty(PROP_PARENT_ID);
		// We are not really a child unless the parent exists
		// And we have a valid pwd
		boolean isChild = false;

		// Get the current site hierarchy
		if (ourParent != null && isCurrentSite)
		{
			List<Site> pwd = getPwd(s, ourParent);
			if (pwd != null)
			{
				List<Map> l = new ArrayList<>();
				// SAK-30477
				// Skip current site size - 1
				for (int i = 0; i < pwd.size() - 1; i++)
				{
					Site site = pwd.get(i);
					log.debug("PWD[{}]={}{}", i, site.getId(), site.getTitle());
					Map<String, Object> pm = new HashMap<>();
					List<String> providers = getProviderIDsForSite(site);

					String parentSiteTitle = getUserSpecificSiteTitle(site, false, false, providers);
					String parentSiteTitleTruncated = getFormattedText().makeShortenedText(parentSiteTitle, null, null, null);
					pm.put("siteTitle", parentSiteTitle);
					pm.put("siteTitleTrunc", parentSiteTitleTruncated);
					pm.put("siteUrl", siteUrl + getFormattedText().escapeUrl(getSiteEffectiveId(site)));

					l.add(pm);
					isChild = true;
				}
				if ( l.size() > 0 ) m.put("pwd", l);
			}
		}

		// If we are a child and have a non-zero length, pwd
		// show breadcrumbs
		if ( isChild ) {
			m.put("isChild", Boolean.valueOf(isChild));
			m.put("parentSite", ourParent);
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

		log.debug("Getting Current Working Directory for {} {}", s.getId(), s.getTitle());

		int depth = 0;
		List<Site> pwd = new ArrayList<>();
		Set<String> added = new HashSet<>();

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
				site = siteService.getSiteVisit(ourParent);
			}
			catch (Exception e)
			{
				break;
			}
			// We have no patience with loops
			if (added.contains(site.getId())) break;

			log.debug("Adding Parent {} {}", site.getId(), site.getTitle());
			pwd.add(0, site); // Push down stack
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
	public Map<String, Object> pageListToMap(HttpServletRequest req, boolean loggedIn, Site site,
	SitePage page, String toolContextPath, String portalPrefix, boolean doPages,
	boolean resetTools, boolean includeSummary)
	{

		Map<String, Object> theMap = new HashMap<>();

		String effectiveSiteId = getSiteEffectiveId(site);

		// Should be pushed up to the API, similar to server configiuration service, but supporting an Enum(always, never, true, false).
		boolean showHelp = true;
		// Supports true, false, never, always
		String showHelpGlobal = serverConfigurationService.getString("display.help.menu", "true");

		if ("never".equals(showHelp))
		{
			showHelp = false;
		}
		else if ("always".equals(showHelpGlobal))
		{
			showHelp = true;
		}
		else
		{
			showHelp = Boolean.valueOf(showHelpGlobal).booleanValue();
			String showHelpSite = site.getProperties().getProperty("display-help-menu");
			if (showHelpSite != null)
			{
				showHelp = Boolean.valueOf(showHelpSite).booleanValue();
			}
		}

		String iconUrl = "";
		try { 
			if (site.getIconUrlFull() != null) {
				iconUrl = new URI(site.getIconUrlFull()).toString();
			}
		} catch (URISyntaxException uex) {
			log.debug("Icon URL is invalid: " + site.getIconUrlFull());
		}

		theMap.put("siteId", site.getId());
		theMap.put("pageNavPublished", Boolean.valueOf(site.isPublished()));
		theMap.put("pageNavType", site.getType());
		theMap.put("pageNavIconUrl", iconUrl);
		theMap.put("roleViewMode", Boolean.valueOf(securityService.isUserRoleSwapped()));
		String htmlInclude = site.getProperties().getProperty(PROP_HTML_INCLUDE);
		if (htmlInclude != null) theMap.put("siteHTMLInclude", htmlInclude);

		boolean siteUpdate = securityService.unlock("site.upd", site.getReference());

		List<Map> l = new ArrayList<>();

		String addMoreToolsUrl = null;
		String manageOverviewUrl = null;
		String manageOverviewUrlInHome = null;

		for (SitePage p : getPermittedPagesInOrder(site)) {
			// check if current user has permission to see page
			// one tool on the page
			List<ToolConfiguration> pageTools = p.getTools();
			ToolConfiguration firstTool = null;
			String toolsOnPage = null;

			// Check the tools that indicate the portal is to do the popup
			String source = null;
			for (ToolConfiguration pageTool : pageTools) {
				source = ToolUtils.getToolPopupUrl(pageTool);
				if ( "sakai.siteinfo".equals(pageTool.getToolId()) ) {
					addMoreToolsUrl = ToolUtils.getPageUrl(req, site, p, portalPrefix, 
					resetTools, effectiveSiteId, null);
					addMoreToolsUrl += "?sakai_action=doMenu_edit_site_tools&panel=Shortcut";

					manageOverviewUrl = ToolUtils.getPageUrl(req, site, p, portalPrefix, resetTools, effectiveSiteId, null);
					manageOverviewUrl += "?sakai_action=doManageOverviewFromHome";
				}
				if ( "sakai.sitesetup".equals(pageTool.getToolId()) ) {
					manageOverviewUrlInHome = ToolUtils.getPageUrl(req, site, p, portalPrefix, resetTools, effectiveSiteId, null);
					manageOverviewUrlInHome += "?sakai_action=doManageOverviewFromHome";
				}
			}
			if ( pageTools.size() != 1 ) {
				source = null;
				addMoreToolsUrl = null;
			}

			boolean current = (page != null && p.getId().equals(page.getId()) && !p.isPopUp());
			String pageAlias = lookupPageToAlias(site.getId(), p);
			String pagerefUrl = ToolUtils.getPageUrl(req, site, p, portalPrefix, 
			resetTools, effectiveSiteId, pageAlias);

			if (doPages || p.isPopUp())
			{
				Map<String, Object> m = new HashMap<>();
				String desc = new String();

				boolean hidden = false;
				if (pageTools != null && pageTools.size() > 0) {
					firstTool = pageTools.get(0);
					hidden = true; // Only set the page to hidden when we have tools that might un-hide it.
					//get the tool descriptions for this page, typically only one per page, execpt for the Home page
					for (ToolConfiguration tc : pageTools) {
						if (hidden && !isHidden(tc)) {
							hidden = false;
						}
					}
					desc = getPageDescription(page);
				}

				if ( ! siteUpdate ){
					addMoreToolsUrl = null;
					manageOverviewUrl = null;
					manageOverviewUrlInHome = null;
				}

				boolean legacyAddMoreToolsPropertyValue = serverConfigurationService.getBoolean("portal.experimental.addmoretools", false);
				if ( ! serverConfigurationService.getBoolean("portal.addmoretools.enable", legacyAddMoreToolsPropertyValue) ) addMoreToolsUrl = null;

				String pagePopupUrl = Web.returnUrl(req, "/page/");

				//SAK-29660 - Refresh tool in the LHS page menu
				String pageResetUrl = pagerefUrl;
				if(pagerefUrl != null){
					if(pagerefUrl.contains("/tool/")){
						pageResetUrl = PortalStringUtil.replaceFirst(pagerefUrl, "/tool/", "/tool-reset/");
					}else if(pagerefUrl.contains("/page/")){
						pageResetUrl = PortalStringUtil.replaceFirst(pagerefUrl, "/page/", "/page-reset/");
					}
				}
				m.put("isPage", Boolean.valueOf(true));
				m.put("current", Boolean.valueOf(current));
				m.put("ispopup", Boolean.valueOf(p.isPopUp()));
				m.put("pagePopupUrl", pagePopupUrl);
				m.put("pageTitle", getFormattedText().escapeHtml(p.getTitle()));
				m.put("jsPageTitle", getFormattedText().escapeJavascript(p.getTitle()));
				m.put("pageId", getFormattedText().escapeUrl(p.getId()));
				m.put("jsPageId", getFormattedText().escapeJavascript(p.getId()));
				m.put("pageRefUrl", pagerefUrl);
				m.put("pageResetUrl", pageResetUrl);
				m.put("toolpopup", Boolean.valueOf(source!=null));
				m.put("toolpopupurl", source);

				m.put("description",  desc);
				m.put("hidden", Boolean.valueOf(hidden));
				boolean locked = !toolManager.isFirstToolVisibleToAnyNonMaintainerRole(p);
				m.put("locked", Boolean.valueOf(locked));
				// toolsOnPage is always null
				//if (toolsOnPage != null) m.put("toolsOnPage", toolsOnPage);
				if (includeSummary) summarizePage(m, site, p);
				if (firstTool != null)
				{
					m.put("wellKnownToolId", firstTool.getToolId());
					String menuClass = firstTool.getToolId();
					menuClass = ICON_SAKAI + menuClass.replace('.', '-');
					m.put("menuClass", menuClass);
					Properties tmp = firstTool.getConfig();
					if ( tmp != null ) {
						String mc = tmp.getProperty(PROP_MENU_CLASS);
						if ( mc != null && mc.length() > 0 ) m.put("menuClassOverride", mc);
						String fa = tmp.getProperty("imsti.fa_icon");
						if ( fa != null && fa.length() > 0 ) {
							m.put("menuClass", "fa");
							m.put("menuClassOverride", collapseToVariable(fa));
						}
					}
				}
				else
				{
					m.put("menuClass", ICON_SAKAI + "default-tool");
				}
				m.put("pageProps", createPageProps(p));
				// this is here to allow the tool reorder to work
				m.put("_sitePage", p);
				l.add(m);
				continue;
			}

			String toolUrl = Web.returnUrl(req, "/" + portalPrefix + "/"
			+ getFormattedText().escapeUrl(getSiteEffectiveId(site)));
			if (resetTools) {
				toolUrl = toolUrl + "/tool-reset/";
			} else {
				toolUrl = toolUrl + "/tool/";
			}

			// Loop through the tools again and Unroll the tools
			Iterator iPt = pageTools.iterator();

			while (iPt.hasNext())
			{
				ToolConfiguration placement = (ToolConfiguration) iPt.next();

				Tool tool = placement.getTool();
				if (tool != null)
				{
					String toolrefUrl = toolUrl + getFormattedText().escapeUrl(placement.getId());

					Map<String, Object> m = new HashMap<String, Object>();
					m.put("isPage", Boolean.valueOf(false));
					m.put("toolId", getFormattedText().escapeUrl(placement.getId()));
					m.put("jsToolId", getFormattedText().escapeJavascript(placement.getId()));
					m.put("toolRegistryId", placement.getToolId());
					m.put("toolTitle", getFormattedText().escapeHtml(placement.getTitle()));
					m.put("jsToolTitle", getFormattedText().escapeJavascript(placement.getTitle()));
					m.put("toolrefUrl", toolrefUrl);
					m.put("toolpopup", Boolean.valueOf(source!=null));
					m.put("toolpopupurl", source);
					m.put("wellKnownToolId", placement.getToolId());
					String menuClass = placement.getToolId();
					menuClass = ICON_SAKAI + menuClass.replace('.', '-');
					m.put("menuClass", menuClass);
					Properties tmp = placement.getConfig();
					if ( tmp != null ) {
						String mc = tmp.getProperty(PROP_MENU_CLASS);
						if ( mc != null && mc.length() > 0 ) m.put("menuClassOverride", mc);
					}
					// this is here to allow the tool reorder to work if requried.
					m.put("_placement", placement);
					l.add(m);
				}
			}

		}

		if ( addMoreToolsUrl != null ) {
			theMap.put("pageNavAddMoreToolsUrl", addMoreToolsUrl);
			theMap.put("pageNavCanAddMoreTools", true);
		} else {
			theMap.put("pageNavCanAddMoreTools", false);
		}

		if(manageOverviewUrl != null){
			theMap.put("manageOverviewUrl", manageOverviewUrl);
			theMap.put("canManageOverview", true);
		}else{
			theMap.put("canManageOverview", false);
		}
		if(manageOverviewUrlInHome != null){
			theMap.put("manageOverviewUrlInHome", manageOverviewUrlInHome);
			theMap.put("canManageOverviewHome", true);
		}else{
			theMap.put("canManageOverviewHome", false);
		}
		theMap.put("pageNavTools", l);

		theMap.put("pageNavTools", l);
		theMap.put("pageMaxIfSingle", serverConfigurationService.getBoolean(
		"portal.experimental.maximizesinglepage", false));
		theMap.put("pageNavToolsCount", Integer.valueOf(l.size()));

		String helpUrl = serverConfigurationService.getHelpUrl(null);
		theMap.put("pageNavShowHelp", Boolean.valueOf(showHelp));
		theMap.put("pageNavHelpUrl", helpUrl);
		theMap.put("helpMenuClass", ICON_SAKAI + "help");
		theMap.put("subsiteClass", ICON_SAKAI + "subsite");

		// theMap.put("pageNavSitContentshead",
		// Web.escapeHtml(rb.getString("sit_contentshead")));

		// Display presence? Global property display.users.present may be always / never / true / false
		// If true or false, the value may be overriden by the site property display-users-present
		// which may be true or false.

		boolean showPresence;
		String globalShowPresence = serverConfigurationService.getString("display.users.present","true");

		if ("never".equals(globalShowPresence)) {
			showPresence = false;
		} else if ("always".equals(globalShowPresence)) {
			showPresence = true;
		} else {
			showPresence = Boolean.valueOf(globalShowPresence).booleanValue();
			String showPresenceSite = site.getProperties().getProperty("display-users-present");
			if (showPresenceSite != null) {
				showPresence = Boolean.valueOf(showPresenceSite).booleanValue();
			}	
		}

		// Check to see if this is a my workspace site, and if so, whether presence is disabled
		if (showPresence && siteService.isUserSite(site.getId()) && !serverConfigurationService.getBoolean("display.users.present.myworkspace", false)) {
			showPresence = false;
		}

		String presenceUrl
		= Web.returnUrl(req, "/presence/" + getFormattedText().escapeUrl(site.getId()));

		theMap.put("pageNavShowPresenceLoggedIn", Boolean.valueOf(showPresence
		&& loggedIn));
		theMap.put("pageNavPresenceUrl", presenceUrl);

		//add softly deleted status
		theMap.put("softlyDeleted", site.isSoftlyDeleted());

		// Initial delay before updating preesnce
		theMap.put("sakaiPresenceTimeDelay", Integer.valueOf(
		serverConfigurationService.getInt("display.users.present.time.delay", 3000)) );

		return theMap;
	}

	/**
	* Collapse a string to only allow characters that can be in variables
	*/
	public String collapseToVariable(String inp)
	{
		if ( inp == null ) return null;
		return inp.replaceAll("[^-_.a-zA-Z0-9]","");
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

		Placement ppp = getToolManager().getCurrentPlacement();
		if (ppp != null && site.getId().equals(ppp.getContext()))
		{
			return true;
		}

		// Create a site-only placement
		Placement placement = new org.sakaiproject.util.Placement("portal-temporary", /* toolId */
		null, /* tool */null,
		/* config */null, /* context */site.getId(), /* title */null);

		threadLocalManager.set(CURRENT_PLACEMENT, placement);

		// Debugging
		ppp = getToolManager().getCurrentPlacement();
		if (ppp == null)
		{
			log.warn("portal-temporary placement not set - null");
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
				log.warn("portal-temporary placement mismatch site="
				+ site.getId() + " context=" + cont);
			}
		}
		return false;
	}

	public boolean summarizePage(Map m, Site site, SitePage page)
	{
		List pageTools = page.getTools();
		Iterator iPt = pageTools.iterator();
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
		Map<String, String> newMap = null;

		/*
		* This is a new, cooler way to do this (I hope) chmaurer... (ieb) Yes:)
		* All summaries now through this interface
		*/

		// offer to all EntityProducers
		for (EntityProducer ep : entityManager.getEntityProducers())
		{
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
			Date modDate = site.getModifiedDate();
			// Yes, some sites have never been modified
			if (modDate != null)
			{
				m.put("rssPubDate", (new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z").format(modDate)));
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
		if (siteService.isUserSite(site.getId()))
		{
			try
			{
				String userId = siteService.getSiteUserId(site.getId());
				String eid = userDirectoryService.getUserEid(userId);
				// SAK-31889: if your EID has special chars, much easier to just use your uid
				if (StringUtils.isAlphanumeric(eid)) {
					return siteService.getUserSiteId(eid);
				}
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
			else
			{
				String reference = portal.getSiteNeighbourhoodService().parseSiteAlias(siteId);
				if (reference != null)
				{
					Reference ref = entityManager.newReference(reference);
					try {
						return siteService.getSiteVisit(ref.getId());
					} catch (IdUnusedException iue) {
					}
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
	protected List<SitePage> getPermittedPagesInOrder(Site site)
	{
		// Get all of the pages
		List<SitePage> pages = site.getOrderedPages();
		boolean siteUpdate = securityService.unlock("site.upd", site.getReference());

		List<SitePage> newPages = new ArrayList<>();

		for (SitePage p : pages)
		{
			// check if current user has permission to see page
			boolean allowPage = false;
			for (ToolConfiguration tc : p.getTools()) {
				boolean thisTool = allowTool(site, tc);
				boolean unHidden = siteUpdate || ! isHidden(tc);
				if (thisTool && unHidden) allowPage = true;
			}
			if (allowPage) newPages.add(p);
		}


		// Force "Overview" to the top at all times if enabled
		if (serverConfigurationService.getBoolean(SAK_PROP_FORCE_OVERVIEW_TO_TOP, SAK_PROP_FORCE_OVERVIEW_TO_TOP_DEFAULT))
		{
			List<SitePage> newPagesCopy = new ArrayList<>(newPages);
			for (SitePage page : newPages)
			{
				if (OVERVIEW_TOOL_TITLE.equalsIgnoreCase(page.getTitle()))
				{
					int index = newPages.indexOf(page);
					if (index >= 0)
					{
						newPagesCopy = new ArrayList<>(newPages.size());
						newPagesCopy.addAll(newPages.subList(0, index));
						newPagesCopy.add(0, (SitePage) newPages.get(index));
						newPagesCopy.addAll(newPages.subList(index + 1, newPages.size()));
					}
				}
			}

			return newPagesCopy;
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
		List<SitePage> pages = getPermittedPagesInOrder(site);
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
		//Shortcut if we aren't using page aliases.
		if (!lookForPageAliases)
		{
			return null;
		}
		SitePage page = null;
		if (alias != null && alias.length() > 0)
		{
			try
			{
				// Use page#{siteId}:{pageAlias} So we can scan for fist colon and alias can contain any character 
				String refString = aliasService.getTarget(buildAlias(alias, site));
				String aliasPageId = entityManager.newReference(refString).getId();
				page = (SitePage) site.getPage(aliasPageId);
			}
			catch (IdUnusedException e)
			{
				log.debug("Alias does not resolve {}", e.getMessage());
			}
		}
		return page;
	}

	public String lookupPageToAlias(String siteId, SitePage page)
	{
		// Shortcut if we aren't using page aliases.
		if (!lookForPageAliases)
		{
			return null;
		}
		String alias = null;
		List<Alias> aliases = aliasService.getAliases(page.getReference());
		if (aliases.size() > 0)
		{	
			if (aliases.size() > 1 && log.isWarnEnabled())
			{
				log.warn("More than one alias for: "+siteId+ ":"+ page.getId());
				// Sort on ID so it is consistent in the alias it uses.
				Collections.sort(aliases, new AliasCreatedTimeComparator());
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

	public boolean allowTool(Site site, Placement placement)
	{
		return getToolManager().allowTool(site, placement);
	}

	/**
	* Check to see if a tool placement is hidden.
	* Can be used to check is a page should be hidden.
	*/
	public boolean isHidden(Placement placement)
	{
		return getToolManager().isHidden(placement);
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
			return new CurrentSiteViewImpl(this,  portal.getSiteNeighbourhoodService(), request, session, siteId, siteService, serverConfigurationService, preferencesService);
			case ALL_SITES_VIEW:
			return new AllSitesViewImpl(this,  portal.getSiteNeighbourhoodService(), request, session, siteId, siteService, serverConfigurationService, preferencesService);
			case DHTML_MORE_VIEW:
			return new MoreSiteViewImpl(this,portal.getSiteNeighbourhoodService(), request, session, siteId, siteService, serverConfigurationService, preferencesService);
		}
		return null;
	}

	public boolean isJoinable(String siteId, String userId) {
		Site site = getSite(siteId);
		return site != null && site.isJoinable() && site.getUserRole(userId) == null;
	}
}
