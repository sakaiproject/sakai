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
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class ZipContentUtil {
    private static final String STATE_MESSAGE = "message";

    public static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.content.content";
    public static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.ContentProperties";
    public static final String RESOURCEBUNDLE = "resource.bundle.content";
    public static final String RESOURCECLASS = "resource.class.content";
    public static final String ZIP_EXTENSION = ".zip";
    public static final int BUFFER_SIZE = 32000;
    public static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;
    public static final int MAX_ZIP_EXTRACT_FILES_DEFAULT = 1000;

    private final ContentHostingService contentHostingService;
    private final ServerConfigurationService serverConfigurationService;
    private final SessionManager sessionManager;

    @Getter
    private Integer maxZipExtractFiles;
    private final MimetypesFileTypeMap mime;
    private final ResourceLoader resourceLoader;

	public ZipContentUtil(ContentHostingService contentHostingService,
						  ServerConfigurationService serverConfigurationService,
						  SessionManager sessionManager) {
		Objects.requireNonNull(contentHostingService, "contentHostingService must not be null");
		Objects.requireNonNull(serverConfigurationService, "serverConfigurationService must not be null");
		Objects.requireNonNull(sessionManager, "sessionManager must not be null");

		this.contentHostingService = contentHostingService;
		this.serverConfigurationService = serverConfigurationService;
		this.sessionManager = sessionManager;

		resourceLoader = Resource.getResourceLoader(
				serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS),
				serverConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE));

		mime = new MimetypesFileTypeMap();
		maxZipExtractFiles = serverConfigurationService.getInt(ContentHostingService.RESOURCES_ZIP_EXPAND_MAX, MAX_ZIP_EXTRACT_FILES_DEFAULT);
		if (maxZipExtractFiles <= 0) {
			maxZipExtractFiles = MAX_ZIP_EXTRACT_FILES_DEFAULT;
			log.warn("{} is set to a value less than or equal to 0, defaulting to {}", ContentHostingService.RESOURCES_ZIP_EXPAND_MAX, MAX_ZIP_EXTRACT_FILES_DEFAULT);
		}
	}

    public void compressSelectedResources(String siteId, String siteTitle, List<String> selectedFolderIds, List<String> selectedFiles, HttpServletResponse response) {
		Map<String, ContentResource> resourcesToZip = new HashMap<>();

		try {
			// Add any files in the selected folders to the files to be in the zip.
			if (!selectedFolderIds.isEmpty()) {
				for (String selectedFolder : selectedFolderIds) {
					List<ContentResource> folderContents = contentHostingService.getAllResources(selectedFolder);
					for (ContentResource folderFile : folderContents) {
						resourcesToZip.put(folderFile.getId(), folderFile);
					}
				}
			}

			// Add any selected files to the list of resources to be in the zip.
			for (String selectedFile : selectedFiles) {
				ContentResource contentFile = contentHostingService.getResource(selectedFile);
				resourcesToZip.put(contentFile.getId(), contentFile);
			}
		} catch (IdUnusedException | PermissionException | TypeException e) {
			// shouldn't happen by this stage.
			log.error(e.getMessage(), e);
		}

		try (OutputStream zipOut = response.getOutputStream(); ZipOutputStream out = new ZipOutputStream(zipOut)) {
			// If in dropbox need to add the word Dropbox to the end of the zip filename - use the first entry in the resourcesToZip map to find if we are in the dropthe user ID.
			if (!resourcesToZip.isEmpty()) {
				String firstContentResourceId = resourcesToZip.entrySet().iterator().next().getKey();
				if (contentHostingService.isInDropbox(firstContentResourceId) && serverConfigurationService.getBoolean("dropbox.zip.haveDisplayname", true)) {
					response.setHeader("Content-disposition", "inline; filename=\"" + siteId + "DropBox.zip\"");
				} else {
					response.setHeader("Content-disposition", "inline; filename=\"" + siteTitle + ".zip\"");
				}
			} else {
				// Return an empty zip.
				response.setHeader("Content-disposition", "inline; filename=\"" + siteTitle + ".zip\"");
			}
			response.setContentType("application/zip");

			for (ContentResource contentResource : resourcesToZip.values()) {
				// User sites does not contain "~" prefix before user id when retrieving files from resources, so we remove it
				siteId = StringUtils.replace(siteId, "~", "");
				// Find the file path.
				int siteIdPosition = contentResource.getId().indexOf(siteId);
				String rootId = contentResource.getId().substring(0, siteIdPosition) + siteId + "/";
				storeContentResource(rootId, contentResource, out);
			}
		} catch (Exception e) {
			log.warn("Could not compress files {}, in site {}", selectedFiles, siteId, e);
		}
	}
	/**
	 * Compresses a ContentCollection to a new zip archive with the same folder name
	 * 
	 * @param reference sakai entity reference
	 * @throws Exception on failure
	 */
    public void compressFolder(Reference reference) { 
		File temp = null;
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		try {
			// Create the compressed archive in the filesystem
			ZipOutputStream out = null;
			try {
				temp = File.createTempFile("sakai_content-", ".tmp");
				ContentCollection collection = contentHostingService.getCollection(reference.getId());
				out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(temp), BUFFER_SIZE), java.nio.charset.StandardCharsets.UTF_8);
				out.setLevel(serverConfigurationService.getInt("zip.compression.level", 1));
				storeContentCollection(reference.getId(), collection, out);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						log.warn("failed to close zip output stream");
					}
				}
			}

			// Get the name of the parent collection
			ContentCollection collection = contentHostingService.getCollection(reference.getId());
			ResourceProperties collectionProps = collection.getProperties();
			String displayName = collectionProps.getProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME);
			String resourceName = displayName + ZIP_EXTENSION;

			// Set the display name prop
			ResourcePropertiesEdit props = contentHostingService.newResourceProperties();
			props.addProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME, resourceName);

			// Store the compressed archive in the repository
			try (FileInputStream fis = new FileInputStream(temp)) {
				contentHostingService.addResource(resourceName, reference.getId(), MAXIMUM_ATTEMPTS_FOR_UNIQUENESS,
						"application/zip", fis, props, null, false, null, null,
						NotificationService.NOTI_NONE);
			} catch (OverQuotaException oqe) {
				addAlert(toolSession, resourceLoader.getString("overquota_error_zip"));
				log.warn(oqe.toString(), oqe);
			} catch (PermissionException pe) {
				addAlert(toolSession, resourceLoader.getString("permission_error_zip"));
				log.warn(pe.toString(), pe);
			}
		} catch (Exception e) {
			addAlert(toolSession, resourceLoader.getString("generic_error_zip"));
			log.error(e.toString(), e);
		} 
		finally {
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
     * @param referenceId the sakai entity reference id
	 * @throws Exception on failure
	 */
	public void extractArchive(String referenceId) throws Exception {
		ContentResource resource = contentHostingService.getResource(referenceId);
		String rootCollectionId = extractZipCollectionPrefix(resource);

		// Prepare Collection
		ContentCollectionEdit rootCollection = contentHostingService.addCollection(rootCollectionId);
		ResourcePropertiesEdit prop = rootCollection.getPropertiesEdit();
		prop.addProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME, extractZipCollectionName(resource));
		contentHostingService.commitCollection(rootCollection);
		
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
					log.warn("{} is not a legal charset", charsetName);
					continue;
				}
                try (ZipFile zipFile = new ZipFile(temp, charset)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry nextElement = entries.nextElement();
                        if (!nextElement.getName().contains("__MACOSX")) {
                            if (nextElement.isDirectory()) {
                                createContentCollection(rootCollectionId, nextElement);
                            } else {
                                if (!nextElement.getName().contains(".DS_Store")) {
                                    createContentResource(rootCollectionId, nextElement, zipFile);
                                }
                            }
                        }
                    }
                    extracted = true;
                    break;
                } catch (Exception e) {
                    log.warn("Cannot extract archive {} with charset {}", referenceId, charset, e);
                }
			}
			if (!extracted) {
				log.warn("Cannot extract archives {} with any charset {}", referenceId, getZipCharsets());
			}
		} catch (Exception e) {
			log.warn("failure extracting archive for reference {}", referenceId, e);
		} finally {
			if (temp != null && temp.exists()) {
				temp.delete();
			}
		}
	}

   /**
     * Get a list of the files in a zip and their size
     * @param reference the sakai entity reference
     * @return a map of file names to file sizes in the zip archive
     * @deprecated 11 Oct 2011 -AZ, use {@link #getZipManifest(String)}
     */
   @Deprecated
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
		Map<String, Long> fileNameSizes = new HashMap<>();
		ContentResource resource;
		try {
			resource = contentHostingService.getResource(referenceId);
		} catch (PermissionException | IdUnusedException | TypeException e1) {
			return null;
		}

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
                try (ZipFile zipFile = new ZipFile(temp, charset)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    int i = 0;
                    //use <= getMAX_ZIP_EXTRACT_SIZE() so the returned value will be
                    //larger than the max and then rejected
                    while (entries.hasMoreElements() && i <= getMaxZipExtractFiles()) {
                        ZipEntry nextElement = entries.nextElement();
                        fileNameSizes.put(nextElement.getName(), nextElement.getSize());
                        i++;
                    }
                    extracted = true;
                    break;
                } catch (Exception e) {
                    log.warn("Cannot get manifest of {} with charset {}", referenceId, charset);
                }
			}
			if (!extracted) {
				log.warn("Cannot get menifest of {} with any charset {}", referenceId, getZipCharsets());
			}
		} 
		catch (Exception e) {
			log.warn("Could not get zip manifest, {}", e.toString());
		} 
		finally {
			if (temp != null && temp.exists()) {
				if (!temp.delete()) {
					log.warn("uanble to delete temp file!");	
				}
			}
		}
		return fileNameSizes;
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
			resourceEdit = contentHostingService.addResource(resourceId);
		} catch (IdUsedException iue) {
			// resource exists, update instead
			log.debug("Content resource with ID " + resourceId + " exists. Editing instead.");
			resourceEdit = contentHostingService.editResource(resourceId);
		}
		resourceEdit.setContent(zipFile.getInputStream(nextElement));
		resourceEdit.setContentType(mime.getContentType(resourceName));
		ResourcePropertiesEdit props = resourceEdit.getPropertiesEdit();
		props.addProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME, resourceName);
		contentHostingService.commitResource(resourceEdit, NotificationService.NOTI_NONE);
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
			collection = contentHostingService.addCollection(resourceId);
		} catch (IdUsedException iue) {
			// collection exists, update instead
			log.debug("Content collection with ID " + resourceId + " exists. Editing instead.");
			collection = contentHostingService.editCollection(resourceId);
		}
		ResourcePropertiesEdit props = collection.getPropertiesEdit();
		props.addProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME, resourceName);
		contentHostingService.commitCollection(collection);
	}
	
	/**
	 * Exports a ContentResource to a temporary file.
	 *
	 * @param resource The ContentResource to export
	 * @return The temporary file containing the resource content, or null if export failed
	 */
	private File exportResourceToFile(ContentResource resource) {
		if (resource == null) {
			log.warn("Cannot export null resource to file");
			return null;
		}

		File tempFile = null;

		try {
			// Create temporary file
			tempFile = File.createTempFile("sakai_content-", ".tmp");

			// Write content to file
			try (InputStream contentStream = resource.streamContent();
				 FileOutputStream outputStream = new FileOutputStream(tempFile)) {

				IOUtils.copy(contentStream, outputStream);
				return tempFile;
			}
		} catch (IOException ioe) {
			log.warn("Could not copy resource [{}] to temp file", resource.getId(), ioe);

			// Clean up the temp file if there was an error
			if (tempFile != null && tempFile.exists()) {
				if (!tempFile.delete()) {
					log.debug("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
				}
			}
		} catch (Exception e) {
			log.warn("Unexpected error exporting resource [{}] to file", resource.getId(), e);
		}
		return null;
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
					ContentCollection memberCollection = contentHostingService.getCollection(memberId);
					storeContentCollection(rootId,memberCollection,out);
				} 
				else {
					ContentResource resource = contentHostingService.getResource(memberId);
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
		String folderName = resource.getId().substring(rootId.length());
		if(contentHostingService.isInDropbox(rootId) && serverConfigurationService.getBoolean("dropbox.zip.haveDisplayname", true)) {
			try {
				folderName = getContainingFolderDisplayName(rootId, folderName);
			} catch (Exception e) {
				log.warn("Unexpected error when trying to create empty folder for Zip archive {} : {}", extractName(rootId), e.getMessage());
				return;
			}
		}
		folderName = this.replaceIllegalFilenameCharacters(folderName);
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
		String filename = resource.getId().substring(rootId.length());
		// Inorder to have username as the folder name rather than having eids
		if(contentHostingService.isInDropbox(rootId) && serverConfigurationService.getBoolean("dropbox.zip.haveDisplayname", true)) {
			try {
				filename = getContainingFolderDisplayName(rootId, filename);
			} catch (Exception e) {
				log.warn("Unexpected error occurred when trying to create zip archive [{}]", rootId, e);
				return;
			}
		}
		filename = this.replaceIllegalFilenameCharacters(filename);
		ZipEntry zipEntry = new ZipEntry(filename);
		zipEntry.setSize(resource.getContentLength());
		out.putNextEntry(zipEntry);
        try (InputStream contentStream = resource.streamContent()) {
            IOUtils.copy(contentStream, out);
        }
	}
	
	private String extractZipCollectionPrefix(ContentResource resource) {
        return resource.getContainingCollection().getId() +
            extractZipCollectionName(resource) +
            Entity.SEPARATOR;
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
		String[] charsetConfig = serverConfigurationService.getStrings("content.zip.expand.charsets");
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
		if(!(rootId.split("/").length > 3) && (filename.split("/").length<2) && filename.endsWith(".zip")){
			return filename;
		}

		String[] filenameArr = filename.split(Entity.SEPARATOR);

		//return rootId when you you zip from sub folder level and gives something like "group-user/site-id/user-id/" when zipping from root folder level by using filenameArr
		String contentEditStr = (rootId.split("/").length > 3)?rootId:rootId+filenameArr[0]+Entity.SEPARATOR;
		ContentCollectionEdit collectionEdit = (ContentCollectionEdit) contentHostingService.getCollection(contentEditStr);
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

	private String replaceIllegalFilenameCharacters(String fileName) {
		// Replace any illegal character in the filename
		return fileName.replaceAll("[^a-zA-Z0-9\\.\\-\\\\/]", "_");
	}

}
