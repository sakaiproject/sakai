/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/DiscussionMessageBean.java $
 * $Id: DiscussionMessageBean.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.messageforums.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Message;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.messageforums.DiscussionForumTool;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;



/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 * @author Chen Wen
 *
 */
public class DiscussionMessageBean
{
	
  private static final Log LOG = LogFactory.getLog(DiscussionForumTool.class);

  private boolean selected;
  private Message message;
  private boolean read;
  private int depth;
  private boolean hasNext;
  private boolean hasPre;
  private boolean hasChild;
  private int childUnread;
  private int childCount;
  private boolean hasNextThread;
  private boolean hasPreThread;
  private long nextThreadId;
  private long preThreadId;
  private String parentTopicTitle;
  private String parentForumTitle;
  private boolean revise;
  private boolean deleted;
  private boolean userCanDelete;
  private boolean userCanEmail;
  private String authorEmail;


  private MessageForumsMessageManager messageManager;

  public DiscussionMessageBean(Message msg, MessageForumsMessageManager messageManager)
  {    
    this.message = msg;
    this.messageManager = messageManager; 
  }

 

  /**
   * @return Returns the selected.
   */
  public boolean isSelected()
  {
    return selected;
  }



  /**
   * @param selected The selected to set.
   */
  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }



  /**
   * @return Returns the msg.
   */
  public Message getMessage()
  {
    return message;
  }

  /**
   * @return Returns the hasAttachment.
   */
  public boolean isHasAttachment()
  {
    if(message==null)
    {
      return false;
    }
    message = messageManager.getMessageByIdWithAttachments(message.getId());
    if(message.getAttachments()==null)
    {
      return false;
    }
    else if(message.getAttachments().size()>0)
    {
    	return true;
    }
/*    else (changed after not lazy loading attachments.)
    {
    	MessageForumsMessageManager mfmm = 
    		(org.sakaiproject.api.app.messageforums.MessageForumsMessageManager)ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsMessageManager");
    	Message messageWithAttach = mfmm.getMessageByIdWithAttachments(message.getId());
    	if(messageWithAttach != null)
    	{
    		if(messageWithAttach.getAttachments().size()>0)
    			return true;
    	}
    }*/
    return false;
  }



  /**
   * @return Returns the read.
   */
  public boolean isRead()
  {		
	  //messages for the anon user are always read
	  if (UserDirectoryService.getCurrentUser().equals(UserDirectoryService.getAnonymousUser()))
		  return true;
	  
    return read;
  }



  /**
   * @param read The read to set.
   */
  public void setRead(boolean read)
  {
    this.read = read;
  }
  
  public void setDepth(int depth)
  {
  	this.depth = depth;
  }
  
  public int getDepth()
  {
  	return depth;
  }

  /**
   * @return Returns the hasNext.
   */
  public boolean getHasNext() 
  {
  	return hasNext;
  }
  
  /**
   * @param hasNext The hasNext to set.
   */
  public void setHasNext(boolean hasNext) 
  {
  	this.hasNext = hasNext;
  }
  
  /**
   * @return Returns the hasPre.
   */
  public boolean getHasPre() 
  {
  	return hasPre;
  }
  
  /**
   * @param hasPre The hasPre to set.
   */
  public void setHasPre(boolean hasPre) {
  	this.hasPre = hasPre;
  }
  
  public boolean getIsOwn()
  {
  	if(this.getMessage().getCreatedBy().equals(SessionManager.getCurrentSessionUserId()))
  		return true;
  	return false;
  }
  
  public void setHasChild(boolean hasChild)
  {
  	this.hasChild = hasChild;
  }

  public boolean getHasChild()
  {
  	List childList = messageManager.getFirstLevelChildMsgs(this.getMessage().getId());
  	if((childList != null) && (childList.size()>0))
  		hasChild = true;
  	else
  		hasChild = false;
  	
  	return hasChild;
  }
  
  public ArrayList getAttachList()
  {
	  ArrayList decoAttachList = new ArrayList();
	  List attachList = message.getAttachments(); 
	  if(attachList != null)
	  {
		  for(int i=0; i<attachList.size(); i++)
		  {
			  DecoratedAttachment decoAttach = new DecoratedAttachment((Attachment)attachList.get(i));
			  decoAttachList.add(decoAttach);
		  }
	  }
	  return decoAttachList;
  }

  public int getChildUnread(){
	  return childUnread;
  }

  public void setChildUnread(int newValue){
	  childUnread = newValue;
  }

  public int getChildCount(){
	  return childCount;
  }

  public void setChildCount(int newValue){
	  childCount = newValue;
  }


  public boolean getHasNextThread(){
	  return hasNextThread;
  }

  public void setHasNextThread(boolean newValue){
	  hasNextThread = newValue;
  }

  public boolean getHasPreThread(){
	  return hasPreThread;
  }

  public void setHasPreThread(boolean newValue){
	  hasPreThread = newValue;
  }

  public long getNextThreadId(){
	  return nextThreadId;
  }

  public void setNextThreadId(long newValue){
	  nextThreadId = newValue;
  }

  public long getPreThreadId(){
	  return preThreadId;
  }

  public void setPreThreadId(long newValue){
	  preThreadId = newValue;
  }

  /**
   * returns true if message has been approved
   * @return
   */
  public boolean isMsgApproved()
  {
	  return message.getApproved() == Boolean.TRUE;
  }
  /**
   * returns true if message has approval pending
   * @return
   */
  public boolean isMsgPending()
  {	
	  return message.getApproved() == null;
  }

  /**
   * returns true if message was denied
   * @return
   */
  public boolean isMsgDenied()
  {
	  return message.getApproved() == Boolean.FALSE;
  }
  
  public void setRevise(boolean revise)
  {
	  this.revise = revise;
  }
  
  public boolean getRevise()
  {
	  return this.revise;
  }
  
  public void setUserCanDelete(boolean userCanDelete) {
	  this.userCanDelete = userCanDelete;
  }
  
  public boolean getUserCanDelete() {
	  return userCanDelete;
  }

  public boolean getDeleted() 
  {
	return message.getDeleted() == Boolean.TRUE;
  }

  public void setDeleted(boolean deleted) 
  {
	message.setDeleted(deleted);
  }
  
  public boolean isUserCanEmail() {
	return userCanEmail;
  }
	
  public void setUserCanEmail(boolean userCanEmail) {
	this.userCanEmail = userCanEmail;
  }
  
	public String getAuthorEmail()
	{
LOG.debug("... getAuthorEmail(): ");
		String userEmail = "";
		try
		{
			String currentUserId = this.getMessage().getCreatedBy();
LOG.debug("... currentUserId : " + currentUserId);
			
			userEmail = UserDirectoryService.getUser(currentUserId).getEmail(); 		
LOG.debug("... getAuthorEmail(): userEmail = " + userEmail);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
LOG.debug("... before return getAuthorEmail(): userEmail = " + userEmail);
		
		return userEmail;
	}
}
