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
package org.sakaiproject.tool.assessment.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;

public class AssessmentFeedbackDateValidatorTest {

    @Test
    public void rejectsFeedbackDatesWithoutDueDate() {
        List<AssessmentFeedbackDateValidator.Violation> violations = AssessmentFeedbackDateValidator.validate(null, null, null,
                date("2026-06-25T00:00:00Z"), null);

        assertTrue(hasError(violations, AssessmentFeedbackDateValidator.Error.MISSING_DUE_DATE));
    }

    @Test
    public void allowsMissingDueDateWhenNoFeedbackDatesAreSet() {
        List<AssessmentFeedbackDateValidator.Violation> violations = AssessmentFeedbackDateValidator.validate(null, null, null, null, null);

        assertFalse(hasError(violations, AssessmentFeedbackDateValidator.Error.MISSING_DUE_DATE));
    }

    @Test
    public void rejectsFeedbackStartBeforeDueDate() {
        List<AssessmentFeedbackDateValidator.Violation> violations = AssessmentFeedbackDateValidator.validate(date("2026-06-25T00:00:00Z"), null, null,
                date("2026-06-24T23:59:59Z"), null);

        assertTrue(hasError(violations, AssessmentFeedbackDateValidator.Error.FEEDBACK_START_BEFORE_DUE_DATE));
    }

    @Test
    public void rejectsFeedbackEndBeforeStart() {
        List<AssessmentFeedbackDateValidator.Violation> violations = AssessmentFeedbackDateValidator.validate(date("2026-06-25T00:00:00Z"), null, null,
                date("2026-06-26T00:00:00Z"), date("2026-06-25T23:59:59Z"));

        assertTrue(hasError(violations, AssessmentFeedbackDateValidator.Error.FEEDBACK_END_BEFORE_START));
    }

    @Test
    public void rejectsFeedbackEndBeforeLateSubmissionDate() {
        List<AssessmentFeedbackDateValidator.Violation> violations = AssessmentFeedbackDateValidator.validate(date("2026-06-25T00:00:00Z"),
                date("2026-06-26T00:00:00Z"), AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION,
                date("2026-06-26T00:00:00Z"), date("2026-06-25T23:59:59Z"));

        assertTrue(hasError(violations, AssessmentFeedbackDateValidator.Error.FEEDBACK_END_BEFORE_DUE_DATE));
    }

    @Test
    public void allowsFeedbackDatesOnDueDate() {
        List<AssessmentFeedbackDateValidator.Violation> violations = AssessmentFeedbackDateValidator.validate(date("2026-06-25T00:00:00Z"), null, null,
                date("2026-06-25T00:00:00Z"), null);

        assertFalse(hasError(violations, AssessmentFeedbackDateValidator.Error.FEEDBACK_START_BEFORE_DUE_DATE));
        assertFalse(hasError(violations, AssessmentFeedbackDateValidator.Error.MISSING_DUE_DATE));
    }

    @Test
    public void validatesStartAgainstDueWhenEndIsInvalidButOmittedFromValidation() {
        List<AssessmentFeedbackDateValidator.Violation> violations = AssessmentFeedbackDateValidator.validate(date("2026-06-25T00:00:00Z"), null, null,
                date("2026-06-24T23:59:59Z"), null);

        assertTrue(hasError(violations, AssessmentFeedbackDateValidator.Error.FEEDBACK_START_BEFORE_DUE_DATE));
    }

    @Test
    public void usesDueDateAsCutoffWhenRetractEqualsDueDate() {
        Date dueDate = date("2026-06-25T00:00:00Z");

        assertEquals(dueDate, AssessmentFeedbackDateValidator.getFeedbackCutoffDate(dueDate, dueDate,
                AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION));
    }

    @Test
    public void usesDueDateAsCutoffWhenRetractIsBeforeDueDate() {
        Date dueDate = date("2026-06-25T00:00:00Z");

        assertEquals(dueDate, AssessmentFeedbackDateValidator.getFeedbackCutoffDate(dueDate, date("2026-06-24T00:00:00Z"),
                AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION));
    }

    private Date date(String value) {
        return Date.from(Instant.parse(value));
    }

    private boolean hasError(List<AssessmentFeedbackDateValidator.Violation> violations, AssessmentFeedbackDateValidator.Error error) {
        return violations.stream().anyMatch(violation -> error.equals(violation.getError()));
    }
}
