/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.content.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.scorm.content.api.Addable;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;

@Slf4j
public abstract class ZipCHH implements ContentHostingHandler, Addable, Serializable
{
	private static final long serialVersionUID = 507912123982364924L;

	private static final String REAL_PARENT_ENTITY_PROPERTY = "zipCHH@REAL_PARENT_ENTITY_ID";
	private static final String VIRTUAL_ZIP_ENTITY_PROPERTY = "zipCHH@IS_VIRTUAL_ZIP_ENTITY";
	private static final String ENTITY_CACHE_KEY = "zipCHHFindEntity@";
	private static final String LIST_CACHE_KEY = "zipCHHFindList@";

	@Getter @Setter private ContentHostingHandlerResolver resolver;
	@Getter @Setter protected ResourceTypeRegistry resourceTypeRegistry;

	public abstract String getContentHostingHandlerName();

	@Override
	public void add(File file, String id)
	{
		try
		{
			ContentResource realParent = (ContentResource) getRealParent(id);
			byte[] archive = realParent.getContent();
			InputStream in = new ByteArrayInputStream(archive);
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			ZipWriter writer = new ZipWriter(in, out);
			InputStream entryStream = new FileInputStream(file);

			String path = getRelativePath(realParent.getId(), id);
			newId(path, file.getName());

			writer.add(path + file.getName(), entryStream);
			writer.process();

			if (entryStream != null)
			{
				entryStream.close();
			}

			ContentResourceEdit realParentEdit = contentService().editResource(realParent.getId());
			realParentEdit.setContent(out.toByteArray());
			contentService().commitResource(realParentEdit, NotificationService.NOTI_NONE);
		}
		catch (Exception soe)
		{
			log.error("Caught an exception trying to add a resource", soe);
		}
	}

	protected void cacheEntity(ContentEntity ce)
	{
		if (null != ce)
		{
			ThreadLocalManager.set(ENTITY_CACHE_KEY + ce.getId(), ce);
		}
	}

	private void cacheList(String key, List<ContentEntity> list)
	{
		if (null != list)
		{
			List<String> idList = new LinkedList<>();
			for (ContentEntity ce : list)
			{
				cacheEntity(ce);
				idList.add(ce.getId());
			}

			ThreadLocalManager.set(LIST_CACHE_KEY + key, idList);
		}
	}

	@Override
	public void cancel(ContentCollectionEdit edit) {}

	@Override
	public void cancel(ContentResourceEdit edit) {}

	@Override
	public void commit(ContentCollectionEdit edit) {}

	@Override
	public void commit(ContentResourceEdit edit)
	{
		if (edit.getVirtualContentEntity() == null)
		{
			try
			{
				contentService().commitResource(edit, NotificationService.NOTI_NONE);
			}
			catch (Exception e)
			{
				log.error("Caught an exception committing resource", e);
			}
		}
	}

	@Override
	public void commitDeleted(ContentResourceEdit edit, String uuid)
	{
		try
		{
			ContentResource realParent = (ContentResource) getRealParent(edit);
			byte[] archive = realParent.getContent();
			InputStream in = new ByteArrayInputStream(archive);
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			ZipWriter writer = new ZipWriter(in, out);

			String path = getRelativePath(realParent.getId(), edit.getId());
			writer.remove(path);
			writer.process();

			ContentResourceEdit realParentEdit = contentService().editResource(realParent.getId());
			realParentEdit.setContent(out.toByteArray());
			contentService().commitResource(realParentEdit, NotificationService.NOTI_NONE);
		} 
		catch (Exception soe)
		{
			log.error("Caught an exception trying to delete a resource", soe);
		}
	}

	protected ContentHostingService contentService()
	{
		return null;
	}

