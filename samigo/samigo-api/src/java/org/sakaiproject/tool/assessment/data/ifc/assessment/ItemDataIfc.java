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
 *       http://www.osedu.org/licenses/ECL-2.0
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

public interface ItemDataIfc extends java.io.Serializable {

  public static Integer ACTIVE_STATUS = Integer.valueOf(1);
  public static Integer INACTIVE_STATUS = Integer.valueOf(0);

  Long getItemId();

  void setItemId(Long itemId);

  String getItemIdString();

  void setItemIdString(String itemIdString);

  SectionDataIfc getSection();

  void setSection(SectionDataIfc section);

  Integer getSequence();

  void setSequence(Integer sequence);

  Integer getDuration();

  void setTriesAllowed(Integer triesAllowed);

  Integer getTriesAllowed();

  void setDuration(Integer duration);

  String getInstruction();

  void setInstruction(String instruction);

  String getDescription();

  void setDescription(String description);

  Long getTypeId();

  void setTypeId(Long typeId);

  String getGrade();

  void setGrade(String grade);

  Float getScore();

  void setScore(Float score);
  
  Float getDiscount();

  void setDiscount(Float discount);

  String getHint();

  void setHint(String hint);

  Boolean getHasRationale();

  void setHasRationale(Boolean hasRationale);

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

  Set getItemTextSet();

  void setItemTextSet(Set itemTextSet) ;

  void addItemText(String itemText, Set answerSet);

  Set getItemMetaDataSet();

  void setItemMetaDataSet(Set itemMetaDataSet);

  HashMap getItemMetaDataMap(Set itemMetaDataSet) ;

  Set getItemFeedbackSet();

  void setItemFeedbackSet(Set itemFeedbackSet);

  HashMap getItemFeedbackMap(Set itemFeedbackSet) ;

  String getItemMetaDataByLabel(String label);

  void addItemMetaData(String label, String entry);

  String getCorrectItemFeedback();

  void setCorrectItemFeedback(String text);

  String getInCorrectItemFeedback();

  void setInCorrectItemFeedback(String text);

  String getGeneralItemFeedback();

  void setGeneralItemFeedback(String text);

  String getItemFeedback(String typeId);

  void addItemFeedback(String typeId, String text);

  void removeFeedbackByType(String typeId);

  void removeMetaDataByType(String typeId);

  public Boolean getIsTrue();

  public String getText();

  TypeIfc getType();

  ArrayList getItemTextArray();

  ArrayList getItemTextArraySorted();

  String getAnswerKey();

  Set getItemAttachmentSet();

  void setItemAttachmentSet(Set itemAttachmentSet);

  List getItemAttachmentList();
}
