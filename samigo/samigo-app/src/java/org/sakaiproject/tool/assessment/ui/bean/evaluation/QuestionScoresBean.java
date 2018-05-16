/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ActionEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.sakaiproject.jsf.model.PhaseAware;
import org.sakaiproject.tool.assessment.business.entity.RecordingData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.AttachmentUtil;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>Description: class form for evaluating question scores</p>
 *
 */
@Slf4j
public class QuestionScoresBean
  implements Serializable, PhaseAware
{
  private String assessmentId;
  private String publishedId;

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 5517587781720762296L;

  public static final String SHOW_SA_RATIONALE_RESPONSES_INLINE = "2"; 
  public static final String SHOW_SA_RATIONALE_RESPONSES_POPUP = "1"; 

  private String assessmentName;
  private String itemName;
    private String partName;
  private String itemId;
  private String anonymous;
  private String groupName;
  private double maxScore;
  private Collection agents;
  //private Collection sortedAgents;
  private Collection sections;
  private Collection deliveryItem;
  private String score;
  private String discount;
  private String answer;
  private String questionScoreComments;
  //private String sortProperty;
  private String lateHandling; // read-only property set for UI late handling
  private String dueDate;
  private String sortType;
  private boolean sortAscending = true;
  private String roleSelection;
  private String allSubmissions;
  private RecordingData recordingData;
  private String totalPeople;
  private String typeId;
  private Map scoresByItem;

  //private String selectedSectionFilterValue = TotalScoresBean.ALL_SECTIONS_SELECT_VALUE;
  private String selectedSectionFilterValue = null;
  
  private String selectedSARationaleView =SHOW_SA_RATIONALE_RESPONSES_POPUP;
  private List allAgents;
  private boolean haveModelShortAnswer;
  
  //Paging.
  private int firstScoreRow;
  private int maxDisplayedScoreRows;
  private int scoreDataRows;
  private int audioMaxDisplayedScoreRows;
  private int othersMaxDisplayedScoreRows;
  private boolean hasAudioMaxDisplayedScoreRowsChanged;
  
  //Searching
  private String searchString;
  private String defaultSearchString;
  
  private Map userIdMap;
  private Map agentResultsByItemGradingId;
  private boolean isAnyItemGradingAttachmentListModified;
  private Boolean releasedToGroups = null;

    private String showTagsInEvaluationStyle;

  /**
   * Creates a new QuestionScoresBean object.
   */
  public QuestionScoresBean()
  {
    log.debug("Creating a new QuestionScoresBean");
    resetFields();
  }

	protected void init() {
        defaultSearchString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "search_default_student_search_string");

        if (searchString == null) {
			searchString = defaultSearchString;
		}
		
		// Get allAgents only at the first time
		if (allAgents == null) {
			allAgents = getAllAgents();
		}
		
		List matchingAgents;
		if (isFilteredSearch()) {
			matchingAgents = findMatchingAgents(searchString);
		}
		else {
			matchingAgents = allAgents;
		}
		scoreDataRows = matchingAgents.size();
		List newAgents = null;
		if (maxDisplayedScoreRows == 0) {
			newAgents = matchingAgents;
		} else {
			int nextPageRow = Math.min(firstScoreRow + maxDisplayedScoreRows, scoreDataRows);
			newAgents = new ArrayList(matchingAgents.subList(firstScoreRow, nextPageRow));
			log.debug("init(): subList " + firstScoreRow + ", " + nextPageRow);
		}
		
		agents = newAgents;
	}
 
	// Following three methods are for interface PhaseAware
	public void endProcessValidators() {
		log.debug("endProcessValidators");
	}

	public void endProcessUpdates() {
		log.debug("endProcessUpdates");
	}
	
	public void startRenderResponse() {
		log.debug("startRenderResponse");
		init();
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
   * get part name
   *
   * @return the name
   */
  public String getPartName()
  {
    return Validator.check(partName, "N/A");
  }

  /**
   * set part name
   *
   * @param ppartName the name
   */
  public void setPartName(String ppartName)
  {
    partName = ppartName;
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
  public double getMaxScore()
  {
    return maxScore;
  }

  /**
   * set max score
   *
   * @param pmaxScore set the max score
   */
  public void setMaxScore(double pmaxScore)
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
	  ResourceLoader rb=new ResourceLoader("org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
	try{
		if (this.getMaxScore() == 1.0)
			return Precision.round(this.getMaxScore(), 2)+ " " + rb.getString("point");
	else
		return Precision.round(this.getMaxScore(), 2)+ " " + rb.getString("points");
	}
	catch(NumberFormatException e){
		return Precision.round(this.getMaxScore(), 2)+ " " + rb.getString("point");
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
    
    
    StringBuilder initialsbuf = new StringBuilder();  
    
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
        initialsbuf.append(initial); 
      }
      catch (Exception ex)
      {
        log.warn(ex.getMessage());
        // if there is any problem, we skip, and go on
      }
    }

    String initials = initialsbuf.toString();
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
   *
   * @return the discount
   */
  public String getDiscount()
  {
    return Validator.check(discount, "N/A");
  }
 
  /**
   * set the discount
   *
   * @param pDiscount the discount
   */
  public void setDiscount(String pDiscount)
  {
    discount = pDiscount;
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
    if (!Boolean.parseBoolean(getAnonymous())) {
  	  return Validator.check(sortType, "lastName");
    }
    else {
  	  return Validator.check(sortType, "assessmentGradingId");
    }
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
   * is scores table sorted in ascending order
   * @return true if it is
   */
  public boolean isSortAscending()
  {
    return sortAscending;
  }

  /**
  *
  * @param sortAscending is scores table sorted in ascending order
  */
 public void setSortAscending(boolean sortAscending)
 {
   this.sortAscending = sortAscending;
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
    if (!pallSubmissions.equals(this.allSubmissions)) {
    	this.allSubmissions = pallSubmissions;
		setFirstRow(0); // clear the paging when we update the search
    }
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

  public Map getScoresByItem()
  {
    return scoresByItem;
  }

  public void setScoresByItem(Map newScores)
  {
    scoresByItem = newScores;
  }


  public String getSelectedSectionFilterValue() {
	  // lazy initialization
	  if (selectedSectionFilterValue == null) {
		  if (isReleasedToGroups()) {
			  setSelectedSectionFilterValue(TotalScoresBean.RELEASED_SECTIONS_GROUPS_SELECT_VALUE);
		  }
		  else {
			  setSelectedSectionFilterValue(TotalScoresBean.ALL_SECTIONS_SELECT_VALUE);
		  }
	  }

	  return selectedSectionFilterValue;
  }

  public void setSelectedSectionFilterValue(String param ) {
      if (!param.equals(this.selectedSectionFilterValue)) {
			this.selectedSectionFilterValue = param;
			setFirstRow(0); // clear the paging when we update the search
      }
  }

  // itemScoresMap = (publishedItemId, HashMap)
  //               = (Long publishedItemId, (Long publishedItemId, Array itemGradings))
  private Map itemScoresMap;
  public void setItemScoresMap(Map itemScoresMap){
    this.itemScoresMap = itemScoresMap;
  }
  public Map getItemScoresMap(){
    return itemScoresMap;
  }

  private PublishedAssessmentIfc publishedAssessment;
  public void setPublishedAssessment(PublishedAssessmentIfc publishedAssessment){
    this.publishedAssessment = publishedAssessment; 
  }
  public PublishedAssessmentIfc getPublishedAssessment(){
    return publishedAssessment;
  }
 
public String getSelectedSARationaleView() {
	return selectedSARationaleView;
}

public void setSelectedSARationaleView(String selectedSARationaleView) {
	this.selectedSARationaleView = selectedSARationaleView;
}

public int getFirstRow() {
    return firstScoreRow;
}
public void setFirstRow(int firstRow) {
    firstScoreRow = firstRow;
}

public int getMaxDisplayedRows() {
    return maxDisplayedScoreRows;
}
public void setMaxDisplayedRows(int maxDisplayedRows) {
    maxDisplayedScoreRows = maxDisplayedRows;
}

public int getAudioMaxDisplayedScoreRows() {
    return audioMaxDisplayedScoreRows;
}
public void setAudioMaxDisplayedScoreRows(int audioMaxDisplayedRows) {
	audioMaxDisplayedScoreRows = audioMaxDisplayedRows;
}

public int getOtherMaxDisplayedScoreRows() {
    return othersMaxDisplayedScoreRows;
}
public void setOtherMaxDisplayedScoreRows(int otherMaxDisplayedRows) {
	othersMaxDisplayedScoreRows = otherMaxDisplayedRows;
}

public boolean getHasAudioMaxDisplayedScoreRowsChanged() {
    return hasAudioMaxDisplayedScoreRowsChanged;
}
public void setHasAudioMaxDisplayedScoreRowsChanged(boolean hasAudioMaxDisplayedRowsChanged) {
	hasAudioMaxDisplayedScoreRowsChanged = hasAudioMaxDisplayedRowsChanged;
}

public int getDataRows() {
    return scoreDataRows;
}

public void setAllAgents(List allAgents) {
	  this.allAgents = allAgents;
}

public List getAllAgents()
{
	  String publishedId = ContextUtil.lookupParam("publishedId");
	  QuestionScoreListener questionScoreListener = new QuestionScoreListener();
	  if (!questionScoreListener.questionScores(publishedId, this, false))
	  {
		  throw new RuntimeException("failed to call questionScores.");
	  }
	  return allAgents;
}

public String getSearchString() {
    return searchString;
}
public void setSearchString(String searchString) {
    if (StringUtils.trimToNull(searchString) == null) {
        searchString = defaultSearchString;
    }
	if (!StringUtils.equals(searchString, this.searchString)) {
	    	log.debug("setSearchString " + searchString);
	        this.searchString = searchString;
	        setFirstRow(0); // clear the paging when we update the search
	    }
}

public void search(ActionEvent event) {
    // We don't need to do anything special here, since init will handle the search
    log.debug("search");
}

public void clear(ActionEvent event) {
    log.debug("clear");
    setSearchString(null);
}

	private boolean isFilteredSearch() {
      return !StringUtils.equals(searchString, defaultSearchString);
	}
	
	public List findMatchingAgents(final String pattern) {
		List filteredList = new ArrayList();
		// name1 example: John Doe
		StringBuilder name1;
		// name2 example: Doe, John
		StringBuilder name2;
		for(Iterator iter = allAgents.iterator(); iter.hasNext();) {
			AgentResults result = (AgentResults)iter.next();
			// name1 example: John Doe
			name1 = new StringBuilder(result.getFirstName());
			name1.append(" ");
			name1.append(result.getLastName());
			// name2 example: Doe, John
			name2 = new StringBuilder(result.getLastName());
			name2.append(", ");
			name2.append(result.getFirstName());
			if (result.getFirstName().toLowerCase().startsWith(pattern.toLowerCase()) ||
				result.getLastName().toLowerCase().startsWith(pattern.toLowerCase()) ||
				result.getAgentEid().toLowerCase().startsWith(pattern.toLowerCase()) ||
				name1.toString().toLowerCase().startsWith(pattern.toLowerCase()) ||
				name2.toString().toLowerCase().startsWith(pattern.toLowerCase())) {
				filteredList.add(result);
			}
		}
		return filteredList;
	}
	
	public boolean getHaveModelShortAnswer()
	{
		return haveModelShortAnswer;
	}

	public void setHaveModelShortAnswer(boolean haveModelShortAnswer)
	{
		this.haveModelShortAnswer = haveModelShortAnswer;
	}

	public boolean isReleasedToGroups() {
		return this.getPublishedAssessment().getAssessmentAccessControl().getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS);
	}
	
	public Map getUserIdMap()
	{
		return userIdMap;
	}

	public void setUserIdMap(Map userIdMap)
	{
		this.userIdMap = userIdMap;
	}	
	
	public void setAttachment(Long itemGradingId){
		List itemGradingAttachmentList = new ArrayList();
		AgentResults agentResults = (AgentResults) agentResultsByItemGradingId.get(itemGradingId);
		if (agentResults != null) {
			AttachmentUtil attachmentUtil = new AttachmentUtil();
			Set attachmentSet = new HashSet();
			if (agentResults.getItemGradingAttachmentList() != null) {
				attachmentSet = new HashSet(agentResults.getItemGradingAttachmentList());
			}
        	itemGradingAttachmentList = attachmentUtil.prepareAssessmentAttachment(agentResults.getItemGrading(), attachmentSet);
		
        	agentResults.setItemGradingAttachmentList(itemGradingAttachmentList);
		}
	}
	
	public Map getAgentResultsByItemGradingId()
	{
		return agentResultsByItemGradingId;
	}

	public void setAgentResultsByItemGradingId(Map agentResultsByItemGradingId)
	{
		this.agentResultsByItemGradingId = agentResultsByItemGradingId;
	}

	public boolean getIsAnyItemGradingAttachmentListModified() {
		return isAnyItemGradingAttachmentListModified;
	}

	public void setIsAnyItemGradingAttachmentListModified(boolean isAnyItemGradingAttachmentListModified)
	{
		this.isAnyItemGradingAttachmentListModified = isAnyItemGradingAttachmentListModified;
	}

    public String getShowTagsInEvaluationStyle() {
        if (ServerConfigurationService.getBoolean("samigo.evaluation.usetags", Boolean.FALSE)){
            return "";
        }else{
            return "display:none;";
        }
    }

    public void setShowTagsInEvaluationStyle(String showTagsInEvaluationStyle) {
        this.showTagsInEvaluationStyle = showTagsInEvaluationStyle;
    }
}
