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

package org.sakaiproject.search.elasticsearch;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHit;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.FilterBuilders.*;

public class ElasticSearchIndexBuilder implements SearchIndexBuilder {

    private static Log log = LogFactory.getLog(ElasticSearchIndexBuilder.class);

    public static final String SEARCH_TOOL_ID = "sakai.search";
    private final static SecurityAdvisor allowAllAdvisor;

    private SiteService siteService;
    private SecurityService securityService;
    private ServerConfigurationService serverConfigurationService;


    private List<EntityContentProducer> producers = new ArrayList<EntityContentProducer>();
    private Client client;
    private String indexName;

    /**
     * number of documents to index at a time for each run of the context indexing task (defaults to 500)
     */
    private int contentIndexBatchSize = 500;

    /**
     * by default the mapping in configured in the mapping.json file.  This can be overridden by injecting
     * json into this property.
     *
     * See {@link <a href="http://www.elasticsearch.org/guide/reference/mapping/">elasticsearch mapping reference</a> } for
     * more information on configuration that is available.  For example, if you want to change the analyzer config
     * this is the place to do it.
     */
    private String mapping = null;

    /**
     * indexing thread that performs loading the actual content into the index.
     */
    private Timer contentIndexTimer = new Timer("[elasticsearch content indexer (event driven)]",true);

    private Timer bulkContentIndexTimer = new Timer("[elasticsearch bulk content indexer (refresh/rebuild)]", true);

    /**
     * number seconds of wait after startup before starting the BulkContentIndexerTask (defaults to 3 minutes)
     */
    private int delay = 180;

    /**
     * how often the BulkContentIndexerTask runs in seconds (defaults to 1 minute)
     */
    private int period = 60;

    /**
     * set to false if you want to index all content, not just sites that have the search tool placed
     */
    private boolean onlyIndexSearchToolSites = true;

    /**
     * set to false to inclue user site content in index
     */
    private boolean excludeUserSites = true;
    private Date startTime;
    private long lastLoad;


