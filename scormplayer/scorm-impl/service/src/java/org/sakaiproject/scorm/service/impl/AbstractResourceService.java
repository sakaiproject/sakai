package org.sakaiproject.scorm.service.impl;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.service.api.ScormResourceService;

public abstract class AbstractResourceService implements ScormResourceService {

	private static Log log = LogFactory.getLog(AbstractResourceService.class);
	
	
	public String convertArchive(String resourceId) {
		unpack(resourceId);
		return resourceId;
	}
	
	protected String getMimeType(String name) {
		String mimeType = new MimetypesFileTypeMap().getContentType(name);
		
		if (name.endsWith(".css"))
			mimeType = "text/css";
		else if (name.endsWith(".swf"))
			mimeType = "application/x-Shockwave-Flash";
		
		return mimeType;
	}
	
	protected abstract String newFolder(String parentPath, ZipEntry entry);
	
	protected abstract String newItem(String parentPath, ZipInputStream zipStream, ZipEntry entry);
	
	protected String stripSuffix(String name) {
		int indexOf = name.lastIndexOf('.');
		
		if (indexOf == -1)
			return name;
		
		return name.substring(0, indexOf);
	}
	
	protected void unpack(String resourceId) {
		Archive archive = getArchive(resourceId);
		String archivePath = stripSuffix(archive.getPath());
		
		if (archive.getMimeType().equals("application/zip")) {
			unzip(archivePath, new ZipInputStream(getArchiveStream(resourceId)));	
		}
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
