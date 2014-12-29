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

package org.sakaiproject.citation.cover;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * 
 */
public class CitationService 
{
//	public static final String REF_TYPE_EXPORT_RIS = org.sakaiproject.citation.api.CitationService.REF_TYPE_EXPORT_RIS;
	public static final String REF_TYPE_EXPORT_RIS_SEL = org.sakaiproject.citation.api.CitationService.REF_TYPE_EXPORT_RIS_SEL;
	public static final String REF_TYPE_EXPORT_RIS_ALL = org.sakaiproject.citation.api.CitationService.REF_TYPE_EXPORT_RIS_ALL;
	public static final String REF_TYPE_VIEW_LIST = org.sakaiproject.citation.api.CitationService.REF_TYPE_VIEW_LIST;
	public static final String CITATION_LIST_ID = org.sakaiproject.citation.api.CitationService.CITATION_LIST_ID;
	public static final String PROP_TEMPORARY_CITATION_LIST = org.sakaiproject.citation.api.CitationService.PROP_TEMPORARY_CITATION_LIST;
	
	private static org.sakaiproject.citation.api.CitationService m_instance;

	/**
	 * Checks permissions to add a CitationList.  Returns true if the user 
	 * has permission to add a resource in the collection identified by the
	 * parameter.
	 * @param contentCollectionId
	 * @return
	 */
	public static boolean allowAddCitationList(String contentCollectionId)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return false;
		}
		return instance.allowAddCitationList(contentCollectionId);
	}
	
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
	public static boolean allowReviseCitationList(String contentResourceId)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return false;
		}
		return instance.allowReviseCitationList(contentResourceId);
	}
	
	/**
	 * 
	 * @return
	 */
	public static boolean allowRemoveCitationList(String contentResourceId)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return false;
		}
		return instance.allowRemoveCitationList(contentResourceId);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.citation.api.CitationService#newCitation(java.lang.String)
	 */
	public static org.sakaiproject.citation.api.Citation addCitation(java.lang.String mediatype)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.addCitation(mediatype);
	}


	public static org.sakaiproject.citation.api.CitationCollection addCollection()
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.addCollection();
	}

	public static org.sakaiproject.citation.api.CitationCollection getCollection(java.lang.String collectionId) throws org.sakaiproject.exception.IdUnusedException
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getCollection(collectionId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.citation.api.CitationService#getDefaultSchema()
	 */
	public static org.sakaiproject.citation.api.Schema getDefaultSchema()
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getDefaultSchema();		
	}

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.citation.api.CitationService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.citation.api.CitationService) ComponentManager
						.get(org.sakaiproject.citation.api.CitationService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.citation.api.CitationService) ComponentManager
					.get(org.sakaiproject.citation.api.CitationService.class);
		}
	}

	public static org.sakaiproject.citation.api.Schema getSchema(java.lang.String name)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getSchema(name);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.citation.api.CitationService#getSchemas()
	 */
	public static java.util.List getSchemas()
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getSchemas();
	}

	public static org.sakaiproject.citation.api.Citation getTemporaryCitation()
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getTemporaryCitation();		
	}
	
	public static org.sakaiproject.citation.api.Citation getTemporaryCitation(org.osid.repository.Asset asset)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getTemporaryCitation(asset);		
	}

	/**
     * @return
     */
    public static org.sakaiproject.citation.api.CitationCollection getTemporaryCollection()
    {
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getTemporaryCollection();
    }

	/**
     * @return
     */
    public static java.util.Set getValidPropertyNames()
    {
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
	    return instance.getValidPropertyNames();
    }
    
    /**
     * @param mediatype
     * @param name
     * @return
     */
    public static boolean isMultivalued(String schemaId, String fieldId)
    {
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return false;
		}
		return instance.isMultivalued(schemaId, fieldId);
    }
    
    /* (non-Javadoc)
	 * @see org.sakaiproject.citation.api.CitationService#listSchemas()
	 */
	public static java.util.List listSchemas()
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.listSchemas();
	}


	public static void save(org.sakaiproject.citation.api.Citation citation)
    {
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return;
		}
		instance.save(citation);
    }


	public static void save(org.sakaiproject.citation.api.CitationCollection collection)
    {
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return;
		}
		instance.save(collection);
    }


	public static CitationCollection copyAll(String citationCollectionId) 
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.copyAll(citationCollectionId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.citation.api.CitationService#newCitation(java.lang.String)
	 */
	public static org.sakaiproject.citation.api.Citation addCitation(HttpServletRequest request)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.addCitation(request);
	}


    
}
