/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.bean.author;
import java.io.Serializable;
import java.util.ArrayList;

import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;

/**
 * General authoring information.
 * @author Ed Smiley
 *
 * @version $Id$
 */
public class AuthorBean implements Serializable
{
  private static Log log = LogFactory.getLog(AuthorBean.class);

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 4216587136245498157L;
  private String assessTitle;
  private String assessmentTemplateId; // added by daisyf - 11/1/04
  private String assessmentTypeId;
  private String assessmentDescription;
  private String assessmentID;
  private AssessmentFacade assessment;
  private ArrayList assessmentTemplateList;
  private ArrayList assessments;
  private ArrayList publishedAssessments;
  private ArrayList inactivePublishedAssessments;
  private SelectItem[] assessmentTemplates;
  private boolean showCompleteAssessment;
  private String totalPoints;
  private String currentSection;
  private ArrayList sections;
  private String currentQuestionType;
  private TemplateBean settings;
  private String totalQuestions;
  private String currentItem;
  private String coreAssessmentOrderBy = "title";
  private boolean coreAscending = true;
  private String publishedAssessmentOrderBy ="title";
  private boolean publishedAscending = true;
  private String inactivePublishedAssessmentOrderBy = "title";
  private boolean inactivePublishedAscending = true;
  private String outcome;
  private boolean showTemplateList;
  private boolean isEditPendingAssessmentFlow = true;
  private String fromPage;
  private boolean isRetractedForEdit = false;
  private boolean editPubAssessmentRestricted;
  private boolean isRepublishAndRegrade = false;
  // This parameter is used to indicate whether we should display the 
  // warning text next to assignment title retracted by edit in instructor/admin view.
  // It is true if at least one of the assessment is currently "retract for edit".
  private boolean isAnyAssessmentRetractForEdit = false;    

  /**
   * @return the id
   */
  public String getAssessmentID()
  {
    return assessmentID;
  }

  public AssessmentFacade getAssessment()
  {
    return assessment;
  }

  public void setAssessment(AssessmentFacade assessment)
  {
    this.assessment = assessment;
  }

  public String getAssessmentTypeId()
  {
    return assessmentTypeId;
  }

  public String getAssessmentDescription()
  {
    return assessmentDescription;
  }

  /**
   * @return the title
   */
  public String getAssessTitle()
  {
    return assessTitle;
  }

  public void setAssessmentTemplateId(String assessmentTemplateId)
  {
    this.assessmentTemplateId = assessmentTemplateId;
  }

  public String getAssessmentTemplateId()
  {
    return assessmentTemplateId;
  }

  public void setAssessmentTemplateList(ArrayList list){
    this.assessmentTemplateList = new ArrayList();
    try{
      for (int i=0; i<list.size();i++){
        AssessmentTemplateFacade f = (AssessmentTemplateFacade) list.get(i);
        // sorry, cannot do f.getAssessmentTemplateId() 'cos such call requires
        // "data" which we do not have in this case. The template list parsed
        // to this method contains merely assesmentBaseId (in this case is the templateId)
        //  & title (see constructor AssessmentTemplateFacade(id, title))
        this.assessmentTemplateList.add(new SelectItem(
            f.getAssessmentBaseId().toString(), f.getTitle()));
      }
    }
    catch(Exception e){
      log.warn(e.getMessage());
    }
  }

  public ArrayList getAssessmentTemplateList(){
    return assessmentTemplateList;
  }

/*
  public SelectItem[] getAssessmentTemplates(){
    return assessmentTemplates;
  }
*/
  public void setAssessments(ArrayList assessments){
    this.assessments = assessments;
  }

  public ArrayList getAssessments(){
    return assessments;
  }

  public void setPublishedAssessments(ArrayList publishedAssessments){
    this.publishedAssessments = publishedAssessments;
  }

  public ArrayList getPublishedAssessments(){
    return publishedAssessments;
  }

  public void setInactivePublishedAssessments(ArrayList inactivePublishedAssessments){
    this.inactivePublishedAssessments = inactivePublishedAssessments;
  }

