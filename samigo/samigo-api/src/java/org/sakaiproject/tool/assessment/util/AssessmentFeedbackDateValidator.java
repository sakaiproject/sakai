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

package org.sakaiproject.tool.assessment.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;

public final class AssessmentFeedbackDateValidator {

  public enum Error {
    FEEDBACK_END_BEFORE_START,
    MISSING_DUE_DATE,
    FEEDBACK_START_BEFORE_DUE_DATE,
    FEEDBACK_END_BEFORE_DUE_DATE
  }

  public enum Field {
    DUE_DATE,
    FEEDBACK_START,
    FEEDBACK_END
  }

  public static final class Violation {
    private final Error error;
    private final Field field;

    private Violation(Error error, Field field) {
      this.error = error;
      this.field = field;
    }

    public Error getError() {
      return error;
    }

    public Field getField() {
      return field;
    }
  }

  private AssessmentFeedbackDateValidator() {
  }

  public static List<Violation> validate(Date dueDate, Date retractDate, Integer lateHandling, Date feedbackStartDate, Date feedbackEndDate) {
    List<Violation> violations = new ArrayList<>();

    if (feedbackStartDate != null && feedbackEndDate != null && feedbackStartDate.after(feedbackEndDate)) {
      violations.add(violation(Error.FEEDBACK_END_BEFORE_START));
    }

    boolean hasFeedbackDate = feedbackStartDate != null || feedbackEndDate != null;
    Date cutoffDate = getFeedbackCutoffDate(dueDate, retractDate, lateHandling);
    if (cutoffDate == null) {
      if (hasFeedbackDate) {
        violations.add(violation(Error.MISSING_DUE_DATE));
      }
      return violations;
    }

    if (feedbackStartDate != null && feedbackStartDate.before(cutoffDate)) {
      violations.add(violation(Error.FEEDBACK_START_BEFORE_DUE_DATE));
    }

    if (feedbackEndDate != null && feedbackEndDate.before(cutoffDate)) {
      violations.add(violation(Error.FEEDBACK_END_BEFORE_DUE_DATE));
    }

    return violations;
  }

  public static Date getFeedbackCutoffDate(Date dueDate, Date retractDate, Integer lateHandling) {
    if (dueDate == null) {
      return null;
    }

    if (AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.equals(lateHandling) && retractDate != null && retractDate.after(dueDate)) {
      return retractDate;
    }

    return dueDate;
  }

  private static Violation violation(Error error) {
    switch (error) {
      case FEEDBACK_END_BEFORE_START:
        return new Violation(error, Field.FEEDBACK_END);
      case MISSING_DUE_DATE:
        return new Violation(error, Field.DUE_DATE);
      case FEEDBACK_START_BEFORE_DUE_DATE:
        return new Violation(error, Field.FEEDBACK_START);
      case FEEDBACK_END_BEFORE_DUE_DATE:
        return new Violation(error, Field.FEEDBACK_END);
      default:
        throw new IllegalArgumentException("Unknown feedback date validation error: " + error);
    }
  }
}
