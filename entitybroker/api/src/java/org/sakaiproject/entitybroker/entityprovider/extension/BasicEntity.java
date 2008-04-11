/**
 * $Id$
 * $URL$
 * BasicEntity.java - entity-broker - Apr 8, 2008 9:11:35 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
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
