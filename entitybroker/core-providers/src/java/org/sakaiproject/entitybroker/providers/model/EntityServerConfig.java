/**
 * $Id$
 * $URL$
 * EntityServerConfig.java - entity-broker - Jul 17, 2008 12:14:38 PM - azeckoski
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
 */

package org.sakaiproject.entitybroker.providers.model;

import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;


/**
 * This entity represents a Sakai server configuration object
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityServerConfig {

   @EntityId
   private String name;
   private Object value;
   private String type = "java.lang.String"; // default

   public EntityServerConfig() {}
   
   public EntityServerConfig(String name, Object value) {
      this.name = name;
      this.value = value;
   }

   public EntityServerConfig(String name, Object value, String type) {
      this(name, value);
      this.type = type;
   }

   @EntityId
   public String getName() {
      return name;
   }
   
   public void setName(String name) {
      this.name = name;
   }
   
   public Object getValue() {
      return value;
   }
   
   public void setValue(Object value) {
      this.value = value;
   }
   
   public String getType() {
      return type;
   }
   
   public void setType(String type) {
      this.type = type;
   }

}
