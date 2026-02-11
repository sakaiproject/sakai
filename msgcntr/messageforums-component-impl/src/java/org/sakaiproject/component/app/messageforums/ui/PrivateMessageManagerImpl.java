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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums.ui;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.LockMode;
import org.hibernate.query.Query;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DefaultPermissionsManager;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DraftRecipient;
import org.sakaiproject.api.app.messageforums.MembershipItem;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.PrivateMessageRecipient;
import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;
import org.sakaiproject.api.app.messageforums.cover.SynopticMsgcntrManagerCover;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.app.messageforums.TestUtil;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageRecipientImpl;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Actor;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.ResourceLoader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Slf4j
public class PrivateMessageManagerImpl extends HibernateDaoSupport implements PrivateMessageManager {

  private final String QUERY_AGGREGATE_COUNT = "findAggregatePvtMsgCntForUserInContext";  
  private final String QUERY_MESSAGES_BY_USER_TYPE_AND_CONTEXT = "findPrvtMsgsByUserTypeContext";
  private final String QUERY_MESSAGES_BY_ID_WITH_RECIPIENTS = "findPrivateMessageByIdWithRecipients";
  private final String QUERY_RESPONSED_COUNT = "findMessageResponsedCountByUser";
  private final String QUERY_USER_ID_BY_FOWARD_MAIL = "findUserIdByFowardMail";
  
  private final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
  private final String REAL_REPLY_PROPERTY = "msgcntr.messages.user.real.reply";
  private final String FROM_REPLY_PROPERTY = "msgcntr.messages.header.from.reply";
  private final String ENCRYPTION_SECRET_PROPERTY = "sakai.encryption.secret";
  private final String FROM_ADDRESS_PROPERTY = "msgcntr.notification.from.address";
  private final String USER_REAL_FROM_PROPERTY = "msgcntr.notification.user.real.from";
  private final String SHOW_MAIL_TO_LINK_PROPERTY = "msgcntr.messages.footer.show.mailto.link";

  private final String USER_NOT_DEFINED = "cannot find user with id ";

  @Setter @Getter private MessageForumsMessageManager messageManager;
  @Setter private MessageForumsForumManager forumManager;
  @Setter private MessageForumsTypeManager typeManager;
  @Setter private AreaManager areaManager;
  @Setter private IdManager idManager;
  @Setter private SessionManager sessionManager;
  @Setter private EmailService emailService;
  @Setter private ContentHostingService contentHostingService;
  @Setter private SecurityService securityService;
  @Setter private EventTrackingService eventTrackingService;
  @Setter private SiteService siteService;
  @Setter private ToolManager toolManager;
  @Setter private UserDirectoryService userDirectoryService;
  @Setter private LearningResourceStoreService learningResourceStoreService;
  @Setter private PreferencesService preferencesService;
  @Setter private ServerConfigurationService serverConfigurationService;
  @Setter private FormattedText formattedText;

  private final String MESSAGES_TITLE = "pvt_message_nav";// Mensajes-->Messages/need to be modified to support internationalization
  
  private final String PVT_RECEIVED = "pvt_received";     // Recibidos ( 0 mensajes )-->Received ( 8 messages - 8 unread )
  private final String PVT_SENT = "pvt_sent";             // Enviados ( 0 mensajes )--> Sent ( 0 message )
  private final String PVT_DELETED = "pvt_deleted";       // Borrados ( 0 mensajes )-->Deleted ( 0 message )
  private final String PVT_DRAFTS = "pvt_drafts";
  private final String PVT_SCHEDULER = "pvt_scheduler";
  
  public final String PVTMSG_MODE_RECEIVED = "pvt_received";
  public final String PVTMSG_MODE_SENT = "pvt_sent";
  public final String PVTMSG_MODE_DELETE = "pvt_deleted";
  public final String PVTMSG_MODE_DRAFT = "pvt_drafts";
  public final String PVTMSG_MODE_SCHEDULER = "pvt_scheduler";
 
  /** String ids for email footer messsage */
  private final String EMAIL_FOOTER1 = "pvt_email_footer1";
  private final String EMAIL_FOOTER2 = "pvt_email_footer2";
  private final String EMAIL_FOOTER3 = "pvt_email_footer3";
  private final String EMAIL_FOOTER4_A = "pvt_email_footer4_a";
  private final String EMAIL_FOOTER4_B = "pvt_email_footer4_b";
  private final String INIT_VECTOR = "RandomIn";

  private ResourceLoader rb;

  @Setter @Getter private String serverName;
  @Setter @Getter private String defaultServerFromEmail;

  public void init() {
    log.info("init()");
    rb = new ResourceLoader(MESSAGECENTER_BUNDLE);
    this.setServerName(serverConfigurationService.getServerName());
    this.setDefaultServerFromEmail(serverConfigurationService.getString("setup.request", "postmaster@" + this.getServerName()));
  }

  public boolean getPrivateAreaEnabled()
  {

    if (log.isDebugEnabled())
    {
      log.debug("getPrivateAreaEnabled()");
    }
        
    return areaManager.isPrivateAreaEnabled();

  }

