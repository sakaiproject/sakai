/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedSectionFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.shared.TypeService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * @author rshastri
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 *
 * Used to be org.navigoproject.ui.web.asi.author.assessment.AssessmentActionForm.java
 */
 @Slf4j
 public class AssessmentBean  implements Serializable {

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -630950053380808339L;
  private AssessmentIfc assessment;
  private String assessmentId;
  private String title;
  // ArrayList of SectionContentsBean
  private List<SectionContentsBean> sections = new ArrayList<SectionContentsBean>(); // this contains list of SectionFacde
  private List<SelectItem> sectionList = new ArrayList<SelectItem>(); // this contains list of javax.faces.model.SelectItem
  private List otherSectionList = new ArrayList(); // contains SectionItem of section except the current section
  private List partNumbers = new ArrayList();
  private int questionSize=0;
  private double totalScore=0;
  private String newQuestionTypeId;
  private String firstSectionId;
  private boolean hasRandomDrawPart;
  private boolean showPrintLink;
  private boolean hasGradingData = false;
  private boolean hasSubmission = false;
  private Boolean showPrintAssessment = null;

  /*
   * Creates a new AssessmentBean object.
   */
  public AssessmentBean() {
  }

  public AssessmentIfc getAssessment() {
    return assessment;
  }

  public void setAssessment(AssessmentIfc assessment) {
    try {
      this.assessment = assessment;
      if (assessment instanceof AssessmentFacade) {
    	  this.assessmentId = assessment.getAssessmentId().toString();
      }
      else if (assessment instanceof PublishedAssessmentFacade) {
    	  this.assessmentId = ((PublishedAssessmentFacade) assessment).getPublishedAssessmentId().toString();
      }
      this.title = assessment.getTitle();

      // work out the question side & total point
      this.sections = new ArrayList<SectionContentsBean>();
      List<? extends SectionDataIfc> sectionArray = assessment.getSectionArraySorted();
      for (int i=0; i<sectionArray.size(); i++){
        SectionDataIfc section = sectionArray.get(i);
        SectionContentsBean sectionBean = new SectionContentsBean(section);
        this.sections.add(sectionBean);
      }
      setPartNumbers();
      setQuestionSizeAndTotalScore();
      setSectionList(sectionArray);
    }
    catch (Exception ex) {
	log.error(ex.getMessage(), ex);
    }
  }

  // properties from Assessment
  public String getAssessmentId() {
    return this.assessmentId;
  }

  public void setAssessmentId(String assessmentId) {
    this.assessmentId = assessmentId;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<SectionContentsBean> getSections() {
    return sections;
  }

  public void setSections(List sections) {
    this.sections = sections;
  }

  public List getPartNumbers() {
    return partNumbers;
  }

  public void setPartNumbers() {
    this.partNumbers = new ArrayList();
    for (int i=1; i<=this.sections.size(); i++){
      this.partNumbers.add(new SelectItem(i+""));
    }
  }

  public int getQuestionSize() {
    return this.questionSize;
  }

  public void setQuestionSizeAndTotalScore() {
   this.questionSize = 0;
   this.totalScore = 0;
   int randomPartCount = 0;

   AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
   
   for(int i=0;i<this.sections.size();i++){
      SectionContentsBean sectionBean = (SectionContentsBean) sections.get(i);
      List items = sectionBean.getItemContents();

      int itemsInThisSection =0;
      if (sectionBean.getSectionAuthorType().equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL)) {
        // for random draw parts, add
   	randomPartCount++ ;
        itemsInThisSection = sectionBean.getNumberToBeDrawn().intValue();
      }
      else {
	itemsInThisSection = items.size();
      }

      this.questionSize += itemsInThisSection;
      for (int j=0; j<itemsInThisSection; j++){
          ItemContentsBean item = (ItemContentsBean)items.get(j);
          if (item.getItemData().getScore()!=null){
            this.totalScore += item.getItemData().getScore().doubleValue();
          }
      }
    }
    if (randomPartCount >0) {
	setHasRandomDrawPart(true);
    }
    else {
	setHasRandomDrawPart(false);
    }    
  }

  public int updateRandomPoolQuestions(String sectionId){
	  for(int i=0;i<this.sections.size();i++){
		  SectionContentsBean sectionBean = (SectionContentsBean) sections.get(i);
		  if(sectionBean.getSectionId().equals(sectionId)){
			  AssessmentService assessmentService = new AssessmentService();
			  int success = assessmentService.updateRandomPoolQuestions(assessmentService.getSection(sectionId));
			  if(success == AssessmentService.UPDATE_SUCCESS){
				  //need to update section since it has changed
				  sections.set(i, new SectionContentsBean(assessmentService.getSection(sectionBean.getSectionId())));
			  }else{
				  return success;
			  }
		  }
	  }
	  return AssessmentService.UPDATE_SUCCESS;
  }

  public double getTotalScore() {
    return this.totalScore;
  }
  
  public void setTotalScore(double totalScore) {
	  this.totalScore = totalScore;
  }

  public String getNewQuestionTypeId() {
    return this.newQuestionTypeId;
  }

  public void setNewQuestionTypeId(String newQuestionTypeId) {
    this.newQuestionTypeId = newQuestionTypeId;
  }


  public SelectItem[] getItemTypes(){
    // return list of TypeD
    TypeService service = new TypeService();
    List list = service.getFacadeItemTypes();
    SelectItem[] itemTypes = new SelectItem[list.size()];
    for (int i=0; i<list.size();i++){
      TypeIfc t = (TypeIfc) list.get(i);
      itemTypes[i] = new SelectItem(
          t.getTypeId().toString(), t.getKeyword());
    }
    return itemTypes;
  }

  /**
   * This set a list of SelectItem (sectionId, title) for selection box
   * @param list
   */
  public void setSectionList(List<? extends SectionDataIfc> list){
    //this.assessmentTemplateIter = new AssessmentTemplateIteratorFacade(list);
    this.sectionList = new ArrayList<SelectItem>();
    try{
      for (int i=0; i<list.size();i++){
        SectionDataIfc f = list.get(i);
        // sorry, cannot do f.getAssessmentTemplateId() 'cos such call requires
        // "data" which we do not have in this case. The template list parsed
        // to this method contains merely assesmentBaseId (in this case is the templateId)
        //  & title (see constructor AssessmentTemplateFacade(id, title))
        this.sectionList.add(new SelectItem(
            f.getSectionId().toString(), f.getTitle()));
        if (i==0){
          this.firstSectionId = f.getSectionId().toString();
        }
      }
    }
    catch(Exception e){
      log.warn(e.getMessage());
    }
  }

  public List<SelectItem> getSectionList(){
    return sectionList;
  }

  public String getFirstSectionId()
  {
    return firstSectionId;
  }

  /**
   * @param string the title
   */
  public void setFirstSectionId(String firstSectionId)
  {
    this.firstSectionId = firstSectionId;
  }

  public List getOtherSectionList(){
      return otherSectionList;
  }

  public void setOtherSectionList(List list){
      this.otherSectionList = list; // list contains javax.faces.model.SelectItem
  }

  public boolean getHasRandomDrawPart() {
    return this.hasRandomDrawPart;
  }

  public void setHasRandomDrawPart(boolean param) {
    this.hasRandomDrawPart= param;
  }

  public boolean getShowPrintLink() {
	return this.showPrintLink;
  }
  
  public void setShowPrintLink(boolean showPrintLink) {
	this.showPrintLink= showPrintLink;
  }
  
  public boolean getHasGradingData() {
		return this.hasGradingData;
  }

  public void setHasGradingData(boolean hasGradingData) {
		this.hasGradingData = hasGradingData;
  }
  
  public boolean getHasSubmission() {
		return this.hasSubmission;
	}

  public void setHasSubmission(boolean hasSubmission) {
		this.hasSubmission = hasSubmission;
  }
  
  public boolean getShowPrintAssessment() {
    //Should this override the setter?
    if (showPrintAssessment == null) {
      String printAssessment = ServerConfigurationService.getString("samigo.printAssessment", "true");
      return Boolean.parseBoolean(printAssessment);
    }
    return showPrintAssessment;
  }

  public void setShowPrintAssessment(Boolean showPrintAssessment) {
	  this.showPrintAssessment= showPrintAssessment;
  }
  
  public boolean getExportable2MarkupText() {
	  AssessmentService assessmentService = new AssessmentService();
	  boolean result = false;
	  if (assessmentId != null) {
		  AssessmentFacade assessment = assessmentService.getAssessment(assessmentId);
		  result =  assessmentService.isExportable(assessment);
	  }
	  return result;
  }
}
