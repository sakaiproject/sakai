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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.controllers.TagsController;
import org.sakaiproject.webapi.exception.GlobalExceptionHandler;
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
@ContextConfiguration(classes = TagsControllerTests.TestConfiguration.class)
public class TagsControllerTests {
    @Configuration
    @Import({WebApiTestConfiguration.class, TagsController.class})
    public static class TestConfiguration {
        @Bean
        public TagService tagService() { return mock(TagService.class); }
    }

    @Autowired private TagsController controller;
    @Autowired private TagService tagService;
    @Autowired private SessionManager sessionManager;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        reset(tagService, sessionManager);
        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("user1");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    public void createsTagsThroughTheSharedService() throws Exception {
        Tag saved = Tag.builder().tagId("saved-tag").tagCollectionId("site1").tagLabel("First").build();
        when(tagService.createSiteTags(eq("site1"), eq("conversations"), anyList()))
            .thenReturn(Collections.singletonList(saved));
        mockMvc.perform(post("/sites/site1/tools/conversations/tags")
                .contentType(MediaType.APPLICATION_JSON).content("[{\"tagLabel\":\"First\"}]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].tagId").value("saved-tag"))
            .andExpect(jsonPath("$[0].tagCollectionId").value("site1"));
        verify(tagService).createSiteTags(eq("site1"), eq("conversations"), anyList());
    }

    @Test
    public void reportsSharedServicePermissionFailures() throws Exception {
        when(tagService.createSiteTags(eq("site1"), eq("conversations"), anyList()))
            .thenThrow(new SecurityException("Not allowed"));
        mockMvc.perform(post("/sites/site1/tools/conversations/tags")
                .contentType(MediaType.APPLICATION_JSON).content("[]"))
            .andExpect(status().isForbidden());
    }
}
