/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.service.standalone.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.exceptions.ResourceNotDeletedException;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.service.impl.AbstractResourceService;

public class StandaloneResourceService extends AbstractResourceService {

	private final String storagePath = "contentPackages";
	
	private static Log log = LogFactory.getLog(StandaloneResourceService.class);
	
	public Archive getArchive(String resourceId) {
		File dir = getContentPackageDirectory(resourceId);
		
		FilenameFilter zipFilter = new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".zip");
			}
		};
		
		FilenameFilter imsmanifestFilter = new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.equals("imsmanifest.xml");
			}
		};
		
		File[] files = dir.listFiles(zipFilter);
		
		File[] imsmanifests = dir.listFiles(imsmanifestFilter);
		
		if (files.length >= 1) {
			Archive archive = new Archive(resourceId, files[0].getName());
			
			if (imsmanifests.length >= 1)
				archive.setValidated(true);
			else
				archive.setValidated(false);
			
			archive.setMimeType("application/zip");
			archive.setPath(dir.getAbsolutePath());
			
			return archive;
		}
			
		return null;
	}

	public InputStream getArchiveStream(String resourceId) {
		File dir = getContentPackageDirectory(resourceId);
		
		FilenameFilter filter = new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".zip");
			}
		};
		
		File[] files = dir.listFiles(filter);
		
		if (files.length >= 1) {
			try {
				FileInputStream fis = new FileInputStream(files[0]);
				
				return fis;
			} catch (FileNotFoundException e) {
				log.error("Unable to create stream for zip file for resourceId: " + resourceId);
			}
		}
			
		return null;
	}
	
	public int getMaximumUploadFileSize() {
		return 2000;
	}
	
	public String getResourcePath(String resourceId, String launchLine) {
		/*File contentPackageDirectory = getContentPackageDirectory(resourceId);
		
		File resource = new File(contentPackageDirectory, launchLine);
		
		String path = resource.getAbsolutePath();
		
		return path.replace(" ", "%20");*/
		
		String fullPath = new StringBuilder().append(File.separatorChar).append(resourceId)
			.append(File.separatorChar).append(launchLine).toString();
		
		if (log.isDebugEnabled())
			log.debug("getResourcePath = " + fullPath);
		
		return fullPath.replace(" ", "%20");
	}

	public List<ContentPackageResource> getResources(String archiveResourceId) {
		File dir = getContentPackageDirectory(archiveResourceId);
		
		return getContentPackageFilesRecursive(archiveResourceId, dir, "");
	}

	public List<Archive> getUnvalidatedArchives() {
		return new LinkedList<Archive>();
	}
	
	public String putArchive(InputStream stream, String name, String mimeType, boolean isHidden) {
		String uuid = UUID.randomUUID().toString();
		
		String fileName = new StringBuilder(name).toString();
		File archiveFile = new File(getContentPackageDirectory(uuid), fileName);
		
		try {
			FileOutputStream fileStream = new FileOutputStream(archiveFile);
			
			byte[] buffer = new byte[1024];
			int length;
			
			while ((length = stream.read(buffer)) > 0) {  
				fileStream.write(buffer, 0, length);
            }
    		
			if (null != fileStream)
				fileStream.close();
			
		} catch (Exception e) {
			log.error("Unable to write archive to disk " + fileName, e);
		}
		
		return uuid;
	}
		
	public void removeArchive(String resourceId) {
		File dir = getContentPackageDirectory(resourceId);
		
		removeAll(dir);
	}

	public void removeResources(String collectionId)
			throws ResourceNotDeletedException {
		File dir = getContentPackageDirectory(collectionId);
		
		removeAll(dir);
	}
	
	protected String getContentPackageDirectoryPath(String uuid) {
		return getContentPackageDirectory(uuid).getAbsolutePath();
	}

	protected String getRootDirectoryPath() {
		return getRootDirectory().getAbsolutePath();
	}

	protected String newFolder(String uuid, ZipEntry entry) {
		File file = new File(getContentPackageDirectory(uuid), entry.getName());
		file.mkdir();
		
		return file.getAbsolutePath();
	}
	
	protected String newItem(String uuid, ZipInputStream zipStream, ZipEntry entry) {
		File file = new File(getContentPackageDirectory(uuid), entry.getName());
		
		FileOutputStream fileStream = null;
		try {
			fileStream = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int length;
			
			while ((length = zipStream.read(buffer)) > 0) {  
				fileStream.write(buffer, 0, length);
            }
		} catch (FileNotFoundException fnfe) {
			log.error("Could not write to file " + file.getAbsolutePath(), fnfe);
		} catch (IOException ioe) {
			log.error("Could not read from zip stream ", ioe);
		} finally {
			if (null != fileStream)
				try { fileStream.close(); } catch (IOException e) { log.error(e); }
		}
		
		return file.getAbsolutePath();
	}
	
	private File getRootDirectory() {
		File rootDir = new File(storagePath);
		
		return rootDir;
	}
	
	private File getContentPackageDirectory(String uuid) {
		File storageDir = new File(getRootDirectory(), uuid);
		// Ensure that this directory exists
		storageDir.mkdirs();
		
		return storageDir;
	}

	private void removeAll(File dir) {
		log.warn("Removing all files under the directory " + dir.getAbsolutePath());
	}
		
	private List<ContentPackageResource> getContentPackageFilesRecursive(String archiveResourceId, File directory, String path) {
		List<ContentPackageResource> files = new LinkedList<ContentPackageResource>();
		
		File[] filesInDirectory = directory.listFiles();
		
		for (File file : filesInDirectory) {
			StringBuilder pathBuilder = new StringBuilder(path);
			if (path.equals("")) 
				pathBuilder.append(file.getName());
			else 
				pathBuilder.append(File.separatorChar).append(file.getName());
				
			String filePath = pathBuilder.toString();
			
			if (file.isDirectory()) 
				files.addAll(getContentPackageFilesRecursive(archiveResourceId, file, filePath));
			else 
				files.add(new ContentPackageFile(archiveResourceId, getResourcePath(archiveResourceId, filePath), file));
		}
		
		return files;
	}


	
		
}
