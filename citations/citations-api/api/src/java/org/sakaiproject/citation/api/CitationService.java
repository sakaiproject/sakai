/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.citation.api;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.osid.repository.Asset;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
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
	
	public static final String REF_TYPE_EXPORT_RIS_SEL = "export_ris_sel";
	public static final String REF_TYPE_EXPORT_RIS_ALL = "export_ris_all";
	
	public static final String RIS_FORMAT = "RIS";

	public static final String UNKNOWN_TYPE = "unknown";
	
	public static final String REF_TYPE_VIEW_LIST = "list";
	
	public static final int DEFAULT_PAGE_SIZE = 10;

	/** Property name on a Citation that will cause getUrl() and getRefernce() to return an alternal root reference. */
	public static final String PROP_ALTERNATE_REFERENCE = "sakai:reference-root";
	
	public static final String CITATION_LIST_ID = "org.sakaiproject.citation.impl.CitationList";
	public static final String HELPER_ID = "sakai.citation.tool";
	
	public static final String PROP_TEMPORARY_CITATION_LIST = "citations.temporary_citation_list";

	/** Property for long open introduction (user settable). [String] */
	public static final String PROP_INTRODUCTION = "CITATIONS:introduction";

	/**
	 * Checks permissions to add a CitationList.  Returns true if the user 
	 * has permission to add a resource in the collection identified by the
	 * parameter.
	 * @param contentCollectionId
	 * @return
	 */
	public boolean allowAddCitationList(String contentCollectionId);
	
	/**
	 * Checks permission to revise a CitationList, including permissions 
	 * to add, remove or revise citations within the CitationList. Returns
	 * true if the user has permission to revise the resource identified by
	 * the parameter.  Also returns true if all of these conditions are met:
	 * (1) the user is the creator of the specified resource, (2) the specified
	 * resource is a temporary CitationList (as identified by the value of
	 * the PROP_TEMPORARY_CITATION_LIST property), and (3) the user has 
	 * permission to add resources in the collection containing the 
	 * resource.
	 * @param contentResourceId
	 * @return
	 */
	public boolean allowReviseCitationList(String contentResourceId);
	
	/**
	 * 
	 * @return
	 */
	public boolean allowRemoveCitationList(String contentResourceId);

	/**
	 * 
	 * @param mediatype The Scheme type to create.
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
     * Creates a duplicate of the citation.
     * @param citation The citation to copy.
     */
    public Citation copyCitation(Citation citation);
    
	/**
	 * This method copies a collection and all the citations it contains.
	 * @param reference The reference of the content resource to copy
	 */
	public void copyCitationCollection(Reference reference);

	/**
     * 
     * @param collection
     */
    public void save(CitationCollection collection);

    /**
     * Attempt to find a Citation from a request.
     * This basically uses OpenURL to look for details of a citation in the request.
     * @param request The servlet request.
     * @return The returned citation which been saved or added to a citation collection,
     * if no citation is found in the request then <code>null</code> is returned.
     */
    public Citation addCitation(HttpServletRequest request);
	/**
	 ** This method save a CitationCollectionOrder representing a h1 section
	 * @param citationCollectionOrder
	 */
	public void saveSection(CitationCollectionOrder citationCollectionOrder);
	/**
	 ** This method save a CitationCollectionOrder representing a h2 or h3 section
	 * @param citationCollectionOrder
	 */
	public void saveSubsection(CitationCollectionOrder citationCollectionOrder);
	/**
	 ** This method saves CitationCollectionOrders representing a nested structure of h1 h2 h3 and citations
	 * @param citationCollectionOrders
	 * @param citationCollectionId
	 */
	public void save(List<CitationCollectionOrder> citationCollectionOrders, String citationCollectionId);
	/**
	 ** This method updates the value of a section
	 * @param citationCollectionOrder
	 */
	public void updateSection(CitationCollectionOrder citationCollectionOrder);
	/**
	 ** Gets the nested sections of a citation collection
	 * @param citationCollectionId
	 */
	public CitationCollectionOrder getNestedCollection(String citationCollectionId);
	/**
	 ** Removes the section of a citation collection and its children or removes a citation
	 * @param collectionId
	 * @param locationId
	 */
	public void removeLocation(String collectionId, int locationId);
	/**
	 ** Gets a citation collection including nested and unnested citations
	 * @param citationCollectionId
	 */
	public CitationCollection getUnnestedCitationCollection(String citationCollectionId);

	/**
	 ** Gets a citation collection including nested and unnested citations
	 * @param citationCollectionId
	 */
	public List<CitationCollectionOrder> getNestedCollectionAsList(String citationCollectionId);

	/**
	 ** Gets next id in citation collection order table for a collection
	 * @param collectionId
	 */
	public String getNextCitationCollectionOrderId(String collectionId);
	/**
	 ** Gets a citation collection order
	 * @param id
	 * @param locationId
	 */
	public CitationCollectionOrder getCitationCollectionOrder(String id, int locationId);

}	// interface CitationService

