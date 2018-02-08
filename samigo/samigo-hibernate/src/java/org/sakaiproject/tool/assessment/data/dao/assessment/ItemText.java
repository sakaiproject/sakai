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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;

public class ItemText
    implements Serializable, ItemTextIfc, Comparable<ItemTextIfc> {
  static Logger errorLogger = LoggerFactory.getLogger("errorLogger");

  private static final long serialVersionUID = 7526471155622776147L;

  private Long id;
  private ItemDataIfc item;
  private Long sequence;
  private String text;
  private Set<AnswerIfc> answerSet;

  private Set<ItemTextAttachmentIfc> itemTextAttachmentSet;
  private Integer requiredOptionsCount;
  
  public ItemText() {}

  public ItemText(ItemData item, Long sequence, String text, Set<AnswerIfc> answerSet) {
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

  public Set<AnswerIfc> getAnswerSet() {
    return answerSet;
  }

  public void setAnswerSet(Set<AnswerIfc> answerSet) {
    this.answerSet = answerSet;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
  }

  public List<AnswerIfc> getAnswerArray() {
    List<AnswerIfc> list = new ArrayList<AnswerIfc>();
    if (answerSet != null) {
      list.addAll(answerSet);
    }
    return list;
  }

  public int compareTo(ItemTextIfc o) {
      return sequence.compareTo(o.getSequence());
  }

  public List<AnswerIfc> getAnswerArraySorted() {
    List<AnswerIfc> list = getAnswerArray();
    Collections.sort(list);
    return list;
  }
  
	public Set<ItemTextAttachmentIfc> getItemTextAttachmentSet() {
		return itemTextAttachmentSet;
	}

	public List<ItemTextAttachmentIfc> getItemTextAttachmentList() {
		return new ArrayList<ItemTextAttachmentIfc>(itemTextAttachmentSet);
	}

	public void setItemTextAttachmentSet(Set<ItemTextAttachmentIfc> itemTextAttachmentSet) {
		this.itemTextAttachmentSet = itemTextAttachmentSet;
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
		Iterator<AnswerIfc> iter = getAnswerArraySorted().iterator();
		while (iter.hasNext()) {
			AnswerIfc answer = iter.next();
			if (answer.getIsCorrect()) {
				correctOptionLabels += answer.getLabel();
			}
		}
		return correctOptionLabels;	
	}
	
	public String toString(){
		return getText();
	}
}
