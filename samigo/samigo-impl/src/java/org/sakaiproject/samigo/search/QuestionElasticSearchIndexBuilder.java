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

import static org.opensearch.index.query.QueryBuilders.boolQuery;
import static org.opensearch.index.query.QueryBuilders.existsQuery;
import static org.opensearch.index.query.QueryBuilders.matchAllQuery;
import static org.opensearch.index.query.QueryBuilders.matchQuery;
import static org.opensearch.index.query.QueryBuilders.queryStringQuery;
import static org.opensearch.index.query.QueryBuilders.termQuery;
import static org.opensearch.index.query.QueryBuilders.termsQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.action.admin.indices.validate.query.ValidateQueryRequest;
import org.opensearch.action.admin.indices.validate.query.ValidateQueryResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchType;
import org.opensearch.client.RequestOptions;
import org.opensearch.common.util.set.Sets;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.Operator;
import org.opensearch.index.reindex.DeleteByQueryRequest;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.search.SearchHit;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.core.xcontent.XContentBuilder;
import org.osid.shared.SharedException;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.elasticsearch.BaseElasticSearchIndexBuilder;
import org.sakaiproject.search.elasticsearch.NoContentException;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolAccessFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolIteratorFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;

import lombok.extern.slf4j.Slf4j;
import net.htmlparser.jericho.Source;

import lombok.Setter;

@Slf4j
public class QuestionElasticSearchIndexBuilder extends BaseElasticSearchIndexBuilder {

    QuestionPoolService questionPoolService  = new QuestionPoolService();
    @Setter private ItemContentProducer itemContentProducer;
    @Setter private PublishedItemContentProducer publishedItemContentProducer;
    @Setter private SiteService siteService;
    protected String[] searchResultFieldNames;

    protected static final String ADD_RESOURCE_VALIDATION_KEY_ITEM = "questionId";
    protected static final String DELETE_RESOURCE_KEY_ITEM = "questionId";
    private static final String RESOURCE_PREFIX_ASSESSMENT = "/sam_assessment/";
    private static final String RESOURCE_PREFIX_PUBLISHED_ASSESSMENT = "/sam_publishedassessment/";
    private static final String RESOURCE_TOKEN_ASSESSMENT_ID = " assessmentId=";
    private static final String RESOURCE_TOKEN_PUBLISHED_ASSESSMENT_ID = " publishedAssessmentId=";

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
    private final Set<String> pendingAssessmentDeletes = Collections.synchronizedSet(new HashSet<>());

