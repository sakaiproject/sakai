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
package org.sakaiproject.webapi.lti;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.sakaiproject.lti.api.SakaiAccessTokenException;
import org.sakaiproject.lti.api.SakaiAccessTokenService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.controllers.test.WebApiTestConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.server.ResponseStatusException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebApiTestConfiguration.class })
public class LtiBearerTokenInterceptorTest {

    @Autowired
    private SakaiAccessTokenService sakaiAccessTokenService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    private LtiBearerTokenInterceptor interceptor;

    @Before
    public void setUp() {

        interceptor = new LtiBearerTokenInterceptor(sakaiAccessTokenService, sessionManager);
        reset(sakaiAccessTokenService, sessionManager, request, response);
    }

    @Test
    public void preHandlePassesThroughWithoutBearerHeader() {

        when(request.getHeader("Authorization")).thenReturn(null);
        when(sakaiAccessTokenService.isWebApiEnabled()).thenReturn(true);

        assertTrue(interceptor.preHandle(request, response, null));
    }

    @Test
    public void preHandleRejectsBearerWhenWebApiDisabled() throws Exception {

        String header = "Bearer fake.jwt.token";
        when(request.getHeader("Authorization")).thenReturn(header);
        when(sakaiAccessTokenService.isWebApiEnabled()).thenReturn(false);

        when(sakaiAccessTokenService.isValidBearerHeader(header)).thenReturn(true);

        Exception e = assertThrows(ResponseStatusException.class, () -> interceptor.preHandle(request, response, null));
        assertTrue(e.getMessage().contains("403"));

        verify(sakaiAccessTokenService, never()).validateToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    public void preHandleThrowsExceptionWhenTokenRejected() throws Exception {

        String header = "Bearer token";
        String token = "token";
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        when(request.getHeader("Authorization")).thenReturn(header);
        when(sakaiAccessTokenService.isValidBearerHeader(header)).thenReturn(true);
        when(sakaiAccessTokenService.isWebApiEnabled()).thenReturn(true);
        when(sakaiAccessTokenService.extractBearerToken(header)).thenReturn(token);
        when(sakaiAccessTokenService.validateToken(token))
                .thenThrow(new SakaiAccessTokenException("signature_error", "Bad \"quote\" and \\slash"));

        Exception e = assertThrows(ResponseStatusException.class, () -> interceptor.preHandle(request, response, null));
        assertTrue(e.getMessage().contains("401"));
    }
}
