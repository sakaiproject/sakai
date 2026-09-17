package org.sakaiproject.videotraining.impl.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;
import org.sakaiproject.videotraining.api.service.ExternalProviderRegistry;
import org.sakaiproject.videotraining.api.service.ExternalVideoProviderStrategy;
import org.sakaiproject.videotraining.api.service.VideoTrainingService;

public class VideoTrainingSynchronizationJobTest {

    @Test
    public void executeShouldUseResolvedProviderForMetadataAndPrivacySync() throws Exception {
        SessionManager sessionManager = mock(SessionManager.class);
        Session session = mock(Session.class);
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        ExternalVideoProviderStrategy provider = mock(ExternalVideoProviderStrategy.class);
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
        video.setTitle("Old title");
        video.setDescription("Old description");
        video.setProviderType(VideoProviderType.EXTERNAL);
        video.setSourceReference("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        video.setVisibilityScope(VideoVisibilityScope.GLOBAL);
        video.setPublicationStatus(VideoPublicationStatus.PUBLISHED);
        video.setInheritTitleMetadata(false);
        video.setInheritDescriptionMetadata(false);

        when(videoTrainingService.countAllVideos("")).thenReturn(1L);
        when(videoTrainingService.findAllVideos("", 0, 200)).thenReturn(Collections.singletonList(video));
        when(videoTrainingService.persistVideoChanges(any(VideoTrainingVideo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(provider.fetchProviderPublicationStatus(video.getSourceReference(), VideoVisibilityScope.GLOBAL)).thenReturn(VideoPublicationStatus.WITHDRAWN);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("user-1");
        when(serverConfigurationService.getSmtpFrom()).thenReturn("no-reply@example.com");

        VideoTrainingSynchronizationJob job = new VideoTrainingSynchronizationJob();
        job.setSessionManager(sessionManager);
        job.setServerConfigurationService(serverConfigurationService);
        job.setProviderRegistry(providerRegistry);
        job.setVideoTrainingService(videoTrainingService);

        job.execute(null);

        verify(provider).fetchProviderPublicationStatus(video.getSourceReference(), VideoVisibilityScope.GLOBAL);
        verify(videoTrainingService, times(1)).persistVideoChanges(video);
        verify(videoTrainingService).registerAudit(eq("site-1"), eq("user-1"), eq("VIDEO_UPDATED"), eq("video-1"), eq("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
    }

    @Test
    public void executeShouldNotMarkExternalVideoAsDeletedWhenMetadataIsUnavailable() throws Exception {
        SessionManager sessionManager = mock(SessionManager.class);
        Session session = mock(Session.class);
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        ExternalVideoProviderStrategy provider = mock(ExternalVideoProviderStrategy.class);
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
        video.setTitle("Existing title");
        video.setDescription("Existing description");
        video.setProviderType(VideoProviderType.YOUTUBE_UPLOAD);
        video.setSourceReference("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        video.setVisibilityScope(VideoVisibilityScope.COURSE);
        video.setPublicationStatus(VideoPublicationStatus.PUBLISHED);
        video.setInheritTitleMetadata(true);
        video.setInheritDescriptionMetadata(false);

        when(videoTrainingService.countAllVideos("")).thenReturn(1L);
        when(videoTrainingService.findAllVideos("", 0, 200)).thenReturn(Collections.singletonList(video));
        when(provider.fetchMetadata(video.getSourceReference())).thenReturn(new org.sakaiproject.videotraining.api.model.MetadataFetchResult("", ""));
        when(provider.fetchProviderPublicationStatus(video.getSourceReference(), VideoVisibilityScope.COURSE)).thenReturn(VideoPublicationStatus.PUBLISHED);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("user-1");
        when(serverConfigurationService.getSmtpFrom()).thenReturn("no-reply@example.com");

        VideoTrainingSynchronizationJob job = new VideoTrainingSynchronizationJob();
        job.setSessionManager(sessionManager);
        job.setServerConfigurationService(serverConfigurationService);
        job.setProviderRegistry(providerRegistry);
        job.setVideoTrainingService(videoTrainingService);

        job.execute(null);

        verify(provider).fetchMetadata(video.getSourceReference());
        verify(provider).fetchProviderPublicationStatus(video.getSourceReference(), VideoVisibilityScope.COURSE);
        verify(videoTrainingService, times(0)).persistVideoChanges(video);
    }
}
