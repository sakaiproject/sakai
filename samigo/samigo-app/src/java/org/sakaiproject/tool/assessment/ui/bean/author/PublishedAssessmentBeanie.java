/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.shared.TypeService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: Backing bean for Published Assessment</p>
 *
 *
 */
@Slf4j
public class PublishedAssessmentBeanie
    implements Serializable {

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -630950053380808339L;
  private PublishedAssessmentFacade assessment;
  private String assessmentId;
  private String title;
  // ArrayList of SectionContentsBean
  private List sections = new ArrayList();
  private List partNumbers = new ArrayList();
  private int questionSize=0;
  private double totalScore=0;
  private String newQuestionTypeId;

  /*
   * Creates a new AssessmentBean object.
   */
  public PublishedAssessmentBeanie() {
  }

  public PublishedAssessmentFacade getAssessment() {
    return assessment;
  }

  public void setAssessment(PublishedAssessmentFacade assessment) {
    try {
      this.assessment = assessment;
      this.assessmentId = assessment.getAssessmentId().toString();
      this.title = assessment.getTitle();

      // work out the question side & total point
      this.sections = new ArrayList();
      List sectionArray = assessment.getSectionArraySorted();
      for (int i=0; i<sectionArray.size(); i++){
        SectionDataIfc section = (SectionDataIfc)sectionArray.get(i);
        SectionContentsBean sectionBean = new SectionContentsBean(section);
        this.sections.add(sectionBean);
      }
      setPartNumbers();
      setQuestionSizeAndTotalScore();
    }
    catch (Exception ex) {
      log.warn(ex.getMessage());
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

  public List getSections() {
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
   for(int i=0;i<this.sections.size();i++){
      SectionContentsBean sectionBean = (SectionContentsBean) sections.get(i);
      List items = sectionBean.getItemContents();
      this.questionSize += items.size();
      for (int j=0; j<items.size();j++){
        ItemContentsBean item = (ItemContentsBean)items.get(j);
        if (item.getItemData().getScore()!=null){
          this.totalScore += item.getItemData().getScore().doubleValue();
        }
      }
    }
  }

  public double getTotalScore() {
    return this.totalScore;
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

}
