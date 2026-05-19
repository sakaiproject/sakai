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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecFetchSiteCsrfInterceptor implements HandlerInterceptor {

    static final String ORIGIN = "Origin";
    static final String REFERER = "Referer";
    static final String SEC_FETCH_SITE = "Sec-Fetch-Site";

    private static final String SAME_ORIGIN = "same-origin";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final Set<String> SAFE_METHODS = new HashSet<>(
            Arrays.asList("GET", "HEAD", "OPTIONS", "TRACE"));

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        if (!isUntrustedUnsafeRequest(request)) {
            return true;
        }

        log.warn("Blocked non-same-origin Tool Order request: {} {}", request.getMethod(), request.getRequestURI());
        rejectRequest(request, response);
        return false;
    }

    static boolean isUntrustedUnsafeRequest(HttpServletRequest request) {
        String method = request.getMethod();
        if (method == null || SAFE_METHODS.contains(method.toUpperCase(Locale.ROOT))) {
            return false;
        }

        String fetchSite = request.getHeader(SEC_FETCH_SITE);
        if (SAME_ORIGIN.equalsIgnoreCase(fetchSite)) {
            return false;
        }

        if (fetchSite != null) {
            return true;
        }

        return !hasSameOriginFallback(request);
    }

    private static boolean hasSameOriginFallback(HttpServletRequest request) {
        return isSameOriginHeader(request.getHeader(ORIGIN), request)
                || isSameOriginHeader(request.getHeader(REFERER), request);
    }

    private static boolean isSameOriginHeader(String header, HttpServletRequest request) {
        if (header == null || header.trim().isEmpty()) {
            return false;
        }

        try {
            URI uri = new URI(header.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null || request.getScheme() == null || request.getServerName() == null) {
                return false;
            }

            return scheme.equalsIgnoreCase(request.getScheme())
                    && host.equalsIgnoreCase(request.getServerName())
                    && normalizedPort(scheme, uri.getPort())
                            == normalizedPort(request.getScheme(), request.getServerPort());
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static int normalizedPort(String scheme, int port) {
        if (port > 0) {
            return port;
        }

        if ("http".equalsIgnoreCase(scheme)) {
            return 80;
        }

        if ("https".equalsIgnoreCase(scheme)) {
            return 443;
        }

        return port;
    }

    private void rejectRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!wantsJson(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(JSON_CONTENT_TYPE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"success\":false,\"message\":\"The request was blocked.\"}");
    }

    private boolean wantsJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return (accept != null && accept.contains(JSON_CONTENT_TYPE))
                || (request.getRequestURI() != null && request.getRequestURI().contains("/api/"));
    }
}
