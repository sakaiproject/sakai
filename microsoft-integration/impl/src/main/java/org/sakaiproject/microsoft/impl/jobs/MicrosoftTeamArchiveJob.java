/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.microsoft.impl.jobs;

import java.time.ZonedDateTime;
import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.MicrosoftLoggingService;
import org.sakaiproject.microsoft.api.data.MicrosoftLogInvokers;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.model.MicrosoftLog;
import org.sakaiproject.microsoft.api.model.MicrosoftTeamArchiveRecord;
import org.sakaiproject.microsoft.api.persistence.MicrosoftTeamArchiveRepository;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisallowConcurrentExecution
public class MicrosoftTeamArchiveJob implements Job {

    @Setter
    private SessionManager sessionManager;

    @Setter
    private MicrosoftCommonService microsoftCommonService;

    @Setter
    private MicrosoftLoggingService microsoftLoggingService;

    @Setter
    private MicrosoftTeamArchiveRepository microsoftTeamArchiveRepository;

    @Setter
    protected ServerConfigurationService serverConfigurationService;

    // Configurable via sakai.properties: microsoft.team.archive.job.batch.size=50
    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_ARCHIVED = 1;
    private static final int STATUS_ERROR = -1;

    public void init() {
        log.info("Initializing Microsoft Team Archive Job");
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("MicrosoftTeamArchiveJob started.");

        Session session = sessionManager.getCurrentSession();
        ZonedDateTime startTime = ZonedDateTime.now();

        int configuredBatchSize = serverConfigurationService.getInt("microsoft.team.archive.job.batch.size", DEFAULT_BATCH_SIZE);
        int batchSize = configuredBatchSize > 0 ? configuredBatchSize : DEFAULT_BATCH_SIZE;
        if (configuredBatchSize <= 0) {
            log.warn("Invalid batch size {}. Falling back to default {}", configuredBatchSize, DEFAULT_BATCH_SIZE);
        }
        int successCount = 0;
        int errorCount = 0;
        boolean credentialsError = false;

        try {
            session.setUserEid("admin");
            session.setUserId("admin");
            session.setAttribute("origin", MicrosoftLogInvokers.JOB.getCode());

            List<MicrosoftTeamArchiveRecord> batch;

            // Process in batches
            while (!(batch = microsoftTeamArchiveRepository.findByStatus(STATUS_PENDING, batchSize, 0)).isEmpty() && !credentialsError) {
                log.debug("Processing batch of size={}", batch.size());

                for (MicrosoftTeamArchiveRecord record : batch) {
                    if (credentialsError) {
                        break;
                    }

                    int retryCount = 0;
                    boolean archived = false;

                    while (retryCount < MAX_RETRIES && !archived) {
                        if (retryCount > 0) {
                            log.debug("Retrying archive for siteId={}, teamId={}, attempt={}", record.getSiteId(), record.getTeamId(), retryCount);
                        }
                        try {
                            boolean result = microsoftCommonService.archiveTeam(record.getTeamId());

                            if (result) {
                                record.setArchiveDate(ZonedDateTime.now());
                                record.setStatus(STATUS_ARCHIVED);
                                record = microsoftTeamArchiveRepository.save(record);

                                microsoftLoggingService.saveLog(MicrosoftLog.builder()
                                        .event(MicrosoftLog.EVENT_TEAM_ARCHIVED)
                                        .status(MicrosoftLog.Status.OK)
                                        .addData("origin", MicrosoftLogInvokers.JOB.getCode())
                                        .addData("siteId", record.getSiteId())
                                        .addData("teamId", record.getTeamId())
                                        .build());

                                log.info("Team archived successfully: siteId={}, teamId={}", record.getSiteId(), record.getTeamId());
                                successCount++;
                                archived = true;
                            } else {
                                log.warn("archiveTeam returned false for siteId={}, teamId={}", record.getSiteId(), record.getTeamId());
                                retryCount++;
                            }

                        } catch (MicrosoftCredentialsException e) {
                            log.error("MicrosoftCredentialsException archiving teamId={} — aborting job", record.getTeamId(), e);
                            saveErrorLog(record, e.getMessage());
                            errorCount++;
                            credentialsError = true;
                            break;
                        } catch (Exception e) {
                            log.error("Exception archiving siteId={}, teamId={}", record.getSiteId(), record.getTeamId(), e);
                            retryCount++;
                        }
                    }

                    if (!archived && !credentialsError) {
                        log.error("Failed to archive siteId={}, teamId={} after {} retries", record.getSiteId(), record.getTeamId(), MAX_RETRIES);
                        record.setStatus(STATUS_ERROR);
                        record = microsoftTeamArchiveRepository.save(record);
                        saveErrorLog(record, "Max retries reached");
                        errorCount++;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Exception while running MicrosoftTeamArchiveJob", e);
        } finally {
            session.clear();
        }

        microsoftLoggingService.saveLog(MicrosoftLog.builder()
                .event(MicrosoftLog.EVENT_ARCHIVE_JOB_RESULT)
                .status(MicrosoftLog.Status.OK)
                .addData("origin", MicrosoftLogInvokers.JOB.getCode())
                .addData("start_time", startTime.toLocalDateTime().toString())
                .addData("total_processed", String.valueOf(successCount + errorCount))
                .addData("success_count", String.valueOf(successCount))
                .addData("error_count", String.valueOf(errorCount))
                .build());

        log.info("MicrosoftTeamArchiveJob completed. success={}, errors={}", successCount, errorCount);
    }

    private void saveErrorLog(MicrosoftTeamArchiveRecord record, String reason) {
        microsoftLoggingService.saveLog(MicrosoftLog.builder()
                .event(MicrosoftLog.EVENT_TEAM_ARCHIVE_ERROR)
                .status(MicrosoftLog.Status.KO)
                .addData("origin", MicrosoftLogInvokers.JOB.getCode())
                .addData("siteId", record.getSiteId())
                .addData("teamId", record.getTeamId())
                .addData("reason", reason)
                .build());
    }
}
