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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.TotalHits;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.common.document.DocumentField;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.metrics.TopHits;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.samigo.api.search.QuestionSearchException;
import org.sakaiproject.samigo.api.search.QuestionSearchResult;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;

@RunWith(MockitoJUnitRunner.class)
public class QuestionSearchServiceImplTest {

    @Mock private QuestionElasticSearchIndexBuilder indexBuilder;
    @Mock private SessionManager sessionManager;
    @Mock private SiteService siteService;
    @Mock private QuestionPoolService questionPoolService;
    @Mock private AssessmentService assessmentService;

    private QuestionSearchServiceImpl service;

    @Before
    public void setUp() {
        service = new QuestionSearchServiceImpl();
        service.setQuestionIndexBuilder(indexBuilder);
        service.setSessionManager(sessionManager);
        service.setSiteService(siteService);
        service.setQuestionPoolService(questionPoolService);
        service.setAssessmentService(assessmentService);
    }

    @Test
    public void blankInputsDoNotSearch() {
        assertTrue(service.searchByText(null, true).isEmpty());
        assertTrue(service.searchByText("  ", true).isEmpty());
        assertTrue(service.searchByTags(null, true).isEmpty());
        assertTrue(service.searchByTags(List.of(), false).isEmpty());
        assertFalse(service.canAccessQuestion(""));
        assertTrue(service.getOrigins(null).isEmpty());

        verify(indexBuilder, never()).search(anyString(), anyList(), anyList(), anyList(), anyInt(), anyInt(), anyMap());
    }

    @Test
    public void searchByTextSendsScopedGroupedQuery() {
        Map<String, String> params = captureSearch("photosynthesis", grouped(), () -> service.searchByText("photosynthesis", true));

        assertEquals("own", params.get("scope"));
        assertEquals("item", params.get("subtype"));
        assertEquals("hash", params.get("group"));
        assertEquals("and", params.get("logic"));
    }

    @Test
    public void searchByTagsSendsEachTagAndOrLogic() {
        Map<String, String> params = captureSearch("", grouped(), () -> service.searchByTags(List.of("easy(Level)", "cells(Topic)"), false));

        assertEquals("or", params.get("logic"));
        assertEquals("easy(Level)", params.get("tag_1"));
        assertEquals("cells(Topic)", params.get("tag_2"));
        assertEquals("hash", params.get("group"));
    }

    @Test
    public void searchMapsAggregatedHitsToResults() {
        QuestionPoolFacade pool = mock(QuestionPoolFacade.class);
        when(pool.getTitle()).thenReturn("Biology pool");
        when(sessionManager.getCurrentSessionUserId()).thenReturn("instructor");
        when(questionPoolService.getPool(7L, "instructor")).thenReturn(pool);
        givenResponse(grouped(
                hit("/sam_item/42", Map.of("typeId", "1", "qText", "What is a cell?", "tags", List.of("easy(Level)"), "questionPoolId", "7"))));

        List<QuestionSearchResult> results = service.searchByText("cell", false);

        assertEquals(1, results.size());
        QuestionSearchResult result = results.get(0);
        assertEquals("42", result.getId());
        assertEquals("1", result.getTypeId());
        assertEquals("What is a cell?", result.getQuestionText());
        assertEquals(Set.of("easy(Level)"), result.getTags());
        assertEquals("Biology pool", result.getOrigin());
    }

    @Test
    public void searchSkipsHitsThatAreNotItems() {
        givenResponse(grouped(hit("/sam_publisheditem/9", Map.of("typeId", "1"))));

        assertTrue(service.searchByText("cell", false).isEmpty());
    }

    @Test
    public void searchResolvesSiteAndAssessmentOriginOncePerTitle() throws Exception {
        Site site = mock(Site.class);
        when(site.getTitle()).thenReturn("BIO 101");
        when(siteService.getSite("site1")).thenReturn(site);
        AssessmentFacade assessment = mock(AssessmentFacade.class);
        when(assessment.getTitle()).thenReturn("Quiz 1");
        when(assessmentService.getAssessment("5")).thenReturn(assessment);
        givenResponse(grouped(
                hit("/sam_item/1", Map.of("typeId", "1", "site", "site1", "assessmentId", "5")),
                hit("/sam_item/2", Map.of("typeId", "2", "site", "site1", "assessmentId", "5"))));

        List<QuestionSearchResult> results = service.searchByText("cell", false);

        assertEquals("BIO 101 : Quiz 1", results.get(0).getOrigin());
        assertEquals("BIO 101 : Quiz 1", results.get(1).getOrigin());
        assertEquals(Set.of(), results.get(0).getTags());
        verify(siteService, times(1)).getSite("site1");
        verify(assessmentService, times(1)).getAssessment("5");
    }

    @Test
    public void searchLeavesOriginEmptyWhenItCannotBeResolved() throws Exception {
        when(siteService.getSite("gone")).thenThrow(new IdUnusedException("gone"));
        when(assessmentService.getAssessment("5")).thenReturn(mock(AssessmentFacade.class));
        givenResponse(grouped(hit("/sam_item/1", Map.of("typeId", "1", "site", "gone", "assessmentId", "5"))));

        assertEquals("", service.searchByText("cell", false).get(0).getOrigin());
    }

