/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * 
 * DropboxContextObserver
 *
 */
@Slf4j
public class DropboxContextObserver implements EntityProducer, ContextObserver
{
	/** This string starts the references to resources in this service. */
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "dropbox";
	
	/**
	 * Dependency: The content service.
	 */
	protected BaseContentService m_contentService = null;

	/**
     * @return the contentService
     */
    public BaseContentService getContentService()
    {
    	return m_contentService;
    }

	/**
     * @param contentService the contentService to set
     */
    public void setContentService(BaseContentService contentService)
    {
    	this.m_contentService = contentService;
    }
    
	/** Dependency: EntityManager. */
	protected EntityManager m_entityManager = null;

	/**
	 * Dependency: EntityManager.
	 * 
	 * @param service
	 *        The EntityManager.
	 */
	public void setEntityManager(EntityManager service)
	{
		m_entityManager = service;
	}
	
	public String archive(String siteId, Document doc, Stack stack, String archivePath,
	        List attachments)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Entity getEntity(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityDescription(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityUrl(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public HttpAccess getHttpAccess()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String merge(String siteId, Element root, String archivePath, String fromSiteId,
	        Map attachmentNames, Map userIdTrans, Set userListAllowImport)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean parseEntityReference(String reference, Reference ref)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean willArchiveMerge()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void contextCreated(String context, boolean toolPlacement)
	{
		if(toolPlacement)
		{
			m_contentService.enableDropbox(context);
		}
	}

	public void contextDeleted(String context, boolean toolPlacement)
	{
		if(toolPlacement)
		{
			m_contentService.disableDropbox(context);
		}
	}

	public void contextUpdated(String context, boolean toolPlacement)
	{
		if(toolPlacement)
		{
			m_contentService.enableDropbox(context);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] myToolIds()
	{
		String[] toolIds = { "sakai.dropbox" };
		return toolIds;
	}


	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		log.info("init()");

		// register as an entity producer
		m_entityManager.registerEntityProducer(this, REFERENCE_ROOT);
	}
	
	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		log.info("destroy()");
	}

}
