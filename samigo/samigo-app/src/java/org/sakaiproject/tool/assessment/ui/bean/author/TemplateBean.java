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



package org.sakaiproject.tool.assessment.ui.bean.author;

//import org.navigoproject.osid.assessment.AssessmentServiceDelegate;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.business.entity.RecordingData;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * <p>  JSF backing bean for Template pages
 * </p>
 *
 */
public class TemplateBean implements Serializable
{
  private static final long serialVersionUID = 7526471155622776147L;
  private HashMap values = new HashMap();
  private String newName;
  private String templateName;
  private String templateAuthor;
  private String templateDescription;
  private String itemAccessType = "2";
  private String displayChunking = "1";
  private String questionNumbering = "1";
  private String displayScoreDuringAssessments = "1";
  private Boolean markForReview = Boolean.TRUE;
  private String submissionModel = "1";
  private String submissionNumber;
  private String lateHandling = "2";
  private Boolean automaticSubmission = Boolean.FALSE;
  private String autoSave = "1";
  private String feedbackType = "1";
  private String feedbackAuthoring = "1";
  private Boolean feedbackComponent_QuestionText = Boolean.TRUE;
  private Boolean feedbackComponent_StudentResp = Boolean.TRUE;
  private Boolean feedbackComponent_CorrectResp = Boolean.TRUE;
  private Boolean feedbackComponent_StudentScore = Boolean.TRUE;
  private Boolean feedbackComponent_StudentQuestionScore = Boolean.TRUE;
  private Boolean feedbackComponent_QuestionLevel = Boolean.TRUE;
  private Boolean feedbackComponent_SelectionLevel = Boolean.FALSE;
  private Boolean feedbackComponent_GraderComments = Boolean.TRUE;
  private Boolean feedbackComponent_Statistics = Boolean.TRUE;
  private HashMap feedbackTypes = new HashMap();
  private String anonymousGrading = "1";
  private String toGradebook = "1";
  private String recordedScore = "1";
  private RecordingData recordingData;
  private String idString;
  private String createdDate;
  private String createdBy;
  private String lastModified;
  private String lastModifiedBy;
  private Date modifiedDate;
  private String outcome;
  
  /**
   * This just sets some defaults.
   */
  public TemplateBean()
  {
    // Set correct defaults
    values.put("assessmentAuthor_isInstructorEditable", Boolean.TRUE);
    values.put("description_isInstructorEditable", Boolean.TRUE);
    values.put("dueDate_isInstructorEditable", Boolean.TRUE);
    values.put("releaseDate_isInstructorEditable", Boolean.TRUE);
    values.put("lateHandling_isInstructorEditable", Boolean.TRUE);
    values.put("markForReview_isInstructorEditable", Boolean.FALSE);
    values.put("automaticSubmission_isInstructorEditable", Boolean.FALSE);
    values.put("feedbackType_isInstructorEditable", Boolean.TRUE);
    values.put("feedbackAuthoring_isInstructorEditable", Boolean.TRUE);
    values.put("feedbackComponents_isInstructorEditable", Boolean.TRUE);
    values.put("anonymousRelease_isInstructorEditable", Boolean.TRUE);
    values.put("authenticatedRelease_isInstructorEditable", Boolean.TRUE);     
  }

    public void setOutcome(String outcome){
	this.outcome=outcome;
    }
    public String getOutcome(){
	return outcome;
    }

  /**
   * set value
   *
   * @param key
   * @param value
   */
  public void setValue(String key, Object value)
  {
    values.put(key, value);
  }

  /**
   * get value
   *
   * @param key
   *
   * @return
   */
  public Object getValue(String key)
  {
    if (values.get(key) == null)
    {
      return Boolean.FALSE;
    }

    return values.get(key);
  }

  /**
   * set value map
   *
   * @param newMap
   */
  public void setValueMap(HashMap newMap)
  {
    values = newMap;
  }

