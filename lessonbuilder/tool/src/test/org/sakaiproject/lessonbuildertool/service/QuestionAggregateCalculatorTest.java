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
package org.sakaiproject.lessonbuildertool.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponse;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponseImpl;
import org.sakaiproject.lessonbuildertool.api.QuestionAggregateCalculator;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sakaiproject.lessonbuildertool.api.QuestionAggregateCalculator.ESSAY_ANY_ANSWER;
import static org.sakaiproject.lessonbuildertool.api.QuestionAggregateCalculator.ESSAY_CARRY_GRADE;
import static org.sakaiproject.lessonbuildertool.api.QuestionAggregateCalculator.MODE_CORRECTNESS;
import static org.sakaiproject.lessonbuildertool.api.QuestionAggregateCalculator.MODE_PARTICIPATION;

public class QuestionAggregateCalculatorTest {

    private static final double EPS = 0.0001;

    @BeforeClass
    public static void wireItemAttributeFactory() {
        // SimplePageItemImpl creates its attributes map through the static dao,
        // exactly as the component wiring does at startup
        SimplePageToolDao dao = mock(SimplePageToolDao.class);
        when(dao.newJSONObject()).thenAnswer(invocation -> new JSONObject());
        SimplePageItemImpl.setSimplePageToolDao(dao);
    }

    private long nextItemId = 1;

    private SimplePageItem multipleChoiceQuestion() {
        SimplePageItem item = new SimplePageItemImpl(nextItemId++, 1, 1, SimplePageItem.QUESTION, "0", "Question");
        item.setAttribute("questionType", "multipleChoice");
        return item;
    }

    private SimplePageItem shortanswerQuestion() {
        SimplePageItem item = new SimplePageItemImpl(nextItemId++, 1, 1, SimplePageItem.QUESTION, "0", "Question");
        item.setAttribute("questionType", "shortanswer");
        return item;
    }

    private SimplePageItem gradedEssayQuestion(int maxPoints) {
        SimplePageItem item = shortanswerQuestion();
        item.setAttribute("questionGraded", "true");
        item.setGradebookPoints(maxPoints);
        return item;
    }

    private SimplePageQuestionResponse mcResponse(String userId, long questionId, long answerId, boolean correct) {
        SimplePageQuestionResponse r = new SimplePageQuestionResponseImpl(userId, questionId);
        r.setMultipleChoiceId(answerId);
        r.setCorrect(correct);
        return r;
    }

    private SimplePageQuestionResponse saResponse(String userId, long questionId, String answer, boolean correct) {
        SimplePageQuestionResponse r = new SimplePageQuestionResponseImpl(userId, questionId);
        r.setShortanswer(answer);
        r.setCorrect(correct);
        return r;
    }

    // ---------- isAnswered ----------

    @Test
    public void multipleChoiceWithChosenAnswerIsAnswered() {
        SimplePageItem q = multipleChoiceQuestion();
        Assert.assertTrue(QuestionAggregateCalculator.isAnswered(q, mcResponse("s1", q.getId(), 3, false)));
    }

    @Test
    public void multipleChoiceWithoutChosenAnswerIsNotAnswered() {
        SimplePageItem q = multipleChoiceQuestion();
        // answer ids are always >= 1; 0 is the unset default on the row
        Assert.assertFalse(QuestionAggregateCalculator.isAnswered(q, mcResponse("s1", q.getId(), 0, false)));
    }

    @Test
    public void shortanswerWithTextIsAnswered() {
        SimplePageItem q = shortanswerQuestion();
        Assert.assertTrue(QuestionAggregateCalculator.isAnswered(q, saResponse("s1", q.getId(), "an answer", false)));
    }

    @Test
    public void blankShortanswerIsNotAnswered() {
        SimplePageItem q = shortanswerQuestion();
        Assert.assertFalse(QuestionAggregateCalculator.isAnswered(q, saResponse("s1", q.getId(), "", false)));
        Assert.assertFalse(QuestionAggregateCalculator.isAnswered(q, saResponse("s1", q.getId(), "   \t\n", false)));
        Assert.assertFalse(QuestionAggregateCalculator.isAnswered(q, saResponse("s1", q.getId(), null, false)));
    }

