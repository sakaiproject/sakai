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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opensearch.action.search.SearchResponse;
import org.opensearch.search.SearchHit;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.metrics.TopHits;
import org.sakaiproject.search.api.InvalidSearchQueryException;
import org.sakaiproject.search.api.SearchService;
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

    private static final String SAM_ITEM_PREFIX = "/sam_item/";

    @Setter
    private SearchService searchService;

    @Setter
    private SiteService siteService;

    @Setter
    private QuestionPoolService questionPoolService;

    @Setter
    private AssessmentService assessmentService;

    @Override
    public List<QuestionSearchResult> searchByTags(List<String> tagLabels, boolean andLogic) {
        if (tagLabels == null || tagLabels.isEmpty()) {
            return List.of();
        }

        Map<String, String> searchParams = buildBaseSearchParams(andLogic);
        int i = 1;
        for (String tagLabel : tagLabels) {
            searchParams.put("tag_" + i++, tagLabel);
        }

        try {
            SearchResponse sr = searchService.searchResponse("", null, null, 0, 0, "questions", searchParams);
            return processSearchResponse(sr);
        } catch (InvalidSearchQueryException ex) {
            log.warn("Error searching questions by tags: {}", ex.getMessage());
            return List.of();
        }
    }

    @Override
    public List<QuestionSearchResult> searchByText(String text, boolean andLogic) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        try {
            SearchResponse sr = searchService.searchResponse(text, null, null, 0, 0, "questions", buildBaseSearchParams(andLogic));
            return processSearchResponse(sr);
        } catch (InvalidSearchQueryException ex) {
            log.warn("Error searching questions by text: {}", ex.getMessage());
            return List.of();
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
            SearchResponse sr = searchService.searchResponse("", null, null, 0, 1, "questions", searchParams);
            return sr.getHits().getTotalHits().value >= 1;
        } catch (Exception ex) {
            log.warn("Error checking question ownership for {}: {}", questionId, ex.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getQuestionOrigins(String hash, Map<String, String> titleCache) {
        if (hash == null || hash.isEmpty()) {
            return List.of();
        }

        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("scope", "own");
        searchParams.put("subtype", "item");
        searchParams.put("hash", hash);

        try {
            SearchResponse sr = searchService.searchResponse("", null, null, 0, 1000, "questions", searchParams);
            List<String> origins = new ArrayList<>();
            for (SearchHit hit : sr.getHits()) {
                String origin = extractOrigin(hit, titleCache);
                if (!origin.isEmpty()) {
                    origins.add(origin);
                }
            }
            return origins;
        } catch (Exception ex) {
            log.warn("Error getting question origins for hash {}: {}", hash, ex.getMessage());
            return List.of();
        }
    }

    @Override
    public String getOriginDisplay(QuestionSearchResult result, Map<String, String> titleCache) {
        if (result == null) {
            return "";
        }
        try {
            if (result.isFromQuestionPool()) {
                String cacheKey = "qp:" + result.getQuestionPoolId();
                if (titleCache != null && titleCache.containsKey(cacheKey)) {
                    return titleCache.get(cacheKey);
                }
                QuestionPoolFacade pool = questionPoolService.getPool(
                        Long.parseLong(result.getQuestionPoolId()), AgentFacade.getAgentString());
                if (pool == null) {
                    return "";
                }
                String title = pool.getTitle();
                if (titleCache != null) {
                    titleCache.put(cacheKey, title);
                }
                return title;
            }

            if (result.isFromAssessment()) {
                String siteCacheKey = "site:" + result.getSiteId();
                String assessmentCacheKey = "assessment:" + result.getAssessmentId();

                String siteTitle;
                if (titleCache != null && titleCache.containsKey(siteCacheKey)) {
                    siteTitle = titleCache.get(siteCacheKey);
                } else {
                    try {
                        siteTitle = siteService.getSite(result.getSiteId()).getTitle();
                        if (titleCache != null) {
                            titleCache.put(siteCacheKey, siteTitle);
                        }
                    } catch (Exception ex) {
                        return "";
                    }
                }

                String assessmentTitle;
                if (titleCache != null && titleCache.containsKey(assessmentCacheKey)) {
                    assessmentTitle = titleCache.get(assessmentCacheKey);
                } else {
                    AssessmentFacade assessment = assessmentService.getAssessment(result.getAssessmentId());
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
        } catch (Exception ex) {
            log.debug("Could not resolve origin for question {}: {}", result.getId(), ex.getMessage());
        }
        return "";
    }

    private Map<String, String> buildBaseSearchParams(boolean andLogic) {
        Map<String, String> params = new HashMap<>();
        params.put("group", "hash");
        params.put("scope", "own");
        params.put("subtype", "item");
        params.put("logic", andLogic ? "and" : "or");
        return params;
    }

    private List<QuestionSearchResult> processSearchResponse(SearchResponse sr) {
        Terms dedup = sr.getAggregations().get("dedup");
        if (dedup == null) {
            return List.of();
        }

        List<QuestionSearchResult> results = new ArrayList<>();
        for (Terms.Bucket entry : dedup.getBuckets()) {
            TopHits topHits = entry.getAggregations().get("dedup_docs");
            if (topHits == null) {
                continue;
            }

            for (SearchHit hit : topHits.getHits().getHits()) {
                try {
                    String id = hit.getId();
                    if (!id.startsWith(SAM_ITEM_PREFIX)) {
                        continue;
                    }

                    String itemId = id.substring(SAM_ITEM_PREFIX.length());
                    String typeId = hit.field("typeId").getValue();
                    String qText = hit.field("qText").getValue();

                    var tags = hit.field("tags") != null
                            ? hit.field("tags").getValues().stream().map(Object::toString).collect(Collectors.toSet())
                            : java.util.Set.of();

                    String questionPoolId = getFieldValue(hit, "questionPoolId");
                    String assessmentId = getFieldValue(hit, "assessmentId");
                    String siteId = getFieldValue(hit, "site");

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
            String qpId = getFieldValue(hit, "questionPoolId");
            if (qpId != null) {
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

            String assessmentId = getFieldValue(hit, "assessmentId");
            String siteId = getFieldValue(hit, "site");
            if (assessmentId != null && siteId != null) {
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
        } catch (Exception ex) {
            log.debug("Could not extract origin: {}", ex.getMessage());
        }
        return "";
    }

    private String getFieldValue(SearchHit hit, String fieldName) {
        var field = hit.field(fieldName);
        return (field != null && field.getValue() != null) ? field.getValue().toString() : null;
    }
}
