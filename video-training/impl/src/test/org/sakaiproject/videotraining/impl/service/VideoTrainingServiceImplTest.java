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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.videotraining.api.VideoTrainingConstants;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingAnalyticsEvent;
import org.sakaiproject.videotraining.api.model.VideoTrainingCategory;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;
import org.sakaiproject.videotraining.api.repository.VideoTrainingAnalyticsEventRepository;
import org.sakaiproject.videotraining.api.repository.VideoTrainingCategoryRepository;
import org.sakaiproject.videotraining.api.repository.VideoTrainingLessonLinkRepository;
import org.sakaiproject.videotraining.api.repository.VideoTrainingVideoRepository;
import org.sakaiproject.videotraining.api.util.ContentResourceHelper;

public class VideoTrainingServiceImplTest {

    private static final String SITE_ID = "site-1";
    private static final String SITE_REF = "/site/site-1";
    private static final String VIDEO_ID = "video-1";
    private static final String USER_ID = "user-1";

    private VideoTrainingServiceImpl service;
    private VideoTrainingVideoRepository videoRepository;
    private VideoTrainingAnalyticsEventRepository analyticsEventRepository;
    private VideoTrainingCategoryRepository categoryRepository;
    private VideoTrainingLessonLinkRepository lessonLinkRepository;
    private ContentHostingService contentHostingService;
    private EventTrackingService eventTrackingService;
    private FunctionManager functionManager;
    private SecurityService securityService;
    private ServerConfigurationService serverConfigurationService;
    private SessionManager sessionManager;
    private SiteService siteService;
    private ContentResourceHelper contentResourceHelper;

    @Before
    public void setUp() throws Exception {
        videoRepository = Mockito.mock(VideoTrainingVideoRepository.class);
        analyticsEventRepository = Mockito.mock(VideoTrainingAnalyticsEventRepository.class);
        categoryRepository = Mockito.mock(VideoTrainingCategoryRepository.class);
        lessonLinkRepository = Mockito.mock(VideoTrainingLessonLinkRepository.class);
        contentHostingService = Mockito.mock(ContentHostingService.class);
        eventTrackingService = Mockito.mock(EventTrackingService.class);
        functionManager = Mockito.mock(FunctionManager.class);
        securityService = Mockito.mock(SecurityService.class);
        serverConfigurationService = Mockito.mock(ServerConfigurationService.class);
        sessionManager = Mockito.mock(SessionManager.class);
        siteService = Mockito.mock(SiteService.class);

        service = new VideoTrainingServiceImpl();
        service.setVideoRepository(videoRepository);
        service.setAnalyticsEventRepository(analyticsEventRepository);
        service.setCategoryRepository(categoryRepository);
        service.setLessonLinkRepository(lessonLinkRepository);
        service.setContentHostingService(contentHostingService);
        service.setEventTrackingService(eventTrackingService);
        service.setFunctionManager(functionManager);
        service.setSecurityService(securityService);
        service.setServerConfigurationService(serverConfigurationService);
        service.setSessionManager(sessionManager);
        service.setSiteService(siteService);
        // inject contentResourceHelper mock
        this.contentResourceHelper = Mockito.mock(ContentResourceHelper.class);
        service.setContentResourceHelper(this.contentResourceHelper);

        when(siteService.siteReference(SITE_ID)).thenReturn(SITE_REF);
        when(siteService.allowAccessSite(SITE_ID)).thenReturn(true);
        when(securityService.isSuperUser()).thenReturn(false);
        when(sessionManager.getCurrentSessionUserId()).thenReturn(USER_ID);

        ContentCollection siteCollection = Mockito.mock(ContentCollection.class);
        when(contentHostingService.getSiteCollection(SITE_ID)).thenReturn("/group/" + SITE_ID + "/");
        when(contentHostingService.getCollection("/group/" + SITE_ID + "/")).thenReturn(siteCollection);
        when(contentHostingService.getQuota(siteCollection)).thenReturn(10_000_000L);
        when(siteCollection.getBodySizeK()).thenReturn(0L);
    }

    @Test
    public void updateVideoStatusShouldAllowWithdrawWhenSourceMissingAndMarkDeleted() throws Exception {
        VideoTrainingVideo video = baseVideo();
        video.setPublicationStatus(VideoPublicationStatus.PUBLISHED);
        video.setProviderType(VideoProviderType.NATIVE);
        video.setSourceDeleted(false);
        video.setOwnerId(USER_ID);

        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(video));
        when(sessionManager.getCurrentSessionUserId()).thenReturn(USER_ID);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(true);

