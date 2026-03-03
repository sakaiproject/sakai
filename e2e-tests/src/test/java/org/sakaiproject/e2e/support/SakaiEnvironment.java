package org.sakaiproject.e2e.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class SakaiEnvironment {

    private static final Map<String, String> INSTRUCTOR_ASSIGNMENTS = new ConcurrentHashMap<>();
    private static final AtomicInteger NEXT_INSTRUCTOR_SLOT = new AtomicInteger(0);

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

    public static String resolveUser(String username, String isolationKey) {
        if (username == null || username.isBlank()) {
            return username;
        }

        if (!isolateInstructorUsers() || !isInstructorUsername(username)) {
            return username;
        }

        List<String> instructors = instructorPool();
        if (instructors.isEmpty()) {
            return username;
        }

        String key = (isolationKey == null || isolationKey.isBlank()) ? "default" : isolationKey;
        return INSTRUCTOR_ASSIGNMENTS.computeIfAbsent(
            key,
            unused -> instructors.get(Math.floorMod(NEXT_INSTRUCTOR_SLOT.getAndIncrement(), instructors.size()))
        );
    }

    private static boolean isolateInstructorUsers() {
        String configured = firstDefined("PLAYWRIGHT_ISOLATE_INSTRUCTORS", "PLAYWRIGHT_ISOLATE_INSTRUCTORS");
        if (configured == null || configured.isBlank()) {
            return true;
        }
        return !"false".equals(configured.toLowerCase(Locale.ROOT));
    }

    private static boolean isInstructorUsername(String username) {
        return username.matches("^instructor\\d*$");
    }

    private static List<String> instructorPool() {
        String configured = firstDefined("PLAYWRIGHT_INSTRUCTOR_POOL", "PLAYWRIGHT_INSTRUCTOR_POOL");
        if (configured == null || configured.isBlank()) {
            return List.of("instructor", "instructor1", "instructor2");
        }

        String[] rawParts = configured.split(",");
        List<String> parsed = new ArrayList<>();
        for (String raw : rawParts) {
            String trimmed = raw.trim();
            if (!trimmed.isBlank()) {
                parsed.add(trimmed);
            }
        }
        return parsed;
    }

    private static String firstDefined(String property, String env) {
        String fromProperty = System.getProperty(property);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        String fromEnv = System.getenv(env);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return null;
    }
}
