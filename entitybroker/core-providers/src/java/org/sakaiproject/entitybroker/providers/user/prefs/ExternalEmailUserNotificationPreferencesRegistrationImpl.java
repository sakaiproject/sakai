/**
 * $Id$
 * $URL$
 **************************************************************************
 * Copyright (c) 2014 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.providers.user.prefs;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.UserNotificationPreferencesRegistrationImpl;

/**
 * Adds external email notification user preferences to the user notification preferences page.
 *
 * Added for https://jira.sakaiproject.org/browse/SAK-25733
 * @author Bill Smith (wsmith @ unicon.net)
 * @author Aaron Zeckoski (azeckoski @ gmail.com) (azeckoski @ unicon.net)
 */
public class ExternalEmailUserNotificationPreferencesRegistrationImpl extends
        UserNotificationPreferencesRegistrationImpl {

    /**
     * Config setting which determines if this is enabled
     */
    public static final String NOTIFY_POST_ENABLED = "notify.post.enabled";

    private DeveloperHelperService developerHelperService;

    /* (non-Javadoc)
     * @see org.sakaiproject.user.api.UserNotificationPreferencesRegistration#getResourceLoader(java.lang.String)
     */
    public ResourceLoader getResourceLoader(String location) {
        return new ResourceLoader(location);
    }

    /**
     * Conditionally enables the user preferences for external email notification posting. 
     * @see org.sakaiproject.entitybroker.providers.NotificationEntityProvider#isEnabled
     */
    public void init() {
        if (developerHelperService.getConfigurationSetting(NOTIFY_POST_ENABLED, false)) {
            // only run the init when this is enabled
            super.init();
        }
    }

    public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
        this.developerHelperService = developerHelperService;
    }
}