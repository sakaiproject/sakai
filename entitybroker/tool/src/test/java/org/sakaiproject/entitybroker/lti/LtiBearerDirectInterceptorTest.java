/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.entitybroker.lti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.sakaiproject.lti.api.SakaiAccessTokenException;
import org.sakaiproject.lti.api.SakaiAccessTokenService;
import org.sakaiproject.tool.api.SessionManager;

public class LtiBearerDirectInterceptorTest {

    @Mock private SakaiAccessTokenService sakaiAccessTokenService;
    @Mock private SessionManager sessionManager;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    private LtiBearerDirectInterceptor interceptor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        interceptor = new LtiBearerDirectInterceptor(sakaiAccessTokenService, sessionManager);
    }

    @Test
    public void preHandlePassesThroughWithoutBearerHeader() {
        when(request.getHeader("Authorization")).thenReturn(null);

        assertTrue(interceptor.preHandle(request, response, null));

        verify(sakaiAccessTokenService, never()).isLtiBearerDirectEnabled();
    }

    @Test
    public void preHandleRejectsBearerWhenDirectDisabled() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer fake.jwt.token");
        when(sakaiAccessTokenService.isLtiBearerDirectEnabled()).thenReturn(false);

        assertFalse(interceptor.preHandle(request, response, null));

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(sakaiAccessTokenService, never()).validateToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    public void preHandleWritesValidJsonWhenTokenRejected() throws Exception {
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(sakaiAccessTokenService.isLtiBearerDirectEnabled()).thenReturn(true);
        when(sakaiAccessTokenService.extractBearerToken("Bearer token")).thenReturn("token");
        when(sakaiAccessTokenService.validateToken("token"))
                .thenThrow(new SakaiAccessTokenException("signature_error", "Bad \"quote\" and \\slash"));

        assertFalse(interceptor.preHandle(request, response, null));

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        JsonNode node = new ObjectMapper().readTree(body.toString());
        assertEquals("signature_error", node.get("error").asText());
        assertEquals("Bad \"quote\" and \\slash", node.get("message").asText());
    }
}
