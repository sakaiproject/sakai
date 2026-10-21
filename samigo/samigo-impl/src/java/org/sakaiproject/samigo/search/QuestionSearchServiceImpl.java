/**********************************************************************************
 * Copyright (c) 2026 The Sakai Foundation
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

package org.sakaiproject.samigo.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.common.document.DocumentField;
import org.opensearch.search.SearchHit;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.metrics.TopHits;
import org.sakaiproject.samigo.api.search.QuestionSearchException;
import org.sakaiproject.samigo.api.search.QuestionSearchResult;
import org.sakaiproject.samigo.api.search.QuestionSearchService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs question searches against {@link QuestionElasticSearchIndexBuilder} and maps the OpenSearch
 * responses to {@link QuestionSearchResult}s, so callers never see OpenSearch types.
 */
@Slf4j
public class QuestionSearchServiceImpl implements QuestionSearchService {

    private static final String ITEM_ID_PREFIX = "/sam_item/";
    private static final String SUBTYPE_ITEM = "item";
    private static final int MAX_ORIGINS = 1000;

    @Setter private QuestionElasticSearchIndexBuilder questionIndexBuilder;
    @Setter private SessionManager sessionManager;
    @Setter private SiteService siteService;
    @Setter private QuestionPoolService questionPoolService = new QuestionPoolService();
    @Setter private AssessmentService assessmentService;

    @Override
    public List<QuestionSearchResult> searchByText(String text, boolean matchAll) {
        if (StringUtils.isBlank(text)) {
            return List.of();
        }
        return toResults(search(text, 0, 0, groupedByHashParams(matchAll)));
    }

