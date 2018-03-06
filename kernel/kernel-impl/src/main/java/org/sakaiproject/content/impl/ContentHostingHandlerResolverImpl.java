/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.sakaiproject.content.impl;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.OperationDelegationException;
import org.sakaiproject.content.impl.BaseContentService.Storage;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.util.SingleStorageUser;

/**
 * <p>
 * Implementation of the Handler Resolver. This class chains back to the storage to get local entities but then resolves the IDs through to virtual content entities based on the name ContentHostingHandlers. The primary 3 methods are getRealParent(),
 * getVirtualEntity(), and getVirtualChild(). The remaining methods are largely plumbing, proxying the Storage mechanism that is being used. The Storage mechanims must be aware that this code will cause re-entry into the Storage methods and so the Storage
 * <b>must</b> implement some sort of call stack advisor to prevent recursion.
 * </p>
 * <p>
 * The getRealParent() method takes the current Id, and finds the closest ancestor that exists in the main Storage area.
 * </p>
 * <p>
 * The getVirtualEntity() converts a ContentEntity into a ContentEntity managed by the ContentHostingHandler named in that entity. If no ContentHostingHandler is named, then the ContentEntity is returned unchanged.
 * </p>
 * <p>
 * The getVirtualChild() method takes a ContentEntity that is a parent of the target ContentEntity and tried to find the target ContentEntity by navigating to children and resolving ContentEntity via ContentHostingHandlers where appropriate. If the target
 * does not exist, null is returned.
 * </p>
 * <p>
 * To make this navigation process efficient there needs to be some form of Cache in place, ideally this would be a cluster wide cache with event based expiry.
 * </p>
 * 
 * @author ieb (initial version), johnf (substantial edits)
 */
@Slf4j
public class ContentHostingHandlerResolverImpl implements ContentHostingHandlerResolver
{
	protected SingleStorageUser resourceStorageUser;

	protected SingleStorageUser collectionStorageUser;

	private Storage storage;

	/**
	 * Find the closest real ancestor to the requested id, this recurses into itself
	 * 
	 * @param id
	 * @return the closest ancestor or null if not found (bit unlikely)
	 */
	public ContentEntity getRealParent( String id)
	{
		ContentEntity ce = storage.getCollection(id);
		if (ce == null)
		{
			try {
			ce = storage.getResource(id);
			}
			catch (TypeException e)
			{
				log.debug("Type Exception ",e);
			}

		}
		if (ce == null)
		{
			if (id.equals(Entity.SEPARATOR)) {
				// If the entity is the root and we didn't get anything, there is nothing we can do
				// no root, no content, no point in trying to get another one
				log.error("Unable to get Root node of the repository");
				throw new AssertionError("Unable to Get Root repository "+Entity.SEPARATOR);
			}
			int lastSlash = id.lastIndexOf(Entity.SEPARATOR, id.length() - 2);
			if (lastSlash > 0)
			{
				String parentId = id.substring(0, lastSlash + 1 /* ian@caret.cam wanted a "- 1" here */);
				ce = getRealParent(parentId);
			}
		}
		return ce;
	}

	/**
	 * Convert the ContentEntity into its virtual shadow via its ContentHostingHandler bean. If no bean is defined for the ContentEntity, no resolution is performed. If the ContentEntity is null, no resolution is performed.
	 * 
	 * @param ce
	 * @return a resolved ContentEntity where appropriate, otherwise the orginal
	 */
	public ContentEntity getVirtualEntity(ContentEntity ce, String finalId)
	{
		if (ce == null)
		{
			return null;
		}
		ResourceProperties p = ce.getProperties();
		String chhbeanname = p.getProperty(CHH_BEAN_NAME);

		if (chhbeanname != null && chhbeanname.length() > 0)
		{
			try
			{
				ContentHostingHandler chh = (ContentHostingHandler) ComponentManager.get(chhbeanname);
				return chh.getVirtualContentEntity(ce, finalId);
				
			}
			catch (Exception e)
			{
				// log and return null
				log.warn("Failed to find CHH Bean " + chhbeanname + " or bean failed to resolve virtual entity ID", e);
				return ce;
			}
		}
		return ce;
	}

