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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.user.prefs;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.impl.BaseContentService;
import org.sakaiproject.util.UserNotificationPreferencesRegistrationImpl;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Resource;


public class ContentUserNotificationPreferencesRegistrationImpl extends UserNotificationPreferencesRegistrationImpl {

	private ServerConfigurationService serverConfigurationService = null;
	
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.ContentProperties";
	private static final String RESOURCECLASS = "resource.class.content";

	
	public ResourceLoader getResourceLoader(String location) {
		//return new ResourceLoader(location);
		
		String resourceClass = getServerConfigurationService().getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
		return new Resource().getLoader(resourceClass, location);
	}

	public ServerConfigurationService getServerConfigurationService() {
		return serverConfigurationService;
	}

	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

}
