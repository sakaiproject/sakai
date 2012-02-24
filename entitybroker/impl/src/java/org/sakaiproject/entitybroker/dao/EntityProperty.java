/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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
 **/

package org.sakaiproject.entitybroker.dao;

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