  /**
   * get value map
   *
   * @return
   */
  public HashMap getValueMap()
  {
    return values;
  }

  /**
   * feedback type
   *
   * @param key
   * @param value
   */
  public void setFeedbackType(String key, Object value)
  {
    values.put(key, value);
  }

  /**
   * feedback tye
   *
   * @param key
   *
   * @return
   */
  public String getFeedbackType(String value)
  {
    if (value == null || value.trim().equals(""))
      return "2";
    return feedbackType;
  }

 /*
   * feedback Authoring
   *
   * @param key
   * @param value
   
  public void setFeedbackAuthoring(String key, Object value)
  {
      values.put(key,value);
  }
 */

  /**
   * feedback Authoring
   *
   * @param key
   *
   * @return
   
  public String getFeedbackAuthoring(String value)
  { 
    if (value == null || value.trim().equals(""))
      return "2";
    return feedbackAuthoring;
   
  }
  */

  /**
   * set feedback type map
   * @param newMap
   */
  public void setFeedbackTypeMap(String newType)
  {
    feedbackType = newType;
  }

  /**
   * get feedback type mape
   *
   * @return
   */
  public HashMap getFeedbackTypeMap()
  {
    return feedbackTypes;
  }

  /**
   * set feedback authoring map
   * @param newMap
   
  public void setFeedbackAuthoringMap(String newAuthoring)
  {
    feedbackAuthoring = newAuthoring;
  }
  */

  /**
   * get feedback authoring mape
   *
   * @return
   
  public HashMap getFeedbackAuthoringMap()
  {
    return feedbackAuthorings;
  }
  */

  /**
   * template name
   *
   * @param newName the name
   */
  public void setNewName(String newName)
  {
    this.newName = newName;
  }

  /**
   * tempalte name
   *
   * @return the name
   */
  public String getNewName()
  {
    return checker(newName, "");
  }

  /**
   * template name
   *
   * @param newName the name
   */
  public void setTemplateName(String newName)
  {
    templateName = newName;
  }

  /**
   * tempalte name
   *
   * @return the name
   */
  public String getTemplateName()
  {
    return checker(templateName, "");
  }

  /**
   * suthor
   *
   * @param newAuthor aquthor
   */
  public void setTemplateAuthor(String newAuthor)
  {
    templateAuthor = newAuthor;
  }

  /**
   * author
   *
   * @return author
   */
  public String getTemplateAuthor()
  {
    return checker(templateAuthor, "");
  }

  /**
   * description
   *
   * @param newDescription description
   */
  public void setTemplateDescription(String newDescription)
  {
    templateDescription = newDescription;
  }

  /**
   * description
   *
   * @return description
   */
  public String getTemplateDescription()
  {
    return checker(templateDescription, "");
  }

  /**
   * item access type
   *
   * @param newItemAccessType item access type
   */
  public void setItemAccessType(String newItemAccessType)
  {
    itemAccessType = newItemAccessType;
  }

  /**
   * item access type
   *
   * @return item access type
   */
  public String getItemAccessType()
  {
    if (itemAccessType == null || itemAccessType.trim().equals("") ||
        itemAccessType.equals("0"))
      return "2";
    return itemAccessType;
  }

  /**
   * numbering
   *
   * @param newQuestionNumbering
   */
  public void setQuestionNumbering(String newQuestionNumbering)
  {
    questionNumbering = newQuestionNumbering;
  }

  /**
   * numbering
   *
   * @return
   */
  public String getQuestionNumbering()
  {
    if ("0".equals(questionNumbering))
      return "1";
    return checker(questionNumbering, "1");
  }
  
  /**
   * displayScoreDuringAssessments
   *
   * @param displayScoreDuringAssessments
   */
  public void setDisplayScoreDuringAssessments(String displayScoreDuringAssessments)
  {
	  displayScoreDuringAssessments = displayScoreDuringAssessments;
  }

