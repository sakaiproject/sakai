package org.sakaiproject.component.app.scheduler.jobs.autoimport;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.quartz.*;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Attempts to download a file that lists all the site archives to import automatically.
 */
public class GetArchivesJob implements Job {

    private final Logger log = LoggerFactory.getLogger(GetArchivesJob.class);

    private ServerConfigurationService serverConfigurationService;

    private SchedulerManager schedulerManager;

    @Inject
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    @Inject
    public void setSchedulerManager(SchedulerManager schedulerManager) {
        this.schedulerManager = schedulerManager;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        String source = serverConfigurationService.getString("archives.import.source", null);
        if (source == null) {
            return;
        }

        log.info("Attempting to import archives listed in: "+ source);
        try {
            URL url = new URL(source);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Sakai Content Importer");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            // Now make the connection.
            connection.connect();
            try (InputStream inputStream = connection.getInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        importArchive(line);
                    }
                }
            }


            String sakaiHome = serverConfigurationService.getSakaiHomePath();
            String archiveHome = sakaiHome + "archive";

            // Find all the folders and load them.
            File archiveDirectory = new File(archiveHome);
            File[] files = archiveDirectory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
            if (files == null) {
                return;
            }
            for (File dir: files) {
                String dirName = dir.getName();
                JobDataMap jobData = new JobDataMap();
                jobData.put("folder", dirName);
                JobDetail jobDetail = JobBuilder.newJob(ImportJob.class)
                        .withIdentity("Import Job")
                        .setJobData(jobData)
                        .build();
                Scheduler scheduler = schedulerManager.getScheduler();
                try {
                    scheduler.addJob(jobDetail, true, true);
                    scheduler.triggerJob(jobDetail.getKey());
                } catch (SchedulerException e) {
                    log.warn("Problem adding job to scheduler to import "+ dirName, e);
                }
            }

        } catch (IOException ioe) {
            log.warn("Problem with "+ source + " "+ ioe.getMessage());
        }
    }

    private void importArchive(String archive) {
        String sakaiHome = serverConfigurationService.getSakaiHomePath();
        String archiveHome = sakaiHome + "archive";

        if (archive == null || archive.trim().length() == 0) {
            log.warn("Empty archive setting.");
            return;
        }
        log.info("Attempting to import: "+ archive);
        try {
            URL url = new URL(archive);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Sakai Content Importer");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            // Now make the connection.
            connection.connect();
            try (InputStream inputStream = connection.getInputStream()) {
                List<ZipError> errors = ZipUtils.expandZip(inputStream, archiveHome);
                for (ZipError error : errors) {
                    log.info(error.toString());
                }
            }
        } catch (IOException ioe) {
            log.warn("Problem with "+ archive+ " "+ ioe.getMessage());
        }
    }
}
