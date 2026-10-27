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

package org.sakaiproject.videotraining.impl.service;

import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoTrainingProcessJob;
import org.sakaiproject.videotraining.api.model.VideoTrainingProcessJobStatus;
import org.sakaiproject.videotraining.api.repository.VideoTrainingProcessJobRepository;
import org.sakaiproject.videotraining.api.service.ProcessJobService;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class ProcessJobServiceImpl implements ProcessJobService {

    private static final String HLS_TRANSCODING_JOB_NAME = "HlsTranscodingJob";
    private static final String EXTERNAL_UPLOAD_JOB_NAME = "ExternalUploadJob";

    @Setter
    private VideoTrainingProcessJobRepository processJobRepository;

    @Setter
    private ScheduledInvocationManager scheduledInvocationManager;

    @Override
    public VideoTrainingProcessJob save(VideoTrainingProcessJob processJob) {
        return processJobRepository.save(processJob);
    }

    @Override
    public void delete(VideoTrainingProcessJob processJob) {
        processJobRepository.delete(processJob);
    }

    @Override
    public List<VideoTrainingProcessJob> findByVideoIdOrderByModifiedOnDesc(String videoId) {
        return processJobRepository.findByVideoIdOrderByModifiedOnDesc(videoId);
    }

    @Override
    public List<VideoTrainingProcessJob> findBySubmitterUserIdOrderByModifiedOnDesc(String submitterUserId) {
        return processJobRepository.findBySubmitterUserIdOrderByModifiedOnDesc(submitterUserId);
    }

    @Override
    public void queueManagedUploadJob(String videoId, String ownerId, String tempFilePath, VideoProviderType providerType) {
        Instant now = Instant.now();
        VideoTrainingProcessJob processJob = new VideoTrainingProcessJob();
        processJob.setVideoId(videoId);
        processJob.setSubmitterUserId(ownerId);
        processJob.setStatus(VideoTrainingProcessJobStatus.PENDING);
        processJob.setTempFilePath(tempFilePath);
        processJob.setErrorMessage(null);
        processJob.setCreatedOn(now);
        processJob.setModifiedOn(now);
        VideoTrainingProcessJob saved = save(processJob);
        log.info("Queued upload process job {} for video {} (submitter={}, provider={})",
                saved.getId(), saved.getVideoId(), saved.getSubmitterUserId(), providerType);

        boolean isHlsUpload = providerType == VideoProviderType.HLS_UPLOAD;
        String jobName = isHlsUpload ? HLS_TRANSCODING_JOB_NAME : EXTERNAL_UPLOAD_JOB_NAME;
        String label = isHlsUpload ? "HLS" : "external upload";
        scheduleDelayedInvocation(jobName, videoId, label);
    }

    @Override
    public void cleanupProcessJobs(String videoId) {
        if (StringUtils.isBlank(videoId)) {
            return;
        }

        try {
            List<VideoTrainingProcessJob> jobs = findByVideoIdOrderByModifiedOnDesc(videoId);
            for (VideoTrainingProcessJob job : jobs) {
                delete(job);
            }
        } catch (Exception ex) {
            log.warn("Failed to cleanup process jobs for video {}", videoId, ex);
        }
    }

    @Override
    public boolean isProcessingManagedUpload(String videoId, VideoProviderType providerType) {
        if (providerType != VideoProviderType.HLS_UPLOAD || StringUtils.isBlank(videoId)) {
            return false;
        }

        List<VideoTrainingProcessJob> jobs = findByVideoIdOrderByModifiedOnDesc(videoId);
        if (jobs == null || jobs.isEmpty()) {
            return false;
        }

        VideoTrainingProcessJobStatus status = jobs.get(0).getStatus();
        return status == VideoTrainingProcessJobStatus.PENDING
                || status == VideoTrainingProcessJobStatus.PROCESSING
                || status == VideoTrainingProcessJobStatus.RETRY;
    }

    private void scheduleDelayedInvocation(String jobName, String videoId, String label) {
        try {
            Instant fireTime = Instant.now().plusSeconds(5L);
            String invocationId = scheduledInvocationManager.createDelayedInvocation(fireTime, jobName, videoId);
            log.info("Scheduled {} trigger {} for video {} at {}", label, invocationId, videoId, fireTime);
        } catch (RuntimeException ex) {
            cleanupProcessJobs(videoId);
            throw ex;
        }
    }
}
