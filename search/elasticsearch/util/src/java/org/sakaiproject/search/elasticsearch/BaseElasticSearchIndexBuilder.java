/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.search.elasticsearch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.status.IndexStatus;
import org.elasticsearch.action.admin.indices.status.IndicesStatusRequest;
import org.elasticsearch.action.admin.indices.status.IndicesStatusResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.lang3.ArrayUtils;
import org.elasticsearch.common.lang3.tuple.ImmutablePair;
import org.elasticsearch.common.lang3.tuple.Pair;
import org.elasticsearch.common.settings.loader.JsonSettingsLoader;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.search.SearchHit;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.EntityContentProducerEvents;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchStatus;
import org.sakaiproject.search.elasticsearch.filter.SearchItemFilter;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.elasticsearch.ElasticSearchConstants;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.FilterBuilders.missingFilter;
import static org.elasticsearch.index.query.FilterBuilders.orFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.facet.FacetBuilders.termsFacet;

/**
 *
 */
public abstract class BaseElasticSearchIndexBuilder implements ElasticSearchIndexBuilder {

    protected static final String DEFAULT_FACET_NAME = "tag";
    protected static final String DEFAULT_SUGGESTION_MATCHING_FIELD_NAME = SearchService.FIELD_TITLE;

    /**
     * Key in the "validation map" built up by {@link #validateAddResourceEvent(Event)} (specifically
     * {@link #validateResourceName(Event, Map)}.
     */
    protected static final String ADD_RESOURCE_VALIDATION_KEY_RESOURCE_NAME = "RESOURCE_NAME";
    protected static final String ADD_RESOURCE_VALIDATION_KEY_CONTENT_PRODUCER = "CONTENT_PRODUCER";
    protected static final String ADD_RESOURCE_VALIDATION_KEY_INDEX_ACTION = "INDEX_ACTION";
    protected static final String ADD_RESOURCE_VALIDATION_KEY_ENTITY_ID = "ENTITY_ID";

    protected static final String DELETE_RESOURCE_KEY_DOCUMENT_ID = "DOCUMENT_ID";
    protected static final String DELETE_RESOURCE_KEY_ENTITY_REFERENCE = "ENTITY_REFERENCE";

