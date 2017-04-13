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

import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.model.SearchBuilderItem;

/**
 * This is a special class than handles ContentResources for the purposes of
 * search. This must be implemented in a thread safe way. The aim is to map the
 * content handler to the mime type
 * 
 * @author ieb
 */
public interface EntityContentProducer
{

	/**
	 * Should the consumer use the reader or is it OK to use a memory copy of
	 * the content
	 * 
	 * @param reference
	 * @return
	 */
	boolean isContentFromReader(String reference);

	/**
	 * Get a reader for the supplied content resource
	 * 
	 * @param reference 
	 * @return
	 */
	Reader getContentReader(String reference);

	/**
	 * Get the content as a string
	 * @see SearchUtils#appendCleanString(String, StringBuilder)
	 * @param reference
	 * @return
	 */
	String getContent(String reference);
	

	/**
	 * get the title for the content
	 * 
	 * @param reference
	 * @return
	 */
	String getTitle(String reference);

	/**
	 * Gets the url that displays the entity
	 * 
	 * @param reference
	 * @return
	 */
	String getUrl(String reference);

	/**
	 * If the reference matches this EntityContentProducer return true
	 * 
	 * @param reference
	 * @return
	 */
	boolean matches(String reference);

	/**
	 * Get the search builder action associated with the event.
	 * @see SearchBuilderItem
	 * @param event
	 * @return One of the {@link SearchBuilderItem} constants.
	 */
	Integer getAction(Event event);

	/**
	 * Is the event owned by this EntityContentProducer
	 * @param event
	 * @return
	 */
	boolean matches(Event event);

	/**
	 * What is the name of the tool, 
	 * @return
	 */
	String getTool();


	
	/**
	 * get the site ID from the resource Name
	 *
	 * @param reference
	 * @return the site ID or {@code null} if a site ID is not present or this provider does not manage site-specific
	 *   content
	 */
	String getSiteId(String reference);

	/**
	 * Get the site content as an iterator
	 * @param context
	 * @return an iterator over all content associted with the given site ID, or an empty iterator if this provider
	 *   does not manage site-specific content
	 */
	Iterator<String> getSiteContentIterator(String context);

	/**
	 * If the reference should be indexed, return true
	 * @param reference
	 * @return
	 */
	boolean isForIndex(String reference);

	/**
	 * returns true if the current user can view the search result
	 * @param reference 
	 * @return
	 */
	boolean canRead(String reference);

	/**
	 * Gets a map of custom document properties. The names of the map map will contain 
	 * the index name to which the value is added.
	 * The value is expected to be a String or String[], containing the value, values to be
	 * added. Before using this method in your entity producer, be certain that the value
	 * is not already in the index. ( See SearchService for list of Fields)
	 * If the key starts with a "T" then the index will be tokenized and the T removed to form the index name
	 * @return
	 */
	Map<String, ?> getCustomProperties(String ref);

	/**
	 * At the moment this is a placeholder, but eventually
	 * It will return a block of Custom RDF, that the EntityContentProducer wants
	 * the search index to index. This is ontop of any RDF that the search index is 
	 * already processing. 
	 * @return
	 */
	String getCustomRDF(String ref);

	/**
	 * @param ref
	 * @return
	 */
	String getId(String ref);

	/**
	 * @param ref
	 * @return
	 */
	String getType(String ref);

	/**
	 * @param ref
	 * @return
	 */
	String getSubType(String ref);

	/**
	 * @param ref
	 * @return
	 */
	String getContainer(String ref);

}
