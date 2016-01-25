package org.sakaiproject.cmprovider.data;

/**
 * Interface for all course management data.
 *
 * Data classes are used to convert json to and from classes in org.sakaiproject.coursemanagement.api.
 * 
 * public Object getSampleEntity() should return a class that implements CmEntityData.
 *
 * @author Christopher Schauer
 */
public interface CmEntityData {
  public String getId();  
}
