/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/MessageForumsForumManagerImpl.java $
 * $Id: MessageForumsForumManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.OpenTopic;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.ActorPermissionsImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageForumsUserImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.OpenTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.Util;
import org.sakaiproject.component.app.messageforums.exception.LockedException;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class MessageForumsForumManagerImpl extends HibernateDaoSupport implements MessageForumsForumManager {

    private static final Log LOG = LogFactory.getLog(MessageForumsForumManagerImpl.class);

    private static final String QUERY_FOR_PRIVATE_TOPICS = "findPrivateTopicsByForumId";

    private static final String QUERY_BY_FORUM_OWNER = "findPrivateForumByOwner";

    private static final String QUERY_BY_FORUM_ID = "findForumById";
    
    private static final String QUERY_BY_FORUM_ID_WITH_ATTACHMENTS = "findForumByIdWithAttachments";    

    private static final String QUERY_BY_FORUM_UUID = "findForumByUuid";
    
    private static final String QUERY_BY_TYPE_AND_CONTEXT = "findForumByTypeAndContext";
    private static final String QUERY_BY_FORUM_ID_AND_TOPICS = "findForumByIdWithTopics";

           
    private static final String QUERY_TOPIC_WITH_MESSAGES_AND_ATTACHMENTS = "findTopicByIdWithMessagesAndAttachments";        
    private static final String QUERY_TOPIC_WITH_MESSAGES = "findTopicByIdWithMessages";        
        
    private static final String QUERY_TOPICS_WITH_MESSAGES_FOR_FORUM = "findTopicsWithMessagesForForum";
    private static final String QUERY_TOPICS_WITH_MESSAGES_AND_ATTACHMENTS_FOR_FORUM = "findTopicsWithMessagesAndAttachmentsForForum";
            
    private static final String QUERY_BY_TOPIC_ID = "findTopicById";
    private static final String QUERY_OPEN_BY_TOPIC_AND_PARENT = "findOpenTopicAndParentById";
    private static final String QUERY_PRIVATE_BY_TOPIC_AND_PARENT = "findPrivateTopicAndParentById";    

    private static final String QUERY_BY_TOPIC_UUID = "findTopicByUuid";

    private static final String QUERY_OF_SUR_KEY_BY_TOPIC = "findOFTopicSurKeyByTopicId";
    private static final String QUERY_PF_SUR_KEY_BY_TOPIC = "findPFTopicSurKeyByTopicId";

    private static final String QUERY_BY_TOPIC_ID_MESSAGES_ATTACHMENTS = "findTopicByIdWithAttachments";

    private IdManager idManager;

    private SessionManager sessionManager;

    private MessageForumsTypeManager typeManager;

    public MessageForumsForumManagerImpl() {}

    private EventTrackingService eventTrackingService;

    public void init() {
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
    
    public void initializeTopicsForForum(BaseForum forum){
      
      getHibernateTemplate().initialize(forum);
      getHibernateTemplate().initialize(forum.getTopicsSet());
    }
    
    public List getTopicsByIdWithMessages(final Long forumId){
      if (forumId == null) {
        throw new IllegalArgumentException("Null Argument");
      }   
      
      HibernateCallback hcb = new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            Query q = session.getNamedQuery(QUERY_TOPICS_WITH_MESSAGES_FOR_FORUM);
            q.setParameter("id", forumId, Hibernate.LONG);            
            return q.list();
        }
    };

    Topic tempTopic = null;
    Set resultSet = new HashSet();      
    List temp = (ArrayList) getHibernateTemplate().execute(hcb);
    for (Iterator i = temp.iterator(); i.hasNext();)
    {
      Object[] results = (Object[]) i.next();        
          
      if (results != null) {
        if (results[0] instanceof Topic) {
          tempTopic = (Topic)results[0];
          tempTopic.setBaseForum((BaseForum)results[1]);            
        } else {
          tempTopic = (Topic)results[1];
          tempTopic.setBaseForum((BaseForum)results[0]);
        }
        resultSet.add(tempTopic);
      }
    }
    return Util.setToList(resultSet);
  }
    
    public List getTopicsByIdWithMessagesAndAttachments(final Long forumId){
      if (forumId == null) {
        throw new IllegalArgumentException("Null Argument");
      }   
      
      HibernateCallback hcb = new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            Query q = session.getNamedQuery(QUERY_TOPICS_WITH_MESSAGES_AND_ATTACHMENTS_FOR_FORUM);
            q.setParameter("id", forumId, Hibernate.LONG);            
            return q.list();
        }
    };

    Topic tempTopic = null;
    Set resultSet = new HashSet();      
    List temp = (ArrayList) getHibernateTemplate().execute(hcb);
    for (Iterator i = temp.iterator(); i.hasNext();)
    {
      Object[] results = (Object[]) i.next();        
          
      if (results != null) {
        if (results[0] instanceof Topic) {
          tempTopic = (Topic)results[0];
          tempTopic.setBaseForum((BaseForum)results[1]);            
        } else {
          tempTopic = (Topic)results[1];
          tempTopic.setBaseForum((BaseForum)results[0]);
        }
        resultSet.add(tempTopic);
      }
    }
    return Util.setToList(resultSet);    
  }
    
    public Topic getTopicByIdWithMessagesAndAttachments(final Long topicId) {

      if (topicId == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

     HibernateCallback hcb = new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException, SQLException {
              Query q = session.getNamedQuery(QUERY_TOPIC_WITH_MESSAGES_AND_ATTACHMENTS);
              q.setParameter("id", topicId, Hibernate.LONG);              
              return q.uniqueResult();
          }
      };
     

      return (Topic) getHibernateTemplate().execute(hcb);
    }
    
    public Topic getTopicByIdWithMessages(final Long topicId) {

      if (topicId == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

     HibernateCallback hcb = new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException, SQLException {
              Query q = session.getNamedQuery(QUERY_TOPIC_WITH_MESSAGES);
              q.setParameter("id", topicId, Hibernate.LONG);              
              return q.uniqueResult();
          }
      };
     

      return (Topic) getHibernateTemplate().execute(hcb);
    }
            
    public BaseForum getForumByIdWithTopics(final Long forumId) {

      if (forumId == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

     HibernateCallback hcb = new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException, SQLException {
              Query q = session.getNamedQuery(QUERY_BY_FORUM_ID_AND_TOPICS);
              q.setParameter("id", forumId, Hibernate.LONG);              
              return q.uniqueResult();
          }
      };
      
      BaseForum bForum = (BaseForum) getHibernateTemplate().execute(hcb);
      
      if (bForum != null){
        getHibernateTemplate().initialize(bForum.getAttachmentsSet());
      }
      
      return bForum;      
    }
    
    public List getForumByTypeAndContext(final String typeUuid) {

      if (typeUuid == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

     HibernateCallback hcb = new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException, SQLException {
              Query q = session.getNamedQuery(QUERY_BY_TYPE_AND_CONTEXT);
              q.setParameter("typeUuid", typeUuid, Hibernate.STRING);
              q.setParameter("contextId", getContextId(), Hibernate.STRING);
              return q.list();
          }
      };

      BaseForum tempForum = null;
      Set resultSet = new HashSet();      
      List temp = (ArrayList) getHibernateTemplate().execute(hcb);
      for (Iterator i = temp.iterator(); i.hasNext();)
      {
        Object[] results = (Object[]) i.next();        
            
        if (results != null) {
          if (results[0] instanceof BaseForum) {
            tempForum = (BaseForum)results[0];
            tempForum.setArea((Area)results[1]);            
          } else {
            tempForum = (BaseForum)results[1];
            tempForum.setArea((Area)results[0]);
          }
          resultSet.add(tempForum);
        }
      }
      return Util.setToList(resultSet);
    }

    public Topic getTopicByIdWithAttachments(final Long topicId) {

        if (topicId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getTopicByIdWithMessagesAndAttachments executing with topicId: " + topicId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_TOPIC_ID_MESSAGES_ATTACHMENTS);
                q.setParameter("id", topicId, Hibernate.LONG);
                return q.uniqueResult();
            }
        };

        return (Topic) getHibernateTemplate().execute(hcb);

    }

    public PrivateForum getPrivateForumByOwner(final String owner) {

        if (owner == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getForumByOwner executing with owner: " + owner);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_FORUM_OWNER);
                q.setParameter("owner", owner, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (PrivateForum) getHibernateTemplate().execute(hcb);
    }

    

    
    public BaseForum getForumByIdWithAttachments(final Long forumId) {
      
      if (forumId == null) {
          throw new IllegalArgumentException("Null Argument");
      }

      LOG.debug("getForumByIdWithAttachments executing with forumId: " + forumId);
                  
      HibernateCallback hcb = new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            Query q = session.getNamedQuery(QUERY_BY_FORUM_ID_WITH_ATTACHMENTS);
            q.setParameter("id", forumId, Hibernate.LONG);
            return q.uniqueResult();
        }
    };

      return (BaseForum) getHibernateTemplate().execute(hcb);

    }


    public BaseForum getForumById(boolean open, final Long forumId) {
        if (forumId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getDiscussionForumById executing with forumId: " + forumId);

        if (open) {
            // open works for both open and discussion forums
            return getForumByIdWithAttachments(forumId);
        } else {
            return (BaseForum) getHibernateTemplate().get(PrivateForumImpl.class, forumId);
        }
    }

    public BaseForum getForumByUuid(final String forumId) {
        if (forumId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getForumByUuid executing with forumId: " + forumId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_FORUM_UUID);
                q.setParameter("uuid", forumId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (BaseForum) getHibernateTemplate().execute(hcb);
    }

    public Topic getTopicById(final boolean open, final Long topicId) {
        if (topicId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getDiscussionForumById executing with topicId: " + topicId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                String query;
                if (open) {
                    query = QUERY_OPEN_BY_TOPIC_AND_PARENT;
                } else {
                    query = QUERY_PRIVATE_BY_TOPIC_AND_PARENT;
                }
                Query q = session.getNamedQuery(QUERY_OPEN_BY_TOPIC_AND_PARENT);
                q.setParameter("id", topicId, Hibernate.LONG);
                return q.list();
            }
        };

        Topic res = null;
        List temp = (ArrayList) getHibernateTemplate().execute(hcb);
        Object [] results = (Object[])temp.get(0);
        if (results != null) {
            if (results[0] instanceof Topic) {
                res = (Topic)results[0];
                res.setBaseForum((BaseForum)results[1]);
            } else {
                res = (Topic)results[1];
                res.setBaseForum((BaseForum)results[0]);
            }
        }
        return res;
    }

    public Topic getTopicByUuid(final String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getDiscussionForumById executing with topicId: " + uuid);
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_TOPIC_UUID);
                q.setParameter("uuid", uuid, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (Topic) getHibernateTemplate().execute(hcb);
    }

    public DiscussionForum createDiscussionForum() {
        DiscussionForum forum = new DiscussionForumImpl();
        forum.setUuid(getNextUuid());
        forum.setCreated(new Date());
        forum.setCreatedBy(getCurrentUser());
        forum.setLocked(Boolean.FALSE);
        forum.setDraft(Boolean.FALSE);
        forum.setTypeUuid(typeManager.getDiscussionForumType());                  
        forum.setActorPermissions(createDefaultActorPermissions());
        LOG.debug("createDiscussionForum executed");
        return forum;
    }

    public ActorPermissions createDefaultActorPermissions()
    {
      ActorPermissions actorPermissions = new ActorPermissionsImpl();      
      MessageForumsUser nonSpecifiedUser = new MessageForumsUserImpl();
      nonSpecifiedUser.setUserId(typeManager.getNotSpecifiedType());
      nonSpecifiedUser.setUuid(typeManager.getNotSpecifiedType());
      nonSpecifiedUser.setTypeUuid(typeManager.getNotSpecifiedType());
                  
      actorPermissions.addAccesssor(nonSpecifiedUser);      
      actorPermissions.addContributor(nonSpecifiedUser);
      actorPermissions.addModerator(nonSpecifiedUser);
       return actorPermissions;
    }
    
//    public void saveActorPermissions(ActorPermissions actorPermissions){
//      boolean isNew = actorPermissions.getId() == null;
//      
//      getHibernateTemplate().saveOrUpdate(actorPermissions);
//
//      if (isNew) {
//          eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_ADD, getEventMessage(actorPermissions), false));
//      } else {
//          eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_WRITE, getEventMessage(actorPermissions), false));
//      }      
//    }

    /**
     * @see org.sakaiproject.api.app.messageforums.MessageForumsForumManager#createPrivateForum(java.lang.String)
     */
    public PrivateForum createPrivateForum(String title) {
        /** set all non-null properties in case hibernate flushes session before explicit save */
        PrivateForum forum = new PrivateForumImpl();        
        forum.setTitle(title);
        forum.setUuid(idManager.createUuid());
        forum.setAutoForwardEmail("");
        forum.setOwner(getCurrentUser());        
        forum.setUuid(getNextUuid());
        forum.setCreated(new Date());
        forum.setCreatedBy(getCurrentUser());
        forum.setSortIndex(new Integer(0));
        forum.setShortDescription("short-desc");
        forum.setExtendedDescription("ext desc");
        forum.setAutoForward(Boolean.FALSE);
        forum.setAutoForwardEmail("");
        forum.setPreviewPaneEnabled(Boolean.FALSE);
        forum.setModified(new Date());
        forum.setModifiedBy(getCurrentUser());
        forum.setTypeUuid(typeManager.getPrivateMessageAreaType());
        LOG.debug("createPrivateForum executed");
        return forum;
    }

    /**
     * @see org.sakaiproject.api.app.messageforums.MessageForumsForumManager#savePrivateForum(org.sakaiproject.api.app.messageforums.PrivateForum)
     */
    public void savePrivateForum(PrivateForum forum) {
        boolean isNew = forum.getId() == null;

        if (forum.getSortIndex() == null) {
            forum.setSortIndex(new Integer(0));
        }

        forum.setModified(new Date());
        forum.setModifiedBy(getCurrentUser());
        forum.setOwner(getCurrentUser());
        getHibernateTemplate().saveOrUpdate(forum);

        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_ADD, getEventMessage(forum), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_WRITE, getEventMessage(forum), false));
        }

        LOG.debug("savePrivateForum executed with forumId: " + forum.getId());
    }

    /**
     * Save a discussion forum
     */
    public void saveDiscussionForum(DiscussionForum forum) {
        saveDiscussionForum(forum, false);
    }

    public void saveDiscussionForum(DiscussionForum forum, boolean draft) {
        boolean isNew = forum.getId() == null;

        if (forum.getSortIndex() == null) {
            forum.setSortIndex(new Integer(0));
        }
        if (forum.getLocked() == null) {
            forum.setLocked(Boolean.FALSE);
        }
        forum.setDraft(new Boolean(draft));
        forum.setModified(new Date());
        forum.setModifiedBy(getCurrentUser());
        getHibernateTemplate().saveOrUpdate(forum);

        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_ADD, getEventMessage(forum), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_WRITE, getEventMessage(forum), false));
        }

        LOG.debug("saveDiscussionForum executed with forumId: " + forum.getId() + ":: draft: " + draft);
    }
    
    public DiscussionTopic createDiscussionForumTopic(DiscussionForum forum) {
        DiscussionTopic topic = new DiscussionTopicImpl();
        topic.setUuid(getNextUuid());
        topic.setTypeUuid(typeManager.getDiscussionForumType());
        topic.setCreated(new Date());
        topic.setCreatedBy(getCurrentUser());
        topic.setBaseForum(forum);
        topic.setLocked(Boolean.FALSE);
        topic.setDraft(forum.getDraft());
        LOG.debug("createDiscussionForumTopic executed");
        return topic;
    }

    /**
     * Save a discussion forum topic
     */
    public void saveDiscussionForumTopic(DiscussionTopic topic) {
        boolean isNew = topic.getId() == null;

//        if (isForumLocked(topic.getBaseForum().getId())) {
//            LOG.info("saveDiscussionForumTopic executed [topicId: " + (isNew ? "new" : topic.getId().toString()) + "] but forum is locked -- save aborted");
//            throw new LockedException("Topic could not be saved [topicId: " + (isNew ? "new" : topic.getId().toString()) + "]");
//        }
        
        if (topic.getMutable() == null) {
            topic.setMutable(Boolean.FALSE);
        }
        if (topic.getSortIndex() == null) {
            topic.setSortIndex(new Integer(0));
        }
        topic.setModified(new Date());
        topic.setModifiedBy(getCurrentUser());
        if (topic.getId() == null) {
            
          DiscussionForum discussionForum = 
            (DiscussionForum) getForumByIdWithTopics(topic.getBaseForum().getId());
          discussionForum.addTopic(topic);
                                  
//          DiscussionForum forum = (DiscussionForum) topic.getBaseForum();
//          getHibernateTemplate().lock(forum, LockMode.NONE);
//          getHibernateTemplate().initialize(forum.getTopicsSet());          
//          forum.addTopic(topic);                       
//          getHibernateTemplate().saveOrUpdate(topic);
          saveDiscussionForum(discussionForum);
            
        } else {
            getHibernateTemplate().saveOrUpdate(topic);
        }

        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_ADD, getEventMessage(topic), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_WRITE, getEventMessage(topic), false));
        }

        LOG.debug("saveDiscussionForumTopic executed with topicId: " + topic.getId());
    }

    public OpenTopic createOpenForumTopic(OpenForum forum) {
        OpenTopic topic = new OpenTopicImpl();
        topic.setUuid(getNextUuid());
        topic.setTypeUuid(typeManager.getOpenDiscussionForumType());
        topic.setCreated(new Date());
        topic.setCreatedBy(getCurrentUser());
        topic.setLocked(Boolean.FALSE);
        topic.setDraft(forum.getDraft());
        LOG.debug("createOpenForumTopic executed");
        return topic;
    }

    public PrivateTopic createPrivateForumTopic(String title, boolean forumIsParent, boolean topicIsMutable, String userId, Long parentId) {
        /** set all non-null properties in case hibernate flushes session before explicit save */
        PrivateTopic topic = new PrivateTopicImpl();
        topic.setTitle(title);
        topic.setUuid(getNextUuid());        
        topic.setCreated(new Date());
        topic.setCreatedBy(getCurrentUser());
        topic.setUserId(userId);
        topic.setShortDescription("short-desc");
        topic.setExtendedDescription("ext-desc");
        topic.setMutable(new Boolean(topicIsMutable));
        topic.setSortIndex(new Integer(0));
        topic.setModified(new Date());
        topic.setModifiedBy(getCurrentUser());
        topic.setTypeUuid(typeManager.getPrivateMessageAreaType());
        LOG.debug("createPrivateForumTopic executed");
        return topic;
    }

    /**
     * Save a private forum topic
     */
    public void savePrivateForumTopic(PrivateTopic topic) {
        boolean isNew = topic.getId() == null;

        topic.setModified(new Date());
        topic.setModifiedBy(getCurrentUser());
        getHibernateTemplate().saveOrUpdate(topic);

        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_ADD, getEventMessage(topic), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_WRITE, getEventMessage(topic), false));
        }

        LOG.debug("savePrivateForumTopic executed with forumId: " + topic.getId());
    }

    /**
     * Save an open forum topic
     */
    public void saveOpenForumTopic(OpenTopic topic) {
        boolean isNew = topic.getId() == null;

        topic.setModified(new Date());
        topic.setModifiedBy(getCurrentUser());
        getHibernateTemplate().saveOrUpdate(topic);

        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_ADD, getEventMessage(topic), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_WRITE, getEventMessage(topic), false));
        }

        LOG.debug("saveOpenForumTopic executed with forumId: " + topic.getId());
    }

    /**
     * Delete a discussion forum and all topics/messages
     */
    public void deleteDiscussionForum(DiscussionForum forum) {
        long id = forum.getId().longValue();
        eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_REMOVE, getEventMessage(forum), false));
        try {
            getSession().evict(forum);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("could not evict forum: " + forum.getId(), e);
        }
        Area area = getAreaByContextIdAndTypeId(typeManager.getDiscussionForumType());
        area.removeDiscussionForum(forum);
        getHibernateTemplate().saveOrUpdate(area);
        //getHibernateTemplate().delete(forum);
        LOG.debug("deleteDiscussionForum executed with forumId: " + id);
    }

    public Area getAreaByContextIdAndTypeId(final String typeId) {
        LOG.debug("getAreaByContextIdAndTypeId executing for current user: " + getCurrentUser());
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery("findAreaByContextIdAndTypeId");
                q.setParameter("contextId", getContextId(), Hibernate.STRING);
                q.setParameter("typeId", typeId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (Area) getHibernateTemplate().execute(hcb);
    }
    
    /**
     * Delete a discussion forum topic
     */
    public void deleteDiscussionForumTopic(DiscussionTopic topic) {
        long id = topic.getId().longValue();
        eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_REMOVE, getEventMessage(topic), false));
        try {
            getSession().evict(topic);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("could not evict topic: " + topic.getId(), e);
        }
        
        Topic finder = getTopicById(true, topic.getId());
        BaseForum forum = finder.getBaseForum();
        forum.removeTopic(topic);
        getHibernateTemplate().saveOrUpdate(forum);
        //getHibernateTemplate().delete(topic);
        LOG.debug("deleteOpenForumTopic executed with topicId: " + id);
    }

    /**
     * Delete an open forum topic
     */
    public void deleteOpenForumTopic(OpenTopic topic) {
        eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_REMOVE, getEventMessage(topic), false));
        getHibernateTemplate().delete(topic);
        LOG.debug("deleteOpenForumTopic executed with forumId: " + topic.getId());
    }

    /**
     * Delete a private forum topic
     */
    public void deletePrivateForumTopic(PrivateTopic topic) {
        eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_REMOVE, getEventMessage(topic), false));
        getHibernateTemplate().delete(topic);
        LOG.debug("deletePrivateForumTopic executed with forumId: " + topic.getId());
    }

    /**
     * Returns a given number of messages if available in the time provided
     * 
     * @param numberMessages
     *            the number of messages to retrieve
     * @param numberDaysInPast
     *            the number days to look back
     */
    public List getRecentPrivateMessages(int numberMessages, int numberDaysInPast) {
        // TODO: Implement Me!
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a given number of discussion forum messages if available in the
     * time provided
     * 
     * @param numberMessages
     *            the number of forum messages to retrieve
     * @param numberDaysInPast
     *            the number days to look back
     */
    public List getRecentDiscussionForumMessages(int numberMessages, int numberDaysInPast) {
        // TODO: Implement Me!
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a given number of open forum messages if available in the time
     * provided
     * 
     * @param numberMessages
     *            the number of forum messages to retrieve
     * @param numberDaysInPast
     *            the number days to look back
     */
    public List getRecentOpenForumMessages(int numberMessages, int numberDaysInPast) {
        // TODO: Implement Me!
        throw new UnsupportedOperationException();
    }

    private boolean isForumLocked(final Long id) {
        if (id == null) {
            LOG.error("isForumLocked failed with id: " + id);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("isForumLocked executing with id: " + id);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery("findForumLockedAttribute");
                q.setParameter("id", id, Hibernate.LONG);
                return q.uniqueResult();
            }
        };

        return ((Boolean) getHibernateTemplate().execute(hcb)).booleanValue();                
    }
    
    
    public List searchTopicMessages(final Long topicId, final String searchText) {
        if (topicId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getDiscussionForumById executing with topicId: " + topicId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery("findMessagesBySearchText");
                q.setParameter("id", topicId, Hibernate.LONG);
                q.setParameter("searchByText", "%" + searchText + "%", Hibernate.STRING);
                return q.list();
            }
        };

        return (List) getHibernateTemplate().execute(hcb);
    }

    
    // helpers
    
    /**
     * ContextId is present site id for now.
     */
    private String getContextId() {
        if (TestUtil.isRunningTests()) {
            return "test-context";
        }
        Placement placement = ToolManager.getCurrentPlacement();
        String presentSiteId = placement.getContext();
        return presentSiteId;
    }

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
        return "MessageCenter::" + getCurrentUser() + "::" + object.toString();
    }

}
