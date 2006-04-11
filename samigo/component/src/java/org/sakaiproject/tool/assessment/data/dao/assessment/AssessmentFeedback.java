/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.data.dao.assessment;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;

public class AssessmentFeedback
    implements java.io.Serializable, AssessmentFeedbackIfc
{

  private Long id;
  private AssessmentBaseIfc assessmentBase;
  private Integer feedbackDelivery; // immediate, on specific date , no feedback
   private Integer feedbackAuthoring; //questionlevel, sectionlevel, both, 
  private Integer editComponents; // 0 = cannot
  private Boolean showQuestionText;
  private Boolean showStudentResponse;
  private Boolean showCorrectResponse;
  private Boolean showStudentScore;
  private Boolean showStudentQuestionScore;
  private Boolean showQuestionLevelFeedback;
  private Boolean showSelectionLevelFeedback; // must be MC
  private Boolean showGraderComments;
  private Boolean showStatistics;

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
    setFeedbackAuthoring(AssessmentFeedbackIfc.QUESTIONLEVEL_FEEDBACK);
  }

  public AssessmentFeedback(
      Integer feedbackDelivery,Integer feedbackAuthoring, Integer editComponents,
      Boolean showQuestionText,
      Boolean showStudentResponse, Boolean showCorrectResponse,
      Boolean showStudentScore,   Boolean showStudentQuestionScore, 
      Boolean showQuestionLevelFeedback, Boolean showSelectionLevelFeedback,
      Boolean showGraderComments, Boolean showStatistics)
  {
    this.feedbackDelivery = feedbackDelivery;
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
        this.getFeedbackDelivery(),this.getFeedbackAuthoring(), this.getEditComponents(),
        this.getShowQuestionText(),
        this.getShowStudentResponse(), this.getShowCorrectResponse(),
        this.getShowStudentScore(),  this.getShowStudentQuestionScore(),
        this.getShowQuestionLevelFeedback(),
        this.getShowSelectionLevelFeedback(), this.getShowGraderComments(),
        this.getShowStatistics());
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

  public Integer getFeedbackDelivery()
  {
    return feedbackDelivery;
  }

  public void setFeedbackDelivery(Integer feedbackDelivery)
  {
    this.feedbackDelivery = feedbackDelivery;
  }

  public Integer getFeedbackAuthoring()
  {
    return feedbackAuthoring;
  }

  public void setFeedbackAuthoring(Integer feedbackAuthoring)
  {
    this.feedbackAuthoring = feedbackAuthoring;
  }

  public Integer getEditComponents() {
    return editComponents;
  }

  public void setEditComponents(Integer editComponents) {
    this.editComponents = editComponents;
  }

  public Boolean getShowQuestionText()
  {
    return showQuestionText;
  }

  public void setShowQuestionText(Boolean showQuestionText)
  {
    this.showQuestionText = showQuestionText;
  }

  public Boolean getShowStudentResponse()
  {
    return showStudentResponse;
  }

  public void setShowStudentResponse(Boolean showStudentResponse)
  {
    this.showStudentResponse = showStudentResponse;
  }

  public Boolean getShowCorrectResponse()
  {
    return showCorrectResponse;
  }

  public void setShowCorrectResponse(Boolean showCorrectResponse)
  {
    this.showCorrectResponse = showCorrectResponse;
  }

  public Boolean getShowStudentScore()
  {
    return showStudentScore;
  }

  public void setShowStudentScore(Boolean showStudentScore)
  {
    this.showStudentScore = showStudentScore;
  }

  public Boolean getShowStudentQuestionScore()
  {
    return showStudentQuestionScore;
  }

  public void setShowStudentQuestionScore(Boolean showStudentQuestionScore)
  {
    this.showStudentQuestionScore = showStudentQuestionScore;
  }

  public Boolean getShowQuestionLevelFeedback()
  {
    return showQuestionLevelFeedback;
  }

  public void setShowQuestionLevelFeedback(Boolean showQuestionLevelFeedback)
  {
    this.showQuestionLevelFeedback = showQuestionLevelFeedback;
  }

  public Boolean getShowSelectionLevelFeedback()
  {
    return showSelectionLevelFeedback;
  }

  public void setShowSelectionLevelFeedback(Boolean showSelectionLevelFeedback)
  {
    this.showSelectionLevelFeedback = showSelectionLevelFeedback;
  }

  public Boolean getShowGraderComments()
  {
    return showGraderComments;
  }

  public void setShowGraderComments(Boolean showGraderComments)
  {
    this.showGraderComments = showGraderComments;
  }

  public Boolean getShowStatistics()
  {
    return showStatistics;
  }

  public void setShowStatistics(Boolean showStatistics)
  {
    this.showStatistics = showStatistics;
  }

}
