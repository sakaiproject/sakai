package org.sakaiproject.content.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;

@SuppressWarnings({ "deprecation", "restriction" })
public class ZipContentUtil {
	
	protected static final Log LOG = LogFactory.getLog(ZipContentUtil.class);
	private static final String ZIP_EXTENSION = ".zip";
	private static final int BUFFER_SIZE = 32000;
	private static final MimetypesFileTypeMap mime = new MimetypesFileTypeMap();
	public static final String PREFIX = "resources.";
	public static final String REQUEST = "request.";
	private static final String STATE_HOME_COLLECTION_ID = PREFIX + REQUEST + "collection_home";
	private static final String STATE_HOME_COLLECTION_DISPLAY_NAME = PREFIX + REQUEST + "collection_home_display_name";
	public static final String STATE_MESSAGE = "message";
	/**
	 * Maximum number of files to extract from a zip archive (1000)
	 */
    public static final int MAX_ZIP_EXTRACT_FILES_DEFAULT = 1000;
	private static Integer MAX_ZIP_EXTRACT_FILES;
    
    public static int getMaxZipExtractFiles() {
        if(MAX_ZIP_EXTRACT_FILES == null){
            MAX_ZIP_EXTRACT_FILES = ServerConfigurationService.getInt(org.sakaiproject.content.api.ContentHostingService.RESOURCES_ZIP_EXPAND_MAX,MAX_ZIP_EXTRACT_FILES_DEFAULT);
        }
        if (MAX_ZIP_EXTRACT_FILES <= 0) {
            MAX_ZIP_EXTRACT_FILES = MAX_ZIP_EXTRACT_FILES_DEFAULT; // any less than this is useless so probably a mistake
            LOG.warn("content.zip.expand.maxfiles is set to a value less than or equal to 0, defaulting to "+MAX_ZIP_EXTRACT_FILES_DEFAULT);
        }
        return MAX_ZIP_EXTRACT_FILES;
    }

