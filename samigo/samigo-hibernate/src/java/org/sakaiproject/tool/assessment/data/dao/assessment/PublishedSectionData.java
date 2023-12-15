/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

public class PublishedSectionData
    implements java.io.Serializable, SectionDataIfc, Comparable{

  private static final long serialVersionUID = 7526471155622776147L;
  public static final Integer ACTIVE_STATUS =  Integer.valueOf(1);
  public static final Integer INACTIVE_STATUS =  Integer.valueOf(0);
  public static final Integer ANY_STATUS =   Integer.valueOf(2);

  private Long id;
  private Long assessmentId;
  private AssessmentIfc assessment;
  private Integer duration;
  private Integer sequence;
  private String title;
  private String description;
  private Long typeId;
  private Integer status;
  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;
  private Set itemSet;
  private Set sectionMetaDataSet;
  private HashMap sectionMetaDataMap;
  private Set sectionAttachmentSet;

  public PublishedSectionData() {}

  public PublishedSectionData(Integer duration, Integer sequence,
                     String title, String description,
                     Long typeId, Integer status,
                     String createdBy, Date createdDate,
                     String lastModifiedBy, Date lastModifiedDate){
    this.duration = duration;
    this.sequence = sequence;
    this.title = title;
    this.description = description;
    this.typeId = typeId;
    this.status = status;
    this.createdBy = createdBy;
    this.createdDate = createdDate;
    this.lastModifiedBy = lastModifiedBy;
    this.lastModifiedDate = lastModifiedDate;

  }

  public Long getSectionId() {
    return this.id;
  }

  public void setSectionId(Long id) {
    this.id = id;
  }

  public Long getAssessmentId() {
    return this.assessmentId;
  }

  public void setAssessmentId(Long assessmentId) {
    this.assessmentId = assessmentId;
  }

  public void setAssessment(AssessmentIfc assessment)
  {
    this.assessment = assessment;
  }

  public AssessmentIfc getAssessment()
  {
      return assessment;
  }

/**
  public AssessmentDataIfc getAssessment()
  {
      return (AssessmentDataIfc)assessment;
  }
*/
  public Integer getDuration() {
    return this.duration;
  }

  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  public Integer getSequence() {
    return this.sequence;
  }

  public void setSequence(Integer sequence) {
    this.sequence = sequence;
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

  public Set getItemSet() {
    return itemSet;
  }

  public void setItemSet(Set itemSet) {
    this.itemSet = itemSet;
  }

  public Set getSectionMetaDataSet() {
    return sectionMetaDataSet;
  }

  public void setSectionMetaDataSet(Set param) {
    this.sectionMetaDataSet= param;
    this.sectionMetaDataMap = getSectionMetaDataMap(sectionMetaDataSet);

  }

  public HashMap getSectionMetaDataMap(Set metaDataSet) {
    HashMap metaDataMap = new HashMap();
    if (metaDataSet != null){
      for (Iterator i = metaDataSet.iterator(); i.hasNext(); ) {
        PublishedSectionMetaData metaData = (PublishedSectionMetaData) i.next();
        metaDataMap.put(metaData.getLabel(), metaData.getEntry());
      }
    }
    return metaDataMap;
  }


  public void addSectionMetaData(String label, String entry) {
    if (this.sectionMetaDataSet== null) {
      setSectionMetaDataSet(new HashSet());
      this.sectionMetaDataMap= new HashMap();
    }
    this.sectionMetaDataMap.put(label, entry);
    this.sectionMetaDataSet.add(new PublishedSectionMetaData(this, label, entry));
  }

  public ArrayList getItemArray() {
    ArrayList list = new ArrayList();
    if(itemSet == null) itemSet = new HashSet();
    Iterator iter = itemSet.iterator();
    while (iter.hasNext()){
      list.add(iter.next());
    }
    return list;
  }

  public String getSectionMetaDataByLabel(String label) {
    return (String)this.sectionMetaDataMap.get(label);
  }

  public ArrayList getItemArraySortedForGrading() {
  // this returns all items, used for grading
    ArrayList list = getItemArray();
    Collections.sort(list);
    return list;
  }

  public void addItem(ItemDataIfc item) {
    if (itemSet == null)
      itemSet = new HashSet();
    itemSet.add(item);
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

  public int compareTo(Object o) {
      PublishedSectionData a = (PublishedSectionData)o;
      return sequence.compareTo(a.sequence);
  }

  public Set getSectionAttachmentSet() {
    return sectionAttachmentSet;
  }

  public void setSectionAttachmentSet(Set sectionAttachmentSet) {
    this.sectionAttachmentSet = sectionAttachmentSet;
  }

  public List getSectionAttachmentList() {
    ArrayList list = new ArrayList();
    if (sectionAttachmentSet != null){
      Iterator iter = sectionAttachmentSet.iterator();
      while (iter.hasNext()){
        SectionAttachmentIfc a = (SectionAttachmentIfc)iter.next();
        list.add(a);
      }
    }
    return list;
  }

}
