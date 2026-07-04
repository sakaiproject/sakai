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
package org.sakaiproject.assignment.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;
import org.sakaiproject.assignment.api.LatePenaltyCalculator;
import org.sakaiproject.assignment.api.LatePenaltyCalculator.LatePenaltyType;

/**
 * SAK-15574 late penalty computation. Grades are scaled integer strings
 * (e.g. 85.5 at scale factor 100 is "8550"); the penalty is computed in the
 * same scaled space and never persisted — the raw instructor grade is the
 * stored value.
 */
public class LatePenaltyCalculatorTest {

    private static final Instant DUE = Instant.parse("2026-01-05T12:00:00Z");

    private static Instant lateBy(Duration duration) {
        return DUE.plus(duration);
    }

    // 1. flat deduction
    @Test
    public void flatPenaltyDeductsScaledPoints() {
        assertEquals("8050", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, "5", DUE, lateBy(Duration.ofHours(1)), false));
    }

    // 2. per-day, partial day counts as a full day
    @Test
    public void perDayPenaltyPartialDayCountsAsFullDay() {
        assertEquals("8350", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.PER_DAY, "2", DUE, lateBy(Duration.ofHours(1)), false));
    }

    // 3. per-day, exactly 24h late is one day, not two
    @Test
    public void perDayPenaltyExactlyOneDayLate() {
        assertEquals("8350", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.PER_DAY, "2", DUE, lateBy(Duration.ofHours(24)), false));
    }

    // 4. per-day, one second past 24h rolls to the second day
    @Test
    public void perDayPenaltyJustOverOneDayLate() {
        assertEquals("8150", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.PER_DAY, "2", DUE, lateBy(Duration.ofHours(24).plusSeconds(1)), false));
    }

    // 5. per-day, 3.5 days late rounds up to 4 days
    @Test
    public void perDayPenaltyMultipleDaysCeiling() {
        assertEquals("7750", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.PER_DAY, "2", DUE, lateBy(Duration.ofHours(84)), false));
    }

    // 6. never below zero
    @Test
    public void penaltyFloorsAtZero() {
        assertEquals("0", LatePenaltyCalculator.applyLatePenalty(
                "3000", 100, LatePenaltyType.FLAT, "50", DUE, lateBy(Duration.ofHours(1)), false));
    }

    // 7. on-time submission untouched
    @Test
    public void submittedBeforeDueDateUnchanged() {
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, "5", DUE, DUE.minus(Duration.ofHours(1)), false));
    }

    // 8. submitted exactly at the due date is NOT late (isAfter semantics,
    // matching the existing late flag in AssignmentServiceImpl)
    @Test
    public void submittedExactlyAtDueDateNotLate() {
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, "5", DUE, DUE, false));
    }

    // 9. per-submission ignore flag suppresses the penalty
    @Test
    public void ignoreFlagSuppressesPenalty() {
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, "5", DUE, lateBy(Duration.ofDays(3)), true));
    }

    // 10. no due date means no lateness
    @Test
    public void nullDueDateUnchanged() {
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, "5", null, lateBy(Duration.ofDays(1)), false));
    }

    // 11. no submitted date (e.g. non-electronic or not yet submitted)
    @Test
    public void nullDateSubmittedUnchanged() {
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, "5", DUE, null, false));
    }

    // 12. ungraded passthrough
    @Test
    public void nullOrBlankGradePassthrough() {
        assertNull(LatePenaltyCalculator.applyLatePenalty(
                null, 100, LatePenaltyType.FLAT, "5", DUE, lateBy(Duration.ofHours(1)), false));
        assertEquals("", LatePenaltyCalculator.applyLatePenalty(
                "", 100, LatePenaltyType.FLAT, "5", DUE, lateBy(Duration.ofHours(1)), false));
    }

    // 13. decimal penalty value at grade precision
    @Test
    public void decimalPenaltyValue() {
        assertEquals("8300", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, "2.5", DUE, lateBy(Duration.ofHours(1)), false));
    }

    // 14. other scale factors
    @Test
    public void scaleFactorTenFlatPenalty() {
        assertEquals("825", LatePenaltyCalculator.applyLatePenalty(
                "855", 10, LatePenaltyType.FLAT, "3", DUE, lateBy(Duration.ofHours(1)), false));
    }

    // 15. defensive: malformed, zero, or negative penalty values apply nothing
    @Test
    public void invalidPenaltyValueUnchanged() {
        Instant submitted = lateBy(Duration.ofHours(1));
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, "abc", DUE, submitted, false));
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, "0", DUE, submitted, false));
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, "-2", DUE, submitted, false));
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, null, DUE, submitted, false));
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, null, "5", DUE, submitted, false));
    }

    // 15b. defensive: a non-numeric stored grade is left alone
    @Test
    public void nonNumericGradeUnchanged() {
        assertEquals("Pass", LatePenaltyCalculator.applyLatePenalty(
                "Pass", 100, LatePenaltyType.FLAT, "5", DUE, lateBy(Duration.ofHours(1)), false));
    }

    // 16. daysLate standalone
    @Test
    public void daysLateComputation() {
        assertEquals(0, LatePenaltyCalculator.daysLate(DUE, DUE.minus(Duration.ofHours(1))));
        assertEquals(0, LatePenaltyCalculator.daysLate(DUE, DUE));
        assertEquals(1, LatePenaltyCalculator.daysLate(DUE, lateBy(Duration.ofMinutes(1))));
        assertEquals(1, LatePenaltyCalculator.daysLate(DUE, lateBy(Duration.ofHours(24))));
        assertEquals(2, LatePenaltyCalculator.daysLate(DUE, lateBy(Duration.ofHours(24).plusSeconds(1))));
        assertEquals(4, LatePenaltyCalculator.daysLate(DUE, lateBy(Duration.ofHours(84))));
        assertEquals(0, LatePenaltyCalculator.daysLate(null, lateBy(Duration.ofHours(1))));
        assertEquals(0, LatePenaltyCalculator.daysLate(DUE, null));
    }

    // penalty amount used for the grader "-X late" annotation
    @Test
    public void penaltyScaledForAnnotation() {
        assertEquals(500, LatePenaltyCalculator.penaltyScaled(
                100, LatePenaltyType.FLAT, "5", DUE, lateBy(Duration.ofDays(3))));
        assertEquals(400, LatePenaltyCalculator.penaltyScaled(
                100, LatePenaltyType.PER_DAY, "2", DUE, lateBy(Duration.ofHours(25))));
        assertEquals(0, LatePenaltyCalculator.penaltyScaled(
                100, LatePenaltyType.PER_DAY, "2", DUE, DUE));
        assertEquals(0, LatePenaltyCalculator.penaltyScaled(
                100, LatePenaltyType.FLAT, "bogus", DUE, lateBy(Duration.ofHours(1))));
    }
}
