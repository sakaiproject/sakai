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

package org.sakaiproject.tool.assessment.data.dao.assessment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;

public class AssessmentData extends org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData
    implements java.io.Serializable, AssessmentIfc
{

  /**
	 * 
	 */
	private static final long serialVersionUID = -2260656620640273214L;
// both Assessment and AssessmentTemplate inherits all the properties & methods from
  // AssessmentBaseData.
  // These are the properties that an assessment has and an assessmentTemplate don't
  private Long assessmentTemplateId;
  private Set sectionSet;
  private Set assessmentAttachmentSet;

  /* Assessment has AssessmentAccessControl and EvaluationModel
   * as well as a set of Sections
   * private AssessmentAccessControlIfc assessmentAccessControl;
   * private EvaluationModelIfc evaluationModel;
   */

  public AssessmentData(){
    setIsTemplate(Boolean.FALSE);
  }

  public AssessmentData(Long assessmentTemplateId, String title, Date lastModifiedDate){
    // in the case of template assessmentBaseId is the assessmentTemplateId
    super(assessmentTemplateId,title,lastModifiedDate);
  }
  
  public AssessmentData(Long assessmentTemplateId, String title, Date lastModifiedDate, String lastModifiedBy){
	    // in the case of template assessmentBaseId is the assessmentTemplateId
	    super(assessmentTemplateId,title,lastModifiedDate,lastModifiedBy);
  }
  
  public AssessmentData(Long assessmentTemplateId, String title, Date lastModifiedDate, String lastModifiedBy, Integer questionSize){
	  super(assessmentTemplateId,title,lastModifiedDate,lastModifiedBy,questionSize);
  }

  public AssessmentData(Long parentId,
                  String title, String description, String comments,
                  Long assessmentTemplateId, Long typeId,
                  Integer instructorNotification, Integer testeeNotification,
                  Integer multipartAllowed, Integer status, String createdBy,
                  Date createdDate, String lastModifiedBy,
                  Date lastModifiedDate) {
    super(Boolean.FALSE,parentId,
               title, description, comments,
               typeId,
               instructorNotification, testeeNotification,
               multipartAllowed, status, createdBy,
               createdDate, lastModifiedBy,
               lastModifiedDate);
    this.assessmentTemplateId = assessmentTemplateId;
  }

  public Long getAssessmentId(){
    return getAssessmentBaseId();
  }

  public Long getAssessmentTemplateId() {
    return this.assessmentTemplateId;
 }

  public void setAssessmentTemplateId(Long assessmentTemplateId) {
    this.assessmentTemplateId = assessmentTemplateId;
  }

  public Set getSectionSet() {
    return sectionSet;
  }

  public void setSectionSet(Set sectionSet) {
    this.sectionSet = sectionSet;
  }
  /*
  public Set getSectionSetWithAllItems() {
	  Iterator iter = sectionSet.iterator();
	  while(iter.hasNext()) {
		  SectionData sectionData = (SectionData) iter.next();
		  //if (sectionData.getSectionMetaDataByLabel("updatePoolScore") != null || sectionData.getSectionMetaDataByLabel("updatePoolScore").equals("")) {
		  String poolId = null;
		  if ((sectionData.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE)!=null) && 
			  (sectionData.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE).equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()))) {
			  poolId = sectionData.getSectionMetaDataByLabel(SectionDataIfc.POOLID_FOR_RANDOM_DRAW);
			  log.debug(poolId);
		  }
		  //}
	  }
	  
	  return sectionSet;
  }
  */
  public ArrayList getSectionArray() {
    ArrayList list = new ArrayList();
    Iterator iter = sectionSet.iterator();
    while (iter.hasNext()){
      list.add(iter.next());
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

  public Set getAssessmentAttachmentSet() {
    return assessmentAttachmentSet;
  }

  public void setAssessmentAttachmentSet(Set assessmentAttachmentSet) {
    this.assessmentAttachmentSet = assessmentAttachmentSet;
  }

  public List getAssessmentAttachmentList() {
    ArrayList list = new ArrayList();
    if (assessmentAttachmentSet !=null ){
      Iterator iter = assessmentAttachmentSet.iterator();
      while (iter.hasNext()){
        AssessmentAttachmentIfc a = (AssessmentAttachmentIfc)iter.next();
        list.add(a);
      }
    }
    return list;
  }
  // Not used. But have to implement this API because this class 
  // implement AssessmentIfc
  public String getHasMetaDataForQuestions() {
	return "false";
  }
}
