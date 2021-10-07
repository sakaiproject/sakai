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

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;

import java.util.Date;

/**
 * This keeps track of the submission scheme, and the number allowed.
 *
 * @author Rachel Gollub
 */
public class AssessmentAccessControl extends AssessmentAccessControlIfc
{

  private static final long serialVersionUID = 8330416434678491916L;

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
    ((AssessmentAccessControl)cloned).setFeedbackEndDate(this.feedbackEndDate);
    ((AssessmentAccessControl)cloned).setFeedbackScoreThreshold(this.feedbackScoreThreshold);
    return cloned;
  }

  public AssessmentBaseIfc getAssessmentBase()
  {
    if (assessmentBase.getIsTemplate().equals(Boolean.TRUE))
      return (AssessmentTemplateIfc)assessmentBase;
    else
      return (AssessmentIfc)assessmentBase;
  }

}
