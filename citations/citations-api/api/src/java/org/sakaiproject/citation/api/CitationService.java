/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.citation.api;

import java.util.List;
import java.util.Set;

import org.osid.repository.Asset;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.exception.IdUnusedException;

import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.Schema;

public interface CitationService extends EntityProducer
{
	/** This string can be used to find the service in the service manager. */
	public static final String SERVICE_NAME = CitationService.class.getName();
	
	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	public static final String APPLICATION_ID = "sakai:citation";
	
	/** This string starts the references to entities in this service. */
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "citation";
	
	public static final String REF_TYPE_EXPORT_RIS = "export_ris";
	
	public static final String RIS_FORMAT = "RIS";

	
	public static final String REF_TYPE_VIEW_LIST = "list";
	
	public static final int DEFAULT_PAGE_SIZE = 10;

	/** Property name on a Citation that will cause getUrl() and getRefernce() to return an alternal root reference. */
	public static final String PROP_ALTERNATE_REFERENCE = "sakai:reference-root";
	
	public static final String CITATION_LIST_ID = "org.sakaiproject.citation.impl.CitationList";
	public static final String HELPER_ID = "sakai.citation.tool";


	/**
	 * 
	 * @param listId
	 * @return
	 */
	public Citation addCitation(String mediatype);
	
	/**
	 * @return
	 */
	public CitationCollection addCollection();
	
 	/**
	 * @return
	 */
	public CitationCollection getCollection(String collectionId) throws IdUnusedException;
	
	/**
	 * @param collectionId
	 * @return
	 */
	public CitationCollection copyAll(String collectionId);
	
	/**
	 * Access the default schema
	 * @return The default schema, or null if no schema has been set as the default.
	 */
	public Schema getDefaultSchema();
	
	/**
	 * Access a schema by its name
	 * @param name The name of the schema
	 * @return The schema, or null if no schema has been defined with that name.
	 */
	public Schema getSchema(String name);
	
	/**
	 * Access a list of all schemas that have been defined
	 * @return A list of Schema objects representing the schemas that have been defined.
	 */
	public List getSchemas();
	
	/**
	 * Create a temporary citation that has not been saved in storage.  Saving it or saving a collection
	 * it's a member of will convert it to a permanent citation and save it in storage.
	 * @return
	 */
	public Citation getTemporaryCitation();
	
	/**
	 * Create a temporary citation to represent an asset.  The new citation will not have been saved in 
	 * storage yet.  Saving it or saving a collection it's a member of will convert it to a permanent 
	 * citation and save it in storage.
     * @param asset
     * @return
     */
    public Citation getTemporaryCitation(Asset asset);
    
	/**
     * @return
     */
    public CitationCollection getTemporaryCollection();

	/**
     * @return
     */
    public Set getValidPropertyNames();

	/**
     * @param schemaId
     * @param fieldId
     * @return
     */
    public boolean isMultivalued(String schemaId, String fieldId);
    
    /**
	 * Access a list of identifiers for all schemas that have been defined
	 * @return A list of Strings representing the names of schemas that have been defined.
	 */
	public List listSchemas();
    
    /**
     * This method permanently removes a CitationCollection and all Citations it contains.
	 * @param edit The CitationCollection to remove.
	 */
	public void removeCollection(CitationCollection edit);

	/**
     * 
     * @param citation
     */
    public void save(Citation citation);

	/**
     * 
     * @param collection
     */
    public void save(CitationCollection collection);

	/**
	 * @return The label that should be used for the open url.
	 */
	public String getOpenUrlLabel();


}	// interface CitationService

