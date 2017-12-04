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

package org.sakaiproject.tool.assessment.data.dao.assessment;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

public class AssessmentBaseData
    implements java.io.Serializable,
               org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc
{

  private static final long serialVersionUID = 7526471155622776147L;
  public static final int TITLE_LENGTH = 255;
  private Long assessmentBaseId;
  private Boolean isTemplate;
  private Long parentId;
  private String title;
  private String description;
  private String comments;
  private Long typeId;
  private Integer instructorNotification;
  private Integer testeeNotification;
  private Integer multipartAllowed;
  private Integer status;
  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;
  private AssessmentAccessControlIfc assessmentAccessControl;
  private EvaluationModelIfc evaluationModel;
  private AssessmentFeedbackIfc assessmentFeedback;
  private Set assessmentMetaDataSet;
  private HashMap assessmentMetaDataMap = new HashMap();
  private HashMap assessmentFeedbackMap = new HashMap();
  private Set securedIPAddressSet;
  private Integer questionSize;

  public AssessmentBaseData() {}

  /**
   * This is a cheap object created for holding just the Id & title. This is
   * by AssessmentFacadeQueries.getTitleXXX() when we only need the Id & title
   * and nothing else. This object is not used for persistence.
   * @param assessmentBaseId
   * @param title
   */
  public AssessmentBaseData(Long assessmentBaseId, String title){
    this.assessmentBaseId = assessmentBaseId;
    this.title = title;
  }

  /**
   * This is another cheap object created for holding just the Id, title &
   * lastModifiedDate. This object is merely used for display. It is not used
   * for persistence.
   */
  public AssessmentBaseData(Long assessmentBaseId, String title,Date lastModifiedDate){
    this.assessmentBaseId = assessmentBaseId;
    this.title = title;
    this.lastModifiedDate = lastModifiedDate;
  }

  /**
   * This is another cheap object created for holding just the Id, title &
   * lastModifiedDate. This object is merely used for display. It is not used
   * for persistence.
   */
  public AssessmentBaseData(Long assessmentBaseId, String title,Date lastModifiedDate, String lastModifiedBy){
	    this.assessmentBaseId = assessmentBaseId;
	    this.title = title;
	    this.lastModifiedDate = lastModifiedDate;
	    this.lastModifiedBy = lastModifiedBy;
	  }
  
  public AssessmentBaseData(Long assessmentBaseId, String title,Date lastModifiedDate, String lastModifiedBy, Integer questionSize){
	    this.assessmentBaseId = assessmentBaseId;
	    this.title = title;
	    this.lastModifiedDate = lastModifiedDate;
	    this.lastModifiedBy = lastModifiedBy;
	    this.questionSize = questionSize;
  }

  public AssessmentBaseData(Long assessmentBaseId, String title,Date lastModifiedDate, Long typeId){
    this.assessmentBaseId = assessmentBaseId;
    this.title = title;
    this.lastModifiedDate = lastModifiedDate;
    this.typeId = typeId;
  }

  public AssessmentBaseData(Boolean isTemplate, Long parentId,
                  String title, String description, String comments,
                  Long typeId,
                  Integer instructorNotification, Integer testeeNotification,
                  Integer multipartAllowed, Integer status, String createdBy,
                  Date createdDate, String lastModifiedBy,
                  Date lastModifiedDate) {
    this.isTemplate = isTemplate;
    this.parentId = parentId;
    this.title = title;
    this.description = description;
    this.comments = comments;
    this.typeId = typeId;
    this.instructorNotification = instructorNotification;
    this.testeeNotification = testeeNotification;
    this.multipartAllowed = multipartAllowed;
    this.status = status;
    this.createdBy = createdBy;
    this.createdDate = createdDate;
    this.lastModifiedBy = lastModifiedBy;
    this.lastModifiedDate = lastModifiedDate;
  }

  public Long getAssessmentBaseId() {
    return this.assessmentBaseId;
  }

  public void setAssessmentBaseId(Long assessmentBaseId) {
    this.assessmentBaseId = assessmentBaseId;
  }

  public Boolean getIsTemplate() {
    return this.isTemplate;
  }

  public void setIsTemplate(Boolean isTemplate) {
    this.isTemplate = isTemplate;
  }

  public Long getParentId() {
    return this.parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getComments() {
    return this.comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public Integer getInstructorNotification() {
    return this.instructorNotification;
  }

  public void setInstructorNotification(Integer instructorNotification) {
    this.instructorNotification = instructorNotification;
  }

  public Integer getTesteeNotification() {
    return this.testeeNotification;
  }

  public void setTesteeNotification(Integer testeeNotification) {
    this.testeeNotification = testeeNotification;
  }

  public Integer getMultipartAllowed() {
    return this.multipartAllowed;
  }

  public void setMultipartAllowed(Integer multipartAllowed) {
    this.multipartAllowed = multipartAllowed;
  }

  public Long getTypeId() {
    return this.typeId;
  }

  public void setTypeId(Long typeId) {
    this.typeId = typeId;
  }

  public Integer getStatus() {
    return this.status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getCreatedDate() {
    return this.createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public String getLastModifiedBy() {
    return this.lastModifiedBy;
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public Date getLastModifiedDate() {
    return this.lastModifiedDate;
  }

  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public AssessmentAccessControlIfc getAssessmentAccessControl() {
    return this.assessmentAccessControl;
  }

  public void setAssessmentAccessControl(AssessmentAccessControlIfc assessmentAccessControl) {
    this.assessmentAccessControl = assessmentAccessControl;
  }

  public EvaluationModelIfc getEvaluationModel() {
    return this.evaluationModel;
  }

  public void setEvaluationModel(EvaluationModelIfc evaluationModel) {
    this.evaluationModel = evaluationModel;
  }

  public AssessmentFeedbackIfc getAssessmentFeedback() {
    return this.assessmentFeedback;
  }

  public void setAssessmentFeedback(AssessmentFeedbackIfc assessmentFeedback) {
    this.assessmentFeedback = assessmentFeedback;
  }

  public Set getSecuredIPAddressSet() {
    return securedIPAddressSet;
  }

  public void setSecuredIPAddressSet(Set securedIPAddressSet) {
    this.securedIPAddressSet = securedIPAddressSet;
  }

  public Set getAssessmentMetaDataSet() {
    return assessmentMetaDataSet;
  }

  public void setAssessmentMetaDataSet(Set assessmentMetaDataSet) {
    this.assessmentMetaDataSet = assessmentMetaDataSet;
    this.assessmentMetaDataMap = getAssessmentMetaDataMap(assessmentMetaDataSet);
  }

  public HashMap getAssessmentMetaDataMap(Set assessmentMetaDataSet) {
    HashMap assessmentMetaDataMap = new HashMap();
    if (assessmentMetaDataSet != null){
      for (Iterator i = assessmentMetaDataSet.iterator(); i.hasNext(); ) {
        AssessmentMetaData assessmentMetaData = (AssessmentMetaData) i.next();
        assessmentMetaDataMap.put(assessmentMetaData.getLabel(), assessmentMetaData.getEntry());
      }
    }
    return assessmentMetaDataMap;
  }

  public HashMap getAssessmentMetaDataMap() {
    HashMap assessmentMetaDataMap = new HashMap();
    if (this.assessmentMetaDataSet != null){
      for (Iterator i = this.assessmentMetaDataSet.iterator(); i.hasNext(); ) {
        AssessmentMetaData assessmentMetaData = (AssessmentMetaData) i.next();
        assessmentMetaDataMap.put(assessmentMetaData.getLabel(), assessmentMetaData.getEntry());
      }
    }
    return assessmentMetaDataMap;
  }

  public String getAssessmentMetaDataByLabel(String label) {
    return (String)this.assessmentMetaDataMap.get(label);
  }

  public void addAssessmentMetaData(String label, String entry) {
    if (this.assessmentMetaDataMap.get(label)!=null){
      // just update
      Iterator iter = this.assessmentMetaDataSet.iterator();
      while (iter.hasNext()){
        AssessmentMetaData metadata = (AssessmentMetaData) iter.next();
        if (metadata.getLabel().equals(label))
          metadata.setEntry(entry);
      }
    }
    else{ // add
      AssessmentMetaData metadata = null;
      if (!("").equals(entry.trim())){
        metadata = new AssessmentMetaData(this, label, entry);
        this.assessmentMetaDataSet.add(metadata);
      }
      setAssessmentMetaDataSet(this.assessmentMetaDataSet);
    }
  }

  public void updateAssessmentMetaData(String label, String entry) {
    addAssessmentMetaData(label, entry);
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
  }

  public TypeIfc getType() {
      /*
    TypeFacadeQueriesAPI typeFacadeQueries = PersistenceService.getInstance().getTypeFacadeQueries();
    TypeIfc type = typeFacadeQueries.getTypeFacadeById(this.typeId);
    TypeD typeD = new TypeD(type.getAuthority(), type.getDomain(),
                    type.getKeyword(), type.getDescription());
    typeD.setTypeId(this.typeId);
    return typeD;
      */
    return null;
  }
  
  public Integer getQuestionSize() {
    return this.questionSize;
  }

  public void setQuestionSize(Integer questionSize) {
    this.questionSize = questionSize;
  }
}
