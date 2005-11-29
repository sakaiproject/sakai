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
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.OpenTopic;
import org.sakaiproject.api.kernel.id.IdManager;
import org.sakaiproject.api.kernel.session.SessionManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.OpenTopicImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class MessageForumsForumManagerImpl extends HibernateDaoSupport implements MessageForumsForumManager {

    private static final Log LOG = LogFactory.getLog(MessageForumsForumManagerImpl.class);

    private static final String QUERY_BY_DISCUSSION_FORUM_ID = "findDiscussionForumById";

    private static final String ID = "ID";

    private IdManager idManager;

    private SessionManager sessionManager;

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public MessageForumsForumManagerImpl() {}

    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    /**
     * Retrieve the current user's discussion forums
     */
    public List getDiscussionForums() {
        // TODO: Implement Me!
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieve a given discussion forum for the current user
     */
    public DiscussionForum getDiscussionForumById(final String forumId) {
        if (forumId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getDiscussionForumById executing with forumId: " + forumId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_DISCUSSION_FORUM_ID);
                q.setParameter(ID, forumId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (DiscussionForum) getHibernateTemplate().execute(hcb);
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
        LOG.debug("saveDiscussionForum executed with forumId: " + forum.getId());
        return forum;
    }

    /**
     * Save a discussion forum
     */
    public void saveDiscussionForum(DiscussionForum forum) {
        forum.setModified(new Date());
        forum.setModifiedBy(getCurrentUser());
        getHibernateTemplate().saveOrUpdate(forum);
        LOG.debug("saveDiscussionForum executed with forumId: " + forum.getId());
    }

    public DiscussionTopic createDiscussionForumTopic() {
        DiscussionTopic topic = new DiscussionTopicImpl();
        topic.setUuid(getNextUuid());
        topic.setCreated(new Date());
        topic.setCreatedBy(getCurrentUser());
        LOG.debug("saveOpenForumTopic executed with forumId: " + topic.getId());
        return topic;
    }

    /**
     * Save a discussion forum topic
     */
    public void saveDiscussionForumTopic(DiscussionTopic topic) {
        topic.setModified(new Date());
        topic.setModifiedBy(getCurrentUser());
        getHibernateTemplate().saveOrUpdate(topic);
        LOG.debug("saveOpenForumTopic executed with forumId: " + topic.getId());
    }

    public OpenTopic createOpenForumTopic() {
        OpenTopic topic = new OpenTopicImpl();
        topic.setUuid(getNextUuid());
        topic.setCreated(new Date());
        topic.setCreatedBy(getCurrentUser());
        LOG.debug("saveOpenForumTopic executed with forumId: " + topic.getId());
        return topic;
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
        try {
            return sessionManager.getCurrentSessionUserId();
        } catch (Exception e) {
            // TODO: remove after done testing
            return "testuser";
        }
    }

    private String getNextUuid() {
        return idManager.createUuid();
    }

    public IdManager getIdManager() {
        return idManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

}