	/**
	 * Locate the ContentEntity with the final Id, or null if can't be found, resolving virtual content entities as part of the resolution process. Will return a real content entity if that is what the finalId represents.
	 * 
	 * @param finalId
	 * @param ce
	 * @param exact -
	 *        if true, the exact match otherwise the nearest ancestor
	 * @return
	 */
	public ContentEntity getVirtualChild(String finalId, ContentEntity ce, boolean exact)
	{
		if (ce == null) 
		{
			return null;  // entirely empty resources tool
		}
		String thisid = ce.getId();
		// check for an exact match
		if (finalId.equals(thisid))
		{
			return ce;
		}
		// find the next ID in the target eg
		// /A/B/C == thisid
		// /A/B/C/D/E/F == finalId
		//         ^
		int nextSlash = finalId.indexOf(Entity.SEPARATOR, thisid.length());
		
		// /A/B/C/D/E/F == finalId
		// not found
		ContentEntity nextce;
		
		if (nextSlash == -1) {
			// hence final id found
			nextce = getVirtualEntity(ce, finalId);
		} else if ( nextSlash == finalId.length() - Entity.SEPARATOR.length()
			&& thisid.length() == nextSlash ) {
                       // we are looking for either:
                       // (i) the root of a virtual container, and the current position is
                       // on the membrane between the real and virtual worlds: the
                       // separator at the end of thisid is the root of the virtual world.
                       // (ii) a virtual container whose name is specified with a trailing
                       // "/" character (eg a directory in a file system) where this is OK.
			nextce = getVirtualEntity(ce, finalId);			
		} else {
                       // found C in the middle of a long string of containers
                       // /A/B/C/D/..
			String nextId = finalId.substring(0, nextSlash);
			nextce = getVirtualEntity(ce.getMember(nextId), finalId);
		}



		if (nextce == null || nextce.getId().equals(thisid))
		{
			if (exact)
			{
				return null;
			}
			else
			{
				return ce;
			}
		}
		else
		{
			return getVirtualChild(finalId, nextce, exact);
		}
	}

	/**
	 * ********************************************************************************** Everything below merely proxies method calls onto the underlying Storage (for real Collections and Resources) or onto the underlying ContentHostingHandler (for
	 * virtual Collections and Resources). i.e. the "plumbing" starts here! **********************************************************************************
	 */

	/**
	 * Cancel collection, using storage if real, or the ContentHostingHandler if present.
	 */
	public void cancelCollection( ContentCollectionEdit edit)
	{
		ContentHostingHandler chh = edit.getContentHandler();
		if (chh != null)
		{
			chh.cancel(edit);
		}
		else
		{
			storage.cancelCollection(edit);
		}
	}

