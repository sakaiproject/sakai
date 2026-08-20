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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.TotalHits;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensearch.action.search.SearchResponse;
import org.sakaiproject.search.api.InvalidSearchQueryException;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;

@RunWith(MockitoJUnitRunner.class)
public class QuestionSearchServiceImplTest {

    @Mock private SearchService searchService;
    @Mock private SiteService siteService;
    @Mock private QuestionPoolService questionPoolService;
    @Mock private AssessmentService assessmentService;

    private QuestionSearchServiceImpl service;

    @Before
    public void setUp() {
        service = new QuestionSearchServiceImpl();
        service.setSearchService(searchService);
        service.setSiteService(siteService);
        service.setQuestionPoolService(questionPoolService);
        service.setAssessmentService(assessmentService);
    }

    // --- Guard clauses ---

    @Test
    public void searchByTags_nullList_returnsEmpty() {
        assertTrue(service.searchByTags(null, true).isEmpty());
    }

    @Test
    public void searchByTags_emptyList_returnsEmpty() {
        assertTrue(service.searchByTags(List.of(), false).isEmpty());
    }

    @Test
    public void searchByText_nullText_returnsEmpty() {
        assertTrue(service.searchByText(null, true).isEmpty());
    }

    @Test
    public void searchByText_blankText_returnsEmpty() {
        assertTrue(service.searchByText("   ", false).isEmpty());
    }

    @Test
    public void userOwnsQuestion_nullId_returnsFalse() {
        assertFalse(service.userOwnsQuestion(null));
    }

    @Test
    public void userOwnsQuestion_emptyId_returnsFalse() {
        assertFalse(service.userOwnsQuestion(""));
    }

    @Test
    public void getQuestionOrigins_nullHash_returnsEmpty() {
        assertTrue(service.getQuestionOrigins(null, new HashMap<>()).isEmpty());
    }

    @Test
    public void getQuestionOrigins_emptyHash_returnsEmpty() {
        assertTrue(service.getQuestionOrigins("", new HashMap<>()).isEmpty());
    }

    @Test
    public void getOriginDisplay_nullResult_returnsEmpty() {
        assertEquals("", service.getOriginDisplay(null, new HashMap<>()));
    }

    // --- userOwnsQuestion with mocked search ---
    // SearchHits is final so we create a real instance instead of mocking it

    @Test
    public void userOwnsQuestion_hitFound_returnsTrue() throws Exception {
        org.opensearch.search.SearchHits realHits = new org.opensearch.search.SearchHits(
                new org.opensearch.search.SearchHit[0],
                new TotalHits(1L, TotalHits.Relation.EQUAL_TO), 0f);
        SearchResponse response = mock(SearchResponse.class);
        when(response.getHits()).thenReturn(realHits);
        when(searchService.searchResponse(eq(""), isNull(), isNull(), eq(0), eq(1), eq("questions"), any()))
                .thenReturn(response);

        assertTrue(service.userOwnsQuestion("item123"));
    }

    @Test
    public void userOwnsQuestion_noHits_returnsFalse() throws Exception {
        org.opensearch.search.SearchHits realHits = new org.opensearch.search.SearchHits(
                new org.opensearch.search.SearchHit[0],
                new TotalHits(0L, TotalHits.Relation.EQUAL_TO), 0f);
        SearchResponse response = mock(SearchResponse.class);
        when(response.getHits()).thenReturn(realHits);
        when(searchService.searchResponse(eq(""), isNull(), isNull(), eq(0), eq(1), eq("questions"), any()))
                .thenReturn(response);

        assertFalse(service.userOwnsQuestion("item123"));
    }

    @Test
    public void userOwnsQuestion_searchThrows_returnsFalse() throws Exception {
        when(searchService.searchResponse(any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenThrow(new RuntimeException("search error"));

        assertFalse(service.userOwnsQuestion("item123"));
    }

    // --- searchByTags parameter verification ---
    // Aggregations.get() calls real implementation on a mock (internal field NPE),
    // so we throw from searchService to skip processSearchResponse and just verify the call args.

    @Test
    public void searchByTags_passesCorrectIndexBuilderAndParams() throws Exception {
        when(searchService.searchResponse(any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenThrow(new InvalidSearchQueryException("test", new RuntimeException()));

        service.searchByTags(List.of("tag1", "tag2"), false);

        verify(searchService).searchResponse(
                eq(""), isNull(), isNull(), eq(0), eq(0), eq("questions"),
                argThat((Map<String, String> params) ->
                        "tag1".equals(params.get("tag_1")) &&
                        "tag2".equals(params.get("tag_2")) &&
                        "or".equals(params.get("logic")) &&
                        "hash".equals(params.get("group"))));
    }

    @Test
    public void searchByTags_andLogic_setsAndParam() throws Exception {
        when(searchService.searchResponse(any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenThrow(new InvalidSearchQueryException("test", new RuntimeException()));

        service.searchByTags(List.of("tag1"), true);

        verify(searchService).searchResponse(any(), any(), any(), anyInt(), anyInt(), any(),
                argThat((Map<String, String> params) -> "and".equals(params.get("logic"))));
    }

    @Test
    public void searchByTags_searchThrows_returnsEmpty() throws Exception {
        when(searchService.searchResponse(any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenThrow(new InvalidSearchQueryException("error", new RuntimeException()));

        assertTrue(service.searchByTags(List.of("tag1"), false).isEmpty());
    }

    // --- searchByText parameter verification ---

    @Test
    public void searchByText_passesTextToSearchService() throws Exception {
        when(searchService.searchResponse(any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenThrow(new InvalidSearchQueryException("test", new RuntimeException()));

        service.searchByText("hello world", false);

        verify(searchService).searchResponse(eq("hello world"), isNull(), isNull(), eq(0), eq(0), eq("questions"), any());
    }

    @Test
    public void searchByText_searchThrows_returnsEmpty() throws Exception {
        when(searchService.searchResponse(any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenThrow(new InvalidSearchQueryException("error", new RuntimeException()));

        assertTrue(service.searchByText("hello", false).isEmpty());
    }

    // --- titleCache used by getOriginDisplay ---

    @Test
    public void getOriginDisplay_cacheHit_returnsCachedValue() {
        QuestionSearchResult result = new QuestionSearchResult("item1", "1", "text", java.util.Set.of(), "pool42", null, null);
        Map<String, String> cache = new HashMap<>();
        cache.put("qp:pool42", "Cached Pool Title");

        assertEquals("Cached Pool Title", service.getOriginDisplay(result, cache));
    }

    @Test
    public void getOriginDisplay_assessmentCacheHit_returnsCachedValue() {
        QuestionSearchResult result = new QuestionSearchResult("item1", "1", "text", java.util.Set.of(), null, "assess99", "site1");
        Map<String, String> cache = new HashMap<>();
        cache.put("site:site1", "My Site");
        cache.put("assessment:assess99", "My Assessment");

        assertEquals("My Site : My Assessment", service.getOriginDisplay(result, cache));
    }
}
