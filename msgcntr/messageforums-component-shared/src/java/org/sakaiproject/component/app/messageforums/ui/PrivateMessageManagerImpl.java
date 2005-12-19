package org.sakaiproject.component.app.messageforums.ui;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DummyDataHelperApi;
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
import org.sakaiproject.api.kernel.id.IdManager;
import org.sakaiproject.api.kernel.session.SessionManager;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.component.app.messageforums.TestUtil;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageRecipientImpl;
import org.sakaiproject.service.legacy.content.ContentResource;
import org.sakaiproject.service.legacy.content.cover.ContentHostingService;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;



public class PrivateMessageManagerImpl extends HibernateDaoSupport implements
    PrivateMessageManager
{  
  
  private static final Log LOG = LogFactory.getLog(PrivateMessageManagerImpl.class);
  
  private static final String QUERY_COUNT = "findPvtMsgCntByTopicIdAndTypeUuid";
  private static final String QUERY_COUNT_BY_UNREAD = "findUnreadPvtMsgCntByTopicIdAndTypeUuid";
  private static final String QUERY_MESSAGES_BY_TYPE = "findPrivateMessagesByTypeUuid";
  private static final String QUERY_MESSAGES_BY_ID_WITH_RECIPIENTS = "findPrivateMessageByIdWithRecipients";
  
  
  private AreaManager areaManager;
  private MessageForumsMessageManager messageManager;
  private MessageForumsForumManager forumManager;
  private MessageForumsTypeManager typeManager;
  private IdManager idManager;
  private SessionManager sessionManager;
  private DummyDataHelperApi helper;
  private boolean usingHelper = false; // just a flag until moved to database from helper
  private Area privateArea =null;
  public void init()
  {
    ;
  }

  public boolean isPrivateAreaUnabled()
  {
    if (usingHelper)
    {
      return helper.isPrivateAreaUnabled();
    }
    return areaManager.isPrivateAreaEnabled();
  }

  public Area getPrivateMessageArea()
  {
    if (usingHelper)
    {
      return helper.getPrivateArea();
    }

      privateArea = areaManager.getPrivateArea();   
    return privateArea;
    //return areaManager.getPrivateArea();
  }
  
  public PrivateForum initializePrivateMessageArea(Area area){
        
    String userId = getCurrentUser();
    
    PrivateForum pf;
    
    /** create default user forum/topics if none exist */
    if ((pf = forumManager.getForumByOwner(getCurrentUser())) == null){
                  
      pf = forumManager.createPrivateForum();      
      pf.setTitle("private forum");
      pf.setUuid(idManager.createUuid());
      pf.setAutoForwardEmail("");
      pf.setOwner(userId);
      
      forumManager.savePrivateForum(pf);      
      area.addPrivateForum(pf);
      pf.setArea(area);
      areaManager.saveArea(area) ;
      
      PrivateTopic receivedTopic = forumManager.createPrivateForumTopic(true, userId, pf.getId());
      receivedTopic.setTitle("Received");
      forumManager.savePrivateForumTopic(receivedTopic);
              
      PrivateTopic sentTopic = forumManager.createPrivateForumTopic(true, userId, pf.getId());
      sentTopic.setTitle("Sent");    
      forumManager.savePrivateForumTopic(sentTopic);
       
      PrivateTopic deletedTopic = forumManager.createPrivateForumTopic(true, userId, pf.getId());
      deletedTopic.setTitle("Deleted");    
      forumManager.savePrivateForumTopic(deletedTopic);
        
      PrivateTopic draftTopic = forumManager.createPrivateForumTopic(true, userId, pf.getId());
      draftTopic.setTitle("Drafts");    
      forumManager.savePrivateForumTopic(draftTopic);
    
    
      pf.addTopic(receivedTopic);
      pf.addTopic(sentTopic);
      pf.addTopic(deletedTopic);
      pf.addTopic(draftTopic);
      
      forumManager.savePrivateForum(pf);                  
      
    }
            
    return pf;
  }
               
    
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#savePrivateMessage(org.sakaiproject.api.app.messageforums.Message)
   */
  public void savePrivateMessage(Message message)
  {
    messageManager.saveMessage(message);
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

      ContentResource cr = ContentHostingService.getResource(attachId);
      attach.setAttachmentSize((new Integer(cr.getContentLength())).toString());
      attach.setCreatedBy(cr.getProperties().getProperty(cr.getProperties().getNamePropCreator()));
      attach.setModifiedBy(cr.getProperties().getProperty(cr.getProperties().getNamePropModifiedBy()));
      attach.setAttachmentType(cr.getContentType());
      String tempString = cr.getUrl();
      String newString = new String();
      char[] oneChar = new char[1];
      for(int i=0; i<tempString.length(); i++)
      {
        if(tempString.charAt(i) != ' ')
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
      attach.setAttachmentUrl(newString);

      return attach;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  // Himansu: I am not quite sure this is what you want... let me know.
  // Before saving a message, we need to add all the attachmnets to a perticular message
  public void addAttachToPvtMsg(PrivateMessage pvtMsgData, Attachment pvtMsgAttach)
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

  public int getUnreadNoMessages(String userId, Topic topic)
  {
    return messageManager.findUnreadMessageCountByTopicId(userId, topic.getId());
  }

  /**
   * Area Setting
   */
  public void saveAreaSetting()
  {
    // TODO Sace settings like activate /forwarding email
    
  }
  
  /**
   * Topic Folder Setting
   */
  public boolean isMutableTopicFolder(String parentTopicId)
  {
    return false;
  }

  
  public String createTopicFolderInForum(String parentForumId, String userId, String name) {
      return null;
  }
  
  public String createTopicFolderInTopic(String parentTopicId, String userId, String name) {
      return null;
  }
  
  public String renameTopicFolder(String parentTopicId, String userId, String newName)
  {
    return null;
  }
  
  public void deleteTopicFolder(String topicId) {
      
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#createPrivateMessage(java.lang.String)
   */
  public PrivateMessage createPrivateMessage(String typeUuid) {
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
    if (message != null && message.getTopic() != null && message.getTopic().getMessages() != null) {
        for (Iterator iter = message.getTopic().getMessages().iterator(); iter.hasNext();) {
            Message m = (Message) iter.next();
            if (next) {
                return true;
            }
            if (m.getId().equals(message.getId())) {
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
      if (message != null && message.getTopic() != null && message.getTopic().getMessages() != null) {
          for (Iterator iter = message.getTopic().getMessages().iterator(); iter.hasNext();) {
              Message m = (Message) iter.next();
              if (m.getId().equals(message.getId())) {
                  // need to check null because we might be on the first message
                  // which means there is no previous one
                  return prev != null;
              }
              prev = (PrivateMessage)m;
          }
      }

      // if we get here, there is no previous message
      return false; 
  }

  public PrivateMessage getNextMessage(PrivateMessage message)
  {
      // TODO: Needs optimized
      boolean next = false;
      if (message != null && message.getTopic() != null && message.getTopic().getMessages() != null) {
          for (Iterator iter = message.getTopic().getMessages().iterator(); iter.hasNext();) {
              Message m = (Message) iter.next();
              if (next) {
                  return (PrivateMessage) m;
              }
              if (m.getId().equals(message.getId())) {
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
      if (message != null && message.getTopic() != null && message.getTopic().getMessages() != null) {
          for (Iterator iter = message.getTopic().getMessages().iterator(); iter.hasNext();) {
              Message m = (Message) iter.next();
              if (m.getId().equals(message.getId())) {
                  return prev;
              }
              prev = (PrivateMessage)m;
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
  
  /**
   * helper method to get messages by type
   * @param typeUuid
   * @return message list
   */
  public List getMessagesByType(final String typeUuid, final String orderField,
    final String order){

    if (LOG.isDebugEnabled()){
      LOG.debug("getMessagesByType(typeUuid:" + typeUuid + ", orderField: " + orderField +
        ", order:" + order + ")");
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
    
    HibernateCallback hcb = new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Query q = session.getNamedQuery(QUERY_MESSAGES_BY_TYPE);
        Query qOrdered= session.createQuery(
          q.getQueryString() + " order by " + orderField + " " + order);
                
        qOrdered.setParameter("userId", getCurrentUser(), Hibernate.STRING);
        qOrdered.setParameter("typeUuid", typeUuid, Hibernate.STRING);                
        return qOrdered.list();
      }
    };

    List l =  (List) getHibernateTemplate().execute(hcb);
    return l;
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#findMessageCount(java.lang.Long, java.lang.String)
   */
  public int findMessageCount(final Long topicId, final String typeUuid) {
    
    String userId = getCurrentUser();
    
    if (LOG.isDebugEnabled()){
      LOG.debug("findMessageCount executing with topicId: "
        + topicId + ", userId: " + userId + ", typeUuid: " + typeUuid);
    }
    
    if (topicId == null || userId == null || typeUuid == null) {
      LOG.error("findMessageCount failed with topicId: " 
          + topicId + ", uerId: " + userId + ", typeUuid: " + typeUuid);
      throw new IllegalArgumentException("Null Argument");
    }
        
    HibernateCallback hcb = new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            Query q = session.getNamedQuery(QUERY_COUNT);
            q.setParameter("typeUuid", typeUuid, Hibernate.STRING);
            //q.setParameter("topicId", topicId, Hibernate.LONG);
            q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
            return q.uniqueResult();
        }
    };

    return ((Integer) getHibernateTemplate().execute(hcb)).intValue();
    
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#findUnreadMessageCount(java.lang.Long, java.lang.String)
   */
  public int findUnreadMessageCount(final Long topicId, final String typeUuid) {
                   
    String userId = getCurrentUser();
    
    if (topicId == null || userId == null || typeUuid == null) {
      LOG.error("findUnreadMessageCount failed with topicId: " 
          + topicId + ", userId: " + userId + ", typeUuid: " + typeUuid);
      throw new IllegalArgumentException("Null Argument");
    }
    
    LOG.debug("findUnreadMessageCount executing with topicId: "
        + topicId + ", userId: " + userId + ", typeUuid: " + typeUuid);

    HibernateCallback hcb = new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            Query q = session.getNamedQuery(QUERY_COUNT_BY_UNREAD);
            q.setParameter("typeUuid", typeUuid, Hibernate.STRING);
            //q.setParameter("topicId", topicId, Hibernate.LONG);
            q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
            return q.uniqueResult();
        }
    };

    return ((Integer) getHibernateTemplate().execute(hcb)).intValue();
    
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#deletePrivateMessage(org.sakaiproject.api.app.messageforums.PrivateMessage, java.lang.String)
   */
  public void deletePrivateMessage(PrivateMessage message, String typeUuid){
            
    String userId = getCurrentUser();
    
    if (LOG.isDebugEnabled()){
      LOG.debug("deletePrivateMessage(message:" + message + ", typeUuid:" + typeUuid + ")");
    }    
    
    /** fetch recipients for message */
    PrivateMessage pvtMessage = getPrivateMessageWithRecipients(message);
            
    /**
     *  create PrivateMessageRecipient to search
     */
    PrivateMessageRecipient pmrReadSearch = new PrivateMessageRecipientImpl(
        userId,
      typeUuid,
      Boolean.TRUE
    );
    
    PrivateMessageRecipient pmrNonReadSearch = new PrivateMessageRecipientImpl(
      userId,
      typeUuid,
      Boolean.FALSE
    );
    
    int indexDelete = -1;
    int indexRead = pvtMessage.getRecipients().indexOf(pmrReadSearch);
    if (indexRead != -1){
      indexDelete = indexRead;      
    }
    else{
      int indexNonRead = pvtMessage.getRecipients().indexOf(pmrNonReadSearch);
      if (indexNonRead != -1){
        indexDelete = indexNonRead;
      }
      else{
        LOG.error("deletePrivateMessage -- cannot find private message for user: " + 
            userId + ", typeUuid: " + typeUuid);
      }
    }
                           
    if (indexDelete != -1){
      PrivateMessageRecipient pmrReturned = (PrivateMessageRecipient)
        pvtMessage.getRecipients().get(indexDelete);
    
      if (pmrReturned != null){
        
        /** check for existing deleted message from user */
        PrivateMessageRecipient pmrDeletedSearch = new PrivateMessageRecipientImpl(
          userId,
          typeManager.getDeletedPrivateMessageType(),
          Boolean.TRUE
        );                
        
        int indexDeleted = pvtMessage.getRecipients().indexOf(pmrDeletedSearch);        
        
        if (indexDeleted == -1){
          pmrReturned.setRead(Boolean.TRUE);
          pmrReturned.setTypeUuid(typeManager.getDeletedPrivateMessageType());                    
        }    
        else{
          pvtMessage.getRecipients().remove(indexDelete);
        }
      }
    }
  }
    
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#sendPrivateMessage(org.sakaiproject.api.app.messageforums.PrivateMessage, java.util.List)
   */
  public void sendPrivateMessage(PrivateMessage message, List recipients)
  {
    
    if (LOG.isDebugEnabled()){
      LOG.debug("sendPrivateMessage(message: " + message + ", recipients: " + recipients + ")");
    }
    
    if (message == null || recipients == null){
      throw new IllegalArgumentException("Null Argument");
    }
    
    if (recipients.size() == 0){
      throw new IllegalArgumentException("Empty recipient list");
    }
    
    List recipientList = new UniqueArrayList();
    
    /** test for draft message */
    if (message.getDraft().booleanValue()){
      PrivateMessageRecipientImpl receiver = new PrivateMessageRecipientImpl(
          getCurrentUser(),
          typeManager.getDraftPrivateMessageType(),
          Boolean.TRUE
      );
      
      recipientList.add(receiver);
      message.setRecipients(recipientList);
      savePrivateMessage(message);
      return;
    }
    
    
    
    for (Iterator i = recipients.iterator(); i.hasNext();){
      String userId = (String) i.next();
      
      PrivateMessageRecipientImpl receiver = new PrivateMessageRecipientImpl(
          userId,
          typeManager.getReceivedPrivateMessageType(),
          Boolean.FALSE
      );
      recipientList.add(receiver);      
    }
    
    /** add sender as a saved recipient */
    PrivateMessageRecipientImpl sender = new PrivateMessageRecipientImpl(
        getCurrentUser(),
        typeManager.getSentPrivateMessageType(),
        Boolean.TRUE
    );
    
    recipientList.add(sender);
    
    message.setRecipients(recipientList);        
    
    savePrivateMessage(message);
    
    /** enable if users are stored in message forums user table
      Iterator i = recipients.iterator();
      while (i.hasNext()){
        String userId = (String) i.next();
      
        //getForumUser will create user if forums user does not exist
        message.addRecipient(userManager.getForumUser(userId.trim()));
      }
    **/        
    
  }
    
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#markMessageAsReadForUser(org.sakaiproject.api.app.messageforums.PrivateMessage)
   */
  public void markMessageAsReadForUser(final PrivateMessage message){
    
    if (LOG.isDebugEnabled()){
      LOG.debug("markMessageAsReadForUser(message: " + message + ")");
    }        
    
    if (message == null){
      throw new IllegalArgumentException("Null Argument");
    }
    
    final String userId = getCurrentUser();
    
    /** fetch recipients for message */
    PrivateMessage pvtMessage = getPrivateMessageWithRecipients(message);
            
    /** create PrivateMessageRecipientImpl to search for recipient to update */
    PrivateMessageRecipientImpl searchRecipient = new PrivateMessageRecipientImpl(
        userId,
        typeManager.getReceivedPrivateMessageType(),
        Boolean.FALSE
    );
    
    List recipientList = pvtMessage.getRecipients();
    
    if (recipientList == null || recipientList.size() == 0){
      LOG.error("markMessageAsReadForUser(message: " + message + ") has empty recipient list");
      throw new Error("markMessageAsReadForUser(message: " + message + ") has empty recipient list");
    }
           
    int recordIndex = pvtMessage.getRecipients().indexOf(searchRecipient);
    
    if (recordIndex != -1){           
      ((PrivateMessageRecipientImpl) recipientList.get(recordIndex)).setRead(Boolean.TRUE);
    }                    
  }    
    
  private PrivateMessage getPrivateMessageWithRecipients(final PrivateMessage message){
    
    if (LOG.isDebugEnabled()){
      LOG.debug("getPrivateMessageWithRecipients(message: " + message + ")");
    }        
    
    if (message == null){
      throw new IllegalArgumentException("Null Argument");
    }
    
    HibernateCallback hcb = new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
          Query q = session.getNamedQuery(QUERY_MESSAGES_BY_ID_WITH_RECIPIENTS);          
          q.setParameter("id", message.getId(), Hibernate.LONG);          
          return q.uniqueResult();
      }
    };

    PrivateMessage pvtMessage = (PrivateMessage) getHibernateTemplate().execute(hcb);
    
    if (pvtMessage == null){
      LOG.error("getPrivateMessageWithRecipients(message: " + message + ") could not find message");
      throw new Error("getPrivateMessageWithRecipients(message: " + message + ") could not find message");
    }
    
    return pvtMessage;
  }
  
  private String getCurrentUser() {        
    if (TestUtil.isRunningTests()) {
      return "test-user";
    }
    return sessionManager.getCurrentSessionUserId();
  }
  
  // start injection
  public void setHelper(DummyDataHelperApi helper)
  {
    this.helper = helper;
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
  /**
   * @return siteId
   */
  private String getContextSiteId()
  {
    LOG.debug("getContextSiteId()");
    return ("/site/" + ToolManager.getCurrentPlacement().getContext());
  }
  
  public void setForumManager(MessageForumsForumManager forumManager)
  {
    this.forumManager = forumManager;
  }
}