    @Test
    public void unknownOrMissingQuestionTypeNeverCounts() {
        SimplePageItem q = new SimplePageItemImpl(99, 1, 1, SimplePageItem.QUESTION, "0", "Question");
        Assert.assertFalse(QuestionAggregateCalculator.isAnswered(q, mcResponse("s1", 99, 3, true)));
        q.setAttribute("questionType", "essay");
        Assert.assertFalse(QuestionAggregateCalculator.isAnswered(q, mcResponse("s1", 99, 3, true)));
        Assert.assertFalse(QuestionAggregateCalculator.isAnswered(null, mcResponse("s1", 99, 3, true)));
        Assert.assertFalse(QuestionAggregateCalculator.isAnswered(q, null));
    }

    // ---------- contribution (mode behavior) ----------

    @Test
    public void wrongMultipleChoiceAnswerCountsForParticipationOnly() {
        SimplePageItem q = multipleChoiceQuestion();
        SimplePageQuestionResponse wrong = mcResponse("s1", q.getId(), 2, false);
        Assert.assertEquals(1, QuestionAggregateCalculator.contribution(q, wrong, MODE_PARTICIPATION, ESSAY_ANY_ANSWER), EPS);
        Assert.assertEquals(0, QuestionAggregateCalculator.contribution(q, wrong, MODE_CORRECTNESS, ESSAY_ANY_ANSWER), EPS);
    }

    @Test
    public void correctAnswerCountsInBothModes() {
        SimplePageItem q = multipleChoiceQuestion();
        SimplePageQuestionResponse right = mcResponse("s1", q.getId(), 1, true);
        Assert.assertEquals(1, QuestionAggregateCalculator.contribution(q, right, MODE_PARTICIPATION, ESSAY_ANY_ANSWER), EPS);
        Assert.assertEquals(1, QuestionAggregateCalculator.contribution(q, right, MODE_CORRECTNESS, ESSAY_ANY_ANSWER), EPS);
    }

    @Test
    public void unansweredNeverCountsEvenIfMarkedCorrect() {
        SimplePageItem q = shortanswerQuestion();
        SimplePageQuestionResponse blankButCorrect = saResponse("s1", q.getId(), "  ", true);
        Assert.assertEquals(0, QuestionAggregateCalculator.contribution(q, blankButCorrect, MODE_PARTICIPATION, ESSAY_ANY_ANSWER), EPS);
        Assert.assertEquals(0, QuestionAggregateCalculator.contribution(q, blankButCorrect, MODE_CORRECTNESS, ESSAY_CARRY_GRADE), EPS);
    }

    @Test
    public void overriddenToCorrectCountsInCorrectnessMode() {
        // GradingBean sets correct=true when the instructor overrides to full points;
        // the calculator must honor the stored flag for multiple choice
        SimplePageItem q = multipleChoiceQuestion();
        SimplePageQuestionResponse r = mcResponse("s1", q.getId(), 2, true);
        r.setOverridden(true);
        Assert.assertEquals(1, QuestionAggregateCalculator.contribution(q, r, MODE_CORRECTNESS, ESSAY_ANY_ANSWER), EPS);
    }

    @Test
    public void unknownModeFallsBackToParticipation() {
        SimplePageItem q = multipleChoiceQuestion();
        SimplePageQuestionResponse wrong = mcResponse("s1", q.getId(), 2, false);
        Assert.assertEquals(1, QuestionAggregateCalculator.contribution(q, wrong, null, ESSAY_ANY_ANSWER), EPS);
        Assert.assertEquals(1, QuestionAggregateCalculator.contribution(q, wrong, "bogus", ESSAY_ANY_ANSWER), EPS);
    }

    // ---------- essay/short-answer handling in correctness mode ----------

    @Test
    public void anyAnswerModeGivesFullShareToWrongShortAnswers() {
        SimplePageItem q = shortanswerQuestion();
        SimplePageQuestionResponse wrong = saResponse("s1", q.getId(), "wrong guess", false);
        Assert.assertEquals(1, QuestionAggregateCalculator.contribution(q, wrong, MODE_CORRECTNESS, ESSAY_ANY_ANSWER), EPS);
    }

