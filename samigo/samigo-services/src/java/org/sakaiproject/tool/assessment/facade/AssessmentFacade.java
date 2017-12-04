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


package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.osid.assessment.AssessmentException;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

@Slf4j
public class AssessmentFacade extends AssessmentBaseFacade
    implements java.io.Serializable, AssessmentIfc
{
  private static final long serialVersionUID = 7526471155622776147L;
  private AssessmentIfc data;
  private Long assessmentTemplateId;
  private Long assessmentId;
  private Set sectionSet;
  private Set assessmentAttachmentSet;
  private Integer questionSize;
  private String lastModifiedDateForDisplay;
  
  public AssessmentFacade() {
    //super();
    this.data = new AssessmentData();
    try {
      // assessment(org.osid.assessment.Assessment) is a protected properties
      // in AssessmentBaseFacade
      assessment.updateData(this.data);
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
  }

  /**
   * IMPORTANT: this constructor do not have "data", this constructor is
   * merely used for holding assessmentBaseId (which is the assessmentId), Title
   * & lastModifiedDate for displaying purpose.
   * This constructor does not persist data (which it has none) to DB
   * @param id
   * @param title
   * @param lastModifiedDate
   */
  public AssessmentFacade(Long id, String title, Date lastModifiedDate) {
    // in the case of template assessmentBaseId is the assessmentTemplateId
    super.setAssessmentBaseId(id);
    super.setTitle(title);
    super.setLastModifiedDate(lastModifiedDate);
  }
  
  /**
   * IMPORTANT: this constructor do not have "data", this constructor is
   * merely used for holding assessmentBaseId (which is the assessmentId), Title
   * & lastModifiedDate for displaying purpose.
   * This constructor does not persist data (which it has none) to DB
   * @param id
   * @param title
   * @param lastModifiedDate
   */
  public AssessmentFacade(Long id, String title, Date lastModifiedDate, String lastModifiedBy) {
    // in the case of template assessmentBaseId is the assessmentTemplateId
    super.setAssessmentBaseId(id);
    super.setTitle(title);
    super.setLastModifiedDate(lastModifiedDate);
    super.setLastModifiedBy(lastModifiedBy);
  }

  public AssessmentFacade(Long id, String title, Date lastModifiedDate, String lastModifiedBy, int questionSize) {
	    // in the case of template assessmentBaseId is the assessmentTemplateId
	    super.setAssessmentBaseId(id);
	    super.setTitle(title);
	    super.setLastModifiedDate(lastModifiedDate);
	    super.setLastModifiedBy(lastModifiedBy);
	    this.questionSize = questionSize;
  }
  
  public AssessmentFacade(AssessmentIfc data, Boolean loadSection) {
    try {
      // assessment(org.osid.assessment.Assessment) is a protected properties
      // in AssessmentBaseFacade
      //assessment.updateData(this.data);
      super.setData(data);
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    // super class does not have assessmentTemplateId nor sectionSet and assessmentAttachmentSet,
    // so we need to set it here
    this.assessmentTemplateId = data.getAssessmentTemplateId();
    this.assessmentAttachmentSet = data.getAssessmentAttachmentSet();
    // sectionSet is a set of SectionFacade
    this.sectionSet = new HashSet();

    // check if we need to load section
    if (loadSection.equals(Boolean.TRUE)){
      Set dataSet = data.getSectionSet();
      Iterator iter = dataSet.iterator();
      while (iter.hasNext()) {
        SectionData s = (SectionData) iter.next();
        this.sectionSet.add(new SectionFacade(s));
      }
    }
  }

  public AssessmentFacade(AssessmentIfc data) {
    try {
      // assessment(org.osid.assessment.Assessment) is a protected properties
      // in AssessmentBaseFacade
      //assessment.updateData(this.data);
      super.setData(data);
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    // super class does not have assessmentTemplateId nor sectionSet and AssessmentAttachemntSet,
    // so we need to set it here
    this.assessmentTemplateId = data.getAssessmentTemplateId();
    this.assessmentAttachmentSet = data.getAssessmentAttachmentSet();
    // sectionSet is a set of SectionFacade
    this.sectionSet = new HashSet();
    Set dataSet = data.getSectionSet();
    Iterator iter = dataSet.iterator();
    while (iter.hasNext()){
      SectionData s = (SectionData)iter.next();
      this.sectionSet.add(new SectionFacade(s));
    }
  }

  public Long getAssessmentId(){
    try {
      this.data = (AssessmentIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getAssessmentId();
  }

  public Long getAssessmentTemplateId() {
    try {
      this.data = (AssessmentIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      log.error(ex.getMessage(), ex);
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getAssessmentTemplateId();
 }

  public void setAssessmentTemplateId(Long assessmentTemplateId) {
    this.assessmentTemplateId = assessmentTemplateId;
    this.data.setAssessmentTemplateId(assessmentTemplateId);
  }

  public Set getSectionSet() {
    try {
      this.data = (AssessmentIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    this.sectionSet = new HashSet();
    Set dataSet = this.data.getSectionSet();
    Iterator iter = dataSet.iterator();
    while (iter.hasNext()){
      SectionData s = (SectionData)iter.next();
      this.sectionSet.add(new SectionFacade(s));
    }
    return this.sectionSet;
  }

  // sectionSet must be a set of SectionFacade
  public void setSectionSet(Set sectionSet) {
    this.sectionSet = sectionSet;
    HashSet set = new HashSet();
    Iterator iter = sectionSet.iterator();
    while (iter.hasNext()){
      SectionFacade sf = (SectionFacade)iter.next();
      set.add(sf.getData());
    }
    this.data.setSectionSet(set);
  }

  public ArrayList getSectionArray() {
    ArrayList list = new ArrayList();
    if (this.sectionSet != null){
      Iterator iter = this.sectionSet.iterator();
      while (iter.hasNext()) {
        SectionFacade s = (SectionFacade)iter.next();
        list.add(s);
      }
    }
    return list;
  }

  public ArrayList getSectionArraySorted() {
    ArrayList list = getSectionArray();
    Collections.sort(list);
    return list;
  }

  public SectionDataIfc getSection(Long sequence){
    ArrayList list = getSectionArraySorted();
    if (list == null)
      return null;
    else
      return (SectionDataIfc) list.get(sequence.intValue()-1);
  }

  public SectionDataIfc getDefaultSection(){
    ArrayList list = getSectionArraySorted();
    if (list == null)
      return null;
    else
      return (SectionDataIfc) list.get(0);
  }

  public Set getAssessmentAttachmentSet() throws DataFacadeException {
    try {
      this.data = (AssessmentIfc) assessment.getData();
      this.assessmentAttachmentSet = data.getAssessmentAttachmentSet();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return assessmentAttachmentSet;
  }

  public void setAssessmentAttachmentSet(Set assessmentAttachmentSet) {
    this.assessmentAttachmentSet = assessmentAttachmentSet;
    this.data.setAssessmentAttachmentSet(assessmentAttachmentSet);
  }

  public List getAssessmentAttachmentList() {
    ArrayList list = new ArrayList();
    if (assessmentAttachmentSet != null){
      Iterator iter = assessmentAttachmentSet.iterator();
      while (iter.hasNext()){
        AssessmentAttachmentIfc a = (AssessmentAttachmentIfc)iter.next();
        list.add(a);
      }
    }
    return list;
  }

  public Integer getQuestionSize() {
	  return questionSize;
  }

  public void setQuestionSize(Integer questionSize) {
	  this.questionSize = questionSize;
  }
  
  public String getLastModifiedDateForDisplay() {
	  return lastModifiedDateForDisplay;
  }

  public void setLastModifiedDateForDisplay(String lastModifiedDateForDisplay) {
	  this.lastModifiedDateForDisplay = lastModifiedDateForDisplay;
  }
}
