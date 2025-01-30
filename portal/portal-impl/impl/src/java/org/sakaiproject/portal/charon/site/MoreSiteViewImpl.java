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

package org.sakaiproject.portal.charon.site;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.text.StringEscapeUtils;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;

import lombok.extern.slf4j.Slf4j;

/**
 * @author ieb
 */
@Slf4j
public class MoreSiteViewImpl extends AbstractSiteViewImpl
{
	/** messages. */
	private static ResourceLoader rb = new ResourceLoader("sitenav");

	/**
	 * @param siteHelper
	 * @param request
	 * @param session
	 * @param currentSiteId
	 */
	public MoreSiteViewImpl(PortalSiteHelperImpl siteHelper, HttpServletRequest request, Session session, String currentSiteId) {
		super(siteHelper, request, session, currentSiteId);
	}

	public Object getRenderContextObject()
	{
		// Get the list of sites in the right order,
		// My WorkSpace will be the first in the list

		// if public workgroup/gateway site is not included, add to list
		boolean siteFound = false;
		for (int i = 0; i < mySites.size(); i++)
		{
			if (((Site) mySites.get(i)).getId().equals(currentSiteId))
			{
				siteFound = true;
			}
		}

		try
		{
			if (!siteFound)
			{
				mySites.add(siteService.getSite(currentSiteId));
			}
		}
		catch (IdUnusedException e)
		{

		} // ignore

		// we allow one site in the drawer - that is OK
		moreSites = new ArrayList<>();
		
		processMySites();

		String profileToolId = serverConfigurationService.getString("portal.profiletool","sakai.profile2");
		String calendarToolId = serverConfigurationService.getString("portal.calendartool","sakai.schedule");
		String preferencesToolId = serverConfigurationService.getString("portal.preferencestool","sakai.preferences");
		String worksiteToolId = serverConfigurationService.getString("portal.worksitetool","sakai.sitesetup");

 		String profileToolUrl = null;
		String calendarToolUrl = null;
 		String worksiteToolUrl = null;
 		String mrphs_profileToolUrl = null;
 		String mrphs_worksiteToolUrl = null;
 		String mrphs_worksiteUrl = null;
        if ( myWorkspaceSiteId != null ) {
            for (Site s : mySites) {
                if (myWorkspaceSiteId.equals(s.getId()) ) {
                    mrphs_worksiteUrl = Web.returnUrl(request, "/site/" + Web.escapeUrl(siteHelper.getSiteEffectiveId(s)));
                    List<SitePage> pages = siteHelper.getPermittedPagesInOrder(s);
                    for (SitePage p : pages) {
                        List<ToolConfiguration> pTools = p.getTools();
                        for (ToolConfiguration placement : pTools) {
                            if ( profileToolId.equals(placement.getToolId()) ) {
                                profileToolUrl = Web.returnUrl(request, "/site/" + Web.escapeUrl(siteHelper.getSiteEffectiveId(s)) + "/page/" + Web.escapeUrl(p.getId()));
                                mrphs_profileToolUrl = Web.returnUrl(request, "/site/" + Web.escapeUrl(siteHelper.getSiteEffectiveId(s)) + "/tool-reset/" + Web.escapeUrl(placement.getId()));
                            } else if ( calendarToolId.equals(placement.getToolId()) ) {
                                calendarToolUrl = Web.returnUrl(request, "/site/" + Web.escapeUrl(siteHelper.getSiteEffectiveId(s)) + "/page/" + Web.escapeUrl(p.getId()));
                            } else if ( worksiteToolId.equals(placement.getToolId()) ) {
                                worksiteToolUrl = Web.returnUrl(request, "/site/" + Web.escapeUrl(siteHelper.getSiteEffectiveId(s)) + "/page/" + Web.escapeUrl(p.getId()));
                                mrphs_worksiteToolUrl = Web.returnUrl(request, "/site/" + Web.escapeUrl(siteHelper.getSiteEffectiveId(s)) + "/tool/" + Web.escapeUrl(placement.getId()));
                            }
                        }
                    }
                }
            }
        }

		if ( mrphs_worksiteUrl != null ) {
			renderContextMap.put("mrphs_worksiteUrl", mrphs_worksiteUrl);
        }
		if ( profileToolUrl != null ) {
			renderContextMap.put("profileToolUrl", profileToolUrl);
			renderContextMap.put("mrphs_profileToolUrl", mrphs_profileToolUrl);
		}
		if ( calendarToolUrl != null ) {
			renderContextMap.put("calendarToolUrl", calendarToolUrl);
		}
		if ( worksiteToolUrl != null ) {
			renderContextMap.put("worksiteToolUrl", worksiteToolUrl);
			renderContextMap.put("mrphs_worksiteToolUrl", mrphs_worksiteToolUrl);
		}
		if (serverConfigurationService.getBoolean("portal.use.tutorial", true)) {
			renderContextMap.put("tutorial", true);
		} else {
			renderContextMap.put("tutorial", false);
		}

		renderContextMap.put("themeSwitcher", serverConfigurationService.getBoolean("portal.themes.switcher", true));

		List<Map<String, Object>> l = siteHelper.convertSitesToMaps(request, mySites, prefix, currentSiteId, myWorkspaceSiteId, false, false,
				serverConfigurationService.getBoolean(Portal.CONFIG_AUTO_RESET, false), true, null, loggedIn);

		int tabsToDisplay = serverConfigurationService.getInt(Portal.CONFIG_DEFAULT_TABS, 15);

		renderContextMap.put("maxFavoritesShown", tabsToDisplay);

		List<Map<String, Object>> pinned
			= l.stream().filter(map -> map.containsKey("isPinned") && (Boolean) map.get("isPinned"))
				.collect(Collectors.toList());

		renderContextMap.put("pinned", pinned);

		// Bump it up by one to make room for the user's workspace
		tabsToDisplay++;

		if (l.size() > tabsToDisplay) {
			List<Map<String, Object>> sublist = l.subList(0, tabsToDisplay);

			boolean listContainsCurrentSite = false;
			for (Map<String, Object> entry : sublist) {
				if (entry.get("isCurrentSite") instanceof Boolean ? (Boolean) entry.get("isCurrentSite") : false) {
					listContainsCurrentSite = true;
				}
			}

			if (!listContainsCurrentSite) {
				// If the current site wouldn't have been shown in the
				// subset of sites we're showing, swap it for the last
				// in the list.
				List<Map<String, Object>> modifiedList = new ArrayList<>(sublist);

				for (Map<String, Object> entry : l) {
					if (entry.get("isCurrentSite") instanceof Boolean ? (Boolean) entry.get("isCurrentSite") : false) {
						modifiedList.set(tabsToDisplay - 1, entry);
						break;
					}
				}

				sublist = modifiedList;
			}

			renderContextMap.put("tabsSites", sublist);
		} else {
		    renderContextMap.put("tabsSites", l);
		}

		boolean displayActive = serverConfigurationService.getBoolean("portal.always.display.active_sites",false);
		//If we don't always want to display it anyway, check to see if we need to display it
		if (!displayActive) {
			displayActive=Boolean.valueOf(moreSites.size() > 0);
		}

		renderContextMap.put("tabsMoreSitesShow", displayActive);

		// more dropdown
		if (!moreSites.isEmpty()) {
			List<Map<String, Object>> m = siteHelper.convertSitesToMaps(request, moreSites, prefix, currentSiteId, myWorkspaceSiteId, false, false,
					serverConfigurationService.getBoolean(Portal.CONFIG_AUTO_RESET, false), true, null, loggedIn);

			renderContextMap.put("tabsMoreSites", m);
		}

		return renderContextMap;
	}

