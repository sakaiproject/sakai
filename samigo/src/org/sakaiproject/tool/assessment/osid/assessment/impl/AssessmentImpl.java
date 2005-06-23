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

public class AssessmentImpl implements Assessment {
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