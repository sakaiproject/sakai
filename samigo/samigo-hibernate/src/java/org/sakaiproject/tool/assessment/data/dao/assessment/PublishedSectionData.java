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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;

public class PublishedSectionData extends SectionDataIfc implements java.io.Serializable, Comparable {

  private static final long serialVersionUID = 7526471155622776147L;
  public static final Integer ACTIVE_STATUS =  Integer.valueOf(1);
  public static final Integer INACTIVE_STATUS =  Integer.valueOf(0);
  public static final Integer ANY_STATUS =   Integer.valueOf(2);

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

  public void addSectionMetaData(String label, String entry) {
    if (this.sectionMetaDataSet== null) {
      setSectionMetaDataSet(new HashSet());
      this.sectionMetaDataMap= new HashMap();
    }
    this.sectionMetaDataMap.put(label, entry);
    this.sectionMetaDataSet.add(new PublishedSectionMetaData(this, label, entry));
  }

  public int compareTo(Object o) {
      PublishedSectionData a = (PublishedSectionData)o;
      return sequence.compareTo(a.sequence);
  }

}
