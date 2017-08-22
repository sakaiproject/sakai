/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.component.cover.ComponentManager;

public class SynopticMsgcntrManagerCover {

	private static SynopticMsgcntrManager m_instance = null;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static SynopticMsgcntrManager getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (SynopticMsgcntrManager) ComponentManager
				.get(SynopticMsgcntrManager.class);
			return m_instance;
		}
		else
		{
			return (SynopticMsgcntrManager) ComponentManager
			.get(SynopticMsgcntrManager.class);
		}
	}

	public static void incrementMessagesSynopticToolInfo(List<String> userIds, String siteId){
		SynopticMsgcntrManager service = getInstance();
		if(service != null)			
			service.incrementMessagesSynopticToolInfo(userIds, siteId);
	}

	public static void incrementForumSynopticToolInfo(List<String> userIds, String siteId){
		SynopticMsgcntrManager service = getInstance();
		if(service != null)			
			service.incrementForumSynopticToolInfo(userIds, siteId);
	}

	public static void decrementMessagesSynopticToolInfo(List<String> userIds, String siteId){
		SynopticMsgcntrManager service = getInstance();
		if(service != null)			
			service.decrementMessagesSynopticToolInfo(userIds, siteId);
	}

	public static void decrementForumSynopticToolInfo(List<String> userIds, String siteId){
		SynopticMsgcntrManager service = getInstance();
		if(service != null)			
			service.decrementForumSynopticToolInfo(userIds, siteId);
	}
	
	public static void createOrUpdateSynopticToolInfo(List<String> userIds, String siteId, String siteTitle, Map<String, Integer[]> unreadCounts){
		SynopticMsgcntrManager service = getInstance();
		if(service != null)			
			service.createOrUpdateSynopticToolInfo(userIds, siteId, siteTitle, unreadCounts);
	}
	
	public static HashMap<String, Integer> getUserToNewMessagesForForumMap(String siteId, Long forumId, Long topicId){
		SynopticMsgcntrManager service = getInstance();
		if(service != null)			
			return service.getUserToNewMessagesForForumMap(siteId, forumId, topicId);
		
		return null;
	}
	
	public static void updateSynopticMessagesForForumComparingOldMessagesCount(String siteId, Long forumId, Long topicId, HashMap<String, Integer> previousCountHM){
		SynopticMsgcntrManager service = getInstance();
		if(service != null)			
			service.updateSynopticMessagesForForumComparingOldMessagesCount(siteId, forumId, topicId, previousCountHM);
	}
	
	public static void resetAllUsersSynopticInfoInSite(String siteId){
		SynopticMsgcntrManager service = getInstance();
		if(service != null)			
			service.resetAllUsersSynopticInfoInSite(siteId);
	}
}
