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
package org.sakaiproject.tool.assessment.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;
import org.sakaiproject.tool.assessment.util.LatePenaltyCalculator.LatePenaltyType;
import org.sakaiproject.tool.assessment.util.LatePenaltyCalculator.LatePenaltyUnit;

/**
 * SAK-52267 late penalty computation for Tests & Quizzes. Mirrors the
 * semantics of the Assignments calculator
 * (org.sakaiproject.assignment.api.LatePenaltyCalculator, SAK-15574):
 * a deduction in points or percent of the maximum score, applied once (flat)
 * or per hour/day/week late with any partial period counting as a full one,
 * results floored at zero, gated on the stored isLate flag, with a
 * per-submission ignore flag that waives the deduction.
 */
public class LatePenaltyCalculatorTest {

    private static final double DELTA = 0.0001;
    private static final long MINUTE = 60 * 1000L;
    private static final long HOUR = 3600 * 1000L;
    private static final long DAY = 24 * HOUR;
    private static final long WEEK = 7 * DAY;
    private static final Date DUE = new Date(1767625200000L); // fixed instant
    private static final Double MAX = 50.0;

    private static Date lateBy(long millis) {
        return new Date(DUE.getTime() + millis);
    }

    private static double points(LatePenaltyType type, Double value, Boolean isLate, Date due, Date submitted) {
        return LatePenaltyCalculator.penaltyPoints(type, LatePenaltyUnit.POINTS, value, null, isLate, due, submitted);
    }

    // type/unit parsing from stored metadata
    @Test
    public void fromStringParsesTypes() {
        assertEquals(LatePenaltyType.FLAT, LatePenaltyType.fromString("FLAT"));
        assertEquals(LatePenaltyType.PER_HOUR, LatePenaltyType.fromString("PER_HOUR"));
        assertEquals(LatePenaltyType.PER_DAY, LatePenaltyType.fromString("PER_DAY"));
        assertEquals(LatePenaltyType.PER_WEEK, LatePenaltyType.fromString("PER_WEEK"));
        assertEquals(LatePenaltyType.FLAT, LatePenaltyType.fromString(" flat "));
        assertNull(LatePenaltyType.fromString(null));
        assertNull(LatePenaltyType.fromString(""));
        assertNull(LatePenaltyType.fromString("bogus"));
    }

    @Test
    public void unitFromStringDefaultsToPoints() {
        assertEquals(LatePenaltyUnit.POINTS, LatePenaltyUnit.fromString("POINTS"));
        assertEquals(LatePenaltyUnit.PERCENT, LatePenaltyUnit.fromString("PERCENT"));
        assertEquals(LatePenaltyUnit.PERCENT, LatePenaltyUnit.fromString(" percent "));
        // absent or unrecognized means POINTS so pre-existing settings keep working
        assertEquals(LatePenaltyUnit.POINTS, LatePenaltyUnit.fromString(null));
        assertEquals(LatePenaltyUnit.POINTS, LatePenaltyUnit.fromString(""));
        assertEquals(LatePenaltyUnit.POINTS, LatePenaltyUnit.fromString("bogus"));
    }

    // shared parser behind both settings validation and scoring-time reads
    @Test
    public void parsePenaltyValueAcceptsPositiveNumbersEitherSeparator() {
        assertEquals(Double.valueOf(5.0), LatePenaltyCalculator.parsePenaltyValue("5"));
        assertEquals(Double.valueOf(2.5), LatePenaltyCalculator.parsePenaltyValue("2.5"));
        assertEquals(Double.valueOf(2.5), LatePenaltyCalculator.parsePenaltyValue("2,5"));
        assertEquals(Double.valueOf(1.0), LatePenaltyCalculator.parsePenaltyValue(" 1 "));
    }

    @Test
    public void parsePenaltyValueRejectsInvalidInput() {
        assertNull(LatePenaltyCalculator.parsePenaltyValue(null));
        assertNull(LatePenaltyCalculator.parsePenaltyValue(""));
        assertNull(LatePenaltyCalculator.parsePenaltyValue("abc"));
        assertNull(LatePenaltyCalculator.parsePenaltyValue("0"));
        assertNull(LatePenaltyCalculator.parsePenaltyValue("-2"));
        assertNull(LatePenaltyCalculator.parsePenaltyValue("NaN"));
    }