	/**
	 * Compresses a ContentCollection to a new zip archive with the same folder name
	 * 
	 * @param reference sakai entity reference
	 * @throws Exception on failure
	 */
    public void compressFolder(Reference reference) { 
		File temp = null;
		FileInputStream fis = null;
		ZipOutputStream out = null;
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		try {
			// Create the compressed archive in the filesystem
			temp = File.createTempFile("sakai_content-", ".tmp");
			temp.deleteOnExit(); 
			ContentCollection collection = ContentHostingService.getCollection(reference.getId());
			out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(temp),BUFFER_SIZE));			
			storeContentCollection(reference.getId(),collection,out);        		
			out.close();
			
			
			// Store the compressed archive in the repository
			String resourceId = reference.getId().substring(0,reference.getId().lastIndexOf(Entity.SEPARATOR));
			String resourceName = extractName(resourceId);			
			String homeCollectionId = (String) toolSession.getAttribute(STATE_HOME_COLLECTION_ID);
			if(homeCollectionId != null && homeCollectionId.equals(reference.getId())){
				//place the zip file into the home folder of the resource tool
				resourceId = reference.getId() + resourceName;
				
				String homeName = (String) toolSession.getAttribute(STATE_HOME_COLLECTION_DISPLAY_NAME);
				if(homeName != null){
					resourceName = homeName;
				}				
			}
			int count = 0;
			ContentResourceEdit resourceEdit = null;
			while(true){
				try{
					String newResourceId = resourceId;
					String newResourceName = resourceName;
					count++;
					if(count > 1){
						//previous naming convention failed, try another one
						newResourceId += "_" + count;
						newResourceName += "_" + count;
					}
					newResourceId += ZIP_EXTENSION;
					newResourceName += ZIP_EXTENSION;
					resourceEdit = ContentHostingService.addResource(newResourceId);
					//success, so keep track of name/id
					resourceId = newResourceId;
					resourceName = newResourceName;
					break;
				}catch(IdUsedException e){
					//do nothing, just let it loop again
				}catch(Exception e){
					throw new Exception(e);
				}
			}
			fis = new FileInputStream(temp);
			resourceEdit.setContent(fis);
			resourceEdit.setContentType(mime.getContentType(resourceId));
			ResourcePropertiesEdit props = resourceEdit.getPropertiesEdit();
			props.addProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME, resourceName);
			ContentHostingService.commitResource(resourceEdit, NotificationService.NOTI_NONE);								
		}
		catch (PermissionException pE){
			addAlert(toolSession, "You do not have the proper permissions for compressing to zip archive");
			LOG.warn(pE);
		}
		catch (Exception e) {
			addAlert(toolSession, "An error has occurred while compressing to zip archive");
			LOG.error(e);
		} 
		finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
			if (temp != null && temp.exists()) { 
				if (!temp.delete()) {
					LOG.warn("failed to remove temp file");
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private void addAlert(ToolSession toolSession, String alert){
		String errorMessage = (String) toolSession.getAttribute(STATE_MESSAGE);
		if(errorMessage == null){
			errorMessage = alert;
		}else{
			errorMessage += "\n\n" + alert;
		}
		toolSession.setAttribute(STATE_MESSAGE, errorMessage);
	}

	/**
     * Extracts a compressed (zip) ContentResource to a new folder with the same name.
     * 
     * @param reference the sakai entity reference
     * @throws Exception on failure
     * @deprecated 11 Oct 2011 -AZ, use {@link #extractArchive(String)} instead
     */
    public void extractArchive(Reference reference) throws Exception {
        if (reference == null) {
            throw new IllegalArgumentException("reference cannot be null");
        }
        extractArchive(reference.getId());
    }

	/**
	 * Extracts a compressed (zip) ContentResource to a new folder with the same name.
	 * 
     * @param referenceId the sakai entity reference id
	 * @throws Exception on failure
	 */
	public void extractArchive(String referenceId) throws Exception {
		ContentResource resource = ContentHostingService.getResource(referenceId);
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
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			temp.delete();	
		}
		
	}

   /**
     * Get a list of the files in a zip and their size
     * @param reference the sakai entity reference
     * @return a map of file names to file sizes in the zip archive
     * @deprecated 11 Oct 2011 -AZ, use {@link #getZipManifest(String)}
     */
    public Map<String, Long> getZipManifest(Reference reference) {
        if (reference == null) {
            throw new IllegalArgumentException("reference cannot be null");
        }
        return getZipManifest(reference.getId());
    }

	/**
	 * Get a list of the files in a zip and their size
	 * @param referenceId the sakai entity reference id
     * @return a map of file names to file sizes in the zip archive
	 */
	public Map<String, Long> getZipManifest(String referenceId) {
		Map<String, Long> ret = new HashMap<String, Long>();
		ContentResource resource;
		try {
			resource = ContentHostingService.getResource(referenceId);
		} catch (PermissionException e1) {
			return null;
		} catch (IdUnusedException e1) {
			return null;
		} catch (TypeException e1) {
			return null;
		}
		//String rootCollectionId = extractZipCollectionPrefix(resource);
		
		// Extract Zip File	
		File temp = null;		
		try {
			temp = exportResourceToFile(resource);
			ZipFile zipFile = new ZipFile(temp,ZipFile.OPEN_READ);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			int i = 0;
			//use <= getMAX_ZIP_EXTRACT_SIZE() so the returned value will be
			//larger than the max and then rejected
			while (entries.hasMoreElements() && i <= getMaxZipExtractFiles()) {
				ZipEntry nextElement = entries.nextElement();						
				ret.put(nextElement.getName(), nextElement.getSize());
				i++;
			}
			zipFile.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		finally {
			if (temp.exists()) {
				if (!temp.delete()) {
					LOG.warn("uanble to delete temp file!");	
				}
			}
		}
		
		return ret;
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
	 */
	private File exportResourceToFile(ContentResource resource) {
		File temp = null;
		FileOutputStream out = null;
		try {
			temp = File.createTempFile("sakai_content-", ".tmp");

			temp.deleteOnExit();

			// Write content to file 
			out = new FileOutputStream(temp);        
			IOUtils.copy(resource.streamContent(),out);
			out.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServerOverloadException e) {
			e.printStackTrace();
		}
		finally {
			if (out !=null) {
				try {
					out.close();
				} catch (IOException e) {
					
				}
			}
		}
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