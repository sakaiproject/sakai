/*
 * Copyright (c) 2026 The Apereo Foundation
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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.beans.TagTransferBean;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.controllers.ConversationsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ConversationsControllerTests.TestConfiguration.class)
public class ConversationsControllerTests {

    @Configuration
    @Import({WebApiTestConfiguration.class, ConversationsController.class})
    public static class TestConfiguration {

        @Bean
        public ConversationsService conversationsService() {
            return mock(ConversationsService.class);
        }

        @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
        public ServerConfigurationService serverConfigurationService() {
            return mock(ServerConfigurationService.class);
        }

        @Bean
        public SearchService searchService() {
            return mock(SearchService.class);
        }
    }

    @Autowired private ConversationsController controller;
    @Autowired private ConversationsService conversationsService;
    @Autowired private SessionManager sessionManager;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        reset(conversationsService, sessionManager);
        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("user1");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void rejectsNullTagListsAndEntries() throws Exception {
        for (String body : new String[] {"null", "[null]", "[{\"label\":\"Valid\"},null]"}) {
            mockMvc.perform(post("/sites/site1/conversations/tags")
                    .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(conversationsService);
    }

    @Test
    public void createsTagsInTheRequestedSiteAndReturnsTheServiceResult() throws Exception {
        TagTransferBean saved = new TagTransferBean();
        saved.setId("saved-tag");
        saved.setSiteId("site1");
        saved.setLabel("First");
        when(conversationsService.createTags(anyList())).thenReturn(Collections.singletonList(saved));

        mockMvc.perform(post("/sites/site1/conversations/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"siteId\":\"other-site\",\"label\":\"First\"},{\"label\":\"Second\"}]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value("saved-tag"))
            .andExpect(jsonPath("$[0].siteId").value("site1"));

        verify(conversationsService).createTags(argThat(tags -> tags.size() == 2
            && tags.stream().allMatch(tag -> "site1".equals(tag.getSiteId()))
            && "First".equals(tags.get(0).getLabel()) && "Second".equals(tags.get(1).getLabel())));
    }

    @Test
    public void acceptsAnEmptyTagList() throws Exception {
        when(conversationsService.createTags(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/sites/site1/conversations/tags")
                .contentType(MediaType.APPLICATION_JSON).content("[]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        verify(conversationsService).createTags(Collections.emptyList());
    }
}
