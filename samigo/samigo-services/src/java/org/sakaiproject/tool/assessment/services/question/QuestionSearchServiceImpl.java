/**********************************************************************************
 * Copyright (c) 2025 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.services.question;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opensearch.action.search.SearchResponse;
import org.opensearch.search.SearchHit;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.metrics.TopHits;
import org.sakaiproject.search.api.InvalidSearchQueryException;
import org.sakaiproject.search.elasticsearch.ElasticSearchService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of QuestionSearchService using ElasticSearch/OpenSearch.
 * This encapsulates all OpenSearch-specific logic for question searching.
 *
 * Note: This service is stateless. Title caching should be done at the caller level
 * (e.g., in a session-scoped bean) to avoid memory leaks in this singleton service.
 */
@Slf4j
public class QuestionSearchServiceImpl implements QuestionSearchService {

    // Constants for index and aggregation names
    private static final String INDEX_NAME = "questions";
    private static final String AGGREGATION_DEDUP = "dedup";
    private static final String AGGREGATION_DEDUP_DOCS = "dedup_docs";

    // Constants for search parameters
    private static final String PARAM_GROUP = "group";
    private static final String PARAM_SCOPE = "scope";
    private static final String PARAM_SUBTYPE = "subtype";
    private static final String PARAM_LOGIC = "logic";
    private static final String PARAM_HASH = "hash";
    private static final String PARAM_QUESTION_ID = "questionId";

    // Constants for field names
    private static final String FIELD_TYPE_ID = "typeId";
    private static final String FIELD_QTEXT = "qText";
    private static final String FIELD_TAGS = "tags";
    private static final String FIELD_QUESTION_POOL_ID = "questionPoolId";
    private static final String FIELD_ASSESSMENT_ID = "assessmentId";
    private static final String FIELD_SITE = "site";

    // Reference prefix for samigo items
    private static final String SAM_ITEM_PREFIX = "/sam_item/";

    @Setter
    private ElasticSearchService elasticSearchService;

    @Setter
    private SiteService siteService;

    @Setter
    private QuestionPoolService questionPoolService;

    @Setter
    private AssessmentService assessmentService;

