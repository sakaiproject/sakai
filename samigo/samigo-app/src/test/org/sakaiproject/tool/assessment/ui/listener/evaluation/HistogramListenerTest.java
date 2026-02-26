/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.StatisticsFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.StatisticsService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramQuestionScoresBean;

public class HistogramListenerTest {

    @Test
    public void testApplyCanonicalSubmissionTalliesMcssBlankCountedOnlyAsBlank() throws Exception {
        HistogramListener listener = new HistogramListener();
        listener.setStatisticsService(createStatisticsService());

        HistogramQuestionScoresBean qbean = new HistogramQuestionScoresBean();
        PublishedItemData item = new PublishedItemData();
        item.setItemId(1L);
        item.setTypeId(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION);

        List<ItemGradingData> scores = new ArrayList<>();
        // Student 1: correct
        scores.add(itemGrading(1L, 100L, "student-1", 10L));
        // Student 2: incorrect
        scores.add(itemGrading(2L, 101L, "student-2", 11L));
        // Student 3: blank
        scores.add(itemGrading(3L, null, "student-3", 12L));

        Map<Long, AnswerIfc> answersById = new HashMap<>();
        answersById.put(100L, answer(100L, true));
        answersById.put(101L, answer(101L, false));

        invokeApplyCanonicalSubmissionTallies(listener, qbean, item, scores, answersById);

        assertEquals(2, qbean.getNumResponses());
        assertEquals(1, qbean.getNumberOfStudentsWithZeroAnswers());
        assertEquals(new TreeSet<>(Arrays.asList("student-1", "student-2")), qbean.getStudentsResponded());
        assertEquals(new TreeSet<>(Collections.singleton("student-1")), qbean.getStudentsWithAllCorrect());
    }

    @Test
    public void testParseMetadataPercentCorrectDefaultsToZeroForBlank() throws Exception {
        HistogramListener listener = new HistogramListener();

        assertEquals(0.0d, listener.parseMetadataPercentCorrect("", "keywords"), 0.0d);
        assertEquals(0.0d, listener.parseMetadataPercentCorrect(null, "keywords"), 0.0d);
        assertEquals(0.0d, listener.parseMetadataPercentCorrect("N/A", "objectives"), 0.0d);
        assertEquals(0.0d, listener.parseMetadataPercentCorrect("not-a-number", "keywords"), 0.0d);
    }

    @Test
    public void testParseMetadataPercentCorrectParsesNumericValues() throws Exception {
        HistogramListener listener = new HistogramListener();

        assertEquals(73.5d, listener.parseMetadataPercentCorrect("73.5", "keywords"), 0.0d);
        assertEquals(0.0d, listener.parseMetadataPercentCorrect("-1", "keywords"), 0.0d);
        assertEquals(100.0d, listener.parseMetadataPercentCorrect("120", "keywords"), 0.0d);
    }

    @Test
    public void testParseMetadataValuesNormalizesWhitespaceCaseAndDeduplicates() {
        HistogramListener listener = new HistogramListener();

        List<String> values = listener.parseMetadataValues("  Rose,\u00A0flowers,, red   rose, ROSE, FLOWERS ");

        assertEquals(Arrays.asList("rose", "flowers", "red rose"), values);
    }

    @Test
    public void testUpdateMetadataAverageMergesReorderedObjectivesAndKeywords() {
        HistogramListener listener = new HistogramListener();
        Map<String, Double> objectivesCorrect = new HashMap<>();
        Map<String, Integer> objectivesCounter = new HashMap<>();
        Map<String, Double> keywordsCorrect = new HashMap<>();
        Map<String, Integer> keywordsCounter = new HashMap<>();

        listener.updateMetadataAverage(objectivesCorrect, objectivesCounter, listener.parseMetadataValues("1.1, 1.2"), 100.0d);
        listener.updateMetadataAverage(objectivesCorrect, objectivesCounter, listener.parseMetadataValues("1.2,\u00A01.1"), 0.0d);

        listener.updateMetadataAverage(keywordsCorrect, keywordsCounter, listener.parseMetadataValues("Flowers, Rose"), 100.0d);
        listener.updateMetadataAverage(keywordsCorrect, keywordsCounter, listener.parseMetadataValues("rose,\u00A0flowers"), 0.0d);

        assertEquals(2, objectivesCorrect.size());
        assertEquals(50.0d, objectivesCorrect.get("1.1"), 0.0d);
        assertEquals(50.0d, objectivesCorrect.get("1.2"), 0.0d);

        assertEquals(2, keywordsCorrect.size());
        assertEquals(50.0d, keywordsCorrect.get("flowers"), 0.0d);
        assertEquals(50.0d, keywordsCorrect.get("rose"), 0.0d);
    }

