/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.bean.delivery;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;

import java.io.Serializable;
import java.util.Set;
import java.util.Iterator;

/**
 * Assessment Settings used in Delivery   
 */
public class SettingsDeliveryBean implements Serializable
{
	/** Use serialVersionUID for interoperability. */
	private final static long serialVersionUID = -1090852048737428722L;

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
  private String displayScoreDuringAssessments;

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
    if (password == null)
    	return "";	  
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
    return checkColor(bgcolor,"bgcolor=transparent");
  }

  public void setBgcolor(String color)
  {
    bgcolor = "bgcolor=" + color;
  }

  public String getBackground()
  {
      return background;
 
  }

  public void setBackground(String bg)
  {
    background="background=" + bg;
  }

    public String getDivBgcolor()
    {
        
	return "background:"+this.getBgcolor().substring(8);
    }

    public String getDivBackground()
    {
	if (getBackground()!=null && !getBackground().equals("")){
	    String divbg= "background-image:url("+this.getBackground().substring(11)+")";
	    return divbg;
	}
        else

	    return "";
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

  public String getDisplayScoreDuringAssessments()
  {
	  return displayScoreDuringAssessments;
  }
  
  public void setDisplayScoreDuringAssessments(String displayScoreDuringAssessments){
	  this.displayScoreDuringAssessments = displayScoreDuringAssessments;
  }
  
  public void setAssessmentAccessControl(PublishedAssessmentIfc pubAssessment){

    AssessmentAccessControlIfc control = pubAssessment.getAssessmentAccessControl();
    setAutoSubmit(control.AUTO_SUBMIT.equals(control.getAutoSubmit()));
    setAutoSave(control.AUTO_SAVE.equals(control.getSubmissionsSaved()));
    setDueDate(control.getDueDate());
    if ((Boolean.TRUE).equals(control.getUnlimitedSubmissions())){
      setUnlimitedAttempts(true);
    }
    else{
      setUnlimitedAttempts(false);
      if (control.getSubmissionsAllowed() != null) {
        setMaxAttempts(control.getSubmissionsAllowed().intValue());
      }
    }
    setSubmissionMessage(control.getSubmissionMessage());
    setFeedbackDate(control.getFeedbackDate());
    Integer format = control.getAssessmentFormat();
    if (format == null)
      format =  Integer.valueOf(1);
    setFormatByAssessment(control.BY_ASSESSMENT.equals(format));
    setFormatByPart(control.BY_PART.equals(format));
    setFormatByQuestion(control.BY_QUESTION.equals(format));
    setUsername(control.getUsername());
    setPassword(control.getPassword());
    setItemNumbering(control.getItemNumbering().toString());
    if (control != null && control.getDisplayScoreDuringAssessments() != null) {
      setDisplayScoreDuringAssessments(control.getDisplayScoreDuringAssessments().toString());
    }
    else {
      setDisplayScoreDuringAssessments("0");
    }

    setIpAddresses(pubAssessment.getSecuredIPAddressSet());

    Set set = pubAssessment.getAssessmentMetaDataSet();
    Iterator iter = set.iterator();
    while (iter.hasNext()) {
      AssessmentMetaDataIfc data = (AssessmentMetaDataIfc) iter.next();
      if (data.getLabel().equals(AssessmentMetaDataIfc.BGCOLOR))
        setBgcolor(data.getEntry());
      else if (data.getLabel().equals(AssessmentMetaDataIfc.BGIMAGE))
        setBackground(data.getEntry());
    }
  }

}