    // periods late: ceiling of whole periods, 0 when on time, boundary counts as that period
    @Test
    public void periodsLateComputation() {
        assertEquals(0, LatePenaltyCalculator.periodsLate(DUE, new Date(DUE.getTime() - HOUR), DAY));
        assertEquals(0, LatePenaltyCalculator.periodsLate(DUE, DUE, DAY)); // exactly at due = not late
        assertEquals(1, LatePenaltyCalculator.periodsLate(DUE, lateBy(MINUTE), DAY));
        assertEquals(1, LatePenaltyCalculator.periodsLate(DUE, lateBy(DAY), DAY)); // exactly 24h = 1 day
        assertEquals(2, LatePenaltyCalculator.periodsLate(DUE, lateBy(DAY + 1000L), DAY));
        assertEquals(4, LatePenaltyCalculator.periodsLate(DUE, lateBy(84 * HOUR), DAY)); // 3.5 days -> 4
        // hours
        assertEquals(1, LatePenaltyCalculator.periodsLate(DUE, lateBy(MINUTE), HOUR));
        assertEquals(1, LatePenaltyCalculator.periodsLate(DUE, lateBy(HOUR), HOUR)); // exactly 60m = 1
        assertEquals(2, LatePenaltyCalculator.periodsLate(DUE, lateBy(HOUR + 1000L), HOUR));
        assertEquals(25, LatePenaltyCalculator.periodsLate(DUE, lateBy(DAY + HOUR), HOUR));
        // weeks
        assertEquals(1, LatePenaltyCalculator.periodsLate(DUE, lateBy(HOUR), WEEK));
        assertEquals(1, LatePenaltyCalculator.periodsLate(DUE, lateBy(WEEK), WEEK)); // exactly 7d = 1
        assertEquals(2, LatePenaltyCalculator.periodsLate(DUE, lateBy(WEEK + 1000L), WEEK));
        // nulls
        assertEquals(0, LatePenaltyCalculator.periodsLate(null, lateBy(HOUR), DAY));
        assertEquals(0, LatePenaltyCalculator.periodsLate(DUE, null, DAY));
    }

