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

package org.sakaiproject.tool.assessment.data.dao.assessment;

import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;

//import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
//import org.sakaiproject.tool.assessment.services.PersistenceService;

import java.util.Date;

/**
 * This keeps track of the submission scheme, and the number allowed.
 *
 * @author Rachel Gollub
 */
public class AssessmentAccessControl
    implements java.io.Serializable, AssessmentAccessControlIfc
{
  // keep in mind that this id can be an assesmentId or assessmentTemplateId.
  // This depends on the AssessmentBase object (superclass of AssessmentData and
  // AssessmentTemplateData) that is associated with it.

  /**
	 * 
	 */
	private static final long serialVersionUID = 8330416434678491916L;
// flag it when no editing on the property is desire
  public static final Integer NO_EDIT = Integer.valueOf(-1);
  // timedAssessment
  public static final Integer TIMED_ASSESSMENT = Integer.valueOf(1);
  public static final Integer DO_NOT_TIMED_ASSESSMENT = Integer.valueOf(0);
  // autoSubmit
  public static final Integer AUTO_SUBMIT = Integer.valueOf(1);
  public static final Integer DO_NOT_AUTO_SUBMIT = Integer.valueOf(0);
  // autoSave
  public static final Integer SAVE_ON_CLICK = Integer.valueOf(1);
  public static final Integer AUTO_SAVE = Integer.valueOf(2);
  // itemNavigation
  public static final Integer LINEAR_ACCESS = Integer.valueOf(1);
  public static final Integer RANDOM_ACCESS = Integer.valueOf(2);
  // assessmentFormat
  public static final Integer BY_QUESTION = Integer.valueOf(1);
  public static final Integer BY_PART = Integer.valueOf(2);
  public static final Integer BY_ASSESSMENT = Integer.valueOf(3);
  // itemNumbering
  public static final Integer CONTINUOUS_NUMBERING = Integer.valueOf(1);
  public static final Integer RESTART_NUMBERING_BY_PART = Integer.valueOf(2);
  //itemScoreDisplay
  public static Integer DISPLAY_ITEM_SCORE_DURING_ASSESSMENT = Integer.valueOf(1);
  public static Integer HIDE_ITEM_SCORE_DURING_ASSESSMENT = Integer.valueOf(2);
  // markForReview
  public static final Integer MARK_FOR_REVIEW = Integer.valueOf(1);
  public static final Integer NOT_MARK_FOR_REVIEW = Integer.valueOf(0);
  // submissionsAllowed
  public static final Integer UNLIMITED_SUBMISSIONS_ALLOWED = Integer.valueOf(9999);
  // lateHandling
  public static final Integer ACCEPT_LATE_SUBMISSION = Integer.valueOf(1);
  public static final Integer NOT_ACCEPT_LATE_SUBMISSION = Integer.valueOf(2);

  private Long id;

  private AssessmentBaseIfc assessmentBase;
  private Integer submissionsAllowed;
  private Boolean unlimitedSubmissions;
  private Integer submissionsSaved;
  private Integer assessmentFormat;
  private Integer bookMarkingItem;
  private Integer timeLimit;
  private Integer timedAssessment;
  private Integer retryAllowed;
  private Integer lateHandling;
  private Integer instructorNotification;
  private Date startDate;
  private Date dueDate;
  private Date scoreDate;
  private Date feedbackDate;
  private Date retractDate;
  private Integer autoSubmit;  // auto submit when time expires
  private Integer itemNavigation; // linear (1)or random (0)
  private Integer itemNumbering;  // continuous between parts(1), restart between parts(0)
  private Integer displayScoreDuringAssessments;
  private String submissionMessage;
  private String finalPageUrl;
  private String releaseTo;
  private String password;
  private Integer markForReview;
  private Boolean honorPledge;

  /**
   * Creates a new SubmissionModel object.
   */
  public AssessmentAccessControl()
  {
    this.submissionsAllowed = Integer.valueOf(9999); // =  no limit
    this.submissionsSaved =  Integer.valueOf(1); // no. of copy
  }

  public AssessmentAccessControl(Integer submissionsAllowed, Integer submissionsSaved,
                                 Integer assessmentFormat, Integer bookMarkingItem,
                                 Integer timeLimit, Integer timedAssessment,
                                 Integer retryAllowed, Integer lateHandling, Integer instructorNotification,
                                 Date startDate, Date dueDate,
                                 Date scoreDate, Date feedbackDate,
                                 Date retractDate, Integer autoSubmit,
                                 Integer itemNavigation, Integer itemNumbering, Integer displayScoreDuringAssessments,
                                 String submissionMessage, String releaseTo)
  {
    this.submissionsAllowed = submissionsAllowed; // =  no limit
    this.submissionsSaved = submissionsSaved; // no. of copy
    this.assessmentFormat = assessmentFormat;
    this.bookMarkingItem =  bookMarkingItem;
    this.timeLimit = timeLimit;
    this.timedAssessment = timedAssessment;
    this.retryAllowed = retryAllowed; // cannot edit(0)
    this.lateHandling = lateHandling; // cannot edit(0)
    this.instructorNotification = instructorNotification;
    this.startDate = startDate;
    this.dueDate = dueDate;
    this.scoreDate = scoreDate;
    this.feedbackDate = feedbackDate;
    this.retractDate = retractDate;
    this.autoSubmit = autoSubmit;  // cannot edit (0) auto submit(1) when time expires (2)
    this.itemNavigation = itemNavigation; // cannot edit (0) linear(1) or random (2)
    this.itemNumbering = itemNumbering;  // cannot edit(0) continuous between parts (1), restart between parts (2)
    this.displayScoreDuringAssessments = displayScoreDuringAssessments;
    this.submissionMessage = submissionMessage;
    this.releaseTo = releaseTo;
  }

  public Object clone() throws CloneNotSupportedException{
    Object cloned = new AssessmentAccessControl(
        this.getSubmissionsAllowed(), this.getSubmissionsSaved(),
        this.getAssessmentFormat(), this.getBookMarkingItem(),
        this.getTimeLimit(), this.getTimedAssessment(),
        this.getRetryAllowed(), this.getLateHandling(), this.getInstructorNotification(),
        this.getStartDate(), this.getDueDate(),
        this.getScoreDate(), this.getFeedbackDate(),
        this.getRetractDate(), this.getAutoSubmit(),
        this.getItemNavigation(), this.getItemNumbering(), this.getDisplayScoreDuringAssessments(),
        this.getSubmissionMessage(), this.getReleaseTo());
    ((AssessmentAccessControl)cloned).setRetractDate(this.retractDate);
    ((AssessmentAccessControl)cloned).setAutoSubmit(this.autoSubmit);
    ((AssessmentAccessControl)cloned).setItemNavigation(this.itemNavigation);
    ((AssessmentAccessControl)cloned).setItemNumbering(this.itemNumbering);
    ((AssessmentAccessControl)cloned).setDisplayScoreDuringAssessments(this.displayScoreDuringAssessments);
    ((AssessmentAccessControl)cloned).setSubmissionMessage(this.submissionMessage);
    ((AssessmentAccessControl)cloned).setPassword(this.password);
    ((AssessmentAccessControl)cloned).setFinalPageUrl(this.finalPageUrl);
    ((AssessmentAccessControl)cloned).setUnlimitedSubmissions(this.unlimitedSubmissions);
    ((AssessmentAccessControl)cloned).setMarkForReview(this.markForReview);
    ((AssessmentAccessControl)cloned).setHonorPledge(this.honorPledge);
    return cloned;
  }

  public Long getId()
  {
    return id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  public void setAssessmentBase(AssessmentBaseIfc assessmentBase)
  {
    this.assessmentBase = assessmentBase;
  }

  public AssessmentBaseIfc getAssessmentBase()
  {
    if (assessmentBase.getIsTemplate().equals(Boolean.TRUE))
      return (AssessmentTemplateIfc)assessmentBase;
    else
      return (AssessmentIfc)assessmentBase;
  }

  public Integer getSubmissionsAllowed()
  {
    return submissionsAllowed;
  }

  public void setSubmissionsAllowed(Integer psubmissionsAllowed)
  {
    submissionsAllowed = psubmissionsAllowed;
  }

  public Integer getSubmissionsSaved()
  {
    return submissionsSaved;
  }

  public void setSubmissionsSaved(Integer psubmissionsSaved)
  {
    submissionsSaved = psubmissionsSaved;
  }

  public Integer getAssessmentFormat()
  {
    return assessmentFormat;
  }

  public void setAssessmentFormat(Integer assessmentFormat)
  {
    this.assessmentFormat = assessmentFormat;
  }

  public Integer getBookMarkingItem()
  {
    return bookMarkingItem;
  }

  public void setBookMarkingItem(Integer bookMarkingItem)
  {
    this.bookMarkingItem = bookMarkingItem;
  }

  public Integer getTimeLimit()
  {
    return timeLimit;
  }

  public void setTimeLimit(Integer timeLimit)
  {
    this.timeLimit = timeLimit;
  }

  public Integer getTimedAssessment()
  {
    return timedAssessment;
  }

  public void setTimedAssessment(Integer timedAssessment)
  {
    this.timedAssessment = timedAssessment;
  }

  public void setRetryAllowed(Integer retryAllowed)
  {
    this.retryAllowed = retryAllowed;
  }

  public Integer getRetryAllowed()
  {
    return retryAllowed;
  }

  public void setLateHandling(Integer lateHandling)
  {
    this.lateHandling = lateHandling;
  }

  public Integer getLateHandling()
  {
    return lateHandling;
  }

  public void setInstructorNotification(Integer instructorNotification)
  {
    this.instructorNotification = instructorNotification;
  }

  public Integer getInstructorNotification()
  {
    return instructorNotification;
  }

  public Date getStartDate() {
    return this.startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getDueDate() {
    return this.dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public Date getScoreDate() {
    return this.scoreDate;
  }

  public void setScoreDate(Date scoreDate) {
    this.scoreDate = scoreDate;
  }

  public Date getFeedbackDate() {
    return this.feedbackDate;
  }

  public void setFeedbackDate(Date feedbackDate) {
    this.feedbackDate = feedbackDate;
  }

  public Date getRetractDate() {
    return this.retractDate;
  }

  public void setRetractDate(Date retractDate) {
    this.retractDate = retractDate;
  }

  public void setAutoSubmit(Integer autoSubmit)
  {
    this.autoSubmit = autoSubmit;
  }

  public Integer getAutoSubmit()
  {
    return autoSubmit;
  }

  public void setItemNavigation(Integer itemNavigation)
  {
    this.itemNavigation = itemNavigation;
  }

  public Integer getItemNavigation()
  {
    return itemNavigation;
  }

  public void setItemNumbering(Integer itemNumbering)
  {
    this.itemNumbering = itemNumbering;
  }

  public Integer getItemNumbering()
  {
    return itemNumbering;
  }
  
  public void setDisplayScoreDuringAssessments(Integer displayScoreDuringAssessments)
  {
    this.displayScoreDuringAssessments = displayScoreDuringAssessments;
  }

  public Integer getDisplayScoreDuringAssessments()
  {
    return displayScoreDuringAssessments;
  }
  
  public void setSubmissionMessage(String submissionMessage)
  {
    this.submissionMessage = submissionMessage;
  }

  public String getSubmissionMessage()
  {
    return submissionMessage;
  }

  public void setFinalPageUrl(String finalPageUrl) {
    this.finalPageUrl = finalPageUrl;
  }

  public String getFinalPageUrl() {
    return finalPageUrl;
  }

  public String getReleaseTo() {
    return this.releaseTo;
  }

  public void setReleaseTo(String releaseTo) {
    this.releaseTo = releaseTo;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Boolean getUnlimitedSubmissions() {
    return this.unlimitedSubmissions;
  }

  public void setUnlimitedSubmissions(Boolean unlimitedSubmissions) {
    this.unlimitedSubmissions = unlimitedSubmissions;
  }
  

  public Integer getMarkForReview() {
	return this.markForReview;
  }

  public void setMarkForReview(Integer markForReview) {
	this.markForReview = markForReview;
  }

  @Override
  public Boolean getHonorPledge() { return this.honorPledge; }

  @Override
  public void setHonorPledge(Boolean honorPledge) { this.honorPledge = honorPledge; }

}
