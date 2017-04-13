/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.search.api;

import java.util.List;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.search.model.SearchBuilderItem;

/**
 * A SearchIndexBuilder builds a search index, it must manage its own list of
 * pending documents and should probably do this in a separate thread
 * 
 * @author ieb
 */
public interface SearchIndexBuilder
{
	// (these defaults have to go here so they end up in a shared classloader and can be referenced from field factory
	// beans in components.xml)
	String DEFAULT_INDEX_BUILDER_NAME = "default";
	String DEFAULT_INDEX_NAME = "sakai_index";

	/**
	 * Access the logical, well-known name of this index builder. This name may be
	 * distinct from the phyisical name of the index this builder manages, and
	 * may be cached by {@link SearchService} as a key for index-specific search
	 * queries.
	 *
	 * @return
	 */
	String getName();

	/**
	 * Adds a resource to the index builder
	 * 
	 * @param notification
	 * @param event
	 */
	void addResource(Notification notification, Event event);

	/**
	 * Register an implementation-specific indexable content producer instance with this
	 * {@code SearchIndexBuilder}.
	 * 
	 * @param ecp
	 */
	void registerEntityContentProducer(EntityContentProducer ecp);

	/**
	 * Refresh the index based on the registered entities
	 */
	void refreshIndex();

	/**
	 * rebuild the index completely from scratch
	 */
	void rebuildIndex();

	/**
	 * Does the Queue contain work to do.
	 * 
	 * @return
	 */
	boolean isBuildQueueEmpty();

	/**
	 * Get all the producers registered, as a clone to avoid concurrent
	 * modification exceptions
	 * 
	 * @return
	 */
	List<EntityContentProducer> getContentProducers();

	/**
	 * Close down the entire search infrastructure
	 */
	void destroy();

	/**
	 * get the number of pending documents
	 * 
	 * @return
	 */
	int getPendingDocuments();

	/**
	 * get a list of all entitied in the search index
	 * @return
	 */
	List<SearchBuilderItem> getAllSearchItems();

	/**
	 * get an entity content producer that can handle the event
	 * @param event
	 * @return
	 */
	EntityContentProducer newEntityContentProducer(Event event);

	/**
	 * get an entity content producer that can handle the reference
	 * @param ref
	 * @return
	 */
	EntityContentProducer newEntityContentProducer(String ref);

	/**
	 * get a list of global search items
	 * @return
	 */
	List<SearchBuilderItem> getGlobalMasterSearchItems();

}
