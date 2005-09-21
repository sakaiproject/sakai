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
package org.sakaiproject.tool.assessment.data.ifc.assessment;

public interface AssessmentFeedbackIfc
    extends java.io.Serializable
{

  public static Integer IMMEDIATE_FEEDBACK = new Integer(1);
  public static Integer FEEDBACK_BY_DATE = new Integer(2);
  public static Integer NO_FEEDBACK = new Integer(3);

  Long getId();

  void setId(Long id);

  void setAssessmentBase(AssessmentBaseIfc assessmentBase);

  AssessmentBaseIfc getAssessmentBase();

  Integer getFeedbackDelivery();

  void setFeedbackDelivery(Integer feedbackDelivery);

  Integer getEditComponents();

  void setEditComponents(Integer editComponents);

  Boolean getShowQuestionText();

  void setShowQuestionText(Boolean showQuestionText);

  Boolean getShowStudentResponse();

  void setShowStudentResponse(Boolean showStudentResponse);

  Boolean getShowCorrectResponse();

  void setShowCorrectResponse(Boolean showCorrectResponse);

  Boolean getShowStudentScore();

  void setShowStudentScore(Boolean showStudentScore);

  Boolean getShowQuestionLevelFeedback();

  void setShowQuestionLevelFeedback(Boolean showQuestionLevelFeedback);

  Boolean getShowSelectionLevelFeedback();

  void setShowSelectionLevelFeedback(Boolean showSelectionLevelFeedback);

  Boolean getShowGraderComments();

  void setShowGraderComments(Boolean showGraderComments);

  Boolean getShowStatistics();

  void setShowStatistics(Boolean showStatistics);
}
