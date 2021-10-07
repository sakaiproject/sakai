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

import lombok.Setter;
import lombok.Getter;

public abstract class AssessmentFeedbackIfc {

  public static final Integer IMMEDIATE_FEEDBACK = 1;
  public static final Integer FEEDBACK_BY_DATE = 2;
  public static final Integer NO_FEEDBACK = 3;
  public static final Integer FEEDBACK_ON_SUBMISSION = 4;

  public static final Integer QUESTIONLEVEL_FEEDBACK = 1;
  public static final Integer SECTIONLEVEL_FEEDBACK = 2;
  public static final Integer BOTH_FEEDBACK = 3;
  public static final Integer SELECT_COMPONENTS = 2;  // select feedback components
  public static final Integer SHOW_TOTALSCORE_ONLY = 1;  // select feedback components

  @Setter @Getter protected Long id;
  @Setter @Getter protected AssessmentBaseIfc assessmentBase;
  @Setter @Getter protected Integer feedbackDelivery; // immediate, on specific date , no feedback
  @Setter @Getter protected Integer feedbackComponentOption; // total scores only, or select components 
  @Setter @Getter protected Integer feedbackAuthoring; //questionlevel, sectionlevel, both, 
  @Setter @Getter protected Integer editComponents; // 0 = cannot
  @Setter @Getter protected Boolean showQuestionText;
  @Setter @Getter protected Boolean showStudentResponse;
  @Setter @Getter protected Boolean showCorrectResponse;
  @Setter @Getter protected Boolean showStudentScore;
  @Setter @Getter protected Boolean showStudentQuestionScore;
  @Setter @Getter protected Boolean showQuestionLevelFeedback;
  @Setter @Getter protected Boolean showSelectionLevelFeedback; // must be MC
  @Setter @Getter protected Boolean showGraderComments;
  @Setter @Getter protected Boolean showStatistics;

}
