/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler.jobs.autoimport;

import java.util.UUID;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;


/**
 * This imports a folder into Sakai. The folder is stored inside the archive folder.
 */
@Slf4j
public class ImportJob implements Job {

    private ArchiveService archiveService;

    private SessionManager sessionManager;

    private String zip;
    private String siteId;

    @Inject
    public void setArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @Inject
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * @param siteId The site ID to import the site into or leave unset to generate an random one.
     */
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        if (siteId == null) {
            siteId = UUID.randomUUID().toString();
        }

        log.info("Attempting to import: " + zip+ " into "+ siteId);
        Session currentSession = sessionManager.getCurrentSession();
        String oldId = currentSession.getUserId();
        String oldEid = currentSession.getUserEid();
        try {
            currentSession.setUserId("admin");
            currentSession.setUserEid("admin");
            archiveService.mergeFromZip(zip, siteId, null);
        } catch (Exception e) {
            log.warn("Failed to import " + zip + " to " + siteId + " " + e.getMessage());
        } finally {
            currentSession.setUserId(oldId);
            currentSession.setUserEid(oldEid);
        }

    }


}
