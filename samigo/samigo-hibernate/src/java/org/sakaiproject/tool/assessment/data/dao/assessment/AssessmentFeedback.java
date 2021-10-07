/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;

public class AssessmentFeedback extends AssessmentFeedbackIfc {

	private static final long serialVersionUID = 711760492880980393L;

  /**
   * Creates a new SubmissionModel object.
   */
  public AssessmentFeedback()
  {
    setShowCorrectResponse(Boolean.FALSE);
    setShowGraderComments(Boolean.FALSE);
    setShowQuestionLevelFeedback(Boolean.FALSE);
    setShowQuestionText(Boolean.TRUE);
    setShowSelectionLevelFeedback(Boolean.FALSE);
    setShowStatistics(Boolean.FALSE);
    setShowStudentScore(Boolean.FALSE);
    setShowStudentQuestionScore(Boolean.FALSE);
    setFeedbackDelivery(AssessmentFeedbackIfc.NO_FEEDBACK);
    setFeedbackComponentOption(AssessmentFeedbackIfc.SELECT_COMPONENTS);
    setFeedbackAuthoring(AssessmentFeedbackIfc.QUESTIONLEVEL_FEEDBACK);
  }

  public AssessmentFeedback(
      Integer feedbackDelivery, Integer feedbackComponentOption, Integer feedbackAuthoring, Integer editComponents,
      Boolean showQuestionText,
      Boolean showStudentResponse, Boolean showCorrectResponse,
      Boolean showStudentScore,   Boolean showStudentQuestionScore, 
      Boolean showQuestionLevelFeedback, Boolean showSelectionLevelFeedback,
      Boolean showGraderComments, Boolean showStatistics)
  {
    this.feedbackDelivery = feedbackDelivery;
    this.feedbackComponentOption = feedbackComponentOption;
    this.feedbackAuthoring = feedbackAuthoring;
    this.editComponents = editComponents;
    this.showQuestionText = showQuestionText;
    this.showStudentResponse = showStudentResponse;
    this.showCorrectResponse = showCorrectResponse;
    this.showStudentScore = showStudentScore;
    this.showStudentQuestionScore = showStudentQuestionScore;
    this.showQuestionLevelFeedback = showQuestionLevelFeedback;
    this.showSelectionLevelFeedback = showSelectionLevelFeedback; // must be MC
    this.showGraderComments = showGraderComments;
    this.showStatistics = showStatistics;
  }

  public Object clone() throws CloneNotSupportedException{
    Object cloned = new AssessmentFeedback(
        this.getFeedbackDelivery(),this.getFeedbackComponentOption(), this.getFeedbackAuthoring(), this.getEditComponents(),
        this.getShowQuestionText(),
        this.getShowStudentResponse(), this.getShowCorrectResponse(),
        this.getShowStudentScore(),  this.getShowStudentQuestionScore(),
        this.getShowQuestionLevelFeedback(),
        this.getShowSelectionLevelFeedback(), this.getShowGraderComments(),
        this.getShowStatistics());
    return cloned;
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

}
