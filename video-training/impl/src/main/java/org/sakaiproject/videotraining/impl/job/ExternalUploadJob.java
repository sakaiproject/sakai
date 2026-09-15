/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.videotraining.impl.job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingProcessJob;
import org.sakaiproject.videotraining.api.model.VideoTrainingProcessJobStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.service.ExternalProviderRegistry;
import org.sakaiproject.videotraining.api.service.ExternalVideoProviderStrategy;
import org.sakaiproject.videotraining.api.service.ProcessJobService;
import org.sakaiproject.videotraining.api.service.VideoTrainingService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExternalUploadJob implements ScheduledInvocationCommand {

    @Setter
    private ProcessJobService processJobService;

    @Setter
    private ExternalProviderRegistry providerRegistry;

    @Setter
    private SessionManager sessionManager;

    @Setter
    private VideoTrainingService videoTrainingService;

    @Override
    public void execute(String opaqueContext) {
        logIn();
        String videoId = StringUtils.trimToNull(opaqueContext);
        log.info("ExternalUploadJob executing for videoId {}", videoId);

        try {
            if (StringUtils.isBlank(videoId)) {
                log.warn("Received blank videoId context");
                return;
            }

            List<VideoTrainingProcessJob> jobs = processJobService.findByVideoIdOrderByModifiedOnDesc(videoId);
            if (jobs == null || jobs.isEmpty()) {
                log.warn("No process job found for video {}", videoId);
                return;
            }

            VideoTrainingProcessJob job = jobs.get(0);
            if (!(job.getStatus() == VideoTrainingProcessJobStatus.PENDING || job.getStatus() == VideoTrainingProcessJobStatus.RETRY)) {
                log.info("Job {} for video {} is already in status {}, skipping", job.getId(), videoId, job.getStatus());
                return;
            }

            processSingleJob(job);
        } catch (RuntimeException ex) {
            log.error("Failed", ex);
        } finally {
            try {
                logOut();
            } catch (RuntimeException ex) {
                log.warn("Failed to cleanup Sakai session.", ex);
            }
        }
    }

    private void processSingleJob(VideoTrainingProcessJob job) {
        if (job == null || StringUtils.isBlank(job.getVideoId()) || StringUtils.isBlank(job.getTempFilePath())) {
            return;
        }

        log.info("Starting job {} for video {}", job.getId(), job.getVideoId());

        job.setStatus(VideoTrainingProcessJobStatus.PROCESSING);
        job.setErrorMessage(null);
        job.setModifiedOn(Instant.now());
        processJobService.save(job);

        VideoTrainingVideo video = videoTrainingService.getVideoById(job.getVideoId()).orElse(null);
        if (video == null) {
            failJob(job, null, "Video not found for external upload: " + job.getVideoId());
            return;
        }

        Path inputFile = Paths.get(job.getTempFilePath());
        if (!Files.exists(inputFile)) {
            failJob(job, video, "Temporary upload not found for external upload processing.");
            return;
        }

        try {
            ExternalVideoProviderStrategy provider = providerRegistry.getProvider(video.getProviderType());
            VideoProviderType uploadProviderType = video.getProviderType();
            String sourceReference = provider.uploadVideo(video, inputFile);

            video.setSourceReference(sourceReference);
            video.setProviderType(uploadProviderType);
            video.setPublicationStatus(VideoPublicationStatus.DRAFT);
            video.setSourceDeleted(false);
            video.setModifiedOn(Instant.now());
            videoTrainingService.persistVideoChanges(video);
            if (videoTrainingService != null) {
                videoTrainingService.registerAudit(video.getSiteId(), sessionManager.getCurrentSessionUserId(), "VIDEO_UPDATED", video.getId(), sourceReference);
            }

            job.setStatus(VideoTrainingProcessJobStatus.COMPLETED);
            job.setErrorMessage(null);
            job.setModifiedOn(Instant.now());
            processJobService.save(job);

            try {
                Files.deleteIfExists(inputFile);
            } catch (IOException ex) {
                log.warn("Could not delete temp file {}", inputFile, ex);
            }
            log.info("Completed video {}", video.getId());
        } catch (IOException | RuntimeException ex) {
            failJob(job, video, StringUtils.defaultIfBlank(ex.getMessage(), "External upload failed."));
        }
    }

    private void failJob(VideoTrainingProcessJob job, VideoTrainingVideo video, String message) {
        log.warn("Job {} failed: {}", job != null ? job.getId() : null, message);
        if (job != null) {
            job.setStatus(VideoTrainingProcessJobStatus.FAILED);
            job.setErrorMessage(StringUtils.abbreviate(message, 4000));
            job.setModifiedOn(Instant.now());
            processJobService.save(job);
        }
        if (video != null) {
            video.setModifiedOn(Instant.now());
            videoTrainingService.persistVideoChanges(video);
        }
    }

    private void logIn() {
        Session sakaiSession = sessionManager.getCurrentSession();
        sakaiSession.setUserId("admin");
        sakaiSession.setUserEid("admin");
    }

    private void logOut() {
        final Session currentSession = sessionManager.getCurrentSession();
        currentSession.invalidate();
    }
}
