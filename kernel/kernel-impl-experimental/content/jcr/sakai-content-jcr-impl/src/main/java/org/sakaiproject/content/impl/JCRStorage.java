/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.OperationDelegationException;
import org.sakaiproject.content.impl.BaseContentService.BaseResourceEdit;
import org.sakaiproject.content.impl.BaseContentService.Storage;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.jcr.api.JCRRegistrationService;
import org.sakaiproject.jcr.api.JCRService;
import org.sakaiproject.jcr.api.JCRConstants;

/**
 * @author ieb
 */
public class JCRStorage implements Storage
{

	private static final Log log = LogFactory.getLog(JCRStorage.class);

	private final String CONTENTCOLLECTION_TL_CACHE = this.toString()+"_contentcollectioncache";
	private final String CONTENTRESOURCE_TL_CACHE = this.toString()+"_contentresourcecache";

	/** A storage for collections. */
	protected BaseJCRStorage m_collectionStore = null;

	/** A storage for resources. */
	protected BaseJCRStorage m_resourceStore = null;

	/** htripath- Storage for resources delete */
	protected BaseJCRStorage m_resourceDeleteStore = null;

	private ThreadLocal stackMarker = new ThreadLocal();

	private JCRContentService jcrContentService;

	private JCRService jcrService;

	private LiteStorageUser collectionUser;

	private LiteStorageUser resourceUser;

	private ContentHostingHandlerResolver resolver;

	private JCRRegistrationService jcrRegistrationService;

	private Map<String, String> namespaces;

	private List<String> nodetypeReources;

	private ThreadLocalCache collectionCache;

	private ThreadLocalCache resourceCache;

	private Map<String, String> convertableNamespaces;

	/**
	 * Construct.
	 * 
	 * @param collectionUser
	 *        The StorageUser class to call back for creation of collection
	 *        objects.
	 * @param resourceUser
	 *        The StorageUser class to call back for creation of resource
	 *        objects.
	 */
	public JCRStorage()
	{

	}

	public void init()
	{
		if ( !jcrService.isEnabled() ) {
			return;
		}

		for (String prefix : namespaces.keySet())
		{
			String url = namespaces.get(prefix);
			log.info("Registering ["+prefix+"] as ["+url+"]");
			jcrRegistrationService.registerNamespace(prefix, url);
		}
		for (String prefix : convertableNamespaces.keySet())
		{
			String url = convertableNamespaces.get(prefix);
			log.info("Registering ["+prefix+"] as ["+url+"]");
			jcrRegistrationService.registerNamespace(prefix, url);
		}
		resourceUser.setNamespaces(convertableNamespaces);
		collectionUser.setNamespaces(convertableNamespaces);
		for (String nodeTypeResource : nodetypeReources)
		{
			try
			{
				InputStream in = this.getClass().getResourceAsStream(nodeTypeResource);
				if ( in == null ) {
					log.error("Didnt Find with class.getResourceAsStream "+nodeTypeResource);
					in = this.getClass().getClassLoader().getResourceAsStream(nodeTypeResource);
				} else {
					log.debug("Loaded resource: " + nodeTypeResource);
				}
				
				jcrRegistrationService.registerNodetypes(in);
				in.close();
			}
			catch (Exception e)
			{
				log.error("Failed to read node type definitions from "+nodeTypeResource);
			}
		}

		// build the collection store - a single level store
		m_collectionStore = new BaseJCRStorage(jcrService, collectionUser,
				JCRConstants.NT_FOLDER);

		// build the resources store - a single level store
		m_resourceStore = new BaseJCRStorage(jcrService, resourceUser,
				JCRConstants.NT_FILE);

		// htripath-build the resource for store of deleted
		// record-single
		// level store
		m_resourceDeleteStore = new BaseJCRStorage(jcrService, collectionUser,
				JCRConstants.NT_FILE);

	}

	public void destroy()
	{

	}

