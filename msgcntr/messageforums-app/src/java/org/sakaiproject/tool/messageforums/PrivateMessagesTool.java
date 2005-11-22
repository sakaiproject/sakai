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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
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
import org.sakaiproject.service.legacy.coursemanagement.CourseMember;
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
  private ErrorMessages errorMessages;
  
  /**
   * Dependency Injected 
   */
  private MessageForumsTypeManager typeManager;
  
  /** portlet configuration parameter values**/
  public static final String PVTMSG_MODE_RECEIVED = "Received";
  public static final String PVTMSG_MODE_SENT = "Sent";
  public static final String PVTMSG_MODE_DELETE = "Deleted";
  public static final String PVTMSG_MODE_DRAFT = "Drafts";
  public static final String PVTMSG_MODE_CASE = "Personal Folders";
  
  
  private String userId;
  private Date time ;
  
  private PrivateForum forum; 
  private List pvtTopics=new ArrayList();
  private List displayPvtMsgs;
  private String msgNavMode="" ;
  private Message detailMsg ;
  private String currentMsgUuid; //this is the message which is being currently edited/displayed/deleted
  private boolean navModeIsDelete=false ; // Delete mode to show up extra buttons in pvtMsg.jsp page
  private List selectedItems;
  
  PrivateForumDecoratedBean decoratedForum;
  //delete confirmation screen - single delete 
  private boolean deleteConfirm=false ; //used for displaying delete confirmation message in same jsp
  
  //Compose Screen
  private List selectedComposeToList;
  private String composeSendAs="pvtmsg"; // currently set as Default as change by user is allowed
  private String composeSubject ;
  private String composeBody ;
  private String composeLabel ;   
  private List totalComposeToList=new ArrayList();
  
  //Delete items - Checkbox display and selection - Multiple delete
  private List selectedDeleteItems;
  private List totalDisplayItems=new ArrayList() ;
  
  //reply to 
  private String replyToBody ;
  
  //Setting Screen
  private String activatePvtMsg="yes";
  private String forwardPvtMsg="no";
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
   * @param typeManager
   */
  public void setTypeManager(MessageForumsTypeManager typeManager)
  {
    this.typeManager = typeManager;
  }

  public Area getArea()
  {
    Area privateArea=prtMsgManager.getPrivateArea();
    if(privateArea != null ) {
     List forums=privateArea.getPrivateForums();
      //Private message return ONLY ONE ELEMENT
      for (Iterator iter = forums.iterator(); iter.hasNext();)
      {
        forum = (PrivateForum) iter.next();
        pvtTopics=forum.getTopics();   
      }
    }
   return privateArea;
  }
  
//  public PrivateForum getForum()
//  {
//   return forum;
//  }
  
  //Return decorated Forum
  public PrivateForumDecoratedBean getDecoratedForum()
  {
    PrivateForumDecoratedBean decoratedForum = new PrivateForumDecoratedBean(forum) ;
    for (Iterator iterator = pvtTopics.iterator(); iterator.hasNext();)
    {
      Topic topic = (Topic) iterator.next();
      if (topic != null)
      {
        PrivateTopicDecoratedBean decoTopic= new PrivateTopicDecoratedBean(topic) ;
        decoTopic.setTotalNoMessages(prtMsgManager.getTotalNoMessages(topic)) ;
        decoTopic.setUnreadNoMessages(prtMsgManager.getUnreadNoMessages(topic)) ;
        decoratedForum.addTopic(decoTopic);
      }          
    }
    return decoratedForum ;
  }
  
  //TODO - area and forum is used only once as to display the title of private message
  // May be 'area' and 'forum' to be deleted and and move all the code under getPvtTopics() 
