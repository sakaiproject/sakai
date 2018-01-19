/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/MessageForumsMessageManagerImpl.java $
 * $Id: MessageForumsMessageManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.component.app.messageforums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.MessageMoveHistory;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.UnreadStatus;
import org.sakaiproject.api.app.messageforums.UserStatistics;
import org.sakaiproject.api.app.messageforums.cover.SynopticMsgcntrManagerCover;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AttachmentImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageMoveHistoryImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.UnreadStatusImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.Util;
import org.sakaiproject.component.app.messageforums.exception.LockedException;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

@Slf4j
public class MessageForumsMessageManagerImpl extends HibernateDaoSupport implements MessageForumsMessageManager {

    //private static final String QUERY_BY_MESSAGE_ID = "findMessageById";
    //private static final String QUERY_ATTACHMENT_BY_ID = "findAttachmentById";
    private static final String QUERY_BY_MESSAGE_ID_WITH_ATTACHMENTS = "findMessageByIdWithAttachments";
    private static final String QUERY_COUNT_BY_READ = "findReadMessageCountByTopicId";
    private static final String QUERY_COUNT_BY_AUTHORED = "findAuhtoredMessageCountByTopicId";
    private static final String QUERY_MESSAGE_COUNTS_FOR_MAIN_PAGE = "findMessageCountsForMainPage";
    private static final String QUERY_READ_MESSAGE_COUNTS_FOR_MAIN_PAGE = "findReadMessageCountsForMainPage";
    private static final String QUERY_BY_TOPIC_ID = "findMessagesByTopicId";
    private static final String QUERY_COUNT_VIEWABLE_BY_TOPIC_ID = "findViewableMessageCountByTopicIdByUserId";
    private static final String QUERY_COUNT_READ_VIEWABLE_BY_TOPIC_ID = "findReadViewableMessageCountByTopicIdByUserId";
    private static final String QUERY_UNREAD_STATUS = "findUnreadStatusForMessage";
    private static final String QUERY_CHILD_MESSAGES = "finalAllChildMessages";
    private static final String QUERY_READ_STATUS_WITH_MSGS_USER = "findReadStatusByMsgIds";
    private static final String QUERY_FIND_PENDING_MSGS_BY_CONTEXT_AND_USER_AND_PERMISSION_LEVEL = "findAllPendingMsgsByContextByMembershipByPermissionLevel";
    private static final String QUERY_FIND_PENDING_MSGS_BY_CONTEXT_AND_USER_AND_PERMISSION_LEVEL_NAME = "findAllPendingMsgsByContextByMembershipByPermissionLevelName";
    private static final String QUERY_FIND_PENDING_MSGS_BY_TOPICID = "findPendingMsgsByTopicId";
    private static final String QUERY_UNDELETED_MSG_BY_TOPIC_ID = "findUndeletedMessagesByTopicId";
    private static final String QUERY_MOVED_MESSAGES_BY_TOPICID = "findMovedMessagesByTopicId";
    private static final String QUERY_MOVED_HISTORY_BY_MESSAGEID = "findMovedHistoryByMessageId";
    //private static final String ID = "id";

    // Oracle's 1000 'in' clause limit
    private static final int MAX_IN_CLAUSE_SIZE = 1000;

    private static final String MESSAGECENTER_HELPER_TOOL_ID = "sakai.messageforums.helper";

    private IdManager idManager;                      

    private MessageForumsTypeManager typeManager;

    private SessionManager sessionManager;

    private EventTrackingService eventTrackingService;
    
    private ContentHostingService contentHostingService;

    private SiteService siteService;
    
    private ToolManager toolManager;
    
    public void init() {
       log.info("init()");
        ;
    }

    public EventTrackingService getEventTrackingService() {
        return eventTrackingService;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }
    
    public MessageForumsTypeManager getTypeManager() {
        return typeManager;
    }

    public void setTypeManager(MessageForumsTypeManager typeManager) {
        this.typeManager = typeManager;
    }
    
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    public IdManager getIdManager() {
        return idManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
    
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }
 
    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by site for sites with
     * 		Forum topics that don't have membership items in the db
     */
    public List<Object []> findDiscussionForumMessageCountsForTopicsWithMissingPermsForAllSites(final List<String> siteList) {
    	if (siteList == null) {
            log.error("findDiscussionForumMessageCountsForTopicsWithMissingPermsForAllSites failed with null site list.");
            throw new IllegalArgumentException("Null Argument");
    	}	
        
    	HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findDiscussionForumMessageCountsForTopicsWithMissingPermsForAllSites");
             q.setParameterList("siteList", siteList);
             q.setString("userId", getCurrentUser());
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by site for sites with
     * 		Forum topics that don't have membership items in the db
     */
    public List<Object []> findDiscussionForumReadMessageCountsForTopicsWithMissingPermsForAllSites(final List<String> siteList) {
    	if (siteList == null) {
            log.error("findDiscussionForumReadMessageCountsForTopicsWithMissingPermsForAllSites failed with null site list.");
            throw new IllegalArgumentException("Null Argument");
    	}	
        
    	HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findDiscussionForumReadMessageCountsForTopicsWithMissingPermsForAllSites");
             q.setParameterList("siteList", siteList);
             q.setString("userId", getCurrentUser());
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by site
     */
    public List findDiscussionForumMessageCountsForAllSitesByPermissionLevelId(final List siteList, final List roleList) {
    	if (siteList == null) {
            log.error("findDiscussionForumMessageCountsForAllSitesByPermissionLevelId failed with null site list.");
            throw new IllegalArgumentException("Null Argument");
    	}	
        
    	HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery("findDiscussionForumMessageCountsForAllSitesByPermissionLevelId");
             q.setParameterList("siteList", siteList);
             q.setParameterList("roleList", roleList);
             q.setString("userId", getCurrentUser());
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }

    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by site
     */
    public List findDiscussionForumMessageCountsForAllSitesByPermissionLevelName(final List siteList, final List roleList) {
    	if (siteList == null) {
            log.error("findDiscussionForumMessageCountsForAllSitesByPermissionLevelName failed with null site list.");
            throw new IllegalArgumentException("Null Argument");
    	}	
        
    	HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery("findDiscussionForumMessageCountsForAllSitesByPermissionLevelName");
             q.setParameterList("siteList", siteList);
             q.setParameterList("roleList", roleList);
             q.setString("userId", getCurrentUser());
             q.setString("customTypeUuid", typeManager.getCustomLevelType());
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of read discussion forum messages grouped by site
     */
    public List findDiscussionForumReadMessageCountsForAllSitesByPermissionLevelId(final List siteList, final List roleList) {
        
    	HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery("findDiscussionForumReadMessageCountsForAllSitesByPermissionLevelId");
            q.setParameterList("siteList", siteList);
            q.setParameterList("roleList", roleList);
            q.setString("userId", getCurrentUser());
            return q.list();
        };
        
        return getHibernateTemplate().execute(hcb);
    }

    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of read discussion forum messages grouped by site
     */
    public List findDiscussionForumReadMessageCountsForAllSitesByPermissionLevelName(final List siteList, final List roleList) {
        
    	HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery("findDiscussionForumReadMessageCountsForAllSitesByPermissionLevelName");
            q.setParameterList("siteList", siteList);
            q.setParameterList("roleList", roleList);
            q.setString("userId", getCurrentUser());
            q.setString("customTypeUuid", typeManager.getCustomLevelType());
            return q.list();
        };
        
        return getHibernateTemplate().execute(hcb);
    }
    
    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by topics within a site
     * 		Used by sites that are grouped
     */
    public List findDiscussionForumMessageCountsForGroupedSitesByTopic(final List siteList, final List roleList) {
        
    	HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery("findDiscussionForumMessageCountsForGroupedSitesByTopic");
            q.setParameterList("siteList", siteList);
            q.setParameterList("roleList", roleList);
            q.setString("userId", getCurrentUser());
            q.setString("customTypeUuid", typeManager.getCustomLevelType());
            return q.list();
        };
        
        return getHibernateTemplate().execute(hcb);
    }

    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by topics within a site
     * 		Used by sites that are grouped
     */
    public List findDiscussionForumReadMessageCountsForGroupedSitesByTopic(final List siteList, final List roleList) {
        
    	HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery("findDiscussionForumReadMessageCountsForGroupedSitesByTopic");
            q.setParameterList("siteList", siteList);
            q.setParameterList("roleList", roleList);
            q.setString("userId", getCurrentUser());
            q.setString("customTypeUuid", typeManager.getCustomLevelType());
            return q.list();
        };
        
