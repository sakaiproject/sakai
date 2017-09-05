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


/**
 * This keeps track of the submission scheme, and the number allowed.
 *
 * @author Rachel Gollub
 */
public interface EvaluationModelIfc
    extends java.io.Serializable
{

  public static final Integer ANONYMOUS_GRADING = 1;
  public static final Integer NON_ANONYMOUS_GRADING = 2;
  public static final Integer GRADEBOOK_NOT_AVAILABLE = 0;
  public static final Integer TO_DEFAULT_GRADEBOOK = 1;
  //public static Integer TO_SELECTED_GRADEBOOK = new Integer(2);  // this is confusing, we are using 2 for 'None' but the name is confusing, 
  public static final Integer NOT_TO_GRADEBOOK = 2;		// so now we added this new constant, SAK-7162
  public static final Integer TO_SELECTED_GRADEBOOK = 3;  // not used, but leave it for now 

  // scoring type 
  public static final Integer HIGHEST_SCORE = 1;
  //public static Integer AVERAGE_SCORE = new Integer(2);
  public static final Integer LAST_SCORE= 2;
  public static final Integer ALL_SCORE= 3;
  public static final Integer AVERAGE_SCORE= 4;

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
