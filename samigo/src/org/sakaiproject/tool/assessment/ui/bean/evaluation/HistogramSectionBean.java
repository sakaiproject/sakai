/*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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
*/

package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * <p>Description: Helper bean for Histograms.
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p> </p>
 * @author Rachel Gollub <rgollub@stanford.edu>
 * @version $Id: HistogramSectionBean.java,v 1.1 2004/11/20 04:17:03 rgollub.stanford.edu Exp $
 */
public class HistogramSectionBean
  implements Serializable
{
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
