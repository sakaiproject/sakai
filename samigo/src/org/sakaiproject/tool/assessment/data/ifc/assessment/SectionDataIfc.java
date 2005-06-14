package org.sakaiproject.tool.assessment.data.ifc.assessment;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

public interface SectionDataIfc
    extends java.io.Serializable{

  public static String AUTHOR_TYPE= "AUTHOR_TYPE";  // author questions one at a time, or random draw from qpool.
  public static Integer QUESTIONS_AUTHORED_ONE_BY_ONE= new Integer(1);
  public static Integer RANDOM_DRAW_FROM_QUESTIONPOOL= new Integer(2);

  public static String QUESTIONS_ORDERING = "QUESTIONS_ORDERING"; // question ordering within a part
  public static Integer AS_LISTED_ON_ASSESSMENT_PAGE= new Integer(1);
  public static Integer RANDOM_WITHIN_PART= new Integer(2);
  public static String POOLID_FOR_RANDOM_DRAW = "POOLID_FOR_RANDOM_DRAW";
  public static String NUM_QUESTIONS_DRAWN = "NUM_QUESTIONS_DRAWN";

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

   ArrayList getItemArraySorted();

   Set getSectionMetaDataSet();

   void setSectionMetaDataSet(Set param);

   HashMap getSectionMetaDataMap(Set param) ;

   String getSectionMetaDataByLabel(String label);

   void addSectionMetaData(String label, String entry);



}
