package org.sakaiproject.tool.assessment.osid.assessment.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.log4j.Category;
import org.osid.assessment.AssessmentException;
import org.osid.shared.Id;
import org.osid.shared.Properties;
import org.osid.shared.PropertiesIterator;
import org.osid.shared.Type;
import org.osid.shared.TypeIterator;

public class ItemImpl implements Serializable, org.osid.assessment.Item {
  static Category errorLogger = Category.getInstance("errorLogger");

  private static final long serialVersionUID = 7526471155622776147L;

  private Id id;
  private String displayName;
  private String description;
  private Serializable data;
  private Type itemType;

  public ItemImpl(){}

  public Id getId(){
    return this.id;
  }

  public Type getItemType(){
    return this.itemType;
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

  public Serializable getData(){
    return this.data;
  }

  public void updateData(Serializable data){
    setData(data);
  }

  private void setData(Serializable data){
    this.data = data;
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