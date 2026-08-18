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
package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;

/**
 * pure logic behind "Apply to Selected": comment merging and row
 * eligibility.
 */
public class ApplyToSelectedListenerTest {

    // comment merge: append preserves what the instructor already wrote
    @Test
    public void appendToExistingCommentKeepsBoth() {
        assertEquals("Good work.\nRegraded essay Q3.",
                ApplyToSelectedListener.mergeComment("Good work.", "Regraded essay Q3.", true));
    }

    @Test
    public void appendToEmptyOrNullIsJustTheAddition() {
        assertEquals("New note.", ApplyToSelectedListener.mergeComment("", "New note.", true));
        assertEquals("New note.", ApplyToSelectedListener.mergeComment(null, "New note.", true));
        assertEquals("New note.", ApplyToSelectedListener.mergeComment("   ", "New note.", true));
    }

    @Test
    public void replaceOverwritesExisting() {
        assertEquals("New note.", ApplyToSelectedListener.mergeComment("Old note.", "New note.", false));
    }

    @Test
    public void blankAdditionLeavesExistingUntouchedInBothModes() {
        assertEquals("Old note.", ApplyToSelectedListener.mergeComment("Old note.", "", true));
        assertEquals("Old note.", ApplyToSelectedListener.mergeComment("Old note.", null, true));
        assertEquals("Old note.", ApplyToSelectedListener.mergeComment("Old note.", "  ", false));
    }

    // no-submission test mirrors the legacy applyToUngraded condition
    private AgentResults agent(Boolean selected, Long gradingId, Date submittedDate) {
        AgentResults a = new AgentResults();
        a.setSelected(selected);
        a.setAssessmentGradingId(gradingId);
        a.setSubmittedDate(submittedDate);
        return a;
    }

    @Test
    public void isNoSubmissionMatchesLegacyCondition() {
        // a real submission: has a grading id and a submitted date
        assertFalse(ApplyToSelectedListener.isNoSubmission(agent(Boolean.FALSE, 7L, new Date())));
        // no grading row, or -1, or no submitted date -> treated as no submission
        assertTrue(ApplyToSelectedListener.isNoSubmission(agent(Boolean.FALSE, -1L, null)));
        assertTrue(ApplyToSelectedListener.isNoSubmission(agent(Boolean.FALSE, 7L, null)));
        assertTrue(ApplyToSelectedListener.isNoSubmission(agent(Boolean.FALSE, null, new Date())));
    }

    @Test
    public void noSubmissionTargetHitsOnlyNonSubmitters() {
        assertTrue(ApplyToSelectedListener.matchesTarget(agent(Boolean.FALSE, -1L, null), ApplyToSelectedListener.NO_SUBMISSION));
        assertFalse(ApplyToSelectedListener.matchesTarget(agent(Boolean.FALSE, 7L, new Date()), ApplyToSelectedListener.NO_SUBMISSION));
    }

    @Test
    public void withSubmissionsTargetHitsOnlySubmitters() {
        assertTrue(ApplyToSelectedListener.matchesTarget(agent(Boolean.FALSE, 7L, new Date()), ApplyToSelectedListener.WITH_SUBMISSIONS));
        assertFalse(ApplyToSelectedListener.matchesTarget(agent(Boolean.FALSE, -1L, null), ApplyToSelectedListener.WITH_SUBMISSIONS));
    }

    @Test
    public void selectedTargetFollowsTheCheckboxOnly() {
        assertTrue(ApplyToSelectedListener.matchesTarget(agent(Boolean.TRUE, 7L, new Date()), ApplyToSelectedListener.SELECTED));
        assertFalse(ApplyToSelectedListener.matchesTarget(agent(Boolean.FALSE, 7L, new Date()), ApplyToSelectedListener.SELECTED));
    }

    @Test
    public void allTargetHitsEveryone() {
        assertTrue(ApplyToSelectedListener.matchesTarget(agent(Boolean.FALSE, 7L, new Date()), ApplyToSelectedListener.ALL));
        assertTrue(ApplyToSelectedListener.matchesTarget(agent(Boolean.FALSE, -1L, null), ApplyToSelectedListener.ALL));
    }

    @Test
    public void unknownTargetFallsBackToNoSubmission() {
        assertTrue(ApplyToSelectedListener.matchesTarget(agent(Boolean.FALSE, -1L, null), "SOMETHING_ELSE"));
        assertFalse(ApplyToSelectedListener.matchesTarget(agent(Boolean.FALSE, 7L, new Date()), "SOMETHING_ELSE"));
    }
}
