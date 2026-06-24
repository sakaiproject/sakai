/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.qti.helper;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.qti.util.Iso8601DateFormat;
import org.sakaiproject.tool.assessment.qti.exception.Iso8601FormatException;
import org.sakaiproject.tool.assessment.util.AssessmentFeedbackDateValidator;

public final class AssessmentFeedbackDatesImportHelper {

  private AssessmentFeedbackDatesImportHelper() {
  }

  public static void applyImportedFeedbackDates(AssessmentFacade assessment, AssessmentAccessControl control,
      Iso8601DateFormat iso, String feedbackDate, String feedbackEndDate, Logger log) {
    if (StringUtils.isBlank(feedbackDate) && StringUtils.isBlank(feedbackEndDate)) {
      return;
    }

    Date parsedFeedbackDate;
    Date parsedFeedbackEndDate;
    try {
      parsedFeedbackDate = parseOptionalFeedbackDate(iso, feedbackDate);
      parsedFeedbackEndDate = parseOptionalFeedbackDate(iso, feedbackEndDate);
    } catch (Iso8601FormatException ex) {
      log.debug("Cannot set feedbackDate.");
      return;
    }

    List<AssessmentFeedbackDateValidator.Violation> feedbackDateErrors = AssessmentFeedbackDateValidator.validate(
        control.getDueDate(), control.getRetractDate(), control.getLateHandling(), parsedFeedbackDate, parsedFeedbackEndDate);
    if (!feedbackDateErrors.isEmpty()) {
      log.warn("Skipping imported feedback dates that fail validation: {}", feedbackDateErrors);
      return;
    }

    control.setFeedbackDate(parsedFeedbackDate);
    control.setFeedbackEndDate(parsedFeedbackEndDate);
    assessment.getData().addAssessmentMetaData("FEEDBACK_DELIVERY", "DATED");
  }

  private static Date parseOptionalFeedbackDate(Iso8601DateFormat iso, String feedbackDate) throws Iso8601FormatException {
    if (StringUtils.isBlank(feedbackDate)) {
      return null;
    }
    return iso.parse(feedbackDate).getTime();
  }
}
