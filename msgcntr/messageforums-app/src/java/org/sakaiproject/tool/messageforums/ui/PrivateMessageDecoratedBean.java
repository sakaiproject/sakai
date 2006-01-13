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
  
  private boolean hasRead = false;
  private int depth;
  private boolean hasNext;
  private boolean hasPre;
  private PrivateMessage uiInReply = null;
  //This string is for display of recipients in Received folder- comman separated list of users
  private String sendToStringDecorated=""; 
  
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
  
}

/**********************************************************************************
*
* $Header$
*
**********************************************************************************/