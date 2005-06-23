/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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

  public SectionImpl() {
  }

  public Id getId() {
    return id;
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
      item.setSequence(new Integer(i));
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