        return getHibernateTemplate().execute(hcb);
    }

    /**
     * FOR STATISTICS TOOL:
     * 		Returns the number of read messages by topic for specified user
     */
    
    public int findAuhtoredMessageCountByTopicIdByUserId(final Long topicId, final String userId){
    	if (topicId == null || userId == null) {
            log.error("findAuthoredMessageCountByTopicIdByUserId failed with topicId: " + topicId + 
            			" and userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findAuthoredMessageCountByTopicIdByUserId executing with topicId: " + topicId + 
        				" and userId: " + userId);

        HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_COUNT_BY_AUTHORED);
            q.setLong("topicId", topicId);
            q.setString("userId", userId);
            return (Number) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb).intValue();
    }
    
    public int findAuthoredMessageCountForStudent(final String userId) {
    	if (userId == null) {
    		log.error("findAuthoredMessageCountForStudentInSite failed with a null userId");
    		throw new IllegalArgumentException("userId cannot be null");
    	}
    	
    	if (log.isDebugEnabled()) log.debug("findAuthoredMessageCountForStudentInSite executing with userId: " + userId);
    	
        HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery("findAuthoredMessageCountForStudent");
            q.setString("contextId", getContextId());
            q.setString("userId", userId);
            return (Number) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb).intValue();
    }
    
    /*
     * (non-Javadoc)
     * @see org.sakaiproject.api.app.messageforums.MessageForumsMessageManager#findAuthoredMessagesForStudent(java.lang.String)
     */
    public List<Message> findAuthoredMessagesForStudent(final String studentId) {
      if (log.isDebugEnabled()) log.debug("findReadMessagesForCurrentStudent()");
      
      HibernateCallback<List<Message>> hcb = session -> {
          Query q = session.getNamedQuery("findAuthoredMessagesForStudent");
          q.setParameter("contextId", getContextId(), StringType.INSTANCE);
          q.setParameter("userId", studentId, StringType.INSTANCE);
          return q.list();
      };
      
      return getHibernateTemplate().execute(hcb);
    }
    
    public List<UserStatistics> findAuthoredStatsForStudent(final String studentId) {
        if (log.isDebugEnabled()) log.debug("findAuthoredStatsForStudent()");
        
        HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findAuthoredStatsForStudent");
            q.setParameter("contextId", getContextId(), StringType.INSTANCE);
            q.setParameter("userId", studentId, StringType.INSTANCE);
            return q.list();
        };
        List<UserStatistics> returnList = new ArrayList<UserStatistics>();
        List<Object[]> results = getHibernateTemplate().execute(hcb);
        for(Object[] result : results){
      	  UserStatistics stat = new UserStatistics((String) result[0], (String) result[1], (Date) result[2], (String) result[3], 
      			  ((Integer) result[4]).toString(), ((Integer) result[5]).toString(), ((Integer) result[6]).toString(), studentId);
      	  returnList.add(stat);
        }
        return returnList;
      }
    
    public List<Message> findAuthoredMessagesForStudentByTopicId(final String studentId, final Long topicId) {
    	if (log.isDebugEnabled()) log.debug("findReadMessagesForCurrentStudentByTopicId()");

    	HibernateCallback<List<Message>> hcb = session -> {
            Query q = session.getNamedQuery("findAuthoredMessagesForStudentByTopicId");
            q.setParameter("contextId", getContextId(), StringType.INSTANCE);
            q.setParameter("userId", studentId, StringType.INSTANCE);
            q.setParameter("topicId", topicId, LongType.INSTANCE);
            return q.list();
        };

    	return getHibernateTemplate().execute(hcb);
    }

    public List<UserStatistics> findAuthoredStatsForStudentByTopicId(final String studentId, final Long topicId) {
    	if (log.isDebugEnabled()) log.debug("findAuthoredStatsForStudentByTopicId()");

    	HibernateCallback hcb = session -> {
            Query q = session.getNamedQuery("findAuthoredStatsForStudentByTopicId");
            q.setParameter("topicId", topicId, LongType.INSTANCE);
            q.setParameter("userId", studentId, StringType.INSTANCE);
            return q.list();
        };
    	List<UserStatistics> returnList = new ArrayList<UserStatistics>();
    	List<Object[]> results = (List<Object[]>)getHibernateTemplate().execute(hcb);
    	for(Object[] result : results){
    		UserStatistics stat = new UserStatistics((String) result[0], (String) result[1], (Date) result[2], (String) result[3], 
    				((Integer) result[4]).toString(), ((Integer) result[5]).toString(), ((Integer) result[6]).toString(), studentId);
    		returnList.add(stat);
    	}
    	return returnList;
    }

    public List<Message> findAuthoredMessagesForStudentByForumId(final String studentId, final Long forumId) {
    	if (log.isDebugEnabled()) log.debug("findAuthoredMessagesForStudentByForumId()");

    	HibernateCallback<List<Message>> hcb = session -> {
            Query q = session.getNamedQuery("findAuthoredMessagesForStudentByForumId");
            q.setParameter("contextId", getContextId(), StringType.INSTANCE);
            q.setParameter("userId", studentId, StringType.INSTANCE);
            q.setParameter("forumId", forumId, LongType.INSTANCE);
            return q.list();
        };

    	return getHibernateTemplate().execute(hcb);
    }
    
    public List<UserStatistics> findAuthoredStatsForStudentByForumId(final String studentId, final Long topicId) {
    	if (log.isDebugEnabled()) log.debug("findAuthoredStatsForStudentByForumId()");

    	HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findAuthoredStatsForStudentByForumId");
            q.setParameter("forumId", topicId, LongType.INSTANCE);
            q.setParameter("userId", studentId, StringType.INSTANCE);
            return q.list();
        };
    	List<UserStatistics> returnList = new ArrayList<UserStatistics>();
    	List<Object[]> results = getHibernateTemplate().execute(hcb);
    	for(Object[] result : results){
    		UserStatistics stat = new UserStatistics((String) result[0], (String) result[1], (Date) result[2], (String) result[3], 
    				((Integer) result[4]).toString(), ((Integer) result[5]).toString(), ((Integer) result[6]).toString(), studentId);
    		returnList.add(stat);
    	}
    	return returnList;
    }
    
    public List<Object[]> findAuthoredMessageCountForAllStudents() {
    	if (log.isDebugEnabled()) log.debug("findAuthoredMessageCountForAllStudents executing");
    	
        HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findAuthoredMessageCountForAllStudents");
            q.setString("contextId", getContextId());
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    public List<Object[]> findAuthoredMessageCountForAllStudentsByTopicId(final Long topicId) {
    	if (log.isDebugEnabled()) log.debug("findAuthoredMessageCountForAllStudentsByTopicId executing");
    	
        HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findAuthoredMessageCountForAllStudentsByTopicId");
            q.setString("contextId", getContextId());
            q.setLong("topicId", topicId);
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    public List<Object[]> findAuthoredMessageCountForAllStudentsByForumId(final Long forumId) {
    	if (log.isDebugEnabled()) log.debug("findAuthoredMessageCountForAllStudentsByForumId executing");
    	
        HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findAuthoredMessageCountForAllStudentsByForumId");
            q.setString("contextId", getContextId());
            q.setLong("forumId", forumId);
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    public int findReadMessageCountByTopicIdByUserId(final Long topicId, final String userId) {
        if (topicId == null || userId == null) {
            log.error("findReadMessageCountByTopicIdByUserId failed with topicId: " + topicId + 
            			" and userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findReadMessageCountByTopicIdByUserId executing with topicId: " + topicId + 
        				" and userId: " + userId);

        HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_COUNT_BY_READ);
            q.setLong("topicId", topicId);
            q.setString("userId", userId);
            return (Number) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb).intValue();
    }
    
    public int findReadMessageCountForStudent(final String userId) {
    	if (userId == null) {
    		log.error("findReadMessageCountForStudent failed with null userId");
    		throw new IllegalArgumentException("userId cannot be null");
    	}
    	
    	if (log.isDebugEnabled()) log.debug("findReadMessageCountForStudent executing with userId: " + userId);
    	
        HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery("findReadMessageCountForStudent");
            q.setString("contextId", getContextId());
            q.setString("userId", userId);
            return (Number) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb).intValue();
    }
    
    /*
     * (non-Javadoc)
     * @see org.sakaiproject.api.app.messageforums.MessageForumsMessageManager#findReadMessagesForStudent()
     */
    public List<UserStatistics> findReadStatsForStudent(final String studentId) {
      if (log.isDebugEnabled()) log.debug("findReadStatsForStudent()");
      
      HibernateCallback<List<Object[]>> hcb = session -> {
          Query q = session.getNamedQuery("findReadStatsForStudent");
          q.setParameter("contextId", getContextId(), StringType.INSTANCE);
          q.setParameter("userId", studentId, StringType.INSTANCE);
          return q.list();
      };
      List<UserStatistics> returnList = new ArrayList<UserStatistics>();
      List<Object[]> results = getHibernateTemplate().execute(hcb);
      for(Object[] result : results){
    	  UserStatistics stat = new UserStatistics((String) result[0], (String) result[1], (Date) result[2], (String) result[3], 
    			  ((Integer) result[4]).toString(), ((Integer) result[5]).toString(), ((Integer) result[6]).toString(), studentId);
    	  returnList.add(stat);
      }
      return returnList;
    }
    
    public List<UserStatistics> findReadStatsForStudentByTopicId(final String studentId, final Long topicId) {
    	if (log.isDebugEnabled()) log.debug("findReadStatsForStudentByTopicId()");

    	HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findReadStatsForStudentByTopicId");
            q.setParameter("userId", studentId, StringType.INSTANCE);
            q.setParameter("topicId", topicId, LongType.INSTANCE);
            return q.list();
        };
        List<UserStatistics> returnList = new ArrayList<UserStatistics>();
        List<Object[]> results = getHibernateTemplate().execute(hcb);
        for(Object[] result : results){
      	  UserStatistics stat = new UserStatistics((String) result[0], (String) result[1], (Date) result[2], (String) result[3], 
      			  ((Integer) result[4]).toString(), ((Integer) result[5]).toString(), ((Integer) result[6]).toString(), studentId);
      	  returnList.add(stat);
        }
        return returnList;
    }
    
    public List<UserStatistics> findReadStatsForStudentByForumId(final String studentId, final Long forumId) {
    	if (log.isDebugEnabled()) log.debug("findReadStatsForStudentByForumId()");

    	HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findReadStatsForStudentByForumId");
            q.setParameter("userId", studentId, StringType.INSTANCE);
            q.setParameter("forumId", forumId, LongType.INSTANCE);
            return q.list();
        };
        List<UserStatistics> returnList = new ArrayList<UserStatistics>();
        List<Object[]> results = getHibernateTemplate().execute(hcb);
        for(Object[] result : results){
      	  UserStatistics stat = new UserStatistics((String) result[0], (String) result[1], (Date) result[2], (String) result[3], 
      			  ((Integer) result[4]).toString(), ((Integer) result[5]).toString(), ((Integer) result[6]).toString(), studentId);
      	  returnList.add(stat);
        }
        return returnList;
    }
    
    public List<Object[]> findReadMessageCountForAllStudents() {
    	if (log.isDebugEnabled()) log.debug("findReadMessageCountForAllStudentsInSite executing");
    	
        HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findReadMessageCountForAllStudents");
            q.setString("contextId", getContextId());
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    public List<Object[]> findReadMessageCountForAllStudentsByTopicId(final Long topicId) {
    	if (log.isDebugEnabled()) log.debug("findReadMessageCountForAllStudentsByTopicId executing");
    	
        HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findReadMessageCountForAllStudentsByTopicId");
            q.setString("contextId", getContextId());
            q.setLong("topicId", topicId);
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    public List<Object[]> findReadMessageCountForAllStudentsByForumId(final Long forumId) {
    	if (log.isDebugEnabled()) log.debug("findReadMessageCountForAllStudentsByForumId executing");
    	
        HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findReadMessageCountForAllStudentsByForumId");
            q.setString("contextId", getContextId());
            q.setLong("forumId", forumId);
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    /**
     * Returns count of all messages in a topic that have been approved or were authored by given user
     */
    public int findViewableMessageCountByTopicIdByUserId(final Long topicId, final String userId) {
        if (topicId == null || userId == null) {
            log.error("findViewableMessageCountByTopicIdByUserId failed with topicId: " + topicId + 
            			" and userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findViewableMessageCountByTopicIdByUserId executing with topicId: " + topicId + 
        				" and userId: " + userId);

        HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_COUNT_VIEWABLE_BY_TOPIC_ID);
            q.setLong("topicId", topicId);
            q.setString("userId", userId);
            return (Number) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb).intValue();
    }
    
    /**
     * Returns count of all msgs in a topic that have been approved or were authored by curr user
     */
    public int findViewableMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findViewableMessageCountByTopicId failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findViewableMessageCountByTopicId executing with topicId: " + topicId);

        if(getCurrentUser()!=null){
        return findViewableMessageCountByTopicIdByUserId(topicId, getCurrentUser());
        }
        else return 0;
    }

   public int findUnreadMessageCountByTopicIdByUserId(final Long topicId, final String userId){
	   if (topicId == null || userId == null) {
           log.error("findUnreadMessageCountByTopicIdByUserId failed with topicId: " + topicId + 
        		   		" and userId: " + userId);
 
           throw new IllegalArgumentException("Null Argument");
       }

       log.debug("findUnreadMessageCountByTopicIdByUserId executing with topicId: " + topicId);

       return findMessageCountByTopicId(topicId) - findReadMessageCountByTopicIdByUserId(topicId, userId);
   }
    
   public int findUnreadMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findUnreadMessageCountByTopicId failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findUnreadMessageCountByTopicId executing with topicId: " + topicId);

        return findMessageCountByTopicId(topicId) - findReadMessageCountByTopicId(topicId);
    }
   
   /**
    * Returns count of all unread msgs for given user that have been approved or
    * were authored by user
    */
   public int findUnreadViewableMessageCountByTopicIdByUserId(final Long topicId, final String userId) {
       if (topicId == null) {
           log.error("findUnreadViewableMessageCountByTopicIdByUserId failed with topicId: null and userid: " + userId);
           throw new IllegalArgumentException("Null Argument");
       }

       log.debug("findUnreadViewableMessageCountByTopicIdByUserId executing with topicId: " + topicId + " userId: " + userId);

       return findViewableMessageCountByTopicIdByUserId(topicId, userId) - findReadViewableMessageCountByTopicIdByUserId(topicId, userId);
   }
   
   /**
    * Returns count of all unread msgs for current user that have been approved or
    * were authored by current user
    */
   public int findUnreadViewableMessageCountByTopicId(final Long topicId) {
       if (topicId == null) {
           log.error("findUnreadViewableMessageCountByTopicId failed with topicId: null");
           throw new IllegalArgumentException("Null Argument");
       }

       log.debug("findUnreadViewableMessageCountByTopicId executing with topicId: " + topicId);

       if(getCurrentUser()!=null){
       return findUnreadViewableMessageCountByTopicIdByUserId(topicId, getCurrentUser());
       }
       else return 0;
   }
    
    public int findReadMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findReadMessageCountByTopicId failed with topicId: Null");
            throw new IllegalArgumentException("Null Argument");
        }

        if(getCurrentUser()!=null){
        return findReadMessageCountByTopicIdByUserId(topicId, getCurrentUser());
        }else return 0;
    }
    
    /**
     * Returns count of all read msgs for given user that have been approved or
     * were authored by user
     * @param topicId
     * @param userId
     * @return
     */
    public int findReadViewableMessageCountByTopicIdByUserId(final Long topicId, final String userId) {
    	if (topicId == null || userId == null) {
            log.error("findReadViewableMessageCountByTopicIdByUserId failed with topicId: " + topicId + 
            			" and userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findReadViewableMessageCountByTopicIdByUserId executing with topicId: " + topicId + 
        				" and userId: " + userId);

        HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_COUNT_READ_VIEWABLE_BY_TOPIC_ID);
            q.setLong("topicId", topicId);
            q.setString("userId", userId);
            return (Number) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb).intValue();
    }
    
    /**
     * Returns count of all read msgs for current user that have been approved or
     * were authored by user
     * @param topicId
     * @return
     */
    public int findReadViewableMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findReadViewableMessageCountByTopicId failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        if(getCurrentUser()!=null){
        return findReadViewableMessageCountByTopicIdByUserId(topicId, getCurrentUser());
        }
        else return 0;
    }
    
    public List findMessagesByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findMessagesByTopicId failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findMessagesByTopicId executing with topicId: " + topicId);

        HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_BY_TOPIC_ID);
            q.setParameter("topicId", topicId, LongType.INSTANCE);
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    public List findUndeletedMessagesByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findUndeletedMessagesByTopicId failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findUndeletedMessagesByTopicId executing with topicId: " + topicId);

        HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_UNDELETED_MSG_BY_TOPIC_ID);
            q.setParameter("topicId", topicId, LongType.INSTANCE);
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    public int findMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findMessageCountByTopicId failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findMessageCountByTopicId executing with topicId: " + topicId);

        HibernateCallback<Number> hcb = session -> {
            Query q = session.getNamedQuery("findMessageCountByTopicId");
            q.setLong("topicId", topicId);
            return (Number) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb).intValue();
    }
    
    public List<Object[]> findMessageCountByForumId(final Long forumId) {
        if (forumId == null) {
            log.error("findMessageCountByForumId failed with forumId: " + forumId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findMessageCountByForumId executing with forumId: " + forumId);

        HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findMessageCountByForumId");
            q.setLong("forumId", forumId);
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    /*
    +     * (non-Javadoc)
    +     * @see org.sakaiproject.api.app.messageforums.MessageForumsForumManager#findMessageCountsForMainPage(java.util.List)
    +     */
    public List<Object[]> findMessageCountsForMainPage(final Collection<Long> topicIds) {
    	if (topicIds.isEmpty()) return new ArrayList<Object[]>();

    	HibernateCallback<List<Object[]>> hcb = session -> {
            // would use the normal 'subList' approach to deal with Oracle's 1000 limit, but we're dealing with a Collection
            Iterator<Long> itTopicIds = topicIds.iterator();
            int numTopics = topicIds.size();

            List<Object[]> retrievedCounts = new ArrayList<>(numTopics);

            List<Long> queryTopics = new ArrayList<>(Math.min(numTopics, MAX_IN_CLAUSE_SIZE));
            int querySize = 0;
            while (itTopicIds.hasNext())
            {
                while (itTopicIds.hasNext() && querySize < MAX_IN_CLAUSE_SIZE)
                {
                    queryTopics.add(itTopicIds.next());
                    querySize++;
                }

                Query q = session.getNamedQuery(QUERY_MESSAGE_COUNTS_FOR_MAIN_PAGE);
                q.setParameterList("topicIds", queryTopics);
                retrievedCounts.addAll(q.list());

                queryTopics.clear();
                querySize = 0;
            }

            return retrievedCounts;
        };

    	return getHibernateTemplate().execute(hcb);
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.api.app.messageforums.MessageForumsMessageManager#findReadMessageCountsForMainPage(java.util.Collection)
     */
    public List<Object[]> findReadMessageCountsForMainPage(final Collection<Long> topicIds) {
    	if (topicIds.isEmpty()) return new ArrayList<>();

    	HibernateCallback<List<Object[]>> hcb = session -> {
            // would use the normal 'subList' approach to deal with Oracle's 1000 limit, but we're dealing with a Collection
            Iterator<Long> itTopicIds = topicIds.iterator();
            int numTopics = topicIds.size();
            String userId = getCurrentUser();

            List<Object[]> retrievedCounts = new ArrayList<Object[]>(numTopics);

            List<Long> queryTopics = new ArrayList<Long>(Math.min(numTopics, MAX_IN_CLAUSE_SIZE));
            int querySize = 0;
            while (itTopicIds.hasNext())
            {
                while (itTopicIds.hasNext() && querySize < MAX_IN_CLAUSE_SIZE)
                {
                    queryTopics.add(itTopicIds.next());
                    querySize++;
                }

                Query q = session.getNamedQuery(QUERY_READ_MESSAGE_COUNTS_FOR_MAIN_PAGE);
                q.setParameterList("topicIds", queryTopics);
                q.setParameter("userId", userId);
                retrievedCounts.addAll(q.list());

                queryTopics.clear();
                querySize = 0;
            }

            return retrievedCounts;
        };

    	return getHibernateTemplate().execute(hcb);
    }



    public List<Object[]> findMessageCountTotal() {
    	HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.getNamedQuery("findMessageCountTotal");
            q.setString("contextId", getContextId());
            return q.list();
        };
    	
    	return getHibernateTemplate().execute(hcb);
    }
    
    public UnreadStatus findUnreadStatusByUserId(final Long topicId, final Long messageId, final String userId){
    	if (messageId == null || topicId == null || userId == null) {
            log.error("findUnreadStatusByUserId failed with topicId: " + topicId + ", messageId: " + messageId
            		+ ", userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findUnreadStatus executing with topicId: " + topicId + ", messageId: " + messageId);

        HibernateCallback<UnreadStatus> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_UNREAD_STATUS);
            q.setParameter("topicId", topicId, LongType.INSTANCE);
            q.setParameter("messageId", messageId, LongType.INSTANCE);
            q.setParameter("userId", userId, StringType.INSTANCE);
            return (UnreadStatus) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    public UnreadStatus findUnreadStatus(final Long topicId, final Long messageId) {
        if (messageId == null || topicId == null) {
            log.error("findUnreadStatus failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        if(getCurrentUser()!=null){
        return findUnreadStatusByUserId(topicId, messageId, getCurrentUser()); 
        }else return null;
    }

    public void deleteUnreadStatus(Long topicId, Long messageId) {
        if (messageId == null || topicId == null) {
            log.error("deleteUnreadStatus failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("deleteUnreadStatus executing with topicId: " + topicId + ", messageId: " + messageId);

        UnreadStatus status = findUnreadStatus(topicId, messageId);
        if (status != null) {
            getHibernateTemplate().delete(status);
        }
    }

    private boolean isMessageFromForums(Message message) {
    	return message.getTopic() != null;
    }
    
    public void markMessageReadForUser(Long topicId, Long messageId, boolean read) {
        if (messageId == null || topicId == null) {
            log.error("markMessageReadForUser failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("markMessageReadForUser executing with topicId: " + topicId + ", messageId: " + messageId);

        if(getCurrentUser()!=null){
        markMessageReadForUser(topicId, messageId, read, getCurrentUser());
        }
        else return;
    }
    
    public void markMessageReadForUser(Long topicId, Long messageId, boolean read, String userId)
    {
    	markMessageReadForUser(topicId, messageId, read, userId, toolManager.getCurrentPlacement().getContext(), toolManager.getCurrentTool().getId());
    }
    
    public void markMessageReadForUser(Long topicId, Long messageId, boolean read, String userId, String context, String toolId)
    {
    	// to only add to event log if not read
    	boolean trulyUnread;
    	boolean originalReadStatus;
    	
    	if (messageId == null || topicId == null || userId == null) {
            log.error("markMessageReadForUser failed with topicId: " + topicId + ", messageId: " + messageId + ", userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("markMessageReadForUser executing with topicId: " + topicId + ", messageId: " + messageId);

        UnreadStatus status = findUnreadStatusByUserId(topicId, messageId, userId);
        if (status == null) {
            status = new UnreadStatusImpl();
            trulyUnread = true;
            originalReadStatus = false;
        }
        else {
        	trulyUnread = status.getRead().booleanValue();
        	trulyUnread = !trulyUnread;
        	originalReadStatus = status.getRead().booleanValue();
        }
        
        status.setTopicId(topicId);
        status.setMessageId(messageId);
        status.setUserId(userId);
        status.setRead(Boolean.valueOf(read));

        Message message = (Message) getMessageById(messageId);
        boolean isMessageFromForums = isMessageFromForums(message);
        if (trulyUnread) {
        	//increment the message count 	 
            	Integer nr = message.getNumReaders(); 	 
            	if (nr == null) 	 
                    nr = Integer.valueOf(0); 	 
            	nr = Integer.valueOf(nr.intValue() + 1); 	 
            	message.setNumReaders(nr); 	 
            	log.debug("set Message readers count to: " + nr); 	 
            	//baseForum is probably null 	 
            	if (message.getTopic().getBaseForum()==null && message.getTopic().getOpenForum() != null) 	 
                    message.getTopic().setBaseForum((BaseForum) message.getTopic().getOpenForum()); 	 
	 
            	this.saveMessage(message, false, toolId, userId, context, true);

        	if (isMessageFromForums)
        		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_READ, getEventMessage(message, toolId, userId, context), false));
        	else
        		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_READ, getEventMessage(message, toolId, userId, context), false));
        }
        	
        getHibernateTemplate().saveOrUpdate(status);
       
        
        	
        	if (isMessageFromForums){
        		if(!originalReadStatus && read){
        			//status is changing from Unread to Read, so decrement unread number for Synoptic Messages
        			decrementForumSynopticToolInfo(userId, context, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        		}else if(originalReadStatus && !read){
        			//status is changing from Read to Unread, so increment unread number for Synoptic Messages
        			incrementForumSynopticToolInfo(userId, context, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        		}
        	}else{
        		if(!originalReadStatus && read){
        			//status is changing from Unread to Read, so decrement unread number for Synoptic Messages
        			decrementMessagesSynopticToolInfo(userId, context, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        		}else if(originalReadStatus && !read){
        			//status is changing from Read to Unread, so increment unread number for Synoptic Messages
        			incrementMessagesSynopticToolInfo(userId, context, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        		}
        	}
        
    }


    public void decrementForumSynopticToolInfo(String userId, String siteId, int numOfAttempts) {
    	try {
    		SynopticMsgcntrManagerCover.decrementForumSynopticToolInfo(Arrays.asList(userId), siteId);
    	} catch (HibernateOptimisticLockingFailureException holfe) {

    		// failed, so wait and try again
    		try {
    			Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
    		} catch (InterruptedException e) {
    			log.error(e.getMessage(), e);
    		}

    		numOfAttempts--;

    		if (numOfAttempts <= 0) {
    			log.info("MessageForumsMessageManagerImpl: decrementForumSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
    			log.error(holfe.getMessage(), holfe);
    		} else {
    			log.info("MessageForumsMessageManagerImpl: decrementForumSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
    					+ numOfAttempts);
    			decrementForumSynopticToolInfo(userId, siteId, numOfAttempts);
    		}
    	}

    }

    public void incrementForumSynopticToolInfo(String userId, String siteId, int numOfAttempts) {
    	try {
    		SynopticMsgcntrManagerCover.incrementForumSynopticToolInfo(Arrays.asList(userId), siteId);
    	} catch (HibernateOptimisticLockingFailureException holfe) {

    		// failed, so wait and try again
    		try {
    			Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
    		} catch (InterruptedException e) {
    			log.error(e.getMessage(), e);
    		}

    		numOfAttempts--;

    		if (numOfAttempts <= 0) {
    			log.info("MessageForumsMessageManagerImpl: incrementForumSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
    			log.error(holfe.getMessage(), holfe);
    		} else {
    			log.info("MessageForumsMessageManagerImpl: incrementForumSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
    					+ numOfAttempts);
    			incrementForumSynopticToolInfo(userId, siteId, numOfAttempts);
    		}
    	}

    }

    public void decrementMessagesSynopticToolInfo(String userId, String siteId, int numOfAttempts) {
    	try {
    		SynopticMsgcntrManagerCover.decrementMessagesSynopticToolInfo(Arrays.asList(userId), siteId);
    	} catch (HibernateOptimisticLockingFailureException holfe) {

    		// failed, so wait and try again
    		try {
    			Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
    		} catch (InterruptedException e) {
    			log.error(e.getMessage(), e);
    		}

    		numOfAttempts--;

    		if (numOfAttempts <= 0) {
    			log.info("MessageForumsMessageManagerImpl: decrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
    			log.error(holfe.getMessage(), holfe);
    		} else {
    			log.info("MessageForumsMessageManagerImpl: decrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
    					+ numOfAttempts);
    			decrementMessagesSynopticToolInfo(userId, siteId, numOfAttempts);
    		}
    	}

    }

    public void incrementMessagesSynopticToolInfo(String userId, String siteId, int numOfAttempts) {
    	try {
    		SynopticMsgcntrManagerCover.incrementMessagesSynopticToolInfo(Arrays.asList(userId), siteId);
    	} catch (HibernateOptimisticLockingFailureException holfe) {

    		// failed, so wait and try again
    		try {
    			Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
    		} catch (InterruptedException e) {
    			log.error(e.getMessage(), e);
    		}

    		numOfAttempts--;

    		if (numOfAttempts <= 0) {
    			log.info("MessageForumsMessageManagerImpl: incrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
    			log.error(holfe.getMessage(), holfe);
    		} else {
    			log.info("MessageForumsMessageManagerImpl: incrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
    					+ numOfAttempts);
    			incrementMessagesSynopticToolInfo(userId, siteId, numOfAttempts);
    		}
    	}

    }
    
    public boolean isMessageReadForUser(final Long topicId, final Long messageId) {
        if (messageId == null || topicId == null) {
            log.error("getMessageById failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getMessageById executing with topicId: " + topicId + ", messageId: " + messageId);

        UnreadStatus status = findUnreadStatus(topicId, messageId);
        if (status == null) {
            return false; // not been saved yet, so it is unread
        }
        return status.getRead().booleanValue();        
    }

    public PrivateMessage createPrivateMessage() {
        PrivateMessage message = new PrivateMessageImpl();
        message.setUuid(getNextUuid());
        message.setTypeUuid(typeManager.getPrivateMessageAreaType());
        message.setCreated(new Date());
        message.setCreatedBy(getCurrentUser());
        message.setDraft(Boolean.FALSE);
        message.setHasAttachments(Boolean.FALSE);
        
        log.debug("message " + message.getUuid() + " created successfully");
        return message;        
    }

    public Message createDiscussionMessage() {
        return createMessage(typeManager.getDiscussionForumType());
    }

    public Message createOpenMessage() {
        return createMessage(typeManager.getOpenDiscussionForumType());
    }

    public Message createMessage(String typeId) {
        Message message = new MessageImpl();
        message.setUuid(getNextUuid());
        message.setTypeUuid(typeId);
        message.setCreated(new Date());
        message.setCreatedBy(getCurrentUser());
        message.setDraft(Boolean.FALSE);
        message.setHasAttachments(Boolean.FALSE);

        log.debug("message " + message.getUuid() + " created successfully");
        return message;        
    }

    public Attachment createAttachment() {
        Attachment attachment = new AttachmentImpl();
        attachment.setUuid(getNextUuid());
        attachment.setCreated(new Date());
        attachment.setCreatedBy(getCurrentUser());
        attachment.setModified(new Date());
        attachment.setModifiedBy(getCurrentUser());

        log.debug("attachment " + attachment.getUuid() + " created successfully");
        return attachment;        
    }

    public void saveMessage(Message message) {
    	saveMessage(message, true);
    }

    public void saveMessage(Message message, boolean logEvent) {
    	saveMessage(message, logEvent, toolManager.getCurrentTool().getId(), getCurrentUser(), getContextId());
    }
    
    public void saveMessage(Message message, boolean logEvent, boolean ignoreLockedTopicForum) {
        saveMessage(message, logEvent, toolManager.getCurrentTool().getId(), getCurrentUser(), getContextId(), ignoreLockedTopicForum);
    }
    
    public void saveMessage(Message message, boolean logEvent, String toolId, String userId, String contextId){
    	saveMessage(message, logEvent, toolId, userId, contextId, false);
    }
    
    public void saveMessage(Message message, boolean logEvent, String toolId, String userId, String contextId, boolean ignoreLockedTopicForum){
        boolean isNew = message.getId() == null;
        
        if (!ignoreLockedTopicForum && !(message instanceof PrivateMessage)){                  
          if (isForumOrTopicLocked(message.getTopic().getBaseForum().getId(), message.getTopic().getId())) {
              log.info("saveMessage executed [messageId: " + (isNew ? "new" : message.getId().toString()) + "] but forum is locked -- save aborted");
              throw new LockedException("Message could not be saved [messageId: " + (isNew ? "new" : message.getId().toString()) + "]");
          }
        }
        
        message.setModified(new Date());
        if(getCurrentUser()!=null){
        message.setModifiedBy(getCurrentUser());
        }
        if(message.getUuid() == null || message.getCreated() == null
        	|| message.getCreatedBy() == null || message.getModified() == null
        	|| message.getModifiedBy() == null || message.getTitle() == null 
        	|| message.getAuthor() == null || message.getHasAttachments() == null
        	|| message.getTypeUuid() == null 
        	|| message.getDraft() == null)
        {
        	log.error("null attribute(s) for saving message in MessageForumsMessageManagerImpl.saveMessage");
        }

        if (message.getNumReaders() == null)
        	message.setNumReaders(0);
        
        //MSGCNTR-448 if this is a top new top level message make sure the thread date is set
        if (logEvent) {
        	if (isNew && message.getDateThreadlastUpdated() == null) { 	                 
        		//we don't need to do this on non log events
        		message.setDateThreadlastUpdated(new Date()); 	                 
        		if (message.getInReplyTo() != null) {
        			if (message.getInReplyTo().getThreadId() != null) {
        				message.setThreadId(message.getInReplyTo().getThreadId());
        			} else {
        				message.setThreadId(message.getInReplyTo().getId());
        			}
        		}
        	}
        }


        getHibernateTemplate().saveOrUpdate(message);

        if (logEvent) {
        	if (isNew) {
        		if (isMessageFromForums(message))
        			eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_ADD, getEventMessage(message, toolId, userId, contextId), false));
        		else
        			eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_ADD, getEventMessage(message, toolId, userId, contextId), false));
        	} else {
        		if (isMessageFromForums(message))
        			eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_RESPONSE, getEventMessage(message, toolId, userId, contextId), false));
        		else
        			eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_RESPONSE, getEventMessage(message, toolId, userId, contextId), false));
        	}           
        }
        
        log.debug("message " + message.getId() + " saved successfully");
        
    }

    public void deleteMessage(Message message) {
        long id = message.getId().longValue();
        message.setInReplyTo(null);
        
        getHibernateTemplate().saveOrUpdate(message);
        
        try {
        	getSessionFactory().getCurrentSession().flush();
        } 
        catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
        
        if (isMessageFromForums(message))
        	eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_REMOVE, getEventMessage(message), false));
        else
        	eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_REMOVE, getEventMessage(message), false));

        try {
            getSessionFactory().getCurrentSession().evict(message);
        } catch (Exception e) {
            log.error("could not evict message: " + message.getId(), e);
        }
        
        Topic topic = message.getTopic();        
        topic.removeMessage(message);
        getHibernateTemplate().saveOrUpdate(topic);
		//getHibernateTemplate().delete(message);

        try {
            getSessionFactory().getCurrentSession().flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.debug("message " + id + " deleted successfully");
    }
    
    public Message getMessageById(final Long messageId) {        
        if (messageId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getMessageById executing with messageId: " + messageId);
        
        return (Message) getHibernateTemplate().get(MessageImpl.class, messageId);
    }   
    
    /**
     * @see org.sakaiproject.api.app.messageforums.MessageForumsMessageManager#getMessageByIdWithAttachments(java.lang.Long)
     */
    public Message getMessageByIdWithAttachments(final Long messageId){
      
      if (messageId == null) {
        throw new IllegalArgumentException("Null Argument");
       }

       log.debug("getMessageByIdWithAttachments executing with messageId: " + messageId);
        

      HibernateCallback<Message> hcb = session -> {
        Query q = session.getNamedQuery(QUERY_BY_MESSAGE_ID_WITH_ATTACHMENTS);
        q.setParameter("id", messageId, LongType.INSTANCE);
        return (Message) q.uniqueResult();
      };

      return getHibernateTemplate().execute(hcb);
    }
    
    public Attachment getAttachmentById(final Long attachmentId) {        
        if (attachmentId == null) {
            throw new IllegalArgumentException("Null Argument");
        }
        
        log.debug("getAttachmentById executing with attachmentId: " + attachmentId);
        
        return (Attachment) getHibernateTemplate().get(AttachmentImpl.class, attachmentId);
    }
    
    public void getChildMsgs(final Long messageId, List returnList)
    {
    	List tempList;

        HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_CHILD_MESSAGES);
            Query qOrdered = session.createQuery(q.getQueryString());

            qOrdered.setParameter("messageId", messageId, LongType.INSTANCE);

            return qOrdered.list();
        };
      
      tempList = getHibernateTemplate().execute(hcb);
      if(tempList != null)
      {
      	for(int i=0; i<tempList.size(); i++)
      	{
      		getChildMsgs(((Message)tempList.get(i)).getId(), returnList);
      		returnList.add((Message) tempList.get(i));
      	}
      }
    }
    
    /**
     * Will set the approved status on the given message
     */
    public void markMessageApproval(Long messageId, boolean approved)
    {
    	if (messageId == null) {
            log.error("markMessageApproval failed with messageId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("markMessageApproval executing with messageId: " + messageId);
        
        Message message = (Message) getMessageById(messageId);
        message.setApproved(Boolean.valueOf(approved));
        
        getHibernateTemplate().saveOrUpdate(message);
    }



    public void deleteMsgWithChild(final Long messageId)
    {
    	List thisList = new ArrayList();
    	getChildMsgs(messageId, thisList);
    	
    	for(int i=0; i<thisList.size(); i++)
    	{
    		Message delMessage = getMessageById(((Message)thisList.get(i)).getId());
    		deleteMessage(delMessage);
    	}

  		deleteMessage(getMessageById(messageId));
    }
    
    public List getFirstLevelChildMsgs(final Long messageId)
    {
        HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_CHILD_MESSAGES);
            Query qOrdered = session.createQuery(q.getQueryString());

            qOrdered.setParameter("messageId", messageId, LongType.INSTANCE);

            return qOrdered.list();
        };
      
      return getHibernateTemplate().execute(hcb);
    }

    public List sortMessageBySubject(Topic topic, boolean asc) {
        List list = topic.getMessages();
        if (asc) {
            Collections.sort(list, MessageImpl.SUBJECT_COMPARATOR);
        } else {
            Collections.sort(list, MessageImpl.SUBJECT_COMPARATOR_DESC);
        }
        topic.setMessages(list);
        return list;
    }

    public List sortMessageByAuthor(Topic topic, boolean asc) {
        List list = topic.getMessages();
        if (asc) {
            Collections.sort(list, MessageImpl.AUTHORED_BY_COMPARATOR);
        } else {
            Collections.sort(list, MessageImpl.AUTHORED_BY_COMPARATOR_DESC);
        }
        topic.setMessages(list);
        return list;
    }

    public List sortMessageByDate(Topic topic, boolean asc) {
        List list = topic.getMessages();
        if (asc) {
            Collections.sort(list, MessageImpl.DATE_COMPARATOR);
        } else {
            Collections.sort(list, MessageImpl.DATE_COMPARATOR_DESC);
        }
        topic.setMessages(list);
        return list;
    }
    
    public List sortMessageByDate(List list, boolean asc) {
        if (list == null || list.isEmpty())
        	return null;
        
        if (asc) {
            Collections.sort(list, MessageImpl.DATE_COMPARATOR);
        } else {
            Collections.sort(list, MessageImpl.DATE_COMPARATOR_DESC);
        }

        return list;
    }
    

    private boolean isForumOrTopicLocked(final Long forumId, final Long topicId) {
        if (forumId == null || topicId == null) {
            log.error("isForumLocked called with null arguments");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("isForumLocked executing with forumId: " + forumId + ":: topicId: " + topicId);

        HibernateCallback<Boolean> hcb = session -> {
            Query q = session.getNamedQuery("findForumLockedAttribute");
            q.setParameter("id", forumId, LongType.INSTANCE);
            return (Boolean) q.uniqueResult();
        };

        HibernateCallback<Boolean> hcb2 = session -> {
            Query q = session.getNamedQuery("findTopicLockedAttribute");
            q.setParameter("id", topicId, LongType.INSTANCE);
            return (Boolean) q.uniqueResult();
        };
        
        return getHibernateTemplate().execute(hcb) || getHibernateTemplate().execute(hcb2);
    }
    
    // helpers
    
    private String getCurrentUser() {        
        if (TestUtil.isRunningTests()) {
            return "test-user";
        }
        return sessionManager.getCurrentSessionUserId();
    }
    
    private String getNextUuid() {        
        return idManager.createUuid();
    }

    private String getEventMessage(Object object) {
    	return getEventMessage(object, toolManager.getCurrentTool().getId(), getCurrentUser(), getContextId());
    }
    
    private String getEventMessage(Object object, String toolId, String userId, String contextId) {
    	String eventMessagePrefix = "";
    	
    		if (toolId.equals(DiscussionForumService.MESSAGE_CENTER_ID))
    			eventMessagePrefix = "/messagesAndForums/site/";
    		else if (toolId.equals(DiscussionForumService.MESSAGES_TOOL_ID))
    			eventMessagePrefix = "/messages/site/";
    		else
    			eventMessagePrefix = "/forums/site/";
    	
    	return eventMessagePrefix + contextId + "/" + object.toString() + "/" + userId;
    }

        
    public List getAllRelatedMsgs(final Long messageId)
    {
    	Message rootMsg = getMessageById(messageId); 
    	while(rootMsg.getInReplyTo() != null)
    	{
    		rootMsg = rootMsg.getInReplyTo();
    	}
    	List childList = new ArrayList();
    	getChildMsgs(rootMsg.getId(), childList);
    	List returnList = new ArrayList();
    	returnList.add(rootMsg);
    	for(int i=0; i<childList.size(); i++)
    	{
    		returnList.add((Message)childList.get(i));
    	}

    	return returnList;
    }
    
    /**
     * 
     * @param topicId
     * @param searchText
     * @return
     */
    
    public List findPvtMsgsBySearchText(final String typeUuid, final String searchText, 
          final Date searchFromDate, final Date searchToDate, final boolean searchByText,
          final boolean searchByAuthor, final boolean searchByBody, final boolean searchByLabel, final boolean searchByDate) {

      log.debug("findPvtMsgsBySearchText executing with searchText: " + searchText);

      HibernateCallback<List> hcb = session -> {
          Query q = session.getNamedQuery("findPvtMsgsBySearchText");
          q.setParameter("searchText", "%" + searchText + "%");
          q.setParameter("searchByText", convertBooleanToInteger(searchByText));
          q.setParameter("searchByAuthor", convertBooleanToInteger(searchByAuthor));
          q.setParameter("searchByBody", convertBooleanToInteger(searchByBody));
          q.setParameter("searchByLabel", convertBooleanToInteger(searchByLabel));
          q.setParameter("searchByDate", convertBooleanToInteger(searchByDate));
          q.setParameter("searchFromDate", (searchFromDate == null) ? new Date(0) : searchFromDate);
          q.setParameter("searchToDate", (searchToDate == null) ? new Date(System.currentTimeMillis()) : searchToDate);
          q.setParameter("userId", getCurrentUser());
          q.setParameter("contextId", toolManager.getCurrentPlacement().getContext());
          q.setParameter("typeUuid", typeUuid);
          return q.list();
      };

      return getHibernateTemplate().execute(hcb);
  }
    
    private Integer convertBooleanToInteger(boolean value) {
       Integer retVal = (Boolean.TRUE.equals(value)) ? 1 : 0;
       return Integer.valueOf(retVal);
    }
    
    private String getContextId() {
      if (TestUtil.isRunningTests()) {
          return "test-context";
      }
      Placement placement = toolManager.getCurrentPlacement();
      String presentSiteId = placement.getContext();
      return presentSiteId;
  }
    
  public String getAttachmentUrl(String id)
  {
  	try
  	{
		return contentHostingService.getResource(id).getUrl(false);
  	}
  	catch(Exception e)
  	{
  		log.error("MessageForumsMessageManagerImpl.getAttachmentUrl" + e, e);
  	}
  	return null;
  }
  
  public String getAttachmentRelativeUrl(String id) {
      try
      {
          return contentHostingService.getResource(id).getUrl(true);
      }
      catch(Exception e)
      {
          log.error("MessageForumsMessageManagerImpl.getAttachmentUrl" + e, e);
      }
      return null;

  }

	/**
	 * Returns true if the tool with the id passed in exists in the
	 * current site.
	 * 
	 * @param toolId
	 * 			The tool id to search for.
	 * 
	 * @return
	 * 			TRUE if tool exists, FALSE otherwise.
	 */
	public boolean currentToolMatch(String toolId) {
		String curToolId = toolManager.getCurrentTool().getId();
		
		if (curToolId.equals(MESSAGECENTER_HELPER_TOOL_ID)) {
			curToolId = toolManager.getCurrentPlacement().getTool().getId();
		}

		if (toolId.equals(curToolId)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Return TRUE if tool with id passed in exists in site passed in
	 * FALSE otherwise.
	 * 
	 * @param thisSite
	 * 			Site object to check
	 * @param toolId
	 * 			Tool id to be checked
	 * 
	 * @return
	 */
	public boolean isToolInSite(String siteId, String toolId) {
		Site thisSite;
		try {
			thisSite = siteService.getSite(siteId);
			
			Collection toolsInSite = thisSite.getTools(toolId);

			return ! toolsInSite.isEmpty();
		} 
		catch (IdUnusedException e) {
			// Weirdness - should not happen
			log.error("IdUnusedException attempting to get site for id " + siteId + " to check if tool " 
							+ "with id " + toolId + " is in it.", e);
		}
		
		return false;
	}

	@Override
	public Map<Long, Boolean> getReadStatusForMessagesWithId(final List<Long> msgIds, final String userId) {
		Map<Long, Boolean> statusMap = new HashMap<>();
		if( msgIds != null && msgIds.size() > 0)
		{
			HibernateCallback<List> hcb = session -> {
                Query q = session.getNamedQuery(QUERY_READ_STATUS_WITH_MSGS_USER);
                q.setParameter("userId", userId, StringType.INSTANCE);
                q.setParameterList("msgIds", msgIds);
                return q.list();
            };

            msgIds.forEach(i -> statusMap.put(i, Boolean.FALSE));

			List statusList = getHibernateTemplate().execute(hcb);
			if(statusList != null)
			{
				for(int i=0; i<statusList.size(); i++)
				{
					UnreadStatus status = (UnreadStatus) statusList.get(i);
					if(status != null)
					{
						statusMap.put(status.getMessageId(), status.getRead());
					}
				}
			}
		}
		return statusMap;
	}
	
	public List getPendingMsgsInSiteByMembership(final List membershipList)
	{   	
		if (membershipList == null) {
            log.error("getPendingMsgsInSiteByMembership failed with membershipList: null");
            throw new IllegalArgumentException("Null Argument");
        }
		
		// First, check by permissionLevel (custom permissions)
		HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_FIND_PENDING_MSGS_BY_CONTEXT_AND_USER_AND_PERMISSION_LEVEL);
            q.setParameter("contextId", getContextId(), StringType.INSTANCE);
            q.setParameterList("membershipList", membershipList);

            return q.list();
        };
		
		Message tempMsg = null;
        Set resultSet = new HashSet();      
        List temp = getHibernateTemplate().execute(hcb);
        for (Iterator i = temp.iterator(); i.hasNext();)
        {
          Object[] results = (Object[]) i.next();        
              
          if (results != null) {
            if (results[0] instanceof Message) {
              tempMsg = (Message)results[0];
              tempMsg.setTopic((Topic)results[1]); 
              tempMsg.getTopic().setBaseForum((BaseForum)results[2]);
            }
            resultSet.add(tempMsg);
          }
        }
        
        // Second, check by PermissionLevelName (non-custom permissions)
        HibernateCallback<List> hcb2 = session -> {
            Query q = session.getNamedQuery(QUERY_FIND_PENDING_MSGS_BY_CONTEXT_AND_USER_AND_PERMISSION_LEVEL_NAME);
            q.setParameter("contextId", getContextId(), StringType.INSTANCE);
            q.setParameterList("membershipList", membershipList);
            q.setParameter("customTypeUuid", typeManager.getCustomLevelType(), StringType.INSTANCE);

            return q.list();
        };
		   
        temp = getHibernateTemplate().execute(hcb2);
        for (Iterator i = temp.iterator(); i.hasNext();)
        {
          Object[] results = (Object[]) i.next();        
              
          if (results != null) {
            if (results[0] instanceof Message) {
              tempMsg = (Message)results[0];
              tempMsg.setTopic((Topic)results[1]); 
              tempMsg.getTopic().setBaseForum((BaseForum)results[2]);
            }
            resultSet.add(tempMsg);
          }
        }
        
        return Util.setToList(resultSet); 
	}
	
	public List getPendingMsgsInTopic(final Long topicId)
	{
		if (topicId == null) {
            log.error("getPendingMsgsInTopic failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getPendingMsgsInTopic executing with topicId: " + topicId);

        HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_FIND_PENDING_MSGS_BY_TOPICID);
            q.setParameter("topicId", topicId, LongType.INSTANCE);
            return q.list();
        };

        Message tempMsg = null;
        Set resultSet = new HashSet();      
        List temp = getHibernateTemplate().execute(hcb);
        for (Iterator i = temp.iterator(); i.hasNext();)
        {
          Object[] results = (Object[]) i.next();        
              
          if (results != null) {
            if (results[0] instanceof Message) {
              tempMsg = (Message)results[0];
              tempMsg.setTopic((Topic)results[1]); 
              tempMsg.getTopic().setBaseForum((BaseForum)results[2]);
            }
            resultSet.add(tempMsg);
          }
        }
        return Util.setToList(resultSet); 
	}
	
	public List<Message> getAllMessagesInSite(final String siteId) {
        log.debug("getAllMessagesInSite executing with siteId: " + siteId);

        HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery("findDiscussionForumMessagesInSite");
            q.setParameter("contextId", siteId, StringType.INSTANCE);
            return q.list();
        };

        Message tempMsg = null;
        Set resultSet = new HashSet();      
        List temp = getHibernateTemplate().execute(hcb);
        log.debug("got an initial list of " + temp.size());
        for (Iterator i = temp.iterator(); i.hasNext();)
        {
          Object[] results = (Object[]) i.next();        
              
          if (results != null) {
            if (results[0] instanceof Message) {
              tempMsg = (Message)results[0];
              tempMsg.setTopic((Topic)results[1]); 
              tempMsg.getTopic().setBaseForum((BaseForum)results[2]);
            }
            resultSet.add(tempMsg);
          }
        }
        
        log.debug("about to return");
        return Util.setToList(resultSet); 
	}
	

	public void saveMessageMoveHistory(Long messageId, Long desttopicId, Long sourceTopicId, boolean checkReminder){
		if (messageId == null || desttopicId == null || sourceTopicId == null) {
			log.error("saveMessageMoveHistory failed with desttopicId: " + desttopicId + ", messageId: " + messageId + ", sourceTopicId: " + sourceTopicId);
			throw new IllegalArgumentException("Null Argument");
}

		if (log.isDebugEnabled()) log.debug("saveMessageMoveHistory executing with desttopicId: " + desttopicId + ", messageId: " + messageId + ", sourceTopicId: " + sourceTopicId);


		List moved_history = null;

		moved_history = this.findMovedHistoryByMessageId(messageId);

		// if moving back to the original topic,  set reminder to false, otherwise the original topic will show a Move reminder.
			if (log.isDebugEnabled()) log.debug("saveMessageMoveHistory (moved_messages size  " + moved_history.size()  );
			for (Iterator histIter = moved_history.iterator(); histIter.hasNext();) {
				MessageMoveHistory hist = (MessageMoveHistory) histIter.next();
				if (log.isDebugEnabled()) log.debug("moved message ids = " +  hist.getId()  + "  from : " + hist.getFromTopicId()  + "   topic : " +  hist.getToTopicId() );
				if (hist.getFromTopicId().equals(desttopicId)){
					hist.setReminder(false);
					hist.setModified(new Date());
					hist.setModifiedBy(getCurrentUser());
 					getHibernateTemplate().update(hist);
				}
			}

		MessageMoveHistory mhist = new MessageMoveHistoryImpl ();

		mhist.setToTopicId(desttopicId);
		mhist.setMessageId(messageId);
		mhist.setFromTopicId(sourceTopicId);
		mhist.setReminder(checkReminder);
		mhist.setUuid(getNextUuid());
		mhist.setCreated(new Date());
		mhist.setCreatedBy(getCurrentUser());
		mhist.setModified(new Date());
		mhist.setModifiedBy(getCurrentUser());

		getHibernateTemplate().saveOrUpdate(mhist);


	}

	public List findMovedMessagesByTopicId(final Long topicId) {
		if (topicId == null) {
			log.error("findMovedMessagesByTopicId failed with topicId: " + topicId);
			throw new IllegalArgumentException("Null Argument");
		}

		if (log.isDebugEnabled()) log.debug("findMovedMessagesByTopicId executing with topicId: " + topicId);

		HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_MOVED_MESSAGES_BY_TOPICID);
            q.setParameter("topicId", topicId, LongType.INSTANCE);
            return q.list();
        };

		return getHibernateTemplate().execute(hcb);
	}

	public List getRecentDiscussionForumThreadsByTopicIds(final List<Long> topicIds, final int numberOfMessages) {
		if (topicIds.isEmpty())
		{
			return new ArrayList<Object[]>();
		}
		if (log.isDebugEnabled())
		{
			log.debug("getRecentDiscussionForumThreadsByTopicIds executing for list of size: " + topicIds.size());
		}
		HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery("findRecentDiscussionForumThreadsByTopicIds");
            q.setParameterList("topicIds", topicIds);
            q.setMaxResults(numberOfMessages);
            return q.list();
        };

		Message tempMsg = null;
		Set resultSet = new HashSet();
		List temp = getHibernateTemplate().execute(hcb);
		log.debug("got an initial list of " + temp.size());
		for (Iterator i = temp.iterator(); i.hasNext();)
		{
			Object[] results = (Object[]) i.next();

			if (results != null) {
				if (results[0] instanceof Message) {
					tempMsg = (Message)results[0];
					tempMsg.setTopic((Topic)results[1]);
					tempMsg.getTopic().setBaseForum((BaseForum)results[2]);
					getHibernateTemplate().initialize(tempMsg.getAttachments());
				}
				resultSet.add(tempMsg);
			}
		}

		log.debug("about to return");
		return Util.setToList(resultSet);
	}

	public List findMovedHistoryByMessageId(final Long messageid){
		if (messageid == null) {
			log.error("findMovedHistoryByMessageId failed with messageid: " + messageid);
			throw new IllegalArgumentException("Null Argument");
		}

		if (log.isDebugEnabled()) log.debug("findMovedHistoryByMessageId executing with messageid: " + messageid);

		HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_MOVED_HISTORY_BY_MESSAGEID);
            q.setParameter("messageId", messageid, LongType.INSTANCE);
            return q.list();
        };

		return getHibernateTemplate().execute(hcb);

	}
	   
}
