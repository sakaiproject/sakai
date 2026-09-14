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
package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class SiteStatsCsrfInterceptorTest {

    private static final String TOKEN = "csrf-token";

    private SakaiCsrfTokens csrfTokens;
    private SiteStatsCsrfInterceptor interceptor;

    @Before
    public void setUp() {
        csrfTokens = mock(SakaiCsrfTokens.class);
        interceptor = new SiteStatsCsrfInterceptor(csrfTokens);
    }

    @Test
    public void allowsSafeRequestsWithoutAToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reports");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    public void allowsUnsafeRequestsWithTheSessionToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/reports/save");
        request.addParameter(SakaiCsrfTokens.REQUEST_PARAMETER, TOKEN);
        when(csrfTokens.matches(TOKEN)).thenReturn(true);

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    public void rejectsUnsafeRequestsWithoutAToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/reports/save");
        assertThrows(InvalidSakaiCsrfTokenException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    public void rejectsUnsafeRequestsWithTheWrongToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/reports/1");
        request.addParameter(SakaiCsrfTokens.REQUEST_PARAMETER, "wrong-token");

        assertThrows(InvalidSakaiCsrfTokenException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }
}
