/**
 * Copyright (c) 2005-2010 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.sakaiproject.component.app.messageforums;

import java.util.TimeZone;

import org.sakaiproject.api.app.messageforums.UserPreferencesManager;
import org.sakaiproject.time.api.UserTimeService;

/**
 * @author branden
 *
 */
public class SakaiUserPreferencesManagerImpl implements UserPreferencesManager {

	protected UserTimeService userTimeService;
	public void setUserTimeService(UserTimeService userTimeService) {
		this.userTimeService = userTimeService;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.messageforums.UserPreferencesManager#getTimeZone()
	 */
	public TimeZone getTimeZone() {
		TimeZone timeZone = userTimeService.getLocalTimeZone();
		
		if (timeZone == null) {
			timeZone = TimeZone.getDefault();
		}
		
		return timeZone;
	}

}