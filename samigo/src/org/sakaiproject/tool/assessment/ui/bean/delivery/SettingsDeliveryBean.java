/*
 /**********************************************************************************
 * $HeadURL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.util.Set;

/**
 * Created on May 28, 2004
 * @author casong
 * @author Ed Smiley esmiley@stanford.edu
 *
 * Used to be QtiSettingsDeliveryBean
 */
public class SettingsDeliveryBean
{
  private String username;
  private String password;
  private Set ipAddresses;
  private boolean unlimitedAttempts;
  private java.util.Date dueDate;
  private java.util.Date feedbackDate;
  private boolean autoSave;
  private boolean autoSubmit;
  private boolean formatByPart;
  private boolean formatByQuestion;
  private boolean formatByAssessment;
  private int maxAttempts;
  private String submissionMessage;
  private String bgcolor;
  private String background;
  private String itemNumbering;

  /**
   * Maximum number of attemtps allowed.
   * @return
   */
  public int getMaxAttempts()
  {
    return maxAttempts;
  }

  /**
   * Maximum number of attemtps allowed.
   * @param string
   */
  public void setMaxAttempts(int maxAttempts)
  {
    this.maxAttempts = maxAttempts;
  }

  /**
   * Is this auto-submit? Is auto submit turned on?
   * @return is this auto-submit?
   */
  public boolean getAutoSubmit()
  {
    return autoSubmit;
  }

  /**
   * Is auto save turned on?
   * @return
   */
  public boolean isAutoSave()
  {
    return autoSave;
  }

  /**
   * Due date for assessment.
   * @return Date
   */
  public java.util.Date getDueDate()
  {
    return dueDate;
  }

  /**
   * Feedback date for assessment.
   * @return
   */
  public java.util.Date getFeedbackDate()
  {
    return feedbackDate;
  }

  /**
   * Is auto submit turned on?
   * @param boolean autoSubmit
   */
  public void setAutoSubmit(boolean autoSubmit)
  {
    this.autoSubmit = autoSubmit;
  }

  /**
   * Is auto save turned on?
   * @param boolean autoSave
   */
  public void setAutoSave(boolean autoSave)
  {
    this.autoSave= autoSave;
  }

  /**
   * Set assessment due date.
   * @param due date
   */
  public void setDueDate(java.util.Date dueDate)
  {
    this.dueDate = dueDate;
  }

  /**
   * the date at which to display feedback
   * @param the date at which to display feedback
   */
  public void setFeedbackDate(java.util.Date feedbackDate)
  {
    this.feedbackDate = feedbackDate;
  }

  /**
   * if required, assessment password
   * @return password
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * if required, assessment user name
   * @return user name
   */
  public String getUsername()
  {
    if (username == null)
      return "";
    return username;
  }

  /**
   * if required, assessment password
   * @param string assessment password
   */
  public void setPassword(String string)
  {
    password = string;
  }

  /**
   * if required, assessment user name
   * @param string assessment user name
   */
  public void setUsername(String string)
  {
    username = string;
  }

  /**
   * Set of IP Addresses.
   * @return list of IP Addresses.
   */
  public Set getIpAddresses()
  {
    return ipAddresses;
  }

  /**
   * Set list of IP Addresses.
   * @param string list of IP Addresses.
   */
  public void setIpAddresses(Set set)
  {
    ipAddresses = set;
  }

  /**
   * No limit to number of attempts.  True or false?
   * @return
   */
  public boolean isUnlimitedAttempts() {
    return unlimitedAttempts;
  }
  /**
   * Limit to number of attempts.  Unlimited-- rue or false?
   * @param unlimitedAttempts
   */
  public void setUnlimitedAttempts(boolean unlimitedAttempts) {
    this.unlimitedAttempts = unlimitedAttempts;
  }

  /**
   * Is there one part per page?
   * @return Is there one part per page?
   */
  public boolean isFormatByPart()
  {
    return formatByPart;
  }
  /**
   * Is there one part per page?
   * @param formatByPart Is there one part per page?
   */
  public void setFormatByPart(boolean formatByPart)
  {
    this.formatByPart = formatByPart;
  }

  /**
   * Is there one question per page?
   * @return Is there one question per page?
   */
  public boolean isFormatByQuestion()
  {
    return formatByQuestion;
  }

  /**
   * Is there one question per page?
   * @param formatByQuestion Is there one question per page?
   */
  public void setFormatByQuestion(boolean formatByQuestion)
  {
    this.formatByQuestion = formatByQuestion;
  }

  /**
   * Is there the entire assessment on one page?
   * @return Is there the entire assessment on one page?
   */
  public boolean isFormatByAssessment()
  {
    return formatByAssessment;
  }
  /**
   * Is there the entire assessment on one page?
   * @param formatByAssessment Is there the entire assessment on one page if true.
   */
  public void setFormatByAssessment(boolean formatByAssessment)
  {
    this.formatByAssessment = formatByAssessment;
  }

  /**
   * Displayed on submission to grading.
   * @return message on submission to grading.
   */
  public String getSubmissionMessage()
  {
    return submissionMessage;
  }

  /**
   * Message on submission to grading.
   * @param submissionMessage displayed on submission to grading.
   */
  public void setSubmissionMessage(String submissionMessage)
  {
    this.submissionMessage = submissionMessage;
  }

  public String getBgcolor()
  {
      return checkColor(bgcolor,"bgcolor='white'");
  }

  public void setBgcolor(String color)
  {
    bgcolor = "bgcolor=" + color;
  }

  public String getBackground()
  {

      return checkColor(background,"background='white'");
  }

  public void setBackground(String bg)
  {
    background="background=" + bg;
  }

  public String checkColor(String color,String defaultcolor)
  {
    if (color==null|| color.equals(""))
    {
      return defaultcolor;
    }
    else
    {
      return color;
    }
  }

  public String getItemNumbering()
  {
    return itemNumbering;
  }

  public void setItemNumbering(String numbering)
  {
    itemNumbering = numbering;
  }
}
