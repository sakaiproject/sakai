/**
 * $Id$
 * $URL$
 * EntityEncodingException.java - entity-broker - Apr 30, 2008 5:33:26 PM - azeckoski
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
 * Throw to indicate that there was a failure during formatting an entity (input or output),
 * use the message to indicate more information about the failure,
 * place the reference and format into the exception
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class FormatUnsupportedException extends RuntimeException {

   /**
    * The entity reference (may be /prefix or /prefix/id) which caused this failure
    */
   public String entityReference;
   /**
    * The format which could not be handled
    */
   public String format;
   
   public FormatUnsupportedException(String message, String entityReference, String format) {
      super(message);
      this.entityReference = entityReference;
      this.format = format;
   }

   public FormatUnsupportedException(String message, Throwable cause, String entityReference, String format) {
      super(message, cause);
      this.entityReference = entityReference;
      this.format = format;
   }

}