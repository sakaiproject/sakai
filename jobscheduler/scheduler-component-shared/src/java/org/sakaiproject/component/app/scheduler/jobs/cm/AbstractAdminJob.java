/**
 * Copyright (c) 2003 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler.jobs.cm;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import java.util.Date;
import java.util.List;

@Slf4j
abstract public class AbstractAdminJob implements Job {

    @Setter
    private SessionManager sessionManager;
    @Setter
    private SecurityService securityService;
    @Setter
    private String adminUser;

    /**
     * Child classes should implement their work in this method.
     *
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    protected abstract void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException;

    /**
     * Switch's the current user to the adminUser and pushes a SecurityAdvisor
     * onto the security stack to avoid authz issues. Then we invoke
     * executeInternal, and switch the current user back.
     *
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("{} starting...", this.getClass().getCanonicalName());

        Session sakaiSession = sessionManager.getCurrentSession();
        String currentUserId = sakaiSession.getUserId();

        sakaiSession.setUserId(adminUser);
        sakaiSession.setUserEid(adminUser);

        SecurityAdvisor securityAdvisor = new SecurityAdvisor() {
            @Override
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                if (userId != null && userId.equals(adminUser)) {
                    return SecurityAdvice.ALLOWED;
                }
                return SecurityAdvice.PASS;
            }
        };
        securityService.pushAdvisor(securityAdvisor);

        boolean jobRunning = false;
        try {
            List<JobExecutionContext> jobs = jobExecutionContext.getScheduler().getCurrentlyExecutingJobs();
            for (JobExecutionContext job : jobs) {
                // compare using job key but ensure its a different instance than the current one
                if (job.getJobDetail().getKey().equals(jobExecutionContext.getJobDetail().getKey())
                        && !job.getFireInstanceId().equals(jobExecutionContext.getFireInstanceId())) {
                    jobRunning = true;
                }
            }
        } catch (SchedulerException e) {
            log.warn("Could not get currently executing jobs.", e);
        }

        Date now = new Date();

        if (jobRunning) {
            log.warn("There's another instance of job {} running, cancelling this instance {}", jobExecutionContext.getJobDetail().getKey(), jobExecutionContext.getFireInstanceId());
        } else {
            executeInternal(jobExecutionContext);
        }

        Date later = new Date();

        sakaiSession.setUserId(currentUserId);
        sakaiSession.setUserEid(currentUserId);

        log.info("{} finished in {} millis", this.getClass().getCanonicalName(), (later.getTime() - now.getTime()));
    }
}
