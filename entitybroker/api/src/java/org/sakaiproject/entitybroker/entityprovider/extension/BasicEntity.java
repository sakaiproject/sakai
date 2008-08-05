/**
 * $Id$
 * $URL$
 * BasicEntity.java - entity-broker - Apr 8, 2008 9:11:35 PM - azeckoski
 **************************************************************************
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.entityprovider.extension;

import org.sakaiproject.entitybroker.EntityReference;

/**
 * BasicEntity object for when one cannot be found
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class BasicEntity {

   private String reference;
   private String prefix;
   private String id;
   private String errorMessage = "Could not resolve entity object, returning known metadata";

   public BasicEntity(EntityReference ref) {
      this.reference = ref.toString();
      this.prefix = ref.getPrefix();
      this.id = ref.getId();
   }

   @Override
   public String toString() {
      return errorMessage + ", reference=" + this.reference;
   };

   public String getReference() {
      return reference;
   }

   public void setReference(String reference) {
      this.reference = reference;
   }

   public String getPrefix() {
      return prefix;
   }

   public void setPrefix(String prefix) {
      this.prefix = prefix;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getErrorMessage() {
      return errorMessage;
   }

   public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
   }
}
