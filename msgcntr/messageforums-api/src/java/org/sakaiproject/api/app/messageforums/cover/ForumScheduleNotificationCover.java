/**
 * Copyright (c) 2003-2010 The Apereo Foundation
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
package org.sakaiproject.api.app.messageforums.cover;

import java.util.Date;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.ForumScheduleNotification;
import org.sakaiproject.component.cover.ComponentManager;

public class ForumScheduleNotificationCover {

	private static ForumScheduleNotification m_instance = null;
	
	public static ForumScheduleNotification getInstance(){
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (ForumScheduleNotification) ComponentManager
				.get(ForumScheduleNotification.class);
			return m_instance;
		}
		else
		{
			return (ForumScheduleNotification) ComponentManager
			.get(ForumScheduleNotification.class);
		}
	}
	
	public static void scheduleAvailability(Area area){
		ForumScheduleNotification service = getInstance();
		if(service != null){
			service.scheduleAvailability(area);
		}
	}
	
	public static void scheduleAvailability(DiscussionForum forum){
		ForumScheduleNotification service = getInstance();
		if(service != null){
			service.scheduleAvailability(forum);
		}
	}
	
	public static void scheduleAvailability(DiscussionTopic topic){
		ForumScheduleNotification service = getInstance();
		if(service != null){
			service.scheduleAvailability(topic);
		}
	}
	
	public static boolean makeAvailableHelper(boolean availabilityRestricted, Date openDate, Date closeDate){
		ForumScheduleNotification service = getInstance();
		if(service != null){
			return service.makeAvailableHelper(availabilityRestricted, openDate, closeDate);
		}
		//when it doubt return true
		return true;
	}
}