  public ArrayList getInactivePublishedAssessments(){
    return inactivePublishedAssessments;
  }

  /**
   * do we show the complete asseassement?
   * @return boolean
   */
  public boolean isShowCompleteAssessment()
  {
    return showCompleteAssessment;
  }

  /**
   * the total points
   * @return the total points
   */
  public String getTotalPoints()
  {
    return totalPoints;
  }

  /**
   * If UI is on a specific question, get the type
   * @return question type
   */
  public String getCurrentQuestionType()
  {
    return currentQuestionType;
  }

  /**
   * If UI is on a specific section, get the section id
   * @return
   */
  public String getCurrentSection()
  {
    return currentSection;
  }

  /**
   * ArrayList of SectionBeans
   * @return
   */
  public ArrayList getSections()
  {
    return sections;
  }

  /**
   * derived property
   * @return true if there are questions
   */
  public boolean isHasQuestions()
  {
    try
    {
      for (int i = 0; i < sections.size(); i++)
      {
        SectionBean s = (SectionBean) sections.get(i);
        if (s.getItems().size() > 0)
        {
          return true;
        }

      }
    }
    catch (RuntimeException ex)
    {
      return false;
    }
    return false;
  }

  /**
   * @param string the id
   */
  public void setAssessmentId(String string)
  {
    assessmentID = string;
  }

  /**
   * @param string the title
   */
  public void setAssessTitle(String string)
  {
    assessTitle = string;
  }

  /**
   * do we show the complete assessment
   * @param showCompleteAssessment boolean
   */
  public void setShowCompleteAssessment(boolean showCompleteAssessment)
  {
    this.showCompleteAssessment = showCompleteAssessment;
  }

  /**
   * total points
   * @param totalPoints total points for assessment
   */
  public void setTotalPoints(String totalPoints)
  {
    this.totalPoints = totalPoints;
  }

  /**
   * If UI is on a specific question, set the type
   * @param currentQuestionType
   */
  public void setCurrentQuestionType(String currentQuestionType)
  {
    this.currentQuestionType = currentQuestionType;
  }

  /**
   * set the UI to a specific section id
   * @param currentSection
   */
  public void setCurrentSection(String currentSection)
  {
    this.currentSection = currentSection;
  }

  /**
   * set a list of SectionBeans
   * @param sections
   */
  public void setSections(ArrayList sections)
  {
    this.sections = sections;
  }

  /**
   * this allows us to store template information
   * @return
   */
  public TemplateBean getSettings()
  {
    return settings;
  }

  /**
   * this allows us to store template information
   * @param settings
   */
  public void setSettings(TemplateBean settings)
  {
    this.settings = settings;
  }
  public String getTotalQuestions()
  {
    return totalQuestions;
  }
  public void setTotalQuestions(String totalQuestions)
  {
    this.totalQuestions = totalQuestions;
  }

  /**
   * Derived property.
   * @return ArrayList of model SelectItems
   */

  public ArrayList getSectionSelectList()
  {
    ArrayList list = new ArrayList();

    if (sections == null) return list;

    for (int i = 0; i < sections.size(); i++) {
      SelectItem selection = new SelectItem();
      SectionBean sBean = (SectionBean) sections.get(i);
      selection.setLabel(sBean.getSectionTitle());
      selection.setValue(sBean.getSectionIdent());
      list.add(selection);
    }

    return list;
  }
  /**
   * If the UI is on a specific item
   * @return the item id
   */
  public String getCurrentItem()
  {
    return currentItem;
  }
  /**
   * set UI  on a specific item
   * @param currentItem the item id
   */
    public void setCurrentItem(String currentItem)
  {
    this.currentItem = currentItem;
  }

  public String editAssessmentSettings(){
    //startEditAssessmentSettings();
    return "editAccessmentSettings";
  }

  /*
  public void startEditAssessmentSettings(){
    String assessmentId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("assessmentId");

  }
  */

  public String getCoreAssessmentOrderBy() {
    return this.coreAssessmentOrderBy;
  }

