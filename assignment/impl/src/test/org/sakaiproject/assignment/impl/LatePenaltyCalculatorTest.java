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
import org.sakaiproject.assignment.api.LatePenaltyCalculator.LatePenaltyUnit;

/**
 * SAK-15574 late penalty computation. Grades are scaled integer strings
 * (e.g. 85.5 at scale factor 100 is "8550"); the penalty is computed in the
 * same scaled space and never persisted — the raw instructor grade is the
 * stored value. Deductions are fixed points or a percent of the max grade,
 * applied once (flat) or per hour/day/week late where any part of a period
 * counts as a full one.
 */
public class LatePenaltyCalculatorTest {

    private static final Instant DUE = Instant.parse("2026-01-05T12:00:00Z");
    private static final Integer MAX = 10000; // 100.00 at factor 100, already scaled

    private static Instant lateBy(Duration duration) {
        return DUE.plus(duration);
    }

    private static String apply(String raw, LatePenaltyType type, String value, Instant submitted, boolean ignore) {
        return LatePenaltyCalculator.applyLatePenalty(raw, 100, type, LatePenaltyUnit.POINTS, value, MAX, DUE, submitted, ignore);
    }

    // 1. flat deduction
    @Test
    public void flatPenaltyDeductsScaledPoints() {
        assertEquals("8050", apply("8550", LatePenaltyType.FLAT, "5", lateBy(Duration.ofHours(1)), false));
    }

    // 2. per-day, partial day counts as a full day
    @Test
    public void perDayPenaltyPartialDayCountsAsFullDay() {
        assertEquals("8350", apply("8550", LatePenaltyType.PER_DAY, "2", lateBy(Duration.ofHours(1)), false));
    }

    // 3. per-day, exactly 24h late is one day, not two
    @Test
    public void perDayPenaltyExactlyOneDayLate() {
        assertEquals("8350", apply("8550", LatePenaltyType.PER_DAY, "2", lateBy(Duration.ofHours(24)), false));
    }

    // 4. per-day, one second past 24h rolls to the second day
    @Test
    public void perDayPenaltyJustOverOneDayLate() {
        assertEquals("8150", apply("8550", LatePenaltyType.PER_DAY, "2", lateBy(Duration.ofHours(24).plusSeconds(1)), false));
    }

    // 5. per-day, 3.5 days late rounds up to 4 days
    @Test
    public void perDayPenaltyMultipleDaysCeiling() {
        assertEquals("7750", apply("8550", LatePenaltyType.PER_DAY, "2", lateBy(Duration.ofHours(84)), false));
    }

    // per-hour: one minute late is one hour, boundary counts as that hour
    @Test
    public void perHourPenalty() {
        assertEquals("8500", apply("8550", LatePenaltyType.PER_HOUR, "0.5", lateBy(Duration.ofMinutes(1)), false));
        assertEquals("8500", apply("8550", LatePenaltyType.PER_HOUR, "0.5", lateBy(Duration.ofHours(1)), false));
        assertEquals("8450", apply("8550", LatePenaltyType.PER_HOUR, "0.5", lateBy(Duration.ofMinutes(61)), false));
    }

    // per-week: partial week is a full week, boundary counts as that week
    @Test
    public void perWeekPenalty() {
        assertEquals("8250", apply("8550", LatePenaltyType.PER_WEEK, "3", lateBy(Duration.ofHours(1)), false));
        assertEquals("8250", apply("8550", LatePenaltyType.PER_WEEK, "3", lateBy(Duration.ofDays(7)), false));
        assertEquals("7950", apply("8550", LatePenaltyType.PER_WEEK, "3", lateBy(Duration.ofDays(7).plusSeconds(1)), false));
    }

    // percent of the max grade per application (Canvas model), in scaled units
    @Test
    public void percentOfMaxFlat() {
        // 10% of 10000 scaled = 1000
        assertEquals("7550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, LatePenaltyUnit.PERCENT, "10", MAX, DUE, lateBy(Duration.ofHours(1)), false));
        // decimals: 2.5% of 10000 = 250
        assertEquals("8300", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, LatePenaltyUnit.PERCENT, "2.5", MAX, DUE, lateBy(Duration.ofHours(1)), false));
    }