    // flat penalty applies once, however late
    @Test
    public void flatPenalty() {
        assertEquals(5.0, points(LatePenaltyType.FLAT, 5.0, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
        assertEquals(5.0, points(LatePenaltyType.FLAT, 5.0, Boolean.TRUE, DUE, lateBy(90 * HOUR)), DELTA);
    }

    // per-interval penalties multiply by whole periods, partial counts as full
    @Test
    public void perHourPenalty() {
        assertEquals(1.5, points(LatePenaltyType.PER_HOUR, 1.5, Boolean.TRUE, DUE, lateBy(MINUTE)), DELTA);
        assertEquals(1.5, points(LatePenaltyType.PER_HOUR, 1.5, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
        assertEquals(3.0, points(LatePenaltyType.PER_HOUR, 1.5, Boolean.TRUE, DUE, lateBy(HOUR + 1000L)), DELTA);
    }

    @Test
    public void perDayPenalty() {
        assertEquals(2.0, points(LatePenaltyType.PER_DAY, 2.0, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
        assertEquals(4.0, points(LatePenaltyType.PER_DAY, 2.0, Boolean.TRUE, DUE, lateBy(30 * HOUR)), DELTA);
        assertEquals(8.0, points(LatePenaltyType.PER_DAY, 2.0, Boolean.TRUE, DUE, lateBy(84 * HOUR)), DELTA);
    }

    @Test
    public void perWeekPenalty() {
        assertEquals(3.0, points(LatePenaltyType.PER_WEEK, 3.0, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
        assertEquals(3.0, points(LatePenaltyType.PER_WEEK, 3.0, Boolean.TRUE, DUE, lateBy(WEEK)), DELTA);
        assertEquals(6.0, points(LatePenaltyType.PER_WEEK, 3.0, Boolean.TRUE, DUE, lateBy(WEEK + DAY)), DELTA);
    }

    // percent deducts a share of the maximum score per application (Canvas model)
    @Test
    public void percentOfMaxFlat() {
        assertEquals(5.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.FLAT, LatePenaltyUnit.PERCENT, 10.0, MAX, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
        // decimals: 2.5% of 40
        assertEquals(1.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.FLAT, LatePenaltyUnit.PERCENT, 2.5, 40.0, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
    }

    @Test
    public void percentOfMaxPerInterval() {
        // 10% of 50 per day, 30h late = 2 days -> 10 points
        assertEquals(10.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.PER_DAY, LatePenaltyUnit.PERCENT, 10.0, MAX, Boolean.TRUE, DUE, lateBy(30 * HOUR)), DELTA);
        // 1% of 50 per hour, 25h late = 25 hours -> 12.5 points
        assertEquals(12.5, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.PER_HOUR, LatePenaltyUnit.PERCENT, 1.0, MAX, Boolean.TRUE, DUE, lateBy(DAY + HOUR)), DELTA);
        // 20% of 50 per week, 8 days late = 2 weeks -> 20 points
        assertEquals(20.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.PER_WEEK, LatePenaltyUnit.PERCENT, 20.0, MAX, Boolean.TRUE, DUE, lateBy(8 * DAY)), DELTA);
    }

    // percent without a usable maximum yields no penalty rather than a guess
    @Test
    public void percentWithMissingMaxNoPenalty() {
        assertEquals(0.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.FLAT, LatePenaltyUnit.PERCENT, 10.0, null, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
        assertEquals(0.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.FLAT, LatePenaltyUnit.PERCENT, 10.0, 0.0, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
        assertEquals(0.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.FLAT, LatePenaltyUnit.PERCENT, 10.0, -5.0, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
    }

    // a null unit behaves as POINTS (pre-existing stored settings)
    @Test
    public void nullUnitBehavesAsPoints() {
        assertEquals(5.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.FLAT, null, 5.0, MAX, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
    }

    // autosubmit edge (SAM-1088): isLate TRUE but the recorded submittedDate is
    // the student's last save, which can be at or before the due date -> minimum one period
    @Test
    public void intervalPenaltyLateFlagWithOnTimeDatesChargesOnePeriod() {
        assertEquals(2.0, points(LatePenaltyType.PER_DAY, 2.0, Boolean.TRUE, DUE, new Date(DUE.getTime() - HOUR)), DELTA);
        assertEquals(2.0, points(LatePenaltyType.PER_DAY, 2.0, Boolean.TRUE, DUE, null), DELTA);
        assertEquals(1.5, points(LatePenaltyType.PER_HOUR, 1.5, Boolean.TRUE, DUE, null), DELTA);
        assertEquals(3.0, points(LatePenaltyType.PER_WEEK, 3.0, Boolean.TRUE, DUE, null), DELTA);
    }

    // the stored isLate flag gates everything: no penalty for on-time work
    @Test
    public void notLateNoPenaltyRegardlessOfDates() {
        assertEquals(0.0, points(LatePenaltyType.FLAT, 5.0, Boolean.FALSE, DUE, lateBy(30 * HOUR)), DELTA);
        assertEquals(0.0, points(LatePenaltyType.FLAT, 5.0, null, DUE, lateBy(30 * HOUR)), DELTA);
    }

    // defensive: missing or invalid configuration yields no penalty
    @Test
    public void invalidConfigurationNoPenalty() {
        Date submitted = lateBy(HOUR);
        assertEquals(0.0, points(null, 5.0, Boolean.TRUE, DUE, submitted), DELTA);
        assertEquals(0.0, points(LatePenaltyType.FLAT, null, Boolean.TRUE, DUE, submitted), DELTA);
        assertEquals(0.0, points(LatePenaltyType.FLAT, 0.0, Boolean.TRUE, DUE, submitted), DELTA);
        assertEquals(0.0, points(LatePenaltyType.FLAT, -2.0, Boolean.TRUE, DUE, submitted), DELTA);
        assertEquals(0.0, points(LatePenaltyType.FLAT, Double.NaN, Boolean.TRUE, DUE, submitted), DELTA);
    }

    // decimal penalty values are honored
    @Test
    public void decimalPenaltyValue() {
        assertEquals(2.5, points(LatePenaltyType.FLAT, 2.5, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
    }

    // applying the penalty floors at zero and honors the ignore waiver
    @Test
    public void applyPenaltyFloorsAndHonorsIgnore() {
        assertEquals(85.0, LatePenaltyCalculator.applyPenalty(90.0, 5.0, null), DELTA);
        assertEquals(85.0, LatePenaltyCalculator.applyPenalty(90.0, 5.0, Boolean.FALSE), DELTA);
        assertEquals(0.0, LatePenaltyCalculator.applyPenalty(3.0, 50.0, null), DELTA);
        assertEquals(90.0, LatePenaltyCalculator.applyPenalty(90.0, 5.0, Boolean.TRUE), DELTA);
        assertEquals(90.0, LatePenaltyCalculator.applyPenalty(90.0, 0.0, null), DELTA);
    }
}
