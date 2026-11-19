/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.services;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.StatisticsFacadeQueries;
import org.sakaiproject.tool.assessment.services.assessment.StatisticsService;
import org.sakaiproject.tool.assessment.services.assessment.StatisticsService.SubmissionOutcome;

public class StatisticsServiceMatchingTest {

    private StatisticsService statisticsService;
    private PublishedItemData item;

    @Before
    public void setUp() {
        statisticsService = new StatisticsService(new GradingService(), new ConcurrentMapCacheManager(),
                new QuestionPoolService(), new StatisticsFacadeQueries());

        PublishedAnswer correctAnswer = new PublishedAnswer();
        correctAnswer.setId(100L);
        correctAnswer.setIsCorrect(true);
        PublishedItemText matchedPrompt = new PublishedItemText();
        matchedPrompt.setId(10L);
        matchedPrompt.setAnswerSet(Set.of(correctAnswer));

        PublishedAnswer incorrectAnswer = new PublishedAnswer();
        incorrectAnswer.setId(200L);
        incorrectAnswer.setIsCorrect(false);
        PublishedItemText unmatchedPrompt = new PublishedItemText();
        unmatchedPrompt.setId(20L);
        unmatchedPrompt.setAnswerSet(Set.of(incorrectAnswer));

        item = new PublishedItemData();
        item.setItemId(1L);
        item.setTypeId(TypeIfc.MATCHING);
        item.setItemTextSet(Set.of(matchedPrompt, unmatchedPrompt));
    }

    @Test
    public void countsCorrectNoneOfTheAboveAsCorrect() {
        assertEquals(SubmissionOutcome.CORRECT, classify(100L, -1L));
    }

    @Test
    public void countsNoneOfTheAboveForPromptWithValidMatchAsIncorrect() {
        assertEquals(SubmissionOutcome.INCORRECT, classify(-1L, -1L));
    }

    @Test
    public void countsOrdinaryAnswerForUnmatchedPromptAsIncorrect() {
        assertEquals(SubmissionOutcome.INCORRECT, classify(100L, 200L));
    }

    @Test
    public void keepsPartiallyAnsweredQuestionIncorrect() {
        assertEquals(SubmissionOutcome.INCORRECT, classify(null, -1L));
        assertEquals(SubmissionOutcome.INCORRECT, classify(100L, null));
    }

    @Test
    public void keepsUnansweredQuestionBlank() {
        assertEquals(SubmissionOutcome.BLANK, classify(null, null));
    }

    private SubmissionOutcome classify(Long matchedAnswerId, Long unmatchedAnswerId) {
        ItemGradingData matchedResponse = new ItemGradingData();
        matchedResponse.setItemGradingId(1L);
        matchedResponse.setAssessmentGradingId(1L);
        matchedResponse.setPublishedItemTextId(10L);
        matchedResponse.setPublishedAnswerId(matchedAnswerId);
        ItemGradingData unmatchedResponse = new ItemGradingData();
        unmatchedResponse.setItemGradingId(2L);
        unmatchedResponse.setAssessmentGradingId(1L);
        unmatchedResponse.setPublishedItemTextId(20L);
        unmatchedResponse.setPublishedAnswerId(unmatchedAnswerId);
        return statisticsService.classifySubmission(item, List.of(matchedResponse, unmatchedResponse), Map.of());
    }
}
