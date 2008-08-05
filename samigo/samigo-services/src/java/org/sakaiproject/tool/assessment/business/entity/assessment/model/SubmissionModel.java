/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/business/entity/assessment/model/SubmissionModel.java $
 * $Id: SubmissionModel.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.business.entity.assessment.model;


/**
 * This keeps track of the submission scheme, and the number allowed.
 *
 * @author Rachel Gollub
 * @author Ed Smiley
 */
public class SubmissionModel
{
  private String numberSubmissions;
  private int submissionsAllowed;
  private int submissionModelId;

  /**
   * Creates a new SubmissionModel object.
   */
  public SubmissionModel()
  {
    //    numberSubmissions = AllChoices.numberSubmissionsShort[0];
    numberSubmissions = AllChoices.numberSubmissionsLong[0];
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public int getSubmissionModelId()
  {
    return submissionModelId;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newId DOCUMENTATION PENDING
   */
  public void setSubmissionModelId(int newId)
  {
    submissionModelId = newId;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getNumberSubmissions()
  {
    return numberSubmissions;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pnumberSubmissions DOCUMENTATION PENDING
   */
  public void setNumberSubmissions(String pnumberSubmissions)
  {
    numberSubmissions = pnumberSubmissions;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public int getSubmissionsAllowed()
  {
    return submissionsAllowed;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param psubmissionsAllowed DOCUMENTATION PENDING
   */
  public void setSubmissionsAllowed(int psubmissionsAllowed)
  {
    submissionsAllowed = psubmissionsAllowed;
  }
}
