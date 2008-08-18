package org.sakaiproject.scorm.service.impl;

import java.io.IOException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.exceptions.InvalidArchiveException;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.service.api.ScormResourceService;

public abstract class AbstractResourceService implements ScormResourceService {

	private static Log log = LogFactory.getLog(AbstractResourceService.class);
	
	
	public String convertArchive(String resourceId, String title) throws InvalidArchiveException {
		return unpack(resourceId);
	}
	
	public String getResourcePath(String resourceId, String launchLine) {
		StringBuilder pathBuilder = new StringBuilder();
		
		if (launchLine.startsWith("/"))
			launchLine = launchLine.substring(1);
		
		pathBuilder.append(getContentPackageDirectoryPath(resourceId));
		pathBuilder.append(launchLine);
		
		return pathBuilder.toString().replace(" ", "%20");
	}
	
	protected String getMimeType(String name) {
		String mimeType = new MimetypesFileTypeMap().getContentType(name);
		
		if (name.endsWith(".css;charset=UTF-8"))
			mimeType = "text/css";
		else if (name.endsWith(".swf"))
			mimeType = "application/x-Shockwave-Flash";
		else if (name.endsWith(".js"))
			mimeType = "text/javascript;charset=UTF-8";
		
		return mimeType;
	}
	
	protected boolean isValidArchive(String mimeType) {
		
		return (mimeType != null && (mimeType.equals("application/zip")
				|| mimeType.equals("application/x-download")));

	}
	
	protected abstract String newFolder(String uuid, ZipEntry entry);
	
	protected abstract String newItem(String uuid, ZipInputStream zipStream, ZipEntry entry);
	
	protected abstract String getRootDirectoryPath();
	
	protected abstract String getContentPackageDirectoryPath(String uuid);
	
	protected String stripSuffix(String name) {
		int indexOf = name.lastIndexOf('.');
		
		if (indexOf == -1)
			return name;
		
		return name.substring(0, indexOf);
	}
	
	protected String unpack(String resourceId) throws InvalidArchiveException {
		Archive archive = getArchive(resourceId);

		String uuid = UUID.randomUUID().toString();
		
		if (!isValidArchive(archive.getMimeType()))
			log.warn("Invalid mime type = " + archive.getMimeType());
		
		if (true)		
			unzip(uuid, new ZipInputStream(getArchiveStream(resourceId)));	 
		else 
			throw new InvalidArchiveException(archive.getMimeType());
		
		return uuid;
	}

	
	protected void unpackEntry(String parentPath, ZipInputStream stream, ZipEntry entry) {	
		if (entry.isDirectory())
			newFolder(parentPath, entry);
		else {
			newItem(parentPath, stream, entry);
		}
	}
				
	protected void unzip(String parentPath, ZipInputStream zipStream) {
		ZipEntry entry;
		try {
			entry = (ZipEntry) zipStream.getNextEntry();
			while (entry != null) {
				unpackEntry(parentPath, zipStream, entry);
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