	protected int countChildren(ContentEntity parent, int depth)
	{
		ContentResource realParent = (ContentResource) getRealParent(parent);
		byte[] archive = null;

		try
		{
			if (realParent != null)
			{
				archive = realParent.getContent();
			}
		}
		catch (ServerOverloadException soe)
		{
			log.error("Caught a server overload exception trying to grab real parent's content", soe);
		}

		if (archive == null || archive.length <= 0)
		{
			return 0;
		}

		final String relativePath = getRelativePath(realParent.getId(), parent.getId());
		ZipReader reader = new ZipReader(new ByteArrayInputStream(archive))
		{
			@Override
			protected boolean includeContent(boolean isDirectory)
			{
				return !isDirectory;
			}

			@Override
			protected boolean isValid(String entryPath)
			{
				if (entryPath.endsWith(Entity.SEPARATOR) && entryPath.length() > 1)
				{
					entryPath = entryPath.substring(0, entryPath.length() - 1);
				}

				return isDeepEnough(entryPath, relativePath, 1);
			}

			@Override
			protected ContentEntity processEntry(String entryPath, ByteArrayOutputStream outStream, boolean isDirectory)
			{
				return null;
			}
		};

		reader.read();
		return reader.getCount();
	}

	protected List<ContentEntity> extractChildren(ContentEntity parent, int depth)
	{
		final ContentResource realParent = (ContentResource) getRealParent(parent);
		byte[] archive = null;

		try
		{
			if (null != realParent)
			{
				archive = realParent.getContent();
			}
		}
		catch (ServerOverloadException soe)
		{
			log.error("Caught a server overload exception trying to grab real parent's content", soe);
		}

		if (archive == null || archive.length <= 0)
		{
			return null;
		}

		final String relativePath = getRelativePath(realParent.getId(), parent.getId());
		ZipReader reader = new ZipReader(new ByteArrayInputStream(archive))
		{
			@Override
			protected boolean includeContent(boolean isDirectory)
			{
				return !isDirectory;
			}

			@Override
			protected boolean isValid(String entryPath)
			{
				if (entryPath.endsWith(Entity.SEPARATOR) && entryPath.length() > 1)
				{
					entryPath = entryPath.substring(0, entryPath.length() - 1);
				}

				return isDeepEnough(entryPath, relativePath, 1);
			}

			@Override
			protected ContentEntity processEntry(String entryPath, ByteArrayOutputStream outStream, boolean isDirectory)
			{
				ContentEntity entity = null;
				if (entryPath.endsWith(Entity.SEPARATOR) && entryPath.length() > 1)
				{
					entryPath = entryPath.substring(0, entryPath.length() - 1);
				}

				if (isDirectory)
				{
					entity = makeCollection(realParent, entryPath, null);
				} 
				else
				{
					// We don't need content stored for these objects 
					entity = makeResource(realParent, entryPath, null, null);
				}

				return entity;
			}
		};

		List<ContentEntity> list = reader.read();
		return list;
	}

	private byte[] extractContent(String id) throws ServerOverloadException
	{
		ContentResource resource = (ContentResource) resolveId(id);
		if (null != resource)
		{
			return resource.getContent();
		}

		return null;
	}

	protected ContentEntity extractEntity(ContentEntity realEntity, final String path) throws ServerOverloadException
	{
		final ContentResource cr = (ContentResource) realEntity;
		byte[] archive = cr.getContent();

		if (archive == null || archive.length <= 0)
		{
			return null;
		}

		ZipReader reader = new ZipReader(new ByteArrayInputStream(archive))
		{
			@Override
			protected boolean includeContent(boolean isDirectory)
			{
				return !isDirectory;
			}

			@Override
			protected boolean isValid(String entryPath)
			{
				return entryPath.equals(path);
			}

			@Override
			protected ContentEntity processEntry(String entryPath, ByteArrayOutputStream outStream, boolean isDirectory)
			{
				if (isDirectory)
				{
					return makeCollection(cr, path, null);
				}

				return makeResource(cr, path, null, outStream.toByteArray());
			}
		};

		return (ContentEntity) reader.readFirst();
	}

	protected List<ContentEntity> findChildren(ContentEntity ce, int depth) throws ServerOverloadException
	{
		List<ContentEntity> list = uncacheList(ce.getId());
		if (null == list)
		{
			list = extractChildren(ce, depth);
			cacheList(ce.getId(), list);
		}

		return list;
	}

