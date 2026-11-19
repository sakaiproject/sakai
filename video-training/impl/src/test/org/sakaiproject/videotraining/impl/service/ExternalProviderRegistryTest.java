package org.sakaiproject.videotraining.impl.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.Test;
import org.sakaiproject.videotraining.api.model.MetadataFetchResult;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;
import org.sakaiproject.videotraining.api.service.ExternalVideoProviderStrategy;
import org.springframework.context.support.StaticApplicationContext;

public class ExternalProviderRegistryTest {

    @Test
    public void shouldRegisterAllProviderBeansByType() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        ExternalVideoProviderStrategy strategy = new StubProvider(VideoProviderType.YOUTUBE_UPLOAD);
        applicationContext.getBeanFactory().registerSingleton("youtubeProvider", strategy);
        applicationContext.refresh();

        ExternalProviderRegistry registry = new ExternalProviderRegistry();
        registry.setApplicationContext(applicationContext);
        registry.afterPropertiesSet();

        assertEquals(1, registry.getProviders().size());
        assertSame(strategy, registry.getProvider(VideoProviderType.YOUTUBE_UPLOAD));
    }

    @Test
    public void shouldThrowClearExceptionWhenProviderIsMissing() {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        ExternalProviderRegistry registry = new ExternalProviderRegistry();
        registry.setApplicationContext(applicationContext);
        registry.afterPropertiesSet();

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> registry.getProvider(VideoProviderType.YOUTUBE_UPLOAD));
        assertEquals("No external video provider strategy is registered for YOUTUBE_UPLOAD", thrown.getMessage());
    }

    private static class StubProvider implements ExternalVideoProviderStrategy {

        private final VideoProviderType providerType;

        private StubProvider(VideoProviderType providerType) {
            this.providerType = providerType;
        }

        @Override
        public VideoProviderType getProviderType() {
            return providerType;
        }

        @Override
        public MetadataFetchResult fetchMetadata(String url) {
            return new MetadataFetchResult("title", "description");
        }

        @Override
        public String uploadVideo(VideoTrainingVideo video, Path inputFile) throws IOException {
            return "https://example.com/video";
        }

        @Override
        public VideoPublicationStatus fetchProviderPublicationStatus(String sourceReference, VideoVisibilityScope scope) {
            return VideoPublicationStatus.PUBLISHED;
        }

        @Override
        public boolean syncPrivacy(String sourceReference, VideoPublicationStatus publicationStatus, VideoVisibilityScope visibilityScope) {
            return true;
        }

        @Override
        public String extractVideoId(String url) {
            return "abc123";
        }
    }
}