//  public List getPvtTopics()
//  {
//    return pvtTopics;
//  }

  public List getDisplayPvtMsgs()
  {
    displayPvtMsgs=new ArrayList() ;
    
    Area privateArea=prtMsgManager.getPrivateArea();
    if(privateArea != null ) {
     List forums=privateArea.getPrivateForums();
      //Private message return ONLY ONE ELEMENT
      for (Iterator iter = forums.iterator(); iter.hasNext();)
      {
        forum = (PrivateForum) iter.next();
        pvtTopics=forum.getTopics();   
        //now get messages for each topics
        for (Iterator iterator = pvtTopics.iterator(); iterator.hasNext();)
        {
          //TODO -- should Topic be changed to PrivateTopic
          Topic topic = (Topic) iterator.next();
          if(topic.getTitle().equals(PVTMSG_MODE_RECEIVED))
          {
            //TODO -- getMessages() should be changed to getReceivedMessages() ;
            displayPvtMsgs=topic.getMessages() ;
            break;
          } 
          if(topic.getTitle().equals(PVTMSG_MODE_SENT))
          {
            displayPvtMsgs=topic.getMessages() ;
            
            break;
          }  
          if(topic.getTitle().equals(PVTMSG_MODE_DELETE))
          {
            displayPvtMsgs=topic.getMessages() ;
            break;
          }  
          if(topic.getTitle().equals(PVTMSG_MODE_DRAFT))
          {
            displayPvtMsgs=topic.getMessages() ;
            break;
          }  
          if(topic.getTitle().equals(PVTMSG_MODE_CASE))
          {
            displayPvtMsgs=topic.getMessages() ;
            break;
          }    
        }
        //create decorated List
        displayPvtMsgs = createDecoratedDisplay(displayPvtMsgs);
        
        // add selectItem for checkboxes
//        for (int i = 0; i < displayPvtMsgs.size(); i++)
//        {
//          totalDisplayItems.add(new SelectItem(((PrivateMessage)displayPvtMsgs.get(i)).getUuid(), 
//              ((PrivateMessage)displayPvtMsgs.get(i)).getUuid())) ;
//        }
        
      }
    }
    return displayPvtMsgs ;
  }
  
  public void setDisplayPvtMsgs(List displayPvtMsgs)
  {
    this.displayPvtMsgs=displayPvtMsgs;
  }
  
  public String getMsgNavMode() 
  {
    return msgNavMode ;
  }
 
  public Message getDetailMsg()
  {
    return detailMsg ;
  }

  public void setDetailMsg(Message detailMsg)
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

  public String getComposeSendAs()
  {
    return composeSendAs;
  }

  public void setComposeSendAs(String composeSendAs)
  {
    this.composeSendAs = composeSendAs;
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
    //TODO add all participants to this site
        List members = new Vector();
        List participants = new Vector();
        String realmId = SiteService.siteReference(PortalService.getCurrentSiteId());//SiteService.siteReference((String) state.getAttribute(STATE_SITE_INSTANCE_ID));
        
        
        try
        {
            AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
            Set grants = realm.getMembers();
            //Collections.sort(users);
            for (Iterator i = grants.iterator(); i.hasNext();)
            {
                Member g = (Member) i.next();
                String userString = g.getUserId();
                Role r = g.getRole();
                
                boolean alreadyInList = false;
                for (Iterator p = members.iterator(); p.hasNext() && !alreadyInList;)
                {
                    CourseMember member = (CourseMember) p.next();
                    String memberUniqname = member.getUniqname();
                    if (userString.equalsIgnoreCase(memberUniqname))
                    {
                        alreadyInList = true;
                        if (r != null)
                        {
                            member.setRole(r.getId());
                        }
                        participants.add(member);
                    }
                }
                if (!alreadyInList)
                {
                    try
                    {
                        User user = UserDirectoryService.getUser(userString);
                        Participant participant = new Participant();
                        participant.name = user.getSortName();
                        participant.uniqname = userString;
                        if (r != null)
                        {
                            participant.role = r.getId();
                        }
                        //Don't add admin/admin 
                        if(!(participant.uniqname).equals("admin"))
                        {
                          participants.add(participant);
                        }
                        
                    }
                    catch (IdUnusedException e)
                    {
                        // deal with missing user quietly without throwing a warning message
                    }
                }
            }
        }
        catch (IdUnusedException e)
        {
            //Log.warn("chef", this + "  IdUnusedException " + realmId);
        }
                
        List totalComposeToList=new ArrayList() ;
//        for (Iterator iter = participants.iterator(); iter.hasNext();)
//        {
//          Participant element = (Participant) iter.next();
//          totalComposeToList.add(new SelectItem(element.getName()));
//          
//        }
        for (int i = 0; i < participants.size(); i++)
        {
          totalComposeToList.add(new SelectItem(((Participant) participants.get(i)).getName(),((Participant) participants.get(i)).getName()));
        }
        //return participants;
    return totalComposeToList;
  }

    /**
    * Participant in site access roles
    *
    */
  public class Participant
  {
    public String name = "";
    public String uniqname = "";
    public String role = ""; 
        
    public String getName() {return name; }
    public String getUniqname() {return uniqname; }
    public String getRole() { return role; } // cast to Role
    public boolean isRemoveable(){return true;}
        
  } // Participant
  
  //decorated display - from List of Message
  public List createDecoratedDisplay(List msg)
  {
    List decLs= new ArrayList() ;
    for (Iterator iter = msg.iterator(); iter.hasNext();)
    {
      PrivateMessage element = (PrivateMessage) iter.next();
      decLs.add(new PrivateMessageDecoratedBean(element)) ;
    }
    return decLs;
  }
  public String getUserId() {
    
   String userId=SessionManager.getCurrentSessionUserId();
   try
  {
    User user=UserDirectoryService.getUser(userId) ;
    userId= user.getDisplayName();
  }
  catch (IdUnusedException e)
  {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
   
   return userId;
   
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


  //message header Getter 
  public String getSearchText()
  {
    return searchText ;
  }
  public void setSeachText(String searchText)
  {
    this.searchText=searchText;
  }
  public String getSelectView()
  {
    return selectView ;
  }
  //////////////////////////////////////////////////////////////////////////////////  
  /**
   * called when any topic like Received/Sent/Deleted clicked
   * @return - pvtMsg
   */
  private String selectedTopicTitle="";
  public String getSelectedTopicTitle()
  {
    return selectedTopicTitle ;
  }
  public void setSelectedTopicTitle(String selectedTopicTitle) 
  {
    this.selectedTopicTitle=selectedTopicTitle;
  }
  
  
  public String processPvtMsgTopic()
  {
    //get external parameter
    try
    {
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      Map paramMap = context.getRequestParameterMap();
      Iterator itr = paramMap.keySet().iterator();
      while (itr.hasNext())
      {
        String key = (String) itr.next();
        if (key != null && key.equals("pvtMsgTopicTitle"))
        {
          selectedTopicTitle = (String) paramMap.get(key);
          break;
        }
      }
    }
    catch (Exception e)
    {
      //TODO create error JSp
      return "main"; 
    }
    //then set up navigation
    if(selectedTopicTitle.equals(PVTMSG_MODE_RECEIVED) || (this.getMsgNavMode().equals(PVTMSG_MODE_RECEIVED)))
    {
      msgNavMode=PVTMSG_MODE_RECEIVED;
      this.setNavModeIsDelete(false); 
      return "pvtMsg";
    }
    if(selectedTopicTitle.equals(PVTMSG_MODE_SENT) ||(this.getMsgNavMode().equals(PVTMSG_MODE_SENT)))
    {
      msgNavMode=PVTMSG_MODE_SENT;
      this.setNavModeIsDelete(false); 
      return "pvtMsg";
    }
    if(selectedTopicTitle.equals(PVTMSG_MODE_DELETE)||(this.getMsgNavMode().equals(PVTMSG_MODE_DELETE)))
    {
      msgNavMode=PVTMSG_MODE_DELETE;
      this.setNavModeIsDelete(true); 
      return "pvtMsg";
    }
    if(selectedTopicTitle.equals(PVTMSG_MODE_DRAFT)||(this.getMsgNavMode().equals(PVTMSG_MODE_DRAFT)))
    {
      msgNavMode=PVTMSG_MODE_DRAFT;
      this.setNavModeIsDelete(false); 
      return "pvtMsg";
    }
    if(selectedTopicTitle.equals(PVTMSG_MODE_CASE)||(this.getMsgNavMode().equals(PVTMSG_MODE_CASE)))
    {
      msgNavMode=PVTMSG_MODE_CASE;
      this.setNavModeIsDelete(false); 
      return "pvtMsg";
    }
    else
    {
      return "main" ;
    }
  }
  
  /**
   * process Cancel from all JSP's
   * @return - pvtMsg
   */  
  public String processPvtMsgCancel() {
    //reset properties as this managed bean is in session    
//    if (this.getMsgNavMode().equals(PVTMSG_MODE_RECEIVED)||
//        this.getMsgNavMode().equals(PVTMSG_MODE_SENT)||
//        this.getMsgNavMode().equals(PVTMSG_MODE_DELETE)||
//        this.getMsgNavMode().equals(PVTMSG_MODE_DRAFT)||
//        this.getMsgNavMode().equals(PVTMSG_MODE_CASE))
//    {
//      resetFormVariable() ;
//      return "pvtMsg" ;
//    }else {
//      resetFormVariable() ;
//      return "main" ;
//    }
    return processPvtMsgTopic();
    
  }
  
  /**
   * called when subject of List of messages to Topic clicked for detail
   * @return - pvtMsgDetail
   */ 
  public String processPvtMsgDetail() {
    String msgId="";
    try
    {
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      Map paramMap = context.getRequestParameterMap();
      Iterator itr = paramMap.keySet().iterator();
      while (itr.hasNext())
      {
        String key = (String) itr.next();
        if (key != null && key.equals("current_msg_detail"))
        {
          msgId = (String) paramMap.get(key);
          //set the current messageUuid for other methods
          this.setCurrentMsgUuid(msgId) ; 
          break;
        }
      }
      //retrive the detail for this message with currentMessageId
      for (Iterator iter = this.getDisplayPvtMsgs().iterator(); iter.hasNext();)
      {
        //Nov- 15th changed from Message to PrivateMessage
        PrivateMessage aMsg = (PrivateMessage) iter.next();
        if(((String)aMsg.getUuid()).equals(msgId)) {
          this.setDetailMsg(aMsg) ;
        }
      }
      this.deleteConfirm=false; //reset this as used for multiple action in same JSP
      //TODO - retrives the details for this message with above Id.
      return "pvtMsgDetail";
    }
    catch (Exception e)
    {
      return "pvtMsgDetail";
    }
  }

  /**
   * called from Single delete Page
   * @return - pvtMsgReply
   */ 
  public String processPvtMsgReply() {
    
    //from message detail screen
    this.setDetailMsg(getDetailMsg()) ;
    
    //from compose screen
    this.setComposeSendAs(getComposeSendAs()) ;
    this.setTotalComposeToList(getTotalComposeToList()) ;
    this.setSelectedComposeToList(getSelectedComposeToList()) ;
    
    return "pvtMsgReply";
  }
  
  /**
   * called from Single delete Page
   * @return - pvtMsgMove
   */ 
  public String processPvtMsgMove() {
    
    return "pvtMsgMove";
  }
  
  /**
   * called from Single delete Page
   * @return - pvtMsgDetail
   */ 
  public String processPvtMsgDeleteConfirm() {
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
    if(getDetailMsg() != null)
    {
      //TODO
      //prtMsgManager.deletePrivateMessage(getDetailMsg()) ;
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
    return "pvtMsgCompose" ;
  }
  
  /**
   * process from Compose screen
   * @return - pvtMsg
   */ 
  public String processPvtMsgSend() {
    
    //TODO - create new PrivateMessage Object and add user input and then save
    //prtMsgManager.savePrivateMessage(constructMessage());
    return "pvtMsg" ;
  }
 
  /**
   * process from Compose screen
   * @return - pvtMsg
   */
  public String processPvtMsgSaveDraft() {
    PrivateMessage dMsg=constructMessage() ;
    dMsg.setDraft(Boolean.TRUE);
    //TODO
    prtMsgManager.savePrivateMessage(dMsg);
    return "pvtMsg" ;    
  }
  // created separate method as to be used with processPvtMsgSend() and processPvtMsgSaveDraft()
  public PrivateMessage constructMessage()
  {
    PrivateMessage aMsg = (PrivateMessage)this.getDetailMsg();
    aMsg.setRecipients(getSelectedComposeToList());
    aMsg.setTitle(getComposeSubject());
    aMsg.setBody(getComposeBody());
    aMsg.setAttachments(getAttachments()) ;
    aMsg.setCreatedBy(getUserId());
    aMsg.setCreated(getTime()) ;
    aMsg.setDraft(Boolean.FALSE);
    
    //Add attachments
    for(int i=0; i<attachments.size(); i++)
    {
      prtMsgManager.addPvtMsgAttachToPvtMsgData(aMsg, (Attachment)attachments.get(i));         
    }
    
    //clear
    attachments.clear();
    oldAttachments.clear();
    
    return aMsg;    
  }
  
  //////////////////////REPLY SEND  /////////////////
  public String processPvtMsgReplySend() {
    PrivateMessage rsMsg=constructMessage() ;
    //TODO - add stuff include reply related things
    //prtMsgManager.savePrivateMessage(rsMsg);
    return "pvtMsg" ;
  }
 
  /**
   * process from Compose screen
   * @return - pvtMsg
   */
  public String processPvtMsgReplySaveDraft() {
    PrivateMessage drMsg=constructMessage() ;
    drMsg.setDraft(Boolean.TRUE);
    //TODO- add stuff include reply related things
    //prtMsgManager.savePrivateMessage(drMsg);
    return "pvtMsg" ;    
  }
  
  ////////////////////////////////////////////////////////////////
  
  public String processPvtMsgEmptyDelete() {
    List delSelLs=new ArrayList() ;
    //this.setDisplayPvtMsgs(getDisplayPvtMsgs());
    
    for (Iterator iter = this.displayPvtMsgs.iterator(); iter.hasNext();)
    {
      PrivateMessageDecoratedBean element = (PrivateMessageDecoratedBean) iter.next();
      if(element.getIsSelected())
      {
        delSelLs.add(element);
      }
      
    }
    this.setSelectedDeleteItems(delSelLs);
    return "pvtMsgDelete";
  }
  
  public String processPvtMsgMultiDelete()
  {
    for (Iterator iter = getSelectedDeleteItems().iterator(); iter.hasNext();)
    {
      //We don't need decorated at this point as we will be deleting PrivateMessage object
      PrivateMessage element = ((PrivateMessageDecoratedBean) iter.next()).getMessage();
      if (element != null) 
      {
        //TODO 
        //prtMsgManager.deletePrivateMessage(element) ;
      }      
    }
    return "main" ;
  }

  
  public String processPvtMsgDispOtions() {
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
    for (Iterator iter = this.getDisplayPvtMsgs().iterator(); iter.hasNext();)
    {
      PrivateMessageDecoratedBean element = (PrivateMessageDecoratedBean) iter.next();
      element.setIsSelected(true);
      newLs.add(element) ;
      //TODO
    }
    this.setDisplayPvtMsgs(newLs) ;
    return "pvtMsg";
  }
  
  //////////////////////////////   ATTACHMENT PROCESSING        //////////////////////////
  private ArrayList attachments = new ArrayList();
  
  private String removeAttachId = null;
  private ArrayList prepareRemoveAttach = new ArrayList();
  private boolean attachCaneled = false;
  private ArrayList oldAttachments = new ArrayList();
  private ArrayList allAttachments = new ArrayList();

  
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
        Attachment thisAttach = prtMsgManager.createPvtMsgAttachmentObject(
            ref.getId(), ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName()));
        
        //Test 
        thisAttach.setPvtMsgAttachId(new Long(1));
        //Test
        attachments.add(thisAttach);
        
//        if(entry.justCreated != true)
//        {
          allAttachments.add(thisAttach);
//        }
      }
    }
    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
    
    return attachments;
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
      prepareRemoveAttach.add(prtMsgManager.getPvtMsgAttachment(removeAttachId));
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
    try
    {
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      context.redirect("sakai.filepicker.helper/tool");
      return null;
    }
    catch(Exception e)
    {
      //logger.error(this + ".processAddAttachRedirect - " + e);
      //e.printStackTrace();
      return null;
    }
  }
  //Process remove attachment 
  public String processDeleteAttach()
  {
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
    try
    {
      Attachment sa = prtMsgManager.getPvtMsgAttachment(removeAttachId);
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
      prtMsgManager.removePvtMsgAttachmentObject(sa);
      if(id.toLowerCase().startsWith("/attachment"))
        ContentHostingService.removeResource(id);
    }
    catch(Exception e)
    {
//      logger.error(this + ".processRemoveAttach() - " + e);
//      e.printStackTrace();
    }
    
    removeAttachId = null;
    prepareRemoveAttach.clear();
    return "compose";
    
  }
  
  public String processRemoveAttachCancel()
  {
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
  public void setSuperUser(boolean superUser)
  {
    this.superUser = superUser;
  }
  
  public String processPvtMsgOrganize()
  {

    return "pvtMsgOrganize";
  }

  public String processPvtMsgStatistics()
  {

    return "pvtMsgStatistics";
  }

  public String processPvtMsgSettings()
  {
    return "pvtMsgSettings";
  }

  public String processPvtMsgSettingRevise() {
    String email= getForwardPvtMsgEmail();
    String act=getActivatePvtMsg() ;
    String frd=getForwardPvtMsg() ;
    //prtMsgManager.saveAreaSetting();
    return "main" ;
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
    return prtMsgManager.isMutableTopicFolder();
  }
  //come from Header page 
  public String processPvtMsgFolderSettings() {
    return "pvtMsgFolderSettings" ;
  }
  
  public String processPvtMsgFolderSettingRevise() {
    return "pvtMsgFolderRevise" ;
  }
  
  public String processPvtMsgFolderSettingAdd() {
    return "pvtMsgFolderAdd" ;
  }
  public String processPvtMsgFolderSettingDelete() {
    return "pvtMsgFolderDelete" ;
  }
  
  public String processPvtMsgFolderSettingCancel() {
    return "pvtMsg" ;
  }
  
  //Create
  public String processPvtMsgFldCreate() {
    String createFolder=getAddFolder() ;
    if(createFolder == null)
    {
      return null ;
    } else {
      //prtMsgManager.createTopicFolder(createFolder);
      return "pvtMsgFolderSettings" ;
    }
  }
  
  //revise
  public String processPvtMsgFldRevise() 
  {
    String newTopicTitle = this.getSelectedTopicTitle();
    //prtMsgManager.renameTopicFolder(newTopicTitle) ;
    return "pvtMsgFolderSettings" ;
  }
  
  //Delete
  public String processPvtMsgFldDelete() 
  {
    String delFolder=getSelectedTopicTitle();
    //prtMsgManager.deleteTopicFolder(delFolder) ;
    return "pvtMsgFolderSettings";
  }
  public String processPvtMsgFldAddCancel() {
    return "pvtMsgFolderSettings";
  }
  
  ///////////////   SEARCH      ///////////////////////
  public String processSearch() 
  {
    List newls = new ArrayList() ;
    for (Iterator iter = getDisplayPvtMsgs().iterator(); iter.hasNext();)
    {
      PrivateMessage element = (PrivateMessage) iter.next();
      String message=element.getTitle();
      StringTokenizer st = new StringTokenizer(message);
      while (st.hasMoreTokens())
      {
        if(st.nextToken().equalsIgnoreCase(getSearchText()))
        {
          newls.add(element) ;
        }
      }
    }
    
    newls = createDecoratedDisplay(newls);
    setDisplayPvtMsgs(newls) ;
    
    
    return null ;
  }
  
  
  
  
  
  
  
  
  
  
  //////// GETTER AND SETTER  ///////////////////  
  ////////////////////
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
  
  public String processCDFMPostMessage()
  {
//    Message message = topicProxy.getMessageModel().createPersistible();
//    messageForumsMessageManager.saveMessage(message);
    return "compose";
  }

  public String processCDFMSaveDraft()
  {
//    Message message = topicProxy.getMessageModel().createPersistible();
//    message.setDraft(Boolean.TRUE);
//    messageForumsMessageManager.saveMessage(message);
    return "compose";
  }

  public String processCDFMCancel()
  {
    return "compose";
  }


  public String processTestLinkCompose()
  {
    return "compose";
  }

}