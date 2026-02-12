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
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import lombok.Getter;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.jsf2.model.PhaseAware;
import org.sakaiproject.jsf2.renderer.PagerRenderer;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.business.entity.RecordingData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.StatisticsService;
import org.sakaiproject.tool.assessment.services.assessment.StatisticsService.SubmissionOutcome;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingSectionAwareServiceAPI;
import org.sakaiproject.tool.assessment.shared.impl.grading.GradingSectionAwareServiceImpl;
import org.sakaiproject.tool.assessment.ui.bean.util.TotalScoresExportBean;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.AttachmentUtil;

/* For evaluation: Total Scores backing bean. */
@Slf4j
@ManagedBean(name="totalScores")
@SessionScoped
public class TotalScoresBean implements Serializable, PhaseAware {
  private String assessmentId;
  private String publishedId;

  public static final String RELEASED_SECTIONS_GROUPS_SELECT_VALUE = "-2";
  
  public static final String ALL_SECTIONS_SELECT_VALUE = "-1";
  public static final String ALL_SUBMISSIONS = "3";
  public static final String LAST_SUBMISSION = "2";
  public static final String HIGHEST_SUBMISSION = "1";

  // indicates which listeber getUserIdMap() is called from
  public static final int CALLED_FROM_SUBMISSION_STATUS_LISTENER = 1;  
  public static final int CALLED_FROM_QUESTION_SCORE_LISTENER = 2;  
  public static final int CALLED_FROM_TOTAL_SCORE_LISTENER = 3;  
  public static final int CALLED_FROM_HISTOGRAM_LISTENER = 4;
  public static final int CALLED_FROM_HISTOGRAM_LISTENER_STUDENT = 5;
  public static final int CALLED_FROM_EXPORT_LISTENER = 6;
  public static final int CALLED_FROM_NOTIFICATION_LISTENER = 7;

