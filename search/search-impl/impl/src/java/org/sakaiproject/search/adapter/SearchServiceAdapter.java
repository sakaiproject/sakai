package org.sakaiproject.search.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.search.api.*;
import org.sakaiproject.search.model.SearchBuilderItem;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Adapter allowing to easily switch the search implementation with a configuration in sakai.properties.
 *
 * @author Colin Hebert
 */
public class SearchServiceAdapter implements SearchService {
    private static Logger log = LoggerFactory.getLogger(SearchServiceAdapter.class);
    private static final String SEARCH_IMPL_PROPERTY = "search.service.impl";
    /**
     * Defaults to the elastic search implementation if nothing was provided.
     */
    private static final String DEFAULT_IMPL = "org.sakaiproject.search.elasticsearch.ElasticSearchService";
    private final SearchService searchService;

    public SearchServiceAdapter() {
        String searchServiceImplementation = ServerConfigurationService.getString(SEARCH_IMPL_PROPERTY, DEFAULT_IMPL);
        searchService = (SearchService) ComponentManager.get(searchServiceImplementation);
    }

    @Override
    public SearchList search(String searchTerms, List<String> contexts, int searchStart, int searchEnd)
            throws InvalidSearchQueryException {
        return searchService.search(searchTerms, contexts, searchStart, searchEnd);
    }

    @Override
    public SearchList search(String searchTerms, List<String> contexts, int start, int end,
                             String filterName, String sorterName)
            throws InvalidSearchQueryException {
        return searchService.search(searchTerms, contexts, start, end, filterName, sorterName);
    }

    @Override
    public void registerFunction(String function) {
        searchService.registerFunction(function);
    }

    @Override
    public void reload() {
        searchService.reload();
    }

    @Override
    public void refreshInstance() {
        searchService.refreshInstance();
    }

    @Override
    public void rebuildInstance() {
        searchService.rebuildInstance();
    }

    @Override
    public void refreshSite(String currentSiteId) {
        searchService.refreshSite(currentSiteId);
    }

    @Override
    public void rebuildSite(String currentSiteId) {
        searchService.rebuildSite(currentSiteId);
    }

    @Override
    public String getStatus() {
        return searchService.getStatus();
    }

    @Override
    public int getNDocs() {
        return searchService.getNDocs();
    }

    @Override
    public int getPendingDocs() {
        return searchService.getPendingDocs();
    }

    @Override
    public List<SearchBuilderItem> getAllSearchItems() {
        return searchService.getAllSearchItems();
    }

    @Override
    public List<SearchBuilderItem> getSiteMasterSearchItems() {
        return searchService.getSiteMasterSearchItems();
    }

    @Override
    public List<SearchBuilderItem> getGlobalMasterSearchItems() {
        return searchService.getGlobalMasterSearchItems();
    }

    @Override
    public SearchStatus getSearchStatus() {
        return searchService.getSearchStatus();
    }

    @Override
    public boolean removeWorkerLock() {
        return searchService.removeWorkerLock();
    }

    @Override
    public List getSegmentInfo() {
        return searchService.getSegmentInfo();
    }

    @Override
    public void forceReload() {
        searchService.forceReload();
    }

    @Override
    public TermFrequency getTerms(int documentId) throws IOException {
        return searchService.getTerms(documentId);
    }

    @Override
    public String searchXML(Map parameterMap) {
        return searchService.searchXML(parameterMap);
    }

    @Override
    public boolean isEnabled() {
        return searchService.isEnabled();
    }

    @Override
    public String getDigestStoragePath() {
        return searchService.getDigestStoragePath();
    }

    @Override
    public String getSearchSuggestion(String searchString) {
        return searchService.getSearchSuggestion(searchString);
    }

    @Override
    public String[] getSearchSuggestions(String searchString, String currentSite, boolean allMySites) {
        return searchService.getSearchSuggestions(searchString, currentSite, allMySites);
    }

    /**
     * provides some backwards compatibility for impls that doesn't support setting this property.
     * @param searchServer
     */
    public void setSearchServer(boolean searchServer) {
        try {
            Method method = searchService.getClass().getMethod("setSearchServer", new Class[]{boolean.class});
            method.invoke(searchService, searchServer);
            log.trace("######### searchService.getSearchServer() = " + searchService.isSearchServer());
        } catch (NoSuchMethodException e) {
            log.debug(searchService.getClass().getName() + " does not have a method called setSearchServer.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isSearchServer() {
        return searchService.isSearchServer();
    }

    @Override
    public void enableDiagnostics() {
        searchService.enableDiagnostics();
    }

    @Override
    public void disableDiagnostics() {
        searchService.disableDiagnostics();
    }

    @Override
    public boolean hasDiagnostics() {
        return searchService.hasDiagnostics();
    }
}
