/**********************************************************************************
 * $URL$
 * $Id$
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
import java.util.List;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.util.ResourceLoader;

public class PrivateMessageDecoratedBean 
{

  private PrivateMessage msg;
  public PrivateMessageDecoratedBean(PrivateMessage msg)
  {
    this.msg=msg ;
  }
  //Wrapper for JSF Selection
  private boolean isSelected;
  public boolean getIsSelected() {
    return isSelected;
  }
  public void setIsSelected(boolean isSelected) {
    this.isSelected=isSelected ;    
  }
  
  private boolean hasRead = false;
  private int depth;
  private boolean hasNext;
  private boolean hasPre;
  private PrivateMessage uiInReply = null;
  //This string is for display of recipients in Received folder- comman separated list of users
  private String sendToStringDecorated=""; 
  
  private boolean isPreview = false;
  private boolean isPreviewReply = false;
  private boolean isPreviewReplyAll = false;
  private boolean isPreviewForward = false;
  private PrivateMessageDecoratedBean previewReplyTmpMsg;
  
  private boolean isReplied = false;
  
  public PrivateMessage getMsg()
  {
    return msg;
  }
  
 
  private List msgs=new ArrayList();
  public void addPvtMessage(PrivateMessageDecoratedBean decomsg)
  {
    if(!msgs.contains(decomsg))
    {
      msgs.add(decomsg);    
    }
  }

  public boolean isHasRead()
  {
    return hasRead;
  }
  public void setHasRead(boolean hasRead)
  {
    this.hasRead = hasRead;
  }
  /**
   * @return Returns the depth.
   */
  public int getDepth()
  {
    return depth;
  }
  /**
   * @param depth The depth to set.
   */
  public void setDepth(int depth)
  {
    this.depth = depth;
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
  public void setHasPre(boolean hasPre)
  {
    this.hasPre = hasPre;
  }
  
  public PrivateMessage getUiInReply()
  {
  	return uiInReply;
  }
  
  public void setUiInReply(PrivateMessage uiInReply)
  {
  	this.uiInReply = uiInReply;
  }
  /**
   * @return Returns the sendToString.
   */
  public String getSendToStringDecorated()
  {
    return sendToStringDecorated;
  }
  /**
   * @param sendToString The sendToString to set.
   */
  public void setSendToStringDecorated(String sendToStringDecorated)
  {
    this.sendToStringDecorated = sendToStringDecorated;
  }
  
  public ArrayList getAttachList()
  {
	ArrayList decoAttachList = new ArrayList();
	List attachList = msg.getAttachments(); 
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

  public String getVisibleRecipientsAsText() {
	String recips = msg.getRecipientsAsText();
	final int parenIndex = recips.indexOf(PrivateMessage.HIDDEN_RECIPIENTS_START);
	
	if (parenIndex > 0) {
		return recips.substring(0, parenIndex-1);
	}
	else {
		return recips;
	}
  }
  
  public String getRecipientsAsText() {
	  return msg.getRecipientsAsText();
  }
  
  public String getRecipientsAsTextBcc() {
	  return msg.getRecipientsAsTextBcc();
  }

  public String getAuthor() {
	  return msg.getAuthor();
  }
	public String getLabel(){
		ResourceLoader rl = new ResourceLoader("org.sakaiproject.api.app.messagecenter.bundle.Messages");
		return rl.getString(msg.getLabel());
	}
  public boolean getIsPreview() {
	  return isPreview;
  }
  public void setIsPreview(boolean isPreview) {
	  this.isPreview = isPreview;
  }
  public boolean getIsPreviewReply() {
	  return isPreviewReply;
  }
  public void setIsPreviewReply(boolean isPreviewReply) {
	  this.isPreviewReply = isPreviewReply;
  }
  public boolean getIsPreviewReplyAll() {
	  return isPreviewReplyAll;
  }
  public void setIsPreviewReplyAll(boolean isPreviewReplyAll) {
	  this.isPreviewReplyAll = isPreviewReplyAll;
  }
  public PrivateMessageDecoratedBean getPreviewReplyTmpMsg() {
	  return previewReplyTmpMsg;
  }
  public void setPreviewReplyTmpMsg(PrivateMessageDecoratedBean previewReplyTmpMsg) {
	  this.previewReplyTmpMsg = previewReplyTmpMsg;
  }
  public boolean getIsPreviewForward() {
	  return isPreviewForward;
  }
  public boolean getExternalEmail(){
	  return this.msg.getExternalEmail();
  }  
  public void setIsPreviewForward(boolean isPreviewForward) {
	  this.isPreviewForward = isPreviewForward;
  }
  
  public boolean isReplied() {
	  return isReplied;
}

  public void setReplied(boolean isReplied) {
	  this.isReplied = isReplied;
  }

}


/**********************************************************************************
*
* $Header$
*
**********************************************************************************/