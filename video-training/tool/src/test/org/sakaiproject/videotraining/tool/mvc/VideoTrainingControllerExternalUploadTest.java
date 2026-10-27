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

package org.sakaiproject.videotraining.tool.mvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.util.api.LocaleService;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.service.ProcessJobService;
import org.sakaiproject.videotraining.api.service.VideoTrainingOAuthCredentialsService;
import org.sakaiproject.videotraining.api.service.VideoTrainingService;
import org.sakaiproject.videotraining.api.service.ExternalMetadataService;
import org.sakaiproject.videotraining.api.service.VideoTrainingUploadService;
import org.sakaiproject.videotraining.tool.mvc.VideoTrainingController;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class VideoTrainingControllerExternalUploadTest {

    @Test
    public void createVideo_createsPendingProcessJob_whenYoutubeUpload() throws Exception {
        MessageSource messageSource = mock(MessageSource.class);
        SessionManager sessionManager = mock(SessionManager.class);
        Placement placement = mock(Placement.class);
        SiteService siteService = mock(SiteService.class);
        ToolManager toolManager = mock(ToolManager.class);
        UserTimeService userTimeService = mock(UserTimeService.class);
        VideoTrainingService videoTrainingService = mock(VideoTrainingService.class);
        VideoTrainingOAuthCredentialsService oauthCredentialsService = mock(VideoTrainingOAuthCredentialsService.class);
        ProcessJobService processJobService = mock(ProcessJobService.class);
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        SecurityService securityService = mock(SecurityService.class);
        ExternalMetadataService externalMetadataService = mock(ExternalMetadataService.class);
        VideoTrainingUploadService uploadService = mock(VideoTrainingUploadService.class);
        LocaleService localeService = mock(LocaleService.class);

        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site-1");
        when(sessionManager.getCurrentSessionUserId()).thenReturn("user1");
        when(videoTrainingService.canManageLibrary("site-1", "user1")).thenReturn(false);
        when(videoTrainingService.hasManagePermission("site-1", "user1")).thenReturn(false);
        when(oauthCredentialsService.isConfigured(VideoProviderType.YOUTUBE_UPLOAD)).thenReturn(true);
        when(uploadService.isHlsUploadEnabled()).thenReturn(true);
        when(uploadService.isValidNativeUpload(anyString(), anyString(), anyBoolean())).thenReturn(true);
        when(uploadService.isManagedUploadProvider(VideoProviderType.YOUTUBE_UPLOAD)).thenReturn(true);
        when(uploadService.getConfiguredMaxNativeUploadBytes()).thenReturn(1024L * 1024L);
        when(uploadService.stageTemporaryManagedUpload(any())).thenReturn("/tmp/video-training/hls-1/upload-1.mp4");

        VideoTrainingController controller = new VideoTrainingController(messageSource, sessionManager, siteService, toolManager, userTimeService, videoTrainingService, oauthCredentialsService, processJobService, serverConfigurationService, securityService, externalMetadataService, uploadService, localeService);

        MockMultipartFile multipart = new MockMultipartFile("nativeFile", "video.mp4", "video/mp4", "dummy".getBytes());

        VideoTrainingVideo saved = new VideoTrainingVideo();
        saved.setId("v1");
        saved.setOwnerId("user1");
        saved.setProviderType(VideoProviderType.YOUTUBE_UPLOAD);

        when(videoTrainingService.saveVideo(any(VideoTrainingVideo.class))).thenReturn(saved);

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        String result = controller.createVideo("title", "desc", VideoProviderType.YOUTUBE_UPLOAD.name(), null, null, null, multipart, null, null, null, null, null, null, null, null, redirectAttributes, java.util.Locale.getDefault());

        Assert.assertEquals("redirect:/videos", result);
        verify(processJobService).queueManagedUploadJob(eq("v1"), eq("user1"), eq("/tmp/video-training/hls-1/upload-1.mp4"), eq(VideoProviderType.YOUTUBE_UPLOAD));
    }
}
