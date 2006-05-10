/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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


/**
 * This keeps track of the submission scheme, and the number allowed.
 *
 * @author Rachel Gollub
 */
public interface EvaluationModelIfc
    extends java.io.Serializable
{

  public static Integer ANONYMOUS_GRADING = new Integer(1);
  public static Integer NON_ANONYMOUS_GRADING = new Integer(2);
  public static Integer GRADEBOOK_NOT_AVAILABLE = new Integer(0);
  public static Integer TO_DEFAULT_GRADEBOOK = new Integer(1);
  public static Integer TO_SELECTED_GRADEBOOK = new Integer(2);

  // scoring type 
  public static Integer HIGHEST_SCORE = new Integer(1);
  //public static Integer AVERAGE_SCORE = new Integer(2);
  public static Integer LAST_SCORE= new Integer(2);
  public static Integer ALL_SCORE= new Integer(3);


  Long getId();

  void setId(Long id);

  void setAssessmentBase(AssessmentBaseIfc assessmentBase);

  AssessmentBaseIfc getAssessmentBase();

  String getEvaluationComponents();

  void setEvaluationComponents(String evaluationComponents);

  Integer getScoringType();

  void setScoringType(Integer scoringType);

  String getNumericModelId();

  void setNumericModelId(String numericModelId);

  Integer getFixedTotalScore();

  void setFixedTotalScore(Integer fixedTotalScore);

  Integer getGradeAvailable();

  void setGradeAvailable(Integer gradeAvailable);

  Integer getIsStudentIdPublic();

  void setAnonymousGrading(Integer anonymousGrading);

  Integer getAnonymousGrading();

  void setAutoScoring(Integer autoScoring);

  Integer getAutoScoring();

  void setIsStudentIdPublic(Integer isStudentIdPublic);

  String getToGradeBook();

  void setToGradeBook(String toGradeBook);
}
