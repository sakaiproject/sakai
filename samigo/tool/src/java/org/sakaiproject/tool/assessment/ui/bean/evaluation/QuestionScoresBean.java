/**********************************************************************************
* $URL$
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
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;

/**
 * <p>Description: class form for evaluating question scores</p>
 *
 * Used to be org.navigoproject.ui.web.form.evaluation.QuestionScoresForm
 *
 * @author Rachel Gollub
 */
public class QuestionScoresBean
  implements Serializable
{
  private String assessmentId;
  private String publishedId;

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 5517587781720762296L;
  private String assessmentName;
  private String itemName;
  private String itemId;
  private String anonymous;
  private String groupName;
  private String maxScore;
  private Collection agents;
  private Collection sortedAgents;
  private Collection sections;
  private Collection deliveryItem;
  private String score;
  private String answer;
  private String questionScoreComments;
  private String sortProperty;
  private String lateHandling; // read-only property set for UI late handling
  private String dueDate;
  private String sortType;
  private String roleSelection;
  private String allSubmissions;
  private RecordingData recordingData;
  private String totalPeople;
  private String typeId;
  private HashMap scoresByItem;
  private static Log log = LogFactory.getLog(QuestionScoresBean.class);
  private String selectedSectionFilterValue = TotalScoresBean.ALL_SECTIONS_SELECT_VALUE;

  /**
   * Creates a new QuestionScoresBean object.
   */
  public QuestionScoresBean()
  {
    log.debug("Creating a new QuestionScoresBean");
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
   * get item name
   *
   * @return the name
   */
  public String getItemName()
  {
    return Validator.check(itemName, "N/A");
  }

  /**
   * set item name
   *
   * @param pitemName the name
   */
  public void setItemName(String pitemName)
  {
    itemName = pitemName;
  }

  /**
   * get item id
   *
   * @return the id
   */
  public String getItemId()
  {
    return Validator.check(itemId, "1");
  }

  /**
   * set item id
   *
   * @param pitemId the id
   */
  public void setItemId(String pitemId)
  {
    itemId = pitemId;
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
   * get the max Point
   *
   * @return the max point
   */
  public String getMaxPoint()
    {  
	try{
	if (Double.parseDouble(this.getMaxScore())>1.0)
	    return this.getMaxScore()+" Points";
	else
	    return this.getMaxScore()+ " Point";
	}
	catch(NumberFormatException e){
	    return this.getMaxScore()+ " Point";
	}
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

  /**
   * get a list of sections
   *
   * @return the collection
   */
  public Collection getSections()
  {
    if (sections == null)
      return new ArrayList();
    return sections;
  }

  /**
   * set the section list
   *
   * @param psections the collection
   */
  public void setSections(Collection psections)
  {
    sections = psections;
  }

  /**
   * get the item to display
   *
   * @return the collection
   */
  public Collection getDeliveryItem()
  {
    if (deliveryItem == null)
      return new ArrayList();
    return deliveryItem;
  }

  /**
   * set the delivery item
   *
   * @param pitem the collection
   */
  public void setDeliveryItem(Collection pitem)
  {
    deliveryItem = pitem;
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
   * @return the score
   */
  public String getScore()
  {
    return Validator.check(score, "N/A");
  }

  /**
   * set the score
   *
   * @param pScore the score
   */
  public void setScore(String pScore)
  {
    score = pScore;
  }

  /**
   * get the answer text
   *
   * @return the answer text
   */
  public String getAnswer()
  {
    return Validator.check(answer, "N/A");
  }

  /**
   * set the answer text
   *
   * @param pAnswertext the answer text
   */
  public void setAnswer(String pAnswertext)
  {
    answer = pAnswertext;
  }

  /**
   * get comments
   *
   * @return the comments
   */
  public String getQuestionScoreComments()
  {
    return Validator.check(questionScoreComments, "");
  }

  /**
   * set comments for question score
   *
   * @param pQuestionScoreComments the comments
   */
  public void setQuestionScoreComments(String pQuestionScoreComments)
  {
    log.debug("setting question score comments to "+pQuestionScoreComments);
    questionScoreComments = pQuestionScoreComments;
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
    return allSubmissions;
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
  public String getTypeId()
  {
    return Validator.check(typeId, "1");
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param ptypeId DOCUMENTATION PENDING
   */
  public void setTypeId(String ptypeId)
  {
    typeId = ptypeId;
  }

  /**
   * reset the fields
   */
  public void resetFields()
  {
    //agents = new ArrayList();
    //setAgents(agents);
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

  public HashMap getScoresByItem()
  {
    return scoresByItem;
  }

  public void setScoresByItem(HashMap newScores)
  {
    scoresByItem = newScores;
  }


  public String getSelectedSectionFilterValue() {
    return selectedSectionFilterValue;
  }

  public void setSelectedSectionFilterValue(String param ) {
      this.selectedSectionFilterValue = param;
  }

  // itemScoresMap = (publishedItemId, HashMap)
  //               = (Long publishedItemId, (Long publishedItemId, Array itemGradings))
  private HashMap itemScoresMap; 
  public void setItemScoresMap(HashMap itemScoresMap){
    this.itemScoresMap = itemScoresMap;
  }
  public HashMap getItemScoresMap(){
    return itemScoresMap;
  }

}
