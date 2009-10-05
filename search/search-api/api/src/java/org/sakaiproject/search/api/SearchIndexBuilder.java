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
 *       http://www.osedu.org/licenses/ECL-2.0
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

	/**
	 * Adds a resource to the index builder
	 * 
	 * @param notification
	 * @param event
	 */
	void addResource(Notification notification, Event event);

	/**
	 * EntityProducers that want their content indexed on full text must
	 * register an EntityContentProducer with the SearchIndexBuilder
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
	 * get all the producers registerd, as a clone to avoid concurrent
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
	 * Rebuild the index for the supplied siteId
	 * @param currentSiteId
	 */
	void rebuildIndex(String currentSiteId);

	/**
	 * Refresh the index for the supplied siteId
	 * @param currentSiteId
	 */
	void refreshIndex(String currentSiteId);

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
	 * get an entity content procuder that can handle the reference
	 * @param ref
	 * @return
	 */
	EntityContentProducer newEntityContentProducer(String ref);

	/**
	 * get a list of Master Search Items that control the search operation for the 
	 * Site (current site)
	 * @return
	 */
	List<SearchBuilderItem> getSiteMasterSearchItems();

	/**
	 * get a list of global search items
	 * @return
	 */
	List<SearchBuilderItem> getGlobalMasterSearchItems();

	boolean isOnlyIndexSearchToolSites();
	
	boolean isExcludeUserSites();
	

}
