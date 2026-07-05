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
 * keep the two in sync: a flat deduction or a per-day deduction where any part
 * of a day late counts as a full day, the result floors at zero, and a
 * per-submission ignore flag waives the deduction.
 *
 * Samigo differences: scores are double point values, lateness is gated on the
 * submission's stored isLate flag (which already honors extended-time
 * exceptions at submit time, SAM-3319), and days late are measured from the
 * student's effective due date. When isLate is set but the recorded
 * submittedDate is not after the due date (an autosubmitted attempt keeps the
 * student's last-save time, SAM-1088), a minimum of one day is charged.
 */
@Slf4j
public final class LatePenaltyCalculator {

    private static final long DAY_MILLIS = 24L * 3600L * 1000L;

    public enum LatePenaltyType {
        FLAT,
        PER_DAY;

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

    private LatePenaltyCalculator() {
    }

    /**
     * Parses a penalty point value as entered in settings or stored in
     * metadata, accepting either decimal separator. Both the settings
     * validation and scoring-time parsing use this, so they cannot drift.
     *
     * @return the positive point value, or null when blank, malformed, NaN,
     *         zero or negative
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
     * Days late as whole 24-hour chunks with any remainder counting as a full
     * day, so one minute late is 1 day and 24h+1s is 2 days. 0 when the
     * submission is not after the due date or either date is missing.
     */
    public static long daysLate(Date effectiveDueDate, Date submittedDate) {
        if (effectiveDueDate == null || submittedDate == null || !submittedDate.after(effectiveDueDate)) return 0;
        long lateMillis = submittedDate.getTime() - effectiveDueDate.getTime();
        return (lateMillis + DAY_MILLIS - 1) / DAY_MILLIS;
    }

    /**
     * The penalty in points for a submission, or 0 when none applies. Gated on
     * the stored isLate flag; a late submission is charged a minimum of one
     * day in PER_DAY mode even when the recorded dates disagree (autosubmit).
     *
     * @param type         penalty mode from assessment metadata, null = none
     * @param value        penalty points per application, null/NaN/&le;0 = none
     * @param isLate       the submission's stored late flag
     * @param effectiveDueDate the student's effective due date (extended time aware)
     * @param submittedDate    when the submission was made
     */
    public static double penaltyPoints(LatePenaltyType type, Double value, Boolean isLate,
            Date effectiveDueDate, Date submittedDate) {

        if (!Boolean.TRUE.equals(isLate) || type == null) return 0d;
        if (value == null || value.isNaN() || value <= 0d) {
            if (value != null && !value.isNaN() && value < 0d) {
                log.debug("Ignoring negative late penalty value [{}]", value);
            }
            return 0d;
        }
        if (type == LatePenaltyType.FLAT) return value;

        long days = Math.max(1, daysLate(effectiveDueDate, submittedDate));
        return value * days;
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