	@Override
	public List getCollections(ContentCollection collection)
	{
		List<ContentEntity> resources = null;
		try
		{
			resources = findChildren(collection, 1);
		} 
		catch (ServerOverloadException soe)
		{
			log.error("Caught a server overload exception trying to extract resources from zip", soe);
		}

		List<ContentEntity> collections = new LinkedList<>();
		if (null != resources)
		{
			for (ContentEntity ce : resources)
			{
				if (ce.isCollection())
				{
					collections.add(ce);
				}
			}
		}

		return collections;
	}

	@Override
	public ContentCollectionEdit getContentCollectionEdit(String id)
	{
		ContentEntity ce = resolveId(id);
		if (ce instanceof ContentCollectionEdit)
		{
			return (ContentCollectionEdit) ce;
		}

		return null;
	}

	@Override
	public ContentResourceEdit getContentResourceEdit(String id)
	{
		ContentEntity ce = resolveId(id);
		if (ce instanceof ContentResourceEdit)
		{
			return (ContentResourceEdit) ce;
		}

		return null;
	}

	@Override
	public List getFlatResources(ContentEntity ce)
	{
		List resourceIds = new LinkedList();
		List<ContentEntity> members = null;
		try
		{
			members = findChildren(ce, -1);
		} 
		catch (Exception e)
		{
			log.error("Caught an exception ", e);
		}

		if (members != null)
		{
			for (ContentEntity member : members)
			{
				resourceIds.add(member.getId());
			}
		}

		return resourceIds;
	}

	@Override
	public int getMemberCount(ContentEntity ce)
	{
		return countChildren(ce, 1);
	}

	protected ContentEntity getRealEntity(String id)
	{
		ContentEntity ce = null;
		try
		{
			ce = contentService().getCollection(id);
		} 
		catch (IdUnusedException iue)
		{
			// The whole point is not to throw this.
		}
		catch (TypeException te)
		{
			// This doesn't seem to get thrown even though the API suggests it should
		}
		catch (PermissionException pe)
		{
			log.error("Caught a permission exception trying to find the real entity of {}", id, pe);
		}

		if (ce == null)
		{
			try
			{
				ce = contentService().getResource(id);
			}
			catch (IdUnusedException iue)
			{
				// The whole point is not to throw this.
			}
			catch (TypeException te)
			{
				// This shouldn't happen.
			}
			catch (PermissionException pe)
			{
				log.error("Caught a permission exception trying to find the real entity of {}", id, pe);
			}
		}

		if (null != ce)
		{
			ResourceProperties props = ce.getProperties();
			try
			{
				if (props.getBooleanProperty(VIRTUAL_ZIP_ENTITY_PROPERTY))
				{
					ce = null;
				}
			}
			catch (EntityPropertyNotDefinedException epnde)
			{
				// This will be thrown each time we look up a real resource rather than a virtual one
			} 
			catch (EntityPropertyTypeException epnde)
			{
				log.warn("This entity property is not of type boolean {}", VIRTUAL_ZIP_ENTITY_PROPERTY, epnde);
			}
		}

		return ce;
	}

	protected ContentEntity getRealParent(ContentEntity ce)
	{
		ResourceProperties props = ce.getProperties();
		String id = null;
		if (null != props)
		{
			try
			{
				id = (String) props.get(REAL_PARENT_ENTITY_PROPERTY);
			}
			catch (Exception e)
			{
				log.debug("Caught an unimportant exception getting a property that might not be there: {}", e.getMessage());
			}
		}

		if (null != id)
		{
			return getRealEntity(id);
		}

		// If that method doesn't work, then fall through to the other one.
		return getRealParent(ce.getId());
	}

	protected ContentEntity getRealParent(String id)
	{
		ContentEntity ce = null;
		ce = getRealEntity(id);
		if (ce == null)
		{
			if (id.equals(Entity.SEPARATOR))
			{
				return getRealParent(Entity.SEPARATOR);
			}

			int lastSlash = id.lastIndexOf(Entity.SEPARATOR);
			if (lastSlash > 0)
			{
				String parentId = id.substring(0, lastSlash);
				ce = getRealParent(parentId);
			}
		}

		return ce;
	}

