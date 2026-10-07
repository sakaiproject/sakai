/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/** Same-origin validation for unsafe browser requests to Sakai tools. */
public final class SecFetchSiteCsrf {

    public static final String ORIGIN = "Origin";
    public static final String REFERER = "Referer";
    public static final String SEC_FETCH_SITE = "Sec-Fetch-Site";

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private SecFetchSiteCsrf() {
    }

    public static boolean isUntrustedUnsafeRequest(HttpServletRequest request) {
        String method = request.getMethod();
        if (method == null || SAFE_METHODS.contains(method.toUpperCase(Locale.ROOT))) {
            return false;
        }

        String fetchSite = request.getHeader(SEC_FETCH_SITE);
        if ("same-origin".equalsIgnoreCase(fetchSite)) {
            return false;
        }
        if (fetchSite != null) {
            return true;
        }

        return !isSameOrigin(request.getHeader(ORIGIN), request)
                && !isSameOrigin(request.getHeader(REFERER), request);
    }

    private static boolean isSameOrigin(String header, HttpServletRequest request) {
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
            return scheme.equalsIgnoreCase(request.getScheme()) && host.equalsIgnoreCase(request.getServerName())
                    && normalizedPort(scheme, uri.getPort()) == normalizedPort(request.getScheme(), request.getServerPort());
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
}
