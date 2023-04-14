/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
package org.sakaiproject.webapi.beans;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchRestBean {

    public String reference;
    public String contentId;
    public float score;
    public String searchResult;
    public String title;
    public String tool;
    public String url;
    public String siteTitle;
    public String siteUrl;
    public String creatorDisplayName;

    public static SearchRestBean of(SearchResult searchResult, SiteService siteService) {

        SearchRestBean bean = new SearchRestBean();
        bean.reference = searchResult.getReference();
        bean.contentId = searchResult.getId();
        bean.score = searchResult.getScore();
        bean.searchResult = searchResult.getSearchResult();
        bean.title = searchResult.getTitle();
        bean.tool = searchResult.getTool();
        bean.url = searchResult.getUrl();
        bean.creatorDisplayName = searchResult.getCreatorDisplayName();

        try {
            Site site = siteService.getSite(searchResult.getSiteId());
            bean.siteTitle = site.getTitle();
            bean.siteUrl = site.getUrl();
        } catch (IdUnusedException e) {
            log.error("No site found for id {}", searchResult.getSiteId());
        }

        return bean;
    }
}
