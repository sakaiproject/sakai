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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.videotraining.api.VideoTrainingConstants;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingProcessJob;
import org.sakaiproject.videotraining.api.model.VideoTrainingProcessJobStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.service.ProcessJobService;
import org.sakaiproject.videotraining.api.service.VideoTrainingService;
import org.sakaiproject.videotraining.api.service.VideoTrainingUploadService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HlsTranscodingJob implements ScheduledInvocationCommand {

    private static final ResourceLoader RB = new ResourceLoader("video-training");

    @Setter
    private ProcessJobService processJobService;

    @Setter
    private VideoTrainingService videoTrainingService;

    @Setter
    private VideoTrainingUploadService uploadService;

    @Setter
    private ContentHostingService contentHostingService;

    @Setter
    private SessionManager sessionManager;

    @Setter
    private EmailService emailService;

    @Setter
    private UserDirectoryService userDirectoryService;

    @Setter
    private ServerConfigurationService serverConfigurationService;

    @Override
    public void execute(String opaqueContext) {
        logIn();
        String videoId = StringUtils.trimToNull(opaqueContext);
        log.info("HlsTranscodingJob executing for videoId {}", videoId);
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
                log.debug("Job {} for video {} is already in status {}, skipping", job.getId(), videoId, job.getStatus());
                return;
            }

            processSingleJob(job);
        } catch (RuntimeException ex) {
            log.error("HlsTranscodingJob failed", ex);
        } finally {
            try {
                logOut();
            } catch (Exception ex) {
                log.warn("Failed to cleanup Sakai session.", ex);
            }
        }
    }

    private void processSingleJob(VideoTrainingProcessJob job) {
        if (job == null || StringUtils.isBlank(job.getVideoId()) || StringUtils.isBlank(job.getTempFilePath())) {
            return;
        }

        log.info("HlsTranscodingJob executing for job {} for video {}", job.getId(), job.getVideoId());

        job.setStatus(VideoTrainingProcessJobStatus.PROCESSING);
        job.setErrorMessage(null);
        job.setModifiedOn(Instant.now());
        processJobService.save(job);

        VideoTrainingVideo video = videoTrainingService.getVideoById(job.getVideoId()).orElse(null);
        if (video == null) {
            failJob(job, null, "Video not found for HLS processing: " + job.getVideoId());
            return;
        }

        Path inputFile = Paths.get(job.getTempFilePath());
        Path workDir = inputFile.getParent();
        if (workDir == null || !Files.exists(inputFile)) {
            failJob(job, video, "Temporary upload not found for HLS processing.");
            return;
        }

        Path outputDir = workDir.resolve("hls");
        try {
            Files.createDirectories(outputDir);
            renderVariants(inputFile, outputDir);
            try {
                extractThumbnail(inputFile, outputDir);
            } catch (Exception e) {
                log.warn("The thumbnail for the video {} could not be extracted, continuing...", video.getId(), e);
            }
            long outputBytes = calculateDirectorySize(outputDir);
            Long quotaBytes = videoTrainingService.getSiteStorageQuotaBytes(video.getSiteId());
            long usageBytes = videoTrainingService.getSiteStorageUsageBytes(video.getSiteId());

            if (quotaBytes != null && quotaBytes > 0 && usageBytes + outputBytes > quotaBytes) {
                failJob(job, video, "Uploading the generated HLS package would exceed the site quota.");
                return;
            }

            String masterResourceId = uploadGeneratedContent(video, outputDir);
            video.setSourceReference(masterResourceId);
            video.setFileSizeBytes(outputBytes);
            video.setProviderType(VideoProviderType.HLS_UPLOAD);
            video.setPublicationStatus(VideoPublicationStatus.DRAFT);
            video.setSourceDeleted(false);
            video.setModifiedOn(Instant.now());
            videoTrainingService.persistVideoChanges(video);
            if (videoTrainingService != null) {
                videoTrainingService.registerAudit(video.getSiteId(), sessionManager.getCurrentSessionUserId(), "VIDEO_UPDATED", video.getId(), masterResourceId);
            }

            job.setStatus(VideoTrainingProcessJobStatus.COMPLETED);
            job.setErrorMessage(null);
            job.setModifiedOn(Instant.now());
            processJobService.save(job);
            sendCompletionEmail(video, job, null);
            cleanupTempDirectory(workDir);
            log.info("HlsTranscodingJob completed video {}", video.getId());
        } catch (IOException ex) {
            failJob(job, video, "HLS transcoder is not available on this server.");
        } catch (Exception ex) {
            failJob(job, video, "HLS processing failed.");
        }
    }

    private void extractThumbnail(Path inputFile, Path outputDir) throws IOException, InterruptedException {
        Path thumbnail = outputDir.resolve("thumbnail.jpg");
        List<String> command = new ArrayList<>();

        command.add(resolveFfmpegCommand());
        command.add("-y");
        command.add("-i");
        command.add(inputFile.toString());
        command.add("-ss");
        command.add(VideoTrainingConstants.DEFAULT_THUMBNAIL_TIMESTAMP);
        command.add("-vframes");
        command.add("1");
        command.add("-f");
        command.add("image2");
        command.add("-update");
        command.add("1");
        command.add("-vf");
        command.add("scale=640:-1");
        command.add("-q:v");
        command.add("4");
        command.add(thumbnail.toString());

        log.debug("Running FFmpeg for thumbnail: {}", String.join(" ", command));
        runProcess(command, outputDir);
    }

    private void renderVariants(Path inputFile, Path outputDir) throws IOException, InterruptedException {
        List<HlsVariant> ALL_VARIANTS = Arrays.asList(
                new HlsVariant("1080p", 1080, "4000k", "4500k", "8000k", 1920, 1080),
                new HlsVariant("720p", 720, "2000k", "2500k", "4000k", 1280, 720),
                new HlsVariant("480p", 480, "1200k", "1400k", "2400k", 854, 480),
                new HlsVariant("360p", 360, "800k", "1000k", "1600k", 640, 360),
                new HlsVariant("144p", 144, "400k", "500k", "800k", 256, 144)
        );

        int sourceHeight = getVideoHeightWithFfprobe(inputFile);

        List<HlsVariant> validVariants = new ArrayList<>();
        for (HlsVariant v : ALL_VARIANTS) {
            if (v.height <= sourceHeight) {
                validVariants.add(v);
            }
        }

        if (validVariants.isEmpty()) {
            validVariants.add(ALL_VARIANTS.get(ALL_VARIANTS.size() - 1));
        }

        List<HlsVariant> variantsToEncode = new ArrayList<>();

        if (validVariants.size() > 0) variantsToEncode.add(validVariants.get(0));

        if (validVariants.size() > 1) variantsToEncode.add(validVariants.get(1));

        HlsVariant lowestVariant = validVariants.get(validVariants.size() - 1);
        if (!variantsToEncode.contains(lowestVariant)) {
            variantsToEncode.add(lowestVariant);
        }

        for (HlsVariant variant : variantsToEncode) {
            Path playlist = outputDir.resolve(variant.name + ".m3u8");
            Path segments = outputDir.resolve(variant.name + "_%03d.ts");
            List<String> command = new ArrayList<>();
            command.add(resolveFfmpegCommand());
            command.add("-y");
            command.add("-i");
            command.add(inputFile.toString());

            String videoFilter = String.format(
                "scale=w=%d:h=%d:force_original_aspect_ratio=decrease,pad=%d:%d:(ow-iw)/2:(oh-ih)/2",
                variant.width, variant.height, variant.width, variant.height
            );
            command.add("-vf");
            command.add(videoFilter);

            command.add("-c:v");
            command.add("libx264");
            command.add("-profile:v");
            command.add("main");
            command.add("-preset");
            command.add("fast");
            command.add("-b:v");
            command.add(variant.videoBitrate);
            command.add("-maxrate");
            command.add(variant.maxRate);
            command.add("-bufsize");
            command.add(variant.bufferSize);

            command.add("-c:a");
            command.add("aac");
            command.add("-b:a");
            command.add("128k");

            command.add("-f");
            command.add("hls");
            command.add("-hls_time");
            command.add("6");
            command.add("-hls_playlist_type");
            command.add("vod");
            command.add("-hls_flags");
            command.add("independent_segments");
            command.add("-hls_segment_filename");
            command.add(segments.toString());
            command.add(playlist.toString());

            runProcess(command, outputDir);
        }

        writeMasterPlaylist(outputDir, variantsToEncode);
    }

    private int getVideoHeightWithFfprobe(Path inputFile) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=height",
                "-of", "csv=s=x:p=0",
                inputFile.toString()
        );

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            process.waitFor();

            if (line != null && !line.trim().isEmpty()) {
                return Integer.parseInt(line.trim());
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse original video height, defaulting to 720p.", e);
        }

        return 720;
    }

    private void runProcess(List<String> command, Path workDir) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(workDir.toFile());
        builder.redirectErrorStream(true);
        Process process;
        try {
            process = builder.start();
        } catch (IOException ex) {
            throw new IOException("HLS transcoder is not available on this server.");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("HLS transcoding failed with exit code " + exitCode);
        }
    }

    private String resolveFfmpegCommand() {
        return StringUtils.trimToEmpty(serverConfigurationService.getString(VideoTrainingConstants.HLS_FFMPEG_PROPERTY, VideoTrainingConstants.DEFAULT_HLS_FFMPEG));
    }

    private void writeMasterPlaylist(Path outputDir, List<HlsVariant> variants) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("#EXTM3U");
        lines.add("#EXT-X-VERSION:3");
        lines.add("#EXT-X-INDEPENDENT-SEGMENTS");
        for (HlsVariant variant : variants) {
            lines.add("#EXT-X-STREAM-INF:BANDWIDTH=" + variant.bandwidth + ",RESOLUTION=" + variant.width + "x" + variant.height);
            lines.add(variant.name + ".m3u8");
        }
        Files.write(outputDir.resolve(VideoTrainingConstants.MASTER_PLAYLIST_FILENAME), lines, StandardCharsets.UTF_8);
    }

    private long calculateDirectorySize(Path directory) throws IOException {
        try (Stream<Path> stream = Files.walk(directory)) {
            return stream.filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    })
                    .sum();
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    private String uploadGeneratedContent(VideoTrainingVideo video, Path outputDir) throws IOException {
        String collectionId;
        try {
            collectionId = uploadService.resolveManagedUploadCollectionId(video.getSiteId(), video.getOwnerId(), video.getVisibilityScope());
            collectionId = ensureVideoHlsCollection(collectionId, video);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
        List<Path> files;
        try (Stream<Path> stream = Files.walk(outputDir)) {
            files = stream.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());
        }

        String masterResourceId = null;
        List<String> uploadedResources = new ArrayList<>();
        try {
            for (Path file : files) {
                String filename = file.getFileName().toString();
                String extension = extractExtension(filename);
                String baseName = extension.isEmpty() ? filename : filename.substring(0, filename.length() - extension.length());
                ContentResourceEdit edit = contentHostingService.addResource(collectionId, baseName, extension, 1);
                boolean committed = false;
                try {
                    byte[] content = Files.readAllBytes(file);
                    edit.setContent(content);
                    edit.setContentLength(content.length);
                    edit.setContentType(contentTypeFor(filename));
                    edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, filename);
                    contentHostingService.commitResource(edit, NotificationService.NOTI_NONE);
                    committed = true;
                    uploadedResources.add(edit.getId());
                    if (VideoTrainingConstants.MASTER_PLAYLIST_FILENAME.equals(filename)) {
                        masterResourceId = edit.getId();
                    }
                } finally {
                    if (!committed) {
                        try {
                            contentHostingService.cancelResource(edit);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } catch (Exception ex) {
            for (String resourceId : uploadedResources) {
                try {
                    contentHostingService.removeResource(resourceId);
                } catch (Exception ignored) {
                }
            }
            throw ex instanceof IOException ? (IOException) ex : new IOException(ex);
        }

        if (StringUtils.isBlank(masterResourceId)) {
            throw new IOException("Master playlist was not generated");
        }
        return masterResourceId;
    }

    private String ensureVideoHlsCollection(String parentCollectionId, VideoTrainingVideo video) throws Exception {
        String videoFolderName = sanitizeResourceName(video.getId(), "video");
        String collectionId = parentCollectionId;
        if (!collectionId.endsWith("/")) {
            collectionId = collectionId + "/";
        }
        collectionId = collectionId + videoFolderName + "/";
        String displayName = StringUtils.defaultIfBlank(video.getTitle(), videoFolderName);
        uploadService.ensureManagedCollectionPath(collectionId, displayName, false);
        return collectionId;
    }

    private void failJob(VideoTrainingProcessJob job, VideoTrainingVideo video, String message) {
        log.warn("HlsTranscodingJob - failed for job {}: {}", job.getId(), message);
        job.setStatus(VideoTrainingProcessJobStatus.FAILED);
        job.setErrorMessage(StringUtils.abbreviate(StringUtils.defaultString(message), 3900));
        job.setModifiedOn(Instant.now());
        processJobService.save(job);
        if (video != null) {
            video.setModifiedOn(Instant.now());
            videoTrainingService.persistVideoChanges(video);
            sendCompletionEmail(video, job, message);
        }
        cleanupTempDirectory(Paths.get(job.getTempFilePath()).getParent());
    }

    private void sendCompletionEmail(VideoTrainingVideo video, VideoTrainingProcessJob job, String errorMessage) {
        String to = resolveRecipientEmail(job.getSubmitterUserId());
        if (StringUtils.isBlank(to)) {
            return;
        }

        if (StringUtils.isBlank(serverConfigurationService.getString(VideoTrainingConstants.SMTP_EMAIL_SERVICE_PROPERTY, VideoTrainingConstants.DEFAULT_SMTP_EMAIL_SERVICE))) {
            log.debug("HlsTranscodingJob - skipping email because SMTP is not configured");
            return;
        }

        String from = serverConfigurationService.getSmtpFrom();
        String serviceName = serverConfigurationService.getString(VideoTrainingConstants.UI_SERVICE_PROPERTY, VideoTrainingConstants.DEFAULT_SERVICE_NAME);
        String subject;
        String body;
        if (StringUtils.isBlank(errorMessage)) {
            subject = RB.getFormattedMessage("video.training.hls.email.completed.subject", new Object[] { video.getTitle() });
            body = RB.getFormattedMessage("video.training.hls.email.completed.body",
                    new Object[] { video.getTitle(), video.getSiteId(), serviceName });
        } else {
            subject = RB.getFormattedMessage("video.training.hls.email.failed.subject", new Object[] { video.getTitle() });
            body = RB.getFormattedMessage("video.training.hls.email.failed.body",
                    new Object[] { video.getTitle(), StringUtils.defaultString(errorMessage), video.getSiteId(), serviceName });
        }

        try {
            emailService.send(from, to, subject, body, null, null, null);
        } catch (Exception ex) {
            log.warn("HlsTranscodingJob - failed to send email to {}", to, ex);
        }
    }

    private String resolveRecipientEmail(String userId) {
        try {
            User user = userDirectoryService.getUser(userId);
            return user != null ? user.getEmail() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private void cleanupTempDirectory(Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }

        try (Stream<Path> stream = Files.walk(directory)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ex) {
                    log.debug("HlsTranscodingJob - failed to delete a temporary HLS file", ex);
                }
            });
        } catch (IOException ex) {
            log.debug("HlsTranscodingJob - cleanup failed for temporary HLS files", ex);
        }
    }

    private String contentTypeFor(String filename) {
        String extension = extractExtension(filename).toLowerCase(Locale.ROOT);
        if (".m3u8".equals(extension)) {
            return "application/vnd.apple.mpegurl";
        }
        if (".ts".equals(extension)) {
            return "video/mp2t";
        }
        if (".jpg".equals(extension) || ".jpeg".equals(extension)) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }

    private String extractExtension(String filename) {
        String normalized = StringUtils.defaultString(filename);
        int index = normalized.lastIndexOf('.');
        return index >= 0 ? normalized.substring(index) : "";
    }

    private String sanitizeResourceName(String value, String defaultValue) {
        String normalized = StringUtils.defaultIfBlank(value, defaultValue).replaceAll("[^A-Za-z0-9._-]+", "_");
        normalized = StringUtils.strip(normalized, "_");
        return StringUtils.defaultIfBlank(normalized, defaultValue);
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

    private static final class HlsVariant {
        private final String name;
        private final int height;
        private final String videoBitrate;
        private final String maxRate;
        private final String bufferSize;
        private final int width;
        private final int bandwidth;

        private HlsVariant(String name, int height, String videoBitrate, String maxRate, String bufferSize, int width, int bandwidth) {
            this.name = name;
            this.height = height;
            this.videoBitrate = videoBitrate;
            this.maxRate = maxRate;
            this.bufferSize = bufferSize;
            this.width = width;
            this.bandwidth = bandwidth;
        }
    }
}
