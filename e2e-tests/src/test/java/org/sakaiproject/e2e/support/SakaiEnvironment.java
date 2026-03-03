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

    public static String resolveUser(String username) {
        return username;
    }
}
