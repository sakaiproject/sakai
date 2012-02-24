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



package org.sakaiproject.tool.assessment.data.ifc.assessment;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.List;

import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

public interface SectionDataIfc
    extends java.io.Serializable{

  public static String AUTHOR_TYPE= "AUTHOR_TYPE";  // author questions one at a time, or random draw from qpool.
  public static Integer QUESTIONS_AUTHORED_ONE_BY_ONE= Integer.valueOf(1);
  public static Integer RANDOM_DRAW_FROM_QUESTIONPOOL= Integer.valueOf(2);

  public static String QUESTIONS_ORDERING = "QUESTIONS_ORDERING"; // question ordering within a part
  public static Integer AS_LISTED_ON_ASSESSMENT_PAGE= Integer.valueOf(1);
  public static Integer RANDOM_WITHIN_PART= Integer.valueOf(2);
  public static String POOLID_FOR_RANDOM_DRAW = "POOLID_FOR_RANDOM_DRAW";
  public static String POOLNAME_FOR_RANDOM_DRAW = "POOLNAME_FOR_RANDOM_DRAW";
  public static String NUM_QUESTIONS_DRAWN = "NUM_QUESTIONS_DRAWN";
  public static String QUESTIONS_RANDOM_DRAW_DATE = "QUESTIONS_RANDOM_DRAW_DATE";
  
  public static String RANDOMIZATION_TYPE = "RANDOMIZATION_TYPE";
  public static String PER_SUBMISSION = "1";
  public static String PER_STUDENT = "2";
  
  public static String POINT_VALUE_FOR_QUESTION = "POINT_VALUE_FOR_QUESTION";
  public static String DISCOUNT_VALUE_FOR_QUESTION = "DISCOUNT_VALUE_FOR_QUESTION";

  
   Long getSectionId() ;

   void setSectionId(Long sectionId);

   Long getAssessmentId() ;

   void setAssessmentId(Long assessmentId);

   AssessmentIfc getAssessment();
   //AssessmentData getAssessment();

   void setAssessment(AssessmentIfc assessment);

   Integer getDuration();

   void setDuration(Integer duration);

   Integer getSequence();

   void setSequence(Integer sequence);

   String getTitle();

   void setTitle(String title);

   String getDescription();

   void setDescription(String description);

   Long getTypeId();

   void setTypeId(Long typeId);

   Integer getStatus();

   void setStatus(Integer status);

   String getCreatedBy();

   void setCreatedBy(String createdBy);

   Date getCreatedDate();

   void setCreatedDate(Date createdDate);

   String getLastModifiedBy();

   void setLastModifiedBy(String lastModifiedBy);

   Date getLastModifiedDate();

   void setLastModifiedDate(Date lastModifiedDate);

   Set getItemSet();

   void setItemSet(Set itemSet);

   void addItem(ItemDataIfc item);

   TypeIfc getType();

   ArrayList getItemArray();

   ArrayList getItemArraySortedForGrading();

   Set getSectionMetaDataSet();

   void setSectionMetaDataSet(Set param);

   HashMap getSectionMetaDataMap(Set param) ;

   String getSectionMetaDataByLabel(String label);

   void addSectionMetaData(String label, String entry);

   Set getSectionAttachmentSet();

   void setSectionAttachmentSet(Set sectionAttachmentSet);

   List getSectionAttachmentList();


}
