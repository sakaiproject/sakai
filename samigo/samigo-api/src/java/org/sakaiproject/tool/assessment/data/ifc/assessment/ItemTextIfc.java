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



package org.sakaiproject.tool.assessment.data.ifc.assessment;

import java.util.List;
import java.util.Set;
import java.util.Map;

public interface ItemTextIfc
    extends Comparable<ItemTextIfc>, java.io.Serializable
{
  public static Long EMI_THEME_TEXT_SEQUENCE = Long.valueOf(-1);
  public static Long EMI_ANSWER_OPTIONS_SEQUENCE = Long.valueOf(-2);
  public static Long EMI_LEAD_IN_TEXT_SEQUENCE = Long.valueOf(-3);
  public static String NONE_OF_THE_ABOVE = "None of the above";
 
  Long getId();

  void setId(Long id);

  ItemDataIfc getItem();

  void setItem(ItemDataIfc item);

  Long getSequence();

  void setSequence(Long sequence);

  String getText();

  void setText(String text);

  Set<AnswerIfc> getAnswerSet();

  void setAnswerSet(Set<AnswerIfc> answerSet);

  List<AnswerIfc> getAnswerArray();

  List<AnswerIfc> getAnswerArraySorted();

  List<AnswerIfc> getAnswerArrayWithDistractorSorted();
  
  Set<ItemTextAttachmentIfc> getItemTextAttachmentSet();

  void setItemTextAttachmentSet(Set<ItemTextAttachmentIfc> itemTextAttachmentSet);

    /**
     * This is an actual EMI Question Item (i.e. not Theme or Lead In Text or
     * Complete Answer Options List)
     *
     * @return
     * @since 2.10
     */

  void addItemTextAttachment(ItemTextAttachmentIfc attachment);
  void addNewItemTextAttachment(ItemTextAttachmentIfc attachment);
  void removeItemTextAttachmentById(Long attachmentId);
  void removeItemTextAttachment(ItemTextAttachmentIfc attachment);
  Map<Long, ItemTextAttachmentIfc> getItemTextAttachmentMap();


    public boolean isEmiQuestionItemText();

    /**
     * Get the number of required option
     *
     * @return the number of required options or 1 if not defined
     * @since 2.10
     */
    public Integer getRequiredOptionsCount();

    public void setRequiredOptionsCount(Integer requiredOptionsCount);

    public String getEmiCorrectOptionLabels();

    public boolean getHasAttachment();
}
