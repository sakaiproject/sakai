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
package org.sakaiproject.webapi.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.webapi.controllers.LoginController;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebApiTestConfiguration.class })
public class LoginControllerTests extends BaseControllerTests {

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    private AuthenticationManager authenticationManager;
    private UsageSessionService usageSessionService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Before
	public void setup() {

        reset(sessionManager);

        LoginController controller = new LoginController();

        controller.setUserDirectoryService(userDirectoryService);

        controller.setSessionManager(sessionManager);

        authenticationManager = mock(AuthenticationManager.class);
        controller.setAuthenticationManager(authenticationManager);

        usageSessionService = mock(UsageSessionService.class);
        controller.setUsageSessionService(usageSessionService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .apply(documentationConfiguration(this.restDocumentation))
            .build();
	}

    @Test
    public void testSuccessfulLogin() throws Exception {

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        Session session = mock(Session.class);
        when(session.getId()).thenReturn("session1");
        when(sessionManager.startSession()).thenReturn(session);

        String username = "user1";
        String password = "password1";

        MvcResult result = mockMvc.perform(post("/login")
                .param("username", username)
                .param("password", password))
            .andExpect(status().isOk())
            .andDo(document("login", preprocessor))
            .andReturn();

        String sessionId = result.getResponse().getContentAsString();
        assertTrue(result.getResponse().getCookies().length == 1);
        assertEquals(sessionId, session.getId());
        verify(sessionManager).setCurrentSession(any());
        verify(usageSessionService).login(any(), any(), any(), any(), any());
    }

    @Test
    public void testFailedSessionCreation() throws Exception {

        when(sessionManager.startSession()).thenReturn(null);

        String username = "user1";
        String password = "password1";

        mockMvc.perform(post("/login").param("username", username).param("password", password))
            .andExpect(status().isInternalServerError());
    }
}
