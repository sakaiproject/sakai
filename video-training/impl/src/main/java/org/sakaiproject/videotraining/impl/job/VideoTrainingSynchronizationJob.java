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

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.videotraining.api.VideoTrainingConstants;
import org.sakaiproject.videotraining.api.model.MetadataFetchResult;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.service.ExternalProviderRegistry;
import org.sakaiproject.videotraining.api.service.ExternalVideoProviderStrategy;
import org.sakaiproject.videotraining.api.service.VideoTrainingService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VideoTrainingSynchronizationJob implements Job {

    private static final ResourceLoader rb = new ResourceLoader("video-training");

    @Setter
    private SessionManager sessionManager;

    @Setter
    private EmailService emailService;

    @Setter
    private UserDirectoryService userDirectoryService;

    @Setter
    private ServerConfigurationService serverConfigurationService;

    @Setter
    private ExternalProviderRegistry providerRegistry;

    @Setter
    private VideoTrainingService videoTrainingService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        final long start = System.currentTimeMillis();
        log.info("VideoTrainingSynchronizationJob start");
        Map<String, List<String>> summaryMessagesByOwner = new LinkedHashMap<>();
        try {
            logIn();

            final int pageSize = 200;
            long total = videoTrainingService.countAllVideos("");
            int pageIndex = 0;
            while ((long) pageIndex * pageSize < total) {
                int offset = pageIndex * pageSize;
                List<VideoTrainingVideo> page = videoTrainingService.findAllVideos("", offset, pageSize);
                if (page == null || page.isEmpty()) {
                    break;
                }
                for (VideoTrainingVideo v : page) {
                    if (resolveExternalProvider(v) != null) {
                        syncExternalVideo(v, summaryMessagesByOwner);
                    } else {
                        syncInternalVideo(v, summaryMessagesByOwner);
                    }
                }
                pageIndex++;
            }

            sendSummaryEmails(summaryMessagesByOwner);

            log.info("VideoTrainingSynchronizationJob finished in {} ms", (System.currentTimeMillis() - start));
        } catch (RuntimeException e) {
            log.error("VideoTrainingSynchronizationJob failed", e);
            throw new JobExecutionException(e);
        } finally {
            try {
                logOut();
            } catch (Exception e) {
                log.warn("Failed to cleanup Sakai session.", e);
            }
        }
    }

    private void syncExternalVideo(VideoTrainingVideo video, Map<String, List<String>> summaryMessagesByOwner) {
        boolean updated = false;
        boolean titleChanged = false;
        boolean descriptionChanged = false;

        MetadataFetchResult meta = null;
        ExternalVideoProviderStrategy provider = resolveExternalProvider(video);
        if (video.isInheritTitleMetadata() || video.isInheritDescriptionMetadata()) {
            try {
                meta = provider != null ? provider.fetchMetadata(video.getSourceReference()) : null;
            } catch (RuntimeException e) {
                log.warn("Failed to fetch metadata for {}: {}", video.getSourceReference(), e.toString());
            }

            if (meta != null && !isMissingMetadata(meta)) {
                if (video.isInheritTitleMetadata()) {
                    String newTitle = StringUtils.trimToNull(meta.getTitle());
                    if (StringUtils.isNotBlank(newTitle) && !newTitle.equals(video.getTitle())) {
                        video.setTitle(newTitle);
                        titleChanged = true;
                        updated = true;
                    }
                }

                if (video.isInheritDescriptionMetadata()) {
                    String newDescription = StringUtils.trimToNull(meta.getDescription());
                    if (StringUtils.isNotBlank(newDescription) && !newDescription.equals(video.getDescription())) {
                        video.setDescription(newDescription);
                        descriptionChanged = true;
                        updated = true;
                    }
                }
            }
        }

        if (updated) {
            persistVideoChange(video, false);
        }

        if (titleChanged) {
            queueSummaryChange(summaryMessagesByOwner, video, "Title updated from external provider metadata");
        }
        if (descriptionChanged) {
            queueSummaryChange(summaryMessagesByOwner, video, "Description updated from external provider metadata");
        }

        syncVideoPrivacy(video, summaryMessagesByOwner);
    }

    private void syncInternalVideo(VideoTrainingVideo video, Map<String, List<String>> summaryMessagesByOwner) {
        boolean sourceStateChanged = false;
        try {
            boolean changed = false;
            if (videoTrainingService != null) {
                try {
                    changed = videoTrainingService.ensureSourceState(video);
                } catch (RuntimeException e) {
                    log.warn("Failed to check source state for {}: {}", video.getSourceReference(), e.toString());
                }
            }

            if (changed) {
                sourceStateChanged = true;
                persistVideoChange(video, true);
            }
        } catch (RuntimeException e) {
            log.warn("Failed to resolve content resource for {}: {}", video.getSourceReference(), e.toString());
        }

        if (sourceStateChanged) {
            if (video.isSourceDeleted()) {
                queueSummaryChange(summaryMessagesByOwner, video, "Source marked as unavailable; publication forced to DRAFT");
            } else {
                queueSummaryChange(summaryMessagesByOwner, video, "Source restored and availability updated");
            }
        }
    }

    private void syncVideoPrivacy(VideoTrainingVideo video, Map<String, List<String>> summaryMessagesByOwner) {
        ExternalVideoProviderStrategy provider = resolveExternalProvider(video);
        if (provider == null) {
            return;
        }

        try {
            VideoPublicationStatus currentStatus = video.getPublicationStatus();
            VideoPublicationStatus providerStatus = provider.fetchProviderPublicationStatus(
                    video.getSourceReference(),
                    video.getVisibilityScope());
            if (providerStatus == null || providerStatus == video.getPublicationStatus()) {
                return;
            }

            VideoPublicationStatus effectiveStatus = providerStatus;
            if (providerStatus == VideoPublicationStatus.DRAFT && currentStatus == VideoPublicationStatus.PUBLISHED) {
                effectiveStatus = VideoPublicationStatus.WITHDRAWN;
            }

            if (effectiveStatus == currentStatus) {
                return;
            }

            video.setPublicationStatus(effectiveStatus);
            video.setModifiedOn(Instant.now());
            persistVideoChange(video, false);
            queueSummaryChange(summaryMessagesByOwner, video,
                    "Publication status synchronized from YouTube: "
                            + StringUtils.defaultString(currentStatus != null ? currentStatus.name() : null)
                            + " -> " + effectiveStatus.name());
        } catch (Exception e) {
            log.warn("Failed to fetch visibility for {}: {}", video.getSourceReference(), e.toString());
        }
    }

    private ExternalVideoProviderStrategy resolveExternalProvider(VideoTrainingVideo video) {
        if (providerRegistry == null || video == null || StringUtils.isBlank(video.getSourceReference())) {
            return null;
        }

        try {
            return providerRegistry.findProviderByUrl(video.getSourceReference()).orElse(null);
        } catch (RuntimeException e) {
            log.warn("Failed to resolve provider for {}: {}", video.getSourceReference(), e.toString());
            return null;
        }
    }

    private boolean isMissingMetadata(MetadataFetchResult meta) {
        if (meta == null) {
            return true;
        }
        return StringUtils.isBlank(meta.getTitle()) && StringUtils.isBlank(meta.getDescription());
    }

    private void persistVideoChange(VideoTrainingVideo video, boolean sourceStateChanged) {
        try {
            videoTrainingService.persistVideoChanges(video);
            if (videoTrainingService != null) {
                videoTrainingService.registerAudit(video.getSiteId(), sessionManager.getCurrentSessionUserId(), "VIDEO_UPDATED", video.getId(), StringUtils.defaultString(video.getSourceReference()));
                if (sourceStateChanged) {
                    if (video.isSourceDeleted()) {
                        videoTrainingService.registerAudit(video.getSiteId(), sessionManager.getCurrentSessionUserId(), "SOURCE_STATE_CHANGED", video.getId(), "deleted");
                        videoTrainingService.registerAudit(video.getSiteId(), sessionManager.getCurrentSessionUserId(), "PUBLICATION_STATUS_CHANGED", video.getId(), "->DRAFT");
                    } else {
                        videoTrainingService.registerAudit(video.getSiteId(), sessionManager.getCurrentSessionUserId(), "SOURCE_STATE_CHANGED", video.getId(), "restored");
                    }
                }
            }
        } catch (RuntimeException e) {
            log.warn("Failed to save updated video {}", video.getId(), e);
        }
    }

    private void queueSummaryChange(Map<String, List<String>> summaryMessagesByOwner, VideoTrainingVideo video, String message) {
        if (summaryMessagesByOwner == null || video == null || StringUtils.isBlank(video.getOwnerId()) || StringUtils.isBlank(message)) {
            return;
        }

        String label = StringUtils.defaultIfBlank(video.getTitle(), video.getId());
        summaryMessagesByOwner.computeIfAbsent(video.getOwnerId(), key -> new ArrayList<>())
                .add(label + ": " + message);
    }

    private void sendSummaryEmails(Map<String, List<String>> summaryMessagesByOwner) {
        if (summaryMessagesByOwner == null || summaryMessagesByOwner.isEmpty() || emailService == null || userDirectoryService == null) {
            return;
        }

        String from = serverConfigurationService.getSmtpFrom();
        String serviceName = serverConfigurationService.getString(VideoTrainingConstants.UI_SERVICE_PROPERTY, VideoTrainingConstants.DEFAULT_SERVICE_NAME);

        for (Map.Entry<String, List<String>> entry : summaryMessagesByOwner.entrySet()) {
            String ownerId = entry.getKey();
            String to = resolveRecipientEmail(ownerId);
            if (StringUtils.isBlank(to)) {
                continue;
            }

            String changes = String.join("\n", entry.getValue().stream().map(change -> "- " + change).toList());
            String subject = rb.getFormattedMessage("video.training.email.summary.subject", new Object[] { entry.getValue().size() });
            String body = rb.getFormattedMessage("video.training.email.summary.body", new Object[] { changes, serviceName });

            try {
                emailService.send(from, to, subject, body, null, null, null);
            } catch (RuntimeException e) {
                log.warn("sendSummaryEmails - failed to send summary email to {}: {}", to, e.toString());
            }
        }
    }

    private String resolveRecipientEmail(String userId) {
        try {
            User user = userDirectoryService.getUser(userId);
            return user != null ? user.getEmail() : null;
        } catch (UserNotDefinedException e) {
            log.warn("sendSummaryEmails - could not resolve user {}: {}", userId, e.toString());
            return null;
        } catch (RuntimeException e) {
            log.warn("sendSummaryEmails - could not resolve user {}: {}", userId, e.toString());
            return null;
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
