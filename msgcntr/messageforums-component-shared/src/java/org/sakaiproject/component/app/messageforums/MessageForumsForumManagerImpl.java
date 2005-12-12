/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.messageforums;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.OpenTopic;
import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.kernel.id.IdManager;
import org.sakaiproject.api.kernel.session.SessionManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.OpenTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.TopicImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class MessageForumsForumManagerImpl extends HibernateDaoSupport implements MessageForumsForumManager {

    private static final Log LOG = LogFactory.getLog(MessageForumsForumManagerImpl.class);

    private static final String QUERY_BY_FORUM_ID = "findForumById";
    private static final String QUERY_BY_FORUM_UUID = "findForumByUuid";
    private static final String QUERY_BY_TOPIC_ID = "findTopicById";    
  
    private IdManager idManager;

    private SessionManager sessionManager;

    private MessageForumsTypeManager typeManager;

    public MessageForumsForumManagerImpl() {}
    
    public void init() {
        ;
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

    /**
     * Retrieve the current user's discussion forums
     */
    public List getDiscussionForums() {
        // TODO: Implement Me!
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieve a given forum for the current user
     */
    public BaseForum getForumById(final Long forumId) {
        if (forumId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getDiscussionForumById executing with forumId: " + forumId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_FORUM_ID);
                q.setParameter("id", forumId, Hibernate.LONG);
                return q.uniqueResult();
            }
        };

        return (BaseForum) getHibernateTemplate().execute(hcb);
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
    
    public Topic getTopicById(final Long topicId) {
        if (topicId == null) {
            throw new IllegalArgumentException("Null Argument");
        }
               
        LOG.debug("getDiscussionForumById executing with topicId: " + topicId);
        
        return (Topic) getHibernateTemplate().get(TopicImpl.class, topicId);

//        HibernateCallback hcb = new HibernateCallback() {
//            public Object doInHibernate(Session session) throws HibernateException, SQLException {
//                Query q = session.getNamedQuery(QUERY_BY_TOPIC_ID);
//                q.setParameter("id", topicId, Hibernate.STRING);
//                return q.uniqueResult();
//            }
//        };
//
//        return (Topic) getHibernateTemplate().execute(hcb);
    }

    
    /**
     * Retrieve topics the current user's open forums
     */
    public List getOpenForums() {
        // TODO: Implement Me!
        throw new UnsupportedOperationException();
    }

    public DiscussionForum createDiscussionForum() {
        DiscussionForum forum = new DiscussionForumImpl();
        forum.setUuid(getNextUuid());
        forum.setCreated(new Date());
        forum.setCreatedBy(getCurrentUser());
        forum.setTypeUuid(typeManager.getDiscussionForumType());
        LOG.debug("createDiscussionForum executed");
        return forum;
    }

    /**
     * Save a discussion forum
     */
    public void saveDiscussionForum(DiscussionForum forum) {
        if (forum.getSortIndex() == null) {
            forum.setSortIndex(new Integer(0));
        }
        if (forum.getLocked() == null) {
            forum.setLocked(Boolean.FALSE);
        }        
        forum.setModified(new Date());
        forum.setModifiedBy(getCurrentUser());
        getHibernateTemplate().saveOrUpdate(forum);
        LOG.debug("saveDiscussionForum executed with forumId: " + forum.getId());
    }

    public DiscussionTopic createDiscussionForumTopic() {
        DiscussionTopic topic = new DiscussionTopicImpl();
        topic.setUuid(getNextUuid());
        topic.setTypeUuid(typeManager.getDiscussionForumType());
        topic.setCreated(new Date());
        topic.setCreatedBy(getCurrentUser());
        LOG.debug("createDiscussionForumTopic executed");
        return topic;
    }

    /**
     * Save a discussion forum topic
     */
    public void saveDiscussionForumTopic(DiscussionTopic topic) {
        if (topic.getMutable() == null) {
            topic.setMutable(Boolean.FALSE);
        }
        if (topic.getSortIndex() == null) {
            topic.setSortIndex(new Integer(0));
        }
        topic.setModified(new Date());
        topic.setModifiedBy(getCurrentUser());
        getHibernateTemplate().saveOrUpdate(topic);
        LOG.debug("saveDiscussionForumTopic executed with topicId: " + topic.getId());
    }

    public OpenTopic createOpenForumTopic() {
        OpenTopic topic = new OpenTopicImpl();
        topic.setUuid(getNextUuid());
        topic.setTypeUuid(typeManager.getOpenDiscussionForumType());
        topic.setCreated(new Date());
        topic.setCreatedBy(getCurrentUser());
        LOG.debug("createOpenForumTopic executed");
        return topic;
    }        
    
    public PrivateTopic createPrivateForumTopic(boolean forumIsParent, String userId, Long parentId) {
      PrivateTopic topic = new PrivateTopicImpl();
      topic.setUuid(getNextUuid());
      topic.setTypeUuid(typeManager.getPrivateMessageAreaType());
      topic.setCreated(new Date());
      topic.setCreatedBy(getCurrentUser());
      topic.setUserId(userId);
      if (forumIsParent) {
          topic.setBaseForum(getForumById(parentId));
      } else {
          topic.setParentTopic((PrivateTopic)getTopicById(parentId));
      }
      LOG.debug("createPrivateForumTopic executed");
      return topic;
    }
    
    /**
     * Save a private forum topic
     */
    public void savePrivateForumTopic(PrivateTopic topic) {
        topic.setModified(new Date());
        topic.setModifiedBy(getCurrentUser());
        getHibernateTemplate().saveOrUpdate(topic);
        LOG.debug("savePrivateForumTopic executed with forumId: " + topic.getId());
    }
    
   

    /**
     * Save an open forum topic
     */
    public void saveOpenForumTopic(OpenTopic topic) {
        topic.setModified(new Date());
        topic.setModifiedBy(getCurrentUser());
        getHibernateTemplate().saveOrUpdate(topic);
        LOG.debug("saveOpenForumTopic executed with forumId: " + topic.getId());
    }

    /**
     * Delete a discussion forum and all topics/messages
     */
    public void deleteDiscussionForum(DiscussionForum forum) {
        getHibernateTemplate().delete(forum);
        LOG.debug("deleteDiscussionForum executed with forumId: " + forum.getId());
    }

    /**
     * Delete a discussion forum topic
     */
    public void deleteDiscussionForumTopic(DiscussionTopic topic) {
        getHibernateTemplate().delete(topic);
        LOG.debug("deleteOpenForumTopic executed with forumId: " + topic.getId());
    }

    /**
     * Delete an open forum topic
     */
    public void deleteOpenForumTopic(OpenTopic topic) {
        getHibernateTemplate().delete(topic);
        LOG.debug("deleteOpenForumTopic executed with forumId: " + topic.getId());
    }
    
    /**
     * Delete a private forum topic
     */
    public void deletePrivateForumTopic(PrivateTopic topic) {
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

}
