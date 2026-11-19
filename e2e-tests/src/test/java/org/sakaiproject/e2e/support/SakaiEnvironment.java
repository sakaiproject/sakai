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
package org.sakaiproject.e2e.support;

import java.util.Locale;

public final class SakaiEnvironment {

    private SakaiEnvironment() {
    }

    public static String baseUrl() {
        String fromProperty = System.getProperty("PLAYWRIGHT_BASE_URL");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }

        String fromEnv = System.getenv("PLAYWRIGHT_BASE_URL");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }

        return "http://127.0.0.1:8080";
    }

    public static boolean headless() {
        String fromProperty = System.getProperty("PLAYWRIGHT_HEADLESS");
        String fromEnv = System.getenv("PLAYWRIGHT_HEADLESS");
        String raw = fromProperty != null ? fromProperty : fromEnv;
        if (raw == null || raw.isBlank()) {
            return true;
        }
        return !"false".equals(raw.toLowerCase(Locale.ROOT));
    }

    public static String browserName() {
        String fromProperty = System.getProperty("PLAYWRIGHT_BROWSER");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }

        String fromEnv = System.getenv("PLAYWRIGHT_BROWSER");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }

        return "chromium";
    }

    /**
     * How long search tests wait for the index to catch up. Defaults to 15s; raise it when running search tests locally,
     * since the index queue is drained every 60s by default.
     */
    public static long searchIndexTimeoutMs() {
        String fromProperty = System.getProperty("PLAYWRIGHT_SEARCH_TIMEOUT_MS");
        String raw = fromProperty != null && !fromProperty.isBlank() ? fromProperty : System.getenv("PLAYWRIGHT_SEARCH_TIMEOUT_MS");
        if (raw == null || raw.isBlank()) {
            return 15_000;
        }
        return Long.parseLong(raw.trim());
    }

    public static String resolveUser(String username) {
        return username;
    }
}
