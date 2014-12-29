/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.basiclti;

import org.sakaiproject.event.cover.EventTrackingService;

public class LocalEventTrackingService {
	/*
	   public static org.sakaiproject.event.api.Event newEvent(java.lang.String param0, java.lang.String param1, java.lang.String param3, boolean param3, int param4) {
	//For 2.6
	return EventTrackingService.newEvent(param0,param1,param2,param3,param4);
	}
	 */
	public static org.sakaiproject.event.api.Event newEvent(java.lang.String param0, java.lang.String param1, java.lang.String param2, boolean param3, int param4) {
		//For 2.5
		return EventTrackingService.newEvent(param0,param1,param3);
	}

	public static void post(org.sakaiproject.event.api.Event param0) {
		EventTrackingService.post(param0);
	}
}
