package org.sakaiproject.component.app.scheduler.jobs.autoimport;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.UUID;

/**
 * This imports a folder into Sakai. The folder is stored inside the archive folder.
 */
public class ImportJob implements Job {

    private final Logger log = LoggerFactory.getLogger(ImportJob.class);

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