    @Override
    public List<QuestionSearchResult> searchByTags(List<String> tags, boolean matchAll) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        Map<String, String> params = groupedByHashParams(matchAll);
        for (int i = 0; i < tags.size(); i++) {
            params.put(QuestionElasticSearchIndexBuilder.PARAM_TAG_PREFIX + (i + 1), tags.get(i));
        }
        return toResults(search("", 0, 0, params));
    }

    @Override
    public boolean canAccessQuestion(String itemId) {
        if (StringUtils.isBlank(itemId)) {
            return false;
        }
        Map<String, String> params = scopedParams();
        params.put(QuestionElasticSearchIndexBuilder.PARAM_QUESTION_ID, ITEM_ID_PREFIX + itemId);
        try {
            SearchResponse response = search("", 0, 1, params);
            return response.getHits().getTotalHits() != null && response.getHits().getTotalHits().value >= 1;
        } catch (QuestionSearchException e) {
            log.warn("Could not check access to question {}: {}", itemId, e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getOrigins(String hash) {
        if (StringUtils.isBlank(hash)) {
            return List.of();
        }
        Map<String, String> params = scopedParams();
        params.put(QuestionElasticSearchIndexBuilder.PARAM_HASH, hash);
        try {
            SearchResponse response = search("", 0, MAX_ORIGINS, params);
            Map<String, String> titles = new HashMap<>();
            List<String> origins = new ArrayList<>();
            for (SearchHit hit : response.getHits()) {
                String origin = resolveOrigin(hit, titles);
                if (!origin.isEmpty()) {
                    origins.add(origin);
                }
            }
            return origins;
        } catch (QuestionSearchException e) {
            log.warn("Could not find origins of question hash {}: {}", hash, e.getMessage());
            return List.of();
        }
    }

    private SearchResponse search(String terms, int start, int end, Map<String, String> params) {
        if (!questionIndexBuilder.isEnabled()) {
            throw new QuestionSearchException("Search is not enabled");
        }
        SearchResponse response;
        try {
            response = questionIndexBuilder.search(terms, List.of(), List.of(), List.of(), start, end, params);
        } catch (RuntimeException e) {
            throw new QuestionSearchException("Question search failed", e);
        }
        if (response == null) {
            // the index builder returns null when the query is invalid or the search engine is unreachable
            throw new QuestionSearchException("Question search was rejected or could not be executed");
        }
        return response;
    }

    /** Only the questions the current user may author with: their sites and readable pools. */
    private Map<String, String> scopedParams() {
        Map<String, String> params = new HashMap<>();
        params.put(QuestionElasticSearchIndexBuilder.PARAM_SCOPE, QuestionElasticSearchIndexBuilder.SCOPE_OWN);
        params.put(QuestionElasticSearchIndexBuilder.PARAM_SUBTYPE, SUBTYPE_ITEM);
        return params;
    }

    /** Scoped and aggregated by hash, so copies of the same question come back once. */
    private Map<String, String> groupedByHashParams(boolean matchAll) {
        Map<String, String> params = scopedParams();
        params.put(QuestionElasticSearchIndexBuilder.PARAM_GROUP, QuestionElasticSearchIndexBuilder.FIELD_HASH);
        params.put(QuestionElasticSearchIndexBuilder.PARAM_LOGIC,
                matchAll ? QuestionElasticSearchIndexBuilder.LOGIC_AND : QuestionElasticSearchIndexBuilder.LOGIC_OR);
        return params;
    }

    private List<QuestionSearchResult> toResults(SearchResponse response) {
        Aggregations aggregations = response.getAggregations();
        Terms dedup = aggregations != null ? aggregations.get(QuestionElasticSearchIndexBuilder.AGGREGATION_NAME) : null;
        if (dedup == null) {
            return List.of();
        }

        Map<String, String> titles = new HashMap<>();
        List<QuestionSearchResult> results = new ArrayList<>();
        for (Terms.Bucket bucket : dedup.getBuckets()) {
            TopHits topHits = bucket.getAggregations().get(QuestionElasticSearchIndexBuilder.AGGREGATION_TOP_HITS);
            if (topHits == null) {
                continue;
            }
            for (SearchHit hit : topHits.getHits().getHits()) {
                if (hit.getId() == null || !hit.getId().startsWith(ITEM_ID_PREFIX)) {
                    continue;
                }
                results.add(new QuestionSearchResult(
                        hit.getId().substring(ITEM_ID_PREFIX.length()),
                        fieldValue(hit, "typeId"),
                        fieldValue(hit, "qText"),
                        tags(hit),
                        resolveOrigin(hit, titles)));
            }
        }
        return results;
    }

    /**
     * @param titles per-request cache of resolved titles, since many hits share a pool, site or assessment
     */
    private String resolveOrigin(SearchHit hit, Map<String, String> titles) {
        String poolId = fieldValue(hit, "questionPoolId");
        if (poolId != null) {
            return titles.computeIfAbsent("pool:" + poolId, k -> poolTitle(poolId));
        }

        String assessmentId = fieldValue(hit, "assessmentId");
        String siteId = fieldValue(hit, "site");
        if (assessmentId == null || siteId == null) {
            return "";
        }
        String siteTitle = titles.computeIfAbsent("site:" + siteId, k -> siteTitle(siteId));
        String assessmentTitle = titles.computeIfAbsent("assessment:" + assessmentId, k -> assessmentTitle(assessmentId));
        if (siteTitle.isEmpty() || assessmentTitle.isEmpty()) {
            return "";
        }
        return siteTitle + " : " + assessmentTitle;
    }

    private String poolTitle(String poolId) {
        try {
            return Objects.toString(questionPoolService.getPool(Long.valueOf(poolId), sessionManager.getCurrentSessionUserId()).getTitle(), "");
        } catch (Exception e) {
            log.debug("Could not resolve title of question pool {}: {}", poolId, e.toString());
            return "";
        }
    }

    private String siteTitle(String siteId) {
        try {
            return Objects.toString(siteService.getSite(siteId).getTitle(), "");
        } catch (Exception e) {
            log.debug("Could not resolve title of site {}: {}", siteId, e.toString());
            return "";
        }
    }

    private String assessmentTitle(String assessmentId) {
        try {
            return Objects.toString(assessmentService.getAssessment(assessmentId).getTitle(), "");
        } catch (Exception e) {
            log.debug("Could not resolve title of assessment {}: {}", assessmentId, e.toString());
            return "";
        }
    }

    private static Set<String> tags(SearchHit hit) {
        DocumentField field = hit.field("tags");
        if (field == null) {
            return Set.of();
        }
        return field.getValues().stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toUnmodifiableSet());
    }

    private static String fieldValue(SearchHit hit, String name) {
        DocumentField field = hit.field(name);
        Object value = field != null ? field.getValue() : null;
        return value != null ? value.toString() : null;
    }
}
