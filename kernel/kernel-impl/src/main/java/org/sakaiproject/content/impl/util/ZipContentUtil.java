package org.sakaiproject.content.impl.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.IOUtils;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;

public class ZipContentUtil {
	
	private static final String ZIP_EXTENSION = ".zip";
	private static final int BUFFER_SIZE = 8192;
	private static final MimetypesFileTypeMap mime = new MimetypesFileTypeMap();
	
	/**
	 * Compresses a ContentCollection to a new zip archive with the same folder name
	 * 
	 * @param reference
	 * @throws Exception
	 */
	public void compressFolder(Reference reference) throws Exception { 
		File temp = null;
		try {
			// Create the compressed archive in the filesystem
			temp = File.createTempFile("sakai_content-", ".tmp");
			temp.deleteOnExit(); 
			ContentCollection collection = ContentHostingService.getCollection(reference.getId());
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(temp),BUFFER_SIZE));			
			storeContentCollection(reference.getId(),collection,out);        		
			out.close();
			
			// Store the compressed archive in the repository
			String resourceId = reference.getId().substring(0,reference.getId().lastIndexOf(Entity.SEPARATOR))+ZIP_EXTENSION;
			String resourceName = extractName(resourceId);
			ContentResourceEdit resourceEdit = ContentHostingService.addResource(resourceId);	
			resourceEdit.setContent(new FileInputStream(temp));
			resourceEdit.setContentType(mime.getContentType(resourceId));
			ResourcePropertiesEdit props = resourceEdit.getPropertiesEdit();
			props.addProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME, resourceName);
			ContentHostingService.commitResource(resourceEdit, NotificationService.NOTI_NONE);								
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		finally {
			temp.delete();
		}
	}

	/**
	 * Extracts a compressed (zip) ContentResource to a new folder with the same name.
	 * 
	 * @param reference
	 * @throws Exception
	 */
	public void extractArchive(Reference reference) throws Exception {
		ContentResource resource = ContentHostingService.getResource(reference.getId());
		String rootCollectionId = extractZipCollectionPrefix(resource);

		// Prepare Collection
		ContentCollectionEdit rootCollection = ContentHostingService.addCollection(rootCollectionId);
		ResourcePropertiesEdit prop = rootCollection.getPropertiesEdit();
		prop.addProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME, extractZipCollectionName(resource));
		ContentHostingService.commitCollection(rootCollection);			
		
		// Extract Zip File	
		File temp = null;		
		try {
			temp = exportResourceToFile(resource);
			ZipFile zipFile = new ZipFile(temp,ZipFile.OPEN_READ);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry nextElement = entries.nextElement();						
				if (nextElement.isDirectory()) {					
					createContentCollection(rootCollectionId, nextElement);
				} 
				else { 
					createContentResource(rootCollectionId, nextElement, zipFile);				
				}
			}
			zipFile.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		finally {
			temp.delete();	
		}
		
	}

	/**
	 * Creates a new ContentResource extracted from ZipFile
	 * 
	 * @param rootCollectionId
	 * @param nextElement
	 * @param zipFile
	 * @throws Exception
	 */
	private void createContentResource(String rootCollectionId,
			ZipEntry nextElement, ZipFile zipFile) throws Exception {
		String resourceId = rootCollectionId + nextElement.getName();
		String resourceName = extractName(nextElement.getName());
		ContentResourceEdit resourceEdit = ContentHostingService.addResource(resourceId);	
		resourceEdit.setContent(zipFile.getInputStream(nextElement));
		resourceEdit.setContentType(mime.getContentType(resourceName));
		ResourcePropertiesEdit props = resourceEdit.getPropertiesEdit();
		props.addProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME, resourceName);
		ContentHostingService.commitResource(resourceEdit, NotificationService.NOTI_NONE);
	}

	/**
	 * Creates a new ContentCollection in the rootCollectionId with the element.getName()
	 * 
	 * @param rootCollectionId
	 * @param element
	 * @throws Exception
	 */
	private void createContentCollection(String rootCollectionId,
			ZipEntry element) throws Exception {
		String resourceId = rootCollectionId + element.getName();
		String resourceName = extractName(element.getName());
		ContentCollectionEdit collection = ContentHostingService.addCollection(resourceId);										
		ResourcePropertiesEdit props = collection.getPropertiesEdit();
		props.addProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME, resourceName);
		ContentHostingService.commitCollection(collection);
	}
	
	/**
	 * Exports a the ContentResource zip file to the operating system
	 * 
	 * @param resource
	 * @return
	 * @throws Exception
	 */
	private File exportResourceToFile(ContentResource resource) throws Exception {
		File temp = File.createTempFile("sakai_content-", ".tmp");
        temp.deleteOnExit();
    
        // Write content to file 
        FileOutputStream out = new FileOutputStream(temp);        
        IOUtils.copy(resource.streamContent(),out);
        out.flush();
        out.close();
        
        return temp;
	}
    
	/**
	 * Iterates the collection.getMembers() and streams content resources recursively to the ZipOutputStream
	 * 
	 * @param rootId
	 * @param collection
	 * @param out
	 * @throws Exception
	 */
	private void storeContentCollection(String rootId, ContentCollection collection, ZipOutputStream out) throws Exception {
		List<String> members = collection.getMembers();
		for (String memberId: members) {
			if (memberId.endsWith(Entity.SEPARATOR)) {
				ContentCollection memberCollection = ContentHostingService.getCollection(memberId);
				storeContentCollection(rootId,memberCollection,out);
			} 
			else {
				ContentResource resource = ContentHostingService.getResource(memberId);
				storeContentResource(rootId, resource, out);
			}
		}
	}

	/**
	 * Streams content resource to the ZipOutputStream
	 * 
	 * @param rootId
	 * @param resource
	 * @param out
	 * @throws Exception
	 */
	private void storeContentResource(String rootId, ContentResource resource, ZipOutputStream out) throws Exception {		
		String filename = resource.getId().substring(rootId.length(),resource.getId().length());				
		ZipEntry zipEntry = new ZipEntry(filename);
		zipEntry.setSize(resource.getContentLength());
		out.putNextEntry(zipEntry);
		InputStream contentStream = null;
		try {
			contentStream = resource.streamContent();
			IOUtils.copy(contentStream, out);
		} finally {
			if (contentStream != null) {
				contentStream.close();
			}
		}
	}
	
	private String extractZipCollectionPrefix(ContentResource resource) {
		String idPrefix = resource.getContainingCollection().getId() + 
			extractZipCollectionName(resource) +
			Entity.SEPARATOR;
		return idPrefix;
	}

	private String extractName(String collectionName) {
		String[] tmp = collectionName.split(Entity.SEPARATOR);
		return tmp[tmp.length-1];
	}
	
	private String extractZipCollectionName(ContentResource resource) {
		String tmp = extractName(resource.getId());
		return tmp.substring(0, tmp.lastIndexOf("."));
	}
}