    @Test
    public void carryModeUsesTheManualGradeAsPartialCredit() {
        SimplePageItem q = gradedEssayQuestion(5);
        SimplePageQuestionResponse r = saResponse("s1", q.getId(), "decent effort", false);
        r.setPoints(3.0);
        r.setOverridden(true);
        Assert.assertEquals(0.6, QuestionAggregateCalculator.contribution(q, r, MODE_CORRECTNESS, ESSAY_CARRY_GRADE), EPS);

        r.setPoints(5.0);
        Assert.assertEquals(1.0, QuestionAggregateCalculator.contribution(q, r, MODE_CORRECTNESS, ESSAY_CARRY_GRADE), EPS);

        r.setPoints(0.0);
        Assert.assertEquals(0.0, QuestionAggregateCalculator.contribution(q, r, MODE_CORRECTNESS, ESSAY_CARRY_GRADE), EPS);
    }

    @Test
    public void carryModeGivesNothingUntilTheEssayIsGraded() {
        SimplePageItem q = gradedEssayQuestion(5);
        SimplePageQuestionResponse ungraded = saResponse("s1", q.getId(), "awaiting grading", false);
        ungraded.setPoints(null);
        Assert.assertEquals(0, QuestionAggregateCalculator.contribution(q, ungraded, MODE_CORRECTNESS, ESSAY_CARRY_GRADE), EPS);
        // but participation mode still counts the answer
        Assert.assertEquals(1, QuestionAggregateCalculator.contribution(q, ungraded, MODE_PARTICIPATION, ESSAY_CARRY_GRADE), EPS);
    }

    @Test
    public void carryModeClampsExcessPointsToTheShare() {
        SimplePageItem q = gradedEssayQuestion(5);
        SimplePageQuestionResponse r = saResponse("s1", q.getId(), "bonus", false);
        r.setPoints(7.5);
        Assert.assertEquals(1.0, QuestionAggregateCalculator.contribution(q, r, MODE_CORRECTNESS, ESSAY_CARRY_GRADE), EPS);
    }

    @Test
    public void carryModeFallsBackToCorrectFlagForAutoCheckedShortAnswers() {
        // short answer with expected answers but NOT manually graded: correct flag rules
        SimplePageItem q = shortanswerQuestion();
        SimplePageQuestionResponse right = saResponse("s1", q.getId(), "photosynthesis", true);
        SimplePageQuestionResponse wrong = saResponse("s2", q.getId(), "osmosis", false);
        Assert.assertEquals(1, QuestionAggregateCalculator.contribution(q, right, MODE_CORRECTNESS, ESSAY_CARRY_GRADE), EPS);
        Assert.assertEquals(0, QuestionAggregateCalculator.contribution(q, wrong, MODE_CORRECTNESS, ESSAY_CARRY_GRADE), EPS);
    }

    @Test
    public void essayModeDoesNotAffectMultipleChoice() {
        SimplePageItem q = multipleChoiceQuestion();
        SimplePageQuestionResponse wrong = mcResponse("s1", q.getId(), 2, false);
        Assert.assertEquals(0, QuestionAggregateCalculator.contribution(q, wrong, MODE_CORRECTNESS, ESSAY_CARRY_GRADE), EPS);
        Assert.assertEquals(0, QuestionAggregateCalculator.contribution(q, wrong, MODE_CORRECTNESS, ESSAY_ANY_ANSWER), EPS);
    }

    // ---------- computeContributions ----------

