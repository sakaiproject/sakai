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
package org.sakaiproject.tool.assessment.ui.bean.print;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfQuestionModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfMatchingRowModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfSelectionAnswerModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPrintPdfModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentStudentReportPdfModel;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ContentsDeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FinBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.MatchingBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SelectionBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.print.settings.PrintSettingsBean;
import org.sakaiproject.util.api.FormattedText;

import javax.faces.model.SelectItem;

public class AssessmentPdfSnapshotBuilderTest {

    @Test
    public void buildPrintModelSkipsCalculatedTextWhenFinArrayIsNull() {
        ItemDataIfc itemData = mock(ItemDataIfc.class);
        when(itemData.getTypeId()).thenReturn(TypeIfc.MULTIPLE_CHOICE);
        when(itemData.getText()).thenReturn("Multiple choice question");

        ItemContentsBean item = new ItemContentsBean();
        item.setItemData(itemData);
        item.setSequence("1");

        SectionContentsBean section = mock(SectionContentsBean.class);
        when(section.getItemContents()).thenReturn(List.of(item));
        when(section.getAttachmentList()).thenReturn(Collections.emptyList());
        when(section.getTitle()).thenReturn("Part 1");
        when(section.getDescription()).thenReturn("");

        DeliveryBean deliveryBean = mock(DeliveryBean.class);
        when(deliveryBean.getAssessmentTitle()).thenReturn("Sample Quiz");
        when(deliveryBean.getIsMathJaxEnabled()).thenReturn(Boolean.FALSE);

        PrintSettingsBean printSettings = new PrintSettingsBean();
        FormattedText formattedText = mock(FormattedText.class);

        AssessmentPrintPdfModel model = AssessmentPdfSnapshotBuilder.buildPrintModel(
                deliveryBean, List.of(section), printSettings, "", formattedText);

        assertNotNull(model);
        AssessmentPdfQuestionModel question = model.getParts().get(0).getQuestions().get(0);
        assertNull(question.getCalculatedQuestionText());
    }

    @Test
    public void buildPrintModelSkipsCalculatedTextWhenFinArrayIsNullForCalculatedQuestion() {
        ItemDataIfc itemData = mock(ItemDataIfc.class);
        when(itemData.getTypeId()).thenReturn(TypeIfc.CALCULATED_QUESTION);

        ItemContentsBean item = new ItemContentsBean();
        item.setItemData(itemData);
        item.setSequence("1");
        item.setInstruction("Kevin has {x} apples");

        SectionContentsBean section = mock(SectionContentsBean.class);
        when(section.getItemContents()).thenReturn(List.of(item));
        when(section.getAttachmentList()).thenReturn(Collections.emptyList());
        when(section.getTitle()).thenReturn("Part 1");
        when(section.getDescription()).thenReturn("");

        DeliveryBean deliveryBean = mock(DeliveryBean.class);
        when(deliveryBean.getAssessmentTitle()).thenReturn("Sample Quiz");
        when(deliveryBean.getIsMathJaxEnabled()).thenReturn(Boolean.FALSE);

        PrintSettingsBean printSettings = new PrintSettingsBean();
        FormattedText formattedText = mock(FormattedText.class);

        AssessmentPrintPdfModel model = AssessmentPdfSnapshotBuilder.buildPrintModel(
                deliveryBean, List.of(section), printSettings, "", formattedText);

        assertNull(model.getParts().get(0).getQuestions().get(0).getCalculatedQuestionText());
    }

