/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.text.NumberFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.model.SelectItem;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.business.entity.RecordingData;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.facade.AgentFacade;

import org.sakaiproject.tool.assessment.shared.impl.grading.GradingSectionAwareServiceImpl;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingSectionAwareServiceAPI;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.services.GradingService;

/**
 * <p>Description: class form for evaluating total scores</p>
 *
 * Used to be org.navigoproject.ui.web.form.evaluation.TotalScoresForm
 *
 * @author Rachel Gollub
 */
public class TotalScoresBean
  implements Serializable
{
  private String assessmentId;
  private String publishedId;

  public static final String ALL_SECTIONS_SELECT_VALUE = "-1";
  public static final String ALL_SUBMISSIONS = "3";

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
  private String allSubmissions = ALL_SUBMISSIONS;
  private RecordingData recordingData;
  private String totalPeople;
  private String firstItem;
  private HashMap answeredItems;
  private boolean hasRandomDrawPart;
  private String scoringOption;

  private String selectedSectionFilterValue = ALL_SECTIONS_SELECT_VALUE;
  private List sectionFilterSelectItems;
  private List availableSections;
  private boolean releaseToAnonymous = false;
  private PublishedAssessmentData publishedAssessment; 

  private static Log log = LogFactory.getLog(TotalScoresBean.class);

  /**
   * Creates a new TotalScoresBean object.
   */
  public TotalScoresBean()
  {
    log.debug("Creating a new TotalScoresBean");
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


  public boolean getHasRandomDrawPart() {
    return this.hasRandomDrawPart;
  }

  public void setHasRandomDrawPart(boolean param) {
    this.hasRandomDrawPart= param;
  }


  public String getSelectedSectionFilterValue() {
    return selectedSectionFilterValue;
  }

  public void setSelectedSectionFilterValue(String param ) {
      this.selectedSectionFilterValue = param;
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
        setSelectedSectionFilterValue(TotalScoresBean.ALL_SECTIONS_SELECT_VALUE);
    }
    return filterSelectItems;
  }

  private List getAllAvailableSections() {
    GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
    return service.getAvailableSections(AgentFacade.getCurrentSiteId(), AgentFacade.getAgentString());
  }


  private List getEnrollmentListForSelectedSections() {
                List enrollments;

                if (this.getSelectedSectionFilterValue().trim().equals(this.ALL_SECTIONS_SELECT_VALUE)) {
                        enrollments = getAvailableEnrollments();
                } else {
                        // The user has selected a particular section.
                        enrollments = getSectionEnrollments(getSelectedSectionUid(this.getSelectedSectionFilterValue()));
                }
        return enrollments;
        }


  private List getSectionEnrollments(String sectionid) {
    GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
    return service.getSectionEnrollments(AgentFacade.getCurrentSiteId(), sectionid , AgentFacade.getAgentString());
}


  private List getAvailableEnrollments() {
    GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
    return service.getAvailableEnrollments(AgentFacade.getCurrentSiteId(), AgentFacade.getAgentString());
}

  private String getSelectedSectionUid(String uid) {
    if (uid.equals(this.ALL_SECTIONS_SELECT_VALUE)){
      return null;
    } else {
      CourseSection section = (CourseSection)(availableSections.get(new Integer(uid).intValue()));
      return section.getUuid();
    }
  }


  public Map getUserIdMap() {
        List enrollments = getEnrollmentListForSelectedSections();
// for debugging
      Iterator useriter = enrollments.iterator();
      while (useriter.hasNext())
      {
         EnrollmentRecord enrollrec = (EnrollmentRecord) useriter.next();
      }
// end for debugging

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

}
