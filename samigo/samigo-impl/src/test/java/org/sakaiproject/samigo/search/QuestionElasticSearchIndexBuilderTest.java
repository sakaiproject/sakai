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
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryStringQueryBuilder;

public class QuestionElasticSearchIndexBuilderTest {

    @Test
    public void textSearchQueriesTheQuestionsIndex() {
        QuestionElasticSearchIndexBuilder indexBuilder = new QuestionElasticSearchIndexBuilder();
        indexBuilder.setIndexName("questions_index");

        SearchRequest request = indexBuilder.prepareSearchRequest("photosynthesis", List.of(), List.of(), List.of(), 0, 10, Map.of());

        assertArrayEquals(new String[] {"questions_index"}, request.indices());
        BoolQueryBuilder query = (BoolQueryBuilder) request.source().query();
        assertTrue(query.must().stream().anyMatch(QueryStringQueryBuilder.class::isInstance));
    }
}
