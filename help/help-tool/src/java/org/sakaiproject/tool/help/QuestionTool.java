/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.help;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.help.HelpManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * question tool
 * @version $Id$
 */
@Slf4j
public class QuestionTool
{

  private String lastName;
  private String firstName;
  private String userName;
  private String emailAddress;
  private String subject;
  private String content;

  private String toEmailAddress;
  private EmailService emailService;
  private HelpManager helpManager;

  /**
   * get help manager
   * @return Returns the helpManager.
   */
  public HelpManager getHelpManager()
  {
    return helpManager;
  }

  /**
   * set help manager
   * @param helpManager The helpManager to set.
   */
  public void setHelpManager(HelpManager helpManager)
  {
    this.helpManager = helpManager;
  }

  /**
   * get email address
   * @return Returns the emailAddress.
   */
  public String getEmailAddress()
  {
    return emailAddress;
  }

  /**
   * set email address
   * @param emailAddress The emailAddress to set.
   */
  public void setEmailAddress(String emailAddress)
  {
    this.emailAddress = emailAddress;
  }

  /**
   * get first name
   * @return Returns the firstName.
   */
  public String getFirstName()
  {
    return firstName;
  }

  /**
   * set first name
   * @param firstName The firstName to set.
   */
  public void setFirstName(String firstName)
  {
    this.firstName = firstName;
  }

  /**
   * get last name
   * @return Returns the lastName.
   */
  public String getLastName()
  {
    return lastName;
  }

  /**
   * set last name
   * @param lastName The lastName to set.
   */
  public void setLastName(String lastName)
  {
    this.lastName = lastName;
  }

  /**
   * get subject
   * @return Returns the subject.
   */
  public String getSubject()
  {
    return subject;
  }

  /**
   * set subject
   * @param subject The subject to set.
   */
  public void setSubject(String subject)
  {
    this.subject = subject;
  }

  /**
   * get user name
   * @return Returns the userName.
   */
  public String getUserName()
  {
    return userName;
  }

  /**
   * set user name
   * @param userName The userName to set.
   */
  public void setUserName(String userName)
  {
    this.userName = userName;
  }

  /**
   * get email service
   * @return Returns the emailService.
   */
  public EmailService getEmailService()
  {
    return emailService;
  }

  /**
   * set email service
   * @param emailService The emailService to set.
   */
  public void setEmailService(EmailService emailService)
  {
    this.emailService = emailService;
  }

  /**
   * get to email address
   * @return Returns the toEmailAddress.
   */
  public String getToEmailAddress()
  {
    if (toEmailAddress == null)
    {
      toEmailAddress = helpManager.getSupportEmailAddress();
    }
    return toEmailAddress;
  }

  /**
   * set to email address
   * @param toEmailAddress The toEmailAddress to set.
   */
  public void setToEmailAddress(String toEmailAddress)
  {
    this.toEmailAddress = toEmailAddress;
  }

  /**
   * get detailed content
   * @return content
   */
  public String getDetailedContent()
  {

    String UNAVAILABLE = "~unavailable~";

    String IP = UNAVAILABLE;
    String agent = UNAVAILABLE;
    String sessionId = UNAVAILABLE;
    String serverName = UNAVAILABLE;

    if (UsageSessionService.getSession() != null)
    {

      IP = UsageSessionService.getSession().getIpAddress();
      agent = UsageSessionService.getSession().getUserAgent();
      sessionId = UsageSessionService.getSession().getId();
      serverName = ServerConfigurationService.getServerName();
    }

    String detailedContent = "\n\n" + "Sender's name: " + this.firstName + " "
        + this.lastName + "\n" + "Sender's UserName: " + userName + "\n"
        + "Sender's IP: " + IP + "\n" + "Sender's Browser/Agent: " + agent
        + "\n" + "Sender's SessionID: " + sessionId + "\n" + "Server Name: "
        + serverName + "\n" + "Comments or questions: \n" + this.getContent()
        + "\n\n" + "Sender's (reply-to) email: " + emailAddress + "\n\n"
        + "Site: Help Tool" + "\n" + "Site Id: "
        + ToolManager.getCurrentPlacement().getContext() + "\n";

    return detailedContent;

  }

  /**
   * submit question
   * @return view
   */
  public String submitQuestion()
  {
    this.sendEmail();
    return "display";
  }

  /**
   * reset
   * @return view
   */
  public String reset()
  {
    this.content = "";
    this.subject = "";
    this.firstName = "";
    this.lastName = "";
    this.emailAddress = "";
    this.userName = "";

    return "main";
  }

  /**
   * submit question cancel
   * @return
   */
  public String submitQuestionCancel()
  {
    return this.reset();
  }

  /**
   * send email
   */
  private void sendEmail()
  {
    try
    {
      String detailedContent = getDetailedContent();
      emailService.send(emailAddress, this.getToEmailAddress(), subject,
          detailedContent, null, null, null);
    }
    catch (Exception e)
    {
      log.error("email service is not set up correctly, can't send user question to support consultant!", e);
    }
  }

  /**
   * get content
   * @return Returns the content.
   */
  public String getContent()
  {
    return content;
  }

  /**
   * set content
   * @param content The content to set.
   */
  public void setContent(String content)
  {
    this.content = content;
  }
}
