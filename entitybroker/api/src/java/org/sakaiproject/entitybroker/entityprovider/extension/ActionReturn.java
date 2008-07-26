/**
 * $Id$
 * $URL$
 * ActionReturn.java - entity-broker - Jul 25, 2008 4:19:15 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.io.OutputStream;
import java.util.List;


/**
 * A special object used to return specialized results from a custom action execution,
 * includes fields to allow for handling of encoded binary data and to indicate
 * that entity action processing should continue as it would have if there
 * had been no custom action call (rather than exiting the standard chain)
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ActionReturn {

   /**
    * The encoding to use for the output when it is returned
    */
   public String encoding = Formats.UTF_8;
   /**
    * The MIME type to use for the output when it is returned
    */
   public String mimeType = Formats.XML_MIME_TYPE;
   /**
    * the data to output, can be binary, leave this null if not used
    */
   public OutputStream output;
   /**
    * the output data in string form, leave this null if not used
    */
   public String outputString;
   /**
    * An entity object to return, leave as null if not used
    */
   public Object entityData;
   /**
    * A List of entities to return, leave as null if not used
    */
   public List<?> entitiesList;
   /**
    * A flag to indicate that entity processing should continue after the
    * action has executed, note that output will be lost if this happens
    */
   public boolean continueProcessing = false;

   /**
    * Create a return that is appropriate for sending binary data or a large chunk of text
    * @param encoding
    * @param mimeType
    * @param output the stream of binary data
    */
   public ActionReturn(String encoding, String mimeType, OutputStream output) {
      this.encoding = encoding;
      this.mimeType = mimeType;
      this.output = output;
   }

   /**
    * Create a return that is appropriate for sending back a string
    * @param encoding
    * @param mimeType
    * @param outputString the string value
    */
   public ActionReturn(String encoding, String mimeType, String outputString) {
      this.encoding = encoding;
      this.mimeType = mimeType;
      this.outputString = outputString;
   }

   /**
    * Create a return that is appropriate for sending back an entity or a list of entities
    * @param entityData an entity or List of entities
    * @param continueProcessing true to continue normal processing
    */
   public ActionReturn(Object entityData, boolean continueProcessing) {
      this.entityData = entityData;
      this.continueProcessing = continueProcessing;
   }

   public ActionReturn(List<?> entitiesList, boolean continueProcessing) {
      this.entitiesList = entitiesList;
      this.continueProcessing = continueProcessing;
   }

}
