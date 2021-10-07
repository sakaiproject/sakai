/**
 * Copyright (c) 2004-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.assessment.data.dao.assessment;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;

/**
 * This keeps track of the submission scheme, and the number allowed.
 *
 * @author Rachel Gollub
 */
public class PublishedEvaluationModel extends EvaluationModelIfc {

  private static final long serialVersionUID = 6919530379488261271L;

  private AssessmentIfc assessment;

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

}
