/*
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
package org.sakaiproject.attendance.services;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.Job;
import org.quartz.JobExecutionException;
import org.sakaiproject.attendance.test.AttendanceTestConfiguration;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AttendanceTestConfiguration.class)
public class AttendanceStatCalcJobTest {
    @Autowired @Qualifier("AttendanceStatCalc") private Job job;
    @Autowired private SessionManager sessionManager;
    @Autowired private UsageSessionService usageSessionService;
    @Autowired private EventTrackingService eventTrackingService;

    @After
    public void resetBoundaryServices() {
        reset(sessionManager, usageSessionService, eventTrackingService);
    }

    @Test
    public void restoresSessionWhenUsageLoginFails() {
        Session previous = mock(Session.class);
        Session current = mock(Session.class);
        when(sessionManager.getCurrentSession()).thenReturn(previous);
        when(sessionManager.startSession()).thenReturn(current);
        assertThrows(JobExecutionException.class, () -> job.execute(null));
        verify(current).invalidate();
        verify(sessionManager).setCurrentSession(previous);
        verify(usageSessionService, never()).logout();
    }

    @Test
    public void restoresSessionEvenWhenTrackingAndLogoutFail() {
        Session previous = mock(Session.class);
        Session current = mock(Session.class);
        when(sessionManager.getCurrentSession()).thenReturn(previous);
        when(sessionManager.startSession()).thenReturn(current);
        when(usageSessionService.startSession(anyString(), isNull(), anyString())).thenReturn(mock(UsageSession.class));
        doThrow(new IllegalStateException("Tracking unavailable")).when(eventTrackingService).post(any());
        doThrow(new IllegalStateException("Logout unavailable")).when(usageSessionService).logout();
        assertThrows(IllegalStateException.class, () -> job.execute(null));
        verify(usageSessionService).logout();
        verify(current).invalidate();
        verify(sessionManager).setCurrentSession(previous);
    }
}
