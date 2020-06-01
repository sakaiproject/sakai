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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;

import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;

import org.sakaiproject.jsf2.model.PhaseAware;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.tool.assessment.business.entity.RecordingData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.AttachmentUtil;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.ResourceLoader;

/* For evaluation: Question Scores backing bean. */
@Slf4j
@ManagedBean(name="questionScores")
@SessionScoped
public class QuestionScoresBean implements Serializable, PhaseAware {
  @Setter
  private String assessmentId;
  @Setter
  private String publishedId;

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 5517587781720762296L;

  public static final String SHOW_SA_RATIONALE_RESPONSES_INLINE = "2"; 
  public static final String SHOW_SA_RATIONALE_RESPONSES_POPUP = "1"; 

  @Setter
  private String assessmentName;
  @Setter
  private String itemName;
  @Setter
  private String partName;
  @Setter
  private String itemId;
  @Setter
  private String anonymous;
  @Setter
  private String groupName;
  @Getter @Setter
  private double maxScore;
  @Getter @Setter @NonNull
  private List agents = new ArrayList();
  @Getter @Setter
  private Collection sections;
  @Getter @Setter
  private Collection deliveryItem;
  @Getter @Setter
  private String score;
  @Getter @Setter
  private String discount;
  @Getter @Setter
  private String answer;
  @Getter @Setter
  private String questionScoreComments;
  @Getter @Setter
  private String lateHandling; // read-only property set for UI late handling
  @Getter @Setter
  private String dueDate;
  @Getter @Setter
  private String sortType;
  @Getter @Setter
  private boolean sortAscending = true;
  @Getter @Setter
  private String roleSelection;
  @Getter @Setter
  private String allSubmissions;
  @Getter @Setter
  private RecordingData recordingData;
  @Getter @Setter
  private String totalPeople;
  @Getter @Setter
  private String typeId;
  @Getter @Setter
  private Map scoresByItem;
  @Getter @Setter
  private Map itemScoresMap;
  @Getter @Setter
  private PublishedAssessmentIfc publishedAssessment;

  @Getter @Setter
  private String selectedSectionFilterValue = null;
  @Getter @Setter
  private String selectedSARationaleView = SHOW_SA_RATIONALE_RESPONSES_POPUP;
  @Setter
  private List allAgents;
  @Getter @Setter
  private boolean haveModelShortAnswer;
  
  //Paging.
  @Getter @Setter
  private int firstRow;
  @Getter @Setter
  private int maxDisplayedRows;
  @Getter @Setter
  private int dataRows;
  @Getter @Setter
  private int audioMaxDisplayedScoreRows;
  @Getter @Setter
  private int otherMaxDisplayedScoreRows;
  @Getter @Setter
  private boolean hasAudioMaxDisplayedScoreRowsChanged;
  
  //Searching
  @Getter @Setter
  private String searchString;
  @Getter @Setter
  private String defaultSearchString;
  
  @Getter @Setter
  private Map userIdMap;
  @Getter @Setter
  private Map<Long, AgentResults> agentResultsByItemGradingId;
  @Getter @Setter
  private boolean anyItemGradingAttachmentListModified;
  @Getter @Setter
  private Boolean releasedToGroups = null;
  @Getter @Setter
  private String showTagsInEvaluationStyle;

  @Setter @Getter
  private String rubricStateDetails;

  @Setter @Getter
  private boolean hasAssociatedRubric;

  /**
   * Creates a new QuestionScoresBean object.
   */
  public QuestionScoresBean()
  {
    log.debug("Creating a new QuestionScoresBean");
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
		dataRows = matchingAgents.size();
		List newAgents = new ArrayList();
		if (maxDisplayedRows == 0) {
			newAgents.addAll(matchingAgents);
		} else {
			int nextPageRow = Math.min(firstRow + maxDisplayedRows, dataRows);
			newAgents.addAll(matchingAgents.subList(firstRow, nextPageRow));
			log.debug("init(): subList " + firstRow + ", " + nextPageRow);
		}

		agents.clear();
		agents.addAll(newAgents);

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
   * get part name
   *
   * @return the name
   */
  public String getPartName()
  {
    return Validator.check(partName, "N/A");
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
   * get item id
   *
   * @return the id
   */
  public String getItemId()
  {
    return Validator.check(itemId, "1");
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
   * get published id
   *
   * @return the published id
   */
  public String getPublishedId()
  {
    return Validator.check(publishedId, "0");
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
   * Get the group name
   * @return group name
   */
  public String getGroupName()
  {
    return Validator.check(groupName, "N/A");
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

  /** This is a read-only calculated property.
   * @return list of uppercase student initials
   */
  public String getAgentInitials()
  {
    List c = getAgents();
    
    
    StringBuilder initialsbuf = new StringBuilder();  
    
    if (c.isEmpty())
    {
      return "";
    }

    for(AgentResults ar : (List<AgentResults>) c)
    {
      try
      {
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
   * get the total number of students for this assessment
   *
   * @return the number
   */
  public String getTotalPeople()
  {
    return Validator.check(totalPeople, "N/A");
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
   *
   * @return the discount
   */
  public String getDiscount()
  {
    return Validator.check(discount, "N/A");
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
   * get comments
   *
   * @return the comments
   */
  public String getQuestionScoreComments()
  {
    return Validator.check(questionScoreComments, "");
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
   * get the due date
   *
   * @return the due date as a String
   */
  public String getDueDate()
  {
    return Validator.check(dueDate, "N/A");
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
   * is scores table sorted in ascending order
   * @return true if it is
   */
  public boolean isSortAscending()
  {
    return sortAscending;
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
   * @return DOCUMENTATION PENDING
   */
  public String getTypeId()
  {
    return Validator.check(typeId, "1");
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

	public boolean isReleasedToGroups() {
		return this.getPublishedAssessment().getAssessmentAccessControl().getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS);
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

    public String getShowTagsInEvaluationStyle() {
        if (ServerConfigurationService.getBoolean("samigo.evaluation.usetags", Boolean.FALSE)){
            return "";
        }else{
            return "display:none;";
        }
    }

	public boolean isHasAssociatedRubric() {
		return hasAssociatedRubric;
	}

	public String getCDNQuery() {
		return PortalUtils.getCDNQuery();
	}
}
