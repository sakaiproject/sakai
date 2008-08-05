/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/business/entity/assessment/model/FeedbackModel.java $
 * $Id: FeedbackModel.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright 2004, 2005, 2006 Sakai Foundation
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

import java.io.Serializable;
import java.util.Date;

/**
 * This keeps track of feedback information for a given access group.
 *
 * @author Rachel Gollub
 * @author Ed Smiley
 *
 * @see AccessGroup
 */
public class FeedbackModel implements Serializable
{
	/** Use serialVersionUID for interoperability. */
	private final static long serialVersionUID = -1090852048737428722L;private String feedbackType;
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
