/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2025 Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Utilities for determining whether iframe URLs are local to Sakai (same-origin).
 * Used by the iframe tool, LTI, Web Content, and portal rendering to decide
 * whether dark mode styling can safely flow into embedded content.
 *
 * @see <a href="https://jira.sakaiproject.org/browse/SAK-52376">SAK-52376</a>
 */
public class IframeUrlUtil {

    /**
     * Determines if the given source URL is local to Sakai (same-origin with the
     * Sakai server). Relative URLs are treated as local. Absolute URLs must
     * match scheme, host, and port of the Sakai base URL.
     *
     * @param source       the iframe src URL
     * @param sakaiBaseUrl  the Sakai server base URL (e.g. from ServerConfigurationService.getServerUrl())
     * @return true if the URL is local/same-origin and dark mode can safely flow through
     */
    public static boolean isLocalToSakai(String source, String sakaiBaseUrl) {
        if (source == null) return false;
        String s = source.trim();
        if (s.isEmpty()) return false;

        // Disallow dangerous/odd schemes early
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.startsWith("javascript:") ||
            lower.startsWith("data:") ||
            lower.startsWith("file:") ||
            lower.startsWith("mailto:") ||
            lower.startsWith("about:")) {
            return false;
        }

        // Protocol-relative URLs ("//example.com/...") are NOT local
        if (s.startsWith("//")) return false;

        // Relative URLs are local ("/...", "./...", "../...", or plain "path")
        // NOTE: plain "path" (no scheme, no leading slash) is still relative.
        if (!hasScheme(s)) return true;

        // Absolute URL: must be same-origin with sakaiBaseUrl
        if (sakaiBaseUrl == null || sakaiBaseUrl.trim().isEmpty()) return false;

        try {
            URI srcUri = new URI(s);
            URI baseUri = new URI(sakaiBaseUrl.trim());

            String srcScheme = normalizeScheme(srcUri.getScheme());
            String baseScheme = normalizeScheme(baseUri.getScheme());

            String srcHost = normalizeHost(srcUri.getHost());
            String baseHost = normalizeHost(baseUri.getHost());

            int srcPort = effectivePort(srcUri);
            int basePort = effectivePort(baseUri);

            if (srcScheme == null || srcHost == null) return false; // malformed absolute URL
            if (baseScheme == null || baseHost == null) return false;

            return srcScheme.equals(baseScheme)
                    && srcHost.equals(baseHost)
                    && srcPort == basePort;

        } catch (URISyntaxException e) {
            // If we can't parse it safely, treat it as external
            return false;
        }
    }

    private static boolean hasScheme(String s) {
        // "http://", "https://", etc.
        // URI parsing is fussier; this cheap check catches normal cases.
        int colon = s.indexOf(':');
        int slash = s.indexOf('/');
        return colon > 0 && (slash == -1 || colon < slash);
    }

    private static String normalizeScheme(String scheme) {
        return scheme == null ? null : scheme.toLowerCase(Locale.ROOT);
    }

    private static String normalizeHost(String host) {
        return host == null ? null : host.toLowerCase(Locale.ROOT);
    }

    private static int effectivePort(URI u) {
        int p = u.getPort();
        if (p != -1) return p;
        String scheme = normalizeScheme(u.getScheme());
        if ("https".equals(scheme)) return 443;
        if ("http".equals(scheme)) return 80;
        // unknown scheme — treat as "not same origin"
        return -2;
    }
}
