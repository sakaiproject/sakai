package org.sakaiproject.scorm.service.sakai.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.scorm.exceptions.InvalidArchiveException;
import org.sakaiproject.scorm.exceptions.ResourceNotDeletedException;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.service.impl.AbstractResourceService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Validator;

public abstract class SakaiResourceService extends AbstractResourceService {

	/**
	 * Security advisor to bypass the content read access.
	 * @author roland
	 *
	 */
	private final class ContentReadSecurityAdvisor implements SecurityAdvisor {
	    public SecurityAdvice isAllowed(String userId, String function, String reference) {
	    	if (ContentHostingService.AUTH_RESOURCE_READ.equals(function)) {
	    		return SecurityAdvice.ALLOWED;
	    	}
	    	if (ContentHostingService.AUTH_RESOURCE_HIDDEN.equals(function)) {
	    		return SecurityAdvice.ALLOWED;
	    	}
	        return SecurityAdvice.PASS;
	    }
    }


	private static Log log = LogFactory.getLog(SakaiResourceService.class);

	private static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;
	private static final String FILE_UPLOAD_MAX_SIZE_CONFIG_KEY = "content.upload.max";
	
	protected abstract ContentHostingService contentService();
	protected abstract ServerConfigurationService configurationService();
	protected abstract ToolManager toolManager();

	
	public String convertArchive(String resourceId, String title) throws InvalidArchiveException {
		String uuid = super.convertArchive(resourceId, title);
		
		int packageCount = countExistingContentPackages(title);
		
		if (packageCount > 1) 
			title = new StringBuilder(title).append(" (").append(packageCount).append(")").toString();
			
			
		ContentCollectionEdit collection = null;
		try {
			collection = contentService().editCollection(getContentPackageDirectoryPath(uuid));
		
			collection.setHidden();
			
			ResourcePropertiesEdit props = collection.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, title);
			
			contentService().commitCollection(collection);
		} catch (Exception e) {
			log.warn("Unable to rename the root collection for " + uuid + " to " + title);
		
			try {
				if (collection != null)
					contentService().cancelCollection(collection);
			} catch (Exception ex) {
				log.warn("Failed to cancel collection edit for " + uuid);
			}
		} 
		
