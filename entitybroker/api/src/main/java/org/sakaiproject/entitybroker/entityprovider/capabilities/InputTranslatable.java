/**
 * $Id$
 * $URL$
 * InputTranslatable.java - entity-broker - Apr 12, 2008 2:03:28 PM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.io.InputStream;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityEncodingException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;

/**
 * Allows this entity to define the way to translate data for a reference
 * into an entity object depending on the format requested,
 * if you just want to use the internal methods to handle formatting the input
 * into an entity then simply use {@link Inputable}<br/>
 * NOTE: throwing {@link FormatUnsupportedException} will pass control over to the internal
 * handlers for formatting, if you want to stop the request for this format type entirely then
 * throw an {@link IllegalStateException} and the processing will be halted
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface InputTranslatable extends Inputable {

   /**
    * Translates the input data stream in the supplied format into an entity object for this reference
    * 
    * @param ref the parsed reference object which uniquely represents this entity
    * @param format a string constant indicating the extension format (from {@link Formats}) 
    * of the input, (example: {@link #XML})
    * @param input an stream which contains the data to make up this entity,
    * you may assume this is UTF-8 encoded if you don't know anything else about it
    * @param params (optional) incoming set of parameters which may be used to send data specific to this request, may be null
    * @return an entity object of the type used for these entities
    * @throws FormatUnsupportedException if you do not handle this format type (passes control to the internal handlers)
    * @throws EntityEncodingException if you cannot encode the received data into an entity
    * @throws IllegalArgumentException if any of the arguments are invalid
    * @throws IllegalStateException for all other failures
    */
   public Object translateFormattedData(EntityReference ref, String format, InputStream input, Map<String, Object> params);

}
