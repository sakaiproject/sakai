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

import static org.opensearch.index.query.QueryBuilders.boolQuery;
import static org.opensearch.index.query.QueryBuilders.existsQuery;
import static org.opensearch.index.query.QueryBuilders.matchAllQuery;
import static org.opensearch.index.query.QueryBuilders.simpleQueryStringQuery;
import static org.opensearch.index.query.QueryBuilders.termQuery;
import static org.opensearch.index.query.QueryBuilders.termsQuery;
import static org.opensearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opensearch.action.admin.cluster.health.ClusterHealthRequest;
import org.opensearch.action.admin.cluster.health.ClusterHealthResponse;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchType;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.CountRequest;
import org.opensearch.client.core.CountResponse;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.CreateIndexResponse;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.client.indices.GetIndexResponse;
import org.opensearch.cluster.health.ClusterIndexHealth;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.util.set.Sets;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.search.SearchHit;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentType;
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
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
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
    protected RestHighLevelClient client;

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
    protected String mappingConfig = null;

    /**
     * Combination of {@link #mappingConfig}, its fallback read from {@link #defaultMappingResource}, and any overrides
     * implemented in {@link #initializeElasticSearchMapping(String)}. (Currently there are no such overrides in that
     * method... just the fallback resource lookup. And historically the mapping config was stored exclusively in
     * {@link #mappingConfig}. This new {@code mappingMerged} field was added for symmetry with {@link #indexSettings}).
     */
    protected String mapping = null;

    protected String defaultIndexSettingsResource = "/org/sakaiproject/search/elastic/bundle/indexSettings.json";

    /**
     * Expects a JSON string of ElasticSearch index settings.  You can set this in your
     * sakai.properties files and inject a value using the indexSettings@org.sakaiproject.search.api.SearchIndexBuilder
     * property.  By default this value is configured by the indexSettings.json files.
     *
     * See {@link <a href="http://www.elasticsearch.org/guide/reference/index-modules/">elasticsearch index modules</a>}
     * for more information on configuration that is available.
     */
    protected String indexSettingsConfig = null;

    protected Settings indexSettings;

    /**
     * indexing thread that performs loading the actual content into the index.
     */
    protected Timer backgroundScheduler = null;

    protected Set<EntityContentProducer> producers = Collections.emptySet();

    protected Set<String> triggerFunctions = new HashSet<>();

    protected ElasticSearchIndexBuilderEventRegistrar eventRegistrar;


    public boolean isEnabled() {
        return serverConfigurationService.getBoolean("search.enable", false);
    }

    @Override
    public void destroy() {
        if (backgroundScheduler != null) {
            log.info("elasticsearch shutting down index worker for index {}", indexName);
            backgroundScheduler.cancel();
            backgroundScheduler = null;
        }

        if (client != null) {
            try {
                log.info("elasticsearch closing client for index {}", indexName);
                client.close();
                client = null;
            } catch (IOException ioe) {
                log.warn("Could not close elasticsearch client, {}", ioe.toString());
            }
        }
        eventRegistrar = null;
    }

    @Override
    public void initialize(ElasticSearchIndexBuilderEventRegistrar eventRegistrar, RestHighLevelClient client) {

        if (!isEnabled()) {
            log.debug("ElasticSearch is not enabled. Skipping initialization of index builder [{}]. Set search.enable=true to change that.", getName());
            return;
        }

        if ( testMode ) {
            log.warn("IN TEST MODE for index builder [{}]. DO NOT enable this in production !!!", getName());
        }

        log.info("Initializing ElasticSearch index builder [{}]...", getName());

        String indexNamespace = serverConfigurationService.getString("search.indexNamespace", null);
        if (StringUtils.isNotBlank(indexNamespace)) {
            this.indexName = indexNamespace + "_" + this.indexName;
        }

        this.eventRegistrar = eventRegistrar;
        this.client = client;

        beforeElasticSearchConfigInitialization();

        requireConfiguration();

        this.mapping = initializeElasticSearchMapping(this.mappingConfig);
        this.indexSettings = initializeElasticSearchIndexSettings(this.indexSettingsConfig);

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
        if (StringUtils.isEmpty(mappingConfig)) {
            try {
                StringWriter writer = new StringWriter();
                IOUtils.copy(getClass().getResourceAsStream(this.defaultMappingResource), writer, "UTF-8");
                mappingConfig = writer.toString();
            } catch (Exception ex) {
                log.error("Failed to load mapping config: " + ex.getMessage(), ex);
            }
        }
        log.info("ElasticSearch mapping will be configured as follows for index builder [{}]:{}", getName(), mappingConfig);
        return mappingConfig;
    }

    protected Settings initializeElasticSearchIndexSettings(String injectedConfig) {
        try {
            String resourceName;
            InputStream inputStream;
            if (StringUtils.isEmpty(injectedConfig)) {
                inputStream = getClass().getResourceAsStream(this.defaultIndexSettingsResource);
                resourceName = this.defaultIndexSettingsResource;
            } else {
                inputStream = IOUtils.toInputStream(injectedConfig, "UTF-8");
                resourceName = "injectedConfig.json";
            }
            Settings.Builder settingsBuilder = Settings.builder().loadFromStream(resourceName, inputStream, true);

            // Set these here so we don't have to do this string concatenation
            // and comparison every time through the upcoming 'for' loop
            final boolean IS_DEFAULT = SearchIndexBuilder.DEFAULT_INDEX_BUILDER_NAME.equals(getName());
            final String DEFAULT_INDEX = ElasticSearchConstants.CONFIG_PROPERTY_PREFIX + "index.";
            final String LOCAL_INDEX = String.format("%s%s%s.", ElasticSearchConstants.CONFIG_PROPERTY_PREFIX, "index_", getName(), ".");

            // load anything set into the ServerConfigurationService that starts with "elasticsearch.index."  this will
            // override anything set in the indexSettings config
            for (ServerConfigurationService.ConfigItem configItem : serverConfigurationService.getConfigData().getItems()) {
                String propertyName = configItem.getName();
                if (IS_DEFAULT && (propertyName.startsWith(DEFAULT_INDEX))) {
                    propertyName = propertyName.replaceFirst(DEFAULT_INDEX, "index.");
                    settingsBuilder.put(propertyName, (String) configItem.getValue());
                } else if (propertyName.startsWith(LOCAL_INDEX)) {
                    propertyName = propertyName.replaceFirst(LOCAL_INDEX, "index.");
                    settingsBuilder.put(propertyName, (String) configItem.getValue());
                }
            }

            for (String key : settingsBuilder.keys()) {
                log.info("Setting [{}={}] added to index builder '{}'", key, settingsBuilder.get(key), getName());
            }
            return settingsBuilder.build();
        } catch (IOException ioe) {
            log.error("Failed to load indexSettings config from [{}] for index builder [{}]", defaultIndexSettingsResource, getName(), ioe);
        }
        return null;
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
                log.debug("Running content indexing task for index builder [{}]", getName());
                enableAzgSecurityAdvisor();
                processContentQueue();
            } catch (Exception e) {
                log.error("Content indexing failure for index builder [{}]", getName(), e);
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
            log.trace("No pending docs for index builder [{}]", getName());
            return;
        }

        SearchResponse response = findContentQueue();

        SearchHit[] hits = response.getHits().getHits();
        List<NoContentException> noContentExceptions = new ArrayList<>();
        log.debug("{} pending docs for index builder [{}]", getPendingDocuments(), getName());

        BulkRequest bulkRequest = new BulkRequest();

        for (SearchHit hit : hits) {

            if (bulkRequest.numberOfActions() < bulkRequestSize) {
                try {
                    processContentQueueEntry(hit, bulkRequest);
                } catch ( NoContentException e ) {
                    noContentExceptions.add(e);
                }
            } else {
                executeBulkRequest(bulkRequest);
                bulkRequest = new BulkRequest();
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
            log.info("Finished indexing {} docs in {}ms for index builder {}", hits.length, ((lastLoad - startTime)), getName());
        }

    }

    protected void processContentQueueEntry(SearchHit hit, BulkRequest bulkRequest) throws NoContentException {
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
                log.error("Failed to process content queue entry with id [{}] in index builder [{}]", hit.getId(), getName(), e);
            }
        } else {
            noContentProducerForContentQueueEntry(hit, reference);
        }
    }

    protected void executeBulkRequest(BulkRequest bulkRequest) {
        BulkResponse bulkResponse = null;
        try {
            bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException ioe) {
            log.warn("Error executing bulk operation, {}", ioe.toString());
            return;
        }

        log.info("Bulk request of batch size: {} took {}ms in index builder: {}", bulkRequest.numberOfActions(), bulkResponse.getTook().getMillis(), getName());

        for (BulkItemResponse response : bulkResponse.getItems()) {
            if (response.getResponse() instanceof DeleteResponse) {
                DeleteResponse deleteResponse = response.getResponse();

                if (response.isFailed()) {
                    log.error("Problem deleting doc: {} in index builder: {} error: {}", response.getId(), getName(), response.getFailureMessage());
                } else if (deleteResponse.status() == RestStatus.NOT_FOUND) {
                    log.debug("ES could not find a doc with id: {} to delete in index builder: {}", deleteResponse.getId(), getName());
                } else {
                    log.debug("ES deleted a doc with id: {} in index builder: {}", deleteResponse.getId(), getName());
                }
            } else if (response.getResponse() instanceof IndexResponse) {
                IndexResponse indexResponse = response.getResponse();

                if (response.isFailed()) {
                    log.error("Problem updating content for doc: {} in index builder: {} error: {}", response.getId(), getName(), response.getFailureMessage());
                } else {
                    log.debug("ES indexed content for doc with id: {} in index builder: {}", indexResponse.getId(), getName());
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

    protected SearchResponse findContentQueue() {
        SearchRequest searchRequest = prepareFindContentQueue();
        return findContentQueueWithRequest(searchRequest);
    }

    protected SearchRequest prepareFindContentQueue() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest = addFindContentQueueRequestParams(searchRequest);
        searchRequest = completeFindContentQueueRequest(searchRequest);
        return searchRequest;
    }

    protected SearchRequest addFindContentQueueRequestParams(SearchRequest searchRequest) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(matchAllQuery())
                .postFilter(boolQuery().should(termQuery(SearchService.FIELD_INDEXED, false)).should(boolQuery().mustNot(existsQuery(SearchService.FIELD_INDEXED))))
                .size(contentIndexBatchSize)
                .storedFields(Arrays.asList(SearchService.FIELD_REFERENCE, SearchService.FIELD_SITEID));
        return searchRequest
                .indices(indexName)
                .source(searchSourceBuilder);
    }

    protected abstract SearchRequest completeFindContentQueueRequest(SearchRequest searchRequest);

    protected SearchResponse findContentQueueWithRequest(SearchRequest searchRequest) {
        try {
            return client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException ioe) {
            log.warn("Search failure, " + ioe);
            return null;
        }
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
        final DeleteRequest deleteRequest = prepareDeleteDocument(deleteParams);
        try {
            DeleteResponse deleteResponse = deleteDocumentWithRequest(deleteRequest);
            if (log.isDebugEnabled()) {
                if (deleteResponse.status() == RestStatus.NOT_FOUND) {
                    log.debug("Could not delete doc with by id: {} in index builder [{}] because the document wasn't found", deleteParams.get(DELETE_RESOURCE_KEY_DOCUMENT_ID), getName());
                } else {
                    log.debug("ES deleted a doc with id: {} in index builder [{}]", deleteResponse.getId(), getName());
                }
            }
        } catch (IOException ioe) {
            log.warn("Delete request failure, " + ioe);
        }

    }

    protected DeleteRequest prepareDeleteDocument(Map<String, Object> deleteParams) {
        return completeDeleteRequest(newDeleteRequest(deleteParams), deleteParams);
    }

    protected abstract DeleteRequest completeDeleteRequest(DeleteRequest deleteRequest, Map<String, Object> deleteParams);

    protected DeleteResponse deleteDocumentWithRequest(DeleteRequest deleteRequest) throws IOException {
        return client.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    private DeleteRequest newDeleteRequest(Map<String, Object> deleteParams) {
        return new DeleteRequest(indexName, (String) deleteParams.get(DELETE_RESOURCE_KEY_DOCUMENT_ID));
    }

    protected Map<String, Object> extractDeleteDocumentParams(SearchHit searchHit) {
        final Map<String, Object> params = new HashMap<>();
        params.put(DELETE_RESOURCE_KEY_DOCUMENT_ID, searchHit.getId());
        return params;
    }

    protected Map<String, Object> extractDeleteDocumentParams(NoContentException noContentException) {
        final Map<String, Object> params = new HashMap<>();
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
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        boolean indexExists = false;
        try {
            indexExists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            if (!indexExists) {
                createIndex();
            }
        } catch (IOException e) {
            log.error("IO Error checking if index {} exists in index builder [{}]", indexName, getName());
        }
    }

    /**
     * creates a new index, does not check if the exist exists
     */
    protected void createIndex() {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            createIndexRequest.settings(indexSettings);
            createIndexRequest.mapping(mapping, XContentType.JSON);
        try {
            CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            if (!createIndexResponse.isAcknowledged()) {
                log.error("Index {} wasn't created for index builder [{}], can't rebuild", indexName, getName());
            }
        } catch (IOException e) {
            log.error("IO Error creating index {} index builder [{}], can't rebuild", indexName, getName());
        }
    }


    /**
     * removes any existing index and creates a new one
     */
    protected void recreateIndex() {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        try {
            boolean indexExists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            if (indexExists) {
                client.indices().delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
            }
            // create index
            createIndex();
        } catch (IOException e) {
            log.error("IO Error recreating index {} index builder [{}], can't recreate", indexName, getName());
        }
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
        try {
            client.indices().refresh(new RefreshRequest(indexName), RequestOptions.DEFAULT);
        } catch (IOException ioe) {
            log.error("IO Error refreshing index {} index builder [{}], {}", indexName, getName(), ioe.toString());
        }
    }

    /**
     *
     * @param resourceName
     * @param ecp
     * @return
     */
    protected IndexRequest prepareIndex(String resourceName, EntityContentProducer ecp, boolean includeContent)
            throws IOException, NoContentException {
        IndexRequest indexRequest = newIndexRequest(resourceName, ecp, includeContent);
        final XContentBuilder requestContentSource = buildIndexRequestContentSource(resourceName, ecp, includeContent);
        indexRequest = indexRequest.source(requestContentSource);
        return completeIndexRequest(indexRequest, resourceName, ecp, includeContent);
    }

    protected IndexRequest newIndexRequest(String resourceName, EntityContentProducer ecp, boolean includeContent) {
        return new IndexRequest(indexName).id(ecp.getId(resourceName));
    }

    protected abstract IndexRequest completeIndexRequest(IndexRequest indexRequest, String resourceName, EntityContentProducer ecp, boolean includeContent);

    protected XContentBuilder buildIndexRequestContentSource(String resourceName, EntityContentProducer ecp, boolean includeContent)
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
                contentSourceBuilder
                        // cannot rely on ecp for providing something reliable to maintain index state
                        // indexed indicates if the document was indexed
                        .field(SearchService.FIELD_INDEXED, true)
                        .field(SearchService.FIELD_CONTENTS, content);
            } else {
                return noContentForIndexRequest(contentSourceBuilder, resourceName, ecp, includeContent);
            }
        } else {
            contentSourceBuilder.field(SearchService.FIELD_INDEXED, false);
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
            IndexRequest request = prepareIndex(resourceName, ecp, includeContent);
            client.index(request, RequestOptions.DEFAULT);
        } catch (NoContentException e) {
            throw e;
        } catch (IOException ioe) {
            log.error("Error: trying to register resource {} in index builder: {}, {}", resourceName, getName(), ioe.toString());
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
            log.error("Problem updating content indexing in index builder: {} for entity: {}", getName(), resourceName, e);
        }
    }

    @Override
    public int getPendingDocuments() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQuery()
                .should(termQuery(SearchService.FIELD_INDEXED, false))
                .should(boolQuery().mustNot(existsQuery(SearchService.FIELD_INDEXED))));
        CountRequest countRequest = new CountRequest(indexName).source(searchSourceBuilder);
        try {
            CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
            return (int) countResponse.getCount();
        } catch (IOException ioe) {
            log.error("Problem getting pending docs for index builder [{}]", getName(), ioe);
        }
        return 0;
    }

    @Override
    public boolean isBuildQueueEmpty() {
        return getPendingDocuments() == 0;
    }

    @Override
    public void addResource(Notification notification, Event event) {
        log.debug("Add resource {}::{} in index builder {}", notification, event, getName());

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
            log.debug("Skipping index for event {} in index builder [{}] because it did not validate", event, getName(), e);
            return;
        }

        dispatchValidatedAddResource(validationContext);
    }

    protected Map<String,Object> validateAddResourceEvent(Event event)
            throws IllegalArgumentException, IllegalStateException {
        final Map<String,Object> validationContext = new HashMap<>();
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
            throw new IllegalArgumentException("Entity Reference is longer than 255 characters. Reference=" + resourceName);
        }
        validationContext.put(ADD_RESOURCE_VALIDATION_KEY_RESOURCE_NAME, resourceName);
    }

    protected void validateContentProducer(Event event, Map<String, Object> validationContext)
            throws IllegalArgumentException, IllegalStateException {
        final EntityContentProducer ecp = newEntityContentProducer(event);

        if (ecp == null) {
            throw new IllegalArgumentException("No registered SearchableContentProducer for event [" + event + "] in indexBuilder [" + getName() + "]");
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
        log.debug("Action on '{}' detected as {} in index builder {}", resourceName, indexAction.name(), getName());

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
        final Map<String,Object> params = new HashMap<>();
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
                    log.warn("Couldn't find what the value for '{}' was. It has been ignored. {}", propertyName, propertyName.getClass());
                values = Collections.emptyList();
            }

            //If this property was already present there (this shouldn't happen, but if it does everything must be stored
            if (properties.containsKey(propertyName)) {
                log.warn("Two properties had a really similar name and were merged. This shouldn't happen! {}", propertyName);
                log.debug("Merged values [{}] with [{}]", properties.get(propertyName), values);
                values = new ArrayList<>(values);
                values.addAll(properties.get(propertyName));
            }

            properties.put(propertyName, values);
        }

        return properties;
    }

    @Override
    public SearchResponse search(String searchTerms, List<String> references, List<String> siteIds, List<String> toolIds, int start, int end) {

        SearchRequest searchRequest = prepareSearchRequest(searchTerms, references, siteIds, toolIds, start, end);

        log.debug("Search request from index builder [{}]: {}", getName(), searchRequest);
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            log.debug("Search request from index builder [{}] took: {}", getName(), response.getTook().getMillis());
            String minified = getMinifiedJson(searchRequest.source().query().toString());
            eventTrackingService.post(
                    eventTrackingService.newEvent(
                            SearchService.EVENT_SEARCH,
                            SearchService.EVENT_SEARCH_REF + minified,
                            true,
                            NotificationService.PREF_IMMEDIATE));
            return response;
        } catch (IOException ioe) {
            log.debug("Error for search request from index builder [{}], {}", getName(), ioe.toString());
        }
        return null;
    }

    private static String getMinifiedJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(json);
            return mapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            log.warn("Could not minify json [{}], {}", json, e.toString());
        }
        return json;
    }

    @Override
    public SearchResponse search(String searchTerms, List<String> references, List<String> siteIds, List<String> toolIds, int start, int end, Map<String,String> additionalSearchInformation) {
        // additional information will be used in specific indexes,
        // so this method can be overridden in the index to make use of that field.
        return search(searchTerms, references, siteIds, toolIds, start, end);
    }

    protected SearchRequest prepareSearchRequest(String searchTerms, List<String> references, List<String> siteIds, List<String> toolIds, int start, int end) {
        SearchRequest searchRequest = newSearchRequestAndQueryBuilders();
        addSearchCoreParams(searchRequest);
        addSearchQuery(searchRequest, searchTerms, references, siteIds, toolIds);
        addSearchResultFields(searchRequest);
        addSearchPagination(searchRequest, start, end);
        addSearchFacetting(searchRequest);
        completeSearchRequestBuilders(searchRequest, searchTerms, references, siteIds);
        return searchRequest;
    }

    protected SearchRequest newSearchRequestAndQueryBuilders() {
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source().query(boolQuery());
        return searchRequest;
    }

    protected void addSearchCoreParams(SearchRequest searchRequest) {
        searchRequest.searchType(SearchType.QUERY_THEN_FETCH);
    }

    protected void addSearchQuery(SearchRequest searchRequest, String searchTerms, List<String> references, List<String> siteIds, List<String> toolIds) {
        addSearchTerms(searchRequest, searchTerms);
        addSearchReferences(searchRequest, references);
        addSearchSiteIds(searchRequest, siteIds);
        addSearchToolIds(searchRequest, toolIds);
    }

    protected void addSearchTerms(SearchRequest searchRequest, String searchTerms) {
        BoolQueryBuilder query = (BoolQueryBuilder) searchRequest.source().query();

        if (searchTerms == null) {
            query.must(matchAllQuery());
        } else {
            Arrays.stream(searchTerms.split(" ")).forEach(term -> {

                if (term.contains(":")) {
                    String[] fieldTerm = term.split(":");
                    query.must(termQuery(fieldTerm[0], fieldTerm[1]));
                } else {
                    query.must(simpleQueryStringQuery(term));
                }
            });
        }
    }

    protected void addSearchReferences(SearchRequest searchRequest, List<String> references) {
        BoolQueryBuilder query = (BoolQueryBuilder) searchRequest.source().query();
        if (references != null && !references.isEmpty()) {
            query.must(termsQuery(SearchService.FIELD_REFERENCE, references.toArray(new String[0])));
        }
    }

    protected void addSearchToolIds(SearchRequest searchRequest, List<String> toolIds) {

        if (toolIds != null && !toolIds.isEmpty()) {
            BoolQueryBuilder queryBuilder = (BoolQueryBuilder) searchRequest.source().query();
            queryBuilder.must(termsQuery(SearchService.FIELD_TOOL, toolIds));
        }
    }


    protected abstract void addSearchSiteIds(SearchRequest searchRequest, List<String> siteIds);

    protected void addSearchResultFields(SearchRequest searchRequest) {
        if (!ArrayUtils.isEmpty(searchResultFieldNames)) {
            searchRequest.source().storedFields(Arrays.asList(searchResultFieldNames));
        }
    }

    protected void addSearchPagination(SearchRequest searchRequest, int start, int end) {
        searchRequest.source().from(start).size(end - start);
    }

    protected void addSearchFacetting(SearchRequest searchRequest) {
        if (useFacetting) {
            TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(facetName).field("contents.lowercase").size(facetTermSize);
            searchRequest.source().aggregation(termsAggregationBuilder);
        }
    }

    protected abstract void completeSearchRequestBuilders(SearchRequest searchRequest, String searchTerms, List<String> references, List<String> siteIds);


    @Override
    public String[] searchSuggestions(String searchString, String currentSite, boolean allMySites) {
        if (!useSuggestions) {
            return new String[0];
        }

        SearchRequest searchRequest = prepareSearchSuggestionsRequest(searchString, currentSite, allMySites);
        log.debug("Search request from index builder [{}]: {}", getName(), searchRequest);

        List<String> suggestions = new ArrayList<>();
        SearchResponse response = null;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit hit : response.getHits()) {
                suggestions.add(getFieldFromSearchHit(suggestionMatchingFieldName, hit));
            }
        } catch (IOException ioe) {
            log.error("Search request from index builder [{}]:{}, {}", getName(), searchRequest, ioe.toString());
        }

        log.debug("Search request from index builder [{}] took: {}", getName(), response.getTook().getStringRep());

        return suggestions.toArray(new String[0]);
    }

    protected SearchRequest prepareSearchSuggestionsRequest(String searchString, String currentSite, boolean allMySites) {
        SearchRequest searchRequest = newSearchSuggestionsRequestAndQueryBuilders(searchString);
        addSearchSuggestionsCoreParams(searchRequest);
        addSearchSuggestionsQuery(searchRequest, searchString, currentSite, allMySites);
        addSearchSuggestionResultFields(searchRequest);
        addSearchSuggestionsPagination(searchRequest);
        completeSearchSuggestionsRequestBuilders(searchRequest, searchString, currentSite, allMySites);
        return searchRequest;
    }

    protected abstract void completeSearchSuggestionsRequestBuilders(SearchRequest searchRequest, String searchString, String currentSite, boolean allMySites);

    protected SearchRequest newSearchSuggestionsRequestAndQueryBuilders(String searchString) {

        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source().query(termQuery(suggestionMatchingFieldName, searchString));
        return searchRequest;
    }

    protected void addSearchSuggestionsCoreParams(SearchRequest searchRequest) {
        searchRequest.searchType(SearchType.QUERY_THEN_FETCH);
    }

    protected void addSearchSuggestionsQuery(SearchRequest searchRequest, String searchString, String currentSite, boolean allMySites) {
        addSearchSuggestionsTerms(searchRequest, searchString);
        addSearchSuggestionsSites(searchRequest, currentSite, allMySites);
    }

    protected abstract void addSearchSuggestionsTerms(SearchRequest searchRequest, String searchString);

    protected abstract void addSearchSuggestionsSites(SearchRequest searchRequest, String currentSite, boolean allMySites);

    protected void addSearchSuggestionResultFields(SearchRequest searchRequest) {
        if (!ArrayUtils.isEmpty(suggestionResultFieldNames)) {
            searchRequest.source().storedFields(Arrays.asList(suggestionResultFieldNames));
        }
    }

    protected void addSearchSuggestionsPagination(SearchRequest searchRequest) {
        searchRequest.source().size(maxNumberOfSuggestions);
    }

    public List<String> getIndices() {
        List<String> indices = new ArrayList<>();
        GetIndexRequest request = new GetIndexRequest(indexName);
        try {
            GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
            indices.addAll(Arrays.asList(response.getIndices()));
        } catch (IOException ioe) {
            log.error("Could not retrieve indices, {}", ioe.toString());
        }
        return indices;
    }

    public ClusterIndexHealth getIndexHealth() {
        ClusterHealthRequest request = new ClusterHealthRequest(indexName);
        try {
            ClusterHealthResponse response = client.cluster().health(request, RequestOptions.DEFAULT);
            return response.getIndices().get(indexName);
        } catch (IOException ioe) {
            log.error("Could not retrieve health status for index {}, {}", indexName, ioe.toString());
        }
        return null;
    }

    public StringBuilder getStatus(StringBuilder into) {
        assureIndex();

        long pendingDocs = getPendingDocuments();

        into.append("Index builder: ").append(getName());
        if (pendingDocs != 0) {
            into.append(" active. " + pendingDocs + " pending items in queue. ");
        } else {
            into.append(" idle. ");
        }
        // These stats may not be available to the High Level Client until ES 7.5
        // into.append("Index Size: " + roundTwoDecimals(status.getStoreSize().getGbFrac()) + " GB" +
        //         " Refresh Time: " + status.getRefreshStats().getTotalTimeInMillis() + "ms" +
        //         " Flush Time: " + status.getFlushStats().getTotalTimeInMillis() + "ms" +
        //         " Merge Time: " + status.getMergeStats().getTotalTimeInMillis() + "ms");

        return into;
    }

    protected double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    @Override
    public long getNDocs() {
        assureIndex();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQuery()
                .must(matchAllQuery())
                .filter(termsQuery(SearchService.FIELD_INDEXED, true)));
        CountRequest countRequest = new CountRequest(indexName).source(searchSourceBuilder);
        try {
            CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
            return countResponse.getCount();
        } catch (IOException ioe) {
            log.error("Problem getting docs for index builder [" + getName() + "]", ioe);
        }
        return 0;
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
        if (ref == null) return null;

        final Optional<EntityContentProducer> producer = matchEntityContentProducer(p -> p.matches(ref));
        if ( producer.isPresent() ) {
            log.debug("Matched content producer {} for reference {} in index builder {}", producer.get(), ref, getName());
            return producer.get();
        }
        log.debug("Failed to match any content producer for reference {} in index builder {}", ref, getName());
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
            log.debug("Matched content producer {} for event {} in index builder {}", producer.get(), event, getName());
            return producer.get();
        }
        log.debug("Failed to match any content producer for event {} in index builder {}", event, getName());
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
    public Collection<EntityContentProducer> getContentProducers() {
        return producers;
    }

    /**
     * register an entity content producer to provide content to the search
     * engine {@inheritDoc}
     */
    @Override
    public void registerEntityContentProducer(EntityContentProducer ecp) {
        if (ecp == null) {
            log.warn("attempting to register a null entity content producer");
            return;
        }

        if (!isEnabled()) {
            log.debug("Search is not enabled. Skipping registration of entity content producer [{}]", ecp);
            return;
        }

        log.debug("register entity content producer [{}]", ecp);
        Set<EntityContentProducer> updateProducers = new HashSet<>(producers);
        updateProducers.add(ecp);
        producers = Collections.unmodifiableSet(updateProducers);
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
                log.debug("Someone has removed our advisor.");
            } else {
                log.debug("Removed someone elses advisor, adding it back.");
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
            return hit.getFields().get(field).getValue();
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

    public void setMappingConfig(String mappingConfig) {
        this.mappingConfig = mappingConfig;
    }

    public void setIndexSettingsConfig(String indexSettingsConfig) {
        this.indexSettingsConfig = indexSettingsConfig;
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
