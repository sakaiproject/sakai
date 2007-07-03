/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.data.ifc.assessment;

public interface AssessmentFeedbackIfc
    extends java.io.Serializable
{

  public static Integer IMMEDIATE_FEEDBACK = new Integer(1);
  public static Integer FEEDBACK_BY_DATE = new Integer(2);
  public static Integer NO_FEEDBACK = new Integer(3);
  public static Integer FEEDBACK_ON_SUBMISSION = new Integer(4);

  public static Integer QUESTIONLEVEL_FEEDBACK = new Integer(1);
  public static Integer SECTIONLEVEL_FEEDBACK = new Integer(2);
  public static Integer BOTH_FEEDBACK = new Integer(3);

  Long getId();

  void setId(Long id);

  void setAssessmentBase(AssessmentBaseIfc assessmentBase);

  AssessmentBaseIfc getAssessmentBase();

  Integer getFeedbackDelivery();

  void setFeedbackDelivery(Integer feedbackDelivery);

  Integer getFeedbackAuthoring();

  void setFeedbackAuthoring(Integer feedbackAuthoring);

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

  Boolean getShowStudentQuestionScore();

  void setShowStudentQuestionScore(Boolean showStudentQuestionScore);

  Boolean getShowQuestionLevelFeedback();

  void setShowQuestionLevelFeedback(Boolean showQuestionLevelFeedback);

  Boolean getShowSelectionLevelFeedback();

  void setShowSelectionLevelFeedback(Boolean showSelectionLevelFeedback);

  Boolean getShowGraderComments();

  void setShowGraderComments(Boolean showGraderComments);

  Boolean getShowStatistics();

  void setShowStatistics(Boolean showStatistics);
}
