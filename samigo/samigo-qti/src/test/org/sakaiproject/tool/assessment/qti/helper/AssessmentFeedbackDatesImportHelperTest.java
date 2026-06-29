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
package org.sakaiproject.tool.assessment.qti.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.qti.util.Iso8601DateFormat;

@Slf4j
public class AssessmentFeedbackDatesImportHelperTest {

  @Test
  public void movesImportedFeedbackDatesBeforeDueDateToDueDate() {
    AssessmentFacade assessment = assessment();
    AssessmentAccessControl control = control(date("2026-06-25T00:00:00Z"), null, null);

    apply(assessment, control, "2026-06-20T00:00:00Z", "2026-06-22T00:00:00Z");

    assertDateEquals("2026-06-25T00:00:00Z", control.getFeedbackDate());
    assertDateEquals("2026-06-27T00:00:00Z", control.getFeedbackEndDate());
    assertEquals(AssessmentFeedbackIfc.FEEDBACK_BY_DATE, assessment.getData().getAssessmentFeedback().getFeedbackDelivery());
    assertEquals("DATED", assessment.getData().getAssessmentMetaDataByLabel("FEEDBACK_DELIVERY"));
  }

  @Test
  public void usesLateSubmissionDateWhenItIsAfterDueDate() {
    AssessmentFacade assessment = assessment();
    AssessmentAccessControl control = control(date("2026-06-25T00:00:00Z"), date("2026-06-28T00:00:00Z"),
        AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION);

    apply(assessment, control, "2026-06-20T00:00:00Z", "2026-06-22T00:00:00Z");

    assertDateEquals("2026-06-28T00:00:00Z", control.getFeedbackDate());
    assertDateEquals("2026-06-30T00:00:00Z", control.getFeedbackEndDate());
  }

  @Test
  public void preservesImportedFeedbackDatesAfterDueDate() {
    AssessmentFacade assessment = assessment();
    AssessmentAccessControl control = control(date("2026-06-25T00:00:00Z"), null, null);

    apply(assessment, control, "2026-06-26T00:00:00Z", "2026-06-28T00:00:00Z");

    assertDateEquals("2026-06-26T00:00:00Z", control.getFeedbackDate());
    assertDateEquals("2026-06-28T00:00:00Z", control.getFeedbackEndDate());
    assertEquals(AssessmentFeedbackIfc.FEEDBACK_BY_DATE, assessment.getData().getAssessmentFeedback().getFeedbackDelivery());
    assertEquals("DATED", assessment.getData().getAssessmentMetaDataByLabel("FEEDBACK_DELIVERY"));
  }

  @Test
  public void removesImportedFeedbackDatesWhenDueDateIsMissing() {
    AssessmentFacade assessment = assessment();
    AssessmentAccessControl control = control(null, null, null);

    apply(assessment, control, "2026-06-20T00:00:00Z", "2026-06-22T00:00:00Z");

    assertNull(control.getFeedbackDate());
    assertNull(control.getFeedbackEndDate());
    assertEquals(AssessmentFeedbackIfc.NO_FEEDBACK, assessment.getData().getAssessmentFeedback().getFeedbackDelivery());
    assertEquals("NONE", assessment.getData().getAssessmentMetaDataByLabel("FEEDBACK_DELIVERY"));
  }

  private void apply(AssessmentFacade assessment, AssessmentAccessControl control, String feedbackDate, String feedbackEndDate) {
    AssessmentFeedbackDatesImportHelper.applyImportedFeedbackDates(assessment, control, new Iso8601DateFormat(), feedbackDate, feedbackEndDate,
        log);
  }

  private AssessmentFacade assessment() {
    AssessmentFacade assessment = new AssessmentFacade();
    assessment.getData().setAssessmentMetaDataSet(new HashSet<>());
    assessment.getData().setAssessmentFeedback(new AssessmentFeedback());
    return assessment;
  }

  private AssessmentAccessControl control(Date dueDate, Date retractDate, Integer lateHandling) {
    AssessmentAccessControl control = new AssessmentAccessControl();
    control.setDueDate(dueDate);
    control.setRetractDate(retractDate);
    control.setLateHandling(lateHandling);
    return control;
  }

  private Date date(String value) {
    return Date.from(Instant.parse(value));
  }

  private void assertDateEquals(String expected, Date actual) {
    assertEquals(date(expected).getTime(), actual.getTime());
  }
}
