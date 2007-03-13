/**
 * 
 */
package org.sakaiproject.content.impl;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.impl.BaseContentService.Storage;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;

/**
 * <p>
 * Implementation of the Handler Resolver. This class chains back to the storage to get local entities but then resolves the IDs through to virtual content entities based on the name ContentHostingHandlers. The primary 3 methods are getRealParent(),
 * getVirtualEntity(), and getVirtualChild(). The remaining methods are largely plumbing, proxying the Storage mechanism that is being used. The Storage mechanims must be aware that this code will cause re-entry into the Sotrage methods and so the Storage
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
public class ContentHostingHandlerResolverImpl implements BaseContentHostingHandlerResolver
{

	private static final Log log = LogFactory.getLog(ContentHostingHandlerResolverImpl.class);

	/**
	 * Find the closest real ancestor to the requested id, this recurses into itself
	 * 
	 * @param id
	 * @return the closest ancestor or null if not found (bit unlikely)
	 */
	private ContentEntity getRealParent(Storage storage, String id)
	{
		ContentEntity ce = storage.getCollection(id);
		if (ce == null)
		{
			ce = storage.getResource(id);
		}
		if (ce == null)
		{
			if (id.equals(Entity.SEPARATOR)) return getRealParent(storage, Entity.SEPARATOR);
			int lastSlash = id.lastIndexOf(Entity.SEPARATOR, id.length() - 2);
			if (lastSlash > 0)
			{
				String parentId = id.substring(0, lastSlash + 1 /* ian@caret.cam wanted a "- 1" here */);
				ce = getRealParent(storage, parentId);
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
	private ContentEntity getVirtualEntity(ContentEntity ce, String finalId)
	{
		if (ce == null)
		{
			return null;
		}
		ContentEntity vce = null;
		ResourceProperties p = ce.getProperties();
		String chhbeanname = p.getProperty(CHH_BEAN_NAME);

		if (chhbeanname != null && chhbeanname.length() > 0)
		{
			try
			{
				ContentHostingHandler chh = (ContentHostingHandler) ComponentManager.get(chhbeanname);
				vce = chh.getVirtualContentEntity(ce, finalId);
			}
			catch (Exception e)
			{
				// log and return null
				log.warn("Failed to find CHH Bean " + chhbeanname + " or bean failed to resolve virtual entity ID", e);
				return ce;
			}
		}
		return vce;
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
	private ContentEntity getVirtualChild(String finalId, ContentEntity ce, boolean exact)
	{
		if (ce==null) return null;  // entirely empty resources tool
		String thisid = ce.getId();
		if (finalId.equals(thisid))
		{
			return ce;
		}
		int nextSlash = finalId.indexOf(Entity.SEPARATOR, thisid.length());
		String nextId;
		if (nextSlash == -1)
			nextId = finalId;
		else
			nextId = finalId.substring(0, nextSlash);

		// virtual container hierarchy needs the starting SEPARATOR to distinguish the (real) CE from the root of the virtual hierarchy.
		ContentEntity nextce;
		if (nextSlash == finalId.length() - Entity.SEPARATOR.length()
				&& thisid.length() + Entity.SEPARATOR.length() == finalId.length())
		{
			nextce = getVirtualEntity(ce, finalId);
		}
		else
		{
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
	public void cancelCollection(Storage storage, ContentCollectionEdit edit)
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
	public void cancelResource(Storage storage, ContentResourceEdit edit)
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
	public boolean checkCollection(Storage storage, String id)
	{
		if (storage.checkCollection(id))
		{
			return true;
		}
		ContentEntity ce = getVirtualChild(id, getRealParent(storage, id), true);
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

	public boolean checkResource(Storage storage, String id)
	{
		if (storage.checkResource(id))
		{
			return true;
		}

		ContentEntity ce = getVirtualChild(id, getRealParent(storage, id), true);
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

	public void commitCollection(Storage storage, ContentCollectionEdit edit)
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

	public void commitDeleteResource(Storage storage, ContentResourceEdit edit, String uuid)
	{
		ContentHostingHandler chh = edit.getContentHandler();
		if (chh != null)
		{
			chh.commitDeleted(edit, uuid);
		}
		else
		{
			storage.commitDeleteResource(edit, uuid);
		}
	}

	/**
	 * {@inheritDoc}
	 */

	public void commitResource(Storage storage, ContentResourceEdit edit) throws ServerOverloadException
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

	public ContentCollectionEdit editCollection(Storage storage, String id)
	{
		ContentEntity ce = getVirtualChild(id, getRealParent(storage, id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				return chh.getContentCollectionEdit(id);
			}
		}
		return storage.editCollection(id);
	}

	/**
	 * {@inheritDoc}
	 */

	public ContentResourceEdit editResource(Storage storage, String id)
	{
		ContentEntity ce = getVirtualChild(id, getRealParent(storage, id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				return chh.getContentResourceEdit(id);
			}
		}
		return storage.editResource(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public ContentCollection getCollection(Storage storage, String id)
	{
		ContentCollection cc = storage.getCollection(id);
		if (cc != null)
		{
			return cc;
		}
		ContentEntity rp = getRealParent(storage, id);
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
	public List getCollections(Storage storage, ContentCollection collection)
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
				ContentResource cr = getResource(storage, o.getId());
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
	public List getFlatResources(Storage storage, String id)
	{
		List l = storage.getFlatResources(id);
		if (l != null)
		{
			return l;
		}
		ContentEntity ce = getVirtualChild(id, getRealParent(storage, id), true);
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
	public ContentResource getResource(Storage storage, String id)
	{

		ContentResource cc = storage.getResource(id);
		if (cc != null)
		{
			return cc;
		}
		ContentEntity ce = getVirtualChild(id, getRealParent(storage, id), true);
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
	public byte[] getResourceBody(Storage storage, ContentResource resource) throws ServerOverloadException
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
	public List getResources(Storage storage, ContentCollection collection)
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
				ContentResource cr = getResource(storage, o.getId());
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
	public ContentCollectionEdit putCollection(Storage storage, String id)
	{
		ContentEntity ce = getVirtualChild(id, getRealParent(storage, id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				return chh.getContentCollectionEdit(id);
			}
		}
		return storage.putCollection(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public ContentResourceEdit putDeleteResource(Storage storage, String id, String uuid, String userId)
	{
		ContentEntity ce = getVirtualChild(id, getRealParent(storage, id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				return chh.putDeleteResource(id, uuid, userId);
			}
		}
		return storage.putDeleteResource(id, uuid, userId);
	}

	/**
	 * {@inheritDoc}
	 */
	public ContentResourceEdit putResource(Storage storage, String id)
	{
		ContentEntity ce = getVirtualChild(id, getRealParent(storage, id), false);
		if (ce != null)
		{
			ContentHostingHandler chh = ce.getContentHandler();
			if (chh != null)
			{
				return chh.getContentResourceEdit(id);
			}
		}
		return storage.putResource(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeCollection(Storage storage, ContentCollectionEdit edit)
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
	public void removeResource(Storage storage, ContentResourceEdit edit)
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
	public InputStream streamResourceBody(Storage storage, ContentResource resource) throws ServerOverloadException
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

	protected org.sakaiproject.util.StorageUser resourceStorageUser;

	protected org.sakaiproject.util.StorageUser collectionStorageUser;

	public void setResourceUser(org.sakaiproject.util.StorageUser rsu)
	{
		resourceStorageUser = rsu;
	}

	public void setCollectionUser(org.sakaiproject.util.StorageUser csu)
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

	public int getMemberCount(Storage storage, String id)
	{
		ContentEntity ce = getVirtualChild(id, getRealParent(storage, id), false);
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
}
