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

package org.sakaiproject.datemanager.impl;

import org.sakaiproject.datemanager.api.DateManagerConstants;
import org.sakaiproject.tool.assessment.util.AssessmentFeedbackDateValidator;

final class AssessmentFeedbackDateValidationSupport {

  private AssessmentFeedbackDateValidationSupport() {
  }

  static String dateManagerMessageKey(AssessmentFeedbackDateValidator.Error error) {
    switch (error) {
      case FEEDBACK_END_BEFORE_START:
        return "error.feedback.start.before.feedback.end";
      case MISSING_DUE_DATE:
        return "error.feedback.due.date.required";
      case FEEDBACK_START_BEFORE_DUE_DATE:
        return "error.feedback.start.before.due.date";
      case FEEDBACK_END_BEFORE_DUE_DATE:
        return "error.feedback.end.before.due.date";
      default:
        throw new IllegalArgumentException("Unknown feedback date validation error: " + error);
    }
  }

  static String dateManagerField(AssessmentFeedbackDateValidator.Field field) {
    switch (field) {
      case DUE_DATE:
        return DateManagerConstants.JSON_DUEDATE_PARAM_NAME;
      case FEEDBACK_END:
        return DateManagerConstants.JSON_FEEDBACKEND_PARAM_NAME;
      case FEEDBACK_START:
      default:
        return DateManagerConstants.JSON_FEEDBACKSTART_PARAM_NAME;
    }
  }
}
