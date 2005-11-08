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
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.PrivateMessageManager;
import org.sakaiproject.api.app.messageforums.Topic;

import org.sakaiproject.component.app.messageforums.dao.hibernate.TopicImpl;
import org.sakaiproject.service.legacy.security.cover.SecurityService;

public class PrivateMessagesTool
{
  private static final Log LOG = LogFactory.getLog(PrivateMessagesTool.class);

  private PrivateMessageManager prtMsgManager;
  private ErrorMessages errorMessages;

  public PrivateMessagesTool()
  {
//    errorMessages = new ErrorMessages();
//    errorMessages.setDisplayTitleErrorMessage(false);
//
//    Topic topic = new TopicImpl();
//    topic.setTitle("Dubai Port Authority Case");
//    topic
//        .setShortDescription("What scope and partners do you recommend for the proposed system?  Provide one sentence of support for your position.");
//    topic.setExtendedDescription("...");
//    
  }

 
  public PrivateMessageManager getPrtMsgManager()
  {
    return prtMsgManager;
  }


  public void setPrtMsgManager(PrivateMessageManager prtMsgManager)
  {
    this.prtMsgManager = prtMsgManager;
  }


  public Area getArea()
  {
   return prtMsgManager.getPrivateArea();
  }
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

  public String processPvtMsgReceived()
  {

    return "pvtMsgReceived";
  }

  public String processPvtMsgSent()
  {

    return "pvtMsgSent";
  }

  public String processPvtMsgDelete()
  {
    return "pvtMsgDelete";
  }

  public String processPvtMsgDrafts()
  {
    return "pvtMsgDrafts";
  }

  public String processPvtMsgCase()
  {
    return "pvtMsgCase";
  }

  public String processPvtMsgGrpCorres()
  {
    return "pvtMsgGrpCorres";
  }

  public String processPvtMsgCancel()
  {
    return "main";
  }
  public String getTest()
  {
    return "TESTING TOOL BEAN";
  }

  public String processPvtMsgSettingRevise() {
     String email= getForwardPvtMsgEmail();
    String test=getActivatePvtMsg() ;
    //TODO - save private message settings here
    return "main" ;
  }
  
  //Received screen
  public String processPvtMsgCompose() {    
    return "compose" ;
  }
  public String processPvtMsgDispOtions() {
    return "pvtMsgOrganize" ;
  }
  public String processPvtMsgFldrSettings() {
    return "pvtMsgSettings" ;
  }
  
  //////// GETTER AND SETTER  ///////////////////
  //Setting Screen
  private String activatePvtMsg="yes";
  private boolean forwardPvtMsg;
  private String forwardPvtMsgEmail;
  private boolean superUser; 
  
  //Received Screen
  private List receivedItems ;
  //////////////////////////////////////////////////////////////
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
  public List getReceivedItems()
  {
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

}