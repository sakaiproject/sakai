/**
 * $Id$
 * $URL$
 * FieldnameNotFoundException.java - genericdao - Apr 27, 2008 2:47:36 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.util.reflect.exception;

/**
 * Indicates that the fieldname could not be found
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class FieldnameNotFoundException extends RuntimeException {

   public String fieldName;

   public FieldnameNotFoundException(String fieldName) {
      this(fieldName, null);
   }

   public FieldnameNotFoundException(String fieldName, Throwable cause) {
      this("Could not find fieldName ("+fieldName+") on object", fieldName, cause);
   }

   public FieldnameNotFoundException(String message, String fieldName, Throwable cause) {
      super(message, cause);
      this.fieldName = fieldName;
   }

}
