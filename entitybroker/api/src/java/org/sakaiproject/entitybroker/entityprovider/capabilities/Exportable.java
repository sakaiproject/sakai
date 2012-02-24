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

import java.io.OutputStream;
import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

/**
 * Indicates an entity provider has the capability of exporting entity data which is related to
 * other entities, note that the decision about which data to export is left up to the implementor
 * based on the reference supplied <br/> This is one of the capability extensions for the
 * {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface Exportable extends EntityProvider {

   /**
    * Request an export stream of data from an entity provider for all data related to a specific
    * entity (this will probably not be an entity in this provider), search, and parameters<br/>
    * This is primarily to support the use case archiving and exporting data from a system which related
    * to a user or a site/group<br/>
    * 
    * @param reference
    *           a globally unique reference to an entity, this is the entity that the exported data
    *           should be associated with (e.g. a reference to a site object or user)
    * @param search (optional) a search which should be used to limit the data which is exported, may be null
    * @param data a stream to put the export data into which will be saved by the archiver/exporter
    * @param destructive if false then the data being exported is not changed, 
    * if true then the data should be deleted or hidden (depending on the internal operation of the entity) 
    * @param params (optional) incoming set of parameters which may be used to send data specific to this request, may be null
    * @return a string key representing the encoding used and possibly other info like a version, this
    *         allows the export to provide tips to the import when data is streamed back in, if
    *         there is no data to export then a null will be returned
    */
   public String exportData(String reference, Search search, OutputStream data, boolean destructive, Map<String, Object> params);

}
