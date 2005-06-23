/**********************************************************************************
* $HeadURL$
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
  public static Integer HIGHEST_SCORE = new Integer(1);
  public static Integer AVERAGE_SCORE = new Integer(2);

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
