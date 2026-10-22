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

package org.sakaiproject.samigo.api.search;

import java.util.List;

/**
 * Searches the question index for questions the current user is allowed to author with,
 * i.e. questions in sites where they can create assessments and in question pools they can read.
 */
public interface QuestionSearchService {

    /**
     * Finds questions whose text matches the given terms. Duplicate questions (same hash) are
     * collapsed into a single result.
     *
     * @param text plain text to search for
     * @param matchAll true to require every term, false to require any term
     * @return matching questions, empty if {@code text} is blank
     * @throws QuestionSearchException if search is disabled or the query cannot be executed
     */
    List<QuestionSearchResult> searchByText(String text, boolean matchAll);

    /**
     * Finds questions carrying the given tags. Duplicate questions (same hash) are collapsed
     * into a single result.
     *
     * @param tags indexed tag values, in the form {@code label(collection)}
     * @param matchAll true to require every tag, false to require any tag
     * @return matching questions, empty if {@code tags} is empty
     * @throws QuestionSearchException if search is disabled or the query cannot be executed
     */
    List<QuestionSearchResult> searchByTags(List<String> tags, boolean matchAll);

    /**
     * @param itemId the question (item) id
     * @return true if the question is within the current user's search scope. False on any failure.
     */
    boolean canAccessQuestion(String itemId);

    /**
     * @param hash the question hash shared by all copies of a question
     * @return the display origin of every copy of the question in the current user's search scope,
     *         as a pool title or {@code site title : assessment title}. Empty on any failure.
     */
    List<String> getOrigins(String hash);
}
