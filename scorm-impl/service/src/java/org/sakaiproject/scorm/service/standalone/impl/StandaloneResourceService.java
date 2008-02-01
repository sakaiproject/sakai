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
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.service.impl.AbstractResourceService;

public abstract class StandaloneResourceService extends AbstractResourceService {

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
	
	
	protected String newFolder(String parentPath, ZipEntry entry) {
		File file = new File(parentPath, entry.getName());
		file.mkdir();
		
		return file.getAbsolutePath();
	}
	
	protected String newItem(String parentPath, ZipInputStream zipStream, ZipEntry entry) {
		File file = new File(parentPath, entry.getName());
		
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
	
	private File getContentPackageDirectory(String uuid) {
		File storageDir = new File(storagePath, uuid);
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
			String filePath = new StringBuilder(path).append(File.separatorChar)
				.append(file.getName()).toString();
			
			if (file.isDirectory()) 
				files.addAll(getContentPackageFilesRecursive(archiveResourceId, file, filePath));
			else 
				files.add(new ContentPackageFile(archiveResourceId, filePath, file));
		}
		
		return files;
	}
		
}
