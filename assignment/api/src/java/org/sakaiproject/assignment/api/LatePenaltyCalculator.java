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
package org.sakaiproject.assignment.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;

import lombok.extern.slf4j.Slf4j;

/**
 * SAK-15574 computes the effective grade for a late submission when the
 * assignment has a late penalty configured: a deduction in fixed points or a
 * percentage of the maximum grade, applied once (flat) or per hour/day/week
 * late where any part of a period counts as a full period. All grade
 * arithmetic happens in the scaled integer space in which SCORE_GRADE_TYPE
 * grades are stored (e.g. 85.5 at scale factor 100 is "8550"). The stored
 * submission grade is always the instructor's raw grade; the penalty is
 * applied wherever the grade is displayed or pushed to the gradebook, never
 * persisted. The Tests & Quizzes twin
 * ({@code org.sakaiproject.tool.assessment.util.LatePenaltyCalculator},
 * SAK-52267) mirrors these semantics — keep the two in sync.
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

        /** @return the type for a stored property value, or null if absent/unrecognized */
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
     * assignment's maximum grade per application.
     */
    public enum LatePenaltyUnit {
        POINTS,
        PERCENT;

        /**
         * @return the unit for a stored property value; absent or unrecognized
         *         means POINTS so properties saved before units existed keep
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
     * Applies the configured late penalty to a raw scaled grade, flooring at zero.
     * Returns the grade unchanged when the penalty does not apply: on-time or
     * unsubmitted work, the ignore flag, a missing/invalid configuration, or a
     * non-numeric grade.
     *
     * @param rawScaledGrade the stored grade in scaled units (e.g. "8550")
     * @param factor         the assignment scale factor (e.g. 100)
     * @param type           penalty mode, from {@link AssignmentConstants#LATE_PENALTY_TYPE}
     * @param unit           POINTS or PERCENT of maxGradePoint, from
     *                       {@link AssignmentConstants#LATE_PENALTY_UNIT}; null = POINTS
     * @param penaltyValue   points or percent per application (unscaled decimal
     *                       string), from {@link AssignmentConstants#LATE_PENALTY_VALUE}
     * @param maxGradePoint  the assignment's maximum grade in scaled units,
     *                       needed for PERCENT; missing/non-positive yields no penalty
     * @param dueDate        the assignment due date
     * @param dateSubmitted  when the submission was made
     * @param ignorePenalty  the per-submission waiver, from
     *                       {@link AssignmentConstants#IGNORE_LATE_PENALTY}
     * @return the effective scaled grade string
     */
    public static String applyLatePenalty(String rawScaledGrade, int factor, LatePenaltyType type,
            LatePenaltyUnit unit, String penaltyValue, Integer maxGradePoint,
            Instant dueDate, Instant dateSubmitted, boolean ignorePenalty) {

        if (StringUtils.isBlank(rawScaledGrade) || ignorePenalty) return rawScaledGrade;

        long penalty = penaltyScaled(factor, type, unit, penaltyValue, maxGradePoint, dueDate, dateSubmitted);
        if (penalty <= 0) return rawScaledGrade;

        long rawGrade;
        try {
            rawGrade = Long.parseLong(rawScaledGrade.trim());
        } catch (NumberFormatException e) {
            log.debug("Not applying late penalty to non-numeric grade [{}], {}", rawScaledGrade, e.toString());
            return rawScaledGrade;
        }

        return Long.toString(Math.max(0, rawGrade - penalty));
    }

    /**
     * Convenience form reading the penalty configuration and waiver from the
     * assignment and submission: applies the assignment's late penalty to a
     * raw scaled grade, returning it unchanged for non-points assignments or
     * when no penalty is configured or applicable.
     *
     * @param rawScaledGrade the grade to penalize, in scaled units (callers
     *                       pass the submission grade or a per-submitter
     *                       override)
     * @param factor         the resolved scale factor (callers default a null
     *                       assignment scale factor themselves)
     */
    public static String effectiveScaledGrade(Assignment assignment, AssignmentSubmission submission,
            String rawScaledGrade, int factor) {

        if (assignment == null || submission == null
                || assignment.getTypeOfGrade() != Assignment.GradeType.SCORE_GRADE_TYPE) {
            return rawScaledGrade;
        }

        LatePenaltyType type = LatePenaltyType.fromString(assignment.getProperties().get(AssignmentConstants.LATE_PENALTY_TYPE));
        if (type == null) return rawScaledGrade;

        LatePenaltyUnit unit = LatePenaltyUnit.fromString(assignment.getProperties().get(AssignmentConstants.LATE_PENALTY_UNIT));
        boolean ignorePenalty = Boolean.parseBoolean(submission.getProperties().get(AssignmentConstants.IGNORE_LATE_PENALTY));
        return applyLatePenalty(rawScaledGrade, factor, type, unit,
                assignment.getProperties().get(AssignmentConstants.LATE_PENALTY_VALUE),
                assignment.getMaxGradePoint(),
                assignment.getDueDate(), submission.getDateSubmitted(), ignorePenalty);
    }

    /**
     * The penalty in scaled units for a submission (used for the grader's
     * "-X late" annotation), or 0 when no penalty applies. Does not consider
     * the per-submission ignore flag.
     */
    public static long penaltyScaled(int factor, LatePenaltyType type, LatePenaltyUnit unit,
            String penaltyValue, Integer maxGradePoint, Instant dueDate, Instant dateSubmitted) {

        if (type == null || !isLate(dueDate, dateSubmitted)) return 0;

        BigDecimal value;
        try {
            value = new BigDecimal(StringUtils.trimToEmpty(penaltyValue));
        } catch (NumberFormatException e) {
            log.debug("Ignoring malformed late penalty value [{}], {}", penaltyValue, e.toString());
            return 0;
        }
        if (value.signum() <= 0) return 0;

        long scaledBase;
        if (unit == LatePenaltyUnit.PERCENT) {
            if (maxGradePoint == null || maxGradePoint <= 0) {
                log.debug("Percent late penalty with no usable max grade [{}], applying nothing", maxGradePoint);
                return 0;
            }
            scaledBase = value.multiply(BigDecimal.valueOf(maxGradePoint))
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).longValue();
        } else {
            scaledBase = value.multiply(BigDecimal.valueOf(factor)).setScale(0, RoundingMode.HALF_UP).longValue();
        }

        long periods = type == LatePenaltyType.FLAT ? 1
                : Math.max(1, periodsLate(dueDate, dateSubmitted, type.getPeriodMillis()));
        return scaledBase * periods;
    }

    /**
     * Whole periods late with any remainder counting as a full period, so one
     * minute late is 1 hour/day/week and exactly one period plus a second is
     * 2. 0 when on time (submitting exactly at the due date is not late,
     * matching the existing late flag) or when either date is missing.
     */
    public static long periodsLate(Instant dueDate, Instant dateSubmitted, long periodMillis) {
        if (periodMillis <= 0 || !isLate(dueDate, dateSubmitted)) return 0;
        long lateMillis = dateSubmitted.toEpochMilli() - dueDate.toEpochMilli();
        return (lateMillis + periodMillis - 1) / periodMillis;
    }

    private static boolean isLate(Instant dueDate, Instant dateSubmitted) {
        return dueDate != null && dateSubmitted != null && dateSubmitted.isAfter(dueDate);
    }
}