    protected final static SecurityAdvisor allowAllAdvisor =
            (userId, function, reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;


    protected SecurityService securityService;
    protected ServerConfigurationService serverConfigurationService;
    protected EventTrackingService eventTrackingService;


    /**
     * ES Client, with access to any indexes in the cluster.
     */
    protected Client client;

    /**
     * the ES indexname
     */
    protected String indexName;

    /**
     * Logical, well-known name, possibly distinct from the physical ES {@link #indexName}
     */
    protected String name;

    /**
     * set to true to force an index rebuild at startup time, defaults to false.  This is probably something
     * you never want to use, other than in development or testing
     */
    protected boolean rebuildIndexOnStartup = false;

    protected boolean useSuggestions = true;

    /**
     * max number of suggestions to return when looking for suggestions (this populates the autocomplete drop down in the UI)
     */
    protected int maxNumberOfSuggestions = 10;

    protected String suggestionMatchingFieldName = DEFAULT_SUGGESTION_MATCHING_FIELD_NAME;

    protected String[] suggestionResultFieldNames;

    protected String[] searchResultFieldNames;

    protected SearchItemFilter filter;

    protected boolean useFacetting = true;

    protected String facetName = DEFAULT_FACET_NAME;

    /**
     *  N most frequent terms
     */
    protected int facetTermSize = 10;

    /**
     * Number of documents to index at a time for each run of the context indexing task (defaults to 500).
     * Setting this too low will slow things down, setting it to high won't allow all nodes in the cluster
     * to share the load.
     */
    protected int contentIndexBatchSize = 500;

    /**
     * Number of actions to send in one elasticsearch bulk index call
     * defaults to 10.  Setting this
     * to too high a number will have memory implications as you'll be keeping
     * more content in memory until the request is executed.
     */
    protected int bulkRequestSize = 10;

    /**
     * number seconds of wait after startup before starting the BulkContentIndexerTask (defaults to 3 minutes)
     */
    protected int delay = 180;

    /**
     * how often the BulkContentIndexerTask runs in seconds (defaults to 1 minute)
     */
    protected int period = 60;

    protected long startTime;

    protected long lastLoad;

    /**
     * this turns off the threads and does indexing inline.  DO NOT enable this in prod.
     * It is meant for testing, especially unit tests only.
     */
    protected boolean testMode = false;

    protected String defaultMappingResource = "/org/sakaiproject/search/elastic/bundle/mapping.json";

    /**
     * by default the mapping in configured in the mapping.json file.  This can be overridden by injecting
     * json into this property.
     *
     * See {@link <a href="http://www.elasticsearch.org/guide/reference/mapping/">elasticsearch mapping reference</a> } for
     * more information on configuration that is available.  For example, if you want to change the analyzer config for
     * a particular field this is the place to do it.
     */
    protected String mapping = null;

    /**
     * Combination of {@link #mapping}, its fallback read from {@link #defaultMappingResource}, and any overrides
     * implemented in {@link #initializeElasticSearchMapping(String)}. (Currently there are no such overrides in that
     * method... just the fallback resource lookup. And historically the mapping config was stored exclusively in
     * {@link #mapping}. This new {@code mappingMerged} field was added for symmetry with {@link #indexSettingsMerged}).
     */
    protected String mappingMerged = null;

    protected String defaultIndexSettingsResource = "/org/sakaiproject/search/elastic/bundle/indexSettings.json";

    /**
     * Expects a JSON string of ElasticSearch index settings.  You can set this in your
     * sakai.properties files and inject a value using the indexSettings@org.sakaiproject.search.api.SearchIndexBuilder
     * property.  By default this value is configured by the indexSettings.json files.
     *
     * See {@link <a href="http://www.elasticsearch.org/guide/reference/index-modules/">elasticsearch index modules</a>}
     * for more information on configuration that is available.
     */
    protected String indexSettings = null;

    /**
     * Combination of {@link #indexSettings}, its fallback read from {@link #defaultIndexSettingsResource} and
     * overrides read in from {@code ServerConfigurationService}. This is the operational set of index configs.
     * {@link #indexSettings} is just preserved for reference.
     */
    protected Map<String, String> indexSettingsMerged = new HashMap();

    protected String indexedDocumentType = null;

    /**
     * indexing thread that performs loading the actual content into the index.
     */
    protected Timer backgroundScheduler = null;

    protected Set<EntityContentProducer> producers = Sets.newConcurrentHashSet();

    protected Set<String> triggerFunctions = Sets.newHashSet();

    protected ElasticSearchIndexBuilderEventRegistrar eventRegistrar;


    public boolean isEnabled() {
        return serverConfigurationService.getBoolean("search.enable", false);
    }

    @Override
    public void destroy() {
        this.client = null;
        this.eventRegistrar = null;
    }

    @Override
    public void initialize(ElasticSearchIndexBuilderEventRegistrar eventRegistrar, Client client) {

        if (!isEnabled()) {
            getLog().debug("ElasticSearch is not enabled. Skipping initialization of index builder ["
                    + getName() + "]. Set search.enable=true to change that.");
            return;
        }

        if ( testMode ) {
            getLog().warn("IN TEST MODE for index builder [" + getName() + "]. DO NOT enable this in production !!!");
        }

        getLog().info("Initializing ElasticSearch index builder [" + getName() + "]...");

        this.eventRegistrar = eventRegistrar;
        this.client = client;

        beforeElasticSearchConfigInitialization();

        requireConfiguration();

        this.mappingMerged = initializeElasticSearchMapping(this.mapping);
        this.indexSettingsMerged = initializeElasticSearchIndexSettings(this.indexSettings);

        beforeBackgroundSchedulerInitialization();

        this.backgroundScheduler = initializeBackgroundScheduler();
        backgroundScheduler.schedule(initializeContentQueueProcessingTask(), (delay * 1000), (period * 1000));

        initializeIndex();

        this.eventRegistrar.updateEventsFor(this);
    }

    /**
     * Gives subclasses a chance to initialize configuration prior to reading/processing any
     * ES configs. May be important for setting up defaults, for example, or for ensuring
     * subclass-specific configs are in place before any background tasks are in place.
     * (Though the latter would be better factored into {@link #beforeBackgroundSchedulerInitialization()}
     */
    protected abstract void beforeElasticSearchConfigInitialization();

    protected void requireConfiguration() {
        Assert.hasText(name, "Must specify a logical name for this index builder");
        Assert.hasText(indexName, "Must specify a physical index name for this index builder");
        Assert.hasText(indexedDocumentType, "Must specify an indexed document type for this index builder");
    }

    /**
     * Called after all ES config has been processed but before the background scheduler has been set up
     * and before any index startup ops have been invoked ({@link #initializeIndex()}. I.e. this is a
     * subclass's last chance to set up any configs on which background jobs and/or index maintenance
     * in general might depend.
     */
    protected abstract void beforeBackgroundSchedulerInitialization();

    protected String initializeElasticSearchMapping(String injectedConfig) {
        // if there is a value here its been overridden by injection, we will use the overridden configuration
        String mappingConfig = injectedConfig;
        if (org.apache.commons.lang.StringUtils.isEmpty(injectedConfig)) {
            try {
                StringWriter writer = new StringWriter();
                IOUtils.copy(getClass().getResourceAsStream(this.defaultMappingResource), writer, "UTF-8");
                mappingConfig = writer.toString();
            } catch (Exception ex) {
                getLog().error("Failed to load mapping config: " + ex.getMessage(), ex);
            }
        }
        getLog().debug("ElasticSearch mapping will be configured as follows for index builder ["
                + getName() + "]:" + mappingConfig);
        return mappingConfig;
    }

    protected Map<String,String> initializeElasticSearchIndexSettings(String injectedConfig) {
        String defaultConfig = injectedConfig;
        if (org.apache.commons.lang.StringUtils.isEmpty(injectedConfig)) {
            try {
                StringWriter writer = new StringWriter();
                IOUtils.copy(getClass().getResourceAsStream(this.defaultIndexSettingsResource), writer, "UTF-8");
                defaultConfig = writer.toString();
            } catch (Exception ex) {
                getLog().error("Failed to load indexSettings config from ["+  this.defaultIndexSettingsResource
                        + "] for index builder [" + getName() + "]", ex);
            }
        }

        JsonSettingsLoader loader = new JsonSettingsLoader();
        Map<String, String> mergedConfig = null;

        try {
            mergedConfig = loader.load(defaultConfig);
        } catch (IOException e) {
            getLog().error("Problem loading indexSettings for index builder [" + getName() + "]", e);
        }

        // Set these here so we don't have to do this string concatenation
        // and comparison every time through the upcoming 'for' loop
        final boolean IS_DEFAULT = SearchIndexBuilder.DEFAULT_INDEX_BUILDER_NAME.equals(getName());
        final String DEFAULT_INDEX = ElasticSearchConstants.CONFIG_PROPERTY_PREFIX + "index.";
        final String LOCAL_INDEX = String.format("%s%s%s.",ElasticSearchConstants.CONFIG_PROPERTY_PREFIX,"index_",getName(),".");

        // load anything set into the ServerConfigurationService that starts with "elasticsearch.index."  this will
        // override anything set in the indexSettings config
        for (ServerConfigurationService.ConfigItem configItem : serverConfigurationService.getConfigData().getItems()) {
            String propertyName = configItem.getName();
            if (IS_DEFAULT && (propertyName.startsWith(DEFAULT_INDEX))) {
                propertyName = propertyName.replaceFirst(DEFAULT_INDEX, "index.");
                mergedConfig.put(propertyName, (String) configItem.getValue());
            } else if (propertyName.startsWith(LOCAL_INDEX)) {
                propertyName = propertyName.replaceFirst(LOCAL_INDEX, "index.");
                mergedConfig.put(propertyName, (String) configItem.getValue());
            }
        }

        if (getLog().isDebugEnabled()) {
            for (Map.Entry<String,String> entry : mergedConfig.entrySet()) {
                getLog().debug("Index property '" + entry.getKey() + "' set to: " + entry.getValue()
                        + "' for index builder '" + getName() + "'");
            }
        }

        return mergedConfig;
    }

    protected Timer initializeBackgroundScheduler() {
        // name is historical
        return new Timer("[elasticsearch content indexer " + getName() + "]", true);
    }

    protected TimerTask initializeContentQueueProcessingTask() {
        return testMode ? new NoOpTask() : newBulkContentIndexerTask();
    }

    protected static class NoOpTask extends TimerTask {
        @Override
        public void run() {
            // nothing to do
        }
    }

    /**
     * This is the task that searches for any docs in the search index that do not have content yet,
     * digests the content and loads it into the index.  Any docs with empty content will be removed from
     * the index.  This timer task is run by the timer thread based on the period configured elsewhere
     */
    protected class BulkContentIndexerTask extends TimerTask {
        @Override
        public void run() {
            try {
                getLog().debug("Running content indexing task for index builder [" + getName() + "]");
                enableAzgSecurityAdvisor();
                processContentQueue();
            } catch (Exception e) {
                getLog().error("Content indexing failure for index builder [" + getName() + "]", e);
            } finally {
                disableAzgSecurityAdvisor();
            }
        }
    }

    protected TimerTask newBulkContentIndexerTask() {
        return new BulkContentIndexerTask();
    }

    protected class RebuildIndexTask extends TimerTask {
        /**
         * Rebuild the index from the entities own stored state {@inheritDoc}
         */
        @Override
        public void run() {
            // let's not hog the whole CPU just in case you have lots of sites with lots of data this could take a bit
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
            rebuildIndexImmediately();
        }
    }

    protected TimerTask newRebuildIndexTask() {
        return new RebuildIndexTask();
    }

    /**
     * Searches for any docs in the search index that have not been indexed yet,
     * digests the content and loads it into the index.  Any docs with empty content will be removed from
     * the index.
     */
    protected void processContentQueue() {
        startTime = System.currentTimeMillis();

        // If there are a lot of docs queued up this could take awhile we don't want
        // to eat up all the CPU cycles.
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);

        if (getPendingDocuments() == 0) {
            getLog().trace("No pending docs for index builder [" + getName() + "]");
            return;
        }

        SearchResponse response = findContentQueue();

        SearchHit[] hits = response.getHits().hits();
        List<NoContentException> noContentExceptions = new ArrayList();
        getLog().debug(getPendingDocuments() + " pending docs for index builder [" + getName() + "]");

        BulkRequestBuilder bulkRequest = newContentQueueBulkUpdateRequestBuilder();

        for (SearchHit hit : hits) {

            if (bulkRequest.numberOfActions() < bulkRequestSize) {
                try {
                    processContentQueueEntry(hit, bulkRequest);
                } catch ( NoContentException e ) {
                    noContentExceptions.add(e);
                }
            } else {
                executeBulkRequest(bulkRequest);
                bulkRequest = newContentQueueBulkUpdateRequestBuilder();
            }
        }

        // execute any remaining bulks requests not executed yet
        if (bulkRequest.numberOfActions() > 0) {
            executeBulkRequest(bulkRequest);
        }

        // remove any docs without content, so we don't try to index them again
        if (!noContentExceptions.isEmpty()) {
            for (NoContentException noContentException : noContentExceptions) {
                deleteDocument(noContentException);
            }
        }

        lastLoad = System.currentTimeMillis();

        if (hits.length > 0) {
            getLog().info("Finished indexing " + hits.length + " docs in " +
                    ((lastLoad - startTime)) + " ms for index builder " + getName());
        }

    }