    @Test
    public void buildPrintModelIncludesCalculatedTextWhenFinArrayIsPresent() {
        ItemDataIfc itemData = mock(ItemDataIfc.class);
        when(itemData.getTypeId()).thenReturn(TypeIfc.CALCULATED_QUESTION);

        FinBean finBean = new FinBean();
        finBean.setText("Kevin has 3 apples");

        ItemContentsBean item = new ItemContentsBean();
        item.setItemData(itemData);
        item.setFinArray(List.of(finBean));
        item.setSequence("1");
        item.setInstruction("Calculated question");

        SectionContentsBean section = mock(SectionContentsBean.class);
        when(section.getItemContents()).thenReturn(List.of(item));
        when(section.getAttachmentList()).thenReturn(Collections.emptyList());
        when(section.getTitle()).thenReturn("Part 1");
        when(section.getDescription()).thenReturn("");

        DeliveryBean deliveryBean = mock(DeliveryBean.class);
        when(deliveryBean.getAssessmentTitle()).thenReturn("Sample Quiz");
        when(deliveryBean.getIsMathJaxEnabled()).thenReturn(Boolean.FALSE);

        PrintSettingsBean printSettings = new PrintSettingsBean();
        FormattedText formattedText = mock(FormattedText.class);

        AssessmentPrintPdfModel model = AssessmentPdfSnapshotBuilder.buildPrintModel(
                deliveryBean, List.of(section), printSettings, "", formattedText);

        assertEquals("Kevin has 3 apples", model.getParts().get(0).getQuestions().get(0).getCalculatedQuestionText());
    }

    @Test
    public void buildStudentReportModelPreservesOverallComments() {
        DeliveryBean deliveryBean = studentReportDeliveryBean(matchingQuestionItem());
        StudentScoresBean studentScoresBean = studentReportScoresBean("Strong work overall.");

        AssessmentStudentReportPdfModel model = AssessmentPdfSnapshotBuilder.buildStudentReportModel(deliveryBean, studentScoresBean);

        assertEquals("Strong work overall.", model.getComments());
        assertTrue(model.hasComments());
    }

    @Test
    public void buildStudentReportModelMapsMatchingAnswers() {
        ItemContentsBean item = matchingQuestionItem();
        AnswerIfc matchingAnswer = mock(AnswerIfc.class);
        when(matchingAnswer.getId()).thenReturn(1L);
        ItemTextIfc itemText = mock(ItemTextIfc.class);
        when(itemText.getId()).thenReturn(1L);
        when(itemText.getAnswerSet()).thenReturn(java.util.Set.of(matchingAnswer));

        MatchingBean matchingBean = new MatchingBean();
        matchingBean.setItemContentsBean(item);
        matchingBean.setItemText(itemText);
        matchingBean.setText("Paris");
        matchingBean.setIsCorrect(Boolean.TRUE);
        java.util.List<SelectItem> choices = new java.util.ArrayList<>();
        choices.add(new SelectItem("1", "France"));
        choices.add(new SelectItem(null, "Unknown"));
        matchingBean.setChoices(choices);
        matchingBean.setResponse("1");

        item.setMatchingArray(java.util.List.of(matchingBean));

        DeliveryBean deliveryBean = studentReportDeliveryBean(item);
        StudentScoresBean studentScoresBean = studentReportScoresBean(null);

        AssessmentStudentReportPdfModel model = AssessmentPdfSnapshotBuilder.buildStudentReportModel(deliveryBean, studentScoresBean);
        AssessmentPdfMatchingRowModel row = model.getParts().get(0).getQuestions().get(0).getMatchingRows().get(0);

        assertEquals("Paris", row.getText());
        assertEquals("1", row.getResponse());
        assertEquals(2, row.getChoices().size());
        assertEquals("1", row.getChoices().get(0).getValue());
        assertEquals("France", row.getChoices().get(0).getLabel());
        assertNull(row.getChoices().get(1).getValue());
    }

