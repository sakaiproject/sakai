/**
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
package org.sakaiproject.site.tool.helper.order;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class SecFetchSiteCsrfInterceptorTest {

    private final SecFetchSiteCsrfInterceptor interceptor = new SecFetchSiteCsrfInterceptor();

    @Test
    public void postWithCrossSiteFetchMetadataIsBlocked() throws Exception {
        HttpServletRequest request = request("POST", "cross-site", "/api/order", "application/json");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        assertFalse(interceptor.preHandle(request, response, new Object()));

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        assertTrue(body.toString().contains("\"success\":false"));
    }

    @Test
    public void postWithSameSiteFetchMetadataIsBlocked() throws Exception {
        HttpServletRequest request = request("POST", "same-site", "/api/order", "application/json");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        assertFalse(interceptor.preHandle(request, response, new Object()));

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        assertTrue(body.toString().contains("\"success\":false"));
    }

    @Test
    public void postWithNoneFetchMetadataIsBlocked() throws Exception {
        HttpServletRequest request = request("POST", "none", "/api/order", "application/json");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        assertFalse(interceptor.preHandle(request, response, new Object()));

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        assertTrue(body.toString().contains("\"success\":false"));
    }

    @Test
    public void nonApiPostWithCrossSiteFetchMetadataIsBlocked() throws Exception {
        HttpServletRequest request = request("POST", "cross-site", "/done", null);
        HttpServletResponse response = mock(HttpServletResponse.class);

        assertFalse(interceptor.preHandle(request, response, new Object()));

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void postWithSameOriginFetchMetadataIsAllowed() throws Exception {
        HttpServletRequest request = request("POST", "same-origin", "/api/order", "application/json");
        HttpServletResponse response = mock(HttpServletResponse.class);

        assertTrue(interceptor.preHandle(request, response, new Object()));

        verify(response, never()).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void postWithoutFetchMetadataAndFallbackIsBlocked() throws Exception {
        HttpServletRequest request = request("POST", null, "/api/order", "application/json");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        assertFalse(interceptor.preHandle(request, response, new Object()));

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        assertTrue(body.toString().contains("\"success\":false"));
    }

    @Test
    public void postWithoutFetchMetadataWithSameOriginHeaderIsAllowed() throws Exception {
        HttpServletRequest request = request("POST", null, "/api/order", "application/json");
        when(request.getHeader(SecFetchSiteCsrfInterceptor.ORIGIN)).thenReturn("https://sakai.example.edu");
        HttpServletResponse response = mock(HttpServletResponse.class);

        assertTrue(interceptor.preHandle(request, response, new Object()));

        verify(response, never()).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void postWithoutFetchMetadataWithSameOriginRefererIsAllowed() throws Exception {
        HttpServletRequest request = request("POST", null, "/api/order", "application/json");
        when(request.getHeader(SecFetchSiteCsrfInterceptor.REFERER))
                .thenReturn("https://sakai.example.edu/portal/site/site1");
        HttpServletResponse response = mock(HttpServletResponse.class);

        assertTrue(interceptor.preHandle(request, response, new Object()));

        verify(response, never()).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void postWithoutFetchMetadataWithCrossOriginHeaderIsBlocked() throws Exception {
        HttpServletRequest request = request("POST", null, "/api/order", "application/json");
        when(request.getHeader(SecFetchSiteCsrfInterceptor.ORIGIN)).thenReturn("https://evil.example.net");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        assertFalse(interceptor.preHandle(request, response, new Object()));

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        assertTrue(body.toString().contains("\"success\":false"));
    }

    @Test
    public void getWithCrossSiteFetchMetadataIsAllowed() throws Exception {
        HttpServletRequest request = request("GET", "cross-site", "/index", null);
        HttpServletResponse response = mock(HttpServletResponse.class);

        assertTrue(interceptor.preHandle(request, response, new Object()));

        verify(response, never()).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    private HttpServletRequest request(String method, String fetchSite, String requestUri, String accept) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getHeader(SecFetchSiteCsrfInterceptor.SEC_FETCH_SITE)).thenReturn(fetchSite);
        when(request.getHeader("Accept")).thenReturn(accept);
        when(request.getRequestURI()).thenReturn(requestUri);
        when(request.getScheme()).thenReturn("https");
        when(request.getServerName()).thenReturn("sakai.example.edu");
        when(request.getServerPort()).thenReturn(443);
        return request;
    }
}
