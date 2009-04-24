/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/ui/PrivateMessageManagerImpl.java $
 * $Id: PrivateMessageManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.component.app.messageforums.ui;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.PrivateMessageRecipient;
import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.site.api.ToolConfiguration;

import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.component.app.messageforums.TestUtil;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageRecipientImpl;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.site.cover.SiteService;

import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.User;

import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


//support for internationalization by huxt
import org.sakaiproject.i18n.InternationalizedMessages;

public class PrivateMessageManagerImpl extends HibernateDaoSupport implements
    PrivateMessageManager
{

  private static final Log LOG = LogFactory
      .getLog(PrivateMessageManagerImpl.class);

  private static final String QUERY_AGGREGATE_COUNT = "findAggregatePvtMsgCntForUserInContext";  
  private static final String QUERY_MESSAGES_BY_USER_TYPE_AND_CONTEXT = "findPrvtMsgsByUserTypeContext";
  private static final String QUERY_MESSAGES_BY_ID_WITH_RECIPIENTS = "findPrivateMessageByIdWithRecipients";
  
  private AreaManager areaManager;
  private MessageForumsMessageManager messageManager;
  private MessageForumsForumManager forumManager;
  private MessageForumsTypeManager typeManager;
  private IdManager idManager;
  private SessionManager sessionManager;  
  private EmailService emailService;
  private ContentHostingService contentHostingService;
  
  
  private static final String MESSAGES_TITLE = "pvt_message_nav";// Mensajes-->Messages/need to be modified to support internationalization
  
  private static final String PVT_RECEIVED = "pvt_received";     // Recibidos ( 0 mensajes )-->Received ( 8 messages - 8 unread )
  private static final String PVT_SENT = "pvt_sent";             // Enviados ( 0 mensajes )--> Sent ( 0 message )
  private static final String PVT_DELETED = "pvt_deleted";       // Borrados ( 0 mensajes )-->Deleted ( 0 message )
  private static final String PVT_DRAFTS = "pvt_drafts";
 
  /** String ids for email footer messsage */
  private static final String EMAIL_FOOTER1 = "pvt_email_footer1";
  private static final String EMAIL_FOOTER2 = "pvt_email_footer2";
  private static final String EMAIL_FOOTER3 = "pvt_email_footer3";
  private static final String EMAIL_FOOTER4 = "pvt_email_footer4";

  public void init()
  {
	LOG.info("init()");
    ;
  }
  
  public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

  public boolean getPrivateAreaEnabled()
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("getPrivateAreaEnabled()");
    }
        
    return areaManager.isPrivateAreaEnabled();

  }

  public void setPrivateAreaEnabled(boolean value)
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("setPrivateAreaEnabled(value: " + value + ")");
    }

  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#isPrivateAreaEnabled()
   */
  public boolean isPrivateAreaEnabled()
  {
    return areaManager.isPrivateAreaEnabled();
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#getPrivateMessageArea()
   */
  public Area getPrivateMessageArea()
  {
    return areaManager.getPrivateArea();    
  }

  public void savePrivateMessageArea(Area area)
  {
    areaManager.saveArea(area);
  }

  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#initializePrivateMessageArea(org.sakaiproject.api.app.messageforums.Area)
   */
  
  
  //==============Need to be modified to support localization huxt
  public PrivateForum initializePrivateMessageArea(Area area, List aggregateList)
  {

    String userId = getCurrentUser();
    
    aggregateList.clear();
    aggregateList.addAll(initializeMessageCounts());
    
    getHibernateTemplate().lock(area, LockMode.NONE);
    
    PrivateForum pf;

    /** create default user forum/topics if none exist */
    if ((pf = forumManager.getPrivateForumByOwnerArea(getCurrentUser(), area)) == null)
    {      
      /** initialize collections */
      //getHibernateTemplate().initialize(area.getPrivateForumsSet());
            
      pf = forumManager.createPrivateForum(getResourceBundleString(MESSAGES_TITLE));
      
      //area.addPrivateForum(pf);
      //pf.setArea(area);
      //areaManager.saveArea(area);
      
      PrivateTopic receivedTopic = forumManager.createPrivateForumTopic(PVT_RECEIVED, true,false,
          userId, pf.getId());     

      PrivateTopic sentTopic = forumManager.createPrivateForumTopic(PVT_SENT, true,false,
          userId, pf.getId());      

      PrivateTopic deletedTopic = forumManager.createPrivateForumTopic(PVT_DELETED, true,false,
          userId, pf.getId());      

      //PrivateTopic draftTopic = forumManager.createPrivateForumTopic("PVT_DRAFTS", true,false,
      //    userId, pf.getId());
    
      /** save individual topics - required to add to forum's topic set */
      forumManager.savePrivateForumTopic(receivedTopic);
      forumManager.savePrivateForumTopic(sentTopic);
      forumManager.savePrivateForumTopic(deletedTopic);
      //forumManager.savePrivateForumTopic(draftTopic);
      
      pf.addTopic(receivedTopic);
      pf.addTopic(sentTopic);
      pf.addTopic(deletedTopic);
      //pf.addTopic(draftTopic);
      pf.setArea(area);  
      
      PrivateForum oldForum;
      if ((oldForum = forumManager.getPrivateForumByOwnerAreaNull(getCurrentUser())) != null)
      {
    		oldForum = initializationHelper(oldForum);
//    		getHibernateTemplate().initialize(oldForum.getTopicsSet());
    		List pvtTopics = oldForum.getTopics();
    		
    		for(int i=0; i<pvtTopics.size(); i++)//reveived deleted sent
    		{
    			PrivateTopic currentTopic = (PrivateTopic) pvtTopics.get(i);
    			if(currentTopic != null)
    			{
    				if(!currentTopic.getTitle().equals(PVT_RECEIVED) && !currentTopic.getTitle().equals(PVT_SENT) && !currentTopic.getTitle().equals(PVT_DELETED) 
    						&& !currentTopic.getTitle().equals(PVT_DRAFTS) && area.getContextId().equals(currentTopic.getContextId()))
    				{
    					currentTopic.setPrivateForum(pf);
    		      forumManager.savePrivateForumTopic(currentTopic);
    					pf.addTopic(currentTopic);
    				}
    			}
    		}
    		if(oldForum.getAutoForward() != null)
    		{
    			pf.setAutoForward(oldForum.getAutoForward());
    		}
    		if(oldForum.getAutoForwardEmail() != null)
    		{
    			pf.setAutoForwardEmail(oldForum.getAutoForwardEmail());
    		}      
      }
      
      forumManager.savePrivateForum(pf);            
      
    }    
    else{      
       //getHibernateTemplate().initialize(pf.getTopicsSet());   
    	 pf = forumManager.getPrivateForumByOwnerAreaWithAllTopics(getCurrentUser(), area);
    }
   
    return pf;
  }
  
  public PrivateForum initializationHelper(PrivateForum forum){
    
    /** reget to load topic foreign keys */
  	//PrivateForum pf = forumManager.getPrivateForumByOwnerAreaNull(getCurrentUser());
    //getHibernateTemplate().initialize(pf.getTopicsSet());    
  	PrivateForum pf = forumManager.getPrivateForumByOwnerAreaNullWithAllTopics(getCurrentUser());
    return pf;
  }

  public PrivateForum initializationHelper(PrivateForum forum, Area area){
    
    /** reget to load topic foreign keys */
  	//PrivateForum pf = forumManager.getPrivateForumByOwnerArea(getCurrentUser(), area);
    //getHibernateTemplate().initialize(pf.getTopicsSet());    
  	PrivateForum pf = forumManager.getPrivateForumByOwnerAreaWithAllTopics(getCurrentUser(), area);
    return pf;
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#savePrivateMessage(org.sakaiproject.api.app.messageforums.Message)
   */
  public void savePrivateMessage(Message message)
  {
    messageManager.saveMessage(message);
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#savePrivateMessage(org.sakaiproject.api.app.messageforums.Message, boolean logEvent)
   */
  public void savePrivateMessage(Message message, boolean logEvent)
  {
	  messageManager.saveMessage(message, logEvent);
  }
  
  public Message getMessageById(Long id)
  {
    return messageManager.getMessageById(id);
  }

  //Attachment
  public Attachment createPvtMsgAttachment(String attachId, String name)
  {
    try
    {
      Attachment attach = messageManager.createAttachment();

      attach.setAttachmentId(attachId);

      attach.setAttachmentName(name);

      ContentResource cr = contentHostingService.getResource(attachId);
      attach.setAttachmentSize((new Integer(cr.getContentLength())).toString());
      attach.setCreatedBy(cr.getProperties().getProperty(
          cr.getProperties().getNamePropCreator()));
      attach.setModifiedBy(cr.getProperties().getProperty(
          cr.getProperties().getNamePropModifiedBy()));
      attach.setAttachmentType(cr.getContentType());
      String tempString = cr.getUrl();
      String newString = new String();
      char[] oneChar = new char[1];
      for (int i = 0; i < tempString.length(); i++)
      {
        if (tempString.charAt(i) != ' ')
        {
          oneChar[0] = tempString.charAt(i);
          String concatString = new String(oneChar);
          newString = newString.concat(concatString);
        }
        else
        {
          newString = newString.concat("%20");
        }
      }
      //tempString.replaceAll(" ", "%20");
      //attach.setAttachmentUrl(newString);
      attach.setAttachmentUrl("/url");

      return attach;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  // Himansu: I am not quite sure this is what you want... let me know.
  // Before saving a message, we need to add all the attachmnets to a perticular message
  public void addAttachToPvtMsg(PrivateMessage pvtMsgData,
      Attachment pvtMsgAttach)
  {
    pvtMsgData.addAttachment(pvtMsgAttach);
  }

  // Required for editing multiple attachments to a message. 
  // When you reply to a message, you do have option to edit attachments to a message
  public void removePvtMsgAttachment(Attachment o)
  {
    o.getMessage().removeAttachment(o);
  }

  public Attachment getPvtMsgAttachment(Long pvtMsgAttachId)
  {
    return messageManager.getAttachmentById(pvtMsgAttachId);
  }

  public int getTotalNoMessages(Topic topic)
  {
    return messageManager.findMessageCountByTopicId(topic.getId());
  }

  public int getUnreadNoMessages(Topic topic)
  {
    return messageManager.findUnreadMessageCountByTopicId(topic.getId());
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#saveAreaAndForumSettings(org.sakaiproject.api.app.messageforums.Area, org.sakaiproject.api.app.messageforums.PrivateForum)
   */
  public void saveAreaAndForumSettings(Area area, PrivateForum forum)
  {

    /** method calls placed in this function to participate in same transaction */
           
    saveForumSettings(forum);

    /** need to evict forum b/c area saves fk on forum (which places two objects w/same id in session */
    //getHibernateTemplate().evict(forum);
    
    if (isInstructor() || isEmailPermit()){
      savePrivateMessageArea(area);
    }
    
  }

  public void saveForumSettings(PrivateForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveForumSettings(forum: " + forum + ")");
    }

    if (forum == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }

    forumManager.savePrivateForum(forum);
  }

  /**
   * Topic Folder Setting
   */
  public boolean isMutableTopicFolder(String parentTopicId)
  {
    return false;
  }
  
  public void createTopicFolderInForum(PrivateForum pf, String folderName)
  {
    String userId = getCurrentUser();
    PrivateTopic createdTopic = forumManager.createPrivateForumTopic(folderName, true,true,
        userId, pf.getId()); 
    
    /** set context and type to differentiate user topics within sites */
    createdTopic.setContextId(getContextId());
    createdTopic.setTypeUuid(typeManager.getUserDefinedPrivateTopicType());
    
    forumManager.savePrivateForumTopic(createdTopic);
    pf.addTopic(createdTopic);   
    forumManager.savePrivateForum(pf);  
  }

  public void createTopicFolderInTopic(PrivateForum pf, PrivateTopic parentTopic, String folderName)
  {
    String userId = getCurrentUser();
    PrivateTopic createdTopic = forumManager.createPrivateForumTopic(folderName, true,true,
        userId, pf.getId()); 
    createdTopic.setParentTopic(parentTopic);
    forumManager.savePrivateForumTopic(createdTopic);
    pf.addTopic(createdTopic);    
    forumManager.savePrivateForum(pf);  
  }

  public void renameTopicFolder(PrivateForum pf, String topicUuid, String newName)
  {
    String userId = getCurrentUser();
    List pvtTopics= pf.getTopics();
    for (Iterator iter = pvtTopics.iterator(); iter.hasNext();)
    {
      PrivateTopic element = (PrivateTopic) iter.next();
      if(element.getUuid().equals(topicUuid))
      {
        element.setTitle(newName);
        element.setModifiedBy(userId);
        element.setModified(new Date());
        
        EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FOLDER_REVISE, getEventMessage(element), false));
      }      
    }
    forumManager.savePrivateForum(pf);
  }

  public void deleteTopicFolder(PrivateForum pf,String topicUuid)
  {
    List pvtTopics= pf.getTopics();
    for (Iterator iter = pvtTopics.iterator(); iter.hasNext();)
    {
      PrivateTopic element = (PrivateTopic) iter.next();
      if(element.getUuid().equals(topicUuid))
      {
        pf.removeTopic(element);

        EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FOLDER_REMOVE, getEventMessage(element), false));
        break;
      }
    }
    forumManager.savePrivateForum(pf);  
  }

  /**
   * Return Topic based on uuid 
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#getTopicByIdWithMessages(java.lang.Long)
   */
  public Topic getTopicByUuid(final String topicUuid)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicByIdWithMessages(final Long" + topicUuid + ")");
    }
    return forumManager.getTopicByUuid(topicUuid);
  }
  
  public static final String PVTMSG_MODE_RECEIVED = "pvt_received";
  public static final String PVTMSG_MODE_SENT = "pvt_sent";
  public static final String PVTMSG_MODE_DELETE = "pvt_deleted";
  public static final String PVTMSG_MODE_DRAFT = "pvt_drafts";

  public void movePvtMsgTopic(PrivateMessage message, Topic oldTopic, Topic newTopic)
  {
    List recipients= message.getRecipients();
    //get new topic type uuid
    String newTopicTypeUuid=getTopicTypeUuid(newTopic.getTitle());
    //get pld topic type uuid
    String oldTopicTypeUuid=getTopicTypeUuid(oldTopic.getTitle());
    
    //now set the recipiant with new topic type uuid
    for (Iterator iter = recipients.iterator(); iter.hasNext();)
    {
      PrivateMessageRecipient element = (PrivateMessageRecipient) iter.next();
      LOG.info("element.getTypeUuid(): "+element.getTypeUuid()+", oldTopicTypeUuid: "+oldTopicTypeUuid+", element.getUserId(): "+element.getUserId()+ ", getCurrentUser(): "+getCurrentUser());
      if (element.getTypeUuid().equals(oldTopicTypeUuid) && (element.getUserId().equals(getCurrentUser())))
      {
        element.setTypeUuid(newTopicTypeUuid);
      }
    }
    savePrivateMessage(message, false);
    
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#createPrivateMessage(java.lang.String)
   */
  public PrivateMessage createPrivateMessage(String typeUuid)
  {
    PrivateMessage message = new PrivateMessageImpl();
    message.setUuid(idManager.createUuid());
    message.setTypeUuid(typeUuid);
    message.setCreated(new Date());
    message.setCreatedBy(getCurrentUser());

    LOG.info("message " + message.getUuid() + " created successfully");
    return message;
  }

  public boolean hasNextMessage(PrivateMessage message)
  {
    // TODO: Needs optimized
    boolean next = false;
    if (message != null && message.getTopic() != null
        && message.getTopic().getMessages() != null)
    {
      for (Iterator iter = message.getTopic().getMessages().iterator(); iter
          .hasNext();)
      {
        Message m = (Message) iter.next();
        if (next)
        {
          return true;
        }
        if (m.getId().equals(message.getId()))
        {
          next = true;
        }
      }
    }

    // if we get here, there is no next message
    return false;
  }

  public boolean hasPreviousMessage(PrivateMessage message)
  {
    // TODO: Needs optimized
    PrivateMessage prev = null;
    if (message != null && message.getTopic() != null
        && message.getTopic().getMessages() != null)
    {
      for (Iterator iter = message.getTopic().getMessages().iterator(); iter
          .hasNext();)
      {
        Message m = (Message) iter.next();
        if (m.getId().equals(message.getId()))
        {
          // need to check null because we might be on the first message
          // which means there is no previous one
          return prev != null;
        }
        prev = (PrivateMessage) m;
      }
    }

    // if we get here, there is no previous message
    return false;
  }

  public PrivateMessage getNextMessage(PrivateMessage message)
  {
    // TODO: Needs optimized
    boolean next = false;
    if (message != null && message.getTopic() != null
        && message.getTopic().getMessages() != null)
    {
      for (Iterator iter = message.getTopic().getMessages().iterator(); iter
          .hasNext();)
      {
        Message m = (Message) iter.next();
        if (next)
        {
          return (PrivateMessage) m;
        }
        if (m.getId().equals(message.getId()))
        {
          next = true;
        }
      }
    }

    // if we get here, there is no next message
    return null;
  }

  public PrivateMessage getPreviousMessage(PrivateMessage message)
  {
    // TODO: Needs optimized
    PrivateMessage prev = null;
    if (message != null && message.getTopic() != null
        && message.getTopic().getMessages() != null)
    {
      for (Iterator iter = message.getTopic().getMessages().iterator(); iter
          .hasNext();)
      {
        Message m = (Message) iter.next();
        if (m.getId().equals(message.getId()))
        {
          return prev;
        }
        prev = (PrivateMessage) m;
      }
    }

    // if we get here, there is no previous message
    return null;
  }

  public List getMessagesByTopic(String userId, Long topicId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public List getReceivedMessages(String orderField, String order)
  {
    return getMessagesByType(typeManager.getReceivedPrivateMessageType(),
        orderField, order);
  }

  public List getSentMessages(String orderField, String order)
  {
    return getMessagesByType(typeManager.getSentPrivateMessageType(),
        orderField, order);    
  }

  public List getDeletedMessages(String orderField, String order)
  {
    return getMessagesByType(typeManager.getDeletedPrivateMessageType(),
        orderField, order);
  }

  public List getDraftedMessages(String orderField, String order)
  {
    return getMessagesByType(typeManager.getDraftPrivateMessageType(),
        orderField, order);
  }
    
  public PrivateMessage initMessageWithAttachmentsAndRecipients(PrivateMessage msg){
    
    PrivateMessage pmReturn = (PrivateMessage) messageManager.getMessageByIdWithAttachments(msg.getId());    
    getHibernateTemplate().initialize(pmReturn.getRecipients());
    return pmReturn;
  }
  
  /**
   * helper method to get messages by type
   * @param typeUuid
   * @return message list
   */
  public List getMessagesByType(final String typeUuid, final String orderField,
      final String order)
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("getMessagesByType(typeUuid:" + typeUuid + ", orderField: "
          + orderField + ", order:" + order + ")");
    }

    //    HibernateCallback hcb = new HibernateCallback() {
    //      public Object doInHibernate(Session session) throws HibernateException, SQLException {
    //        Criteria messageCriteria = session.createCriteria(PrivateMessageImpl.class);
    //        Criteria recipientCriteria = messageCriteria.createCriteria("recipients");
    //        
    //        Conjunction conjunction = Expression.conjunction();
    //        conjunction.add(Expression.eq("userId", getCurrentUser()));
    //        conjunction.add(Expression.eq("typeUuid", typeUuid));        
    //        
    //        recipientCriteria.add(conjunction);
    //        
    //        if ("asc".equalsIgnoreCase(order)){
    //          messageCriteria.addOrder(Order.asc(orderField));
    //        }
    //        else if ("desc".equalsIgnoreCase(order)){
    //          messageCriteria.addOrder(Order.desc(orderField));
    //        }
    //        else{
    //          LOG.debug("getMessagesByType failed with (typeUuid:" + typeUuid + ", orderField: " + orderField +
    //              ", order:" + order + ")");
    //          throw new IllegalArgumentException("order must have value asc or desc");          
    //        }
    //        
    //        //todo: parameterize fetch mode
    //        messageCriteria.setFetchMode("recipients", FetchMode.EAGER);
    //        messageCriteria.setFetchMode("attachments", FetchMode.EAGER);
    //        
    //        return messageCriteria.list();        
    //      }
    //    };

    HibernateCallback hcb = new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException
      {
        Query q = session.getNamedQuery(QUERY_MESSAGES_BY_USER_TYPE_AND_CONTEXT);
        Query qOrdered = session.createQuery(q.getQueryString() + " order by "
            + orderField + " " + order);

        qOrdered.setParameter("userId", getCurrentUser(), Hibernate.STRING);
        qOrdered.setParameter("typeUuid", typeUuid, Hibernate.STRING);
        qOrdered.setParameter("contextId", getContextId(), Hibernate.STRING);
        return qOrdered.list();
      }
    };

    return (List) getHibernateTemplate().execute(hcb);        
  }
  
  /**
   * FOR SYNOPTIC TOOL:
   * 	helper method to get messages by type
   * 	needed to pass contextId since could be in MyWorkspace
   * 
   * @param typeUuid
   * 			The type of forum it is (Private or Topic)
   * @param contextId
   * 			The site id whose messages are needed
   * 
   * @return message list
   */
  public List getMessagesByTypeByContext(final String typeUuid, final String contextId)
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("getMessagesByTypeForASite(typeUuid:" + typeUuid + ")");
    }

    HibernateCallback hcb = new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException
      {
        Query q = session.getNamedQuery(QUERY_MESSAGES_BY_USER_TYPE_AND_CONTEXT);

        q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
        q.setParameter("typeUuid", typeUuid, Hibernate.STRING);
        q.setParameter("contextId", contextId, Hibernate.STRING);
        return q.list();
      }
    };

    return (List) getHibernateTemplate().execute(hcb);        
  }
  

    /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#findMessageCount(java.lang.String)
   */
  public int findMessageCount(String typeUuid, List aggregateList)
  {    
    if (LOG.isDebugEnabled())
    {
      LOG.debug("findMessageCount executing with typeUuid: " + typeUuid);
    }

    if (typeUuid == null)
    {
      LOG.error("findMessageCount failed with typeUuid: " + typeUuid);
      throw new IllegalArgumentException("Null Argument");
    }    
    
    if (aggregateList == null)
    {
      LOG.error("findMessageCount failed with aggregateList: " + aggregateList);
      throw new IllegalStateException("aggregateList is null");
    }
    
    int totalCount = 0;
    for (Iterator i = aggregateList.iterator(); i.hasNext();){
      Object[] element = (Object[]) i.next();
      /** filter on type */
      if (typeUuid.equals(element[1])){        
        /** add read/unread message types */        
        totalCount += ((Integer) element[2]).intValue(); 
      }      
    }
            
    return totalCount;
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#findUnreadMessageCount(java.lang.String)
   */
  public int findUnreadMessageCount(String typeUuid, List aggregateList)
  {    
    if (LOG.isDebugEnabled())
    {
      LOG.debug("findUnreadMessageCount executing with typeUuid: " + typeUuid);
    }

    if (typeUuid == null)
    {
      LOG.error("findUnreadMessageCount failed with typeUuid: " + typeUuid);
      throw new IllegalArgumentException("Null Argument");
    }    
    
    if (aggregateList == null)
    {
      LOG.error("findMessageCount failed with aggregateList: " + aggregateList);
      throw new IllegalStateException("aggregateList is null");
    }
    
    int unreadCount = 0;
    for (Iterator i = aggregateList.iterator(); i.hasNext();){
      Object[] element = (Object[]) i.next();
      /** filter on type and read status*/
      if (!typeUuid.equals(element[1]) || Boolean.TRUE.equals(element[0])){
        continue;
      }
      else{        
        unreadCount = ((Integer) element[2]).intValue();
        break;
      }      
    }
            
    return unreadCount;    
  }
  
  /**
   * initialize message counts
   * @param typeUuid
   */
  private List initializeMessageCounts()
  {    
    if (LOG.isDebugEnabled())
    {
      LOG.debug("initializeMessageCounts executing");
    }

    HibernateCallback hcb = new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException
      {
        Query q = session.getNamedQuery(QUERY_AGGREGATE_COUNT);        
        q.setParameter("contextId", getContextId(), Hibernate.STRING);
        q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
        return q.list();
      }
    };
        
    return (List) getHibernateTemplate().execute(hcb);        
  }


  /**
   * FOR SYNOPTIC TOOL:
   * 	Returns a list of all sites this user is in along with a count of his/her
   * 	unread messages
   * 
   * @return
   * 	List of site id, count of unread message pairs
   */
  public List getPrivateMessageCountsForAllSites() {
	  HibernateCallback hcb = new HibernateCallback() {
		  public Object doInHibernate(Session session) throws HibernateException,
	  	 	SQLException
	  	 {
			  Query q = session.getNamedQuery("findUnreadPvtMsgCntByUserForAllSites");
			  q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
			  return q.list();
	  	 }
	  };
  
	  return (List) getHibernateTemplate().execute(hcb);
	  
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#deletePrivateMessage(org.sakaiproject.api.app.messageforums.PrivateMessage, java.lang.String)
   */
  public void deletePrivateMessage(PrivateMessage message, String typeUuid)
  {

    String userId = getCurrentUser();

    if (LOG.isDebugEnabled())
    {
      LOG.debug("deletePrivateMessage(message:" + message + ", typeUuid:"
          + typeUuid + ")");
    }

    /** fetch recipients for message */
    PrivateMessage pvtMessage = getPrivateMessageWithRecipients(message);

    /**
     *  create PrivateMessageRecipient to search
     */
    PrivateMessageRecipient pmrReadSearch = new PrivateMessageRecipientImpl(
        userId, typeUuid, getContextId(), Boolean.TRUE);

    PrivateMessageRecipient pmrNonReadSearch = new PrivateMessageRecipientImpl(
        userId, typeUuid, getContextId(), Boolean.FALSE);

    int indexDelete = -1;
    int indexRead = pvtMessage.getRecipients().indexOf(pmrReadSearch);
    if (indexRead != -1)
    {
      indexDelete = indexRead;
    }
    else
    {
      int indexNonRead = pvtMessage.getRecipients().indexOf(pmrNonReadSearch);
      if (indexNonRead != -1)
      {
        indexDelete = indexNonRead;
      }
      else
      {
        LOG
            .error("deletePrivateMessage -- cannot find private message for user: "
                + userId + ", typeUuid: " + typeUuid);
      }
    }

    if (indexDelete != -1)
    {
      PrivateMessageRecipient pmrReturned = (PrivateMessageRecipient) pvtMessage
          .getRecipients().get(indexDelete);

      if (pmrReturned != null)
      {

        /** check for existing deleted message from user */
        PrivateMessageRecipient pmrDeletedSearch = new PrivateMessageRecipientImpl(
            userId, typeManager.getDeletedPrivateMessageType(), getContextId(),
            Boolean.TRUE);

        int indexDeleted = pvtMessage.getRecipients().indexOf(pmrDeletedSearch);

        if (indexDeleted == -1)
        {
          pmrReturned.setRead(Boolean.TRUE);
          pmrReturned.setTypeUuid(typeManager.getDeletedPrivateMessageType());
        }
        else
        {
          pvtMessage.getRecipients().remove(indexDelete);
        }
      }
    }
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#sendPrivateMessage(org.sakaiproject.api.app.messageforums.PrivateMessage, java.util.Set, boolean)
   */
  public void sendPrivateMessage(PrivateMessage message, Set recipients, boolean asEmail)
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("sendPrivateMessage(message: " + message + ", recipients: "
          + recipients + ")");
    }

    if (message == null || recipients == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }

    if (recipients.size() == 0)
    {
      /** for no just return out
        throw new IllegalArgumentException("Empty recipient list");
      **/
      return;
    }

    String currentUserAsString = getCurrentUser();
    User currentUser = UserDirectoryService.getCurrentUser();
    List recipientList = new UniqueArrayList();

    /** test for draft message */
    if (message.getDraft().booleanValue())
    {
      PrivateMessageRecipientImpl receiver = new PrivateMessageRecipientImpl(
      		currentUserAsString, typeManager.getDraftPrivateMessageType(),
          getContextId(), Boolean.TRUE);

      recipientList.add(receiver);
      message.setRecipients(recipientList);
      savePrivateMessage(message, false);
      return;
    }

    //build the message body
    List additionalHeaders = new ArrayList(1);
    additionalHeaders.add("Content-Type: text/html");
    

    /** determines if default in sakai.properties is set, if not will make a reasonable default */
    String defaultEmail = "postmaster@" + ServerConfigurationService.getServerName();
    String systemEmail = ServerConfigurationService.getString("msgcntr.notification.from.address", defaultEmail);
   
    if (!ServerConfigurationService.getBoolean("msgcntr.notification.user.real.from", false)) {
    	systemEmail = ServerConfigurationService.getString("msgcntr.notification.from.address", defaultEmail);
    } else  {
    	if (currentUser.getEmail() != null)
    		systemEmail = currentUser.getEmail();
    	else
    		systemEmail = ServerConfigurationService.getString("msgcntr.notification.from.address", defaultEmail);
    }
    
    String bodyString = buildMessageBody(message);
    
    Area currentArea = null;
    List<PrivateForum> privateForums = null;
    Map<String, PrivateForum> pfMap = null;
    
    Set forwardRecipients = new HashSet();
    
    if (!asEmail) {
    	currentArea = getAreaByContextIdAndTypeId(typeManager.getPrivateMessageAreaType());

    	privateForums = currentArea.getPrivateForums();

    	//create a map for efficient lookup for large sites
    	pfMap = new HashMap<String, PrivateForum>();
    	for (int i = 0; i < privateForums.size(); i++) {
    		PrivateForum pf1 = (PrivateForum)privateForums.get(i);
    		pfMap.put(pf1.getOwner(), pf1);
    	}
    	
    	boolean forwardingEnable = false;
    	String forwardAddress = null;
    	
    	PrivateForum pf = null;
    	if (pfMap.containsKey(currentUser.getId())) {   		
    		pf = (PrivateForum)pfMap.get(currentUser.getId());
    	}
    	
    	if (pf != null && pf.getAutoForward().booleanValue()){
    		forwardingEnable = true;
			forwardAddress = pf.getAutoForwardEmail();			
		}
    	
		if( pf == null)  
		{
			//only check for default settings if the pf is null
			PrivateForum oldPf = forumManager.getPrivateForumByOwnerAreaNull(currentUser.getId());
			if (oldPf != null && oldPf.getAutoForward().booleanValue()) {
				forwardAddress = oldPf.getAutoForwardEmail();
				forwardingEnable = true;				
			}
		}
		
		if (forwardingEnable){
			forwardRecipients.add(currentUser);
			additionalHeaders.add("From: " + systemEmail);
	    	additionalHeaders.add("Subject: " + message.getTitle());
	    	emailService.sendToUsers(forwardRecipients, additionalHeaders, bodyString);
		}    	

    	//this only needs to be done if the message is not being sent
		for (Iterator i = recipients.iterator(); i.hasNext();)
		{
			User u = (User) i.next();      
			String userId = u.getId();

			/** determine if current user is equal to recipient */
			Boolean isRecipientCurrentUser = 
				(currentUserAsString.equals(userId) ? Boolean.TRUE : Boolean.FALSE);      

			PrivateMessageRecipientImpl receiver = new PrivateMessageRecipientImpl(
					userId, typeManager.getReceivedPrivateMessageType(), getContextId(),
					isRecipientCurrentUser);
			recipientList.add(receiver);

		}

    } else {
    	//send as 1 action to all recipients
    	//we need to add som headers
    	additionalHeaders.add("From: " + systemEmail);
    	additionalHeaders.add("Subject: " + message.getTitle());
    	emailService.sendToUsers(recipients, additionalHeaders, bodyString);
    	
    	//After send message to the campus email, send message to Sakai Message recipient(s)'s receive folder.
    	for (Iterator i = recipients.iterator(); i.hasNext();) {
    		User u = (User) i.next();      
    		String userId = u.getId();

    		/** determine if current user is equal to recipient */
    		Boolean isRecipientCurrentUser = 
    			(currentUserAsString.equals(userId) ? Boolean.TRUE : Boolean.FALSE);      


    		PrivateMessageRecipientImpl receiver = new PrivateMessageRecipientImpl(
    				userId, typeManager.getReceivedPrivateMessageType(), getContextId(),
    				isRecipientCurrentUser);
    		recipientList.add(receiver);
    	}   	

    }
    
    
    
    /** add sender as a saved recipient */
    PrivateMessageRecipientImpl sender = new PrivateMessageRecipientImpl(
    		currentUserAsString, typeManager.getSentPrivateMessageType(),
        getContextId(), Boolean.TRUE);

    recipientList.add(sender);

    message.setRecipients(recipientList);

    savePrivateMessage(message, false);
  }


  private String buildMessageBody(PrivateMessage message) {
	  User currentUser = UserDirectoryService.getCurrentUser();
	  StringBuilder body = new StringBuilder(message.getBody());

	  body.insert(0, "From: " + currentUser.getDisplayName() + "<p/>"); 

	  // need to filter out hidden users if there are any and:
	  //   a non-instructor (! site.upd)
	  //   instructor but not the author
	  String sendToString = message.getRecipientsAsText();
	  if (sendToString.indexOf("(") > 0 && (! isInstructor() || !isEmailPermit()|| (!message.getAuthor().equals(getAuthorString()))) ) {
		  sendToString = sendToString.substring(0, sendToString.indexOf("("));
	  }

	  body.insert(0, "To: " + sendToString + "<p/>");

	  if (message.getAttachments() != null && message.getAttachments().size() > 0) {

		  body.append("<br/><br/>");
		  for (Iterator iter = message.getAttachments().iterator(); iter.hasNext();) {
			  Attachment attachment = (Attachment) iter.next();
			  //body.append("<a href=\"" + attachment.getAttachmentUrl() +
			  //"\">" + attachment.getAttachmentName() + "</a><br/>");            
			  body.append("<a href=\"" + messageManager.getAttachmentUrl(attachment.getAttachmentId()) +
					  "\">" + attachment.getAttachmentName() + "</a><br/>");            
		  }
	  }

	  String siteTitle = null;
	  try{
		  siteTitle = SiteService.getSite(getContextId()).getTitle();
	  }
	  catch (IdUnusedException e){
		  LOG.error(e.getMessage(), e);
	  }

	  String thisPageId = "";
	  ToolSession ts = sessionManager.getCurrentToolSession();
	  if (ts != null)
	  {
		  ToolConfiguration tool = SiteService.findTool(ts.getPlacementId());
		  if (tool != null)
		  {
			  thisPageId = tool.getPageId();
		  }
	  }

	  String footer = "<p>----------------------<br>" +
	  getResourceBundleString(EMAIL_FOOTER1) + " " + ServerConfigurationService.getString("ui.service") +
	  " " + getResourceBundleString(EMAIL_FOOTER2) + " \"" +
	  siteTitle + "\" " + getResourceBundleString(EMAIL_FOOTER3) + "\n" +
	  getResourceBundleString(EMAIL_FOOTER4) +
	  " <a href=\"" +
	  ServerConfigurationService.getPortalUrl() + 
	  "/site/" + ToolManager.getCurrentPlacement().getContext() +
	  "/page/" + thisPageId+
	  "\">";


	  footer += siteTitle + "</a>.</p>";                      
	  body.append(footer);

	  String bodyString = body.toString();
	  return bodyString;
  }


  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#markMessageAsReadForUser(org.sakaiproject.api.app.messageforums.PrivateMessage)
   */
  public void markMessageAsReadForUser(final PrivateMessage message)
  {
	  markMessageAsReadForUser(message, getContextId());
   }

  /**
   * FOR SYNOPTIC TOOL:
   * 	Need to pass in contextId also
   */
  public void markMessageAsReadForUser(final PrivateMessage message, final String contextId)
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("markMessageAsReadForUser(message: " + message + ")");
    }

    if (message == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }

    final String userId = getCurrentUser();

    /** fetch recipients for message */
    PrivateMessage pvtMessage = getPrivateMessageWithRecipients(message);

    /** create PrivateMessageRecipientImpl to search for recipient to update */
    PrivateMessageRecipientImpl searchRecipient = new PrivateMessageRecipientImpl(
        userId, typeManager.getReceivedPrivateMessageType(), contextId,
        Boolean.FALSE);

    List recipientList = pvtMessage.getRecipients();

    if (recipientList == null || recipientList.size() == 0)
    {
      LOG.error("markMessageAsReadForUser(message: " + message
          + ") has empty recipient list");
      throw new Error("markMessageAsReadForUser(message: " + message
          + ") has empty recipient list");
    }
    
    int recordIndex = -1;
    for(int i  = 0; i < pvtMessage.getRecipients().size(); i++) {
    	if(((PrivateMessageRecipientImpl) pvtMessage.getRecipients().get(i)).getUserId().equals(searchRecipient.getUserId())){
    		recordIndex = i;
    	}      
    }
    
    if (recordIndex != -1)
    {
    	if (! ((PrivateMessageRecipientImpl) recipientList.get(recordIndex)).getRead()) {
    		((PrivateMessageRecipientImpl) recipientList.get(recordIndex)).setRead(Boolean.TRUE);
    		
      	  EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_READ, getEventMessage(pvtMessage), false));
    	}
    }
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#markMessageAsReadForUser(org.sakaiproject.api.app.messageforums.PrivateMessage)
   */

  public void markMessageAsUnreadForUser(final PrivateMessage message)

  {
	  markMessageAsUnreadForUser(message, getContextId());
  }


  /**
   * FOR SYNOPTIC TOOL:
   * 	Need to pass in contextId also
   */

  public void markMessageAsUnreadForUser(final PrivateMessage message,
		  final String contextId) {

	  if (LOG.isDebugEnabled()) {
		  LOG.debug("markMessageAsUnreadForUser(message: " + message + ")");
	  }

	  if (message == null) {
		  throw new IllegalArgumentException("Null Argument");
	  }

	  final String userId = getCurrentUser();

	  /** fetch recipients for message */

	  PrivateMessage pvtMessage = getPrivateMessageWithRecipients(message);

	  /** create PrivateMessageRecipientImpl to search for recipient to update */
	  PrivateMessageRecipientImpl searchRecipient = new PrivateMessageRecipientImpl(
			  userId, typeManager.getReceivedPrivateMessageType(), contextId,
			  Boolean.TRUE);

	  List recipientList = pvtMessage.getRecipients();

	  if (recipientList == null || recipientList.size() == 0)
	  {
		  LOG.error("markMessageAsUnreadForUser(message: " + message
				  + ") has empty recipient list");
		  throw new Error("markMessageAsUnreadForUser(message: " + message
				  + ") has empty recipient list");

	  }

	  int recordIndex = -1;
	  for(int i  = 0; i < pvtMessage.getRecipients().size(); i++) {
		  if(((PrivateMessageRecipientImpl) pvtMessage.getRecipients().get(i)).getUserId().equals(searchRecipient.getUserId())){
			  recordIndex = i;
		  }      
	  }

	  if (recordIndex != -1)
	  {
		  if (((PrivateMessageRecipientImpl) recipientList.get(recordIndex))
				  .getRead()) {
			  ((PrivateMessageRecipientImpl) recipientList.get(recordIndex))
			  .setRead(Boolean.FALSE);
			  EventTrackingService.post(EventTrackingService.newEvent(
					  DiscussionForumService.EVENT_MESSAGES_UNREAD,
					  getEventMessage(pvtMessage), false));
		  }
	  }
  }

  

  private PrivateMessage getPrivateMessageWithRecipients(
      final PrivateMessage message)
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("getPrivateMessageWithRecipients(message: " + message + ")");
    }

    if (message == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }

    HibernateCallback hcb = new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException
      {
        Query q = session.getNamedQuery(QUERY_MESSAGES_BY_ID_WITH_RECIPIENTS);
        q.setParameter("id", message.getId(), Hibernate.LONG);
        return q.uniqueResult();
      }
    };

    PrivateMessage pvtMessage = (PrivateMessage) getHibernateTemplate()
        .execute(hcb);

    if (pvtMessage == null)
    {
      LOG.error("getPrivateMessageWithRecipients(message: " + message
          + ") could not find message");
      throw new Error("getPrivateMessageWithRecipients(message: " + message
          + ") could not find message");
    }

    return pvtMessage;
  }
    
  
  public List searchPvtMsgs(String typeUuid, String searchText,Date searchFromDate, Date searchToDate, 
      boolean searchByText, boolean searchByAuthor, boolean searchByBody, boolean searchByLabel, boolean searchByDate)
  {    
    return messageManager.findPvtMsgsBySearchText(typeUuid, searchText,searchFromDate, searchToDate, 
        searchByText,searchByAuthor,searchByBody,searchByLabel,searchByDate);
  }

  public String getAuthorString() {
      String authorString = getCurrentUser();
      
      try
      {
        authorString = UserDirectoryService.getUser(authorString).getSortName();

      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
      
      return authorString;
   }
   
  private String getCurrentUser()
  {
    if (TestUtil.isRunningTests())
    {
      return "test-user";
    }
    return sessionManager.getCurrentSessionUserId();
  }

  public AreaManager getAreaManager()
  {
    return areaManager;
  }

  public void setAreaManager(AreaManager areaManager)
  {
    this.areaManager = areaManager;
  }

  public MessageForumsMessageManager getMessageManager()
  {
    return messageManager;
  }

  public void setMessageManager(MessageForumsMessageManager messageManager)
  {
    this.messageManager = messageManager;
  }

  public void setTypeManager(MessageForumsTypeManager typeManager)
  {
    this.typeManager = typeManager;
  }

  public void setSessionManager(SessionManager sessionManager)
  {
    this.sessionManager = sessionManager;
  }

  public void setIdManager(IdManager idManager)
  {
    this.idManager = idManager;
  }
  
  public void setForumManager(MessageForumsForumManager forumManager)
  {
    this.forumManager = forumManager;
  }

  public void setEmailService(EmailService emailService)
  {
    this.emailService = emailService;
  }
    
  
  public boolean isInstructor()
  {
    LOG.debug("isInstructor()");
    return isInstructor(UserDirectoryService.getCurrentUser());
  }

  /**
   * Check if the given user has site.upd access
   * 
   * @param user
   * @return
   */
  private boolean isInstructor(User user)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isInstructor(User " + user + ")");
    }
    if (user != null)
      return SecurityService.unlock(user, "site.upd", getContextSiteId());
    else
      return false;
  }
  
  public boolean isEmailPermit() {
	  LOG.debug("isEmailPermit()");
	  return isEmailPermit(UserDirectoryService.getCurrentUser());
  }
  
  private boolean isEmailPermit(User user)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isEmailPermit(User " + user + ")");
    }
    if (user != null)
      return SecurityService.unlock(user, "msg.emailout", getContextSiteId());
    else
      return false;
  }
  

  /**
   * @return siteId
   */
  public String getContextSiteId()
  {
    LOG.debug("getContextSiteId()");

    return ("/site/" + ToolManager.getCurrentPlacement().getContext());
  }

  public String getContextId()
  {

    LOG.debug("getContextId()");

    if (TestUtil.isRunningTests())
    {
      return "01001010";
    }
    else
    {
    	//org.sakaiproject.tool.api.ToolManager manager = getInstance();
      return ToolManager.getCurrentPlacement().getContext();
    }
  }  
     
  //Helper class
  public String getTopicTypeUuid(String topicTitle)
  {
    String topicTypeUuid;

    if(PVTMSG_MODE_RECEIVED.equals(topicTitle))
    {
      topicTypeUuid=typeManager.getReceivedPrivateMessageType();
    }
    else if(PVTMSG_MODE_SENT.equals(topicTitle))
    {
      topicTypeUuid=typeManager.getSentPrivateMessageType();
    }
    else if(PVTMSG_MODE_DELETE.equals(topicTitle))
    {
      topicTypeUuid=typeManager.getDeletedPrivateMessageType();
    }
    else if(PVTMSG_MODE_DRAFT.equals(topicTitle))
    {
      topicTypeUuid=typeManager.getDraftPrivateMessageType();
    }
    else
    {
      topicTypeUuid=typeManager.getCustomTopicType(topicTitle);
    }
return topicTypeUuid;
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
   * Gets Strings from Message Bundle (specifically for titles)
   * TODO: pull directly from bundle instead of using areaManager
   * 		as an intermediary
   * 
   * @param key
   * 			Message bundle key for String wanted
   * 
   * @return
   * 			String requested or "[missing key: key]" if not found
   */
  public String getResourceBundleString(String key) 
  {
//	 ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);

      return areaManager.getResourceBundleString(key);
  }

  /**
   * Constructs the event message string
   */
  private String getEventMessage(Object object) {
  	String eventMessagePrefix = "";
  	final String toolId = ToolManager.getCurrentTool().getId();
		  	
	if (toolId.equals(DiscussionForumService.MESSAGE_CENTER_ID))
		eventMessagePrefix = "/messages&Forums/site/";
	else if (toolId.equals(DiscussionForumService.MESSAGES_TOOL_ID))
		eventMessagePrefix = "/messages/site/";
	else
		eventMessagePrefix = "/forums/site/";
  	
  	return eventMessagePrefix + getContextId() + "/" + object.toString() + "/" + getCurrentUser();
	}
}