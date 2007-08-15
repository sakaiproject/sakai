package org.sakaiproject.scorm.client.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.ServerOverloadException;

public class ScormContentHostingHandler implements ContentHostingHandler {
	private static final String ACTUAL_RESOURCE_ID_PROPERTY = "ACTUAL_RESOURCE_ID";
	
	private static Log log = LogFactory.getLog(ScormContentHostingHandler.class);
			
	protected ContentHostingService contentService() { return null; }
	private ContentHostingHandlerResolver resolver;
	
	public void cancel(ContentCollectionEdit edit) {
		//contentService().cancelCollection(edit);
	}

	public void cancel(ContentResourceEdit edit) {
		//contentService().cancelResource(edit);
	}

	public void commit(ContentCollectionEdit edit) {
		//contentService().commitCollection(edit);
	}

	public void commit(ContentResourceEdit edit) {
		/*try {
			contentService().commitResource(edit);
		} catch (Exception oqe) {
			log.error("Caught an exception committing scorm resource!", oqe);
		}*/
	}

	public void commitDeleted(ContentResourceEdit edit, String uuid) {
		//contentService().removeResource(edit);
	}

	public List getCollections(ContentCollection collection) {
		
		List<ContentEntity> resources = null;
		try {
			resources = extractZippedResources(collection, 1);
		} catch (ServerOverloadException soe) {
			log.error("Caught a server overload exception trying to extract resources from zip", soe);
		}
		
		List<ContentEntity> collections = new LinkedList<ContentEntity>();
		
		for (ContentEntity ce : resources) {
			if (ce.isCollection())
				collections.add(ce);
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
			members = extractZippedResources(ce, -1);
		} catch (Exception e) {
			log.error("Caught an exception ", e);
		}
		
		for (ContentEntity member : members) {
			resourceIds.add(member.getId());
		}
		
		return resourceIds;
	}

	public int getMemberCount(ContentEntity ce) {	
		List<ContentEntity> members = null;
		try {
			members = extractZippedResources(ce, 1);
		} catch (Exception e) {
			log.error("Caught an exception ", e);
		}
		
		if (null != members)
			return members.size();
		
		return 0;
	}

	public byte[] getResourceBody(ContentResource resource)
			throws ServerOverloadException {

		/*ContentResource realResource = getRealResource(resource);
		
		if (null != realResource)
			return realResource.getContent();
		*/
		return resource.getContent();
	}

	public List getResources(ContentCollection collection) {
		List<ContentEntity> items = null;
		try {
			items = extractZippedResources(collection, 1);
		} catch (ServerOverloadException soe) {
			log.error("Caught a server overload exception trying to extract resources from zip", soe);
		}
		
		List<ContentEntity> resources = new LinkedList<ContentEntity>();
		
		for (ContentEntity ce : items) {
			if (!ce.isCollection())
				resources.add(ce);
		}
		
		return resources;
	}

	public ContentEntity getVirtualContentEntity(ContentEntity ce, String finalId) {
		ContentEntity virtualEntity = null;
		
		if (ce == null)
			return null;
			
		String thisid = ce.getId();
		String path = "";
		
		if (finalId.length() > thisid.length()) 
			path = finalId.substring(thisid.length());
		
		ContentResource cr = (ContentResource)ce;
		if (path.equals("/")) {
			// Grab some data from the real content entity
			ResourceProperties realProperties = ce.getProperties();
			String name = (String)realProperties.get(ResourceProperties.PROP_DISPLAY_NAME);
			
			// This is the root of the virtual entity
			ContentCollectionEdit virtualCollection = (ContentCollectionEdit)resolver.newCollectionEdit(finalId);
			virtualCollection.setContentHandler(this);
			ce.setVirtualContentEntity(virtualCollection);
			
			ResourcePropertiesEdit props = virtualCollection.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
			props.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.client.api.ContentHostingHandler");
			props.addProperty(ACTUAL_RESOURCE_ID_PROPERTY, thisid);
			props.addProperty(ResourceProperties.PROP_IS_COLLECTION, "true");

			virtualEntity = virtualCollection;
		} else {
			System.out.println("Path is " + path);

			// Strip off any leading "/"
			if (path.length() > 1) {
				if (path.startsWith("/"))
					path = path.substring(1);
			}
			
			try {
				virtualEntity = extractZippedResource(ce, path);
			} catch (Exception e) {
				log.error("Caught an exception extracting a zipped resource ", e);
			}
		}
				
		return virtualEntity;
	}

