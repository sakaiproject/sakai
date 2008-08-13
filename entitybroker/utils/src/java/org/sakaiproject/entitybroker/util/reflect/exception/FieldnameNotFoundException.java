/**
 * $Id$
 * $URL$
 * FieldnameNotFoundException.java - genericdao - Apr 27, 2008 2:47:36 PM - azeckoski
 **************************************************************************
 * Copyright 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
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
