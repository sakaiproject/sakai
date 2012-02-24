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
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;

/**
 * This keeps track of the submission scheme, and the number allowed.
 *
 * @author Rachel Gollub
 */
public class PublishedEvaluationModel
    implements java.io.Serializable, EvaluationModelIfc
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 6919530379488261271L;
private Long id;
  private AssessmentIfc assessment;
  private String evaluationComponents;
  private Integer scoringType;
  private String numericModelId;
  private Integer fixedTotalScore;
  private Integer gradeAvailable;
  private Integer isStudentIdPublic;
  private Integer anonymousGrading;
  private Integer autoScoring;
  private String toGradeBook;

  /**
   * Creates a new SubmissionModel object.
   */
  public PublishedEvaluationModel()
  {
  }

  public PublishedEvaluationModel(String evaluationComponents, Integer scoringType,
                                 String numericModelId, Integer fixedTotalScore,
                                 Integer gradeAvailable, Integer isStudentIdPublic,
                                 Integer anonymousGrading, Integer autoScoring,
                                 String toGradeBook)
  {
    this.evaluationComponents = evaluationComponents; // =  no limit
    this.scoringType = scoringType; // no. of copy
    this.numericModelId = numericModelId;
    this.fixedTotalScore =  fixedTotalScore;
    this.gradeAvailable = gradeAvailable;
    this.isStudentIdPublic = isStudentIdPublic;
    this.anonymousGrading = anonymousGrading;
    this.autoScoring = autoScoring;
    this.toGradeBook = toGradeBook;
  }

  public Long getId()
  {
    return id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  public void setAssessment(AssessmentIfc assessment)
  {
    this.assessment = assessment;
  }

  public AssessmentIfc getAssessment()
  {
    return assessment;
  }

  public void setAssessmentBase(AssessmentBaseIfc assessmentBase)
  {
    setAssessment((AssessmentIfc)assessmentBase);
  }

  public AssessmentBaseIfc getAssessmentBase()
  {
    return getAssessment();
  }

  public String getEvaluationComponents()
  {
    return evaluationComponents;
  }

  public void setEvaluationComponents(String evaluationComponents)
  {
    this.evaluationComponents = evaluationComponents;
  }

  public Integer getScoringType()
  {
    return scoringType;
  }

  public void setScoringType(Integer scoringType)
  {
    this.scoringType = scoringType;
  }

  public String getNumericModelId()
  {
    return numericModelId;
  }

  public void setNumericModelId(String numericModelId)
  {
    this.numericModelId = numericModelId;
  }

  public Integer getFixedTotalScore()
  {
    return fixedTotalScore;
  }

  public void setFixedTotalScore(Integer fixedTotalScore)
  {
    this.fixedTotalScore = fixedTotalScore;
  }

  public Integer getGradeAvailable()
  {
    return gradeAvailable;
  }

  public void setGradeAvailable(Integer gradeAvailable)
  {
    this.gradeAvailable = gradeAvailable;
  }

  public Integer getIsStudentIdPublic()
  {
    return isStudentIdPublic;
  }

  public void setAnonymousGrading(Integer anonymousGrading)
  {
    this.anonymousGrading = anonymousGrading;
  }

  public Integer getAnonymousGrading()
  {
    return anonymousGrading;
  }

  public void setAutoScoring(Integer autoScoring)
  {
    this.autoScoring = autoScoring;
  }

  public Integer getAutoScoring()
  {
    return autoScoring;
  }

  public void setIsStudentIdPublic(Integer isStudentIdPublic)
  {
    this.isStudentIdPublic = isStudentIdPublic;
  }

  public String getToGradeBook() {
    return this.toGradeBook;
  }

  public void setToGradeBook(String toGradeBook) {
    this.toGradeBook = toGradeBook;
  }
}
