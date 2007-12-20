package org.sakaiproject.scorm.service.standalone.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.ScormResourceService;

public abstract class StandaloneResourceService implements ScormResourceService {

	private final String storagePath = "contentPackages";
	
	private static Log log = LogFactory.getLog(StandaloneResourceService.class);
	
	protected abstract IdManager idManager();
	
	public void convertArchive(String resourceId, String manifestResourceId) {
		
	}
	
	public ContentPackageManifest getManifest(String resourceId, String manifestResourceId) { 
		File manifestFile = null;
		if (manifestResourceId == null) {
			manifestFile = new File(getContentPackageDirectory(resourceId), "manifest.obj");
		} else {
			manifestFile = new File(manifestResourceId);
		}
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(manifestFile));
		
			ContentPackageManifest manifest = (ContentPackageManifest)ois.readObject();
		
			return manifest;
		} catch (Exception e) {
			log.error("Unable to unserialize the manifest object for " + resourceId, e);
		}
		
		return null;
	}
	
	public ContentPackageResource getResource(SessionBean sessionBean) {
		
		if (null != sessionBean.getLaunchData()) {
			return getResource(sessionBean.getCourseId(), sessionBean.getLaunchData().getLaunchLine());		
		}
		
		return null;
	}
	
	public ContentPackageResource getResource(String resourceId, String path) {
		
		String cleanPath = path;
		
		try {
			cleanPath = (null == path) ? "" : URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			log.error("Failed to decode the path for this resource " + path, uee);
		}
		
		File file = new File(getContentPackageDirectory(resourceId), cleanPath);
		
		InputStream inputStream = null;
		
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			log.error("Failed to retrieve file at " + file, e);
		}
		
		String mimeType = new MimetypesFileTypeMap().getContentType(file);
		
		if (file.getName().endsWith(".css"))
			mimeType = "text/css";
		else if (file.getName().endsWith(".swf"))
			mimeType = "application/x-Shockwave-Flash";
		
		log.info("Returning resource from " + file.getAbsolutePath() + " with mime type " + mimeType);
		
		return new ContentPackageResource(inputStream, mimeType);
	}
	
	public String getUrl(SessionBean sessionBean) {
		if (sessionBean == null || sessionBean.getLaunchData() == null)
			return null;
		
		String path = sessionBean.getLaunchData().getLaunchLine();
		
		return "contentPackages/resourceId/" + sessionBean.getCourseId() + "/path/" + path;
	}
	
	
	public String putArchive(InputStream stream, String name, String mimeType) {
		String uuid = idManager().createUuid();
		File dir = getContentPackageDirectory(uuid);
		
		unzip(dir, new ZipInputStream(stream));
		
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
	}
	

}
