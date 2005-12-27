/**********************************************************************************
* $URL: $
* $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.messageforums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.PrivateMessageRecipient;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.api.kernel.session.ToolSession;
import org.sakaiproject.api.kernel.session.cover.SessionManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.framework.portal.cover.PortalService;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.authzGroup.Member;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.authzGroup.cover.AuthzGroupService;
import org.sakaiproject.service.legacy.content.ContentResource;
import org.sakaiproject.service.legacy.content.cover.ContentHostingService;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.filepicker.FilePickerHelper;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.sakaiproject.tool.messageforums.ui.PrivateForumDecoratedBean;
import org.sakaiproject.tool.messageforums.ui.PrivateMessageDecoratedBean;
import org.sakaiproject.tool.messageforums.ui.PrivateTopicDecoratedBean;

public class PrivateMessagesTool
{
  private static final Log LOG = LogFactory.getLog(PrivateMessagesTool.class);

  /**
   *Dependency Injected 
   */
  private PrivateMessageManager prtMsgManager;
  private MessageForumsMessageManager messageManager;
  private MessageForumsForumManager forumManager;
  private ErrorMessages errorMessages;
  SakaiPersonManager sakaiPersonManager ;
  
  /** Dependency Injected   */
  private MessageForumsTypeManager typeManager;
  
  /** Naigation for JSP   */
  public static final String MAIN_PG="main";
  public static final String DISPLAY_MESSAGES_PG="pvtMsg";
  public static final String SELECTED_MESSAGE_PG="pvtMsgDetail";
  public static final String COMPOSE_MSG_PG="compose";
  public static final String MESSAGE_SETTING_PG="pvtMsgSettings";
  public static final String SEARCH_RESULT_MESSAGES_PG="pvtMsgEx";
  public static final String DELETE_MESSAGES_PG="pvtMsgDelete";
  public static final String DELETE_FOLDER_PG="pvtMsgFolderDelete";
  public static final String MESSAGE_STATISTICS_PG="pvtMsgStatistics";
  
  /** portlet configuration parameter values**/
  public static final String PVTMSG_MODE_RECEIVED = "Received";
  public static final String PVTMSG_MODE_SENT = "Sent";
  public static final String PVTMSG_MODE_DELETE = "Deleted";
  public static final String PVTMSG_MODE_DRAFT = "Drafts";
  public static final String PVTMSG_MODE_CASE = "Personal Folders";
  
  public static final String RECIPIANTS_ENTIRE_CLASS= "Entire Class";
  public static final String RECIPIANTS_ALL_INSTRUCTORS= "All Instructors";
  
  public static final String SET_AS_YES="yes";
  public static final String SET_AS_NO="no";    
  
  PrivateForumDecoratedBean decoratedForum;
  
  private Area area;
  private PrivateForum forum;  
  private List pvtTopics=new ArrayList();
  private List decoratedPvtMsgs;
  private String msgNavMode="" ;
  private PrivateMessageDecoratedBean detailMsg ;
  
  private String currentMsgUuid; //this is the message which is being currently edited/displayed/deleted
  private boolean navModeIsDelete=false ; // Delete mode to show up extra buttons in pvtMsg.jsp page
  private List selectedItems;
  
  private String userName;    //current user
  private Date time ;       //current time
  
  //delete confirmation screen - single delete 
  private boolean deleteConfirm=false ; //used for displaying delete confirmation message in same jsp
  private boolean validEmail=true ;
  
  //Compose Screen
  private List selectedComposeToList = new ArrayList();
  private String composeSendAsPvtMsg=SET_AS_YES; // currently set as Default as change by user is allowed
  private String composeSubject ;
  private String composeBody ;
  private String composeLabel ;   
  private List totalComposeToList;
  private List totalComposeToListRecipients;
  
  //Delete items - Checkbox display and selection - Multiple delete
  private List selectedDeleteItems;
  private List totalDisplayItems=new ArrayList() ;
  
  //reply to 
  private String replyToBody ;
  private String replyToSubject;
  //Setting Screen
  private String activatePvtMsg=SET_AS_NO; 
  private String forwardPvtMsg=SET_AS_NO;
  private String forwardPvtMsgEmail;
  private boolean superUser; 
  
  //message header screen
  private String searchText="";
  private String selectView;
  //////////////////////
  /** The configuration mode, received, sent,delete, case etc ... */
  public static final String STATE_PVTMSG_MODE = "pvtmsg.mode";
  
  public PrivateMessagesTool()
  {    
  }

  
  /**
   * @return
   */
  public MessageForumsTypeManager getTypeManager()
  {
    return typeManager;
  }


  /**
   * @param prtMsgManager
   */
  public void setPrtMsgManager(PrivateMessageManager prtMsgManager)
  {
    this.prtMsgManager = prtMsgManager;
  }
  
  /**
   * @param messageManager
   */
  public void setMessageManager(MessageForumsMessageManager messageManager)
  {
    this.messageManager = messageManager;
  }

  
  /**
   * @param typeManager
   */
  public void setTypeManager(MessageForumsTypeManager typeManager)
  {
    this.typeManager = typeManager;
  }

  
  /**
   * @param sakaiPersonManager The sakaiPersonManager to set.
   */
  public void setSakaiPersonManager(SakaiPersonManager sakaiPersonManager)
  {
    this.sakaiPersonManager = sakaiPersonManager;
  }


  public void initializePrivateMessageArea()
  {           
    /** get area per request */
    area = prtMsgManager.getPrivateMessageArea();
            
    if (getPvtAreaEnabled() || isInstructor()){      
      PrivateForum pf = prtMsgManager.initializePrivateMessageArea(area);    
      pvtTopics = pf.getTopics();
      forum=pf;           
      activatePvtMsg = (Boolean.TRUE.equals(area.getEnabled())) ? "yes" : "no";
      forwardPvtMsg = (Boolean.TRUE.equals(pf.getAutoForward())) ? "yes" : "no";
      forwardPvtMsgEmail = pf.getAutoForwardEmail();      
    }                
  }
  
  public boolean getPvtAreaEnabled()
  {      
    return area.getEnabled().booleanValue();
  }    
  