	public ContentResourceEdit putDeleteResource(String id, String uuid,
			String userId) {

		return null;
	}

	public void removeCollection(ContentCollectionEdit edit) {
		try {
			contentService().removeCollection(edit);
		} catch (Exception e) {
			log.error("Caught an exception removing a collection!", e);
		}
	}

	public void removeResource(ContentResourceEdit edit) {
		try {
			contentService().removeResource(edit);
		} catch (Exception e) {
			log.error("Caught an exception removing a resource!", e);
		}
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
	
	private ContentEntity resolveId(String id) {
		// Begin at the root element, since we can be fairly confident this is a real entity
		String path = String.valueOf(id);
		// Strip off any initial slashes
		if (path.startsWith(Entity.SEPARATOR) && path.length() > 1)
			path = path.substring(1);
		
		int firstSlash = path.indexOf(Entity.SEPARATOR);
		String shortId = path.substring(0, firstSlash);
		String finalId = path.substring(firstSlash);
		
		try {
			ContentEntity ce = contentService().getCollection(shortId);
			if (ce == null)
				ce = contentService().getResource(shortId);

			ContentEntity virtual = ce.getVirtualContentEntity();
		
			if (null != virtual) {
				return getVirtualContentEntity(ce, finalId);
			} 
			
			return resolveId(finalId);
		
		} catch (Exception iuue) {
			log.error(iuue);
			return null;
		}
	}
	
	
	// Return all of them
	private List getZipEntries(byte[] archive) {
		if (null == archive || archive.length <= 0)
			return null;
		
		ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(archive));
		ZipEntry entry;
		String entryName;
		List entries = new LinkedList();
		try {
			entry = (ZipEntry) zipStream.getNextEntry();
			while (entry != null) {
		    	entryName = entry.getName();
		    	entries.add(entryName);
		    	entry = (ZipEntry) zipStream.getNextEntry();
		    }
		} catch (IOException ioe) {
			log.error("Caught an io exception writing manifest file to temp space!", ioe);
			return null;
		} finally {
			try {
				if (null != zipStream)
					zipStream.close();
			} catch (IOException noie) {
				log.info("Caught an io exception closing streams!");
			}
		}
		
		return entries;
	}
	
	
	private ContentResource extractZippedResource(ContentEntity ce, String path) throws ServerOverloadException {
		ContentResource cr = (ContentResource)ce;
		
		byte[] archive = cr.getContent();
		
		if (archive == null || archive.length <= 0)
			return null;
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(archive));
		ZipEntry entry;
		String entryName;
		byte[] buffer = new byte[1024];
		int length;
		
