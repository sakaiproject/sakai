/**
 * $Id$
 * $URL$
 * EntityExistsException.java - entity-broker - Apr 6, 2008 8:52:59 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.exception;


/**
 * This exception is used to indicate that an entity could not be found (it does not exist)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityExistsException extends RuntimeException {

   /**
    * The unique reference for the entity or entity space related to this failure
    */
   public String entityReference;

   /**
    * Create an exception to indicate that this entity could not found,
    * this will trigger an HTTP NOT FOUND error if not caught before reaching the direct servlet
    * 
    * @param message
    * @param entityReference the unique reference to an entity
    */
   public EntityExistsException(String message, String entityReference) {
      super(message);
      this.entityReference = entityReference;
   }

   public EntityExistsException(Throwable cause, String entityReference) {
      super(cause);
      this.entityReference = entityReference;
   }

   public EntityExistsException(String message, Throwable cause, String entityReference) {
      super(message, cause);
      this.entityReference = entityReference;
   }

}
