/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.webapi.controllers.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;
import org.sakaiproject.videotraining.api.model.VideoTrainingAnalyticsSummary;
import org.sakaiproject.videotraining.api.model.VideoTrainingCategory;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.service.VideoTrainingService;
import org.sakaiproject.webapi.controllers.VideoTrainingController;
import org.sakaiproject.webapi.beans.VideoTrainingRestBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebApiTestConfiguration.class})
public class VideoTrainingControllerTests extends BaseControllerTests {

    private static final String SITE_ID = "site-1";
    private static final String USER_ID = "user-1";
    private static final String VIDEO_ID = "video-1";
    private static final int LIMIT = 24;
    private static final int OFFSET = 0;

    private MockMvc mockMvc;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private SiteService siteService;

    @Mock
    private VideoTrainingService videoTrainingService;

    private AutoCloseable mocks;

    @Before
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);

        VideoTrainingController controller = new VideoTrainingController();
        controller.setSessionManager(sessionManager);
        controller.setSiteService(siteService);

        Site site = mock(Site.class);
        when(siteService.getOptionalSite(SITE_ID)).thenReturn(Optional.of(site));
        when(siteService.allowAccessSite(SITE_ID)).thenReturn(true);

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn(USER_ID);
        when(sessionManager.getCurrentSession()).thenReturn(session);

        when(videoTrainingService.getLessonLinksForVideo(any())).thenReturn(List.of());
        when(videoTrainingService.getVideoCategoryIds(any())).thenReturn(List.of());

        injectVideoTrainingService(controller, videoTrainingService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @After
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testGetSiteVideosIncludesCapabilityFlags() throws Exception {
        VideoTrainingVideo video = createVideo();

        when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(false);
        when(videoTrainingService.canViewAnalytics(SITE_ID, USER_ID)).thenReturn(false);
        when(videoTrainingService.countVisibleVideosForUser(eq(SITE_ID), eq(USER_ID), any(Instant.class), eq(""))).thenReturn(1L);
        when(videoTrainingService.getVisibleVideosForUserPage(eq(SITE_ID), eq(USER_ID), any(Instant.class), eq(""), eq(1), eq(24))).thenReturn(List.of(video));
        when(videoTrainingService.canViewVideo(eq(video), eq(USER_ID), any(Instant.class))).thenReturn(true);

        mockMvc.perform(get("/sites/{siteId}/video-training", SITE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videos[0].id", is(VIDEO_ID)))
                .andExpect(jsonPath("$.videos[0].visibilityScope", is("COURSE")))
                .andExpect(jsonPath("$.videos[0].publicationStatus", is("PUBLISHED")))
                .andExpect(jsonPath("$.videos[0].lessonLinkCount", is(0)))
                .andExpect(jsonPath("$.videos[0].canView", is(true)))
                .andExpect(jsonPath("$.videos[0].canManage", is(false)))
                .andExpect(jsonPath("$.videos[0].canViewAnalytics", is(false)));
    }

    @Test
    public void testGetSiteVideosUsesLibraryWhenCanManage() throws Exception {
        VideoTrainingVideo video = createVideo();

        when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(true);
        when(videoTrainingService.canViewAnalytics(SITE_ID, USER_ID)).thenReturn(true);
        when(videoTrainingService.countSiteLibrary(SITE_ID, "")).thenReturn(1L);
        when(videoTrainingService.getSiteLibraryPage(SITE_ID, "", 1, 24)).thenReturn(List.of(video));
        when(videoTrainingService.canViewVideo(eq(video), eq(USER_ID), any(Instant.class))).thenReturn(false);

        mockMvc.perform(get("/sites/{siteId}/video-training", SITE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videos[0].id", is(VIDEO_ID)))
                .andExpect(jsonPath("$.videos[0].canView", is(true)))
                .andExpect(jsonPath("$.videos[0].canManage", is(true)))
                .andExpect(jsonPath("$.videos[0].canViewAnalytics", is(true)));
    }

    @Test
    public void testGetSiteVideosAlias() throws Exception {
        VideoTrainingVideo video = createVideo();

        when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(true);
        when(videoTrainingService.canViewAnalytics(SITE_ID, USER_ID)).thenReturn(true);
        when(videoTrainingService.countSiteLibrary(SITE_ID, "")).thenReturn(1L);
        when(videoTrainingService.getSiteLibraryPage(SITE_ID, "", 1, 24)).thenReturn(List.of(video));
        when(videoTrainingService.canViewVideo(eq(video), eq(USER_ID), any(Instant.class))).thenReturn(true);

        mockMvc.perform(get("/sites/{siteId}/video-training", SITE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videos[0].id", is(VIDEO_ID)));
    }

    @Test
    public void testGetCourseVideosEndpoint() throws Exception {
        VideoTrainingVideo video = createVideo();

        when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(false);
        when(videoTrainingService.canViewAnalytics(SITE_ID, USER_ID)).thenReturn(false);
        when(videoTrainingService.countVisibleVideosForUser(eq(SITE_ID), eq(USER_ID), any(Instant.class), eq(""))).thenReturn(1L);
        when(videoTrainingService.getVisibleVideosForUserPage(eq(SITE_ID), eq(USER_ID), any(Instant.class), eq(""), eq(1), eq(24))).thenReturn(List.of(video));
        when(videoTrainingService.canViewVideo(eq(video), eq(USER_ID), any(Instant.class))).thenReturn(true);

        mockMvc.perform(get("/courses/{courseId}/videos", SITE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videos[0].id", is(VIDEO_ID)))
                .andExpect(jsonPath("$.query", is("")));
    }

    @Test
    public void testGetVideosCatalogEndpoint() throws Exception {
        VideoTrainingVideo video = createVideo();

        when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(false);
        when(videoTrainingService.canViewAnalytics(SITE_ID, USER_ID)).thenReturn(false);
        when(videoTrainingService.countVisibleVideosForUser(eq(SITE_ID), eq(USER_ID), any(Instant.class), eq(""))).thenReturn(1L);
        when(videoTrainingService.getVisibleVideosForUserPage(eq(SITE_ID), eq(USER_ID), any(Instant.class), eq(""), eq(1), eq(24))).thenReturn(List.of(video));
        when(videoTrainingService.canViewVideo(eq(video), eq(USER_ID), any(Instant.class))).thenReturn(true);

        mockMvc.perform(get("/videos")
                .param("siteId", SITE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videos[0].id", is(VIDEO_ID)))
                .andExpect(jsonPath("$.query", is("")));
    }

    @Test
    public void testGetAnalyticsForbiddenWhenNoPermission() throws Exception {
        when(videoTrainingService.canViewAnalytics(SITE_ID, USER_ID)).thenReturn(false);

        mockMvc.perform(get("/sites/{siteId}/video-training/analytics", SITE_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAnalyticsSuccessWhenAllowed() throws Exception {
        VideoTrainingAnalyticsSummary summary = new VideoTrainingAnalyticsSummary(VIDEO_ID, 5L, 3L);

        when(videoTrainingService.canViewAnalytics(SITE_ID, USER_ID)).thenReturn(true);
        when(videoTrainingService.getSiteAnalyticsSummary(SITE_ID)).thenReturn(List.of(summary));

        mockMvc.perform(get("/sites/{siteId}/video-training/analytics", SITE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analytics[0].videoId", is(VIDEO_ID)))
                .andExpect(jsonPath("$.analytics[0].viewCount", is(5)))
                .andExpect(jsonPath("$.analytics[0].uniqueViewerCount", is(3)));
    }

    @Test
    public void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/video-training/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("video-training")));
    }

    @Test
    public void testGetVideoDetailsForbiddenWhenCannotView() throws Exception {
        VideoTrainingVideo video = createVideo();

        when(videoTrainingService.getVideoById(VIDEO_ID)).thenReturn(Optional.of(video));
        when(videoTrainingService.canViewVideo(eq(video), eq(USER_ID), any(Instant.class))).thenReturn(false);
        when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(false);

        mockMvc.perform(get("/sites/{siteId}/video-training/{videoId}", SITE_ID, VIDEO_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetVideoDetailsNotFound() throws Exception {
        when(videoTrainingService.getVideoById(VIDEO_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/sites/{siteId}/video-training/{videoId}", SITE_ID, VIDEO_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetVideoDetailsBadRequestWhenSiteDoesNotMatch() throws Exception {
        VideoTrainingVideo video = createVideo();
        video.setSiteId("other-site");

        when(videoTrainingService.getVideoById(VIDEO_ID)).thenReturn(Optional.of(video));

        mockMvc.perform(get("/sites/{siteId}/video-training/{videoId}", SITE_ID, VIDEO_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetVideoDetailsEndpoint() throws Exception {
        VideoTrainingVideo video = createVideo();

        when(videoTrainingService.getVideoById(VIDEO_ID)).thenReturn(Optional.of(video));
        when(videoTrainingService.canViewVideo(eq(video), eq(USER_ID), any(Instant.class))).thenReturn(true);
        when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(false);
        when(videoTrainingService.canViewAnalytics(SITE_ID, USER_ID)).thenReturn(false);

        mockMvc.perform(get("/videos/{videoId}", VIDEO_ID)
                    .param("siteId", SITE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(VIDEO_ID)))
                .andExpect(jsonPath("$.visibilityScope", is("COURSE")))
                .andExpect(jsonPath("$.publicationStatus", is("PUBLISHED")));
    }

    @Test
    public void testGetCategoriesEndpoint() throws Exception {
        VideoTrainingCategory category = new VideoTrainingCategory();
        category.setId("cat-1");
        category.setSiteId(SITE_ID);
        category.setName("Module A");

        when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(true);
        when(videoTrainingService.getCategories(SITE_ID, OFFSET, LIMIT)).thenReturn(List.of(category));

        mockMvc.perform(get("/sites/{siteId}/videos/categories", SITE_ID)
                    .param("offset", String.valueOf(OFFSET))
                    .param("limit", String.valueOf(LIMIT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].id", is("cat-1")))
                .andExpect(jsonPath("$.categories[0].name", is("Module A")));
    }

    @Test
    public void testGetCategoryTreeEndpoint() throws Exception {
        VideoTrainingCategory root = new VideoTrainingCategory();
        root.setId("cat-1");
        root.setSiteId(SITE_ID);
        root.setName("Module A");

        VideoTrainingCategory child = new VideoTrainingCategory();
        child.setId("cat-2");
        child.setSiteId(SITE_ID);
        child.setName("Module A.1");
        child.setParentCategoryId("cat-1");

        root.setChildren(List.of(child));
        root.setVideoCount(3L);
        child.setVideoCount(1L);

        when(videoTrainingService.canManageCategories(SITE_ID, USER_ID)).thenReturn(true);
        when(videoTrainingService.countTopLevelCategoriesForSite(SITE_ID, USER_ID, "")).thenReturn(1L);
        when(videoTrainingService.getCategoryTree(SITE_ID, "", 1, 10)).thenReturn(List.of(root));

        mockMvc.perform(get("/sites/{siteId}/videos/categories/tree", SITE_ID))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id", is("cat-1")))
            .andExpect(jsonPath("$.data[0].children[0].id", is("cat-2")))
            .andExpect(jsonPath("$.data[0].videoCount", is(3)));
    }

    @Test
    public void testPromoteLessonResourceEndpoint() throws Exception {
        VideoTrainingVideo video = createVideo();
        video.setVisibilityScope(VideoVisibilityScope.LESSON);

        when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(true);
        when(videoTrainingService.canViewAnalytics(SITE_ID, USER_ID)).thenReturn(false);
        when(videoTrainingService.promoteLessonResource(eq(SITE_ID), eq("page-1"), eq("item-1"), eq("/content/group/site-1/video.mp4"),
                eq("Promoted"), eq("From lessons"), eq(1234L))).thenReturn(video);
        when(videoTrainingService.canViewVideo(eq(video), eq(USER_ID), any(Instant.class))).thenReturn(true);

        String payload = "{" +
                "\"lessonPageId\":\"page-1\"," +
                "\"lessonItemId\":\"item-1\"," +
                "\"resourceReference\":\"/content/group/site-1/video.mp4\"," +
                "\"title\":\"Promoted\"," +
                "\"description\":\"From lessons\"," +
                "\"fileSizeBytes\":1234" +
                "}";

        mockMvc.perform(post("/lessons/{siteId}/promote-resource", SITE_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(VIDEO_ID)))
                .andExpect(jsonPath("$.visibilityScope", is("LESSON")));
    }

    @Test
    public void testUpdateMetadataForbiddenWhenCannotManage() throws Exception {
        VideoTrainingVideo video = createVideo();

        when(videoTrainingService.getVideoById(VIDEO_ID)).thenReturn(Optional.of(video));
        when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(false);

        mockMvc.perform(post("/sites/{siteId}/video-training/{videoId}/metadata", SITE_ID, VIDEO_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateMetadataSuccessWhenCanManage() throws Exception {
        VideoTrainingVideo video = createVideo();
        when(videoTrainingService.getVideoById(VIDEO_ID)).thenReturn(Optional.of(video));
        when(videoTrainingService.canManageLibrary(SITE_ID, USER_ID)).thenReturn(true);
        when(videoTrainingService.canViewAnalytics(SITE_ID, USER_ID)).thenReturn(true);
        when(videoTrainingService.canViewVideo(eq(video), eq(USER_ID), any(Instant.class))).thenReturn(true);
        when(videoTrainingService.saveVideo(any(VideoTrainingVideo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String payload = "{" +
                "\"title\":\"Updated title\"," +
                "\"description\":\"Updated description\"," +
                "\"visibilityScope\":\"LESSON\"," +
                "\"publicationStatus\":\"WITHDRAWN\"" +
                "}";

        mockMvc.perform(post("/sites/{siteId}/video-training/{videoId}/metadata", SITE_ID, VIDEO_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(VIDEO_ID)))
                .andExpect(jsonPath("$.title", is("Updated title")))
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.visibilityScope", is("LESSON")))
                .andExpect(jsonPath("$.publicationStatus", is("WITHDRAWN")))
                .andExpect(jsonPath("$.canManage", is(true)));
    }

    @Test
    public void testUpdateMetadataClearsCategoriesWhenEmptyArrayProvided() throws Exception {
        VideoTrainingVideo video = createVideo();
        when(videoTrainingService.getVideoById(VIDEO_ID)).thenReturn(Optional.of(video));
        when(videoTrainingService.canManageVideo(VIDEO_ID, USER_ID)).thenReturn(true);
        when(videoTrainingService.saveVideo(any(VideoTrainingVideo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(videoTrainingService.getVideoById(VIDEO_ID)).thenReturn(Optional.of(video));

        VideoTrainingController controller = new VideoTrainingController();
        controller.setSessionManager(sessionManager);
        controller.setSiteService(siteService);
        injectVideoTrainingService(controller, videoTrainingService);

        VideoTrainingController.VideoTrainingUpdateRequest request = new VideoTrainingController.VideoTrainingUpdateRequest();
        request.setCategoryIds(List.of());

        VideoTrainingRestBean response = controller.updateVideoMetadata(VIDEO_ID, request);
        assertEquals(VIDEO_ID, response.getId());

        verify(videoTrainingService).setVideoCategoryIds(VIDEO_ID, List.of());
    }

    private VideoTrainingVideo createVideo() {
        VideoTrainingVideo video = new VideoTrainingVideo();
        video.setId(VIDEO_ID);
        video.setSiteId(SITE_ID);
        video.setTitle("Intro");
        video.setDescription("Description");
        video.setProviderType(VideoProviderType.NATIVE);
        video.setVisibilityScope(VideoVisibilityScope.COURSE);
        video.setPublicationStatus(VideoPublicationStatus.PUBLISHED);
        video.setSourceReference("/content/group/site-1/video.mp4");
        return video;
    }

    private void injectVideoTrainingService(VideoTrainingController controller, VideoTrainingService service) {
        try {
            java.lang.reflect.Field field = VideoTrainingController.class.getDeclaredField("videoTrainingService");
            field.setAccessible(true);
            field.set(controller, service);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot inject videoTrainingService for tests", e);
        }
    }
}
