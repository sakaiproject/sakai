/**
 * $Id$
 * $URL$
 * EncodingException.java - entity-broker - Apr 30, 2008 5:33:26 PM - azeckoski
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
 * Throw to indicate that there was a failure during encoding an entity or related data
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EncodingException extends RuntimeException {

   String entityReference;

   public EncodingException(String message, String entityReference) {
      super(message);
      this.entityReference = entityReference;
   }

   public EncodingException(String message, String entityReference, Throwable cause) {
      super(message, cause);
      this.entityReference = entityReference;
   }

}