	protected String getRelativePath(String parentId, String finalId)
	{
		String path = "";
		if (finalId.startsWith(parentId) && finalId.length() > parentId.length() + 1)
		{
			path = finalId.substring(parentId.length() + 1);
		}

		return path;
	}

	@Override
	public byte[] getResourceBody(ContentResource resource) throws ServerOverloadException
	{
		return extractContent(resource.getId());
	}

	@Override
	public List getResources(ContentCollection collection)
	{
		List<ContentEntity> items = null;
		try
		{
			items = findChildren(collection, 1);
		}
		catch (ServerOverloadException soe)
		{
			log.error("Caught a server overload exception trying to extract resources from zip", soe);
		}

		List<ContentEntity> resources = new LinkedList<>();
		if (null != items)
		{
			for (ContentEntity ce : items)
			{
				if (!ce.isCollection())
				{
					resources.add(ce);
				}
			}
		}

		return resources;
	}

	@Override
	public ContentEntity getVirtualContentEntity(ContentEntity ce, String finalId)
	{
		ContentEntity virtualEntity = null;
		if (null == ce)
		{
			return null;
		}

		ResourceProperties realProperties = ce.getProperties();
		if (null == realProperties)
		{
			return null;
		}

		String parentId = ce.getId();
		String path = getRelativePath(parentId, finalId);
		if (path.length() == 0)
		{
			// Grab some data from the real content entity
			String name = (String) realProperties.get(ResourceProperties.PROP_DISPLAY_NAME);
			virtualEntity = makeCollection(ce, path, name);

			virtualEntity.setContentHandler(this);
			ce.setVirtualContentEntity(virtualEntity);
		}
		else
		{
			virtualEntity = uncacheEntity(newId(parentId, path));
			if (null == virtualEntity)
			{
				try
				{
					virtualEntity = extractEntity(ce, path);
					cacheEntity(virtualEntity);
				} 
				catch (Exception e)
				{
					log.error("Caught an exception extracting resource ", e);
				}
			}
		}

		return virtualEntity;
	}

	public void init() {}

	protected boolean isDeepEnough(String zipPath, String path, int depth)
	{
		int howDeep = 1;
		if (path.length() > 0)
		{
			String[] pFields = path.split(Entity.SEPARATOR);

			if (null != pFields)
			{
				depth += pFields.length;
			}

			if (!zipPath.startsWith(path))
			{
				return false;
			}
		}

		String[] fields = zipPath.split(Entity.SEPARATOR);
		if (fields != null)
		{
			howDeep = fields.length;
		}

		return howDeep == depth;
	}

	protected ContentCollectionEdit makeCollection(ContentEntity ce, String path, String name)
	{
		String[] fields = path.split(Entity.SEPARATOR);
		if (null == name)
		{
			name = "[error]";

			if (null != fields && fields.length >= 1)
			{
				// Grab the last item as a name for this collection
				name = fields[fields.length - 1];
			}
		}

		String newId = newId(ce.getId(), path);
		if (!newId.endsWith(Entity.SEPARATOR))
		{
			newId += Entity.SEPARATOR;
		}

		ContentCollectionEdit collection = (ContentCollectionEdit) resolver.newCollectionEdit(newId);
		ResourcePropertiesEdit props = collection.getPropertiesEdit();
		props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
		props.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, getContentHostingHandlerName());
		props.addProperty(ResourceProperties.PROP_IS_COLLECTION, "true");
		props.addProperty(VIRTUAL_ZIP_ENTITY_PROPERTY, "true");
		props.addProperty(REAL_PARENT_ENTITY_PROPERTY, ce.getId());