    @Test
    public void buildStudentReportModelMapsRepresentativeSelectionAnswers() {
        AnswerIfc answer = mock(AnswerIfc.class);
        when(answer.getLabel()).thenReturn("A");
        when(answer.getText()).thenReturn("First choice");
        when(answer.getIsCorrect()).thenReturn(Boolean.TRUE);
        when(answer.getId()).thenReturn(1L);

        ItemTextIfc itemText = mock(ItemTextIfc.class);
        when(itemText.getId()).thenReturn(1L);

        ItemDataIfc itemData = mock(ItemDataIfc.class);
        when(itemData.getTypeId()).thenReturn(TypeIfc.MULTIPLE_CHOICE);
        when(itemData.getItemId()).thenReturn(1L);
        when(itemData.getItemTextSet()).thenReturn(java.util.Set.of(itemText));

        ItemContentsBean item = new ItemContentsBean();
        item.setItemData(itemData);
        item.setSequence("1");
        item.setItemGradingDataArray(new java.util.ArrayList<>());

        SelectionBean selectionBean = new SelectionBean();
        selectionBean.setItemContentsBean(item);
        selectionBean.setAnswer(answer);
        selectionBean.setResponse(true);
        item.setAnswers(java.util.List.of(selectionBean));

        DeliveryBean deliveryBean = studentReportDeliveryBean(item);
        StudentScoresBean studentScoresBean = studentReportScoresBean(null);

        AssessmentStudentReportPdfModel model = AssessmentPdfSnapshotBuilder.buildStudentReportModel(deliveryBean, studentScoresBean);
        AssessmentPdfSelectionAnswerModel selection = model.getParts().get(0).getQuestions().get(0).getSelectionAnswers().get(0);

        assertEquals("A", selection.getLabel());
        assertEquals("First choice", selection.getText());
        assertEquals(Boolean.TRUE, selection.getCorrect());
        assertTrue(selection.isSelected());
    }

    private static ItemContentsBean matchingQuestionItem() {
        ItemDataIfc itemData = mock(ItemDataIfc.class);
        when(itemData.getTypeId()).thenReturn(TypeIfc.MATCHING);
        when(itemData.getItemId()).thenReturn(1L);

        ItemTextIfc itemText = mock(ItemTextIfc.class);
        when(itemText.getId()).thenReturn(1L);
        when(itemText.getAnswerSet()).thenReturn(Collections.emptySet());

        ItemContentsBean item = new ItemContentsBean();
        item.setItemData(itemData);
        item.setSequence("1");
        item.setItemGradingDataArray(new java.util.ArrayList<>());

        return item;
    }

    private static DeliveryBean studentReportDeliveryBean(ItemContentsBean item) {
        SectionContentsBean section = mock(SectionContentsBean.class);
        when(section.getItemContents()).thenReturn(java.util.List.of(item));
        when(section.getAttachmentList()).thenReturn(Collections.emptyList());
        when(section.getTitle()).thenReturn("Part 1");
        when(section.getDescription()).thenReturn("");
        when(section.getNumber()).thenReturn("1");
        when(section.getQuestions()).thenReturn(1);
        when(section.getUnansweredQuestions()).thenReturn(0);
        when(section.getPoints()).thenReturn(1.0);
        when(section.getMaxPoints()).thenReturn(1.0);

        ContentsDeliveryBean pageContents = new ContentsDeliveryBean();
        pageContents.setPartsContents(java.util.List.of(section));

        ContentsDeliveryBean tableOfContents = new ContentsDeliveryBean();
        tableOfContents.setCurrentScore(8.0);
        tableOfContents.setMaxScore(10.0);

        DeliveryBean deliveryBean = mock(DeliveryBean.class);
        when(deliveryBean.getPageContents()).thenReturn(pageContents);
        when(deliveryBean.getTableOfContents()).thenReturn(tableOfContents);
        when(deliveryBean.getAssessmentTitle()).thenReturn("Sample Quiz");
        when(deliveryBean.getIsMathJaxEnabled()).thenReturn(Boolean.FALSE);
        return deliveryBean;
    }

    private static StudentScoresBean studentReportScoresBean(String comments) {
        StudentScoresBean studentScoresBean = mock(StudentScoresBean.class);
        when(studentScoresBean.getStudentName()).thenReturn("Student One");
        when(studentScoresBean.getFirstName()).thenReturn("Student");
        when(studentScoresBean.getEmail()).thenReturn("student@example.com");
        when(studentScoresBean.getComments()).thenReturn(comments);
        return studentScoresBean;
    }
}
