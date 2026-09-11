package org.sakaiproject.videotraining.tool.mvc;

import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.api.LocaleService;
import org.sakaiproject.videotraining.api.VideoTrainingConstants;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;
import org.sakaiproject.videotraining.api.service.ExternalMetadataService;
import org.sakaiproject.videotraining.api.service.ProcessJobService;
import org.sakaiproject.videotraining.api.service.VideoTrainingOAuthCredentialsService;
import org.sakaiproject.videotraining.api.service.VideoTrainingService;
import org.sakaiproject.videotraining.api.service.VideoTrainingUploadService;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class VideoTrainingControllerNegativeTest {

    private static final String SITE_ID = "site-1";
    private static final String USER_ID = "user-1";

    private MessageSource messageSource;
    private SessionManager sessionManager;
    private Session session;
    private SiteService siteService;
    private ToolManager toolManager;
    private UserTimeService userTimeService;
    private VideoTrainingService videoTrainingService;
    private VideoTrainingOAuthCredentialsService oauthCredentialsService;
    private ProcessJobService processJobService;
    private ServerConfigurationService serverConfigurationService;
    private SecurityService securityService;
    private ExternalMetadataService externalMetadataService;
    private VideoTrainingUploadService uploadService;
    private LocaleService localeService;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        messageSource = mock(MessageSource.class);
        sessionManager = mock(SessionManager.class);
        session = mock(Session.class);
        siteService = mock(SiteService.class);
        toolManager = mock(ToolManager.class);
        userTimeService = mock(UserTimeService.class);
        videoTrainingService = mock(VideoTrainingService.class);
        oauthCredentialsService = mock(VideoTrainingOAuthCredentialsService.class);
        processJobService = mock(ProcessJobService.class);
        serverConfigurationService = mock(ServerConfigurationService.class);
        securityService = mock(SecurityService.class);
        externalMetadataService = mock(ExternalMetadataService.class);
        uploadService = mock(VideoTrainingUploadService.class);
        localeService = mock(LocaleService.class);
        when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(Locale.getDefault());

        Placement placement = mock(Placement.class);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn(SITE_ID);
        when(siteService.siteReference(SITE_ID)).thenReturn("siteRef-1");
        when(sessionManager.getCurrentSessionUserId()).thenReturn(USER_ID);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(securityService.isSuperUser()).thenReturn(true);
        when(uploadService.getConfiguredMaxNativeUploadBytes()).thenReturn(1024L * 1024L);
        when(uploadService.isValidNativeUpload(anyString(), anyString(), anyBoolean())).thenReturn(true);
        when(messageSource.getMessage(anyString(), nullable(Object[].class), any(Locale.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(externalMetadataService.extractVideoId(anyString())).thenReturn("dQw4w9WgXcQ");
        when(externalMetadataService.retrieveVideoProvider(anyString())).thenReturn("YouTube");
        when(externalMetadataService.fetch(anyString())).thenReturn(new org.sakaiproject.videotraining.api.model.MetadataFetchResult("Title test", "Description test"));

        VideoTrainingController controller = new VideoTrainingController(
                messageSource,
                sessionManager,
                siteService,
                toolManager,
                userTimeService,
                videoTrainingService,
                oauthCredentialsService,
                processJobService,
                serverConfigurationService,
                securityService,
                externalMetadataService,
                uploadService,
                localeService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

        @Test
        public void newVideo_hidesGlobalVisibilityWithoutPermission() throws Exception {
                when(securityService.isSuperUser()).thenReturn(false);
                when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(false);
                when(videoTrainingService.hasManagePermission(SITE_ID, USER_ID)).thenReturn(true);
                when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_GLOBAL, "siteRef-1")).thenReturn(false);
                when(videoTrainingService.getSelectableCategoryTree(SITE_ID, "", 1, Integer.MAX_VALUE)).thenReturn(java.util.Collections.emptyList());

                org.springframework.test.web.servlet.MvcResult result = mockMvc.perform(get("/videos/new"))
                                .andExpect(status().isOk())
                                .andReturn();

                java.util.List<?> visibilityScopes = (java.util.List<?>) result.getModelAndView().getModel().get("visibilityScopes");
                assertFalse(visibilityScopes.contains(VideoVisibilityScope.GLOBAL));
        }

        @Test
        public void newVideo_showsGlobalVisibilityForSuperUser() throws Exception {
                when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(true);
                when(videoTrainingService.getSelectableCategoryTree(SITE_ID, "", 1, Integer.MAX_VALUE)).thenReturn(java.util.Collections.emptyList());

                org.springframework.test.web.servlet.MvcResult result = mockMvc.perform(get("/videos/new"))
                                .andExpect(status().isOk())
                                .andReturn();

                java.util.List<?> visibilityScopes = (java.util.List<?>) result.getModelAndView().getModel().get("visibilityScopes");
                assertTrue(visibilityScopes.contains(VideoVisibilityScope.GLOBAL));
        }

    @Test
    public void createVideo_rejectsOversizedNativeUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "nativeFile",
                "video.mp4",
                "video/mp4",
                new byte[1024 * 1024 + 64 * 1024 + 1]);

        mockMvc.perform(multipart("/videos")
                        .file(file)
                        .param("title", "Title")
                        .param("description", "Description")
                        .param("providerType", VideoProviderType.NATIVE.name())
                        .param("sourceMode", VideoTrainingConstants.SOURCE_MODE_UPLOAD)
                        .param("visibilityScope", VideoVisibilityScope.COURSE.name())
                        .param("publicationStatus", VideoPublicationStatus.DRAFT.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/videos/new"))
                .andExpect(flash().attribute("error", "video.training.nativeUploadTooLarge"));

        verify(videoTrainingService, never()).saveVideo(any(VideoTrainingVideo.class));
    }

    @Test
    public void createVideo_rejectsUnsupportedFileExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "nativeFile",
                "video.exe",
                "application/octet-stream",
                "dummy".getBytes());

        when(uploadService.isValidNativeUpload(eq("video.exe"), eq("application/octet-stream"), anyBoolean())).thenReturn(false);

        mockMvc.perform(multipart("/videos")
                        .file(file)
                        .param("title", "Title")
                        .param("description", "Description")
                        .param("providerType", VideoProviderType.NATIVE.name())
                        .param("sourceMode", VideoTrainingConstants.SOURCE_MODE_UPLOAD)
                        .param("visibilityScope", VideoVisibilityScope.COURSE.name())
                        .param("publicationStatus", VideoPublicationStatus.DRAFT.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/videos/new"))
                .andExpect(flash().attribute("error", "video.training.nativeUploadInvalidType"));

        verify(videoTrainingService, never()).saveVideo(any(VideoTrainingVideo.class));
    }

    @Test
    public void createVideo_rejectsYoutubeUploadWhenOauthServiceIsNotConfigured() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "nativeFile",
                "video.mp4",
                "video/mp4",
                "dummy".getBytes());

        when(oauthCredentialsService.isConfigured(VideoProviderType.YOUTUBE_UPLOAD)).thenReturn(false);

        mockMvc.perform(multipart("/videos")
                        .file(file)
                        .param("title", "Title")
                        .param("description", "Description")
                        .param("providerType", VideoProviderType.YOUTUBE_UPLOAD.name())
                        .param("sourceMode", VideoTrainingConstants.SOURCE_MODE_UPLOAD)
                        .param("visibilityScope", VideoVisibilityScope.COURSE.name())
                        .param("publicationStatus", VideoPublicationStatus.DRAFT.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/videos/new"))
                .andExpect(flash().attribute("error", "video.training.invalidProvider"));

        verify(oauthCredentialsService).isConfigured(VideoProviderType.YOUTUBE_UPLOAD);
        verify(videoTrainingService, never()).saveVideo(any(VideoTrainingVideo.class));
    }

    @Test
    public void youtubeCredentialsCallback_rejectsInvalidStateOrCode() throws Exception {
        when(session.getAttribute(VideoTrainingConstants.PROVIDER_YOUTUBE_AUTH_STATE_SESSION_KEY)).thenReturn("expected-state");

        mockMvc.perform(get("/credentials/youtube/callback")
                        .param("code", "auth-code")
                        .param("state", "wrong-state"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/credentials"))
                .andExpect(flash().attribute("error", "video.training.credentials.youtube.authorizationFailed"));

        verify(session).removeAttribute(VideoTrainingConstants.PROVIDER_YOUTUBE_AUTH_STATE_SESSION_KEY);
        verify(oauthCredentialsService, never()).saveCredentials(
                eq(VideoProviderType.YOUTUBE_UPLOAD),
                anyString(),
                anyString(),
                anyString(),
                anyString());
    }

    @Test
    public void youtubeCredentialsCallback_redirectsWhenCredentialsMissing() throws Exception {
        when(session.getAttribute(VideoTrainingConstants.PROVIDER_YOUTUBE_AUTH_STATE_SESSION_KEY)).thenReturn("expected-state");
        when(oauthCredentialsService.getCredentials(VideoProviderType.YOUTUBE_UPLOAD)).thenReturn(Optional.empty());

        mockMvc.perform(get("/credentials/youtube/callback")
                        .param("code", "auth-code")
                        .param("state", "expected-state"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/credentials"))
                .andExpect(flash().attribute("error", "video.training.credentials.youtube.notConfigured"));

        verify(oauthCredentialsService, never()).saveCredentials(
                eq(VideoProviderType.YOUTUBE_UPLOAD),
                anyString(),
                anyString(),
                anyString(),
                anyString());
    }

    @Test
    public void updateVideo_fallsBackWhenSaveFails() throws Exception {
        VideoTrainingVideo existing = new VideoTrainingVideo();
        existing.setId("video-1");
        existing.setSiteId(SITE_ID);
        existing.setOwnerId(USER_ID);
        existing.setTitle("Existing title");
        existing.setDescription("Existing description");
        existing.setProviderType(VideoProviderType.EXTERNAL);
        existing.setSourceReference("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        existing.setVisibilityScope(VideoVisibilityScope.COURSE);
        existing.setPublicationStatus(VideoPublicationStatus.DRAFT);

        when(videoTrainingService.getVideoById("video-1")).thenReturn(Optional.of(existing));
        when(videoTrainingService.saveVideo(any(VideoTrainingVideo.class)))
            .thenThrow(new RuntimeException("save boom"));

        mockMvc.perform(multipart("/videos/video-1")
                        .param("title", "Updated title")
                        .param("description", "Updated description")
                        .param("providerType", VideoProviderType.EXTERNAL.name())
                        .param("sourceMode", VideoTrainingConstants.SOURCE_MODE_EXTERNAL)
                        .param("sourceReference", "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                        .param("visibilityScope", VideoVisibilityScope.COURSE.name())
                        .param("publicationStatus", VideoPublicationStatus.DRAFT.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/videos/video-1/edit"))
                .andExpect(flash().attribute("error", "video.training.invalidInput"));

        verify(videoTrainingService, times(2)).saveVideo(any(VideoTrainingVideo.class));
    }

        @Test
        public void publishVideo_allowsManageOnlyWhenModerationIsDisabled() throws Exception {
                when(securityService.isSuperUser()).thenReturn(false);
                when(siteService.siteReference(SITE_ID)).thenReturn("siteRef-1");

                VideoTrainingVideo existing = new VideoTrainingVideo();
                existing.setId("video-1");
                existing.setSiteId(SITE_ID);
                existing.setOwnerId(USER_ID);
                existing.setVisibilityScope(VideoVisibilityScope.COURSE);
                existing.setPublicationStatus(VideoPublicationStatus.DRAFT);

                VideoTrainingVideo saved = new VideoTrainingVideo();
                saved.setId("video-1");
                saved.setSiteId(SITE_ID);
                saved.setOwnerId(USER_ID);
                saved.setVisibilityScope(VideoVisibilityScope.COURSE);
                saved.setPublicationStatus(VideoPublicationStatus.PUBLISHED);

                when(videoTrainingService.getVideoById("video-1")).thenReturn(Optional.of(existing));
                when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(false);
                when(videoTrainingService.hasManagePermission(SITE_ID, USER_ID)).thenReturn(true);
                when(videoTrainingService.updateVideoStatus("video-1", VideoPublicationStatus.PUBLISHED)).thenReturn(saved);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/videos/video-1/publish"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/videos"))
                                .andExpect(flash().attribute("success", "video.training.published"));

                verify(videoTrainingService).updateVideoStatus("video-1", VideoPublicationStatus.PUBLISHED);
        }

        @Test
        public void publishVideo_fallsBackToPendingApprovalWhenModerationIsEnabledAndUserCannotApprove() throws Exception {
                when(securityService.isSuperUser()).thenReturn(false);
                when(serverConfigurationService.getBoolean(VideoTrainingConstants.MODERATION_ENABLED_PROPERTY, VideoTrainingConstants.DEFAULT_MODERATION_ENABLED)).thenReturn(true);
                when(siteService.siteReference(SITE_ID)).thenReturn("siteRef-1");

                VideoTrainingVideo existing = new VideoTrainingVideo();
                existing.setId("video-1");
                existing.setSiteId(SITE_ID);
                existing.setOwnerId(USER_ID);
                existing.setVisibilityScope(VideoVisibilityScope.GLOBAL);
                existing.setPublicationStatus(VideoPublicationStatus.DRAFT);

                VideoTrainingVideo saved = new VideoTrainingVideo();
                saved.setId("video-1");
                saved.setSiteId(SITE_ID);
                saved.setOwnerId(USER_ID);
                saved.setVisibilityScope(VideoVisibilityScope.GLOBAL);
                saved.setPublicationStatus(VideoPublicationStatus.PENDING_APPROVAL);

                when(videoTrainingService.getVideoById("video-1")).thenReturn(Optional.of(existing));
                when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(false);
                when(videoTrainingService.hasManagePermission(SITE_ID, USER_ID)).thenReturn(true);
                when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_APPROVE_PUBLISH, "siteRef-1")).thenReturn(false);
                when(videoTrainingService.updateVideoStatus("video-1", VideoPublicationStatus.PENDING_APPROVAL)).thenReturn(saved);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/videos/video-1/publish"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/videos"))
                                .andExpect(flash().attribute("success", "video.training.pendingApprovalSubmitted"));

                verify(videoTrainingService).updateVideoStatus("video-1", VideoPublicationStatus.PENDING_APPROVAL);
        }

        @Test
        public void submitVideoForApproval_allowsManageOnlyWhenModerationIsEnabled() throws Exception {
                when(securityService.isSuperUser()).thenReturn(false);
                when(serverConfigurationService.getBoolean(VideoTrainingConstants.MODERATION_ENABLED_PROPERTY, VideoTrainingConstants.DEFAULT_MODERATION_ENABLED)).thenReturn(true);

                VideoTrainingVideo existing = new VideoTrainingVideo();
                existing.setId("video-1");
                existing.setSiteId(SITE_ID);
                existing.setOwnerId(USER_ID);
                existing.setVisibilityScope(VideoVisibilityScope.GLOBAL);
                existing.setPublicationStatus(VideoPublicationStatus.DRAFT);

                VideoTrainingVideo saved = new VideoTrainingVideo();
                saved.setId("video-1");
                saved.setSiteId(SITE_ID);
                saved.setOwnerId(USER_ID);
                saved.setVisibilityScope(VideoVisibilityScope.GLOBAL);
                saved.setPublicationStatus(VideoPublicationStatus.PENDING_APPROVAL);

                when(videoTrainingService.getVideoById("video-1")).thenReturn(Optional.of(existing));
                when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(false);
                when(videoTrainingService.hasManagePermission(SITE_ID, USER_ID)).thenReturn(true);
                when(videoTrainingService.updateVideoStatus("video-1", VideoPublicationStatus.PENDING_APPROVAL)).thenReturn(saved);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/videos/video-1/submit-approval"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/videos"))
                                .andExpect(flash().attribute("success", "video.training.pendingApprovalSubmitted"));

                verify(videoTrainingService).updateVideoStatus("video-1", VideoPublicationStatus.PENDING_APPROVAL);
        }

        @Test
        public void publishVideo_rejectsPendingApprovalWithoutApprovalPermission() throws Exception {
                when(securityService.isSuperUser()).thenReturn(false);
                when(serverConfigurationService.getBoolean(VideoTrainingConstants.MODERATION_ENABLED_PROPERTY, VideoTrainingConstants.DEFAULT_MODERATION_ENABLED)).thenReturn(true);
                when(siteService.siteReference(SITE_ID)).thenReturn("siteRef-1");

                VideoTrainingVideo existing = new VideoTrainingVideo();
                existing.setId("video-1");
                existing.setSiteId(SITE_ID);
                existing.setOwnerId(USER_ID);
                existing.setVisibilityScope(VideoVisibilityScope.GLOBAL);
                existing.setPublicationStatus(VideoPublicationStatus.PENDING_APPROVAL);

                when(videoTrainingService.getVideoById("video-1")).thenReturn(Optional.of(existing));
                when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(false);
                when(videoTrainingService.hasManagePermission(SITE_ID, USER_ID)).thenReturn(true);
                when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_APPROVE_PUBLISH, "siteRef-1")).thenReturn(false);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/videos/video-1/publish"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/videos"))
                                .andExpect(flash().attribute("error", "video.training.accessDenied"));

                verify(videoTrainingService, never()).updateVideoStatus(anyString(), any(VideoPublicationStatus.class));
        }

        @Test
        public void publishVideo_allowsPendingApprovalWithApprovalPermission() throws Exception {
                when(securityService.isSuperUser()).thenReturn(false);
                when(serverConfigurationService.getBoolean(VideoTrainingConstants.MODERATION_ENABLED_PROPERTY, VideoTrainingConstants.DEFAULT_MODERATION_ENABLED)).thenReturn(true);
                when(siteService.siteReference(SITE_ID)).thenReturn("siteRef-1");

                VideoTrainingVideo existing = new VideoTrainingVideo();
                existing.setId("video-1");
                existing.setSiteId(SITE_ID);
                existing.setOwnerId(USER_ID);
                existing.setVisibilityScope(VideoVisibilityScope.GLOBAL);
                existing.setPublicationStatus(VideoPublicationStatus.PENDING_APPROVAL);

                VideoTrainingVideo saved = new VideoTrainingVideo();
                saved.setId("video-1");
                saved.setSiteId(SITE_ID);
                saved.setOwnerId(USER_ID);
                saved.setVisibilityScope(VideoVisibilityScope.GLOBAL);
                saved.setPublicationStatus(VideoPublicationStatus.PUBLISHED);

                when(videoTrainingService.getVideoById("video-1")).thenReturn(Optional.of(existing));
                when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(false);
                when(videoTrainingService.hasManagePermission(SITE_ID, USER_ID)).thenReturn(true);
                when(securityService.unlock(USER_ID, VideoTrainingConstants.PERMISSION_APPROVE_PUBLISH, "siteRef-1")).thenReturn(true);
                when(videoTrainingService.updateVideoStatus("video-1", VideoPublicationStatus.PUBLISHED)).thenReturn(saved);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/videos/video-1/publish"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/videos"))
                                .andExpect(flash().attribute("success", "video.training.published"));

                verify(videoTrainingService).updateVideoStatus("video-1", VideoPublicationStatus.PUBLISHED);
        }
}
