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

package org.sakaiproject.tool.assessment.business.entity.assessment.model;

import java.util.Date;

/**
 * This keeps track of feedback information for a given access group.
 *
 * @author Rachel Gollub
 * @author Ed Smiley
 *
 * @see AccessGroup
 */
public class FeedbackModel
{
  private String feedbackType;
  private String immediateFeedbackType;
  private String datedFeedbackType;
  private String perQuestionFeedbackType;
  private Date feedbackDate;
  private Date scoreDate;

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getFeedbackType()
  {
    return feedbackType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pfeedbackType DOCUMENTATION PENDING
   */
  public void setFeedbackType(String pfeedbackType)
  {
    feedbackType = pfeedbackType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getImmediateFeedbackType()
  {
    return immediateFeedbackType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pimmediateFeedbackType DOCUMENTATION PENDING
   */
  public void setImmediateFeedbackType(String pimmediateFeedbackType)
  {
    immediateFeedbackType = pimmediateFeedbackType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getDatedFeedbackType()
  {
    return datedFeedbackType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pdatedFeedbackType DOCUMENTATION PENDING
   */
  public void setDatedFeedbackType(String pdatedFeedbackType)
  {
    datedFeedbackType = pdatedFeedbackType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getPerQuestionFeedbackType()
  {
    return perQuestionFeedbackType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pfeedbackType DOCUMENTATION PENDING
   */
  public void setPerQuestionFeedbackType(String pfeedbackType)
  {
    perQuestionFeedbackType = pfeedbackType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Date getFeedbackDate()
  {
    return feedbackDate;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pfeedbackDate DOCUMENTATION PENDING
   */
  public void setFeedbackDate(Date pfeedbackDate)
  {
    feedbackDate = pfeedbackDate;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Date getScoreDate()
  {
    return scoreDate;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pscoreDate DOCUMENTATION PENDING
   */
  public void setScoreDate(Date pscoreDate)
  {
    scoreDate = pscoreDate;
  }
}
