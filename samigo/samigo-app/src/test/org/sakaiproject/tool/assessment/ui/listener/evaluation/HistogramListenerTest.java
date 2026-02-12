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
