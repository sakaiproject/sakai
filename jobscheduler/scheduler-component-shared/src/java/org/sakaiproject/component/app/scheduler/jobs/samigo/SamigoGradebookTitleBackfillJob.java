/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler.jobs.samigo;

import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.sakaiproject.samigo.api.SamigoGradebookTitleRepairService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Scheduler wrapper for {@link SamigoGradebookTitleRepairService}.
 */
public class SamigoGradebookTitleBackfillJob implements InterruptableJob {

    private static final String APPLY = "apply";
    private static final String SITE_ID = "site.id";
    private static final String LOG_CHANGES = "log.changes";

    private SessionManager sessionManager;
    private SamigoGradebookTitleRepairService repairService;
    private volatile boolean run = true;

    @Autowired
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Autowired
    public void setRepairService(SamigoGradebookTitleRepairService repairService) {
        this.repairService = repairService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();
        boolean apply = Boolean.parseBoolean(data.getString(APPLY));
        boolean logChanges = Boolean.parseBoolean(data.getString(LOG_CHANGES));
        String siteId = data.getString(SITE_ID);

        Session session = sessionManager.getCurrentSession();
        try {
            session.setUserEid("admin");
            session.setUserId("admin");

            repairService.repair(apply, logChanges, siteId, () -> run);
        } finally {
            session.clear();
            run = true;
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        run = false;
    }
}
