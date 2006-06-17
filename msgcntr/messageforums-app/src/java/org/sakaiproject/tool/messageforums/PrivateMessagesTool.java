/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/PrivateMessagesTool.java $
 * $Id: PrivateMessagesTool.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.tool.messageforums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.MembershipManager;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.PrivateMessageRecipient;
import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.app.messageforums.MembershipItem;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateTopicImpl;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.messageforums.ui.PrivateForumDecoratedBean;
import org.sakaiproject.tool.messageforums.ui.PrivateMessageDecoratedBean;
import org.sakaiproject.tool.messageforums.ui.PrivateTopicDecoratedBean;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

public class PrivateMessagesTool
{
  
  private static final Log LOG = LogFactory.getLog(PrivateMessagesTool.class);
  private static final String MESSAGECENTER_PRIVACY_URL = "messagecenter.privacy.url";

  private static final String MESSAGECENTER_PRIVACY_TEXT = "messagecenter.privacy.text";

  

  /**
   *Dependency Injected 
   */
  private PrivateMessageManager prtMsgManager;
  private MessageForumsMessageManager messageManager;
  private MessageForumsForumManager forumManager;
  private ErrorMessages errorMessages;
  private SakaiPersonManager sakaiPersonManager;
  private MembershipManager membershipManager;
  
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
  
  public static final String RECIPIANTS_ENTIRE_CLASS= "All Participants";
  public static final String RECIPIANTS_ALL_INSTRUCTORS= "All Instructors";
  
  public static final String SET_AS_YES="yes";
  public static final String SET_AS_NO="no";    
  
  PrivateForumDecoratedBean decoratedForum;
  
  private Area area;
  private PrivateForum forum;  
  private List pvtTopics=new ArrayList();
  private List decoratedPvtMsgs;
  private String msgNavMode="privateMessages" ;
  private PrivateMessageDecoratedBean detailMsg ;
  
  private String currentMsgUuid; //this is the message which is being currently edited/displayed/deleted
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
  private String selectedLabel="Normal" ;   //defautl set
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
  
  private Map courseMemberMap;
  
  
  public static final String SORT_SUBJECT_ASC = "subject_asc";
  public static final String SORT_SUBJECT_DESC = "subject_desc";
  public static final String SORT_AUTHOR_ASC = "author_asc";
  public static final String SORT_AUTHOR_DESC = "author_desc";
  public static final String SORT_DATE_ASC = "date_asc";
  public static final String SORT_DATE_DESC = "date_desc";
  public static final String SORT_LABEL_ASC = "label_asc";
  public static final String SORT_LABEL_DESC = "label_desc";
  public static final String SORT_TO_ASC = "to_asc";
  public static final String SORT_TO_DESC = "to_desc";
  public static final String SORT_ATTACHMENT_ASC = "attachment_asc";
  public static final String SORT_ATTACHMENT_DESC = "attachment_desc";
  