  private static final SiteService siteService = (SiteService) ComponentManager.get(SiteService.class);
  private static final ToolManager toolManager = (ToolManager) ComponentManager.get(ToolManager.class);
  private static final ServerConfigurationService serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);

  private static final String SAK_PROP_DELETE_RESTRICTED = "samigo.removeSubmission.restricted";
  private static final boolean SAK_PROP_DELETE_RESTRICTED_DEFAULT = false;
  
  private boolean deleteRestrictedForCurrentSite = false;
 
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
  private boolean sortAscending = true;
  private String roleSelection;
  private String allSubmissions = ALL_SUBMISSIONS;
  private RecordingData recordingData;
  private String totalPeople;
  private String firstItem;
  private Map answeredItems;
  private boolean hasRandomDrawPart;
  private String scoringOption;
  
  private String selectedSectionFilterValue = null;

  private List sectionFilterSelectItems;
  private List<CourseSection> availableSections;
  private int availableSectionSize;
  private boolean releaseToAnonymous = false;
  private PublishedAssessmentData publishedAssessment; 
  private List allAgents;
  
  private String graderName;
  private String graderEmailInfo;
  
  // Paging.
  private int firstScoreRow;
  private int maxDisplayedScoreRows = PagerRenderer.MAX_PAGE_SIZE;
  private int scoreDataRows;
  
  // Searching
  private String searchString;
  private String defaultSearchString;
  private String applyToUngraded = "";
  
  private boolean multipleSubmissionsAllowed = false;
  private boolean isTimedAssessment = false;
  private boolean acceptLateSubmission = false;

  private Boolean releasedToGroups = null;
  private Map agentResultsByAssessmentGradingId;
  private boolean isAnyAssessmentGradingAttachmentListModified;
  private Map userIdMap;
  
  private boolean isAutoScored = false;
  private boolean hasFileUpload = false;
  
  @Setter
  private boolean isOneSelectionType  = true;

  @Setter
  private Map results;

  @Getter @Setter
  private boolean resultsAlreadyCalculated = false;

  /**
   * Creates a new TotalScoresBean object.
   */
  public TotalScoresBean()
  {
    log.debug("Creating a new TotalScoresBean");
    resetFields();
  }

	protected void init() {
        defaultSearchString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "search_default_student_search_string");

		try {
			Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
			boolean sitePropertyExists = false;
			if (site != null) {
				ResourceProperties siteProperties = site.getProperties();
				if (siteProperties != null) {
					String prop = StringUtils.trimToEmpty(siteProperties.getProperty(SAK_PROP_DELETE_RESTRICTED));
					sitePropertyExists = !prop.isEmpty();
					deleteRestrictedForCurrentSite = StringUtils.equalsIgnoreCase(prop, "true");
				}
			}

			if (!sitePropertyExists) {
				deleteRestrictedForCurrentSite = serverConfigurationService.getBoolean(SAK_PROP_DELETE_RESTRICTED, SAK_PROP_DELETE_RESTRICTED_DEFAULT);
			}

		} catch (Exception ex) {
			log.warn(ex.getMessage(), ex);
		}

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
		List newAgents;
		if (maxDisplayedScoreRows == 0) {
			newAgents = matchingAgents;
		} else {
			int nextPageRow = Math.min(firstScoreRow + maxDisplayedScoreRows, scoreDataRows);
			newAgents = new ArrayList(matchingAgents.subList(firstScoreRow, nextPageRow));
			log.debug("init(): subList " + firstScoreRow + ", " + nextPageRow);
		}
		
		agents = newAgents;	
	}

  public boolean getIsOneSelectionType() {
    PublishedAssessmentData currentPublishedAssessment = this.getPublishedAssessment();
    if (currentPublishedAssessment == null) {
      String currentPublishedId = StringUtils.trimToNull(getPublishedId());
      if (currentPublishedId != null && !"0".equals(currentPublishedId)) {
        PublishedAssessmentFacade publishedAssessmentFacade = new PublishedAssessmentService().getPublishedAssessment(currentPublishedId);
        if (publishedAssessmentFacade != null && publishedAssessmentFacade.getData() instanceof PublishedAssessmentData) {
          currentPublishedAssessment = (PublishedAssessmentData) publishedAssessmentFacade.getData();
          this.publishedAssessment = currentPublishedAssessment;
        }
      }
    }

    if (currentPublishedAssessment == null) {
      return isOneSelectionType;
    } else {
      for (Object sectionObject : currentPublishedAssessment.getSectionArray()) {
        PublishedSectionData sectionData = (PublishedSectionData) sectionObject;
        for (Object itemObject : sectionData.getItemArray()) {
          PublishedItemData item = (PublishedItemData) itemObject;
          if (isTallyableItemType(item.getTypeId())) {
            return true;
          }
        }
      }
      return false;
    }
  }

  /**
   * Get the responses sent by each student as a Map. This function will only work 
   * when the test only use the selection type answers. This Map has like key the 
   * agentId (student) and in the value has a List of Integer that means:
   *  - First (0 in the array): Correct responses count
   *  - Second (1 in the array): Incorrect responses count
   *  - Third (2 in the array): Empty responses count
   * 
   * @return results - Map
   */
  public Map getResults() {
    if (!this.isResultsAlreadyCalculated()) {
      this.setResultsAlreadyCalculated(true);
      // Instance a new PublishedAssessmentService to get all the published Answer for each student
      PublishedAssessmentService pubAssessmentService = new PublishedAssessmentService();
      PublishedAssessmentIfc publishedAssessmentData = pubAssessmentService.getPublishedAssessment(this.getPublishedId());
      Map publishedAnswerHash = pubAssessmentService.preparePublishedAnswerHash(publishedAssessmentData);
      // Instance a new GradingService to get all the student responses
      GradingService gradingService = new GradingService();
      StatisticsService statisticsService = new StatisticsService();
      Map<Long, List<Integer>> resultsByUser = new HashMap<>();
      List<PublishedItemData> tallyableItems = getTallyableItems(publishedAssessmentData);
      Map<Long, AnswerIfc> answersById = new HashMap<>();
      for (Object answerObject : publishedAnswerHash.values()) {
        AnswerIfc answer = (AnswerIfc) answerObject;
        if (answer != null && answer.getId() != null) {
          answersById.put(answer.getId(), answer);
        }
      }
      // For each agent (student) we will search the correct/incorrect/empty responses
      for (Object object : agents) {
        AgentResults agentResults = (AgentResults) object;
        if (agentResults.getAssessmentGradingId() != -1) {
          // Tallying needs item gradings only; skip attachment loading to avoid extra per-student DB work.
          AssessmentGradingData assessmentGradingAux = gradingService.load(agentResults.getAssessmentGradingId().toString(), false);
          List<Integer> resultsAux = new ArrayList<>(Collections.nCopies(3, 0));
          Map<Long, List<ItemGradingData>> gradingByItem = groupGradingsByItem(assessmentGradingAux);
          for (PublishedItemData item : tallyableItems) {
            Long itemId = item.getItemId();
            List<ItemGradingData> gradingList = gradingByItem.get(itemId);
            if (hasRandomDrawPart && gradingList == null) {
              continue;
            }
            SubmissionOutcome submissionOutcome = statisticsService.classifySubmission(item, gradingList, answersById);
            if (submissionOutcome == SubmissionOutcome.CORRECT) {
              resultsAux.set(0, resultsAux.get(0) + 1);
            } else if (submissionOutcome == SubmissionOutcome.INCORRECT) {
              resultsAux.set(1, resultsAux.get(1) + 1);
            } else if (submissionOutcome == SubmissionOutcome.BLANK) {
              resultsAux.set(2, resultsAux.get(2) + 1);
            }
          }
          resultsByUser.put(agentResults.getAssessmentGradingId(), resultsAux);
        }
      }
      results = resultsByUser;
    }
    return results;
  }

  private boolean isTallyableItemType(Long typeId) {
    return StatisticsService.supportsTotalScoresTally(typeId);
  }

  private List<PublishedItemData> getTallyableItems(PublishedAssessmentIfc publishedAssessmentData) {
    List<PublishedItemData> tallyableItems = new ArrayList<>();
    if (publishedAssessmentData == null) {
      return tallyableItems;
    }
    for (Object sectionObject : publishedAssessmentData.getSectionArray()) {
      SectionDataIfc sectionData = (SectionDataIfc) sectionObject;
      for (Object itemObject : sectionData.getItemArray()) {
        PublishedItemData item = (PublishedItemData) itemObject;
        if (isTallyableItemType(item.getTypeId())) {
          tallyableItems.add(item);
        }
      }
    }
    return tallyableItems;
  }

  private Map<Long, Integer> getCorrectAnswerCountByItem(Map publishedAnswerHash) {
    Map<Long, Integer> correctAnswerCountByItem = new HashMap<>();
    if (publishedAnswerHash == null) {
      return correctAnswerCountByItem;
    }
    for (Object answerObject : publishedAnswerHash.values()) {
      AnswerIfc answer = (AnswerIfc) answerObject;
      if (answer == null || answer.getItem() == null) {
        continue;
      }
      if (Boolean.TRUE.equals(answer.getIsCorrect())) {
        Long itemId = answer.getItem().getItemId();
        Integer count = correctAnswerCountByItem.get(itemId);
        correctAnswerCountByItem.put(itemId, count == null ? 1 : count + 1);
      }
    }
    return correctAnswerCountByItem;
  }

  private Map<Long, List<ItemGradingData>> groupGradingsByItem(AssessmentGradingData assessmentGradingData) {
    Map<Long, List<ItemGradingData>> gradingByItem = new HashMap<>();
    if (assessmentGradingData == null || assessmentGradingData.getItemGradingSet() == null) {
      return gradingByItem;
    }
    for (ItemGradingData gradingData : (Set<ItemGradingData>) assessmentGradingData.getItemGradingSet()) {
      if (gradingData == null) {
        continue;
      }
      Long itemId = gradingData.getPublishedItemId();
      if (itemId == null) {
        continue;
      }
      List<ItemGradingData> gradingList = gradingByItem.get(itemId);
      if (gradingList == null) {
        gradingList = new ArrayList<>();
        gradingByItem.put(itemId, gradingList);
      }
      gradingList.add(gradingData);
    }
    return gradingByItem;
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

  public String getSiteId() {
    return toolManager.getCurrentPlacement().getContext();
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
    try {

      String newmax= ContextUtil.getRoundedValue(maxScore, 2);
      return Validator.check(newmax, "N/A");
    }
    catch (Exception e) {
      // encountered some weird number format/locale
      return Validator.check(maxScore, "N/A");
    }
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
   * Is this an all submissions or, the highest, or the largest
   * Scoring option from assessment Settings page 
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
    	allSubmissions = pallSubmissions;
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
  public Map getAnsweredItems()
  {
    return answeredItems;
  }

  /**
   * This stores a map of which items actually have answers.
   * Used by QuestionScores.
   */
  public void setAnsweredItems(Map newItems)
  {
    answeredItems = newItems;
  }


  public boolean getHasRandomDrawPart() {
    return this.hasRandomDrawPart;
  }

  public void setHasRandomDrawPart(boolean param) {
    this.hasRandomDrawPart= param;
  }


  public String getSelectedSectionFilterValue() {
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
    if ( param!=null && (selectedSectionFilterValue==null ||  
    	!param.equals(this.selectedSectionFilterValue))) {
			this.selectedSectionFilterValue = param;
			setFirstRow(0); // clear the paging when we update the search
    }
  }

  public String getScoringOption()
  {
    return scoringOption;
  }

  public void setScoringOption(String param)
  {
    scoringOption= param;
  }


  public void setAvailableSections(List param) {
    availableSections= param;
  }

  public List getAvailableSections() {
    return availableSections;
  }

  public int getAvailableSectionSize() {
	  availableSections = getAllAvailableSections();
	  return availableSections.size();
  }
  
  public void setSectionFilterSelectItems(List param) {
    sectionFilterSelectItems = param;
  }

  public List getSectionFilterSelectItems() {
	  if (availableSections == null) {
		  availableSections = getAllAvailableSections();
	  }
	  
	    List filterSelectItems = new ArrayList();

		if (isReleasedToGroups()) {
			filterSelectItems.add(new SelectItem(TotalScoresBean.RELEASED_SECTIONS_GROUPS_SELECT_VALUE, ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "released_sections_groups")));
		}

	    
	    // The first choice is always "All available enrollments"
	    filterSelectItems.add(new SelectItem(TotalScoresBean.ALL_SECTIONS_SELECT_VALUE, ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "all_sections")));
	    // TODO If there are unassigned students and the current user is allowed to see them, add them next.

	    String userId = AgentFacade.getAnonymousId();
	    try {
	        Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
	        GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
	        // Add the available sections to which it belongs
	        Collection<Group> groups = site.getGroups();
	        int i = 0;
	        boolean addedMoreFilterSelect = false;
	        for (CourseSection section : availableSections) {
	            String uuid = ((CourseSection) section).getUuid();
	            for (Group group : groups) {
	                if (uuid.contains(group.getId())) {
	                    if(group.getMember(userId)!=null) {
	                        filterSelectItems.add(new SelectItem(String.valueOf(i), group.getTitle()));
	                        addedMoreFilterSelect = true;
	                    }
	                    i++;
	                    break;
	                }
	            }
	        }
	        if (!addedMoreFilterSelect && service.isUserAbleToGradeAll(site.getId(), userId)) {
	            // Add the available sections
	            for (i = 0; i < availableSections.size(); i++) {
	                CourseSection section = (CourseSection)availableSections.get(i);
	                filterSelectItems.add(new SelectItem(String.valueOf(i), section.getTitle()));
	            }
	        }
	    } catch (IdUnusedException ex) {
	        log.warn("No site found while attempting to get groups for this user, {}", ex.toString());
	    }

	    // If the selected value now falls out of legal range due to sections
	    // being deleted, throw it back to the default value (meaning everyone).
	    int selectedSectionVal = new Integer(selectedSectionFilterValue).intValue();
	    if ((selectedSectionVal >= 0) && (selectedSectionVal >= availableSections.size())) {
	    	if (isReleasedToGroups()) {
	    		setSelectedSectionFilterValue(TotalScoresBean.RELEASED_SECTIONS_GROUPS_SELECT_VALUE);
	    	}
	    	else {
	    		setSelectedSectionFilterValue(TotalScoresBean.ALL_SECTIONS_SELECT_VALUE);
	    	}
	    }
	    return filterSelectItems;

  }

  private List getAllAvailableSections() {
    GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
    return service.getAvailableSections(AgentFacade.getCurrentSiteId(), AgentFacade.getAgentString());
  }


  private List getEnrollmentListForSelectedSections(int calledFrom, String siteId) {
    List enrollments;
    
    // Check if current user has privilege to assess all groups - if so, they should see all student submissions regardless of group restrictions
    boolean hasAllGroupsPrivilege = PersistenceService.getInstance()
        .getAuthzQueriesFacade()
        .hasPrivilege(SamigoConstants.AUTHZ_ASSESSMENT_ALL_GROUPS, siteId);
    
    if (calledFrom==CALLED_FROM_HISTOGRAM_LISTENER_STUDENT){
    	enrollments = getAvailableEnrollments(true, siteId);
    }
    else if (this.getSelectedSectionFilterValue().trim().equals(this.ALL_SECTIONS_SELECT_VALUE)
    		|| (calledFrom==CALLED_FROM_TOTAL_SCORE_LISTENER 
    				&& "true".equalsIgnoreCase(anonymous)) 
	    	|| (calledFrom==CALLED_FROM_QUESTION_SCORE_LISTENER 
    	    		&& "true".equalsIgnoreCase(anonymous))
    		|| (calledFrom==CALLED_FROM_HISTOGRAM_LISTENER 
    	    		&& "true".equalsIgnoreCase(anonymous)) 
    		|| (calledFrom==CALLED_FROM_NOTIFICATION_LISTENER
	    	        && "true".equalsIgnoreCase(anonymous))
    	    || (calledFrom==CALLED_FROM_EXPORT_LISTENER
    	    	    && "true".equalsIgnoreCase(anonymous))) {
        // For instructors who have all groups privilege, get all available enrollments without group filtering
        if (hasAllGroupsPrivilege) {
            enrollments = getAvailableEnrollments(false, siteId);
        } else {
            enrollments = getAllGroupsReleaseEnrollments(siteId);
        }
    }
    else if (getSelectedSectionFilterValue().trim().equals(RELEASED_SECTIONS_GROUPS_SELECT_VALUE)) {
    	enrollments = getGroupReleaseEnrollments(siteId);
    }
    else {
        // The user has selected a particular section.
        enrollments = getSectionEnrollments(getSelectedSectionUid(this.getSelectedSectionFilterValue()), siteId);
    }
	return enrollments;
  }


  public List getSectionEnrollments(String sectionid, String siteId) {
    GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
    return service.getSectionEnrollments(siteId, sectionid , AgentFacade.getAgentString());
  }


  public List<EnrollmentRecord> getAvailableEnrollments(boolean fromStudentStatistics, String siteId) {
    GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();

    if (fromStudentStatistics) {
    	return service.getAvailableEnrollments(siteId, "-1");
    }

    return service.getAvailableEnrollments(siteId, AgentFacade.getAgentString());
  }  

  private List<EnrollmentRecord> getGroupReleaseEnrollments(String siteId) {
    GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
    return service.getGroupReleaseEnrollments(siteId, AgentFacade.getAgentString(), publishedId);
  }

  private List<EnrollmentRecord> getAllGroupsReleaseEnrollments(String siteId) {
    GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
    return service.getAllGroupsReleaseEnrollments(siteId, AgentFacade.getAgentString(), publishedId);
  }

  private String getSelectedSectionUid(String uid) {
    if (uid.equals(ALL_SECTIONS_SELECT_VALUE) 
    		|| uid.equals(RELEASED_SECTIONS_GROUPS_SELECT_VALUE) ){
      return null;
    } else {
      CourseSection section = (CourseSection)(availableSections.get(new Integer(uid).intValue()));
      return section.getUuid();
    }
  }

  public void setUserIdMap(Map userIdMap) {
	  this.userIdMap = userIdMap;
  }
  
  /**
   * @param calledFrom - where this method is called from
   * @return
   */
  public Map<String, EnrollmentRecord> getUserIdMap(int calledFrom, String siteId) {
        List enrollments = getEnrollmentListForSelectedSections(calledFrom, siteId);

// for debugging
/*
      Iterator useriter = enrollments.iterator();
      while (useriter.hasNext())
      {
         EnrollmentRecord enrollrec = (EnrollmentRecord) useriter.next();
      }
*/

        Map<String, EnrollmentRecord> enrollmentMap = new HashMap<>();

      for (Object enrollment : enrollments) {
          EnrollmentRecord enr = (EnrollmentRecord) enrollment;
          enrollmentMap.put(enr.getUser().getUserUid(), enr);
      }

        return enrollmentMap;
  }

  public boolean getReleaseToAnonymous() {
    return releaseToAnonymous;
  }

  public void setReleaseToAnonymous(boolean param) {
    releaseToAnonymous = param;
  }

  public PublishedAssessmentData getPublishedAssessment(){
    return publishedAssessment;
  }

  public void setPublishedAssessment(PublishedAssessmentData publishedAssessment){
    if (publishedAssessment!=null){
	this.publishedAssessment = publishedAssessment;
      setPublishedId(publishedAssessment.getPublishedAssessmentId().toString());
      setAssessmentName(publishedAssessment.getTitle());

      // set accessControl properties
      PublishedAccessControl ac = (PublishedAccessControl) publishedAssessment.getAssessmentAccessControl();
      setAccessControlProperties(ac);
     
      // set evaluation model properties
      PublishedEvaluationModel eval = (PublishedEvaluationModel) publishedAssessment.getEvaluationModel();
      setEvaluationModelProperties(eval);
      

    }
  }

  public void setAccessControlProperties(PublishedAccessControl ac){
    if (ac != null){
      if (ac.getDueDate()!=null) setDueDate(ac.getDueDate().toString());
      if (ac.getLateHandling()!=null) setLateHandling(ac.getLateHandling().toString());
      // set ReleaseToAnonymous
      String releaseTo = ac.getReleaseTo();
      if (releaseTo != null && releaseTo.indexOf("Anonymous Users")== -1){
        setReleaseToAnonymous(false);
      }
      else setReleaseToAnonymous(true);
      
      // set submission allowed
      
      if (ac.getSubmissionsAllowed()!=null){
      	if (ac.getSubmissionsAllowed().intValue()> 1){
      		setMultipleSubmissionsAllowed(true);
      	}
      	else {
      		setMultipleSubmissionsAllowed(false);
      	}
      }
      else {
    	  setMultipleSubmissionsAllowed(true);
      }
    }
  }

  public void setEvaluationModelProperties(PublishedEvaluationModel eval){
    if (eval != null && eval.getScoringType()!=null )
      setScoringOption(eval.getScoringType().toString());

    if (eval != null){
      String anon = eval.getAnonymousGrading().equals(
                    EvaluationModelIfc.ANONYMOUS_GRADING)?"true":"false";
      setAnonymous(anon);
    }
 
    if (eval != null && eval.getFixedTotalScore()!=null )
      setMaxScore(eval.getFixedTotalScore().toString());
    else if (publishedAssessment.getTotalScore()!=null)
      setMaxScore(publishedAssessment.getTotalScore().toString());
  }

  private Map assessmentGradingHash = new HashMap();
  public void setAssessmentGradingHash(Long publishedAssessmentId){
    GradingService service = new GradingService();
    Map<Long, AssessmentGradingData> h = service.getAssessmentGradingByItemGradingId(publishedAssessmentId.toString());
    assessmentGradingHash.put(publishedAssessmentId, h);
  }

  public Map getAssessmentGradingHash(Long publishedAssessmentId){
    return (Map)assessmentGradingHash.get(publishedAssessmentId);
  }

  private List assessmentGradingList;
  public void setAssessmentGradingList(List assessmentGradingList){
      this.assessmentGradingList = assessmentGradingList;
  }
  public List getAssessmentGradingList(){
    return assessmentGradingList;
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
  public int getDataRows() {
      return scoreDataRows;
  }
  
  public void setAllAgents(List allAgents) {
	  this.allAgents = allAgents;
  }
  /**
   * This will populate the SubmissionStatusBean with the data associated with the
   * particular versioned assessment based on the publishedId.
   *
   * @todo Some of this code will change when we move this to Hibernate persistence.
   * @param publishedId String
   * @param bean SubmissionStatusBean
   * @return boolean
   */
  public List getAllAgents()
  {
	  String publishedId = ContextUtil.lookupParam("publishedId");
	  PublishedAssessmentService pubAssessmentService = new PublishedAssessmentService();
	  PublishedAssessmentFacade pubAssessment = pubAssessmentService.getPublishedAssessment(publishedId);
	  TotalScoreListener totalScoreListener = new TotalScoreListener();
	  if (!totalScoreListener.totalScores(pubAssessment, this, false))
	  {
		  throw new RuntimeException("failed to call questionScores.");
	  }
	  return allAgents;
  }

  public List getAllAgentsDirect(){
  	  return allAgents;
  }

  public String getGraderName() {
	  return Validator.check(graderName, "");
  }
  

  public void setGraderName(String graderName) {
	  this.graderName = graderName; 
  }
  
  public String getGraderEmailInfo() {
	  return Validator.check(graderEmailInfo, "");
  }
  

  public void setGraderEmailInfo(String graderEmailInfo) {
	  this.graderEmailInfo = graderEmailInfo; 
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
      setApplyToUngraded("");
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
		// name1 example: John Doe
		StringBuilder name1;
		// name2 example: Doe, John
		StringBuilder name2;
		String normalizedPattern = normalizeSearchText(pattern);
		
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
			
			String normalizedFirstName = normalizeSearchText(result.getFirstName());
			String normalizedLastName = normalizeSearchText(result.getLastName());
			String normalizedAgentEid = normalizeSearchText(result.getAgentEid());
			String normalizedName1 = normalizeSearchText(name1.toString());
			String normalizedName2 = normalizeSearchText(name2.toString());
			
			if (normalizedFirstName.startsWith(normalizedPattern) ||
				normalizedLastName.startsWith(normalizedPattern) ||
				normalizedAgentEid.startsWith(normalizedPattern) ||
				normalizedName1.startsWith(normalizedPattern) ||
				normalizedName2.startsWith(normalizedPattern)) {
				filteredList.add(result);
			}
		}
		return filteredList;
	}

	/**
	 * @return Returns the multipleSubmissionsAllowed.
	 */
	public boolean isMultipleSubmissionsAllowed() {
		return multipleSubmissionsAllowed;
	}

	/**
	 * @param multipleSubmissionsAllowed The multipleSubmissionsAllowed to set.
	 */
	public void setMultipleSubmissionsAllowed(boolean multipleSubmissionsAllowed) {
		this.multipleSubmissionsAllowed = multipleSubmissionsAllowed;
	}
	
	public boolean getIsTimedAssessment() {
		return isTimedAssessment;
	}
	
	public void setIsTimedAssessment(boolean isTimedAssessment) {
		this.isTimedAssessment = isTimedAssessment;
	}

	public boolean getAcceptLateSubmission() {
		return acceptLateSubmission;
	}
	
	public void setAcceptLateSubmission(boolean acceptLateSubmission) {
		this.acceptLateSubmission = acceptLateSubmission;
	}

	/**
	 * Is this assessment group scoped?
	 * @return
	 */
	public boolean isReleasedToGroups() {
		PublishedAssessmentData publishedAssessment = this.getPublishedAssessment();
		//SAM-1777 if this is null we have a JSF state issue - DH
		if (publishedAssessment == null) {
			throw new IllegalStateException("Bean's published assessment is not set!");
		}
		
		return AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS.equals(publishedAssessment.getAssessmentAccessControl().getReleaseTo());
	}
	
	public boolean getIsAutoScored() {
		return isAutoScored;
	}

	public void setIsAutoScored(boolean isAutoScored) {		
		this.isAutoScored = isAutoScored;
	}	
	
	public String getApplyToUngraded() {
		return applyToUngraded;
	}

	public void setApplyToUngraded(String applyToUngraded) {
		this.applyToUngraded = applyToUngraded;
	}	
	
	public boolean getHasFileUpload() {
		return hasFileUpload;
	}

	public void setHasFileUpload(boolean hasFileUpload) {		
		this.hasFileUpload = hasFileUpload;
	}
	
	public void setAttachment(Long assessmentGradingId){
		List assessmentGradingAttachmentList = new ArrayList();
		AgentResults agentResults = (AgentResults) agentResultsByAssessmentGradingId.get(assessmentGradingId);
		if (agentResults != null) {
			AttachmentUtil attachmentUtil = new AttachmentUtil();
			Set attachmentSet = new HashSet();
			if (agentResults.getItemGradingAttachmentList() != null) {
				attachmentSet = new HashSet(agentResults.getItemGradingAttachmentList());
			}
			assessmentGradingAttachmentList = attachmentUtil.prepareAssessmentAttachment(agentResults.getAssessmentGrading(), attachmentSet);
		
        	agentResults.setAssessmentGradingAttachmentList(assessmentGradingAttachmentList);
		}
	}
	
	public Map getAgentResultsByAssessmentGradingId()
	{
		return agentResultsByAssessmentGradingId;
	}

	public void setAgentResultsByAssessmentGradingId(Map agentResultsByAssessmentGradingId)
	{
		this.agentResultsByAssessmentGradingId = agentResultsByAssessmentGradingId;
	}

	public boolean getIsAnyAssessmentGradingAttachmentListModified() {
		return isAnyAssessmentGradingAttachmentListModified;
	}

	public void setIsAnyAssessmentGradingAttachmentListModified(boolean isAnyAssessmentGradingAttachmentListModified)
	{
		this.isAnyAssessmentGradingAttachmentListModified = isAnyAssessmentGradingAttachmentListModified;
	}

	public boolean getRestrictedDelete() {
		return deleteRestrictedForCurrentSite;
	}

	public String getCDNQuery() {
		return PortalUtils.getCDNQuery();
	}

	public void exportExcel() {
		TotalScoresExportBean totalScoresExportBean = (TotalScoresExportBean) ContextUtil.lookupBean("totalScoresExportBean");
		totalScoresExportBean.exportExcel(assessmentName, allAgents);
	}
}
