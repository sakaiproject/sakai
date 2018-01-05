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

package org.sakaiproject.tool.assessment.facade;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.osid.assessment.Assessment;
import org.osid.assessment.AssessmentException;
import org.osid.shared.Type;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.osid.assessment.impl.AssessmentImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;

public class AssessmentBaseFacade
    implements java.io.Serializable, AssessmentBaseIfc{

  private AssessmentImpl assessmentImpl = new AssessmentImpl(); //<-- place holder
  protected org.osid.assessment.Assessment assessment =
      (Assessment)assessmentImpl;

  private static final long serialVersionUID = 7526471155622776147L;
  // We have 2 sets of properties:
  // #1) properties according to org.osid.assessment.Assessment.
  // Properties "description" will be persisted through data - daisyf 09/28/04
  private org.osid.shared.Id id;
  private String displayName;
  private String description;
  private AssessmentBaseIfc data;
  private org.osid.shared.Type assessmentType;
  // #2) properties according to AssessmentBaseIfc
  private Long assessmentId;
  private Boolean isTemplate;
  private String title;
  private Long typeId;
  private Long parentId;
  private Integer instructorNotification;
  private Integer testeeNotification;
  private Integer multipartAllowed;
  private Integer status;
  private String comments;
  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;
  private AssessmentAccessControlIfc assessmentAccessControl;
  private EvaluationModelIfc evaluationModel;
  private AssessmentFeedbackIfc assessmentFeedback;
  private Set assessmentMetaDataSet = new HashSet();
  private HashMap assessmentMetaDataMap = new HashMap();
  private Set securedIPAddressSet;
  private String assessmentAttachmentMetaData;
  
  /** AssessmentBaseFacade is the class that is exposed to developer
   *  It contains some of the useful methods specified in
   *  org.osid.assessment.Assessment and it implements
   *  org.sakaiproject.tool.assessment.ifc.
   *  When new methods is added to osid api, this code is still workable.
   *  If signature in any of the osid methods that we mirrored changes,
   *  we only need to modify those particular methods.
   *  - daisyf
   */

  public AssessmentBaseFacade() {
    // need to hook AssessmentFacade.data to IsessmentData, our POJO for Hibernate
    // persistence
     this.data = new AssessmentBaseData();
     try {
       assessment.updateData(this.data);
     }
     catch (AssessmentException ex) {
       throw new DataFacadeException(ex.getMessage());
     }
  }

  /**
    * This is a very important constructor. Please make sure that you have
    * set all the properties (declared above as private) of AssessmentBaseFacade
    * using the "data" supplied. "data" is a org.osid.assessment.Assessment properties
    * and I use it to store info about an assessment.
    * @param data
    */
  public AssessmentBaseFacade(AssessmentBaseIfc data) {
    try {
      setData(data);
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
  }

  /**
   * Get the Id for this AssessmentBaseFacade.
   * @return org.osid.shared.Id
   */
  org.osid.shared.Id getId(){
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return PersistenceService.getInstance().getAssessmentFacadeQueries().getId(
        this.data.getAssessmentBaseId());
  }

  /**
   * Get the Type for this AssessmentBaseFacade.
   * @return org.osid.shared.Type
   */
  Type getAssessmentType() {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    TypeFacadeQueriesAPI typeFacadeQueries = PersistenceService.getInstance().getTypeFacadeQueries();
    return typeFacadeQueries.getTypeById(this.data.getTypeId());
  }

  public TypeIfc getType() {
    return getAssessmentTypeFacade();
  }

  public TypeFacade getAssessmentTypeFacade() {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    TypeFacadeQueriesAPI typeFacadeQueries = PersistenceService.getInstance().getTypeFacadeQueries();
    return typeFacadeQueries.getTypeFacadeById(this.data.getTypeId());
  }



  /**
   * Get the data for this AssessmentBaseFacade.
   * @return AssessmentDataIfc
   */
  public AssessmentBaseIfc getData(){
    return this.data;
  }

  /**
   * Call setDate() to update data in AssessmentBaseFacade
   * @param data
   */
  public void updateData(AssessmentBaseIfc data) {
      try {
        setData(data);
      }
      catch (AssessmentException ex) {
        throw new DataFacadeException(ex.getMessage());
      }
  }

  /**
   * Set data for AssessmentBaseFacade
   * @param data
   */
  public void setData(AssessmentBaseIfc data) throws AssessmentException {
    this.data = data;
    try {
      assessment.updateData(data);
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    this.id = getId();
    this.displayName = getDisplayName();
    this.description = getDescription();
    this.typeId = getTypeId();
    this.parentId = getParentId();
    this.assessmentType = getAssessmentType();
    this.assessmentId= getAssessmentBaseId();
    this.isTemplate = getIsTemplate();
    this.title = getTitle();
    this.comments = getComments();
    this.assessmentMetaDataSet = getAssessmentMetaDataSet();
    this.instructorNotification = getInstructorNotification();
    this.testeeNotification = getTesteeNotification();
    this.multipartAllowed = getMultipartAllowed();
    this.status = getStatus();
    this.createdBy = getCreatedBy();
    this.createdDate = getCreatedDate();
    this.lastModifiedBy = getLastModifiedBy();
    this.lastModifiedDate = getLastModifiedDate();
    this.assessmentAccessControl = getAssessmentAccessControl();
    this.assessmentFeedback = getAssessmentFeedback();
    this.evaluationModel = getEvaluationModel();
    this.assessmentMetaDataMap = getAssessmentMetaDataMap(
        this.assessmentMetaDataSet);
    this.securedIPAddressSet = getSecuredIPAddressSet();
  }

  // the following methods implements
  // org.sakaiproject.tool.assessment.ifc.AssessmentBaseIfc
  public Long getAssessmentBaseId() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getAssessmentBaseId();
  }

  /**
   * Set assessmentId for AssessmentTemlateFacade
   * @param assessmentId
   */
  public void setAssessmentBaseId(Long assessmentId) {
    this.assessmentId = assessmentId;
    this.data.setAssessmentBaseId(assessmentId);
  }

  public Boolean getIsTemplate() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getIsTemplate();
  }

  /**
   * Set to true if record is a assessmentTemplate
   */
  public void setIsTemplate(Boolean isTemplate) {
    this.isTemplate = isTemplate;
    this.data.setIsTemplate(isTemplate);
  }

  public String getDisplayName() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getDescription();
  }

  /**
   * Set description for AssessmentBaseFacade
   * @param description
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
    this.data.setTitle(displayName);
  }

  public String getDescription() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getDescription();
  }

  /**
   * Set description for AssessmentBaseFacade
   * @param description
   */
  public void setDescription(String description) {
    this.description = description;
    this.data.setDescription(description);
  }

  public Long getTypeId() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getTypeId();
  }

  /**
   * Set TypeId for AssessmentBaseType. This property is used to indicate an
   * assessment type. e.g. 11 = Quiz, 12 = Homework, 13 = Mid Term.
   * Please check out ddl/02_TypeData.sql and table "type".
   * @param typeId
   */
  public void setTypeId(Long typeId) {
    this.typeId = typeId;
    this.data.setTypeId(typeId);
  }

  /**
   * Get parentId of AssessmentBaseFacade.
   * @return
   * @throws DataFacadeException
   */
  public Long getParentId() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getParentId();
  }

  /**
   * Set parentId for AssessmentBaseFacade.
   * @param parentId
   */
  public void setParentId(Long parentId) {
    this.parentId = parentId;
    this.data.setParentId(parentId);
  }

  /**
   * Get Title for AssessmentBaseFacade
   * @return
   * @throws DataFacadeException
   */
  public String getTitle() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getTitle();
  }

  /**
   * Set Title for AssessmentBaseFacade
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
    this.data.setTitle(title);
  }

  /**
   * Set Comments for AssessmentBaseFacade
   * @param comments
   */
  public void setComments(String comments) {
    this.comments = comments;
    this.data.setComments(comments);
  }

  /**
   * Get Comments for AssessmentBaseFacade
   * @return
   * @throws DataFacadeException
   */
  public String getComments() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getComments();
  }


  /**
   * Set InstructorNotification for AssessmentBaseFacade
   * @param instructorNotification
   */
  public void setInstructorNotification(Integer instructorNotification) {
    this.instructorNotification = instructorNotification;
    this.data.setInstructorNotification(instructorNotification);
  }

  /**
   * Get InstructorNotification for AssessmentBaseFacade
   * @return
   * @throws DataFacadeException
   */
  public Integer getInstructorNotification() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getInstructorNotification();
  }

  /**
   * Set TesteeNotification for AssessmentBaseFacade
   * @param testeeNotification
   */
  public void setTesteeNotification(Integer testeeNotification) {
    this.testeeNotification = testeeNotification;
    this.data.setTesteeNotification(testeeNotification);
  }

  /**
   * Get TesteeNotification for AssessmentBaseFacade
   * @return
   * @throws DataFacadeException
   */
  public Integer getTesteeNotification() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getTesteeNotification();
  }

  /**
   * Set MultipartAllowed for AssessmentBaseFacade
   * @param multipartAllowed
   */
  public void setMultipartAllowed(Integer multipartAllowed) {
    this.multipartAllowed = multipartAllowed;
    this.data.setMultipartAllowed(multipartAllowed);
  }

  /**
   * Get MultipartAllowed for AssessmentBaseFacade
   * @return
   * @throws DataFacadeException
   */
  public Integer getMultipartAllowed() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getMultipartAllowed();
  }

  /**
   * Get status of AssessmentBaseFacade. 1 = active, 0 = inactive
   * @return
   * @throws DataFacadeException
   */
  public Integer getStatus() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getStatus();
  }

  /**
   * Set status for AssessmentBaseFacade. 1 = active, 0 = inactive
   * @param status
   */
  public void setStatus(Integer status) {
    this.status = status;
    this.data.setStatus(status);
  }

  /**
   * Get createdBy for AssessmentBaseFacade. This represents the agentId of the person
   * who created the record
   * @return
   * @throws DataFacadeException
   */
  public String getCreatedBy() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getCreatedBy();
  }

  /**
   * Set createdBy for AssessmentBaseFacade. This represents the agentId of the person
   * who created the record
   * @param createdBy
   */
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
    this.data.setCreatedBy(createdBy);
  }


  /**
   * Get the creation date of AssessmentBaseFacade.
   * @return
   * @throws DataFacadeException
   */
  public Date getCreatedDate() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getCreatedDate();
  }

  /**
   * Set the creation date of AssessmentBaseFacade
   * @param createdDate
   */
  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
    this.data.setCreatedDate(createdDate);
  }

  /**
   * Get the agentId of the person who last modified AssessmentBaseFacade
   * @return
   * @throws DataFacadeException
   */
  public String getLastModifiedBy() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getLastModifiedBy();
  }

  /**
   * set the agentId of the person who last modified AssessmentBaseFacade
   * @param lastModifiedBy
   */
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
    this.data.setLastModifiedBy(lastModifiedBy);
  }

  /**
   * Get the date when AssessmentBaseFacade where last modified By
   * @return
   * @throws DataFacadeException
   */
  public Date getLastModifiedDate() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getLastModifiedDate();
  }

  /**
   * Set the last modified date
   * @param lastModifiedBy
   */
  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
    this.data.setLastModifiedDate(lastModifiedDate);
  }

  /**
   * Get the AccessControl of the person who last modified AssessmentBaseFacade
   * @return
   * @throws DataFacadeException
   */
  public AssessmentAccessControlIfc getAssessmentAccessControl() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getAssessmentAccessControl();
  }

  /**
   * set the AccessControl of the person who last modified AssessmentBaseFacade
   * @param assessmentAccessControl
   */
  public void setAssessmentAccessControl(AssessmentAccessControlIfc assessmentAccessControl) {
    this.assessmentAccessControl = assessmentAccessControl;
    this.data.setAssessmentAccessControl(assessmentAccessControl);
  }

  /**
   * Get the EvaluationModel of the person who last modified AssessmentBaseFacade
   * @return
   * @throws DataFacadeException
   */
  public EvaluationModelIfc getEvaluationModel() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getEvaluationModel();
  }

  /**
   * set the EvaluationModel of the person who last modified AssessmentBaseFacade
   * @param evaluationModel
   */
  public void setEvaluationModel(EvaluationModelIfc evaluationModel) {
    this.evaluationModel = evaluationModel;
    this.data.setEvaluationModel(evaluationModel);
  }

  public AssessmentFeedbackIfc getAssessmentFeedback() {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getAssessmentFeedback();
  }

  public void setAssessmentFeedback(AssessmentFeedbackIfc assessmentFeedback) {
    this.assessmentFeedback = assessmentFeedback;
    this.data.setAssessmentFeedback(assessmentFeedback);
  }

  public Set getSecuredIPAddressSet() {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getSecuredIPAddressSet();
  }

  public void setSecuredIPAddressSet(Set securedIPAddressSet) {
    this.securedIPAddressSet =  securedIPAddressSet;
    this.data.setSecuredIPAddressSet(securedIPAddressSet);
  }

  public Set getAssessmentMetaDataSet() throws DataFacadeException {
    try {
      this.data = (AssessmentBaseIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getAssessmentMetaDataSet();
  }

  /**
   * Set assessment metadata set in AssessmentBaseFacade and
   * AssessmentBaseFacade.data
   * @param assessmentMetaDataSet
   */
  public void setAssessmentMetaDataSet(Set assessmentMetaDataSet) {
    this.assessmentMetaDataSet = assessmentMetaDataSet;
    this.data.setAssessmentMetaDataSet(assessmentMetaDataSet);
    this.assessmentMetaDataMap = getAssessmentMetaDataMap(assessmentMetaDataSet);
  }

  /**
   * Get assessment metadata in HashMap (String Label, AssessmentMetaData assessmentMetaData) of
   * AssessmentFacade
   * @param assessmentMetaDataSet
   * @return
   */
  public HashMap getAssessmentMetaDataMap(Set assessmentMetaDataSet) {
    HashMap assessmentMetaDataMap = new HashMap();
    if (assessmentMetaDataSet !=null){
      for (Iterator i = assessmentMetaDataSet.iterator(); i.hasNext(); ) {
        AssessmentMetaData assessmentMetaData = (AssessmentMetaData) i.next();
        assessmentMetaDataMap.put(assessmentMetaData.getLabel(), assessmentMetaData.getEntry());
      }
    }
    return assessmentMetaDataMap;
  }

  public HashMap getAssessmentMetaDataMap() {
    HashMap assessmentMetaDataMap = new HashMap();
    if (this.assessmentMetaDataSet !=null){
      for (Iterator i = assessmentMetaDataSet.iterator(); i.hasNext(); ) {
        AssessmentMetaData assessmentMetaData = (AssessmentMetaData) i.next();
        assessmentMetaDataMap.put(assessmentMetaData.getLabel(), assessmentMetaData.getEntry());
      }
    }
    return assessmentMetaDataMap;
  }


  /**
   * Get meta data by label
   * @param label
   * @return
   */
  public String getAssessmentMetaDataByLabel(String label) {
	  if (this.assessmentMetaDataMap.get(label) == null) {
		  return "";
	  }
    return (String) this.assessmentMetaDataMap.get(label);
  }

  /**
   * Convenient method to check if question metadata is editable
   * @param label
   * @param entry
   */
  public String getMetaDataQuestions_isInstructorEditable() {
    return (String)this.assessmentMetaDataMap.get(METADATAQUESTIONS_ISINSTRUCTOREDITABLE);
  }

  /**
   * Convenient method to check if this is question metadata
   * @param label
   * @param entry
   */
  public String getHasMetaDataForQuestions() {
    return (String)this.assessmentMetaDataMap.get(HASMETADATAFORQUESTIONS);
  }

  public String getShowFeedbackAuthoring() {
    return (String)this.assessmentMetaDataMap.get(SHOWFEEDBACKAUTHORING);
  }

  /**
   * Add a Meta Data to AssessmentFacade
   * @param label
   * @param entry
   */
  public void addAssessmentMetaData(String label, String entry) {
    this.assessmentMetaDataMap = getAssessmentMetaDataMap();
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
      if (entry!=null && !("").equals(entry)){
        metadata = new AssessmentMetaData(this.data, label, entry);
        this.assessmentMetaDataSet.add(metadata);
      }
      setAssessmentMetaDataSet(this.assessmentMetaDataSet);
    }
  }

  public void updateAssessmentMetaData(String label, String entry) {
    addAssessmentMetaData(label,entry);
  }

  public void addAssessmentAttachmentMetaData(String entry) {
	  assessmentAttachmentMetaData = entry;
  }
  
  public String getAssessmentAttachmentMetaData() {
	  return assessmentAttachmentMetaData;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
  }
}
