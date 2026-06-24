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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.AssessmentFeedbackDateValidator;

final class AssessmentFeedbackDateValidationHelper {

  private static final String ASSESSMENT_SETTINGS_MESSAGES = "org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages";
  private static final String GENERAL_MESSAGES = "org.sakaiproject.tool.assessment.bundle.GeneralMessages";

  private AssessmentFeedbackDateValidationHelper() {
  }

  static boolean isFeedbackByDate(AssessmentSettingsBean settings) {
    return isFeedbackByDate(settings.getFeedbackDelivery());
  }

  static boolean isFeedbackByDate(PublishedAssessmentSettingsBean settings) {
    return isFeedbackByDate(settings.getFeedbackDelivery());
  }

  static boolean addFeedbackDateErrors(FacesContext context, AssessmentSettingsBean settings) {
    return addFeedbackDateErrors(context, settings.getFeedbackDelivery(), settings.getFeedbackDateString(),
        settings.getIsValidFeedbackDate(), settings.getIsValidFeedbackEndDate(), settings.getDueDate(),
        settings.getRetractDate(), settings.getLateHandling(), settings.getFeedbackDate(), settings.getFeedbackEndDate());
  }

  static boolean addFeedbackDateErrors(FacesContext context, PublishedAssessmentSettingsBean settings) {
    return addFeedbackDateErrors(context, settings.getFeedbackDelivery(), settings.getFeedbackDateString(),
        settings.getIsValidFeedbackDate(), settings.getIsValidFeedbackEndDate(), settings.getDueDate(),
        settings.getRetractDate(), settings.getLateHandling(), settings.getFeedbackDate(), settings.getFeedbackEndDate());
  }

  private static boolean isFeedbackByDate(String feedbackDelivery) {
    return AssessmentFeedbackIfc.FEEDBACK_BY_DATE.toString().equals(feedbackDelivery);
  }

  private static boolean addFeedbackDateErrors(FacesContext context, String feedbackDelivery, String feedbackDateString,
      boolean validFeedbackDate, boolean validFeedbackEndDate, Date dueDate, Date retractDate, String lateHandling,
      Date feedbackDate, Date feedbackEndDate) {

    if (!isFeedbackByDate(feedbackDelivery)) {
      return false;
    }

    boolean error = false;
    if (StringUtils.isBlank(feedbackDateString)) {
      addAssessmentSettingsMessage(context, "date_error");
      error = true;
    } else {
      Date feedbackStartForValidation = validFeedbackDate ? feedbackDate : null;
      Date feedbackEndForValidation = validFeedbackEndDate ? feedbackEndDate : null;
      Set<String> messageKeys = new LinkedHashSet<>();
      for (AssessmentFeedbackDateValidator.Violation violation : AssessmentFeedbackDateValidator.validate(dueDate,
          retractDate, parseLateHandling(lateHandling), feedbackStartForValidation, feedbackEndForValidation)) {
        messageKeys.add(samigoMessageKey(violation.getError()));
      }
      for (String messageKey : messageKeys) {
        addGeneralMessage(context, messageKey);
        error = true;
      }
    }

    if (!validFeedbackDate) {
      addGeneralMessage(context, "invalid_feedback_date");
      error = true;
    }
    if (!validFeedbackEndDate) {
      addGeneralMessage(context, "invalid_feedback_end_date");
      error = true;
    }

    return error;
  }

  private static Integer parseLateHandling(String lateHandling) {
    if (lateHandling == null) {
      return null;
    }

    try {
      return Integer.valueOf(lateHandling);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static String samigoMessageKey(AssessmentFeedbackDateValidator.Error error) {
    switch (error) {
      case FEEDBACK_END_BEFORE_START:
        return "invalid_feedback_ranges";
      case MISSING_DUE_DATE:
        return "invalid_feedback_dates_missing_due";
      case FEEDBACK_START_BEFORE_DUE_DATE:
      case FEEDBACK_END_BEFORE_DUE_DATE:
        return "invalid_feedback_dates_before_due";
      default:
        throw new IllegalArgumentException("Unknown feedback date validation error: " + error);
    }
  }

  private static void addAssessmentSettingsMessage(FacesContext context, String key) {
    context.addMessage(null, new FacesMessage(ContextUtil.getLocalizedString(ASSESSMENT_SETTINGS_MESSAGES, key)));
  }

  private static void addGeneralMessage(FacesContext context, String key) {
    context.addMessage(null, new FacesMessage(ContextUtil.getLocalizedString(GENERAL_MESSAGES, key)));
  }
}