//  public void setPvtAreaEnabled(boolean value){
//    
//    Area varArea = prtMsgManager.getPrivateMessageArea();
//    varArea.setEnabled(Boolean.valueOf(value));
//    prtMsgManager.savePrivateMessageArea(varArea);    
//    
//  }
  
  //Return decorated Forum
  public PrivateForumDecoratedBean getDecoratedForum()
  {      
      PrivateForumDecoratedBean decoratedForum = new PrivateForumDecoratedBean(getForum()) ;
      
      /** only load topics/counts if area is enabled */
      if (getPvtAreaEnabled()){        
        for (Iterator iterator = pvtTopics.iterator(); iterator.hasNext();)
        {
          Topic topic = (Topic) iterator.next();
          if (topic != null)
          {
            PrivateTopicDecoratedBean decoTopic= new PrivateTopicDecoratedBean(topic) ;
            //decoTopic.setTotalNoMessages(prtMsgManager.getTotalNoMessages(topic)) ;
            //decoTopic.setUnreadNoMessages(prtMsgManager.getUnreadNoMessages(SessionManager.getCurrentSessionUserId(), topic)) ;
          
            String typeUuid = getPrivateMessageTypeFromContext(topic.getTitle());          
          
            decoTopic.setTotalNoMessages(prtMsgManager.findMessageCount(topic.getId(),
              typeUuid));
            decoTopic.setUnreadNoMessages(prtMsgManager.findUnreadMessageCount(topic.getId(),
              typeUuid));
          
            decoratedForum.addTopic(decoTopic);
          }          
        }
      }   
    return decoratedForum ;
  }

  public List getDecoratedPvtMsgs()
  {
    decoratedPvtMsgs=new ArrayList() ;
    
    String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);        
    
    decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
        PrivateMessageManager.SORT_DESC);
    
    decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);
    