	/**
	 * Open and be ready to read / write.
	 */
	public void open()
	{
		m_collectionStore.open();
		m_resourceStore.open();
		m_resourceDeleteStore.open();
	}

	/**
	 * Close.
	 */
	public void close()
	{
		m_collectionStore.close();
		m_resourceStore.close();
		m_resourceDeleteStore.close();
	}

	private class StackRef
	{
		protected int count = 0;
	}

	/**
	 * increase the stack counter and return true if this is the top of the
	 * stack
	 * 
	 * @return
	 */
	private boolean in()
	{
		StackRef r = (StackRef) stackMarker.get();
		if (r == null)
		{
			r = new StackRef();
			stackMarker.set(r);
		}
		r.count++;
		return r.count <= 1;// johnf@caret -- used to permit no
		// self-recurses; now permits 0 or 2
		// (r.count == 1);
	}

	private int position()
	{
		StackRef r = (StackRef) stackMarker.get();
		if (r == null)
		{
			r = new StackRef();
			stackMarker.set(r);
		}
		return r.count;
	}

	/**
	 * decrement the stack counter on the thread
	 */
	private void out()
	{
		StackRef r = (StackRef) stackMarker.get();
		if (r == null)
		{
			r = new StackRef();
			stackMarker.set(r);
		}
		r.count--;
		if (r.count < 0)
		{
			r.count = 0;
		}
	}

	/** Collections * */

