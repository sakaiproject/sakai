/**
 * $Id$
 * $URL$
 * HTMLdefineable.java - entity-broker - Apr 6, 2008 7:44:11 PM - azeckoski
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

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityEncodingException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;

/**
 * Allows this entity to define the output data format for a reference
 * or a list of entity objects depending on the format requested,
 * if you just want to use the internal methods to handle formatting the output
 * then simply use {@link Outputable}<br/>
 * NOTE: throwing {@link FormatUnsupportedException} will pass control over to the internal
 * handlers for formatting, if you want to stop the request for this format type entirely then
 * throw an {@link IllegalStateException} and the processing will be halted
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface OutputFormattable extends Outputable {

   /**
    * Formats the entity or collection included or referred to by this entity ref object
    * into output according to the format string provided,
    * Should take into account the reference when determining what the entities are
    * and how to encode them <br/>
    * <b>NOTE:</b> be careful to correctly handle the list of entities which are meant
    * to be encoded, note that the {@link EntityData} objects include meta data and
    * the data they contain can be of any object type (though all data will come from 
    * your provider so the types should not be surprising)
    * 
    * @param ref the parsed reference object which uniquely represents this entity
    * @param format a string constant indicating the extension format (from {@link Formats}) 
    * for output, (example: {@link #XML})
    * @param entities (optional) a list of entity data objects to create formatted output for,
    * if this is null then the entities should be retrieved based on the reference,
    * if this contains only a single item AND the ref refers to a single entity
    * then the entity data object should be extracted from the list and encoded without the indication
    * that it is a collection, for all other cases the encoding should include an indication that
    * this is a list of entities
    * @param params (optional) incoming set of parameters which may be used to send data specific to this request, may be null
    * @param output the output stream to place the formatted data in,
    * should be UTF-8 encoded if there is char data
    * @throws FormatUnsupportedException if you do not handle this format type (passes control to the internal handlers)
    * @throws EntityEncodingException if you cannot format the entity data for some reason
    * @throws IllegalArgumentException if any of the arguments are invalid
    * @throws IllegalStateException for all other failures
    */
   public void formatOutput(EntityReference ref, String format, List<EntityData> entities, Map<String, Object> params, OutputStream output);

}
