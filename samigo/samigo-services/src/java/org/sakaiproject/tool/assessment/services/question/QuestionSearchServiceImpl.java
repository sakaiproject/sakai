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
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuestionSearchServiceImpl implements QuestionSearchService {

    @Setter
    private ElasticSearchService elasticSearchService;

    @Setter
    private SiteService siteService;

    @Setter
    private QuestionPoolService questionPoolService;

    @Setter
    private AssessmentService assessmentService;

    @Override
    public List<QuestionSearchResult> searchByTags(List<String> tagLabels, boolean andLogic) {
        if (tagLabels == null || tagLabels.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("group", "hash");
        searchParams.put("scope", "own");
        searchParams.put("subtype", "item");
        searchParams.put("logic", andLogic ? "and" : "or");

        int i = 1;
        for (String tagLabel : tagLabels) {
            searchParams.put("tag_" + i, tagLabel);
            i++;
        }

        try {
            SearchResponse sr = elasticSearchService.searchResponse(
                "", null, null, 0, 0, "questions", searchParams);
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

        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("group", "hash");
        searchParams.put("scope", "own");
        searchParams.put("subtype", "item");
        searchParams.put("logic", andLogic ? "and" : "or");

        try {
            SearchResponse sr = elasticSearchService.searchResponse(
                text, null, null, 0, 0, "questions", searchParams);
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

        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("scope", "own");
        searchParams.put("subtype", "item");
        searchParams.put("questionId", questionId);

        try {
            SearchResponse sr = elasticSearchService.searchResponse(
                "", null, null, 0, 1, "questions", searchParams);
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
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("scope", "own");
        searchParams.put("subtype", "item");
        searchParams.put("hash", hash);

        try {
            SearchResponse sr = elasticSearchService.searchResponse(
                "", null, null, 0, 1000, "questions", searchParams);

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
                    if (!id.startsWith("/sam_item/")) {
                        continue;
                    }

                    String itemId = id.substring(10);
                    String typeId = hit.field("typeId").getValue();
                    String qText = hit.field("qText").getValue();

                    Set<String> tags = new HashSet<>();
                    List<Object> tagValues = hit.field("tags").getValues();
                    if (tagValues != null) {
                        for (Object tagValue : tagValues) {
                            tags.add(tagValue.toString());
                        }
                    }

                    String questionPoolId = null;
                    if (hit.field("questionPoolId") != null && hit.field("questionPoolId").getValue() != null) {
                        questionPoolId = hit.field("questionPoolId").getValue().toString();
                    }

                    String assessmentId = null;
                    if (hit.field("assessmentId") != null && hit.field("assessmentId").getValue() != null) {
                        assessmentId = hit.field("assessmentId").getValue().toString();
                    }

                    String siteId = null;
                    if (hit.field("site") != null && hit.field("site").getValue() != null) {
                        siteId = hit.field("site").getValue().toString();
                    }

                    results.add(new QuestionSearchResult(itemId, typeId, qText, tags,
                        questionPoolId, assessmentId, siteId));

                } catch (Exception ex) {
                    log.debug("Error processing search hit {}: {}", hit.getId(), ex.getMessage());
                }
            }
        }

        return results;
    }

    private String extractOrigin(SearchHit hit, Map<String, String> titleCache) {
        try {
            // Check question pool first
            if (hit.field("questionPoolId") != null && hit.field("questionPoolId").getValue() != null) {
                String qpId = hit.field("questionPoolId").getValue().toString();
                String cacheKey = "qp:" + qpId;

                if (titleCache != null && titleCache.containsKey(cacheKey)) {
                    return titleCache.get(cacheKey);
                }

                QuestionPoolFacade pool = questionPoolService.getPool(Long.parseLong(qpId), AgentFacade.getAgentString());
                if (pool == null) {
                    return "";
                }
                String title = pool.getTitle();
                if (titleCache != null) {
                    titleCache.put(cacheKey, title);
                }
                return title;
            }

            // Check assessment
            if (hit.field("assessmentId") != null && hit.field("assessmentId").getValue() != null &&
                hit.field("site") != null && hit.field("site").getValue() != null) {

                String assessmentId = hit.field("assessmentId").getValue().toString();
                String siteId = hit.field("site").getValue().toString();

                // Get site title
                String siteCacheKey = "site:" + siteId;
                String siteTitle;
                if (titleCache != null && titleCache.containsKey(siteCacheKey)) {
                    siteTitle = titleCache.get(siteCacheKey);
                } else {
                    try {
                        siteTitle = siteService.getSite(siteId).getTitle();
                        if (titleCache != null) {
                            titleCache.put(siteCacheKey, siteTitle);
                        }
                    } catch (Exception ex) {
                        return "";
                    }
                }

                // Get assessment title
                String assessmentCacheKey = "assessment:" + assessmentId;
                String assessmentTitle;
                if (titleCache != null && titleCache.containsKey(assessmentCacheKey)) {
                    assessmentTitle = titleCache.get(assessmentCacheKey);
                } else {
                    AssessmentFacade assessment = assessmentService.getAssessment(assessmentId);
                    if (assessment == null) {
                        return "";
                    }
                    assessmentTitle = assessment.getTitle();
                    if (titleCache != null) {
                        titleCache.put(assessmentCacheKey, assessmentTitle);
                    }
                }

                return siteTitle + " : " + assessmentTitle;
            }

            return "";

        } catch (Exception ex) {
            log.debug("Could not extract origin: {}", ex.getMessage());
            return "";
        }
    }
}
