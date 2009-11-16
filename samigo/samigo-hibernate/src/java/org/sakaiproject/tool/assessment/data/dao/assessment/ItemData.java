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
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
//import org.sakaiproject.tool.assessment.facade.TypeFacadeQueriesAPI;
//import org.sakaiproject.tool.assessment.services.PersistenceService;

public class ItemData
    implements java.io.Serializable,
    ItemDataIfc, Comparable {
  static Category errorLogger = Category.getInstance("errorLogger");

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
  private Float score;
  private Float discount;
  private String hint;
  private Boolean hasRationale;
  private Integer status;
  private Boolean partialCreditFlag;
  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;
  private Set itemTextSet;
  private Set itemMetaDataSet;
  private Set itemFeedbackSet;
  private HashMap itemMetaDataMap = new HashMap();
  private HashMap itemFeedbackMap;
  private Set itemAttachmentSet;

  public ItemData() {}

  // this constructor should be deprecated, it is missing triesAllowed
  public ItemData(SectionDataIfc section, Integer sequence,
                  Integer duration, String instruction, String description,
                  Long typeId, String grade, Float score, Float discount, String hint,
                  Boolean hasRationale, Integer status, String createdBy,
                  Date createdDate, String lastModifiedBy,
                  Date lastModifiedDate,
                  Set itemTextSet, Set itemMetaDataSet, Set itemFeedbackSet, Boolean partialCreditFlag ) {
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

  public ItemData(SectionDataIfc section, Integer sequence,
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

  public Float getScore() {
    return this.score;
  }

  public void setScore(Float score) {
    this.score = score;
  }

  public Float getDiscount() {
	  if (this.discount==null){
		  this.discount=Float.valueOf(0);
	  }
	  return this.discount;
  }

  public void setDiscount(Float discount) {
	  if (discount==null){
		  discount=Float.valueOf(0);
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
        ItemMetaData itemMetaData = (ItemMetaData) i.next();
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
        ItemFeedbackIfc itemFeedback = (ItemFeedbackIfc) i.next();
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
    Long sequence =Long.valueOf(this.itemTextSet.size()+1);
    ItemText itemText = new ItemText(this, sequence, text, answerSet);
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
    if (this.itemFeedbackMap == null)
      this.itemFeedbackMap = getItemFeedbackMap(this.itemFeedbackSet);
    return (String)this.itemFeedbackMap.get(typeId);
  }

  public void addItemFeedback(String typeId, String text) {
    if (this.itemFeedbackSet == null) {
      setItemFeedbackSet(new HashSet());
      this.itemFeedbackMap = new HashMap();
    }
    this.itemFeedbackMap.put(typeId, text);
    this.itemFeedbackSet.add(new ItemFeedback(this, typeId, text));
  }

  public void removeFeedbackByType(String typeId) {
    if (itemFeedbackSet != null) {
      for (Iterator i = this.itemFeedbackSet.iterator(); i.hasNext(); ) {
        ItemFeedback itemFeedback = (ItemFeedback) i.next();
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
      for (Iterator i = this.itemMetaDataSet.iterator(); i.hasNext(); ) {
        ItemMetaData itemMetaData= (ItemMetaData) i.next();
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
   * one element and return it; in FIB or FIN return multiple elements separated by underscores.
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
   * Added by Huong Nguyen for other types as well.
   */
  public String getAnswerKey(){
   String answerKey="";
   ArrayList itemTextArray = getItemTextArraySorted();
   if (itemTextArray.size()==0)
     return answerKey;

   ArrayList answerArray = ((ItemTextIfc)itemTextArray.get(0)).getAnswerArraySorted();
   HashMap h = new HashMap();

   for (int i=0; i<itemTextArray.size();i++){
     ItemTextIfc text = (ItemTextIfc)itemTextArray.get(i);
     ArrayList answers = text.getAnswerArraySorted();
     for (int j=0; j<answers.size();j++){
       AnswerIfc a = (AnswerIfc)answers.get(j);
       if ((Boolean.TRUE).equals(a.getIsCorrect())){
         String pair = (String)h.get(a.getLabel());
           if(!this.getTypeId().equals(TypeD.MATCHING))
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

           else{

	       if (pair==null)
		   {
		       String s = a.getLabel() + ":" + text.getSequence();
		       h.put(a.getLabel(), s);
		   }
	       else
		   {
		       h.put(a.getLabel(), pair+" "+text.getSequence());
		   }
	   }
       }
     }
   }

   if (this.getTypeId().equals(TypeD.MATCHING))
       {
	   for (int k=0; k<answerArray.size();k++)
	       {
		   AnswerIfc a = (AnswerIfc)answerArray.get(k);
		   String pair = (String)h.get(a.getLabel());
     //if answer is not a match to any text, just print answer label
		   if (pair == null)
		       pair = a.getLabel()+": ";

		   if (k!=0)
		       answerKey = answerKey+",  "+pair;
		   else

		       answerKey = pair;
	       }
       }



   return answerKey;


  }

  public int compareTo(Object o) {
      ItemData a = (ItemData)o;
      return sequence.compareTo(a.sequence);
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
      String t=(wyzText.replaceAll("<.*?>", " ")).trim();
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
	  return this.partialCreditFlag;
  }

  public void setPartialCreditFlag(Boolean particalCreditFlag) {
	  this.partialCreditFlag=particalCreditFlag;	
  }

}
