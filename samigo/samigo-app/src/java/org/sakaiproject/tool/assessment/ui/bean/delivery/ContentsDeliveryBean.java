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



package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.math3.util.Precision;

/**
 * <p> Table of Contents and Contents Data</p>
 * <p>This is a 'dual purpose' bean.  It can serve as a
 * representation of the entire (table of) contents for an
 * assessment, or the contents presented in a praticular page view.</p>
 */

public class ContentsDeliveryBean
  implements Serializable
{
	private static final long serialVersionUID = -4619361961662881387L;
  private List partsContents;
  private double currentScore;
  private double maxScore; // SectionContentsBean
  // for display/hide score
  private boolean showStudentScore;
  private String pointsDisplayString;

  /**
   * Current score for entire contents.
   * @return current score for entire contents
   */
  public double getCurrentScore()
  {
    return currentScore;
  }

  /**
   * Current score for entire contents
   * @param currentScore current score for entire contents
   */
  public void setCurrentScore(double currentScore)
  {
    this.currentScore = currentScore;
  }

  /**
   * Maximum score for entire contents.
   * @return maximum score for entire contents
   */
  public double getMaxScore()
  {
    return maxScore;
  }

  /**
   * Maximum score for entire contents.
   * @param maxScore maximum score for entire contents
   */
  public void setMaxScore(double maxScore)
  {
    this.maxScore = maxScore;
  }

  /**
   * List of parts (SectionContentsBean) for entire contents.
   * @return parts for entire contents
   */
  public List getPartsContents()
  {
    return partsContents;
  }

  /**
   * Set parts (SectionContentsBean) for entire contents
   * @param partsContents parts (SectionContentsBean) for entire contents
   */
  public void setPartsContents(List partsContents)
  {
    this.partsContents = partsContents;
  }

  /**
   * Show the student score currently earned?
   * @return the score
   */
  public boolean isShowStudentScore()
  {
    return showStudentScore;
  }

  /**
   * Set the student score currently earned.
   * @param showStudentScore true/false Show the student score currently earned?
   */
  public void setShowStudentScore(boolean showStudentScore)
  {
	  this.showStudentScore = showStudentScore;
  }

  /**
   * If we display the current score, return it, otherwise an empty string.
   * Not currently used, provided if we need it later.
   * @return either, a) the current score, otherwise, b) "" (empty string)
   */
  public String getPointsDisplayString()
  {
	  String pointsDisplayString = "";
	  if (showStudentScore)
	  {
		  pointsDisplayString = "" + Precision.round(currentScore, 2);
	  }
	  return pointsDisplayString;
  }

  public boolean getIsNoParts()
  {
	  if (partsContents == null || partsContents.size() == 0) {
		  return true;
	  }
	  return false;
  }

}
