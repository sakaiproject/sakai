package org.sakaiproject.component.app.scheduler.jobs.autoimport;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Attempts to download a file that lists all the site archives to import automatically.
 */
public class GetArchivesJob implements Job {

    private final Logger log = LoggerFactory.getLogger(GetArchivesJob.class);

    private final Pattern uuidRegex = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

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
                        String file = downloadArchive(line);
                        if (file != null) {
                            String siteId = extractSiteId(line);
                            scheduleImport(file, siteId);
                        }
                    }
                }
            }

        } catch (IOException ioe) {
            log.warn("Problem with "+ source + " "+ ioe.getMessage());
        }
    }

    private String extractSiteId(String line) {
        Matcher matcher = uuidRegex.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;

    }

    private void scheduleImport(String file, String siteId) {
        JobDataMap jobData = new JobDataMap();
        jobData.put("zip", file);
        if (siteId != null) {
            jobData.put("siteId", siteId);
        }

        JobDetail jobDetail = JobBuilder.newJob(ImportJob.class)
                .withIdentity("Import Job")
                .setJobData(jobData)
                .build();
        Scheduler scheduler = schedulerManager.getScheduler();
        try {
            scheduler.addJob(jobDetail, true, true);
            scheduler.triggerJob(jobDetail.getKey());
        } catch (SchedulerException e) {
            log.warn("Problem adding job to scheduler to import "+ file, e);
        }
    }

    private String downloadArchive(String archiveUrl) {

        if (archiveUrl == null || archiveUrl.trim().length() == 0) {
            log.warn("Empty archive setting.");
            return null;
        }
        log.info("Attempting to import: "+ archiveUrl);
        try {
            URL url = new URL(archiveUrl);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Sakai Content Importer");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            // Now make the connection.
            connection.connect();

            Path out = Files.createTempFile("archive", ".zip");

            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
            }
            return out.toString();

        } catch (IOException ioe) {
            log.warn("Problem with "+ archiveUrl+ " "+ ioe.getMessage());
        }
        return null;
    }
}