    /**
     * Gives subclasses a chance to initialize configuration prior to reading/processing any
     * ES configs. May be important for setting up defaults, for example, or for ensuring
     * subclass-specific configs are in place before any background tasks are in place.
     * (Though the latter would be better factored into {@link #beforeBackgroundSchedulerInitialization()}
     */
    @Override
    protected void beforeElasticSearchConfigInitialization() {
        if (ArrayUtils.isEmpty(this.suggestionResultFieldNames)) {
            this.suggestionResultFieldNames = new String[] {};
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
        if ("true".equals(serverConfigurationService.getString("search.enable", "false"))) {
            registerEntityContentProducer(itemContentProducer);
            registerEntityContentProducer(publishedItemContentProducer);
        }
    }

    /**
     * Called after all ES config has been processed but before the background scheduler has been set up
     * and before any index startup ops have been invoked ({@link #initializeIndex()}. I.e. this is a
     * subclass's last chance to set up any configs on which background jobs and/or index maintenance
     * in general might depend.
     */
    @Override
    protected void beforeBackgroundSchedulerInitialization() {
    }

    @Override
    protected SearchRequest completeFindContentQueueRequest(SearchRequest searchRequest) {
        return searchRequest;
    }

    @Override
    protected DeleteRequest completeDeleteRequest(DeleteRequest deleteRequest, Map<String, Object> deleteParams) {
        return deleteRequest.routing((String)deleteParams.get(DELETE_RESOURCE_KEY_ITEM));
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
                log.error("problem queuing content indexing for site: " + siteId + " error: " + e.getMessage());
            }
        }

    }

    @Override
    protected void rebuildIndexImmediately(){
        // 1. ITEMS in assessments will be indexed for each site
        for (Site s : siteService.getSites(SiteService.SelectionType.ANY, null, null, null, SiteService.SortType.NONE, null)) {
            if (isSiteIndexable(s)) {
                rebuildSiteIndex(s.getId());
            }
        }

        // 2. Items in Question Pools
        questionPoolService.getAllPools().forEach(p -> rebuildQuestionPoolIndex(Long.toString(p.getQuestionPoolId())));
    }

    protected boolean isSiteIndexable(Site site) {
        //We don't need to check the user sites or special sites.
        return !(siteService.isSpecialSite(site.getId()) ||
                siteService.isUserSite(site.getId()));
    }


    protected void rebuildQuestionPoolIndex(String qpId)  {
        log.info("Rebuilding the index for QP: '" + qpId + "'");

        try {
            List qpItemsIds = questionPoolService.getAllItemsIds(Long.valueOf(qpId));

            enableAzgSecurityAdvisor();
            deleteAllDocumentForQuestionPool(qpId);

            long start = System.currentTimeMillis();
            int numberOfDocs = 0;

            BulkRequest bulkRequest = new BulkRequest();

            EntityContentProducer ecp = itemContentProducer;

            for (Object qpItemsId : qpItemsIds) {

                if (bulkRequest.numberOfActions() < bulkRequestSize) {

                    String reference = Long.toString((Long) qpItemsId);
                    try {
                        deleteDocument(ecp.getId(reference));
                        bulkRequest.add(prepareIndex(reference, ecp, false));
                        numberOfDocs++;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
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

            log.info("Queued " + numberOfDocs + " docs for indexing from question pool: " + qpId + " in " + (System.currentTimeMillis() - start) + " ms");

        } catch (Exception e) {
            log.error("An exception occurred while rebuilding the index of question pool '" + qpId + "'", e);
        } finally {
            disableAzgSecurityAdvisor();
        }
    }



    protected void rebuildSiteIndex(String siteId)  {
        log.info("Rebuilding the index for '" + siteId + "'");

        try {
            enableAzgSecurityAdvisor();
            deleteAllDocumentForSite(siteId);

            long start = System.currentTimeMillis();
            int numberOfDocs = 0;

            BulkRequest bulkRequest = new BulkRequest();

            Set<EntityContentProducer> questionProducers = Sets.newConcurrentHashSet();
            questionProducers.add(itemContentProducer);
            questionProducers.add(publishedItemContentProducer);

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
                                log.error(e.getMessage(), e);
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

            }

            log.info("Queued " + numberOfDocs + " docs for indexing from site: " + siteId + " in " + (System.currentTimeMillis() - start) + " ms");

        } catch (Exception e) {
            log.error("An exception occurred while rebuilding the index of '" + siteId + "'", e);
        } finally {
            disableAzgSecurityAdvisor();
        }
    }

    private void deleteAllDocumentForSite(String siteId) {
        log.debug("removing all documents from question index for site: " + siteId);
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName)
                .setQuery(termQuery("site", siteId));
        try {
            client.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Failed to remove all documents from question index for site: " + siteId + ", " + e);
        }
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
        log.debug("removing all documents from question index for questionPool: " + qpId);
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName)
                .setQuery(termQuery("questionPoolId", qpId));
            try {
                client.deleteByQuery(request, RequestOptions.DEFAULT);
            } catch (IOException e) {
                log.error("Failed to remove all documents from question index for questionPool: " + qpId + ", " + e);
            }
        }

    protected void deleteAllDocumentForAssessment(String assessmentId, String subtype) {
        log.debug("removing all documents from question index for assessment: {} and subtype: {}", assessmentId, subtype);
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName)
                .setQuery(boolQuery()
                        .must(termQuery("assessmentId", assessmentId))
                        .must(termQuery("subtype", subtype)));
        try {
            client.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Failed to remove all documents from question index for assessment: " + assessmentId + ", " + e);
        }
    }

    protected void scheduleDeleteAllDocumentForAssessment(String assessmentId, String subtype) {
        final String deleteKey = subtype + ":" + assessmentId;
        if (!pendingAssessmentDeletes.add(deleteKey)) {
            return;
        }

        if (backgroundScheduler == null) {
            try {
                deleteAllDocumentForAssessment(assessmentId, subtype);
            } finally {
                pendingAssessmentDeletes.remove(deleteKey);
            }
            return;
        }

        backgroundScheduler.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    deleteAllDocumentForAssessment(assessmentId, subtype);
                } catch (Throwable t) {
                    log.error("Failed to remove all documents from question index for assessment: {} and subtype: {}", assessmentId, subtype, t);
                } finally {
                    pendingAssessmentDeletes.remove(deleteKey);
                }
            }
        }, 0);
    }

    protected void deleteDocument(String id) {
        final Map<String, Object> params = new HashMap<>();
        params.put("questionId", id);
        deleteDocumentWithParams(params);
    }


    @Override
    protected IndexRequest completeIndexRequest(IndexRequest indexRequest, String resourceName, EntityContentProducer ecp, boolean includeContent) {
        return indexRequest.routing(null);
    }


    @Override
    public EntityContentProducer newEntityContentProducer(String ref) {
        //We will select between the items and the publishedItems
        if (ref.contains("/sam_item/") || ref.contains(" itemId=")){
                log.debug("Matched content producer ItemContentProducer for reference " + ref + " in index builder "
                    + getName());
            return itemContentProducer;
        }
        if (ref.contains("/sam_publisheditem/") || ref.contains(" publishedItemId=")){
            log.debug("Matched content producer PublishedItemContentProducer for reference " + ref + " in index builder "
                    + getName());
            return publishedItemContentProducer;
        }
        log.debug("Failed to match any content producer for reference " + ref + " in index builder " + getName());
        return null;
    }

    @Override
    public EntityContentProducer newEntityContentProducer(Event event) {
        ItemContentProducer icp = itemContentProducer;
        Set<String> triggerIcp = icp.getTriggerFunctions();
        if (triggerIcp.contains(event.getEvent())){
            log.debug("we have a ItemContentProducer for the event " + event + " in index builder " + getName());
            return itemContentProducer;
        }
        PublishedItemContentProducer picp = publishedItemContentProducer;
        Set<String> triggerPicp = picp.getTriggerFunctions();
        if (triggerPicp.contains(event.getEvent())){
            log.debug("we have a PublishedContentProducer for the event " + event + " in index builder " + getName());
            return publishedItemContentProducer;
        }
        log.debug("Failed to match any content producer for event " + event + " in index builder " + getName());
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

                Map<String,Object> allFields = icp.getAllFields(resourceName);

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

                Map<String,Object> allFieldsPub = picp.getAllFields(resourceName);

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
            throws IllegalArgumentException, IllegalStateException {
    }

    @Override
    protected DeleteRequest prepareDeleteDocument(Map<String, Object> deleteParams) {
        DeleteRequest request = newDeleteRequest(deleteParams);
        request = completeDeleteRequest(request, deleteParams);
        return request;
    }

    private DeleteRequest newDeleteRequest(Map<String, Object> deleteParams) {
        return new DeleteRequest(indexName, (String) deleteParams.get(DELETE_RESOURCE_KEY_ITEM));
    }

    @Override
    protected Map<String, Object> extractDeleteDocumentParams(Map<String, Object> validationContext) {
        final Map<String,Object> params = new HashMap<>();
        params.put(DELETE_RESOURCE_KEY_ITEM, validationContext.get(ADD_RESOURCE_VALIDATION_KEY_RESOURCE_NAME));
        return params;
    }

    @Override
    protected Map<String, Object> extractDeleteDocumentParams(NoContentException noContentException) {
        Map<String,Object> params = new HashMap<>();
        params.put(DELETE_RESOURCE_KEY_ITEM, noContentException.getId());
        return params;
    }

    @Override
    protected Map<String, Object> extractDeleteDocumentParams(SearchHit searchHit) {
        String id = getFieldFromSearchHit("questionId", searchHit);
        final Map<String, Object> params = new HashMap<>();
        params.put(DELETE_RESOURCE_KEY_ITEM, id);
        return params;
    }

    @Override
    protected DeleteResponse deleteDocumentWithRequest(DeleteRequest deleteRequest) throws IOException {
        return client.delete(deleteRequest, RequestOptions.DEFAULT);
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
        if (resourceName.indexOf(RESOURCE_TOKEN_ASSESSMENT_ID)!=-1){
            resourceName = RESOURCE_PREFIX_ASSESSMENT + resourceName.substring(resourceName.indexOf(RESOURCE_TOKEN_ASSESSMENT_ID) + RESOURCE_TOKEN_ASSESSMENT_ID.length());
        }
        if (resourceName.indexOf(RESOURCE_TOKEN_PUBLISHED_ASSESSMENT_ID)!=-1){
            resourceName = RESOURCE_PREFIX_PUBLISHED_ASSESSMENT + resourceName.substring(resourceName.indexOf(RESOURCE_TOKEN_PUBLISHED_ASSESSMENT_ID) + RESOURCE_TOKEN_PUBLISHED_ASSESSMENT_ID.length());
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

        if (resourceName.startsWith(RESOURCE_PREFIX_ASSESSMENT) || resourceName.startsWith(RESOURCE_PREFIX_PUBLISHED_ASSESSMENT)) {
            id1 = resourceName;
        } else if (event.getResource().indexOf(".delete@/") != -1){
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
                if (resourceName.startsWith(RESOURCE_PREFIX_ASSESSMENT)) {
                    scheduleDeleteAllDocumentForAssessment(resourceName.substring(RESOURCE_PREFIX_ASSESSMENT.length()), "item");
                } else if (resourceName.startsWith(RESOURCE_PREFIX_PUBLISHED_ASSESSMENT)) {
                    scheduleDeleteAllDocumentForAssessment(resourceName.substring(RESOURCE_PREFIX_PUBLISHED_ASSESSMENT.length()), "publisheditem");
                } else {
                    deleteDocumentWithParams(extractDeleteDocumentParams(validationContext));
                }
                break;
            default:
                // Should never happen if validation process was implemented correctly
                throw new UnsupportedOperationException(indexAction + " is not supported in index builder " + getName());
        }
    }

    @Override
    protected void deleteDocumentWithParams(Map<String, Object> deleteParams) {
        final DeleteRequest request = prepareDeleteDocument(deleteParams);
        try {
            final DeleteResponse deleteResponse = deleteDocumentWithRequest(request);
            if (log.isDebugEnabled()) {
                if (RestStatus.NOT_FOUND == deleteResponse.status()) {
                    log.debug("Could not delete doc with by id: "
                            + deleteParams.get(DELETE_RESOURCE_KEY_ITEM)
                            + " in index builder ["
                            + getName()
                            + "] because the document wasn't found");
                } else {
                    log.debug("ES deleted a doc with id: " + deleteResponse.getId() + " in index builder [" + getName() + "]");
                }
            }
        } catch (IOException e) {
            log.error("Failed to delete a doc in index builder [" + getName() + "], " + e);
        }
    }

    @Override
    protected void processContentQueue() {
        startTime = System.currentTimeMillis();

        // If there are a lot of docs queued up this could take awhile we don't want
        // to eat up all the CPU cycles.
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);

        if (getPendingDocuments() == 0) {
            log.trace("No pending docs for index builder [" + getName() + "]");
            return;
        }

        SearchResponse response = findContentQueue();

        SearchHit[] hits = response.getHits().getHits();

        List<NoContentException> noContentExceptions = new ArrayList();
        log.trace(getPendingDocuments() + " pending docs for index builder [" + getName() + "]");

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
            log.trace("Finished indexing " + hits.length + " docs in " +
                    ((lastLoad - startTime)) + " ms for index builder " + getName());
        }

    }

    @Override
    protected SearchRequest addFindContentQueueRequestParams(SearchRequest searchRequest) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(matchAllQuery())
                .postFilter(boolQuery().should(termQuery(SearchService.FIELD_INDEXED, false)).should(boolQuery().mustNot(existsQuery(SearchService.FIELD_INDEXED))))
                .size(contentIndexBatchSize)
                .storedFields(Arrays.asList("questionId", "subtype"));
        return searchRequest
                .indices(indexName)
                .source(searchSourceBuilder);
    }


    @Override
    protected void processContentQueueEntry(SearchHit hit, BulkRequest bulkRequest) throws NoContentException {

        String reference = getFieldFromSearchHit("questionId", hit);
        String subtype = getFieldFromSearchHit("subtype", hit);
        EntityContentProducer ecp;
        if (subtype.equals("item")) {
            ecp = itemContentProducer;
        }else{
            ecp = publishedItemContentProducer;
        }

        if (ecp != null) {
            //updating was causing issues without a _source, so doing delete and re-add
            try {
                deleteDocument(hit);
                bulkRequest.add(prepareIndex(reference, ecp, true));
            } catch (NoContentException e) {
                throw e;
            } catch (Exception e) {
                log.error("Failed to process content queue entry with id [" + hit.getId() + "] in index builder ["
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
            IndexRequest request = prepareIndex(resourceName, ecp, includeContent);
            client.index(request, RequestOptions.DEFAULT);
        } catch (NoContentException e) {
            throw e;
        } catch (Throwable t) {
            log.error("Error: trying to register resource " + resourceName + " in index builder: " + getName(), t);
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
            log.error("Problem updating content indexing in index builder: " + getName()
                    + " for entity: " + resourceName, e);
        }
    }



    @Override
    public SearchResponse search(String searchTerms, List<String> references, List<String> siteIds, List<String> toolIds, int start, int end) {
       return search(searchTerms,references, siteIds, toolIds, start, end, new HashMap<>());
    }

    /**
     * This is a new search that accepts additionalSearchInformation. We need it for our complex question searches.
     * We have duplicated the methods that need this parameter, like prepareSearchRequest
     */
    public SearchResponse search(String searchTerms, List<String> references, List<String> siteIds, List<String> toolIds, int start, int end, Map<String,String> additionalSearchInformation) {
        SearchRequest searchRequest = prepareSearchRequest(searchTerms, references, siteIds, toolIds, start, end, additionalSearchInformation);
        log.debug("Search request from index builder [{}]: {}", getName(), searchRequest.toString());
        ValidateQueryRequest validateQueryRequest = new ValidateQueryRequest(indexName);
        validateQueryRequest.query(searchRequest.source().query());
        validateQueryRequest.explain(true);

        try {
            ValidateQueryResponse validateQueryResponse = client.indices().validateQuery(validateQueryRequest, RequestOptions.DEFAULT);
            if (validateQueryResponse.isValid()) {
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

                log.debug("Search request from index builder [{}] took: {}", getName(), searchResponse.getTook().millis());

                eventTrackingService.post(
                        eventTrackingService.newEvent(
                                SearchService.EVENT_SEARCH,
                                SearchService.EVENT_SEARCH_REF + searchRequest.source().query().toString(),
                                true,
                                NotificationService.PREF_IMMEDIATE));

                return searchResponse;
            } else {
                log.warn("Query was not valid for index {} index builder [{}]", indexName, getName());
            }
        } catch (IOException ioe) {
            log.warn("Failure search request from index builder [{}], {}", getName(), ioe.toString());
        }
        return null;
    }

    @Override
    protected SearchRequest prepareSearchRequest(String searchTerms, List<String> references, List<String> siteIds, List<String> toolIds, int start, int end) {
        return prepareSearchRequest(searchTerms,references, siteIds, toolIds, start, end, new HashMap<>());
    }

    protected SearchRequest prepareSearchRequest(String searchTerms, List<String> references, List<String> siteIds, List<String> toolIds, int start, int end, Map<String,String> additionalSearchInformation) {
        SearchRequest searchRequest = newSearchRequestAndQueryBuilders(searchTerms, references, siteIds);
        addSearchCoreParams(searchRequest);
        addSearchQuery(searchRequest, searchTerms, references, siteIds, toolIds, additionalSearchInformation);
        addSearchResultFields(searchRequest);
        addSearchPagination(searchRequest, start, end);
        addSearchFacetting(searchRequest);
        completeSearchRequestBuilders(searchRequest, searchTerms, references, siteIds);
        return searchRequest;
    }

    protected SearchRequest newSearchRequestAndQueryBuilders(String searchTerms, List<String> references, List<String> siteIds) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source().query(boolQuery());
        return searchRequest;
    }

    @Override
    protected void addSearchCoreParams(SearchRequest searchRequest) {
        searchRequest.searchType(SearchType.QUERY_THEN_FETCH);
    }

    @Override
    protected void addSearchQuery(SearchRequest searchRequest, String searchTerms, List<String> references, List<String> siteIds, List<String> toolIds) {
        addSearchQuery(searchRequest, searchTerms, references, siteIds, toolIds, new HashMap<>());
    }

    protected void addSearchQuery(SearchRequest searchRequest, String searchTerms, List<String> references, List<String> siteIds, List<String> toolIds, Map<String,String> additionalSearchInformation ) {
        addSearchTerms(searchRequest, searchTerms, additionalSearchInformation);
        addSearchReferences(searchRequest, references);
        addSearchSiteIds(searchRequest, siteIds);
    }

    @Override
    protected void addSearchTerms(SearchRequest searchRequest, String searchTerms) {
        addSearchTerms(searchRequest, searchTerms, new HashMap<>());
    }

    /**
     * Here we create our specific search query with the parameters that are sent in the additionalSearchInformation

     */
    protected void addSearchTerms(SearchRequest searchRequest, String searchTerms, Map<String, String> additionalSearchInformation) {
        // receive the text in the searchTerms
        // manage the TEXT query
        log.debug("Searching the searchterm: {}", searchTerms);
        // remove the html code because it can cause exceptions.
        Source parseSearchTerms = new Source(searchTerms);
        searchTerms = parseSearchTerms.getTextExtractor().toString();
        //and now let's remove especial chars
        String regex = "([+\\-!\\(\\){}\\[\\]^~*?:\\\\]|[&\\|]{2})";
        searchTerms = searchTerms.replaceAll(regex, "\\\\$1");
        log.debug("Searching the searchterms after escape them: {}", searchTerms);

        final BoolQueryBuilder query = (BoolQueryBuilder) searchRequest.source().query();
        if (StringUtils.isNotEmpty(searchTerms)) {
            if (additionalSearchInformation.containsKey("logic") && additionalSearchInformation.get("logic").equals("and")) {
                query.must(queryStringQuery(searchTerms).defaultField("contents").defaultOperator(Operator.AND));
            } else {
                query.must(queryStringQuery(searchTerms).defaultField("contents").defaultOperator(Operator.OR));
            }
        }

        final BoolQueryBuilder tagsQuery = boolQuery();
        final boolean logicalOr = "or".equals(additionalSearchInformation.get("logic")) ? true : false;

        // the tags are in the form of tag_1 / tag value, tag_2..., tag_n...
        additionalSearchInformation.keySet().stream().filter(k -> k.startsWith("tag_")).forEach(k -> {
            String tag = additionalSearchInformation.get(k);
            log.debug("search tag: {}", tag);
            if (logicalOr) {
                tagsQuery.should(termQuery("tags", tag));
            } else {
                tagsQuery.must(termQuery("tags", tag));
            }
        });

        if (tagsQuery.hasClauses()) {
            if (tagsQuery.should().size() > 1) {
                tagsQuery.minimumShouldMatch(1);
            }
            log.debug("tags sub query: {}", tagsQuery);
            query.must(tagsQuery);
        }

        if (additionalSearchInformation.containsKey("subtype")) {
            log.debug("We will search this subtype: {}", additionalSearchInformation.get("subtype"));
            query.must(matchQuery("subtype", additionalSearchInformation.get("subtype")));
        }

        if (additionalSearchInformation.containsKey("hash")) {
            log.debug("We will search this hash: {}", additionalSearchInformation.get("hash"));
            query.must(matchQuery("hash", additionalSearchInformation.get("hash")));
        }

        // the scope of the search in the advancedSearchInformation Map...
        // scope "all" = no filter
        // scope "owns" or no scope = all their sites as instructor and qpools with permissions (That's the default value)
        // scope "custom" = filter all the site_1, site_2, site_3 and qp_1,qp2,qp3....

        if (additionalSearchInformation.containsKey("scope")) {
            if (additionalSearchInformation.get("scope").equals("all")) {
                // do nothing here
                log.debug("we won't filter the results by scope");
            } else if (additionalSearchInformation.get("scope").equals("custom")) {
                log.debug("We have a custom scope request");
                Set<String> keysForACL = additionalSearchInformation.keySet();
                Iterator<String> keysForACLIterator = keysForACL.iterator();
                while (keysForACLIterator.hasNext()) {
                    String key = keysForACLIterator.next();
                    List<String> siteIds = new ArrayList<>();
                    List<String> questionPoolsIds = new ArrayList<>();

                    if (key.startsWith("site_")) {
                        String siteId = additionalSearchInformation.get(key);
                        log.debug("We have this siteId to add: {}", siteId);
                        siteIds.add(siteId);
                        query.should(termsQuery("site", siteIds.toArray(new String[siteIds.size()])));
                    }

                    if (key.startsWith("qp_")) {
                        String qpId = additionalSearchInformation.get(key);
                        log.debug("We have this question Pool to add: {}", qpId);
                        questionPoolsIds.add(qpId);
                        query.should(termsQuery("questionPoolId", questionPoolsIds.toArray(new String[questionPoolsIds.size()])));
                        query.minimumShouldMatch(1);
                    }
                }
            } else {
                if (!(securityService.isSuperUser())) {
                    log.debug("No superuser.Searching based in permissions");
                    List<String> siteIds = getAllSitesUserHasQuestionAuthorPermissions();
                    List<String> questionPoolsIds = getAllUserQuestionPools();
                    query.should(termsQuery("site", siteIds.toArray(new String[siteIds.size()])));
                    query.should(termsQuery("questionPoolId", questionPoolsIds.toArray(new String[questionPoolsIds.size()])));
                    query.minimumShouldMatch(1);
                } else {
                    log.debug("Superuser, so no filtering");
                }
            }

            // aggregations will have a key "group" with the field on it.
            if (additionalSearchInformation.containsKey("group")) {
                log.debug("We are going to group results by: {}", additionalSearchInformation.get("group"));
                // need aggregation, don't need normal results.
                searchRequest.source().size(0);
                addSearchAggregation(searchRequest, additionalSearchInformation.get("group"));
            }
        }
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

        QuestionPoolIteratorFacade qpif = questionPoolService.getAllPoolsWithAccess(AgentFacade.getAgentString(), QuestionPoolAccessFacade.READ_ONLY);
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
    protected void addSearchReferences(SearchRequest searchRequest, List<String> references) {
        if (references.size() > 0){
            ((BoolQueryBuilder) searchRequest.source().query()).must(termsQuery(SearchService.FIELD_REFERENCE, references.toArray(new String[0])));
        }
    }

    @Override
    protected void addSearchSiteIds(SearchRequest searchRequest, List<String> siteIds) {
    }

    @Override
    protected void addSearchResultFields(SearchRequest searchRequest) {
        if (!ArrayUtils.isEmpty(searchResultFieldNames)) {
            searchRequest.source().storedFields(Arrays.asList(searchResultFieldNames));
        }
    }

    @Override
    protected void addSearchPagination(SearchRequest searchRequest, int start, int end) {
        searchRequest.source().from(start).size(end - start);
    }

    @Override
    protected void addSearchFacetting(SearchRequest searchRequest) {
        if (useFacetting) {
            TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(facetName).field("hash").size(facetTermSize);
            searchRequest.source().aggregation(termsAggregationBuilder);
        }
    }

    protected void addSearchAggregation(SearchRequest searchRequest, String field) {
        if (useAggregation) {
            List<String> fields = Arrays.asList("assessmentId", "site", "questionPoolId", "typeId", "tags", "qText");
            searchRequest.source().aggregation(
                    AggregationBuilders.terms(AGGREGATION_NAME).field(field).size(serverConfigurationService.getInt("samigo.search.maxResults",50))
                            .subAggregation(AggregationBuilders.topHits(AGGREGATION_TOP_HITS).size(1).storedFields(fields)));
                            // .subAggregation(AggregationBuilders.topHits(AGGREGATION_TOP_HITS).size(1).storedField("origin")));
        }
    }

    @Override
    protected void completeSearchRequestBuilders(SearchRequest searchRequest, String searchTerms, List<String> references, List<String> siteIds){
        log.debug("This is the search query: {}", searchRequest.source().query().toString());
    }

    @Override
    protected void completeSearchSuggestionsRequestBuilders(SearchRequest searchRequest, String searchString, String currentSite, boolean allMySites) {
    }

    @Override
    protected void addSearchSuggestionsTerms(SearchRequest searchRequest, String searchString) {
    }

    @Override
    protected void addSearchSuggestionsSites(SearchRequest searchRequest, String currentSite, boolean allMySites){
    }

    @Override
    public String getEventResourceFilter() {
        return "/";
    }

}