    @Test
    public void mixedFixtureScoresDifferByMode() {
        SimplePageItem q1 = multipleChoiceQuestion();
        SimplePageItem q2 = multipleChoiceQuestion();
        SimplePageItem q3 = shortanswerQuestion();

        Map<SimplePageItem, List<SimplePageQuestionResponse>> members = new LinkedHashMap<>();
        // s1: q1 right, q2 wrong, q3 unanswered (blank submission)
        // s2: q1 right, q2 right, q3 right
        // s3: only q3, wrong (auto-checked)
        members.put(q1, Arrays.asList(
                mcResponse("s1", q1.getId(), 1, true),
                mcResponse("s2", q1.getId(), 1, true)));
        members.put(q2, Arrays.asList(
                mcResponse("s1", q2.getId(), 2, false),
                mcResponse("s2", q2.getId(), 1, true)));
        members.put(q3, Arrays.asList(
                saResponse("s1", q3.getId(), "", false),
                saResponse("s2", q3.getId(), "right", true),
                saResponse("s3", q3.getId(), "wrong", false)));

        Map<String, Double> participation = QuestionAggregateCalculator.computeContributions(members, MODE_PARTICIPATION, ESSAY_ANY_ANSWER, null);
        Assert.assertEquals(2, participation.get("s1"), EPS);
        Assert.assertEquals(3, participation.get("s2"), EPS);
        Assert.assertEquals(1, participation.get("s3"), EPS);

        Map<String, Double> correctness = QuestionAggregateCalculator.computeContributions(members, MODE_CORRECTNESS, ESSAY_CARRY_GRADE, null);
        Assert.assertEquals(1, correctness.get("s1"), EPS);
        Assert.assertEquals(3, correctness.get("s2"), EPS);
        Assert.assertEquals(0, correctness.get("s3"), EPS);
    }

    @Test
    public void respondersToNonMemberQuestionsGetExplicitZero() {
        SimplePageItem q1 = multipleChoiceQuestion();
        Map<SimplePageItem, List<SimplePageQuestionResponse>> members = new LinkedHashMap<>();
        members.put(q1, Collections.singletonList(mcResponse("s1", q1.getId(), 1, true)));

        // s9 answered only questions that are not in the aggregate: must be seeded 0
        Set<String> allResponders = new HashSet<>(Arrays.asList("s1", "s9"));
        Map<String, Double> totals = QuestionAggregateCalculator.computeContributions(members, MODE_PARTICIPATION, ESSAY_ANY_ANSWER, allResponders);

        Assert.assertEquals(1, totals.get("s1"), EPS);
        Assert.assertEquals(0, totals.get("s9"), EPS);
        Assert.assertEquals(2, totals.size());
    }

    @Test
    public void answeredButNotCountingStudentStillGetsZeroRow() {
        // a student whose only member-question response is wrong (correctness mode)
        // must get an explicit 0 pushed, not be omitted, even without seeding
        SimplePageItem q1 = multipleChoiceQuestion();
        Map<SimplePageItem, List<SimplePageQuestionResponse>> members = new LinkedHashMap<>();
        members.put(q1, Collections.singletonList(mcResponse("s1", q1.getId(), 2, false)));

        Map<String, Double> totals = QuestionAggregateCalculator.computeContributions(members, MODE_CORRECTNESS, ESSAY_ANY_ANSWER, null);
        Assert.assertEquals(0, totals.get("s1"), EPS);
    }

    @Test
    public void emptyMembersProducesOnlySeededZeros() {
        Map<String, Double> totals = QuestionAggregateCalculator.computeContributions(
                new LinkedHashMap<>(), MODE_PARTICIPATION, ESSAY_ANY_ANSWER, new HashSet<>(Arrays.asList("s1")));
        Assert.assertEquals(Collections.singletonMap("s1", 0.0), totals);

        Assert.assertTrue(QuestionAggregateCalculator.computeContributions(null, MODE_PARTICIPATION, ESSAY_ANY_ANSWER, null).isEmpty());
    }

    @Test
    public void duplicateResponseRowsForSameUserCountOnce() {
        SimplePageItem q1 = multipleChoiceQuestion();
        Map<SimplePageItem, List<SimplePageQuestionResponse>> members = new LinkedHashMap<>();
        members.put(q1, Arrays.asList(
                mcResponse("s1", q1.getId(), 1, true),
                mcResponse("s1", q1.getId(), 1, true)));

        Map<String, Double> totals = QuestionAggregateCalculator.computeContributions(members, MODE_PARTICIPATION, ESSAY_ANY_ANSWER, null);
        Assert.assertEquals(1, totals.get("s1"), EPS);
    }

    @Test
    public void nullEntriesAreIgnored() {
        SimplePageItem q1 = multipleChoiceQuestion();
        SimplePageItem q2 = multipleChoiceQuestion();
        Map<SimplePageItem, List<SimplePageQuestionResponse>> members = new LinkedHashMap<>();
        List<SimplePageQuestionResponse> withNulls = new ArrayList<>();
        withNulls.add(null);
        withNulls.add(mcResponse("s1", q1.getId(), 1, true));
        members.put(q1, withNulls);
        members.put(q2, null);

        Map<String, Double> totals = QuestionAggregateCalculator.computeContributions(members, MODE_PARTICIPATION, ESSAY_ANY_ANSWER, null);
        Assert.assertEquals(1, totals.get("s1"), EPS);
    }

