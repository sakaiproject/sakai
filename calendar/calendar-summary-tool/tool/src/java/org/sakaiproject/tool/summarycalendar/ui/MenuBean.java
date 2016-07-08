/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.summarycalendar.ui;

import java.io.Serializable;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;

public class MenuBean implements Serializable {
	private static final long	serialVersionUID	= 8092527674632095783L;

	private transient ServerConfigurationService serverConfigurationService =
			(ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class.getName());

	public String processCalendar(){
		return "calendar";
	}

	public String processPreferences(){
		return "prefs";
	}

	public String processSubscribe(){
		return "subscribe";
	}

	public boolean isSubscribeEnabled() {
		return serverConfigurationService.getBoolean("ical.public.secureurl.subscribe", serverConfigurationService.getBoolean("ical.opaqueurl.subscribe", true));
	}
}