    protected void processContentQueueEntry(SearchHit hit, BulkRequestBuilder bulkRequest) throws NoContentException {
        String reference = getFieldFromSearchHit(SearchService.FIELD_REFERENCE, hit);
        EntityContentProducer ecp = newEntityContentProducer(reference);

        if (ecp != null) {
            //updating was causing issues without a _source, so doing delete and re-add
            try {
                deleteDocument(hit);
                bulkRequest.add(prepareIndex(reference, ecp, true));
            } catch (NoContentException e) {
                throw e;
            } catch (Exception e) {
                getLog().error("Failed to process content queue entry with id [" + hit.getId() + "] in index builder ["
                        + getName() + "]", e);
            }
        } else {
            noContentProducerForContentQueueEntry(hit, reference);
        }
    }

    protected void executeBulkRequest(BulkRequestBuilder bulkRequest) {
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();

        getLog().info("Bulk request of batch size: " + bulkRequest.numberOfActions() + " took "
                + bulkResponse.getTookInMillis() + " ms in index builder: " + getName());

        for (BulkItemResponse response : bulkResponse.getItems()) {
            if (response.getResponse() instanceof DeleteResponse) {
                DeleteResponse deleteResponse = response.getResponse();

                if (response.isFailed()) {
                    getLog().error("Problem deleting doc: " + response.getId() + " in index builder: " + getName()
                            + " error: " + response.getFailureMessage());
                } else if (!deleteResponse.isFound()) {
                    getLog().debug("ES could not find a doc with id: " + deleteResponse.getId()
                            + " to delete in index builder: " + getName());
                } else {
                    getLog().debug("ES deleted a doc with id: " + deleteResponse.getId() + " in index builder: "
                            + getName());
                }
            } else if (response.getResponse() instanceof IndexResponse) {
                IndexResponse indexResponse = response.getResponse();

                if (response.isFailed()) {
                    getLog().error("Problem updating content for doc: " + response.getId() + " in index builder: "
                            + getName() + " error: " + response.getFailureMessage());
                } else {
                    getLog().debug("ES indexed content for doc with id: " + indexResponse.getId()
                            + " in index builder: " + getName());
                }
            }
        }
    }

    protected void noContentProducerForContentQueueEntry(SearchHit hit, String reference) throws NoContentException {
        // if there is no content to index remove the doc, its pointless to have it included in the index
        // and we will just waste cycles looking at it again everytime this thread runs, and will probably
        // never finish because of it.
        throw new NoContentException(hit.getId(), reference, null);
    }

    protected BulkRequestBuilder newContentQueueBulkUpdateRequestBuilder() {
        return client.prepareBulk();
    }

    protected SearchResponse findContentQueue() {
        SearchRequestBuilder searchRequestBuilder = prepareFindContentQueue();
        return findContentQueueWithRequest(searchRequestBuilder);
    }

    protected SearchRequestBuilder prepareFindContentQueue() {
        SearchRequestBuilder searchRequestBuilder = newFindContentQueueRequestBuilder();
        searchRequestBuilder = addFindContentQueueRequestParams(searchRequestBuilder);
        searchRequestBuilder = completeFindContentQueueRequestBuilder(searchRequestBuilder);
        return searchRequestBuilder;
    }

    protected SearchRequestBuilder newFindContentQueueRequestBuilder() {
        return client.prepareSearch(indexName);
    }

    protected SearchRequestBuilder addFindContentQueueRequestParams(SearchRequestBuilder searchRequestBuilder) {
        return searchRequestBuilder
                .setQuery(matchAllQuery())
                .setTypes(indexedDocumentType)
                .setPostFilter( orFilter(
                        missingFilter(SearchService.FIELD_INDEXED),
                        termFilter(SearchService.FIELD_INDEXED, false)))
                .setSize(contentIndexBatchSize)
                .addFields(SearchService.FIELD_REFERENCE, SearchService.FIELD_SITEID);
    }

    protected abstract SearchRequestBuilder completeFindContentQueueRequestBuilder(SearchRequestBuilder searchRequestBuilder);

    protected SearchResponse findContentQueueWithRequest(SearchRequestBuilder searchRequestBuilder) {
        return searchRequestBuilder.execute().actionGet();
    }

    protected void deleteDocument(SearchHit searchHit) {
        final Map<String,Object> deleteParams = extractDeleteDocumentParams(searchHit);
        deleteDocumentWithParams(deleteParams);
    }

    protected void deleteDocument(NoContentException noContentException) {
        final Map<String, Object> deleteParams = extractDeleteDocumentParams(noContentException);
        deleteDocumentWithParams(deleteParams);
    }

    protected void deleteDocumentWithParams(Map<String, Object> deleteParams) {
        final DeleteRequestBuilder deleteRequestBuilder = prepareDeleteDocument(deleteParams);
        final DeleteResponse deleteResponse = deleteDocumentWithRequest(deleteRequestBuilder);

        if (getLog().isDebugEnabled()) {
            if (!deleteResponse.isFound()) {
                getLog().debug("Could not delete doc with by id: " + deleteParams.get(DELETE_RESOURCE_KEY_DOCUMENT_ID)
                        + " in index builder [" + getName() + "] because the document wasn't found");
            } else {
                getLog().debug("ES deleted a doc with id: " + deleteResponse.getId() + " in index builder ["
                        + getName() + "]");
            }
        }
    }

    protected DeleteRequestBuilder prepareDeleteDocument(Map<String, Object> deleteParams) {
        DeleteRequestBuilder deleteRequestBuilder = newDeleteRequestBuilder(deleteParams);
        deleteRequestBuilder = completeDeleteRequestBuilder(deleteRequestBuilder, deleteParams);
        return deleteRequestBuilder;
    }

    protected abstract DeleteRequestBuilder completeDeleteRequestBuilder(DeleteRequestBuilder deleteRequestBuilder,
                                                                         Map<String, Object> deleteParams);

    protected DeleteResponse deleteDocumentWithRequest(DeleteRequestBuilder deleteRequestBuilder) {
        return deleteRequestBuilder.execute().actionGet();
    }

    private DeleteRequestBuilder newDeleteRequestBuilder(Map<String, Object> deleteParams) {
        return client.prepareDelete(indexName, indexedDocumentType, (String)deleteParams.get(DELETE_RESOURCE_KEY_DOCUMENT_ID));
    }

