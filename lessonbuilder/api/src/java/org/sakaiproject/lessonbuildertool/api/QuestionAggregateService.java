/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.api;

import java.time.Instant;

import org.sakaiproject.lessonbuildertool.SimplePageItem;

/**
 * Manages the site-wide shared gradebook item aggregated across Lessons inline
 * question items (the ones flagged with the questionAggregate attribute). The
 * implementation lives in the component layer so the post-due-date final pass
 * can run as a scheduled invocation outside any tool session.
 */
public interface QuestionAggregateService {

    String EXTERNAL_ID_PREFIX = "lesson-builder:qaggregate:";
    /** item attribute marking a question as a member of the aggregate */
    String MEMBER_ATTRIBUTE = "questionAggregate";

    /**
     * Group-restricted questions are barred from the aggregate: students outside
     * the groups could never earn those shares, which would silently cap them
     * below the item maximum.
     */
    static boolean isGroupRestricted(SimplePageItem item) {
        return item.getGroups() != null && !item.getGroups().trim().isEmpty();
    }

    /** whether the shared item is configured (has a title) for the site */
    boolean isEnabled(String siteId);

    String getTitle(String siteId);

    /** QuestionAggregateCalculator.MODE_PARTICIPATION or MODE_CORRECTNESS */
    String getMode(String siteId);

    /** QuestionAggregateCalculator.ESSAY_ANY_ANSWER or ESSAY_CARRY_GRADE —
     *  how correctness mode treats short-answer questions */
    String getEssayMode(String siteId);

    /** whether newly created questions should default to being included */
    boolean isAutoInclude(String siteId);

    /** worth of each question in points, or null for the default of 1 */
    Double getPerQuestionPoints(String siteId);

    /** fixed worth of the whole item, or null to grow with the question count */
    Double getFixedPoints(String siteId);

    /** due date shown on the gradebook item and used for the final recompute pass, or null */
    Instant getDueDate(String siteId);

    /**
     * Persist the shared item's settings and resync the gradebook item. A blank
     * title disables the feature and removes the item. Exactly one of
     * perQuestionPoints / fixedPoints should be set (both null = 1 point per
     * question). When a future due date is set, a final recompute is scheduled
     * to run just after it passes.
     *
     * @return false when the gradebook rejected the item (e.g. an unrelated item
     *         already owns the title); property changes are still saved
     */
    boolean configure(String siteId, String title, String mode, String essayMode,
            Double perQuestionPoints, Double fixedPoints, Instant dueDate, boolean autoInclude);

    /**
     * Flag every question item as a member — across the whole site, or a single
     * page when pageId &gt; 0. Group-restricted questions are skipped. Does not
     * recompute; call {@link #recompute}.
     *
     * @return number of questions newly flagged
     */
    int includeAllQuestions(String siteId, long pageId);

    /**
     * Change just the due date (e.g. from Date Manager): updates the stored
     * setting, the gradebook item's displayed date, and the scheduled final
     * pass. Null clears the date. No-op when the feature is disabled.
     */
    void setDueDate(String siteId, Instant dueDate);

    /**
     * Carry the shared item's configuration into a copied site (site duplication /
     * Import from Site) so the feature arrives enabled there. Call after the
     * question items — whose membership flags copy with them — have landed.
     * The due date copies as-is; Date Manager's bulk shift moves it forward with
     * the rest of the term's dates. No-op when the source site has no shared item.
     */
    void copyConfig(String fromSiteId, String toSiteId);

    /**
     * Rebuild the shared item from stored responses: max points, every student's
     * score, and explicit zeros for responders whose questions left the aggregate.
     * Creates the external item on first use and removes it when no members remain.
     */
    boolean recompute(String siteId);

    /** Recount and push one student's score; call after their response changes. */
    void updateScoreForUser(String siteId, String userId);
}
