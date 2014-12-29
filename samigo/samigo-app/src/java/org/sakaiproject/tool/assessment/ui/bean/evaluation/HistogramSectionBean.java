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
import java.util.ArrayList;

/**
 *
 * <p>Description: Helper bean for Histograms.
 */

public class HistogramSectionBean
  implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -7669608870369454294L;
private ArrayList itemBeans; // The items for this section
  private String partName; // Part name
  private String sequence; // The number indicating order (1, 2, 3...)

  /**
   * Returns a list of HistogramQuestionScoresBeans
   * @return ArrayList
   */
  public ArrayList getItemBeans()
  {
    return itemBeans;
  }

  /**
   * Sets a list of HistogramQuestionScoresBeans
   * @param pquestionNumberList ArrayList
   */
  public void setItemBeans(ArrayList pItemBeans)
  {
    itemBeans = pItemBeans;
  }

  /**
   * Adds an itembean.
   */
  public void addItemBean(HistogramQuestionScoresBean bean)
  {
    if (itemBeans == null)
      itemBeans = new ArrayList();
    itemBeans.add(bean);
  }

  /**
   * Set the part name.
   * @param ppartName String
   */
  public void setPartName(String ppartName)
  {
    partName = ppartName;
  }

  /**
   * Get the part name.
   */
  public String getPartName()
  {
    return partName;
  }

  /**
   * Set the sequence value.
   */
  public void setSequence(String newSeq)
  {
    sequence = newSeq;
  }

  /**
   * Get the sequence value.
   */
  public String getSequence()
  {
    return sequence;
  }
}
