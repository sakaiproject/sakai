/**
 * Copyright (c) 2005-2026 The Apereo Foundation
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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;

import org.junit.Test;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;

public class FeedbackDateValidatorTest {

    private static final String ACCEPT_LATE = AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.toString();
    private static final String NO_LATE = AssessmentAccessControlIfc.NOT_ACCEPT_LATE_SUBMISSION.toString();

    private static final Date EARLIER = new Date(1_000_000L);
    private static final Date DUE = new Date(2_000_000L);
    private static final Date RETRACT = new Date(3_000_000L);
    private static final Date LATER = new Date(4_000_000L);

    private ExtendedTime entry(Date due, Date retract) {
        ExtendedTime e = new ExtendedTime();
        e.setDueDate(due);
        e.setRetractDate(retract);
        return e;
    }

    // --- base cohort (no exceptions) ---

    @Test
    public void deadlineIsDueDateWithoutLateHandling() {
        assertEquals(DUE, FeedbackDateValidator.latestSubmissionDeadline(DUE, RETRACT, NO_LATE, Collections.emptyList()));
    }

    @Test
    public void deadlineIsRetractDateWhenLateSubmissionsAccepted() {
        assertEquals(RETRACT, FeedbackDateValidator.latestSubmissionDeadline(DUE, RETRACT, ACCEPT_LATE, Collections.emptyList()));
    }

    @Test
    public void noDeadlineWhenLateAcceptedWithoutRetractDate() {
        // late submissions accepted with no late acceptance date: a first submission
        // is allowed at any time after the due date, so there is no final deadline
        assertNull(FeedbackDateValidator.latestSubmissionDeadline(DUE, null, ACCEPT_LATE, null));
    }

    @Test
    public void noDeadlineWhenNoDueDateAndNotAcceptingLate() {
        assertNull(FeedbackDateValidator.latestSubmissionDeadline(null, RETRACT, NO_LATE, Collections.emptyList()));
    }

    @Test
    public void deadlineIsNullWhenNoDatesAnywhere() {
        assertNull(FeedbackDateValidator.latestSubmissionDeadline(null, null, NO_LATE, Collections.emptyList()));
    }

    @Test
    public void nullOrUnknownLateHandlingFallsBackToDueDate() {
        assertEquals(DUE, FeedbackDateValidator.latestSubmissionDeadline(DUE, RETRACT, null, Collections.emptyList()));
        assertEquals(DUE, FeedbackDateValidator.latestSubmissionDeadline(DUE, RETRACT, "not-a-code", Collections.emptyList()));
    }

    @Test
    public void nullExceptionListLeavesBaseDeadlineIntact() {
        assertEquals(DUE, FeedbackDateValidator.latestSubmissionDeadline(DUE, null, NO_LATE, null));
    }

    // --- exceptions extending a finite base deadline ---

    @Test
    public void exceptionDueDateExtendsDeadlineWhenNotAcceptingLate() {
        // not accepting late: an exception's deadline is its due date (its retract is irrelevant)
        assertEquals(LATER, FeedbackDateValidator.latestSubmissionDeadline(DUE, null, NO_LATE,
                Collections.singletonList(entry(LATER, EARLIER))));
    }

    @Test
    public void exceptionRetractDateExtendsDeadlineWhenLateAccepted() {
        // accepting late: an exception's deadline is its retract date (its due is irrelevant)
        assertEquals(LATER, FeedbackDateValidator.latestSubmissionDeadline(DUE, RETRACT, ACCEPT_LATE,
                Collections.singletonList(entry(EARLIER, LATER))));
    }

    @Test
    public void earlierExceptionDoesNotShrinkDeadline() {
        assertEquals(RETRACT, FeedbackDateValidator.latestSubmissionDeadline(DUE, RETRACT, ACCEPT_LATE,
                Collections.singletonList(entry(DUE, EARLIER))));
    }

    // --- open-ended cohorts must poison the deadline to null (SAK-34476 completeness) ---

    @Test
    public void openEndedBaseIsNotRescuedByAnException() {
        // regular students are open-ended (no due date, not accepting late); an exception with a
        // finite date must NOT make the assessment look bounded - regular students still leak
        assertNull(FeedbackDateValidator.latestSubmissionDeadline(null, null, NO_LATE,
                Collections.singletonList(entry(LATER, null))));
    }

    @Test
    public void nullRetractExceptionPoisonsDeadlineWhenLateAccepted() {
        // accepting late + an exception holder with no retract date is open-ended for that holder
        assertNull(FeedbackDateValidator.latestSubmissionDeadline(DUE, RETRACT, ACCEPT_LATE,
                Collections.singletonList(entry(DUE, null))));
    }

    // --- isFeedbackDateAfterDeadline: the accepting paths never touch the FacesContext (only the
    // rejecting paths call context.addMessage), so a null context both exercises the happy path and
    // asserts that no message is emitted. The rejecting paths need live JSF/bundle wiring and are
    // covered by manual QA. ---

    @Test
    public void feedbackDateEqualToDeadlineIsAllowed() {
        assertTrue(FeedbackDateValidator.isFeedbackDateAfterDeadline(
                new Date(DUE.getTime()), DUE, RETRACT, NO_LATE, Collections.emptyList(), null));
    }

    @Test
    public void feedbackDateAfterDeadlineIsAllowed() {
        assertTrue(FeedbackDateValidator.isFeedbackDateAfterDeadline(
                LATER, DUE, RETRACT, NO_LATE, Collections.emptyList(), null));
    }

    @Test
    public void nullFeedbackDateIsLeftToExistingChecks() {
        assertTrue(FeedbackDateValidator.isFeedbackDateAfterDeadline(
                null, DUE, null, ACCEPT_LATE, null, null));
    }
}
