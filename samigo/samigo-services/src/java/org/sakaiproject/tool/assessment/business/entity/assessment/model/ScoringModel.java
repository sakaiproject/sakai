/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/business/entity/assessment/model/ScoringModel.java $
 * $Id: ScoringModel.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
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


package org.sakaiproject.tool.assessment.business.entity.assessment.model;


/**
 * This holds scoring model information.  It can be numerical, +/check/-,
 * letter grade, or P/F.  Numeric grading has more detailed information on
 * weighting and total.
 *
 * @author Rachel Gollub
 * @author Ed Smiley
 */
public class ScoringModel
{
  private int scoringModelId;
  private String scoringType;
  private String numericModel;
  private int defaultQuestionValue;
  private int fixedTotalScore;

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getScoringType()
  {
    return scoringType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pscoringType DOCUMENTATION PENDING
   */
  public void setScoringType(String pscoringType)
  {
    scoringType = pscoringType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getNumericModel()
  {
    return numericModel;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pnumericModel DOCUMENTATION PENDING
   */
  public void setNumericModel(String pnumericModel)
  {
    numericModel = pnumericModel;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public int getDefaultQuestionValue()
  {
    return defaultQuestionValue;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pQuestionValue DOCUMENTATION PENDING
   */
  public void setDefaultQuestionValue(int pQuestionValue)
  {
    defaultQuestionValue = pQuestionValue;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public int getFixedTotalScore()
  {
    return fixedTotalScore;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pTotalScore DOCUMENTATION PENDING
   */
  public void setFixedTotalScore(int pTotalScore)
  {
    fixedTotalScore = pTotalScore;
  }
}