    @Test
    public void testUpdateMetadataAverageDeduplicatesRepeatedTagsWithinQuestion() {
        HistogramListener listener = new HistogramListener();
        Map<String, Double> keywordsCorrect = new HashMap<>();
        Map<String, Integer> keywordsCounter = new HashMap<>();

        listener.updateMetadataAverage(keywordsCorrect, keywordsCounter, listener.parseMetadataValues("Rose, rose, ROSE"), 100.0d);
        listener.updateMetadataAverage(keywordsCorrect, keywordsCounter, listener.parseMetadataValues("rose"), 0.0d);

        assertEquals(1, keywordsCorrect.size());
        assertEquals(2, (int) keywordsCounter.get("rose"));
        assertEquals(50.0d, keywordsCorrect.get("rose"), 0.0d);
    }

    @Test
    public void testUpdateMetadataAverageKeepsFullPrecisionUntilPresentation() {
        HistogramListener listener = new HistogramListener();
        Map<String, Double> objectivesCorrect = new HashMap<>();
        Map<String, Integer> objectivesCounter = new HashMap<>();

        listener.updateMetadataAverage(objectivesCorrect, objectivesCounter, listener.parseMetadataValues("1.1"), 33.335d);
        listener.updateMetadataAverage(objectivesCorrect, objectivesCounter, listener.parseMetadataValues("1.1"), 33.335d);

        assertEquals(33.335d, objectivesCorrect.get("1.1"), 0.0d);
    }

    @Test
    public void testResolveScoreStatisticsPercentCorrectUsesMeanOverItemScore() throws Exception {
        HistogramListener listener = new HistogramListener();
        HistogramQuestionScoresBean qbean = new HistogramQuestionScoresBean();
        qbean.setNumResponses(1);
        qbean.setMean("2.0");
        qbean.setTotalScore("2.0");

        assertEquals(100, listener.resolveScoreStatisticsPercentCorrect(qbean));
    }

    @Test
    public void testResolveScoreStatisticsPercentCorrectTwoOfThreeRoundsTo67() throws Exception {
        HistogramListener listener = new HistogramListener();
        HistogramQuestionScoresBean qbean = new HistogramQuestionScoresBean();
        qbean.setNumResponses(1);
        qbean.setMean("2.0");
        qbean.setTotalScore("3.0");

        assertEquals(67, listener.resolveScoreStatisticsPercentCorrect(qbean));
    }

    @Test
    public void testResolveScoreStatisticsPercentCorrectReturnsZeroWhenNoResponses() throws Exception {
        HistogramListener listener = new HistogramListener();
        HistogramQuestionScoresBean qbean = new HistogramQuestionScoresBean();
        qbean.setNumResponses(0);
        qbean.setMean("2.0");
        qbean.setTotalScore("3.0");

        assertEquals(0, listener.resolveScoreStatisticsPercentCorrect(qbean));
    }

    @Test
    public void testResolveScoreStatisticsPercentCorrectReturnsZeroWhenTotalPossibleScoreZero() throws Exception {
        HistogramListener listener = new HistogramListener();
        HistogramQuestionScoresBean qbean = new HistogramQuestionScoresBean();
        qbean.setNumResponses(1);
        qbean.setMean("2.0");
        qbean.setTotalScore("0.0");

        assertEquals(0, listener.resolveScoreStatisticsPercentCorrect(qbean));
    }

