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

import org.osid.assessment.Assessment;
import org.osid.assessment.AssessmentException;
import org.osid.shared.Id;
import org.osid.shared.Type;
import org.osid.shared.TypeIterator;
import org.osid.shared.PropertiesIterator;
import org.osid.shared.Properties;
import org.osid.assessment.Section;
import org.osid.assessment.SectionIterator;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.ArrayList;

public class AssessmentImpl implements Serializable, Assessment {
	private static final long serialVersionUID = 7526471155622776147L;
	private Id id;

	private String displayName;
	private String description;
	private String topic;
	private Serializable data;
	private Type assessmentType;
	private List sectionList;

  public AssessmentImpl() {
  }

  public Id getId(){
    return this.id;
  }
  
  public void setId(Id id) {
	this.id = id;
  }


  public String getDisplayName(){
    return this.displayName;
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

  public String getTopic(){
    return this.topic;
  }

  public void updateTopic(String topic){
    setTopic(topic);
  }

  private void setTopic(String topic){
    this.topic = topic;
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

  public Type getAssessmentType(){
    return this.assessmentType;
  }

  public void updateAssessmentType(Type assessmentType){
    setAssessmentType(assessmentType);
  }

  private void setAssessmentType(Type assessmentType){
    this.assessmentType = assessmentType;
  }

  public void addSection(Section section) {
    this.sectionList.add(section);
  }
  public void removeSection(Id parm1){
  }

  public SectionIterator getSections() {
    return new SectionIteratorImpl(sectionList);
  }

  public void orderSections(Section[] sectionArray){
    ArrayList newSectionList = new ArrayList();
    for (int i=0;i < sectionArray.length; i++){
      Section section = sectionArray[i];
      newSectionList.add(section);
    }
    setSectionList(newSectionList);
  }

  private void setSectionList(List sectionList){
    this.sectionList = sectionList;
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

  /**
   * implements Serializable
   * @param out
   * @throws IOException
   */
  private void writeObject(ObjectOutputStream out)
      throws IOException{
    out.defaultWriteObject();
  }

  /**
   * implements Serializable
   * @param in
   * @throws IOException
   */
  private void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException{
    in.defaultReadObject();
  }

}
