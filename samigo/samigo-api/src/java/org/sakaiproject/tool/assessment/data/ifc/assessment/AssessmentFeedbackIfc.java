/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

public interface AssessmentFeedbackIfc
    extends java.io.Serializable
{

  public static final Integer IMMEDIATE_FEEDBACK = 1;
  public static final Integer FEEDBACK_BY_DATE = 2;
  public static final Integer NO_FEEDBACK = 3;
  public static final Integer FEEDBACK_ON_SUBMISSION = 4;

  public static final Integer QUESTIONLEVEL_FEEDBACK = 1;
  public static final Integer SECTIONLEVEL_FEEDBACK = 2;
  public static final Integer BOTH_FEEDBACK = 3;
  public static final Integer SELECT_COMPONENTS = 2;  // select feedback components
  public static final Integer SHOW_TOTALSCORE_ONLY = 1;  // select feedback components

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

  Integer getFeedbackComponentOption();

  void setFeedbackComponentOption(Integer feedbackComponentOption);

}