    @Test
    public void testDoScoreStatisticsPreservesQuestionTotalPossibleScoreForPercentCorrect() throws Exception {
        HistogramListener listener = new HistogramListener();
        HistogramQuestionScoresBean qbean = new HistogramQuestionScoresBean();
        qbean.setTotalScore("2.0");

        List<ItemGradingData> scores = new ArrayList<>();
        ItemGradingData first = new ItemGradingData();
        first.setAutoScore(2.0d);
        scores.add(first);

        ItemGradingData second = new ItemGradingData();
        second.setAutoScore(1.0d);
        scores.add(second);

        listener.doScoreStatistics(qbean, scores);

        assertEquals("75", qbean.getPercentCorrect());
        assertEquals("2.0", qbean.getTotalScore());
    }

    @Test
    public void testDoScoreStatisticsKeepsBlankQuestionTotalPossibleScore() {
        HistogramListener listener = new HistogramListener();
        HistogramQuestionScoresBean qbean = new HistogramQuestionScoresBean();
        qbean.setTotalScore("");

        List<ItemGradingData> scores = new ArrayList<>();
        ItemGradingData first = new ItemGradingData();
        first.setAutoScore(2.0d);
        scores.add(first);

        ItemGradingData second = new ItemGradingData();
        second.setAutoScore(1.0d);
        scores.add(second);

        listener.doScoreStatistics(qbean, scores);

        assertEquals("0", qbean.getPercentCorrect());
        assertEquals("N/A", qbean.getTotalScore());
    }

    @Test
    public void testResolveMetadataPercentCorrectUsesPreciseAnswerRatio() throws Exception {
        HistogramListener listener = new HistogramListener();
        HistogramQuestionScoresBean qbean = new HistogramQuestionScoresBean();
        qbean.setQuestionType(TypeIfc.MULTIPLE_CHOICE.toString());
        qbean.setNumResponses(3);
        qbean.setNumberOfStudentsWithCorrectAnswers(1L);
        qbean.setPercentCorrect("33");

        assertEquals(33.333d, listener.resolveMetadataPercentCorrect(qbean, "objectives"), 0.001d);
    }

    @Test
    public void testResolveMetadataPercentCorrectUsesPreciseScoreRatio() throws Exception {
        HistogramListener listener = new HistogramListener();
        HistogramQuestionScoresBean qbean = new HistogramQuestionScoresBean();
        qbean.setQuestionType(TypeIfc.ESSAY_QUESTION.toString());
        qbean.setNumResponses(1);
        qbean.setMean("1.0");
        qbean.setTotalScore("3.0");

        assertEquals(33.333d, listener.resolveMetadataPercentCorrect(qbean, "objectives"), 0.001d);
    }

    private StatisticsService createStatisticsService() {
        GradingService gradingService = new GradingService();
        MemoryService memoryService = mock(MemoryService.class);
        Cache cache = mock(Cache.class);
        when(memoryService.getCache(anyString())).thenReturn(cache);
        QuestionPoolService questionPoolService = mock(QuestionPoolService.class);
        StatisticsFacadeQueriesAPI statisticsFacadeQueries = mock(StatisticsFacadeQueriesAPI.class);

        return new StatisticsService(gradingService, memoryService, questionPoolService, statisticsFacadeQueries);
    }

    private ItemGradingData itemGrading(Long itemGradingId, Long answerId, String agentId, Long assessmentGradingId) {
        ItemGradingData data = new ItemGradingData();
        data.setItemGradingId(itemGradingId);
        data.setAssessmentGradingId(assessmentGradingId);
        data.setAgentId(agentId);
        data.setPublishedAnswerId(answerId);
        return data;
    }

    private PublishedAnswer answer(Long answerId, boolean isCorrect) {
        PublishedAnswer answer = new PublishedAnswer();
        answer.setId(answerId);
        answer.setIsCorrect(isCorrect);
        return answer;
    }

    private void invokeApplyCanonicalSubmissionTallies(HistogramListener listener, HistogramQuestionScoresBean qbean,
            ItemDataIfc item, List<ItemGradingData> scores, Map<Long, AnswerIfc> answersById) throws Exception {
        Method method = HistogramListener.class.getDeclaredMethod("applyCanonicalSubmissionTallies",
                HistogramQuestionScoresBean.class, ItemDataIfc.class, List.class, Map.class);
        method.setAccessible(true);
        method.invoke(listener, qbean, item, scores, answersById);
    }

}