  /** sort member */
  private String sortType = SORT_DATE_DESC;
  
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
   * @param membershipManager
   */
  public void setMembershipManager(MembershipManager membershipManager)
  {
    this.membershipManager = membershipManager;
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
      pf = prtMsgManager.initializationHelper(pf);
      pvtTopics = pf.getTopics();
      Collections.sort(pvtTopics, PrivateTopicImpl.TITLE_COMPARATOR);   //changed to date comparator
      forum=pf;           
      activatePvtMsg = (Boolean.TRUE.equals(area.getEnabled())) ? "yes" : "no";
      forwardPvtMsg = (Boolean.TRUE.equals(pf.getAutoForward())) ? "yes" : "no";
      forwardPvtMsgEmail = pf.getAutoForwardEmail();      
    } 
  }
  
  /**
   * Property created rather than setErrorMessage for design requirement
   * @return
   */
  public boolean isDispError()
  {
    if (isInstructor() && !getPvtAreaEnabled())
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  public boolean getPvtAreaEnabled()
  {  
    return area.getEnabled().booleanValue();
  }      
  
  //Return decorated Forum
  public PrivateForumDecoratedBean getDecoratedForum()
  {      
      PrivateForumDecoratedBean decoratedForum = new PrivateForumDecoratedBean(getForum()) ;
      
      /** only load topics/counts if area is enabled */
      if (getPvtAreaEnabled()){        
        for (Iterator iterator = pvtTopics.iterator(); iterator.hasNext();)
        {
          PrivateTopic topic = (PrivateTopic) iterator.next();
          if (topic != null)
          {
          	
          	/** filter topics by context and type*/                                                    
            if (topic.getTypeUuid() != null
            		&& topic.getTypeUuid().equals(typeManager.getUserDefinedPrivateTopicType())
            	  && topic.getContextId() != null && !topic.getContextId().equals(prtMsgManager.getContextId())){
               continue;
            }       
          	
            PrivateTopicDecoratedBean decoTopic= new PrivateTopicDecoratedBean(topic) ;
            //decoTopic.setTotalNoMessages(prtMsgManager.getTotalNoMessages(topic)) ;
            //decoTopic.setUnreadNoMessages(prtMsgManager.getUnreadNoMessages(SessionManager.getCurrentSessionUserId(), topic)) ;
          
            String typeUuid = getPrivateMessageTypeFromContext(topic.getTitle());          
          
            decoTopic.setTotalNoMessages(prtMsgManager.findMessageCount(typeUuid));
            decoTopic.setUnreadNoMessages(prtMsgManager.findUnreadMessageCount(typeUuid));
          
            decoratedForum.addTopic(decoTopic);
          }          
        }
      }
    return decoratedForum ;
  }

  public List getDecoratedPvtMsgs()
  {
  	/** 
  	    avoid apply_request_values and render_response from calling this method on postback
  	    solution -- only call durig render_response phase
  	*/
  	if (!FacesContext.getCurrentInstance().getRenderResponse()){
  		return decoratedPvtMsgs;
  	}
  	  	  	
    decoratedPvtMsgs=new ArrayList() ;
        
    String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);
    
    /** support for sorting */
    String sortColumnParameter = getExternalParameterByKey("sortColumn");
  	
  	if ("subject".equals(sortColumnParameter)){  		  		
  		sortType = (SORT_SUBJECT_ASC.equals(sortType)) ? SORT_SUBJECT_DESC : SORT_SUBJECT_ASC;  			 		
  	}
  	else if ("author".equals(sortColumnParameter)){  		  		
  		sortType = (SORT_AUTHOR_ASC.equals(sortType)) ? SORT_AUTHOR_DESC : SORT_AUTHOR_ASC;  			 		
  	}
  	else if ("date".equals(sortColumnParameter)){  		  		
  		sortType = (SORT_DATE_ASC.equals(sortType)) ? SORT_DATE_DESC : SORT_DATE_ASC;  			 		
  	}
  	else if ("label".equals(sortColumnParameter)){  		  		
  		sortType = (SORT_LABEL_ASC.equals(sortType)) ? SORT_LABEL_DESC : SORT_LABEL_ASC;  			 		
  	}
  	else if ("to".equals(sortColumnParameter)){  		  		
  		sortType = (SORT_TO_ASC.equals(sortType)) ? SORT_TO_DESC : SORT_TO_ASC;  			 		
  	}
  	else if ("attachment".equals(sortColumnParameter)){  		  		
  		sortType = (SORT_ATTACHMENT_ASC.equals(sortType)) ? SORT_ATTACHMENT_DESC : SORT_ATTACHMENT_ASC;  			 		
  	}
  	else{
  		sortType = SORT_DATE_DESC;
  	}
    
    /** add support for sorting */
    if (SORT_SUBJECT_ASC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_SUBJECT,
          PrivateMessageManager.SORT_ASC);
    }
    else if (SORT_SUBJECT_DESC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_SUBJECT,
          PrivateMessageManager.SORT_DESC);
    }
    else if (SORT_AUTHOR_ASC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_AUTHOR,
          PrivateMessageManager.SORT_ASC);
    }        
    else if (SORT_AUTHOR_DESC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_AUTHOR,
          PrivateMessageManager.SORT_DESC);
    }
    else if (SORT_DATE_ASC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
          PrivateMessageManager.SORT_ASC);
    }        
    else if (SORT_DATE_DESC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
          PrivateMessageManager.SORT_DESC);
    }
    else if (SORT_LABEL_ASC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_LABEL,
          PrivateMessageManager.SORT_ASC);
    }        
    else if (SORT_LABEL_DESC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_LABEL,
          PrivateMessageManager.SORT_DESC);
    }
    else if (SORT_TO_ASC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_TO,
          PrivateMessageManager.SORT_ASC);
    }        
    else if (SORT_TO_DESC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_TO,
          PrivateMessageManager.SORT_DESC);
    }        
    else if (SORT_ATTACHMENT_ASC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_ATTACHMENT,
          PrivateMessageManager.SORT_ASC);
    }        
    else if (SORT_ATTACHMENT_DESC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_ATTACHMENT,
          PrivateMessageManager.SORT_DESC);
    }
    
    
    decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);    

    //pre/next message
    if(decoratedPvtMsgs != null)
    {
      List tempMsgs = decoratedPvtMsgs;
      for(int i=0; i<tempMsgs.size(); i++)
      {
        PrivateMessageDecoratedBean dmb = (PrivateMessageDecoratedBean)tempMsgs.get(i);
        if(i==0)
        {
          dmb.setHasPre(false);
          if(i==(tempMsgs.size()-1))
          {
              dmb.setHasNext(false);
          }
          else
          {
              dmb.setHasNext(true);
          }
        }
        else if(i==(tempMsgs.size()-1))
        {
          dmb.setHasPre(true);
          dmb.setHasNext(false);
        }
        else
        {
          dmb.setHasNext(true);
          dmb.setHasPre(true);
        }
      }
    }
    if(selectView!=null && selectView.equalsIgnoreCase("threaded"))
    {
    	this.rearrageTopicMsgsThreaded(false);
    }
    return decoratedPvtMsgs ;
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

  public String getSelectedLabel()
  {
    return selectedLabel;
  }
  
  public void setSelectedLabel(String selectedLabel)
  {
    this.selectedLabel = selectedLabel;
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
    
  public List getTotalComposeToList()
  { 
    
    /** protect from jsf calling multiple times */
    if (totalComposeToList != null){
      return totalComposeToList;
    }
    
    totalComposeToListRecipients = new ArrayList();
 
    courseMemberMap = membershipManager.getFilteredCourseMembers(true);
 
    List members = membershipManager.convertMemberMapToList(courseMemberMap);
    List selectItemList = new ArrayList();
    
    /** create a list of SelectItem elements */
    for (Iterator i = members.iterator(); i.hasNext();){
      
      MembershipItem item = (MembershipItem) i.next();     
      selectItemList.add(
        new SelectItem(item.getId(), item.getName()));
    }
    
    totalComposeToList = selectItemList;
    return selectItemList;       
  }
  
  public String getUserSortNameById(String id){    
    try
    {
      User user=UserDirectoryService.getUser(id) ;
      userName= user.getSortName();
    }
    catch (UserNotDefinedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    return userName;
  }

  public String getUserName() {
   String userId=SessionManager.getCurrentSessionUserId();
   try
   {
     User user=UserDirectoryService.getUser(userId) ;
     userName= user.getDisplayName();
   }
   catch (UserNotDefinedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
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
  public void processChangeSelectView(ValueChangeEvent eve)
  {
    String currentValue = (String) eve.getNewValue();
  	if (currentValue == null)
  	{
  		selectView = "";
  		return;
    }
  	else
  	{
  		if(currentValue.equalsIgnoreCase("threaded"))
  		{
  			selectView = "threaded";
  			return;
  		}
  		else
  		{
  			selectView = "";
  			return;
  		}
  	}
  }
  
  public void rearrageTopicMsgsThreaded(boolean searcModeOn)
  {
  	List msgsList = new ArrayList();
    
    if(searcModeOn)
    {
      for(int i=0; i<searchPvtMsgs.size(); i++)
      {
          msgsList.add((PrivateMessageDecoratedBean)searchPvtMsgs.get(i));
      }
      searchPvtMsgs.clear();
    }else
    {
      for(int i=0; i<decoratedPvtMsgs.size(); i++)
      {
          msgsList.add((PrivateMessageDecoratedBean)decoratedPvtMsgs.get(i));
      }   
      decoratedPvtMsgs.clear();
    }
  	
  	
  	if(msgsList != null)
  	{
  		Set msgsSet = new HashSet();
  		for(int i=0; i<msgsList.size(); i++)
  		{
  			msgsSet.add((PrivateMessageDecoratedBean)msgsList.get(i));
  		}
  		Iterator iter = msgsSet.iterator();
  		while(iter.hasNext())
  		{
  			List allRelatedMsgs = messageManager.getAllRelatedMsgs(
  					((PrivateMessageDecoratedBean)iter.next()).getMsg().getId());
  			List currentRelatedMsgs = new ArrayList();
  			if(allRelatedMsgs != null && allRelatedMsgs.size()>0)
  			{
  				PrivateMessageDecoratedBean pdb = new PrivateMessageDecoratedBean((PrivateMessage)allRelatedMsgs.get(0));
  				pdb.setDepth(-1);
  				boolean firstEleAdded = false;
  				for(int i=0; i<msgsList.size(); i++)
  				{
  					PrivateMessageDecoratedBean tempPMDB = (PrivateMessageDecoratedBean)msgsList.get(i);
  	        if (tempPMDB.getMsg().getId().equals(pdb.getMsg().getId()))
  	        {
  	          tempPMDB.setDepth(0);
  	          currentRelatedMsgs.add(tempPMDB);
  	          firstEleAdded = true;
  	          recursiveGetThreadedMsgsFromList(msgsList, allRelatedMsgs, currentRelatedMsgs, tempPMDB);
  	          break;
  	        }
  				}
  				if(!firstEleAdded)
  					recursiveGetThreadedMsgsFromList(msgsList, allRelatedMsgs, currentRelatedMsgs, pdb);
  			}
  			for(int i=0; i<currentRelatedMsgs.size(); i++)
  			{
  				if(searcModeOn)
                {
                  searchPvtMsgs.add((PrivateMessageDecoratedBean)currentRelatedMsgs.get(i));
                }else
                {
                  decoratedPvtMsgs.add((PrivateMessageDecoratedBean)currentRelatedMsgs.get(i));
                }
              
  				msgsSet.remove((PrivateMessageDecoratedBean)currentRelatedMsgs.get(i));
  			}
  			
  			iter = msgsSet.iterator();
  		}
  	}
  }
  
  private void recursiveGetThreadedMsgsFromList(List msgsList, 
  		List allRelatedMsgs, List returnList,
      PrivateMessageDecoratedBean currentMsg)
  {
    for (int i = 0; i < allRelatedMsgs.size(); i++)
    {
    	PrivateMessageDecoratedBean thisMsgBean = 
    		new PrivateMessageDecoratedBean((PrivateMessage)allRelatedMsgs.get(i));
      Message thisMsg = thisMsgBean.getMsg();
      boolean existedInCurrentUserList = false;
      for(int j=0; j< msgsList.size(); j++)
      {
      	PrivateMessageDecoratedBean currentUserBean = 
      		(PrivateMessageDecoratedBean)msgsList.get(j);
        if (thisMsg.getInReplyTo() != null
            && thisMsg.getInReplyTo().getId().equals(
                currentMsg.getMsg().getId())
						&& currentUserBean.getMsg().getId().equals(thisMsg.getId()))
        {
          currentUserBean.setDepth(currentMsg.getDepth() + 1);
          if(currentMsg.getDepth() > -1)
          {
          	currentUserBean.setUiInReply(((PrivateMessageDecoratedBean)returnList.get(returnList.size()-1)).getMsg());
          }
          returnList.add(currentUserBean);
          existedInCurrentUserList = true;
          recursiveGetThreadedMsgsFromList(msgsList, allRelatedMsgs, returnList, currentUserBean);
          break;
        }
      }
      if(!existedInCurrentUserList)
      {
        if(thisMsg.getInReplyTo() != null
            && thisMsg.getInReplyTo().getId().equals(
                currentMsg.getMsg().getId()))
        {
          thisMsgBean.setDepth(currentMsg.getDepth());
          recursiveGetThreadedMsgsFromList(msgsList, allRelatedMsgs, returnList, thisMsgBean);
        }
      }
    }
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
    msgNavMode = "privateMessages";
    return  "main";
  }  
  public String processActionPrivateMessages()
  {
    LOG.debug("processActionPrivateMessages()");                    
    msgNavMode = "privateMessages";            
    return  "pvtMsgHpView";
  }        
  public String processDisplayForum()
  {
    LOG.debug("processDisplayForum()");
    return "pvtMsg" ;
  }
  public String processPvtMsgTopic()
  {
    LOG.debug("processPvtMsgTopic()");
    
    /** reset sort type */
    sortType = SORT_DATE_DESC;    
    
    //get external parameter
    selectedTopicTitle = getExternalParameterByKey("pvtMsgTopicTitle") ;
    setSelectedTopicId(getExternalParameterByKey("pvtMsgTopicId")) ;
    msgNavMode=getSelectedTopicTitle();

    //set prev/next topic details
    setPrevNextTopicDetails(msgNavMode);
    
    return "pvtMsg";
  }
    
  /**
   * process Cancel from all JSP's
   * @return - pvtMsg
   */  
  public String processPvtMsgCancel() {
    LOG.debug("processPvtMsgCancel()");
    return "main";     
  }
  
  public String processPvtMsgDetailCancel()
  {
  	return "pvtMsg";
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
        
        List recLs= initPrivateMessage.getRecipients();
        for (Iterator iterator = recLs.iterator(); iterator.hasNext();)
        {
          PrivateMessageRecipient element = (PrivateMessageRecipient) iterator.next();
          if (element != null)
          {
            if((element.getRead().booleanValue()) || (element.getUserId().equals(getUserId())) )
            {
             getDetailMsg().setHasRead(true) ;
            }
          }
        }
        //ADD the recipientText here 
        this.getDetailMsg().getMsg().setRecipientsAsText(dMsg.getMsg().getRecipientsAsText());
      }
    }
    this.deleteConfirm=false; //reset this as used for multiple action in same JSP
   
    //prev/next message 
    if(decoratedPvtMsgs != null)
    {
      for(int i=0; i<decoratedPvtMsgs.size(); i++)
      {
        PrivateMessageDecoratedBean thisDmb = (PrivateMessageDecoratedBean)decoratedPvtMsgs.get(i);
        if(((PrivateMessageDecoratedBean)decoratedPvtMsgs.get(i)).getMsg().getId().toString().equals(msgId))
        {
          detailMsg.setDepth(thisDmb.getDepth());
          detailMsg.setHasNext(thisDmb.getHasNext());
          detailMsg.setHasPre(thisDmb.getHasPre());
          break;
        }
      }
    }
    //default setting for moveTo
    moveToTopic=selectedTopicId;
    return "pvtMsgDetail";
  }

  /**
   * called from Single delete Page
   * @return - pvtMsgReply
   */ 
  public String processPvtMsgReply() {
    LOG.debug("processPvtMsgReply()");
//    //set default userName
//    List defName = new ArrayList();
//    defName.add(getUserName());
//    List d = new ArrayList();
//    for (Iterator iter = defName.iterator(); iter.hasNext();)
//    {
//      String element = (String) iter.next();
//      d.add(new SelectItem(element));      
//    }
//    this.setSelectedComposeToList(d);
    //set Dafult Subject
    if(getDetailMsg() != null)
    {
      replyToSubject="Re: " +getDetailMsg().getMsg().getTitle();
    }
    
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
   * @return - pvtMsgDetail
   */ 
  public String processPvtMsgDeleteConfirm() {
    LOG.debug("processPvtMsgDeleteConfirm()");
    
    this.setDeleteConfirm(true);
    setErrorMessage("Are you sure you want to delete this message? If yes, click Delete to delete the message.");
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
    
    this.msgNavMode="" ;
    this.deleteConfirm=false;
    selectAll=false;
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
    if(("privateMessages").equals(getMsgNavMode()))
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
    this.setComposeSendAsPvtMsg(SET_AS_YES); //set as default
    this.getSelectedComposeToList().clear();
    this.setReplyToSubject("");
    this.setReplyToBody("");
    this.getAttachments().clear();
    this.getAllAttachments().clear();
    //reset label
    this.setSelectedLabel("Normal");
  }
  /**
   * process from Compose screen
   * @return - pvtMsg
   */ 
  public String processPvtMsgSend() {
          
    LOG.debug("processPvtMsgSend()");
    
    if(!hasValue(getComposeSubject()))
    {
      setErrorMessage("You must enter a subject before you may send this message.");
      return null ;
    }
//    if(!hasValue(getComposeBody()) )
//    {
//      setErrorMessage("Please enter message body for this compose message.");
//      return null ;
//    }
    if(getSelectedComposeToList().size()<1)
    {
      setErrorMessage("You must select a recipient before you may send this message.");
      return null ;
    }
    
    PrivateMessage pMsg= constructMessage() ;
    
    if((SET_AS_YES).equals(getComposeSendAsPvtMsg()))
    {
      prtMsgManager.sendPrivateMessage(pMsg, getRecipients(), false); 
    }
    else{
      prtMsgManager.sendPrivateMessage(pMsg, getRecipients(), true);
    }

    //reset contents
    resetComposeContents();
    
    return "main";    
  }
     
  /**
   * process from Compose screen
   * @return - pvtMsg
   */
  public String processPvtMsgSaveDraft() {
    LOG.debug("processPvtMsgSaveDraft()");
    if(!hasValue(getComposeSubject()))
    {
      setErrorMessage("You must enter a subject before you may send this message.");
      return null ;
    }
//    if(!hasValue(getComposeBody()) )
//    {
//      setErrorMessage("Please enter message body for this compose message.");
//      return null ;
//    }
    if(getSelectedComposeToList().size()<1)
    {
      setErrorMessage("You must select a recipient before you may send this message.");
      return null ;
    }
    
    PrivateMessage dMsg=constructMessage() ;
    dMsg.setDraft(Boolean.TRUE);
    
    if((SET_AS_YES).equals(getComposeSendAsPvtMsg()))
    {
      prtMsgManager.sendPrivateMessage(dMsg, getRecipients(), false); 
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
      
      String authorString = "";
      if(!"false".equalsIgnoreCase(ServerConfigurationService.getString
  			("separateIdEid@org.sakaiproject.user.api.UserDirectoryService")))
  	  {
  	    try
  	    {
  		  authorString += UserDirectoryService.getUser(getUserId()).getLastName() + ", ";
  		  authorString += UserDirectoryService.getUser(getUserId()).getFirstName();
  	    }
  	    catch(Exception e)
  	    {
  	      e.printStackTrace();
  	    }
  	  }
      else
    	authorString = getUserId();
      aMsg.setAuthor(authorString);
      aMsg.setDraft(Boolean.FALSE);      
      aMsg.setApproved(Boolean.FALSE);     
      aMsg.setLabel(getSelectedLabel());
      
      //Add the recipientList as String for display in Sent folder
      String sendToString="";
      for (int i = 0; i < selectedComposeToList.size(); i++)
      {
        MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(i));  
        if(membershipItem != null)
        {
          sendToString +=membershipItem.getName()+";" ;
        }          
      }
      sendToString=sendToString.substring(0, sendToString.length()-1); //remove last comma
      aMsg.setRecipientsAsText(sendToString);
      
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
  /**
   * Set Previous and Next message details with each PrivateMessageDecoratedBean
   */
  public void setPrevNextMessageDetails()
  {
    List tempMsgs = decoratedPvtMsgs;
    for(int i=0; i<tempMsgs.size(); i++)
    {
      PrivateMessageDecoratedBean dmb = (PrivateMessageDecoratedBean)tempMsgs.get(i);
      if(i==0)
      {
        dmb.setHasPre(false);
        if(i==(tempMsgs.size()-1))
        {
            dmb.setHasNext(false);
        }
        else
        {
            dmb.setHasNext(true);
        }
      }
      else if(i==(tempMsgs.size()-1))
      {
        dmb.setHasPre(true);
        dmb.setHasNext(false);
      }
      else
      {
        dmb.setHasNext(true);
        dmb.setHasPre(true);
      }
    }
  }
  
  /**
   * processDisplayPreviousMsg()
   * Display the previous message from the list of decorated messages
   */
  public String processDisplayPreviousMsg()
  {
    List tempMsgs = getDecoratedPvtMsgs(); // all messages
    int currentMsgPosition = -1;
    if(tempMsgs != null)
    {
      for(int i=0; i<tempMsgs.size(); i++)
      {
        PrivateMessageDecoratedBean thisDmb = (PrivateMessageDecoratedBean)tempMsgs.get(i);
        if(detailMsg.getMsg().getId().equals(thisDmb.getMsg().getId()))
        {
          currentMsgPosition = i;
          break;
        }
      }
    }
    
    if(currentMsgPosition > 0)
    {
      PrivateMessageDecoratedBean thisDmb = (PrivateMessageDecoratedBean)tempMsgs.get(currentMsgPosition-1);
      PrivateMessage message= (PrivateMessage) prtMsgManager.getMessageById(thisDmb.getMsg().getId()); 
      
      detailMsg= new PrivateMessageDecoratedBean(message);
      //get attachments
      prtMsgManager.markMessageAsReadForUser(detailMsg.getMsg());
      
      PrivateMessage initPrivateMessage = prtMsgManager.initMessageWithAttachmentsAndRecipients(detailMsg.getMsg());
      this.setDetailMsg(new PrivateMessageDecoratedBean(initPrivateMessage));
      
      List recLs= initPrivateMessage.getRecipients();
      for (Iterator iterator = recLs.iterator(); iterator.hasNext();)
      {
        PrivateMessageRecipient element = (PrivateMessageRecipient) iterator.next();
        if (element != null)
        {
          if((element.getRead().booleanValue()) || (element.getUserId().equals(getUserId())) )
          {
           getDetailMsg().setHasRead(true) ;
          }
        }
      }      
      getDetailMsg().setDepth(thisDmb.getDepth()) ;
      getDetailMsg().setHasNext(thisDmb.getHasNext());
      getDetailMsg().setHasPre(thisDmb.getHasPre()) ;

    }    
    return null;
  }

  /**
   * processDisplayNextMsg()
   * Display the Next message from the list of decorated messages
   */    
  public String processDisplayNextMsg()
  {
    List tempMsgs = getDecoratedPvtMsgs();
    int currentMsgPosition = -1;
    if(tempMsgs != null)
    {
      for(int i=0; i<tempMsgs.size(); i++)
      {
        PrivateMessageDecoratedBean thisDmb = (PrivateMessageDecoratedBean)tempMsgs.get(i);
        if(detailMsg.getMsg().getId().equals(thisDmb.getMsg().getId()))
        {
          currentMsgPosition = i;
          break;
        }
      }
    }
    
    if(currentMsgPosition > -2  && currentMsgPosition < (tempMsgs.size()-1))
    {
      PrivateMessageDecoratedBean thisDmb = (PrivateMessageDecoratedBean)tempMsgs.get(currentMsgPosition+1);
      PrivateMessage message= (PrivateMessage) prtMsgManager.getMessageById(thisDmb.getMsg().getId()); 
      //get attachments
      prtMsgManager.markMessageAsReadForUser(thisDmb.getMsg());
      
      PrivateMessage initPrivateMessage = prtMsgManager.initMessageWithAttachmentsAndRecipients(thisDmb.getMsg());
      this.setDetailMsg(new PrivateMessageDecoratedBean(initPrivateMessage));
      
      List recLs= initPrivateMessage.getRecipients();
      for (Iterator iterator = recLs.iterator(); iterator.hasNext();)
      {
        PrivateMessageRecipient element = (PrivateMessageRecipient) iterator.next();
        if (element != null)
        {
          if((element.getRead().booleanValue()) || (element.getUserId().equals(getUserId())) )
          {
           getDetailMsg().setHasRead(true) ;
          }
        }
      }
      
      getDetailMsg().setDepth(thisDmb.getDepth()) ;
      getDetailMsg().setHasNext(thisDmb.getHasNext());
      getDetailMsg().setHasPre(thisDmb.getHasPre()) ;
    }
    
    return null;
  }
  
  
  /////////////////////////////////////     DISPLAY NEXT/PREVIOUS TOPIC     //////////////////////////////////  
  private PrivateTopicDecoratedBean selectedTopic;
  
  /**
   * @return Returns the selectedTopic.
   */
  public PrivateTopicDecoratedBean getSelectedTopic()
  {
    return selectedTopic;
  }


  /**
   * @param selectedTopic The selectedTopic to set.
   */
  public void setSelectedTopic(PrivateTopicDecoratedBean selectedTopic)
  {
    this.selectedTopic = selectedTopic;
  }


  /**
   * Add prev and next topic UUID value and booleans for display of links
   * 
   */
  public void setPrevNextTopicDetails(String msgNavMode)
  {
    for (int i = 0; i < pvtTopics.size(); i++)
    {
      Topic el = (Topic)pvtTopics.get(i);
      if(el.getTitle().equals(msgNavMode))
      {
        setSelectedTopic(new PrivateTopicDecoratedBean(el)) ;
        if(i ==0)
        {
          getSelectedTopic().setHasPreviousTopic(false);
          if(i==(pvtTopics.size()-1))
          {
            getSelectedTopic().setHasNextTopic(false) ;
          }
          else
          {
            getSelectedTopic().setHasNextTopic(true) ;
            Topic nt=(Topic)pvtTopics.get(i+1);
            if (nt != null)
            {
              //getSelectedTopic().setNextTopicId(nt.getUuid());
              getSelectedTopic().setNextTopicTitle(nt.getTitle());
            }
          }
        }
        else if(i==(pvtTopics.size()-1))
        {
          getSelectedTopic().setHasPreviousTopic(true);
          getSelectedTopic().setHasNextTopic(false) ;
          
          Topic pt=(Topic)pvtTopics.get(i-1);
          if (pt != null)
          {
            //getSelectedTopic().setPreviousTopicId(pt.getUuid());
            getSelectedTopic().setPreviousTopicTitle(pt.getTitle());
          }          
        }
        else
        {
          getSelectedTopic().setHasNextTopic(true) ;
          getSelectedTopic().setHasPreviousTopic(true);
          
          Topic nt=(Topic)pvtTopics.get(i+1);
          if (nt != null)
          {
            //getSelectedTopic().setNextTopicId(nt.getUuid());
            getSelectedTopic().setNextTopicTitle(nt.getTitle());
          }
          Topic pt=(Topic)pvtTopics.get(i-1);
          if (pt != null)
          {
            //getSelectedTopic().setPreviousTopicId(pt.getUuid());
            getSelectedTopic().setPreviousTopicTitle(pt.getTitle());
          }
        }
        
      }
    }
    //create a selected topic and set the topic id for next/prev topic
  }
  /**
   * processDisplayPreviousFolder()
   */
  public String processDisplayPreviousTopic() {
    String prevTopicTitle = getExternalParameterByKey("previousTopicTitle");
    if(hasValue(prevTopicTitle))
    {
      msgNavMode=prevTopicTitle;
      
      decoratedPvtMsgs=new ArrayList() ;
      
      String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);        
      
      decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
          PrivateMessageManager.SORT_DESC);
      
      decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);

      //set prev/next Topic
      setPrevNextTopicDetails(msgNavMode);
      //set prev/next message
      setPrevNextMessageDetails();
      
    }
    return null;
  }
  
  /**
   * processDisplayNextFolder()
   */
  public String processDisplayNextTopic()
  {  	  	
    String nextTitle = getExternalParameterByKey("nextTopicTitle");
    if(hasValue(nextTitle))
    {
      msgNavMode=nextTitle;
      decoratedPvtMsgs=new ArrayList() ;
      
      String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);        
      
      decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
          PrivateMessageManager.SORT_DESC);
      
      decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);

      //set prev/next Topic
      setPrevNextTopicDetails(msgNavMode);
      //set prev/next message
      setPrevNextMessageDetails();
    }
    return null;
  }
