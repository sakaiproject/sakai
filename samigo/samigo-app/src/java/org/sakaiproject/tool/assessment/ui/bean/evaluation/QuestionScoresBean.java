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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.jsf2.model.PhaseAware;
import org.sakaiproject.jsf2.renderer.PagerRenderer;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.business.entity.RecordingData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.AttachmentUtil;
import org.sakaiproject.tool.assessment.util.ItemCancellationUtil;
import org.sakaiproject.util.ResourceLoader;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
  private static final ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.EvaluationMessages");

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
  private ItemDataIfc itemData;
  @Setter
  private String score;
  @Setter
  private String discount;
  @Getter @Setter
  private String minScore;
  @Setter
  private String answer;
  @Setter
  private String questionScoreComments;
  @Setter
  private String lateHandling; // read-only property set for UI late handling
  @Setter
  private String dueDate;
  @Setter
  private String sortType;
  @Setter
  private boolean sortAscending = true;
  @Setter
  private String roleSelection;
  @Getter
  private String allSubmissions;
  @Getter @Setter
  private RecordingData recordingData;
  @Setter
  private String totalPeople;
  @Setter
  private String typeId;
  @Getter @Setter
  private Map scoresByItem;
  @Getter @Setter
  private Map itemScoresMap;
  @Getter @Setter
  private PublishedAssessmentIfc publishedAssessment;

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
  private int maxDisplayedRows = PagerRenderer.MAX_PAGE_SIZE;
  @Getter @Setter
  private int dataRows;
  @Getter @Setter
  private int audioMaxDisplayedScoreRows;
  @Getter @Setter
  private int otherMaxDisplayedScoreRows;
  @Getter @Setter
  private boolean hasAudioMaxDisplayedScoreRowsChanged;
  
  //Searching
  @Getter
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
  @Setter
  private String showTagsInEvaluationStyle;

  @Setter @Getter
  private String rubricStateDetails;

  @Setter @Getter
  private boolean hasAssociatedRubric;

  @Setter @Getter
  private boolean cancellationAllowed;

  @Setter @Getter
  private boolean emiItemPresent;

  @Setter @Getter
  private boolean randomItemPresent;

  @Setter @Getter
  private String associatedRubricType;

  private static final ToolManager toolManager = (ToolManager) ComponentManager.get(ToolManager.class);

  /**
   * Creates a new QuestionScoresBean object.
   */
  public QuestionScoresBean()
  {
    log.debug("Creating a new QuestionScoresBean");
  }

	protected void init() {
		boolean valueChanged = false;
		if (ContextUtil.lookupParam("resetCache") != null && ContextUtil.lookupParam("resetCache").equals("true")){
			allAgents = null;
			valueChanged = true;
			searchString = null;
		}

		defaultSearchString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "search_default_student_search_string");

		if (searchString == null) {
			searchString = defaultSearchString;
		}

		// Get allAgents only at the first time
		if (allAgents == null) {
			allAgents = getAllAgents(valueChanged);
		}

		List matchingAgents;
		if (isFilteredSearch()) {
			matchingAgents = findMatchingAgents(searchString);
		}
		else {
			matchingAgents = allAgents;
		}
		dataRows = matchingAgents.size();
		List newAgents;
		if (maxDisplayedRows == 0) {
			newAgents = matchingAgents;
		} else {
			int nextPageRow = Math.min(firstRow + maxDisplayedRows, dataRows);
			newAgents = new ArrayList(matchingAgents.subList(firstRow, nextPageRow));
			log.debug("init(): subList {},{}", firstRow, nextPageRow);
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

  public String getSiteId() {
    return toolManager.getCurrentPlacement().getContext();
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

public List getAllAgents(boolean valueChanged)
{
	  String publishedId = ContextUtil.lookupParam("publishedId");
	  QuestionScoreListener questionScoreListener = new QuestionScoreListener();
	  if (!questionScoreListener.questionScores(publishedId, this, valueChanged))
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
	        log.debug("setSearchString {}", searchString);
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
	
	/**
	 * Normalize text for accent-insensitive search
	 */
	private String normalizeSearchText(String text) {
		if (text == null) return null;
		return java.text.Normalizer.normalize(text.toLowerCase(), java.text.Normalizer.Form.NFD)
				.replaceAll("[\\u0300-\\u036f]", "");
	}

	public List findMatchingAgents(final String pattern) {
		List filteredList = new ArrayList();
		String normalizedPattern = normalizeSearchText(pattern);

		for(Iterator iter = allAgents.iterator(); iter.hasNext();) {
			AgentResults result = (AgentResults)iter.next();
			String normalizedDisplayName = normalizeSearchText(result.getDisplayName());
			String normalizedAgentEid = normalizeSearchText(result.getAgentEid());

			if (normalizedDisplayName.contains(normalizedPattern) ||
				normalizedAgentEid.startsWith(normalizedPattern)) {
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

    @SuppressWarnings("unused")
    public String getShowTagsInEvaluationStyle() {
        if (ServerConfigurationService.getBoolean("samigo.evaluation.usetags", Boolean.FALSE)){
            return "";
        }else{
            return "display:none;";
        }
    }

    @SuppressWarnings("unused")
	public String getCDNQuery() {
		return PortalUtils.getCDNQuery();
	}

    @SuppressWarnings("unused")
	public boolean isEnablePdfExport() {
        return ServerConfigurationService.getBoolean(RubricsConstants.RBCS_EXPORT_PDF, true);
	}

  public Boolean getDeliveryItemCancelled() {
      return deliveryItem != null || deliveryItem.isEmpty()
          ? ItemCancellationUtil.isCancelled((ItemDataIfc) deliveryItem.stream().findAny().get())
          : null;
  }

  @SuppressWarnings("unused")
  public boolean isTrackingQuestions() {
      return Boolean.valueOf(publishedAssessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.TRACK_QUESTIONS));
  }
}