//    Area privateArea=prtMsgManager.getPrivateMessageArea();
//    if(privateArea != null ) {
//     List forums=privateArea.getPrivateForums();
//      //Private message return ONLY ONE ELEMENT
//      for (Iterator iter = forums.iterator(); iter.hasNext();)
//      {
//        forum = (PrivateForum) iter.next();
//        pvtTopics=forum.getTopics();
//        
//        //now get messages for each topics
//        for (Iterator iterator = pvtTopics.iterator(); iterator.hasNext();)
//        {
//          Topic topic = (Topic) iterator.next();          
//          if(topic.getTitle().equals(PVTMSG_MODE_RECEIVED))
//          {            
//            //TODO -- getMessages() should be changed to getReceivedMessages() ;
//            //decoratedPvtMsgs=prtMsgManager.getReceivedMessages(getUserId()) ;
//            decoratedPvtMsgs=topic.getMessages() ;
//            break;
//          } 
//          if(topic.getTitle().equals(PVTMSG_MODE_SENT))
//          {
//            //decoratedPvtMsgs=prtMsgManager.getSentMessages(getUserId()) ;
//            decoratedPvtMsgs=topic.getMessages() ;
//            break;
//          }  
//          if(topic.getTitle().equals(PVTMSG_MODE_DELETE))
//          {
//            //decoratedPvtMsgs=prtMsgManager.getDeletedMessages(getUserId()) ;
//            decoratedPvtMsgs=topic.getMessages() ;
//            break;
//          }  
//          if(topic.getTitle().equals(PVTMSG_MODE_DRAFT))
//          {
//            //decoratedPvtMsgs=prtMsgManager.getDraftedMessages(getUserId()) ;
//            decoratedPvtMsgs=topic.getMessages() ;
//            break;
//          }  
//          if(topic.getTitle().equals(PVTMSG_MODE_CASE))
//          {
//            //decoratedPvtMsgs=prtMsgManager.getMessagesByTopic(getUserId(), getSelectedTopicId()) ;
//            decoratedPvtMsgs=topic.getMessages() ;
//            break;
//          }    
//        }
//        //create decorated List
//        decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);
//      }
//    }
    return decoratedPvtMsgs ;
  }
  
  //decorated display - from List of Message
  public List createDecoratedDisplay(List msg)
  {
    List decLs= new ArrayList() ;
    for (Iterator iter = msg.iterator(); iter.hasNext();)
    {
      PrivateMessage element = (PrivateMessage) iter.next();                  
      
      boolean prev = prtMsgManager.hasPreviousMessage(element);
      boolean next = prtMsgManager.hasNextMessage(element);
      PrivateMessageDecoratedBean dbean= new PrivateMessageDecoratedBean(element);
      dbean.setHasPreviousMsg(prev);
      dbean.setHasNextMsg(next);
      
      decLs.add(dbean) ;
    }
    return decLs;
  }
  
  
  public void setDecoratedPvtMsgs(List displayPvtMsgs)
  {
    this.decoratedPvtMsgs=displayPvtMsgs;
  }
  
  public String getMsgNavMode() 
  {
    return msgNavMode ;
  }
 
  public PrivateMessageDecoratedBean getDetailMsg()
  {
    return detailMsg ;
  }

  public void setDetailMsg(PrivateMessageDecoratedBean detailMsg)
  {    
    this.detailMsg = detailMsg;
  }

  public String getCurrentMsgUuid()
  {
    return currentMsgUuid;
  }
  
  public void setCurrentMsgUuid(String currentMsgUuid)
  {
    this.currentMsgUuid = currentMsgUuid;
  }

  public List getSelectedItems()
  {
    return selectedItems;
  }
  
  public void setSelectedItems(List selectedItems)
  {
    this.selectedItems=selectedItems ;
  }
  
  public boolean isDeleteConfirm()
  {
    return deleteConfirm;
  }

  public void setDeleteConfirm(boolean deleteConfirm)
  {
    this.deleteConfirm = deleteConfirm;
  }

 
  public boolean isValidEmail()
  {
    return validEmail;
  }
  public void setValidEmail(boolean validEmail)
  {
    this.validEmail = validEmail;
  }


  public boolean getNavModeIsDelete()
  {
    return navModeIsDelete;
  }
  
  public void setNavModeIsDelete(boolean navModeIsDelete)
  {
    this.navModeIsDelete=navModeIsDelete ;
  }
  
  //Deleted page - checkbox display and selection
  public List getSelectedDeleteItems()
  {
    return selectedDeleteItems;
  }
  public List getTotalDisplayItems()
  {
    return totalDisplayItems;
  }
  public void setTotalDisplayItems(List totalDisplayItems)
  {
    this.totalDisplayItems = totalDisplayItems;
  }
  public void setSelectedDeleteItems(List selectedDeleteItems)
  {
    this.selectedDeleteItems = selectedDeleteItems;
  }

  //Compose Getter and Setter
  public String getComposeBody()
  {
    return composeBody;
  }
  
  public void setComposeBody(String composeBody)
  {
    this.composeBody = composeBody;
  }

  public String getComposeLabel()
  {
    return composeLabel;
  }
  
  public void setComposeLabel(String composeLabel)
  {
    this.composeLabel = composeLabel;
  }

  public String getComposeSendAsPvtMsg()
  {
    return composeSendAsPvtMsg;
  }

  public void setComposeSendAsPvtMsg(String composeSendAsPvtMsg)
  {
    this.composeSendAsPvtMsg = composeSendAsPvtMsg;
  }

  public String getComposeSubject()
  {
    return composeSubject;
  }

  public void setComposeSubject(String composeSubject)
  {
    this.composeSubject = composeSubject;
  }

  public void setSelectedComposeToList(List selectedComposeToList)
  {
    this.selectedComposeToList = selectedComposeToList;
  }
  
  public void setTotalComposeToList(List totalComposeToList)
  {
    this.totalComposeToList = totalComposeToList;
  }
  
  public List getSelectedComposeToList()
  {
    return selectedComposeToList;
  }
  
  //return a list of participants of 'participant' type object
  
  public List getTotalComposeToList()
  { 
    totalComposeToListRecipients = new ArrayList();
    
    List roles = new ArrayList();
    /** get roles for realm */    
    try
    {
      AuthzGroup group = AuthzGroupService.getAuthzGroup(SiteService.siteReference(PortalService.getCurrentSiteId()));
      if (group != null && group.getRoles() != null){
        for (Iterator i = group.getRoles().iterator(); i.hasNext();){
          Role r = (Role) i.next();
          RecipientItem p = new RecipientItem();
          String roleId = r.getId();          
          switch (roleId.length()){
            case 0:
              break;
            case 1:
              roleId = roleId.toUpperCase();
              break;
            default:
              roleId = roleId.substring(0,1).toUpperCase() + roleId.substring(1);              
          }          
          p.setName(roleId + " Group");
          p.setRole(r);
          roles.add(p);
        }                  
      }      
    }
    catch (IdUnusedException e)
    {
      LOG.error(e.getMessage(), e);      
    }    
    
    Collections.sort(roles);
    
    /** add entire class to top of list */        
    RecipientItem entireClass = new RecipientItem();
    entireClass.setName("Entire Class");
    roles.add(0, entireClass);                
    
    List totalComposeToList=new ArrayList() ;
    for (int i = 0; i < roles.size(); i++)
    {
      RecipientItem currentRecipient = (RecipientItem) roles.get(i); 
      String currentDisplayName = currentRecipient.getName();      
      totalComposeToList.add(new SelectItem(currentDisplayName));
      totalComposeToListRecipients.add(currentRecipient);
    }
    
    Set members = getAllCourseMembers();  
    for (Iterator i = members.iterator(); i.hasNext();)
    {
      RecipientItem currentRecipient = (RecipientItem) i.next();
      String currentDisplayName = currentRecipient.getName();
      totalComposeToList.add(new SelectItem(currentDisplayName));
      totalComposeToListRecipients.add(currentRecipient);
    }
    
    this.totalComposeToList = totalComposeToList;    
    return totalComposeToList;
  }

  public String getUserName() {
   String userId=SessionManager.getCurrentSessionUserId();
   try
   {
     User user=UserDirectoryService.getUser(userId) ;
     userName= user.getDisplayName();
   }
   catch (IdUnusedException e)
   {
     LOG.debug("getUserName() - Error");
   }
   return userName;
  }
  
  public String getUserId()
  {
    return SessionManager.getCurrentSessionUserId();
  }
  //Reply time
  public Date getTime()
  {
    return new Date();
  }
  //Reply to page
  public String getReplyToBody() {
    return replyToBody;
  }
  public void setReplyToBody(String replyToBody) {
    this.replyToBody=replyToBody;
  }
  public String getReplyToSubject()
  {
    if(getDetailMsg() != null)
    {
      replyToSubject="Re:" +getDetailMsg().getMsg().getTitle();
    }
    return replyToSubject;
  }
  public void setReplyToSubject(String replyToSubject)
  {
    this.replyToSubject = replyToSubject;
  }


  //message header Getter 
  public String getSearchText()
  {
    return searchText ;
  }
  public void setSearchText(String searchText)
  {
    this.searchText=searchText;
  }
  public String getSelectView() 
  {
    return selectView ;
  }
  public void setSelectView(String selectView)
  {
    this.selectView=selectView ;
  }
  //////////////////////////////////////////////////////////////////////////////////  
  /**
   * called when any topic like Received/Sent/Deleted clicked
   * @return - pvtMsg
   */
  private String selectedTopicTitle="";
  private String selectedTopicId="";
  public String getSelectedTopicTitle()
  {
    return selectedTopicTitle ;
  }
  public void setSelectedTopicTitle(String selectedTopicTitle) 
  {
    this.selectedTopicTitle=selectedTopicTitle;
  }
  public String getSelectedTopicId()
  {
    return selectedTopicId;
  }
  private void setSelectedTopicId(String selectedTopicId)
  {
    this.selectedTopicId=selectedTopicId;    
  }
  
  public String processActionHome()
  {
    LOG.debug("processActionHome()");
    return  "main";
  }
  public String processDisplayForum()
  {
    LOG.debug("processDisplayForum()");
    return "pvtMsg" ;
  }
  public String processPvtMsgTopic()
  {
    LOG.debug("processPvtMsgTopic()");
    //get external parameter
    selectedTopicTitle = getExternalParameterByKey("pvtMsgTopicTitle") ;
    setSelectedTopicId(getExternalParameterByKey("pvtMsgTopicId")) ;
    msgNavMode=getSelectedTopicTitle();

    return "pvtMsg";
  }
  
  public String processHpView()
  { 
    //TODO - check if instructor
    return "pvtMsgHpView";
  }
  /**
   * process Cancel from all JSP's
   * @return - pvtMsg
   */  
  public String processPvtMsgCancel() {
    LOG.debug("processPvtMsgCancel()");
    return "main";     
  }

  /**
   * called when subject of List of messages to Topic clicked for detail
   * @return - pvtMsgDetail
   */ 
  public String processPvtMsgDetail() {
    LOG.debug("processPvtMsgDetail()");
    
    String msgId=getExternalParameterByKey("current_msg_detail");
    setCurrentMsgUuid(msgId) ; 
    //retrive the detail for this message with currentMessageId    
    for (Iterator iter = decoratedPvtMsgs.iterator(); iter.hasNext();)
    {
      PrivateMessageDecoratedBean dMsg= (PrivateMessageDecoratedBean) iter.next();
      if (dMsg.getMsg().getId().equals(new Long(msgId)))
      {
        this.setDetailMsg(dMsg);        
        prtMsgManager.markMessageAsReadForUser(dMsg.getMsg());
               
        PrivateMessage initPrivateMessage = prtMsgManager.initMessageWithAttachmentsAndRecipients(dMsg.getMsg());
        this.setDetailMsg(new PrivateMessageDecoratedBean(initPrivateMessage));
        
        List recLs= dMsg.getMsg().getRecipients();
        for (Iterator iterator = recLs.iterator(); iterator.hasNext();)
        {
          PrivateMessageRecipient element = (PrivateMessageRecipient) iterator.next();
          if((element.getRead().booleanValue()) || (element.getUserId().equals(getUserId())) )
          {
           getDetailMsg().setHasRead(true) ;
          }
        }
      }
    }
    this.deleteConfirm=false; //reset this as used for multiple action in same JSP
    
    return "pvtMsgDetail";
  }

  /**
   * called from Single delete Page
   * @return - pvtMsgReply
   */ 
  public String processPvtMsgReply() {
    LOG.debug("processPvtMsgReply()");
    
    //from message detail screen
    this.setDetailMsg(getDetailMsg()) ;
//    
//    //from compose screen
//    this.setComposeSendAs(getComposeSendAs()) ;
//    this.setTotalComposeToList(getTotalComposeToList()) ;
//    this.setSelectedComposeToList(getSelectedComposeToList()) ;
    
    return "pvtMsgReply";
  }
  
  /**
   * called from Single delete Page
   * @return - pvtMsgMove
   */ 
  public String processPvtMsgMove() {
    LOG.debug("processPvtMsgMove()");
    return "pvtMsgMove";
  }
  
  /**
   * called from Single delete Page
   * @return - pvtMsgDetail
   */ 
  public String processPvtMsgDeleteConfirm() {
    LOG.debug("processPvtMsgDeleteConfirm()");
    
    this.setDeleteConfirm(true);
    /*
     * same action is used for delete..however if user presses some other action after first
     * delete then 'deleteConfirm' boolean is reset
     */
    return "pvtMsgDetail" ;
  }
  
  /**
   * called from Single delete Page -
   * called when 'delete' button pressed second time
   * @return - pvtMsg
   */ 
  public String processPvtMsgDeleteConfirmYes() {
    LOG.debug("processPvtMsgDeleteConfirmYes()");
    if(getDetailMsg() != null)
    {      
      prtMsgManager.deletePrivateMessage(getDetailMsg().getMsg(), getPrivateMessageTypeFromContext(msgNavMode));      
    }
    return "main" ;
  }
  
  //RESET form variable - required as the bean is in session and some attributes are used as helper for navigation
  public void resetFormVariable() {
    
    this.setNavModeIsDelete(false); 
    this.msgNavMode="" ;
    this.deleteConfirm=false;
    
    attachments.clear();
    oldAttachments.clear();
  }
  
  /**
   * process Compose action from different JSP'S
   * @return - pvtMsgCompose
   */ 
  public String processPvtMsgCompose() {
    this.setDetailMsg(new PrivateMessageDecoratedBean(messageManager.createPrivateMessage()));
    LOG.debug("processPvtMsgCompose()");
    return "pvtMsgCompose" ;
  }
  
  
  public String processPvtMsgComposeCancel()
  {
    LOG.debug("processPvtMsgComposeCancel()");
    resetComposeContents();
    if(getMsgNavMode().equals(""))
    {
      return "main" ; // if navigation is from main page
    }
    else
    {
      return "pvtMsg";
    } 
  }
  
  public void resetComposeContents()
  {
    this.setComposeBody("");
    this.setComposeSubject("");
    this.getSelectedComposeToList().clear();
    this.setReplyToSubject("");
    this.setReplyToBody("");
  }
  /**
   * process from Compose screen
   * @return - pvtMsg
   */ 
  public String processPvtMsgSend() {
          
    LOG.debug("processPvtMsgSend()");
    
    if(!hasValue(getComposeSubject()))
    {
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage("Please enter subject for this compose message."));
      return null ;
    }
    if(!hasValue(getComposeBody()) )
    {
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage("Please enter body for this compose message."));
      return null ;
    }
    if(getSelectedComposeToList().size()<1)
    {
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage("Please select recipiants list for this compose message."));
      return null ;
    }
    
    PrivateMessage pMsg= constructMessage() ;
    
    if((SET_AS_YES).equals(getComposeSendAsPvtMsg()))
    {
      prtMsgManager.sendPrivateMessage(pMsg, getRecipients()); 
    }

    //reset contents
    resetComposeContents();
    if(getMsgNavMode().equals(""))
    {
      return "main" ; // if navigation is from main page
    }
    else
    {
      return "pvtMsg";
    }
  }
 
  /**
   * process from Compose screen
   * @return - pvtMsg
   */
  public String processPvtMsgSaveDraft() {
    LOG.debug("processPvtMsgSaveDraft()");
    if(!hasValue(getComposeSubject()))
    {
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage("Please enter subject for this compose message."));
      return null ;
    }
    if(!hasValue(getComposeBody()) )
    {
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage("Please enter body for this compose message."));
      return null ;
    }
    if(getSelectedComposeToList().size()<1)
    {
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage("Please select recipiants list for this compose message."));
      return null ;
    }
    
    PrivateMessage dMsg=constructMessage() ;
    dMsg.setDraft(Boolean.TRUE);
    
    if((SET_AS_YES).equals(getComposeSendAsPvtMsg()))
    {
      prtMsgManager.sendPrivateMessage(dMsg, getRecipients()); 
    }

    //reset contents
    resetComposeContents();
    
    if(getMsgNavMode().equals(""))
    {
      return "main" ; // if navigation is from main page
    }
    else
    {
      return "pvtMsg";
    } 
  }
  // created separate method as to be used with processPvtMsgSend() and processPvtMsgSaveDraft()
  public PrivateMessage constructMessage()
  {
    PrivateMessage aMsg;
    // in case of compose this is a new message 
    if (this.getDetailMsg() == null )
    {
      aMsg = messageManager.createPrivateMessage() ;
    }
    //if reply to a message then message is existing
    else {
      aMsg = (PrivateMessage)this.getDetailMsg().getMsg();       
    }
    if (aMsg != null)
    {
      aMsg.setTitle(getComposeSubject());
      aMsg.setBody(getComposeBody());
      // these are set by the create method above -- you can remove them or keep them if you really want :)
      //aMsg.setCreatedBy(getUserId());
      //aMsg.setCreated(getTime()) ;
      aMsg.setAuthor(getUserId());
      aMsg.setDraft(Boolean.FALSE);      
      aMsg.setApproved(Boolean.FALSE);      
    }
    //Add attachments
    for(int i=0; i<attachments.size(); i++)
    {
      prtMsgManager.addAttachToPvtMsg(aMsg, (Attachment)attachments.get(i));         
    }    
    //clear
    attachments.clear();
    oldAttachments.clear();
    
    return aMsg;    
  }
  ///////////////////// Previous/Next topic and message on Detail message page
  public String processDisplayNextMsg()
  {
    LOG.debug("processDisplayNextMsg()");
    //return processDisplayMsgById("nextMsgId");
    PrivateMessage pmsg = (PrivateMessage) prtMsgManager.getNextMessage(getDetailMsg().getMsg());
    boolean prev = prtMsgManager.hasPreviousMessage(getDetailMsg().getMsg());
    boolean next = prtMsgManager.hasNextMessage(getDetailMsg().getMsg());
    
    this.setDetailMsg(new PrivateMessageDecoratedBean(pmsg)) ;
    
    //set boolean in decoratedbean 
    getDetailMsg().setHasPreviousMsg(prev);
    getDetailMsg().setHasNextMsg(next);
    
    return "pvtMsgDetail";
  }
  
  /**
   * @return
   */
  public String processDisplayPreviousMsg()
  {
    LOG.debug("processDisplayPreviousMsg()");
    //return processDisplayMsgById("previousMsgId");
    PrivateMessage pmsg = (PrivateMessage) prtMsgManager.getPreviousMessage(getDetailMsg().getMsg());
    boolean prev = prtMsgManager.hasPreviousMessage(getDetailMsg().getMsg());
    boolean next = prtMsgManager.hasNextMessage(getDetailMsg().getMsg());
    
    this.setDetailMsg(new PrivateMessageDecoratedBean(pmsg)) ;
    
    //set boolean in decoratedbean 
    getDetailMsg().setHasPreviousMsg(prev);
    getDetailMsg().setHasNextMsg(next);
    
    return "pvtMsgDetail";
  }
  /**
   * @param externalTopicId
   * @return
   */
  private String processDisplayMsgById(String externalMsgId)
  {
    LOG.debug("processDisplayMsgById()");
    String msgId=getExternalParameterByKey(externalMsgId);
    if(msgId!=null)
    {
      PrivateMessageDecoratedBean dbean=null;
      PrivateMessage msg = (PrivateMessage) prtMsgManager.getMessageById(new Long(msgId)) ;
      if(msg != null)
      {
        dbean.addPvtMessage(new PrivateMessageDecoratedBean(msg)) ;
        detailMsg = dbean;
      }
    }
    else
    {
      LOG.debug("processDisplayMsgById() - Error");
      return "pvtMsg";
    }
    return "pvtMsgDetail";
  }
  
  //////////////////////REPLY SEND  /////////////////
  public String processPvtMsgReplySend() {
    LOG.debug("processPvtMsgReplySend()");
    
    if(!hasValue(getReplyToSubject()))
    {
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage("Please enter subject for this reply message."));
      return null ;
    }
    if(!hasValue(getReplyToBody()) )
    {
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage("Please enter body for this reply message."));
      return null ;
    }
    if(getSelectedComposeToList().size()<1)
    {
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage("Please select recipiants list for this reply message."));
      return null ;
    }
    
    PrivateMessage rMsg=getDetailMsg().getMsg() ;
    PrivateMessage rrepMsg = messageManager.createPrivateMessage() ;
       
    rrepMsg.setTitle(getReplyToSubject()) ; //rrepMsg.setTitle(rMsg.getTitle()) ;
    rrepMsg.setDraft(Boolean.FALSE);
    rrepMsg.setAuthor(getUserId());
    rrepMsg.setApproved(Boolean.FALSE);
    rrepMsg.setBody(getReplyToBody()) ;
    
    rrepMsg.setInReplyTo(rMsg) ;
    this.getRecipients().add(rMsg.getCreatedBy());
    
    if((SET_AS_YES).equals(getComposeSendAsPvtMsg()))
    {
      prtMsgManager.sendPrivateMessage(rrepMsg, getRecipients());
    }
    
    //reset contents
    resetComposeContents();
    
    return "pvtMsg" ;
  }
 
  /**
   * process from Compose screen
   * @return - pvtMsg
   */
  public String processPvtMsgReplySaveDraft() {
    LOG.debug("processPvtMsgReplySaveDraft()");
    
    if(!hasValue(getReplyToSubject()))
    {
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage("Please enter subject for this reply message."));
      return null ;
    }
    if(!hasValue(getReplyToBody()) )
    {
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage("Please enter body for this reply message."));
      return null ;
    }
    if(getSelectedComposeToList().size()<1)
    {
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage("Please select recipiants list for this reply message."));
      return null ;
    }
    
    PrivateMessage drMsg=getDetailMsg().getMsg() ;
    //drMsg.setDraft(Boolean.TRUE);
    PrivateMessage drrepMsg = messageManager.createPrivateMessage() ;
    drrepMsg.setTitle(getReplyToSubject()) ;
    drrepMsg.setDraft(Boolean.TRUE);
    drrepMsg.setAuthor(getUserId());
    drrepMsg.setApproved(Boolean.FALSE);
    drrepMsg.setBody(getReplyToBody()) ;
    
    drrepMsg.setInReplyTo(drMsg) ;
    this.getRecipients().add(drMsg.getCreatedBy());
    
    if((SET_AS_YES).equals(getComposeSendAsPvtMsg()))
    {
      prtMsgManager.sendPrivateMessage(drrepMsg, getRecipients());  
    }
    
    //reset contents
    resetComposeContents();
    
    return "pvtMsg" ;    
  }
  
  ////////////////////////////////////////////////////////////////  
  public String processPvtMsgEmptyDelete() {
    LOG.debug("processPvtMsgEmptyDelete()");
    
    List delSelLs=new ArrayList() ;
    //this.setDisplayPvtMsgs(getDisplayPvtMsgs());    
    for (Iterator iter = this.decoratedPvtMsgs.iterator(); iter.hasNext();)
    {
      PrivateMessageDecoratedBean element = (PrivateMessageDecoratedBean) iter.next();
      if(element.getIsSelected())
      {
        delSelLs.add(element);
      }      
    }
    this.setSelectedDeleteItems(delSelLs);
    if(delSelLs.size()<1)
    {
      return null;  //stay in the same page if nothing is selected for delete
    }else {
      return "pvtMsgDelete";
    }
  }
  
  //delete private message 
  public String processPvtMsgMultiDelete()
  { 
    LOG.debug("processPvtMsgMultiDelete()");
  
    for (Iterator iter = getSelectedDeleteItems().iterator(); iter.hasNext();)
    {
      PrivateMessage element = ((PrivateMessageDecoratedBean) iter.next()).getMsg();
      if (element != null) 
      {
        prtMsgManager.deletePrivateMessage(element, getPrivateMessageTypeFromContext(msgNavMode)) ;        
      }      
    }
    return "main" ;
  }

  
  public String processPvtMsgDispOtions() 
  {
    LOG.debug("processPvtMsgDispOptions()");
    
    return "pvtMsgOrganize" ;
  }
  
  

  //select all
  private boolean isSelectAllJobsSelected = false;  
  public boolean isSelectAllJobsSelected()
  {
    return isSelectAllJobsSelected;
  }
  public void setSelectAllJobsSelected(boolean isSelectAllJobsSelected)
  {
    this.isSelectAllJobsSelected = isSelectAllJobsSelected;
  }
  
  public String processSelectAllJobs()
  {
    List newLs=new ArrayList() ;
//    isSelectAllJobsSelected = !isSelectAllJobsSelected;
//    processRefreshJobs();
    for (Iterator iter = this.getDecoratedPvtMsgs().iterator(); iter.hasNext();)
    {
      PrivateMessageDecoratedBean element = (PrivateMessageDecoratedBean) iter.next();
      element.setIsSelected(true);
      newLs.add(element) ;
      //TODO
    }
    this.setDecoratedPvtMsgs(newLs) ;
    return "pvtMsg";
  }
  
  //////////////////////////////   ATTACHMENT PROCESSING        //////////////////////////
  private ArrayList attachments = new ArrayList();
  
  private String removeAttachId = null;
  private ArrayList prepareRemoveAttach = new ArrayList();
  private boolean attachCaneled = false;
  private ArrayList oldAttachments = new ArrayList();
  private List allAttachments = new ArrayList();

  
  public ArrayList getAttachments()
  {
    ToolSession session = SessionManager.getCurrentToolSession();
    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
        session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) 
    {
      List refs = (List)session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      Reference ref = (Reference)refs.get(0);
      
      for(int i=0; i<refs.size(); i++)
      {
        ref = (Reference) refs.get(i);
        Attachment thisAttach = prtMsgManager.createPvtMsgAttachment(
            ref.getId(), ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName()));
        
        //TODO - remove this as being set for test only  
        //thisAttach.setPvtMsgAttachId(new Long(1));
        
        attachments.add(thisAttach);
        
      }
    }
    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
    
    return attachments;
  }
  
  public List getAllAttachments()
  {
    ToolSession session = SessionManager.getCurrentToolSession();
    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
        session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) 
    {
      List refs = (List)session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      Reference ref = (Reference)refs.get(0);
      
      for(int i=0; i<refs.size(); i++)
      {
        ref = (Reference) refs.get(i);
        Attachment thisAttach = prtMsgManager.createPvtMsgAttachment(
            ref.getId(), ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName()));
        
        //TODO - remove this as being set for test only
        //thisAttach.setPvtMsgAttachId(new Long(1));
        allAttachments.add(thisAttach);
      }
    }
    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
    
    if( allAttachments == null || (allAttachments.size()<1))
    {
      allAttachments.addAll(this.getDetailMsg().getMsg().getAttachments()) ;
    }
    return allAttachments;
  }
  
  public void setAttachments(ArrayList attachments)
  {
    this.attachments = attachments;
  }
  
  public String getRemoveAttachId()
  {
    return removeAttachId;
  }

  public final void setRemoveAttachId(String removeAttachId)
  {
    this.removeAttachId = removeAttachId;
  }
  
  public ArrayList getPrepareRemoveAttach()
  {
    if((removeAttachId != null) && (!removeAttachId.equals("")))
    {
      prepareRemoveAttach.add(prtMsgManager.getPvtMsgAttachment(new Long(removeAttachId)));
    }
    
    return prepareRemoveAttach;
  }

  public final void setPrepareRemoveAttach(ArrayList prepareRemoveAttach)
  {
    this.prepareRemoveAttach = prepareRemoveAttach;
  }
  
  //Redirect to File picker
  public String processAddAttachmentRedirect()
  {
    LOG.debug("processAddAttachmentRedirect()");
    try
    {
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      context.redirect("sakai.filepicker.helper/tool");
      return null;
    }
    catch(Exception e)
    {
      LOG.debug("processAddAttachmentRedirect() - Exception");
      return null;
    }
  }
  //Process remove attachment 
  public String processDeleteAttach()
  {
    LOG.debug("processDeleteAttach()");
    
    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
    String attachId = null;
    
    Map paramMap = context.getRequestParameterMap();
    Iterator itr = paramMap.keySet().iterator();
    while(itr.hasNext())
    {
      Object key = itr.next();
      if( key instanceof String)
      {
        String name =  (String)key;
        int pos = name.lastIndexOf("pvtmsg_current_attach");
        
        if(pos>=0 && name.length()==pos+"pvtmsg_current_attach".length())
        {
          attachId = (String)paramMap.get(key);
          break;
        }
      }
    }
    
    removeAttachId = attachId;
    
    //separate screen
//    if((removeAttachId != null) && (!removeAttachId.equals("")))
//      return "removeAttachConfirm";
//    else
//      return null;
    List newLs= new ArrayList();
    for (Iterator iter = getAttachments().iterator(); iter.hasNext();)
    {
      Attachment element = (Attachment) iter.next();
      if(!((element.getPvtMsgAttachId().toString()).equals(attachId)))
      {
        newLs.add(element);
      }
    }
    this.setAttachments((ArrayList) newLs) ;
    
    return null ;
  }
  
  //process deleting confirm from separate screen
  public String processRemoveAttach()
  {
    LOG.debug("processRemoveAttach()");
    
    try
    {
      Attachment sa = prtMsgManager.getPvtMsgAttachment(new Long(removeAttachId));
      String id = sa.getAttachmentId();
      
      for(int i=0; i<attachments.size(); i++)
      {
        Attachment thisAttach = (Attachment)attachments.get(i);
        if(((Long)thisAttach.getPvtMsgAttachId()).toString().equals(removeAttachId))
        {
          attachments.remove(i);
          break;
        }
      }
      
      ContentResource cr = ContentHostingService.getResource(id);
      prtMsgManager.removePvtMsgAttachment(sa);
      if(id.toLowerCase().startsWith("/attachment"))
        ContentHostingService.removeResource(id);
    }
    catch(Exception e)
    {
      LOG.debug("processRemoveAttach() - Exception");
    }
    
    removeAttachId = null;
    prepareRemoveAttach.clear();
    return "compose";
    
  }
  
  public String processRemoveAttachCancel()
  {
    LOG.debug("processRemoveAttachCancel()");
    
    removeAttachId = null;
    prepareRemoveAttach.clear();
    return "compose" ;
  }
  

  ////////////  SETTINGS        //////////////////////////////
  //Setting Getter and Setter
  public String getActivatePvtMsg()
  {
    return activatePvtMsg;
  }
  public void setActivatePvtMsg(String activatePvtMsg)
  {
    this.activatePvtMsg = activatePvtMsg;
  }
  public String getForwardPvtMsg()
  {
    return forwardPvtMsg;
  }
  public void setForwardPvtMsg(String forwardPvtMsg)
  {
    this.forwardPvtMsg = forwardPvtMsg;
  }
  public String getForwardPvtMsgEmail()
  {
    return forwardPvtMsgEmail;
  }
  public void setForwardPvtMsgEmail(String forwardPvtMsgEmail)
  {
    this.forwardPvtMsgEmail = forwardPvtMsgEmail;
  }
  public boolean getSuperUser()
  {
    superUser=SecurityService.isSuperUser();
    return superUser;
  }
  //is instructor
  public boolean isInstructor()
  {
    return prtMsgManager.isInstructor();
  }
  
  public void setSuperUser(boolean superUser)
  {
    this.superUser = superUser;
  }
  
  public String processPvtMsgOrganize()
  {
    LOG.debug("processPvtMsgOrganize()");
    return null ;
    //return "pvtMsgOrganize";
  }

  public String processPvtMsgStatistics()
  {
    LOG.debug("processPvtMsgStatistics()");
    
    return null ;
    //return "pvtMsgStatistics";
  }
  
  public String processPvtMsgSettings()
  {
    LOG.debug("processPvtMsgSettings()");    
    return "pvtMsgSettings";
  }
    
  public void processPvtMsgSettingsRevise(ValueChangeEvent event)
  {
    LOG.debug("processPvtMsgSettingsRevise()");   
    
    /** block executes when changing value to "no" */
    if ("yes".equals(forwardPvtMsg)){
      setForwardPvtMsgEmail(null);      
    }       
    if ("no".equals(forwardPvtMsg)){
      setValidEmail(true);
    }
  }
  
  public String processPvtMsgSettingsSave()
  {
    LOG.debug("processPvtMsgSettingsSave()");
    
    String email= getForwardPvtMsgEmail();
    String activate=getActivatePvtMsg() ;
    String forward=getForwardPvtMsg() ;
    if (email != null && !"no".equals(forward) && !(email.trim().indexOf("@") > -1)){
      setValidEmail(false);
      setActivatePvtMsg("yes");
      return "pvtMsgSettings";
    }
    else
    {
      Area area = prtMsgManager.getPrivateMessageArea();            
      
      Boolean formAreaEnabledValue = ("yes".equals(activate)) ? Boolean.TRUE : Boolean.FALSE;
      area.setEnabled(formAreaEnabledValue);
      
      Boolean formAutoForward = ("yes".equals(forward)) ? Boolean.TRUE : Boolean.FALSE;            
      forum.setAutoForward(formAutoForward);
      if (Boolean.TRUE.equals(formAutoForward)){
        forum.setAutoForwardEmail(email);  
      }
      else{
        forum.setAutoForwardEmail(null);  
      }
             
      prtMsgManager.saveAreaAndForumSettings(area, forum);
      return "main";
    }
    
  }
  

  ///////////////////   FOLDER SETTINGS         ///////////////////////
  //TODO - may add total number of messages with this folder.. 
  //--getDecoratedForum() iteratae and when title eqauls selectedTopicTitle - then get total number of messages
  private String addFolder;
  private boolean ismutable;
  
  public String getAddFolder()
  {
    return addFolder ;    
  }
  public void setAddFolder(String addFolder)
  {
    this.addFolder=addFolder;
  }
  
  public boolean getIsmutable()
  {
    return prtMsgManager.isMutableTopicFolder(getSelectedTopicId());
  }
  
  //navigated from header pagecome from Header page 
  public String processPvtMsgFolderSettings() {
    LOG.debug("processPvtMsgFolderSettings()");
    String topicTitle= getExternalParameterByKey("pvtMsgTopicTitle");
    setSelectedTopicTitle(topicTitle) ;
    String topicId=getExternalParameterByKey("pvtMsgTopicId") ;
    setSelectedTopicId(topicId);
    
    return "pvtMsgFolderSettings" ;
  }

  public String processPvtMsgFolderSettingRevise() {
    LOG.debug("processPvtMsgFolderSettingRevise()");
    
    if(this.ismutable)
    {
      return null;
    }else 
    {
      return "pvtMsgFolderRevise" ;
    }    
  }
  
  public String processPvtMsgFolderSettingAdd() {
    LOG.debug("processPvtMsgFolderSettingAdd()");    
    return "pvtMsgFolderAdd" ;
  }
  public String processPvtMsgFolderSettingDelete() {
    LOG.debug("processPvtMsgFolderSettingDelete()");
    
    if(this.ismutable)
    {
      return null;
    }else {
      return "pvtMsgFolderDelete" ;
    }    
  }
  
  public String processPvtMsgFolderSettingCancel() 
  {
    LOG.debug("processPvtMsgFolderSettingCancel()");
    
    return "main" ;
  }
  
  //Create a folder within a forum
  public String processPvtMsgFldCreate() 
  {
    LOG.debug("processPvtMsgFldCreate()");
    
    String createFolder=getAddFolder() ;
    if(createFolder == null)
    {
      return null ;
    } else {
      prtMsgManager.createTopicFolderInForum(this.getDecoratedForum().getForum().getId().toString(), this.getUserId(), createFolder);
      return "main" ;
    }
  }
  
  //revise
  public String processPvtMsgFldRevise() 
  {
    LOG.debug("processPvtMsgFldRevise()");
    
    String newTopicTitle = this.getSelectedTopicTitle();    
    prtMsgManager.renameTopicFolder(getSelectedTopicId(), getUserId(), newTopicTitle);
    
    return "main" ;
  }
  
  //Delete
  public String processPvtMsgFldDelete() 
  {
    LOG.debug("processPvtMsgFldDelete()");
    
    prtMsgManager.deleteTopicFolder(getSelectedTopicId()) ;
    
    return "main";
  }
  public String processPvtMsgFldAddCancel() 
  {
    LOG.debug("processPvtMsgFldAddCancel()");
    
    return "main";
  }
  
  ///////////////   SEARCH      ///////////////////////
  private List searchPvtMsgs;
  public List getSearchPvtMsgs()
  {
    return searchPvtMsgs;
  }
  public void setSearchPvtMsgs(List searchPvtMsgs)
  {
    this.searchPvtMsgs=searchPvtMsgs ;
  }
  public String processSearch() 
  {
    LOG.debug("processSearch()");
    
    List newls = new ArrayList() ;
    for (Iterator iter = getDecoratedPvtMsgs().iterator(); iter.hasNext();)
    {
      PrivateMessageDecoratedBean element = (PrivateMessageDecoratedBean) iter.next();
      
      String message=element.getMsg().getTitle();
      StringTokenizer st = new StringTokenizer(message);
      while (st.hasMoreTokens())
      {
        if(st.nextToken().equalsIgnoreCase(getSearchText()))
        {
          newls.add(element) ;
          break;
        }
      }
    }
      this.setSearchPvtMsgs(newls) ;

    return "pvtMsgEx" ;
  }
  
 
  //////////////        HELPER      //////////////////////////////////
  /**
   * @return
   */
  private String getExternalParameterByKey(String parameterId)
  {
    String parameterValue = null;
    ExternalContext context = FacesContext.getCurrentInstance()
        .getExternalContext();
    Map paramMap = context.getRequestParameterMap();
    Iterator itr = paramMap.keySet().iterator();
    while (itr.hasNext())
    {
      String key = (String) itr.next();
      if (key != null && key.equals(parameterId))
      {
        parameterValue = (String) paramMap.get(key);
        break;
      }
    }
    return parameterValue;
  }

  /**
   * get recipients
   * @return a set of recipients (User objects)
   */
  private Set getRecipients()
  {    
    Set allCourseMembers = getAllCourseMembers();
    List selectedList = getSelectedComposeToList();
    
    Set returnSet = new HashSet();    
    
    for (Iterator i = selectedList.iterator(); i.hasNext();){
      String selectedItem = (String) i.next();
      if (selectedItem.equals(RECIPIANTS_ENTIRE_CLASS)){
        returnSet.addAll(getUserRecipients(allCourseMembers));
        break;
      }
      else{
        /** search totalComposeToList for matching name */
        RecipientItem matchingItem = null;
        
        for (Iterator j = totalComposeToListRecipients.iterator(); j.hasNext();){
          RecipientItem item = (RecipientItem) j.next();          
          
          if (item.getName().equalsIgnoreCase(selectedItem)){
            matchingItem = item;
            break;
          }
        }
        
        if (matchingItem != null){
          if (matchingItem.getUser() == null){ /** role **/
            returnSet.addAll(getUserRecipientsForRole(matchingItem.getRole().getId(), allCourseMembers));
          }
          else{ /** user **/
            returnSet.addAll(getUserRecipients(allCourseMembers)); 
          }
        }                
      }
    }            
    
    return returnSet;
  }
  
  /**
   * getUserRecipients
   * @param courseMembers
   * @return set of all User objects for course
   */
  private Set getUserRecipients(Set courseMembers){    
    Set returnSet = new HashSet();
    
    for (Iterator i = courseMembers.iterator(); i.hasNext();){
      RecipientItem item = (RecipientItem) i.next();      
        returnSet.add(item.getUser());
    }    
    return returnSet;    
  }
  
  /**
   * getUserRecipientsForRole
   * @param roleName
   * @param courseMembers
   * @return set of all User objects for role
   */
  private Set getUserRecipientsForRole(String roleName, Set courseMembers){    
    Set returnSet = new HashSet();
    
    for (Iterator i = courseMembers.iterator(); i.hasNext();){
      RecipientItem item = (RecipientItem) i.next();
      if (item.getRole().equals(roleName)){
        returnSet.add(item.getUser());   
      }
    }    
    return returnSet;    
  }
    
  /**
   * get all members for course
   * @return list of members
   */
  private Set getAllCourseMembers()
  {   
    List FERPAEnabledMembers = sakaiPersonManager.findAllFerpaEnabled();
    Set recipients = new HashSet();    
    String realmId = SiteService.siteReference(PortalService.getCurrentSiteId());
 
    try
    {
      AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
      Set grants = realm.getMembers();      
      for (Iterator i = grants.iterator(); i.hasNext();)
      {
        Member m = (Member) i.next();
        String userId = m.getUserId();
        Role role = m.getRole();
                
        User user = UserDirectoryService.getUser(userId);
        RecipientItem recipient = new RecipientItem();
        recipient.setName(user.getSortName());
        recipient.setUniqueName(userId);
        recipient.setUser(user);
        recipient.setRole(role);
        
        if(!(userId).equals("admin"))
        {
          if(FERPAEnabledMembers==null || !FERPAEnabledMembers.contains(userId))
          {
            recipients.add(recipient);
          }              
        }                                
      }
    }
    catch (IdUnusedException e)
    {
      LOG.debug(e.getMessage(), e);
    }
        
    return recipients;
  }  
  
  private String getPrivateMessageTypeFromContext(String navMode){
    
    if (PVTMSG_MODE_RECEIVED.equalsIgnoreCase(navMode)){
      return typeManager.getReceivedPrivateMessageType();
    }
    else if (PVTMSG_MODE_SENT.equalsIgnoreCase(navMode)){
      return typeManager.getSentPrivateMessageType();
    }
    else if (PVTMSG_MODE_DELETE.equalsIgnoreCase(navMode)){
      return typeManager.getDeletedPrivateMessageType(); 
    }
    else if (PVTMSG_MODE_DRAFT.equalsIgnoreCase(navMode)){
      return typeManager.getDraftPrivateMessageType();
    }
    else{
      return "";
    }    
  }

  
  /*
   * return all instructor participants
   */
