/**
 * $Id: TagSearchable.java 59674 2009-04-03 23:05:58Z arwhyte@umich.edu $
 * $URL: https://source.sakaiproject.org/svn/entitybroker/trunk/api/src/java/org/sakaiproject/entitybroker/entityprovider/capabilities/TagSearchable.java $
 * TagSearchable.java - entity-broker - Mar 21, 2008 9:39:17 PM - azeckoski
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

import java.util.List;

import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.SearchContent;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;

/**
 * Indicates an entity provider has the capability of allowing the entities associated with it to be searchable,
 * entities which implement this will be linked to the core search functionality <br/>
 * 
 * If you need to force an entity or entities to be added to the search indexes then you should use the 
 * {@link EntityBroker#add(String, org.sakaiproject.entitybroker.entityprovider.extension.SearchContent)} 
 * method in the system core to cause immediate indexing
 * 
 * <br/> This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface Searchable extends EntityProvider {

    /**
     * Will be called to retrieve the data for this item when
     * the system thinks the data related to an entity may have changed
     * 
     * @param reference a globally unique reference to an entity (e.g. /myprefix/myid), 
     * consists of the entity prefix and optional segments (normally the id at least)
     * @return the search content which should be indexed OR null to cause nothing to happen
     * @throws EntityNotFoundException if it does not exist (causes the search system to purge out the entry)
     */
    SearchContent getData(String reference);

    /**
     * Retrieve all entities for a given context
     * 
     * @param context probably a site or group but this may also be null to indicate that the entire list of all entity refs should be sent
     * @return the list of all entity references (e.g. /myprefix/myid)
     */
    List<String> getAllRefs(String context);

}
