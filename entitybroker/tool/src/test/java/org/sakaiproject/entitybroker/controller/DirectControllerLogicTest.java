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
package org.sakaiproject.entitybroker.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
import org.sakaiproject.util.BasicAuth;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for DirectController using MockMvc (no Spring context required).
 */
public class DirectControllerLogicTest {

    private EntityRequestHandler mockHandler;
    private BasicAuth mockBasicAuth;
    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockHandler = mock(EntityRequestHandler.class);
        mockBasicAuth = mock(BasicAuth.class);
        when(mockBasicAuth.doAuth(any(), any())).thenReturn(false);
    }

    private MockMvc buildMvc(String userId) {
        return MockMvcBuilders.standaloneSetup(new TestableDirectController(mockHandler, mockBasicAuth, userId)).build();
    }

    @Test
    public void testGetEntityDelegatesToHandler() throws Exception {
        mockMvc = buildMvc("user1");

        mockMvc.perform(get("/testprefix/testid")).andExpect(status().isOk());

        verify(mockHandler).handleEntityAccess(any(HttpServletRequest.class), any(HttpServletResponse.class), eq("/testprefix/testid"));
    }

    @Test
    public void testPostEntityDelegatesToHandler() throws Exception {
        mockMvc = buildMvc("user1");

        mockMvc.perform(post("/testprefix")).andExpect(status().isOk());

        verify(mockHandler).handleEntityAccess(any(HttpServletRequest.class), any(HttpServletResponse.class), eq("/testprefix"));
    }

    @Test
    public void testEntityExceptionMappedToHttpError() throws Exception {
        mockMvc = buildMvc("user1");

        doThrow(new EntityException("not found", "/testprefix/x", HttpServletResponse.SC_NOT_FOUND)).when(mockHandler).handleEntityAccess(any(), any(), any());

        mockMvc.perform(get("/testprefix/x")).andExpect(status().isNotFound());
    }

    @Test
    public void testSecurityExceptionWhenLoggedInReturns403() throws Exception {
        mockMvc = buildMvc("user1");

        doThrow(new SecurityException("access denied")).when(mockHandler).handleEntityAccess(any(), any(), any());

        mockMvc.perform(get("/testprefix/x")).andExpect(status().isForbidden());
    }

    @Test
    public void testInternalErrorOnUnexpectedException() throws Exception {
        mockMvc = buildMvc("user1");

        doThrow(new RuntimeException("unexpected")).when(mockHandler).handleEntityAccess(any(), any(), any());
        when(mockHandler.handleEntityError(any(), any())).thenReturn("error message");

        mockMvc.perform(get("/testprefix/x")).andExpect(status().isInternalServerError());
    }

    /** Subclass that overrides currentUserId() to avoid Sakai static cover calls in tests. */
    private static class TestableDirectController extends DirectController {
        private final String userId;

        TestableDirectController(EntityRequestHandler handler, BasicAuth basicAuth, String userId) {
            super(handler, basicAuth);
            this.userId = userId;
        }

        @Override
        protected String currentUserId() {
            return userId;
        }
    }
}
