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

package org.sakaiproject.samigo.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.htmlparser.jericho.Source;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.lang3.ArrayUtils;
import org.elasticsearch.common.lang3.tuple.Pair;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import static org.elasticsearch.index.query.FilterBuilders.missingFilter;
import static org.elasticsearch.index.query.FilterBuilders.orFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.facet.FacetBuilders.termsFacet;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryRequest;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryResponse;
import org.elasticsearch.action.admin.indices.validate.query.QueryExplanation;
import org.elasticsearch.action.support.QuerySourceBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.action.ActionFuture;
import org.osid.shared.SharedException;
import org.slf4j.Logger;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.elasticsearch.BaseElasticSearchIndexBuilder;
import org.sakaiproject.search.elasticsearch.NoContentException;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolIteratorFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;

@Slf4j
public class QuestionElasticSearchIndexBuilder extends BaseElasticSearchIndexBuilder {

    QuestionPoolService questionPoolService  = new QuestionPoolService();
    private SiteService siteService;
    protected String[] searchResultFieldNames;

    protected static final String SAKAI_DOC_TYPE = "question_doc";
    protected static final String ADD_RESOURCE_VALIDATION_KEY_ITEM = "questionId";
    protected static final String DELETE_RESOURCE_KEY_ITEM = "questionId";

    /**
     * set to true to force an index rebuild at startup time, defaults to false.  This is probably something
     * you never want to use, other than in development or testing
     */

    protected boolean useSuggestions = false;
    protected boolean useFacetting = false;

    /**
     * We will use aggregations to retrun only one item from all the items with the same hash.
     */
    protected boolean useAggregation = true;
    protected static final String AGGREGATION_NAME = "dedup";
    protected static final String AGGREGATION_TOP_HITS = "dedup_docs";

    /**
     * Gives subclasses a chance to initialize configuration prior to reading/processing any
     * ES configs. May be important for setting up defaults, for example, or for ensuring
     * subclass-specific configs are in place before any background tasks are in place.
     * (Though the latter would be better factored into {@link #beforeBackgroundSchedulerInitialization()}
     */
    @Override
    protected void beforeElasticSearchConfigInitialization() {
        if (StringUtils.isEmpty(this.indexedDocumentType)) {
            this.indexedDocumentType = SAKAI_DOC_TYPE;
        }
        if (ArrayUtils.isEmpty(this.suggestionResultFieldNames)) {
            this.suggestionResultFieldNames = new String[] {
            };
        }
        if ( ArrayUtils.isEmpty(this.searchResultFieldNames)) {
            this.searchResultFieldNames = new String[] {
                    SearchService.FIELD_CONTENTS,
                    "questionId",
                    "site",
                    "tags",
                    "questionPoolId",
                    "assessmentId",
                    "hash",
                    "type",
                    "subtype"
            };
        }
    }

    /**
     * Called after all ES config has been processed but before the background scheduler has been set up
     * and before any index startup ops have been invoked ({@link #initializeIndex()}. I.e. this is a
     * subclass's last chance to set up any configs on which background jobs and/or index maintenance
     * in general might depend.
     */
    @Override
    protected void beforeBackgroundSchedulerInitialization(){
        //no-op
    }

    @Override
    protected SearchRequestBuilder completeFindContentQueueRequestBuilder(SearchRequestBuilder searchRequestBuilder){
        return searchRequestBuilder;
    }

    @Override
    protected DeleteRequestBuilder completeDeleteRequestBuilder(DeleteRequestBuilder deleteRequestBuilder,
                                                                Map<String, Object> deleteParams) {
        return deleteRequestBuilder.setRouting((String)deleteParams.get(DELETE_RESOURCE_KEY_ITEM));
    }


