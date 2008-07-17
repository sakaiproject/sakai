/**
 * $Id$
 * $URL$
 * EntityServerConfig.java - entity-broker - Jul 17, 2008 12:14:38 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.providers;

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
