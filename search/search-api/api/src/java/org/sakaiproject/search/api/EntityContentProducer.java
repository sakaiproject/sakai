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
 *		 http://www.opensource.org/licenses/ECL-2.0
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

import org.sakaiproject.entity.api.Entity;
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
	default boolean isContentFromReader(String reference) {
		return false;
	}

	/**
	 * Get a reader for the supplied content resource
	 * 
	 * @param reference 
	 * @return
	 */
	default Reader getContentReader(String reference) {
		return null;
	}

	/**
	 * Get the content as a string
	 * @see SearchUtils#appendCleanString(String, StringBuilder)
	 * @param reference
	 * @return
	 */
	default String getContent(String reference) {
		return "";
	}
	

	/**
	 * get the title for the content
	 * 
	 * @param reference
	 * @return
	 */
	default String getTitle(String reference) {
		return "";
	}

	/**
	 * Gets the url that displays the entity
	 * 
	 * @param reference
	 * @return
	 */
	default String getUrl(String reference) {
		return "";
	}

	/**
	 * Gets the url that displays the entity. You can specify a UrlType depending on
	 * what kind of link you want.
	 *
	 * @param reference
	 * @param EntityUrlType
	 * @return The url as a String
	 */
	default String getUrl(String reference, Entity.UrlType urlType) {
		return this.getUrl(reference);
	}

	/**
	 * If the reference matches this EntityContentProducer return true
	 * 
	 * @param reference
	 * @return
	 */
	default boolean matches(String reference) {
		return false;
	}

	/**
	 * Get the search builder action associated with the event.
	 * @see SearchBuilderItem
	 * @param event
	 * @return One of the {@link SearchBuilderItem} constants.
	 */
	default Integer getAction(Event event) {
		return SearchBuilderItem.ACTION_UNKNOWN;
	}

	/**
	 * Is the event owned by this EntityContentProducer
	 * @param event
	 * @return
	 */
	default boolean matches(Event event) {
		return false;
	}

	/**
	 * What is the name of the tool, 
	 * @return
	 */
	default String getTool() {
		return "";
	}
	
	/**
	 * get the site ID from the resource Name
	 *
	 * @param reference
	 * @return the site ID or {@code null} if a site ID is not present or this provider does not manage site-specific
	 *	 content
	 */
	default String getSiteId(String reference) {
		return "";
	}

	/**
	 * Get the display name of the resource creator
	 *
	 * @param reference
	 * @return the display name or "" if this provider does not support it
	 */
	default String getCreatorDisplayName(String reference) {
		return "";
	}

	/**
	 * Get the user id of the resource creator
	 *
	 * @param reference
	 * @return the user id or "" if this provider does not support it
	 */
	default String getCreatorId(String reference) {
		return "";
	}

	/**
	 * Get the user name of the resource creator
	 *
	 * @param reference
	 * @return the user name or "" if this provider does not support it
	 */
	default String getCreatorUserName(String reference) {
		return "";
	}

	/**
	 * Get the site content as an iterator
	 * @param context
	 * @return an iterator over all content associted with the given site ID, or an empty iterator if this provider
	 *	 does not manage site-specific content
	 */
	default Iterator<String> getSiteContentIterator(String context) {
		return null;
	}

	/**
	 * If the reference should be indexed, return true
	 * @param reference
	 * @return
	 */
	default boolean isForIndex(String reference) {
		return false;
	}

	/**
	 * returns true if the current user can view the search result
	 * @param reference 
	 * @return
	 */
	default boolean canRead(String reference) {
		return false;
	}

	/**
	 * Gets a map of custom document properties. The names of the map map will contain 
	 * the index name to which the value is added.
	 * The value is expected to be a String or String[], containing the value, values to be
	 * added. Before using this method in your entity producer, be certain that the value
	 * is not already in the index. ( See SearchService for list of Fields)
	 * If the key starts with a "T" then the index will be tokenized and the T removed to form the index name
	 * @return
	 */
	default Map<String, ?> getCustomProperties(String ref) {
		return null;
	}

	/**
	 * At the moment this is a placeholder, but eventually
	 * It will return a block of Custom RDF, that the EntityContentProducer wants
	 * the search index to index. This is ontop of any RDF that the search index is 
	 * already processing. 
	 * @return
	 */
	default String getCustomRDF(String ref) {
		return "";
	}

	default String getId(String ref) {
		return "";
	}

	default String getType(String ref) {
		return "";
	}

	default String getSubType(String ref) {
		return "";
	}

	default String getContainer(String ref) {
		return "";
	}
}
