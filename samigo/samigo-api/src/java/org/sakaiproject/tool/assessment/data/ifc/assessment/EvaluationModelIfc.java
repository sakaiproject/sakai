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

package org.sakaiproject.tool.assessment.data.ifc.assessment;

import lombok.Setter;
import lombok.Getter;

/**
 * This keeps track of the submission scheme, and the number allowed.
 *
 * @author Rachel Gollub
 */
public abstract class EvaluationModelIfc {

  public static final Integer ANONYMOUS_GRADING = 1;
  public static final Integer NON_ANONYMOUS_GRADING = 2;
  public static final Integer GRADEBOOK_NOT_AVAILABLE = 0;
  public static final Integer TO_DEFAULT_GRADEBOOK = 1;
  public static final Integer NOT_TO_GRADEBOOK = 2;		// so now we added this new constant, SAK-7162
  public static final Integer TO_SELECTED_GRADEBOOK = 3;  // not used, but leave it for now 

  // scoring type 
  public static final Integer HIGHEST_SCORE = 1;
  public static final Integer LAST_SCORE= 2;
  public static final Integer ALL_SCORE= 3;
  public static final Integer AVERAGE_SCORE= 4;
  
  @Setter @Getter protected Long id;

  @Setter @Getter protected AssessmentIfc assessment;
  @Setter @Getter protected AssessmentBaseIfc assessmentBase;

  @Setter @Getter protected String evaluationComponents;
  @Setter @Getter protected Integer scoringType;
  @Setter @Getter protected String numericModelId;
  @Setter @Getter protected Integer fixedTotalScore;
  @Setter @Getter protected Integer gradeAvailable;
  @Setter @Getter protected Integer isStudentIdPublic;
  @Setter @Getter protected Integer anonymousGrading;
  @Setter @Getter protected Integer autoScoring;
  @Setter @Getter protected String toGradeBook;

}