  /**
   * displayScoreDuringAssessments
   *
   * @return
   */
  public String getDisplayScoreDuringAssessments()
  {
    if ("0".equals(displayScoreDuringAssessments))
      return "1";
    return checker(displayScoreDuringAssessments, "1");
  }
  
  /**
   * numbering
   *
   * @param newDisplayChunking
   */
  public void setDisplayChunking(String newDisplayChunking)
  {
    displayChunking = newDisplayChunking;
  }

  /**
   * chunking
   *
   * @return
   */
  public String getDisplayChunking()
  {
    if (displayChunking == null || displayChunking.trim().equals("")
        || displayChunking.equals("0"))
      return "1";
    return displayChunking;
  }
  
  /**
   * submission model
   *
   * @param newSubmissionModel submission model
   */
  public void setMarkForReview(Boolean newMarkForReview)
  {
    markForReview = newMarkForReview;
  }

  /**
   * submission model
   *
   * @return submission model
   */
  public Boolean getMarkForReview()
  {
    return markForReview;
  }

  /**
   * submission model
   *
   * @param newSubmissionModel submission model
   */
  public void setSubmissionModel(String newSubmissionModel)
  {
    submissionModel = newSubmissionModel;
  }

  /**
   * submission model
   *
   * @return submission model
   */
  public String getSubmissionModel()
  {
    return submissionModel;
  }

  /**
   * submission number
   *
   * @param newSubmissionNumber submission number
   */
  public void setSubmissionNumber(String newSubmissionNumber)
  {
    submissionNumber = newSubmissionNumber;
  }

  /**
   * submission number
   *
   * @return submission number
   */
  public String getSubmissionNumber()
  {
    return submissionNumber;
  }

  /**
   * automaticSubmission
   *
   * @param newAutomaticSubmission  3
   */
  public void setAutomaticSubmission(Boolean newAutomaticSubmission)
  {
	  automaticSubmission = newAutomaticSubmission;
  }

  /**
   * automaticSubmission
   *
   * @return
   */
  public Boolean getAutomaticSubmission()
  {
    return automaticSubmission;
  }

  /**
   * autosave
   *
   * @param newAutoSave  3
   */
  public void setAutoSave(String newAutoSave)
  {
    autoSave = newAutoSave;
  }

  /**
   * autosave
   *
   * @return
   */
  public String getAutoSave()
  {
    if ("0".equals(autoSave))
      return "1";
    return checker(autoSave, "1");
  }

  /**
   * late handling
   *
   * @param newLateHandling late handling
   */
  public void setLateHandling(String newLateHandling)
  {
    lateHandling = newLateHandling;
  }

  /**
   * late handling
   *
   * @return laqte handling
   */
  public String getLateHandling()
  {
    if ("0".equals(lateHandling))
    {
      return "2";
    }
    return checker(lateHandling, "2");
  }

  // Feedback
  public void setFeedbackType(String newFeedbackType)
  {
    feedbackType = newFeedbackType;
  }

  /**
   *
   *
   * @return feedback type
   */
  public String getFeedbackType()
  {
    if ("0".equals(feedbackType))
      return "1";
    return checker(feedbackType, "1");
  }

  // FeedAuthoring
  public void setFeedbackAuthoring(String newFeedbackAuthoring)
  {
    feedbackAuthoring = newFeedbackAuthoring;
  }

  /**
   *
   *
   * @return feedback authoring
   */
  public String getFeedbackAuthoring()
  {
    if ("0".equals(feedbackAuthoring))
      return "1";
    return checker(feedbackAuthoring, "1");
  }

  /**
   * feedback component...
   *
   * @param newComponent
   */
  public void setFeedbackComponent_QuestionText(Boolean newComponent)
  {
    feedbackComponent_QuestionText = newComponent;
  }

  /**
   * feedback component...
   *
   * @return feedback component...
   */
  public Boolean getFeedbackComponent_QuestionText()
  {
    return bchecker(feedbackComponent_QuestionText, true);
  }

