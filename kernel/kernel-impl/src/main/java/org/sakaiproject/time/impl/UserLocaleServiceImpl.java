package org.sakaiproject.time.impl;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;

import java.util.Locale;
import java.util.Objects;

/**
 * This provides a cached lookup to the user's locale. This might not be needed but due to the number of calls
 * made to formatting dates we need to be careful about constantly going back to the service to find out the user's
 * locale as it isn't cached at the moment.
 * <p>
 * In the future the ResourceLoader should be refactored so that getting a user's locale is separated out from the
 * looking up of resource strings.
 */
public class UserLocaleServiceImpl {

    // Cache of userIds to Locales
    private Cache<String, String> userLocaleCache;

    private SessionManager sessionManager;
    private MemoryService memoryService;
    private ResourceLoader resourceLoader;

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setMemoryService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void init() {
        Objects.requireNonNull(sessionManager);
        Objects.requireNonNull(memoryService);
        Objects.requireNonNull(resourceLoader);
        userLocaleCache = memoryService.getCache("org.sakaiproject.time.impl.BasicTimeService.userLocaleCache");
    }

    public String getLocalLocale() {
        String userId = sessionManager.getCurrentSessionUserId();
        if (userId == null) {
            return Locale.getDefault().toString();
        }
        String locale = userLocaleCache.get(userId);
        if (locale == null) {
            // Load the user's locale
            locale = resourceLoader.getLocale().toString();
            userLocaleCache.put(userId, locale);
        }
        return locale;
    }

    public boolean clearLocalLocale(String userId) {
        userLocaleCache.remove(userId);
        return true;
    }
}