	/**
	 * {@inheritDoc} Cancel collection, using storage if real, or the ContentHostingHandler if present
	 */
	public void cancelResource( ContentResourceEdit edit)
	{
		ContentHostingHandler chh = edit.getContentHandler();
		if (chh != null)
		{
			chh.cancel(edit);
		}
		else
		{
			storage.cancelResource(edit);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean checkCollection( String id)
	{
		if (storage.checkCollection(id))
		{
			return true;
		}
		ContentEntity ce = getVirtualChild(id, getRealParent( id), true);
		if (ce != null)
		{
			if (id.equals(ce.getId()))
			{
				if (ce instanceof ContentCollection)
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */

	public boolean checkResource( String id)
	{
		if (storage.checkResource(id))
		{
			return true;
		}

		ContentEntity ce = getVirtualChild(id, getRealParent( id), true);
		if (ce != null)
		{
			if (id.equals(ce.getId()))
			{
				if (ce instanceof ContentResource)
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */

	public void commitCollection( ContentCollectionEdit edit)
	{
		ContentHostingHandler chh = edit.getContentHandler();
		if (chh != null)
		{
			chh.commit(edit);
		}
		else
		{
			storage.commitCollection(edit);
		}
	}

	/**
	 * {@inheritDoc}
	 */

	public void commitDeletedResource( ContentResourceEdit edit, String uuid) throws ServerOverloadException
	{
		ContentHostingHandler chh = edit.getContentHandler();
		if (chh != null)
		{
			chh.commitDeleted(edit, uuid);
		}
		else
		{
			storage.commitDeletedResource(edit, uuid);
		}
	}

	/**
	 * {@inheritDoc}
	 */

	public void commitResource( ContentResourceEdit edit) throws ServerOverloadException
	{
		ContentHostingHandler chh = edit.getContentHandler();
		if (chh != null)
		{
			chh.commit(edit);
		}
		else
		{
			storage.commitResource(edit);
		}
	}

	/**
	 * {@inheritDoc}
	 */

	public ContentCollectionEdit editCollection( String id)
	{
		ContentEntity ce = getVirtualChild(id, getRealParent(id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				ContentCollectionEdit cce = chh.getContentCollectionEdit(id);
				cce.setVirtualContentEntity(getVirtualChild(id, getRealParent(id), false).getVirtualContentEntity());
				return cce;
			}
		}
		return storage.editCollection(id);
	}

	/**
	 * {@inheritDoc}
	 */

	public ContentResourceEdit editResource( String id)
	{
		ContentEntity ce = getVirtualChild(id, getRealParent(id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				ContentResourceEdit cre = chh.getContentResourceEdit(id);
				cre.setVirtualContentEntity(getVirtualChild(id, getRealParent( id), false).getVirtualContentEntity());
				return cre;
			}
		}
		return storage.editResource(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public ContentCollection getCollection( String id)
	{
		ContentCollection cc = storage.getCollection(id);
		if (cc != null)
		{
			return cc;
		}
		ContentEntity rp = getRealParent( id);
		if (rp == null)
		{
			return null;
		}

		ContentEntity ce = getVirtualChild(id, rp, true);
		if (ce instanceof ContentCollection)
		{
			return (ContentCollection) ce;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getCollections( ContentCollection collection)
	{
		ContentHostingHandler chh = collection.getContentHandler();
		if (chh != null)
		{
			return chh.getCollections(collection);
		}
		else
		{
			List allCollections = storage.getCollections(collection); // the real collections

			// Find any virtual *resources* which are really *collections*
			List l = storage.getResources(collection);
			for (java.util.Iterator i = l.iterator(); i.hasNext();)
			{
				ContentResource o = (ContentResource) i.next();
				ContentResource cr = getResource( o.getId());
				if (cr != null)
				{
					ResourceProperties p = cr.getProperties();
					if (p != null && p.getProperty(CHH_BEAN_NAME) != null && p.getProperty(CHH_BEAN_NAME).length() > 0)
					{
						allCollections.add(getVirtualChild(cr.getId() + Entity.SEPARATOR, cr, true)); // this one is a virtual collection!
					}
				}
			}
			return allCollections;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List getFlatResources( String id)
	{
		List l = storage.getFlatResources(id);
		if (l != null)
		{
			return l;
		}
		ContentEntity ce = getVirtualChild(id, getRealParent(id), true);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				return chh.getFlatResources(ce);
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ContentResource getResource( String id)
	{

		ContentResource cc = null;
		try
		{
			cc = storage.getResource(id);
		}
		catch (TypeException e)
		{
			log.debug("Type Exception ",e);
		}
		if (cc != null)
		{
			return cc;
		}
		ContentEntity ce = getVirtualChild(id, getRealParent(id), true);
		if (ce instanceof ContentResource)
		{
			return (ContentResource) ce;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ServerOverloadException
	 */
	public byte[] getResourceBody( ContentResource resource) throws ServerOverloadException
	{
		ContentHostingHandler chh = resource.getContentHandler();
		if (chh != null)
		{
			return chh.getResourceBody(resource);
		}
		else
		{
			return storage.getResourceBody(resource);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List getResources( ContentCollection collection)
	{
		ContentHostingHandler chh = collection.getContentHandler();
		if (chh != null)
		{
			return chh.getResources(collection);
		}
		else
		{
			List l = storage.getResources(collection);
			for (java.util.Iterator i = l.iterator(); i.hasNext();)
			{
				ContentResource o = (ContentResource) i.next();
				ContentResource cr = getResource(o.getId());
				if (cr != null)
				{
					ResourceProperties p = cr.getProperties();
					if (p != null && p.getProperty(CHH_BEAN_NAME) != null && p.getProperty(CHH_BEAN_NAME).length() > 0) i.remove(); // this one is a virtual collection!
				}
			}
			return l;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ContentCollectionEdit putCollection( String id)
	{
		ContentEntity ce = getVirtualChild(id, getRealParent(id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				ContentCollectionEdit cce = chh.getContentCollectionEdit(id);
				cce.setVirtualContentEntity(getVirtualChild(id, getRealParent(id), false).getVirtualContentEntity());
				return cce;
			}
		}
		return storage.putCollection(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public ContentResourceEdit putDeleteResource( String id, String uuid, String userId)
	{
		ContentEntity ce = getVirtualChild(id, getRealParent(id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				ContentResourceEdit cre = chh.putDeleteResource(id, uuid, userId);
				cre.setVirtualContentEntity(getVirtualChild(id, getRealParent(id), false).getVirtualContentEntity());
				return cre;
			}
		}
		return storage.putDeleteResource(id, uuid, userId);
	}

	/**
	 * {@inheritDoc}
	 */
	public ContentResourceEdit putResource( String id)
	{
		ContentEntity ce = getVirtualChild(id, getRealParent(id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				ContentResourceEdit cre = chh.getContentResourceEdit(id);
				cre.setVirtualContentEntity(getVirtualChild(id, getRealParent(id), false).getVirtualContentEntity());
				return cre;
			}
		}
		return storage.putResource(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeCollection( ContentCollectionEdit edit)
	{
		ContentHostingHandler chh = edit.getContentHandler();
		if (chh != null)
		{
			chh.removeCollection(edit);
		}
		else
		{
			storage.removeCollection(edit);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeResource( ContentResourceEdit edit)
	{
		ContentHostingHandler chh = edit.getContentHandler();
		if (chh != null)
		{
			chh.removeResource(edit);
		}
		else
		{
			storage.removeResource(edit);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ServerOverloadException
	 */
	public InputStream streamResourceBody( ContentResource resource) throws ServerOverloadException
	{
		ContentHostingHandler chh = resource.getContentHandler();
		if (chh != null)
		{
			return chh.streamResourceBody(resource);
		}
		else
		{
			return storage.streamResourceBody(resource);
		}
	}


	public void setResourceUser(SingleStorageUser rsu)
	{
		resourceStorageUser = rsu;
	}

	public void setCollectionUser(SingleStorageUser csu)
	{
		collectionStorageUser = csu;
	}

	public Edit newCollectionEdit(String id)
	{
		return collectionStorageUser.newResourceEdit(null, id, null);
	}

	public Edit newResourceEdit(String id)
	{
		return resourceStorageUser.newResourceEdit(null, id, null);
	}

	public int getMemberCount(String id)
	{
		ContentEntity ce = getVirtualChild(id, getRealParent(id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				return chh.getMemberCount(ce);
			}
		}
		return storage.getMemberCount(id);
	}

	/**
	 * @param storage
	 */
	public void setStorage(Storage storage)
	{
		this.storage = storage;
		
	}

	/**
	 * @return the storage
	 */
	public Storage getStorage()
	{
		return storage;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentHostingHandlerResolver#getMemberCollectionIds(java.lang.String)
	 */
	public Collection<String> getMemberCollectionIds(String collectionId)
	{
		ContentEntity ce = getVirtualChild(collectionId, getRealParent(collectionId), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				return chh.getMemberCollectionIds(ce);
			}
		}
		return storage.getMemberCollectionIds(collectionId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentHostingHandlerResolver#getMemberResourceIds(java.lang.String)
	 */
	public Collection<String> getMemberResourceIds(String collectionId)
	{
		ContentEntity ce = getVirtualChild(collectionId, getRealParent(collectionId), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				return chh.getMemberResourceIds(ce);
			}
		}
		return storage.getMemberResourceIds(collectionId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentHostingHandlerResolver#moveCollection(org.sakaiproject.content.api.ContentCollectionEdit, java.lang.String)
	 */
	public String moveCollection(ContentCollectionEdit thisCollection, String new_folder_id) throws OperationDelegationException
	{
		ContentEntity ce = getVirtualChild(new_folder_id, getRealParent(new_folder_id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				return chh.moveCollection(thisCollection,new_folder_id);
			}
		}
		throw new OperationDelegationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentHostingHandlerResolver#moveResource(org.sakaiproject.content.api.ContentResourceEdit, java.lang.String)
	 */
	public String moveResource(ContentResourceEdit thisResource, String new_id) throws OperationDelegationException
	{
		ContentEntity ce = getVirtualChild(new_id, getRealParent(new_id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				return chh.moveResource(thisResource,new_id);
			}
		}
		throw new OperationDelegationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentHostingHandlerResolver#setResourceUuid(java.lang.String, java.lang.String)
	 */
	public void setResourceUuid(String resourceId, String uuid) throws OperationDelegationException
	{
		ContentEntity ce = getVirtualChild(resourceId, getRealParent(resourceId), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				chh.setResourceUuid(resourceId,uuid);
			}
		}
		throw new OperationDelegationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentHostingHandlerResolver#getUuid(java.lang.String)
	 */
	public String getUuid(String id) throws OperationDelegationException
	{
		ContentEntity ce = getVirtualChild(id, getRealParent(id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				chh.getUuid(id);
			}
		}
		throw new OperationDelegationException();
	}
}
