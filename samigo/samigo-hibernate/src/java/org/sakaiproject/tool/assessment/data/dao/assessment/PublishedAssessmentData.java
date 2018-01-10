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

package org.sakaiproject.tool.assessment.data.dao.assessment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

public class PublishedAssessmentData
    implements java.io.Serializable,
    org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc {

  private static final long serialVersionUID = 7526471155622776147L;

  private Long assessmentId; // this is the core assessment Id
  private Long assessmentBaseId; // this is the published AssessmentId
////  private AssessmentIfc assessment;
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
  private Set securedIPAddressSet;
  private HashMap assessmentMetaDataMap = new HashMap();
  private Set sectionSet;
  // the following properties is added for the "Convenient Constructor"
  private String releaseTo;
  private Date startDate;
  private Date dueDate;
  private Date retractDate;
  private int submissionSize;
  private Integer lateHandling;
  private Boolean unlimitedSubmissions;
  private Integer submissionsAllowed;
  private Integer feedbackDelivery;
  private Integer feedbackComponentOption;
  private Integer feedbackAuthoring;
  private Date feedbackDate;
  //private String ownerSiteName;
  private Set assessmentAttachmentSet;
  private Integer scoringType;
  private Date lastNeedResubmitDate;
  private Integer timeLimit;

  
  public PublishedAssessmentData() {}
  /**
   * "Convenient Constructor"
   * This is a cheap object created for holding just the Id, title. This object is merely used for validation of assessmentTitleIsUniqueForAll. It is not used
   * for persistence.
   */
    public PublishedAssessmentData(Long id, String title, Date lastModifiedDate) {
    this.assessmentBaseId = id;
    this.title = title;
    this.lastModifiedDate=lastModifiedDate;
  }


  /**
   * "Convenient Constructor"
   * This is a cheap object created for holding just the Id, title &
   * delivery dates. This object is merely used for display. It is not used
   * for persistence.
   */
  public PublishedAssessmentData(Long id, String title, String releaseTo,
                                 Date startDate, Date dueDate, Date retractDate) {
    this.assessmentBaseId = id;
    this.title = title;
    this.releaseTo = releaseTo;
    this.startDate = startDate;
    this.dueDate = dueDate;
    this.retractDate = retractDate;
  }
  
  /**
   * "Convenient Constructor"
   * This is a cheap object created for holding just the Id, title &
   * delivery dates. This object is merely used for display. It is not used
   * for persistence.
   */
  public PublishedAssessmentData(Long id, String title, String releaseTo,
                                 Date startDate, Date dueDate, Date retractDate, Integer status,
                                 Date lastModifiedDate, String lastModifiedBy,
                                 Integer lateHandling, Boolean unlimitedSubmissions, Integer submissionsAllowed) {
    this.assessmentBaseId = id;
    this.title = title;
    this.releaseTo = releaseTo;
    this.startDate = startDate;
    this.dueDate = dueDate;
    this.retractDate = retractDate;
    this.status = status;
    this.lastModifiedDate = lastModifiedDate;
    this.lastModifiedBy = lastModifiedBy;
    this.lateHandling = lateHandling;
    this.unlimitedSubmissions = unlimitedSubmissions;
    this.submissionsAllowed = submissionsAllowed;
  }

  /**
   * "Convenient Constructor"
   * This is a cheap object created for holding just the Id, title &
   * delivery dates. This object is merely used for display. It is not used
   * for persistence.
   */
  public PublishedAssessmentData(Long id, String title, String releaseTo,
                                 Date startDate, Date dueDate, Date retractDate, Date lastModifiedDate, String lastModifiedBy) {
    this.assessmentBaseId = id;
    this.title = title;
    this.releaseTo = releaseTo;
    this.startDate = startDate;
    this.dueDate = dueDate;
    this.retractDate = retractDate;
    this.lastModifiedDate = lastModifiedDate;
    this.lastModifiedBy = lastModifiedBy;
  }
  
  public PublishedAssessmentData(Long id, String title, String releaseTo,
          Date startDate, Date dueDate, Date retractDate, Integer status) {
	  this.assessmentBaseId = id;
	  this.title = title;
	  this.releaseTo = releaseTo;
	  this.startDate = startDate;
	  this.dueDate = dueDate;
	  this.retractDate = retractDate;
	  this.status = status;
  }
  
  public PublishedAssessmentData(Long id, String title, String releaseTo,
          Date startDate, Date dueDate, Date retractDate, Integer status, Date lastModifiedDate, String lastModifiedBy) {
	  this.assessmentBaseId = id;
	  this.title = title;
	  this.releaseTo = releaseTo;
	  this.startDate = startDate;
	  this.dueDate = dueDate;
	  this.retractDate = retractDate;
	  this.status = status;
	  this.lastModifiedDate = lastModifiedDate;
	  this.lastModifiedBy = lastModifiedBy;
  }

  /**
   * Someone forgot to check this in, so I'm adding it.
   */
  public PublishedAssessmentData(Long id, String title, String releaseTo,
                                 Date startDate, Date dueDate, Date retractDate,
                                 Integer lateHandling,
                                 Boolean unlimitedSubmissions,
                                 Integer submissionsAllowed)
 {
		this.assessmentBaseId = id;
		this.title = title;
		this.releaseTo = releaseTo;
		this.startDate = startDate;
		this.dueDate = dueDate;
		this.retractDate = retractDate;
		this.lateHandling = lateHandling;
		if (unlimitedSubmissions != null)
			this.unlimitedSubmissions = unlimitedSubmissions;
		else
			this.unlimitedSubmissions = Boolean.TRUE;
		if (submissionsAllowed == null)
			this.submissionsAllowed =  Integer.valueOf(0);
		else
			this.submissionsAllowed = submissionsAllowed;
	}

  public PublishedAssessmentData(Long id, String title,
                                 PublishedAccessControl assessmentAccessControl) {
    this.assessmentBaseId = id;
    this.title = title;
    this.assessmentAccessControl = assessmentAccessControl;
  }
  
  public PublishedAssessmentData(Long id, String title, String releaseTo,
          Date startDate, Date dueDate, Date retractDate,
          Date feedbackDate, Integer feedbackDelivery, Integer feedbackComponentOption,  Integer feedbackAuthoring,
          Integer lateHandling,
          Boolean unlimitedSubmissions,
          Integer submissionsAllowed) {
	  this(id, title, releaseTo, startDate, dueDate, retractDate, feedbackDate,
			  feedbackDelivery,feedbackComponentOption,  feedbackAuthoring, lateHandling, unlimitedSubmissions, submissionsAllowed, null, null, null);
  }
  
  public PublishedAssessmentData(Long id, String title, String releaseTo,
          Date startDate, Date dueDate, Date retractDate,
          Date feedbackDate, Integer feedbackDelivery, Integer feedbackComponentOption, Integer feedbackAuthoring,
          Integer lateHandling,
          Boolean unlimitedSubmissions,
          Integer submissionsAllowed, Integer scoringType) {
	  this(id, title, releaseTo, startDate, dueDate, retractDate, feedbackDate,
			  feedbackDelivery, feedbackComponentOption, feedbackAuthoring, lateHandling, unlimitedSubmissions, submissionsAllowed, scoringType, null, null);
  }

  public PublishedAssessmentData(Long id, String title, String releaseTo,
          Date startDate, Date dueDate, Date retractDate,
          Date feedbackDate, Integer feedbackDelivery, Integer feedbackComponentOption, Integer feedbackAuthoring,
          Integer lateHandling,
          Boolean unlimitedSubmissions,
          Integer submissionsAllowed, Integer scoringType, Integer status) {
	  this(id, title, releaseTo, startDate, dueDate, retractDate, feedbackDate,
			  feedbackDelivery,feedbackComponentOption, feedbackAuthoring, lateHandling, unlimitedSubmissions, submissionsAllowed, scoringType, status, null);
  }
  
  public PublishedAssessmentData(Long id, String title, String releaseTo,
          Date startDate, Date dueDate, Date retractDate,
          Date feedbackDate, Integer feedbackDelivery,  Integer feedbackComponentOption,Integer feedbackAuthoring,
          Integer lateHandling,
          Boolean unlimitedSubmissions,
          Integer submissionsAllowed, Integer scoringType, Integer status, Date lastModifiedDate) {
	  this(id, title, releaseTo, startDate, dueDate, retractDate, feedbackDate,
			  feedbackDelivery,feedbackComponentOption, feedbackAuthoring, lateHandling, unlimitedSubmissions, submissionsAllowed, scoringType, status, lastModifiedDate, null);
  }
  public PublishedAssessmentData(Long id, String title, String releaseTo,
                                 Date startDate, Date dueDate, Date retractDate,
                                 Date feedbackDate, Integer feedbackDelivery,  Integer feedbackComponentOption,Integer feedbackAuthoring,
                                 Integer lateHandling,
                                 Boolean unlimitedSubmissions,
                                 Integer submissionsAllowed, Integer scoringType, Integer status, Date lastModifiedDate, Integer timeLimit) {
    this.assessmentBaseId = id;
    this.title = title;
    this.releaseTo = releaseTo;
    this.startDate = startDate;
    this.dueDate = dueDate;
    this.retractDate = retractDate;
    this.feedbackDelivery = feedbackDelivery; //=publishedFeedback.feedbackDelivery
    this.feedbackComponentOption = feedbackComponentOption;
    this.feedbackAuthoring = feedbackAuthoring; //=publishedFeedback.feedbackAuthoring
    this.feedbackDate = feedbackDate;
    this.lateHandling = lateHandling;
    if (unlimitedSubmissions != null)
      this.unlimitedSubmissions = unlimitedSubmissions;
    else
      this.unlimitedSubmissions = Boolean.TRUE;
    if (submissionsAllowed == null)
      this.submissionsAllowed =  Integer.valueOf(0);
    else
      this.submissionsAllowed = submissionsAllowed;
    this.scoringType = scoringType;
    this.status = status;
    this.lastModifiedDate = lastModifiedDate;
    this.timeLimit = timeLimit;
  }

  public PublishedAssessmentData(Long id, int submissionSize) {
    this.assessmentBaseId = id;
    this.submissionSize = submissionSize;
  }

  public PublishedAssessmentData(
      String title, String description, String comments,
      Long typeId,
      Integer instructorNotification, Integer testeeNotification,
      Integer multipartAllowed, Integer status, String createdBy,
      Date createdDate, String lastModifiedBy,
      Date lastModifiedDate) {
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

  public Long getAssessmentId() {
    return this.assessmentId;
  }

  public void setAssessmentId(Long assessmentId) {
    this.assessmentId = assessmentId;
  }

  public Long getPublishedAssessmentId() {
    return this.assessmentBaseId;
  }

  public void setPublishedAssessmentId(Long assessmentBaseId) {
    this.assessmentBaseId = assessmentBaseId;
  }

/*  public AssessmentIfc getAssessment() {
    return assessment;
  }

  public void setAssessment(AssessmentIfc assessment) {
    this.assessment = assessment;
  }
*/
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

  public void setAssessmentAccessControl(AssessmentAccessControlIfc
                                         assessmentAccessControl) {
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

  public Set getAssessmentMetaDataSet() {
    return assessmentMetaDataSet;
  }

  public void setAssessmentMetaDataSet(Set assessmentMetaDataSet) {
    this.assessmentMetaDataSet = assessmentMetaDataSet;
    this.assessmentMetaDataMap = getAssessmentMetaDataMap(assessmentMetaDataSet);
  }

  public Set getSecuredIPAddressSet() {
    return securedIPAddressSet;
  }

  public void setSecuredIPAddressSet(Set securedIPAddressSet) {
    this.securedIPAddressSet = securedIPAddressSet;
  }

  public HashMap getAssessmentMetaDataMap(Set assessmentMetaDataSet) {
    HashMap assessmentMetaDataMap = new HashMap();
    if (assessmentMetaDataSet != null) {
      for (Iterator i = assessmentMetaDataSet.iterator(); i.hasNext(); ) {
        PublishedMetaData assessmentMetaData = (PublishedMetaData) i.next();
        assessmentMetaDataMap.put(assessmentMetaData.getLabel(),
                                  assessmentMetaData.getEntry());
      }
    }
    return assessmentMetaDataMap;
  }

  public HashMap getAssessmentMetaDataMap() {
    HashMap assessmentMetaDataMap = new HashMap();
    if (this.assessmentMetaDataSet != null) {
      for (Iterator i = this.assessmentMetaDataSet.iterator(); i.hasNext(); ) {
        PublishedMetaData assessmentMetaData = (PublishedMetaData) i.next();
        assessmentMetaDataMap.put(assessmentMetaData.getLabel(),
                                  assessmentMetaData.getEntry());
      }
    }
    return assessmentMetaDataMap;
  }

  public String getAssessmentMetaDataByLabel(String label) {
    return (String)this.assessmentMetaDataMap.get(label);
  }

  public void addAssessmentMetaData(String label, String entry) {
    if (this.assessmentMetaDataMap.get(label) != null) {
      // just update
      Iterator iter = this.assessmentMetaDataSet.iterator();
      while (iter.hasNext()) {
        AssessmentMetaData metadata = (AssessmentMetaData) iter.next();
        if (metadata.getLabel().equals(label)) {
          metadata.setEntry(entry);
        }
      }
    }
    else { // add
      AssessmentMetaData metadata = null;
      if (! ("").equals(entry.trim())) {
        metadata = new AssessmentMetaData(this, label, entry);
        this.assessmentMetaDataSet.add(metadata);
      }
      setAssessmentMetaDataSet(this.assessmentMetaDataSet);
    }
  }

  public void updateAssessmentMetaData(String label, String entry) {
    addAssessmentMetaData(label, entry);
  }

  public Set getSectionSet() {
    return sectionSet;
  }

  public void setSectionSet(Set sectionSet) {
    this.sectionSet = sectionSet;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
  }

  public Long getAssessmentBaseId() {
    return getAssessmentId();
  }

  public void setAssessmentBaseId(Long assessmentBaseId) {
    setAssessmentId(assessmentBaseId);
  }

  public Boolean getIsTemplate() {
    return Boolean.FALSE;
  }

  public void setIsTemplate(Boolean isTemplate) {
  }

  // axed it, published assessment don't have parent
  public Long getParentId() {
    return null;
  }

  public void setParentId(Long parentId) {
  }

  public Long getAssessmentTemplateId() {
    return null;
  }

  public void setAssessmentTemplateId(Long assessmentTemplateId) {
  }

  public TypeIfc getType() {
      /*
    TypeFacadeQueriesAPI typeFacadeQueries = PersistenceService.getInstance().
        getTypeFacadeQueries();
    TypeIfc type = typeFacadeQueries.getTypeFacadeById(this.typeId);
    TypeD typeD = new TypeD(type.getAuthority(), type.getDomain(),
                            type.getKeyword(), type.getDescription());
    typeD.setTypeId(this.typeId);
    return typeD;
      */
    return null;
  }

  public String getReleaseTo() {
    return this.releaseTo;
  }

  public Date getStartDate() {
    return this.startDate;
  }

  public Date getDueDate() {
    return this.dueDate;
  }

  public Date getRetractDate() {
    return this.retractDate;
  }

  public int getSubmissionSize() {
    return this.submissionSize;
  }

  public ArrayList getSectionArray() {
    ArrayList list = new ArrayList();
    Iterator iter = sectionSet.iterator();
    while (iter.hasNext()) {
      list.add(iter.next());
    }
    return list;
  }

  public ArrayList getSectionArraySorted() {
    ArrayList list = getSectionArray();
    Collections.sort(list);
    return list;
  }

  public SectionDataIfc getSection(Long sequence) {
    ArrayList list = getSectionArraySorted();
    if (list == null) {
      return null;
    }
    else {
      return (SectionDataIfc) list.get(sequence.intValue() - 1);
    }

  }

  public SectionDataIfc getDefaultSection() {
    ArrayList list = getSectionArraySorted();
    if (list == null) {
      return null;
    }
    else {
      return (SectionDataIfc) list.get(0);
    }
  }

  public Integer getLateHandling() {
    return lateHandling;
  }

  public Boolean getUnlimitedSubmissions() {
    return this.unlimitedSubmissions;
  }

  public Integer getSubmissionsAllowed() {
    return submissionsAllowed;
  }
  
  public Integer getScoringType() {
	    return scoringType;
  }

  public Integer getFeedbackDelivery()
  {
    return feedbackDelivery;
  }
  
  public Integer getFeedbackComponentOption()
  {
    return feedbackComponentOption;
  }

  public Integer getFeedbackAuthoring()
  {
    return feedbackAuthoring;
  }

  public Date getFeedbackDate() {
    return this.feedbackDate;
  }

  public Double getTotalScore(){
    double total = 0;
    Iterator iter = this.sectionSet.iterator();
    while (iter.hasNext()){
      PublishedSectionData s = (PublishedSectionData) iter.next();
      ArrayList list = s.getItemArray();
      Iterator iter2 = null;
      if ((s.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE)!=null) && (s.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE).equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString())))
{
        ArrayList randomsample = new ArrayList();
        Integer numberToBeDrawn= Integer.valueOf(0);
        if (s.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN) !=null ) {
          numberToBeDrawn= new Integer(s.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN));
        }

        int samplesize = numberToBeDrawn.intValue();
        for (int i=0; i<samplesize; i++){
          randomsample.add(list.get(i));
        }
        iter2 = randomsample.iterator();
      }
      else {
        iter2 = list.iterator();
      }

      while (iter2.hasNext()){
        PublishedItemData item = (PublishedItemData)iter2.next();
        total= total + item.getScore().doubleValue();
      }
    }
    return  Double.valueOf(total);
  }

  public Set getAssessmentAttachmentSet() {
    return assessmentAttachmentSet;
  }

  public void setAssessmentAttachmentSet(Set assessmentAttachmentSet) {
    this.assessmentAttachmentSet = assessmentAttachmentSet;
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
  
  // Not used. But have to implement this API because this class 
  // implement AssessmentIfc
  public String getHasMetaDataForQuestions() {
		return "false";
  }
  
  public Date getLastNeedResubmitDate() {
	  return this.lastNeedResubmitDate;
  }

  public void setLastNeedResubmitDate(Date lastNeedResubmitDate) {
	  this.lastNeedResubmitDate = lastNeedResubmitDate;
  }

  public Integer getTimeLimit() {
	  return this.timeLimit;
  }

  public void setTimeLimit(Integer timeLimit) {
	  this.timeLimit = timeLimit;
  }

}
