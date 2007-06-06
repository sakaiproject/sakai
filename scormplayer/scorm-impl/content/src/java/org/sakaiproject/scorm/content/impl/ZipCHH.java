package org.sakaiproject.scorm.content.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;

public class ZipCHH implements ContentHostingHandler {
	private static final String REAL_PARENT_ENTITY_PROPERTY = "zipCHH@REAL_PARENT_ENTITY_ID";
	private static final String VIRTUAL_ZIP_ENTITY_PROPERTY = "zipCHH@IS_VIRTUAL_ZIP_ENTITY";
	private static final String ENTITY_CACHE_KEY = "zipCHHFindEntity@";
	private static final String LIST_CACHE_KEY = "zipCHHFindList@";	
	
	private static Log log = LogFactory.getLog(ZipCHH.class);
			
	protected ContentHostingService contentService() { return null; }
	private ContentHostingHandlerResolver resolver;
	
	public void cancel(ContentCollectionEdit edit) {

	}

	public void cancel(ContentResourceEdit edit) {

	}

	public void commit(ContentCollectionEdit edit) {

	}

	public void commit(ContentResourceEdit edit) {

	}

	public void commitDeleted(ContentResourceEdit edit, String uuid) {
		try {
			ContentResource realParent = (ContentResource)getRealParent(edit); 
			byte[] archive = realParent.getContent();
			InputStream in = new ByteArrayInputStream(archive);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			ZipWriter writer = new ZipWriter(in, out);
			
			String path = getRelativePath(realParent.getId(), edit.getId());
			writer.remove(path);
			writer.process();
		
			ContentResourceEdit realParentEdit = contentService().editResource(realParent.getId());
			realParentEdit.setContent(out.toByteArray());
			contentService().commitResource(realParentEdit);
		} catch (Exception soe) {
			log.error("Caught an exception trying to delete a resource", soe);
		}
	}

	public List getCollections(ContentCollection collection) {
		
		List<ContentEntity> resources = null;
		try {
			resources = findChildren(collection, 1);
		} catch (ServerOverloadException soe) {
			log.error("Caught a server overload exception trying to extract resources from zip", soe);
		}
		
		List<ContentEntity> collections = new LinkedList<ContentEntity>();
		
		if (null != resources) {
			for (ContentEntity ce : resources) {
				if (ce.isCollection())
					collections.add(ce);
			}
		}
		return collections;
	}

	public ContentCollectionEdit getContentCollectionEdit(String id) {
		ContentEntity ce = resolveId(id);
		
		if (ce instanceof ContentCollectionEdit)
			return (ContentCollectionEdit)ce;
		
		return null;
	}

	public ContentResourceEdit getContentResourceEdit(String id) {
		ContentEntity ce = resolveId(id);
		
		if (ce instanceof ContentResourceEdit)
			return (ContentResourceEdit)ce;
		
		return null;
	}

	public List getFlatResources(ContentEntity ce) {
		List resourceIds = new LinkedList();
		
		List<ContentEntity> members = null;
		try {
			members = findChildren(ce, -1);
		} catch (Exception e) {
			log.error("Caught an exception ", e);
		}
		
		for (ContentEntity member : members) {
			resourceIds.add(member.getId());
		}
		
		return resourceIds;
	}

	public int getMemberCount(ContentEntity ce) {			
		return countChildren(ce, 1);
	}

	public byte[] getResourceBody(ContentResource resource)
			throws ServerOverloadException {

		return extractContent(resource.getId());
	}

	public List getResources(ContentCollection collection) {
		List<ContentEntity> items = null;
		try {
			items = findChildren(collection, 1);
		} catch (ServerOverloadException soe) {
			log.error("Caught a server overload exception trying to extract resources from zip", soe);
		}
		
		List<ContentEntity> resources = new LinkedList<ContentEntity>();
		
		if (null != items) {
			for (ContentEntity ce : items) {
				if (!ce.isCollection())
					resources.add(ce);
			}
		}
		
		return resources;
	}
	
	public ContentEntity getVirtualContentEntity(ContentEntity ce, String finalId) {
		ContentEntity virtualEntity = null;
		
		if (null == ce)
			return null;
		
		ResourceProperties realProperties = ce.getProperties();
		if (null == realProperties)
			return null;
		
		String chhbeanname = realProperties.getProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME);
		
		if (chhbeanname == null || !chhbeanname.equals("org.sakaiproject.scorm.client.api.ContentHostingHandler"))
			return getRealEntity(ce.getId());
			
