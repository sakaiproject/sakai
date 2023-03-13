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



package org.sakaiproject.tool.assessment.data.ifc.assessment;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.List;

import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;


public interface ItemDataIfc extends Comparable<ItemDataIfc>, java.io.Serializable {

  public static final Integer ACTIVE_STATUS = 1;
  public static final Integer INACTIVE_STATUS = 0;

  public static final int ITEM_NOT_CANCELED = 0;
  public static final int ITEM_TOTAL_SCORE_TO_CANCEL = 10;
  public static final int ITEM_TOTAL_SCORE_CANCELLED = 11;
  public static final int ITEM_DISTRIBUTED_TO_CANCEL = 20;
  public static final int ITEM_DISTRIBUTED_CANCELLED = 21;

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

  Double getScore();

  void setScore(Double score);
  
  Double getDiscount();
  
  Double getMinScore();

  void setMinScore(Double minScore);
  
  void setDiscount(Double discount);

  String getHint();

  void setHint(String hint);

  Boolean getHasRationale();

  void setHasRationale(Boolean hasRationale);
  
  void setPartialCreditFlag(Boolean particalCreditFlag);
  
  Boolean getPartialCreditFlag();

  Integer getStatus();

  void setStatus(Integer status);

  Integer getCancellation();

  void setCancellation(Integer cancellation);

  String getCreatedBy();

  void setCreatedBy(String createdBy);

  Date getCreatedDate();

  void setCreatedDate(Date createdDate);

  String getLastModifiedBy();

  void setLastModifiedBy(String lastModifiedBy);

  Date getLastModifiedDate();

  void setLastModifiedDate(Date lastModifiedDate);

  Boolean getIsExtraCredit();

  void setIsExtraCredit(Boolean isExtraCredit);

  Set<ItemTextIfc> getItemTextSet();

  void setItemTextSet(Set<ItemTextIfc> itemTextSet) ;

  void addItemText(String itemText, Set<AnswerIfc> answerSet);

  Set<ItemMetaDataIfc> getItemMetaDataSet();

  void setItemMetaDataSet(Set<ItemMetaDataIfc> itemMetaDataSet);

  Set<ItemTagIfc> getItemTagSet();

  void setItemTagSet(Set<ItemTagIfc> itemTagSet);

  Set<ItemFeedbackIfc> getItemFeedbackSet();

  void setItemFeedbackSet(Set<ItemFeedbackIfc> itemFeedbackSet);

  String getHash();

  void setHash(String hash);

  String getItemMetaDataByLabel(String label);

  void addItemMetaData(String label, String entry);

  String getCorrectItemFeedback();

  String getCorrectItemFeedbackValue();

  void setCorrectItemFeedback(String text, String value);

  String getInCorrectItemFeedback();

  String getInCorrectItemFeedbackValue();

  void setInCorrectItemFeedback(String text, String value);

  String getGeneralItemFeedback();

  void setGeneralItemFeedback(String text, String value);

  String getItemFeedback(String typeId);

  void addItemFeedback(String typeId, String text, String value);

  void removeFeedbackByType(String typeId);

  void removeMetaDataByType(String typeId);

  public Boolean getIsTrue();

  public String getText();

  TypeIfc getType();

  List<ItemTextIfc> getItemTextArray();

  List<ItemTextIfc> getItemTextArraySorted();

  String getAnswerKey();

  Set<ItemAttachmentIfc> getItemAttachmentSet();

  void setItemAttachmentSet(Set<ItemAttachmentIfc> itemAttachmentSet);

  List<ItemAttachmentIfc> getItemAttachmentList();
  void addItemAttachment(ItemAttachmentIfc attachment);
  void removeItemAttachmentById(Long attachmentId);
  void removeItemAttachment(ItemAttachmentIfc attachment);
  Map<Long, ItemAttachmentIfc> getItemAttachmentMap();
  
  String getLeadInText();
  String getThemeText();
  //public ItemTextIfc getEmiAnswerComponentsItemText();
  public int getNumberOfCorrectEmiOptions();
  public String getEmiAnswerOptionLabels();
  public boolean isValidEmiAnswerOptionLabel(String label);

  public List<AnswerIfc> getEmiAnswerOptions();
  public List<ItemTextIfc> getEmiQuestionAnswerCombinations();

  public ItemTextIfc getItemTextBySequence(Long itemTextSequence);
  public Integer getAnswerOptionsRichCount();
  public void setAnswerOptionsRichCount(Integer answerOptionsRichCount);
  
  // for EMI
  public static final String ANSWER_OPTION_LABELS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  public static final String ANSWER_OPTION_VALID_DELIMITERS = " ,;:.";
  public static final Integer ANSWER_OPTIONS_SIMPLE = 0;
  public static final Integer ANSWER_OPTIONS_RICH = 1;
  public Integer getAnswerOptionsSimpleOrRich();
  public void setAnswerOptionsSimpleOrRich(Integer answerOptionsSimpleOrRich);  
  public String getEmiAnswerOptionsRichText();
  public boolean getIsAnswerOptionsSimple();
  public boolean getIsAnswerOptionsRich();
  String getImageMapSrc();
  String getImageMapAltText();
  Boolean getScoreDisplayFlag();
  void setScoreDisplayFlag(Boolean scoreDisplayFlag);
  public String getTagListToJsonString();
  
  public Long getOriginalItemId();
  public void setOriginalItemId(Long originalItemId);
  public void updateFeedbackByType(String typeId, String text, String value);
}
