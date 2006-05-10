/**********************************************************************************
 * $URL$
 * $Id$
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



package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.business.entity.RecordingData;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;

/**
 * <p>Title: sakaiproject.org</p>
 * <p>Description: class form for evaluating submission status</p>
 * <p>Company: Stanford University</p>
 *
 *
 */
public class SubmissionStatusBean
  implements Serializable
{
  private String assessmentId;
  private String publishedId;

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 5517587781720762296L;
  private String assessmentName;
  private String anonymous;
  private String groupName;
  private String maxScore;
  private Collection agents;
  private Collection sortedAgents;
  private String totalScore;
  private String adjustmentTotalScore;
  private String totalScoreComments;
  private String sortProperty;
  private String lateHandling; // read-only property set for UI late handling
  private String dueDate;
  private String sortType;
  private String roleSelection;
  private String allSubmissions;
  private RecordingData recordingData;
  private String totalPeople;
  private String firstItem;
  private HashMap answeredItems;
  private static Log log = LogFactory.getLog(SubmissionStatusBean.class);
  private String selectedSectionFilterValue = TotalScoresBean.ALL_SECTIONS_SELECT_VALUE;

  /**
   * Creates a new SubmissionStatusBean object.
   */
  public SubmissionStatusBean()
  {
    log.debug("Creating a new SubmissionStatusBean");
    resetFields();
  }

  /**
   * get assessment name
   *
   * @return the name
   */
  public String getAssessmentName()
  {
    return Validator.check(assessmentName, "N/A");
  }

  /**
   * set assessment name
   *
   * @param passessmentName the name
   */
  public void setAssessmentName(String passessmentName)
  {
    assessmentName = passessmentName;
  }

  /**
   * get assessment id
   *
   * @return the assessment id
   */
  public String getAssessmentId()
  {
    return Validator.check(assessmentId, "0");
  }

  /**
   * set assessment id
   *
   * @param passessmentId the id
   */
  public void setAssessmentId(String passessmentId)
  {
    assessmentId = passessmentId;
  }

  /**
   * get published id
   *
   * @return the published id
   */
  public String getPublishedId()
  {
    return Validator.check(publishedId, "0");
  }

  /**
   * set published id
   *
   * @param passessmentId the id
   */
  public void setPublishedId(String ppublishedId)
  {
    publishedId = ppublishedId;
  }

  /**
   * Is this anonymous grading?
   *
   * @return anonymous grading? true or false
   */
  public String getAnonymous()
  {
    return Validator.check(anonymous, "false");
  }

  /**
   * Set switch if this is anonymous grading.
   *
   * @param panonymous anonymous grading? true or false
   */
  public void setAnonymous(String panonymous)
  {
    anonymous = panonymous;
  }

  /**
   * Get the group name
   * @return group name
   */
  public String getGroupName()
  {
    return Validator.check(groupName, "N/A");
  }

  /**
   * set the group name
   *
   * @param pgroupName the name
   */
  public void setGroupName(String pgroupName)
  {
    groupName = pgroupName;
  }

  /**
   * get the max score
   *
   * @return the max score
   */
  public String getMaxScore()
  {
    return Validator.check(maxScore, "N/A");
  }

  /**
   * set max score
   *
   * @param pmaxScore set the max score
   */
  public void setMaxScore(String pmaxScore)
  {
    maxScore = pmaxScore;
  }

  /**
   * get an agent result collection
   *
   * @return the collection
   */
  public Collection getAgents()
  {
    if (agents == null)
      return new ArrayList();
    return agents;
  }

  /**
   * set the agent collection
   *
   * @param pagents the collection
   */
  public void setAgents(Collection pagents)
  {
    agents = pagents;
  }

  /** This is a read-only calculated property.
   * @return list of uppercase student initials
   */
  public String getAgentInitials()
  {
    Collection c = getAgents();
    String initials = "";
    if (c.isEmpty())
    {
      return "";
    }

    Iterator it = c.iterator();

    while (it.hasNext())
    {
      try
      {
        AgentResults ar = (AgentResults) it.next();
        String initial = ar.getLastInitial();
        initials = initials + initial;
      }
      catch (Exception ex)
      {
        // if there is any problem, we skip, and go on
      }
    }

    return initials.toUpperCase();
  }

  /**
   * get agent resutls as an array
   *
   * @return the array
   */
  public Object[] getAgentArray()
  {
    if (agents == null)
      return new Object[0];
    return agents.toArray();
  }

  /**
   * get the total number of students for this assessment
   *
   * @return the number
   */
  public String getTotalPeople()
  {
    return Validator.check(totalPeople, "N/A");
  }

  /**
   * set the total number of people
   *
   * @param ptotalPeople the total
   */
  public void setTotalPeople(String ptotalPeople)
  {
    totalPeople = ptotalPeople;
  }

  /**
   *
   * @return the total score
   */
  public String getTotalScore()
  {
    return Validator.check(totalScore, "N/A");
  }

  /**
   * set the total score
   *
   * @param pTotalScore the total score
   */
  public void setTotalScore(String pTotalScore)
  {
    totalScore = pTotalScore;
  }

  /**
   * get the adjustment to the total score
   *
   * @return the total score
   */
  public String getAdjustmentTotalScore()
  {
    return Validator.check(adjustmentTotalScore, "N/A");
  }

  /**
   * set the adjustment to total score
   *
   * @param pAdjustmentTotalScore the adjustment
   */
  public void setAdjustmentTotalScore(String pAdjustmentTotalScore)
  {
    adjustmentTotalScore = pAdjustmentTotalScore;
  }

  /**
   * get total score
   *
   * @return the total score
   */
  public String getTotalScoreComments()
  {
    return Validator.check(totalScoreComments, "");
  }

  /**
   * set comments for totals score
   *
   * @param pTotalScoreComments the comments
   */
  public void setTotalScoreComments(String pTotalScoreComments)
  {
    log.debug("setting total score comments to " + pTotalScoreComments);
    totalScoreComments = pTotalScoreComments;
  }

  /**
   * get late handling
   *
   * @return late handlign
   */
  public String getLateHandling()
  {
    return Validator.check(lateHandling, "1");
  }

  /**
   * set late handling
   *
   * @param plateHandling the late handling
   */
  public void setLateHandling(String plateHandling)
  {
    lateHandling = plateHandling;
  }

  /**
   * get the due date
   *
   * @return the due date as a String
   */
  public String getDueDate()
  {
    return Validator.check(dueDate, "N/A");
  }

  /**
   * set due date string
   *
   * @param dateString the date string
   */
  public void setDueDate(String dateString)
  {
    dueDate = dateString;
  }

  /**
   * get sort type
   * @return sort type
   */
  public String getSortType()
  {
    return Validator.check(sortType, "lastName");
  }

  /**
   * set sort type, trigger property sorts
   * @param psortType the type
   */
  public void setSortType(String psortType)
  {
    sortType = psortType;
  }

  /**
   * Is this an all submissions or, just the largest
   * @return true if is is, else false
   */
  public String getAllSubmissions()
  {
    return Validator.check(allSubmissions, "false");
  }

  /**
   * set whether all submissions are to be exposed
   * @param pallSubmissions true if it is
   */
  public void setAllSubmissions(String pallSubmissions)
  {
    allSubmissions = pallSubmissions;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getRoleSelection()
  {
    return Validator.check(roleSelection, "N/A");
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param proleSelection DOCUMENTATION PENDING
   */
  public void setRoleSelection(String proleSelection)
  {
    roleSelection = proleSelection;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getFirstItem()
  {
    return Validator.check(firstItem, "");
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param proleSelection DOCUMENTATION PENDING
   */
  public void setFirstItem(String pfirstItem)
  {
    firstItem = pfirstItem;
  }

  /**
   * reset the fields
   */
  public void resetFields()
  {
    agents = new ArrayList();
    setAgents(agents);
  }

  /**
   * encapsulates audio recording info
   * @return recording data
   */
  public RecordingData getRecordingData()
  {
    return this.recordingData;
  }

  /**
   * encapsulates audio recording info
   * @param rd
   */
  public void setRecordingData(RecordingData rd)
  {
    this.recordingData = rd;
  }

  /**
   * This returns a map of which items actually have answers.
   * Used by QuestionScores.
   */
  public HashMap getAnsweredItems()
  {
    return answeredItems;
  }

  /**
   * This stores a map of which items actually have answers.
   * Used by QuestionScores.
   */
  public void setAnsweredItems(HashMap newItems)
  {
    answeredItems = newItems;
  }


  public String getSelectedSectionFilterValue() {
    return selectedSectionFilterValue;
  }

  public void setSelectedSectionFilterValue(String param ) {
      this.selectedSectionFilterValue = param;
  }

}
