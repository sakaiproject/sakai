/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
*
**********************************************************************************/

package org.sakaiproject.tool.assessment.ui.bean.select;

import java.io.Serializable;

import java.util.ArrayList;

/**
 *
 * <p>Title: sakaiproject.org</p>
 * <p>Description: OKI based implementation</p>
* <p>Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*  </p>
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id$
 *
 * Used to be org.navigoproject.ui.web.asi.select.SelectAssessmentForm.java
 */
public class SelectAssessmentBean
  implements Serializable
{
  private ArrayList takeableAssessments;

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 7401578412639293693L;
  private ArrayList lateHandlingAssessments;
  private ArrayList reviewableAssessments;
  private ArrayList nonReviewableAssessments;
  private String reviewableSortOrder="title";
  private String takeableSortOrder ="title";
  private boolean takeableAscending = true;
  private boolean reviewableAscending = true;
  private org.sakaiproject.tool.assessment.ui.model.PagingModel reviewPager;
  private org.sakaiproject.tool.assessment.ui.model.PagingModel takePager;

  /**
   * ArrayLists should be lists of DeliveryBean objects
   *
   * @return ArrayLists of DeliveryBean objects
   */
  public ArrayList getTakeableAssessments()
  {
    return takeableAssessments;
  }

  /**
   * ArrayLists should be lists of DeliveryBean objects
   *
   * @param takeableAssessments ArrayLists of DeliveryBean objects
   */
  public void setTakeableAssessments(ArrayList takeableAssessments)
  {
    this.takeableAssessments = takeableAssessments;
  }

  /**
   * ArrayLists should be lists of DeliveryBean objects
   *
   * @return ArrayLists of DeliveryBean objects
   */
  public ArrayList getLateHandlingAssessments()
  {
    return lateHandlingAssessments;
  }

  /**
   * ArrayLists should be lists of DeliveryBean objects
   *
   * @param lateHandlingAssessments ArrayLists of DeliveryBean objects
   */
  public void setLateHandlingAssessments(ArrayList lateHandlingAssessments)
  {
    this.lateHandlingAssessments = lateHandlingAssessments;
  }

  /**
   * ArrayLists should be lists of DeliveryBean objects
   *
   * @return ArrayLists of DeliveryBean objects
   */
  public ArrayList getReviewableAssessments()
  {
    return reviewableAssessments;
  }

  /**
   * ArrayLists should be lists of DeliveryBean objects
   *
   * @param reviewAssessments ArrayLists should be lists of DeliveryBean objects
   */
  public void setReviewableAssessments(ArrayList reviewableAssessments)
  {
    this.reviewableAssessments = reviewableAssessments;
  }

  /**
   * ArrayLists should be lists of DeliveryBean objects
   *
   * @return ArrayLists of DeliveryBean objects
   */
  public ArrayList getNonReviewableAssessments()
  {
    return this.nonReviewableAssessments;
  }

  /**
   * ArrayLists should be lists of DeliveryBean objects
   *
   * @param nonReviewableAssessments ArrayLists should be lists of DeliveryBean objects
   */
  public void setNonReviewableAssessments(ArrayList nonReviewableAssessments)
  {
    this.nonReviewableAssessments = nonReviewableAssessments;
  }

  ////////////////////////////////////////////////////////////////
  // not used right now
  public org.sakaiproject.tool.assessment.ui.model.PagingModel getReviewPager() {
    return reviewPager;
  }
  public void setReviewPager(org.sakaiproject.tool.assessment.ui.model.PagingModel reviewPager) {
    this.reviewPager = reviewPager;
  }
  public org.sakaiproject.tool.assessment.ui.model.PagingModel getTakePager() {
    return takePager;
  }
  public void setTakePager(org.sakaiproject.tool.assessment.ui.model.PagingModel takePager) {
    this.takePager = takePager;
  }

  // sorting model
  /**
   * sort order for review table
   * @return
   */
  public String getReviewableSortOrder()
  {
    return reviewableSortOrder;
  }

  /**
   * ascending descending for review table
   * @return
   */
  public boolean isReviewableAscending()
  {
    return reviewableAscending;
  }


  /**
   * sort for take assessment table
   * @return
   */


  public String getTakeableSortOrder()
  {
    return takeableSortOrder;
  }

  /**
   * sort for take assessment table
   * @param sort
   */
  public void setTakeableSortOrder(String takeableSortOrder)
  {
    this.takeableSortOrder = takeableSortOrder;
  }

  /**
   * sort for review assessment table
   * @param sort
   */
  public void setReviewableSortOrder(String reviewableSortOrder)
  {
    this.reviewableSortOrder = reviewableSortOrder;
  }
  /**
   *
   * @param reviewableAscending
   */
  public void setReviewableAscending(boolean reviewableAscending)
  {
    this.reviewableAscending = reviewableAscending;
  }
  /**
   * is takable table sorted in ascending order
   * @return true if it is
   */
  public boolean isTakeableAscending()
  {
    return takeableAscending;
  }

  /**
   *
   * @param takeableAscending is takable table sorted in ascending order
   */
  public void setTakeableAscending(boolean takeableAscending)
  {
    this.takeableAscending = takeableAscending;
  }

}
