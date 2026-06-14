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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.UserNotificationTransferBean;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.controllers.NotificationsController;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebApiTestConfiguration.class })
public class NotificationsControllerTests extends BaseControllerTests {

    private MockMvc mockMvc;

    @Autowired
    private UserMessagingService userMessagingService;

    @Autowired
    private PortalService portalService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private SiteService siteService;

    private String user1Id = "user1";

    @Before
    public void setup() {

        reset(userMessagingService, portalService, sessionManager, siteService);

        NotificationsController controller = new NotificationsController();

        controller.setUserMessagingService(userMessagingService);
        controller.setSiteService(siteService);
        controller.setPortalService(portalService);

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn(user1Id);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        controller.setSessionManager(sessionManager);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).apply(configurer).build();
    }

    @Test
    public void testGetUsersNotifications() throws Exception {

        // Mock up some notifications

        String event1 = "annc.new";
        String ref1 = "/announcement/1";
        UserNotificationTransferBean noti1 = new UserNotificationTransferBean();
        noti1.id = 1L;
        noti1.from = "sender-id";
        noti1.to = user1Id;
        noti1.event = event1;
        noti1.ref = ref1;
        noti1.siteId = "site1";
        noti1.title = "Title 1";
        noti1.tool = "sakai.announcements";
        noti1.fromDisplayName = "Sender Uno";
        noti1.siteTitle = "Math 101";
        noti1.eventDate = Instant.now();
        noti1.formattedEventDate = "12 Dec 2024";

        String event2 = "assn.new";
        String ref2 = "/assignment/1";
        UserNotificationTransferBean noti2 = new UserNotificationTransferBean();
        noti2.id = 2L;
        noti2.from = "sender-id";
        noti2.to = user1Id;
        noti2.event = event2;
        noti2.ref = ref2;
        noti2.siteId = "site1";
        noti2.title = "Title 2";
        noti2.tool = "sakai.assignments";
        noti2.fromDisplayName = "Sender Uno";
        noti2.siteTitle = "Math 101";
        noti2.eventDate = Instant.now();
        noti2.formattedEventDate = "18 Jan 2026";

        when(userMessagingService.getNotifications()).thenReturn(List.of(noti1, noti2));

        mockMvc.perform(get("/users/me/notifications"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$.[*].id", hasItems(noti1.id.intValue(), noti2.id.intValue())))
            .andExpect(jsonPath("$.[0].from", is(noti1.from)))
            .andExpect(jsonPath("$.[0].to", is(noti1.to)))
            .andExpect(jsonPath("$.[0].event", is(noti1.event)))
            .andExpect(jsonPath("$.[0].ref", is(noti1.ref)))
            .andExpect(jsonPath("$.[0].siteId", is(noti1.siteId)))
            .andExpect(jsonPath("$.[0].title", is(noti1.title)))
            .andExpect(jsonPath("$.[0].tool", is(noti1.tool)))
            .andExpect(jsonPath("$.[0].fromDisplayName", is(noti1.fromDisplayName)))
            .andExpect(jsonPath("$.[0].siteTitle", is(noti1.siteTitle)))
            .andExpect(jsonPath("$.[0].formattedEventDate", is(noti1.formattedEventDate)))
            .andExpect(jsonPath("$.[1].from", is(noti2.from)))
            .andExpect(jsonPath("$.[1].to", is(noti2.to)))
            .andExpect(jsonPath("$.[1].event", is(noti2.event)))
            .andExpect(jsonPath("$.[1].ref", is(noti2.ref)))
            .andExpect(jsonPath("$.[1].siteId", is(noti2.siteId)))
            .andExpect(jsonPath("$.[1].title", is(noti2.title)))
            .andExpect(jsonPath("$.[1].tool", is(noti2.tool)))
            .andExpect(jsonPath("$.[1].fromDisplayName", is(noti2.fromDisplayName)))
            .andExpect(jsonPath("$.[1].siteTitle", is(noti2.siteTitle)))
            .andExpect(jsonPath("$.[1].formattedEventDate", is(noti2.formattedEventDate)))
            .andDo(document("get-user-notifications"));
    }

    @Test
    public void testClearNotification() throws Exception {

        long id = 1L;
        mockMvc.perform(post("/users/me/notifications/" + id + "/clear"))
            .andExpect(status().isOk())
            .andDo(document("clear-user-notification"));

        verify(userMessagingService).clearNotification(id);
    }

    @Test
    public void testClearAllNotifications() throws Exception {

        mockMvc.perform(post("/users/me/notifications/clear"))
            .andExpect(status().isOk())
            .andDo(document("clear-user-notifications"));

        verify(userMessagingService).clearAllNotifications();
    }

    @Test
    public void testMarkAllNotificationsViewed() throws Exception {

        String siteId = "site1";
        String toolId = "tool1";
        mockMvc.perform(post("/users/me/notifications/markViewed").param("siteId", siteId).param("toolId", toolId))
            .andExpect(status().isOk())
            .andDo(document("mark-user-notifications-viewed"));

        verify(userMessagingService).markAllNotificationsViewed(siteId, toolId);
    }

    @Test
    public void testSendTestNotification() throws Exception {

        mockMvc.perform(post("/users/me/notifications/test"))
            .andExpect(status().isOk())
            .andDo(document("send-test-notification"));

        verify(userMessagingService).sendTestNotification();
    }
}
