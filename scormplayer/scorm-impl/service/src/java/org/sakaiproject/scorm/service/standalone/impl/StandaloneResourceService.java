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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.scorm.exceptions.ResourceNotFoundException;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.impl.AbstractResourceService;

public abstract class StandaloneResourceService extends AbstractResourceService {

	private final String storagePath = "contentPackages";
	
	private static Log log = LogFactory.getLog(StandaloneResourceService.class);
	
	protected abstract IdManager idManager();
	
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
	
	public ContentPackageManifest getManifest(String manifestResourceId) { 
		File manifestFile = new File(manifestResourceId);
		
		ObjectInputStream ois = null;
		ContentPackageManifest manifest = null;
		
		try {
			ois = new ObjectInputStream(new FileInputStream(manifestFile));		
			manifest = (ContentPackageManifest)ois.readObject();
		} catch (Exception e) {
			log.error("Unable to unserialize the manifest object located at " + manifestResourceId, e);
		} finally {
			if (ois != null)
				try { ois.close(); } catch (IOException ioe) { log.error(ioe); }
		}
		
		return manifest;
	}
	
	public ContentPackageResource getResource(SessionBean sessionBean) {
		
		if (null != sessionBean.getLaunchData()) {
			return getResource(sessionBean.getContentPackage().getResourceId(), sessionBean.getLaunchData().getLaunchLine());		
		}
		
		return null;
	}
	
	public ContentPackageResource getResource(String resourceId, String path) {
		
		String cleanPath = path;
		
		log.info("Looking up resource from " + resourceId + " with path " + path);
		
		try {
			cleanPath = (null == path) ? "" : URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			log.error("Failed to decode the path for this resource " + path, uee);
		}
		
		File file = new File(getContentPackageDirectory(resourceId), cleanPath);

		return new ContentPackageFile(resourceId, path, file);
	}
	
	public List<ContentPackageResource> getResources(String archiveResourceId) {
		File dir = getContentPackageDirectory(archiveResourceId);
		
		return getContentPackageFilesRecursive(archiveResourceId, dir, "");
	}
	
	
	/*public String getUrl(SessionBean sessionBean) {
		if (sessionBean == null || sessionBean.getLaunchData() == null)
			return null;
		
		String path = sessionBean.getLaunchData().getLaunchLine();
		
		return "contentPackages/resourceId/" + sessionBean.getCourseId() + "/path/" + path;
	}*/
	
	
	public String putArchive(InputStream stream, String name, String mimeType, boolean isHidden) {
		String uuid = idManager().createUuid();
		
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
	
	
	public String putManifest(String resourceId, ContentPackageManifest manifest) {
		File manifestFile = new File(getContentPackageDirectory(resourceId), "manifest.obj");
        FileOutputStream fos;
		try {
			fos = new FileOutputStream(manifestFile);
		
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(manifest);
	        oos.close();
	        fos.close();
		} catch (Exception e) {
			log.error("Unable to serialize manifest file to disk", e);
		}
        return manifestFile.getPath();
	}
	
	
	public String removeArchive(String resourceId) {
		File dir = getContentPackageDirectory(resourceId);
		
		removeAll(dir);
		
		return null;
	}
	
	public void removeManifest(String resourceId, String manifestResourceId) {
		// This doesn't need to be implemented in the case of webapp, since the manifest is stored under the same dir
		// as the unpacked archive.
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
	
	/*
	private void unzip(File rootDir, ZipInputStream zipStream) {
		ZipEntry entry;
		byte[] buffer = new byte[1024];
		int length;
		try {
			entry = (ZipEntry) zipStream.getNextEntry();
			while (entry != null) {
				File file = new File(rootDir, entry.getName());
				
				if (entry.isDirectory())
					file.mkdir();
				else {
					FileOutputStream fileStream = new FileOutputStream(file);
					
					while ((length = zipStream.read(buffer)) > 0) {  
						fileStream.write(buffer, 0, length);
		            }
		    		
					if (null != fileStream)
						fileStream.close();
				}
		    	entry = (ZipEntry) zipStream.getNextEntry();
		    }
		} catch (IOException ioe) {
			log.error("Caught an io exception reading from zip stream", ioe);
		} finally {
			try {
				if (null != zipStream)
					zipStream.close();
			} catch (IOException noie) {
				log.info("Caught an io exception closing streams!");
			}
		}
	}*/


	public int getMaximumUploadFileSize() {
		return 2000;
	}

	public List<Archive> getUnvalidatedArchives() {
		return new LinkedList<Archive>();
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
