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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of QuestionSearchService using ElasticSearch/OpenSearch.
 * This encapsulates all OpenSearch-specific logic for question searching.
 */
@Slf4j
public class QuestionSearchServiceImpl implements QuestionSearchService {

    @Setter
    private ElasticSearchService elasticSearchService;

    @Setter
    private TagService tagService;

    @Setter
    private SiteService siteService;

    @Setter
    private QuestionPoolService questionPoolService;

    @Setter
    private AssessmentService assessmentService;

    // Caches for titles to avoid repeated service calls
    private final Map<String, String> qpTitlesCache = new HashMap<>();
    private final Map<String, String> assessmentTitlesCache = new HashMap<>();
    private final Map<String, String> siteTitlesCache = new HashMap<>();

    @Override
    public List<QuestionSearchResult> searchByTags(String[] tags, boolean andLogic) {
        if (tags == null || tags.length == 0) {
            return new ArrayList<>();
        }

        Map<String, String> additionalSearchInformation = new HashMap<>();
        additionalSearchInformation.put("group", "hash");
        additionalSearchInformation.put("scope", "own");
        additionalSearchInformation.put("subtype", "item");
        additionalSearchInformation.put("logic", andLogic ? "and" : "or");

        // Add tag information
        int i = 1;
        for (String tagId : tags) {
            if (tagService.getTags().getForId(tagId).isPresent()) {
                Tag tag = tagService.getTags().getForId(tagId).get();
                String tagLabel = tag.getTagLabel();
                String tagCollectionName = tag.getCollectionName();
                additionalSearchInformation.put("tag_" + i, tagLabel + "(" + tagCollectionName + ")");
                i++;
            }
        }

        try {
            SearchResponse sr = elasticSearchService.searchResponse(
                "", null, null, 0, 0, "questions", additionalSearchInformation);

            return processSearchResponse(sr);

        } catch (InvalidSearchQueryException ex) {
            log.warn("Error searching questions by tags: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<QuestionSearchResult> searchByText(String text, boolean andLogic) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, String> additionalSearchInformation = new HashMap<>();
        additionalSearchInformation.put("group", "hash");
        additionalSearchInformation.put("scope", "own");
        additionalSearchInformation.put("subtype", "item");
        additionalSearchInformation.put("logic", andLogic ? "and" : "or");

        try {
            SearchResponse sr = elasticSearchService.searchResponse(
                text, null, null, 0, 0, "questions", additionalSearchInformation);

            return processSearchResponse(sr);

        } catch (InvalidSearchQueryException ex) {
            log.warn("Error searching questions by text: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean userOwnsQuestion(String questionId) {
        if (questionId == null || questionId.isEmpty()) {
            return false;
        }

        Map<String, String> additionalSearchInformation = new HashMap<>();
        additionalSearchInformation.put("scope", "own");
        additionalSearchInformation.put("subtype", "item");
        additionalSearchInformation.put("questionId", questionId);

        try {
            SearchResponse sr = elasticSearchService.searchResponse(
                "", null, null, 0, 1, "questions", additionalSearchInformation);

            return sr.getHits().getTotalHits().value >= 1;

        } catch (Exception ex) {
            log.warn("Error checking question ownership for {}: {}", questionId, ex.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getQuestionOrigins(String hash) {
        if (hash == null || hash.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> origins = new ArrayList<>();
        Map<String, String> additionalSearchInformation = new HashMap<>();
        additionalSearchInformation.put("scope", "own");
        additionalSearchInformation.put("subtype", "item");
        additionalSearchInformation.put("hash", hash);

        try {
            SearchResponse sr = elasticSearchService.searchResponse(
                "", null, null, 0, 1000, "questions", additionalSearchInformation);

            for (SearchHit hit : sr.getHits()) {
                String origin = extractOrigin(hit);
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
     * Process the OpenSearch response and convert to QuestionSearchResult list.
     */
    private List<QuestionSearchResult> processSearchResponse(SearchResponse sr) {
        List<QuestionSearchResult> results = new ArrayList<>();

        Terms dedup = sr.getAggregations().get("dedup");
        if (dedup == null) {
            return results;
        }

        for (Terms.Bucket entry : dedup.getBuckets()) {
            TopHits topHits = entry.getAggregations().get("dedup_docs");
            if (topHits == null) {
                continue;
            }

            for (SearchHit hit : topHits.getHits().getHits()) {
                try {
                    String id = hit.getId();
                    String typeId = hit.field("typeId").getValue();
                    String qText = hit.field("qText").getValue();
                    Set<String> tags = extractTags(hit.field("tags").getValues());
                    String origin = extractOrigin(hit);

                    results.add(new QuestionSearchResult(id, typeId, qText, tags, origin));

                } catch (Exception ex) {
                    log.debug("Error processing search hit {}: {}", hit.getId(), ex.getMessage());
                }
            }
        }

        return results;
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
     * Extract the origin description from a search hit.
     * Returns the question pool name, or "Site : Assessment" format.
     */
    private String extractOrigin(SearchHit hit) {
        try {
            // Check for question pool
            if (hit.field("questionPoolId") != null && hit.field("questionPoolId").getValue() != null) {
                String qpId = hit.field("questionPoolId").getValue();

                if (qpTitlesCache.containsKey(qpId)) {
                    return qpTitlesCache.get(qpId);
                } else {
                    String qpTitle = questionPoolService.getPool(
                        Long.parseLong(qpId), AgentFacade.getAgentString()).getTitle();
                    qpTitlesCache.put(qpId, qpTitle);
                    return qpTitle;
                }
            }

            // Check for assessment
            if (hit.field("assessmentId") != null && hit.field("assessmentId").getValue() != null &&
                hit.field("site") != null && hit.field("site").getValue() != null) {

                String assessmentId = hit.field("assessmentId").getValue().toString();
                String siteId = hit.field("site").getValue().toString();

                String assessmentTitle;
                if (assessmentTitlesCache.containsKey(assessmentId)) {
                    assessmentTitle = assessmentTitlesCache.get(assessmentId);
                } else {
                    assessmentTitle = assessmentService.getAssessment(assessmentId).getTitle();
                    assessmentTitlesCache.put(assessmentId, assessmentTitle);
                }

                String siteTitle;
                if (siteTitlesCache.containsKey(siteId)) {
                    siteTitle = siteTitlesCache.get(siteId);
                } else {
                    siteTitle = siteService.getSite(siteId).getTitle();
                    siteTitlesCache.put(siteId, siteTitle);
                }

                return siteTitle + " : " + assessmentTitle;
            }

            return "";

        } catch (Exception ex) {
            // Question may be orphaned without an assessment or question pool
            log.debug("Could not extract origin for question {}: {}", hit.getId(), ex.getMessage());
            return "";
        }
    }
}
