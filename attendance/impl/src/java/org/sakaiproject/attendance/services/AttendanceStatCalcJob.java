/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.services;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;


/**
 * The AttendanceStatCalc Job calculates all AttendanceUserStats and all AttendanceItemStats
 * for each AttendanceSite in batches of 5 Attendance Sites.
 *
 * The job should be run once after upgrading past Attendance 1.0. Afterwards, it should be run
 * only in cases where the statistics got out of sync (very very rare).
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Slf4j
@DisallowConcurrentExecution
public class AttendanceStatCalcJob implements Job {
            private   String serverName   = "N/A";
    final   private   String jobName      = "AttendanceStatCalcJob";

    public AttendanceStatCalcJob() {
        super();
    }

    public void init() {
        log.info(jobName + " init.");
        serverName = serverConfigurationService.getServerName();
    }

    public void destroy() {
        log.info(jobName +"  D E S T R O Y E D.");
    }

    public void execute(JobExecutionContext context) throws org.quartz.JobExecutionException {
        Session previousSession = sessionManager.getCurrentSession();
        Session jobSession = sessionManager.startSession();
        sessionManager.setCurrentSession(jobSession);
        boolean loggedIn = false;
        try {
            UsageSession usageSession = usageSessionService.startSession("admin", serverName, jobName);
            if (usageSession == null) {
                throw new org.quartz.JobExecutionException("Unable to create attendance statistics usage session");
            }
            loggedIn = true;
            jobSession.setUserId("admin");
            jobSession.setUserEid("admin");
            authzGroupService.refreshUser("admin");
            eventTrackingService.post(eventTrackingService.newEvent(UsageSessionService.EVENT_LOGIN,
                    "admin running " + serverName, true));
            String description = "Job: " + context.getJobDetail().getKey() + " Trigger: " + context.getTrigger().getKey();
            eventTrackingService.post(eventTrackingService.newEvent("attend.calc.job", safeEventLength(description), true));
            attendanceStatCalc.execute();
        } finally {
            try {
                if (loggedIn) {
                    try {
                        eventTrackingService.post(eventTrackingService.newEvent(UsageSessionService.EVENT_LOGOUT, null, true));
                    } finally {
                        usageSessionService.logout();
                    }
                }
            } finally {
                try {
                    jobSession.invalidate();
                } finally {
                    sessionManager.setCurrentSession(previousSession);
                }
            }
        }
    }

    static public String safeEventLength(final String target)
    {
        return (target.length() > 255 ? target.substring(0, 255) : target);
    }

    @Setter
    private ServerConfigurationService serverConfigurationService;

    @Setter
    private AttendanceStatCalcService attendanceStatCalc;

    @Setter
    private EventTrackingService eventTrackingService;

    @Setter
    private UsageSessionService usageSessionService;

    @Setter
    private AuthzGroupService authzGroupService;

    @Setter
    private SessionManager sessionManager;
}