		try {
			entry = (ZipEntry) zipStream.getNextEntry();
			while (entry != null) {
		    	entryName = entry.getName();
		    	if (entryName.endsWith(path)) {
		    		while ((length = zipStream.read(buffer)) > 0) {  
		    			outStream.write(buffer, 0, length);
		            }
		    		
		    		outStream.close();
		    		zipStream.closeEntry();
		    		zipStream.close();
		    		
		    		ContentResourceEdit resource = (ContentResourceEdit)resolver.newResourceEdit(newId(cr.getId(), path));
		    		
		    		byte[] resourceContent = outStream.toByteArray();
		    		resource.setContent(resourceContent);
		    		resource.setContentLength(resourceContent.length);
		    		resource.setResourceType(cr.getResourceType());
		    		
		    		ResourcePropertiesEdit props = resource.getPropertiesEdit();
					props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, entryName);
					props.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.client.api.ContentHostingHandler");

		    		
		    		return resource;
		    	}
		    	entry = (ZipEntry) zipStream.getNextEntry();
		    }
		} catch (IOException ioe) {
			log.error("Caught an io exception writing manifest file to temp space!", ioe);
			return null;
		} finally {
			try {
				if (null != outStream)
					outStream.close();
				if (null != zipStream)
					zipStream.close();
			} catch (IOException noie) {
				log.info("Caught an io exception closing streams!");
			}
		}
		
		return null;
	}
	
	private List<ContentEntity> extractZippedResources(ContentEntity ce, int depth) throws ServerOverloadException {
		List<ContentEntity> members = new LinkedList<ContentEntity>();
		Map<String, ContentEntity> directories = new HashMap<String, ContentEntity>();
		
		ContentResource cr = getRealResource(ce);
		byte[] archive = cr.getContent();
		
		if (archive == null || archive.length <= 0)
			return null;
		
		String thisid = ce.getId();
		String finalId = cr.getId();
		
		if (finalId.length() > thisid.length() && finalId.startsWith(thisid)) {
			String diffPath = finalId.substring(thisid.length());
		
			depth += checkDepth(diffPath);
		}
		
		ByteArrayOutputStream outStream = null;
		ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(archive));
		ZipEntry entry;
		byte[] buffer = new byte[1024];
		int length;
		
		try {
			entry = (ZipEntry) zipStream.getNextEntry();
			while (entry != null) {
		    	String path = entry.getName();
		    	boolean isDirectory = entry.isDirectory();
		    			
		    	// Strip off any trailing slashes
		    	if (path.endsWith(Entity.SEPARATOR) && path.length() > 1)
					path = path.substring(0, path.length() - 1);
	    		
		    	if (depth != -1 && checkDepth(path) == depth) {
		    		byte[] byteArray = null;
		    		if (!isDirectory) {
				    	outStream = new ByteArrayOutputStream();
				    	while ((length = zipStream.read(buffer)) > 0) {  
			    			outStream.write(buffer, 0, length);
			            }
			    		outStream.close();
			    		byteArray = outStream.toByteArray();
			    	} 
		    		
		    		ContentEntity entity = makeContentEntity(ce.getId(), path, byteArray, isDirectory);    	
			    	members.add(entity);
			    }
		    	entry = (ZipEntry) zipStream.getNextEntry();
		    }
		} catch (IOException ioe) {
			log.error("Caught an io exception writing manifest file to temp space!", ioe);
			return null;
		} finally {
			try {
				if (null != outStream)
					outStream.close();
				if (null != zipStream)
					zipStream.close();
			} catch (IOException noie) {
				log.info("Caught an io exception closing streams!");
			}
		}
		
		return members;
	}	
	
	private int checkDepth(String path) {
		int currentIndex = 0;
		int count = 1;
		
		currentIndex = path.indexOf(Entity.SEPARATOR);
		
		while (currentIndex != -1) {
			currentIndex = path.indexOf(Entity.SEPARATOR, currentIndex + 1);
			count++;
		}
		
		return count;
	}
	
	private String newId(String id, String path) {
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
	
	private ContentEntity makeContentEntity(String id, String path, byte[] content, boolean isDirectory) {
		ContentEntity ce = null;
			
		if (isDirectory)
			ce = makeCollection(id, path);
		else
			ce = makeResource(id, path, content);
		
		if (null != ce) {
			ce.setContentHandler(this);
		}
			
		return ce;
	}
	
	private ContentCollectionEdit makeCollection(String id, String path) {
		int lastSlash = path.lastIndexOf(Entity.SEPARATOR);
		String directoryName = path;
		if (lastSlash != -1 && path.length() > 1) {
			directoryName = path.substring(lastSlash + 1);
		}

		ContentCollectionEdit collection = (ContentCollectionEdit)resolver.newCollectionEdit(newId(id, path));
		
		ResourcePropertiesEdit props = collection.getPropertiesEdit();
		props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, directoryName);
		props.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.client.api.ContentHostingHandler");
		props.addProperty(ACTUAL_RESOURCE_ID_PROPERTY, id);
		props.addProperty(ResourceProperties.PROP_IS_COLLECTION, "true");
		
		return collection;
	}
	
	private ContentResourceEdit makeResource(String id, String path, byte[] content) {		
		int lastSlash = path.lastIndexOf(Entity.SEPARATOR);
		
		String fileName = path;
		if (lastSlash != -1 && path.length() > 1) {
			fileName = path.substring(lastSlash + 1);
		}
		
		ContentResourceEdit resource = (ContentResourceEdit)resolver.newResourceEdit(newId(id, path));
		
		resource.setContent(content);
		resource.setContentLength(content.length);
		resource.setResourceType(ResourceType.TYPE_HTML);
		
		ResourcePropertiesEdit props = resource.getPropertiesEdit();
		props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
		props.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.client.api.ContentHostingHandler");
		props.addProperty(ACTUAL_RESOURCE_ID_PROPERTY, id);
				
		return resource;
	}
	
	private String getZipEntry(byte[] archive, String path) {
		if (null == archive || archive.length <= 0)
			return null;
		
		ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(archive));
		ZipEntry entry;
		String entryName;
		
		try {
			entry = (ZipEntry) zipStream.getNextEntry();
			while (entry != null) {
		    	entryName = entry.getName();
		    	if (entryName.endsWith(path)) {    		
		    		zipStream.closeEntry();
		    		zipStream.close();
		    		return entryName;
		    	}
		    	entry = (ZipEntry) zipStream.getNextEntry();
		    }
		} catch (IOException ioe) {
			log.error("Caught an io exception writing manifest file to temp space!", ioe);
			return null;
		} finally {
			try {
				if (null != zipStream)
					zipStream.close();
			} catch (IOException noie) {
				log.info("Caught an io exception closing streams!");
			}
		}
		
		return null;
	}
	
	
	private byte[] extractZippedBytes(byte[] archive, String path) {
		if (null == archive || archive.length <= 0)
			return archive;
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(archive));
		ZipEntry entry;
		String entryName;
		byte[] buffer = new byte[1024];
		int length;
		
		try {
			entry = (ZipEntry) zipStream.getNextEntry();
			while (entry != null) {
		    	entryName = entry.getName();
		    	if (entryName.endsWith(path)) {
		    		while ((length = zipStream.read(buffer)) > 0) {  
		    			outStream.write(buffer, 0, length);
		            }
		    		
		    		outStream.close();
		    		zipStream.closeEntry();
		    		zipStream.close();
		    		return outStream.toByteArray();
		    	}
		    	entry = (ZipEntry) zipStream.getNextEntry();
		    }
		} catch (IOException ioe) {
			log.error("Caught an io exception writing manifest file to temp space!", ioe);
			return null;
		} finally {
			try {
				if (null != outStream)
					outStream.close();
				if (null != zipStream)
					zipStream.close();
			} catch (IOException noie) {
				log.info("Caught an io exception closing streams!");
			}
		}
		
		return null;
	}
	
	
	private ContentResource getRealResource(ContentEntity ce) {
		ContentResource realResource = null;
		ResourceProperties properties = ce.getProperties();	
		String realId = (String)properties.get(ACTUAL_RESOURCE_ID_PROPERTY);
		
		if (null != realId) {
			try {
				realResource = contentService().getResource(realId);
			} catch (Exception e) {
				log.error("Caught an exception getting the real resource for this virtual one", e);
			}
		} else if (ce instanceof ContentResource){
			realResource = (ContentResource)ce;
		}
		
		return realResource;
	}
	
	
	

}
