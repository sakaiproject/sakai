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

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * SAK-52267 computes the automatic late penalty for Tests & Quizzes
 * submissions. The semantics intentionally mirror the Assignments calculator
 * ({@code org.sakaiproject.assignment.api.LatePenaltyCalculator}, SAK-15574) —
 * keep the two in sync: a deduction in fixed points or a percentage of the
 * maximum score, applied once (flat) or per hour/day/week late where any part
 * of a period counts as a full period, the result floors at zero, and a
 * per-submission ignore flag waives the deduction.
 *
 * Samigo differences: scores are double point values, lateness is gated on the
 * submission's stored isLate flag (which already honors extended-time
 * exceptions at submit time, SAM-3319), and periods late are measured from the
 * student's effective due date. When isLate is set but the recorded
 * submittedDate is not after the due date (an autosubmitted attempt keeps the
 * student's last-save time, SAM-1088), a minimum of one period is charged.
 */
@Slf4j
public final class LatePenaltyCalculator {

    private static final long HOUR_MILLIS = 3600L * 1000L;
    private static final long DAY_MILLIS = 24L * HOUR_MILLIS;
    private static final long WEEK_MILLIS = 7L * DAY_MILLIS;

    public enum LatePenaltyType {
        FLAT(0L),
        PER_HOUR(HOUR_MILLIS),
        PER_DAY(DAY_MILLIS),
        PER_WEEK(WEEK_MILLIS);

        private final long periodMillis;

        LatePenaltyType(long periodMillis) {
            this.periodMillis = periodMillis;
        }

        public long getPeriodMillis() {
            return periodMillis;
        }

        /** @return the type for a stored metadata value, or null when absent/unrecognized */
        public static LatePenaltyType fromString(String value) {
            if (StringUtils.isBlank(value)) return null;
            try {
                return valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.debug("Unrecognized late penalty type [{}], treating as no penalty", value);
                return null;
            }
        }
    }

    /**
     * What the penalty value means: fixed points, or a percentage of the
     * assessment's maximum score per application.
     */
    public enum LatePenaltyUnit {
        POINTS,
        PERCENT;

        /**
         * @return the unit for a stored metadata value; absent or unrecognized
         *         means POINTS so settings saved before units existed keep
         *         their behavior
         */
        public static LatePenaltyUnit fromString(String value) {
            if (StringUtils.isBlank(value)) return POINTS;
            try {
                return valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.debug("Unrecognized late penalty unit [{}], treating as POINTS", value);
                return POINTS;
            }
        }
    }

    private LatePenaltyCalculator() {
    }

    /**
     * Parses a penalty value as entered in settings or stored in metadata,
     * accepting either decimal separator. Both the settings validation and
     * scoring-time parsing use this, so they cannot drift.
     *
     * @return the positive value, or null when blank, malformed, NaN, zero or
     *         negative
     */
    public static Double parsePenaltyValue(String raw) {
        if (StringUtils.isBlank(raw)) return null;
        try {
            double value = Double.parseDouble(raw.trim().replace(",", "."));
            if (Double.isNaN(value) || value <= 0d) {
                log.debug("Rejecting non-positive late penalty value [{}]", raw);
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            log.debug("Rejecting malformed late penalty value [{}], {}", raw, e.toString());
            return null;
        }
    }

    /**
     * Whole periods late with any remainder counting as a full period, so one
     * minute late is 1 hour/day/week and exactly one period plus a second is
     * 2. 0 when the submission is not after the due date or either date is
     * missing.
     */
    public static long periodsLate(Date effectiveDueDate, Date submittedDate, long periodMillis) {
        if (effectiveDueDate == null || submittedDate == null || periodMillis <= 0
                || !submittedDate.after(effectiveDueDate)) return 0;
        long lateMillis = submittedDate.getTime() - effectiveDueDate.getTime();
        return (lateMillis + periodMillis - 1) / periodMillis;
    }

    /**
     * The penalty in points for a submission, or 0 when none applies. Gated on
     * the stored isLate flag; a late submission is charged a minimum of one
     * period in the interval modes even when the recorded dates disagree
     * (autosubmit).
     *
     * @param type          penalty mode from assessment metadata, null = none
     * @param unit          POINTS or PERCENT of maxPoints; null = POINTS
     * @param value         points or percent per application, null/NaN/&le;0 = none
     * @param maxPoints     the assessment's maximum score, needed for PERCENT;
     *                      a missing/non-positive maximum yields no penalty
     * @param isLate        the submission's stored late flag
     * @param effectiveDueDate the student's effective due date (extended time aware)
     * @param submittedDate    when the submission was made
     */
    public static double penaltyPoints(LatePenaltyType type, LatePenaltyUnit unit, Double value,
            Double maxPoints, Boolean isLate, Date effectiveDueDate, Date submittedDate) {

        if (!Boolean.TRUE.equals(isLate) || type == null) return 0d;
        if (value == null || value.isNaN() || value <= 0d) {
            if (value != null && !value.isNaN() && value < 0d) {
                log.debug("Ignoring negative late penalty value [{}]", value);
            }
            return 0d;
        }

        double base;
        if (unit == LatePenaltyUnit.PERCENT) {
            if (maxPoints == null || maxPoints.isNaN() || maxPoints <= 0d) {
                log.debug("Percent late penalty with no usable maximum score [{}], applying nothing", maxPoints);
                return 0d;
            }
            base = value / 100d * maxPoints;
        } else {
            base = value;
        }

        if (type == LatePenaltyType.FLAT) return base;

        long periods = Math.max(1, periodsLate(effectiveDueDate, submittedDate, type.getPeriodMillis()));
        return base * periods;
    }

    /**
     * Applies a penalty to a raw final score, flooring at zero; the score is
     * returned unchanged when the per-submission ignore flag is set or no
     * penalty applies.
     */
    public static double applyPenalty(double rawFinalScore, double penalty, Boolean ignoreLatePenalty) {
        if (Boolean.TRUE.equals(ignoreLatePenalty) || penalty <= 0d) return rawFinalScore;
        return Math.max(0d, rawFinalScore - penalty);
    }
}