	protected void processMySites()
	{
		List<Site> allSites = new ArrayList<>();
		allSites.addAll(mySites);
		allSites.addAll(moreSites);
		// get Sections
		Map<String, List<Site>> termsToSites = new HashMap<>();
		Map<String, List<Map<String, Object>>> tabsMoreTerms = new TreeMap<>();
		
		//SAK-30712
		String[] moresitesExternalSites = serverConfigurationService.getStrings("moresites.externalConfig.siteTypes");
		String moresitesExternalPrefix = serverConfigurationService.getString("moresites.externalConfig.prefix","moresites_");
		boolean moresitesExternalConfig = (moresitesExternalSites!=null) && (moresitesExternalSites.length>0);
		
		Map<String, String> moresitesExternalSiteTypes = new HashMap<String, String>();
		if (moresitesExternalConfig)
		{
			for (int i=0;i<moresitesExternalSites.length;i++)
			{
				moresitesExternalSiteTypes.put(moresitesExternalSites[i], moresitesExternalPrefix+moresitesExternalSites[i]);
			}
		}
		
		for (int i = 0; i < allSites.size(); i++)
		{
			Site site = allSites.get(i);
			ResourceProperties siteProperties = site.getProperties();

			String type = site.getType();
			String term = null;

			if (moresitesExternalConfig && moresitesExternalSiteTypes.containsKey(type))
			{
				term = rb.getString(moresitesExternalSiteTypes.get(type));
			}
			else if (isCourseType(type))
			{
				term = siteProperties.getProperty("term");
				if(null==term) {
					term = rb.getString("moresite_unknown_term");
				}

			}
			else if (isProjectType(type))
			{
				term = rb.getString("moresite_projects");
			}
			else if ("admin".equals(type))
			{
				term = rb.getString("moresite_administration");
			}
			else
			{
				term = rb.getString("moresite_other");
			}

			List<Site> currentList = new ArrayList();
			if (termsToSites.containsKey(term))
			{
				currentList = termsToSites.get(term);
				termsToSites.remove(term);
			}
			currentList.add(site);
			termsToSites.put(term, currentList);
		}

		class TitleSorter implements Comparator<Map>
		{

			public int compare(Map first, Map second)
			{

				if (first == null || second == null) return 0;

				String firstTitle = (String) first.get("siteTitle");
				String secondTitle = (String) second.get("siteTitle");

				if (firstTitle != null)
					return StringEscapeUtils.unescapeHtml4(firstTitle).compareToIgnoreCase(StringEscapeUtils.unescapeHtml4(secondTitle));

				return 0;

			}

		}

		Comparator<Map> titleSorter = new TitleSorter();

		// now loop through each section and convert the Lists to maps
		for (Map.Entry<String, List<Site>> entry : termsToSites.entrySet()) {
			List<Site> currentList = entry.getValue();
			List<Map<String, Object>> temp = siteHelper.convertSitesToMaps(request, currentList, prefix, currentSiteId, myWorkspaceSiteId, false, false,
					serverConfigurationService.getBoolean(Portal.CONFIG_AUTO_RESET, false), true, null, loggedIn);

			Collections.sort(temp, titleSorter);

			tabsMoreTerms.put(entry.getKey(), temp);

		}

		//Get a list of sorted terms
		List<String> tabsMoreSortedTermList = PortalUtils.getPortalTermOrder(tabsMoreTerms.keySet());

		SitePanesArrangement sitesByPane = arrangeSitesIntoPanes(tabsMoreTerms);
		renderContextMap.put("tabsMoreTermsLeftPane", sitesByPane.sitesInLeftPane);
		renderContextMap.put("tabsMoreTermsRightPane", sitesByPane.sitesInRightPane);

		renderContextMap.put("tabsMoreSortedTermList", tabsMoreSortedTermList);

	}

