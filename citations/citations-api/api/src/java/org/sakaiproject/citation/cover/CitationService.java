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

package org.sakaiproject.citation.cover;

import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * 
 */
public class CitationService 
{
	public static final String REF_TYPE_EXPORT_RIS = org.sakaiproject.citation.api.CitationService.REF_TYPE_EXPORT_RIS;
	public static final String REF_TYPE_VIEW_LIST = org.sakaiproject.citation.api.CitationService.REF_TYPE_VIEW_LIST;
	public static final String CITATION_LIST_ID = org.sakaiproject.citation.api.CitationService.CITATION_LIST_ID;
	
	private static org.sakaiproject.citation.api.CitationService m_instance;

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
    
}
