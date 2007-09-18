/**
 * EntityProperty.java - created by aaronz on Jun 20, 2007
 */

package org.sakaiproject.entitybroker.dao.model;

/**
 * This is the persistent object for storing entity properties
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityProperty {

   private Long id;
   private String entityRef;
   private String entityPrefix;
   private String propertyName;
   private String propertyValue;

   public EntityProperty() {
   }

   public EntityProperty(String entityRef, String entityPrefix, String propertyName,
         String propertyValue) {
      this.entityRef = entityRef;
      this.entityPrefix = entityPrefix;
      this.propertyName = propertyName;
      this.propertyValue = propertyValue;
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getEntityPrefix() {
      return entityPrefix;
   }

   public void setEntityPrefix(String entityPrefix) {
      this.entityPrefix = entityPrefix;
   }

   public String getEntityRef() {
      return entityRef;
   }

   public void setEntityRef(String entityRef) {
      this.entityRef = entityRef;
   }

   public String getPropertyName() {
      return propertyName;
   }

   public void setPropertyName(String propertyName) {
      this.propertyName = propertyName;
   }

   public String getPropertyValue() {
      return propertyValue;
   }

   public void setPropertyValue(String propertyValue) {
      this.propertyValue = propertyValue;
   }

}
