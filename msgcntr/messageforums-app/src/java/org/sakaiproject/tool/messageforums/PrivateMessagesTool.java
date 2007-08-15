/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/PrivateMessagesTool.java $
 * $Id: PrivateMessagesTool.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
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
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.app.messageforums.MembershipItem;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateTopicImpl;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.messageforums.ui.DecoratedAttachment;
import org.sakaiproject.tool.messageforums.ui.PrivateForumDecoratedBean;
import org.sakaiproject.tool.messageforums.ui.PrivateMessageDecoratedBean;
import org.sakaiproject.tool.messageforums.ui.PrivateTopicDecoratedBean;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.ResourceLoader;

public class PrivateMessagesTool
{
  
  private static final Log LOG = LogFactory.getLog(PrivateMessagesTool.class);

  private static final String MESSAGECENTER_PRIVACY_URL = "messagecenter.privacy.url";
  private static final String MESSAGECENTER_PRIVACY_TEXT = "messagecenter.privacy.text";

  private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
 
  private static final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);
  
  /**
   * List individual private messages details
   */
  private static final String REPLY_SUBJECT_PREFIX = "pvt_reply_prefix";
  private static final String FORWARD_SUBJECT_PREFIX = "pvt_forward_prefix";
  private static final String ALERT = "pvt_alert";
  private static final String NO_MATCH_FOUND = "pvt_no_match_found";
  private static final String MISSING_BEG_END_DATE = "pvt_missing_date_range";
  private static final String CREATE_DIFF_FOLDER_NAME = "pvt_create_diff_folder_name";
  private static final String FOLDER_NAME_BLANK = "pvt_folder_name_blank";
  private static final String ENTER_FOLDER_NAME = "pvt_enter_new_folder_name";
  private static final String CONFIRM_FOLDER_DELETE = "pvt_delete_folder_confirm";
  private static final String CANNOT_DEL_REVISE_FOLDER = "pvt_no_delete_revise_folder";
  private static final String PROVIDE_VALID_EMAIL = "pvt_provide_email_addr";
  private static final String CONFIRM_PERM_MSG_DELETE = "pvt_confirm_perm_msg_delete";
  private static final String SELECT_MSGS_TO_DELETE = "pvt_select_msgs_to_delete";
  private static final String SELECT_RECIPIENT_LIST_FOR_REPLY = "pvt_select_reply_recipients_list";
  private static final String MISSING_SUBJECT = "pvt_missing_subject";
  private static final String SELECT_MSG_RECIPIENT = "pvt_select_msg_recipient";
  private static final String CONFIRM_MSG_DELETE = "pvt_confirm_msg_delete";
  private static final String ENTER_SEARCH_TEXT = "pvt_enter_search_text";
  private static final String MOVE_MSG_ERROR = "pvt_move_msg_error";
  private static final String NO_MARKED_READ_MESSAGE = "pvt_no_message_mark_read";
  private static final String NO_MARKED_DELETE_MESSAGE = "pvt_no_message_mark_delete";
  private static final String NO_MARKED_MOVE_MESSAGE = "pvt_no_message_mark_move";
  private static final String MULTIDELETE_SUCCESS_MSG = "pvt_deleted_success";
  private static final String PERM_DELETE_SUCCESS_MSG = "pvt_perm_deleted_success";
  
  /** Used to determine if this is combined tool or not */
  private static final String MESSAGECENTER_TOOL_ID = "sakai.messagecenter";
  private static final String MESSAGECENTER_HELPER_TOOL_ID = "sakai.messageforums.helper";
  private static final String MESSAGES_TOOL_ID = "sakai.messages";
  private static final String FORUMS_TOOL_ID = "sakai.forums";
  
  /**
   *Dependency Injected 
   */
  private PrivateMessageManager prtMsgManager;
  private MessageForumsMessageManager messageManager;
  private MessageForumsForumManager forumManager;
  private ErrorMessages errorMessages;
  private MembershipManager membershipManager;
    
  /** Dependency Injected   */
  private MessageForumsTypeManager typeManager;
 
  /** Naigation for JSP   */
  public static final String MAIN_PG="main";
  public static final String DISPLAY_MESSAGES_PG="pvtMsg";
  public static final String SELECTED_MESSAGE_PG="pvtMsgDetail";
  public static final String COMPOSE_MSG_PG="compose";
  public static final String COMPOSE_FROM_PG="msgForum:mainOrHp";
  public static final String MESSAGE_SETTING_PG="pvtMsgSettings";
  public static final String MESSAGE_FOLDER_SETTING_PG="pvtMsgFolderSettings";
  public static final String SEARCH_RESULT_MESSAGES_PG="pvtMsgEx";
  public static final String DELETE_MESSAGES_PG="pvtMsgDelete";
  public static final String DELETE_FOLDER_PG="pvtMsgFolderDelete";
  public static final String MESSAGE_STATISTICS_PG="pvtMsgStatistics";
  public static final String MESSAGE_HOME_PG="pvtMsgHpView";
  public static final String MESSAGE_REPLY_PG="pvtMsgReply";
  //SAKAI-10505
  public static final String MESSAGE_FORWARD_PG="pvtMsgForward";
  public static final String DELETE_MESSAGE_PG="pvtMsgDelete";
  public static final String REVISE_FOLDER_PG="pvtMsgFolderRevise";
  public static final String MOVE_MESSAGE_PG="pvtMsgMove";
  public static final String ADD_FOLDER_IN_FOLDER_PG="pvtMsgFolderInFolderAdd";
  public static final String ADD_MESSAGE_FOLDER_PG="pvtMsgFolderAdd";
  public static final String PVTMSG_COMPOSE = "pvtMsgCompose";
  
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
  
  public static final String THREADED_VIEW = "threaded";
  
  PrivateForumDecoratedBean decoratedForum;
  
  private Area area;
  private PrivateForum forum;  
  private List pvtTopics=new ArrayList();
  private List decoratedPvtMsgs;
  private String msgNavMode="privateMessages" ;
  private PrivateMessageDecoratedBean detailMsg ;
  private boolean viewChanged = false;
  
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
  private String composeBody;
  private String selectedLabel="Normal" ;   //defautl set
  private List totalComposeToList;
  private List totalComposeToListRecipients;
  
  //Delete items - Checkbox display and selection - Multiple delete
  private List selectedDeleteItems;
  private boolean multiDeleteSuccess;
  private String multiDeleteSuccessMsg;
  private List totalDisplayItems=new ArrayList();
  
  // Move to folder - Checkbox display and selection - Multiple move to folder
  private List selectedMoveToFolderItems;
  
  //reply to 
  private String replyToBody;
  private String replyToSubject;
  
  //forwarding
  private String forwardBody;
  private String forwardSubject;
  
  //Setting Screen
  private String activatePvtMsg=SET_AS_NO; 
  private String forwardPvtMsg=SET_AS_NO;
  private String forwardPvtMsgEmail;
  private boolean superUser; 
  
  //message header screen
  private String searchText="";
  private String selectView;
  
  //return to previous page after send msg
  private String fromMainOrHp = null;
  
  // for compose, are we coming from main page?
  private boolean fromMain;
  
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

  public void initializePrivateMessageArea()
  {           
    /** get area per request */
    area = prtMsgManager.getPrivateMessageArea();
    
    if (! area.getEnabled() && isMessages()) {
    	area.setEnabled(true);
    }
    
    if (getPvtAreaEnabled() || isInstructor()){      
      PrivateForum pf = prtMsgManager.initializePrivateMessageArea(area);
      pf = prtMsgManager.initializationHelper(pf, area);
      pvtTopics = pf.getTopics();
      Collections.sort(pvtTopics, PrivateTopicImpl.TITLE_COMPARATOR);   //changed to date comparator
      forum=pf;           
      activatePvtMsg = (Boolean.TRUE.equals(area.getEnabled())) ? SET_AS_YES : SET_AS_NO;
      forwardPvtMsg = (Boolean.TRUE.equals(pf.getAutoForward())) ? SET_AS_YES : SET_AS_NO;
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
  	if (!FacesContext.getCurrentInstance().getRenderResponse() && !viewChanged){
  		return decoratedPvtMsgs;
  	}
  	
	if(selectView!=null && selectView.equalsIgnoreCase(THREADED_VIEW))
    {
    	this.rearrageTopicMsgsThreaded(false);
    	return decoratedPvtMsgs;
    }
  	  	  	
  	decoratedPvtMsgs=new ArrayList() ;

  	String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);

  	/** support for sorting */
  	/* if the view was changed to "All Messages", we want to retain the previous
  	 * sort setting. Otherwise, the user has selected a different sort setting.
  	 * Also retain sort setting if user has hit "Check All"
  	 */
  	if ((!viewChanged || sortType == null) && !selectAll)
  	{
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
  	}

  	viewChanged = false; 
    
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
        // the recipient list is stored as a CLOB in Oracle, so we cannot use the
    	// db query to obtain the sorted list - cannot order by CLOB
    	/*decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_TO,
          PrivateMessageManager.SORT_ASC);*/
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
    	          PrivateMessageManager.SORT_ASC);
    	Collections.sort(decoratedPvtMsgs, PrivateMessageImpl.RECIPIENT_LIST_COMPARATOR_ASC);
    }        
    else if (SORT_TO_DESC.equals(sortType)){
    	// the recipient list is stored as a CLOB in Oracle, so we cannot use the
    	// db query to obtain the sorted list - cannot order by CLOB
    	/*decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_TO,
          PrivateMessageManager.SORT_DESC);*/
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
    	          PrivateMessageManager.SORT_ASC);
    	Collections.sort(decoratedPvtMsgs, PrivateMessageImpl.RECIPIENT_LIST_COMPARATOR_DESC);
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
   		setMessageBeanPreNextStatus();
    }
    
    return decoratedPvtMsgs ;
  }
  
  private void setMessageBeanPreNextStatus()
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
  
  private String getSiteId() {
	  return ToolManager.getCurrentPlacement().getContext();
  }
    
  private String getContextSiteId() 
  {
	 return "/site/" + ToolManager.getCurrentPlacement().getContext();
  }
  
  public List getTotalComposeToList()
  { 
  	/** just need to refilter */
    if (totalComposeToList != null) {
    	
  		List selectItemList = new ArrayList();
        
   		for (Iterator i = totalComposeToList.iterator(); i.hasNext();) {
   			MembershipItem item = (MembershipItem) i.next();

   			if (isInstructor() || item.isViewable()) {
   				selectItemList.add(new SelectItem(item.getId(), item.getName()));
   			}
   		}
    		
   		return selectItemList;       
    }
    
    totalComposeToListRecipients = new ArrayList();
 
    courseMemberMap = membershipManager.getFilteredCourseMembers(true);
//    courseMemberMap = membershipManager.getAllCourseMembers(true, true, true);
    List members = membershipManager.convertMemberMapToList(courseMemberMap);

    Set memberIds = new HashSet();
    
    for (Iterator i = members.iterator(); i.hasNext();){       
        MembershipItem item = (MembershipItem) i.next();
 
        String name = item.getName();
        
       	memberIds.add(item.getId());
    }

    totalComposeToList = members;
    
    List selectItemList = new ArrayList();
    
	for (Iterator i = members.iterator(); i.hasNext();) {

		MembershipItem item = (MembershipItem) i.next();

		if (isInstructor() || item.isViewable()) {
			selectItemList.add(new SelectItem(item.getId(), item.getName()));
		}
	}

	return selectItemList;       
  }
  
  /**
   * 
   * @param id
   * @return
   */
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
  
  // Forward a message
  public String getForwardBody() {
    return forwardBody;
  }
  public void setForwardBody(String forwardBody) {
    this.forwardBody=forwardBody;
  }
  public String getForwardSubject()
  {
    return forwardSubject;
  }
  public void setForwardSubject(String forwardSubject)
  {
    this.forwardSubject = forwardSubject;
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
  
  public boolean isMultiDeleteSuccess() 
  {
	return multiDeleteSuccess;
  }

  public void setMultiDeleteSuccess(boolean multiDeleteSuccess) 
  {
	this.multiDeleteSuccess = multiDeleteSuccess;
  }


  public String getMultiDeleteSuccessMsg() 
  {
	return multiDeleteSuccessMsg;
  }

  public void setMultiDeleteSuccessMsg(String multiDeleteSuccessMsg) 
  {
	this.multiDeleteSuccessMsg = multiDeleteSuccessMsg;
  }

public boolean isFromMain() {
	return fromMain;
}

public String getServerUrl() {
    return ServerConfigurationService.getServerUrl();
 }

public void processChangeSelectView(ValueChangeEvent eve)
  {
    multiDeleteSuccess = false;
    String currentValue = (String) eve.getNewValue();
  	if (!currentValue.equalsIgnoreCase(THREADED_VIEW) && selectView != null && selectView.equals(THREADED_VIEW))
  	{
  		selectView = "";
  		viewChanged = true;
  		getDecoratedPvtMsgs();
  		return;
    }
  	else if (currentValue.equalsIgnoreCase(THREADED_VIEW))
  	{
  		selectView = THREADED_VIEW;
  		if (searchPvtMsgs != null && !searchPvtMsgs.isEmpty())
  			this.rearrageTopicMsgsThreaded(true);
  		else
  			this.rearrageTopicMsgsThreaded(false);
  		return;
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
		  // always start with the decorated pm in ascending date order
		  String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);
		  decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
		          PrivateMessageManager.SORT_ASC);
		  decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);
		  
		  for(int i=0; i<decoratedPvtMsgs.size(); i++)
		  {
			  msgsList.add((PrivateMessageDecoratedBean)decoratedPvtMsgs.get(i));
		  }   
		  decoratedPvtMsgs.clear();
	  }


	  if(msgsList != null)
	  {
		  List tempMsgsList = new ArrayList();
		  for(int i=0; i<msgsList.size(); i++)
		  {
			  tempMsgsList.add((PrivateMessageDecoratedBean)msgsList.get(i));
		  }
		  Iterator iter = tempMsgsList.iterator();
		  while(iter.hasNext())
		  {
			  List allRelatedMsgs = messageManager.getAllRelatedMsgs(
					  ((PrivateMessageDecoratedBean)iter.next()).getMsg().getId());
			  List currentRelatedMsgs = new ArrayList();
			  if(allRelatedMsgs != null && allRelatedMsgs.size()>0)
			  {
				  Long msgId = ((Message)allRelatedMsgs.get(0)).getId();
				  PrivateMessage pvtMsg= (PrivateMessage) prtMsgManager.getMessageById(msgId);
				  PrivateMessageDecoratedBean pdb = new PrivateMessageDecoratedBean(pvtMsg);
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

				  tempMsgsList.remove((PrivateMessageDecoratedBean)currentRelatedMsgs.get(i));
			  }

			  iter = tempMsgsList.iterator();
		  }
	  }

	  setMessageBeanPreNextStatus();

  }
  
  private void recursiveGetThreadedMsgsFromList(List msgsList, 
  		List allRelatedMsgs, List returnList,
      PrivateMessageDecoratedBean currentMsg)
  {
    for (int i = 0; i < allRelatedMsgs.size(); i++)
    {
      Long msgId = ((Message)allRelatedMsgs.get(i)).getId();
	  PrivateMessage pvtMsg= (PrivateMessage) prtMsgManager.getMessageById(msgId);
	  PrivateMessageDecoratedBean thisMsgBean = new PrivateMessageDecoratedBean(pvtMsg);

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
  public void setSelectedTopicId(String selectedTopicId)
  {
    this.selectedTopicId=selectedTopicId;    
  }
  
  public String processActionHome()
  {
    LOG.debug("processActionHome()");
    msgNavMode = "privateMessages";
    multiDeleteSuccess = false;
    if (searchPvtMsgs != null)
    	searchPvtMsgs.clear();
    return  MAIN_PG;
  }  
  public String processActionPrivateMessages()
  {
    LOG.debug("processActionPrivateMessages()");                    
    msgNavMode = "privateMessages";            
    multiDeleteSuccess = false;
    if (searchPvtMsgs != null) 
    	searchPvtMsgs.clear();
    return  MESSAGE_HOME_PG;
  }        
  public String processDisplayForum()
  {
    LOG.debug("processDisplayForum()");
    if (searchPvtMsgs != null)
    	searchPvtMsgs.clear();
    return DISPLAY_MESSAGES_PG;
  }
 //SAKAI-10505 
  public String processDisplayMessages()
  {
    LOG.debug("processDisplayMessages()");
    if (searchPvtMsgs != null)
    	searchPvtMsgs.clear();
    return SELECTED_MESSAGE_PG;
  }
  public String processPvtMsgTopic()
  {
    LOG.debug("processPvtMsgTopic()");
    
    /** reset sort type */
    sortType = SORT_DATE_DESC;    
    
    //get external parameter
    //selectedTopicTitle = getExternalParameterByKey("pvtMsgTopicTitle") ;
    selectedTopicTitle = forumManager.getTopicByUuid(getExternalParameterByKey("pvtMsgTopicId")).getTitle();
    setSelectedTopicId(getExternalParameterByKey("pvtMsgTopicId")) ;
    msgNavMode=getSelectedTopicTitle();

    //set prev/next topic details
    setPrevNextTopicDetails(msgNavMode);
    
    return DISPLAY_MESSAGES_PG;
  }
    
  /**
   * process Cancel from all JSP's
   * @return - pvtMsg
   */  
  public String processPvtMsgCancel() {
    LOG.debug("processPvtMsgCancel()");
    
    // Return to Messages & Forums page or Messages page
    if (isMessagesandForums()) {
    	return MAIN_PG;
    }
    else {
    	return MESSAGE_HOME_PG;
    }
  }
  
  public String processPvtMsgCancelToListView()
  {
  	return DISPLAY_MESSAGES_PG;
  }

  /**
   * called when subject of List of messages to Topic clicked for detail
   * @return - pvtMsgDetail
   */ 
  public String processPvtMsgDetail() {
    LOG.debug("processPvtMsgDetail()");
    multiDeleteSuccess = false;

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
    return SELECTED_MESSAGE_PG;
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
    	if(getDetailMsg().getMsg().getTitle() != null && !getDetailMsg().getMsg().getTitle().startsWith(getResourceBundleString(REPLY_SUBJECT_PREFIX)))
    		replyToSubject = getResourceBundleString(REPLY_SUBJECT_PREFIX) + ' ' + getDetailMsg().getMsg().getTitle();
    	else
    		replyToSubject = getDetailMsg().getMsg().getTitle();
    }
    
    //from message detail screen
    this.setDetailMsg(getDetailMsg()) ;
//    
//    //from compose screen
//    this.setComposeSendAs(getComposeSendAs()) ;
//    this.setTotalComposeToList(getTotalComposeToList()) ;
//    this.setSelectedComposeToList(getSelectedComposeToList()) ;
    
    return MESSAGE_REPLY_PG;
  }
  
  
  /**
   * navigate to "forward" a private message
   * @return - pvtMsgForward
   */ 
  public String processPvtMsgForward() {
	    LOG.debug("processPvtMsgForward()");
	    if (getDetailMsg() == null)
	    	return null;
	    
	    PrivateMessage pm = getDetailMsg().getMsg();
	    
	    String title = pm.getTitle();
    	if(title != null && !title.startsWith(getResourceBundleString(FORWARD_SUBJECT_PREFIX)))
    		forwardSubject = getResourceBundleString(FORWARD_SUBJECT_PREFIX) + ' ' + title;
    	else
    		forwardSubject = title;

    	// format the created date according to the setting in the bundle
	    SimpleDateFormat formatter = new SimpleDateFormat(getResourceBundleString("date_format"));
		formatter.setTimeZone(TimeService.getLocalTimeZone());
		String formattedCreateDate = formatter.format(pm.getCreated());
		
		StringBuffer forwardedText = new StringBuffer();
	    
	    // populate replyToBody with the forwarded text
		forwardedText.append(getResourceBundleString("pvt_msg_fwd_heading") + "<br /><br />" +
	    	getResourceBundleString("pvt_msg_fwd_authby", new Object[] {pm.getAuthor(), formattedCreateDate}) +  "<br />" +
	    	getResourceBundleString("pvt_msg_fwd_to", new Object[] {pm.getRecipientsAsText()}) + "<br />" +
	    	getResourceBundleString("pvt_msg_fwd_subject", new Object[] {pm.getTitle()}) + "<br />" +
	    	getResourceBundleString("pvt_msg_fwd_label", new Object[] {pm.getLabel()}) + "<br />");
	    
	    List attachList = getDetailMsg().getAttachList();
	    if (attachList != null && attachList.size() > 0) {
	    	forwardedText.append(getResourceBundleString("pvt_msg_fwd_attachments") + "<br />");
	    	forwardedText.append("<ul style=\"list-style-type:none;margin:0;padding:0;padding-left:0.5em;\">");
	    	for (Iterator attachIter = attachList.iterator(); attachIter.hasNext();) {
	    		DecoratedAttachment decoAttach = (DecoratedAttachment) attachIter.next();
	    		if (decoAttach != null) {
	    			forwardedText.append("<li>");
	    			// It seems like there must be a better way to do the attachment image...
	    			String fileType = decoAttach.getAttachment().getAttachmentType();
	    			String imageUrl = null;
	    			if (fileType.equalsIgnoreCase("application/vnd.ms-excel"))
	    				imageUrl = "/sakai-messageforums-tool/images/excel.gif";
	    			else if (fileType.equalsIgnoreCase("text/html"))
	    				imageUrl = "/sakai-messageforums-tool/images/html.gif";
	    			else if (fileType.equalsIgnoreCase("application/pdf"))
	    				imageUrl = "/sakai-messageforums-tool/images/pdf.gif";
	    			else if (fileType.equalsIgnoreCase("application/vnd.ms-powerpoint"))
	    				imageUrl = "/sakai-messageforums-tool/images/ppt.gif";
	    			else if (fileType.equalsIgnoreCase("text/plain"))
	    				imageUrl = "/sakai-messageforums-tool/images/text.gif";
	    			else if (fileType.equalsIgnoreCase("application/msword"))
	    				imageUrl = "/sakai-messageforums-tool/images/word.gif";
	    			
	    			if (imageUrl != null) {
	    				forwardedText.append("<img alt=\"\" src=\"" + imageUrl + "\" />");
	    			}
	    			
	    			forwardedText.append("<a href=\"" + decoAttach.getUrl() + "\" target=\"_blank\">" + decoAttach.getAttachment().getAttachmentName() + "</a></li>");
	    		}
	    	}
	    	forwardedText.append("</ul>");
	    }
	    String origBody = pm.getBody();
	    if (origBody != null && origBody.trim().length() > 0) {
	    	forwardedText.append(pm.getBody());
	    }
	    
	    this.setForwardBody(forwardedText.toString());
	    //from message detail screen
	    this.setDetailMsg(getDetailMsg()) ;

	    return MESSAGE_FORWARD_PG;
	  }
	  
  /**
   * called from Single delete Page
   * @return - pvtMsgDetail
   */ 
  public String processPvtMsgDeleteConfirm() {
    LOG.debug("processPvtMsgDeleteConfirm()");
    
    this.setDeleteConfirm(true);
    setErrorMessage(getResourceBundleString(CONFIRM_MSG_DELETE));
    /*
     * same action is used for delete..however if user presses some other action after first
     * delete then 'deleteConfirm' boolean is reset
     */
    return SELECTED_MESSAGE_PG ;
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
    return DISPLAY_MESSAGES_PG ;
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
    setFromMainOrHp();
    fromMain = (msgNavMode == "") || (msgNavMode == "privateMessages");
    LOG.debug("processPvtMsgCompose()");
    return PVTMSG_COMPOSE;
  }
  
  
  public String processPvtMsgComposeCancel()
  {
    LOG.debug("processPvtMsgComposeCancel()");
    resetComposeContents();
    if(("privateMessages").equals(getMsgNavMode()))
    {
    	return processPvtMsgReturnToMainOrHp();
    }
    else
    {
      return DISPLAY_MESSAGES_PG;
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
      setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
      return null;
    }

    if(getSelectedComposeToList().size()<1)
    {
      setErrorMessage(getResourceBundleString(SELECT_MSG_RECIPIENT));
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

    EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_ADD, getEventMessage(pMsg), false));

    if(fromMainOrHp != null && !fromMainOrHp.equals(""))
    {
    	String tmpBackPage = fromMainOrHp;
    	fromMainOrHp = "";
    	return tmpBackPage;
    }
    else if(selectedTopic != null)
    {
    	msgNavMode=getSelectedTopicTitle();
    	setPrevNextTopicDetails(msgNavMode);

    	return DISPLAY_MESSAGES_PG;
    }

    // Return to Messages & Forums page or Messages page
    if (isMessagesandForums()) {
    	return MAIN_PG;
    }
    else {
    	return MESSAGE_HOME_PG;
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
      setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
      return null ;
    }
//    if(!hasValue(getComposeBody()) )
//    {
//      setErrorMessage("Please enter message body for this compose message.");
//      return null ;
//    }
    if(getSelectedComposeToList().size()<1)
    {
      setErrorMessage(getResourceBundleString(SELECT_MSG_RECIPIENT));
      return null ;
    }
    
    PrivateMessage dMsg=constructMessage() ;
    dMsg.setDraft(Boolean.TRUE);
    dMsg.setDeleted(Boolean.FALSE);
    
    if((SET_AS_YES).equals(getComposeSendAsPvtMsg()))
    {
      prtMsgManager.sendPrivateMessage(dMsg, getRecipients(), false); 
    }

    //reset contents
    resetComposeContents();
    
    if(getMsgNavMode().equals(""))
    {
    	// Return to Messages & Forums page or Messages page
        if (isMessagesandForums()) {
        	return MAIN_PG;
        }
        else {
        	return MESSAGE_HOME_PG;
        }
    }
    else
    {
      return DISPLAY_MESSAGES_PG;
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
      
      aMsg.setAuthor(getAuthorString());
      aMsg.setDraft(Boolean.FALSE);      
      aMsg.setApproved(Boolean.FALSE);     
      aMsg.setLabel(getSelectedLabel());
      
      // this property added so can delete forum messages
      // since that and PM share same message object and
      // delete is not null....
      aMsg.setDeleted(Boolean.FALSE);
      
      // Add the recipientList as String for display in Sent folder
      // Any hidden users will be tacked on at the end
      String sendToString="";
      String sendToHiddenString = "";
      
      if (selectedComposeToList.size() == 1) {
          MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(0));
          if (membershipItem != null) {
        	  sendToString +=membershipItem.getName()+"; " ;
          }
      }
      else {
    	  for (int i = 0; i < selectedComposeToList.size(); i++)
    	  {
    		  MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(i));  
    		  if(membershipItem != null)
    		  {
    			  if (membershipItem.isViewable()) {
    				  sendToString +=membershipItem.getName()+"; " ;
    			  }
    			  else {
    				  sendToHiddenString += membershipItem.getName() + "; ";
    			  }
    		  }
    	  }
      }

      if (! "".equals(sendToString)) {
    	  sendToString=sendToString.substring(0, sendToString.length()-2); //remove last comma and space
      }
      
      if ("".equals(sendToHiddenString)) {
    	  aMsg.setRecipientsAsText(sendToString);
      }
      else {
    	  sendToHiddenString=sendToHiddenString.substring(0, sendToHiddenString.length()-2); //remove last comma and space
    	  aMsg.setRecipientsAsText(sendToString + " (" + sendToHiddenString + ")");
      }
      
    }
    //Add attachments
    for(int i=0; i<attachments.size(); i++)
    {
      prtMsgManager.addAttachToPvtMsg(aMsg, ((DecoratedAttachment)attachments.get(i)).getAttachment());         
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
	multiDeleteSuccess = false;
    String prevTopicTitle = getExternalParameterByKey("previousTopicTitle");
    if(hasValue(prevTopicTitle))
    {
      msgNavMode=prevTopicTitle;
      
      decoratedPvtMsgs=new ArrayList() ;
      
      String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);        
      
      decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
          PrivateMessageManager.SORT_DESC);
      
      decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);
      
      if(selectView!=null && selectView.equalsIgnoreCase(THREADED_VIEW))
      {
      	this.rearrageTopicMsgsThreaded(false);
      }

      //set prev/next Topic
      setPrevNextTopicDetails(msgNavMode);
      //set prev/next message
      setPrevNextMessageDetails();
      
      if (searchPvtMsgs != null)
      {
    	  searchPvtMsgs.clear();
    	  return DISPLAY_MESSAGES_PG;
      }
    }
    return null;
  }
  
  /**
   * processDisplayNextFolder()
   */
  public String processDisplayNextTopic()
  { 
	multiDeleteSuccess = false;
    String nextTitle = getExternalParameterByKey("nextTopicTitle");
    if(hasValue(nextTitle))
    {
      msgNavMode=nextTitle;
      decoratedPvtMsgs=new ArrayList() ;
      
      String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);        
      
      decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
          PrivateMessageManager.SORT_DESC);
      
      decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);
      
      if(selectView!=null && selectView.equalsIgnoreCase(THREADED_VIEW))
      {
      	this.rearrageTopicMsgsThreaded(false);
      }

      //set prev/next Topic
      setPrevNextTopicDetails(msgNavMode);
      //set prev/next message
      setPrevNextMessageDetails();
      
      if (searchPvtMsgs != null)
      {
    	  searchPvtMsgs.clear();
    	  return DISPLAY_MESSAGES_PG;
      }
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
      return DISPLAY_MESSAGES_PG;
    }
    return SELECTED_MESSAGE_PG;
  }
  
  //////////////////////REPLY SEND  /////////////////
  public String processPvtMsgReplySend() {
    LOG.debug("processPvtMsgReplySend()");
    
    PrivateMessage currentMessage = getDetailMsg().getMsg() ;
        
    //by default add user who sent original message    
    for (Iterator i = totalComposeToList.iterator(); i.hasNext();) {      
      MembershipItem membershipItem = (MembershipItem) i.next();                
      
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
      setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
      return null ;
    }

    if(selectedComposeToList.size()<1)
    {
      setErrorMessage(getResourceBundleString(SELECT_RECIPIENT_LIST_FOR_REPLY));
      return null ;
    }
        
    PrivateMessage rrepMsg = messageManager.createPrivateMessage() ;
       
    rrepMsg.setTitle(getReplyToSubject()) ; //rrepMsg.setTitle(rMsg.getTitle()) ;
    rrepMsg.setDraft(Boolean.FALSE);
    rrepMsg.setDeleted(Boolean.FALSE);
    
    rrepMsg.setAuthor(getAuthorString());
    rrepMsg.setApproved(Boolean.FALSE);
    rrepMsg.setBody(getReplyToBody()) ;
    
    rrepMsg.setLabel(getSelectedLabel());
    
    rrepMsg.setInReplyTo(currentMessage) ;
    
    //Add the recipientList as String for display in Sent folder
    // Since some users may be hidden, if some of these are recipients
    // filter them out (already checked if no recipients)
    // if only 1 recipient no need to check visibility
    String sendToString="";
    String sendToHiddenString="";
    
    if (selectedComposeToList.size() == 1) {
        MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(0));
        if(membershipItem != null)
        {
      		  sendToString +=membershipItem.getName()+"; " ;
        }          
    }
    else {
    	for (int i = 0; i < selectedComposeToList.size(); i++)
    	{
    		MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(i));
    		if(membershipItem != null)
    		{
    			if (membershipItem.isViewable()) {
    				sendToString +=membershipItem.getName()+"; " ;
    			}
   		       	else {
   	        		sendToHiddenString += membershipItem.getName() + "; ";
   	        	}
   	        }          
    	}
    }

    if (! "".equals(sendToString)) {
  	  sendToString=sendToString.substring(0, sendToString.length()-2); //remove last comma and space
    }

    if ("".equals(sendToHiddenString)) {
        rrepMsg.setRecipientsAsText(sendToString);
    }
    else {
    	sendToHiddenString=sendToHiddenString.substring(0, sendToHiddenString.length()-2); //remove last comma and space    
    	rrepMsg.setRecipientsAsText(sendToString + " (" + sendToHiddenString + ")");
    }    
    
    //Add attachments
    for(int i=0; i<allAttachments.size(); i++)
    {
      prtMsgManager.addAttachToPvtMsg(rrepMsg, ((DecoratedAttachment)allAttachments.get(i)).getAttachment());         
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
    
    EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_RESPONSE, getEventMessage(rrepMsg), false));

    return DISPLAY_MESSAGES_PG;

  }
  
  //////////////////////Forward SEND  /////////////////
  public String processPvtMsgForwardSend() {
    LOG.debug("processPvtMsgForwardSend()");
    
    PrivateMessage currentMessage = getDetailMsg().getMsg() ;
  
    if(!hasValue(getForwardSubject()))
    {
      setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
      return null ;
    }
    
    if(getSelectedComposeToList().size()<1)
    {
      setErrorMessage(getResourceBundleString(SELECT_MSG_RECIPIENT));
      return null ;
    }

        
    PrivateMessage rrepMsg = messageManager.createPrivateMessage() ;
       
    rrepMsg.setTitle(getForwardSubject()) ; 
    rrepMsg.setDraft(Boolean.FALSE);
    rrepMsg.setDeleted(Boolean.FALSE);
    
    rrepMsg.setAuthor(getAuthorString());
    rrepMsg.setApproved(Boolean.FALSE);
    rrepMsg.setBody(getForwardBody()) ;
    
    rrepMsg.setLabel(getSelectedLabel());
    
    rrepMsg.setInReplyTo(currentMessage) ;
    
    //Add the recipientList as String for display in Sent folder
    // Since some users may be hidden, if some of these are recipients
    // filter them out (already checked if no recipients)
    // if only 1 recipient no need to check visibility
    String sendToString="";
    String sendToHiddenString="";
    
    if (selectedComposeToList.size() == 1) {
        MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(0));
        if(membershipItem != null)
        {
      		  sendToString +=membershipItem.getName()+"; " ;
        }          
    }
    else {
    	for (int i = 0; i < selectedComposeToList.size(); i++)
    	{
    		MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(i));
    		if(membershipItem != null)
    		{
    			if (membershipItem.isViewable()) {
    				sendToString +=membershipItem.getName()+"; " ;
    			}
   		       	else {
   	        		sendToHiddenString += membershipItem.getName() + "; ";
   	        	}
   	        }          
    	}
    }

    if (! "".equals(sendToString)) {
  	  sendToString=sendToString.substring(0, sendToString.length()-2); //remove last comma and space
    }

    if ("".equals(sendToHiddenString)) {
        rrepMsg.setRecipientsAsText(sendToString);
    }
    else {
    	sendToHiddenString=sendToHiddenString.substring(0, sendToHiddenString.length()-2); //remove last comma and space    
    	rrepMsg.setRecipientsAsText(sendToString + " (" + sendToHiddenString + ")");
    }    
    
    //Add attachments
    for(int i=0; i<allAttachments.size(); i++)
    {
      prtMsgManager.addAttachToPvtMsg(rrepMsg, ((DecoratedAttachment)allAttachments.get(i)).getAttachment());         
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
    
    EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FORWARD, getEventMessage(rrepMsg), false));
    
    return DISPLAY_MESSAGES_PG;

  }
  
  /**
   * process from Compose screen
   * @return - pvtMsg
   */
  public String processPvtMsgReplySaveDraft() {
    LOG.debug("processPvtMsgReplySaveDraft()");
    
    if(!hasValue(getReplyToSubject()))
    {
      setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
      return null ;
    }

    if(getSelectedComposeToList().size()<1)
    {
      setErrorMessage(getResourceBundleString(SELECT_RECIPIENT_LIST_FOR_REPLY));
      return null ;
    }
    
    PrivateMessage drMsg=getDetailMsg().getMsg() ;

    PrivateMessage drrepMsg = messageManager.createPrivateMessage() ;
    drrepMsg.setTitle(getReplyToSubject()) ;
    drrepMsg.setDraft(Boolean.TRUE);
    drrepMsg.setDeleted(Boolean.FALSE);
    
    drrepMsg.setAuthor(getAuthorString());
    drrepMsg.setApproved(Boolean.FALSE);
    drrepMsg.setBody(getReplyToBody()) ;
    
    drrepMsg.setInReplyTo(drMsg) ;
    this.getRecipients().add(drMsg.getCreatedBy());
    
    //Add attachments
    for(int i=0; i<allAttachments.size(); i++)
    {
      prtMsgManager.addAttachToPvtMsg(drrepMsg, ((DecoratedAttachment)allAttachments.get(i)).getAttachment());         
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
    
    return DISPLAY_MESSAGES_PG ;    
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
      setErrorMessage(getResourceBundleString(SELECT_MSGS_TO_DELETE));
      return null;  //stay in the same page if nothing is selected for delete
    }else {
      setErrorMessage(getResourceBundleString(CONFIRM_PERM_MSG_DELETE));
      return DELETE_MESSAGE_PG;
    }
  }
  
  //delete private message 
  public String processPvtMsgMultiDelete()
  { 
    LOG.debug("processPvtMsgMultiDelete()");
  
    boolean deleted = false;
    for (Iterator iter = getSelectedDeleteItems().iterator(); iter.hasNext();)
    {
      PrivateMessage element = ((PrivateMessageDecoratedBean) iter.next()).getMsg();
      if (element != null) 
      {
    	deleted = true;
        prtMsgManager.deletePrivateMessage(element, getPrivateMessageTypeFromContext(msgNavMode)) ;        
      }      
      
      if ("Deleted".equals(msgNavMode))
    	  EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_REMOVE, getEventMessage((Message) element), false));
    }
    
    if (deleted)
    {
    	if ("Deleted".equals(msgNavMode))
    		multiDeleteSuccessMsg = getResourceBundleString(PERM_DELETE_SUCCESS_MSG);
    	else
    		multiDeleteSuccessMsg = getResourceBundleString(MULTIDELETE_SUCCESS_MSG);
    	
    	multiDeleteSuccess = true;
    }


	return DISPLAY_MESSAGES_PG;
  }

  
  public String processPvtMsgDispOtions() 
  {
    LOG.debug("processPvtMsgDispOptions()");
    
    return "pvtMsgOrganize" ;
  }
  
  
  ///////////////////////////       Process Select All       ///////////////////////////////
  private boolean selectAll = false;  
  private int numberChecked = 0; // to cover case where user selectes check all
  
  public boolean isSelectAll()
  {
    return selectAll;
  }
  public void setSelectAll(boolean selectAll)
  {
    this.selectAll = selectAll;
  }

  public int getNumberChecked() 
  {
	return numberChecked;
  }

  /**
   * process isSelected for all decorated messages
   * @return same page i.e. will be pvtMsg 
   */
  public String processCheckAll()
  {
    LOG.debug("processCheckAll()");
    selectAll= true;
    multiDeleteSuccess = false;

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
          
          attachments.add(new DecoratedAttachment(thisAttach));
          
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
          allAttachments.add(new DecoratedAttachment(thisAttach));
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
        if (attachId.equalsIgnoreCase(((DecoratedAttachment) attachments.get(i)).getAttachment()
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
        if (attachId.equalsIgnoreCase(((DecoratedAttachment) allAttachments.get(i)).getAttachment()
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
      	DecoratedAttachment thisAttach = (DecoratedAttachment)attachments.get(i);
        if(((Long)thisAttach.getAttachment().getPvtMsgAttachId()).toString().equals(removeAttachId))
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
    return COMPOSE_MSG_PG;
    
  }
  
  public String processRemoveAttachCancel()
  {
    LOG.debug("processRemoveAttachCancel()");
    
    removeAttachId = null;
    prepareRemoveAttach.clear();
    return COMPOSE_MSG_PG ;
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
    return MESSAGE_SETTING_PG;
  }
    
  public void processPvtMsgSettingsRevise(ValueChangeEvent event)
  {
    LOG.debug("processPvtMsgSettingsRevise()");   
    
    /** block executes when changing value to "no" */
    if (SET_AS_YES.equals(forwardPvtMsg)){
      setForwardPvtMsgEmail(null);      
    }       
    if (SET_AS_NO.equals(forwardPvtMsg)){
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
      setErrorMessage(getResourceBundleString(PROVIDE_VALID_EMAIL));
      setActivatePvtMsg(activate);
      return MESSAGE_SETTING_PG;
    }
    else
    {
      Area area = prtMsgManager.getPrivateMessageArea();            
      
      Boolean formAreaEnabledValue = (SET_AS_YES.equals(activate)) ? Boolean.TRUE : Boolean.FALSE;
      area.setEnabled(formAreaEnabledValue);
      
      Boolean formAutoForward = (SET_AS_YES.equals(forward)) ? Boolean.TRUE : Boolean.FALSE;            
      forum.setAutoForward(formAutoForward);
      if (Boolean.TRUE.equals(formAutoForward)){
        forum.setAutoForwardEmail(email);  
      }
      else{
        forum.setAutoForwardEmail(null);  
      }
             
      prtMsgManager.saveAreaAndForumSettings(area, forum);

      if (isMessagesandForums()) {
    	  return MAIN_PG;
      }
      else {
    	  return MESSAGE_HOME_PG;
      }
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
    //String topicTitle= getExternalParameterByKey("pvtMsgTopicTitle");
    String topicTitle = forumManager.getTopicByUuid(getExternalParameterByKey("pvtMsgTopicId")).getTitle();
    setSelectedTopicTitle(topicTitle) ;
    String topicId=getExternalParameterByKey("pvtMsgTopicId") ;
    setSelectedTopicId(topicId);
    
    setFromMainOrHp();
    
    return MESSAGE_FOLDER_SETTING_PG;
  }

  public String processPvtMsgFolderSettingRevise() {
    LOG.debug("processPvtMsgFolderSettingRevise()");
    
    if(this.ismutable)
    {
      return null;
    }else 
    {
      selectedNewTopicTitle = selectedTopicTitle;
      return REVISE_FOLDER_PG ;
    }    
  }
  
  public String processPvtMsgFolderSettingAdd() {
    LOG.debug("processPvtMsgFolderSettingAdd()");
    
    setFromMainOrHp();
    this.setAddFolder("");  // make sure the input box is empty
    
    return ADD_MESSAGE_FOLDER_PG ;
  }
  public String processPvtMsgFolderSettingDelete() {
    LOG.debug("processPvtMsgFolderSettingDelete()");
    
    setFromMainOrHp();
    
    String typeUuid = getPrivateMessageTypeFromContext(selectedTopicTitle);          
    
    setTotalMsgInFolder(prtMsgManager.findMessageCount(typeUuid));
    
    if(ismutable)
    {
      setErrorMessage(getResourceBundleString(CANNOT_DEL_REVISE_FOLDER));
      return null;
    }else {
      setErrorMessage(getResourceBundleString(CONFIRM_FOLDER_DELETE));
      return DELETE_FOLDER_PG;
    }    
  }
  
  public String processPvtMsgReturnToMainOrHp()
  {
	  LOG.debug("processPvtMsgReturnToMainOrHp()");
	    if(fromMainOrHp != null && (fromMainOrHp.equals(MESSAGE_HOME_PG) || (fromMainOrHp.equals(MAIN_PG))))
	    {
	    	String returnToPage = fromMainOrHp;
			fromMainOrHp = "";
			return returnToPage;
	    }
	    else
	    {
	    	return MAIN_PG ;
	    }
  }
  
  public String processPvtMsgReturnToFolderView() 
  {
	  return MESSAGE_FOLDER_SETTING_PG;
  }
  
  //Create a folder within a forum
  public String processPvtMsgFldCreate() 
  {
    LOG.debug("processPvtMsgFldCreate()");
    
    String createFolder=getAddFolder() ;
    if(createFolder == null || createFolder.trim().length() == 0)
    {
      setErrorMessage(getResourceBundleString(ENTER_FOLDER_NAME));
      return null ;
    } else {
      if(PVTMSG_MODE_RECEIVED.equals(createFolder) || PVTMSG_MODE_SENT.equals(createFolder)|| 
          PVTMSG_MODE_DELETE.equals(createFolder) || PVTMSG_MODE_DRAFT.equals(createFolder))
      {
        setErrorMessage(getResourceBundleString(CREATE_DIFF_FOLDER_NAME));
      } else 
      {
        prtMsgManager.createTopicFolderInForum(forum, createFolder);
      //create a typeUUID in commons
      String newTypeUuid= typeManager.getCustomTopicType(createFolder); 
      }
      //since PrivateMessagesTool has a session scope, 
      //reset addFolder to blank for new form
      addFolder = "";
      return processPvtMsgReturnToMainOrHp();
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
      setErrorMessage(getResourceBundleString(FOLDER_NAME_BLANK));
      return REVISE_FOLDER_PG;
    }
    else {
      List tmpMsgList = prtMsgManager.getMessagesByType(typeManager.getCustomTopicType(prtMsgManager.getTopicByUuid(selectedTopicId).getTitle()), PrivateMessageManager.SORT_COLUMN_DATE,
          PrivateMessageManager.SORT_ASC);
      prtMsgManager.renameTopicFolder(forum, selectedTopicId,  newTopicTitle);
      //rename topic in commons -- as messages are linked through commons type
      //TODO - what if more than one type objects are returned-- We need to switch from title
      String newTypeUuid = typeManager.renameCustomTopicType(selectedTopicTitle, newTopicTitle);
      for(int i=0; i<tmpMsgList.size(); i++)
      {
      	PrivateMessage tmpPM = (PrivateMessage) tmpMsgList.get(i);
      	List tmpRecipList = tmpPM.getRecipients();
      	tmpPM.setTypeUuid(newTypeUuid);
      	String currentUserId = SessionManager.getCurrentSessionUserId();
      	Iterator iter = tmpRecipList.iterator();
      	while(iter.hasNext())
      	{
      		PrivateMessageRecipient tmpPMR = (PrivateMessageRecipient) iter.next();
      		if(tmpPMR != null && tmpPMR.getUserId().equals(currentUserId))
      		{
      			tmpPMR.setTypeUuid(newTypeUuid);
      		}
      	}
      	tmpPM.setRecipients(tmpRecipList);
      	prtMsgManager.savePrivateMessage(tmpPM, false);
      }
    }
    setSelectedTopicTitle(newTopicTitle) ;
    return MESSAGE_FOLDER_SETTING_PG ;
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
    return processPvtMsgReturnToMainOrHp();
  }
  
  //create folder within folder
  public String processPvtMsgFolderInFolderAdd()
  {
    LOG.debug("processPvtMsgFolderSettingAdd()");  
    
    setFromMainOrHp();
    this.setAddFolder("");
    
    return ADD_FOLDER_IN_FOLDER_PG ;
  }
  
  //create folder within Folder
  //TODO - add parent fodler id for this 
  public String processPvtMsgFldInFldCreate() 
  {
    LOG.debug("processPvtMsgFldCreate()");
    
    PrivateTopic parentTopic=(PrivateTopic) prtMsgManager.getTopicByUuid(selectedTopicId);
    
    String createFolder=getAddFolder() ;
    if(createFolder == null || createFolder.trim().length() == 0)
    {
      setErrorMessage(getResourceBundleString(ENTER_FOLDER_NAME));
      return null ;
    } else {
      if(PVTMSG_MODE_RECEIVED.equals(createFolder) || PVTMSG_MODE_SENT.equals(createFolder)|| 
          PVTMSG_MODE_DELETE.equals(createFolder) || PVTMSG_MODE_DRAFT.equals(createFolder))
      {
        setErrorMessage(CREATE_DIFF_FOLDER_NAME);
      } else 
      {
        prtMsgManager.createTopicFolderInTopic(forum, parentTopic, createFolder);
      //create a typeUUID in commons
      String newTypeUuid= typeManager.getCustomTopicType(createFolder); 
      }
      
      addFolder = "";
      return processPvtMsgReturnToMainOrHp();
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
    return MOVE_MESSAGE_PG;
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
    if( moveTopicTitle == null || moveTopicTitle.trim().length() == 0){
    	setErrorMessage(getResourceBundleString(MOVE_MSG_ERROR));
    	return null;
    }
    
    Topic newTopic= prtMsgManager.getTopicByUuid(moveTopicTitle);
    Topic oldTopic=selectedTopic.getTopic();
    if( newTopic.getUuid() == oldTopic.getUuid()){
    	//error
    	setErrorMessage(getResourceBundleString(MOVE_MSG_ERROR));
    	return null;
    }
    else{
    	if (selectedMoveToFolderItems == null)
    	{
    		prtMsgManager.movePvtMsgTopic(detailMsg.getMsg(), oldTopic, newTopic);
    	}
    	else
    	{
    		for (Iterator movingIter = selectedMoveToFolderItems.iterator(); movingIter.hasNext();)
    		{
    			PrivateMessageDecoratedBean decoMessage = (PrivateMessageDecoratedBean) movingIter.next();
		        final PrivateMessage initPrivateMessage = prtMsgManager.initMessageWithAttachmentsAndRecipients(decoMessage.getMsg());
    			decoMessage = new PrivateMessageDecoratedBean(initPrivateMessage);
    			
    			prtMsgManager.movePvtMsgTopic(decoMessage.getMsg(), oldTopic, newTopic);
    		}
    	}
	    
		//reset 
		moveToTopic="";
		moveToNewTopic="";
		selectedMoveToFolderItems = null;
		
    	// Return to Messages & Forums page or Messages page
   		if (isMessagesandForums()) 
   		{
   			return MAIN_PG;
   		}
   		else
   		{
   			return MESSAGE_HOME_PG;
   		}
    }
  }
  
  /**
   * 
   * @return
   */
  public String processPvtMsgCancelToDetailView()
  {
    LOG.debug("processPvtMsgCancelToDetailView()");
    this.deleteConfirm=false;
    
    // due to adding ability to move multiple messages
    if (selectedMoveToFolderItems != null)
    {
    	selectedMoveToFolderItems = null;
    	return DISPLAY_MESSAGES_PG;
    }
    
    return SELECTED_MESSAGE_PG;
  }
  
  ///////////////   SEARCH      ///////////////////////
  private List searchPvtMsgs;
  public List getSearchPvtMsgs()
  {
    if(selectView!=null && selectView.equalsIgnoreCase(THREADED_VIEW))
    {
        this.rearrageTopicMsgsThreaded(true);
    }
    //  If "check all", update the decorated pmb to show selected
    if (selectAll)
    {
    	Iterator searchIter = searchPvtMsgs.iterator();
    	while (searchIter.hasNext())
    	{
    		PrivateMessageDecoratedBean searchMsg = (PrivateMessageDecoratedBean)searchIter.next();
    		searchMsg.setIsSelected(true);
    	}
    	
    	selectAll = false;
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
    multiDeleteSuccess = false;

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
    
    if(searchOnDate && searchFromDate == null && searchToDate==null)
    {
       setErrorMessage(getResourceBundleString(MISSING_BEG_END_DATE));
    }
    
    if(!hasValue(searchText))
    {
       setErrorMessage(getResourceBundleString(ENTER_SEARCH_TEXT));
    }
    
    tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), 
          getSearchText(), getSearchFromDate(), getSearchToDate(),
          searchOnSubject, searchOnAuthor, searchOnBody, searchOnLabel, searchOnDate) ;
    
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
      return SEARCH_RESULT_MESSAGES_PG ;
    }
    else 
      {
        setErrorMessage(getResourceBundleString(NO_MATCH_FOUND));
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
      //searchPvtMsgs= decoratedPvtMsgs;   
    }
    
    searchOnBody=false ;
    searchOnSubject=true;
    searchOnLabel= false ;
    searchOnAuthor=false;
    searchOnDate=false;
    searchFromDate=null;
    searchToDate=null;
    
    return DISPLAY_MESSAGES_PG;
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
    numberChecked = 0;

    for (Iterator iter = msg.iterator(); iter.hasNext();)
    {
      PrivateMessage element = (PrivateMessage) iter.next();                  
      
      PrivateMessageDecoratedBean dbean= new PrivateMessageDecoratedBean(element);
      //if processSelectAll is set, then set isSelected true for all messages,
      if(selectAll)
      {
        dbean.setIsSelected(true);
        numberChecked++;
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
    return DISPLAY_MESSAGES_PG ; 
  }
  
  public String processUploadConfirm()
  {
    return DISPLAY_MESSAGES_PG;
  }
  
  public String processUploadCancel()
  {
    return DISPLAY_MESSAGES_PG ;
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
        new FacesMessage(getResourceBundleString(ALERT) + ' ' + errorMsg));
  }

  private void setInformationMessage(String infoMsg)
  {
	    LOG.debug("setInformationMessage(String " + infoMsg + ")");
	    FacesContext.getCurrentInstance().addMessage(null,
	        new FacesMessage(infoMsg));
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
	
    public static String getResourceBundleString(String key) 
    {
        return rb.getString(key);
    }
    
    public static String getResourceBundleString(String key, Object[] args) {
    	return rb.getFormattedMessage(key, args);
    }


    public String getAuthorString() 
    {
       String authorString = getUserId();
       
       try
       {
         authorString = UserDirectoryService.getUser(getUserId()).getSortName();

       }
       catch(Exception e)
       {
         e.printStackTrace();
       }
       
       return authorString;
    }
    
    public String getPlacementId() 
    {
       return Validator.escapeJavascript("Main" + ToolManager.getCurrentPlacement().getId());
    }

    public boolean isSearchPvtMsgsEmpty()
    {
    	return searchPvtMsgs == null || searchPvtMsgs.isEmpty();
    }

    public void setMsgNavMode(String msgNavMode) {
		this.msgNavMode = msgNavMode;
	}	
	
	/**
	 * @return
	 */
	public String processActionMarkCheckedAsRead()
	{
		return markCheckedMessages(true);
	}
	
	public String processActionDeleteChecked() {
	    LOG.debug("processActionDeleteChecked()");

		List pvtMsgList = getPvtMsgListToProcess();
		boolean msgSelected = false;
		selectedDeleteItems = new ArrayList();
		
		Iterator pvtMsgListIter = pvtMsgList.iterator(); 
		while (pvtMsgListIter.hasNext())
		{
			PrivateMessageDecoratedBean decoMessage = (PrivateMessageDecoratedBean) pvtMsgListIter.next();
			if(decoMessage.getIsSelected())
			{
				msgSelected = true;
				selectedDeleteItems.add(decoMessage);
			}
		}

		if (!msgSelected)
		{
			setErrorMessage(getResourceBundleString(NO_MARKED_DELETE_MESSAGE));
			return null;
		}
		
		return processPvtMsgMultiDelete();
	}
	
	public String processActionMoveCheckedToFolder() {
	    LOG.debug("processActionMoveCheckedToFolder()");

	    List pvtMsgList = getPvtMsgListToProcess();
	    boolean msgSelected = false;
	    selectedMoveToFolderItems = new ArrayList();
	    
		Iterator pvtMsgListIter = pvtMsgList.iterator(); 
		while (pvtMsgListIter.hasNext())
		{
			PrivateMessageDecoratedBean decoMessage = (PrivateMessageDecoratedBean) pvtMsgListIter.next();
			if(decoMessage.getIsSelected())
			{
				msgSelected = true;
		        selectedMoveToFolderItems.add(decoMessage);
			}
		}

		if (!msgSelected)
		{
			setErrorMessage(getResourceBundleString(NO_MARKED_MOVE_MESSAGE));
			return null;
		}
		
	    moveToTopic = selectedTopicId;
	    return MOVE_MESSAGE_PG;
		
	}
	
	private List getPvtMsgListToProcess() {
		List pvtMsgList = new ArrayList();
		boolean searchMode = false;
		// determine if we are looking at search results or the main listing
		if (searchPvtMsgs != null && !searchPvtMsgs.isEmpty())
		{
			searchMode = true;
			pvtMsgList = searchPvtMsgs;
		}
		else
		{
			pvtMsgList = decoratedPvtMsgs;
		}
		
		return pvtMsgList;
	}
	/**
	 * 
	 * @param readStatus
	 * @return
	 */
	private String markCheckedMessages(boolean readStatus)
	{
		List pvtMsgList = getPvtMsgListToProcess();
		boolean msgSelected = false;
		boolean searchMode = false;
		
		Iterator pvtMsgListIter = pvtMsgList.iterator(); 
		while (pvtMsgListIter.hasNext())
		{
			PrivateMessageDecoratedBean decoMessage = (PrivateMessageDecoratedBean) pvtMsgListIter.next();
			if(decoMessage.getIsSelected())
			{
				msgSelected = true;
				prtMsgManager.markMessageAsReadForUser(decoMessage.getMsg());
				
				if (searchMode)
				{
					// Although the change was made in the db, the search
					// view needs to be refreshed (it doesn't return to db)
					decoMessage.setHasRead(true);
					decoMessage.setIsSelected(false);
				}
			}      
		}
		
		if (!msgSelected)
		{
			setErrorMessage(getResourceBundleString(NO_MARKED_READ_MESSAGE));
			return null;
		}
		
		if (searchMode)
		{
			return SEARCH_RESULT_MESSAGES_PG;
		}
		
		return DISPLAY_MESSAGES_PG; 
	}
	
	private void setFromMainOrHp()
	{
		String fromPage = getExternalParameterByKey(COMPOSE_FROM_PG);
	    if(fromPage != null && (fromPage.equals(MESSAGE_HOME_PG) || (fromPage.equals(MAIN_PG))))
	    {
	    	fromMainOrHp = fromPage;
	    }
	}

	/**
	 * @return TRUE if within Messages & Forums tool, FALSE otherwise
	 */
	public boolean isMessagesandForums() {
		return messageManager.currentToolMatch(MESSAGECENTER_TOOL_ID);
	}
	
	/**
	 * @return TRUE if within Messages tool, FALSE otherwise
	 */
	public boolean isMessages() {
		return messageManager.currentToolMatch(MESSAGES_TOOL_ID);
	}
	
	/**
	 * @return TRUE if Message Forums (Message Center) exists in this site,
	 *         FALSE otherwise
	 */
	public boolean isMessageForumsPageInSite() {
		return isMessageForumsPageInSite(getSiteId());
	}

	/**
	 * @return TRUE if Messages & Forums (Message Center) exists in this site,
	 *         FALSE otherwise
	 */
	private boolean isMessageForumsPageInSite(String siteId) {
		return messageManager.isToolInSite(siteId, MESSAGECENTER_TOOL_ID);
	}
	
	/**
	 * @return TRUE if Messages tool exists in this site,
	 *         FALSE otherwise
	 */
	public boolean isMessagesPageInSite() {
		return isMessagesPageInSite(getSiteId());
	}

	/**
	 * @return TRUE if Messages tool exists in this site,
	 *         FALSE otherwise
	 */
	private boolean isMessagesPageInSite(String siteId) {
		return messageManager.isToolInSite(siteId, MESSAGES_TOOL_ID);
	}
	
	private String getEventMessage(Object object) {
	  	String eventMessagePrefix = "";
	  	final String toolId = ToolManager.getCurrentTool().getId();
		  	
		if (toolId.equals(DiscussionForumService.MESSAGE_CENTER_ID))
			eventMessagePrefix = "/messages&Forums/site/";
		else if (toolId.equals(DiscussionForumService.MESSAGES_TOOL_ID))
			eventMessagePrefix = "/messages/site/";
		else
			eventMessagePrefix = "/forums/site/";
	  	
	  	return eventMessagePrefix + ToolManager.getCurrentPlacement().getContext() + 
	  				"/" + object.toString() + "/" + SessionManager.getCurrentSessionUserId();
	}
}