		collection.setResourceType(ZipCollectionType.ZIP_COLLECTION_TYPE_ID);
		collection.setContentHandler(this);
		return collection;
	}

	protected ContentResourceEdit makeResource(ContentEntity ce, String path, String name, byte[] content)
	{
		String[] fields = path.split(Entity.SEPARATOR);
		if (null == name)
		{
			name = "[error]";

			if (null != fields && fields.length >= 1)
			{
				// Grab the last item as a name for this collection
				name = fields[fields.length - 1];
			}
		}

		ContentResourceEdit resource = (ContentResourceEdit) resolver.newResourceEdit(newId(ce.getId(), path));
		if (null != content)
		{
			resource.setContent(content);
			resource.setContentLength(content.length);
		}

		resource.setResourceType(ResourceType.TYPE_HTML);
		resource.setContentType(new MimetypesFileTypeMap().getContentType(name));

		ResourcePropertiesEdit props = resource.getPropertiesEdit();
		props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
		props.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, getContentHostingHandlerName());
		props.addProperty(VIRTUAL_ZIP_ENTITY_PROPERTY, "true");
		props.addProperty(REAL_PARENT_ENTITY_PROPERTY, ce.getId());
		resource.setContentHandler(this);
		return resource;
	}

	protected String newId(String id, String path)
	{
		StringBuilder buffer = new StringBuilder();
		if (id.endsWith(Entity.SEPARATOR))
		{
			if (path.startsWith(Entity.SEPARATOR))
			{
				path = path.substring(1);
			}

			buffer.append(id).append(path);
		}
		else
		{
			if (path.startsWith(Entity.SEPARATOR))
			{
				buffer.append(id).append(path);
			} 
			else
			{
				buffer.append(id).append(Entity.SEPARATOR).append(path);
			}
		}

		return buffer.toString();
	}

	@Override
	public ContentResourceEdit putDeleteResource(String id, String uuid, String userId)
	{
		return (ContentResourceEdit) resolveId(id);
	}

	@Override
	public void removeCollection(ContentCollectionEdit edit)
	{
		ContentEntity pe = getRealParent(edit.getId());
		if (pe != null)
		{
			try
			{
				contentService().removeResource(pe.getId());
			}
			catch (Exception e)
			{
				log.error("Unable to remove the underlying resource", e);
			}
		}
	}

	@Override
	public void removeResource(ContentResourceEdit edit)
	{
		ContentEntity pe = getRealParent(edit.getId());
		if (pe != null)
		{
			try
			{
				contentService().removeResource(pe.getId());
			}
			catch (Exception e)
			{
				log.error("Unable to remove the underlying resource", e);
			}
		}
	}

	private ContentEntity resolveId(String id)
	{
		ContentEntity ce = null;
		ContentEntity pe = getRealParent(id);
		if (null != pe)
		{
			ce = getVirtualContentEntity(pe, id);
		}

		return ce;
	}

	@Override
	public InputStream streamResourceBody(ContentResource resource) throws ServerOverloadException
	{
		InputStream stream = new ByteArrayInputStream(resource.getContent());
		return stream;
	}

	/*
	 * These cache/uncache methods bind objects to the 'current' request -- since the entire zip stream
	 * would be read through several times per request otherwise, I think it makes sense to do this.
	 */
	protected ContentEntity uncacheEntity(String key)
	{
		ContentEntity ce = null;
		try
		{
			ce = (ContentEntity) ThreadLocalManager.get(ENTITY_CACHE_KEY + key);
		}
		catch (ClassCastException e)
		{
			log.error("Caught a class cast exception finding resource with key {}", key, e);
		}

		return ce;
	}

	private List<ContentEntity> uncacheList(String key)
	{
		List<ContentEntity> list = null;
		List<String> idList = null;

		try
		{
			idList = (List<String>) ThreadLocalManager.get(LIST_CACHE_KEY + key);
			if (null != idList)
			{
				list = new LinkedList<>();
				for (String id : idList)
				{
					ContentEntity ce = uncacheEntity(id);
					if (null != ce)
					{
						list.add(ce);
					}
				}
			}
		} 
		catch (ClassCastException e)
		{
			log.error("Caught a class cast exception finding id list with key {}", key, e);
		}

		return list;
	}
}
