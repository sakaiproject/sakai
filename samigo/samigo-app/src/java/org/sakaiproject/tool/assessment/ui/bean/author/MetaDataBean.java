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



package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class MetaDataBean implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1329360369551821799L;
private boolean considerUserId;
  private boolean collectItemMetadata;
  private boolean considerAllowIP;
  private boolean autoSubmit;
  private boolean feedbackShowCorrectResponse;
  private boolean feedbackShowStudentScore;
  private boolean feedbackShowStudentQuestionScore;
  private boolean feedbackShowItemLevel;
  private boolean feedbackShowSelectionLevel;
  private boolean feedbackShowGraderComment;
  private boolean feedbackShowStats;
  private boolean feedbackShowQuestion;
  private boolean feedbackShowResponse;
  private boolean anonymousGrading;
  private boolean collectSectionMetadata;
  private String allowedIP;
  private String password;
  private String enableDisableAssessmentOrganization;
  private boolean displayNumberingContinuous;
  private boolean navigationRandom;
  private boolean unlimitedAttempts;
  private int maxAttempts;
  private boolean feedbackImmediate;
  private boolean recordHighestScore;
  private String keywords;
  private String objectives;
  private String rubrics;

  /**
   * consider user id
   * @return true or false
   */
  public boolean isConsiderUserId()
  {
    return considerUserId;
  }

  /**
   * consider user id
   * @param considerUserId true or false
   */
  public void setConsiderUserId(boolean considerUserId)
  {
    this.considerUserId = considerUserId;
  }

  /**
   * consider ip address
   * @return true or false
   */
  public boolean isConsiderAllowIP()
  {
    return considerAllowIP;
  }

  /**
   * consider ip address
   * @param considerAllowIP consider ip address
   */
  public void setConsiderAllowIP(boolean considerAllowIP)
  {
    this.considerAllowIP = considerAllowIP;
  }

  /**
   * autosubmit?
   * @return true or false
   */
  public boolean isAutoSubmit()
  {
    return autoSubmit;
  }

  /**
   * autosubmit?
   * @param autoSubmit
   */
  public void setAutoSubmit(boolean autoSubmit)
  {
    this.autoSubmit = autoSubmit;
  }

  /**
   * show correct response?
   * @return true or false
   */
  public boolean isFeedbackShowCorrectResponse()
  {
    return feedbackShowCorrectResponse;
  }

  /**
   * show correct response?
   * @param feedbackShowCorrectResponse show correct response
   */
  public void setFeedbackShowCorrectResponse(boolean
    feedbackShowCorrectResponse)
  {
    this.feedbackShowCorrectResponse = feedbackShowCorrectResponse;
  }

  /**
   * show student score?
   * @return true or false
   */
  public boolean isFeedbackShowStudentScore()
  {
    return feedbackShowStudentScore;
  }

  /**
   * show student score?
   * @param feedbackShowStudentScore
   */
  public void setFeedbackShowStudentScore(boolean feedbackShowStudentScore)
  {
    this.feedbackShowStudentScore = feedbackShowStudentScore;
  }

  /**
   * show student Question score?
   * @return true or false
   */
  public boolean isFeedbackShowStudentQuestionScore()
  {
    return feedbackShowStudentQuestionScore;
  }

  /**
   * show student Question score?
   * @param feedbackShowStudentQuestionScore
   */
  public void setFeedbackShowStudentQuestionScore(boolean feedbackShowStudentQuestionScore)
  {
    this.feedbackShowStudentQuestionScore = feedbackShowStudentQuestionScore;
  }


  /**
   * item level feedback?
   * @return true or false
   */
  public boolean isFeedbackShowItemLevel()
  {
    return feedbackShowItemLevel;
  }

  /**
   * item level feedback?
   * @param feedbackShowItemLevel
   */
  public void setFeedbackShowItemLevel(boolean feedbackShowItemLevel)
  {
    this.feedbackShowItemLevel = feedbackShowItemLevel;
  }

  /**
   * selection level feedback?
   * @return true or false
   */
  public boolean isFeedbackShowSelectionLevel()
  {
    return feedbackShowSelectionLevel;
  }

  /**
   * selection level feedback?
   * @param feedbackShowSelectionLevel
   */
  public void setFeedbackShowSelectionLevel(boolean
    feedbackShowSelectionLevel)
  {
    this.feedbackShowSelectionLevel = feedbackShowSelectionLevel;
  }

  /**
   * grader comments?
   * @return true or false
   */
  public boolean isFeedbackShowGraderComment()
  {
    return feedbackShowGraderComment;
  }

  /**
   * grader comments?
   * @param feedbackShowGraderComment
   */
  public void setFeedbackShowGraderComment(boolean
    feedbackShowGraderComment)
  {
    this.feedbackShowGraderComment = feedbackShowGraderComment;
  }

  /**
   * show statistics and histograms?
   * @return true or false
   */
  public boolean isFeedbackShowStats()
  {
    return feedbackShowStats;
  }

  /**
   * show statistics and histograms?
   * @param feedbackShowStats
   */
  public void setFeedbackShowStats(boolean feedbackShowStats)
  {
    this.feedbackShowStats = feedbackShowStats;
  }

  /**
   * show question feedback?
   * @return true or false
   */
  public boolean isFeedbackShowQuestion()
  {
    return feedbackShowQuestion;
  }

  /**
   * show question feedback?
   * @param feedbackShowQuestion
   */
  public void setFeedbackShowQuestion(boolean feedbackShowQuestion)
  {
    this.feedbackShowQuestion = feedbackShowQuestion;
  }

  /**
   * feedback response?
   * @return true or false
   */
  public boolean isFeedbackShowResponse()
  {
    return feedbackShowResponse;
  }

  /**
   * feedback response?
   * @param feedbackShowResponse
   */
  public void setFeedbackShowResponse(boolean feedbackShowResponse)
  {
    this.feedbackShowResponse = feedbackShowResponse;
  }

  /**
   * anonymous grading?
   * @return true or false
   */
  public boolean isAnonymousGrading()
  {
    return anonymousGrading;
  }

  /**
   * anonymous grading?
   * @param anonymousGrading
   */
  public void setAnonymousGrading(boolean anonymousGrading)
  {
    this.anonymousGrading = anonymousGrading;
  }

  /**
   * collect section metadata?
   * @return true or false
   */
  public boolean isCollectSectionMetadata()
  {
    return collectSectionMetadata;
  }

  /**
   * collect section metadata?
   * @param collectSectionMetadata
   */
  public void setCollectSectionMetadata(boolean collectSectionMetadata)
  {
    this.collectSectionMetadata = collectSectionMetadata;
  }

  /**
   * collect item metadata?
   * @return true or false
   */
  public boolean getCollectItemMetadata()
  {
    return collectItemMetadata;
  }

  /**
   * collect item metadata?
   * @param collectItemMetadata
   */
  public void setCollectItemMetadata(boolean collectItemMetadata)
  {
    this.collectItemMetadata = collectItemMetadata;
  }

  /**
   * allowed IP addresses, '\n' delimited
   * @return allowed IP addresses, '\n' delimited
   */
  public String getAllowedIP()
  {
    return allowedIP;
  }

  /**
   * allowed IP addresses, '\n' delimited
   * @param allowedIP allowed IP addresses, '\n' delimited
   */
  public void setAllowedIP(String allowedIP)
  {
    this.allowedIP = allowedIP;
  }

  /**
   * derived property
   * list of IP address strings
   * @return list of IP address strings
   */
  public ArrayList getAllowedIPList()
  {
    ArrayList list = new ArrayList();
    StringTokenizer st = new StringTokenizer(allowedIP, "\n", false);
    while (st.hasMoreTokens())
    {
      list.add(st.nextToken());
    }
    return list;
  }

  /**
   * password to restrict to
   * @return password to restrict to
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * password to restrict to
   * @param password password to restrict to
   */
  public void setPassword(String password)
  {
    this.password = password;
  }

  /**
   * assessment organization flag
   * <ul>
   * <li>I=each item (question) is on a separate page</li>
   * <li>S=each section (part) is on a separate page</li>
   * <li>A=assessment on a single page</li>
   * </ul>
   * @return "I", "S" or "A"
   */
  public String getEnableDisableAssessmentOrganization()
  {
    return enableDisableAssessmentOrganization;
  }

  /**
   *  set assessment organization flag
   * <ul>
   * <li>I=each item (question) is on a separate page</li>
   * <li>S=each section (part) is on a separate page</li>
   * <li>A=assessment on a single page</li>
   * </ul>
   * @param enableDisableAssessmentOrganization should be "I", "S" or "A"
   */
  public void setEnableDisableAssessmentOrganization(String
    enableDisableAssessmentOrganization)
  {
    this.enableDisableAssessmentOrganization =
      enableDisableAssessmentOrganization;
  }

  /**
   * continuous numbering?
   * @return true or false
   */
  public boolean isDisplayNumberingContinuous()
  {
    return displayNumberingContinuous;
  }

  /**
   * continuous numbering?
   * @param displayNumberingContinuous
   */
  public void setDisplayNumberingContinuous(boolean
    displayNumberingContinuous)
  {
    this.displayNumberingContinuous = displayNumberingContinuous;
  }

  /**
   * random navigation?
   * @return true or false
   */
  public boolean isNavigationRandom()
  {
    return navigationRandom;
  }

  /**
   * random navigation?
   * @param navigationRandom
   */
  public void setNavigationRandom(boolean navigationRandom)
  {
    this.navigationRandom = navigationRandom;
  }

  /**
   * unlimited tries?
   * @return true or false
   */
  public boolean isUnlimitedAttempts()
  {
    return unlimitedAttempts;
  }

  /**
   * unlimited tries?
   * @param unlimitedAttempts
   */
  public void setUnlimitedAttempts(boolean unlimitedAttempts)
  {
    this.unlimitedAttempts = unlimitedAttempts;
  }

  /**
   * maximum tries
   * @return true or false
   */
  public int getMaxAttempts()
  {
    return maxAttempts;
  }

  /**
   * maximum tries
   * @param maxAttempts
   */
  public void setMaxAttempts(int maxAttempts)
  {
    this.maxAttempts = maxAttempts;
  }

  /**
   * immediate feedback?
   * @return true or false
   */
  public boolean isFeedbackImmediate()
  {
    return feedbackImmediate;
  }

  /**
   * set immediate feedback
   * @param feedbackImmediate
   */
  public void setFeedbackImmediate(boolean feedbackImmediate)
  {
    this.feedbackImmediate = feedbackImmediate;
  }

  /**
   * record highest score
   * @return true or -- false in which record average
   */
  public boolean isRecordHighestScore()
  {
    return recordHighestScore;
  }

  /**
   * record highest score
   * @param recordHighestScore if true, false if use average
   */
  public void setRecordHighestScore(boolean recordHighestScore)
  {
    this.recordHighestScore = recordHighestScore;
  }

  /**
   * keywords
   * @return
   */
  public String getKeywords()
  {
    return keywords;
  }

  /**
   * keywords
   * @param keywords
   */
  public void setKeywords(String keywords)
  {
    this.keywords = keywords;
  }

  /**
   * objectives
   * @return
   */
  public String getObjectives()
  {
    return objectives;
  }

  /**
   * objectives
   * @param objectives
   */
  public void setObjectives(String objectives)
  {
    this.objectives = objectives;
  }

  /**
   * rubrics
   * @return
   */
  public String getRubrics()
  {
    return rubrics;
  }

  /**
   * rubrics
   * @param rubrics
   */
  public void setRubrics(String rubrics)
  {
    this.rubrics = rubrics;
  }
}