/////////////////////////////////////     DISPLAY NEXT/PREVIOUS TOPIC     //////////////////////////////////
    
  
  
  //////////////////////////////////////////////////////////
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
    
    PrivateMessage currentMessage = getDetailMsg().getMsg() ;
        
    //by default add current user
    for (Iterator i = totalComposeToList.iterator(); i.hasNext();) {      
      SelectItem selectItem = (SelectItem) i.next();
      MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectItem.getValue());                
      
      if (MembershipItem.TYPE_USER.equals(membershipItem.getType())) {
        if (membershipItem.getUser() != null) {
          if (membershipItem.getUser().getId().equals(currentMessage.getCreatedBy())) {
            selectedComposeToList.add(membershipItem.getId());
          }
        }
      }
    }
    
    if(!hasValue(getReplyToSubject()))
    {
      setErrorMessage("You must enter a subject before you may send this message.");
      return null ;
    }

    if(getSelectedComposeToList().size()<1)
    {
      setErrorMessage("Please select recipients list for this reply message.");
      return null ;
    }
        
    PrivateMessage rrepMsg = messageManager.createPrivateMessage() ;
       
    rrepMsg.setTitle(getReplyToSubject()) ; //rrepMsg.setTitle(rMsg.getTitle()) ;
    rrepMsg.setDraft(Boolean.FALSE);
    String authorString = "";
    if("true".equalsIgnoreCase(ServerConfigurationService.getString
  			("separateIdEid@org.sakaiproject.user.api.UserDirectoryService")))
  	{
  	  try
  	  {
  		authorString += UserDirectoryService.getUser(getUserId()).getLastName() + ", ";
  		authorString += UserDirectoryService.getUser(getUserId()).getFirstName();
  	  }
  	  catch(Exception e)
  	  {
  		e.printStackTrace();
  	  }
  	}
    else
      authorString += getUserId();
    rrepMsg.setAuthor(authorString);
    rrepMsg.setApproved(Boolean.FALSE);
    rrepMsg.setBody(getReplyToBody()) ;
    
    rrepMsg.setLabel(getSelectedLabel());
    
    rrepMsg.setInReplyTo(currentMessage) ;
    
    //Add the recipientList as String for display in Sent folder
    String sendToString="";
    for (int i = 0; i < selectedComposeToList.size(); i++)
    {
      MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(i));  
      if(membershipItem != null)
      {
        sendToString +=membershipItem.getName()+";" ;
      }          
    }
    sendToString=sendToString.substring(0, sendToString.length()-1); //remove last comma
    rrepMsg.setRecipientsAsText(sendToString);
    
    
    //Add attachments
    for(int i=0; i<allAttachments.size(); i++)
    {
      prtMsgManager.addAttachToPvtMsg(rrepMsg, (Attachment)allAttachments.get(i));         
    }            
    
    if((SET_AS_YES).equals(getComposeSendAsPvtMsg()))
    {
      prtMsgManager.sendPrivateMessage(rrepMsg, getRecipients(), false);
    }
    else{
      prtMsgManager.sendPrivateMessage(rrepMsg, getRecipients(), true);
    }
    
    //reset contents
    resetComposeContents();
    
    return "main" ;

  }
 
  /**
   * process from Compose screen
   * @return - pvtMsg
   */
  public String processPvtMsgReplySaveDraft() {
    LOG.debug("processPvtMsgReplySaveDraft()");
    
    if(!hasValue(getReplyToSubject()))
    {
      setErrorMessage("You must enter a subject before you may send this message.");
      return null ;
    }
//    if(!hasValue(getReplyToBody()) )
//    {
//      setErrorMessage("You must enter a message content before you may send this message.");
//      return null ;
//    }
    if(getSelectedComposeToList().size()<1)
    {
      setErrorMessage("Please select recipients list for this reply message.");
      return null ;
    }
    
    PrivateMessage drMsg=getDetailMsg().getMsg() ;
    //drMsg.setDraft(Boolean.TRUE);
    PrivateMessage drrepMsg = messageManager.createPrivateMessage() ;
    drrepMsg.setTitle(getReplyToSubject()) ;
    drrepMsg.setDraft(Boolean.TRUE);
    String authorString = "";
    if("true".equalsIgnoreCase(ServerConfigurationService.getString
  			("separateIdEid@org.sakaiproject.user.api.UserDirectoryService")))
  	{
  	  try
  	  {
  		authorString += UserDirectoryService.getUser(getUserId()).getLastName() + ", ";
  		authorString += UserDirectoryService.getUser(getUserId()).getFirstName();
  	  }
  	  catch(Exception e)
  	  {
  		e.printStackTrace();
  	  }
  	}
    else
      authorString = getUserId();
    drrepMsg.setAuthor(authorString);
    drrepMsg.setApproved(Boolean.FALSE);
    drrepMsg.setBody(getReplyToBody()) ;
    
    drrepMsg.setInReplyTo(drMsg) ;
    this.getRecipients().add(drMsg.getCreatedBy());
    
    //Add attachments
    for(int i=0; i<allAttachments.size(); i++)
    {
      prtMsgManager.addAttachToPvtMsg(drrepMsg, (Attachment)allAttachments.get(i));         
    } 
    
    if((SET_AS_YES).equals(getComposeSendAsPvtMsg()))
    {
     prtMsgManager.sendPrivateMessage(drrepMsg, getRecipients(), false);  
    }
    else{
      prtMsgManager.sendPrivateMessage(drrepMsg, getRecipients(), true);
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
      setErrorMessage("Please select list of messages to be deleted.");
      return null;  //stay in the same page if nothing is selected for delete
    }else {
      setErrorMessage("Are you sure you want to permanently delete the following message(s)?");
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
  
  
  ///////////////////////////       Process Select All       ///////////////////////////////
  private boolean selectAll = false;  
  public boolean isSelectAll()
  {
    return selectAll;
  }
  public void setSelectAll(boolean selectAll)
  {
    this.selectAll = selectAll;
  }

  /**
   * process isSelected for all decorated messages
   * @return same page i.e. will be pvtMsg 
   */
  public String processCheckAll()
  {
    LOG.debug("processCheckAll()");
    selectAll= !selectAll;
    
    return null;
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
      if(refs != null && refs.size()>0)
      {
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
      if(refs != null && refs.size()>0)
      {
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
    }
    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
    
//    if( allAttachments == null || (allAttachments.size()<1))
//    {
//      allAttachments.addAll(this.getDetailMsg().getMsg().getAttachments()) ;
//    }
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
        int pos = name.lastIndexOf("pvmsg_current_attach");
        
        if(pos>=0 && name.length()==pos+"pvmsg_current_attach".length())
        {
          attachId = (String)paramMap.get(key);
          break;
        }
      }
    }
    
    if ((attachId != null) && (!attachId.equals("")))
    {
      for (int i = 0; i < attachments.size(); i++)
      {
        if (attachId.equalsIgnoreCase(((Attachment) attachments.get(i))
            .getAttachmentId()))
        {
          attachments.remove(i);
          break;
        }
      }
    }
    
    return null ;
  }
 
  
  //Process remove attachments from reply message  
  public String processDeleteReplyAttach()
  {
    LOG.debug("processDeleteReplyAttach()");
    
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
        int pos = name.lastIndexOf("remsg_current_attach");
        
        if(pos>=0 && name.length()==pos+"remsg_current_attach".length())
        {
          attachId = (String)paramMap.get(key);
          break;
        }
      }
    }
    
    if ((attachId != null) && (!attachId.equals("")))
    {
      for (int i = 0; i < allAttachments.size(); i++)
      {
        if (attachId.equalsIgnoreCase(((Attachment) allAttachments.get(i))
            .getAttachmentId()))
        {
          allAttachments.remove(i);
          break;
        }
      }
    }
    
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
    if (email != null && (!SET_AS_NO.equals(forward)) && (!email.matches(".+@.+\\..+"))){
      setValidEmail(false);
      setErrorMessage("Please provide a valid email address");
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
  private String addFolder;
  private boolean ismutable;
  private int totalMsgInFolder;
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
  
  public int getTotalMsgInFolder()
  {
    return totalMsgInFolder;
  }

  public void setTotalMsgInFolder(int totalMsgInFolder)
  {
    this.totalMsgInFolder = totalMsgInFolder;
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
    
    String typeUuid = getPrivateMessageTypeFromContext(selectedTopicTitle);          
    
    setTotalMsgInFolder(prtMsgManager.findMessageCount(typeUuid));
    
    if(ismutable)
    {
      setErrorMessage("You cann't Revise or Delete this folder.");
      return null;
    }else {
      setErrorMessage("The folder contains messages that will also be deleted! Are you sure you want to delete the following folder and its contents?");
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
      setErrorMessage("Please enter name of folder,which you want to create.");
      return null ;
    } else {
      if(PVTMSG_MODE_RECEIVED.equals(createFolder) || PVTMSG_MODE_SENT.equals(createFolder)|| 
          PVTMSG_MODE_DELETE.equals(createFolder) || PVTMSG_MODE_DRAFT.equals(createFolder))
      {
        setErrorMessage("Please create a different folder name.");
      } else 
      {
        prtMsgManager.createTopicFolderInForum(forum, createFolder);
      //create a typeUUID in commons
      String newTypeUuid= typeManager.getCustomTopicType(createFolder); 
      }
      return "main" ;
    }
  }
  
  private String selectedNewTopicTitle=selectedTopicTitle;  //default
  public String getSelectedNewTopicTitle()
  {
    return selectedNewTopicTitle;
  }
  public void setSelectedNewTopicTitle(String selectedNewTopicTitle)
  {
    this.selectedNewTopicTitle = selectedNewTopicTitle;
  }


  /** 
   * revise
   **/
  public String processPvtMsgFldRevise() 
  {
    LOG.debug("processPvtMsgFldRevise()");
    
    String newTopicTitle = this.getSelectedNewTopicTitle(); 
    
    if(!hasValue(newTopicTitle))
    {
      setErrorMessage("Folder name shouldn't be blank.");
    }
    else {
      prtMsgManager.renameTopicFolder(forum, selectedTopicId,  newTopicTitle);
      //rename topic in commons -- as messages are linked through commons type
      //TODO - what if more than one type objects are returned-- We need to switch from title
      typeManager.renameCustomTopicType(selectedTopicTitle, newTopicTitle);
    }
    
    return "main" ;
  }
  
  //Delete
  public String processPvtMsgFldDelete() 
  {
    LOG.debug("processPvtMsgFldDelete()");
    
    prtMsgManager.deleteTopicFolder(forum,getSelectedTopicId()) ;
    
    //delete the messages
    String typeUuid = getPrivateMessageTypeFromContext(selectedTopicTitle);
    List allPvtMsgs= prtMsgManager.getMessagesByType(typeUuid,PrivateMessageManager.SORT_COLUMN_DATE,
        PrivateMessageManager.SORT_DESC);
    for (Iterator iter = allPvtMsgs.iterator(); iter.hasNext();)
    {
      PrivateMessage element = (PrivateMessage) iter.next();
      prtMsgManager.deletePrivateMessage(element, typeUuid);
    }
    return "main";
  }
  public String processPvtMsgFldAddCancel() 
  {
    LOG.debug("processPvtMsgFldAddCancel()");
    
    return "main";
  }
  
  //create folder within folder
  public String processPvtMsgFolderInFolderAdd()
  {
    LOG.debug("processPvtMsgFolderSettingAdd()");    
    return "pvtMsgFolderInFolderAdd" ;
  }
  //create folder within Folder
  //TODO - add parent fodler id for this 
  public String processPvtMsgFldInFldCreate() 
  {
    LOG.debug("processPvtMsgFldCreate()");
    
    PrivateTopic parentTopic=(PrivateTopic) prtMsgManager.getTopicByUuid(selectedTopicId);
    
    String createFolder=getAddFolder() ;
    if(createFolder == null)
    {
      setErrorMessage("Please enter name of folder,which you want to create.");
      return null ;
    } else {
      if(PVTMSG_MODE_RECEIVED.equals(createFolder) || PVTMSG_MODE_SENT.equals(createFolder)|| 
          PVTMSG_MODE_DELETE.equals(createFolder) || PVTMSG_MODE_DRAFT.equals(createFolder))
      {
        setErrorMessage("Please create a different folder name.");
      } else 
      {
        prtMsgManager.createTopicFolderInTopic(forum, parentTopic, createFolder);
      //create a typeUUID in commons
      String newTypeUuid= typeManager.getCustomTopicType(createFolder); 
      }
      return "main" ;
    }
  }
  ///////////////////// MOVE    //////////////////////
  private String moveToTopic="";
  private String moveToNewTopic="";
  
  public String getMoveToTopic()
  {
    if(hasValue(moveToNewTopic))
    {
      moveToTopic=moveToNewTopic;
    }
    return moveToTopic;
  }
  public void setMoveToTopic(String moveToTopic)
  {
    this.moveToTopic = moveToTopic;
  }

  /**
   * called from Single delete Page
   * @return - pvtMsgMove
   */ 
  public String processPvtMsgMove() {
    LOG.debug("processPvtMsgMove()");
    return "pvtMsgMove";
  }
  
  public void processPvtMsgParentFolderMove(ValueChangeEvent event)
  {
    LOG.debug("processPvtMsgSettingsRevise()"); 
    if ((String)event.getNewValue() != null)
    {
      moveToNewTopic= (String)event.getNewValue();
    }
  }
  
  public String processPvtMsgMoveMessage()
  {
    LOG.debug("processPvtMsgMoveMessage()");
    String moveTopicTitle=getMoveToTopic(); //this is uuid of new topic
    
    Topic newTopic= prtMsgManager.getTopicByUuid(moveTopicTitle);
    Topic oldTopic=selectedTopic.getTopic();
    
    prtMsgManager.movePvtMsgTopic(detailMsg.getMsg(), oldTopic, newTopic);
    
    //reset 
    moveToTopic="";
    moveToNewTopic="";
    
    return "main" ;
  }
  
  /**
   * 
   * @return
   */
  public String processPvtMsgMoveCancel()
  {
    LOG.debug("processPvtMsgMoveCancel()");
    
    return "pvtMsgDetail" ;
  }
  
  ///////////////   SEARCH      ///////////////////////
  private List searchPvtMsgs;
  public List getSearchPvtMsgs()
  {
    if(selectView!=null && selectView.equalsIgnoreCase("threaded"))
    {
        this.rearrageTopicMsgsThreaded(true);
    }
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
//    for (Iterator iter = getDecoratedPvtMsgs().iterator(); iter.hasNext();)
//    {
//      PrivateMessageDecoratedBean element = (PrivateMessageDecoratedBean) iter.next();
//      
//      String message=element.getMsg().getTitle();
//      String searchText = getSearchText();
//      //if search on subject is set - default is true
//      if(searchOnSubject)
//      {
//        StringTokenizer st = new StringTokenizer(message);
//        while (st.hasMoreTokens())
//        {
//          if(st.nextToken().matches("(?i).*getSearchText().*")) //matches anywhere 
//          //if(st.nextToken().equalsIgnoreCase(getSearchText()))            
//          {
//            newls.add(element) ;
//            break;
//          }
//        }
//      }
//      //is search 
//      
//    }

    /**TODO - 
     * In advance srearch as there can be ANY type of combination like selection of 
     * ANY 1 or 2 or 3 or 4 or 5 options like - subject, Author By, label, body and date
     * so all possible cases are taken care off.
     * This doesn't look nice, but good this is that we are using same method - our backend is cleaner
     * First - checked if date is selected then purmutation of 4 options
     * ELSE - permutations of other 4 options
     */ 
    List tempPvtMsgLs= new ArrayList();
    
    if(searchOnDate)
    {
      if((searchFromDate == null) && (searchToDate==null))
      {
        setErrorMessage("Please enter from and to dates for search");
      }
      
      if(searchOnSubject && searchOnAuthor && searchOnBody && searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(1), new Long(1), new Long(1),new Long(1)) ;
      } 
      else if(searchOnSubject && searchOnAuthor && searchOnBody)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(1), new Long(1), new Long(0),new Long(1)) ;
      } 
      else if(searchOnSubject && searchOnAuthor && searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(1), new Long(0), new Long(1),new Long(1)) ;
      }    
      else if(searchOnSubject && searchOnLabel && searchOnBody)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(0), new Long(1), new Long(1),new Long(1)) ;
      }
      else if(searchOnAuthor && searchOnBody && searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(1), new Long(1), new Long(1),new Long(1)) ;
      } 
      else if(searchOnSubject && searchOnAuthor)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(1), new Long(0), new Long(0),new Long(1)) ;
      } 
      else if(searchOnSubject && searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(0), new Long(0), new Long(1),new Long(1)) ;
      } 
      else if(searchOnSubject && searchOnBody)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(0), new Long(1), new Long(0),new Long(1)) ;
      } 
      else if(searchOnAuthor && searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(1), new Long(0), new Long(1),new Long(1)) ;
      }   
      else if(searchOnAuthor && searchOnBody)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(1), new Long(1), new Long(0),new Long(1)) ;
      }
      else if(searchOnBody && searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(0), new Long(1), new Long(1),new Long(1)) ;
      }
      else if(searchOnSubject)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(0),new Long(0), new Long(0),new Long(1)) ;
      } 
      else if (searchOnAuthor)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(1),new Long(0), new Long(0),new Long(1)) ;
      }
      else if (searchOnBody)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(0),new Long(1), new Long(0),new Long(1)) ;
      }    
      else if (searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(0),new Long(0), new Long(1),new Long(1)) ;
      } 
      else
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(0),new Long(0), new Long(0),new Long(1)) ;
      }
    }
    //if date is not selected
    else {
      if(!hasValue(searchText))
      {
        setErrorMessage("Please enter text for search.");
      }
      if(searchOnSubject && searchOnAuthor && searchOnBody && searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(1), new Long(1), new Long(1),new Long(0)) ;
      } 
      else if(searchOnSubject && searchOnAuthor && searchOnBody)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(1), new Long(1), new Long(0),new Long(0)) ;
      } 
      else if(searchOnSubject && searchOnAuthor && searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(1), new Long(0), new Long(1),new Long(0)) ;
      }    
      else if(searchOnSubject && searchOnLabel && searchOnBody)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(0), new Long(1), new Long(1),new Long(0)) ;
      }
      else if(searchOnAuthor && searchOnBody && searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(1), new Long(1), new Long(1),new Long(0)) ;
      } 
      else if(searchOnSubject && searchOnAuthor)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(1), new Long(0), new Long(0),new Long(0)) ;
      } 
      else if(searchOnSubject && searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(0), new Long(0), new Long(1),new Long(0)) ;
      } 
      else if(searchOnSubject && searchOnBody)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(0), new Long(1), new Long(0),new Long(0)) ;
      } 
      else if(searchOnAuthor && searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(1), new Long(0), new Long(1),new Long(0)) ;
      }   
      else if(searchOnAuthor && searchOnBody)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(1), new Long(1), new Long(0),new Long(0)) ;
      }
      else if(searchOnBody && searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(0), new Long(1), new Long(1),new Long(0)) ;
      }
      else if(searchOnSubject)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(1), new Long(0),new Long(0), new Long(0),new Long(0)) ;
      } 
      else if (searchOnAuthor)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(1),new Long(0), new Long(0),new Long(0)) ;
      }
      else if (searchOnBody)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(0),new Long(1), new Long(0),new Long(0)) ;
      }    
      else if (searchOnLabel)
      {
        tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), getSearchText(), getSearchFromDate(), getSearchToDate(),
            new Long(0), new Long(0),new Long(0), new Long(1),new Long(0)) ;
      }        
    }
    
    newls= createDecoratedDisplay(tempPvtMsgLs);
