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
package org.sakaiproject.tool.assessment.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class ExtendedTimeValidatorTest {

    @Test
    public void rejectsExceptionDueAfterFeedbackDate() {
        try (MockedStatic<ContextUtil> contextUtil = mockStatic(ContextUtil.class)) {
            contextUtil.when(() -> ContextUtil.getLocalizedString(anyString(), anyString()))
                    .thenAnswer(invocation -> invocation.getArgument(1));

            FacesContext facesContext = mock(FacesContext.class);
            AssessmentSettingsBean settings = feedbackByDateSettings(date("2030-06-27T00:00:00Z"));

            ExtendedTime entry = exceptionEntry(date("2030-06-01T00:00:00Z"), date("2030-06-28T00:00:00Z"), null);

            assertFalse(new ExtendedTimeValidator().validateEntry(entry, facesContext, settings));
            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
        }
    }

    @Test
    public void allowsExceptionDueOnFeedbackDate() {
        try (MockedStatic<ContextUtil> contextUtil = mockStatic(ContextUtil.class)) {
            contextUtil.when(() -> ContextUtil.getLocalizedString(anyString(), anyString()))
                    .thenAnswer(invocation -> invocation.getArgument(1));

            FacesContext facesContext = mock(FacesContext.class);
            Date feedbackDate = date("2030-06-28T00:00:00Z");
            AssessmentSettingsBean settings = feedbackByDateSettings(feedbackDate);

            ExtendedTime entry = exceptionEntry(date("2030-06-01T00:00:00Z"), feedbackDate, null);

            assertTrue(new ExtendedTimeValidator().validateEntry(entry, facesContext, settings));
        }
    }

    @Test
    public void rejectsExceptionLateDeadlineAfterFeedbackDate() {
        try (MockedStatic<ContextUtil> contextUtil = mockStatic(ContextUtil.class)) {
            contextUtil.when(() -> ContextUtil.getLocalizedString(anyString(), anyString()))
                    .thenAnswer(invocation -> invocation.getArgument(1));

            FacesContext facesContext = mock(FacesContext.class);
            AssessmentSettingsBean settings = feedbackByDateSettings(date("2030-06-28T00:00:00Z"));
            when(settings.getLateHandling()).thenReturn(AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.toString());

            ExtendedTime entry = exceptionEntry(date("2030-06-01T00:00:00Z"), date("2030-06-26T00:00:00Z"),
                    date("2030-06-30T00:00:00Z"));

            assertFalse(new ExtendedTimeValidator().validateEntry(entry, facesContext, settings));
            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
        }
    }

    private AssessmentSettingsBean feedbackByDateSettings(Date feedbackDate) {
        AssessmentSettingsBean settings = mock(AssessmentSettingsBean.class);
        when(settings.getLateHandling()).thenReturn(AssessmentAccessControlIfc.NOT_ACCEPT_LATE_SUBMISSION.toString());
        when(settings.getFeedbackDelivery()).thenReturn(AssessmentFeedbackIfc.FEEDBACK_BY_DATE.toString());
        when(settings.getFeedbackDate()).thenReturn(feedbackDate);
        when(settings.getStartDate()).thenReturn(date("2030-06-01T00:00:00Z"));
        when(settings.getUsersInSite()).thenReturn(new SelectItem[] { new SelectItem("user1", "User One") });
        when(settings.getGroupsForSite()).thenReturn(new SelectItem[] {});
        return settings;
    }

    private ExtendedTime exceptionEntry(Date startDate, Date dueDate, Date retractDate) {
        ExtendedTime entry = new ExtendedTime();
        entry.setUser("user1");
        entry.setStartDate(startDate);
        entry.setDueDate(dueDate);
        entry.setRetractDate(retractDate);
        return entry;
    }

    private Date date(String value) {
        return Date.from(Instant.parse(value));
    }
}
