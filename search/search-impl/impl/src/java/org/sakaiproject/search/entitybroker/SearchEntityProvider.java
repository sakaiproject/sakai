/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/search/trunk/search-impl/impl/src/java/org/sakaiproject/search/component/Messages.java $
 * $Id: Messages.java 59685 2009-04-03 23:36:24Z arwhyte@umich.edu $
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
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (azeckoski @ unicon.net)
 **********************************************************************************/

package org.sakaiproject.search.entitybroker;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.search.api.InvalidSearchQueryException;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Provides basic search functionality via EB.
 *
 * @author Adrian Fish (a.fish@lancaster.ac.uk)
 */
public class SearchEntityProvider extends AbstractEntityProvider implements CollectionResolvable,Outputable,Describeable {

    private static Log log = LogFactory.getLog(SearchEntityProvider.class);

	private SearchService searchService = null;

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	private SiteService siteService = null;

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

   	public UserDirectoryService userDirectoryService;

   	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      	this.userDirectoryService = userDirectoryService;
   	}

	public String getEntityPrefix() {
		return "search";
	}

    public String[] getHandledOutputFormats() {
        return new String[] { Formats.JSON, Formats.XML };
    }

	public List<?> getEntities(EntityReference ref, Search search) {
		Restriction searchTermsRestriction = search.getRestrictionByProperty("searchTerms");

		if(searchTermsRestriction == null)
			throw new IllegalArgumentException("No searchTerms supplied");

		String searchTerms = searchTermsRestriction.getStringValue();

        // fix up the search limits
        if (search.getLimit() > 50 || search.getLimit() == 0) {
            search.setLimit(50);
        }
        if (search.getStart() > 49) {
            search.setStart(0);
        }

		List<String> contexts = null;

		Restriction contextsRestriction = search.getRestrictionByProperty("contexts");

		if(contextsRestriction != null) {
			String[] contextsArray = contextsRestriction.getStringValue().split(",");
			contexts = Arrays.asList(contextsArray);
		}
		else {
			// No contexts supplied. Get all the sites the current user is a member of
			contexts = getAllUsersSites();
		}

		SearchList searchResults;
		try {
			searchResults = searchService.search(searchTerms,contexts,(int) search.getStart(),(int) search.getLimit(),"normal","normal");
		} catch (InvalidSearchQueryException e) {
			throw new IllegalArgumentException(searchTerms + " is not a valid query expression");
		}

		Restriction toolRestriction = search.getRestrictionByProperty("tool");

		String tool = null;

		if(toolRestriction != null) {
			tool = toolRestriction.getStringValue();
		}
		
		List<SearchResultEntity> results = new ArrayList<SearchResultEntity>();

		for(SearchResult result : searchResults) {
			if(tool == null || result.getTool().equalsIgnoreCase(tool))
				results.add(new SearchResultEntity(result));
		}

		return results;
	}

	public Object getEntity(EntityReference ref) {
		System.out.println("getEntity(" + ref.getReference() + ")");
		return null;
	}

	private List<String> getAllUsersSites() {
        List<Site> sites = siteService.getSites(
			SiteService.SelectionType.ACCESS,null, null, null, null, null);
        List<String> siteIds = new ArrayList<String>(sites.size());
        for (Site site: sites) {
            if (site != null && site.getId() != null) {
                siteIds.add(site.getId());
            }
        }
        siteIds.add(siteService.getUserSiteId(
			userDirectoryService.getCurrentUser().getId()));
        return siteIds;
    }

	public class SearchResultEntity {

		private SearchResult result = null;

		private SearchResultEntity(SearchResult result) {
			this.result = result;
		}

		public String getId() {
			return result.getId();
		}

		public float getScore() {
			return result.getScore();
		}

		public String getSearchResult() {
			return result.getSearchResult();
		}

		public String getTitle() {
			return result.getTitle();
		}

		public String getTool() {
			return result.getTool();
		}

		public String getUrl() {
			return result.getUrl();
		}
	}
}
