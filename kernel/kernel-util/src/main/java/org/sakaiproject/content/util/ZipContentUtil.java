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
package org.sakaiproject.content.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.activation.MimetypesFileTypeMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;

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
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;

@SuppressWarnings({ "deprecation", "restriction" })
@Slf4j
public class ZipContentUtil {
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
    
    private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.ContentProperties";
    private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.content.content";
    private static final String RESOURCECLASS = "resource.class.content";
    private static final String RESOURCEBUNDLE = "resource.bundle.content";
	private static ResourceLoader rb = new Resource().getLoader(ServerConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS), ServerConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE));
	
    public static int getMaxZipExtractFiles() {
        if(MAX_ZIP_EXTRACT_FILES == null){
            MAX_ZIP_EXTRACT_FILES = ServerConfigurationService.getInt(org.sakaiproject.content.api.ContentHostingService.RESOURCES_ZIP_EXPAND_MAX,MAX_ZIP_EXTRACT_FILES_DEFAULT);
        }
        if (MAX_ZIP_EXTRACT_FILES <= 0) {
            MAX_ZIP_EXTRACT_FILES = MAX_ZIP_EXTRACT_FILES_DEFAULT; // any less than this is useless so probably a mistake
            log.warn("content.zip.expand.maxfiles is set to a value less than or equal to 0, defaulting to "+MAX_ZIP_EXTRACT_FILES_DEFAULT);
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
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		try {
			// Create the compressed archive in the filesystem
			ZipOutputStream out = null;
			try {
				temp = File.createTempFile("sakai_content-", ".tmp");
				ContentCollection collection = ContentHostingService.getCollection(reference.getId());
				out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(temp),BUFFER_SIZE),java.nio.charset.StandardCharsets.UTF_8);
				storeContentCollection(reference.getId(),collection,out);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
					}
				}
			}
			
			
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
			String displayName="";
			while(true){
				try{
					String newResourceId = resourceId;
					String newResourceName = resourceName;
					displayName=newResourceName;
					count++;
					if(count > 1){
						//previous naming convention failed, try another one
						newResourceId += "_" + count;
						newResourceName += "_" + count;
					}
					newResourceId += ZIP_EXTENSION;
					newResourceName += ZIP_EXTENSION;
					ContentCollectionEdit currentEdit;
					if(reference.getId().split(Entity.SEPARATOR).length>3 && ContentHostingService.isInDropbox(reference.getId())) {
						currentEdit = (ContentCollectionEdit) ContentHostingService.getCollection(resourceId + Entity.SEPARATOR);
						displayName = currentEdit.getProperties().getProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME);
						if (displayName != null && displayName.length() > 0) {
							displayName += ZIP_EXTENSION;
						}
						else {
							displayName = newResourceName;
						}
					}
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
			props.addProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME, displayName);
			ContentHostingService.commitResource(resourceEdit, NotificationService.NOTI_NONE);								
		}
		catch (PermissionException pE){
			addAlert(toolSession, rb.getString("permission_error_zip"));
			log.warn(pE.getMessage(), pE);
		}
		catch (Exception e) {
			addAlert(toolSession, rb.getString("generic_error_zip"));
			log.error(e.getMessage(), e);
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
					log.warn("failed to remove temp file");
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
			boolean extracted = false;
			for (String charsetName: getZipCharsets()) {
				Charset charset;
				try {
					charset = Charset.forName(charsetName);
				} catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
					log.warn(String.format("%s is not a legal charset.", charsetName));
					continue;
				}
				ZipFile zipFile = null;
				try {
					zipFile = new ZipFile(temp, charset);
					Enumeration<? extends ZipEntry> entries = zipFile.entries();
					while (entries.hasMoreElements()) {
						ZipEntry nextElement = entries.nextElement();
						if (!nextElement.getName().contains("__MACOSX")){
							if (nextElement.isDirectory()) {
								createContentCollection(rootCollectionId, nextElement);
							}
							else {
								if(!nextElement.getName().contains(".DS_Store")){
									createContentResource(rootCollectionId, nextElement, zipFile);
								}
							}
						}
					}
					extracted = true;
					break;
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					log.warn(String.format("Cannot extract archive %s with charset %s.", referenceId, charset));
				} finally {
					if (zipFile != null){
						zipFile.close();
					}
				}
			}
			if (!extracted) {
				log.warn(String.format("Cannot extract archives %s with any charset %s.", referenceId, getZipCharsets()));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
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
			boolean extracted = false;
			for (String charsetName: getZipCharsets()) {
				Charset charset;
				try {
					charset = Charset.forName(charsetName);
				} catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
					log.warn(String.format("%s is not a legal charset.", charsetName));
					continue;
				}
				ZipFile zipFile = null;
				try {
					zipFile = new ZipFile(temp, charset);
					Enumeration<? extends ZipEntry> entries = zipFile.entries();
					int i = 0;
					//use <= getMAX_ZIP_EXTRACT_SIZE() so the returned value will be
					//larger than the max and then rejected
					while (entries.hasMoreElements() && i <= getMaxZipExtractFiles()) {
						ZipEntry nextElement = entries.nextElement();						
						ret.put(nextElement.getName(), nextElement.getSize());
						i++;
					}
					extracted = true;
					break;
				} catch (Exception e) {
					log.warn(String.format("Cannot get menifest of %s with charset %s.", referenceId, charset));
				} finally {
					if (zipFile != null){
						zipFile.close();
					}
				}
			}
			if (!extracted) {
				log.warn(String.format("Cannot get menifest of %s with any charset %s.", referenceId, getZipCharsets()));
			}
		} 
		catch (Exception e) {
			log.error(e.getMessage(), e);
		} 
		finally {
			if (temp.exists()) {
				if (!temp.delete()) {
					log.warn("uanble to delete temp file!");	
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
		ContentResourceEdit resourceEdit;
		try {
			resourceEdit = ContentHostingService.addResource(resourceId);
		} catch (IdUsedException iue) {
			// resource exists, update instead
			log.debug("Content resource with ID " + resourceId + " exists. Editing instead.");
			resourceEdit = ContentHostingService.editResource(resourceId);
		}
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
		ContentCollectionEdit collection;
		try {
			collection = ContentHostingService.addCollection(resourceId);
		} catch (IdUsedException iue) {
			// collection exists, update instead
			log.debug("Content collection with ID " + resourceId + " exists. Editing instead.");
			collection = ContentHostingService.editCollection(resourceId);
		}
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
			log.error(e.getMessage(), e);
		} catch (ServerOverloadException e) {
			log.error(e.getMessage(), e);
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
		if (members.isEmpty()) storeEmptyFolder(rootId,collection,out);
		else {
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
	}
	
	/**
	 * Add an empty folder to the zip
	 * 
	 * @param rootId
	 * @param resource
	 * @param out
	 * @throws Exception
	 */
	private void storeEmptyFolder(String rootId, ContentCollection resource, ZipOutputStream out) throws Exception {		
		String folderName = resource.getId().substring(rootId.length(),resource.getId().length());
		ZipEntry zipEntry = new ZipEntry(folderName);
		out.putNextEntry(zipEntry);
		out.closeEntry();
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
		//Inorder to have username as the folder name rather than having eids
		if(ContentHostingService.isInDropbox(rootId) && ServerConfigurationService.getBoolean("dropbox.zip.haveDisplayname", true)) {
			try {
				filename = getContainingFolderDisplayName(rootId, filename);
			} catch(TypeException e){
				log.warn("Unexpected error occurred when trying to create Zip archive:" + extractName(rootId), e.getCause());
				return;
			} catch(IdUnusedException e ){
				log.warn("Unexpected error occurred when trying to create Zip archive:" + extractName(rootId), e.getCause());
				return;
			} catch(PermissionException e){
				log.warn("Unexpected error occurred when trying to create Zip archive:" + extractName(rootId), e.getCause());
				return;
			} catch (Exception e) {
				log.warn("Unexpected error occurred when trying to create Zip archive:" + extractName(rootId), e.getCause());
				return;
			}
		}
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
	
	private List<String> getZipCharsets() {
		String[] charsetConfig = ServerConfigurationService.getStrings("content.zip.expand.charsets");
		if (charsetConfig == null) {
			charsetConfig = new String[0];
		}
		List<String> charsets = new ArrayList<>(Arrays.asList(charsetConfig));
		// Add UTF-8 as fallback
		charsets.add("UTF-8");
		return charsets;
	}

	private String getContainingFolderDisplayName(String rootId,String filename) throws IdUnusedException, TypeException, PermissionException {
		//dont manipulate filename when you are a zip file from a root folder level
		if(!(rootId.split("/").length > 3) && (filename.split("/").length<2) &&filename.endsWith(".zip")){
			return filename;
		}

		String filenameArr[] = filename.split(Entity.SEPARATOR);

		//return rootId when you you zip from sub folder level and gives something like "group-user/site-id/user-id/" when zipping from root folder level by using filenameArr
		String contentEditStr = (rootId.split("/").length > 3)?rootId:rootId+filenameArr[0]+Entity.SEPARATOR;
		ContentCollectionEdit collectionEdit = (ContentCollectionEdit) ContentHostingService.getCollection(contentEditStr);
		ResourcePropertiesEdit props = collectionEdit.getPropertiesEdit();
		String displayName = props.getProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME);

		//returns displayname along with the filename for zipping from sub folder level
		if(contentEditStr.equals(rootId)) {
			return displayName +Entity.SEPARATOR+ filename;
		}
		else { // just replaces the user-id with the displayname and returns the filename
			return filename.replaceFirst(filenameArr[0],displayName);
		}

	}

}