    // ---------- maxPoints / toScores (worth models) ----------

    @Test
    public void maxPointsIsOnePointPerMemberQuestionByDefault() {
        Assert.assertEquals(0.0, QuestionAggregateCalculator.maxPoints(0, null, null), EPS);
        Assert.assertEquals(3.0, QuestionAggregateCalculator.maxPoints(3, null, null), EPS);
        Assert.assertEquals(40.0, QuestionAggregateCalculator.maxPoints(40, null, null), EPS);
    }

    @Test
    public void perQuestionWorthMultipliesTheMax() {
        Assert.assertEquals(8.0, QuestionAggregateCalculator.maxPoints(4, 2.0, null), EPS);
        Assert.assertEquals(2.0, QuestionAggregateCalculator.maxPoints(4, 0.5, null), EPS);
        // non-positive worth falls back to 1
        Assert.assertEquals(4.0, QuestionAggregateCalculator.maxPoints(4, 0.0, null), EPS);
    }

    @Test
    public void fixedTotalOverridesEverything() {
        Assert.assertEquals(10.0, QuestionAggregateCalculator.maxPoints(40, 2.0, 10.0), EPS);
        // non-positive total falls back to per-question worth
        Assert.assertEquals(80.0, QuestionAggregateCalculator.maxPoints(40, 2.0, 0.0), EPS);
    }

    @Test
    public void scoresScaleByPerQuestionWorth() {
        Map<String, Double> totals = new LinkedHashMap<>();
        totals.put("s1", 3.0);
        totals.put("s2", 0.0);

        Map<String, String> scores = QuestionAggregateCalculator.toScores(totals, 4, 2.0, null);
        Assert.assertEquals("6", scores.get("s1"));
        Assert.assertEquals("0", scores.get("s2"));
    }

    @Test
    public void scoresScaleToTheFixedTotalAsAPercentage() {
        Map<String, Double> totals = new LinkedHashMap<>();
        totals.put("s1", 3.0);   // 3 of 4 shares
        totals.put("s2", 4.0);

        Map<String, String> scores = QuestionAggregateCalculator.toScores(totals, 4, null, 10.0);
        Assert.assertEquals("7.5", scores.get("s1"));
        Assert.assertEquals("10", scores.get("s2"));
    }

    @Test
    public void partialCreditSharesSurviveScaling() {
        Map<String, Double> totals = Collections.singletonMap("s1", 2.6); // e.g. 2 full + 0.6 carry
        Assert.assertEquals("2.6", QuestionAggregateCalculator.toScores(totals, 4, null, null).get("s1"));
        Assert.assertEquals("6.5", QuestionAggregateCalculator.toScores(totals, 4, null, 10.0).get("s1"));
        Assert.assertEquals("5.2", QuestionAggregateCalculator.toScores(totals, 4, 2.0, null).get("s1"));
    }

    @Test
    public void scalingRoundsToTwoDecimals() {
        Map<String, Double> totals = Collections.singletonMap("s1", 1.0); // 1 of 3 × 10 = 3.333…
        Assert.assertEquals("3.33", QuestionAggregateCalculator.toScores(totals, 3, null, 10.0).get("s1"));

        totals = Collections.singletonMap("s1", 2.0); // 2 of 3 × 10 = 6.666… → 6.67
        Assert.assertEquals("6.67", QuestionAggregateCalculator.toScores(totals, 3, null, 10.0).get("s1"));
    }

    @Test
    public void scoresAreCleanStringsWithoutTrailingZeros() {
        Map<String, Double> totals = new LinkedHashMap<>();
        totals.put("s1", 2.0);
        totals.put("s2", 2.5);
        Map<String, String> scores = QuestionAggregateCalculator.toScores(totals, 4, null, null);
        Assert.assertEquals("2", scores.get("s1"));
        Assert.assertEquals("2.5", scores.get("s2"));
    }
}
