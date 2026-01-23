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

import java.util.List;
import java.util.Map;

/**
 * Service for searching questions in the Samigo question bank.
 * This provides a clean API for question search operations without exposing
 * OpenSearch-specific types.
 *
 * <p>This service is stateless. Callers should manage their own caches for
 * title lookups to avoid memory issues.</p>
 */
public interface QuestionSearchService {

    /**
     * Search for questions by tags.
     *
     * @param tagLabels list of tag labels in format "TagLabel(CollectionName)"
     * @param andLogic true to require all tags (AND logic), false for any tag (OR logic)
     * @return list of matching questions, or empty list if none found
     * @throws QuestionSearchException if the search operation fails
     */
    List<QuestionSearchResult> searchByTags(List<String> tagLabels, boolean andLogic)
            throws QuestionSearchException;

    /**
     * Search for questions by text content.
     *
     * @param text the search text
     * @param andLogic true for AND logic, false for OR logic
     * @return list of matching questions, or empty list if none found
     * @throws QuestionSearchException if the search operation fails
     */
    List<QuestionSearchResult> searchByText(String text, boolean andLogic)
            throws QuestionSearchException;

    /**
     * Check if the current user owns a specific question.
     *
     * @param questionId the question ID to check
     * @return true if the current user owns this question, false otherwise
     */
    boolean userOwnsQuestion(String questionId);

    /**
     * Get all origin descriptions for questions with the same hash.
     * This is used to find duplicate questions across different assessments/pools.
     *
     * @param hash the question content hash
     * @param titleCache optional cache for title lookups (keys: "qp:id", "site:id", "assessment:id")
     *                   Pass null if no caching is desired
     * @return list of origin descriptions (e.g., "Site Name : Assessment Name")
     */
    List<String> getQuestionOrigins(String hash, Map<String, String> titleCache);
}
