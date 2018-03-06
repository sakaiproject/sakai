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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.AnonymousManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.Rank;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;

/** 
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 * @author Chen Wen
 *
 */
@Slf4j
public class DiscussionMessageBean
{

  private boolean selected;
  private Message message;
  private boolean read;
  private int depth;
  private boolean hasNext;
  private boolean hasPre;
  private boolean hasChild;
  private boolean hasNotDeletedChild;
  private int childUnread;
  private int childCount;
  private boolean hasNextThread;
  private boolean hasPreThread;
  private long nextThreadId;
  private long preThreadId;
  private boolean revise;
  private boolean userCanDelete;
  private boolean userCanEmail;
  //Move Threads
  private boolean selected_move;
  private boolean moved;
  private int authorPostCount;
  private Rank authorRank = null;
  private Boolean useAnonymousId = null;
  private String anonId;

  public Rank getAuthorRank() {
    return authorRank;
  }

  public void setAuthorRank(Rank aRank) {
    authorRank = aRank;
  }

  public void setAuthorPostCount(String userEid) {
    // This is invoked a lot, but it's only relevant when a student has a rank.
    if (authorRank != null)
    {
      int authorCount = messageManager.findAuthoredMessageCountForStudent(userEid);
      authorPostCount = authorCount;
    }
  }

  public int getAuthorPostCount() {
    return authorPostCount;
  }

  private MessageForumsMessageManager messageManager;

  public DiscussionMessageBean(Message msg, MessageForumsMessageManager messageManager)
  {    
    this.message = msg;
    this.messageManager = messageManager; 
  }

  public void setMoved(boolean b) {
    moved = b;
  }
 