    @Override
    public List<QuestionSearchResult> searchByTags(List<String> tagLabels, boolean andLogic)
            throws QuestionSearchException {
        if (tagLabels == null || tagLabels.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, String> searchParams = createBaseSearchParams(andLogic);

        // Add tag information (labels already resolved by caller)
        int i = 1;
        for (String tagLabel : tagLabels) {
            searchParams.put("tag_" + i, tagLabel);
            i++;
        }

        try {
            SearchResponse sr = elasticSearchService.searchResponse(
                "", null, null, 0, 0, INDEX_NAME, searchParams);
            return processSearchResponse(sr);
        } catch (InvalidSearchQueryException ex) {
            log.warn("Error searching questions by tags: {}", ex.getMessage());
            throw new QuestionSearchException("Failed to search questions by tags", ex);
        }
    }

    @Override
    public List<QuestionSearchResult> searchByText(String text, boolean andLogic)
            throws QuestionSearchException {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, String> searchParams = createBaseSearchParams(andLogic);

        try {
            SearchResponse sr = elasticSearchService.searchResponse(
                text, null, null, 0, 0, INDEX_NAME, searchParams);
            return processSearchResponse(sr);
        } catch (InvalidSearchQueryException ex) {
            log.warn("Error searching questions by text: {}", ex.getMessage());
            throw new QuestionSearchException("Failed to search questions by text", ex);
        }
    }

    @Override
    public boolean userOwnsQuestion(String questionId) {
        if (questionId == null || questionId.isEmpty()) {
            return false;
        }

        Map<String, String> searchParams = new LinkedHashMap<>();
        searchParams.put(PARAM_SCOPE, "own");
        searchParams.put(PARAM_SUBTYPE, "item");
        searchParams.put(PARAM_QUESTION_ID, questionId);

        try {
            SearchResponse sr = elasticSearchService.searchResponse(
                "", null, null, 0, 1, INDEX_NAME, searchParams);
            return sr.getHits().getTotalHits().value >= 1;
        } catch (Exception ex) {
            log.warn("Error checking question ownership for {}: {}", questionId, ex.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getQuestionOrigins(String hash, Map<String, String> titleCache) {
        if (hash == null || hash.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> origins = new ArrayList<>();
        Map<String, String> searchParams = new LinkedHashMap<>();
        searchParams.put(PARAM_SCOPE, "own");
        searchParams.put(PARAM_SUBTYPE, "item");
        searchParams.put(PARAM_HASH, hash);

        try {
            SearchResponse sr = elasticSearchService.searchResponse(
                "", null, null, 0, 1000, INDEX_NAME, searchParams);

            for (SearchHit hit : sr.getHits()) {
                String origin = extractOrigin(hit, titleCache);
                if (!origin.isEmpty()) {
                    origins.add(origin);
                }
            }
            return origins;
        } catch (Exception ex) {
            log.warn("Error getting question origins for hash {}: {}", hash, ex.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Create base search parameters common to tag and text searches.
     */
    private Map<String, String> createBaseSearchParams(boolean andLogic) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put(PARAM_GROUP, PARAM_HASH);
        params.put(PARAM_SCOPE, "own");
        params.put(PARAM_SUBTYPE, "item");
        params.put(PARAM_LOGIC, andLogic ? "and" : "or");
        return params;
    }

    /**
     * Process the OpenSearch response and convert to QuestionSearchResult list.
     * Only includes results with the /sam_item/ prefix.
     */
    private List<QuestionSearchResult> processSearchResponse(SearchResponse sr) {
        List<QuestionSearchResult> results = new ArrayList<>();

        Terms dedup = sr.getAggregations().get(AGGREGATION_DEDUP);
        if (dedup == null) {
            return results;
        }

        for (Terms.Bucket entry : dedup.getBuckets()) {
            TopHits topHits = entry.getAggregations().get(AGGREGATION_DEDUP_DOCS);
            if (topHits == null) {
                continue;
            }

            for (SearchHit hit : topHits.getHits().getHits()) {
                try {
                    String id = hit.getId();

                    // Filter: only include /sam_item/ references
                    if (!id.startsWith(SAM_ITEM_PREFIX)) {
                        continue;
                    }

                    // Extract the actual item ID (remove prefix)
                    String itemId = id.substring(SAM_ITEM_PREFIX.length());

                    String typeId = hit.field(FIELD_TYPE_ID).getValue();
                    String qText = hit.field(FIELD_QTEXT).getValue();
                    Set<String> tags = extractTags(hit.field(FIELD_TAGS).getValues());

                    // Pass null for cache - origin will be resolved by caller if needed
                    String questionPoolId = extractFieldValue(hit, FIELD_QUESTION_POOL_ID);
                    String assessmentId = extractFieldValue(hit, FIELD_ASSESSMENT_ID);
                    String siteId = extractFieldValue(hit, FIELD_SITE);

                    results.add(new QuestionSearchResult(itemId, typeId, qText, tags,
                        questionPoolId, assessmentId, siteId));

                } catch (Exception ex) {
                    log.debug("Error processing search hit {}: {}", hit.getId(), ex.getMessage());
                }
            }
        }

        return results;
    }

    /**
     * Safely extract a field value from a search hit.
     */
    private String extractFieldValue(SearchHit hit, String fieldName) {
        if (hit.field(fieldName) != null && hit.field(fieldName).getValue() != null) {
            return hit.field(fieldName).getValue().toString();
        }
        return null;
    }

    /**
     * Extract tags from the search hit field values.
     */
    private Set<String> extractTags(List<Object> tagValues) {
        Set<String> tagSet = new HashSet<>();
        if (tagValues != null) {
            Iterator<Object> iterator = tagValues.iterator();
            while (iterator.hasNext()) {
                tagSet.add(iterator.next().toString());
            }
        }
        return tagSet;
    }

    /**
     * Extract the origin description from a search hit using provided cache.
     * Returns the question pool name, or "Site : Assessment" format.
     */
    private String extractOrigin(SearchHit hit, Map<String, String> titleCache) {
        try {
            String qpId = extractFieldValue(hit, FIELD_QUESTION_POOL_ID);
            if (qpId != null) {
                return resolveQuestionPoolTitle(qpId, titleCache);
            }

            String assessmentId = extractFieldValue(hit, FIELD_ASSESSMENT_ID);
            String siteId = extractFieldValue(hit, FIELD_SITE);

            if (assessmentId != null && siteId != null) {
                String siteTitle = resolveSiteTitle(siteId, titleCache);
                String assessmentTitle = resolveAssessmentTitle(assessmentId, titleCache);
                return siteTitle + " : " + assessmentTitle;
            }

            return "";

        } catch (Exception ex) {
            log.debug("Could not extract origin for question {}: {}", hit.getId(), ex.getMessage());
            return "";
        }
    }

    /**
     * Resolve question pool title, using cache if available.
     */
    private String resolveQuestionPoolTitle(String qpId, Map<String, String> cache) {
        String cacheKey = "qp:" + qpId;
        if (cache != null && cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        String title = questionPoolService.getPool(
            Long.parseLong(qpId), AgentFacade.getAgentString()).getTitle();
        if (cache != null) {
            cache.put(cacheKey, title);
        }
        return title;
    }

    /**
     * Resolve site title, using cache if available.
     */
    private String resolveSiteTitle(String siteId, Map<String, String> cache) throws Exception {
        String cacheKey = "site:" + siteId;
        if (cache != null && cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        String title = siteService.getSite(siteId).getTitle();
        if (cache != null) {
            cache.put(cacheKey, title);
        }
        return title;
    }

    /**
     * Resolve assessment title, using cache if available.
     */
    private String resolveAssessmentTitle(String assessmentId, Map<String, String> cache) {
        String cacheKey = "assessment:" + assessmentId;
        if (cache != null && cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        String title = assessmentService.getAssessment(assessmentId).getTitle();
        if (cache != null) {
            cache.put(cacheKey, title);
        }
        return title;
    }
}
