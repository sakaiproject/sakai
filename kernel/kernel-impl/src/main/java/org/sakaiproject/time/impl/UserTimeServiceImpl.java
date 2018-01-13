package org.sakaiproject.time.impl;

import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;

/**
 * This just deals with the user specific part of what timezone they are in.
 */
public class UserTimeServiceImpl implements UserTimeService {
    // Cache of userIds to Timezone
    private Cache<String, String> M_userTzCache;

    // Map of Timezone/Locales to LocalTzFormat objects
    private ConcurrentHashMap<String, TimeZone> tzCache = new ConcurrentHashMap<>();

    // Default Timezone/Locale
    private String defaultTimezone = TimeZone.getDefault().getID();

    private MemoryService memoryService;
    private SessionManager sessionManager;
    private PreferencesService preferencesService;

    public void setMemoryService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setPreferencesService(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    public void init() {
        //register the Cache
        M_userTzCache = memoryService.getCache("org.sakaiproject.time.impl.BasicTimeService.userTimezoneCache");
    }

    protected String getUserTimezone() {
        // Check if we already cached this user's timezone
        String userId = sessionManager.getCurrentSessionUserId();
        if (userId == null) return defaultTimezone;

        String timeZoneLocale = M_userTzCache.get(userId);
        if (timeZoneLocale != null) return timeZoneLocale;

        // Otherwise, get the user's preferred time zone
        Preferences prefs = preferencesService.getPreferences(userId);
        ResourceProperties tzProps = prefs.getProperties(TimeService.APPLICATION_ID);
        String timeZone = tzProps.getProperty(TimeService.TIMEZONE_KEY);

        if (timeZone == null || timeZone.equals(""))
            timeZone = TimeZone.getDefault().getID();

        timeZoneLocale = timeZone;

        M_userTzCache.put(userId, timeZoneLocale);

        return timeZoneLocale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZone getLocalTimeZone() {
        String tz = getUserTimezone();
        // Not holding a cache can be slow.
        return tzCache.computeIfAbsent(tz, TimeZone::getTimeZone);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean clearLocalTimeZone(String userId) {
        M_userTzCache.remove(userId);
        return true;
    }

}
