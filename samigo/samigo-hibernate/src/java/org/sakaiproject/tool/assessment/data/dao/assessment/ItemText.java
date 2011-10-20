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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Category;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;

public class ItemText
    implements Serializable, ItemTextIfc, Comparable {
  static Category errorLogger = Category.getInstance("errorLogger");

  private static final long serialVersionUID = 7526471155622776147L;

  private Long id;
  private ItemDataIfc item;
  private Long sequence;
  private String text;
  private Set answerSet;

  public ItemText() {}

  public ItemText(ItemData item, Long sequence, String text, Set answerSet) {
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

  public int compareTo(Object o) {
      ItemText a = (ItemText)o;
      return sequence.compareTo(a.sequence);
  }

  public ArrayList getAnswerArraySorted() {
    ArrayList list = getAnswerArray();
    Collections.sort(list);
    return list;
  }

}
