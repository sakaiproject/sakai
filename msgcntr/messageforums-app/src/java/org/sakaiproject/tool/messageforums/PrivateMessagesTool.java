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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessageManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.framework.portal.cover.PortalService;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.authzGroup.Member;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.authzGroup.cover.AuthzGroupService;
import org.sakaiproject.service.legacy.coursemanagement.CourseMember;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;

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
  
  private PrivateForum forum; 
  private List pvtTopics=new ArrayList();
  private List displayPvtMsgs=new ArrayList() ;
  private String msgNavMode="" ;
  private Message detailMsg ;
  private String currentMsgUuid; //this is the message which is being currently edited/displayed/deleted
  private boolean navModeIsDelete=false ; // Delete mode to show up extra buttons in pvtMsg.jsp page
  
  //delete confirmation screen 
  private boolean deleteConfirm=false ; //used for displaying delete confirmation message in same jsp
  
  //Compose Screen
  private String[] selectedComposeToList;
  private String composeSendAs="pvtmsg"; // currently set as Default as change by user is allowed
  private String composeSubject ;
  private String composeBody ;
  private String composeLabel ;   
  private List totalComposeToList=new ArrayList();
  
  
  //Setting Screen
  private String activatePvtMsg="yes";
  private boolean forwardPvtMsg;
  private String forwardPvtMsgEmail;
  private boolean superUser; 
  
  //message header screen
  private String searchText;
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
  
  public PrivateForum getForum()
  {
    return forum;
  }
  
  public List getPvtTopics()
  {
    return pvtTopics;
  }
  
  public List getDispPvtMsgs()
  {
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
      }
    }
    return displayPvtMsgs ;
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

  public void setSelectedComposeToList(String[] selectedComposeToList)
  {
    this.selectedComposeToList = selectedComposeToList;
  }
  
  public void setTotalComposeToList(List totalComposeToList)
  {
    this.totalComposeToList = totalComposeToList;
  }
  
  public String[] getSelectedComposeToList()
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
                        participants.add(participant);
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
          totalComposeToList.add(new SelectItem(((Participant) participants.get(i)).getName(),((Participant) participants.get(i)).getUniqname()));
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
    
  //Setting Getter and Setter
  public String getActivatePvtMsg()
  {
    return activatePvtMsg;
  }
  public void setActivatePvtMsg(String activatePvtMsg)
  {
    this.activatePvtMsg = activatePvtMsg;
  }
  public boolean isForwardPvtMsg()
  {
    return forwardPvtMsg;
  }
  public void setForwardPvtMsg(boolean forwardPvtMsg)
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
  public boolean isSuperUser()
  {
    return superUser;
  }
  public void setSuperUser(boolean superUser)
  {
    this.superUser = superUser;
  }

  //message header Getter 
  public String getSearchText()
  {
    return searchText ;
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
  public String processPvtMsgTopic()
  {
    String pvtMsgTopicTitle="";
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
          pvtMsgTopicTitle = (String) paramMap.get(key);
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
    if(pvtMsgTopicTitle.equals(PVTMSG_MODE_RECEIVED))
    {
      msgNavMode=PVTMSG_MODE_RECEIVED;
      return "pvtMsg";
    }
    if(pvtMsgTopicTitle.equals(PVTMSG_MODE_SENT))
    {
      msgNavMode=PVTMSG_MODE_SENT;
      return "pvtMsg";
    }
    if(pvtMsgTopicTitle.equals(PVTMSG_MODE_DELETE))
    {
      msgNavMode=PVTMSG_MODE_DELETE;
      this.setNavModeIsDelete(true); 
      return "pvtMsg";
    }
    if(pvtMsgTopicTitle.equals(PVTMSG_MODE_DRAFT))
    {
      msgNavMode=PVTMSG_MODE_DRAFT;
      return "pvtMsg";
    }
    if(pvtMsgTopicTitle.equals(PVTMSG_MODE_CASE))
    {
      msgNavMode=PVTMSG_MODE_CASE;
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
    if (this.getMsgNavMode().equals(PVTMSG_MODE_RECEIVED)||
        this.getMsgNavMode().equals(PVTMSG_MODE_SENT)||
        this.getMsgNavMode().equals(PVTMSG_MODE_DELETE)||
        this.getMsgNavMode().equals(PVTMSG_MODE_DRAFT)||
        this.getMsgNavMode().equals(PVTMSG_MODE_CASE))
    {
      resetFormVariable() ;
      return "pvtMsg" ;
    }else {
      resetFormVariable() ;
      return "main" ;
    }
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
      for (Iterator iter = this.getDispPvtMsgs().iterator(); iter.hasNext();)
      {
        Message aMsg = (Message) iter.next();
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
    //TODO - delete the message based on getCurrentMsgUuid()    
    if (this.getNavMode().equals(PVTMSG_MODE_RECEIVED))
    {
      return "pvtMsg" ;
    }else {
      return "main" ;
    }
  }
  
  //RESET form variable - required as the bean is in session and some attributes are used as helper for navigation
  public void resetFormVariable() {
    
    this.setNavModeIsDelete(false); 
    this.navMode="" ;
    this.deleteConfirm=false;
  }
  
  /**
   * process Compose action from different JSP'S
   * @return - pvtMsgCompose
   */ 
  public String processPvtMsgCompose() {     
    this.setNavModeIsDelete(false);
    return "pvtMsgCompose" ;
  }
  
  /**
   * process from Compose screen
   * @return - pvtMsg
   */ 
  public String processPvtMsgSend() {
    return "pvtMsg" ;
  }
  
  public String processPvtMsgEmptyDelete() {
    return "pvtMsg";
  }
  /**
   * process from Compose screen
   * @return - pvtMsg
   */
  public String processPvtMsgSaveDraft() {
    return "pvtMsg" ;    
  }
  
  public String processPvtMsgDispOtions() {
    return "pvtMsgOrganize" ;
  }
  public String processPvtMsgFldrSettings() {
    return "pvtMsgSettings" ;
  }

  ////////////////////////////////////////////////////////
  
  
  //
  // start button process actions
  //
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

  public String processCDFMAddAttachmentRedirect()
  {
    try
    {
      ExternalContext context = FacesContext.getCurrentInstance()
          .getExternalContext();
      context.redirect("sakai.filepicker.helper/tool");
      return null;
    }
    catch (Exception e)
    {
      LOG.error(this + ".processAddAttachRedirect - " + e);
      e.printStackTrace();
      return null;
    }
  }

  public String processTestLinkCompose()
  {
    return "compose";
  }

  //
  // end button process actions
  //

  // helpers

  public ErrorMessages getErrorMessages()
  {
    return errorMessages;
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
    this.setSuperUser(SecurityService.isSuperUser());
    //TODO get private message settings
    return "pvtMsgSettings";
  }

  public String processPvtMsgSettingRevise() {
     String email= getForwardPvtMsgEmail();
    String test=getActivatePvtMsg() ;
    //TODO - save private message settings here
    return "main" ;
  }
  
  //Received screen


  

  //////// GETTER AND SETTER  ///////////////////
  private String navMode ;
  //First screen - main
  private String forumTitle ;
  private List pvtTopicList ;  //title of this should be something like Received/Sent/Delete etc And shoild contain Total number of messages
  

  
  //Received Screen
  private List receivedItems ;
  

  
  //////////////////////////////////////////////////////////////
  public String getNavMode()
  {
    return navMode;
  }  
  public void setNavMode(String navMode)
  {
    this.navMode = navMode;
  }
  
  public String getForumTitle()
  {
    Area privateArea=prtMsgManager.getPrivateArea();
    if(privateArea != null ) {
      List forums=privateArea.getPrivateForums();
      //Private message return ONLY ONE ELEMENT
      for (Iterator iter = forums.iterator(); iter.hasNext();)
      {
        PrivateForum element = (PrivateForum) iter.next();
        this.forumTitle= element.getTitle();  
      }
    }
    return forumTitle;
  }
  public void setForumTitle(String forumTitle)
  {
    this.forumTitle = forumTitle;
  }
  public List getPvtTopicList()
  {
    Area privateArea=prtMsgManager.getPrivateArea();
    if(privateArea != null ) {
      List forums=privateArea.getPrivateForums();
      //Private message return ONLY ONE ELEMENT
      for (Iterator iter = forums.iterator(); iter.hasNext();)
      {
        PrivateForum element = (PrivateForum) iter.next();
        this.setForumTitle(element.getTitle());
        
        List pvtTopics=element.getTopics();
        this.pvtTopicList= pvtTopics ;      
      }
    }    
    return pvtTopicList;
  }
  public void setPvtTopicList(List pvtTopicList)
  {
    this.pvtTopicList = pvtTopicList;
  }

 public List getReceivedItems()
  {
    //receivedItems= (List) prtMsgManager.getPrivateArea();
//    Area privateArea=prtMsgManager.getPrivateArea();
//    List forums=privateArea.getForums();
//    Iterator iter = forums.iterator();
//    while(iter.hasNext())
//    {
//      PrivateForum forum = (PrivateForum)iter.next();
//      List topics=forum.getTopics();
//      Iterator iter1 = topics.iterator();
//      while(iter1.hasNext())
//      {
//        Topic privateTopic = (Topic) iter1.next();
//        List messages=privateTopic.getMessages();
//        Iterator iter2 = messages.iterator();
//         while (iter2.hasNext())
//         {
//           PrivateMessage msg = (PrivateMessage) iter2.next();
//         }
//      }      
//    }
    return receivedItems;
  }
  public void setReceivedItems(List receivedItems)
  {
    this.receivedItems = receivedItems;
  }

  //List containing description for radio buttons in setting screen
  public List getActivateMsgLs()
  {
    List a= new ArrayList();
    a.add("yes");
    a.add("no") ;
    return a;
  }

 
  /**
   * @return Returns the deletConfirm.
   */
  

  ///compose 
  
}