  public void setPrivateAreaEnabled(boolean value)
  {

    if (log.isDebugEnabled())
    {
      log.debug("setPrivateAreaEnabled(value: " + value + ")");
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
  
  public Area getPrivateMessageArea(String siteId)
  {
    return areaManager.getPrivateArea(siteId);    
  }


  public Area savePrivateMessageArea(Area area)
  {
    return areaManager.saveArea(area);
  }

  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#initializePrivateMessageArea(org.sakaiproject.api.app.messageforums.Area)
   */
  
  
  public PrivateForum initializePrivateMessageArea(Area area, List aggregateList){
	  return initializePrivateMessageArea(area, aggregateList, getCurrentUser());
  }
  
  //==============Need to be modified to support localization huxt
  public PrivateForum initializePrivateMessageArea(Area area, List aggregateList, String userId){
	  return initializePrivateMessageArea(area, aggregateList, userId, getContextId());
  }  
  
  public PrivateForum initializePrivateMessageArea(Area area, List aggregateList, String userId, String siteId)
  {

   // String userId = getCurrentUser();
    
    aggregateList.clear();
    aggregateList.addAll(initializeMessageCounts(userId, siteId));
    
    getHibernateTemplate().lock(area, LockMode.NONE);
    
    PrivateForum pf;
    PrivateTopic schedulerTopicSaved;

    /** create default user forum/topics if none exist */
    if ((pf = forumManager.getPrivateForumByOwnerArea(userId, area)) == null)
    {      
      /** initialize collections */
      //getHibernateTemplate().initialize(area.getPrivateForumsSet());
            
      pf = forumManager.createPrivateForum(getResourceBundleString(MESSAGES_TITLE), userId);
      
      //area.addPrivateForum(pf);
      //pf.setArea(area);
      //areaManager.saveArea(area);
      
      PrivateTopic receivedTopic = forumManager.createPrivateForumTopic(PVT_RECEIVED, true,false,
          userId, pf.getId());     

      PrivateTopic sentTopic = forumManager.createPrivateForumTopic(PVT_SENT, true,false,
          userId, pf.getId());      

      PrivateTopic deletedTopic = forumManager.createPrivateForumTopic(PVT_DELETED, true,false,
          userId, pf.getId());      

      PrivateTopic draftTopic = forumManager.createPrivateForumTopic(PVT_DRAFTS, true,false,
          userId, pf.getId());
      PrivateTopic schedulerTopic = forumManager.createPrivateForumTopic(PVT_SCHEDULER, true,false,
              userId, pf.getId());

      /** save individual topics - required to add to forum's topic set */
      PrivateTopic receivedTopicSaved = forumManager.savePrivateForumTopic(receivedTopic, userId, siteId);
      PrivateTopic sentTopicSaved = forumManager.savePrivateForumTopic(sentTopic, userId, siteId);
      PrivateTopic deletedTopicSaved = forumManager.savePrivateForumTopic(deletedTopic, userId, siteId);
      PrivateTopic draftTopicSaved = forumManager.savePrivateForumTopic(draftTopic);
      schedulerTopicSaved = forumManager.savePrivateForumTopic(schedulerTopic, userId, siteId);
      
      pf.addTopic(receivedTopicSaved);
      pf.addTopic(sentTopicSaved);
      pf.addTopic(deletedTopicSaved);
      pf.addTopic(draftTopicSaved);
      pf.addTopic(schedulerTopicSaved);
      pf.setArea(area);  
      
      PrivateForum oldForum;
      if ((oldForum = forumManager.getPrivateForumByOwnerAreaNull(userId)) != null)
      {
    		oldForum = initializationHelper(oldForum, userId);
//    		getHibernateTemplate().initialize(oldForum.getTopicsSet());
    		List pvtTopics = oldForum.getTopics();
    		
    		for (int i=0; i<pvtTopics.size(); i++)//reveived deleted sent
    		{
    			PrivateTopic currentTopic = (PrivateTopic) pvtTopics.get(i);
    			if (currentTopic != null)
    			{
    				if (!currentTopic.getTitle().equals(PVT_RECEIVED) && !currentTopic.getTitle().equals(PVT_SENT) && !currentTopic.getTitle().equals(PVT_DELETED) 
    			    		&& !currentTopic.getTitle().equals(PVT_DRAFTS) && !currentTopic.getTitle().equals(PVT_SCHEDULER) && area.getContextId().equals(currentTopic.getContextId()))
    				{
    					currentTopic.setPrivateForum(pf);
    					PrivateTopic currentTopicSaved = forumManager.savePrivateForumTopic(currentTopic, userId, siteId);
    					pf.addTopic(currentTopicSaved);
    				}
    			}
    		}
    		if (oldForum.getAutoForward() != null)
    		{
    			pf.setAutoForward(oldForum.getAutoForward());
    		}
    		if (oldForum.getAutoForwardEmail() != null)
    		{
    			pf.setAutoForwardEmail(oldForum.getAutoForwardEmail());
    		}      
      }
      
    }    
    else{
		if(pf.getTopics().stream().noneMatch(t -> PVT_SCHEDULER.equals(((Topic)t).getTitle()))) {
			PrivateTopic schedulerTopic = forumManager.createPrivateForumTopic(PVT_SCHEDULER, true,false,
	    			userId, pf.getId());
			schedulerTopicSaved = forumManager.savePrivateForumTopic(schedulerTopic, userId, siteId);
			pf.addTopic(schedulerTopicSaved);
		}
       getHibernateTemplate().initialize(pf.getTopicsSet());
    }

    PrivateForum pfSaved = forumManager.savePrivateForum(pf, userId);

    return pfSaved;
  }
  
  public PrivateForum initializationHelper(PrivateForum forum){
	  return initializationHelper(forum, getCurrentUser());
  }
  
  public PrivateForum initializationHelper(PrivateForum forum, String userId){
    
    /** reget to load topic foreign keys */
    PrivateForum pf = forumManager.getPrivateForumByOwnerAreaNull(userId);
    getHibernateTemplate().initialize(pf.getTopicsSet());
    return pf;
  }

  public PrivateForum initializationHelper(PrivateForum forum, Area area){
	  return initializationHelper(forum, area, getCurrentUser());
  }
  
  public PrivateForum initializationHelper(PrivateForum forum, Area area, String userId){
    
    /** reget to load topic foreign keys */
    PrivateForum pf = forumManager.getPrivateForumByOwnerArea(userId, area);
    getHibernateTemplate().initialize(pf.getTopicsSet());
    return pf;
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#savePrivateMessage(org.sakaiproject.api.app.messageforums.Message)
   */
  public Message savePrivateMessage(Message message)
  {
    return messageManager.saveOrUpdateMessage(message);
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#savePrivateMessage(org.sakaiproject.api.app.messageforums.Message, boolean logEvent)
   */
  public Message savePrivateMessage(Message message, boolean logEvent)
  {
	  return messageManager.saveOrUpdateMessage(message, logEvent);
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
      attach.setAttachmentSize((Long.valueOf(cr.getContentLength())).toString());
      attach.setCreatedBy(cr.getProperties().getProperty(
          cr.getProperties().getNamePropCreator()));
      attach.setModifiedBy(cr.getProperties().getProperty(
          cr.getProperties().getNamePropModifiedBy()));
      attach.setAttachmentType(cr.getContentType());
      String tempString = cr.getUrl();
      String newString = "";
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
      log.error(e.getMessage(), e);
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
    if (log.isDebugEnabled())
    {
      log.debug("saveForumSettings(forum: " + forum + ")");
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
    
    PrivateTopic createdTopicSaved = forumManager.savePrivateForumTopic(createdTopic);
    pf.addTopic(createdTopicSaved);
    forumManager.savePrivateForum(pf);
  }

  public void createTopicFolderInTopic(PrivateForum pf, PrivateTopic parentTopic, String folderName)
  {
    String userId = getCurrentUser();
    PrivateTopic createdTopic = forumManager.createPrivateForumTopic(folderName, true,true,
        userId, pf.getId()); 
    createdTopic.setParentTopic(parentTopic);
    PrivateTopic createdTopicSaved = forumManager.savePrivateForumTopic(createdTopic);
    pf.addTopic(createdTopicSaved);
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
        
        eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FOLDER_REVISE, getEventMessage(element), false));
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

        eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FOLDER_REMOVE, getEventMessage(element), false));
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
    if (log.isDebugEnabled())
    {
      log.debug("getTopicByIdWithMessages(final Long" + topicUuid + ")");
    }
    return forumManager.getTopicByUuid(topicUuid);
  }
  
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
      if (element != null) {
        log.debug("element.getTypeUuid(): "+element.getTypeUuid()+", oldTopicTypeUuid: "+oldTopicTypeUuid+", element.getUserId(): "+element.getUserId()+ ", getCurrentUser(): "+getCurrentUser());
        if (element.getTypeUuid().equals(oldTopicTypeUuid) && (element.getUserId().equals(getCurrentUser())))
        {
          element.setTypeUuid(newTopicTypeUuid);
        }
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

    log.debug("message " + message.getUuid() + " created successfully");
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

  public List getSchedulertedMessages(String orderField, String order)
  {
    return getMessagesByType(typeManager.getSchedulerPrivateMessageType(),
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

    if (log.isDebugEnabled())
    {
      log.debug("getMessagesByType(typeUuid:" + typeUuid + ", orderField: "
          + orderField + ", order:" + order + ")");
    }

    HibernateCallback<List> hcb = session -> {
      Query q = session.getNamedQuery(QUERY_MESSAGES_BY_USER_TYPE_AND_CONTEXT);
      Query qOrdered = session.createQuery(q.getQueryString() + " order by "
          + orderField + " " + order);

      qOrdered.setParameter("userId", getCurrentUser(), StringType.INSTANCE);
      qOrdered.setParameter("typeUuid", typeUuid, StringType.INSTANCE);
      qOrdered.setParameter("contextId", getContextId(), StringType.INSTANCE);
      return qOrdered.list();
    };

    return getHibernateTemplate().execute(hcb);
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

    if (log.isDebugEnabled())
    {
      log.debug("getMessagesByTypeForASite(typeUuid:" + typeUuid + ")");
    }

    HibernateCallback<List> hcb = session -> {
      Query q = session.getNamedQuery(QUERY_MESSAGES_BY_USER_TYPE_AND_CONTEXT);

      q.setParameter("userId", getCurrentUser(), StringType.INSTANCE);
      q.setParameter("typeUuid", typeUuid, StringType.INSTANCE);
      q.setParameter("contextId", contextId, StringType.INSTANCE);
      return q.list();
    };

    return getHibernateTemplate().execute(hcb);
  }
  
  public List getMessagesByTypeByContext(final String typeUuid, final String contextId, final String userId, final String orderField,
	      final String order){
    if (log.isDebugEnabled())
    {
      log.debug("getMessagesByTypeForASite(typeUuid:" + typeUuid + ")");
    }

    HibernateCallback<List> hcb = session -> {
      Query q = session.getNamedQuery(QUERY_MESSAGES_BY_USER_TYPE_AND_CONTEXT);
      Query qOrdered = session.createQuery(q.getQueryString() + " order by "
              + orderField + " " + order);
      qOrdered.setParameter("userId", userId, StringType.INSTANCE);
      qOrdered.setParameter("typeUuid", typeUuid, StringType.INSTANCE);
      qOrdered.setParameter("contextId", contextId, StringType.INSTANCE);
      return qOrdered.list();
    };

    return getHibernateTemplate().execute(hcb);
  }


    /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#findMessageCount(java.lang.String)
   */
  public int findMessageCount(String typeUuid, List aggregateList)
  {    
    if (log.isDebugEnabled())
    {
      log.debug("findMessageCount executing with typeUuid: " + typeUuid);
    }

    if (typeUuid == null)
    {
      log.error("findMessageCount failed with typeUuid: null");
      throw new IllegalArgumentException("Null Argument");
    }    
    
    if (aggregateList == null)
    {
      log.error("findMessageCount failed with aggregateList: null");
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
    if (log.isDebugEnabled())
    {
      log.debug("findUnreadMessageCount executing with typeUuid: " + typeUuid);
    }

    if (typeUuid == null)
    {
      log.error("findUnreadMessageCount failed with typeUuid: null");
      throw new IllegalArgumentException("Null Argument");
    }    
    
    if (aggregateList == null)
    {
      log.error("findMessageCount failed with aggregateList: Null");
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
  

  private List initializeMessageCounts(final String userId, final String contextId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("initializeMessageCounts executing");
    }

    HibernateCallback<List> hcb = session -> {
      Query q = session.getNamedQuery(QUERY_AGGREGATE_COUNT);
      q.setParameter("contextId", contextId, StringType.INSTANCE);
      q.setParameter("userId", userId, StringType.INSTANCE);
      return q.list();
    };
        
    return getHibernateTemplate().execute(hcb);
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
	  HibernateCallback<List> hcb = session -> {
         Query q = session.getNamedQuery("findUnreadPvtMsgCntByUserForAllSites");
         q.setParameter("userId", getCurrentUser(), StringType.INSTANCE);
         return q.list();
      };
  
	  return getHibernateTemplate().execute(hcb);
	  
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#deletePrivateMessage(org.sakaiproject.api.app.messageforums.PrivateMessage, java.lang.String)
   */
  public void deletePrivateMessage(PrivateMessage message, String typeUuid)
  {

    String userId = getCurrentUser();

    if (log.isDebugEnabled())
    {
      log.debug("deletePrivateMessage(message:" + message + ", typeUuid:"
          + typeUuid + ")");
    }

    /** fetch recipients for message */
    PrivateMessage pvtMessage = getPrivateMessageWithRecipients(message);

    /**
     *  create PrivateMessageRecipient to search
     */
    PrivateMessageRecipient pmrReadSearch = new PrivateMessageRecipientImpl(
        userId, typeUuid, getContextId(), Boolean.TRUE, false);

    PrivateMessageRecipient pmrNonReadSearch = new PrivateMessageRecipientImpl(
        userId, typeUuid, getContextId(), Boolean.FALSE , false);

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
        log.error("deletePrivateMessage -- cannot find private message for user: "
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
            Boolean.TRUE, false);

        int indexDeleted = pvtMessage.getRecipients().indexOf(pmrDeletedSearch);

        if (indexDeleted == -1)
        {
        	boolean prevReadStatus = pmrReturned.getRead();
        	pmrReturned.setRead(Boolean.TRUE);

        	String contextId = getContextId();
        	if(!prevReadStatus){
        		decrementMessagesSynopticToolInfo(userId, contextId, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        	}

        	pmrReturned.setTypeUuid(typeManager.getDeletedPrivateMessageType());
        }
        else
        {
          pvtMessage.getRecipients().remove(indexDelete);
        }
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
				log.info("PrivateMessageManagerImpl: decrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
				log.error(holfe.getMessage(), holfe);
			} else {
				log.info("PrivateMessageManagerImpl: decrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
								+ numOfAttempts);
				decrementMessagesSynopticToolInfo(userId, siteId, numOfAttempts);
			}
		}

	}

  public void incrementMessagesSynopticToolInfo(String userId, String siteId , int numOfAttempts) {
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
				log.info("PrivateMessageManagerImpl: incrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
				log.error(holfe.getMessage(), holfe);
			} else {
				log.info("PrivateMessageManagerImpl: incrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
								+ numOfAttempts);
				incrementMessagesSynopticToolInfo(userId, siteId, numOfAttempts);
			}
		}

	}

  private String currentUserAsString(PrivateMessage message, boolean isMailArchive) {
	    String currentUserAsString = getCurrentUser();
	    if (isMailArchive || currentUserAsString == null) {
	        currentUserAsString = message.getCreatedBy();
	    }
	    return currentUserAsString;
  }

  private User currentUser(PrivateMessage message, boolean isMailArchive){
	    User currentUser = userDirectoryService.getCurrentUser();
	    if (isMailArchive || currentUser == null) {
	        try {
	            currentUser = userDirectoryService.getUser(message.getCreatedBy());
	        } catch (UserNotDefinedException e) {
	            log.error(USER_NOT_DEFINED + message.getCreatedBy() + " " + e.getMessage());
	            throw new IllegalArgumentException(USER_NOT_DEFINED + message.getCreatedBy());
	        }
	    }
	    return currentUser;
  }

  private Message saveMessage(Message message, boolean isMailArchive, String contextId, String currentUserAsString){
	  Message pmessage;
      if (isMailArchive) {
	        pmessage = messageManager.saveOrUpdateMessage(message, false, DiscussionForumService.MESSAGES_TOOL_ID, currentUserAsString, contextId);
      } else {
	        pmessage = savePrivateMessage(message, false);
      }
      return pmessage;
  }

  private boolean getForwardingEnabled(Map<User, Boolean> recipients, Map<String, PrivateForum> pfMap, String currentUserAsString, String contextId, List recipientList, List<InternetAddress> fAddresses, boolean asEmail, boolean readReceipt) throws MessagingException{
	  boolean forwardingEnabled = false;
	  //this only needs to be done if the message is not being sent
	  int submitterEmailReceiptPref;
	  for (Iterator<Entry<User, Boolean>> i = recipients.entrySet().iterator(); i.hasNext();)
		  {
		  Entry<User, Boolean> entrySet = i.next();
		  User u = entrySet.getKey();
		  Boolean bcc = entrySet.getValue();
		  String userId = u.getId();
		  String mailAFoward = u.getEmail();

		  submitterEmailReceiptPref = 0;
		  submitterEmailReceiptPref = serverConfigurationService.getInt("prefs.msg.notification", submitterEmailReceiptPref);
		  Preferences submitterPrefs = preferencesService.getPreferences( userId );
		  ResourceProperties props = submitterPrefs.getProperties( NotificationService.PREFS_TYPE + "sakai:messageforums" );

		  try {
			  submitterEmailReceiptPref = (int) props.getLongProperty("2");
		  } catch (EntityPropertyNotDefinedException | EntityPropertyTypeException ex) {
			  /* User hasn't changed preference */
		  }

		  PrivateForum pf = null;
		  pf = pfMap.get(userId);

		  PrivateForum oldPf = forumManager.getPrivateForumByOwnerAreaNull(userId);
		  if (pf != null && (pf.getAutoForward()==PrivateForumImpl.AUTO_FOWARD_YES) && pf.getAutoForwardEmail() != null && (!pf.getAutoForwardEmail().equalsIgnoreCase(u.getEmail()) || !asEmail)){
			  forwardingEnabled = true;
			  fAddresses.add(new InternetAddress(pf.getAutoForwardEmail()));
		  }
		  else if (pf != null && (pf.getAutoForward()==PrivateForumImpl.AUTO_FOWARD_DEFAULT) && submitterEmailReceiptPref==PrivateForumImpl.AUTO_FOWARD_YES && !asEmail){
			  pf.setAutoForwardEmail(mailAFoward);
			  forwardingEnabled = true;
			  fAddresses.add(new InternetAddress(pf.getAutoForwardEmail()));
		  }
		  //only check for default settings if the pf is null
		  else if (pf == null && oldPf != null && (oldPf.getAutoForward()==PrivateForumImpl.AUTO_FOWARD_YES) && oldPf.getAutoForwardEmail() != null && (!oldPf.getAutoForwardEmail().equalsIgnoreCase(u.getEmail()) || !asEmail)) {
			  forwardingEnabled = true;
			  fAddresses.add(new InternetAddress(oldPf.getAutoForwardEmail()));
		  }
		  else if (pf == null && oldPf != null && (oldPf.getAutoForward()==PrivateForumImpl.AUTO_FOWARD_DEFAULT) && submitterEmailReceiptPref==PrivateForumImpl.AUTO_FOWARD_YES && !asEmail) {
			  oldPf.setAutoForwardEmail(mailAFoward);
			  forwardingEnabled = true;
			  fAddresses.add(new InternetAddress(oldPf.getAutoForwardEmail()));
		  }

		  /** determine if current user is equal to recipient */
		  Boolean isRecipientCurrentUser =
				  (currentUserAsString.equals(userId) ? Boolean.TRUE : Boolean.FALSE);

		  PrivateMessageRecipientImpl receiver = new PrivateMessageRecipientImpl(
				  userId, typeManager.getReceivedPrivateMessageType(), contextId,
				  isRecipientCurrentUser, bcc);
		  receiver.setReadReceipt(readReceipt);
		  recipientList.add(receiver);
		  }
	  return forwardingEnabled;
  }

  private String getSystemAndReplyEmail(User currentUser, Message savedMessage, List replyEmail, String contextId) throws MessagingException{
	    /** determines if default in sakai.properties is set, if not will make a reasonable default */
	    String defaultEmail = serverConfigurationService.getSmtpFrom();
	    String systemEmail = defaultEmail;

	    if (serverConfigurationService.getBoolean("msgcntr.notification.user.real.from", false) && currentUser.getEmail() != null) {
	        systemEmail = currentUser.getEmail();
	    }
	    String[] realReply = serverConfigurationService.getStrings(REAL_REPLY_PROPERTY);
	    if (realReply!=null && ("all".equals(serverConfigurationService.getString(REAL_REPLY_PROPERTY, "none")) || Arrays.asList(realReply).contains(contextId)) && systemEmail.equalsIgnoreCase(defaultEmail)) {
			replyEmail.add(new InternetAddress(buildMailReply(savedMessage)));
			systemEmail = serverConfigurationService.getString("msgcntr.notification.from.address.reply", this.getDefaultServerFromEmail());
		}
	    return systemEmail;
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#sendPrivateMessage(org.sakaiproject.api.app.messageforums.PrivateMessage, java.util.Set, boolean, boolean)
   */
  public Long sendPrivateMessage(PrivateMessage message, Map<User, Boolean> recipients, boolean asEmail, boolean readReceipt) {
    return sendPrivateMessage(message, recipients, asEmail, Collections.emptyList(), Collections.emptyList(), readReceipt);
  }

  @Override
  public Long sendPrivateMessage(PrivateMessage message, Map<User, Boolean> recipients, boolean asEmail, 
    List<MembershipItem> draftRecipients, List<MembershipItem> draftBccRecipients, boolean readReceipt) {

    try 
    {
        log.debug("sendPrivateMessage(message: {}, recipients: {})", message, recipients);

    if (message == null || recipients == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }

    if (recipients.isEmpty() && !message.getDraft())
    {
      return null;
    }

    String contextId="";
    boolean isMailArchive = false;
    try {
        contextId = getContextId();
    } catch (Exception e) {
	    contextId = ((PrivateMessageRecipientImpl)message.getRecipients().get(0)).getContextId();
	    isMailArchive = true;
	}
    String currentUserAsString = currentUserAsString(message, isMailArchive);

    User currentUser = currentUser(message, isMailArchive);
    List recipientList = new UniqueArrayList();

    if (message.getDraft()) {
        PrivateMessageRecipient receiver = new PrivateMessageRecipientImpl(currentUserAsString, typeManager.getDraftPrivateMessageType(),
            contextId, Boolean.TRUE, false);
        receiver.setReadReceipt(readReceipt);
        recipientList.add(receiver);
        message.setRecipients(recipientList);
        Message savedMessage = saveMessage(message, isMailArchive, contextId, currentUserAsString);

        List<DraftRecipient> allDraftRecipients = getDraftRecipients(savedMessage.getId(), draftRecipients, draftBccRecipients);
        messageManager.deleteDraftRecipientsByMessageId(savedMessage.getId());
        messageManager.saveDraftRecipients(savedMessage.getId(), allDraftRecipients);

        return savedMessage.getId();
    }

    //build the message body
    List<String> additionalHeaders = new ArrayList<>(1);
    additionalHeaders.add("Content-Type: text/html; charset=utf-8");

    Area currentArea = null;
    List<PrivateForum> privateForums = null;
    Map<String, PrivateForum> pfMap = null;
    
    	currentArea = getAreaByContextIdAndTypeId(typeManager.getPrivateMessageAreaType(), contextId);

    	// make sure the site-wide email copy preference is respected in case an invalid
    	// value slipped in
    	if (currentArea.getSendToEmail() == Area.EMAIL_COPY_ALWAYS) {
    	    asEmail = true;
    	} else if (currentArea.getSendToEmail() == Area.EMAIL_COPY_NEVER) {
    	    asEmail = false;
    	}
    	
        //this is fairly inneficient and should realy be a convenience method to lookup
        // the users who want to forward their messages
        privateForums = currentArea.getPrivateForums();

    	//create a map for efficient lookup for large sites
    	pfMap = new HashMap<String, PrivateForum>();
        for (PrivateForum privateForum : privateForums) {
            pfMap.put(privateForum.getOwner(), privateForum);
        }

      List<InternetAddress> fAddresses = new ArrayList<>();
      boolean forwardingEnabled = getForwardingEnabled(recipients, pfMap, currentUserAsString, contextId, recipientList, fAddresses, asEmail, readReceipt);
      //this only needs to be done if the message is not being sent
    
    /** add sender as a saved recipient */
    PrivateMessageRecipientImpl sender = new PrivateMessageRecipientImpl(
    		currentUserAsString, typeManager.getSentPrivateMessageType(),
    		contextId, Boolean.TRUE, false);

    recipientList.add(sender);

    message.setRecipients(recipientList);

	Message savedMessage = saveMessage(message, isMailArchive, contextId, currentUserAsString);

    message.setId(savedMessage.getId());

    // clean up anything in the draftrecipients table since the message has now been sent
    messageManager.deleteDraftRecipientsByMessageId(message.getId());

    String bodyString = buildMessageBody(message);
    List<InternetAddress> replyEmail  = new ArrayList<>();

    String systemEmail = getSystemAndReplyEmail(currentUser, savedMessage, replyEmail, contextId);

	if (asEmail)
	{

	//send as 1 action to all recipients
	//we need to add som headers
	additionalHeaders.add("From: " + systemEmail);
	String subjectHeader = "Subject: [" + this.getServerName() + "]: ";
	String siteTitle = this.getSiteTitle(contextId);
	if (siteTitle != null) {
		subjectHeader += siteTitle + ": ";
	}
	subjectHeader += message.getTitle();
	additionalHeaders.add(subjectHeader);
	if (!replyEmail.isEmpty()) {
		additionalHeaders.add("Reply-To: " + replyEmail.get(0));
	}
	emailService.sendToUsers(recipients.keySet(), additionalHeaders, bodyString);
	}   	

	if (!isEmailForwardDisabled() && forwardingEnabled)
	{
		InternetAddress[] fAddressesArr = new InternetAddress[fAddresses.size()];
		fAddressesArr = fAddresses.toArray(fAddressesArr);
		emailService.sendMail(new InternetAddress(systemEmail), fAddressesArr, message.getTitle(), 
				bodyString, null, replyEmail.toArray(new InternetAddress[0]), additionalHeaders);
	}

	return savedMessage.getId();
	}
	catch (MessagingException e) 
	{
		log.warn("PrivateMessageManagerImpl.sendPrivateMessage: exception: {}", e.getMessage(), e);
	}
	
	return null;
  }

  public boolean isEmailForwardDisabled(){
	  return serverConfigurationService.getBoolean("mc.messages.forwardEmailDisabled", false);
  }
  private String buildMailReply(Message message) {
	  
	  StringBuilder replyMail = new StringBuilder();
	  replyMail.append(serverConfigurationService.getString(FROM_REPLY_PROPERTY));
	  replyMail.append(encrypt(message.getId().toString()));
	  replyMail.append("@");
	  replyMail.append(this.getServerName());
	  return replyMail.toString();
  }

  private String buildMessageBody(PrivateMessage message) {
	  String contextId="";
	  boolean isMailArchive = false;
	  try {
		  contextId = getContextId();
	  } catch (Exception e) {
		  contextId = ((PrivateMessageRecipientImpl)message.getRecipients().get(0)).getContextId();
		  isMailArchive = true;
	  }

	  User currentUser = userDirectoryService.getCurrentUser();
	  if (currentUser==null || isMailArchive) {
		  try {
			  currentUser = userDirectoryService.getUser(message.getCreatedBy());
		  } catch (UserNotDefinedException e) {
			  log.error(USER_NOT_DEFINED + message.getCreatedBy() + " " + e.getMessage());
			  throw new IllegalArgumentException(USER_NOT_DEFINED + message.getCreatedBy());
		  }
	  }
	  StringBuilder body = new StringBuilder(message.getBody());
	  
	  StringBuilder fromString = new StringBuilder();
	  fromString.append("<p>");
	  if (serverConfigurationService.getBoolean("msg.displayEid", true)) {
	      fromString.append(getResourceBundleString("pvt_email_from_with_eid", 
                      new Object[] {currentUser.getDisplayName(), currentUser.getEid(), currentUser.getEmail() }));
	  } else {
	      fromString.append(getResourceBundleString("pvt_email_from", 
	              new Object[] {currentUser.getDisplayName(), currentUser.getEmail() }));
	  }
	  
	  fromString.append("</p>");

	  body.insert(0, fromString.toString());

	  // need to determine if there are "hidden" recipients to this message.
	  // If so, we need to replace them with "Undisclosed Recipients"
	  // Identifying them is tricky now because hidden users are identified by having their
	  // names in parentheses at the end of the list. At some point, the usernames were also added to
	  // this in parentheses (although this may be overridden via a property). 
	  // So to fix this going forward, the hidden users will be indicated by brackets [],
	  // but we still need to handle the old data with hidden users in parens
	  String sendToString = message.getRecipientsAsText();
	  
	  if (sendToString.indexOf(PrivateMessage.HIDDEN_RECIPIENTS_START) > 0) {
	      sendToString = sendToString.substring(0, sendToString.indexOf(PrivateMessage.HIDDEN_RECIPIENTS_START));
	      
	      // add "Undisclosed Recipients" in place of the hidden users
	      sendToString = sendToString.trim();
	      if (sendToString.length() > 0) {
	          sendToString += "; ";
	      } 
	      sendToString += getResourceBundleString("pvt_HiddenRecipients");
	  } else {
	      // we may have parens around a list of names with eid in parens
	      if (serverConfigurationService.getBoolean("msg.displayEid", true)) {
	          String originalSendTo = sendToString;
	          sendToString = sendToString.replaceAll("\\([^)]+\\(.*", "");
	          
	          // add "Undisclosed Recipients" in place of the hidden users
	          if (!sendToString.equals(originalSendTo)) {
	              sendToString = sendToString.trim();
	              if (sendToString.length() > 0) {
	                  sendToString += "; ";
	              }
	              sendToString += getResourceBundleString("pvt_HiddenRecipients");
	          }
	      } else {
	          // the old data just has the hidden users in parens
	          if (sendToString.indexOf("(") > 0) {
	              sendToString = sendToString.substring(0, sendToString.indexOf("("));
	              
	              // add "Undisclosed Recipients" in place of the hidden users
	              sendToString = sendToString.trim();
	              if (sendToString.length() > 0) {
	                  sendToString += "; ";
	              }
	              sendToString += getResourceBundleString("pvt_HiddenRecipients");
	          }
	          
	      }
	  }

      sendToString = sendToString
              .replaceAll(getResourceBundleString("pvt_bccUndisclosed"), "")
              // remove trailing spaces and semicolons
              .replaceAll("[;\\s]+$", "");

	  body.insert(0, "<p>" + getResourceBundleString("pvt_email_to", new Object[] {sendToString}) + "<p/>");

	  if (message.getAttachments() != null && message.getAttachments().size() > 0) {

		  body.append("<br/><br/>");
		  for (Iterator iter = message.getAttachments().iterator(); iter.hasNext();) {
			  Attachment attachment = (Attachment) iter.next();
			  body.append("<a href=\"" + messageManager.getAttachmentUrl(attachment.getAttachmentId()) +
					  "\">" + attachment.getAttachmentName() + "</a><br/>");            
		  }
	  }

	  String contextTool = StringUtils.EMPTY;
	  String siteTitle = null;
	  if (isMailArchive) {
		  contextTool = contextId;
		  siteTitle = this.getSiteTitle(contextId);
	  } else {
		  contextTool = toolManager.getCurrentPlacement().getContext();
		  siteTitle = this.getSiteTitle(getContextId());
	  }
	  String thisToolId = "";
	  if (isMailArchive) {
		  thisToolId = DiscussionForumService.MESSAGES_TOOL_ID;
	  } else {
		  ToolSession ts = sessionManager.getCurrentToolSession();
		  if (ts != null) {
			  ToolConfiguration tool = siteService.findTool(ts.getPlacementId());
			  if (tool != null) {
				  thisToolId = tool.getId();
			  }
		  }
	  }

	  String footer = "<p>----------------------<br>";

	  boolean showMailToLink = serverConfigurationService.getBoolean(SHOW_MAIL_TO_LINK_PROPERTY, false);
	  String currentUserEmail = currentUser.getEmail();
	  if (StringUtils.isNotBlank(currentUserEmail) && showMailToLink) {
	      footer += getResourceBundleString("pvt_email_click");
	      footer += " <a href=\"mailto:" + currentUserEmail + "\">" + currentUserEmail + "</a> ";
	      footer += getResourceBundleString("pvt_email_warning") + "<br><br>";
	  }

	  footer += getResourceBundleString(EMAIL_FOOTER1) + " " + serverConfigurationService.getString("ui.service","Sakai") +
	  " " + getResourceBundleString(EMAIL_FOOTER2) + " \"" +
	  siteTitle + "\" " + getResourceBundleString(EMAIL_FOOTER3) + "\n";

	  String[] realReply = serverConfigurationService.getStrings(REAL_REPLY_PROPERTY);
	  if (realReply!=null && ("all".equals(serverConfigurationService.getString(REAL_REPLY_PROPERTY, "none")) || Arrays.asList(realReply).contains(contextId))){
		  footer = footer +  getResourceBundleString(EMAIL_FOOTER4_A) +
				  " <a href=\"" +
				  serverConfigurationService.getPortalUrl() + 
				  "/site/" + contextTool +
				  "/tool/" + thisToolId+
				  (message!=null?"/privateMsg/pvtMsgDirectAccess?current_msg_detail="+message.getId():"")+
				  "\">";
	  }else {
		  footer = footer +  getResourceBundleString(EMAIL_FOOTER4_B) +
				  " <a href=\"" +
				  serverConfigurationService.getPortalUrl() + 
				  "/site/" + contextTool +
				  "/tool/" + thisToolId+
				  (message!=null?"/privateMsg/pvtMsgDirectAccess?current_msg_detail="+message.getId():"")+
				  "\">";
	  }

	  footer += siteTitle + "</a><br>----------------------</p>";
	  body.insert(0, footer); // Put the footer at the top.

	  return body.toString();
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
	  markMessageAsReadForUser(message, contextId, getCurrentUser(), toolManager.getCurrentTool().getId());
	  
  }
  
  public void markMessageAsReadForUser(final PrivateMessage message, final String contextId, final String userId, String toolId)
  {

    if (log.isDebugEnabled())
    {
      log.debug("markMessageAsReadForUser(message: " + message + ")");
    }

    if (message == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }


    /** fetch recipients for message */
    PrivateMessage pvtMessage = getPrivateMessageWithRecipients(message);

    /** create PrivateMessageRecipientImpl to search for recipient to update */
    PrivateMessageRecipientImpl searchRecipient = new PrivateMessageRecipientImpl(
        userId, typeManager.getReceivedPrivateMessageType(), contextId,
        Boolean.FALSE, false);

    List recipientList = pvtMessage.getRecipients();

    if (recipientList == null || recipientList.size() == 0)
    {
      log.error("markMessageAsReadForUser(message: " + message
          + ") has empty recipient list");
      throw new RuntimeException("markMessageAsReadForUser(message: " + message
          + ") has empty recipient list");
    }
    
    int recordIndex = -1;
    for (int i  = 0; i < pvtMessage.getRecipients().size(); i++) {
    	if (((PrivateMessageRecipientImpl) pvtMessage.getRecipients().get(i)).getUserId().equals(searchRecipient.getUserId())){
    		recordIndex = i;
    		if (! ((PrivateMessageRecipientImpl) recipientList.get(recordIndex)).getRead()) {
    		  if (((PrivateMessageRecipientImpl) recipientList.get(recordIndex)).getReadReceipt() != null && ((PrivateMessageRecipientImpl) recipientList.get(recordIndex)).getReadReceipt()) {

    		    this.sendConfirmationEmail(pvtMessage, (PrivateMessageRecipientImpl) recipientList.get(recordIndex), contextId);
    		    ((PrivateMessageRecipientImpl) recipientList.get(recordIndex)).setReadReceipt(false);

    		  }
    		  ((PrivateMessageRecipientImpl) recipientList.get(recordIndex)).setRead(Boolean.TRUE);
    		}
    	}      
    }
    
    if (recordIndex != -1){
		decrementMessagesSynopticToolInfo(searchRecipient.getUserId(), contextId, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_READ, getEventMessage(pvtMessage, toolId, userId, contextId), false));
    }
  }

  public void sendConfirmationEmail(PrivateMessage pvtMessage, PrivateMessageRecipientImpl pvtRecipient, String contextId){
    String currentUserAsString = currentUserAsString(pvtMessage, false);

    String systemEmail = "";
    try {
      User currentUser = currentUser(pvtMessage, false);
      List<InternetAddress> replyEmail  = new ArrayList<>();
      systemEmail = getSystemAndReplyEmail(currentUser, pvtMessage, replyEmail, contextId);
    } catch (MessagingException e) {
      log.warn("PrivateMessageManagerImpl.sendConfirmationEmail: exception: " + e.getMessage(), e);
    }

    List additionalHeaders = new ArrayList(1);
    additionalHeaders.add("Content-Type: text/html; charset=utf-8");
    additionalHeaders.add("From: " + systemEmail);
    additionalHeaders.add("Subject: " + rb.getFormattedMessage("pvt_read_receipt_email_subject", pvtMessage.getTitle()));

    User user = null;
    User messageCreator = null;
    try {
      user = userDirectoryService.getUser(pvtRecipient.getUserId());
      messageCreator = userDirectoryService.getUser(pvtMessage.getCreatedBy());
    } catch (UserNotDefinedException e) { }

    SimpleDateFormat formatter_date = new SimpleDateFormat(rb.getString("date_format"), new ResourceLoader().getLocale());
    Site site = null;
    try {
      site = siteService.getSite(contextId);
    } catch (IdUnusedException e) {
      log.error(e.getMessage(), e);
    }
    String messageUrl = serverConfigurationService.getPortalUrl() + "/site/" + contextId + "/tool/" 
                        + site.getToolForCommonId(DiscussionForumService.MESSAGES_TOOL_ID).getId() 
                        + "/privateMsg/pvtMsgDirectAccess?current_msg_detail=" + pvtMessage.getId();
    
    String bodyString = rb.getFormattedMessage("pvt_read_receipt_email", user.getDisplayName(), user.getEid(), pvtMessage.getTitle(), formatter_date.format(new Date()), rb.getFormattedMessage("pvt_email_href", messageUrl, site.getTitle()));
    
    emailService.sendToUser(messageCreator, additionalHeaders, bodyString);
    eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_READ_RECEIPT, getEventMessage(pvtMessage, DiscussionForumService.MESSAGES_TOOL_ID, pvtMessage.getCreatedBy(), contextId), false));
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

	  if (log.isDebugEnabled()) {
		  log.debug("markMessageAsUnreadForUser(message: " + message + ")");
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
			  Boolean.TRUE, false);

	  List recipientList = pvtMessage.getRecipients();

	  if (recipientList == null || recipientList.size() == 0)
	  {
		  log.error("markMessageAsUnreadForUser(message: " + message
				  + ") has empty recipient list");
		  throw new RuntimeException("markMessageAsUnreadForUser(message: " + message
				  + ") has empty recipient list");

	  }

	  int recordIndex = -1;
	  for (int i  = 0; i < pvtMessage.getRecipients().size(); i++) {
		  if (((PrivateMessageRecipientImpl) pvtMessage.getRecipients().get(i)).getUserId().equals(searchRecipient.getUserId())){
			  recordIndex = i;
			  if (((PrivateMessageRecipientImpl) recipientList.get(recordIndex))
					  .getRead()) {
				  ((PrivateMessageRecipientImpl) recipientList.get(recordIndex))
				  .setRead(Boolean.FALSE);				  
			  }
		  }      
	  }
	  if (recordIndex != -1){
		  try {
			  //TODO is this only used to prevent increments if the site doesn't exit? DH
			  siteService.getSite(contextId);
			  incrementMessagesSynopticToolInfo(searchRecipient
					  .getUserId(), contextId,
					  SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
		  } catch (IdUnusedException e) {
			  log.error(e.getMessage(), e);
		  }
		  
		  eventTrackingService.post(eventTrackingService.newEvent(
				  DiscussionForumService.EVENT_MESSAGES_UNREAD,
				  getEventMessage(pvtMessage), false));
	  }
	}

	public void markMessageAsRepliedForUser(final PrivateMessage message) {				
  
		PrivateMessage pvtMessage = getPrivateMessageWithRecipients(message);
		List recipientList = pvtMessage.getRecipients();

		if (recipientList != null) {
			String userId = getCurrentUser();
			for (Object r : recipientList) {
				if (((PrivateMessageRecipientImpl) r).getUserId().equals(userId)) {
					((PrivateMessageRecipientImpl) r).setReplied(true);
				}
			}
		}		
	}

	public void markMessageAsRepliedForUser(final PrivateMessage message, String userId) {				
		  
		PrivateMessage pvtMessage = getPrivateMessageWithRecipients(message);
		List<PrivateMessageRecipient> recipientList = pvtMessage.getRecipients();

		if (recipientList != null) {
			for (Object r : recipientList) {
				if (((PrivateMessageRecipientImpl) r).getUserId().equals(userId)) {
					((PrivateMessageRecipientImpl) r).setReplied(true);
				}
			}
		}
	}

  private PrivateMessage getPrivateMessageWithRecipients(
      final PrivateMessage message)
  {

    if (log.isDebugEnabled())
    {
      log.debug("getPrivateMessageWithRecipients(message: " + message + ")");
    }

    if (message == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }

    HibernateCallback<PrivateMessage> hcb = session -> {
      Query q = session.getNamedQuery(QUERY_MESSAGES_BY_ID_WITH_RECIPIENTS);
      q.setParameter("id", message.getId(), LongType.INSTANCE);
      return (PrivateMessage) q.uniqueResult();
    };

    PrivateMessage pvtMessage = getHibernateTemplate().execute(hcb);

    if (pvtMessage == null)
    {
      log.error("getPrivateMessageWithRecipients(message: " + message
          + ") could not find message");
      throw new RuntimeException("getPrivateMessageWithRecipients(message: " + message
          + ") could not find message");
    }

    return pvtMessage;
  }
    
  
  public List searchPvtMsgs(String typeUuid, String searchText,Date searchFromDate, Date searchToDate, String selectedLabel,
      boolean searchByText, boolean searchByAuthor, boolean searchByBody, boolean searchByLabel, boolean searchByDate)
  {    
    return messageManager.findPvtMsgsBySearchText(typeUuid, searchText,searchFromDate, searchToDate, selectedLabel,
        searchByText,searchByAuthor,searchByBody,searchByLabel,searchByDate);
  }

  public String getAuthorString() {
      String authorString = getCurrentUser();
      
      try
      {
        authorString = userDirectoryService.getUser(authorString).getSortName();

      }
      catch(Exception e)
      {
        log.error(e.getMessage(), e);
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

  public boolean isInstructor()
  {
    log.debug("isInstructor()");
    return isInstructor(userDirectoryService.getCurrentUser());
  }
  
  public boolean isSectionTA()
  {
    log.debug("isSectionTA()");
    return isSectionTA(userDirectoryService.getCurrentUser());
  }

  /**
   * Check if the given user has site.upd access
   * 
   * @param user
   * @return
   */
  private boolean isInstructor(User user)
  {
    if (log.isDebugEnabled())
    {
      log.debug("isInstructor(User " + user + ")");
    }
    if (user != null)
      return securityService.unlock(user, "site.upd", getContextSiteId());
    else
      return false;
  }
  @Override
  public boolean isInstructor(User user, String contextId)
  {
      log.debug("Check is instructor for user: {} in site: {}", user, contextId);
    if (user != null)
      return securityService.unlock(user, "site.upd", contextId);
    else
      return false;
  }

  private boolean isSectionTA(User user) {
      if (user != null)
          return securityService.unlock(user, "section.role.ta", getContextSiteId());
        else
          return false;
  }
  @Override
  public boolean isSectionTA(User user, String contextId) {
      if (user != null)
          return securityService.unlock(user, "section.role.ta", contextId);
        else
          return false;
  }
  
  public boolean isEmailPermit() {
	  log.debug("isEmailPermit()");
	  return isEmailPermit(userDirectoryService.getCurrentUser());
  }

  private boolean isEmailPermit(User user)
  {
    if (log.isDebugEnabled())
    {
      log.debug("isEmailPermit(User " + user + ")");
    }
    if (user != null)
      return securityService.unlock(user, "msg.emailout", getContextSiteId());
    else
      return false;
  }


  @Override
  public boolean isAllowToFieldGroups() {
	  log.debug("isAllowToFieldGroups()");
	  return isAllowToFieldGroups(userDirectoryService.getCurrentUser());
  }

  @Override
  public boolean isAllowToFieldGroups(User user)
  {
    if (log.isDebugEnabled())
    {
      log.debug("isAllowToFieldGroups(User " + user + ")");
    }
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_GROUPS, getContextSiteId());
    else
      return false;
  }

  @Override
  public boolean isAllowToFieldGroups(User user, String contextId)
  {
    log.debug("Check permissions group for user: {} in site: {}", user, contextId);
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_GROUPS, contextId);
    else
      return false;
  }

  @Override
  public boolean isAllowToFieldAllParticipants() {
	  log.debug("isAllowToFieldAllParticipants()");
	  return isAllowToFieldAllParticipants(userDirectoryService.getCurrentUser());
  }

  @Override
  public boolean isAllowToFieldAllParticipants(User user)
  {
    if (log.isDebugEnabled())
    {
      log.debug("isAllowToFieldAllParticipants(User " + user + ")");
    }
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_ALL_PARTICIPANTS, getContextSiteId());
    else
      return false;
  }

  @Override
  public boolean isAllowToFieldAllParticipants(User user, String contextId)
  {
    log.debug("Check permissions for user: {} in site: {}", user, contextId);
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_ALL_PARTICIPANTS, contextId);
    else
      return false;
  }

  @Override
  public boolean isAllowToFieldRoles() {
	  log.debug("isAllowToFieldRoles()");
	  return isAllowToFieldRoles(userDirectoryService.getCurrentUser());
  }

  @Override
  public boolean isAllowToFieldRoles(User user)
  {
    if (log.isDebugEnabled())
    {
      log.debug("isAllowToFieldRoles(User " + user + ")");
    }
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_ROLES, getContextSiteId());
    else
      return false;
  }

  @Override
  public boolean isAllowToFieldRoles(User user, String contextId)
  {
    log.debug("Check permissions roles for user: {} in site: {}", user, contextId);
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_ROLES, contextId);
    else
      return false;
  }

