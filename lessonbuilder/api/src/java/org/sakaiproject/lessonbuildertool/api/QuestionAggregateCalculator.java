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
package org.sakaiproject.lessonbuildertool.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponse;

/**
 * Pure scoring logic for the shared "inline question aggregate" gradebook item:
 * one site-wide gradebook column where every member question contributes an equal
 * share, scored by participation (answered at all) or correctness. Each response
 * contributes 0..1 of its question's share; shares are then worth a configurable
 * number of points each, or an equal percentage of a fixed item total.
 *
 * Kept free of Spring/DAO/RSF dependencies so it can be unit tested directly.
 */
public class QuestionAggregateCalculator {

    public static final String MODE_PARTICIPATION = "participation";
    public static final String MODE_CORRECTNESS = "correctness";

    /** correctness handling of short-answer questions: any non-blank answer earns the full share */
    public static final String ESSAY_ANY_ANSWER = "anyanswer";
    /** correctness handling of short-answer questions: a manually graded question's
     *  awarded points carry over as a fraction of its share (partial credit) */
    public static final String ESSAY_CARRY_GRADE = "carry";

    private QuestionAggregateCalculator() {}

    /**
     * A response counts as "answered" when the student actually submitted something:
     * multiple choice needs a chosen answer id (real ids are always &gt;= 1),
     * short answer needs non-blank text. Unknown question types never count.
     */
    public static boolean isAnswered(SimplePageItem question, SimplePageQuestionResponse response) {
        if (question == null || response == null) {
            return false;
        }
        String questionType = question.getAttribute("questionType");
        if ("multipleChoice".equals(questionType)) {
            return response.getMultipleChoiceId() != 0;
        }
        if ("shortanswer".equals(questionType)) {
            String answer = response.getShortanswer();
            return answer != null && !answer.trim().isEmpty();
        }
        return false;
    }

    /**
     * How much of its question's share a response earns, 0..1.
     *
     * Participation mode: any answered question earns the full share. Correctness
     * mode: multiple choice follows the stored correct flag (which already reflects
     * instructor overrides); short answer follows {@code essayMode} — either any
     * answer earns the full share, or a manually graded question's awarded points
     * carry over proportionally ({@code points / question max}, clamped to 0..1;
     * ungraded-so-far responses earn nothing). Short answers that are not manually
     * graded (auto-checked against expected answers, or open polls) fall back to
     * the stored correct flag either way.
     */
    public static double contribution(SimplePageItem question, SimplePageQuestionResponse response,
            String mode, String essayMode) {
        if (!isAnswered(question, response)) {
            return 0;
        }
        if (!MODE_CORRECTNESS.equals(mode)) {
            return 1;
        }
        if ("shortanswer".equals(question.getAttribute("questionType"))) {
            if (!ESSAY_CARRY_GRADE.equals(essayMode)) {
                return 1;
            }
            boolean manuallyGraded = "true".equals(question.getAttribute("questionGraded"));
            Integer questionMax = question.getGradebookPoints();
            if (manuallyGraded && questionMax != null && questionMax > 0) {
                Double points = response.getPoints();
                if (points == null) {
                    return 0;
                }
                return Math.max(0, Math.min(1, points / questionMax));
            }
        }
        return response.isCorrect() ? 1 : 0;
    }

    /**
     * Sum every student's share contributions across the member questions.
     *
     * @param membersWithResponses each member question mapped to its stored responses
     * @param mode {@link #MODE_PARTICIPATION} or {@link #MODE_CORRECTNESS}
     * @param essayMode {@link #ESSAY_ANY_ANSWER} or {@link #ESSAY_CARRY_GRADE}
     * @param allResponderIds every user who has responded to ANY question in the site,
     *        member or not; each is seeded with an explicit 0 so scores of students
     *        whose questions left the aggregate are cleared rather than left stale
     * @return userId -&gt; total shares earned (0 .. member count)
     */
    public static Map<String, Double> computeContributions(
            Map<SimplePageItem, List<SimplePageQuestionResponse>> membersWithResponses,
            String mode, String essayMode, Set<String> allResponderIds) {
        Map<String, Double> totals = new HashMap<>();
        if (allResponderIds != null) {
            allResponderIds.forEach(userId -> totals.put(userId, 0.0));
        }
        if (membersWithResponses != null) {
            for (Map.Entry<SimplePageItem, List<SimplePageQuestionResponse>> entry : membersWithResponses.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                // one response row per user per question is the DB invariant, but
                // guard against duplicates so a bad list can't double-count
                Set<String> counted = new HashSet<>();
                for (SimplePageQuestionResponse response : entry.getValue()) {
                    if (response == null || response.getUserId() == null || !counted.add(response.getUserId())) {
                        continue;
                    }
                    totals.merge(response.getUserId(),
                            contribution(entry.getKey(), response, mode, essayMode), Double::sum);
                }
            }
        }
        return totals;
    }

    /**
     * Parse a user-entered positive points value, or null when blank / not a
     * positive number. Shared by the settings dialog (validating input) and the
     * service (re-reading the stored value) so both agree on what is valid.
     */
    public static Double parsePositivePoints(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            double parsed = Double.parseDouble(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    /**
     * Max points of the aggregate item: the fixed item total when set, otherwise
     * member count &times; per-question worth (worth defaults to 1). A fixed total
     * keeps the item's weight constant in points-based gradebooks as questions
     * are added; per-question worth makes the max grow with the question count.
     */
    public static double maxPoints(int memberCount, Double perQuestionPoints, Double fixedTotal) {
        if (fixedTotal != null && fixedTotal > 0) {
            return fixedTotal;
        }
        double worth = perQuestionPoints != null && perQuestionPoints > 0 ? perQuestionPoints : 1;
        return memberCount * worth;
    }

    /**
     * Convert share totals (from {@link #computeContributions}) to gradebook score
     * strings under the same worth model as {@link #maxPoints}: shares &times;
     * per-question worth, or shares / member count &times; the fixed total.
     * Rounded to 2 decimals, trailing zeros stripped.
     */
    public static Map<String, String> toScores(Map<String, Double> contributions, int memberCount,
            Double perQuestionPoints, Double fixedTotal) {
        Map<String, String> scores = new HashMap<>();
        if (contributions == null) {
            return scores;
        }
        boolean fixed = fixedTotal != null && fixedTotal > 0 && memberCount > 0;
        double worth = perQuestionPoints != null && perQuestionPoints > 0 ? perQuestionPoints : 1;
        contributions.forEach((userId, shares) -> {
            BigDecimal points = fixed
                    ? BigDecimal.valueOf(shares).multiply(BigDecimal.valueOf(fixedTotal))
                            .divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.valueOf(shares).multiply(BigDecimal.valueOf(worth))
                            .setScale(2, RoundingMode.HALF_UP);
            scores.put(userId, points.stripTrailingZeros().toPlainString());
        });
        return scores;
    }
}