    public void rebuildIndex(String siteId) {
        if (testMode) {
            rebuildSiteIndex(siteId);
            return;
        }
        backgroundScheduler.schedule(new RebuildSiteTask(siteId), 0);
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
            try {
                // let's not hog the whole CPU just in case you have lots of sites with lots of data this could take a bit
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
                rebuildSiteIndex(siteId);
            } catch (Exception e) {
                getLog().error("problem queuing content indexing for site: " + siteId + " error: " + e.getMessage());
            }
        }

    }

    @Override
    protected void rebuildIndexImmediately(){
         //1. ITEMS in assessments will be indexed for each site
        for (Site s : siteService.getSites(SiteService.SelectionType.ANY, null, null, null, SiteService.SortType.NONE, null)) {
            if (isSiteIndexable(s)) {
                rebuildSiteIndex(s.getId());
            }
        }

        //2. Items in Question Pools
       List allPools =  questionPoolService.getAllPools();
        Iterator<QuestionPoolData> qpit = allPools.iterator();

            while (qpit.hasNext()) {
                String qpId = Long.toString(qpit.next().getQuestionPoolId());
                rebuildQuestionPoolIndex(qpId);
            }
    }

    protected boolean isSiteIndexable(Site site) {
        //We don't need to check the user sites or special sites.
        return !(siteService.isSpecialSite(site.getId()) ||
                siteService.isUserSite(site.getId()));
    }


    protected void rebuildQuestionPoolIndex(String qpId)  {
        getLog().info("Rebuilding the index for QP: '" + qpId + "'");

        try {
            List qpItemsIds = questionPoolService.getAllItemsIds(Long.valueOf(qpId));

            enableAzgSecurityAdvisor();
            deleteAllDocumentForQuestionPool(qpId);

            long start = System.currentTimeMillis();
            int numberOfDocs = 0;

            BulkRequestBuilder bulkRequest = client.prepareBulk();


            EntityContentProducer ecp = new ItemContentProducer();

            for (Iterator i = qpItemsIds.iterator(); i.hasNext(); ) {

                    if (bulkRequest.numberOfActions() < bulkRequestSize) {

                        String reference = Long.toString((Long)i.next());
                            try {
                                deleteDocument(ecp.getId(reference));
                                bulkRequest.add(prepareIndex(reference, ecp, false));
                                numberOfDocs++;
                            } catch (Exception e) {
                                getLog().error(e.getMessage(), e);
                            }

                    } else {
                        executeBulkRequest(bulkRequest);
                        bulkRequest = client.prepareBulk();
                    }
                }

                // execute any remaining bulks requests not executed yet
                if (bulkRequest.numberOfActions() > 0) {
                    executeBulkRequest(bulkRequest);
                }

            getLog().info("Queued " + numberOfDocs + " docs for indexing from question pool: " + qpId + " in " + (System.currentTimeMillis() - start) + " ms");

        } catch (Exception e) {
            getLog().error("An exception occurred while rebuilding the index of question pool '" + qpId + "'", e);
        } finally {
            disableAzgSecurityAdvisor();
        }
    }



    protected void rebuildSiteIndex(String siteId)  {
        getLog().info("Rebuilding the index for '" + siteId + "'");

        try {
            enableAzgSecurityAdvisor();
            deleteAllDocumentForSite(siteId);

            long start = System.currentTimeMillis();
            int numberOfDocs = 0;

            BulkRequestBuilder bulkRequest = client.prepareBulk();

            Set<EntityContentProducer> questionProducers = Sets.newConcurrentHashSet();
            questionProducers.add(new ItemContentProducer());
            questionProducers.add(new PublishedItemContentProducer());

            for (final EntityContentProducer ecp : questionProducers) {

                for (Iterator i = ecp.getSiteContentIterator(siteId); i.hasNext(); ) {

                    if (bulkRequest.numberOfActions() < bulkRequestSize) {

                        String reference = Long.toString((Long)i.next());
                            //updating was causing issues without a _source, so doing delete and re-add
                            try {
                                deleteDocument(ecp.getId(reference));
                                bulkRequest.add(prepareIndex(reference, ecp, false));
                                numberOfDocs++;
                            } catch (Exception e) {
                                getLog().error(e.getMessage(), e);
                            }

                    } else {
                        executeBulkRequest(bulkRequest);
                        bulkRequest = client.prepareBulk();
                    }
                }

                // execute any remaining bulks requests not executed yet
                if (bulkRequest.numberOfActions() > 0) {
                    executeBulkRequest(bulkRequest);
                }

            }

            getLog().info("Queued " + numberOfDocs + " docs for indexing from site: " + siteId + " in " + (System.currentTimeMillis() - start) + " ms");

        } catch (Exception e) {
            getLog().error("An exception occurred while rebuilding the index of '" + siteId + "'", e);
        } finally {
            disableAzgSecurityAdvisor();
        }
    }

    //Used in the content producer
    private void deleteAllDocumentForSite(String siteId) {
        getLog().debug("removing all documents from question index for siteId: " + siteId);
        DeleteByQueryResponse response = client.prepareDeleteByQuery(indexName)
                .setQuery(termQuery("site", siteId))
                .setTypes(indexedDocumentType)
                .execute()
                .actionGet();
    }

    public void deleteAllDocumentForSiteIfDeleted(String siteId) {

         try {
            if ((!(siteService.siteExists(siteId))) || (siteService.getSite(siteId).isSoftlyDeleted())) {
                deleteAllDocumentForSite(siteId);
            }
        }catch(Exception ex){
            //Never happens, but if it happens means that the site is not available, so we continue deleting
             //Because we don't want elelemts indexed in a site that doesn't exist.
            deleteAllDocumentForSite(siteId);
        }
    }



        protected void deleteAllDocumentForQuestionPool(String qpId) {
        getLog().debug("removing all documents from question index for questionPool: " + qpId);
        DeleteByQueryResponse response = client.prepareDeleteByQuery(indexName)
                .setQuery(termQuery("questionPoolId", qpId))
                .setTypes(indexedDocumentType)
                .execute()
                .actionGet();
    }

    protected void deleteDocument(String id) {
        final Map<String, Object> params = Maps.newHashMap();
        params.put("questionId", id);
        deleteDocumentWithParams(params);
    }


    @Override
    protected IndexRequestBuilder completeIndexRequestBuilder(IndexRequestBuilder requestBuilder,
                                                              String resourceName, EntityContentProducer ecp,
                                                              boolean includeContent)
            throws IOException{
        return requestBuilder.setRouting(null);
    }


    @Override
    public EntityContentProducer newEntityContentProducer(String ref) {
        //We will select between the items and the publishedItems
        if (ref.contains("/sam_item/") || ref.contains(" itemId=")){
                getLog().debug("Matched content producer ItemContentProducer for reference " + ref + " in index builder "
                    + getName());
            return new ItemContentProducer();
        }
        if (ref.contains("/sam_publisheditem/") || ref.contains(" publishedItemId=")){
            getLog().debug("Matched content producer PublishedItemContentProducer for reference " + ref + " in index builder "
                    + getName());
            return new PublishedItemContentProducer();
        }
        getLog().debug("Failed to match any content producer for reference " + ref + " in index builder " + getName());
        return null;
    }

    @Override
    public EntityContentProducer newEntityContentProducer(Event event) {
        ItemContentProducer icp = new ItemContentProducer();
        Set<String> triggerIcp = icp.getTriggerFunctions();
        if (triggerIcp.contains(event.getEvent())){
            getLog().debug("we have a ItemContentProducer for the event " + event + " in index builder " + getName());
            return new ItemContentProducer();
        }
        PublishedItemContentProducer picp = new PublishedItemContentProducer();
        Set<String> triggerPicp = picp.getTriggerFunctions();
        if (triggerPicp.contains(event.getEvent())){
            getLog().debug("we have a PublishedContentProducer for the event " + event + " in index builder " + getName());
            return new PublishedItemContentProducer();
        }
        getLog().debug("Failed to match any content producer for event " + event + " in index builder " + getName());
        return null;
    }

    @Override
    protected XContentBuilder addFields(XContentBuilder contentSourceBuilder, String resourceName,
                                        EntityContentProducer ecp, boolean includeContent) throws IOException{
        if (includeContent || testMode) {
        if (ecp.getSubType(resourceName).equals("item")){
            ItemContentProducer icp = (ItemContentProducer)ecp;

            //We only want the siteId if the question is not in a question pool
            //in that way we can delete all the items in a site w/o affect the
            //questionpools.

                HashMap<String,Object> allFields = icp.getAllFields(resourceName);

                if (allFields.get("isFromQuestionPool").equals("false")){
                    contentSourceBuilder.field("site", allFields.get("site"));
                    contentSourceBuilder.field("questionPoolId", new ArrayList<String>());
            }else{
                contentSourceBuilder.field("site", new ArrayList<String>());
                    contentSourceBuilder.field("questionPoolId", allFields.get("questionPoolId"));
            }
                return contentSourceBuilder.field("questionId", allFields.get("questionId"))
                    .field("tags", allFields.get("tags"))
                    .field("assessmentId", allFields.get("assessmentId"))
                    .field("hash",  allFields.get("hash"))
                    .field("type",  allFields.get("type"))
                    .field("subtype",  allFields.get("subtype"))
                    .field("typeId",  allFields.get("typeId"))
                    .field("qText",  allFields.get("qText"));

            }else if (ecp.getSubType(resourceName).equals("publisheditem")){
                PublishedItemContentProducer picp = (PublishedItemContentProducer)ecp;

                HashMap<String,Object> allFieldsPub = picp.getAllFields(resourceName);

                return contentSourceBuilder.field("questionId", allFieldsPub.get("questionId"))
                        .field("site", allFieldsPub.get("site"))
                        .field("tags", allFieldsPub.get("tags"))
                        .field("questionPoolId", new ArrayList<String>())
                        .field("assessmentId", allFieldsPub.get("assessmentId"))
                        .field("hash", allFieldsPub.get("hash"))
                        .field("type", allFieldsPub.get("type"))
                        .field("subtype",  allFieldsPub.get("subtype"))
                        .field("typeId",  allFieldsPub.get("typeId"))
                        .field("qText",  allFieldsPub.get("qText"));

            }else{
                return contentSourceBuilder.field("questionId", ecp.getId(resourceName));
            }
        }else{
            if (ecp.getSubType(resourceName).equals("item")){
                ItemContentProducer icp = (ItemContentProducer)ecp;
            return contentSourceBuilder.field("questionId", icp.getId(resourceName))
                    .field("subtype", icp.getSubType(resourceName));
        }else if (ecp.getSubType(resourceName).equals("publisheditem")){
            PublishedItemContentProducer picp = (PublishedItemContentProducer)ecp;
            return contentSourceBuilder.field("questionId", picp.getId(resourceName))
                    .field("subtype", picp.getSubType(resourceName));
        }else{
            return contentSourceBuilder.field("questionId", ecp.getId(resourceName));
        }
    }
    }



    @Override
    protected XContentBuilder addCustomProperties(XContentBuilder contentSourceBuilder, String resourceName,
                                                  EntityContentProducer ecp,
                                                  boolean includeContent) throws IOException {
        if (includeContent || testMode) {
        Map<String, Collection<String>> properties = extractCustomProperties(resourceName, ecp);
        for (Map.Entry<String, Collection<String>> entry : properties.entrySet()) {
            contentSourceBuilder = contentSourceBuilder.field(entry.getKey(), entry.getValue());
        }
        }
        return contentSourceBuilder;
    }



    @Override
    protected XContentBuilder noContentForIndexRequest(XContentBuilder contentSourceBuilder, String resourceName,
                                                       EntityContentProducer ecp, boolean includeContent)
            throws NoContentException {
        throw new NoContentException(ecp.getId(resourceName), resourceName, ecp.getSiteId(resourceName));
    }

    @Override
    protected XContentBuilder completeIndexRequestContentSourceBuilder(XContentBuilder contentSourceBuilder,
                                                                       String resourceName, EntityContentProducer ecp,
                                                                       boolean includeContent) throws IOException {
        return contentSourceBuilder.endObject();
    }


    @Override
    protected void completeAddResourceEventValidations(Event event, Map<String, Object> validationContext)
            throws IllegalArgumentException, IllegalStateException{
        //no-op
    }

    @Override
    protected DeleteRequestBuilder prepareDeleteDocument(Map<String, Object> deleteParams) {
        DeleteRequestBuilder deleteRequestBuilder = newDeleteRequestBuilder(deleteParams);
        deleteRequestBuilder = completeDeleteRequestBuilder(deleteRequestBuilder, deleteParams);
        return deleteRequestBuilder;
    }

    private DeleteRequestBuilder newDeleteRequestBuilder(Map<String, Object> deleteParams) {
        return client.prepareDelete(indexName, indexedDocumentType, (String)deleteParams.get(DELETE_RESOURCE_KEY_ITEM));
    }

    @Override
    protected Map<String, Object> extractDeleteDocumentParams(Map<String, Object> validationContext) {
        final Map<String,Object> params = Maps.newHashMap();
        params.put(DELETE_RESOURCE_KEY_ITEM, validationContext.get(ADD_RESOURCE_VALIDATION_KEY_RESOURCE_NAME));
        return params;
    }

    @Override
    protected Map<String, Object> extractDeleteDocumentParams(NoContentException noContentException) {
        Map<String,Object> params = Maps.newHashMap();
        params.put(DELETE_RESOURCE_KEY_ITEM, noContentException.getId());
        return params;
    }

    @Override
    protected Map<String, Object> extractDeleteDocumentParams(SearchHit searchHit) {
        String id = getFieldFromSearchHit("questionId", searchHit);
        final Map<String, Object> params = Maps.newHashMap();
        params.put(DELETE_RESOURCE_KEY_ITEM, id);
        return params;
    }

    @Override
    protected DeleteResponse deleteDocumentWithRequest(DeleteRequestBuilder deleteRequestBuilder) {
        return deleteRequestBuilder.execute().actionGet();
    }

    @Override
    protected void validateResourceName(Event event, Map<String, Object> validationContext)
            throws IllegalArgumentException, IllegalStateException {
        String resourceName = event.getResource();

        if (resourceName == null) {
            // default if null (historical behavior)
            resourceName = "";
        }
        if (resourceName.indexOf(" itemId=")!=-1){
            resourceName = "/sam_item/" + resourceName.substring(resourceName.indexOf(" itemId=") + 8);
        }
        if (resourceName.indexOf(" publishedItemId=")!=-1){
            resourceName = "/sam_publisheditem/" + resourceName.substring(resourceName.indexOf(" publishedItemId=") + 17);
        }
        if (resourceName.length() > 255) {
            throw new IllegalArgumentException("Entity Reference is longer than 255 characters. Reference="
                    + resourceName);
        }
        validationContext.put(ADD_RESOURCE_VALIDATION_KEY_RESOURCE_NAME, resourceName);
    }

    @Override
    protected void validateIndexable(Event event, Map<String, Object> validationContext)
            throws IllegalArgumentException, IllegalStateException {
        final EntityContentProducer ecp = (EntityContentProducer)validationContext.get(ADD_RESOURCE_VALIDATION_KEY_CONTENT_PRODUCER);
        final String resourceName = (String)validationContext.get(ADD_RESOURCE_VALIDATION_KEY_RESOURCE_NAME);
        String id1;

        if (event.getResource().indexOf(".delete@/") != -1){
            if (event.getResource().indexOf(" itemId=")!=-1){
                id1 = "/sam_item/" + event.getResource().substring(event.getResource().indexOf(" itemId=") + 8);
            }else if (event.getResource().indexOf(" publishedItemId=")!=-1){
                id1 = "/sam_publisheditem/" + event.getResource().substring(event.getResource().indexOf(" publishedItemId=") + 17);
            }else{
                id1 = "error";
            }
        }else{
            id1 = ecp.getId(resourceName);
        }
        final String id = id1;

        if ( StringUtils.isEmpty(id) ) {
            throw new IllegalArgumentException("Entity ID could not be derived from resource name [" + resourceName
                    + "] for event [" + event + "] in index builder [" + getName() + "]");
        }
        validationContext.put(ADD_RESOURCE_VALIDATION_KEY_ENTITY_ID, id);
    }

    @Override
    protected void validateIndexAction(Event event, Map<String, Object> validationContext)
            throws IllegalArgumentException, IllegalStateException, UnsupportedOperationException {
        final EntityContentProducer ecp = (EntityContentProducer)validationContext.get(ADD_RESOURCE_VALIDATION_KEY_CONTENT_PRODUCER);
        IndexAction action1;
        if (ecp.getAction(event)==100){
            //if we need to delete all the site items we delete it and continue with Action_unknown
            //to stop the process.
            String siteId = event.getResource().substring(6);
            deleteAllDocumentForSiteIfDeleted(siteId);
            action1 = IndexAction.getAction(SearchBuilderItem.ACTION_UNKNOWN);
        }else {
            action1 = IndexAction.getAction(ecp.getAction(event));
        }
        final IndexAction action = action1;
        if ( !(isSupportedIndexAction(action)) ) {
            throw new UnsupportedOperationException("Event [" + event
                    + "] resolved to an unsupported IndexAction [" + action + "] in index builder [" + getName() + "]");
        }
        validationContext.put(ADD_RESOURCE_VALIDATION_KEY_INDEX_ACTION, action);
    }

    @Override
    protected void dispatchValidatedAddResource(Map<String, Object> validationContext) {
        final IndexAction indexAction = (IndexAction)validationContext.get(ADD_RESOURCE_VALIDATION_KEY_INDEX_ACTION);
        final String resourceName = (String)validationContext.get(ADD_RESOURCE_VALIDATION_KEY_RESOURCE_NAME);
        final EntityContentProducer ecp = (EntityContentProducer)validationContext.get(ADD_RESOURCE_VALIDATION_KEY_CONTENT_PRODUCER);
        log.debug("Action on '" + resourceName + "' detected as " + indexAction.name() + " in index builder "
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

    @Override
    protected void deleteDocumentWithParams(Map<String, Object> deleteParams) {
        final DeleteRequestBuilder deleteRequestBuilder = prepareDeleteDocument(deleteParams);
        final DeleteResponse deleteResponse = deleteDocumentWithRequest(deleteRequestBuilder);
        if (getLog().isDebugEnabled()) {
            if (!deleteResponse.isFound()) {
                getLog().debug("Could not delete doc with by id: " + deleteParams.get(DELETE_RESOURCE_KEY_ITEM)
                        + " in index builder [" + getName() + "] because the document wasn't found");
            } else {
                getLog().debug("ES deleted a doc with id: " + deleteResponse.getId() + " in index builder ["
                        + getName() + "]");
            }
        }
    }

    @Override
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
        getLog().trace(getPendingDocuments() + " pending docs for index builder [" + getName() + "]");

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
            getLog().trace("Finished indexing " + hits.length + " docs in " +
                    ((lastLoad - startTime)) + " ms for index builder " + getName());
        }

    }

    @Override
    protected SearchRequestBuilder addFindContentQueueRequestParams(SearchRequestBuilder searchRequestBuilder) {
        return searchRequestBuilder
                .setQuery(matchAllQuery())
                .setTypes(indexedDocumentType)
                .setPostFilter( orFilter(
                        missingFilter(SearchService.FIELD_INDEXED),
                        termFilter(SearchService.FIELD_INDEXED, false)))
                .setSize(contentIndexBatchSize)
                .addFields("questionId", "subtype");
    }


    @Override
    protected void processContentQueueEntry(SearchHit hit, BulkRequestBuilder bulkRequest) throws NoContentException {

        String reference = getFieldFromSearchHit("questionId", hit);
        String subtype = getFieldFromSearchHit("subtype", hit);
        EntityContentProducer ecp;
        if (subtype.equals("item")) {
            ecp = new ItemContentProducer();
        }else{
            ecp = new PublishedItemContentProducer();
        }

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

    /**
     *
     * @param resourceName
     * @param ecp
     * @return
     */
    @Override
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
    @Override
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
    public SearchResponse search(String searchTerms, List<String> references, List<String> siteIds, int start, int end) {
       return search(searchTerms,references, siteIds, start, end, new HashMap<>());
    }

    /**
     * This is a new search that accepts additionalSearchInformation. We need it for our complex question searches.
     * We have duplicated the methods that need this parameter, like prepareSearchRequest
     */
    public SearchResponse search(String searchTerms, List<String> references, List<String> siteIds, int start, int end, Map<String,String> additionalSearchInformation) {
        final Pair<SearchRequestBuilder,QueryBuilder> searchBuilders = prepareSearchRequest(searchTerms, references, siteIds, start, end, additionalSearchInformation);
        final SearchRequestBuilder searchRequestBuilder = searchBuilders.getLeft();
        final QueryBuilder queryBuilder = searchBuilders.getRight();

        getLog().debug("Search request from index builder [" + getName() + "]: " + searchRequestBuilder.toString());

        ValidateQueryRequest validateQueryRequest = new ValidateQueryRequest(indexName);
        QuerySourceBuilder querySourceBuilder = new QuerySourceBuilder().setQuery(queryBuilder);
        validateQueryRequest.source(querySourceBuilder);
        validateQueryRequest.explain(true);


        try {
            ActionFuture<ValidateQueryResponse> future = client.admin().indices().validateQuery(validateQueryRequest); // the client is org.elasticsearch.client.Client
            ValidateQueryResponse responseV = future.get(); // typical java future as response

            if (responseV.isValid()) {
                SearchResponse response = searchRequestBuilder.execute().actionGet();
                getLog().debug("Search request from index builder [" + getName() + "] took: " + response.getTook().format());
                eventTrackingService.post(eventTrackingService.newEvent(SearchService.EVENT_SEARCH,
                        SearchService.EVENT_SEARCH_REF + queryBuilder.toString(), true,
                        NotificationService.PREF_IMMEDIATE));

                return response;
            }else{
                return null;
            }
        }catch(Exception ex){
            return null;
        }

    }

    @Override
    protected Pair<SearchRequestBuilder,QueryBuilder> prepareSearchRequest(String searchTerms, List<String> references, List<String> siteIds, int start, int end) {
        return prepareSearchRequest(searchTerms,references, siteIds, start, end, new HashMap<>());
    }

    protected Pair<SearchRequestBuilder,QueryBuilder> prepareSearchRequest(String searchTerms, List<String> references, List<String> siteIds, int start, int end, Map<String,String> additionalSearchInformation) {
        // All this Pair<SearchRequestBuilder,QueryBuilder> business b/c:
        //    a) Legacy eventing in search() needs the QueryBuilder, not just the SearchRequestBuilder, and
        //    b) SiteId handling entails manipulation of both objects, so presumably completeSearchRequestBuilders()
        //       would as well
        //    c) There is no getQuery() on SearchRequestBuilder
        Pair<SearchRequestBuilder,QueryBuilder> builders = newSearchRequestAndQueryBuilders(searchTerms, references, siteIds);
        builders = addSearchCoreParams(builders, searchTerms, references, siteIds);
        builders = addSearchQuery(builders, searchTerms, references, siteIds, additionalSearchInformation);
        builders = pairOf(addSearchResultFields(builders.getLeft()), builders.getRight());
        builders = pairOf(addSearchPagination(builders.getLeft(), start, end), builders.getRight());
        builders = pairOf(addSearchFacetting(builders.getLeft()), builders.getRight());
        return completeSearchRequestBuilders(builders, searchTerms, references, siteIds);
    }

    @Override
    protected Pair<SearchRequestBuilder,QueryBuilder> newSearchRequestAndQueryBuilders(String searchTerms,
                                                                                       List<String> references,
                                                                                       List<String> siteIds) {
        return pairOf(client.prepareSearch(indexName), boolQuery());
    }

    @Override
    protected Pair<SearchRequestBuilder, QueryBuilder> addSearchCoreParams(Pair<SearchRequestBuilder, QueryBuilder> builders,
                                                                           String searchTerms, List<String> references,
                                                                           List<String> siteIds) {
        final SearchRequestBuilder searchRequestBuilder = builders.getLeft();
        return pairOf(searchRequestBuilder.setSearchType(SearchType.QUERY_THEN_FETCH).setTypes(indexedDocumentType),
                builders.getRight());
    }

    @Override
    protected Pair<SearchRequestBuilder,QueryBuilder> addSearchQuery(Pair<SearchRequestBuilder,QueryBuilder> builders,
                                                                     String searchTerms, List<String> references,
                                                                     List<String> siteIds) {
        return addSearchQuery(builders, searchTerms, references, siteIds, new HashMap<>());
    }

    protected Pair<SearchRequestBuilder,QueryBuilder> addSearchQuery(Pair<SearchRequestBuilder,QueryBuilder> builders,
                                                                     String searchTerms, List<String> references,
                                                                     List<String> siteIds, Map<String,String> additionalSearchInformation ) {
        builders = addSearchTerms(builders, searchTerms, additionalSearchInformation);
        builders = addSearchReferences(builders, references);
        builders = addSearchSiteIds(builders, siteIds);
        return pairOf(builders.getLeft().setQuery(builders.getRight()), builders.getRight());
    }

    @Override
    protected Pair<SearchRequestBuilder, QueryBuilder> addSearchTerms(Pair<SearchRequestBuilder, QueryBuilder> builders,
                                                                      String searchTerms) {
        return addSearchTerms(builders, searchTerms, new HashMap<>());
    }

    /**
     * Here we create our specific search query with the parameters that are sent in the additionalSearchInformation

     */
    protected Pair<SearchRequestBuilder, QueryBuilder> addSearchTerms(Pair<SearchRequestBuilder, QueryBuilder> builders,
                                                                      String searchTerms, Map<String,String> additionalSearchInformation) {
        BoolQueryBuilder query = (BoolQueryBuilder)builders.getRight();
        SearchRequestBuilder searchRequestBuilder = builders.getLeft();
        //We will receive the text in the searchTerms
        //This is were manage the TEXT query
        log.debug("Searching the searchterm: " + searchTerms);
        //First, let's remove the html code because it can cause exceptions.
        Source parseSearchTerms = new Source(searchTerms);
        searchTerms = parseSearchTerms.getTextExtractor().toString();
        //and now let's remove especial chars
        String regex = "([+\\-!\\(\\){}\\[\\]^~*?:\\\\]|[&\\|]{2})";
        searchTerms = searchTerms.replaceAll(regex,"\\\\$1");
        log.debug("Searching the searchterms after escape them: " + searchTerms);
        if (StringUtils.isNotEmpty(searchTerms)) {
            if (additionalSearchInformation.containsKey("logic") && additionalSearchInformation.get("logic").equals("and")) {
                query = query.must(queryStringQuery(searchTerms).defaultField("contents").defaultOperator(QueryStringQueryBuilder.Operator.AND));
            }else{
                query = query.must(queryStringQuery(searchTerms).defaultField("contents").defaultOperator(QueryStringQueryBuilder.Operator.OR));
            }
        }

        //We will receive the Tags in tag_1 / tag value, tag_2..., tag_n...
        Set<String> keys = additionalSearchInformation.keySet();
        Iterator<String> keysIterator = keys.iterator();
        Boolean oneTimeTagShould = true;
        BoolQueryBuilder tagsQuery = QueryBuilders.boolQuery();
        Boolean tagsFounded = false;
        while (keysIterator.hasNext()){
            String key = keysIterator.next();
            if (key.startsWith("tag_")){
                tagsFounded = true;
                String tag= additionalSearchInformation.get(key);
                log.debug("We will search this tag:" + tag);
                if (additionalSearchInformation.containsKey("logic") && additionalSearchInformation.get("logic").equals("or")) {
                    tagsQuery = tagsQuery.should(termQuery("tags", tag));
                    if (oneTimeTagShould){
                        tagsQuery.minimumNumberShouldMatch(1);
                    }
                    oneTimeTagShould = false;
                }else {
                    tagsQuery = tagsQuery.must(termQuery("tags", tag));
                }
            }
        }

        if (tagsFounded) {
            query = query.must(tagsQuery);
        }

        if (additionalSearchInformation.containsKey("subtype")){
            log.debug("We will search this subtype:" + additionalSearchInformation.get("subtype"));
            query = query.must(matchQuery("subtype", additionalSearchInformation.get("subtype")));
        }

        if (additionalSearchInformation.containsKey("hash")){
            log.debug("We will search this hash:" + additionalSearchInformation.get("hash"));
            query = query.must(matchQuery("hash", additionalSearchInformation.get("hash")));
        }

        //We will receive the scope of the search in the advancedSearchInformation Map...
        // scope "all" = no filter
        // scope "owns" or no scope = all their sites as instructor and qpools with permissions (That's the default value)
        // scope "custom" = we will filter all the site_1, site_2, site_3 and qp_1,qp2,qp3....

        if (additionalSearchInformation.containsKey("scope")){
            if (additionalSearchInformation.get("scope").equals("all")){
                //Doing nothing here
                log.debug("we won't filter the results by scope");
            } else if (additionalSearchInformation.get("scope").equals("custom")){
                log.debug("We have a custom scope request");
                Set<String> keysForACL = additionalSearchInformation.keySet();
                Iterator<String> keysForACLIterator = keysForACL.iterator();
                while (keysForACLIterator.hasNext()){
                    String key = keysForACLIterator.next();
                    List<String> siteIds = new ArrayList<>();
                    List<String> questionPoolsIds = new ArrayList<>();
                    if (key.startsWith("site_")){
                        String siteId= additionalSearchInformation.get(key);
                        log.debug("We have this siteId to add:" + siteId);
                        siteIds.add(siteId);
                        query = query.should(termsQuery("site", siteIds.toArray(new String[siteIds.size()])));

                    }

                    if (key.startsWith("qp_")){
                        String qpId= additionalSearchInformation.get(key);
                        log.debug("We have this question Pool to add:" + qpId);
                        questionPoolsIds.add(qpId);
                        query = query.should(termsQuery("questionPoolId", questionPoolsIds.toArray(new String[questionPoolsIds.size()])));
                        query = query.minimumNumberShouldMatch(1);
                    }
                }
            } else {
                if (!(securityService.isSuperUser())) {
                    log.debug("No superuser.Searching based in permissions");
                    List<String> siteIds = getAllSitesUserHasQuestionAuthorPermissions();
                    List<String> questionPoolsIds = getAllUserQuestionPools();
                    query = query.should(termsQuery("site", siteIds.toArray(new String[siteIds.size()])));
                    query = query.should(termsQuery("questionPoolId", questionPoolsIds.toArray(new String[questionPoolsIds.size()])));
                    query = query.minimumNumberShouldMatch(1);

                }else{
                    log.debug("Superuser, so no filtering");
                }

            }

            //If we want aggregation we will have a key "group" with the field on it.
            if (additionalSearchInformation.containsKey("group")) {
                log.debug("We are going to group results by: " + additionalSearchInformation.get("group"));
                //if we need aggregation, we don't need normal results.
                searchRequestBuilder.setSize(0);
                searchRequestBuilder = addSearchAggregation(searchRequestBuilder,additionalSearchInformation.get("group"));
            }

        }

        return pairOf(searchRequestBuilder, query);
    }


    /**
     * We implement the security here. So the search can filter to search only in the user's allowed places.
     * @return List of sites with question edit.
     */
    private List<String> getAllSitesUserHasQuestionAuthorPermissions(){

        List<String> sitesIds = new ArrayList<String>();

        try {
            List allSites = siteService.getSites(SiteService.SelectionType.ACCESS, null, null,
                    null, SiteService.SortType.TITLE_ASC, null);
            List moreSites = siteService.getSites(SiteService.SelectionType.UPDATE, null, null,
                    null, SiteService.SortType.TITLE_ASC, null);

            if ((allSites == null || moreSites == null) || (allSites.size() == 0 && moreSites.size() == 0)) {
                return sitesIds;
            }

            // Remove duplicates and combine two lists
            allSites.removeAll(moreSites);
            allSites.addAll(moreSites);

            //Select from these sites the ones with the right permission
            Iterator<Site> allSitesIterator = allSites.iterator();
            while (allSitesIterator.hasNext()){
                String siteId =  allSitesIterator.next().getId();
                log.debug("Checking if user "+AgentFacade.getAgentString()+" is allowed in this site:" +  siteId);
                if (securityService.unlock(AgentFacade.getAgentString(), "asn.new", siteService.siteReference(siteId))) {
                    sitesIds.add(siteId);
                    log.debug("User allowed in this site:" +  siteId);
                }
            }
            //To avoid search in all sites if someone is not in any site, if this is empty
            //we will return a "-2" as unique site id to search, that of course won't find results.
            if (sitesIds.size()==0){
                sitesIds.add("-2");
            }
            return sitesIds;
        } catch (Exception e) {
            log.error("Error in getAllSitesUserHasQuestionAuthorPermissions(): " + e.getClass().getName() + " : " + e.getMessage());
            return sitesIds;
        }

    }
    /**
     * We implement the security here. So the search can filter to search only in the user's allowed places.
     * @return List of question pools allowed to the user
     */
    private List<String> getAllUserQuestionPools() {
        List<String> questionPoolsIds = new ArrayList<String>();

        QuestionPoolIteratorFacade qpif = questionPoolService.getAllPoolsWithAccess(AgentFacade.getAgentString());
        try{
            while (qpif.hasNext()){
                String qpId = Long.toString(qpif.next().getQuestionPoolId());
                questionPoolsIds.add(qpId);
                log.debug("User allowed in this QP:" +  qpId);
            }
        }catch (SharedException e){
            log.error("Error retrieving questionPools for actual user: " + e.getClass().getName() + " : " + e.getMessage());
        }catch (Exception ex){
            log.error("Error retrieving questionPools for actual user: " + ex.getClass().getName() + " : " + ex.getMessage());
        }
        //To avoid search in all question pools if someone is not in a question pool, if this is empty
        //we will return a "-2" as unique question pool id to search.
        if (questionPoolsIds.size()==0){
            questionPoolsIds.add("-2");
        }
        return questionPoolsIds;
    }


        @Override
    protected Pair<SearchRequestBuilder, QueryBuilder> addSearchReferences(Pair<SearchRequestBuilder, QueryBuilder> builders,
                                                                           List<String> references) {
        BoolQueryBuilder query = (BoolQueryBuilder)builders.getRight();
        if (references.size() > 0){
            query = query.must(termsQuery(SearchService.FIELD_REFERENCE, references.toArray(new String[references.size()])));
        }
        return pairOf(builders.getLeft(), query);
    }

    @Override
    protected Pair<SearchRequestBuilder,QueryBuilder> addSearchSiteIds(Pair<SearchRequestBuilder,QueryBuilder> builders,
                                                                       List<String> siteIds){
           return builders;
    }

    @Override
    protected  SearchRequestBuilder addSearchResultFields(SearchRequestBuilder searchRequestBuilder) {
        if (ArrayUtils.isEmpty(searchResultFieldNames)) {
            return searchRequestBuilder;
        }
        return searchRequestBuilder.addFields(searchResultFieldNames);
    }

    @Override
    protected SearchRequestBuilder addSearchPagination(SearchRequestBuilder searchRequestBuilder, int start, int end) {
        return searchRequestBuilder.setFrom(start).setSize(end - start);
    }

    @Override
    protected SearchRequestBuilder addSearchFacetting(SearchRequestBuilder searchRequestBuilder) {
        if(useFacetting) {
            return searchRequestBuilder.addFacet(termsFacet(facetName).field("hash").size(facetTermSize));
        }
        return searchRequestBuilder;
    }

    protected SearchRequestBuilder addSearchAggregation(SearchRequestBuilder searchRequestBuilder, String field) {
        if(useAggregation) {
            return searchRequestBuilder.addAggregation(
                    AggregationBuilders.terms(AGGREGATION_NAME).field(field).size(serverConfigurationService.getInt("samigo.search.maxResults",50))
                            .subAggregation(AggregationBuilders.topHits(AGGREGATION_TOP_HITS).setSize(1).addFieldDataField("assessmentId").addFieldDataField("site").addFieldDataField("questionPoolId").addFieldDataField("typeId").addFieldDataField("tags").addFieldDataField("qText")));
            //.subAggregation(AggregationBuilders.topHits(AGGREGATION_TOP_HITS).setSize(1).addFieldDataField("origin")));

        }
        return searchRequestBuilder;
    }

    @Override
    protected Pair<SearchRequestBuilder,QueryBuilder> completeSearchRequestBuilders(Pair<SearchRequestBuilder,QueryBuilder> builders,
                                                                                    String searchTerms,
                                                                                    List<String> references,
                                                                                    List<String> siteIds){
        log.debug("This is the search query: " + builders.getRight().toString());
        return builders;


    }

    @Override
    protected Pair<SearchRequestBuilder,QueryBuilder> completeSearchSuggestionsRequestBuilders(Pair<SearchRequestBuilder, QueryBuilder> builders,
                                                                                               String searchString,
                                                                                               String currentSite,boolean allMySites){
        //no-op, no suggestions on this index
        return null;
    }

    @Override
    protected Pair<SearchRequestBuilder,QueryBuilder> addSearchSuggestionsTerms(Pair<SearchRequestBuilder, QueryBuilder> builders,
                                                                                String searchString) {
        //no-op, no suggestions on this index
        return null;
    }

    @Override
    protected Pair<SearchRequestBuilder, QueryBuilder> addSearchSuggestionsSites(Pair<SearchRequestBuilder, QueryBuilder> builders, String currentSite, boolean allMySites){
        //no-op, no suggestions on this index
        return null;
    }

    @Override
    public String getEventResourceFilter() {
        return "/";
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
