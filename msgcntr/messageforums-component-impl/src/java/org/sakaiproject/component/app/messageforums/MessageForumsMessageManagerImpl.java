/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/MessageForumsMessageManagerImpl.java $
 * $Id: MessageForumsMessageManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.UnreadStatus;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AttachmentImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.UnreadStatusImpl;
import org.sakaiproject.component.app.messageforums.exception.LockedException;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class MessageForumsMessageManagerImpl extends HibernateDaoSupport implements MessageForumsMessageManager {

    private static final Log LOG = LogFactory.getLog(MessageForumsMessageManagerImpl.class);    

    //private static final String QUERY_BY_MESSAGE_ID = "findMessageById";
    //private static final String QUERY_ATTACHMENT_BY_ID = "findAttachmentById";
    private static final String QUERY_BY_MESSAGE_ID_WITH_ATTACHMENTS = "findMessageByIdWithAttachments";
    private static final String QUERY_COUNT_BY_READ = "findReadMessageCountByTopicId";
    private static final String QUERY_BY_TOPIC_ID = "findMessagesByTopicId";
    private static final String QUERY_UNREAD_STATUS = "findUnreadStatusForMessage";
    private static final String QUERY_CHILD_MESSAGES = "finalAllChildMessages";
    //private static final String ID = "id";

    private IdManager idManager;                             

    private MessageForumsTypeManager typeManager;

    private SessionManager sessionManager;

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
    
    public int findUnreadMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            LOG.error("findUnreadMessageCountByTopicId failed with topicId: " + topicId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findUnreadMessageCountByTopicId executing with topicId: " + topicId);

        return findMessageCountByTopicId(topicId) - findReadMessageCountByTopicId(topicId);
    }
    
    public int findReadMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            LOG.error("findReadMessageCountByTopicId failed with topicId: " + topicId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findReadMessageCountByTopicId executing with topicId: " + topicId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_COUNT_BY_READ);
                q.setParameter("topicId", topicId, Hibernate.LONG);
                q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return ((Integer) getHibernateTemplate().execute(hcb)).intValue();        
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

    public UnreadStatus findUnreadStatus(final Long topicId, final Long messageId) {
        if (messageId == null || topicId == null) {
            LOG.error("findUnreadStatus failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("findUnreadStatus executing with topicId: " + topicId + ", messageId: " + messageId);

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_UNREAD_STATUS);
                q.setParameter("topicId", topicId, Hibernate.LONG);
                q.setParameter("messageId", messageId, Hibernate.LONG);
                q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (UnreadStatus) getHibernateTemplate().execute(hcb);        
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

    public void markMessageReadForUser(Long topicId, Long messageId, boolean read) {
        if (messageId == null || topicId == null) {
            LOG.error("markMessageReadForUser failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        LOG.debug("markMessageReadForUser executing with topicId: " + topicId + ", messageId: " + messageId);

        UnreadStatus status = findUnreadStatus(topicId, messageId);
        if (status == null) {
            status = new UnreadStatusImpl();
        }        
        status.setTopicId(topicId);
        status.setMessageId(messageId);
        status.setUserId(getCurrentUser());
        status.setRead(new Boolean(read));
        
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
        boolean isNew = message.getId() == null;
        
        if (!(message instanceof PrivateMessage)){                  
          if (isForumOrTopicLocked(message.getTopic().getBaseForum().getId(), message.getTopic().getId())) {
              LOG.info("saveMessage executed [messageId: " + (isNew ? "new" : message.getId().toString()) + "] but forum is locked -- save aborted");
              throw new LockedException("Message could not be saved [messageId: " + (isNew ? "new" : message.getId().toString()) + "]");
          }
        }
        
        message.setModified(new Date());
        message.setModifiedBy(getCurrentUser());       
        getHibernateTemplate().saveOrUpdate(message);
        
        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_ADD, getEventMessage(message), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_WRITE, getEventMessage(message), false));
        }

        LOG.info("message " + message.getId() + " saved successfully");
    }

    public void deleteMessage(Message message) {
        long id = message.getId().longValue();
        message.setInReplyTo(null);
        getHibernateTemplate().saveOrUpdate(message);
        try 
				{
        	getSession().flush();
        } 
        catch (Exception e) 
				{
        	e.printStackTrace();
        }
        eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_REMOVE, getEventMessage(message), false));
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


    public void deleteMsgWithChild(final Long messageId)
    {
    	List thisList = new ArrayList();
    	getChildMsgs(messageId, thisList);
    	
    	for(int i=0; i<thisList.size(); i++)
    	{
    		//Message delMessage = getMessageByIdWithAttachments(((Message)thisList.get(i)).getId());
    		//deleteMessage(getMessageById(((Message)thisList.get(i)).getId()));
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
        return "MessageCenter::" + getCurrentUser() + "::" + object.toString();
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
    
    public List findPvtMsgsBySearchText(final String typeUuid, final String searchText,final Date searchFromDate, final Date searchToDate, final Long searchByText,
                final Long searchByAuthor,final Long searchByBody,final Long searchByLabel,final Long searchByDate) {

      LOG.debug("findPvtMsgsBySearchText executing with searchText: " + searchText);

      HibernateCallback hcb = new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException, SQLException {
              Query q = session.getNamedQuery("findPvtMsgsBySearchText");
              q.setParameter("searchText", "%" + searchText + "%");
              q.setParameter("searchByText", searchByText);
              q.setParameter("searchByAuthor", searchByAuthor);
              q.setParameter("searchByBody", searchByBody);
              q.setParameter("searchByLabel", searchByLabel);
              q.setParameter("searchByDate", searchByDate);
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
}
