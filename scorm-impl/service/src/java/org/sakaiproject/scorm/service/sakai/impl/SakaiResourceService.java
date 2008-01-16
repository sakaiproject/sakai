package org.sakaiproject.scorm.service.sakai.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.impl.AbstractResourceService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Validator;

public abstract class SakaiResourceService extends AbstractResourceService {

	private static Log log = LogFactory.getLog(SakaiResourceService.class);

	private static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;
	private static final String MANIFEST_RESOURCE_ID_PROPERTY = "manifest_resource_id";
	private static final String FILE_UPLOAD_MAX_SIZE_CONFIG_KEY = "content.upload.max";
	
	protected abstract ContentHostingService contentService();
	protected abstract ServerConfigurationService configurationService();
	protected abstract ToolManager toolManager();
	
	public String convertArchive(String resourceId) {
		String archiveId = stripSuffix(super.convertArchive(resourceId)) + "/";
		return archiveId;
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
	
	public ContentPackageManifest getManifest(String manifestResourceId) {
		ContentPackageManifest manifest = null;
		
		try {

			manifest = (ContentPackageManifest)getObject(manifestResourceId);
				        
		} catch (Exception ioe) {
			log.error("Caught io exception reading manifest from file!", ioe);
		}
		
		return manifest;
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
	
	public Object getObject(String key) {
		Object object = null;
		
		try {
			ContentResource objectResource = contentService().getResource(key);
			
			byte[] bytes = objectResource.getContent();
		
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			
	        ObjectInputStream ie = new ObjectInputStream(in);
	        object = ie.readObject();
	        ie.close();
	        in.close();

		} catch (Exception ioe) {
			log.error("Caught io exception reading manifest from file!", ioe);
		}
		
		return object;
	}
	
	public ContentPackageResource getResource(String resourceId, String path) {
	
		log.warn("NOT IMPLEMENTED -- SakaiResourceService.getResource!!!");
		
		//FIXME: Need to grab the resource -- either by unpacking the archive on the fly, or by referring to it as a shared resource
		// Or by grabbing it out of the content repository on its own.
		
		/*String fullResourceId = new StringBuilder(resourceId).append("/").append(path).toString();
		
		try {
			ContentResource resource = contentService().getResource(fullResourceId);
		
			InputStream stream = resource.streamContent();
			
			return new ContentPackageResource(stream, resource.getContentType());

		} catch (Exception e) {
			log.error("Failed to retrieve resource from content hosting ", e);
		}*/
	
		return null;
	}

	public ContentPackageResource getResource(SessionBean sessionBean) {
		return getResource(sessionBean.getContentPackage().getResourceId(), sessionBean.getLaunchData().getLaunchLine());
	}
	
	public List<ContentPackageResource> getResources(String archiveResourceId) {
		return getContentResourcesRecursive(archiveResourceId, "");
	}
		
	public String putArchive(InputStream stream, String name, String mimeType, boolean isHidden) {
		String siteId = toolManager().getCurrentPlacement().getContext();
		String collectionId = contentService().getSiteCollection(siteId);
		
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
		String siteId = toolManager().getCurrentPlacement().getContext();
		String siteCollectionId = contentService().getSiteCollection(siteId);
		
		return findUnvalidatedArchives(siteCollectionId);
	}
	
	public String putManifest(String resourceId, ContentPackageManifest manifest) {
		String key = new StringBuilder(resourceId).append(":manifest.obj").toString();
		
		return putObject(key, manifest);
	}

	public String putObject(String key, Object object) {
		ContentResource resource = null;
		
		String site = toolManager().getCurrentPlacement().getContext();
		String tool = "scorm";
		String type = "application/octet-stream";
		
		try {	
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(object);
			out.close();
			
			resource = contentService().addAttachmentResource(key, site, tool, type, byteOut.toByteArray(), null);
			
			return resource.getId();
		} catch (Exception soe) {
			log.error("Caught an exception adding an attachment resource!", soe);
		} 
		
		return null;
	}
	
	
	public String removeArchive(String resourceId) {		
		String manifestResourceId = null;
		
		try {
			ContentResourceEdit edit = contentService().editResource(resourceId);
			
			if (edit != null) {
				ResourceProperties props = edit.getProperties();
				manifestResourceId = props.getProperty(MANIFEST_RESOURCE_ID_PROPERTY);
			
				contentService().removeResource(edit);
			}
		} catch (Exception e) {
			log.error("Unable to remove archive: " + resourceId, e);
		}
		
		return manifestResourceId;
	}
	
	
	public void removeManifest(String resourceId, String manifestResourceId) {
		try {
			if (manifestResourceId != null) {
				ContentResourceEdit manifestEdit = contentService().editResource(manifestResourceId);
				
				if (manifestEdit != null) 
					contentService().removeResource(manifestEdit);
			}
		} catch (Exception e) {
			log.error("Unable to remove manifest: " + manifestResourceId, e);
		}
	}
	
	protected List<ContentPackageResource> getContentResourcesRecursive(String collectionId, String path) {
		List<ContentPackageResource> resources = new LinkedList<ContentPackageResource>();
		try {
			ContentCollection collection = contentService().getCollection(collectionId);
			List<ContentEntity> members = collection.getMemberResources();
						
			for (ContentEntity member : members) {
				String resourcePath = member.getId();
				resourcePath = resourcePath.replace(" ", "%20");
				
				if (member.isResource() && member instanceof ContentResource) 
					resources.add(new ContentPackageSakaiResource(resourcePath, (ContentResource)member));
				else if (member.isCollection() && member instanceof ContentCollection)
					resources.addAll(getContentResourcesRecursive(member.getId(), ""));
			}
		
		} catch (Exception e) {
			log.error("Caught an exception looking for content packages", e);
		}
		
		return resources;
	}
	
	private String composeResourcePath(String parentPath, String resourcePath) {
		if (parentPath.endsWith("/"))
			parentPath = parentPath.substring(0, parentPath.length() - 1);
		if (resourcePath.startsWith("/"))
			resourcePath = resourcePath.substring(1);
		
		String path = new StringBuilder(parentPath).append("/").append(resourcePath).toString();
		path = path.replace(" ", "%20");
		
		return path;
	}
	
		
	protected String newFolder(String parentPath, ZipEntry entry) {
		String collectionId = new StringBuilder(parentPath).append("/").append(entry.getName()).toString();
		
		ContentCollectionEdit collection = null;
		
		try {
			collection = contentService().addCollection(collectionId);
			
			ResourcePropertiesEdit props = collection.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, getDisplayName(entry.getName()));
			
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
	
	protected String newItem(String parentPath, ZipInputStream zipStream, ZipEntry entry) {
		String resourceId = new StringBuilder(parentPath).append("/").append(entry.getName()).toString();

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
			
			resource.setContent(outStream.toByteArray());
			resource.setContentType(getMimeType(entry.getName()));
						
			ResourcePropertiesEdit props = resource.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, getDisplayName(entry.getName()));
			
			contentService().commitResource(resource);
			
		} catch (Exception e) {
			log.error("Failed to create new resource with id " + resourceId, e);
			
			if (resource != null)
				contentService().cancelResource(resource);
			
			return null;
		}
		
		return resourceId;
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
					if (mimeType != null && mimeType.equals("application/zip")) {
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