    static {
        allowAllAdvisor = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                return SecurityAdvice.ALLOWED;
            }
        };
    }

    public boolean isEnabled() {
        return serverConfigurationService.getBoolean("search.enable", false);
    }

    public void init() {
        if (!isEnabled()) {
            log.debug("ElasticSearch is not enabled. Set search.enable=true to change that.");
            return;
        }

        initMapping();
        bulkContentIndexTimer.schedule(new BulkContentIndexerTask(), (delay * 1000), (period * 1000));

    }



    /**
     * register an entity content producer to provide content to the search
     * engine {@inheritDoc}
     */
    public void registerEntityContentProducer(EntityContentProducer ecp) {
        log.debug("register " + ecp);
        producers.add(ecp);
    }

    /**
     * Add a resource to the indexing queue {@inheritDoc}
     */
    public void addResource(Notification notification, Event event) {
        log.debug("Add resource " + notification + "::" + event);

        if (!isEnabled()) {
            log.debug("ElasticSearch is not enabled. Set search.enable=true to change that.");
            return;
        }


        String resourceName = event.getResource();
        if (resourceName == null) {
            // default if null
            resourceName = "";
        }
        if (resourceName.length() > 255) {
            log.warn("Entity Reference is longer than 255 characters, not indexing. Reference="
                    + resourceName);
            return;
        }
        EntityContentProducer ecp = newEntityContentProducer(event);
        if (ecp == null || ecp.getSiteId(resourceName) == null) {
            log.debug("Not indexing " + resourceName + " as it has no context");
            return;
        }
        if (onlyIndexSearchToolSites) {
            try {
                String siteId = ecp.getSiteId(resourceName);
                Site s = siteService.getSite(siteId);
                ToolConfiguration t = s.getToolForCommonId(SEARCH_TOOL_ID);
                if (t == null) {
                    log.debug("Not indexing " + resourceName
                            + " as it has no search tool");
                    return;
                }
            } catch (Exception ex) {
                log.debug("Not indexing  " + resourceName + " as it has no site", ex);
                return;

            }
        }

        IndexAction action = IndexAction.getAction(ecp.getAction(event));
        log.debug("Action on '" + resourceName + "' detected as " + action.name());

        switch (action) {
            case ADD:
                scheduleIndexAdd(resourceName, ecp);
                break;
            case DELETE:
                deleteDocument(resourceName, ecp);
                break;
            default:
                throw new UnsupportedOperationException(action + " is not yet supported");
        }

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
                log.warn("Someone has removed our advisor.");
            } else {
                log.warn("Removed someone elses advisor, adding it back.");
                securityService.pushAdvisor(popped);
            }
        }
    }

    /**
     *
     * @param resourceName
     * @param ecp
     * @return
     */
    protected void prepareIndexAdd(String resourceName, EntityContentProducer ecp, boolean includeContent) {
        try {
            client.prepareIndex(indexName, ecp.getType(resourceName), ecp.getId(resourceName))
                    .setSource(buildIndexRequest(ecp, resourceName, includeContent))
                    .setType(ElasticSearchService.SAKAI_DOC_TYPE)
                    .setRouting(ecp.getSiteId(resourceName))
                    .execute()
                    .actionGet();

        } catch (Throwable t) {
            log.error("Error: trying to register resource " + resourceName
                    + " in search engine this resource will"
                    + " not be indexed until it is modified: " + t.getMessage());
        }

    }

    /**
     * schedules content for indexing.
     * @param resourceName
     * @param ecp
     * @return
     */
    protected void scheduleIndexAdd(String resourceName, EntityContentProducer ecp) {
        contentIndexTimer.schedule(new ContentIndexerTask( resourceName,  ecp), 0);
    }

    /**
     * build up the elasticsearch request
     * @param ecp
     * @param resourceName
     * @return
     * @throws IOException
     */
    protected XContentBuilder buildIndexRequest(EntityContentProducer ecp, String resourceName, boolean includeContent) throws IOException {
        XContentBuilder xContentBuilder = jsonBuilder()
                .startObject()
                .field(SearchService.FIELD_SITEID, ecp.getSiteId(resourceName))
                .field(SearchService.FIELD_TITLE, ecp.getTitle(resourceName))
                .field(SearchService.FIELD_REFERENCE, resourceName)
                .field(SearchService.FIELD_URL, ecp.getUrl(resourceName))
                //.field(SearchService.FIELD_ID, ecp.getId(resourceName))
                .field(SearchService.FIELD_TOOL, ecp.getTool())
                .field(SearchService.FIELD_CONTAINER, ecp.getContainer(resourceName))
                .field(SearchService.FIELD_TYPE, ecp.getType(resourceName));
                //.field(SearchService.FIELD_SUBTYPE, ecp.getSubType(resourceName));

        //Add the custom properties
        Map<String, Collection<String>> properties = extractCustomProperties(resourceName, ecp);
        for (Map.Entry<String, Collection<String>> entry : properties.entrySet()) {
            xContentBuilder.field(entry.getKey(), entry.getValue());
        }

        if (includeContent) {
            String content = ecp.getContent(resourceName);
            if (StringUtils.isNotEmpty(content)) {
                xContentBuilder.field(SearchService.FIELD_CONTENTS, content);
            } else {
                log.info("no content for " + resourceName + " to index");
            }
        }

        return xContentBuilder.endObject();

    }

    public long getLastLoad() {
        return lastLoad;
    }

    protected class RebuildSiteTask extends TimerTask {
        private final String siteId;

        public RebuildSiteTask(String siteId) {
            this.siteId = siteId;
        }

        /**
         * Rebuild the index from the entities own stored state {@inheritDoc}, for just
         * the supplied siteId
         */
        public void run() {
            log.info("Rebuilding the index for '" + siteId + "'");

            try {
                enableAzgSecurityAdvisor();
                deleteAllDocumentForSite(siteId);

                for (final EntityContentProducer entityContentProducer : getProducers()) {
                    try {
                        for (Iterator<String> i = entityContentProducer.getSiteContentIterator(siteId); i.hasNext(); ) {
                            String ref = i.next();
                            // this won't index the actual content just load the doc
                            // another thread will pick up the content, this allows the task to finish
                            // quickly and spread the content digesting across the cluster
                            prepareIndexAdd(ref, entityContentProducer, false);
                        }
                    } catch (Exception e) {
                        log.error("An exception occurred while rebuilding the index of '" + siteId + "'", e);
                    }
                }

                flushIndex();
                refreshIndex();
            } catch (Exception e) {
                log.error("An exception occurred while rebuilding the index of '" + siteId + "'", e);
            } finally {
                disableAzgSecurityAdvisor();
            }

        }

    }

    protected class ContentIndexerTask extends TimerTask {
        String reference;
        EntityContentProducer ecp;

        public ContentIndexerTask(String reference, EntityContentProducer ecp) {
            this.reference = reference;
            this.ecp = ecp;
        }

        public void run() {
            log.debug("running content indexing task");
            enableAzgSecurityAdvisor();
            try {
                //updating was causing issues, so doing delete and re-add
                deleteDocument(reference, ecp);
                prepareIndexAdd(reference, ecp, true);

            } catch (Exception e) {
                log.error("problem updating content indexing for entity: " + reference + " error: " + e.getMessage());
            } finally {
                disableAzgSecurityAdvisor();
            }

        }

    }

    /**
     * This is the task that searches for any docs in the search index that do not have content yet,
     * digests the content and loads it into the index.  Any docs with empty content will be removed from
     * the index.  This timer task is run by the timer thread based on the period set above
     */
    protected class BulkContentIndexerTask extends TimerTask {

        public void run() {

            try {
                log.debug("running content indexing task");
                enableAzgSecurityAdvisor();
                processContentQueue();
            } catch (Exception e) {
                log.error("content indexing failure: " + e.getMessage(), e);
            } finally {
                disableAzgSecurityAdvisor();
            }
        }
    }

    /**
     * Searches for any docs in the search index that do not have content yet,
     * digests the content and loads it into the index.  Any docs with empty content will be removed from
     * the index.
     */
    protected void processContentQueue() {
        startTime = new Date();

        // If there are a lot of docs queued up this could take awhile we don't want
        // to eat up all the CPU cycles.
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);

        assureIndex();

        SearchResponse response = client.prepareSearch(indexName)
                .setQuery(matchAllQuery())
                .setTypes(ElasticSearchService.SAKAI_DOC_TYPE)
                .setFilter(missingFilter(SearchService.FIELD_CONTENTS))
                .setSize(contentIndexBatchSize)
                .execute().actionGet();

        SearchHit[] hits = response.getHits().hits();


        log.debug(getPendingDocuments() + " pending docs.");

        for (SearchHit hit : hits) {
            String reference = getFieldFromSearchHit(SearchService.FIELD_REFERENCE, hit);
            log.debug("indexing content for entity:" + reference);
            try {
                EntityContentProducer ecp = this.getContentProducerForReference(reference);
                if (StringUtils.isNotEmpty(ecp.getContent(reference))) {

                    //updating was causing issues, so doing delete and re-add
                    deleteDocument(reference, ecp);
                    prepareIndexAdd(reference, ecp, true);

                } else {
                    // if there is no content to index remove the doc, its pointless to have it included in the index
                    // and we will just waste cycles looking at it again everytime this thread runs
                    deleteDocument(reference, ecp);
                }

            } catch (Exception e) {
                log.error("problem updating content indexing for entity: " + reference + " error: " + e.getMessage());
            }
        }

        flushIndex();
        refreshIndex();
        lastLoad = new Date().getTime();
        if (hits.length > 0) {
            log.debug("Finished indexing " + hits.length + " docs in " +
                    ((new Date().getTime() - startTime.getTime()) / 1000) + " seconds.");
        }

    }

    /**
     * Extract properties from the {@link EntityContentProducer}
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
    private Map<String, Collection<String>> extractCustomProperties(String resourceName, EntityContentProducer contentProducer) {
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
                    log.warn("Couldn't find what the value for '" + propertyName + "' was. It has been ignored. " + propertyName.getClass());
                values = Collections.emptyList();
            }

            //If this property was already present there (this shouldn't happen, but if it does everything must be stored
            if (properties.containsKey(propertyName)) {
                log.warn("Two properties had a really similar name and were merged. This shouldn't happen! " + propertyName);
                log.debug("Merged values '" + properties.get(propertyName) + "' with '" + values);
                values = new ArrayList<String>(values);
                values.addAll(properties.get(propertyName));
            }

            properties.put(propertyName, values);
        }

        return properties;
    }


    /**
     * refresh the index from the current stored state {@inheritDoc}
     */
    public void refreshIndex() {
        RefreshResponse response = client.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
    }

    public void destroy() {
    }

    @Override
    public int getPendingDocuments() {
        CountResponse response = client.prepareCount(indexName)
                .setQuery(filteredQuery(matchAllQuery(), missingFilter(SearchService.FIELD_CONTENTS)))
                .execute()
                .actionGet();
        return (int) response.count();
    }

    protected void initMapping() {
        // if there is a value here its been overridden by injection, we will use the overridden configuration
        if (org.apache.commons.lang.StringUtils.isEmpty(mapping)) {
            try {
                StringWriter writer = new StringWriter();
                IOUtils.copy(ElasticSearchService.class.getResourceAsStream("/org/sakaiproject/search/elastic/bundle/mapping.json"), writer, "UTF-8");
                mapping = writer.toString();
            } catch (Exception ex) {
                log.error("Failed to load Stop words into Analyzer", ex);
            }
        }
        log.debug("ElasticSearch mapping will be configured as follows:" + mapping);
    }

    /**
     * creates a new index if one does not exist
     */
    public void assureIndex() {
        IndicesExistsResponse response = client.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet();
        if (!response.exists()) {
            createIndex();
        }
    }

    /**
     * creates a new index, does not check if the exist exists
     */
    public void createIndex() {
        // create index
        CreateIndexResponse createResponse = client.admin().indices().create(new CreateIndexRequest(indexName).mapping(ElasticSearchService.SAKAI_DOC_TYPE, mapping)).actionGet();
        if (!createResponse.acknowledged()) {
            log.error("Index wasn't created, can't rebuild");
        }

        client.admin().cluster().health(new ClusterHealthRequest(indexName).waitForYellowStatus()).actionGet();
    }


    /**
     * removes any existing index and creates a new one
     */
    public void recreateIndex() {
        IndicesExistsResponse response = client.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet();
        if (response.exists()) {

            DeleteIndexResponse delete = client.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();
            if (!delete.acknowledged()) {
                log.error("Index wasn't deleted, can't recreate it");
                return;
            }
        }

        // create index
        createIndex();

    }

    /**
     * Removes any existing index, creates a new index, and rebuilds the index from the entities own stored state {@inheritDoc}
     */
    public void rebuildIndex() {
        recreateIndex();

        // rebuild index
        for (Site s : siteService.getSites(SiteService.SelectionType.ANY, null, null, null, SiteService.SortType.NONE, null)) {
            if (isSiteIndexable(s)) {
                rebuildIndex(s.getId());
            }
        }
    }

    /**
     * causes elasticsearch write any in memory index changes to  storage
     */
    protected void flushIndex() {
        //flush
        client.admin().indices().flush(new FlushRequest(indexName).refresh(true)).actionGet();
    }

    /**
     * Check if a site is considered as indexable based on the current server configuration.
     * <p>
     * Not indexable sites are:
     * <ul>
     * <li>Special sites</li>
     * <li>Sites without the search tool (if the option is enabled)</li>
     * <li>User sites (if the option is enabled)</li>
     * </ul>
     * </p>
     *
     * @param site site which may be indexable
     * @return true if the site can be index, false otherwise
     */
    private boolean isSiteIndexable(Site site) {
        log.debug("Check if '" + site + "' is indexable.");
        return !(siteService.isSpecialSite(site.getId()) ||
                (isOnlyIndexSearchToolSites() && site.getToolForCommonId(SEARCH_TOOL_ID) == null) ||
                (isExcludeUserSites() && siteService.isUserSite(site.getId())));
    }

    @Override
    public boolean isBuildQueueEmpty() {
        return getPendingDocuments() == 0;
    }


    /**
     * Generates a SearchableEntityProducer
     *
     * @param ref
     * @return
     * @throws PermissionException
     * @throws IdUnusedException
     * @throws TypeException
     */
    public EntityContentProducer newEntityContentProducer(String ref) {
        log.debug(" new entitycontent producer");
        for (Iterator<EntityContentProducer> i = producers.iterator(); i.hasNext(); ) {
            EntityContentProducer ecp = i.next();
            if (ecp.matches(ref)) {
                return ecp;
            }
        }
        return null;
    }

    @Override
    public List<SearchBuilderItem> getSiteMasterSearchItems() {
        return Collections.emptyList();
    }

    @Override
    public List<SearchBuilderItem> getGlobalMasterSearchItems() {
        return Collections.emptyList();
    }

    /**
     * get hold of an entity content producer using the event
     *
     * @param event
     * @return
     */
    public EntityContentProducer newEntityContentProducer(Event event) {
        log.debug(" new entitycontent producer");
        for (Iterator<EntityContentProducer> i = producers.iterator(); i.hasNext(); ) {
            EntityContentProducer ecp = i.next();
            if (ecp.matches(event)) {
                log.debug(" Matched Entity Content Producer for event " + event
                        + " with " + ecp);
                return ecp;
            } else {
                log.debug("Skipped ECP " + ecp);
            }
        }
        log.debug("Failed to match any Entity Content Producer for event " + event);
        return null;
    }

    protected EntityContentProducer getContentProducerForReference(String ref) {
        for (EntityContentProducer ecp : producers) {
            if (ecp.matches(ref)) {
                return ecp;
            }
        }
        return null;
    }


    /**
     * get all the producers registered, as a clone to avoid concurrent
     * modification exceptions
     *
     * @return
     */
    public List<EntityContentProducer> getContentProducers() {
        return new ArrayList<EntityContentProducer>(producers);
    }


    /**
     * Rebuild the index from the entities own stored state {@inheritDoc}, for just
     * the supplied siteId
     */
    public void rebuildIndex(String siteId) {
        bulkContentIndexTimer.schedule(new RebuildSiteTask(siteId), 0);
    }

    protected void deleteAllDocumentForSite(String siteId) {
        log.debug("removing all documents from search index for siteId: " + siteId);
        DeleteByQueryResponse response = client.prepareDeleteByQuery(indexName)
                .setQuery(termQuery(SearchService.FIELD_SITEID, siteId))
                .setTypes(ElasticSearchService.SAKAI_DOC_TYPE)
                .execute()
                .actionGet();
    }

    protected void deleteDocument(String ref, EntityContentProducer ecp) {
        log.debug("deleting " + ref + " from the search index");
        DeleteResponse response = client.prepareDelete(indexName, ecp.getType(ref), ecp.getId(ref))
                .setType(ElasticSearchService.SAKAI_DOC_TYPE)
                .execute()
                .actionGet();
        //flushIndex();
    }

    /**
     * Refresh the index for the supplied site.  This simply refreshes the docs that ES already knows about.
     * It does not create any new docs.  If you want to reload all site content you need to do a {@see rebuildIndex()}
     */
    public void refreshIndex(String siteId) {
        log.info("Refreshing the index for '" + siteId + "'");
        //Get the currently indexed resources for this site
        Collection<String> resourceNames = getResourceNames(siteId);
        log.debug(resourceNames.size() + " elements will be refreshed");
        for (String resourceName : resourceNames) {
            EntityContentProducer entityContentProducer = getContentProducerForReference(resourceName);

            //If there is no matching entity content producer or no associated site, skip the resource
            //it is either not available anymore, or the corresponding entityContentProducer doesn't exist anymore
            if (entityContentProducer == null || entityContentProducer.getSiteId(resourceName) == null) {
                log.warn("Couldn't either find an entityContentProducer or the resource itself for '" + resourceName + "'");
                continue;
            }

            prepareIndexAdd(resourceName, entityContentProducer, false);
        }

        flushIndex();
        refreshIndex();
    }

    /**
     * Get all indexed resources for a site
     *
     * @param siteId Site containing indexed resources
     * @return a collection of resource references or an empty collection if no resource was found
     */
    protected Collection<String> getResourceNames(String siteId) {
        log.debug("Obtaining indexed elements for site: '" + siteId + "'");

        SearchResponse response = client.prepareSearch(indexName)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(termQuery(SearchService.FIELD_SITEID, siteId))
                .setTypes(ElasticSearchService.SAKAI_DOC_TYPE)
                .setSize(Integer.MAX_VALUE)
                .execute()
                .actionGet();

        Collection<String> resourceNames = new ArrayList<String>();
        for (SearchHit hit : response.getHits().hits()) {
            resourceNames.add(getFieldFromSearchHit(SearchService.FIELD_REFERENCE, hit));
        }
        return resourceNames;

    }

    /**
     * loads the field from the SearchHit. It does this by looking at the source, not the fields.
     * This is safer as depending on how the SearchHit was loaded the field data might not be there,
     * but there will always be a copy in the source.
     * @param field
     * @param hit
     * @return
     */
    static public String getFieldFromSearchHit(String field, SearchHit hit) {
        if (hit == null || hit.getSource().get(field) == null) {
            return null;
        }
        return (String) hit.getSource().get(field);
    }

    @Override
    public List<SearchBuilderItem> getAllSearchItems() {
        return null;
    }


    public static enum IndexAction {
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

    /**
     * @return the onlyIndexSearchToolSites
     */
    public boolean isOnlyIndexSearchToolSites() {
        return onlyIndexSearchToolSites;
    }

    /**
     * @param onlyIndexSearchToolSites the onlyIndexSearchToolSites to set
     */
    public void setOnlyIndexSearchToolSites(boolean onlyIndexSearchToolSites) {
        this.onlyIndexSearchToolSites = onlyIndexSearchToolSites;
    }


    public void setExcludeUserSites(boolean excludeUserSites) {
        this.excludeUserSites = excludeUserSites;
    }

    public boolean isExcludeUserSites() {
        // TODO Auto-generated method stub
        return excludeUserSites;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }



    public Date getStartTime() {
        return startTime;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public void setContentIndexBatchSize(int contentIndexBatchSize) {
        this.contentIndexBatchSize = contentIndexBatchSize;
    }

    public List<EntityContentProducer> getProducers() {
        return producers;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }
}