        Mockito.when(contentResourceHelper.getContentResource(video.getSourceReference())).thenThrow(new IdUnusedException(video.getSourceReference()));

        when(videoRepository.save(video)).thenReturn(video);

        VideoTrainingVideo saved = service.updateVideoStatus(VIDEO_ID, VideoPublicationStatus.WITHDRAWN);

        assertEquals(VideoPublicationStatus.WITHDRAWN, saved.getPublicationStatus());
        assertTrue(saved.isSourceDeleted());
        verify(videoRepository).save(video);
    }

    @Test
    public void updateVideoStatusShouldRejectPublishWhenSourceMissing() throws Exception {
        VideoTrainingVideo video = baseVideo();
        video.setPublicationStatus(VideoPublicationStatus.DRAFT);
        video.setProviderType(VideoProviderType.NATIVE);
        video.setSourceDeleted(false);
        video.setOwnerId(USER_ID);

        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(video));
        when(sessionManager.getCurrentSessionUserId()).thenReturn(USER_ID);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(true);

        Mockito.when(contentResourceHelper.getContentResource(video.getSourceReference())).thenThrow(new IdUnusedException(video.getSourceReference()));

        Throwable thrown = org.junit.Assert.assertThrows(IllegalStateException.class, () -> service.updateVideoStatus(VIDEO_ID, VideoPublicationStatus.PUBLISHED));
        assertNotNull(thrown);
        verify(videoRepository, never()).save(video);
    }

    @Test
    public void canViewVideoShouldReturnFalseBeforeRelease() {
        Instant now = Instant.now();
        VideoTrainingVideo video = baseVideo();
        video.setReleaseDate(now.plus(1, ChronoUnit.MINUTES));

        boolean result = service.canViewVideo(video, USER_ID, now);

        assertFalse(result);
    }

    @Test
    public void getVisibleVideosForUserPageShouldExcludeVideosWithFutureReleaseDate() {
        Instant now = Instant.now();
        VideoTrainingVideo video = baseVideo();
        video.setReleaseDate(now.plus(1, ChronoUnit.DAYS));
        video.setPublicationStatus(VideoPublicationStatus.PUBLISHED);

        when(videoRepository.findVisibleBySiteIdAt(Mockito.eq(SITE_ID), Mockito.any(Instant.class), Mockito.eq("video"), Mockito.eq(0), Mockito.eq(24)))
                .thenReturn(List.of(video));
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);

        List<VideoTrainingVideo> result = service.getVisibleVideosForUserPage(SITE_ID, USER_ID, Instant.now(), "video", 1, 24);

        // should be filtered out by canViewVideo because releaseDate is in the future
        assertEquals(0, result.size());
    }

    @Test
    public void getVisibleVideosForUserPageShouldExcludeVideosWithPastRetractDate() {
        Instant now = Instant.now();
        VideoTrainingVideo video = baseVideo();
        video.setRetractDate(now.minus(1, ChronoUnit.DAYS));
        video.setPublicationStatus(VideoPublicationStatus.PUBLISHED);

        when(videoRepository.findVisibleBySiteIdAt(Mockito.eq(SITE_ID), Mockito.any(Instant.class), Mockito.eq("video"), Mockito.eq(0), Mockito.eq(24)))
                .thenReturn(List.of(video));
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);

        List<VideoTrainingVideo> result = service.getVisibleVideosForUserPage(SITE_ID, USER_ID, Instant.now(), "video", 1, 24);

        // should be filtered out by canViewVideo because retractDate is in the past
        assertEquals(0, result.size());
    }

    @Test
    public void canViewVideoShouldReturnFalseAfterRetract() {
        Instant now = Instant.now();
        VideoTrainingVideo video = baseVideo();
        video.setRetractDate(now.minus(1, ChronoUnit.MINUTES));

        boolean result = service.canViewVideo(video, USER_ID, now);

        assertFalse(result);
    }

    @Test
    public void canViewVideoShouldReturnTrueForSuperUser() {
        VideoTrainingVideo video = baseVideo();
        when(securityService.isSuperUser(USER_ID)).thenReturn(true);

        boolean result = service.canViewVideo(video, USER_ID, Instant.now());

        assertTrue(result);
    }

    @Test
    public void canViewVideoShouldRequireViewPermission() {
        VideoTrainingVideo video = baseVideo();
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_VIEW, SITE_REF)).thenReturn(false);

        boolean result = service.canViewVideo(video, USER_ID, Instant.now());

        assertFalse(result);
        verify(securityService).unlock(USER_ID, VideoTrainingConstants.PERMISSION_VIEW, SITE_REF);
        verify(securityService, never()).unlock(USER_ID, VideoTrainingConstants.PERMISSION_ANALYTICS, SITE_REF);
    }

    @Test
    public void canViewVideoShouldReturnFalseWhenPublicationStatusIsNotPublished() {
        VideoTrainingVideo video = baseVideo();
        video.setPublicationStatus(VideoPublicationStatus.WITHDRAWN);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);

        boolean result = service.canViewVideo(video, USER_ID, Instant.now());

        assertFalse(result);
        verify(securityService, never()).unlock(USER_ID, VideoTrainingConstants.PERMISSION_VIEW, SITE_REF);
    }

    @Test
    public void canViewVideoShouldReturnFalseWhenVisibilityScopeIsLesson() {
        VideoTrainingVideo video = baseVideo();
        video.setVisibilityScope(VideoVisibilityScope.LESSON);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);

        boolean result = service.canViewVideo(video, USER_ID, Instant.now());

        assertFalse(result);
        verify(securityService, never()).unlock(USER_ID, VideoTrainingConstants.PERMISSION_VIEW, SITE_REF);
    }

    @Test
    public void canViewVideoShouldAllowLessonWhenLessonPageIsProvided() {
        VideoTrainingVideo video = baseVideo();
        video.setVisibilityScope(VideoVisibilityScope.LESSON);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);

        boolean result = service.canViewVideo(video, USER_ID, Instant.now(), "71");

        assertTrue(result);
        verify(securityService, never()).unlock(USER_ID, VideoTrainingConstants.PERMISSION_VIEW, SITE_REF);
    }

    @Test
    public void canViewVideoShouldAllowGlobalWithoutSiteViewPermission() {
        VideoTrainingVideo video = baseVideo();
        video.setVisibilityScope(VideoVisibilityScope.GLOBAL);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE_ALL, SITE_REF)).thenReturn(false);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);

        boolean result = service.canViewVideo(video, USER_ID, Instant.now());

        assertTrue(result);
        verify(securityService, never()).unlock(USER_ID, VideoTrainingConstants.PERMISSION_VIEW, SITE_REF);
    }

    @Test
    public void canViewVideoShouldRequireVideoSpecificPermission() {
        VideoTrainingVideo video = baseVideo();
        video.setRequiredViewPermission("video.training.custom");
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_VIEW, SITE_REF)).thenReturn(true);
        when(securityService.unlock(USER_ID, "video.training.custom", SITE_REF)).thenReturn(false);

        boolean result = service.canViewVideo(video, USER_ID, Instant.now());

        assertFalse(result);
        verify(securityService).unlock(USER_ID, "video.training.custom", SITE_REF);
    }

    @Test
    public void registerViewShouldPersistEventWhenUserCanView() {
        VideoTrainingVideo video = baseVideo();
        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(video));
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_VIEW, SITE_REF)).thenReturn(true);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_VIEW, SITE_REF)).thenReturn(true);

        service.registerView(SITE_ID, VIDEO_ID, USER_ID, Instant.now());

        verify(analyticsEventRepository).save(Mockito.any(VideoTrainingAnalyticsEvent.class));
    }

    @Test
    public void registerViewShouldNotPersistEventWhenUserCannotView() {
        VideoTrainingVideo video = baseVideo();
        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(video));
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_VIEW, SITE_REF)).thenReturn(false);

        service.registerView(SITE_ID, VIDEO_ID, USER_ID, Instant.now());

        verify(analyticsEventRepository, never()).save(Mockito.any(VideoTrainingAnalyticsEvent.class));
    }

    @Test
    public void registerViewShouldPersistEventForLessonVisibilityWhenLessonPageIsProvided() {
        VideoTrainingVideo video = baseVideo();
        video.setVisibilityScope(VideoVisibilityScope.LESSON);
        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(video));
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);

        service.registerView(SITE_ID, VIDEO_ID, USER_ID, Instant.now(), "71");

        verify(analyticsEventRepository).save(Mockito.any(VideoTrainingAnalyticsEvent.class));
    }

    @Test
    public void saveVideoShouldSetOwnerAndDefaultPermissionOnCreate() {
        VideoTrainingVideo video = baseVideo();
        video.setId(null);
        video.setOwnerId(null);
        video.setRequiredViewPermission(null);
        video.setVisibilityScope(null);
        video.setPublicationStatus(null);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(true);
        when(videoRepository.save(video)).thenReturn(video);

        VideoTrainingVideo saved = service.saveVideo(video);

        assertNotNull(saved.getCreatedOn());
        assertNotNull(saved.getModifiedOn());
        assertTrue(USER_ID.equals(saved.getOwnerId()));
        assertTrue(VideoTrainingConstants.PERMISSION_VIEW.equals(saved.getRequiredViewPermission()));
        assertTrue(VideoVisibilityScope.COURSE == saved.getVisibilityScope());
        assertTrue(VideoPublicationStatus.DRAFT == saved.getPublicationStatus());
        verify(videoRepository).save(video);
    }

    @Test
    public void saveVideoShouldRejectCreateWithNonDraftStatus() {
        VideoTrainingVideo video = baseVideo();
        video.setId(null);
        video.setPublicationStatus(VideoPublicationStatus.PUBLISHED);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(true);

        Throwable thrown = org.junit.Assert.assertThrows(IllegalArgumentException.class, () -> service.saveVideo(video));
        assertNotNull(thrown);
        verify(videoRepository, never()).save(video);
    }

    @Test
    public void saveVideoShouldRejectInvalidTransitionFromPublishedToDraft() {
        VideoTrainingVideo existing = baseVideo();
        existing.setPublicationStatus(VideoPublicationStatus.PUBLISHED);

        VideoTrainingVideo update = baseVideo();
        update.setPublicationStatus(VideoPublicationStatus.DRAFT);

        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE_ALL, SITE_REF)).thenReturn(true);
        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(existing));

        Throwable thrown = org.junit.Assert.assertThrows(IllegalArgumentException.class, () -> service.saveVideo(update));
        assertNotNull(thrown);
        verify(videoRepository, never()).save(update);
    }

    @Test
    public void saveVideoShouldRejectExternalSourceWithoutHttpScheme() {
        VideoTrainingVideo video = baseVideo();
        video.setProviderType(VideoProviderType.EXTERNAL);
        video.setSourceReference("kaltura:entry:123");

        Throwable thrown = org.junit.Assert.assertThrows(IllegalArgumentException.class, () -> service.saveVideo(video));
        assertNotNull(thrown);
    }

    @Test
    public void saveVideoShouldRejectWhenUserCannotManage() {
        VideoTrainingVideo video = baseVideo();
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);

        Throwable thrown = org.junit.Assert.assertThrows(SecurityException.class, () -> service.saveVideo(video));
        assertNotNull(thrown);
    }

    @Test
    public void saveVideoShouldRejectGlobalVisibilityWithoutPermission() {
        VideoTrainingVideo video = baseVideo();
        video.setVisibilityScope(VideoVisibilityScope.GLOBAL);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(true);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_GLOBAL, SITE_REF)).thenReturn(false);

        Throwable thrown = org.junit.Assert.assertThrows(SecurityException.class, () -> service.saveVideo(video));
        assertNotNull(thrown);
        verify(videoRepository, never()).save(video);
    }

    @Test
    public void saveVideoShouldAcceptExternalSourceWithHttps() {
        VideoTrainingVideo video = baseVideo();
        video.setProviderType(VideoProviderType.EXTERNAL);
        video.setSourceReference("https://example.org/video/123");
        video.setPublicationStatus(VideoPublicationStatus.DRAFT);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE_ALL, SITE_REF)).thenReturn(true);
        when(videoRepository.save(video)).thenReturn(video);

        VideoTrainingVideo saved = service.saveVideo(video);

        assertTrue(VideoProviderType.EXTERNAL == saved.getProviderType());
        verify(videoRepository).save(video);
    }

    @Test
    public void saveVideoWithCategoryIdsShouldReturnVideoWithCategoriesLoaded() {
        VideoTrainingCategory category = new VideoTrainingCategory();
        category.setId("cat-1");
        category.setSiteId(SITE_ID);
        category.setName("Category 1");

        VideoTrainingVideo video = baseVideo();
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE_ALL, SITE_REF)).thenReturn(true);
        when(videoRepository.save(video)).thenReturn(video);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));
        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(video));

        VideoTrainingVideo saved = service.saveVideoWithCategoryIds(video, List.of("cat-1"));

        assertNotNull(saved.getCategories());
        assertEquals(1, saved.getCategories().size());
        assertTrue(saved.getCategories().contains(category));
        verify(videoRepository, times(2)).save(video);
    }

    @Test
    public void deleteVideoShouldDeleteEventsBeforeVideo() {
        VideoTrainingVideo video = baseVideo();
        VideoTrainingAnalyticsEvent event = new VideoTrainingAnalyticsEvent();
        event.setId("e1");

        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(video));
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE_ALL, SITE_REF)).thenReturn(true);
        when(analyticsEventRepository.findByVideoIdOrderByEventTimeDesc(VIDEO_ID)).thenReturn(List.of(event));

        service.deleteVideo(VIDEO_ID);

        verify(analyticsEventRepository).delete(event);
        verify(videoRepository).delete(video);
    }

    @Test
    public void deleteVideoShouldRejectWhenUserCannotManage() {
        VideoTrainingVideo video = baseVideo();
        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(video));
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);

        Throwable thrown = org.junit.Assert.assertThrows(SecurityException.class, () -> service.deleteVideo(VIDEO_ID));
        assertNotNull(thrown);
    }

    @Test
    public void getSiteLibraryPageShouldDelegateToRepositoryWithOffsetAndLimit() {
        VideoTrainingVideo video = baseVideo();
        when(videoRepository.findBySiteIdOrderByModifiedOnDesc(SITE_ID, "video", 24, 24)).thenReturn(List.of(video));

        List<VideoTrainingVideo> result = service.getSiteLibraryPage(SITE_ID, "video", 2, 24);

        assertEquals(1, result.size());
        verify(videoRepository).findBySiteIdOrderByModifiedOnDesc(SITE_ID, "video", 24, 24);
    }

    @Test
    public void getGlobalVideosForUserShouldDelegateToRepositoryWithOffsetAndLimit() {
        VideoTrainingVideo video = baseVideo();
        when(securityService.isSuperUser(USER_ID)).thenReturn(false);
        when(videoRepository.findVisibleByGlobal("video", 24, 24)).thenReturn(List.of(video));

        List<VideoTrainingVideo> result = service.getGlobalVideosForUser(USER_ID, "video", 2, 24);

        assertEquals(1, result.size());
        verify(videoRepository).findVisibleByGlobal("video", 24, 24);
    }

    @Test
    public void getSiteViewableVideosForUserPageShouldUseOwnerVisibilityAndOffset() {
        VideoTrainingVideo video = baseVideo();
        video.setOwnerId(USER_ID);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE_ALL, SITE_REF)).thenReturn(false);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(true);
        when(videoRepository.findVisibleBySiteIdAt(Mockito.eq(SITE_ID), Mockito.any(Instant.class), Mockito.eq("video"), Mockito.eq(24), Mockito.eq(24)))
            .thenReturn(List.of(video));

        List<VideoTrainingVideo> result = service.getSiteViewableVideosForUserPage(SITE_ID, USER_ID, "video", 2, 24);

        assertEquals(1, result.size());
        verify(videoRepository).findVisibleBySiteIdAt(Mockito.eq(SITE_ID), Mockito.any(Instant.class), Mockito.eq("video"), Mockito.eq(24), Mockito.eq(24));
    }

    @Test
    public void countVisibleVideosForUserShouldReturnZeroWhenUserCannotViewSite() {
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_VIEW, SITE_REF)).thenReturn(false);

        long result = service.countVisibleVideosForUser(SITE_ID, USER_ID, Instant.now(), "");

        assertEquals(0L, result);
        verify(videoRepository, never()).countVisibleBySiteIdAt(Mockito.eq(SITE_ID), Mockito.any(Instant.class), Mockito.anyString());
    }

    @Test
    public void countVisibleVideosForUserShouldDelegateWhenUserHasViewPermission() {
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(false);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_VIEW, SITE_REF)).thenReturn(true);
        when(videoRepository.countVisibleBySiteIdAt(Mockito.eq(SITE_ID), Mockito.any(Instant.class), Mockito.eq("training"))).thenReturn(42L);

        long result = service.countVisibleVideosForUser(SITE_ID, USER_ID, Instant.now(), "training");

        assertEquals(42L, result);
        verify(videoRepository).countVisibleBySiteIdAt(Mockito.eq(SITE_ID), Mockito.any(Instant.class), Mockito.eq("training"));
    }

    @Test
    public void countSiteLibraryShouldUseCacheWithinTtl() {
        when(videoRepository.countBySiteId(SITE_ID, "video")).thenReturn(7L);

        long first = service.countSiteLibrary(SITE_ID, "video");
        long second = service.countSiteLibrary(SITE_ID, "video");

        assertEquals(7L, first);
        assertEquals(7L, second);
        verify(videoRepository, times(1)).countBySiteId(SITE_ID, "video");
    }

    @Test
    public void saveVideoShouldInvalidateListCaches() {
        when(videoRepository.countBySiteId(SITE_ID, "video")).thenReturn(4L);

        long beforeSave = service.countSiteLibrary(SITE_ID, "video");
        assertEquals(4L, beforeSave);

        VideoTrainingVideo video = baseVideo();
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE_ALL, SITE_REF)).thenReturn(true);
        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(video));
        when(videoRepository.save(video)).thenReturn(video);
        service.saveVideo(video);

        long afterSave = service.countSiteLibrary(SITE_ID, "video");
        assertEquals(4L, afterSave);
        verify(videoRepository, times(2)).countBySiteId(SITE_ID, "video");
    }

    @Test
    public void saveVideoShouldRegisterVisibilityScopeChangedEvent() {
        VideoTrainingVideo existing = baseVideo();
        existing.setVisibilityScope(VideoVisibilityScope.GLOBAL);

        VideoTrainingVideo update = baseVideo();
        update.setVisibilityScope(VideoVisibilityScope.COURSE);

        Event mockEvent = Mockito.mock(Event.class);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE_ALL, SITE_REF)).thenReturn(true);
        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(existing));
        when(videoRepository.save(update)).thenReturn(update);
        when(eventTrackingService.newEvent(Mockito.eq("video.training.visibility.scope.changed"),
                Mockito.anyString(), Mockito.eq(SITE_ID), Mockito.eq(true),
                Mockito.eq(NotificationService.NOTI_OPTIONAL))).thenReturn(mockEvent);

        service.saveVideo(update);

        verify(eventTrackingService).newEvent(Mockito.eq("video.training.visibility.scope.changed"),
                Mockito.anyString(), Mockito.eq(SITE_ID), Mockito.eq(true),
                Mockito.eq(NotificationService.NOTI_OPTIONAL));
        verify(eventTrackingService).post(mockEvent);
    }

    @Test
    public void saveCategoryShouldSetAuditFields() {
        VideoTrainingCategory category = new VideoTrainingCategory();
        category.setSiteId(SITE_ID);
        category.setName("Module A");
        category.setParentCategoryId(null);
        category.setSortOrder(2);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_CATEGORIES_MANAGE, SITE_REF)).thenReturn(true);
        when(categoryRepository.save(category)).thenReturn(category);

        VideoTrainingCategory saved = service.saveCategory(category);

        assertEquals(USER_ID, saved.getCreatedBy());
        assertEquals(USER_ID, saved.getModifiedBy());
        assertNotNull(saved.getCreatedOn());
        assertNotNull(saved.getModifiedOn());
        verify(categoryRepository).save(category);
    }

    @Test
    public void saveCategoryShouldRejectMovingCategoryWithChildrenIntoParent() {
        VideoTrainingCategory root = new VideoTrainingCategory();
        root.setId("root-1");
        root.setSiteId(SITE_ID);
        root.setName("Root");
        root.setParentCategoryId(null);

        VideoTrainingCategory child = new VideoTrainingCategory();
        child.setId("child-1");
        child.setSiteId(SITE_ID);
        child.setName("Child");
        child.setParentCategoryId("root-1");

        VideoTrainingCategory moving = new VideoTrainingCategory();
        moving.setId("root-1");
        moving.setSiteId(SITE_ID);
        moving.setName("Root");
        moving.setParentCategoryId("child-1");

        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_CATEGORIES_MANAGE, SITE_REF)).thenReturn(true);
        when(categoryRepository.findById("root-1")).thenReturn(Optional.of(root));
        when(categoryRepository.findById("child-1")).thenReturn(Optional.of(child));
        when(categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(SITE_ID, 0, 0)).thenReturn(List.of(root, child));

        Throwable thrown = org.junit.Assert.assertThrows(IllegalArgumentException.class, () -> service.saveCategory(moving));
        assertNotNull(thrown);
        verify(categoryRepository, never()).save(moving);
    }

    @Test
    public void getCategoryDeleteImpactShouldIncludeChildrenAndVideos() {
        VideoTrainingCategory root = new VideoTrainingCategory();
        root.setId("root-1");
        root.setSiteId(SITE_ID);
        root.setName("Root");

        VideoTrainingCategory child = new VideoTrainingCategory();
        child.setId("child-1");
        child.setSiteId(SITE_ID);
        child.setName("Child");
        child.setParentCategoryId("root-1");

        VideoTrainingVideo video = baseVideo();
        video.setId("video-2");
        root.setVideos(Set.of(video));
        child.setVideos(Set.of(video));

        when(categoryRepository.findById("root-1")).thenReturn(Optional.of(root));
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_CATEGORIES_MANAGE, SITE_REF)).thenReturn(true);
        when(categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(SITE_ID, 0, 0)).thenReturn(List.of(root, child));

        org.sakaiproject.videotraining.api.model.VideoTrainingCategoryDeleteImpact impact = service.getCategoryDeleteImpact("root-1");

        assertEquals(root, impact.getCategory());
        assertEquals(2, impact.getCategoriesToDelete().size());
        assertEquals(1, impact.getAffectedVideos().size());
        assertEquals("video-2", impact.getAffectedVideos().get(0).getId());
    }

    @Test
    public void deleteCategoryShouldRemoveChildrenAndUnlinkVideos() {
        VideoTrainingCategory root = new VideoTrainingCategory();
        root.setId("root-1");
        root.setSiteId(SITE_ID);
        root.setName("Root");

        VideoTrainingCategory child = new VideoTrainingCategory();
        child.setId("child-1");
        child.setSiteId(SITE_ID);
        child.setName("Child");
        child.setParentCategoryId("root-1");

        VideoTrainingVideo video = baseVideo();
        video.setId("video-2");
        video.setCategories(new java.util.HashSet<>(Set.of(root, child)));
        root.setVideos(Set.of(video));
        child.setVideos(Set.of(video));

        when(categoryRepository.findById("root-1")).thenReturn(Optional.of(root));
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_CATEGORIES_MANAGE, SITE_REF)).thenReturn(true);
        when(categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(SITE_ID, 0, 0)).thenReturn(List.of(root, child));
        when(videoRepository.save(any(VideoTrainingVideo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deleteCategory("root-1");

        verify(videoRepository).save(video);
        verify(categoryRepository, times(2)).delete(any(VideoTrainingCategory.class));
    }

    @Test
    public void getCategoryTreeShouldReturnRootsWithChildrenAndUniqueVideoCounts() {
        VideoTrainingCategory root = new VideoTrainingCategory();
        root.setId("root-1");
        root.setSiteId(SITE_ID);
        root.setName("Root");

        VideoTrainingCategory child = new VideoTrainingCategory();
        child.setId("child-1");
        child.setSiteId(SITE_ID);
        child.setName("Child");
        child.setParentCategoryId("root-1");

        VideoTrainingVideo video = baseVideo();
        video.setId("video-2");
        root.setVideos(Set.of(video));
        child.setVideos(Set.of(video));

        when(categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(SITE_ID, 0, 0)).thenReturn(List.of(root, child));

        List<VideoTrainingCategory> tree = service.getCategoryTree(SITE_ID, "", 1, 10);

        assertEquals(1, tree.size());
        assertEquals(1L, tree.get(0).getVideoCount().longValue());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals(1L, tree.get(0).getChildren().get(0).getVideoCount().longValue());
    }

    @Test
    public void getSelectableCategoryTreeShouldIncludeGlobalCategories() {
        VideoTrainingCategory local = new VideoTrainingCategory();
        local.setId("local-1");
        local.setSiteId(SITE_ID);
        local.setName("Local");

        VideoTrainingCategory shared = new VideoTrainingCategory();
        shared.setId("global-1");
        shared.setSiteId("!global");
        shared.setName("Shared");

        when(categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(SITE_ID, 0, 0)).thenReturn(List.of(local));
        when(categoryRepository.findBySiteIdStartingWithOrderBySortOrderAscNameAsc("!", 0, 0)).thenReturn(List.of(shared));

        List<VideoTrainingCategory> tree = service.getSelectableCategoryTree(SITE_ID, "", 1, 10);

        assertEquals(2, tree.size());
    }

    @Test
    public void saveVideoWithCategoryIdsShouldAllowGlobalCategory() {
        VideoTrainingCategory category = new VideoTrainingCategory();
        category.setId("cat-1");
        category.setSiteId("!global");
        category.setName("Category 1");

        VideoTrainingVideo video = baseVideo();
        video.setOwnerId(USER_ID);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE_ALL, SITE_REF)).thenReturn(false);
        when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_MANAGE, SITE_REF)).thenReturn(true);
        when(videoRepository.save(video)).thenReturn(video);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));
        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(video));

        VideoTrainingVideo saved = service.saveVideoWithCategoryIds(video, List.of("cat-1"));

        assertNotNull(saved.getCategories());
        assertEquals(1, saved.getCategories().size());
        assertTrue(saved.getCategories().contains(category));
    }

    @Test
    public void getValidPublicationStatusTransitionsShouldAllowPendingApprovalWhenModerationEnabled() {
        Mockito.doReturn(true).when(serverConfigurationService)
            .getBoolean(VideoTrainingConstants.MODERATION_ENABLED_PROPERTY, VideoTrainingConstants.DEFAULT_MODERATION_ENABLED);

        VideoPublicationStatus[] transitions = service.getValidPublicationStatusTransitions(VideoPublicationStatus.DRAFT, VideoVisibilityScope.COURSE);

        assertTrue(java.util.Arrays.asList(transitions).contains(VideoPublicationStatus.PENDING_APPROVAL));
    }

    @Test
    public void validatePublicationStatusTransitionShouldAllowDraftToPendingApprovalWhenModerationEnabled() {
        Mockito.doReturn(true).when(serverConfigurationService)
            .getBoolean(VideoTrainingConstants.MODERATION_ENABLED_PROPERTY, VideoTrainingConstants.DEFAULT_MODERATION_ENABLED);

        VideoTrainingVideo existing = baseVideo();
        existing.setPublicationStatus(VideoPublicationStatus.DRAFT);
        existing.setVisibilityScope(VideoVisibilityScope.COURSE);
        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(existing));
        when(videoRepository.save(existing)).thenReturn(existing);

        service.updateVideoStatus(VIDEO_ID, VideoPublicationStatus.PENDING_APPROVAL);

        verify(videoRepository).save(existing);
    }

    @Test
    public void validatePublicationStatusTransitionShouldAllowPendingApprovalToPublishedWhenModerationEnabled() throws Exception {
        Mockito.doReturn(true).when(serverConfigurationService)
            .getBoolean(VideoTrainingConstants.MODERATION_ENABLED_PROPERTY, VideoTrainingConstants.DEFAULT_MODERATION_ENABLED);

        VideoTrainingVideo existing = baseVideo();
        existing.setPublicationStatus(VideoPublicationStatus.PENDING_APPROVAL);
        existing.setVisibilityScope(VideoVisibilityScope.COURSE);
        Mockito.doReturn(Mockito.mock(ContentResource.class))
            .when(contentResourceHelper)
            .getContentResource(existing.getSourceReference());
        when(videoRepository.findById(VIDEO_ID)).thenReturn(Optional.of(existing));
        when(videoRepository.save(existing)).thenReturn(existing);

        service.updateVideoStatus(VIDEO_ID, VideoPublicationStatus.PUBLISHED);

        verify(videoRepository).save(existing);
    }

    private VideoTrainingVideo baseVideo() {
        VideoTrainingVideo video = new VideoTrainingVideo();
        video.setId(VIDEO_ID);
        video.setSiteId(SITE_ID);
        video.setTitle("Video");
        video.setProviderType(VideoProviderType.NATIVE);
        video.setSourceReference("source");
        video.setPublicationStatus(VideoPublicationStatus.PUBLISHED);
        video.setRequiredViewPermission(VideoTrainingConstants.PERMISSION_VIEW);
        return video;
    }
}