    @Test
    public void searchFailsWhenSearchIsDisabled() {
        when(indexBuilder.isEnabled()).thenReturn(false);

        assertThrows(QuestionSearchException.class, () -> service.searchByText("cell", true));
    }

    @Test
    public void searchFailsWhenTheQueryIsRejected() {
        givenResponse(null);

        assertThrows(QuestionSearchException.class, () -> service.searchByTags(List.of("easy(Level)"), true));
    }

    @Test
    public void searchWrapsSearchEngineErrors() {
        when(indexBuilder.isEnabled()).thenReturn(true);
        when(indexBuilder.search(anyString(), anyList(), anyList(), anyList(), anyInt(), anyInt(), anyMap()))
                .thenThrow(new IllegalStateException("cluster down"));

        assertThrows(QuestionSearchException.class, () -> service.searchByText("cell", true));
    }

    @Test
    public void canAccessQuestionFiltersOnTheIndexedQuestionId() {
        Map<String, String> params = captureSearch("", hitsResponse(1), () -> assertTrue(service.canAccessQuestion("42")));

        assertEquals("/sam_item/42", params.get("questionId"));
        assertEquals("own", params.get("scope"));
        assertFalse(params.containsKey("group"));
    }

    @Test
    public void canAccessQuestionIsFalseWithoutHitsOrOnFailure() {
        givenResponse(hitsResponse(0));
        assertFalse(service.canAccessQuestion("42"));

        givenResponse(null);
        assertFalse(service.canAccessQuestion("42"));
    }

    @Test
    public void getOriginsListsEveryResolvableCopy() {
        QuestionPoolFacade pool = mock(QuestionPoolFacade.class);
        when(pool.getTitle()).thenReturn("Biology pool");
        when(sessionManager.getCurrentSessionUserId()).thenReturn("instructor");
        when(questionPoolService.getPool(7L, "instructor")).thenReturn(pool);
        SearchResponse response = hitsResponse(2,
                hit("/sam_item/1", Map.of("questionPoolId", "7")),
                hit("/sam_item/2", Map.of()));

        Map<String, String> params = captureSearch("", response, () -> assertEquals(List.of("Biology pool"), service.getOrigins("abc")));

        assertEquals("abc", params.get("hash"));
    }

    @Test
    public void getOriginsIsEmptyOnFailure() {
        when(indexBuilder.isEnabled()).thenReturn(false);

        assertTrue(service.getOrigins("abc").isEmpty());
    }

    private void givenResponse(SearchResponse response) {
        when(indexBuilder.isEnabled()).thenReturn(true);
        when(indexBuilder.search(anyString(), anyList(), anyList(), anyList(), anyInt(), anyInt(), anyMap())).thenReturn(response);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> captureSearch(String terms, SearchResponse response, Runnable call) {
        givenResponse(response);
        call.run();
        ArgumentCaptor<Map<String, String>> params = ArgumentCaptor.forClass(Map.class);
        verify(indexBuilder).search(eq(terms), anyList(), anyList(), anyList(), anyInt(), anyInt(), params.capture());
        return params.getValue();
    }

    private static SearchHit hit(String id, Map<String, Object> fields) {
        Map<String, DocumentField> documentFields = new HashMap<>();
        fields.forEach((name, value) -> documentFields.put(name,
                new DocumentField(name, value instanceof List ? new ArrayList<>((List<?>) value) : List.of(value))));
        return new SearchHit(0, id, documentFields, Map.of());
    }

    private static SearchHits searchHits(long total, SearchHit... hits) {
        return new SearchHits(hits, new TotalHits(total, TotalHits.Relation.EQUAL_TO), 1f);
    }

    private static SearchResponse hitsResponse(long total, SearchHit... hits) {
        SearchResponse response = mock(SearchResponse.class);
        when(response.getHits()).thenReturn(searchHits(total, hits));
        return response;
    }

    /** A response aggregated by hash, with one bucket per hit, as the index builder returns for grouped searches. */
    private static SearchResponse grouped(SearchHit... hits) {
        List<Terms.Bucket> buckets = new ArrayList<>();
        for (SearchHit hit : hits) {
            TopHits topHits = mock(TopHits.class);
            when(topHits.getName()).thenReturn(QuestionElasticSearchIndexBuilder.AGGREGATION_TOP_HITS);
            when(topHits.getHits()).thenReturn(searchHits(1, hit));
            Terms.Bucket bucket = mock(Terms.Bucket.class);
            when(bucket.getAggregations()).thenReturn(new Aggregations(List.of(topHits)));
            buckets.add(bucket);
        }
        Terms dedup = mock(Terms.class);
        when(dedup.getName()).thenReturn(QuestionElasticSearchIndexBuilder.AGGREGATION_NAME);
        doReturn(buckets).when(dedup).getBuckets();

        SearchResponse response = mock(SearchResponse.class);
        when(response.getAggregations()).thenReturn(new Aggregations(List.of(dedup)));
        return response;
    }
}
