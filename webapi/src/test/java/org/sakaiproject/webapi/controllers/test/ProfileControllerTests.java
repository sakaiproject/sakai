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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.profile2.api.ProfileService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.controllers.ProfileController;
import org.sakaiproject.webapi.exception.GlobalExceptionHandler;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ProfileControllerTests {

    @Mock
    private ProfileService profileService;

    @Mock
    private SessionManager sessionManager;

    private MockMvc mockMvc;
    private AutoCloseable mocks;

    @Before
    public void setup() {

        mocks = MockitoAnnotations.openMocks(this);

        ProfileController profileController = new ProfileController();
        profileController.setSessionManager(sessionManager);
        ReflectionTestUtils.setField(profileController, "profileService", profileService);

        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @After
    public void tearDown() throws Exception {

        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void removeProfileImageRequiresSession() throws Exception {

        Session session = mock(Session.class);
        when(sessionManager.getCurrentSession()).thenReturn(session);

        mockMvc.perform(delete("/users/user1/profile/image"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Missing or invalid Sakai session"));
    }

    @Test
    public void removeProfileImageMapsServiceSecurityExceptionToForbidden() throws Exception {

        setupCurrentUser("user1");
        doThrow(new SecurityException("Not allowed to remove a user's profile image."))
            .when(profileService).removeProfileImage("user2");

        mockMvc.perform(delete("/users/user2/profile/image"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.detail").value("Not allowed to remove a user's profile image."));
    }

    @Test
    public void removeProfileImageAllowsOwner() throws Exception {

        setupCurrentUser("user1");

        mockMvc.perform(delete("/users/user1/profile/image"))
            .andExpect(status().isOk());

        verify(profileService).removeProfileImage("user1");
    }

    @Test
    public void removeProfileImageAllowsSuperUser() throws Exception {

        setupCurrentUser("admin");
        when(profileService.removeProfileImage("user2")).thenReturn(true);

        mockMvc.perform(delete("/users/user2/profile/image"))
            .andExpect(status().isOk());

        verify(profileService).removeProfileImage("user2");
    }

    private void setupCurrentUser(String userId) {

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn(userId);
        when(sessionManager.getCurrentSession()).thenReturn(session);
    }
}
