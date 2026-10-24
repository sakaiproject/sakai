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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagSummary;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.webapi.controllers.TagsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebApiTestConfiguration.class, TagsController.class })
public class TagsControllerTests extends BaseControllerTests {

    @Autowired private TagsController controller;
    @Autowired private TagService tagService;
    @Autowired private SecurityService securityService;

    @Before
    public void setup() {

        reset(tagService, sessionManager, securityService);
        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("user1");
        when(sessionManager.getCurrentSession()).thenReturn(session);

        buildMockMvc(controller);
    }

    @Test
    public void testMissingSession() throws Exception {
        testMissingSession("/sites/site-xyz/tools/tool-xyz/tags/collection-xyz");
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
    @Test
    public void selectorUsesTheSharedSiteCatalogForEverySiteTool() throws Exception {
        when(securityService.unlock(TagService.TAGSERVICE_MANAGE_PERMISSION, "/site/site1")).thenReturn(true);
        when(tagService.getTagsForSite("site1")).thenReturn(Collections.singletonList(
            new TagSummary("global", "admin-collection", "Competency", "Objectives", null)));
        mockMvc.perform(get("/sites/site1/tools/assignments/tags/site1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].tagId").value("global"));
        verify(tagService).getTagsForSite("site1");
    }

    @Test
    public void conversationTagPermissionAllowsReadingTheSharedCatalog() throws Exception {
        when(securityService.unlock("conversations.topic.tag", "/site/site1")).thenReturn(true);
        mockMvc.perform(get("/sites/site1/tools/conversations/tags/site1")).andExpect(status().isOk());
        verify(tagService).getTagsForSite("site1");
    }

    @Test
    public void sitePermissionsDoNotExposeAnotherUsersPrivatePoolCatalog() throws Exception {
        when(securityService.unlock(TagService.TAGSERVICE_MANAGE_PERMISSION, "/site/site1")).thenReturn(true);
        mockMvc.perform(get("/sites/site1/tools/samigo/tags/another-user")).andExpect(status().isForbidden());
    }

}
