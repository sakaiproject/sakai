package org.sakaiproject.search.adapter;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.model.SearchBuilderItem;

import java.util.List;

/**
 * Simple adapter allowing to switch easily between Search implementations with a property in sakai.properties.
 *
 * @author Colin Hebert
 */
public class SearchIndexBuilderAdapter implements SearchIndexBuilder {
    private static final String SEARCH_BUILDER_IMPL_PROPERTY = "search.indexbuilder.impl";
    /**
     * Defaults to the elastic search implementation if nothing was provided.
     */
    private static final String DEFAULT_IMPL = "org.sakaiproject.search.component.service.impl.SearchIndexBuilderImpl";
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
}
