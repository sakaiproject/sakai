/**
 * Copyright (c) 2005-2026 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.util;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.api.LocaleService;

/**
 * SAK-34476: when feedback is shown "on specific dates", a feedback date earlier than the
 * last moment a student can submit silently behaves like immediate feedback — anyone taking
 * the assessment after the feedback date can open the Feedback link and see the correct
 * answers mid-take. The deadline has to account for every exception, since an extended
 * due/retract date can reopen the assessment past an otherwise safe feedback date.
 */
public final class FeedbackDateValidator {

    private static final String BUNDLE = "org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages";

    private FeedbackDateValidator() {}

    /**
     * The latest moment any student can still submit: the due date, or the late acceptance
     * date when late submissions are on, pushed out by the due/retract date of every
     * exception. Null when there is no deadline at all - including when late submissions
     * are accepted without a late acceptance date, because a student who has not submitted
     * yet may then still begin at any time after the due date.
     */
    public static Date latestSubmissionDeadline(Date dueDate, Date retractDate, String lateHandling, List<ExtendedTime> extendedTimes) {
        boolean acceptLate = AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.toString().equals(lateHandling);
        // The base cohort (students without an exception). With late submissions accepted the
        // deadline is the late acceptance date; otherwise the due date. A null here means the
        // assessment is open-ended for regular students, so there is no safe feedback date at all.
        Date deadline = acceptLate ? retractDate : dueDate;
        if (deadline == null) {
            return null;
        }
        if (extendedTimes != null) {
            for (ExtendedTime entry : extendedTimes) {
                // An exception replaces the base dates wholesale at delivery time, evaluated under
                // the same lateHandling, so its deadline mirrors the base rule. A null here means
                // this exception's holder is open-ended - poison the whole deadline to null.
                Date entryDeadline = acceptLate ? entry.getRetractDate() : entry.getDueDate();
                if (entryDeadline == null) {
                    return null;
                }
                if (entryDeadline.after(deadline)) {
                    deadline = entryDeadline;
                }
            }
        }
        return deadline;
    }

    /**
     * Validate that the feedback date falls on or after the latest submission deadline,
     * adding an error message to the context when it does not.
     *
     * @return true if the feedback date is acceptable; false if saving must be blocked
     */
    public static boolean isFeedbackDateAfterDeadline(Date feedbackDate, Date dueDate, Date retractDate,
            String lateHandling, List<ExtendedTime> extendedTimes, FacesContext context) {
        if (feedbackDate == null) {
            // a missing or unparseable date is already reported by the existing checks
            return true;
        }
        Date deadline = latestSubmissionDeadline(dueDate, retractDate, lateHandling, extendedTimes);
        if (deadline == null) {
            context.addMessage(null, new FacesMessage(ContextUtil.getLocalizedString(BUNDLE, "feedback_date_requires_deadline")));
            return false;
        }
        if (feedbackDate.before(deadline)) {
            UserTimeService userTimeService = ComponentManager.get(UserTimeService.class);
            Locale locale = ComponentManager.get(LocaleService.class).getLocaleForCurrentSiteAndUser();
            String deadlineDisplay = userTimeService.dateTimeFormat(deadline, locale, DateFormat.LONG);
            String msg = MessageFormat.format(ContextUtil.getLocalizedString(BUNDLE, "feedback_date_earlier_than_deadline"), deadlineDisplay);
            context.addMessage(null, new FacesMessage(msg));
            return false;
        }
        return true;
    }
}
