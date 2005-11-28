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

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.UnreadStatus;
import org.sakaiproject.component.app.messageforums.dao.hibernate.UnreadStatusImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class MessageForumsMessageManagerImpl extends HibernateDaoSupport implements MessageForumsMessageManager {

    private static final Log LOG = LogFactory.getLog(MessageForumsMessageManagerImpl.class);    

    private static final String QUERY_BY_MESSAGE_ID = "findMessageById";
    private static final String QUERY_UNREAD_STATUS = "findUnreadStatusForMessage";
    private static final String ID = "id";

    public void init() {
        ;
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

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_UNREAD_STATUS);
                q.setParameter("topicId", topicId, Hibernate.STRING);
                q.setParameter("messageId", messageId, Hibernate.STRING);
                q.setParameter("userId", userId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        UnreadStatus status = (UnreadStatus) getHibernateTemplate().execute(hcb);
        if (status == null) {
            return false; // not been saved yet, so it is unread
        }
        return status.getRead().booleanValue();        
    }
    
    public void saveMessage(Message message) {
        // a new message
        if (message.getUuid() == null) {
            // TODO: get a uuid for this new message
            message.setUuid("001");
            message.setCreated(new Date());
            message.setCreatedBy(getCurrentUser());
            // TODO: Call into the sakai type manager - set by tool?
            message.setTypeUuid("xxx");
        } 
        
        // always need to update the last modified stuff
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
    
    // helpers
    
    private String getCurrentUser() {
        // TODO: add the session manager back
        return "joe"; //SessionManager.getCurrentSession().getUserEid();
    }
    
}
