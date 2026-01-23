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

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a question search result.
 * This is a clean DTO that doesn't expose OpenSearch implementation types.
 *
 * <p>The origin can be computed from questionPoolId, assessmentId, and siteId
 * by the caller. This avoids service-level caching issues.</p>
 */
@Data
@AllArgsConstructor
public class QuestionSearchResult {
    /**
     * The question item ID (without "/sam_item/" prefix)
     */
    private String id;

    /**
     * The question type ID
     */
    private String typeId;

    /**
     * The question text
     */
    private String questionText;

    /**
     * Tags associated with this question
     */
    private Set<String> tags;

    /**
     * The question pool ID if this question is from a pool, null otherwise
     */
    private String questionPoolId;

    /**
     * The assessment ID if this question is from an assessment, null otherwise
     */
    private String assessmentId;

    /**
     * The site ID if this question is from an assessment, null otherwise
     */
    private String siteId;

    /**
     * Check if this question is from a question pool.
     */
    public boolean isFromQuestionPool() {
        return questionPoolId != null;
    }

    /**
     * Check if this question is from an assessment.
     */
    public boolean isFromAssessment() {
        return assessmentId != null && siteId != null;
    }
}