  /**
   * feedback component...
   *
   * @param newComponent feedback component...
   */
  public void setFeedbackComponent_StudentResp(Boolean newComponent)
  {
    feedbackComponent_StudentResp = newComponent;
  }

  /**
   * feedback component...
   *
   * @return feedback component...
   */
  public Boolean getFeedbackComponent_StudentResp()
  {
    return bchecker(feedbackComponent_StudentResp, true);
  }

  /**
   * feedback component...
   *
   * @param newComponent feedback component...
   */
  public void setFeedbackComponent_CorrectResp(Boolean newComponent)
  {
    feedbackComponent_CorrectResp = newComponent;
  }

  /**
   * feedback component...
   *
   * @return feedback component...
   */
  public Boolean getFeedbackComponent_CorrectResp()
  {
    return bchecker(feedbackComponent_CorrectResp, true);
  }

  /**
   * feedback component...
   *
   * @param newComponent feedback component...
   */
  public void setFeedbackComponent_StudentScore(Boolean newComponent)
  {
    feedbackComponent_StudentScore = newComponent;
  }

  /**
   * feedback component...
   *
   * @return feedback component...
   */
  public Boolean getFeedbackComponent_StudentScore()
  {
    return bchecker(feedbackComponent_StudentScore, true);
  }

 /**
   * feedback component...
   *
   * @param newComponent feedback component...
   */
  public void setFeedbackComponent_StudentQuestionScore(Boolean newComponent)
  {
    feedbackComponent_StudentQuestionScore = newComponent;
  }

  /**
   * feedback component...
   *
   * @return feedback component...
   */
  public Boolean getFeedbackComponent_StudentQuestionScore()
  {
    return bchecker(feedbackComponent_StudentQuestionScore, true);
  }


  /**
   * feedback component...
   *
   * @param newComponent feedback component...
   */
  public void setFeedbackComponent_QuestionLevel(Boolean newComponent)
  {
    feedbackComponent_QuestionLevel = newComponent;
  }

  /**
   * v
   *
   * @return feedback component...
   */
  public Boolean getFeedbackComponent_QuestionLevel()
  {
    return bchecker(feedbackComponent_QuestionLevel, true);
  }

  /**
   * feedback component...
   *
   * @param newComponent feedback component...
   */
  public void setFeedbackComponent_SelectionLevel(Boolean newComponent)
  {
    feedbackComponent_SelectionLevel = newComponent;
  }

  /**
   * feedback component...
   *
   * @return feedback component...
   */
  public Boolean getFeedbackComponent_SelectionLevel()
  {
    return bchecker(feedbackComponent_SelectionLevel, false);
  }

  /**
   * feedback component...
   *
   * @param newComponent feedback component...
   */
  public void setFeedbackComponent_GraderComments(Boolean newComponent)
  {
    feedbackComponent_GraderComments = newComponent;
  }

  /**
   * feedback component...
   *
   * @return feedback component...
   */
  public Boolean getFeedbackComponent_GraderComments()
  {
    return bchecker(feedbackComponent_GraderComments, true);
  }

  /**
   * feedback component...
   *
   * @param newComponent feedback component...
   */
  public void setFeedbackComponent_Statistics(Boolean newComponent)
  {
    feedbackComponent_Statistics = newComponent;
  }

  /**
   * feedback component...
   *
   * @return feedback component...
   */
  public Boolean getFeedbackComponent_Statistics()
  {
    return bchecker(feedbackComponent_Statistics, true);
  }

  /**
   * anonymous grading
   * @param newAnonymousGrading
   */
  public void setAnonymousGrading(String newAnonymousGrading)
  {
    anonymousGrading = newAnonymousGrading;
  }

  /**
   * anonymous grading
   * @return
   */
  public String getAnonymousGrading()
  {
    if ("0".equals(anonymousGrading))
      return "1";
    return checker(anonymousGrading, "1");
  }

