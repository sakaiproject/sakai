/**
 * Copyright (c) 2004-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.assessment.data.ifc.assessment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

import lombok.Setter;
import lombok.Getter;

public abstract class SectionDataIfc implements java.io.Serializable {

  public static final String AUTHOR_TYPE= "AUTHOR_TYPE";  // author questions one at a time, or random draw from qpool.
  public static final Integer QUESTIONS_AUTHORED_ONE_BY_ONE= 1;
  public static final Integer RANDOM_DRAW_FROM_QUESTIONPOOL= 2;

  public static final String QUESTIONS_ORDERING = "QUESTIONS_ORDERING"; // question ordering within a part
  public static final Integer AS_LISTED_ON_ASSESSMENT_PAGE= 1;
  public static final Integer RANDOM_WITHIN_PART= 2;
  public static final String POOLID_FOR_RANDOM_DRAW = "POOLID_FOR_RANDOM_DRAW";
  public static final String POOLNAME_FOR_RANDOM_DRAW = "POOLNAME_FOR_RANDOM_DRAW";
  public static final String NUM_QUESTIONS_DRAWN = "NUM_QUESTIONS_DRAWN";
  public static final String QUESTIONS_RANDOM_DRAW_DATE = "QUESTIONS_RANDOM_DRAW_DATE";
  
  public static final String RANDOMIZATION_TYPE = "RANDOMIZATION_TYPE";
  public static final String PER_SUBMISSION = "1";
  public static final String PER_STUDENT = "2";
  
  public static final String POINT_VALUE_FOR_QUESTION = "POINT_VALUE_FOR_QUESTION";
  public static final String DISCOUNT_VALUE_FOR_QUESTION = "DISCOUNT_VALUE_FOR_QUESTION";

  @Setter @Getter protected Long id;
  @Setter @Getter protected Long assessmentId;
  @Setter @Getter protected AssessmentIfc assessment;
  @Setter @Getter protected Integer duration;
  @Setter @Getter protected Integer sequence;
  @Setter @Getter protected String title;
  @Setter @Getter protected String description;
  @Setter @Getter protected Long typeId;
  @Setter @Getter protected Integer status;
  @Setter @Getter protected String createdBy;
  @Setter @Getter protected Date createdDate;
  @Setter @Getter protected String lastModifiedBy;
  @Setter @Getter protected Date lastModifiedDate;
  @Setter @Getter protected Set itemSet;
  @Setter @Getter protected HashMap sectionMetaDataMap;
  @Setter @Getter protected Set sectionAttachmentSet;
  @Getter protected Set sectionMetaDataSet;

  public void setSectionMetaDataSet(Set param) {
    this.sectionMetaDataSet= param;
    this.sectionMetaDataMap = getSectionMetaDataMap(sectionMetaDataSet);
  }

  public HashMap getSectionMetaDataMap(Set metaDataSet) {
    HashMap metaDataMap = new HashMap();
    if (metaDataSet != null){
      for (Iterator i = metaDataSet.iterator(); i.hasNext(); ) {
        SectionMetaDataIfc metaData = (SectionMetaDataIfc) i.next();
        metaDataMap.put(metaData.getLabel(), metaData.getEntry());
      }
    }
    return metaDataMap;
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
  
  public Long getSectionId() {
    return this.id;
  }

  public void setSectionId(Long id) {
    this.id = id;
  }

  public ArrayList getItemArraySortedForGrading() {
    ArrayList list = getItemArray();
    Collections.sort(list);
    return list;
  }

  public List getSectionAttachmentList() {
    ArrayList list = new ArrayList();
    if (sectionAttachmentSet !=null ){
      Iterator iter = sectionAttachmentSet.iterator();
      while (iter.hasNext()){
        SectionAttachmentIfc a = (SectionAttachmentIfc)iter.next();
        list.add(a);
      }
    }
    return list;
  }

  public void addItem(ItemDataIfc item) {
    if (itemSet == null)
      itemSet = new HashSet();
    itemSet.add(item);
  }

}
