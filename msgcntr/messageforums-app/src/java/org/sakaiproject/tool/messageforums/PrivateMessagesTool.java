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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.proxy.TopicProxy;
import org.sakaiproject.component.app.messageforums.dao.hibernate.TopicImpl;
import org.sakaiproject.tool.messageforums.proxy.TopicProxyImpl;

public class PrivateMessagesTool
{
  private static final Log LOG = LogFactory.getLog(PrivateMessagesTool.class);

  private TopicProxy topicProxy; // TODO: can be deleted as soon as more of the backend works
  private MessageForumsMessageManager messageForumsMessageManager;
  private MessageForumsForumManager messageForumsForumManager;
  private ErrorMessages errorMessages;

  public PrivateMessagesTool()
  {
    errorMessages = new ErrorMessages();
    errorMessages.setDisplayTitleErrorMessage(false);

    Topic topic = new TopicImpl();
    topic.setTitle("Dubai Port Authority Case");
    topic
        .setShortDescription("What scope and partners do you recommend for the proposed system?  Provide one sentence of support for your position.");
    topic.setExtendedDescription("...");
    topicProxy = new TopicProxyImpl(topic);
  }

  // start injections
  public void setMessageForumsMessageManager(
      MessageForumsMessageManager messageForumsMessageManager)
  {
    this.messageForumsMessageManager = messageForumsMessageManager;
  }

  public void setMessageForumsForumManager(
      MessageForumsForumManager messageForumsForumManager)
  {
    this.messageForumsForumManager = messageForumsForumManager;
  }

  // end injections

  public MessageForumsMessageManager getMessageForumsMessageManager()
  {
    return messageForumsMessageManager;
  }

  public MessageForumsForumManager getMessageForumsForumManager()
  {
    return messageForumsForumManager;
  }

  //
  // start button process actions
  //
  public String processCDFMPostMessage()
  {
    Message message = topicProxy.getMessageModel().createPersistible();
    messageForumsMessageManager.saveMessage(message);
    return "compose";
  }

  public String processCDFMSaveDraft()
  {
    Message message = topicProxy.getMessageModel().createPersistible();
    message.setDraft(Boolean.TRUE);
    messageForumsMessageManager.saveMessage(message);
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

  public TopicProxy getTopicProxy()
  {
    return topicProxy;
  }

  // htripath
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

  // htripath
}