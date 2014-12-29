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



package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;

/**
 * @author palcasi
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 *
 * Used to be org.navigoproject.ui.web.asi.result.GradeResultsForm.java
 */
public class EvaluationResultBean
  implements Serializable
{
  private String assessmentId;

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 2987887369259283101L;

  /**
   * Creates a new EvaluationResultBean object.
   */
  public EvaluationResultBean()
  {
    this.assessmentId = null;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getAssessmentId()
  {
    return this.assessmentId;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param assessmentId DOCUMENTATION PENDING
   */
  public void setAssessmentId(String assessmentId)
  {
    this.assessmentId = assessmentId;
  }
}
