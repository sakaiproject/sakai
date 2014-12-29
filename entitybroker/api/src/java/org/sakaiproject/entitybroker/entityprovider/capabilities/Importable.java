/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
 **/

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.io.InputStream;
import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Indicates an entity provider has the capability of importing entity data that was previously
 * exported via the {@link Exportable} capability which will be related to other entities, note that
 * the way to associate the data is left up to the implementor based on the reference supplied <br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface Importable extends Exportable {

   /**
    * Request that an import stream be turned into real data related to the entities in this
    * provider and associated with a specific entity (this will probably not be an entity in this
    * provider)
    * 
    * @param reference
    *           a globally unique reference to an entity, this is the entity that the imported data
    *           should be associated with (e.g. a reference to a site object or user)
    * @param data
    *           a stream of data from the archiver/importer, this should match a previous export
    *           stream exactly (or at least very closely in order for the importer to be able to understand it)
    * @param encodingKey
    *           a string representing the encoding used and possibly other info like a version, this
    *           should be the string sent with the export
    * @param params (optional) incoming set of parameters which may be used to send data specific to this request, may be null
    * @return the array of all entity references which were created from the import OR empty if none created
    * @throws IllegalArgumentException if any arguments are invalid or missing
    * @throws IllegalStateException if a failure occurs with the import (message should be as descriptive as possible)
    */
   public String[] importData(String reference, InputStream data, String encodingKey, Map<String, Object> params);

}
