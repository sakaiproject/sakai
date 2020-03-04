package org.sakaiproject.component.app.messageforums;

import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.UserNotificationPreferencesRegistrationImpl;

public class MsgcntrUserNotificationPreferencesRegistrationImpl extends UserNotificationPreferencesRegistrationImpl {

    public ResourceLoader getResourceLoader(String location) {
        return new ResourceLoader(location);
    }
}
