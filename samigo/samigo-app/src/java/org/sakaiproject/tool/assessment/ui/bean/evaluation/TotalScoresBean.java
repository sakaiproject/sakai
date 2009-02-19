/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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



package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.jsf.model.PhaseAware;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.tool.assessment.business.entity.RecordingData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingSectionAwareServiceAPI;
import org.sakaiproject.tool.assessment.shared.impl.grading.GradingSectionAwareServiceImpl;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Description: class form for evaluating total scores</p>
 *
 */
public class TotalScoresBean
  implements Serializable, PhaseAware
{
  private String assessmentId;
  private String publishedId;

  public static final String RELEASED_SECTIONS_GROUPS_SELECT_VALUE = "-2"; // added by gopalrc - Jan 2008
  
  public static final String ALL_SECTIONS_SELECT_VALUE = "-1";
  public static final String ALL_SUBMISSIONS = "3";
  public static final String LAST_SUBMISSION = "2";
  public static final String HIGHEST_SUBMISSION = "1";

  // gopalrc - indicates which listeber getUserIdMap() is called from
  public static final int CALLED_FROM_SUBMISSION_STATUS_LISTENER = 1;  
  public static final int CALLED_FROM_QUESTION_SCORE_LISTENER = 2;  
  public static final int CALLED_FROM_TOTAL_SCORE_LISTENER = 3;  
  public static final int CALLED_FROM_HISTOGRAM_LISTENER = 4;
  public static final int CALLED_FROM_HISTOGRAM_LISTENER_STUDENT = 5;
    
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
  private HashMap answeredItems;
  private boolean hasRandomDrawPart;
  private String scoringOption;
  
  
  // modified by gopalrc - Jan 2008
  //private String selectedSectionFilterValue = TotalScoresBean.ALL_SECTIONS_SELECT_VALUE;
  private String selectedSectionFilterValue = null;

  private List sectionFilterSelectItems;
  private List availableSections;
  private boolean releaseToAnonymous = false;
  private PublishedAssessmentData publishedAssessment; 
  private ArrayList allAgents;
  
  private String graderName;
  private String graderEmailInfo;
  
  // Paging.
  private int firstScoreRow;
  private int maxDisplayedScoreRows;
  private int scoreDataRows;
  
  // Searching
  private String searchString;
  private String defaultSearchString;
  
  private boolean multipleSubmissionsAllowed = false;
  private boolean isTimedAssessment = false;
  private boolean acceptLateSubmission = false;

  private Boolean releasedToGroups = null; // added by gopalrc - Jan 2008
  private Map userIdMap;
  private String releaseTo;
  
  private static Log log = LogFactory.getLog(TotalScoresBean.class);

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

		if (searchString == null) {
			searchString = defaultSearchString;
		}
		
		// Get allAgents only at the first time
		if (allAgents == null) {
			allAgents = getAllAgents();
		}
		
		// For anonymous grading, we want to take out the records that has not been submitted
		if ("true".equalsIgnoreCase(anonymous)) {
			Iterator iter = allAgents.iterator();
			ArrayList anonymousAgents = new ArrayList();
			while (iter.hasNext()) {
				AgentResults agentResult = (AgentResults) iter.next();
				if (agentResult.getSubmittedDate() != null && agentResult.getAssessmentGradingId().intValue() != -1) {
					anonymousAgents.add(agentResult);
				}
			}
			allAgents = anonymousAgents;
		}
		
		ArrayList matchingAgents;
		if (isFilteredSearch()) {
			matchingAgents = findMatchingAgents(searchString);
		}
		else {
			matchingAgents = allAgents;
		}
		scoreDataRows = matchingAgents.size();
		ArrayList newAgents;
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

    //added by gopalrc - Jan 2007
	if (isReleasedToGroups()) {
		setSelectedSectionFilterValue(TotalScoresBean.RELEASED_SECTIONS_GROUPS_SELECT_VALUE);
	}
	else {
		setSelectedSectionFilterValue(TotalScoresBean.ALL_SECTIONS_SELECT_VALUE);
	}
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


  public boolean getHasRandomDrawPart() {
    return this.hasRandomDrawPart;
  }

  public void setHasRandomDrawPart(boolean param) {
    this.hasRandomDrawPart= param;
  }


  public String getSelectedSectionFilterValue() {
	  // lazy initialization added by gopalrc - Jan 2008  
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
	// gopalrc added null check  
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

  public void setSectionFilterSelectItems(List param) {
    sectionFilterSelectItems = param;
  }

  public List getSectionFilterSelectItems() {
    availableSections = getAllAvailableSections();
    List filterSelectItems = new ArrayList();

    // added by gopalrc - Jan 2008
	if (isReleasedToGroups()) {
		filterSelectItems.add(new SelectItem(TotalScoresBean.RELEASED_SECTIONS_GROUPS_SELECT_VALUE, ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "released_sections_groups")));
	}

    
    // The first choice is always "All available enrollments"
    filterSelectItems.add(new SelectItem(TotalScoresBean.ALL_SECTIONS_SELECT_VALUE, ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "all_sections")));
    // TODO If there are unassigned students and the current user is allowed to see them, add them next.

    // Add the available sections.
    for (int i = 0; i < availableSections.size(); i++) {
        CourseSection section = (CourseSection)availableSections.get(i);
        filterSelectItems.add(new SelectItem(String.valueOf(i), section.getTitle()));
        //filterSelectItems.add(new SelectItem(section.getUuid(), section.getTitle()));
    }

    // If the selected value now falls out of legal range due to sections
    // being deleted, throw it back to the default value (meaning everyone).
    int selectedSectionVal = new Integer(selectedSectionFilterValue).intValue();
    if ((selectedSectionVal >= 0) && (selectedSectionVal >= availableSections.size())) {
    	// condition added by gopalrc - Jan 2008
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


  private List getEnrollmentListForSelectedSections(int calledFrom) {
    List enrollments;
/*
    if (this.getSelectedSectionFilterValue().trim().equals(this.ALL_SECTIONS_SELECT_VALUE)
    		|| (getSelectedSectionFilterValue().trim().equals(RELEASED_SECTIONS_GROUPS_SELECT_VALUE) 
    				&& calledFrom==CALLED_FROM_TOTAL_SCORE_LISTENER 
    				&& "true".equalsIgnoreCase(anonymous)) 
    				
	    	|| (getSelectedSectionFilterValue().trim().equals(RELEASED_SECTIONS_GROUPS_SELECT_VALUE) 
    	    		&& calledFrom==CALLED_FROM_QUESTION_SCORE_LISTENER 
    	    		&& "true".equalsIgnoreCase(anonymous)) 
    ) {
*/  
    if (calledFrom==CALLED_FROM_HISTOGRAM_LISTENER_STUDENT){
    	enrollments = getAvailableEnrollments(true);
    }
    else if (this.getSelectedSectionFilterValue().trim().equals(this.ALL_SECTIONS_SELECT_VALUE)
    		|| (calledFrom==CALLED_FROM_TOTAL_SCORE_LISTENER 
    				&& "true".equalsIgnoreCase(anonymous)) 
	    	|| (calledFrom==CALLED_FROM_QUESTION_SCORE_LISTENER 
    	    		&& "true".equalsIgnoreCase(anonymous))
    		|| (calledFrom==CALLED_FROM_HISTOGRAM_LISTENER 
    	    		&& "true".equalsIgnoreCase(anonymous))) {
        enrollments = getAvailableEnrollments(false);
    }
    // added by gopalrc - Jan 2008
    else if (getSelectedSectionFilterValue().trim().equals(RELEASED_SECTIONS_GROUPS_SELECT_VALUE)) {
    	enrollments = getGroupReleaseEnrollments();
    }
    else {
        // The user has selected a particular section.
        enrollments = getSectionEnrollments(getSelectedSectionUid(this.getSelectedSectionFilterValue()));
    }
	return enrollments;
  }

  private List getSectionEnrollments(String sectionid) {
    GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
    return service.getSectionEnrollments(AgentFacade.getCurrentSiteId(), sectionid , AgentFacade.getAgentString());
  }


  private List getAvailableEnrollments(boolean fromStudentStatistics) {
    GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
    List list = null;
    if (fromStudentStatistics) {
    	list = service.getAvailableEnrollments(AgentFacade.getCurrentSiteId(), "-1");
    }
    else {
    	list = service.getAvailableEnrollments(AgentFacade.getCurrentSiteId(), AgentFacade.getAgentString());
    }
    return list; 
  }  

  private List getGroupReleaseEnrollments() {
    GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
    return service.getGroupReleaseEnrollments(AgentFacade.getCurrentSiteId(), AgentFacade.getAgentString(), publishedId);
  }
  

  private String getSelectedSectionUid(String uid) {
    if (uid.equals(ALL_SECTIONS_SELECT_VALUE) 
    		|| uid.equals(RELEASED_SECTIONS_GROUPS_SELECT_VALUE) ){ // gopalrc - Jan 2008
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
   * calledFrom param added by gopalrc 
   * @param calledFrom - where this method is called from
   * @return
   */
  public Map getUserIdMap(int calledFrom) {
        List enrollments = getEnrollmentListForSelectedSections(calledFrom);

// for debugging
/*
      Iterator useriter = enrollments.iterator();
      while (useriter.hasNext())
      {
         EnrollmentRecord enrollrec = (EnrollmentRecord) useriter.next();
      }
*/

        Map enrollmentMap = new HashMap();

        for (Iterator iter = enrollments.iterator(); iter.hasNext(); ) {
                EnrollmentRecord enr = (EnrollmentRecord)iter.next();
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

  private HashMap assessmentGradingHash = new HashMap();
  public void setAssessmentGradingHash(Long publishedAssessmentId){
    GradingService service = new GradingService();
    HashMap h = service.getAssessmentGradingByItemGradingId(publishedAssessmentId.toString());
    assessmentGradingHash.put(publishedAssessmentId, h);
  }

  public HashMap getAssessmentGradingHash(Long publishedAssessmentId){
    return (HashMap)assessmentGradingHash.get(publishedAssessmentId);
  }

  private ArrayList assessmentGradingList;
  public void setAssessmentGradingList(ArrayList assessmentGradingList){
      this.assessmentGradingList = assessmentGradingList;
  }
  public ArrayList getAssessmentGradingList(){
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
  
  public void setAllAgents(ArrayList allAgents) {
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
  public ArrayList getAllAgents()
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
  }
  
	private boolean isFilteredSearch() {
        return !StringUtils.equals(searchString, defaultSearchString);
	}
	
	public ArrayList findMatchingAgents(final String pattern) {
		ArrayList filteredList = new ArrayList();
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
	
	public String getReleaseTo() {
		return releaseTo;
	}
	
	public void setReleaseTo(String releaseTo) {
		this.releaseTo = releaseTo;
	}
		
	/**
	 * added by gopalrc - jan 2008
	 * @return
	 */
	public boolean isReleasedToGroups() {
		/*
    	PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    	releasedToGroups = publishedAssessmentService.isReleasedToGroups(publishedId);
		return releasedToGroups;
		*/
		boolean isReleasedToGroups = false;
		if (getPublishedAssessment() != null && getPublishedAssessment().getAssessmentAccessControl() != null) {
			isReleasedToGroups = AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS.equals(getPublishedAssessment().getAssessmentAccessControl().getReleaseTo());
		}
		else {
			// From AuthorActionListener, the PublishedAssessment is not set (null)
			isReleasedToGroups = AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS.equals(getReleaseTo());
		}
		return isReleasedToGroups;
	}
	
}