  @Override
  public boolean isAllowToViewHiddenGroups() {
	  log.debug("isAllowToViewHiddenGroups()");
	  return isAllowToViewHiddenGroups(userDirectoryService.getCurrentUser());
  }

  @Override
  public boolean isAllowToViewHiddenGroups(User user)
  {
    if (log.isDebugEnabled())
    {
      log.debug("isAllowToViewHiddenGroups(User " + user + ")");
    }
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_VIEW_HIDDEN_GROUPS, getContextSiteId());
    else
      return false;
  } 

  @Override
  public boolean isAllowToViewHiddenGroups(User user, String contextId)
  {
    log.debug("Check permissions hidden group for user: {} in site: {}", user, contextId);
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_VIEW_HIDDEN_GROUPS, contextId);
    else
      return false;
  }
  
  @Override
  public boolean isAllowToFieldUsers() {
	  log.debug("isAllowToFieldUsers()");
	  return isAllowToFieldUsers(userDirectoryService.getCurrentUser());
  }

  @Override
  public boolean isAllowToFieldUsers(User user)
  {
    if (log.isDebugEnabled())
    {
      log.debug("isAllowToFieldUsers(User " + user + ")");
    }
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_USERS, getContextSiteId());
    else
      return false;
  }

  @Override
  public boolean isAllowToFieldUsers(User user, String contextId)
  {
    log.debug("Check permissions field for user: {} in site: {}", user, contextId);
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_USERS, contextId);
    else
      return false;
  }
  
  @Override
  public boolean isAllowToFieldMyGroups() {
	  log.debug("isAllowToFieldMyGroups()");
	  return isAllowToFieldMyGroups(userDirectoryService.getCurrentUser());
  }

  @Override
  public boolean isAllowToFieldMyGroups(User user)
  {
    if (log.isDebugEnabled())
    {
      log.debug("isAllowToFieldMyGroups(User " + user + ")");
    }
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_MYGROUPS, getContextSiteId());
    else
      return false;
  }

  @Override
  public boolean isAllowToFieldMyGroups(User user, String contextId)
  {
    log.debug("Check permissions to field groups for user: {} in site: {}", user, contextId);
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_MYGROUPS, contextId);
    else
      return false;
  }
  
  @Override
  public boolean isAllowToFieldMyGroupMembers() {
	  log.debug("isAllowToFieldMyGroupMembers()");
	  return isAllowToFieldMyGroupMembers(userDirectoryService.getCurrentUser());
  }

  @Override
  public boolean isAllowToFieldMyGroupMembers(User user)
  {
    if (log.isDebugEnabled())
    {
      log.debug("isAllowToFieldMyGroupMembers(User " + user + ")");
    }
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_MYGROUPMEMBERS, getContextSiteId());
    else
      return false;
  }

  @Override
  public boolean isAllowToFieldMyGroupMembers(User user, String contextId)
  {
    log.debug("Check permissions to field group members for user: {} in site: {}", user, contextId);
    if (user != null)
      return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_MYGROUPMEMBERS, contextId);
    else
      return false;
  }
  
  @Override 
  public boolean isAllowToFieldMyGroupRoles() {
	  return isAllowToFieldMyGroupRoles(userDirectoryService.getCurrentUser());
  }

  @Override
  public boolean isAllowToFieldMyGroupRoles(User user)
  {
	  log.debug("isAllowToFieldMyGroupRoles(User {})",user);
	  if (user != null)
		  return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_MYGROUPROLES, getContextSiteId());
	  else
		  return false;
  }

  @Override
  public boolean isAllowToFieldMyGroupRoles(User user, String contextId)
  {
	  log.debug("isAllowToFieldMyGroupRoles(User {})",user);
	  if (user != null)
		  return securityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_MYGROUPROLES, contextId);
	  else
		  return false;
  }

  /**
   * @return siteId
   */
  public String getContextSiteId()
  {
    log.debug("getContextSiteId()");

    return ("/site/" + toolManager.getCurrentPlacement().getContext());
  }

  public String getContextId()
  {

    log.debug("getContextId()");

    if (TestUtil.isRunningTests())
    {
      return "01001010";
    }
    else
    {
    	//org.sakaiproject.tool.api.ToolManager manager = getInstance();
      return toolManager.getCurrentPlacement().getContext();
    }
  }  
     
  //Helper class
  public String getTopicTypeUuid(String topicTitle)
  {
    String topicTypeUuid;

    if (PVTMSG_MODE_RECEIVED.equals(topicTitle))
    {
      topicTypeUuid=typeManager.getReceivedPrivateMessageType();
    }
    else if (PVTMSG_MODE_SENT.equals(topicTitle))
    {
      topicTypeUuid=typeManager.getSentPrivateMessageType();
    }
    else if (PVTMSG_MODE_DELETE.equals(topicTitle))
    {
      topicTypeUuid=typeManager.getDeletedPrivateMessageType();
    }
    else if (PVTMSG_MODE_DRAFT.equals(topicTitle))
    {
      topicTypeUuid=typeManager.getDraftPrivateMessageType();
    }
    else if (PVTMSG_MODE_SCHEDULER.equals(topicTitle))
    {
      topicTypeUuid=typeManager.getSchedulerPrivateMessageType();
    }
    else
    {
      topicTypeUuid=typeManager.getCustomTopicType(topicTitle);
    }
return topicTypeUuid;
  }
  
  public Area getAreaByContextIdAndTypeId(final String typeId) {
    log.debug("getAreaByContextIdAndTypeId executing for current user: " + getCurrentUser());
    HibernateCallback<Area> hcb = session -> {
        Query q = session.getNamedQuery("findAreaByContextIdAndTypeId");
        q.setParameter("contextId", getContextId(), StringType.INSTANCE);
        q.setParameter("typeId", typeId, StringType.INSTANCE);
        return (Area) q.uniqueResult();
    };

    return getHibernateTemplate().execute(hcb);
  }
  
  public Area getAreaByContextIdAndTypeId(final String typeId, String contextId) {
	    log.debug("getAreaByContextIdAndTypeId executing for current user: " + getCurrentUser());
	    HibernateCallback<Area> hcb = session -> {
	        Query q = session.getNamedQuery("findAreaByContextIdAndTypeId");
	        q.setParameter("contextId", contextId, StringType.INSTANCE);
	        q.setParameter("typeId", typeId, StringType.INSTANCE);
	        return (Area) q.uniqueResult();
	    };

	    return getHibernateTemplate().execute(hcb);
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
  
  private String getResourceBundleString(String key, Object[] replacementValues) {
      return rb.getFormattedMessage(key, replacementValues);
  }

  /**
   * Constructs the event message string
   */
  private String getEventMessage(Object object) {
	  return getEventMessage(object, toolManager.getCurrentTool().getId(), getCurrentUser(), getContextId());
	}
  
  public String getEventMessage(Object object, String toolId, String userId, String contextId) {
  	String eventMessagePrefix = "";
  	
  		if (toolId.equals(DiscussionForumService.MESSAGE_CENTER_ID))
  			eventMessagePrefix = "/messagesAndForums/site/";
  		else if (toolId.equals(DiscussionForumService.MESSAGES_TOOL_ID))
  			eventMessagePrefix = "/messages/site/";
  		else
  			eventMessagePrefix = "/forums/site/";
  	
  	return eventMessagePrefix + contextId + "/" + object.toString() + "/" + userId;
  }

  public PrivateMessage getPrivateMessage(final String id) throws MessagingException {
	  PrivateMessage currentMessage = (PrivateMessage) messageManager.getMessageByIdWithAttachments(Long.parseLong(decrypt(id)));
	  getHibernateTemplate().initialize(currentMessage.getRecipients());
	  return currentMessage;
  }
  
  public PrivateMessage getPrivateMessageByDecryptedId(String id) throws MessagingException {
	  PrivateMessage currentMessage = (PrivateMessage) messageManager.getMessageByIdWithAttachments(Long.parseLong(id));
	  getHibernateTemplate().initialize(currentMessage.getRecipients());
	  return currentMessage;
  }
  
  private PrivateMessage createResponseMessage(PrivateMessage currentMessage, MimeMessage msg, String from) throws MessagingException {
	  PrivateMessage rrepMsg = messageManager.createPrivateMessage() ;

	  if (StringUtils.isBlank(msg.getSubject())) {
		  rrepMsg.setTitle("[" + getResourceBundleString("pvt_no_subject") + "]");
	  } else {
		  String subject = msg.getSubject();
		  String truncatedSubject = StringUtils.truncate(subject, 252) + (subject.length() > 255 ? "..." : "");
		  rrepMsg.setTitle(truncatedSubject);
	  }
	  try {
		  rrepMsg.setCreatedBy(userDirectoryService.getUserByEid(from).getId());
	  } catch (UserNotDefinedException e) {
		  rrepMsg.setCreatedBy(getUserIdByFowardMail(currentMessage.getId(), from));
		  if (rrepMsg.getCreatedBy() == null) {
			  log.warn("Exception: Sender's email does not belong in Sakai, nor is it recognized as an automatic forwarding address. If you have replied to this email from an address other than the one you have configured for email forwarding, please reply from that address.");
			  throw new MessagingException("683 - Sender's email does not belong in Sakai, nor is it recognized as an automatic forwarding address. If you have replied to this email from an address other than the one you have configured for email forwarding, please reply from that address.");
		  }
	  }
	  User userFrom;
	  String fromMail = from;
	  try {
		  userFrom = userDirectoryService.getUser(rrepMsg.getCreatedBy());
		  fromMail = userFrom.getEmail();
	  } catch (UserNotDefinedException e1) {
		  log.warn("Exception: Sender's email does not belong in Sakai, nor is it recognized as an automatic forwarding address. If you have replied to this email from an address other than the one you have configured for email forwarding, please reply from that address.");
		  throw new MessagingException("683 - Sender's email does not belong in Sakai, nor is it recognized as an automatic forwarding address. If you have replied to this email from an address other than the one you have configured for email forwarding, please reply from that address.");
	  }
	  rrepMsg.setAuthor(userFrom.getSortName().concat(" ("+fromMail+")"));
	  rrepMsg.setInReplyTo(currentMessage);
	  if (errRecipient(currentMessage.getRecipients(), rrepMsg)) {
		  log.warn("Exception: Sender's email address does not belong to any of the mail original recipients.  If you are replying from an address different than the one from which you received the email, please use the correct email address to reply to it.");
		  throw new MessagingException("359 - Sender's email address does not belong to any of the mail original recipients.  If you are replying from an address different than the one from which you received the email, please use the correct email address to reply to it.");
	  }
	  if (errMaxMessageRespond(rrepMsg)) {
		  log.warn("Exception: The maximum number of responses for the sender's mail address has been exceeded.  For security reasons, please reply to this mail from Sakai's private messages tool.");
		  throw new MessagingException("682 - The maximum number of responses for the sender's mail address has been exceeded.  For security reasons, please reply to this mail from Sakai's private messages tool.");
	  }

	  rrepMsg.setDraft(Boolean.FALSE);
	  rrepMsg.setDeleted(Boolean.FALSE);
	  rrepMsg.setApproved(Boolean.FALSE);
	  return rrepMsg;
  }

  public PrivateMessage getPvtMsgReplyMessage(PrivateMessage currentMessage, MimeMessage msg, StringBuilder[] bodyBuf, List<Reference> attachments, String from) throws MessagingException {

	  PrivateMessage rrepMsg = createResponseMessage(currentMessage, msg, from);

	  
      StringBuilder cleanBody;
	  if (StringUtils.isNotBlank(bodyBuf[1].toString())) {
		  cleanBody = new StringBuilder(formattedText.processFormattedText(bodyBuf[1].toString(), null, null));
	  } else {
		  cleanBody = new StringBuilder(formattedText.escapeHtml(bodyBuf[0].toString()));
		  if(StringUtils.isNotBlank(cleanBody)) {
			  cleanBody.insert(0, "<pre>");
			  cleanBody.insert(cleanBody.length(), "</pre>");
		  }
	  }
	  
	  if(StringUtils.isBlank(cleanBody)) {
		  log.warn("358 - Unexpected error processing text of body.");
		  throw new MessagingException("358 - Unexpected error processing text of body.");
	  }else {
		  rrepMsg.setBody(cleanBody.toString());
	  }
	  rrepMsg.setLabel(currentMessage.getLabel());

	  StringBuilder sendToString = new StringBuilder("");

	  if (StringUtils.isNotBlank(currentMessage.getAuthor()))
	  {
		  sendToString.append(currentMessage.getAuthor());
	  }
	  rrepMsg.setRecipientsAsText(sendToString.toString());
	  /** add sender as a saved recipient */
	  List recipientList = new UniqueArrayList();
	  PrivateMessageRecipientImpl sender = new PrivateMessageRecipientImpl(
			  currentMessage.getCreatedBy(), typeManager.getSentPrivateMessageType(),
			  ((PrivateMessageRecipientImpl)currentMessage.getRecipients().get(0)).getContextId(), Boolean.FALSE, false);

	  recipientList.add(sender);

	  rrepMsg.setRecipients(recipientList);
	  rrepMsg.setModifiedBy(rrepMsg.getCreatedBy());
	  //Add attachments
	  if (CollectionUtils.isNotEmpty(attachments)){
		  for (int i=0; i<attachments.size(); i++)
		  {
			  Attachment thisAttach = createPvtMsgAttachment(attachments.get(i).getId(), attachments.get(i).getProperties().getProperty(attachments.get(i).getProperties().getNamePropDisplayName()));
			  addAttachToPvtMsg(rrepMsg, thisAttach);
		  }
	  }
		
	  return rrepMsg;
  }
  
  public Map<User, Boolean> getRecipients(List recipients) {     
	  Map<User, Boolean> returnSet = new HashMap<>();
    
	  /** get List of unfiltered course members */
	  for (Iterator iterator = recipients.iterator(); iterator.hasNext();) {
		  PrivateMessageRecipient element = (PrivateMessageRecipient) iterator.next();
		  try {
			  returnSet.put(userDirectoryService.getUser(element.getUserId()), false);
		  } catch (UserNotDefinedException e) {
			  log.warn(e.getMessage(), e);
		  }
	  }
	  //now add them all back
	  return returnSet;
  }

  private boolean errRecipient(List recipients, PrivateMessage rrepMsg) {
	  for (Iterator iterator = recipients.iterator(); iterator.hasNext();) {
		  PrivateMessageRecipient element = (PrivateMessageRecipient) iterator.next();
		  if (element.getUserId().equals(rrepMsg.getCreatedBy())) {
			  return false;
		  }
	  }
	  return true;
  }

  private boolean errMaxMessageRespond(PrivateMessage rrepMsg) {
	  int max = serverConfigurationService.getInt("msgcntr.no.reply.max.respond", 5);
	  return getNumMessageRespond(rrepMsg.getCreatedBy(), rrepMsg.getInReplyTo().getId()) > max;
  }
  
  private int getNumMessageRespond(final String userId, final Long messageId) {
	  log.debug("getNumMessageRespond executing");

	  HibernateCallback<Number> hcb = session -> {
		  Query q = session.getNamedQuery(QUERY_RESPONSED_COUNT);
		  q.setParameter("userId", userId, StringType.INSTANCE);
		  q.setParameter("messageId", messageId, LongType.INSTANCE);
		  return (Number) q.uniqueResult();
	  };
    
	  return getHibernateTemplate().execute(hcb).intValue();
  }
  
  private String getUserIdByFowardMail(final Long messageId, final String mail) {
	  log.debug("getUserIdByFowardMail executing");

	  HibernateCallback<String> hcb = session -> {
		  Query q = session.getNamedQuery(QUERY_USER_ID_BY_FOWARD_MAIL);
		  q.setParameter("messageId", messageId, LongType.INSTANCE);
		  q.setParameter("mail", mail, StringType.INSTANCE);
		  return (String)q.uniqueResult();
	  };

	  return getHibernateTemplate().execute(hcb);
  }

  public void processPvtMsgReplySentAction(PrivateMessage currentMessage, PrivateMessage rrepMsg) {
	  if (rrepMsg != null) {
		  Map<User, Boolean> recipients = getRecipients(rrepMsg.getRecipients());

		  sendPrivateMessage(rrepMsg, recipients, false, false);

		  if (!rrepMsg.getDraft()) {
			  markMessageAsRepliedForUser(currentMessage, rrepMsg.getAuthorId());
			  String contextId = ((PrivateMessageRecipientImpl)currentMessage.getRecipients().get(0)).getContextId();
			  markMessageAsReadForUser(currentMessage, contextId, rrepMsg.getAuthorId(), DiscussionForumService.MESSAGES_TOOL_ID);
			  LRS_Statement statement = null;
			  try{
				  statement = getStatementForUserSentPvtMsg(currentMessage.getTitle(), SAKAI_VERB.responded, currentMessage);
			  }catch(Exception e){
				  log.error(e.getMessage(), e);
			  }

			  Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_RESPONSE, getEventMessage(currentMessage, DiscussionForumService.MESSAGES_TOOL_ID, rrepMsg.getAuthorId(), ((PrivateMessageRecipientImpl)currentMessage.getRecipients().get(0)).getContextId()), contextId, true, NotificationService.NOTI_OPTIONAL, statement);
			  eventTrackingService.post(event);
		  }
	  }
  }
  
  private String encrypt(String value) {
	  try {
		  IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
		  String key = serverConfigurationService.getString(ENCRYPTION_SECRET_PROPERTY, this.getServerName());
		  SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "DES");

		  Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5PADDING");
		  cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

		  byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

		  return Hex.encodeHexString(Base64.encodeBase64String(encrypted).getBytes(StandardCharsets.UTF_8));

	  } catch (Exception ex) {
		  log.error(ex.getMessage(), ex);
	  }

	  return null;
  }

  private String decrypt(String encrypted) throws MessagingException {
	  try {
		  String hexencrypted = new String(Hex.decodeHex(encrypted.toLowerCase().toCharArray()),StandardCharsets.UTF_8);
		  IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
		  String key = serverConfigurationService.getString(ENCRYPTION_SECRET_PROPERTY,  this.getServerName());
		  SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "DES");

		  Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5PADDING");
		  cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

		  byte[] original = cipher.doFinal(Base64.decodeBase64(hexencrypted));

		  return new String(original,StandardCharsets.UTF_8);
	  } catch (Exception ex) {
		  log.warn("Exception: Recipient couldn't be obtained. Please, reply to this mail from Sakai's private messages tool.");
		  throw new MessagingException("521 - Recipient couldn't be obtained. Please, reply to this mail from Sakai's private messages tool.");
	  }
  }
  
  public LRS_Statement getStatementForUserSentPvtMsg(String subject, SAKAI_VERB sakaiVerb, PrivateMessage rrepMsg) {
	  LRS_Actor student = learningResourceStoreService.getActor(rrepMsg.getCreatedBy());
	  String url = serverConfigurationService.getPortalUrl();
	  LRS_Verb verb = new LRS_Verb(sakaiVerb);
	  LRS_Object lrsObject = new LRS_Object(url + "/privateMessage", "send-private-message");
	  HashMap<String, String> nameMap = new HashMap<>();
	  nameMap.put("en-US", "User sent a private message");
	  lrsObject.setActivityName(nameMap);
	  HashMap<String, String> descMap = new HashMap<>();
	  descMap.put("en-US", "User sent a private message with subject: " + subject);
	  lrsObject.setDescription(descMap);
	  return new LRS_Statement(student, verb, lrsObject);
  }

  private List<DraftRecipient> getDraftRecipients(long msgId, List<MembershipItem> recipients, List<MembershipItem> bccRecipients) {
	  List<DraftRecipient> draftRecipients = recipients.stream().map(mi -> DraftRecipient.from(mi, msgId, false)).collect(Collectors.toList());
	  draftRecipients.addAll(bccRecipients.stream().map(mi -> DraftRecipient.from(mi, msgId, true)).collect(Collectors.toList()));

	  return draftRecipients.stream().filter(dr -> dr.getType() != MembershipItem.TYPE_NOT_SPECIFIED).collect(Collectors.toList());
  }

  public void sendProgamMessage(PrivateMessage message, List<MembershipItem> draftRecipients, List<MembershipItem> draftBccRecipients, boolean asEmail) {
	    if (message == null || draftRecipients == null || draftRecipients.size() == 0)
	    {
	      throw new IllegalArgumentException("Null Argument");
	    }

	    String contextId="";
	    boolean isMailArchive = false;
	    try {
	        contextId = getContextId();
	    } catch (Exception e) {
		    contextId = ((PrivateMessageRecipientImpl)message.getRecipients().get(0)).getContextId();
		    isMailArchive = true;
		}
	    String currentUserAsString = currentUserAsString(message, isMailArchive);

	    List recipientList = new UniqueArrayList();

        PrivateMessageRecipient receiver = new PrivateMessageRecipientImpl(currentUserAsString, typeManager.getSchedulerPrivateMessageType(),
            contextId, Boolean.TRUE, false);

        recipientList.add(receiver);
        message.setRecipients(recipientList);
        message.setDraft(true);
        Message savedMessage = saveMessage(message, isMailArchive, contextId, currentUserAsString);

        List<DraftRecipient> allDraftRecipients = getDraftRecipients(savedMessage.getId(), draftRecipients, draftBccRecipients);
        messageManager.deleteDraftRecipientsByMessageId(savedMessage.getId());
        messageManager.saveDraftRecipients(savedMessage.getId(), allDraftRecipients);

        message.setId(savedMessage.getId());
  }

  private String getSiteTitle(String contextId) {
    String siteTitle = null;
    try {
      siteTitle = siteService.getSite(contextId).getTitle();
    } catch (IdUnusedException e) {
      log.error(e.getMessage(), e);
    }
    return siteTitle;
  }

}
