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

import org.apache.log4j.Category;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
//import org.sakaiproject.tool.assessment.facade.AgentFacade;
//import org.sakaiproject.tool.assessment.facade.TypeFacadeQueriesAPI;
//import org.sakaiproject.tool.assessment.services.GradingService;
//import org.sakaiproject.tool.assessment.services.PersistenceService;

public class PublishedItemData
    implements java.io.Serializable, ItemDataIfc, Comparable {
  static Category errorLogger = Category.getInstance("errorLogger");

  private static final long serialVersionUID = 7526471155622776147L;

  private Long itemId;
  private String itemIdString;
  private SectionDataIfc section;
  private Integer sequence;
  private Integer duration;
  private Integer triesAllowed;
  private String instruction;
  private String description;
  private Long typeId;
  private String grade;
  private Float score;
  private Float discount;
  private String hint;
  private Boolean hasRationale;
  private Integer status;
  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;
  private Set itemTextSet;
  private Set itemMetaDataSet;
  private Set itemFeedbackSet;
  private HashMap itemMetaDataMap = new HashMap();
  private HashMap itemFeedbackMap = new HashMap();
  private ItemGradingData lastItemGradingDataByAgent;
  private Set itemAttachmentSet;
  private Boolean partialCreditFlag;

  public PublishedItemData() {}

  // this constructor should be deprecated, it is missing triesAllowed
  public PublishedItemData(SectionDataIfc section, Integer sequence,
                  Integer duration, String instruction, String description,
                  Long typeId, String grade, Float score, Float discount, String hint,
                  Boolean hasRationale, Integer status, String createdBy,
                  Date createdDate, String lastModifiedBy,
                  Date lastModifiedDate,
                  Set itemTextSet, Set itemMetaDataSet, Set itemFeedbackSet, Boolean partialCrditFlag) {
    this.section = section;
    this.sequence = sequence;
    this.duration = duration;
    this.instruction = instruction;
    this.description = description;
    this.typeId = typeId;
    this.grade = grade;
    this.score = score;
    this.discount = discount;
    this.hint = hint;
    this.hasRationale = hasRationale;
    this.status = status;
    this.createdBy = createdBy;
    this.createdDate = createdDate;
    this.lastModifiedBy = lastModifiedBy;
    this.lastModifiedDate = lastModifiedDate;
    this.itemTextSet = itemTextSet;
    this.itemMetaDataSet = itemMetaDataSet;
    this.itemFeedbackSet = itemFeedbackSet;
    this.partialCreditFlag=partialCreditFlag;
  }

  public PublishedItemData(SectionDataIfc section, Integer sequence,
                  Integer duration, String instruction, String description,
                  Long typeId, String grade, Float score, Float discount, String hint,
                  Boolean hasRationale, Integer status, String createdBy,
                  Date createdDate, String lastModifiedBy,
                  Date lastModifiedDate,
                  Set itemTextSet, Set itemMetaDataSet, Set itemFeedbackSet,
                  Integer triesAllowed, Boolean partialCreditFlag) {
    this.section = section;
    this.sequence = sequence;
    this.duration = duration;
    this.instruction = instruction;
    this.description = description;
    this.typeId = typeId;
    this.grade = grade;
    this.score = score;
    this.discount = discount;
    this.hint = hint;
    this.hasRationale = hasRationale;
    this.status = status;
    this.createdBy = createdBy;
    this.createdDate = createdDate;
    this.lastModifiedBy = lastModifiedBy;
    this.lastModifiedDate = lastModifiedDate;
    this.itemTextSet = itemTextSet;
    this.itemMetaDataSet = itemMetaDataSet;
    this.itemFeedbackSet = itemFeedbackSet;
    this.triesAllowed = triesAllowed;
    this.partialCreditFlag=partialCreditFlag;
  }

  public Long getItemId() {
    return this.itemId;
  }

  public void setItemId(Long itemId) {
    this.itemId = itemId;
    setItemIdString(itemId.toString());
  }

  public String getItemIdString() {
    return this.itemIdString;
  }

  public void setItemIdString(String itemIdString) {
    this.itemIdString = itemIdString;
  }

  public SectionDataIfc getSection() {
    return this.section;
  }

  public void setSection(SectionDataIfc section) {
    this.section = section;
  }

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

  public String getInstruction() {
    return this.instruction;
  }

  public void setInstruction(String instruction) {
    this.instruction = instruction;
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

  public String getGrade() {
    return this.grade;
  }

  public void setGrade(String grade) {
    this.grade = grade;
  }

  public Float getScore() {
    return this.score;
  }

  public void setScore(Float score) {
    this.score = score;
  }

  public Float getDiscount() {
	  if (this.discount==null){
		  this.discount= Float.valueOf(0);
	  }
	  return this.discount;
  }

  public void setDiscount(Float discount) {
	  if (discount==null){
		  discount =Float.valueOf(0);
	  }
	  this.discount = discount;
  }
	  
  public String getHint() {
    return this.hint;
  }

  public void setHint(String hint) {
    this.hint = hint;
  }

  public Boolean getHasRationale() {
    return this.hasRationale;
  }

  public void setHasRationale(Boolean hasRationale) {
    this.hasRationale = hasRationale;
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

  public Set getItemTextSet() {
    return itemTextSet;
  }

  public void setItemTextSet(Set itemTextSet) {
    this.itemTextSet = itemTextSet;
  }

  public Set getItemMetaDataSet() {
    return itemMetaDataSet;
  }

  public void setItemMetaDataSet(Set itemMetaDataSet) {
    this.itemMetaDataSet = itemMetaDataSet;
    this.itemMetaDataMap = getItemMetaDataMap(itemMetaDataSet);
  }

  public HashMap getItemMetaDataMap(Set itemMetaDataSet) {
    HashMap itemMetaDataMap = new HashMap();
    if (itemMetaDataSet != null){
      for (Iterator i = itemMetaDataSet.iterator(); i.hasNext(); ) {
        PublishedItemMetaData itemMetaData = (PublishedItemMetaData) i.next();
        itemMetaDataMap.put(itemMetaData.getLabel(), itemMetaData.getEntry());
      }
    }
    return itemMetaDataMap;
  }

  public Set getItemFeedbackSet() {
    return itemFeedbackSet;
  }

  public void setItemFeedbackSet(Set itemFeedbackSet) {
    this.itemFeedbackSet = itemFeedbackSet;
    this.itemFeedbackMap = getItemFeedbackMap(itemFeedbackSet);
  }

  public HashMap getItemFeedbackMap(Set itemFeedbackSet) {
    HashMap itemFeedbackMap = new HashMap();
    if (itemFeedbackSet != null){
      for (Iterator i = itemFeedbackSet.iterator(); i.hasNext(); ) {
        PublishedItemFeedback itemFeedback = (PublishedItemFeedback) i.next();
        itemFeedbackMap.put(itemFeedback.getTypeId(), itemFeedback.getText());
      }
    }
    return itemFeedbackMap;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
  }

  public void addItemText(String text, Set answerSet) {
    if (this.itemTextSet == null) {
      this.itemTextSet = new HashSet();
    }
    Long sequence =  Long.valueOf(this.itemTextSet.size()+1);
    PublishedItemText itemText = new PublishedItemText(this, sequence, text, answerSet);
    this.itemTextSet.add(itemText);
  }

  public String getItemMetaDataByLabel(String label) {
    return (String)this.itemMetaDataMap.get(label);
  }

  public void addItemMetaData(String label, String entry) {
    if (this.itemMetaDataSet == null) {
      setItemMetaDataSet(new HashSet());
      this.itemMetaDataMap = new HashMap();
    }
    this.itemMetaDataMap.put(label, entry);
    this.itemMetaDataSet.add(new PublishedItemMetaData(this, label, entry));
  }

  public String getCorrectItemFeedback() {
    return getItemFeedback(PublishedItemFeedback.CORRECT_FEEDBACK);
  }

  public void setCorrectItemFeedback(String text) {
    removeFeedbackByType(PublishedItemFeedback.CORRECT_FEEDBACK);
    addItemFeedback(PublishedItemFeedback.CORRECT_FEEDBACK, text);
  }

  public String getInCorrectItemFeedback() {
    return getItemFeedback(PublishedItemFeedback.INCORRECT_FEEDBACK);
  }

  public void setInCorrectItemFeedback(String text) {
    removeFeedbackByType(PublishedItemFeedback.INCORRECT_FEEDBACK);
    addItemFeedback(PublishedItemFeedback.INCORRECT_FEEDBACK, text);
  }

 /**
  * Get General Feedback
  * @return
  */
  public String getGeneralItemFeedback() {
    return getItemFeedback(ItemFeedback.GENERAL_FEEDBACK);
  }

  /**
   * Set General Feedback
   * @param text
   */
  public void setGeneralItemFeedback(String text) {
    removeFeedbackByType(ItemFeedback.GENERAL_FEEDBACK);
    addItemFeedback(ItemFeedback.GENERAL_FEEDBACK, text);
  }

  public String getItemFeedback(String typeId) {
    return (String)this.itemFeedbackMap.get(typeId);
  }

  public void addItemFeedback(String typeId, String text) {
    if (this.itemFeedbackSet == null) {
      setItemFeedbackSet(new HashSet());
      this.itemFeedbackMap = new HashMap();
    }
    this.itemFeedbackMap.put(typeId, text);
    this.itemFeedbackSet.add(new PublishedItemFeedback(this, typeId, text));
  }

  public void removeFeedbackByType(String typeId) {
    if (itemFeedbackSet != null) {
      for (Iterator i = this.itemFeedbackSet.iterator(); i.hasNext(); ) {
        PublishedItemFeedback itemFeedback = (PublishedItemFeedback) i.next();
        if (itemFeedback.getTypeId().equals(typeId)) {
          this.itemFeedbackSet.remove(itemFeedback);
        }
      }
    }
  }

  public void removeMetaDataByType(String label) {
   try {
    if (itemMetaDataSet!= null) {
      for (Iterator i = this.itemMetaDataSet.iterator(); i.hasNext(); ) {
        PublishedItemMetaData itemMetaData= (PublishedItemMetaData) i.next();
        if (itemMetaData.getLabel().equals(label)) {
          //this.itemMetaDataSet.remove(itemMetaData);
          i.remove();
        } 
      } 
    } 
  }
  catch (Exception e) {
   e.printStackTrace();
  }
}

  /**
  * If this is a true-false question return true if it is true, else false.
  * If it is not a true-false question return false.
  * @return true if this is a true true-false question
  */
   public Boolean getIsTrue() {
     // if not true false, done.
     if (!this.getTypeId().equals(TypeD.TRUE_FALSE))
     {
       return Boolean.FALSE;
     }

     Set answerSet = null;

     Set set = this.getItemTextSet();
     Iterator iter = set.iterator();
     if (iter.hasNext())
     {
       answerSet = ( (ItemTextIfc) iter.next()).getAnswerSet();
     }

     // if the FIRST answer is CORRECT, the true false question is TRUE
     // Note that this is implementation dependent
     if (answerSet != null)
     {
       Iterator aiter = answerSet.iterator();
       if (aiter.hasNext())
       {
         AnswerIfc answer = (AnswerIfc) aiter.next();
         return answer.getIsCorrect();
       }
     }

     return Boolean.FALSE;

   }

   /**
  * In the case of an ordinary question, this will obtain the a set of text with
  * one element and return it; in FIB return multiple elements separated by underscores.
  * @return text of question
  */
   public String getText() {
     String text = "";
     if (getTypeId().equals(TypeIfc.MATCHING))
       return instruction;
     Set set = this.getItemTextSet();
     Iterator iter = set.iterator();

     while (iter.hasNext())
     {
       ItemTextIfc itemText = (ItemTextIfc) iter.next();
       text += "" + itemText.getText(); //each text add it in

       if (this.getTypeId().equals(TypeIfc.FILL_IN_BLANK))
       { //e.g. Roses are {}. Violets are {}. replace as
         // Roses are ____. Violets are ____.
         text = text.replaceAll("\\{","__");
         text = text.replaceAll("\\}","__");
      }
       if (this.getTypeId().equals(TypeIfc.FILL_IN_NUMERIC))
       { //e.g. Roses are {}. Violets are {}. replace as
         // Roses are ____. Violets are ____.
         text = text.replaceAll("\\{","__");
         text = text.replaceAll("\\}","__");
      }
    }
    return text;
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

   public ArrayList getItemTextArray() {
     ArrayList list = new ArrayList();
     Iterator iter = itemTextSet.iterator();
     while (iter.hasNext()){
       list.add(iter.next());
     }
     return list;
   }

   public ArrayList getItemTextArraySorted() {
     ArrayList list = getItemTextArray();
     Collections.sort(list);
     return list;
   }

   public void setTriesAllowed(Integer triesAllowed) {
     this.triesAllowed = triesAllowed;
   }

   public Integer getTriesAllowed() {
     return this.triesAllowed;
   }

   /**
    * This method return the answerKey for a matching question
    * e.g. A:2, B:3, C:1, D:4 (where A, B & C is the answer label and 1,2 &3
    * are the itemText sequence
    */
   public String getAnswerKey() {
		String answerKey = "";
		ArrayList itemTextArray = getItemTextArraySorted();
		if (itemTextArray.size() == 0)
			return answerKey;

		ArrayList answerArray = ((ItemTextIfc) itemTextArray.get(0))
				.getAnswerArraySorted();
		HashMap h = new HashMap();
		for (int i = 0; i < itemTextArray.size(); i++) {
			ItemTextIfc text = (ItemTextIfc) itemTextArray.get(i);
			ArrayList answers = text.getAnswerArraySorted();
			for (int j = 0; j < answers.size(); j++) {
				AnswerIfc a = (AnswerIfc) answers.get(j);
				if ((Boolean.TRUE).equals(a.getIsCorrect())) {
					String pair = (String) h.get(a.getLabel());
					if (!this.getTypeId().equals(TypeD.MATCHING)) {
						if (this.getTypeId().equals(TypeD.TRUE_FALSE)) {
							answerKey = a.getText();
						} else {
							if (("").equals(answerKey)) {
								answerKey = a.getLabel();
							} else {
								answerKey += "," + a.getLabel();
							}
						}
					} else {
						if (pair == null) {
							String s = a.getLabel() + ":" + text.getSequence();
							h.put(a.getLabel(), s);
						} else {
							h.put(a.getLabel(), pair + " " + text.getSequence());
						}
					}
				}
			}
			if (this.getTypeId().equals(TypeD.MATCHING)) {
				for (int k = 0; k < answerArray.size(); k++) {
					AnswerIfc a = (AnswerIfc) answerArray.get(k);
					String pair = (String) h.get(a.getLabel());
					// if answer is not a match to any text, just print answer
					// label
					if (pair == null)
						pair = a.getLabel() + ": ";

					if (k != 0)
						answerKey = answerKey + ",  " + pair;
					else
						answerKey = pair;
				}
			}
		}

		return answerKey;
	}

  public int compareTo(Object o) {
      PublishedItemData a = (PublishedItemData)o;
      return sequence.compareTo(a.sequence);
  }

    /*
  public ItemGradingData getLastItemGradingDataByAgent(){
    GradingService service = new GradingService();
    ItemGradingData i= service.getLastItemGradingDataByAgent(
        this.itemId.toString(), AgentFacade.getAgentString());
    return i;
  }

  public ItemGradingData getLastItemGradingDataByGivenAgent(String agentId){
    GradingService service = new GradingService();
    ItemGradingData i= service.getLastItemGradingDataByAgent(
        this.itemId.toString(), "jon");
    return i;
  }
    */

    public boolean getGeneralItemFbIsNotEmpty() {
		return isNotEmpty(getGeneralItemFeedback());
	}

	public boolean getCorrectItemFbIsNotEmpty() {
		return isNotEmpty(getCorrectItemFeedback());
	}

	public boolean getIncorrectItemFbIsNotEmpty() {
		return isNotEmpty(getInCorrectItemFeedback());
	}

	public boolean isNotEmpty(String wyzText) {

		if (wyzText != null) {
			int index = 0;
			String t = (wyzText.replaceAll("<.*?>", " ")).trim();
			while (index < t.length()) {
				char c = t.charAt(index);
				if (Character.isLetterOrDigit(c)) {
					return true;
				}
				index++;
			}
		}
		return false;
	}

  public Set getItemAttachmentSet() {
    return itemAttachmentSet;
  }

  public void setItemAttachmentSet(Set itemAttachmentSet) {
    this.itemAttachmentSet = itemAttachmentSet;
  }

  public List getItemAttachmentList() {
    ArrayList list = new ArrayList();
    if (itemAttachmentSet !=null ){
      Iterator iter = itemAttachmentSet.iterator();
      while (iter.hasNext()){
        ItemAttachmentIfc a = (ItemAttachmentIfc)iter.next();
        list.add(a);
      }
    }
    return list;
  }

  public Boolean getPartialCreditFlag() {
	  return partialCreditFlag;
  }

  public void setPartialCreditFlag(Boolean partialCreditFlag) {
	  this.partialCreditFlag = partialCreditFlag;
  }
}
