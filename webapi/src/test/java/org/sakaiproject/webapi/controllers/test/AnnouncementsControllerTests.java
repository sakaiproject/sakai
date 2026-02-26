/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.webapi.controllers.AnnouncementsController;

import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.github.javafaker.Faker;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebApiTestConfiguration.class })
public class AnnouncementsControllerTests extends BaseControllerTests {

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private ContentHostingService contentHostingService;

    @Mock
    private AnnouncementService announcementService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private PortalService portalService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private SiteService siteService;

    @Mock
    private UserDirectoryService userDirectoryService;

    private AutoCloseable mocks;

    private Faker faker;

    @Before
	public void setup() {

        mocks = MockitoAnnotations.openMocks(this);

        faker = new Faker(new Random(24));

        reset(announcementService);

        AnnouncementsController controller = new AnnouncementsController();

        var session = mock(Session.class);
        when(session.getUserId()).thenReturn("user3");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        controller.setSessionManager(sessionManager);

        controller.setAnnouncementService(announcementService);
        controller.setPortalService(portalService);
        controller.setSiteService(siteService);
        controller.setEntityManager(entityManager);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .apply(documentationConfiguration(this.restDocumentation))
            .build();
	}

    @After
    public void tearDown() throws Exception {

        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testGetUsersAnnouncements() throws Exception {

        String site1Id = UUID.randomUUID().toString();
        String site1Title = "Site 1";
        String site2Id = UUID.randomUUID().toString();
        String site2Title = "Site 2";

        Site site1 = mock(Site.class);
        when(site1.getId()).thenReturn(site1Id);
        when(site1.getTitle()).thenReturn(site1Title);
        when(siteService.getSite(site1Id)).thenReturn(site1);
        String site1ChannelRef = "/main/" + site1Id;
        when(announcementService.channelReference(site1Id, SiteService.MAIN_CONTAINER)).thenReturn(site1ChannelRef);

        var site2 = mock(Site.class);
        when(site2.getId()).thenReturn(site2Id);
        when(site2.getTitle()).thenReturn(site2Title);
        when(siteService.getSite(site2Id)).thenReturn(site2);
        var site2ChannelRef = "/main/" + site2Id;
        when(announcementService.channelReference(site2Id, SiteService.MAIN_CONTAINER)).thenReturn(site2ChannelRef);

        when(portalService.getPinnedSites()).thenReturn(List.of(site1Id, site2Id));

        String subject1 = faker.lorem().sentence();
        String author1 = faker.name().fullName();
        var releaseDate1 = Instant.now().minus(7, ChronoUnit.DAYS);
        var url1 = "url1";
        var ref1 = "/ref/" + UUID.randomUUID().toString();
        AnnouncementMessage am1 = createAnnouncementMessage(site1Id, subject1, author1, releaseDate1, ref1, url1);

        when(announcementService.getMessages(site1ChannelRef, null, false, false)).thenReturn(List.of(am1));

        String subject2 = faker.lorem().sentence();
        String author2 = faker.name().fullName();
        var releaseDate2 = Instant.now().minus(14, ChronoUnit.DAYS);
        var url2 = "url2";
        var ref2 = "/ref/" + UUID.randomUUID().toString();
        var am2 = createAnnouncementMessage(site2Id, subject2, author2, releaseDate2, ref2, url2);

        when(announcementService.getMessages(site2ChannelRef, null, false, false)).thenReturn(List.of(am2));

        mockMvc.perform(get("/users/me/announcements"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.announcements[0].id", is(am1.getId())))
            .andExpect(jsonPath("$.announcements[0].siteId", is(site1Id)))
            .andExpect(jsonPath("$.announcements[0].siteTitle", is(site1Title)))
            .andExpect(jsonPath("$.announcements[0].subject", is(subject1)))
            .andExpect(jsonPath("$.announcements[0].author", is(author1)))
            .andExpect(jsonPath("$.announcements[0].url", is(url1)))
            .andExpect(jsonPath("$.announcements[0].date", is(releaseDate1.toEpochMilli())))
            .andExpect(jsonPath("$.announcements[1].id", is(am2.getId())))
            .andExpect(jsonPath("$.announcements[1].siteId", is(site2Id)))
            .andExpect(jsonPath("$.announcements[1].siteTitle", is(site2Title)))
            .andExpect(jsonPath("$.announcements[1].subject", is(subject2)))
            .andExpect(jsonPath("$.announcements[1].author", is(author2)))
            .andExpect(jsonPath("$.announcements[1].url", is(url2)))
            .andExpect(jsonPath("$.announcements[1].date", is(releaseDate2.toEpochMilli())))
            .andDo(document("get-user-announcements", preprocessor));
    }

    @Test
    public void testGetSiteAnnouncements() throws Exception {

        var siteId = UUID.randomUUID().toString();
        var siteTitle = "My Site";
        var channelRef = "/main/" + siteId;

        var site = mock(Site.class);
        when(site.getId()).thenReturn(siteId);
        when(site.getTitle()).thenReturn(siteTitle);
        Properties props = new Properties();
        var tc = mock(ToolConfiguration.class);
        when(tc.getPlacementConfig()).thenReturn(props);
        when(site.getToolForCommonId(AnnouncementService.SAKAI_ANNOUNCEMENT_TOOL_ID)).thenReturn(tc);

        when(siteService.getSite(siteId)).thenReturn(site);

        when(announcementService.channelReference(siteId, "main")).thenReturn(channelRef);

        String author = faker.name().fullName();
        String subject = faker.lorem().sentence();
        var releaseDate = Instant.now().minus(3, ChronoUnit.DAYS);
        var url = "http://example.com/something";
        var ref = "/ref/" + UUID.randomUUID().toString();

        when(announcementService.isMessageViewable(any())).thenReturn(true);

        AnnouncementMessage am1 = createAnnouncementMessage(siteId, subject, author, releaseDate, ref, url);

        when(announcementService.getChannelMessages(channelRef, null, false, null, false, false, siteId, 10)).thenReturn(List.of(am1));

        mockMvc.perform(get("/sites/" + siteId + "/announcements"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.announcements[0].id", is(am1.getId())))
            .andExpect(jsonPath("$.announcements[0].siteId", is(siteId)))
            .andExpect(jsonPath("$.announcements[0].siteTitle", is(siteTitle)))
            .andExpect(jsonPath("$.announcements[0].subject", is(subject)))
            .andExpect(jsonPath("$.announcements[0].author", is(author)))
            .andExpect(jsonPath("$.announcements[0].url", is(url)))
            .andExpect(jsonPath("$.announcements[0].date", is(releaseDate.toEpochMilli())))
            .andDo(document("get-site-announcements", preprocessor));
    }

    private AnnouncementMessage createAnnouncementMessage(String siteId, String subject, String author, Instant releaseDate, String ref, String url) throws Exception {

        var header = mock(AnnouncementMessageHeader.class);
        when(header.getSubject()).thenReturn(subject);

        var from = mock(User.class);
        when(from.getDisplayName()).thenReturn(author);
        when(header.getFrom()).thenReturn(from);

        when(header.getInstant()).thenReturn(releaseDate);

        when(entityManager.getUrl(ref, Entity.UrlType.PORTAL)).thenReturn(Optional.of(url));

        var announcement = mock(AnnouncementMessage.class);
        when(announcement.getReference()).thenReturn(ref);
        when(announcement.getId()).thenReturn(UUID.randomUUID().toString());
        when(announcement.getAnnouncementHeader()).thenReturn(header);
        when(announcement.getReference()).thenReturn(ref);
        var props = mock(ResourceProperties.class);
        when(props.getInstantProperty(AnnouncementService.RELEASE_DATE)).thenReturn(releaseDate);
        when(announcement.getProperties()).thenReturn(props);

        return announcement;
    }
}