    protected Map<String, Object> extractDeleteDocumentParams(SearchHit searchHit) {
        final Map<String, Object> params = Maps.newHashMap();
        params.put(DELETE_RESOURCE_KEY_DOCUMENT_ID, searchHit.getId());
        return params;
    }

    protected Map<String, Object> extractDeleteDocumentParams(NoContentException noContentException) {
        final Map<String, Object> params = Maps.newHashMap();
        params.put(DELETE_RESOURCE_KEY_DOCUMENT_ID, noContentException.getId());
        params.put(DELETE_RESOURCE_KEY_ENTITY_REFERENCE, noContentException.getReference());
        return params;
    }

    protected void initializeIndex() {
        // init index and kick off rebuild if necessary
        if (rebuildIndexOnStartup) {
            rebuildIndex();
        } else {
            assureIndex();
        }
    }

    /**
     * creates a new index if one does not exist
     */
    protected void assureIndex() {
        IndicesExistsResponse response = client.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet();
        if (!response.isExists()) {
            createIndex();
        }
    }

    /**
     * creates a new index, does not check if the exist exists
     */
    protected void createIndex() {
        try {
            CreateIndexResponse createResponse = client.admin().indices().create(new CreateIndexRequest(indexName)
                    .settings(indexSettingsMerged).mapping(indexedDocumentType, mappingMerged)).actionGet();
            if (!createResponse.isAcknowledged()) {
                getLog().error("Index wasn't created for index builder [" + getName() + "], can't rebuild");
            }
        } catch (IndexAlreadyExistsException e) {
            getLog().warn("Index already created for index builder [" + getName() + "]");
        }
    }


    /**
     * removes any existing index and creates a new one
     */
    protected void recreateIndex() {
        IndicesExistsResponse response = client.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet();
        if (response.isExists()) {
            client.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();
        }

        // create index
        createIndex();
    }

    /**
     * Removes any existing index, creates a new index, and rebuilds the index from the entities own stored state {@inheritDoc}
     */
    @Override
    public void rebuildIndex() {
        recreateIndex();

        if (testMode) {
            rebuildIndexImmediately();
            return;
        }

        backgroundScheduler.schedule(newRebuildIndexTask(), 0);
    }

    protected abstract void rebuildIndexImmediately();

    /**
     * refresh the index from the current stored state {@inheritDoc}
     */
    public void refreshIndex() {
        RefreshResponse response = client.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
    }

    /**
     *
     * @param resourceName
     * @param ecp
     * @return
     */
    protected IndexRequestBuilder prepareIndex(String resourceName, EntityContentProducer ecp, boolean includeContent)
            throws IOException, NoContentException {
        IndexRequestBuilder requestBuilder = newIndexRequestBuilder(resourceName, ecp, includeContent);
        final XContentBuilder requestContentSource = buildIndexRequestContentSource(resourceName, ecp, includeContent);
        requestBuilder = requestBuilder.setSource(requestContentSource);
        return completeIndexRequestBuilder(requestBuilder, resourceName, ecp, includeContent);
    }

    protected IndexRequestBuilder newIndexRequestBuilder(String resourceName, EntityContentProducer ecp,
                                                         boolean includeContent)
            throws IOException {
        return client.prepareIndex(indexName, indexedDocumentType, ecp.getId(resourceName));
    }

    protected abstract IndexRequestBuilder completeIndexRequestBuilder(IndexRequestBuilder requestBuilder,
                                                    String resourceName, EntityContentProducer ecp,
                                                    boolean includeContent)
            throws IOException;

    protected  XContentBuilder buildIndexRequestContentSource(String resourceName, EntityContentProducer ecp,
                                                              boolean includeContent)
            throws NoContentException, IOException {
        XContentBuilder requestBuilder = newIndexRequestContentSourceBuilder(resourceName, ecp, includeContent);
        requestBuilder = addFields(requestBuilder, resourceName, ecp, includeContent);
        requestBuilder = addCustomProperties(requestBuilder, resourceName, ecp, includeContent);
        requestBuilder = addContent(requestBuilder, resourceName, ecp, includeContent);
        return completeIndexRequestContentSourceBuilder(requestBuilder, resourceName, ecp, includeContent);
    }

    protected XContentBuilder newIndexRequestContentSourceBuilder(String resourceName, EntityContentProducer ecp,
                                                                  boolean includeContent) throws IOException {
        return jsonBuilder().startObject();
    }

    protected abstract XContentBuilder addFields(XContentBuilder contentSourceBuilder, String resourceName,
                                                 EntityContentProducer ecp, boolean includeContent) throws IOException;

    protected XContentBuilder addCustomProperties(XContentBuilder contentSourceBuilder, String resourceName,
                                                  EntityContentProducer ecp,
                                                  boolean includeContent) throws IOException {
        Map<String, Collection<String>> properties = extractCustomProperties(resourceName, ecp);
        for (Map.Entry<String, Collection<String>> entry : properties.entrySet()) {
            contentSourceBuilder = contentSourceBuilder.field(entry.getKey(), entry.getValue());
        }
        return contentSourceBuilder;
    }

    protected XContentBuilder addContent(XContentBuilder contentSourceBuilder, String resourceName,
                                         EntityContentProducer ecp,
                                         boolean includeContent) throws NoContentException, IOException {
        if (includeContent || testMode) {
            String content = ecp.getContent(resourceName);
            // some of the ecp impls produce content with nothing but whitespace, its waste of time to index those
            if (StringUtils.isNotBlank(content)) {
                return contentSourceBuilder
                        // cannot rely on ecp for providing something reliable to maintain index state
                        // indexed indicates if the document was indexed
                        .field(SearchService.FIELD_INDEXED, true)
                        .field(SearchService.FIELD_CONTENTS, content);
            } else {
                return noContentForIndexRequest(contentSourceBuilder, resourceName, ecp, includeContent);
            }
        }
        return contentSourceBuilder;
    }

    protected abstract XContentBuilder noContentForIndexRequest(XContentBuilder contentSourceBuilder,
                                                                String resourceName, EntityContentProducer ecp,
                                                                boolean includeContent)
            throws NoContentException ;

    protected XContentBuilder completeIndexRequestContentSourceBuilder(XContentBuilder contentSourceBuilder,
                                                                       String resourceName, EntityContentProducer ecp,
                                                                       boolean includeContent) throws IOException {
        return contentSourceBuilder.endObject();
    }

    /**
     *
     * @param resourceName
     * @param ecp
     * @return
     */
    protected void prepareIndexAdd(String resourceName, EntityContentProducer ecp, boolean includeContent) throws NoContentException {
        try {
            prepareIndex(resourceName, ecp, includeContent).execute().actionGet();
        } catch (NoContentException e) {
            throw e;
        } catch (Throwable t) {
            getLog().error("Error: trying to register resource " + resourceName
                    + " in index builder: " + getName(), t);
        }

    }

    /**
     * schedules content for indexing.
     * @param resourceName
     * @param ecp
     * @return
     */
    protected void indexAdd(String resourceName, EntityContentProducer ecp) {
        try {
            prepareIndexAdd(resourceName, ecp, false);
        } catch (NoContentException e) {
            deleteDocument(e);
        } catch (Exception e) {
            getLog().error("Problem updating content indexing in index builder: " + getName()
                    + " for entity: " + resourceName, e);
        }
    }

