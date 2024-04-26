/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2010 The Sakai Foundation
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

package org.sakaiproject.announcement.user.prefs;

import lombok.Setter;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.util.UserNotificationPreferencesRegistrationImpl;
import org.sakaiproject.util.ResourceLoader;

public class AnnouncementUserNotificationPreferencesRegistrationImpl extends UserNotificationPreferencesRegistrationImpl {

	private static final String NOTIFICATION_ANNOUNCEMENTS_DEFAULT_OPTION = "prefs.notification.announcements.default.option";
	public ResourceLoader getResourceLoader(String location) {
		return new ResourceLoader(location);
	}

	@Setter
	private ServerConfigurationService serverConfigurationService;

	public void init() {
		super.init();
		setDefaultValue(serverConfigurationService.getString(NOTIFICATION_ANNOUNCEMENTS_DEFAULT_OPTION));
	}
}
