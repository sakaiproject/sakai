/**
 * $Id$
 * $URL$
 * EntityEncodingException.java - entity-broker - Apr 30, 2008 5:33:26 PM - azeckoski
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

package org.sakaiproject.entitybroker.exception;


/**
 * Throw to indicate that there was a failure during formatting an entity (input or output),
 * use the message to indicate more information about the failure,
 * place the reference and format into the exception
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class FormatUnsupportedException extends EntityBrokerException {

   /**
    * The format which could not be handled
    */
   public String format;
   
   public FormatUnsupportedException(String message, String entityReference, String format) {
      super(message, entityReference);
      this.format = format;
   }

   public FormatUnsupportedException(String message, Throwable cause, String entityReference, String format) {
      super(message, entityReference, cause);
      this.format = format;
   }

}