//    
//    for (Iterator iter = tempPvtMsgLs.iterator(); iter.hasNext();)
//    {
//      PrivateMessage element = (PrivateMessage) iter.next();
//      PrivateMessageDecoratedBean dbean = new PrivateMessageDecoratedBean(element);
//      
//      //getRecipients() is filtered for this perticular user i.e. returned list of only one PrivateMessageRecipient object
//      for (Iterator iterator = element.getRecipients().iterator(); iterator.hasNext();)
//      {
//        PrivateMessageRecipient el = (PrivateMessageRecipient) iterator.next();
//        if (el != null){
//          if(!el.getRead().booleanValue())
//          {
//            dbean.setHasRead(el.getRead().booleanValue());
//            break;
//          }
//        }
//      }
//      //Add decorate 'TO' String for sent message
//      if(PVTMSG_MODE_SENT.equals(msgNavMode))
//      {
//        dbean.setSendToStringDecorated(createDecoratedSentToDisplay(dbean)); 
//      }
//      newls.add(dbean);     
//    }
    //set threaded view as  false in search 
    selectView="";
    
    if(newls.size()>0)
    {
      this.setSearchPvtMsgs(newls) ;
      return "pvtMsgEx" ;
    }
    else 
      {
        setErrorMessage("No matching result found");
        return null;
      }    
  }
  
  /**
   * Clear Search text
   */
  public String processClearSearch()
  {
    searchText="";
    if(searchPvtMsgs != null)
    {
      searchPvtMsgs.clear();
      searchPvtMsgs= decoratedPvtMsgs;   
    }
    
    searchOnBody=false ;
    searchOnSubject=true;
    searchOnLabel= false ;
    searchOnAuthor=false;
    searchOnDate=false;
    searchFromDate=null;
    searchToDate=null;
    
    return "pvtMsg";
  }
  
  public boolean searchOnBody=false ;
  public boolean searchOnSubject=true;  //default is search on Subject
  public boolean searchOnLabel= false ;
  public boolean searchOnAuthor=false;
  public boolean searchOnDate=false;
  public Date searchFromDate;
  public Date searchToDate; 
  
  public boolean isSearchOnAuthor()
  {
    return searchOnAuthor;
  }
  public void setSearchOnAuthor(boolean searchOnAuthor)
  {
    this.searchOnAuthor = searchOnAuthor;
  }
  public boolean isSearchOnBody()
  {
    return searchOnBody;
  }
  public void setSearchOnBody(boolean searchOnBody)
  {
    this.searchOnBody = searchOnBody;
  }
  public boolean isSearchOnLabel()
  {
    return searchOnLabel;
  }
  public void setSearchOnLabel(boolean searchOnLabel)
  {
    this.searchOnLabel = searchOnLabel;
  }
  public boolean isSearchOnSubject()
  {
    return searchOnSubject;
  }
  public void setSearchOnSubject(boolean searchOnSubject)
  {
    this.searchOnSubject = searchOnSubject;
  }
  public boolean isSearchOnDate()
  {
    return searchOnDate;
  }
  public void setSearchOnDate(boolean searchOnDate)
  {
    this.searchOnDate = searchOnDate;
  }
  public Date getSearchFromDate()
  {
    return searchFromDate;
  }
  public void setSearchFromDate(Date searchFromDate)
  {
    this.searchFromDate = searchFromDate;
  }
  public Date getSearchToDate()
  {
    return searchToDate;
  }
  public void setSearchToDate(Date searchToDate)
  {
    this.searchToDate = searchToDate;
  }


  //////////////        HELPER      //////////////////////////////////
  /**
   * @return
   */
  private String getExternalParameterByKey(String parameterId)
  {    
    ExternalContext context = FacesContext.getCurrentInstance()
        .getExternalContext();
    Map paramMap = context.getRequestParameterMap();
    
    return (String) paramMap.get(parameterId);    
  }

  
  /**
   * decorated display - from List of Message
   */
  public List createDecoratedDisplay(List msg)
  {
    List decLs= new ArrayList() ;
    for (Iterator iter = msg.iterator(); iter.hasNext();)
    {
      PrivateMessage element = (PrivateMessage) iter.next();                  
      
      PrivateMessageDecoratedBean dbean= new PrivateMessageDecoratedBean(element);
      //if processSelectAll is set, then set isSelected true for all messages,
      if(selectAll)
      {
        dbean.setIsSelected(true);
      }
       
      //getRecipients() is filtered for this perticular user i.e. returned list of only one PrivateMessageRecipient object
      for (Iterator iterator = element.getRecipients().iterator(); iterator.hasNext();)
      {
        PrivateMessageRecipient el = (PrivateMessageRecipient) iterator.next();
        if (el != null){
          dbean.setHasRead(el.getRead().booleanValue());
        }
      }
      //Add decorate 'TO' String for sent message
      if(PVTMSG_MODE_SENT.equals(msgNavMode))
      {
        dbean.setSendToStringDecorated(createDecoratedSentToDisplay(dbean)); 
      }

      decLs.add(dbean) ;
    }
    //reset selectAll flag, for future use
    selectAll=false;
    return decLs;
  }
  
  /**
   * create decorated display for Sent To display for Sent folder
   * @param dbean
   * @return
   */
  public String createDecoratedSentToDisplay(PrivateMessageDecoratedBean dbean)
  {
    String deocratedToDisplay="";
    if(dbean.getMsg().getRecipientsAsText() != null)
    {
      if (dbean.getMsg().getRecipientsAsText().length()>25)
      {
        deocratedToDisplay=(dbean.getMsg().getRecipientsAsText()).substring(0, 20)+" (..)";
      } else
      {
        deocratedToDisplay=dbean.getMsg().getRecipientsAsText();
      }
    }
    return deocratedToDisplay;
  }
  
  /**
   * get recipients
   * @return a set of recipients (User objects)
   */
  private Set getRecipients()
  {     
    List selectedList = getSelectedComposeToList();    
    Set returnSet = new HashSet();
    
    /** get List of unfiltered course members */
    List allCourseUsers = membershipManager.getAllCourseUsers();                                       
                    
    for (Iterator i = selectedList.iterator(); i.hasNext();){
      String selectedItem = (String) i.next();
      
      /** lookup item in map */
      MembershipItem item = (MembershipItem) courseMemberMap.get(selectedItem);
      if (item == null){
        LOG.warn("getRecipients() could not resolve uuid: " + selectedItem);
      }
      else{                              
        if (MembershipItem.TYPE_ALL_PARTICIPANTS.equals(item.getType())){
          for (Iterator a = allCourseUsers.iterator(); a.hasNext();){
            MembershipItem member = (MembershipItem) a.next();            
              returnSet.add(member.getUser());            
          }
        }
        else if (MembershipItem.TYPE_ROLE.equals(item.getType())){
          for (Iterator r = allCourseUsers.iterator(); r.hasNext();){
            MembershipItem member = (MembershipItem) r.next();
            if (member.getRole().equals(item.getRole())){
              returnSet.add(member.getUser());
            }
          }
        }
        else if (MembershipItem.TYPE_GROUP.equals(item.getType())){
          for (Iterator g = allCourseUsers.iterator(); g.hasNext();){
            MembershipItem member = (MembershipItem) g.next();            
            Set groupMemberSet = item.getGroup().getMembers();
            for (Iterator s = groupMemberSet.iterator(); s.hasNext();){
              Member m = (Member) s.next();
              if (m.getUserId() != null && m.getUserId().equals(member.getUser().getId())){
                returnSet.add(member.getUser());
              }
            }            
          }
        }
        else if (MembershipItem.TYPE_USER.equals(item.getType())){
          returnSet.add(item.getUser());
        } 
        else{
          LOG.warn("getRecipients() could not resolve membership type: " + item.getType());
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
      MembershipItem item = (MembershipItem) i.next();      
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
      MembershipItem item = (MembershipItem) i.next();
      if (item.getRole().getId().equalsIgnoreCase(roleName)){
        returnSet.add(item.getUser());   
      }
    }    
    return returnSet;    
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
      return typeManager.getCustomTopicType(navMode);
    }    
  }

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
  
  /**
   * @param errorMsg
   */
  private void setErrorMessage(String errorMsg)
  {
    LOG.debug("setErrorMessage(String " + errorMsg + ")");
    FacesContext.getCurrentInstance().addMessage(null,
        new FacesMessage("Alert: " + errorMsg));
  }
  
  /**
   * Enable privacy message
   * @return
   */
  public boolean getRenderPrivacyAlert()
  {
   if(ServerConfigurationService.getString(MESSAGECENTER_PRIVACY_TEXT)!=null &&
       ServerConfigurationService.getString(MESSAGECENTER_PRIVACY_TEXT).trim().length()>0 )
   {
     return true;
   }
    return false;
  }
  
  /**
   * Get Privacy message link  from sakai.properties
   * @return
   */
  public String getPrivacyAlertUrl()
  {
    return ServerConfigurationService.getString(MESSAGECENTER_PRIVACY_URL);
  }
  
  /**
   * Get Privacy message from sakai.properties
   * @return
   */
  public String getPrivacyAlert()
  {
    return ServerConfigurationService.getString(MESSAGECENTER_PRIVACY_TEXT);
  }
  
  public boolean isAtMain(){
    HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    
    String servletPath = req.getServletPath();
    if (servletPath != null){
      if (servletPath.startsWith("/jsp/main")){
        return true;
      }      
    }
    return false;
  }


	public String getSortType() {
		return sortType;
	}


	public void setSortType(String sortType) {
		this.sortType = sortType;
	}



}