  public void setCoreAssessmentOrderBy(String coreAssessmentOrderBy) {
    this.coreAssessmentOrderBy = coreAssessmentOrderBy;
  }

 /**
   * is core assessment table sorted in ascending order
   * @return true if it is
   */
  public boolean isCoreAscending()
  {
    return coreAscending;
  }

  /**
   *
   * @param coreAscending is core assessment table sorted in ascending order
   */
  public void setCoreAscending(boolean coreAscending)
  {
    this.coreAscending = coreAscending;
  }

  public String getPublishedAssessmentOrderBy() {
    return this.publishedAssessmentOrderBy;
  }

  public void setPublishedAssessmentOrderBy(String publishedAssessmentOrderBy) {
    this.publishedAssessmentOrderBy = publishedAssessmentOrderBy;
  }

 /**
   * is published assessment table sorted in ascending order
   * @return true if it is
   */
  public boolean isPublishedAscending()
  {
    return publishedAscending;
  }

  /**
   *
   * @param publishedAscending is published assessment table sorted in ascending order
   */
  public void setPublishedAscending(boolean publishedAscending)
  {
    this.publishedAscending = publishedAscending;
  }

  public String getInactivePublishedAssessmentOrderBy() {
    return this.inactivePublishedAssessmentOrderBy;
  }

  public void setInactivePublishedAssessmentOrderBy(String inactivePublishedAssessmentOrderBy) {
    this.inactivePublishedAssessmentOrderBy = inactivePublishedAssessmentOrderBy;
  }

  /**
   * is inactive published assessment table sorted in ascending order
   * @return true if it is
   */
  public boolean isInactivePublishedAscending()
  {
    return inactivePublishedAscending;
  }

  /**
   *
   * @param inactivePublishedAscending is inactive published assessment table sorted in ascending order
   */
  public void setInactivePublishedAscending(boolean inactivePublishedAscending)
  {
    this.inactivePublishedAscending = inactivePublishedAscending;
  }

  public void setAssessmentTypeId(String typeId){
    this.assessmentTypeId = typeId;
  }

  public void setAssessmentDescription(String description){
    this.assessmentDescription = description;
  }

  public String getOutcome()
  {
    return outcome;
  }

  public void setOutcome(String outcome)
  {
    this.outcome = outcome;
  }

  public boolean getShowTemplateList()
  {
    return showTemplateList;
  }

  public void setShowTemplateList(boolean showTemplateList)
  {
    this.showTemplateList = showTemplateList;
  }

  public boolean getIsEditPendingAssessmentFlow()
  {
    return isEditPendingAssessmentFlow;
  }

  public void setIsEditPendingAssessmentFlow(boolean isEditPendingAssessmentFlow)
  {
    this.isEditPendingAssessmentFlow = isEditPendingAssessmentFlow;
  }
  
  public String getFromPage()
  {
    return fromPage;
  }

  public void setFromPage(String fromPage)
  {
    this.fromPage = fromPage;
  }
  
  public boolean getIsRetractedForEdit()
  {
    return isRetractedForEdit;
  }

  public void setIsRetractedForEdit(boolean isRetractedForEdit)
  {
    this.isRetractedForEdit = isRetractedForEdit;
  }

  public boolean getEditPubAssessmentRestricted()
  {
	  return editPubAssessmentRestricted;
  }

  public void setEditPubAssessmentRestricted(boolean editPubAssessmentRestricted)
  {
	  this.editPubAssessmentRestricted = editPubAssessmentRestricted;
  }
  
  public boolean getIsRepublishAndRegrade()
  {
	  return isRepublishAndRegrade;
  }

  public void setIsRepublishAndRegrade(boolean isRepublishAndRegrade)
  {
	  this.isRepublishAndRegrade = isRepublishAndRegrade;
  }
  
  public boolean getIsAnyAssessmentRetractForEdit(){
	  return isAnyAssessmentRetractForEdit;
  }

  public void setIsAnyAssessmentRetractForEdit(boolean isAnyAssessmentRetractForEdit){
	  this.isAnyAssessmentRetractForEdit = isAnyAssessmentRetractForEdit;
  }
}
