/**
 * $Id$
 * $URL$
 * HTMLdefineable.java - entity-broker - Apr 6, 2008 7:44:11 PM - azeckoski
 **************************************************************************
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;

/**
 * Allows this entity to define the output data format for a reference
 * or a list of entity objects depending on the format requested,
 * if you just want to use the internal methods to handle formatting the output
 * then simply use {@link Outputable}
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface OutputFormattable extends Outputable {

   /**
    * Formats the entity or collection included or referred to by this entity ref object
    * into output according to the format string provided,
    * Should take into account the reference when determining what the entities are
    * and how to encode them
    * 
    * @param ref the parsed reference object which uniquely represents this entity
    * @param format a string constant indicating the extension format (from {@link Formats}) 
    * for output, (example: {@link #XML})
    * @param entities (optional) a list of entities to create formatted output for,
    * if this is null then the entities should be retrieved based on the reference,
    * if this contains only a single item AND the ref refers to a single entity
    * then the entity should be extracted from the list and encoded without the indication
    * that it is a collection, for all other cases the encoding should include an indication that
    * this is a list of entities
    * @param params (optional) incoming set of parameters which may be used to send data specific to this request, may be null
    * @param output the output stream to place the formatted data in,
    * should be UTF-8 encoded if there is char data
    */
   public void formatOutput(EntityReference ref, String format, List<?> entities, Map<String, Object> params, OutputStream output);

}