  /**
   *
   *
   * @param newToGradebook
   */
  public void setToGradebook(String newToGradebook)
  {
    toGradebook = newToGradebook;
  }

  /**
   *
   *
   * @return to what gradebook
   */
  public String getToGradebook()
  {
    if ("0".equals(toGradebook))
      return "1";
    return checker(toGradebook, "1");
  }

  /**
   *
   *
   * @param newRecordedScore
   */
  public void setRecordedScore(String newRecordedScore)
  {
    recordedScore = newRecordedScore;
  }

  /**
   *
   *
   * @return the recorded score
   */
  public String getRecordedScore()
  {
    if ("0".equals(recordedScore))
      return "1";
    return checker(recordedScore, "1");
  }

  /**
   * encapsulates audio recording info
   * @return recording data
   */
  public RecordingData getRecordingData()
  {
    return this.recordingData;
  }

  /**
   * encapsulates audio recording info
   * @param rd
   */
  public void setRecordingData(RecordingData rd)
  {
    this.recordingData = rd;
  }

  /**
   * get id string
   * @return id
   */
  public String getIdString()
  {
    return checker(this.idString, "0");
  }

  /**
   * set id string
   * @param idString
   */
  public void setIdString(String idString)
  {
    this.idString = idString;
  }

  /**
   * Get created date
   */
  public String getCreatedDate()
  {
    return checker(createdDate, "N/A");
  }

  /**
   * Set created date
   */
  public void setCreatedDate(String newDate)
  {
    createdDate = newDate;
  }

  /**
   * Get created by
   */
  public String getCreatedBy()
  {
    return checker(createdBy, "N/A");
  }

  /**
   * Set created by
   */
  public void setCreatedBy(String agent)
  {
    createdBy = agent;
  }

  /**
   * Get last modified date
   */
  public String getLastModified()
  {
    return checker(lastModified, "N/A");
  }

    public Date getModifiedDate(){

        DateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd hh:ss:mm.SSS");
       	try{
         modifiedDate=dateFm.parse(getLastModified());
	}
	catch(ParseException e){
	    e.printStackTrace();
       }
    return modifiedDate;

    }

  /**
   * Set last modified date
   */
  public void setLastModified(String newDate)
  {
    lastModified = newDate;
  }

  /**
   * Get last modified by
   */
  public String getLastModifiedBy()
  {
    return checker(lastModifiedBy, "N/A");
  }

  /**
   * Set last modified by
   */
  public void setLastModifiedBy(String agent)
  {
    lastModifiedBy = agent;
  }

  private String checker(String mytest, String mydefault)
  {
    if (mytest == null || mytest.trim().equals(""))
      return mydefault;
    return mytest;
  }

  private Boolean bchecker(Boolean mytest, boolean mydefault)
  {
    if (mytest == null)
      return Boolean.valueOf(mydefault);
    return mytest;
  }

  private String typeId;
  public void setTypeId(String typeId)
  {
    this.typeId = typeId;
  }

  public String getTypeId()
  {
    return typeId;
  }
  
  protected String groups[] = {"Group 1", "Group 2", "Group 3"};
  protected String selectedGroups[] = new String[0];

  public String[] getGroups() {
	  return groups;
  }

  public void setGroups(String[] groups) {
	  this.groups = groups;
  }

  public String[] getSelectedGroups() {
	  return selectedGroups;
  }

  public void setSelectedGroups(String[] selectedGroups) {
	  this.selectedGroups = selectedGroups;
  }
  
  public boolean getShowAssessmentTypes() {
	  return ServerConfigurationService.getBoolean("samigo.showAssessmentTypes", false);
  }

  public boolean isSecureDeliveryAvailable() {
	  SecureDeliveryServiceAPI secureDeliveryService = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI(); 
	  return secureDeliveryService.isSecureDeliveryAvaliable();
  }
}