    @Override
    public int getPendingDocuments() {
        try {
            CountResponse response = client.prepareCount(indexName)
                    .setQuery(filteredQuery(matchAllQuery(), orFilter(
                            missingFilter(SearchService.FIELD_INDEXED),
                            termFilter(SearchService.FIELD_INDEXED, false))))
                    .execute()
                    .actionGet();
            return (int) response.getCount();
        } catch (Exception e) {
            getLog().error("Problem getting pending docs for index builder [" + getName() + "]", e);
        }
        return 0;
    }

    @Override
    public boolean isBuildQueueEmpty() {
        return getPendingDocuments() == 0;
    }

    @Override
    public void addResource(Notification notification, Event event) {
        getLog().debug("Add resource " + notification + "::" + event + " in index builder " + getName());

        final Map<String, Object> validationContext;
        try {
            validationContext = validateAddResourceEvent(event);
        } catch ( Exception e ) {
            // Only debug level b/c almost all runtime validation failures will be uninteresting.
            // In almost all cases they'll be caused by either the absence of a capable SearchableContentHandler,
            // and actually that can be expected to happen *a lot* since the historical EntityContentProducer
            // registration mechanism set events of interest on the search *service*, not on the index builder to
            // which it (the producer) was actually bound. And that registration does not include a pointer back to the
            // registering producer. So without modifying all providers in the wild, index builders will just have
            // to deal with event storms they don't care about.
            getLog().debug("Skipping index for event " + event + " in index builder [" + getName()
                    + "] because it did not validate", e);
            return;
        }

        dispatchValidatedAddResource(validationContext);
    }

    protected Map<String,Object> validateAddResourceEvent(Event event)
            throws IllegalArgumentException, IllegalStateException {
        final Map<String,Object> validationContext = Maps.newHashMap();
        validateServiceEnabled(event, validationContext);
        validateResourceName(event, validationContext);
        validateContentProducer(event, validationContext);
        validateIndexable(event, validationContext);
        validateIndexAction(event, validationContext);
        completeAddResourceEventValidations(event, validationContext);
        return validationContext;
    }

    protected void validateServiceEnabled(Event event, Map<String, Object> validationContext)
            throws IllegalArgumentException, IllegalStateException {
        if (!isEnabled()) {
            throw new IllegalStateException("ElasticSearch is not enabled. Set search.enable=true to change that.");
        }
    }

    protected void validateResourceName(Event event, Map<String, Object> validationContext)
            throws IllegalArgumentException, IllegalStateException {
        String resourceName = event.getResource();
        if (resourceName == null) {
            // default if null (historical behavior)
            resourceName = "";
        }
        if (resourceName.length() > 255) {
            throw new IllegalArgumentException("Entity Reference is longer than 255 characters. Reference="
                    + resourceName);
        }
        validationContext.put(ADD_RESOURCE_VALIDATION_KEY_RESOURCE_NAME, resourceName);
    }

    protected void validateContentProducer(Event event, Map<String, Object> validationContext)
            throws IllegalArgumentException, IllegalStateException {
        final EntityContentProducer ecp = newEntityContentProducer(event);

        if (ecp == null) {
            throw new IllegalArgumentException("No registered SearchableContentProducer for event [" + event
                    + "] in indexBuilder [" + getName() + "]");
        }
        validationContext.put(ADD_RESOURCE_VALIDATION_KEY_CONTENT_PRODUCER, ecp);
    }

    protected void validateIndexable(Event event, Map<String, Object> validationContext)
            throws IllegalArgumentException, IllegalStateException {
        final EntityContentProducer ecp = (EntityContentProducer)validationContext.get(ADD_RESOURCE_VALIDATION_KEY_CONTENT_PRODUCER);
        final String resourceName = (String)validationContext.get(ADD_RESOURCE_VALIDATION_KEY_RESOURCE_NAME);
        final String id = ecp.getId(resourceName);
        if ( StringUtils.isEmpty(id) ) {
            throw new IllegalArgumentException("Entity ID could not be derived from resource name [" + resourceName
                    + "] for event [" + event + "] in index builder [" + getName() + "]");
        }
        validationContext.put(ADD_RESOURCE_VALIDATION_KEY_ENTITY_ID, id);
    }

    protected void validateIndexAction(Event event, Map<String, Object> validationContext)
            throws IllegalArgumentException, IllegalStateException, UnsupportedOperationException {
        final EntityContentProducer ecp = (EntityContentProducer)validationContext.get(ADD_RESOURCE_VALIDATION_KEY_CONTENT_PRODUCER);
        final IndexAction action = IndexAction.getAction(ecp.getAction(event));
        if ( !(isSupportedIndexAction(action)) ) {
            throw new UnsupportedOperationException("Event [" + event
                    + "] resolved to an unsupported IndexAction [" + action + "] in index builder [" + getName() + "]");
        }
        validationContext.put(ADD_RESOURCE_VALIDATION_KEY_INDEX_ACTION, action);
    }

    protected boolean isSupportedIndexAction(IndexAction action) {
        return IndexAction.ADD.equals(action) || IndexAction.DELETE.equals(action);
    }

    protected abstract void completeAddResourceEventValidations(Event event, Map<String, Object> validationContext)
            throws IllegalArgumentException, IllegalStateException;

    protected void dispatchValidatedAddResource(Map<String, Object> validationContext) {
        final IndexAction indexAction = (IndexAction)validationContext.get(ADD_RESOURCE_VALIDATION_KEY_INDEX_ACTION);
        final String resourceName = (String)validationContext.get(ADD_RESOURCE_VALIDATION_KEY_RESOURCE_NAME);
        final EntityContentProducer ecp = (EntityContentProducer)validationContext.get(ADD_RESOURCE_VALIDATION_KEY_CONTENT_PRODUCER);
        getLog().debug("Action on '" + resourceName + "' detected as " + indexAction.name() + " in index builder "
                + getName());

        switch (indexAction) {
            case ADD:
                indexAdd(resourceName, ecp);
                break;
            case DELETE:
                deleteDocumentWithParams(extractDeleteDocumentParams(validationContext));
                break;
            default:
                // Should never happen if validation process was implemented correctly
                throw new UnsupportedOperationException(indexAction + " is not supported in index builder " + getName());
        }
    }

    protected Map<String, Object> extractDeleteDocumentParams(Map<String, Object> validationContext) {
        final Map<String,Object> params = Maps.newHashMap();
        params.put(DELETE_RESOURCE_KEY_DOCUMENT_ID, validationContext.get(ADD_RESOURCE_VALIDATION_KEY_ENTITY_ID));
        return params;
    }

