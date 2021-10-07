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

import java.util.Date;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;

import lombok.Setter;
import lombok.Getter;

/**
 * This keeps track of the submission scheme, and the number allowed.
 *
 * @author Rachel Gollub
 */
public class PublishedAccessControl extends AssessmentAccessControlIfc {

  private static final long serialVersionUID = -221497966468066618L;

  @Setter @Getter private AssessmentIfc assessment;

  /**
   * Creates a new SubmissionModel object.
   */
  public PublishedAccessControl()
  {
	super();
  }

  public PublishedAccessControl(Integer submissionsAllowed, Integer submissionsSaved,
                                 Integer assessmentFormat, Integer bookMarkingItem,
                                 Integer timeLimit, Integer timedAssessment,
                                 Integer retryAllowed, Integer lateHandling, Integer instructorNotification,
                                 Date startDate, Date dueDate,
                                 Date scoreDate, Date feedbackDate, 
                                 String releaseTo)
  {
    this.submissionsAllowed = submissionsAllowed; // =  no limit
    this.submissionsSaved = submissionsSaved;
    this.assessmentFormat = assessmentFormat;
    this.bookMarkingItem =  bookMarkingItem;
    this.timeLimit = timeLimit;
    this.timedAssessment = timedAssessment;
    this.retryAllowed = retryAllowed;
    this.lateHandling = lateHandling;
    this.instructorNotification = instructorNotification;
    this.startDate = startDate;
    this.dueDate = dueDate;
    this.scoreDate = scoreDate;
    this.feedbackDate = feedbackDate;
    this.releaseTo = releaseTo;
  }

  public PublishedAccessControl(Integer submissionsAllowed, Integer submissionsSaved,
                                 Integer assessmentFormat, Integer bookMarkingItem,
                                 Integer timeLimit, Integer timedAssessment,
                                 Integer retryAllowed, Integer lateHandling, Integer instructorNotification,
                                 Date startDate, Date dueDate,
                                 Date scoreDate, Date feedbackDate)
  {
    this.submissionsAllowed = submissionsAllowed; // =  no limit
    this.submissionsSaved = submissionsSaved;
    this.assessmentFormat = assessmentFormat;
    this.bookMarkingItem =  bookMarkingItem;
    this.timeLimit = timeLimit;
    this.timedAssessment = timedAssessment;
    this.retryAllowed = retryAllowed;
    this.lateHandling = lateHandling;
    this.instructorNotification = instructorNotification;
    this.startDate = startDate;
    this.dueDate = dueDate;
    this.scoreDate = scoreDate;
    this.feedbackDate = feedbackDate;
  }

  public Object clone() throws CloneNotSupportedException{
    Object cloned = new PublishedAccessControl(
        this.getSubmissionsAllowed(), this.getSubmissionsSaved(),
        this.getAssessmentFormat(), this.getBookMarkingItem(),
        this.getTimeLimit(), this.getTimedAssessment(),
        this.getRetryAllowed(), this.getLateHandling(), this.getInstructorNotification(),
        this.getStartDate(), this.getDueDate(),
        this.getScoreDate(), this.getFeedbackDate(),
        this.getReleaseTo());
    ((PublishedAccessControl)cloned).setRetractDate(this.retractDate);
    ((PublishedAccessControl)cloned).setAutoSubmit(this.autoSubmit);
    ((PublishedAccessControl)cloned).setItemNavigation(this.itemNavigation);
    ((PublishedAccessControl)cloned).setItemNumbering(this.itemNumbering);
    ((PublishedAccessControl)cloned).setDisplayScoreDuringAssessments(this.displayScoreDuringAssessments);
    ((PublishedAccessControl)cloned).setSubmissionMessage(this.submissionMessage);
    ((PublishedAccessControl)cloned).setPassword(this.password);
    ((PublishedAccessControl)cloned).setFinalPageUrl(this.finalPageUrl);
    ((PublishedAccessControl)cloned).setUnlimitedSubmissions(this.unlimitedSubmissions);
    ((PublishedAccessControl)cloned).setMarkForReview(this.markForReview);
    ((PublishedAccessControl)cloned).setHonorPledge(this.honorPledge);
    ((PublishedAccessControl)cloned).setFeedbackEndDate(this.feedbackEndDate);
    ((PublishedAccessControl)cloned).setFeedbackScoreThreshold(this.feedbackScoreThreshold);
    return cloned;
  }

  public void setAssessment(AssessmentIfc assessment)
  {
    this.assessment = assessment;
  }

  public AssessmentIfc getAssessment()
  {
     return (AssessmentIfc)assessment;
  }

  public void setAssessmentBase(AssessmentBaseIfc assessment)
  {
    setAssessment((AssessmentIfc)assessment);
  }

  public AssessmentBaseIfc getAssessmentBase()
  {
    return getAssessment();
  }

}
