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

/**
 * SAK-52267 late penalty computation for Tests & Quizzes. Mirrors the
 * semantics of the Assignments calculator
 * (org.sakaiproject.assignment.api.LatePenaltyCalculator, SAK-15574):
 * any part of a day late counts as a full day, results floor at zero, and a
 * per-submission ignore flag waives the deduction. Samigo operates on double
 * point values and java.util.Date, and gates the penalty on the stored isLate
 * flag, which already respects extended-time exceptions at submit time.
 */
public class LatePenaltyCalculatorTest {

    private static final double DELTA = 0.0001;
    private static final long HOUR = 3600 * 1000L;
    private static final Date DUE = new Date(1767625200000L); // fixed instant

    private static Date lateBy(long millis) {
        return new Date(DUE.getTime() + millis);
    }

    // type parsing from stored metadata
    @Test
    public void fromStringParsesTypes() {
        assertEquals(LatePenaltyType.FLAT, LatePenaltyType.fromString("FLAT"));
        assertEquals(LatePenaltyType.PER_DAY, LatePenaltyType.fromString("PER_DAY"));
        assertEquals(LatePenaltyType.FLAT, LatePenaltyType.fromString(" flat "));
        assertNull(LatePenaltyType.fromString(null));
        assertNull(LatePenaltyType.fromString(""));
        assertNull(LatePenaltyType.fromString("bogus"));
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

    // days late: ceiling of 24h chunks, 0 when on time
    @Test
    public void daysLateComputation() {
        assertEquals(0, LatePenaltyCalculator.daysLate(DUE, new Date(DUE.getTime() - HOUR)));
        assertEquals(0, LatePenaltyCalculator.daysLate(DUE, DUE)); // exactly at due = not late
        assertEquals(1, LatePenaltyCalculator.daysLate(DUE, lateBy(60 * 1000L))); // 1 minute
        assertEquals(1, LatePenaltyCalculator.daysLate(DUE, lateBy(24 * HOUR))); // exactly 24h
        assertEquals(2, LatePenaltyCalculator.daysLate(DUE, lateBy(24 * HOUR + 1000L))); // 24h + 1s
        assertEquals(4, LatePenaltyCalculator.daysLate(DUE, lateBy(84 * HOUR))); // 3.5 days -> 4
        assertEquals(0, LatePenaltyCalculator.daysLate(null, lateBy(HOUR)));
        assertEquals(0, LatePenaltyCalculator.daysLate(DUE, null));
    }

    // flat penalty applies once, however late
    @Test
    public void flatPenalty() {
        assertEquals(5.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.FLAT, 5.0, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
        assertEquals(5.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.FLAT, 5.0, Boolean.TRUE, DUE, lateBy(90 * HOUR)), DELTA);
    }

    // per-day penalty multiplies by whole days, partial day counts as full
    @Test
    public void perDayPenalty() {
        assertEquals(2.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.PER_DAY, 2.0, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
        assertEquals(4.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.PER_DAY, 2.0, Boolean.TRUE, DUE, lateBy(30 * HOUR)), DELTA);
        assertEquals(8.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.PER_DAY, 2.0, Boolean.TRUE, DUE, lateBy(84 * HOUR)), DELTA);
    }

    // autosubmit edge (SAM-1088): isLate TRUE but the recorded submittedDate is
    // the student's last save, which can be at or before the due date -> minimum one day
    @Test
    public void perDayPenaltyLateFlagWithOnTimeDatesChargesOneDay() {
        assertEquals(2.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.PER_DAY, 2.0, Boolean.TRUE, DUE, new Date(DUE.getTime() - HOUR)), DELTA);
        assertEquals(2.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.PER_DAY, 2.0, Boolean.TRUE, DUE, null), DELTA);
    }

    // the stored isLate flag gates everything: no penalty for on-time work
    @Test
    public void notLateNoPenaltyRegardlessOfDates() {
        assertEquals(0.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.FLAT, 5.0, Boolean.FALSE, DUE, lateBy(30 * HOUR)), DELTA);
        assertEquals(0.0, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.FLAT, 5.0, null, DUE, lateBy(30 * HOUR)), DELTA);
    }

    // defensive: missing or invalid configuration yields no penalty
    @Test
    public void invalidConfigurationNoPenalty() {
        Date submitted = lateBy(HOUR);
        assertEquals(0.0, LatePenaltyCalculator.penaltyPoints(null, 5.0, Boolean.TRUE, DUE, submitted), DELTA);
        assertEquals(0.0, LatePenaltyCalculator.penaltyPoints(LatePenaltyType.FLAT, null, Boolean.TRUE, DUE, submitted), DELTA);
        assertEquals(0.0, LatePenaltyCalculator.penaltyPoints(LatePenaltyType.FLAT, 0.0, Boolean.TRUE, DUE, submitted), DELTA);
        assertEquals(0.0, LatePenaltyCalculator.penaltyPoints(LatePenaltyType.FLAT, -2.0, Boolean.TRUE, DUE, submitted), DELTA);
        assertEquals(0.0, LatePenaltyCalculator.penaltyPoints(LatePenaltyType.FLAT, Double.NaN, Boolean.TRUE, DUE, submitted), DELTA);
    }

    // decimal penalty values are honored
    @Test
    public void decimalPenaltyValue() {
        assertEquals(2.5, LatePenaltyCalculator.penaltyPoints(
                LatePenaltyType.FLAT, 2.5, Boolean.TRUE, DUE, lateBy(HOUR)), DELTA);
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