    /**
     * Extract properties from the given {@link EntityContentProducer}
     * <p>
     * The {@link EntityContentProducer#getCustomProperties(String)} method returns a map of different kind of elements.
     * To avoid casting and calls to {@code instanceof}, extractCustomProperties does all the work and returns a formated
     * map containing only {@link Collection<String>}.
     * </p>
     *
     * @param resourceName    affected resource
     * @param contentProducer producer providing properties for the given resource
     * @return a formated map of {@link Collection<String>}
     */
    protected Map<String, Collection<String>> extractCustomProperties(String resourceName, EntityContentProducer contentProducer) {
        Map<String, ?> m = contentProducer.getCustomProperties(resourceName);

        if (m == null)
            return Collections.emptyMap();

        Map<String, Collection<String>> properties = new HashMap<String, Collection<String>>(m.size());
        for (Map.Entry<String, ?> propertyEntry : m.entrySet()) {
            String propertyName = propertyEntry.getKey();
            Object propertyValue = propertyEntry.getValue();
            Collection<String> values;

            //Check for basic data type that could be provided by the EntityContentProducer
            //If the data type can't be defined, nothing is stored. The toString method could be called, but some values
            //could be not meant to be indexed.
            if (propertyValue instanceof String)
                values = Collections.singleton((String) propertyValue);
            else if (propertyValue instanceof String[])
                values = Arrays.asList((String[]) propertyValue);
            else if (propertyValue instanceof Collection)
                values = (Collection<String>) propertyValue;
            else {
                if (propertyValue != null)
                    getLog().warn("Couldn't find what the value for '" + propertyName + "' was. It has been ignored. " + propertyName.getClass());
                values = Collections.emptyList();
            }

            //If this property was already present there (this shouldn't happen, but if it does everything must be stored
            if (properties.containsKey(propertyName)) {
                getLog().warn("Two properties had a really similar name and were merged. This shouldn't happen! " + propertyName);
                getLog().debug("Merged values '" + properties.get(propertyName) + "' with '" + values);
                values = new ArrayList<String>(values);
                values.addAll(properties.get(propertyName));
            }

            properties.put(propertyName, values);
        }

        return properties;
    }


    @Override
    public SearchResponse search(String searchTerms, List<String> references, List<String> siteIds, int start, int end) {

        final Pair<SearchRequestBuilder,QueryBuilder> searchBuilders = prepareSearchRequest(searchTerms, references, siteIds, start, end);
        final SearchRequestBuilder searchRequestBuilder = searchBuilders.getLeft();
        final QueryBuilder queryBuilder = searchBuilders.getRight();

        getLog().debug("Search request from index builder [" + getName() + "]: " + searchRequestBuilder.toString());
        SearchResponse response = searchRequestBuilder.execute().actionGet();
        getLog().debug("Search request from index builder [" + getName() + "] took: " + response.getTook().format());

        eventTrackingService.post(eventTrackingService.newEvent(SearchService.EVENT_SEARCH,
                SearchService.EVENT_SEARCH_REF + queryBuilder.toString(), true,
                NotificationService.PREF_IMMEDIATE));

        return response;
    }

    @Override
    public SearchResponse search(String searchTerms, List<String> references, List<String> siteIds, int start, int end, Map<String,String> additionalSearchInformation) {
        //The additional information will be used in specific indexes, so this method can be overrided in the index
        // to make use of that field.
        return search(searchTerms,references,siteIds,start,end);

    }

    protected Pair<SearchRequestBuilder,QueryBuilder> prepareSearchRequest(String searchTerms, List<String> references, List<String> siteIds, int start, int end) {
        // All this Pair<SearchRequestBuilder,QueryBuilder> business b/c:
        //    a) Legacy eventing in search() needs the QueryBuilder, not just the SearchRequestBuilder, and
        //    b) SiteId handling entails manipulation of both objects, so presumably completeSearchRequestBuilders()
        //       would as well
        //    c) There is no getQuery() on SearchRequestBuilder
        Pair<SearchRequestBuilder,QueryBuilder> builders = newSearchRequestAndQueryBuilders(searchTerms, references, siteIds);
        builders = addSearchCoreParams(builders, searchTerms, references, siteIds);
        builders = addSearchQuery(builders, searchTerms, references, siteIds);
        builders = pairOf(addSearchResultFields(builders.getLeft()), builders.getRight());
        builders = pairOf(addSearchPagination(builders.getLeft(), start, end), builders.getRight());
        builders = pairOf(addSearchFacetting(builders.getLeft()), builders.getRight());
        return completeSearchRequestBuilders(builders, searchTerms, references, siteIds);
    }

    protected Pair<SearchRequestBuilder,QueryBuilder> newSearchRequestAndQueryBuilders(String searchTerms,
                                                                                       List<String> references,
                                                                                       List<String> siteIds) {
        return pairOf(client.prepareSearch(indexName), boolQuery());
    }

    protected Pair<SearchRequestBuilder, QueryBuilder> addSearchCoreParams(Pair<SearchRequestBuilder, QueryBuilder> builders,
                                                                           String searchTerms, List<String> references,
                                                                           List<String> siteIds) {
        final SearchRequestBuilder searchRequestBuilder = builders.getLeft();
        return pairOf(searchRequestBuilder.setSearchType(SearchType.QUERY_THEN_FETCH).setTypes(indexedDocumentType),
                builders.getRight());
    }

    protected Pair<SearchRequestBuilder,QueryBuilder> addSearchQuery(Pair<SearchRequestBuilder,QueryBuilder> builders,
                                                                     String searchTerms, List<String> references,
                                                                     List<String> siteIds) {
        builders = addSearchTerms(builders, searchTerms);
        builders = addSearchReferences(builders, references);
        builders = addSearchSiteIds(builders, siteIds);
        return pairOf(builders.getLeft().setQuery(builders.getRight()), builders.getRight());
    }

    protected Pair<SearchRequestBuilder, QueryBuilder> addSearchTerms(Pair<SearchRequestBuilder, QueryBuilder> builders,
                                                                      String searchTerms) {
        BoolQueryBuilder query = (BoolQueryBuilder)builders.getRight();

        if (searchTerms.contains(":")) {
            String[] termWithType = searchTerms.split(":");
            String termType = termWithType[0];
            String termValue = termWithType[1];
            // little fragile but seems like most providers follow this convention, there isn't a nice way to get the type
            // without a handle to a reference.
            query = query.must(termQuery(SearchService.FIELD_TYPE, "sakai:" + termType));
            query = query.must(matchQuery(SearchService.FIELD_CONTENTS, termValue));
        } else {
            query = query.must(matchQuery(SearchService.FIELD_CONTENTS, searchTerms));
        }

        return pairOf(builders.getLeft(), query);
    }

    protected Pair<SearchRequestBuilder, QueryBuilder> addSearchReferences(Pair<SearchRequestBuilder, QueryBuilder> builders,
                                                                           List<String> references) {
        BoolQueryBuilder query = (BoolQueryBuilder)builders.getRight();
        if (references.size() > 0){
            query = query.must(termsQuery(SearchService.FIELD_REFERENCE, references.toArray(new String[references.size()])));
        }
        return pairOf(builders.getLeft(), query);
    }

    protected abstract Pair<SearchRequestBuilder,QueryBuilder> addSearchSiteIds(Pair<SearchRequestBuilder,QueryBuilder> builders,
                                                                                List<String> siteIds);

    protected  SearchRequestBuilder addSearchResultFields(SearchRequestBuilder searchRequestBuilder) {
        if (ArrayUtils.isEmpty(searchResultFieldNames)) {
            return searchRequestBuilder;
        }
        return searchRequestBuilder.addFields(searchResultFieldNames);
    }

    protected SearchRequestBuilder addSearchPagination(SearchRequestBuilder searchRequestBuilder, int start, int end) {
        return searchRequestBuilder.setFrom(start).setSize(end - start);
    }

    protected SearchRequestBuilder addSearchFacetting(SearchRequestBuilder searchRequestBuilder) {
        if(useFacetting) {
            return searchRequestBuilder.addFacet(termsFacet(facetName).field("contents.lowercase").size(facetTermSize));
        }
        return searchRequestBuilder;
    }

