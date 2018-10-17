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

package org.sakaiproject.tool.assessment.data.dao.assessment;

import java.io.*;
import java.util.*;

import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;

public class PublishedItemText
    implements Serializable, ItemTextIfc, Comparable<ItemTextIfc> {

  private static final long serialVersionUID = 7526471155622776147L;
  static ResourceBundle rb =
      ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.Messages");

  private Long id;
  private ItemDataIfc item;
  private Long sequence;
  private String text;
  private Set answerSet;

  private Set<ItemTextAttachmentIfc> itemTextAttachmentSet;
  private Integer requiredOptionsCount;

  public PublishedItemText() {}

  public PublishedItemText(PublishedItemData item, Long sequence, String text, Set answerSet) {
    this.item = item;
    this.sequence = sequence;
    this.text = text;
    this.answerSet = answerSet;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ItemDataIfc getItem() {
    return item;
  }

  public void setItem(ItemDataIfc item) {
    this.item = item;
  }

  public Long getSequence() {
    return sequence;
  }

  public void setSequence(Long sequence) {
    this.sequence = sequence;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Set getAnswerSet() {
    return answerSet;
  }

  public void setAnswerSet(Set answerSet) {
    this.answerSet = answerSet;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
  }

  public ArrayList getAnswerArray() {
    ArrayList list = new ArrayList();
    Iterator iter = answerSet.iterator();
    while (iter.hasNext()){
      list.add(iter.next());
    }
    return list;
  }

  private boolean hasDistractors(){
    List thisItemElements = getItem().getItemTextArray();
    return thisItemElements.size() != answerSet.size();
  }

  private boolean hasCorrectAnswers(){
    for (Object thisAnswer : answerSet) {
      if (((PublishedAnswer) thisAnswer).getIsCorrect()) {
        return true;
      }
    }
    return false;
  }

  public ArrayList getAnswerArraySorted() {
    ArrayList list = getAnswerArray();
    Collections.sort(list);
    return list;
  }

  /**
   * This is used for displaying the enumerated answers, with a distractor option if necessary.
   * If the question has a distractor, it should be presented once and only once, at the end of the list of choices. Ex:
   * A. Option 1
   * B. Option 2
   * C. None of the above
   * @return
   */
  public List<AnswerIfc> getAnswerArrayWithDistractorSorted() {
    List<AnswerIfc> answers = getAnswerArraySorted();
    List<ItemTextIfc> questions = item.getItemTextArray();

    // If the number of questions differs from the number of answers, there's either distractors or questions with the same answer
    if (questions.size() != answers.size()) {
      for (ItemTextIfc question : questions) {

        boolean isDistractor = true;
        List<AnswerIfc> answersSorted = question.getAnswerArraySorted();
        for (AnswerIfc answer : answersSorted) {
          if (answer.getIsCorrect()) {
            isDistractor = false;
            break;
          }
        }

        // There's at least one distractor; add the distractor answer to the end of the list for presentation purposes
        if (isDistractor) {
          Answer distractor = new Answer();
          distractor.setId(new Long(0));
          distractor.setLabel(rb.getString("choice_labels").split(":")[answersSorted.size()]);
          distractor.setText(NONE_OF_THE_ABOVE);
          distractor.setIsCorrect(false);
          distractor.setScore(this.getItem().getScore());
          distractor.setSequence(new Long(answers.size()));
          answers.add(distractor);
          break;
        }
      }
    }

    return answers;
  }

  public int compareTo(ItemTextIfc o) {
      return sequence.compareTo(o.getSequence());
  }
  
	public Set getItemTextAttachmentSet() {
		return itemTextAttachmentSet;
	}

	public List getItemTextAttachmentList() {
		return new ArrayList(itemTextAttachmentSet);
	}

	public void setItemTextAttachmentSet(Set itemTextAttachmentSet) {
		this.itemTextAttachmentSet = itemTextAttachmentSet;
	}

    public Map<Long, ItemTextAttachmentIfc> getItemTextAttachmentMap() {
        final Map<Long, ItemTextAttachmentIfc> map = new HashMap<>();
        if ( this.itemTextAttachmentSet == null || this.itemTextAttachmentSet.isEmpty() ) {
            return map;
        }
        for (ItemTextAttachmentIfc a : this.itemTextAttachmentSet) {
            map.put(a.getAttachmentId(), a);
        }
        return map;
    }

    public void addItemTextAttachment(ItemTextAttachmentIfc attachment) {
        if ( attachment == null ) {
            return;
        }
        if ( this.itemTextAttachmentSet == null ) {
            this.itemTextAttachmentSet = new HashSet<>();
        }
        attachment.setItemText(this);
        this.itemTextAttachmentSet.add(attachment);
    }


    public void addNewItemTextAttachment(ItemTextAttachmentIfc attachment) {
        if ( attachment == null ) {
            return;
        }
        if ( this.itemTextAttachmentSet == null ) {
            this.itemTextAttachmentSet = new HashSet<>();
        }
        Long attachmentId = attachment.getAttachmentId();
        if (attachmentId != null){
            //We need to recreate the list again because it is deleted with every edition,
            //so we need to clear the id, and remove the previous attachments
            //in this way, hibernate will insert the new ones and not try to update.
            attachment.setAttachmentId(null);
            removeItemTextAttachmentById(attachmentId);
        }
        attachment.setItemText(this);
        this.itemTextAttachmentSet.add(attachment);
    }


    public void removeItemTextAttachmentById(Long attachmentId) {
        if ( attachmentId == null ) {
            return;
        }
        if ( this.itemTextAttachmentSet == null || this.itemTextAttachmentSet.isEmpty() ) {
            return;
        }
        Iterator i = this.itemTextAttachmentSet.iterator();
        while ( i.hasNext() ) {
            final ItemTextAttachmentIfc a = (ItemTextAttachmentIfc)i.next();
            if ( attachmentId.equals(a.getAttachmentId()) ) {
                i.remove();
                a.setItemText(null);
            }
        }
    }

    public void removeItemTextAttachment(ItemTextAttachmentIfc attachment) {
        if ( attachment == null ) {
            return;
        }
        attachment.setItemText(null);
        if ( this.itemTextAttachmentSet == null || this.itemTextAttachmentSet.isEmpty() ) {
            return;
        }
        this.itemTextAttachmentSet.remove(attachment);
    }




	  // for EMI - Attachments at Answer Level
	  public boolean getHasAttachment(){
	    if (itemTextAttachmentSet != null && itemTextAttachmentSet.size() >0)
	      return true;
	    else
	      return false;    
	  }
	
	  //This is an actual EMI Question Item 
	  //(i.e. not Theme or Lead In Text or the complete Answer Options list) 
	  public boolean isEmiQuestionItemText() {
		  return getSequence() > 0;
	  }

		public Integer getRequiredOptionsCount() {
			if (requiredOptionsCount == null) {
				return Integer.valueOf(1);
			}
			return requiredOptionsCount;
		}

		public void setRequiredOptionsCount(Integer requiredOptionsCount) {
			this.requiredOptionsCount = requiredOptionsCount;
		}

		public String getEmiCorrectOptionLabels() {
			if (!item.getTypeId().equals(TypeD.EXTENDED_MATCHING_ITEMS)) return null;
			if (!this.isEmiQuestionItemText()) return null;
			if (answerSet==null) return null;
			String correctOptionLabels = "";
			Iterator iter = getAnswerArraySorted().iterator();
			while (iter.hasNext()) {
				AnswerIfc answer = (AnswerIfc)iter.next();
				if (answer.getIsCorrect()) {
					correctOptionLabels += answer.getLabel();
				}
			}
			return correctOptionLabels;	
		}
}
