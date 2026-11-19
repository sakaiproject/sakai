/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.sakaiproject.util.SecFetchSiteCsrf;
import org.springframework.web.servlet.HandlerInterceptor;

/** Rejects unsafe cross-site tool requests, optionally returning a JSON error for API clients. */
public class SecFetchSiteCsrfInterceptor implements HandlerInterceptor {

    private static final String JSON_CONTENT_TYPE = "application/json";

    private final boolean jsonResponses;

    public SecFetchSiteCsrfInterceptor() {
        this(false);
    }

    public SecFetchSiteCsrfInterceptor(boolean jsonResponses) {
        this.jsonResponses = jsonResponses;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!SecFetchSiteCsrf.isUntrustedUnsafeRequest(request)) {
            return true;
        }

        if (jsonResponses && wantsJson(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(JSON_CONTENT_TYPE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"success\":false,\"message\":\"The request was blocked.\"}");
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
        return false;
    }

    private boolean wantsJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return (accept != null && accept.contains(JSON_CONTENT_TYPE))
                || (request.getRequestURI() != null && request.getRequestURI().contains("/api/"));
    }
}
