package org.sakaiproject.api.app.messageforums.cover;

import java.util.HashMap;
import java.util.List;

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

	public static void incrementMessagesSynopticToolInfo(String userId, String siteId){
		SynopticMsgcntrManager service = getInstance();
		if(service != null)			
			service.incrementMessagesSynopticToolInfo(userId, siteId);
	}

	public static void incrementForumSynopticToolInfo(String userId, String siteId){
		SynopticMsgcntrManager service = getInstance();
		if(service != null)			
			service.incrementForumSynopticToolInfo(userId, siteId);
	}

	public static void decrementMessagesSynopticToolInfo(String userId, String siteId){
		SynopticMsgcntrManager service = getInstance();
		if(service != null)			
			service.decrementMessagesSynopticToolInfo(userId, siteId);
	}

	public static void decrementForumSynopticToolInfo(String userId, String siteId){
		SynopticMsgcntrManager service = getInstance();
		if(service != null)			
			service.decrementForumSynopticToolInfo(userId, siteId);
	}
	
	public static void createOrUpdateSynopticToolInfo(String userId, String siteId, String siteTitle, int unreadMessageCount, int unreadForumCount){
		SynopticMsgcntrManager service = getInstance();
		if(service != null)			
			service.createOrUpdateSynopticToolInfo(userId, siteId, siteTitle, unreadMessageCount, unreadForumCount);
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
}
