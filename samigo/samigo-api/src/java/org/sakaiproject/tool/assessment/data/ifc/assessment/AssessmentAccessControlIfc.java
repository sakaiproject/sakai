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



package org.sakaiproject.tool.assessment.data.ifc.assessment;

import java.util.Date;

/**
 * This keeps track of the submission scheme, and the number allowed.
 *
 * @author Rachel Gollub
 */
public interface AssessmentAccessControlIfc
    extends java.io.Serializable
{
  // flag it when no editing on the property is desire
  public static final Integer NO_EDIT = -1;
  // timedAssessment
  public static final Integer TIMED_ASSESSMENT = 1;
  public static final Integer DO_NOT_TIMED_ASSESSMENT = 0;
  // autoSubmit
  public static final Integer AUTO_SUBMIT = 1;
  public static final Integer DO_NOT_AUTO_SUBMIT = 0;
  // autoSave
  public static final Integer SAVE_ON_CLICK = 1;
  public static final Integer AUTO_SAVE = 2;
  // itemNavigation
  public static final Integer LINEAR_ACCESS = 1;
  public static final Integer RANDOM_ACCESS = 2;
  // assessmentFormat
  public static final Integer BY_QUESTION = 1;
  public static final Integer BY_PART = 2;
  public static final Integer BY_ASSESSMENT = 3;
  // itemNumbering
  public static final Integer CONTINUOUS_NUMBERING = 1;
  public static final Integer RESTART_NUMBERING_BY_PART = 2;
  // itemScoreDisplay
  public static final Integer DISPLAY_ITEM_SCORE_DURING_ASSESSMENT = 1;
  public static final Integer HIDE_ITEM_SCORE_DURING_ASSESSMENT = 2;
  //markForReview
  public static final Integer MARK_FOR_REVIEW = 1;
  public static final Integer NOT_MARK_FOR_REVIEW = 0;
  // submissionsAllowed
  public static final Integer UNLIMITED_SUBMISSIONS_ALLOWED = 9999;
  public static final Integer UNLIMITED_SUBMISSIONS = 1;
  public static final Integer LIMITED_SUBMISSIONS = 0;
  // lateHandling
  public static final Integer ACCEPT_LATE_SUBMISSION = 1;
  public static final Integer NOT_ACCEPT_LATE_SUBMISSION = 2;
  // group release
  public static final String RELEASE_TO_SELECTED_GROUPS = "Selected Groups";

  Long getId();

  void setId(Long id);

  void setAssessmentBase(AssessmentBaseIfc assessmentBase);

  AssessmentBaseIfc getAssessmentBase();

  Integer getSubmissionsAllowed();

  void setSubmissionsAllowed(Integer submissionsAllowed);

  Integer getSubmissionsSaved();

  void setSubmissionsSaved(Integer submissionsSaved);

  Integer getAssessmentFormat();

  void setAssessmentFormat(Integer assessmentFormat);

  Integer getBookMarkingItem();

  void setBookMarkingItem(Integer bookMarkingItem);

  Integer getTimeLimit();

  void setTimeLimit(Integer timeLimit);

  Integer getTimedAssessment();

  void setRetryAllowed(Integer retryAllowed);

  Integer getRetryAllowed();

  void setLateHandling(Integer lateHandling);

  Integer getLateHandling();

  void setInstructorNotification(Integer instructorNotification);

  Integer getInstructorNotification();

  void setTimedAssessment(Integer timedAssessment);

  Date getStartDate();

  void setStartDate(Date startDate);

  Date getDueDate();

  void setDueDate(Date dueDate);

  Date getScoreDate();

  void setScoreDate(Date scoreDate);

  Date getFeedbackDate();

  void setFeedbackDate(Date feedbackDate);

  Date getFeedbackEndDate();

  void setFeedbackEndDate(Date feedbackEndDate);

  Double getFeedbackScoreThreshold();

  void setFeedbackScoreThreshold(Double feedbackScoreThreshold);

  Date getRetractDate();

  void setRetractDate(Date retractDate);

  void setAutoSubmit(Integer autoSubmit);

  Integer getAutoSubmit();

  void setItemNavigation(Integer itemNavigation);

  Integer getItemNavigation();

  void setItemNumbering(Integer itemNumbering);

  Integer getItemNumbering();
  
  void setDisplayScoreDuringAssessments(Integer displayScore);
  
  Integer getDisplayScoreDuringAssessments();

  void setSubmissionMessage(String submissionMessage);

  String getSubmissionMessage();

  String getReleaseTo();

  void setReleaseTo(String releaseTo);

  String getPassword();

  void setPassword(String password);

  void setFinalPageUrl(String finalPageUrl);

  String getFinalPageUrl();

  Boolean getUnlimitedSubmissions();

  void setUnlimitedSubmissions(Boolean unlimitedSubmissions);

  Integer getMarkForReview();

  void setMarkForReview(Integer markForReview);

  Boolean getHonorPledge();

  void setHonorPledge(Boolean honorPledge);

}
