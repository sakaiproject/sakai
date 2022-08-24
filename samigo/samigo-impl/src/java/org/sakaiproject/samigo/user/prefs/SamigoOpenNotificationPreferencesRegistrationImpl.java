package org.sakaiproject.samigo.user.prefs;

import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.UserNotificationPreferencesRegistrationImpl;

public class SamigoOpenNotificationPreferencesRegistrationImpl extends UserNotificationPreferencesRegistrationImpl {
    public ResourceLoader getResourceLoader(String location) {
        return new ResourceLoader(location);
    }
}