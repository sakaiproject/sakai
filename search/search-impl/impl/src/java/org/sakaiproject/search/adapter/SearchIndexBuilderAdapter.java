package org.sakaiproject.search.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.model.SearchBuilderItem;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Simple adapter allowing to switch easily between Search implementations with a property in sakai.properties.
 *
 * @author Colin Hebert
 */
public class SearchIndexBuilderAdapter implements SearchIndexBuilder {
    private static Logger log = LoggerFactory.getLogger(SearchIndexBuilderAdapter.class);
    private static final String SEARCH_BUILDER_IMPL_PROPERTY = "search.indexbuilder.impl";
    /**
     * Defaults to the elastic search implementation if nothing was provided.
     */
    private static final String DEFAULT_IMPL = "org.sakaiproject.search.elasticsearch.ElasticSearchIndexBuilder";
    private final SearchIndexBuilder searchIndexBuilder;

    public SearchIndexBuilderAdapter() {
        String searchIndexBuilderImplementation =
                ServerConfigurationService.getString(SEARCH_BUILDER_IMPL_PROPERTY, DEFAULT_IMPL);
        searchIndexBuilder = (SearchIndexBuilder) ComponentManager.get(searchIndexBuilderImplementation);
    }

    @Override
    public void addResource(Notification notification, Event event) {
        searchIndexBuilder.addResource(notification, event);
    }

    @Override
    public void registerEntityContentProducer(EntityContentProducer ecp) {
        searchIndexBuilder.registerEntityContentProducer(ecp);
    }

    @Override
    public void refreshIndex() {
        searchIndexBuilder.refreshIndex();
    }

    @Override
    public void rebuildIndex() {
        searchIndexBuilder.rebuildIndex();
    }

    @Override
    public boolean isBuildQueueEmpty() {
        return searchIndexBuilder.isBuildQueueEmpty();
    }

    @Override
    public List<EntityContentProducer> getContentProducers() {
        return searchIndexBuilder.getContentProducers();
    }

    @Override
    public void destroy() {
        searchIndexBuilder.destroy();
    }

    @Override
    public int getPendingDocuments() {
        return searchIndexBuilder.getPendingDocuments();
    }

    @Override
    public void rebuildIndex(String currentSiteId) {
        searchIndexBuilder.rebuildIndex(currentSiteId);
    }

    @Override
    public void refreshIndex(String currentSiteId) {
        searchIndexBuilder.refreshIndex(currentSiteId);
    }

    @Override
    public List<SearchBuilderItem> getAllSearchItems() {
        return searchIndexBuilder.getAllSearchItems();
    }

    @Override
    public EntityContentProducer newEntityContentProducer(Event event) {
        return searchIndexBuilder.newEntityContentProducer(event);
    }

    @Override
    public EntityContentProducer newEntityContentProducer(String ref) {
        return searchIndexBuilder.newEntityContentProducer(ref);
    }

    @Override
    public List<SearchBuilderItem> getSiteMasterSearchItems() {
        return searchIndexBuilder.getSiteMasterSearchItems();
    }

    @Override
    public List<SearchBuilderItem> getGlobalMasterSearchItems() {
        return searchIndexBuilder.getGlobalMasterSearchItems();
    }

    @Override
    public boolean isOnlyIndexSearchToolSites() {
        return searchIndexBuilder.isOnlyIndexSearchToolSites();
    }

    @Override
    public boolean isExcludeUserSites() {
        return searchIndexBuilder.isExcludeUserSites();
    }

    public void setOnlyIndexSearchToolSites(boolean onlyIndexSearchToolSites) {
        try {
            Method method = searchIndexBuilder.getClass().getMethod("setOnlyIndexSearchToolSites", new Class[]{boolean.class});
            method.invoke(searchIndexBuilder, onlyIndexSearchToolSites);
            log.trace("######### searchIndexBuilder.getSearchServer() = " + searchIndexBuilder.isOnlyIndexSearchToolSites());
        } catch (NoSuchMethodException e) {
            log.debug(searchIndexBuilder.getClass().getName() + " does not have a method called setOnlyIndexSearchToolSites.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    public void setExcludeUserSites(boolean excludeUserSites) {
        try {
            Method method = searchIndexBuilder.getClass().getMethod("setExcludeUserSites", new Class[]{boolean.class});
            method.invoke(searchIndexBuilder, excludeUserSites);
            log.trace("######### searchIndexBuilder.getSearchServer() = " + searchIndexBuilder.isExcludeUserSites());
        } catch (NoSuchMethodException e) {
            log.debug(searchIndexBuilder.getClass().getName() + " does not have a method called setExcludeUserSites.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }
}
