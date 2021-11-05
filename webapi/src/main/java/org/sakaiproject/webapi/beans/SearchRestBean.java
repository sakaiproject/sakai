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
