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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums;

import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.UnreadStatus;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AttachmentImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.UnreadStatusImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.Util;
import org.sakaiproject.component.app.messageforums.exception.LockedException;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.ToolManager;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class MessageForumsMessageManagerImpl extends HibernateDaoSupport implements MessageForumsMessageManager {

    private static final Log LOG = LogFactory.getLog(MessageForumsMessageManagerImpl.class);    

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
    private static final String QUERY_FIND_PENDING_MSGS_BY_CONTEXT_AND_USER = "findAllPendingMsgsByContextByMembership";
    private static final String QUERY_FIND_PENDING_MSGS_BY_TOPICID = "findPendingMsgsByTopicId";
    private static final String QUERY_UNDELETED_MSG_BY_TOPIC_ID = "findUndeletedMessagesByTopicId";
    //private static final String ID = "id";

    private static final String MESSAGECENTER_HELPER_TOOL_ID = "sakai.messageforums.helper";

    private IdManager idManager;                      

    private MessageForumsTypeManager typeManager;

    private SessionManager sessionManager;

    private EventTrackingService eventTrackingService;
    
    private ContentHostingService contentHostingService;

    public void init() {
       LOG.info("init()");
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
            LOG.error("findDiscussionForumMessageCountsForTopicsWithMissingPermsForAllSites failed with null site list.");
            throw new IllegalArgumentException("Null Argument");
    	}	
        
    	HibernateCallback hcb = new HibernateCallback() {
               public Object doInHibernate(Session session) throws HibernateException, SQLException {
                   Query q = session.getNamedQuery("findDiscussionForumMessageCountsForTopicsWithMissingPermsForAllSites");
                    q.setParameterList("siteList", siteList);
                    q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
                   return q.list();
               }
    	};

        return (List) getHibernateTemplate().execute(hcb);
    }
    
    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by site for sites with
     * 		Forum topics that don't have membership items in the db
     */
    public List<Object []> findDiscussionForumReadMessageCountsForTopicsWithMissingPermsForAllSites(final List<String> siteList) {
    	if (siteList == null) {
            LOG.error("findDiscussionForumReadMessageCountsForTopicsWithMissingPermsForAllSites failed with null site list.");
            throw new IllegalArgumentException("Null Argument");
    	}	
        
    	HibernateCallback hcb = new HibernateCallback() {
               public Object doInHibernate(Session session) throws HibernateException, SQLException {
                   Query q = session.getNamedQuery("findDiscussionForumReadMessageCountsForTopicsWithMissingPermsForAllSites");
                    q.setParameterList("siteList", siteList);
                    q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
                   return q.list();
               }
    	};

        return (List) getHibernateTemplate().execute(hcb);
    }
    
    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by site
      */
    public List findDiscussionForumMessageCountsForAllSites(final List siteList, final List roleList) {
    	if (siteList == null) {
            LOG.error("findDiscussionForumMessageCountsForAllSites failed with null site list.");
            throw new IllegalArgumentException("Null Argument");
    	}	
        
    	HibernateCallback hcb = new HibernateCallback() {
               public Object doInHibernate(Session session) throws HibernateException, SQLException {
                   Query q = session.getNamedQuery("findDiscussionForumMessageCountsForAllSites");
                    q.setParameterList("siteList", siteList);
                    q.setParameterList("roleList", roleList);
                    q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
                    q.setParameter("customTypeUuid", typeManager.getCustomLevelType(), Hibernate.STRING);
                   return q.list();
               }
    	};

        return (List) getHibernateTemplate().execute(hcb);
    }

    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of read discussion forum messages grouped by site
     */
    public List findDiscussionForumReadMessageCountsForAllSites(final List siteList, final List roleList) {
        
    	HibernateCallback hcb = new HibernateCallback() {
               public Object doInHibernate(Session session) throws HibernateException, SQLException {
                   Query q = session.getNamedQuery("findDiscussionForumReadMessageCountsForAllSites");
                   q.setParameterList("siteList", siteList);
                   q.setParameterList("roleList", roleList);
                   q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
                   q.setParameter("customTypeUuid", typeManager.getCustomLevelType(), Hibernate.STRING);
                   return q.list();
               }
    	};
        
        return (List) getHibernateTemplate().execute(hcb);
    }

    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by topics within a site
     * 		Used by sites that are grouped
     */
    public List findDiscussionForumMessageCountsForGroupedSitesByTopic(final List siteList, final List roleList) {
        
    	HibernateCallback hcb = new HibernateCallback() {
               public Object doInHibernate(Session session) throws HibernateException, SQLException {
                   Query q = session.getNamedQuery("findDiscussionForumMessageCountsForGroupedSitesByTopic");
                   q.setParameterList("siteList", siteList);
                   q.setParameterList("roleList", roleList);
                   q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
                   q.setParameter("customTypeUuid", typeManager.getCustomLevelType(), Hibernate.STRING);
                   return q.list();
               }
    	};
        
        return (List) getHibernateTemplate().execute(hcb);
    }

    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by topics within a site
     * 		Used by sites that are grouped
     */
    public List findDiscussionForumReadMessageCountsForGroupedSitesByTopic(final List siteList, final List roleList) {
        
    	HibernateCallback hcb = new HibernateCallback() {
               public Object doInHibernate(Session session) throws HibernateException, SQLException {
                   Query q = session.getNamedQuery("findDiscussionForumReadMessageCountsForGroupedSitesByTopic");
                   q.setParameterList("siteList", siteList);
                   q.setParameterList("roleList", roleList);
                   	q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
                   	q.setParameter("customTypeUuid", typeManager.getCustomLevelType(), Hibernate.STRING);
                   return q.list();
               }
    	};
        
        return (List) getHibernateTemplate().execute(hcb);
    }

    /**
     * FOR STATISTICS TOOL:
     * 		Returns the number of read messages by topic for specified user
     */
    
    public int findAuhtoredMessageCountByTopicIdByUserId(final Long topicId, final String userId){
    	if (topicId == null || userId == null) {
            LOG.error("findAuthoredMessageCountByTopicIdByUserId failed with topicId: " + topicId + 
            			" and userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findAuthoredMessageCountByTopicIdByUserId executing with topicId: " + topicId + 
        				" and userId: " + userId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_COUNT_BY_AUTHORED);
                q.setParameter("topicId", topicId, Hibernate.LONG);
                q.setParameter("userId", userId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return ((Integer) getHibernateTemplate().execute(hcb)).intValue(); 
    }
    
    public int findAuthoredMessageCountForStudent(final String userId) {
    	if (userId == null) {
    		LOG.error("findAuthoredMessageCountForStudentInSite failed with a null userId");
    		throw new IllegalArgumentException("userId cannot be null");
    	}
    	
    	if (LOG.isDebugEnabled()) LOG.debug("findAuthoredMessageCountForStudentInSite executing with userId: " + userId);
    	
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery("findAuthoredMessageCountForStudentInSite");
                q.setParameter("contextId", getContextId(), Hibernate.STRING);
                q.setParameter("userId", userId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return ((Integer) getHibernateTemplate().execute(hcb)).intValue();     	
    }
    
    /*
     * (non-Javadoc)
     * @see org.sakaiproject.api.app.messageforums.MessageForumsMessageManager#findAuthoredMessagesForStudent(java.lang.String)
     */
    public List<Message> findAuthoredMessagesForStudent(final String studentId) {
      if (LOG.isDebugEnabled()) LOG.debug("findReadMessagesForCurrentStudent()");
      
      HibernateCallback hcb = new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            Query q = session.getNamedQuery("findAuthoredMessagesForStudent");
            q.setParameter("contextId", getContextId(), Hibernate.STRING);
            q.setParameter("userId", studentId, Hibernate.STRING);
            return q.list();
        }
      };
      
      return (List)getHibernateTemplate().execute(hcb);
    }
    
    public List<Object[]> findAuthoredMessageCountForAllStudents() {
    	if (LOG.isDebugEnabled()) LOG.debug("findAuthoredMessageCountForAllStudents executing");
    	
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery("findAuthoredMessageCountForAllStudents");
                q.setParameter("contextId", getContextId(), Hibernate.STRING);
                return q.list();
            }
        };

        return (List)getHibernateTemplate().execute(hcb);        
    }
    
    public int findReadMessageCountByTopicIdByUserId(final Long topicId, final String userId) {
        if (topicId == null || userId == null) {
            LOG.error("findReadMessageCountByTopicIdByUserId failed with topicId: " + topicId + 
            			" and userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findReadMessageCountByTopicIdByUserId executing with topicId: " + topicId + 
        				" and userId: " + userId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_COUNT_BY_READ);
                q.setParameter("topicId", topicId, Hibernate.LONG);
                q.setParameter("userId", userId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return ((Integer) getHibernateTemplate().execute(hcb)).intValue();        
    }
    
    public int findReadMessageCountForStudent(final String userId) {
    	if (userId == null) {
    		LOG.error("findReadMessageCountForStudent failed with null userId");
    		throw new IllegalArgumentException("userId cannot be null");
    	}
    	
    	if (LOG.isDebugEnabled()) LOG.debug("findReadMessageCountForStudent executing with userId: " + userId);
    	
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery("findReadMessageCountForStudent");
                q.setParameter("contextId", getContextId(), Hibernate.STRING);
                q.setParameter("userId", userId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return ((Integer) getHibernateTemplate().execute(hcb)).intValue();        
    }
    
    /*
     * (non-Javadoc)
     * @see org.sakaiproject.api.app.messageforums.MessageForumsMessageManager#findReadMessagesForStudent()
     */
    public List<Message> findReadMessagesForStudent(final String studentId) {
      if (LOG.isDebugEnabled()) LOG.debug("findReadMessagesForCurrentStudent()");
      
      HibernateCallback hcb = new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            Query q = session.getNamedQuery("findReadMessagesForStudent");
            q.setParameter("contextId", getContextId(), Hibernate.STRING);
            q.setParameter("userId", studentId, Hibernate.STRING);
            return q.list();
        }
      };
      
      return (List)getHibernateTemplate().execute(hcb);
    }
    
    public List<Object[]> findReadMessageCountForAllStudents() {
    	if (LOG.isDebugEnabled()) LOG.debug("findReadMessageCountForAllStudentsInSite executing");
    	
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery("findReadMessageCountForAllStudents");
                q.setParameter("contextId", getContextId(), Hibernate.STRING);
                return q.list();
            }
        };

        return (List)getHibernateTemplate().execute(hcb);        
    }
    
    /**
     * Returns count of all messages in a topic that have been approved or were authored by given user
     */
    public int findViewableMessageCountByTopicIdByUserId(final Long topicId, final String userId) {
        if (topicId == null || userId == null) {
            LOG.error("findViewableMessageCountByTopicIdByUserId failed with topicId: " + topicId + 
            			" and userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findViewableMessageCountByTopicIdByUserId executing with topicId: " + topicId + 
        				" and userId: " + userId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_COUNT_VIEWABLE_BY_TOPIC_ID);
                q.setParameter("topicId", topicId, Hibernate.LONG);
                q.setParameter("userId", userId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return ((Integer) getHibernateTemplate().execute(hcb)).intValue();        
    }
    
    /**
     * Returns count of all msgs in a topic that have been approved or were authored by curr user
     */
    public int findViewableMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            LOG.error("findViewableMessageCountByTopicId failed with topicId: " + topicId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findViewableMessageCountByTopicId executing with topicId: " + topicId);

        return findViewableMessageCountByTopicIdByUserId(topicId, getCurrentUser());
    }

   public int findUnreadMessageCountByTopicIdByUserId(final Long topicId, final String userId){
	   if (topicId == null || userId == null) {
           LOG.error("findUnreadMessageCountByTopicIdByUserId failed with topicId: " + topicId + 
        		   		" and userId: " + userId);
           throw new IllegalArgumentException("Null Argument");
       }

       LOG.debug("findUnreadMessageCountByTopicIdByUserId executing with topicId: " + topicId);

       return findMessageCountByTopicId(topicId) - findReadMessageCountByTopicIdByUserId(topicId, userId);
   }
    
   public int findUnreadMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            LOG.error("findUnreadMessageCountByTopicId failed with topicId: " + topicId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findUnreadMessageCountByTopicId executing with topicId: " + topicId);

        return findMessageCountByTopicId(topicId) - findReadMessageCountByTopicId(topicId);
    }
   
   /**
    * Returns count of all unread msgs for given user that have been approved or
    * were authored by user
    */
   public int findUnreadViewableMessageCountByTopicIdByUserId(final Long topicId, final String userId) {
       if (topicId == null) {
           LOG.error("findUnreadViewableMessageCountByTopicIdByUserId failed with topicId: " + topicId + " and userid: " + userId);
           throw new IllegalArgumentException("Null Argument");
       }

       LOG.debug("findUnreadViewableMessageCountByTopicIdByUserId executing with topicId: " + topicId + " userId: " + userId);

       return findViewableMessageCountByTopicIdByUserId(topicId, userId) - findReadViewableMessageCountByTopicIdByUserId(topicId, userId);
   }
   
   /**
    * Returns count of all unread msgs for current user that have been approved or
    * were authored by current user
    */
   public int findUnreadViewableMessageCountByTopicId(final Long topicId) {
       if (topicId == null) {
           LOG.error("findUnreadViewableMessageCountByTopicId failed with topicId: " + topicId);
           throw new IllegalArgumentException("Null Argument");
       }

       LOG.debug("findUnreadViewableMessageCountByTopicId executing with topicId: " + topicId);

       return findUnreadViewableMessageCountByTopicIdByUserId(topicId, getCurrentUser());
   }
    
    public int findReadMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            LOG.error("findReadMessageCountByTopicId failed with topicId: " + topicId);
            throw new IllegalArgumentException("Null Argument");
        }

        return findReadMessageCountByTopicIdByUserId(topicId, getCurrentUser());
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
            LOG.error("findReadViewableMessageCountByTopicIdByUserId failed with topicId: " + topicId + 
            			" and userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findReadViewableMessageCountByTopicIdByUserId executing with topicId: " + topicId + 
        				" and userId: " + userId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_COUNT_READ_VIEWABLE_BY_TOPIC_ID);
                q.setParameter("topicId", topicId, Hibernate.LONG);
                q.setParameter("userId", userId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return ((Integer) getHibernateTemplate().execute(hcb)).intValue();   
    }
    
    /**
     * Returns count of all read msgs for current user that have been approved or
     * were authored by user
     * @param topicId
     * @return
     */
    public int findReadViewableMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            LOG.error("findReadViewableMessageCountByTopicId failed with topicId: " + topicId);
            throw new IllegalArgumentException("Null Argument");
        }

        return findReadViewableMessageCountByTopicIdByUserId(topicId, getCurrentUser());
    }
    
    public List findMessagesByTopicId(final Long topicId) {
        if (topicId == null) {
            LOG.error("findMessagesByTopicId failed with topicId: " + topicId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findMessagesByTopicId executing with topicId: " + topicId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_TOPIC_ID);
                q.setParameter("topicId", topicId, Hibernate.LONG);
                return q.list();
            }
        };

        return (List) getHibernateTemplate().execute(hcb);        
    }
    
    public List findUndeletedMessagesByTopicId(final Long topicId) {
        if (topicId == null) {
            LOG.error("findUndeletedMessagesByTopicId failed with topicId: " + topicId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findUndeletedMessagesByTopicId executing with topicId: " + topicId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_UNDELETED_MSG_BY_TOPIC_ID);
                q.setParameter("topicId", topicId, Hibernate.LONG);
                return q.list();
            }
        };

        return (List) getHibernateTemplate().execute(hcb);        
    }
    
    public int findMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            LOG.error("findMessageCountByTopicId failed with topicId: " + topicId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findMessageCountByTopicId executing with topicId: " + topicId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery("findMessageCountByTopicId");
                q.setParameter("topicId", topicId, Hibernate.LONG);
                return q.uniqueResult();
            }
        };

        return ((Integer) getHibernateTemplate().execute(hcb)).intValue();        
    }
    
    /*
    +     * (non-Javadoc)
    +     * @see org.sakaiproject.api.app.messageforums.MessageForumsForumManager#findMessageCountsForMainPage(java.util.List)
    +     */
    public List<Object[]> findMessageCountsForMainPage(final Collection<Long> topicIds) {
    	// have to manually check to make sure the Collection isn't empty to prevent Hibernate from 
    	// producing the "unexpected end of subtree" error.  (SAKAI-399/357 [local UDayton] & SAK-16848)
    	if (topicIds.isEmpty()) return new ArrayList<Object[]>();

    	HibernateCallback hcb = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException, SQLException {
    			Query q = session.getNamedQuery(QUERY_MESSAGE_COUNTS_FOR_MAIN_PAGE);
    			q.setParameterList("topicIds", topicIds);
    			return q.list();
    		}
    	};

    	return (List)getHibernateTemplate().execute(hcb);
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.api.app.messageforums.MessageForumsMessageManager#findReadMessageCountsForMainPage(java.util.Collection)
     */
    public List<Object[]> findReadMessageCountsForMainPage(final Collection<Long> topicIds) {
    	// have to manually check to make sure the Collection isn't empty to prevent Hibernate from 
    	// producing the "unexpected end of subtree" error.  (SAKAI-399/357 [local UDayton] & SAK-16848)
    	if (topicIds.isEmpty()) return new ArrayList<Object[]>();

    	HibernateCallback hcb = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException, SQLException {
    			Query q = session.getNamedQuery(QUERY_READ_MESSAGE_COUNTS_FOR_MAIN_PAGE);
    			q.setParameterList("topicIds", topicIds);
    			q.setParameter("userId", getCurrentUser());
    			return q.list();
    		}
    	};

    	return (List)getHibernateTemplate().execute(hcb);
    }



    public int findMessageCountTotal() {
    	HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery("findMessageCountTotal");
                q.setParameter("contextId", getContextId(), Hibernate.STRING);
                return q.uniqueResult();
            }
    	};
    	
    	return ((Integer)getHibernateTemplate().execute(hcb)).intValue();
    }
    
    public UnreadStatus findUnreadStatusByUserId(final Long topicId, final Long messageId, final String userId){
    	if (messageId == null || topicId == null || userId == null) {
            LOG.error("findUnreadStatusByUserId failed with topicId: " + topicId + ", messageId: " + messageId
            		+ ", userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findUnreadStatus executing with topicId: " + topicId + ", messageId: " + messageId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_UNREAD_STATUS);
                q.setParameter("topicId", topicId, Hibernate.LONG);
                q.setParameter("messageId", messageId, Hibernate.LONG);
                q.setParameter("userId", userId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (UnreadStatus) getHibernateTemplate().execute(hcb);
    }
    
    public UnreadStatus findUnreadStatus(final Long topicId, final Long messageId) {
        if (messageId == null || topicId == null) {
            LOG.error("findUnreadStatus failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        return findUnreadStatusByUserId(topicId, messageId, getCurrentUser());       
    }

    public void deleteUnreadStatus(Long topicId, Long messageId) {
        if (messageId == null || topicId == null) {
            LOG.error("deleteUnreadStatus failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("deleteUnreadStatus executing with topicId: " + topicId + ", messageId: " + messageId);

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
            LOG.error("markMessageReadForUser failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("markMessageReadForUser executing with topicId: " + topicId + ", messageId: " + messageId);

        markMessageReadForUser(topicId, messageId, read, getCurrentUser());
    }
    
    public void markMessageReadForUser(Long topicId, Long messageId, boolean read, String userId)
    {
    	// to only add to event log if not read
    	boolean trulyUnread;
    	
    	if (messageId == null || topicId == null || userId == null) {
            LOG.error("markMessageReadForUser failed with topicId: " + topicId + ", messageId: " + messageId + ", userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("markMessageReadForUser executing with topicId: " + topicId + ", messageId: " + messageId);

        UnreadStatus status = findUnreadStatusByUserId(topicId, messageId, userId);
        if (status == null) {
            status = new UnreadStatusImpl();
            trulyUnread = true;
        }
        else {
        	trulyUnread = status.getRead().booleanValue();
        	trulyUnread = !trulyUnread;
        }
        
        status.setTopicId(topicId);
        status.setMessageId(messageId);
        status.setUserId(userId);
        status.setRead(new Boolean(read));

        if (trulyUnread) {
        	Message message = (Message) getMessageById(messageId);
        	if (isMessageFromForums(message))
        		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_READ, getEventMessage(message), false));
        	else
        		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_READ, getEventMessage(message), false));
        }
        	
        getHibernateTemplate().saveOrUpdate(status);
    }
    
    public boolean isMessageReadForUser(final Long topicId, final Long messageId) {
        if (messageId == null || topicId == null) {
            LOG.error("getMessageById failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getMessageById executing with topicId: " + topicId + ", messageId: " + messageId);

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
        
        LOG.info("message " + message.getUuid() + " created successfully");
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

        LOG.info("message " + message.getUuid() + " created successfully");
        return message;        
    }

    public Attachment createAttachment() {
        Attachment attachment = new AttachmentImpl();
        attachment.setUuid(getNextUuid());
        attachment.setCreated(new Date());
        attachment.setCreatedBy(getCurrentUser());
        attachment.setModified(new Date());
        attachment.setModifiedBy(getCurrentUser());

        LOG.info("attachment " + attachment.getUuid() + " created successfully");
        return attachment;        
    }

    public void saveMessage(Message message) {
    	saveMessage(message, true);
    }

    public void saveMessage(Message message, boolean logEvent) {
        boolean isNew = message.getId() == null;
        
        if (!(message instanceof PrivateMessage)){                  
          if (isForumOrTopicLocked(message.getTopic().getBaseForum().getId(), message.getTopic().getId())) {
              LOG.info("saveMessage executed [messageId: " + (isNew ? "new" : message.getId().toString()) + "] but forum is locked -- save aborted");
              throw new LockedException("Message could not be saved [messageId: " + (isNew ? "new" : message.getId().toString()) + "]");
          }
        }
        
        message.setModified(new Date());
        message.setModifiedBy(getCurrentUser());
        if(message.getUuid() == null || message.getCreated() == null
        	|| message.getCreatedBy() == null || message.getModified() == null
        	|| message.getModifiedBy() == null || message.getTitle() == null 
        	|| message.getAuthor() == null || message.getHasAttachments() == null
        	|| message.getTypeUuid() == null 
        	|| message.getDraft() == null)
        {
        	LOG.error("null attribute(s) for saving message in MessageForumsMessageManagerImpl.saveMessage");
        }
        getHibernateTemplate().saveOrUpdate(message);
    
        if (logEvent) {
        	if (isNew) {
        		if (isMessageFromForums(message))
        			eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_ADD, getEventMessage(message), false));
        		else
        			eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_ADD, getEventMessage(message), false));
        	} else {
        		if (isMessageFromForums(message))
        			eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_RESPONSE, getEventMessage(message), false));
        		else
        			eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_RESPONSE, getEventMessage(message), false));
        	}
        }
        
        LOG.info("message " + message.getId() + " saved successfully");
    }

    public void deleteMessage(Message message) {
        long id = message.getId().longValue();
        message.setInReplyTo(null);
        
        getHibernateTemplate().saveOrUpdate(message);
        
        try {
        	getSession().flush();
        } 
        catch (Exception e) {
        	e.printStackTrace();
        }
        
        if (isMessageFromForums(message))
        	eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_REMOVE, getEventMessage(message), false));
        else
        	eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_REMOVE, getEventMessage(message), false));

        try {
            getSession().evict(message);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("could not evict message: " + message.getId(), e);
        }
        
        Topic topic = message.getTopic();        
        topic.removeMessage(message);
        getHibernateTemplate().saveOrUpdate(topic);
		//getHibernateTemplate().delete(message);

        try {
            getSession().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.info("message " + id + " deleted successfully");
    }
    
    public Message getMessageById(final Long messageId) {        
        if (messageId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getMessageById executing with messageId: " + messageId);
        
        return (Message) getHibernateTemplate().get(MessageImpl.class, messageId);
    }   
    
    /**
     * @see org.sakaiproject.api.app.messageforums.MessageForumsMessageManager#getMessageByIdWithAttachments(java.lang.Long)
     */
    public Message getMessageByIdWithAttachments(final Long messageId){
      
      if (messageId == null) {
        throw new IllegalArgumentException("Null Argument");
       }

       LOG.debug("getMessageByIdWithAttachments executing with messageId: " + messageId);
        

      HibernateCallback hcb = new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          Query q = session.getNamedQuery(QUERY_BY_MESSAGE_ID_WITH_ATTACHMENTS);
          q.setParameter("id", messageId, Hibernate.LONG);
          return q.uniqueResult();
        }
      };    

      return (Message) getHibernateTemplate().execute(hcb);
    }
    
    public Attachment getAttachmentById(final Long attachmentId) {        
        if (attachmentId == null) {
            throw new IllegalArgumentException("Null Argument");
        }
        
        LOG.debug("getAttachmentById executing with attachmentId: " + attachmentId);
        
        return (Attachment) getHibernateTemplate().get(AttachmentImpl.class, attachmentId);
    }
    
    public void getChildMsgs(final Long messageId, List returnList)
    {
    	List tempList;
    	
      HibernateCallback hcb = new HibernateCallback() 
			{
        public Object doInHibernate(Session session) throws HibernateException, SQLException 
				{
          Query q = session.getNamedQuery(QUERY_CHILD_MESSAGES);
          Query qOrdered= session.createQuery(q.getQueryString());
                  
          qOrdered.setParameter("messageId", messageId, Hibernate.LONG);
          
          return qOrdered.list();
        }
      };
      
      tempList = (List) getHibernateTemplate().execute(hcb);
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
            LOG.error("markMessageApproval failed with messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("markMessageApproval executing with messageId: " + messageId);
        
        Message message = (Message) getMessageById(messageId);
        message.setApproved(new Boolean(approved));
        
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
      HibernateCallback hcb = new HibernateCallback() 
			{
        public Object doInHibernate(Session session) throws HibernateException, SQLException 
				{
          Query q = session.getNamedQuery(QUERY_CHILD_MESSAGES);
          Query qOrdered= session.createQuery(q.getQueryString());
                  
          qOrdered.setParameter("messageId", messageId, Hibernate.LONG);
          
          return qOrdered.list();
        }
      };
      
      return (List)getHibernateTemplate().executeFind(hcb);
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
            LOG.error("isForumLocked called with null arguments");
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("isForumLocked executing with forumId: " + forumId + ":: topicId: " + topicId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery("findForumLockedAttribute");
                q.setParameter("id", forumId, Hibernate.LONG);
                return q.uniqueResult();
            }
        };

        HibernateCallback hcb2 = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery("findTopicLockedAttribute");
                q.setParameter("id", topicId, Hibernate.LONG);
                return q.uniqueResult();
            }
        };
        
        return ((Boolean) getHibernateTemplate().execute(hcb)).booleanValue() || ((Boolean) getHibernateTemplate().execute(hcb2)).booleanValue();                
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
    	String eventMessagePrefix = "";
    	final String toolId = ToolManager.getCurrentTool().getId();
    	
    		if (toolId.equals(DiscussionForumService.MESSAGE_CENTER_ID))
    			eventMessagePrefix = "/messagesAndForums/site/";
    		else if (toolId.equals(DiscussionForumService.MESSAGES_TOOL_ID))
    			eventMessagePrefix = "/messages/site/";
    		else
    			eventMessagePrefix = "/forums/site/";
    	
    	return eventMessagePrefix + getContextId() + "/" + object.toString() + "/" + getCurrentUser();
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

      LOG.debug("findPvtMsgsBySearchText executing with searchText: " + searchText);

      HibernateCallback hcb = new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException, SQLException {
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
              q.setParameter("contextId", ToolManager.getCurrentPlacement().getContext());
              q.setParameter("typeUuid", typeUuid);
              return q.list();
          }
      };

      return (List) getHibernateTemplate().execute(hcb);
  }
    
    private Integer convertBooleanToInteger(boolean value) {
       Integer retVal = (Boolean.TRUE.equals(value)) ? 1 : 0;
       return new Integer(retVal);
    }
    
    private String getContextId() {
      if (TestUtil.isRunningTests()) {
          return "test-context";
      }
      Placement placement = ToolManager.getCurrentPlacement();
      String presentSiteId = placement.getContext();
      return presentSiteId;
  }
    
  public String getAttachmentUrl(String id)
  {
  	try
  	{
		String link = contentHostingService.getResource(id).getUrl(false);
  		Pattern pattern = Pattern.compile("^(https?://[^/]+)(.+/)([^/]+)$");
  		Matcher matcher = pattern.matcher(link);
  		
  		if(matcher.matches()){
  			String host = matcher.group(1);
  			String path = matcher.group(2).replace(" ", "%20");
  			String file = matcher.group(3);
  			String encoded = URLEncoder.encode(file, "UTF-8").replace("+", "%20");
  			return (host + path + encoded);
  		}else{
  			return link;
  		}
  	}
  	catch(Exception e)
  	{
  		LOG.error("MessageForumsMessageManagerImpl.getAttachmentUrl" + e, e);
  	}
  	return null;
  }
  
  public String getAttachmentRelativeUrl(String id) {
      try
      {
          String tempString = contentHostingService.getResource(id).getUrl(true);
          String newString = org.sakaiproject.util.Web.escapeUrl(tempString);
          return newString;
      }
      catch(Exception e)
      {
          LOG.error("MessageForumsMessageManagerImpl.getAttachmentUrl" + e, e);
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
		String curToolId = ToolManager.getCurrentTool().getId();
		
		if (curToolId.equals(MESSAGECENTER_HELPER_TOOL_ID)) {
			curToolId = ToolManager.getCurrentPlacement().getTool().getId();
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
			thisSite = SiteService.getSite(siteId);
			
			Collection toolsInSite = thisSite.getTools(toolId);

			return ! toolsInSite.isEmpty();
		} 
		catch (IdUnusedException e) {
			// Weirdness - should not happen
			LOG.error("IdUnusedException attempting to get site for id " + siteId + " to check if tool " 
							+ "with id " + toolId + " is in it.", e);
		}
		
		return false;
	}

	public Map getReadStatusForMessagesWithId(final List msgIds, final String userId)
	{
		Map statusMap = new HashMap();
		if( msgIds != null && msgIds.size() > 0)
		{
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.getNamedQuery(QUERY_READ_STATUS_WITH_MSGS_USER);
					q.setParameter("userId", userId, Hibernate.STRING);
					q.setParameterList("msgIds", msgIds);
					return q.list();
				}
			};
			
			for (Iterator msgIdIter = msgIds.iterator(); msgIdIter.hasNext();) {
				Long msgId = (Long) msgIdIter.next();
				statusMap.put(msgId, Boolean.FALSE);
			}
			List statusList = (List)getHibernateTemplate().execute(hcb);
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
            LOG.error("getPendingMsgsInSiteByUser failed with membershipList: " + membershipList);
            throw new IllegalArgumentException("Null Argument");
        }
		
		HibernateCallback hcb = new HibernateCallback() 
		{
			public Object doInHibernate(Session session) throws HibernateException, SQLException 
			{
				Query q = session.getNamedQuery(QUERY_FIND_PENDING_MSGS_BY_CONTEXT_AND_USER);
				q.setParameter("contextId", getContextId(), Hibernate.STRING);
				q.setParameterList("membershipList", membershipList);
				q.setParameter("customTypeUuid", typeManager.getCustomLevelType(), Hibernate.STRING);
				
				return q.list();
			}
		};
		
		Message tempMsg = null;
        Set resultSet = new HashSet();      
        List temp = (ArrayList) getHibernateTemplate().execute(hcb);
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
            LOG.error("getNumPendingMsgsInTopic failed with topicId: " + topicId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getNumPendingMsgsInTopic executing with topicId: " + topicId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_FIND_PENDING_MSGS_BY_TOPICID);
                q.setParameter("topicId", topicId, Hibernate.LONG);
                return q.list();
            }
        };

        Message tempMsg = null;
        Set resultSet = new HashSet();      
        List temp = (ArrayList) getHibernateTemplate().execute(hcb);
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
}
