/**********************************************************************************
 * $URL: $
 * $Id:  $
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
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.UnreadStatus;
import org.sakaiproject.api.kernel.id.IdManager;
import org.sakaiproject.api.kernel.session.SessionManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AttachmentImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.UnreadStatusImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class MessageForumsMessageManagerImpl extends HibernateDaoSupport implements MessageForumsMessageManager {

    private static final Log LOG = LogFactory.getLog(MessageForumsMessageManagerImpl.class);    

    private static final String QUERY_BY_MESSAGE_ID = "findMessageById";
    private static final String QUERY_ATTACHMENT_BY_ID = "findAttachmentById";    
    private static final String QUERY_COUNT_BY_READ = "findReadMessageCountByTopicId";
    private static final String QUERY_BY_TOPIC_ID = "findMessagesByTopicId";
    private static final String QUERY_UNREAD_STATUS = "findUnreadStatusForMessage";
    private static final String ID = "id";

    private IdManager idManager;                             

    private MessageForumsTypeManager typeManager;

    private SessionManager sessionManager;

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
    
    public int findUnreadMessageCountByTopicId(final String userId, final String topicId) {
        if (topicId == null) {
            LOG.error("findUnreadMessageCountByTopicId failed with topicId: " + topicId + ", userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findUnreadMessageCountByTopicId executing with topicId: " + topicId + ", userId: " + userId);

        return findMessageCountByTopicId(topicId) - findReadMessageCountByTopicId(userId, topicId);
    }
    
    public int findReadMessageCountByTopicId(final String userId, final String topicId) {
        if (topicId == null) {
            LOG.error("findReadMessageCountByTopicId failed with topicId: " + topicId + ", userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findReadMessageCountByTopicId executing with topicId: " + topicId + ", userId: " + userId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_COUNT_BY_READ);
                q.setParameter("topicId", topicId, Hibernate.STRING);
                q.setParameter("userId", userId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return ((Integer) getHibernateTemplate().execute(hcb)).intValue();        
    }

    public List findMessagesByTopicId(final String topicId) {
        if (topicId == null) {
            LOG.error("findMessagesByTopicId failed with topicId: " + topicId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findMessagesByTopicId executing with topicId: " + topicId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_TOPIC_ID);
                q.setParameter("topicId", topicId, Hibernate.STRING);
                return q.list();
            }
        };

        return (List) getHibernateTemplate().execute(hcb);        
    }
    
    public int findMessageCountByTopicId(final String topicId) {
        if (topicId == null) {
            LOG.error("findMessageCountByTopicId failed with topicId: " + topicId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findMessageCountByTopicId executing with topicId: " + topicId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery("findMessageCountByTopicId");
                q.setParameter("topicId", topicId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return ((Integer) getHibernateTemplate().execute(hcb)).intValue();        
    }

    public UnreadStatus findUnreadStatus(final String userId, final String topicId, final String messageId) {
        if (messageId == null || topicId == null || userId == null) {
            LOG.error("findUnreadStatus failed with topicId: " + topicId + ", messageId: " + messageId + ", userId:" + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findUnreadStatus executing with topicId: " + topicId + ", messageId: " + messageId + ", userId:" + userId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_UNREAD_STATUS);
                q.setParameter("topicId", topicId, Hibernate.STRING);
                q.setParameter("messageId", messageId, Hibernate.STRING);
                q.setParameter("userId", userId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (UnreadStatus) getHibernateTemplate().execute(hcb);        
    }

    public void deleteUnreadStatus(String userId, String topicId, String messageId) {
        if (messageId == null || topicId == null || userId == null) {
            LOG.error("deleteUnreadStatus failed with topicId: " + topicId + ", messageId: " + messageId + ", userId:" + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("deleteUnreadStatus executing with topicId: " + topicId + ", messageId: " + messageId + ", userId:" + userId);

        UnreadStatus status = findUnreadStatus(userId, topicId, messageId);
        if (status != null) {
            getHibernateTemplate().delete(status);
        }
    }

    public void markMessageReadForUser(String userId, String topicId, String messageId) {
        if (messageId == null || topicId == null || userId == null) {
            LOG.error("markMessageReadForUser failed with topicId: " + topicId + ", messageId: " + messageId + ", userId:" + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("markMessageReadForUser executing with topicId: " + topicId + ", messageId: " + messageId + ", userId:" + userId);

        UnreadStatus status = new UnreadStatusImpl();
        status.setTopicId(topicId);
        status.setMessageId(messageId);
        status.setUserId(userId);
        status.setRead(Boolean.TRUE);
        
        getHibernateTemplate().saveOrUpdate(status);
    }
    
    public boolean isMessageReadForUser(final String userId, final String topicId, final String messageId) {
        if (messageId == null || topicId == null || userId == null) {
            LOG.error("getMessageById failed with topicId: " + topicId + ", messageId: " + messageId + ", userId:" + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getMessageById executing with topicId: " + topicId + ", messageId: " + messageId + ", userId:" + userId);

        UnreadStatus status = findUnreadStatus(userId, topicId, messageId);
        if (status == null) {
            return false; // not been saved yet, so it is unread
        }
        return status.getRead().booleanValue();        
    }

    public PrivateMessage createPrivateMessage() {
        PrivateMessage message = new PrivateMessageImpl();
        message.setUuid(getNextUuid());
        message.setTypeUuid(typeManager.getPrivateType());
        message.setCreated(new Date());
        message.setCreatedBy(getCurrentUser());

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
        message.setModified(new Date());
        message.setModifiedBy(getCurrentUser());        
        getHibernateTemplate().saveOrUpdate(message);
        LOG.info("message " + message.getId() + " saved successfully");
    }

    public void deleteMessage(Message message) {
        getHibernateTemplate().delete(message);
        LOG.info("message " + message.getId() + " deleted successfully");
    }
    
    public Message getMessageById(final String messageId) {        
        if (messageId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getMessageById executing with messageId: " + messageId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_MESSAGE_ID);
                q.setParameter(ID, messageId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (Message) getHibernateTemplate().execute(hcb);
    }    
    
    public Attachment getAttachmentById(final String attachmentId) {        
        if (attachmentId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("getAttachmentById executing with attachmentId: " + attachmentId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_ATTACHMENT_BY_ID);
                q.setParameter(ID, attachmentId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (Attachment) getHibernateTemplate().execute(hcb);
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
