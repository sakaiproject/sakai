package org.sakaiproject.component.app.scheduler.jobs.autoimport;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.io.File;

/**
 * This imports a folder into Sakai. The folder is stored inside the archive folder.
 */
public class ImportJob implements Job {

    private final Logger log = LoggerFactory.getLogger(ImportJob.class);

    private ServerConfigurationService serverConfigurationService;

    private ArchiveService archiveService;

    private SiteService siteService;

    private SessionManager sessionManager;

    private String folder;

    @Inject
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    @Inject
    public void setArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @Inject
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Inject
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String sakaiHome = serverConfigurationService.getSakaiHomePath();
        String archiveHome = sakaiHome + "archive";
        File archiveDirectory = new File(archiveHome);

        File dir = new File(archiveDirectory, folder);
        if (!dir.exists()) {
            log.warn("Could not find {}, not importing.", dir);
            return;
        }
        String siteId = trimArchive(folder);
        if (siteService.siteExists(siteId)) {
            log.info("Site already exists, not importing: " + siteId);
            return;
        }

        log.info("Attempting to import: " + folder);
        Session currentSession = sessionManager.getCurrentSession();
        String oldId = currentSession.getUserId();
        String oldEid = currentSession.getUserEid();
        try {
            currentSession.setUserId("admin");
            currentSession.setUserEid("admin");
            archiveService.merge(dir.getName(), siteId, null);
        } catch (Exception e) {
            log.warn("Failed to import " + dir.getAbsolutePath() + " to " + siteId + " " + e.getMessage());
        } finally {
            currentSession.setUserId(oldId);
            currentSession.setUserEid(oldEid);
        }

    }

    private String trimArchive(String siteId) {
        if (siteId != null && siteId.endsWith("-archive")) {
            siteId = siteId.substring(0, siteId.length() - "-archive".length());
        }
        return siteId;
    }
}
