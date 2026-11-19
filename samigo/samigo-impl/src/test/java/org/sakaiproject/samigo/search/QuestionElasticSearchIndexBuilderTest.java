/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.samigo.search;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryStringQueryBuilder;
import org.opensearch.index.query.TermQueryBuilder;

public class QuestionElasticSearchIndexBuilderTest {

    private final QuestionElasticSearchIndexBuilder indexBuilder = new QuestionElasticSearchIndexBuilder();

    @Before
    public void setUp() {
        indexBuilder.setIndexName("questions_index");
    }

    @Test
    public void textSearchQueriesTheQuestionsIndex() {
        SearchRequest request = indexBuilder.prepareSearchRequest("photosynthesis", List.of(), List.of(), List.of(), 0, 10, Map.of());

        assertArrayEquals(new String[] {"questions_index"}, request.indices());
        BoolQueryBuilder query = (BoolQueryBuilder) request.source().query();
        assertTrue(query.must().stream().anyMatch(QueryStringQueryBuilder.class::isInstance));
    }

    @Test
    public void questionIdLimitsTheSearchToThatQuestion() {
        SearchRequest request = indexBuilder.prepareSearchRequest("", List.of(), List.of(), List.of(), 0, 1,
                Map.of("subtype", "item", "questionId", "/sam_item/42"));

        BoolQueryBuilder query = (BoolQueryBuilder) request.source().query();
        List<TermQueryBuilder> questionTerms = query.must().stream()
                .filter(TermQueryBuilder.class::isInstance)
                .map(TermQueryBuilder.class::cast)
                .filter(term -> "questionId".equals(term.fieldName()))
                .toList();
        assertEquals(1, questionTerms.size());
        assertEquals("/sam_item/42", questionTerms.get(0).value());
    }
}