    @Test
    public void percentOfMaxPerInterval() {
        // 10% per day, 30h late = 2 days -> 2000 scaled
        assertEquals("6550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.PER_DAY, LatePenaltyUnit.PERCENT, "10", MAX, DUE, lateBy(Duration.ofHours(30)), false));
    }

    // percent without a usable maximum yields no penalty rather than a guess
    @Test
    public void percentWithMissingMaxUnchanged() {
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, LatePenaltyUnit.PERCENT, "10", null, DUE, lateBy(Duration.ofHours(1)), false));
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, LatePenaltyUnit.PERCENT, "10", 0, DUE, lateBy(Duration.ofHours(1)), false));
    }

    // a null unit behaves as POINTS (properties saved before units existed)
    @Test
    public void nullUnitBehavesAsPoints() {
        assertEquals("8050", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, null, "5", MAX, DUE, lateBy(Duration.ofHours(1)), false));
    }

    @Test
    public void unitFromStringDefaultsToPoints() {
        assertEquals(LatePenaltyUnit.POINTS, LatePenaltyUnit.fromString("POINTS"));
        assertEquals(LatePenaltyUnit.PERCENT, LatePenaltyUnit.fromString("PERCENT"));
        assertEquals(LatePenaltyUnit.POINTS, LatePenaltyUnit.fromString(null));
        assertEquals(LatePenaltyUnit.POINTS, LatePenaltyUnit.fromString(""));
        assertEquals(LatePenaltyUnit.POINTS, LatePenaltyUnit.fromString("bogus"));
    }

    // 6. never below zero
    @Test
    public void penaltyFloorsAtZero() {
        assertEquals("0", apply("3000", LatePenaltyType.FLAT, "50", lateBy(Duration.ofHours(1)), false));
    }

    // 7. on-time submission untouched
    @Test
    public void submittedBeforeDueDateUnchanged() {
        assertEquals("8550", apply("8550", LatePenaltyType.FLAT, "5", DUE.minus(Duration.ofHours(1)), false));
    }

    // 8. submitted exactly at the due date is NOT late (isAfter semantics,
    // matching the existing late flag in AssignmentServiceImpl)
    @Test
    public void submittedExactlyAtDueDateNotLate() {
        assertEquals("8550", apply("8550", LatePenaltyType.FLAT, "5", DUE, false));
    }

    // 9. per-submission ignore flag suppresses the penalty
    @Test
    public void ignoreFlagSuppressesPenalty() {
        assertEquals("8550", apply("8550", LatePenaltyType.FLAT, "5", lateBy(Duration.ofDays(3)), true));
    }

    // 10-11. no due date / no submitted date means no lateness
    @Test
    public void nullDatesUnchanged() {
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, LatePenaltyUnit.POINTS, "5", MAX, null, lateBy(Duration.ofDays(1)), false));
        assertEquals("8550", LatePenaltyCalculator.applyLatePenalty(
                "8550", 100, LatePenaltyType.FLAT, LatePenaltyUnit.POINTS, "5", MAX, DUE, null, false));
    }

    // 12. ungraded passthrough
    @Test
    public void nullOrBlankGradePassthrough() {
        assertNull(apply(null, LatePenaltyType.FLAT, "5", lateBy(Duration.ofHours(1)), false));
        assertEquals("", apply("", LatePenaltyType.FLAT, "5", lateBy(Duration.ofHours(1)), false));
    }

    // 13. decimal penalty value at grade precision
    @Test
    public void decimalPenaltyValue() {
        assertEquals("8300", apply("8550", LatePenaltyType.FLAT, "2.5", lateBy(Duration.ofHours(1)), false));
    }

    // 14. other scale factors
    @Test
    public void scaleFactorTenFlatPenalty() {
        assertEquals("825", LatePenaltyCalculator.applyLatePenalty(
                "855", 10, LatePenaltyType.FLAT, LatePenaltyUnit.POINTS, "3", 1000, DUE, lateBy(Duration.ofHours(1)), false));
    }

    // 15. defensive: malformed, zero, or negative penalty values apply nothing
    @Test
    public void invalidPenaltyValueUnchanged() {
        Instant submitted = lateBy(Duration.ofHours(1));
        assertEquals("8550", apply("8550", LatePenaltyType.FLAT, "abc", submitted, false));
        assertEquals("8550", apply("8550", LatePenaltyType.FLAT, "0", submitted, false));
        assertEquals("8550", apply("8550", LatePenaltyType.FLAT, "-2", submitted, false));
        assertEquals("8550", apply("8550", LatePenaltyType.FLAT, null, submitted, false));
        assertEquals("8550", apply("8550", null, "5", submitted, false));
    }

    // 15b. defensive: a non-numeric stored grade is left alone
    @Test
    public void nonNumericGradeUnchanged() {
        assertEquals("Pass", apply("Pass", LatePenaltyType.FLAT, "5", lateBy(Duration.ofHours(1)), false));
    }

    // 16. periods standalone: ceiling per interval, boundary counts as that period
    @Test
    public void periodsLateComputation() {
        long day = 24L * 3600L * 1000L;
        long hour = 3600L * 1000L;
        long week = 7L * day;
        assertEquals(0, LatePenaltyCalculator.periodsLate(DUE, DUE.minus(Duration.ofHours(1)), day));
        assertEquals(0, LatePenaltyCalculator.periodsLate(DUE, DUE, day));
        assertEquals(1, LatePenaltyCalculator.periodsLate(DUE, lateBy(Duration.ofMinutes(1)), day));
        assertEquals(1, LatePenaltyCalculator.periodsLate(DUE, lateBy(Duration.ofHours(24)), day));
        assertEquals(2, LatePenaltyCalculator.periodsLate(DUE, lateBy(Duration.ofHours(24).plusSeconds(1)), day));
        assertEquals(4, LatePenaltyCalculator.periodsLate(DUE, lateBy(Duration.ofHours(84)), day));
        assertEquals(1, LatePenaltyCalculator.periodsLate(DUE, lateBy(Duration.ofMinutes(1)), hour));
        assertEquals(2, LatePenaltyCalculator.periodsLate(DUE, lateBy(Duration.ofMinutes(61)), hour));
        assertEquals(1, LatePenaltyCalculator.periodsLate(DUE, lateBy(Duration.ofDays(7)), week));
        assertEquals(2, LatePenaltyCalculator.periodsLate(DUE, lateBy(Duration.ofDays(7).plusSeconds(1)), week));
        assertEquals(0, LatePenaltyCalculator.periodsLate(null, lateBy(Duration.ofHours(1)), day));
        assertEquals(0, LatePenaltyCalculator.periodsLate(DUE, null, day));
    }

    // penalty amount used for the grader "-X late" annotation
    @Test
    public void penaltyScaledForAnnotation() {
        assertEquals(500, LatePenaltyCalculator.penaltyScaled(
                100, LatePenaltyType.FLAT, LatePenaltyUnit.POINTS, "5", MAX, DUE, lateBy(Duration.ofDays(3))));
        assertEquals(400, LatePenaltyCalculator.penaltyScaled(
                100, LatePenaltyType.PER_DAY, LatePenaltyUnit.POINTS, "2", MAX, DUE, lateBy(Duration.ofHours(25))));
        // 10% of max per day, 2 days late = 2000 scaled
        assertEquals(2000, LatePenaltyCalculator.penaltyScaled(
                100, LatePenaltyType.PER_DAY, LatePenaltyUnit.PERCENT, "10", MAX, DUE, lateBy(Duration.ofHours(25))));
        assertEquals(0, LatePenaltyCalculator.penaltyScaled(
                100, LatePenaltyType.PER_DAY, LatePenaltyUnit.POINTS, "2", MAX, DUE, DUE));
        assertEquals(0, LatePenaltyCalculator.penaltyScaled(
                100, LatePenaltyType.FLAT, LatePenaltyUnit.POINTS, "bogus", MAX, DUE, lateBy(Duration.ofHours(1))));
    }
}