    protected abstract Pair<SearchRequestBuilder,QueryBuilder> completeSearchRequestBuilders(Pair<SearchRequestBuilder,QueryBuilder> builders,
                                                                                             String searchTerms,
                                                                                             List<String> references,
                                                                                             List<String> siteIds);


    @Override
    public String[] searchSuggestions(String searchString, String currentSite, boolean allMySites) {
        if (!useSuggestions) {
            return new String[0];
        }

        final Pair<SearchRequestBuilder,QueryBuilder> builders =
                prepareSearchSuggestionsRequest(searchString, currentSite, allMySites);

        final SearchRequestBuilder searchRequestBuilder = builders.getLeft();

        getLog().debug("Search request from index builder [" + getName() + "]: " + searchRequestBuilder);

        SearchResponse response = searchRequestBuilder.execute().actionGet();

        getLog().debug("Search request from index builder [" + getName() + "] took: " + response.getTook().format());

        List<String> suggestions = Lists.newArrayList();

        for (SearchHit hit : response.getHits()) {
            suggestions.add(getFieldFromSearchHit(suggestionMatchingFieldName, hit));
        }

        return suggestions.toArray(new String[suggestions.size()]);
    }

    protected Pair<SearchRequestBuilder, QueryBuilder> prepareSearchSuggestionsRequest(String searchString, String currentSite, boolean allMySites) {
        Pair<SearchRequestBuilder,QueryBuilder> builders =
                newSearchSuggestionsRequestAndQueryBuilders(searchString, currentSite, allMySites);
        builders = addSearchSuggestionsCoreParams(builders, searchString, currentSite, allMySites);
        builders = addSearchSuggestionsQuery(builders, searchString, currentSite, allMySites);
        builders = pairOf(addSearchSuggestionResultFields(builders.getLeft()), builders.getRight());
        builders = pairOf(addSearchSuggestionsPagination(builders.getLeft()), builders.getRight());
        return completeSearchSuggestionsRequestBuilders(builders, searchString, currentSite, allMySites);
    }

    protected abstract Pair<SearchRequestBuilder,QueryBuilder> completeSearchSuggestionsRequestBuilders(Pair<SearchRequestBuilder, QueryBuilder> builders,
                                                                                                        String searchString,
                                                                                                        String currentSite,
                                                                                                        boolean allMySites);

    protected Pair<SearchRequestBuilder, QueryBuilder> newSearchSuggestionsRequestAndQueryBuilders(String searchString,
                                                                                                   String currentSite,
                                                                                                   boolean allMySites) {
        return pairOf(client.prepareSearch(indexName), termQuery(suggestionMatchingFieldName, searchString));
    }

    protected Pair<SearchRequestBuilder, QueryBuilder> addSearchSuggestionsCoreParams(Pair<SearchRequestBuilder, QueryBuilder> builders, String searchString, String currentSite, boolean allMySites) {
        final SearchRequestBuilder searchRequestBuilder = builders.getLeft();
        return pairOf(searchRequestBuilder.setSearchType(SearchType.QUERY_THEN_FETCH).setTypes(indexedDocumentType),
                builders.getRight());
    }

    protected Pair<SearchRequestBuilder, QueryBuilder> addSearchSuggestionsQuery(Pair<SearchRequestBuilder, QueryBuilder> builders,
                                                                                 String searchString,
                                                                                 String currentSite,
                                                                                 boolean allMySites) {
        builders = addSearchSuggestionsTerms(builders, searchString);
        builders = addSearchSuggestionsSites(builders, currentSite, allMySites);
        return pairOf(builders.getLeft().setQuery(builders.getRight()), builders.getRight());
    }

    protected abstract Pair<SearchRequestBuilder,QueryBuilder> addSearchSuggestionsTerms(Pair<SearchRequestBuilder, QueryBuilder> builders,
                                                                                         String searchString);

    protected abstract Pair<SearchRequestBuilder, QueryBuilder> addSearchSuggestionsSites(Pair<SearchRequestBuilder, QueryBuilder> builders, String currentSite, boolean allMySites);

    protected SearchRequestBuilder addSearchSuggestionResultFields(SearchRequestBuilder searchRequestBuilder) {
        if (ArrayUtils.isEmpty(suggestionResultFieldNames)) {
            return searchRequestBuilder;
        }
        return searchRequestBuilder.addFields(suggestionResultFieldNames);
    }

    protected SearchRequestBuilder addSearchSuggestionsPagination(SearchRequestBuilder searchRequestBuilder) {
        return searchRequestBuilder.setSize(maxNumberOfSuggestions);
    }


    public StringBuilder getStatus(StringBuilder into) {
        assureIndex();
        IndicesStatusResponse response = client.admin().indices().status(new IndicesStatusRequest(indexName)).actionGet();
        IndexStatus status = response.getIndices().get(indexName);

        long pendingDocs = getPendingDocuments();

        into.append("Index builder: ").append(getName());
        if (pendingDocs != 0) {
            into.append(" active. " + pendingDocs + " pending items in queue. ");
        } else {
            into.append(" idle. ");
        }

        into.append("Index Size: " + roundTwoDecimals(status.getStoreSize().getGbFrac()) + " GB" +
                " Refresh Time: " + status.getRefreshStats().getTotalTimeInMillis() + "ms" +
                " Flush Time: " + status.getFlushStats().getTotalTimeInMillis() + "ms" +
                " Merge Time: " + status.getMergeStats().getTotalTimeInMillis() + "ms");

        return into;
    }

    protected double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    @Override
    public int getNDocs() {
        assureIndex();
        CountResponse response = client.prepareCount(indexName)
                .setQuery(filteredQuery(matchAllQuery(),termFilter(SearchService.FIELD_INDEXED, true)))
                .execute()
                .actionGet();
        return (int) response.getCount();
    }

    @Override
    public SearchStatus getSearchStatus() {

        final String lastLoadStr = new Date(lastLoad).toString();
        final String loadTimeStr = String.valueOf((double) (0.001 * lastLoad));
        final String pdocs = String.valueOf(getPendingDocuments());
        final String ndocs = String.valueOf(getNDocs());


        return new SearchStatus() {
            public String getLastLoad() {
                return lastLoadStr;
            }

            public String getLoadTime() {
                return loadTimeStr;
            }

            public String getCurrentWorker() {
                return null;
            }

            public String getCurrentWorkerETC() {
                return null;
            }

            public List getWorkerNodes() {
                return Collections.EMPTY_LIST;
            }

            public String getNDocuments() {
                return ndocs;
            }

            public String getPDocuments() {
                return pdocs;
            }
        };
    }

    /**
     * Find a {@link EntityContentProducer} capable of handling the given entity reference, or null if no
     * such producer has been registered.
     *
     * @param ref the entity reference
     */
    @Override
    public EntityContentProducer newEntityContentProducer(String ref) {
        final Optional<EntityContentProducer> producer = matchEntityContentProducer(p -> p.matches(ref));
        if ( producer.isPresent() ) {
            getLog().debug("Matched content producer " + producer.get() + " for reference " + ref + " in index builder "
                    + getName());
            return producer.get();
        }
        getLog().debug("Failed to match any content producer for reference " + ref + " in index builder " + getName());
        return null;
    }

