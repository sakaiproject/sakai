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

package org.sakaiproject.portal.charon.site;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;

/**
 * @author ieb
 *
 */
public class MoreSiteViewImpl extends DefaultSiteViewImpl
{
	/** messages. */
	private static ResourceLoader rb = new ResourceLoader("sitenav");

	/**
	 * @param siteHelper
	 * @param request
	 * @param session
	 * @param currentSiteId
	 * @param siteService
	 * @param serverConfigurationService
	 * @param preferencesService
	 */
	public MoreSiteViewImpl(PortalSiteHelperImpl siteHelper,  SiteNeighbourhoodService siteNeighbourhoodService, HttpServletRequest request,
			Session session, String currentSiteId, SiteService siteService,
			ServerConfigurationService serverConfigurationService,
			PreferencesService preferencesService)
	{
		super(siteHelper, siteNeighbourhoodService, request, session, currentSiteId, siteService,
				serverConfigurationService, preferencesService);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.portal.charon.DefaultSiteViewImpl#processMySites(java.util.Map)
	 */
	@Override
	protected void processMySites()
	{
		boolean useDHTMLMore =  Boolean.valueOf(serverConfigurationService.getBoolean("portal.use.dhtml.more", false));
		if (useDHTMLMore)
		{
			List<Site> allSites = new ArrayList<Site>();
			allSites.addAll(mySites);
			allSites.addAll(moreSites);
			// get Sections
			Map<String, List> termsToSites = new HashMap<String, List>();
			Map<String, List> tabsMoreTerms = new HashMap<String, List>();
			for (int i = 0; i < allSites.size(); i++)
			{
				Site site = allSites.get(i);
				ResourceProperties siteProperties = site.getProperties();

				String type = site.getType();
				String term = null;

				if ("course".equals(type))
				{
					term = siteProperties.getProperty("term");
				}
				else if ("project".equals(type))
				{
					term = rb.getString("moresite_projects");
				}
				else if ("portfolio".equals(type))
				{
					term = rb.getString("moresite_portfolios");
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
						return firstTitle.compareToIgnoreCase(secondTitle);

					return 0;

				}

			}

			Comparator<Map> titleSorter = new TitleSorter();

			// now loop through each section and convert the Lists to maps
			for (String key : termsToSites.keySet())
			{
				List<Site> currentList = termsToSites.get(key);
				List<Map> temp = siteHelper.convertSitesToMaps(request, currentList, prefix,
						currentSiteId, myWorkspaceSiteId,
						/* includeSummary */false, /* expandSite */false,
						/* resetTools */"true".equals(serverConfigurationService
								.getString(Portal.CONFIG_AUTO_RESET)),
						/* doPages */true, /* toolContextPath */null, loggedIn);

				Collections.sort(temp, titleSorter);

				tabsMoreTerms.put(key, temp);

			}

			String[] termOrder = serverConfigurationService
					.getStrings("portal.term.order");
			List<String> tabsMoreSortedTermList = new ArrayList<String>();

			// Order term column headers according to order specified in
			// portal.term.order
			// Filter out terms for which user is not a member of any sites

			if (termOrder != null)
			{
				for (int i = 0; i < termOrder.length; i++)
				{

					if (tabsMoreTerms.containsKey(termOrder[i]))
					{

						tabsMoreSortedTermList.add(termOrder[i]);

					}

				}
			}

			Iterator i = tabsMoreTerms.keySet().iterator();
			while (i.hasNext())
			{
				String term = (String) i.next();
				if (!tabsMoreSortedTermList.contains(term))
				{
					tabsMoreSortedTermList.add(term);

				}
			}
			renderContextMap.put("tabsMoreTerms", tabsMoreTerms);
			renderContextMap.put("tabsMoreSortedTermList", tabsMoreSortedTermList);
		}

	}
}