		return uuid;
	}
	
	public Archive getArchive(String resourceId) {
		Archive archive = null;
		try {
			ContentResource resource = contentService().getResource(resourceId);

			ResourceProperties props = resource.getProperties();
		
			String title = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
					
			archive = new Archive(resourceId, title);

			archive.setHidden(resource.isHidden());
			archive.setMimeType(resource.getContentType());
			archive.setPath(resourceId);
			
		} catch (Exception e) {
			log.error("Failed to retrieve resource from content hosting ", e);
		}
	
		return archive;
	}
	
	public InputStream getArchiveStream(String resourceId) {
		try {
			ContentResource resource = contentService().getResource(resourceId);
		
			return resource.streamContent();
		
		} catch (Exception e) {
			log.error("Failed to retrieve resource from content hosting ", e);
		}
	
		return null;
	}

	
	public int getMaximumUploadFileSize() {
		String maxSize = null;
		int megaBytes = 1;
		try {
			maxSize = configurationService().getString(FILE_UPLOAD_MAX_SIZE_CONFIG_KEY, "1");
			if (null == maxSize)
				log.warn("The sakai property '" + FILE_UPLOAD_MAX_SIZE_CONFIG_KEY + "' is not set!");
			else
				megaBytes = Integer.parseInt(maxSize);
		} catch(NumberFormatException nfe) {
			log.error("Failed to parse " + maxSize + " as an integer ", nfe);
		}
		
		return megaBytes;
	}
		
	public String getResourcePath(String resourceId, String launchLine) {
		StringBuilder pathBuilder = new StringBuilder();
		
		if (launchLine.startsWith("/"))
			launchLine = launchLine.substring(1);
		
		pathBuilder.append(getContentPackageDirectoryPath(resourceId));
		pathBuilder.append(launchLine);
		
		return pathBuilder.toString(); //.replace(" ", "%20");
	}
	
	public List<ContentPackageResource> getResources(String uuid) {
		String contentPackageDirectoryId = getContentPackageDirectoryPath(uuid);
		return getContentResourcesRecursive(contentPackageDirectoryId, uuid, "");
	}
		
	public String putArchive(InputStream stream, String name, String mimeType, boolean isHidden) {
		String collectionId = getRootDirectoryPath();
		
		String fileName = new String(name);
		int extIndex = fileName.lastIndexOf('.');
		String basename = fileName.substring(0, extIndex);
		String extension = fileName.substring(extIndex);

		ContentResourceEdit edit = null;
		try {
			edit = contentService().addResource(collectionId,Validator.escapeResourceName(basename),Validator.escapeResourceName(extension), MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				
			edit.setContent(stream);
			edit.setContentType(mimeType);
			
			if (isHidden)
				edit.setHidden();
			
			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
	        		
	        contentService().commitResource(edit);			

	        return edit.getId();
		} catch (Exception e) {
			if (edit != null)
				contentService().cancelResource(edit);
			
			log.error("Failed to place resources in Sakai content repository", e);
		}
		
		return null;
	}
	
	public List<Archive> getUnvalidatedArchives() {
		String siteCollectionId = getRootDirectoryPath();
		
		return findUnvalidatedArchives(siteCollectionId);
	}
	
	
	
	public void removeResources(String uuid) throws ResourceNotDeletedException {		
		String contentPackageDirectoryId = "";
		try {
			contentPackageDirectoryId = getContentPackageDirectoryPath(uuid);
			removeResourcesRecursive(contentPackageDirectoryId);
			
			if (log.isDebugEnabled())
				log.debug("Removing collection " + contentPackageDirectoryId);
			
			contentService().removeCollection(contentPackageDirectoryId);
		} catch (IdUnusedException iuue) {
			// I think this could be a bug with the BaseContentService
			// possibly related to caching... 
			// Trap this. We can keep going; this isn't really a case that we want to give up on the whole operation
			log.warn("An underlying collection or resource was not properly removed, causing this collection remove to fail: " + contentPackageDirectoryId, iuue);
		} catch (Exception e) {
			log.error("Unable to remove archive: " + contentPackageDirectoryId, e);
			throw new ResourceNotDeletedException(e.getMessage());
		}
	}
	
	protected String getRootDirectoryPath() {
		String siteId = toolManager().getCurrentPlacement().getContext();
		String collectionId = contentService().getSiteCollection(siteId);
		
		return collectionId;
	}
	
	protected String getContentPackageDirectoryPath(String uuid) {
		return new StringBuilder(getRootDirectoryPath()).append(uuid).append("/").toString();
	}
	
	private void removeResourcesRecursive(String collectionId) 
		throws IdUnusedException, InUseException, PermissionException, 
			ServerOverloadException, TypeException {
		
		ContentCollection collection = contentService().getCollection(collectionId);
		
		List<ContentEntity> members = collection.getMemberResources();
		
		for (ContentEntity member : members) {
			if (member.isResource() && member instanceof ContentResource) {
				if (log.isDebugEnabled())
					log.debug("Removing resource " + member.getId());
				try {
					contentService().removeResource(member.getId());
				} catch (IdUnusedException iuue) {
					// I think this could be a bug with the BaseContentService
					// possibly related to caching... 
					// Trap this. We can keep going; this isn't really a case that we want to give up on the whole operation
					log.warn("Could not find this resource to remove: " + member.getId());
				}
			} else if (member.isCollection() && member instanceof ContentCollection)
				removeResourcesRecursive(member.getId());
		}
		
		if (log.isDebugEnabled())
			log.debug("Removing collection " + collectionId);
		
		try {
			contentService().removeCollection(collectionId);
		} catch (IdUnusedException iuue) {
			// I think this could be a bug with the BaseContentService
			// possibly related to caching... 
			// Trap this. We can keep going; this isn't really a case that we want to give up on the whole operation
			log.warn("Could not find this collection to remove: " + collectionId);
		}
	}
	
	
	protected List<ContentPackageResource> getContentResourcesRecursive(String collectionId, String uuid, String path) {
		
		SecurityService.pushAdvisor(new ContentReadSecurityAdvisor());
		
		List<ContentPackageResource> resources = new LinkedList<ContentPackageResource>();
		try {
			ContentCollection collection = contentService().getCollection(collectionId);
			List<ContentEntity> members = collection.getMemberResources();
						
			for (ContentEntity member : members) {
				//String resourcePath = member.getId();
				//resourcePath = resourcePath.replace(" ", "%20");
				
				String id = member.getId();
				
				String[] tokens = id.split("/");
				
				String filename = tokens[tokens.length - 1];
				
				StringBuilder launchLineBuilder = new StringBuilder(path);
				
				if (path.equals(""))
					launchLineBuilder.append(filename);
				else if (!path.endsWith("/"))
					launchLineBuilder.append("/").append(filename);
				else
					launchLineBuilder.append(filename);
				
				if (member.isCollection())
					launchLineBuilder.append("/");
				
				String launchline = launchLineBuilder.toString();
				String resourcePath = getResourcePath(uuid, launchline);
				
				if (member.isResource() && member instanceof ContentResource) {
	                resources.add(new ContentPackageSakaiResource(resourcePath, ((ContentResource)member)));
                } else if (member.isCollection() && member instanceof ContentCollection)
					resources.addAll(getContentResourcesRecursive(member.getId(), uuid, launchline));
			}
		
		} catch (Exception e) {
			log.error("Caught an exception looking for content packages", e);
		} finally {
			SecurityService.popAdvisor();
		}
		
		return resources;
	}

		
	protected String newFolder(String uuid, ZipEntry entry) {
		String entryName = entry.getName();
		
		if (entryName.indexOf('\\') != -1)
			entryName = entryName.replace('\\', '/');
		
		ContentCollectionEdit collection = null;
		
		String collectionId = getResourcePath(uuid, entryName);
		
		if (log.isDebugEnabled())
			log.debug("Adding a folder with collection id: " + collectionId);
		
		try {
			collection = contentService().addCollection(collectionId);
			
			String displayName = getDisplayName(entryName);

			collection.setHidden();
			
			ResourcePropertiesEdit props = collection.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
			
			contentService().commitCollection(collection);
		} catch (IdUsedException e) {
			// Well, if it's used, then we'll go ahead and use it again, I guess
		} catch (Exception e) {
			log.error("Failed to add a folder with id " + collectionId);
			
			if (collection != null)
				contentService().cancelCollection(collection);
			
			return null;
		} 
		
		return collectionId;
	}
	
	protected String newItem(String uuid, ZipInputStream zipStream, ZipEntry entry) {
		String entryName = entry.getName();
		
		if (entryName.indexOf('\\') != -1)
			entryName = entryName.replace('\\', '/');
		
		String resourceId = getResourcePath(uuid, entryName);

		ContentResourceEdit resource = null;
		
		try {
			resource = contentService().addResource(resourceId);
			
			byte[] buffer = new byte[1024];
			int length;
			
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			
			while ((length = zipStream.read(buffer)) > 0) {  
				outStream.write(buffer, 0, length);
            }
			
			outStream.close();
			
			//convert resource content to UTF-8 charset
			/*
			String resourceContent = new String(outStream.toByteArray());
			ByteArrayOutputStream encodedStream = new ByteArrayOutputStream();
			OutputStreamWriter ow = new OutputStreamWriter(encodedStream, "UTF-8");
			ow.write(resourceContent);
			ow.close();
			*/
			
			resource.setContent(outStream.toByteArray());
			resource.setContentType(getMimeType(entry.getName()));
			resource.setHidden();
						
			ResourcePropertiesEdit props = resource.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, getDisplayName(entry.getName()));
			props.addProperty(ResourceProperties.PROP_CONTENT_ENCODING, "UTF-8");
			
			contentService().commitResource(resource);
			
		} catch (Exception e) {
			log.error("Failed to create new resource with id " + resourceId, e);
			
			if (resource != null)
				contentService().cancelResource(resource);
			
			return null;
		}
		
		return resourceId;
	}
	
	private int countExistingContentPackages(String title) {
		int count = 1;
		
		String rootCollectionId = getRootDirectoryPath();
		
		try {
			ContentCollection collection = contentService().getCollection(rootCollectionId);
		
			List<ContentEntity> members = collection.getMemberResources();
			
			for (ContentEntity entity : members) {
				
				if (entity.isCollection()) {
					ResourceProperties props = entity.getProperties();
				
					String name = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				
					if (name != null) {
						Pattern p = Pattern.compile(title + "\\s*\\(?\\d*\\)?");
						Matcher m = p.matcher(name);
						if (m.matches())
							count++;
					}
				}
			}
			
		
		} catch (Exception e) {
			log.warn("Unable to find existing content packages with title " + title);
		}

		
		return count;
	}
	
	
	private String getDisplayName(String name) {
		String[] parts = name.split("/");
		
		if (parts.length == 0)
			return "Unknown";
		
		return parts[parts.length - 1];
	}
	
	
	private List<Archive> findUnvalidatedArchives(String collectionId) {
		List<Archive> archives = new LinkedList<Archive>();
		try {
			ContentCollection collection = contentService().getCollection(collectionId);
			List<ContentEntity> members = collection.getMemberResources();
			
			for (ContentEntity member : members) {
				if (member.isResource()) {
					String mimeType = ((ContentResource)member).getContentType();
					if (isValidArchive(mimeType)) {
						ResourceProperties props = member.getProperties();
						String title = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
						
						archives.add(new Archive(member.getId(), title));
					}
				} else if (member.isCollection() && member.getVirtualContentEntity() == null &&
						member.getContentHandler() == null)
					archives.addAll(findUnvalidatedArchives(member.getId()));
			}
		
		} catch (Exception e) {
			log.error("Caught an exception looking for content packages", e);
		}
		
		return archives;
	}
	
}
