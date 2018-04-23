/*
  Copyright (c) 2003-2018 The Apereo Foundation

  Licensed under the Educational Community License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

              http://opensource.org/licenses/ecl2

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.sakaiproject.assignment.impl.reminder;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Job which when runs will send Email Reminders for Assignments due in 24 Hours
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Slf4j
public class AutoSendAssignmentDueRemindersJob implements Job {


    public void init() {
        log.debug("AutoSendAssignmentDueRemindersJob init()");
    }

    public void destroy() {
        log.debug("AutoSendAssignmentDueRemindersJob destroy()");
    }

    public AutoSendAssignmentDueRemindersJob(){
        super();
    }

    @Override
    public void execute(JobExecutionContext jobInfo) throws JobExecutionException {
        log.debug("AutoSendAssignmentDueRemindersJob execute()");
        loginToSakai();

        reminders.execute();

        logoutFromSakai();
    }

    private void loginToSakai() {
        log.debug("AutoSendAssignmentDueRemindersJob loginToSakai()");
        String serverName = ServerConfigurationService.getServerName();
        String admin = "admin";


        UsageSession session = usageSessionService.startSession(admin, serverName, "AutoSubmitAssessmentsJob");
        if (session == null)
        {
            eventTrackingService.post(eventTrackingService.newEvent("asn.auto-reminder.job.error", admin + " unable to log into " + serverName, true));
            return;
        }

        Session sakaiSession = sessionManager.getCurrentSession();
        sakaiSession.setUserId(admin);
        sakaiSession.setUserEid(admin);

        // update the user's externally provided realm definitions
        authzGroupService.refreshUser(admin);

        // post the login events
        eventTrackingService.post(eventTrackingService.newEvent(UsageSessionService.EVENT_LOGIN, admin + " running " + serverName, true));
    }

    private void logoutFromSakai() {
        String serverName = ServerConfigurationService.getServerName();
        log.debug(" AutoSubmitAssessmentsJob Logging out of Sakai on " + serverName);
        eventTrackingService.post(eventTrackingService.newEvent(UsageSessionService.EVENT_LOGOUT, null, true));
        usageSessionService.logout(); // safe to logout? what if other jobs are running?
    }

    @Setter
    private AutoSendAssignmentDueReminders reminders;

    @Setter
    private EventTrackingService eventTrackingService;

    @Setter
    private UsageSessionService usageSessionService;

    @Setter
    private AuthzGroupService authzGroupService;

    @Setter
    private SessionManager sessionManager;
}
