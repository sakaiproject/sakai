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
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;

/**
 * SAK-15574 computes the effective grade for a late submission when the
 * assignment has a late penalty configured (a flat point deduction, or points
 * per day late). All grade arithmetic happens in the scaled integer space in
 * which SCORE_GRADE_TYPE grades are stored (e.g. 85.5 at scale factor 100 is
 * "8550"). The stored submission grade is always the instructor's raw grade;
 * the penalty is applied wherever the grade is displayed or pushed to the
 * gradebook, never persisted.
 */
public final class LatePenaltyCalculator {

    public enum LatePenaltyType {
        FLAT,
        PER_DAY;

        /** @return the type for a stored property value, or null if absent/unrecognized */
        public static LatePenaltyType fromString(String value) {
            if (StringUtils.isBlank(value)) return null;
            try {
                return valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
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
     * @param penaltyValue   penalty in points (unscaled decimal string), from
     *                       {@link AssignmentConstants#LATE_PENALTY_VALUE}
     * @param dueDate        the assignment due date
     * @param dateSubmitted  when the submission was made
     * @param ignorePenalty  the per-submission waiver, from
     *                       {@link AssignmentConstants#IGNORE_LATE_PENALTY}
     * @return the effective scaled grade string
     */
    public static String applyLatePenalty(String rawScaledGrade, int factor, LatePenaltyType type,
            String penaltyValue, Instant dueDate, Instant dateSubmitted, boolean ignorePenalty) {

        if (StringUtils.isBlank(rawScaledGrade) || ignorePenalty) return rawScaledGrade;

        long penalty = penaltyScaled(factor, type, penaltyValue, dueDate, dateSubmitted);
        if (penalty <= 0) return rawScaledGrade;

        long rawGrade;
        try {
            rawGrade = Long.parseLong(rawScaledGrade.trim());
        } catch (NumberFormatException e) {
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

        boolean ignorePenalty = Boolean.parseBoolean(submission.getProperties().get(AssignmentConstants.IGNORE_LATE_PENALTY));
        return applyLatePenalty(rawScaledGrade, factor, type,
                assignment.getProperties().get(AssignmentConstants.LATE_PENALTY_VALUE),
                assignment.getDueDate(), submission.getDateSubmitted(), ignorePenalty);
    }

    /**
     * The penalty in scaled units for a submission (used for the grader's
     * "-X late" annotation), or 0 when no penalty applies. Does not consider
     * the per-submission ignore flag.
     */
    public static long penaltyScaled(int factor, LatePenaltyType type, String penaltyValue,
            Instant dueDate, Instant dateSubmitted) {

        long days = daysLate(dueDate, dateSubmitted);
        if (days == 0 || type == null) return 0;

        BigDecimal points;
        try {
            points = new BigDecimal(StringUtils.trimToEmpty(penaltyValue));
        } catch (NumberFormatException e) {
            return 0;
        }
        if (points.signum() <= 0) return 0;

        long scaledPoints = points.multiply(BigDecimal.valueOf(factor)).setScale(0, RoundingMode.HALF_UP).longValue();
        return type == LatePenaltyType.PER_DAY ? scaledPoints * days : scaledPoints;
    }

    /**
     * Days late as whole 24-hour chunks with any remainder counting as a full
     * day, so one minute late is 1 day and 24h+1s is 2 days. 0 when on time
     * (submitting exactly at the due date is not late, matching the existing
     * late flag) or when either date is missing.
     */
    public static long daysLate(Instant dueDate, Instant dateSubmitted) {
        if (dueDate == null || dateSubmitted == null || !dateSubmitted.isAfter(dueDate)) return 0;
        Duration lateBy = Duration.between(dueDate, dateSubmitted);
        long wholeDays = lateBy.toDays();
        return lateBy.equals(Duration.ofDays(wholeDays)) ? wholeDays : wholeDays + 1;
    }
}