		String parentId = ce.getId();
		String path = getRelativePath(parentId, finalId);
		
		if (path.length() == 0) {
			// Grab some data from the real content entity
			String name = (String)realProperties.get(ResourceProperties.PROP_DISPLAY_NAME);			
			virtualEntity = makeCollection(parentId, path, name);
		} else {		
			virtualEntity = uncacheEntity(newId(parentId, path));

			if (null == virtualEntity) {
				try {
					virtualEntity = extractEntity(ce, path);
					cacheEntity(virtualEntity);
				} catch (Exception e) {
					log.error("Caught an exception extracting resource ", e);
				}
			}			
		}
				
		return virtualEntity;
	}

	public ContentResourceEdit putDeleteResource(String id, String uuid, String userId) {
		return (ContentResourceEdit)resolveId(id);
	}

	public void removeCollection(ContentCollectionEdit edit) {

	}

	public void removeResource(ContentResourceEdit edit) {

	}

	public InputStream streamResourceBody(ContentResource resource)
			throws ServerOverloadException {
		
		InputStream stream = new ByteArrayInputStream(resource.getContent());
		
		return stream;
	}

	public ContentHostingHandlerResolver getResolver() {
		return resolver;
	}

	public void setResolver(ContentHostingHandlerResolver resolver) {
		this.resolver = resolver;
	}
	
	protected String getRelativePath(String parentId, String finalId) {
		String path = "";
		
		if (finalId.startsWith(parentId) && finalId.length() > parentId.length() + 1) 
			path = finalId.substring(parentId.length() + 1);
		
		return path;
	}
	
	protected ContentEntity getRealParent(ContentEntity ce) {
		ResourceProperties props = ce.getProperties();
		
		String id = null;
		if (null != props) {
			try {
				id = (String)props.get(REAL_PARENT_ENTITY_PROPERTY);
			} catch (Exception e) {
				log.debug("Caught an unimportant exception getting a property that might not be there: " + e.getMessage());
			}
		}

		if (null != id)
			return getRealEntity(id);

			
		// If that method doesn't work, then fall through to the other one.
		return getRealParent(ce.getId());
	}
	
	protected ContentEntity getRealParent(String id) {
		ContentEntity ce = null;
		
		ce = getRealEntity(id);
			
		if (ce == null) {
			if (id.equals(Entity.SEPARATOR)) return getRealParent(Entity.SEPARATOR);
			int lastSlash = id.lastIndexOf(Entity.SEPARATOR);
			if (lastSlash > 0)
			{
				String parentId = id.substring(0, lastSlash);
				ce = getRealParent(parentId);
			}
		}
		
		return ce;
	}
	
	
	private ContentEntity getRealEntity(String id) {
		ContentEntity ce = null;

		try {
			ce = contentService().getCollection(id);
		} catch (IdUnusedException iue) {
			// The whole point is not to throw this.
		} catch (TypeException te) {
			// This doesn't seem to get thrown even though the API suggests it should
		} catch (PermissionException pe) {
			log.error("Caught a permission exception trying to find the real entity of " + id, pe);
		}
		
		try {
			ce = contentService().getResource(id);
		} catch (IdUnusedException iue) {
			// The whole point is not to throw this.
		} catch (TypeException te) {
			// This shouldn't happen.
		} catch (PermissionException pe) {
			log.error("Caught a permission exception trying to find the real entity of " + id, pe);
		}
		
		if (null != ce) {
			ResourceProperties props = ce.getProperties();
			
			try {
				if (props.getBooleanProperty(VIRTUAL_ZIP_ENTITY_PROPERTY)) {
					ce = null;
				}
			} catch (EntityPropertyNotDefinedException epnde) {
				// This will be thrown each time we look up a real resource rather than a virtual one
			} catch (EntityPropertyTypeException epnde) {
				log.warn("This entity property is not of type boolean " + VIRTUAL_ZIP_ENTITY_PROPERTY);
			}
		}
		
		return ce;
	}
	
	private ContentEntity resolveId(String id) {
		ContentEntity ce = null;
		
		ContentEntity pe = getRealParent(id);
		if (null != pe) {
			ce = getVirtualContentEntity(pe, id);
		}
		return ce;
	}
	
	protected List<ContentEntity> findChildren(ContentEntity ce, int depth) throws ServerOverloadException {
		
		List<ContentEntity> list = uncacheList(ce.getId());
		
		if (null == list) {
			list = extractChildren(ce, depth);
			cacheList(ce.getId(), list);
		}
		return list;
	}
	
	/*
	 * These cache/uncache methods bind objects to the 'current' request -- since the entire zip stream
	 * would be read through several times per request otherwise, I think it makes sense to do this.
	 */
	private ContentEntity uncacheEntity(String key) {
		ContentEntity ce = null;
		try {
			ce = (ContentEntity) ThreadLocalManager.get(ENTITY_CACHE_KEY + key);
		} catch(ClassCastException e) {
			log.error("Caught a class cast exception finding resource with key " + key, e);
		}
		
		return ce;
	}
	
	private void cacheEntity(ContentEntity ce) {
		if (null != ce) {			
			ThreadLocalManager.set(ENTITY_CACHE_KEY + ce.getId(), ce);
		}
	}
	
	private List<ContentEntity> uncacheList(String key) {
		List<ContentEntity> list = null;
		List<String> idList = null;
		
		try {
			idList = (List<String>) ThreadLocalManager.get(LIST_CACHE_KEY + key);
			
			if (null != idList) {
				list = new LinkedList<ContentEntity>();
				for (String id : idList) {
					ContentEntity ce = uncacheEntity(id);
			
					if (null != ce) {
						list.add(ce);
					}
				}
			}
		} catch(ClassCastException e) {
			log.error("Caught a class cast exception finding id list with key " + key, e);
		}

		return list;
	}
	
	private void cacheList(String key, List<ContentEntity> list) {
		if (null != list) {
			List<String> idList = new LinkedList<String>();
			
			for (ContentEntity ce : list) {
				cacheEntity(ce);
				idList.add(ce.getId());
			}
			
			ThreadLocalManager.set(LIST_CACHE_KEY + key, idList);
		}
	}
	
	private byte[] extractContent(String id) throws ServerOverloadException {
		ContentResource resource = (ContentResource)resolveId(id);
		
		if (null != resource)
			return resource.getContent();
		
		return null;
	}
		
	private ContentEntity extractEntity(ContentEntity realEntity, final String path) throws ServerOverloadException {
		ContentResource cr = (ContentResource)realEntity;
		
		byte[] archive = cr.getContent();
		
		if (archive == null || archive.length <= 0)
			return null;
		
		final String id = cr.getId();
		
		ZipReader reader = new ZipReader(new ByteArrayInputStream(archive)) {
			
			protected boolean includeContent(boolean isDirectory) {
				return !isDirectory;
			}
			
			protected boolean isValid(String entryPath) {
				return entryPath.equals(path);
			}

			protected Object processEntry(String entryPath, ByteArrayOutputStream outStream, boolean isDirectory) {
				if (isDirectory)
					return makeCollection(id, path, null);
				
	    		return makeResource(id, path, null, outStream.toByteArray());
			}
			
		};
		
		return (ContentEntity)reader.readFirst();
	}
	
	protected List<ContentEntity> extractChildren(ContentEntity parent, int depth)  {
		ContentResource realParent = (ContentResource)getRealParent(parent);
		
		byte[] archive = null;
		
		try {
			if (null != realParent)
				archive = realParent.getContent();
		} catch (ServerOverloadException soe) {
			log.error("Caught a server overload exception trying to grab real parent's content", soe);
		}
		
		if (archive == null || archive.length <= 0)
			return null;
		
		final String relativePath = getRelativePath(realParent.getId(), parent.getId());
		final String realParentId = realParent.getId();
		
		ZipReader reader = new ZipReader(new ByteArrayInputStream(archive)) {
			
			protected boolean includeContent(boolean isDirectory) {
				return !isDirectory;
			}
			
			protected boolean isValid(String entryPath) {
				if (entryPath.endsWith(Entity.SEPARATOR) && entryPath.length() > 1)
					entryPath = entryPath.substring(0, entryPath.length() - 1);
	    		
		    	return isDeepEnough(entryPath, relativePath, 1);
			}
			
			protected Object processEntry(String entryPath, ByteArrayOutputStream outStream, boolean isDirectory) {
				ContentEntity entity = null;
				
				if (entryPath.endsWith(Entity.SEPARATOR) && entryPath.length() > 1)
					entryPath = entryPath.substring(0, entryPath.length() - 1);
				
				if (isDirectory) {
	    			entity = makeCollection(realParentId, entryPath, null);
	    		} else {
	    			// We don't need content stored for these objects 
	    			entity = makeResource(realParentId, entryPath, null, null);
	    		}
				
				return entity;
			}
			
		};
		
		List<ContentEntity> list = reader.read();
		
		return list;
	}
	
	protected int countChildren(ContentEntity parent, int depth) {
		ContentResource realParent = (ContentResource)getRealParent(parent);
		
		byte[] archive = null;
		
		try {
			if (realParent != null)
				archive = realParent.getContent();
		} catch (ServerOverloadException soe) {
			log.error("Caught a server overload exception trying to grab real parent's content", soe);
		}
		
		if (archive == null || archive.length <= 0)
			return 0;
		
		final String relativePath = getRelativePath(realParent.getId(), parent.getId());
			
		ZipReader reader = new ZipReader(new ByteArrayInputStream(archive)) {
			
			protected boolean includeContent(boolean isDirectory) {
				return !isDirectory;
			}
			
			protected boolean isValid(String entryPath) {
				if (entryPath.endsWith(Entity.SEPARATOR) && entryPath.length() > 1)
					entryPath = entryPath.substring(0, entryPath.length() - 1);
	    		
		    	return isDeepEnough(entryPath, relativePath, 1);
			}

			protected Object processEntry(String entryPath, ByteArrayOutputStream outStream, boolean isDirectory) {
	    		return null;
			}
		};
		
		reader.read();
		
		return reader.getCount();
	}
	
	protected boolean isDeepEnough(String zipPath, String path, int depth) {
		int howDeep = 1;
		
		if (path.length() > 0) {
			String[] pFields = path.split(Entity.SEPARATOR);
			
			if (null != pFields)
				depth += pFields.length;
			
			if (!zipPath.startsWith(path))
				return false;
		}
		
		String[] fields = zipPath.split(Entity.SEPARATOR);

		if (fields != null)
			howDeep = fields.length;
		
		return howDeep == depth;
	}
	
	
	
	protected String newId(String id, String path) {
		StringBuffer buffer = new StringBuffer();
		if (id.endsWith(Entity.SEPARATOR)) {
			if (path.startsWith(Entity.SEPARATOR)) 
				path = path.substring(1);
			buffer.append(id).append(path);
		} else {
			if (path.startsWith(Entity.SEPARATOR)) {
				buffer.append(id).append(path);
			} else {
				buffer.append(id).append(Entity.SEPARATOR).append(path);
			}
		}
		
		return buffer.toString();
	}
	
	protected ContentCollectionEdit makeCollection(String id, String path, String name) {		
		String[] fields = path.split(Entity.SEPARATOR);
		
		if (null == name) {
			name = "[error]";
			
			if (null != fields && fields.length >= 1) {
				// Grab the last item as a name for this collection
				name = fields[fields.length - 1];
			}
		}
		String newId = newId(id, path);
		if (!newId.endsWith(Entity.SEPARATOR))
			newId += Entity.SEPARATOR;
		
		ContentCollectionEdit collection = (ContentCollectionEdit)resolver.newCollectionEdit(newId);
		
		ResourcePropertiesEdit props = collection.getPropertiesEdit();
		props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
		props.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.client.api.ContentHostingHandler");
		props.addProperty(ResourceProperties.PROP_IS_COLLECTION, "true");
		props.addProperty(VIRTUAL_ZIP_ENTITY_PROPERTY, "true");
		props.addProperty(REAL_PARENT_ENTITY_PROPERTY, id);
		
		collection.setContentHandler(this);
		
		return collection;
	}
		
	protected ContentResourceEdit makeResource(String id, String path, String name, byte[] content) {		
		String[] fields = path.split(Entity.SEPARATOR);
		
		if (null == name) {
			name = "[error]";
			
			if (null != fields && fields.length >= 1) {
				// Grab the last item as a name for this collection
				name = fields[fields.length - 1];
			}
		}
		
		ContentResourceEdit resource = (ContentResourceEdit)resolver.newResourceEdit(newId(id, path));
		
		if (null != content) {
			resource.setContent(content);
			resource.setContentLength(content.length);
		}
		
		resource.setResourceType(ResourceType.TYPE_HTML);
		resource.setContentType(new MimetypesFileTypeMap().getContentType(name));
		
		ResourcePropertiesEdit props = resource.getPropertiesEdit();
		props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
		props.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.client.api.ContentHostingHandler");
		props.addProperty(VIRTUAL_ZIP_ENTITY_PROPERTY, "true");	
		props.addProperty(REAL_PARENT_ENTITY_PROPERTY, id);
		
		resource.setContentHandler(this);
		
		return resource;
	}

}