	private static class SitePanesArrangement {
		public Map<String, List> sitesInLeftPane = new TreeMap<String, List>();
		public Map<String, List> sitesInRightPane = new TreeMap<String, List>();
	}

	private SitePanesArrangement arrangeSitesIntoPanes(Map<String, List<Map<String, Object>>> tabsMoreTerms) {
		SitePanesArrangement result = new SitePanesArrangement();

		for (String term : tabsMoreTerms.keySet()) {
			result.sitesInLeftPane.put(term, new ArrayList<>());
			result.sitesInRightPane.put(term, new ArrayList<>());

			for (Map<String, Object> site : tabsMoreTerms.get(term)) {
				String type = (String) Optional.ofNullable(site.get("siteType"))
						.filter(o -> o instanceof String)
						.orElse(null);
				if (isCourseType(type)) {
					result.sitesInLeftPane.get(term).add(site);
				} else {
					result.sitesInRightPane.get(term).add(site);
				}
			}
		}

		return result;
	}

	/**
	 * read the site Type definition from configuration files
	 */
	public List<String> getSiteTypeStrings(String type)
	{
		String[] siteTypes = serverConfigurationService.getStrings(type + "SiteType");
		if (siteTypes == null || siteTypes.length == 0)
		{
			siteTypes = new String[] {type};
		}
		return Arrays.asList(siteTypes);
	}

	private boolean isCourseType(String type) {
		if (type != null) {
			List<String> courseSiteTypes = getSiteTypeStrings("course");
			return courseSiteTypes.contains(type);
		}
		return false;
	}

	private boolean isProjectType(String type) {
		if (type != null) {
			List<String> projectSiteTypes = getSiteTypeStrings("project");
			return projectSiteTypes.contains(type);
		}
		return false;
	}

}
