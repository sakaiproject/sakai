/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.osid.assessment.impl;

import java.io.Serializable;
import java.util.Set;

import org.osid.assessment.AssessmentException;
import org.osid.assessment.Item;
import org.osid.assessment.ItemIterator;
import org.osid.assessment.Section;
import org.osid.assessment.SectionIterator;
import org.osid.shared.Id;
import org.osid.shared.Properties;
import org.osid.shared.PropertiesIterator;
import org.osid.shared.Type;
import org.osid.shared.TypeIterator;
import org.sakaiproject.tool.assessment.facade.ItemFacade;

public class SectionImpl implements Section {
  private Id id;


private String displayName;
  private String description;
  private Serializable data;
  private Type sectionType;



private Set itemSet;

  public Set getItemSet() {
	return itemSet;
}

public void setItemSet(Set itemSet) {
	this.itemSet = itemSet;
}

public SectionImpl() {
  }

  public Id getId() {
    return id;
  }

  public void setId(Id id) {
		this.id = id;
	}

  public String getDisplayName() {
    return displayName;
  }

  public void updateDisplayName(String displayName){
    setDisplayName(displayName);
  }

  private void setDisplayName(String displayName){
    this.displayName = displayName;
  }

  public String getDescription(){
    return this.description;
  }

  public void updateDescription(String description){
    setDescription(description);
  }

  private void setDescription(String description){
    this.description = description;
  }

  public Serializable getData(){
    return this.data;
  }

  public void updateData(Serializable data){
    setData(data);
  }

  private void setData(Serializable data){
    this.data = data;
  }

  public Type getSectionType() {
    return sectionType;
  }

  public void setSectionType(Type sectionType) {
		this.sectionType = sectionType;
	}
  public PropertiesIterator getProperties() throws AssessmentException{
    throw new AssessmentException(AssessmentException.UNIMPLEMENTED);
  }

  public Properties getPropertiesByType(Type type) throws AssessmentException{
    throw new AssessmentException(AssessmentException.UNIMPLEMENTED);
  }

  public TypeIterator getPropertyTypes() throws AssessmentException{
    throw new AssessmentException(AssessmentException.UNIMPLEMENTED);
  }

  public void addItem(Item item) {
    itemSet.add(item);
  }

  public void removeItem(Id itemId) throws org.osid.assessment.
      AssessmentException
 {
    ItemIterator iter = getItems();
    while(iter.hasNextItem())
    {
      Item item = (Item) iter.nextItem();
      if(item.getId().equals(itemId))
      {
        itemSet.remove(item);
        break;
      }
    }
  }

  public ItemIterator getItems() {
    return new ItemIteratorImpl(itemSet);
  }

  public void orderItems(Item[] itemArray) {
    for(int i = 0; i < itemArray.length; i++)
    {
      ItemFacade item = (ItemFacade) itemArray[i];
      item.setSequence( Integer.valueOf(i));
    }
  }

  public void addSection(Section parm1) throws AssessmentException{
    throw new AssessmentException(AssessmentException.UNIMPLEMENTED);
  }

  public void removeSection(Id parm1) throws AssessmentException{
    throw new AssessmentException(AssessmentException.UNIMPLEMENTED);
  }


  public SectionIterator getSections() throws AssessmentException{
    throw new AssessmentException(AssessmentException.UNIMPLEMENTED);
  }


  public void orderSections(Section[] parm1) throws AssessmentException{
    throw new AssessmentException(AssessmentException.UNIMPLEMENTED);
  }

}