    /**
     * Find a {@link EntityContentProducer} capable of handling the given {@code Event}, or null if no
     * such producer has been registered.
     *
     * @param event
     * @return
     */
    @Override
    public EntityContentProducer newEntityContentProducer(Event event) {
        final Optional<EntityContentProducer> producer = matchEntityContentProducer(p -> p.matches(event));
        if ( producer.isPresent() ) {
            getLog().debug("Matched content producer " + producer.get() + " for event " + event + " in index builder "
                    + getName());
            return producer.get();
        }
        getLog().debug("Failed to match any content producer for event " + event + " in index builder " + getName());
        return null;
    }

    protected Optional<EntityContentProducer> matchEntityContentProducer(Predicate<EntityContentProducer> matcher) {
        return producers.stream().filter(matcher).findFirst();
    }

    /**
     * get all the producers registered, as a clone to avoid concurrent
     * modification exceptions
     *
     * @return
     */
    @Override
    public List<EntityContentProducer> getContentProducers() {
        return Lists.newArrayList(producers);
    }

    /**
     * register an entity content producer to provide content to the search
     * engine {@inheritDoc}
     */
    @Override
    public void registerEntityContentProducer(EntityContentProducer ecp) {
        getLog().debug("register " + ecp);
        // no synchronization here b/c:
        //   a) producers collection internally threadsafe, and
        //   b) event registrations are append-only and order isn't important, so interleaved updateEventsFor() calls
        //      will result in the same functional end state
        //   c) we know the updateEventsFor() impl will serialize invocations anyway
        producers.add(ecp);
        if ( eventRegistrar != null ) {
            eventRegistrar.updateEventsFor(this);
        }
    }

    @Override
    public Set<String> getContentFunctions() {
        return producers.stream()
                .filter(ecp -> ecp instanceof EntityContentProducerEvents)
                .flatMap(ecp -> ((EntityContentProducerEvents)ecp).getTriggerFunctions().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Establish a security advisor to allow the "embedded"  work to occur with no need for additional security permissions.
     */
    protected void enableAzgSecurityAdvisor() {
        // put in a security advisor so we can do our  work without need of further permissions
        securityService.pushAdvisor(allowAllAdvisor);
    }

    /**
     * Disable the security advisor.
     */
    protected void disableAzgSecurityAdvisor() {
        SecurityAdvisor popped = securityService.popAdvisor(allowAllAdvisor);
        if (!allowAllAdvisor.equals(popped)) {
            if (popped == null) {
                getLog().debug("Someone has removed our advisor.");
            } else {
                getLog().debug("Removed someone elses advisor, adding it back.");
                securityService.pushAdvisor(popped);
            }
        }
    }

    /**
     * loads the field from the SearchHit. Loads from field not from source since
     * we aren't storing the source.
     * @param field
     * @param hit
     * @return
     */
    @Override
    public String getFieldFromSearchHit(String field, SearchHit hit) {
        if (hit != null && hit.getFields() != null && hit.getFields().get(field) != null) {
            return hit.getFields().get(field).value();
        }
        return null;
    }

    protected <L,R> Pair<L,R> pairOf(L left, R right) {
        return new ImmutablePair<>(left,right);
    }

    @Override
    public List<SearchBuilderItem> getAllSearchItems() {
        return null;
    }

    public void setIndexedDocumentType(String indexedDocumentType){
        this.indexedDocumentType = indexedDocumentType;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public void setRebuildIndexOnStartup(boolean rebuildIndexOnStartup) {
        this.rebuildIndexOnStartup = rebuildIndexOnStartup;
    }

    @Override
    public boolean getUseFacetting() {
        return this.useFacetting;
    }

    public void setUseFacetting(boolean useFacetting) {
        this.useFacetting = useFacetting;
    }

    public void setFacetName(String facetName) {
        this.facetName = facetName;
    }

    public void setMaxNumberOfSuggestions(int maxNumberOfSuggestions) {
        this.maxNumberOfSuggestions = maxNumberOfSuggestions;
    }

    public void setUseSuggestions(boolean useSuggestions) {
        this.useSuggestions = useSuggestions;
    }

    public void setSuggestionResultFieldNames(String[] suggestionResultFieldNames) {
        this.suggestionResultFieldNames = suggestionResultFieldNames;
    }

    public void setSuggestionMatchingFieldName(String suggestionMatchingFieldName) {
        this.suggestionMatchingFieldName = suggestionMatchingFieldName;
    }

    public void setSearchResultFieldNames(String[] searchResultFieldNames) {
        this.searchResultFieldNames = searchResultFieldNames;
    }

    public void setFilter(SearchItemFilter filter) {
        this.filter = filter;
    }

    @Override
    public SearchItemFilter getFilter() {
        return filter;
    }

    @Override
    public String getFacetName() {
        return facetName;
    }

    public void setFacetTermSize(int facetTermSize) {
        this.facetTermSize = facetTermSize;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIndexName(String indexName) {
        //elasticsearch wants lowers case index names
        this.indexName = indexName.toLowerCase();
    }

    public void setDefaultIndexSettingsResource(String defaultIndexSettingsResource) {
        this.defaultIndexSettingsResource = defaultIndexSettingsResource;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public void setIndexSettings(String indexSettings) {
        this.indexSettings = indexSettings;
    }

    public void setDefaultMappingResource(String defaultMappingResource) {
        this.defaultMappingResource = defaultMappingResource;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setContentIndexBatchSize(int contentIndexBatchSize) {
        this.contentIndexBatchSize = contentIndexBatchSize;
    }

    public void setBulkRequestSize(int bulkRequestSize) {
        this.bulkRequestSize = bulkRequestSize;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void setTriggerFunctions(Collection<String> triggerFunctions) {
        this.triggerFunctions = (triggerFunctions instanceof Set)
                ? (Set)triggerFunctions : Sets.newHashSet(triggerFunctions);
    }

    @Override
    public Set<String> getTriggerFunctions() {
        return triggerFunctions;
    }

    @Override
    public List<SearchBuilderItem> getGlobalMasterSearchItems() {
        return Collections.emptyList();
    }

    protected abstract Logger getLog();


    public enum IndexAction {
        /**
         * Action Unknown, usually because the record has just been created
         */
        UNKNOWN(SearchBuilderItem.ACTION_UNKNOWN),

        /**
         * Action ADD the record to the search engine, if the doc ID is set, then
         * remove first, if not set, check its not there.
         */
        ADD(SearchBuilderItem.ACTION_ADD),

        /**
         * Action DELETE the record from the search engine, once complete delete the
         * record
         */
        DELETE(SearchBuilderItem.ACTION_DELETE),

        /**
         * The action REBUILD causes the indexer thread to rebuild the index from
         * scratch, re-fetching all entities This should only ever appear on the
         * master record
         */
        REBUILD(SearchBuilderItem.ACTION_REBUILD),

        /**
         * The action REFRESH causes the indexer thread to refresh the search index
         * from the current set of entities. If a Rebuild is in progress, the
         * refresh will not override the rebuild
         */
        REFRESH(SearchBuilderItem.ACTION_REFRESH);

        private final int itemAction;

        private IndexAction(int itemAction) {
            this.itemAction = itemAction;
        }

        /**
         * Generate an IndexAction based on an action ID provided by the Search API
         *
         * @param itemActionId action ID used by the Search API
         * @return IndexAction matching the given ID, null if nothing has been found
         */
        public static IndexAction getAction(int itemActionId) {
            for (IndexAction indexAction : values()) {
                if (indexAction.getItemAction() == itemActionId)
                    return indexAction;
            }

            return null;
        }

        public int getItemAction() {
            return itemAction;
        }
    }

}