  public boolean isMoved() {
    return moved;
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
   * @return Returns the selected threads to move.
   */
  public boolean isSelected_move()
  {
    return selected_move;
  }

  /**
   * @param selected_move:  The selected threads to set.
   */
  public void setSelected_move(boolean selected)
  {
    this.selected_move = selected;
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
  	if(this.getMessage().getCreatedBy().equals(getSessionManager().getCurrentSessionUserId()))
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

  public boolean getHasNotDeletedDescendant(final Long messageId)
  {
      List childList = messageManager.getFirstLevelChildMsgs(messageId==null?this.getMessage().getId():messageId);
      if((childList != null) && (childList.size() > 0))
      {
          hasNotDeletedChild = false;
          Iterator childIter = childList.iterator();
          while (childIter.hasNext() && !hasNotDeletedChild)
          {
              Message msg = (Message) childIter.next();
              if (!msg.getDeleted()) hasNotDeletedChild = true;
          }
          if (!hasNotDeletedChild) 
          {
              // Maybe a descendant
                childIter = childList.iterator();
                while (childIter.hasNext() && !hasNotDeletedChild)
                {
                    Message msg = (Message) childIter.next();
                    if (getHasNotDeletedDescendant(msg.getId())) hasNotDeletedChild = true;
                }
          }
      }
      else 
      {
          hasNotDeletedChild = false;
      }
      return hasNotDeletedChild;
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
log.debug("... getAuthorEmail(): ");
		String userEmail = "";
		try
		{
			String currentUserId = this.getMessage().getCreatedBy();
log.debug("... currentUserId : " + currentUserId);
			
			userEmail = UserDirectoryService.getUser(currentUserId).getEmail(); 		
log.debug("... getAuthorEmail(): userEmail = " + userEmail);
		}
		catch(Exception e)
		{
			log.error(e.getMessage(), e);
		}
log.debug("... before return getAuthorEmail(): userEmail = " + userEmail);
		
		return userEmail;
	}
	
	public String getAuthorEid(){
		String userEid = this.getMessage().getCreatedBy();
 		return userEid;
	}	

	private boolean isAnonymousEnabled()
	{
		return getAnonymousManager().isAnonymousEnabled();
	}

	public void setAnonId(String anonId)
	{
		this.anonId = anonId;
	}

	/**
	 * Returns the author's anonymousID regardless of whether we're in an anonymous context (unless anonymous is disabled system wide)
	 */
	public String getAnonId()
	{
		if (!isAnonymousEnabled())
		{
			return message.getAuthor();
		}

		if (anonId != null)
		{
			// anonId has already been supplied
			return anonId;
		}

		// get / create the anonId from the AnonymousManager
		String siteId = getToolManager().getCurrentPlacement().getContext();
		anonId = getAnonymousManager().getOrCreateAnonId(siteId, message.getAuthorId());
		return anonId;
	}

	private ToolManager getToolManager()
	{
		return ComponentManager.get(ToolManager.class);
	}

	private AnonymousManager getAnonymousManager()
	{
		return ComponentManager.get(AnonymousManager.class);
	}

	/**
	 * Returns the author's name and eid, or their anonymousID as appropriate
	 */
	public String getAnonAwareAuthor()
	{
		return isUseAnonymousId() ? getAnonId() : message.getAuthor();
	}

	/**
	 * Sets whether the message's author should display as an anonymousID
	 */
	public void setUseAnonymousId(boolean useAnonymousId)
	{
		this.useAnonymousId = useAnonymousId;
	}

	/**
	 * Determines whether the message's author should display as an anonymousID.
	 * If setUseAnonymousId has not yet been invoked against 'this', 
	 * this method will go to the database, and then cache the result for any subsequent calls.
	 * It's preferable to use setUseAnonymousId up front wherever possible for performance gains
	 */
	public boolean isUseAnonymousId()
	{
		if (!isAnonymousEnabled())
		{
			return false;
		}

		if (useAnonymousId != null)
		{
			// useAnonymousId has already been supplied
			return useAnonymousId;
		}

		// Determine if the containing topic is anonymous
		Topic topic = message.getTopic();
		if (topic != null)
		{
			// Determine if topic is anonymous
			// Topic may or may not be detached (ie. it may have been retrieved as the surrogate key of this mesage, so its only initialized attribute may be the topic ID).
			// Retrieve the full topic only when this is an issue via a try / catch
			Boolean postAnonymous = null;
			try
			{
				postAnonymous = topic.getPostAnonymous();
			}
			catch (RuntimeException e)
			{
				// Topic must be detached, retrieve it via ForumManager
				MessageForumsForumManager forumManager = (MessageForumsForumManager)ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsForumManager");
				topic = forumManager.getTopicById(true, topic.getId());
				postAnonymous = topic.getPostAnonymous();
			}

			if (postAnonymous)
			{
				// Are we supposed to reveal authors' identities to certain roles in this topic?
				if (topic.getRevealIDsToRoles())
				{
					if (getUIPermissionsManager().isIdentifyAnonAuthors(topic))
					{
						// This user has permission to identify authors in this topic
						useAnonymousId = Boolean.FALSE;
						return false;
					}
				}

				/*
				 * The topic is anonymous, and either
				 *   a) the topic is not configured to reveal identities to anymous, or
				 *   b) the topic is configured to reveal identities to some roles, but the current user does not have such a role
				 *
				 * Use the anonymous identity
				 */
				useAnonymousId = Boolean.TRUE;
				return true;
			}
		}

		// Topic is not anonymous; reveal identities
		useAnonymousId = Boolean.FALSE;
		return false;
	}

	/**
	 * Determines whether the author is the current user and the message is in an anonymous context
	 * This is useful to determine whether to display "(me)" beside the author's anon ID
	 */
	public boolean isCurrentUserAndAnonymous()
	{
		return isUseAnonymousId() && message.getAuthorId().equals(getSessionManager().getCurrentSessionUserId());
	}

	private SessionManager getSessionManager()
	{
		return ComponentManager.get(SessionManager.class);
	}

	private UIPermissionsManager getUIPermissionsManager()
	{
		return ComponentManager.get(UIPermissionsManager.class);
	}
}
