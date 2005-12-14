/**********************************************************************************
*
* $Header$
*
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

package org.sakaiproject.tool.messageforums.ui;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.api.app.messageforums.PrivateMessage;


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
  
  private boolean hasNextMsg=false;
  private boolean hasPreviousMsg=false;
  private String nextMsgId;
  private String previousMsgId;
  private boolean hasRead = false;
  
  
  public PrivateMessage getMsg()
  {
    return msg;
  }
  /**
   * @return Returns the hasNextMsg.
   */
  public boolean isHasNextMsg()
  {
    return hasNextMsg;
  }
  /**
   * @param hasNextMsg The hasNextMsg to set.
   */
  public void setHasNextMsg(boolean hasNextMsg)
  {
    this.hasNextMsg = hasNextMsg;
  }
  /**
   * @return Returns the hasPreviousMsg.
   */
  public boolean isHasPreviousMsg()
  {
    return hasPreviousMsg;
  }
  /**
   * @param hasPreviousMsg The hasPreviousMsg to set.
   */
  public void setHasPreviousMsg(boolean hasPreviousMsg)
  {
    this.hasPreviousMsg = hasPreviousMsg;
  }
  /**
   * @return Returns the nextMsgId.
   */
  public String getNextMsgId()
  {
    return nextMsgId;
  }
  /**
   * @param nextMsgId The nextMsgId to set.
   */
  public void setNextMsgId(String nextMsgId)
  {
    this.nextMsgId = nextMsgId;
  }
  /**
   * @return Returns the previousMsgId.
   */
  public String getPreviousMsgId()
  {
    return previousMsgId;
  }
  /**
   * @param previousMsgId The previousMsgId to set.
   */
  public void setPreviousMsgId(String previousMsgId)
  {
    this.previousMsgId = previousMsgId;
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
  
  
  
}

/**********************************************************************************
*
* $Header$
*
**********************************************************************************/