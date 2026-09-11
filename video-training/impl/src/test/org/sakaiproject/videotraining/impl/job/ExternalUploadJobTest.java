package org.sakaiproject.videotraining.impl.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;
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

public class ExternalUploadJobTest {

    private Path tempFile;

    @After
    public void tearDown() throws IOException {
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void executeShouldDelegateToRegisteredProviderAndCompleteJob() throws Exception {
        tempFile = Files.createTempFile("vtm-upload", ".mp4");

        ProcessJobService processJobService = mock(ProcessJobService.class);
        ExternalVideoProviderStrategy provider = mock(ExternalVideoProviderStrategy.class);
        SessionManager sessionManager = mock(SessionManager.class);
        Session session = mock(Session.class);
        VideoTrainingService videoTrainingService = mock(VideoTrainingService.class);
        ExternalProviderRegistry providerRegistry = new ExternalProviderRegistry() {
            @Override
            public ExternalVideoProviderStrategy getProvider(VideoProviderType type) {
                return provider;
            }

            @Override
            public java.util.Collection<ExternalVideoProviderStrategy> getProviders() {
                return Collections.singletonList(provider);
            }

            @Override
            public Optional<ExternalVideoProviderStrategy> findProviderByUrl(String url) {
                return Optional.of(provider);
            }
        };

        VideoTrainingVideo video = new VideoTrainingVideo();
        video.setId("video-1");
        video.setSiteId("site-1");
        video.setOwnerId("user-1");
        video.setTitle("Sample video");
        video.setDescription("Sample description");
        video.setProviderType(VideoProviderType.YOUTUBE_UPLOAD);
        video.setSourceReference("pending");
        video.setPublicationStatus(VideoPublicationStatus.DRAFT);

        VideoTrainingProcessJob job = new VideoTrainingProcessJob();
        job.setId("job-1");
        job.setVideoId("video-1");
        job.setTempFilePath(tempFile.toString());
        job.setStatus(VideoTrainingProcessJobStatus.PENDING);
        job.setModifiedOn(Instant.now());

        when(processJobService.findByVideoIdOrderByModifiedOnDesc("video-1")).thenReturn(Collections.singletonList(job));
        when(processJobService.save(any(VideoTrainingProcessJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(videoTrainingService.getVideoById("video-1")).thenReturn(Optional.of(video));
        when(videoTrainingService.persistVideoChanges(any(VideoTrainingVideo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(provider.uploadVideo(eq(video), any(Path.class))).thenReturn("https://www.youtube.com/watch?v=abc123");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("user-1");

        ExternalUploadJob uploadJob = new ExternalUploadJob();
        uploadJob.setProcessJobService(processJobService);
        uploadJob.setProviderRegistry(providerRegistry);
        uploadJob.setSessionManager(sessionManager);
        uploadJob.setVideoTrainingService(videoTrainingService);

        uploadJob.execute("video-1");

        verify(provider).uploadVideo(eq(video), eq(tempFile));
        org.junit.Assert.assertEquals(VideoProviderType.YOUTUBE_UPLOAD, video.getProviderType());
        verify(videoTrainingService).persistVideoChanges(video);
        verify(processJobService, org.mockito.Mockito.times(2)).save(job);
        verify(videoTrainingService).registerAudit(eq("site-1"), eq("user-1"), eq("VIDEO_UPDATED"), eq("video-1"), eq("https://www.youtube.com/watch?v=abc123"));
    }
}
