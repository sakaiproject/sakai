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
package org.sakaiproject.tool.assessment.ui.servlet.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramBarBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramQuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramScoresBean;
import org.sakaiproject.tool.assessment.ui.model.AssessmentReportCell;

public class ExportReportServletTest {

    @Test
    public void testItemAnalysisDataHandlesNullTalliesAndPadsAnswerColumns() throws Exception {
        ExportReportServlet servlet = allocateServletWithoutConstructor();
        HistogramScoresBean scoresBean = new HistogramScoresBean();
        scoresBean.setMaxNumberOfAnswers(2);

        HistogramQuestionScoresBean surveyRow = new HistogramQuestionScoresBean();
        surveyRow.setQuestionNumber("1");
        surveyRow.setNumResponses(0);
        surveyRow.setPercentCorrect("0%");
        surveyRow.setShowIndividualAnswersInDetailedStatistics(false);
        surveyRow.setNumberOfStudentsWithZeroAnswers(1);

        HistogramQuestionScoresBean answeredRow = new HistogramQuestionScoresBean();
        answeredRow.setQuestionNumber("2");
        answeredRow.setNumResponses(1);
        answeredRow.setPercentCorrect("100%");
        answeredRow.setShowIndividualAnswersInDetailedStatistics(true);
        answeredRow.setNumberOfStudentsWithZeroAnswers(0);
        HistogramBarBean answerA = new HistogramBarBean();
        answerA.setNumStudents(1);
        answerA.setIsCorrect(true);
        answeredRow.setHistogramBars(new HistogramBarBean[] {answerA});

        scoresBean.setDetailedStatistics(Arrays.asList(surveyRow, answeredRow));

        List<List<AssessmentReportCell>> rows = invokeItemAnalysisDataCells(servlet, scoresBean);
        assertEquals(2, rows.size());

        List<AssessmentReportCell> firstRow = rows.get(0);
        List<AssessmentReportCell> secondRow = rows.get(1);
        assertEquals(9, firstRow.size());
        assertEquals(9, secondRow.size());

        assertEquals("", firstRow.get(3).getValue()); // Difficulty
        assertEquals("", firstRow.get(4).getValue()); // Total Correct
        assertEquals("", firstRow.get(5).getValue()); // Total Incorrect
        assertEquals("1", firstRow.get(6).getValue()); // No Answer
        assertEquals("", firstRow.get(7).getValue()); // A
        assertEquals("", firstRow.get(8).getValue()); // B

        assertEquals("1", secondRow.get(7).getValue()); // A
        assertTrue(secondRow.get(7).isBold()); // A should be bold when correct
        assertEquals("", secondRow.get(8).getValue()); // B padded
        assertFalse(secondRow.get(8).isBold()); // B padded cell should not be bold
    }

    @SuppressWarnings("unchecked")
    private List<List<AssessmentReportCell>> invokeItemAnalysisDataCells(ExportReportServlet servlet, HistogramScoresBean scoresBean) throws Exception {
        Method method = ExportReportServlet.class.getDeclaredMethod("itemAnalysisDataCells", HistogramScoresBean.class);
        method.setAccessible(true);
        return (List<List<AssessmentReportCell>>) method.invoke(servlet, scoresBean);
    }

    private ExportReportServlet allocateServletWithoutConstructor() throws Exception {
        return Mockito.mock(ExportReportServlet.class, Mockito.CALLS_REAL_METHODS);
    }
}