//  private List getAllInstructorParticipants()
//  {
//    List members = new Vector();
//    
//    List participants = new Vector();    
//    String realmId = SiteService.siteReference(PortalService.getCurrentSiteId());//SiteService.siteReference((String) state.getAttribute(STATE_SITE_INSTANCE_ID));
// 
//    try
//    {
//      AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
//      Set grants = realm.getMembers();
//      //Collections.sort(users);
//      for (Iterator i = grants.iterator(); i.hasNext();)
//      {
//        Member g = (Member) i.next();
//        String userString = g.getUserId();
//        Role r = g.getRole();
//        
//        boolean alreadyInList = false;
//        for (Iterator p = members.iterator(); p.hasNext() && !alreadyInList;)
//        {
//          CourseMember member = (CourseMember) p.next();
//          String memberUniqname = member.getUniqname();
//          if (userString.equalsIgnoreCase(memberUniqname))
//          {
//              alreadyInList = true;
//              if (r != null)
//              {
//                  member.setRole(r.getId());
//              }
//              participants.add(member);
//          }
//        }
//        if (!alreadyInList)
//        {
//          try
//          {
//            User user = UserDirectoryService.getUser(userString);
//            Participant participant = new Participant();
//            participant.name = user.getSortName();
//            participant.setUniqeName(userString);
//            if (r != null)
//            {
//                participant.role = r;
//            }
//            //Don't add admin/admin 
//            if(!(participant.getUniqueName()).equals("admin") && prtMsgManager.isInstructor())
//            {
//              participants.add(participant);
//            }                
//          }
//          catch (IdUnusedException e)
//          {
//            // deal with missing user quietly without throwing a warning message
//          }
//        }
//      }
//    }
//    catch (IdUnusedException e)
//    {
//      //Log.warn("chef", this + "  IdUnusedException " + realmId);
//    } 
//    return participants ;
//  }
  

  //////// GETTER AND SETTER  ///////////////////  
  public String processUpload(ValueChangeEvent event)
  {
    return "pvtMsg" ; 
  }
  
  public String processUploadConfirm()
  {
    return "pvtMsg";
  }
  
  public String processUploadCancel()
  {
    return "pvtMsg" ;
  }


  public void setForumManager(MessageForumsForumManager forumManager)
  {
    this.forumManager = forumManager;
  }


  public PrivateForum getForum()
  {            
    return forum;
  }


  public void setForum(PrivateForum forum)
  {
    this.forum = forum;
  }
 
// public String processActionAreaSettingEmailFwd()
// {
//   
// }
  
  /**
   * Check String has value, not null
   * @return boolean
   */
  protected boolean hasValue(String eval)
  {
    if (eval != null && !eval.trim().equals(""))
    {
      return true;
    }
    else
    {
      return false;
    }
  }
}