	public boolean checkCollection(String id)
	{
		if (id == null || id.trim().length() == 0)
		{
			return false;
		}
		ContentCollection cc = (ContentCollection) collectionCache.get(id);
		if ( cc != null ) {
			return true;
		}
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return resolver.checkCollection(id);
			}
			else
			{
				return m_collectionStore.checkResource(id);
			}
		}
		finally
		{
			out();
		}
	}

	public ContentCollection getCollection(String id)
	{
		if (id == null || id.trim().length() == 0)
		{
			return null;
		}
		ContentCollection  cc = (ContentCollection) collectionCache.get(id);
		if ( cc != null ) {
			return cc;
		}
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				cc = resolver.getCollection(id);
				return (ContentCollection) collectionCache.put(id, cc);
			}
			else
			{
				cc = (ContentCollection) m_collectionStore
						.getResource(id);
				return (ContentCollection) collectionCache.put(id, cc);
			}
		}
		finally
		{
			out();
		}

	}


	/**
	 * Get a list of all getCollections within a collection.
	 */
	public List getCollections(ContentCollection collection)
	{
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return resolver.getCollections(collection);
			}
			else
			{
				// limit to those whose reference path (based on id)
				// matches
				// the
				// collection id
				final String target = collection.getId();

				/*
				 * // read all the records, then filter them to accept only
				 * those in this collection // Note: this is not desirable, as
				 * the read is linear to the database site -ggolden List rv =
				 * m_collectionStore.getSelectedResources( new Filter() { public
				 * boolean accept(Object o) { // o is a String, the collection
				 * id return StringUtil.referencePath((String)
				 * o).equals(target); } } );
				 */

				// read the records with a where clause to let the
				// database
				// select
				// those in this collection
				return m_collectionStore.getAllResourcesWhere("IN_COLLECTION", target);
			}
		}
		finally
		{
			out();
		}

	} // getCollections

	public ContentCollectionEdit putCollection(String id)
	{
		if (id == null || id.trim().length() == 0)
		{
			return null;
		}
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return (ContentCollectionEdit) collectionCache.put(id, resolver.putCollection(id));
			}
			else
			{
				return (ContentCollectionEdit) collectionCache.put(id,m_collectionStore.putResource(id, null));
			}
		}
		finally
		{
			out();
		}
	}

	public ContentCollectionEdit editCollection(String id)
	{
		if (id == null || id.trim().length() == 0)
		{
			return null;
		}
		try {
			ContentCollectionEdit cce = (ContentCollectionEdit) collectionCache.get(id);
			if ( cce != null ) {
				return cce;
			}
		} catch ( Exception ex ) {
			
		}
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return  (ContentCollectionEdit) collectionCache.put(id, resolver.editCollection(id));
			}
			else
			{
				return   (ContentCollectionEdit) collectionCache.put(id, m_collectionStore.editResource(id));
			}
		}
		finally
		{
			out();
		}
	}

	// protected String
	// externalResourceDeleteFileName(ContentResource resource)
	// {
	// return m_bodyPath + "/delete/" + ((BaseResourceEdit)
	// resource).m_filePath;
	// }

	// htripath -end

	public void cancelResource(ContentResourceEdit edit)
	{
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				resolver.cancelResource(edit);
			}
			else
			{
				// clear the memory image of the body
				byte[] body = ((BaseResourceEdit) edit).m_body;
				((BaseResourceEdit) edit).m_body = null;
				m_resourceStore.cancelResource(edit);

			}
			resourceCache.remove(edit.getId());
		}
		finally
		{
			out();
		}
	}



	public void commitCollection(ContentCollectionEdit edit)
	{
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				resolver.commitCollection(edit);
			}
			else
			{
				m_collectionStore.commitResource(edit);
			}
			collectionCache.remove(edit.getId());
		}
		finally
		{
			out();
		}
	}

	public void cancelCollection(ContentCollectionEdit edit)
	{
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				resolver.cancelCollection(edit);
			}
			else
			{
				m_collectionStore.cancelResource(edit);
			}
			collectionCache.remove(edit.getId());
		}
		finally
		{
			out();
		}

	}

	public void removeCollection(ContentCollectionEdit edit)
	{
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				resolver.removeCollection(edit);
			}
			else
			{
				m_collectionStore.removeResource(edit);
			}
			collectionCache.remove(edit.getId());

		}
		finally
		{
			out();
		}
	}

	/** Resources * */

	public boolean checkResource(String id)
	{
		if (id == null || id.trim().length() == 0)
		{
			return false;
		}
		ContentResource cr = (ContentResource) resourceCache.get(id);
		if ( cr != null ) {
			return true;
		}
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return resolver.checkResource(id);
			}
			else
			{
				return m_resourceStore.checkResource(id);
			}
		}
		finally
		{
			out();
		}
	}

	public ContentResource getResource(String id) throws TypeException
	{
		if (id == null || id.trim().length() == 0)
		{
			return null;
		}
		ContentResource cr = (ContentResource) resourceCache.get(id);
		if ( cr != null ) {
			return cr;
		}
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				cr = (ContentResource) resolver.getResource(id);
				return (ContentResource) resourceCache.put(id, cr);
			}
			else
			{
				Entity ce =  m_resourceStore.getResource(id);
				if ( ce != null ) {
					if ( ! (ce instanceof ContentResource) ) {
						// CHS tries to see if urls not ending in a / are resources first
						if ( log.isDebugEnabled() ) log.debug("=================RESORUCE is not a RESOURCE "+id);
					} else {
						cr = (ContentResource) ce;
					}
				} else {
				}
				return (ContentResource) resourceCache.put(id, cr);
			}
		}
		finally
		{
			out();
		}
	}

	public List getResources(ContentCollection collection)
	{
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return resolver.getResources(collection);
			}
			else
			{
				// limit to those whose reference path (based on id)
				// matches
				// the
				// collection id
				final String target = collection.getId();

				/*
				 * // read all the records, then filter them to accept only
				 * those in this collection // Note: this is not desirable, as
				 * the read is linear to the database site -ggolden List rv =
				 * m_resourceStore.getSelectedResources( new Filter() { public
				 * boolean accept(Object o) { // o is a String, the resource id
				 * return StringUtil.referencePath((String) o).equals(target); } } );
				 */

				// read the records with a where clause to let the
				// database
				// select
				// those in this collection
				return m_resourceStore.getAllResourcesWhere("IN_COLLECTION", target);
			}
		}
		finally
		{
			out();
		}

	} // getResources

	public List getFlatResources(String collectionId)
	{
		List rv = null;
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				rv = resolver.getFlatResources(collectionId);
			}
			else
			{
				rv = m_resourceStore.getAllResourcesWhereLike("IN_COLLECTION",
						collectionId + "%");
			}
			return rv;
		}
		finally
		{
			out();
		}
	}

	public ContentResourceEdit putResource(String id)
	{
		if (id == null || id.trim().length() == 0)
		{
			return null;
		}
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return (ContentResourceEdit) resourceCache.put(id,resolver.putResource(id));
			}
			else
			{
				return (ContentResourceEdit) resourceCache.put(id, m_resourceStore.putResource(id, null));
			}
		}
		finally
		{
			out();
		}
	}

	public ContentResourceEdit editResource(String id)
	{
		if (id == null || id.trim().length() == 0)
		{
			return null;
		}
		try {
			ContentResourceEdit cr = (ContentResourceEdit) resourceCache.get(id);
			if ( cr != null ) {
				return cr;
			}
		} catch ( Exception ex ) {
			
		}
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return (ContentResourceEdit) resourceCache.put(id, resolver.editResource(id));
			}
			else
			{
				return (ContentResourceEdit) resourceCache.put(id, m_resourceStore.editResource(id));
			}
		}
		finally
		{
			out();
		}
	}

	public void commitResource(ContentResourceEdit edit) throws ServerOverloadException
	{
		// keep the body out of the XML

		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				resolver.commitResource(edit);
			}
			else
			{
				BaseResourceEdit redit = (BaseResourceEdit) edit;

				if (redit.m_contentStream != null)
				{

				}
				else if (redit.m_body != null)
				{

				}
				else
				{

				}
				m_resourceStore.commitResource(edit);
			}
			resourceCache.remove(edit.getId());

		}
		finally
		{
			out();
		}
	}

	// htripath - start
	/**
	 * Add resource to content_resouce_delete table for user deleted resources
	 */
	public ContentResourceEdit putDeleteResource(String id, String uuid, String userId)
	{
		resourceCache.remove(id);
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return (ContentResourceEdit) resolver.putDeleteResource(id, uuid, userId);
			}
			else
			{
				return (ContentResourceEdit) m_resourceDeleteStore.putDeleteResource(id,
						uuid, userId, null);
			}
		}
		finally
		{
			out();
		}
	}

	/**
	 * update xml and store the body of file TODO storing of body content is not
	 * used now.
	 */
	public void commitDeleteResource(ContentResourceEdit edit, String uuid)
	{
		resourceCache.remove(edit.getId());
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				resolver.commitDeleteResource(edit, uuid);
			}
			else
			{
				m_resourceDeleteStore.commitDeleteResource(edit, uuid);
			}
		}
		finally
		{
			out();
		}

	}

	public void removeResource(ContentResourceEdit edit)
	{
		// delete the body
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				resolver.removeResource(edit);
			}
			else
			{

				m_resourceStore.removeResource(edit);

			}
			resourceCache.remove(edit.getId());

		}
		finally
		{
			out();
		}

	}

	/**
	 * Read the resource's body.
	 * 
	 * @param resource
	 *        The resource whose body is desired.
	 * @return The resources's body content as a byte array.
	 * @exception ServerOverloadException
	 *            if the server is configured to save the resource body in the
	 *            filesystem and an error occurs while accessing the server's
	 *            filesystem.
	 */
	public byte[] getResourceBody(ContentResource resource)
			throws ServerOverloadException
	{
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return resolver.getResourceBody(resource);
			}
			else
			{
				return resource.getContent();
			}
		}
		finally
		{
			out();
		}

	}

	// the body is already in the resource for this version of
	// storage
	public InputStream streamResourceBody(ContentResource resource)
			throws ServerOverloadException
	{
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return resolver.streamResourceBody(resource);
			}
			else
			{
				return resource.streamContent();
			}
		}
		finally
		{
			out();
		}
	}

	public int getMemberCount(String collectionId)
	{

		if (collectionId == null || collectionId.trim().length() == 0)
		{
			return 0;
		}

		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return resolver.getMemberCount(collectionId);
			}
			else
			{

				return m_collectionStore.getMemberCount(collectionId);
			}
		}
		finally
		{
			out();
		}
	}

	public Collection<String> getMemberCollectionIds(String collectionId)
	{
		if (collectionId == null || collectionId.trim().length() == 0)
		{
			return new ArrayList<String>();
		}
		List list = null;
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return resolver.getMemberCollectionIds(collectionId);
			}
			else
			{

				return m_collectionStore.getMemberCollectionIds(collectionId);
			}
		}
		finally
		{
			out();
		}

	}

	public Collection<String> getMemberResourceIds(String collectionId)
	{
		if (collectionId == null || collectionId.trim().length() == 0)
		{
			return new ArrayList<String>();
		}
		List list = null;
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				return resolver.getMemberResourceIds(collectionId);
			}
			else
			{

				return m_collectionStore.getMemberResourceIds(collectionId);
			}
		}
		finally
		{
			out();
		}
	}

	/**
	 * @param id
	 * @param uuid
	 */
	public void setResourceUuid(String resourceId, String uuid)
	{
		if (resourceId == null || resourceId.trim().length() == 0)
		{
			return;
		}
		List list = null;
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				try
				{
					resolver.setResourceUuid(resourceId, uuid);
				}
				catch (OperationDelegationException e)
				{
				}
			}
			m_resourceStore.setResourceUuid(resourceId, uuid);
		}
		finally
		{
			out();
		}
	}

	/**
	 * @param thisCollection
	 * @param new_folder_id
	 * @return
	 * @throws IdUnusedException
	 *         When the target folder does not exit
	 * @throws TypeException
	 *         When the target is not a folder
	 * @throws IdUsedException
	 *         When a unique target cannot be found
	 * @throws ServerOverloadException
	 *         Failed to move collection due to repository error
	 */
	public String moveCollection(ContentCollectionEdit thisCollection,
			String new_folder_id) throws IdUnusedException, TypeException,
			IdUsedException, ServerOverloadException
	{
		if (thisCollection == null || new_folder_id == null
				|| new_folder_id.trim().length() == 0)
		{
			return null;
		}
		List list = null;
		boolean goin = in();
		try
		{
			if (resolver != null && goin)
			{
				try
				{
					return resolver.moveCollection(thisCollection, new_folder_id);
				}
				catch (OperationDelegationException e)
				{
				}
			}
			return m_resourceStore.moveCollection(thisCollection, new_folder_id);
		}
		finally
		{
			out();
		}
	}

	/**
	 * @param thisResource
	 * @param new_id
	 * @return
	 * @throws ServerOverloadException
	 * @throws IdUsedException
	 * @throws TypeException
	 * @throws IdUnusedException
	 */
	public String moveResource(ContentResourceEdit thisResource, String new_id)
			throws IdUnusedException, TypeException, IdUsedException,
			ServerOverloadException
	{
		if (thisResource == null || new_id == null || new_id.trim().length() == 0)
		{
			return null;
		}
		List list = null;
		boolean goin = in();
		try
		{
			try
			{
				if (resolver != null && goin)
				{
					return resolver.moveResource(thisResource, new_id);
				}
			}
			catch (OperationDelegationException e)
			{
			}
			return m_resourceStore.moveResource(thisResource, new_id);
		}
		finally
		{
			out();
		}
	}

	/**
	 * @param id
	 * @return
	 */
	public String getUuid(String id)
	{
		if (id == null || id.trim().length() == 0)
		{
			return null;
		}
		List list = null;
		boolean goin = in();
		try
		{
			try
			{
				if (resolver != null && goin)
				{
					return resolver.getUuid(id);
				}
			}
			catch (OperationDelegationException e)
			{
			}
			return m_resourceStore.getUuid(id);
		}
		finally
		{
			out();
		}
	}

	// ===================================== getters and setters
	// =====================
	/**
	 * @return the collectionUser
	 */
	public LiteStorageUser getCollectionUser()
	{
		return collectionUser;
	}

	/**
	 * @param collectionUser
	 *        the collectionUser to set
	 */
	public void setCollectionUser(LiteStorageUser collectionUser)
	{
		this.collectionUser = collectionUser;
	}

	/**
	 * @return the jcrService
	 */
	public JCRService getJcrService()
	{
		return jcrService;
	}

	/**
	 * @param jcrService
	 *        the jcrService to set
	 */
	public void setJcrService(JCRService jcrService)
	{
		this.jcrService = jcrService;
	}

	/**
	 * @return the parentService
	 */
	public JCRContentService getJcrContentService()
	{
		return jcrContentService;
	}

	/**
	 * @param parentService
	 *        the parentService to set
	 */
	public void setJcrContentService(JCRContentService jcrContentService)
	{
		this.jcrContentService = jcrContentService;
	}

	/**
	 * @return the resourceUser
	 */
	public LiteStorageUser getResourceUser()
	{
		return resourceUser;
	}

	/**
	 * @param resourceUser
	 *        the resourceUser to set
	 */
	public void setResourceUser(LiteStorageUser resourceUser)
	{
		this.resourceUser = resourceUser;
	}

	/**
	 * @return the resolver
	 */
	public ContentHostingHandlerResolver getResolver()
	{
		return resolver;
	}

	/**
	 * @param resolver
	 *        the resolver to set
	 */
	public void setResolver(ContentHostingHandlerResolver resolver)
	{
		this.resolver = resolver;
	}

	/**
	 * @return the jcrRegistrationService
	 */
	public JCRRegistrationService getJcrRegistrationService()
	{
		return jcrRegistrationService;
	}

	/**
	 * @param jcrRegistrationService
	 *        the jcrRegistrationService to set
	 */
	public void setJcrRegistrationService(JCRRegistrationService jcrRegistrationService)
	{
		this.jcrRegistrationService = jcrRegistrationService;
	}

	/**
	 * @return the namespaces
	 */
	public Map<String, String> getNamespaces()
	{
		return namespaces;
	}

	/**
	 * @param namespaces the namespaces to set
	 */
	public void setNamespaces(Map<String, String> namespaces)
	{
		this.namespaces = namespaces;
	}

	/**
	 * @return the nodetypeReources
	 */
	public List<String> getNodetypeReources()
	{
		return nodetypeReources;
	}

	/**
	 * @param nodetypeReources the nodetypeReources to set
	 */
	public void setNodetypeReources(List<String> nodetypeReources)
	{
		this.nodetypeReources = nodetypeReources;
	}

	/**
	 * @return the collectionCache
	 */
	public ThreadLocalCache getCollectionCache()
	{
		return collectionCache;
	}

	/**
	 * @param collectionCache the collectionCache to set
	 */
	public void setCollectionCache(ThreadLocalCache collectionCache)
	{
		this.collectionCache = collectionCache;
	}

	/**
	 * @return the resourceCache
	 */
	public ThreadLocalCache getResourceCache()
	{
		return resourceCache;
	}

	/**
	 * @param resourceCache the resourceCache to set
	 */
	public void setResourceCache(ThreadLocalCache resourceCache)
	{
		this.resourceCache = resourceCache;
	}

	/**
	 * @return the convertableNamespaces
	 */
	public Map<String, String> getConvertableNamespaces()
	{
		return convertableNamespaces;
	}

	/**
	 * @param convertableNamespaces the convertableNamespaces to set
	 */
	public void setConvertableNamespaces(Map<String, String> convertableNamespaces)
	{
		this.convertableNamespaces = convertableNamespaces;
	}

	public Collection<ContentResource> getResourcesOfType(String resourceType, int pageSize, int page) 
	{
		// TODO Auto-generated method stub
		return null;
	}


}
