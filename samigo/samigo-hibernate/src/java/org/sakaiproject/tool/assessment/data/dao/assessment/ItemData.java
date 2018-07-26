/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.data.dao.assessment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTagIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

@Slf4j
public class ItemData
    implements java.io.Serializable,
    ItemDataIfc, Comparable<ItemDataIfc> {
  static ResourceBundle rb = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.Messages");

  private static final long serialVersionUID = 7526471155622776147L;
  public static final Long ADMIN = Long.valueOf(34);

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
  private Double score;
  private Double discount;
  private Boolean scoreDisplayFlag = Boolean.TRUE;
  private String hint;
  private Boolean hasRationale;
  private Integer status;
  private Boolean partialCreditFlag;
  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;
  private Set<ItemTextIfc> itemTextSet;
  private Set<ItemMetaDataIfc> itemMetaDataSet;
  private Set<ItemFeedbackIfc> itemFeedbackSet;
  private Set<ItemAttachmentIfc> itemAttachmentSet;
  private Set<ItemTagIfc> itemTagSet;
  private Double minScore;
  private String hash;
 
  // for EMI question
  private String themeText;
  private String leadInText;
  private Integer answerOptionsRichCount;
  private Integer answerOptionsSimpleOrRich = ItemDataIfc.ANSWER_OPTIONS_SIMPLE;

  private String tagListToJsonString;
  
public ItemData() {}

  // this constructor should be deprecated, it is missing triesAllowed
  public ItemData(SectionDataIfc section, Integer sequence,
                  Integer duration, String instruction, String description,
                  Long typeId, String grade, Double score, Boolean scoreDisplayFlag, Double discount, Double minScore, String hint,
                  Boolean hasRationale, Integer status, String createdBy,
                  Date createdDate, String lastModifiedBy,
                  Date lastModifiedDate,
                  Set<ItemTextIfc> itemTextSet, Set<ItemMetaDataIfc> itemMetaDataSet, Set<ItemFeedbackIfc> itemFeedbackSet, Boolean partialCreditFlag, String hash) {
    this.section = section;
    this.sequence = sequence;
    this.duration = duration;
    this.instruction = instruction;
    this.description = description;
    this.typeId = typeId;
    this.grade = grade;
    this.score = score;
    this.scoreDisplayFlag = scoreDisplayFlag;
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
    this.minScore = minScore;
    this.hash = hash;

  }

  public ItemData(SectionDataIfc section, Integer sequence,
                  Integer duration, String instruction, String description,
                  Long typeId, String grade, Double score, Boolean scoreDisplayFlag, Double discount, Double minScore, String hint,
                  Boolean hasRationale, Integer status, String createdBy,
                  Date createdDate, String lastModifiedBy,
                  Date lastModifiedDate,
                  Set<ItemTextIfc> itemTextSet, Set<ItemMetaDataIfc> itemMetaDataSet, Set<ItemFeedbackIfc> itemFeedbackSet,
                  Integer triesAllowed, Boolean partialCreditFlag, String hash) {
    this.section = section;
    this.sequence = sequence;
    this.duration = duration;
    this.instruction = instruction;
    this.description = description;
    this.typeId = typeId;
    this.grade = grade;
    this.score = score;
    this.scoreDisplayFlag = scoreDisplayFlag;
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
    this.minScore = minScore;
    this.hash = hash;
  }

    /*
  public Object clone() throws CloneNotSupportedException{

    ItemData cloned= new ItemData(
        this.getSection(),this.getSequence(), this.getDuration(), this.getInstruction(),
	this.getDescription(),this.getTypeId(),this.getGrade(),this.getScore(),
	this.getHint(),this.getHasRationale(),this.getStatus(),this.getCreatedBy(),
	this.getCreatedDate(),this.getLastModifiedBy(),this.getLastModifiedDate(),
        null, null, null, this.getTriesAllowed());

    // perform deep copy, set ItemTextSet, itemMetaDataSet and itemFeedbackSet
    Set newItemTextSet = copyItemTextSet(cloned, this.getItemTextSet());
    Set newItemMetaDataSet = copyItemMetaDataSet(cloned, this.getItemMetaDataSet());
    Set newItemFeedbackSet = copyItemFeedbackSet(cloned, this.getItemFeedbackSet());
    Set newItemAttachmentSet = copyItemAttachmentSet(cloned, this.getItemAttachmentSet());
    cloned.setItemTextSet(newItemTextSet);
    cloned.setItemMetaDataSet(newItemMetaDataSet);
    cloned.setItemFeedbackSet(newItemFeedbackSet);
    cloned.setItemAttachmentSet(newItemAttachmentSet);

    return (Object)cloned;
  }

  public Set copyItemTextSet(ItemData cloned, Set itemTextSet) {
    HashSet h = new HashSet();
    Iterator k = itemTextSet.iterator();
    while (k.hasNext()) {
      ItemText itemText = (ItemText) k.next();
      ItemText newItemText = new ItemText(cloned, itemText.getSequence(), itemText.getText(), null);
      Set newAnswerSet = copyAnswerSet(newItemText, itemText.getAnswerSet());
      newItemText.setAnswerSet(newAnswerSet);
      h.add(newItemText);
    }
    return h;
  }

  public Set copyAnswerSet(ItemText newItemText, Set answerSet) {
    HashSet h = new HashSet();
    Iterator l = answerSet.iterator();
    while (l.hasNext()) {
      Answer answer = (Answer) l.next();
      Answer newAnswer = new Answer(
          newItemText, answer.getText(), answer.getSequence(),
          answer.getLabel(),
          answer.getIsCorrect(), answer.getGrade(), answer.getScore(), null);
      Set newAnswerFeedbackSet = copyAnswerFeedbackSet(
          newAnswer, answer.getAnswerFeedbackSet());
      newAnswer.setAnswerFeedbackSet(newAnswerFeedbackSet);
      h.add(newAnswer);
    }
    return h;
  }

  public Set copyAnswerFeedbackSet(Answer newAnswer, Set answerFeedbackSet) {
    HashSet h = new HashSet();
    Iterator m = answerFeedbackSet.iterator();
    while (m.hasNext()) {
      AnswerFeedback answerFeedback = (AnswerFeedback) m.next();
      AnswerFeedback newAnswerFeedback = new AnswerFeedback(
          newAnswer, answerFeedback.getTypeId(), answerFeedback.getText());
      h.add(newAnswerFeedback);
    }
    return h;
  }

  public Set copyItemMetaDataSet(ItemData cloned, Set itemMetaDataSet) {
    HashSet h = new HashSet();
    Iterator n = itemMetaDataSet.iterator();
    while (n.hasNext()) {
      ItemMetaData itemMetaData = (ItemMetaData) n.next();
      ItemMetaData newItemMetaData = new ItemMetaData(
          cloned, itemMetaData.getLabel(), itemMetaData.getEntry());
      h.add(newItemMetaData);
    }
    return h;
  }

  public Set copyItemFeedbackSet(ItemData cloned, Set itemFeedbackSet) {
    HashSet h = new HashSet();
    Iterator o = itemFeedbackSet.iterator();
    while (o.hasNext()) {
      ItemFeedback itemFeedback = (ItemFeedback) o.next();
      ItemFeedback newItemFeedback = new ItemFeedback(
          cloned, itemFeedback.getTypeId(), itemFeedback.getText());
      h.add(newItemFeedback);
    }
    return h;
  }

    */


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

  public Double getScore() {
    return this.score;
  }

  public void setScore(Double score) {
    this.score = score;
  }

  public Boolean getScoreDisplayFlag(){
	  if (this.scoreDisplayFlag == null) {
		  return Boolean.TRUE;
	  }
	  return this.scoreDisplayFlag;
  }
  
  public void setScoreDisplayFlag(Boolean scoreDisplayFlag){
	  this.scoreDisplayFlag = scoreDisplayFlag;
  }
  
  public Double getDiscount() {
	  if (this.discount==null){
		  this.discount=Double.valueOf(0);
	  }
	  return this.discount;
  }

  public void setDiscount(Double discount) {
	  if (discount==null){
		  discount=Double.valueOf(0);
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
	  if (hasRationale == null) this.hasRationale = Boolean.valueOf(false);
	  else this.hasRationale = hasRationale;
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

  public Set<ItemTextIfc> getItemTextSet() {
    return itemTextSet;
  }

  public void setItemTextSet(Set<ItemTextIfc> itemTextSet) {
    this.itemTextSet = itemTextSet;
  }

  public Set<ItemMetaDataIfc> getItemMetaDataSet() {
    return itemMetaDataSet;
  }

  public void setItemMetaDataSet(Set<ItemMetaDataIfc> itemMetaDataSet) {
    this.itemMetaDataSet = itemMetaDataSet;
  }

  public Set<ItemTagIfc> getItemTagSet() { return itemTagSet; }

  public void setItemTagSet(Set<ItemTagIfc> itemTagSet) { this.itemTagSet = itemTagSet; this.tagListToJsonString = convertTagListToJsonString(itemTagSet);}

  public Set<ItemFeedbackIfc> getItemFeedbackSet() {
    return itemFeedbackSet;
  }

  public void setItemFeedbackSet(Set<ItemFeedbackIfc> itemFeedbackSet) {
    this.itemFeedbackSet = itemFeedbackSet;
  }

  public String getHash() {
    return this.hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
  }

  public void addItemText(String text, Set<AnswerIfc> answerSet) {
    if (this.itemTextSet == null) {
      this.itemTextSet = new HashSet<ItemTextIfc>();
    }
    Long sequence =Long.valueOf(this.itemTextSet.size()+1);
    ItemText itemText = new ItemText(this, sequence, text, answerSet);
    this.itemTextSet.add(itemText);
  }

  public String getItemMetaDataByLabel(String label) {
	  for (Iterator<ItemMetaDataIfc> i = this.itemMetaDataSet.iterator(); i.hasNext(); ) {
		  ItemMetaData imd = (ItemMetaData) i.next();
		  if (imd.getLabel().equals(label)) {
			  return (String) imd.getEntry();
		  }
	  }
	  return null;
  }

  public void addItemMetaData(String label, String entry) {
    if (this.itemMetaDataSet == null) {
      setItemMetaDataSet(new HashSet<ItemMetaDataIfc>());
    }
    this.itemMetaDataSet.add(new ItemMetaData(this, label, entry));
  }

  public String getCorrectItemFeedback() {
    return getItemFeedback(ItemFeedback.CORRECT_FEEDBACK);
  }

  public void setCorrectItemFeedback(String text) {
    removeFeedbackByType(ItemFeedback.CORRECT_FEEDBACK);
    addItemFeedback(ItemFeedback.CORRECT_FEEDBACK, text);
  }

  public String getInCorrectItemFeedback() {
    return getItemFeedback(ItemFeedback.INCORRECT_FEEDBACK);
  }

  public void setInCorrectItemFeedback(String text) {
    removeFeedbackByType(ItemFeedback.INCORRECT_FEEDBACK);
    addItemFeedback(ItemFeedback.INCORRECT_FEEDBACK, text);
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
	  if ( this.itemFeedbackSet == null || this.itemFeedbackSet.isEmpty() ) {
		  return null;
	  }
	  for (Iterator<ItemFeedbackIfc> i = this.itemFeedbackSet.iterator(); i.hasNext(); ) {
		  ItemFeedback itemFeedback = (ItemFeedback) i.next();
		  if (itemFeedback.getTypeId().equals(typeId)) {
			  return (String) itemFeedback.getText();
		  }
	  }
	  return null;
  }

  public void addItemFeedback(String typeId, String text) {
    if (this.itemFeedbackSet == null) {
      setItemFeedbackSet(new HashSet<ItemFeedbackIfc>());
    }
    this.itemFeedbackSet.add(new ItemFeedback(this, typeId, text));
  }

  public void removeFeedbackByType(String typeId) {
    if (itemFeedbackSet != null) {
      for (Iterator<ItemFeedbackIfc> i = this.itemFeedbackSet.iterator(); i.hasNext(); ) {
        ItemFeedbackIfc itemFeedback = i.next();
        if (itemFeedback.getTypeId().equals(typeId)) {
          //this.itemFeedbackSet.remove(itemFeedback);
          i.remove();
        }
      }
    }
  }


  public void removeMetaDataByType(String label) {
   try {
    if (itemMetaDataSet!= null) {
      for (Iterator<ItemMetaDataIfc> i = this.itemMetaDataSet.iterator(); i.hasNext(); ) {
        ItemMetaDataIfc itemMetaData= i.next();
        if (itemMetaData.getLabel().equals(label)) {
          //this.itemMetaDataSet.remove(itemMetaData);
          i.remove();
        } 
      } 
    } 
  }
  catch (Exception e) {
      log.error(e.getMessage(), e);
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

    Set<AnswerIfc> answerSet = null;

    Set<ItemTextIfc> set = this.getItemTextSet();
    Iterator<ItemTextIfc> iter = set.iterator();
    if (iter.hasNext())
    {
      answerSet = iter.next().getAnswerSet();
    }

    // if the FIRST answer is CORRECT, the true false question is TRUE
    // Note that this is implementation dependent
    if (answerSet != null)
    {
      Iterator<AnswerIfc> aiter = answerSet.iterator();
      if (aiter.hasNext())
      {
        AnswerIfc answer = aiter.next();
        return answer.getIsCorrect();
      }
    }

    return Boolean.FALSE;

  }

  /**
   * In the case of an ordinary question, this will obtain the a set of text with
   * one element and return it; in FIB or FIN return multiple elements separated by underscores.
   * @return text of question
   */
   public String getText() {
     String text = "";
     if (getTypeId().equals(TypeIfc.MATCHING) 
             || getTypeId().equals(TypeIfc.CALCULATED_QUESTION)
             || getTypeId().equals(TypeIfc.IMAGEMAP_QUESTION)
             || getTypeId().equals(TypeIfc.MATRIX_CHOICES_SURVEY))
       return instruction;
     Set<ItemTextIfc> set = this.getItemTextSet();
     Iterator<ItemTextIfc> iter = set.iterator();

     while (iter.hasNext())
     {
       ItemTextIfc itemText = iter.next();
       
       //if EMI use only the first textItem's text for display (sequence = 0)
       if (this.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS)) {
    	   if (!itemText.getSequence().equals(Long.valueOf(0))) {
    		   continue;
    	   }
    	   else {
    		   text = itemText.getText();
    		   break;
    	   }
       }
       
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

  public List<ItemTextIfc> getItemTextArray() {
    ArrayList<ItemTextIfc> list = new ArrayList<ItemTextIfc>();
    if(itemTextSet != null){
    	Iterator<ItemTextIfc> iter = itemTextSet.iterator();
    	while (iter.hasNext()){
    		list.add(iter.next());
    	}
    }
    return list;
  }

  public List<ItemTextIfc> getItemTextArraySorted() {
    List<ItemTextIfc> list = getItemTextArray();
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
   * Added by Huong Nguyen for other types as well.
   */
  public String getAnswerKey(){
	  
   String answerKey="";
   List<ItemTextIfc> itemTextArray = getItemTextArraySorted();
   if (itemTextArray.size()==0)
     return answerKey;

	if (this.getTypeId().equals(TypeD.EXTENDED_MATCHING_ITEMS)) {
		Iterator<ItemTextIfc> itemTextIter = itemTextArray.iterator();
		while (itemTextIter.hasNext()) {
			ItemTextIfc itemText = itemTextIter.next();
			if (itemText.isEmiQuestionItemText()) {
			   answerKey += itemText.getSequence() + ":";
			   List<AnswerIfc> emiItems = itemText.getAnswerArraySorted();
			   Iterator<AnswerIfc> emiItemsIter = emiItems.iterator();
			   while (emiItemsIter.hasNext()) {
				   AnswerIfc answer = emiItemsIter.next();
				   if (answer.getIsCorrect()) {
					   answerKey += answer.getLabel();
				   }
			   }
			   answerKey += " ";
			}
		}
		return answerKey;
	}

	else if (typeId.equals(TypeD.MATCHING)) {

		List<String> answerKeys = new ArrayList<>(itemTextArray.size());
		for (ItemTextIfc question : itemTextArray) {
			boolean isDistractor = true;

			List<AnswerIfc> answersSorted = question.getAnswerArraySorted();
			for (AnswerIfc answer : answersSorted) {
				if (!getPartialCreditFlag() && answer.getIsCorrect()) {
					answerKeys.add(question.getSequence() + ":" + answer.getLabel());
					isDistractor = false;
					break;
				}
			}

			if (isDistractor) {
				answerKeys.add(question.getSequence() + ":" + rb.getString("choice_labels").split(":")[answersSorted.size()]);
			}
		}

		answerKey = StringUtils.join(answerKeys, ", ");
		return answerKey;
	}

   for (int i=0; i<itemTextArray.size();i++){
	   ItemTextIfc text = itemTextArray.get(i);
	   List<AnswerIfc> answers = text.getAnswerArraySorted();
	   for (int j=0; j<answers.size();j++){
		   AnswerIfc a = answers.get(j);
		   if (!this.getPartialCreditFlag() && (Boolean.TRUE).equals(a.getIsCorrect())){
			   if((!this.getTypeId().equals(TypeD.MATCHING))&&(!this.getTypeId().equals(TypeD.IMAGEMAP_QUESTION)))
			   {
				   if(this.getTypeId().equals(TypeD.TRUE_FALSE))
				   {
					   answerKey=a.getText();
				   }
				   else
				   {
					   if(("").equals(answerKey))
					   {
						   answerKey=a.getLabel();
					   }
					   else
					   {
						   answerKey+=","+a.getLabel();
					   }
				   }
			   }
		   }
		   //multiple choice partial credit:
		   if (this.getTypeId().equals(TypeD.MULTIPLE_CHOICE) && this.getPartialCreditFlag()){
			   Double pc =  Double.valueOf(a.getPartialCredit());
			   if (pc == null) {
				   pc = Double.valueOf(0d);
			   }
			   if(pc > 0){
				   String correct = rb.getString("correct");
				   if(("").equals(answerKey)){
					   answerKey = a.getLabel() + "&nbsp;<span style='color: green'>(" + pc + "%&nbsp;" + correct + ")</span>";
				   }else{
					   answerKey += ",&nbsp;" + a.getLabel() + "&nbsp;<span style='color: green'>(" + pc + "%&nbsp;" + correct + ")</span>";
				   }
			   }
		   }
	   }
   }

   return answerKey;
  }

  public int compareTo(ItemDataIfc o) {
      return sequence.compareTo(o.getSequence());
  }

  public boolean getGeneralItemFbIsNotEmpty(){
     return isNotEmpty(getGeneralItemFeedback());
  }

  public boolean getCorrectItemFbIsNotEmpty(){
     return isNotEmpty(getCorrectItemFeedback());
  }

  public boolean getIncorrectItemFbIsNotEmpty(){
    return isNotEmpty(getInCorrectItemFeedback());
  }

  
 public boolean isNotEmpty(String wyzText){
    
   if(wyzText!=null){
      int index=0;
      String t=(wyzText.replaceAll("(?i)<(?!img|/img).*?>", " ")).trim();
      while(index<t.length()){ 
        char c=t.charAt(index);
        if(Character.isLetterOrDigit(c)){
	  return true; 
	}
	index++;
      }
   }
   return false;
  }

  public Set<ItemAttachmentIfc> getItemAttachmentSet() {
    return itemAttachmentSet;
  }

  public void setItemAttachmentSet(Set<ItemAttachmentIfc> itemAttachmentSet) {
    this.itemAttachmentSet = itemAttachmentSet;
  }

  public List<ItemAttachmentIfc> getItemAttachmentList() {
    if ( this.itemAttachmentSet == null || this.itemAttachmentSet.isEmpty() ) {
      return new ArrayList<>();
    }
    return new ArrayList<>(this.itemAttachmentSet);
  }

  public Map<Long, ItemAttachmentIfc> getItemAttachmentMap() {
    final Map<Long, ItemAttachmentIfc> map = new HashMap<>();
    if ( this.itemAttachmentSet == null || this.itemAttachmentSet.isEmpty() ) {
      return map;
    }
    for (ItemAttachmentIfc a : this.itemAttachmentSet) {
      map.put(a.getAttachmentId(), a);
    }
    return map;
  }

  public void addItemAttachment(ItemAttachmentIfc attachment) {
    if ( attachment == null ) {
      return;
    }
    if ( this.itemAttachmentSet == null ) {
      this.itemAttachmentSet = new HashSet<>();
    }
    attachment.setItem(this);
    this.itemAttachmentSet.add(attachment);
  }

  public void removeItemAttachmentById(Long attachmentId) {
    if ( attachmentId == null ) {
      return;
    }
    if ( this.itemAttachmentSet == null || this.itemAttachmentSet.isEmpty() ) {
      return;
    }
    Iterator i = this.itemAttachmentSet.iterator();
    while ( i.hasNext() ) {
      final ItemAttachmentIfc a = (ItemAttachmentIfc)i.next();
      if ( attachmentId.equals(a.getAttachmentId()) ) {
        i.remove();
        a.setItem(null);
      }
    }
  }

  public void removeItemAttachment(ItemAttachmentIfc attachment) {
    if ( attachment == null ) {
      return;
    }
    attachment.setItem(null);
    if ( this.itemAttachmentSet == null || this.itemAttachmentSet.isEmpty() ) {
      return;
    }
    this.itemAttachmentSet.remove(attachment);
  }

  public Boolean getPartialCreditFlag() {
	  if (partialCreditFlag == null) {
		  return Boolean.FALSE;
	  }
	  return partialCreditFlag;
  }

  public void setPartialCreditFlag(Boolean particalCreditFlag) {
	  this.partialCreditFlag = particalCreditFlag;	
  }
  
  public String getLeadInText() {
	if (leadInText == null) {
		setThemeAndLeadInText();
	}
	return leadInText;
  }

  public String getThemeText() {
	if (themeText == null) {
		setThemeAndLeadInText();
	}
	return themeText;
  }

  private void setThemeAndLeadInText() {
	if (TypeD.EXTENDED_MATCHING_ITEMS.equals(getTypeId())) {
		boolean themeTextIsSet = false, leadInTextIsSet = false;
		if(itemTextSet == null){
			return;
		}
		Iterator<ItemTextIfc> iter = itemTextSet.iterator();
		while (iter.hasNext()) {
			ItemTextIfc itemText= (ItemTextIfc) iter.next();
			if (itemText.getSequence().equals(ItemTextIfc.EMI_THEME_TEXT_SEQUENCE)) {
				themeText = itemText.getText();
				themeTextIsSet = true;
			}
			if (itemText.getSequence().equals(ItemTextIfc.EMI_LEAD_IN_TEXT_SEQUENCE)) {
				leadInText = itemText.getText();
				leadInTextIsSet = true;
			}
			
			if (themeTextIsSet && leadInTextIsSet) {
				return;
			}
		}
	}
  }
  
  // total number of correct EMI answers
	public int getNumberOfCorrectEmiOptions() {
		int count=0;
		if (itemTextSet == null){
			return count;
		}
		Iterator<ItemTextIfc> itemTextIter = itemTextSet.iterator();
		while (itemTextIter.hasNext()) {
			ItemTextIfc itemText = itemTextIter.next();
			if (!itemText.isEmiQuestionItemText()) continue;
			Iterator<AnswerIfc> answerIter = itemText.getAnswerSet().iterator();
			while (answerIter.hasNext()) {
				AnswerIfc answer = answerIter.next();
				if (answer.getIsCorrect()) count++;
			}
		}
		return count;
	}
	
	// available option labels for EMI answers
	public String getEmiAnswerOptionLabels() {
		String emiAnswerOptionLabels = null;
		if (TypeD.EXTENDED_MATCHING_ITEMS.equals(getTypeId())) {
			if (getIsAnswerOptionsSimple()) {
				emiAnswerOptionLabels = "";
				if (getEmiAnswerOptions() != null) {
					Iterator<AnswerIfc> iter = getEmiAnswerOptions().iterator();
					while (iter.hasNext()) {
						AnswerIfc answer = iter.next();
						emiAnswerOptionLabels += answer.getLabel();
					}
				}
			}
			else { // Rich
				emiAnswerOptionLabels = ItemDataIfc.ANSWER_OPTION_LABELS.substring(0, getAnswerOptionsRichCount().intValue());
			}
		}
		return emiAnswerOptionLabels;
	}
	
	public boolean isValidEmiAnswerOptionLabel(String label) {
		if (label == null) return false;
		String validOptionLabels = getEmiAnswerOptionLabels();
		if (label.length()==1 && validOptionLabels.contains(label)) {
			return true;
		}
		return false;
	}

	  public List<AnswerIfc> getEmiAnswerOptions() {
		  if (!typeId.equals(TypeD.EXTENDED_MATCHING_ITEMS)) return null;
		  ItemTextIfc itemText = getItemTextBySequence(ItemTextIfc.EMI_ANSWER_OPTIONS_SEQUENCE);  
		  if (itemText != null) {
			    return itemText.getAnswerArraySorted();
		  }
		  return null;
	  }
	  
	  public List<ItemTextIfc> getEmiQuestionAnswerCombinations() {
		  if (!typeId.equals(TypeD.EXTENDED_MATCHING_ITEMS)) return null;
		  Iterator<ItemTextIfc> iter = getItemTextArraySorted().iterator();
		  ArrayList<ItemTextIfc> emiQuestionAnswerCombinations = new ArrayList<ItemTextIfc>();
		  while (iter.hasNext()) {
			  ItemTextIfc itemText = iter.next();
			  if (itemText.isEmiQuestionItemText()) {
				  emiQuestionAnswerCombinations.add(itemText);
			  }
		  }
		  return emiQuestionAnswerCombinations;
	  }
	  
	  public ItemTextIfc getItemTextBySequence(Long itemTextSequence) {
		  ItemTextIfc itemText = null;  
		  if(itemTextSet == null){
			  return null;
		  }
		  Iterator<ItemTextIfc> itemTextIter = itemTextSet.iterator();
		  while (itemTextIter.hasNext()) {
			  itemText = itemTextIter.next();
			  if (itemText.getSequence().equals(itemTextSequence)) {
				  return itemText;
			  }
		  }
		  return null;
	  }

	public Integer getAnswerOptionsRichCount() {
		return answerOptionsRichCount;
	}

	public void setAnswerOptionsRichCount(Integer answerOptionsRichCount) {
		this.answerOptionsRichCount = answerOptionsRichCount;
	}	  
	  
	public Integer getAnswerOptionsSimpleOrRich() {
		if (answerOptionsSimpleOrRich==null) {
			answerOptionsSimpleOrRich = ItemDataIfc.ANSWER_OPTIONS_SIMPLE;
		}
		return answerOptionsSimpleOrRich;
	}

	public void setAnswerOptionsSimpleOrRich(Integer answerOptionsSimpleOrRich) {
		this.answerOptionsSimpleOrRich = answerOptionsSimpleOrRich;
	}
	  
  public String getEmiAnswerOptionsRichText() {
	  if (!typeId.equals(TypeD.EXTENDED_MATCHING_ITEMS)) return null;
	  ItemTextIfc itemText = getItemTextBySequence(ItemTextIfc.EMI_ANSWER_OPTIONS_SEQUENCE);  
	  if (itemText != null) {
		    return itemText.getText();
	  }
	  return null;
  }
  
  public boolean getIsAnswerOptionsSimple() {
	  return this.getAnswerOptionsSimpleOrRich().equals(ItemDataIfc.ANSWER_OPTIONS_SIMPLE);
  }

  public boolean getIsAnswerOptionsRich() {
	  return this.getAnswerOptionsSimpleOrRich().equals(ItemDataIfc.ANSWER_OPTIONS_RICH);
  }

  public String[] getRowChoices(){

	  List<ItemTextIfc> itemTextArray = getItemTextArraySorted();

	  List<String> stringList = new ArrayList<String>();

	  for(int i=0; i<itemTextArray.size();i++) {
		  String str = itemTextArray.get(i).getText();
		  if(str!= null && str.length() > 0) {
			  stringList.add(str);
		  }
	  }

	  String [] rowChoices = stringList.toArray(new String[stringList.size()]);

	  return rowChoices;	 
  }
  
  public List<Integer> getColumnIndexList() {

	  List<Integer> columnIndexList = new ArrayList<Integer>();
	  List<ItemTextIfc> itemTextArray = getItemTextArraySorted();
	  List<AnswerIfc> answerArray = itemTextArray.get(0).getAnswerArraySorted();  
	  List<String> stringList = new ArrayList<String>();

	  for(int i=0; i<answerArray.size();i++) {
		  String str = answerArray.get(i).getText();
		  if(str!= null && str.length() > 0) {
			  stringList.add(str);
		  }
	  }
	  for (int k=0; k< stringList.size(); k++){
		  columnIndexList.add(Integer.valueOf(k));
	  }
	  return columnIndexList;
  }

  public String[] getColumnChoices() {
	  List<ItemTextIfc> itemTextArray = getItemTextArraySorted();
	  List<AnswerIfc> answerArray = itemTextArray.get(0).getAnswerArraySorted();   
	  List<String> stringList = new ArrayList<String>();

	  for(int i=0; i<answerArray.size();i++) {
		  String str = ((AnswerIfc) answerArray.get(i)).getText();
		  if(str!= null && str.length() > 0) {
			  stringList.add(str);
		  }
	  }
	  String [] columnChoices = stringList.toArray(new String[stringList.size()]);

	  return columnChoices;

  }

  public boolean getAddCommentFlag(){
	  if (getItemMetaDataByLabel(ItemMetaDataIfc.ADD_COMMENT_MATRIX) != null)
		  return Boolean.parseBoolean(getItemMetaDataByLabel(ItemMetaDataIfc.ADD_COMMENT_MATRIX));
	  return false;
  }

  public String getCommentField(){
	  if (getItemMetaDataByLabel(ItemMetaDataIfc.ADD_COMMENT_MATRIX) != null && getItemMetaDataByLabel(ItemMetaDataIfc.ADD_COMMENT_MATRIX).equalsIgnoreCase("true"))	
		  return (String)(getItemMetaDataByLabel(ItemMetaDataIfc.MX_SURVEY_QUESTION_COMMENTFIELD));
	  return null; 
  }

  public String getRelativeWidthStyle() {
	  String width = (String)(getItemMetaDataByLabel(ItemMetaDataIfc.MX_SURVEY_RELATIVE_WIDTH));
	  if (width != null && Integer.valueOf(width) != 0)
		  return "width:" + width + "%";
	  else
		  return "";
  }
  public String getImageMapSrc() {
	  return getItemMetaDataByLabel(ItemMetaDataIfc.IMAGE_MAP_SRC);	  
  }

  public Double getMinScore() {
         return minScore;
 }
	  	 
  public void setMinScore(Double minScore) {
        this.minScore = minScore;
  }

  private String convertTagListToJsonString(Set<ItemTagIfc> itemTagSet) {

    String tagsListToJson = "[";
    if (itemTagSet != null) {
      Iterator<ItemTagIfc> i = itemTagSet.iterator();
      Boolean more = false;
      while (i.hasNext()) {
        if (more) {
          tagsListToJson += ",";
        }
        ItemTagIfc tagToShow = (ItemTagIfc) i.next();
        String tagId = tagToShow.getTagId();
        String tagLabel = tagToShow.getTagLabel();
        String tagCollectionName = tagToShow.getTagCollectionName();
        tagsListToJson += "{\"tagId\":\"" + tagId + "\",\"tagLabel\":\"" + tagLabel + "\",\"tagCollectionName\":\"" + tagCollectionName + "\"}";
        more = true;
      }
    }
    tagsListToJson += "]";
    return tagsListToJson;
  }

  public String getTagListToJsonString() {
    return this.tagListToJsonString;
  }

  public void setTagListToJsonString(String tagListToJsonString) {
    this.tagListToJsonString = tagListToJsonString;
  }
}
