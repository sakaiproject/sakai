/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.service.sakai.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.scorm.exceptions.InvalidArchiveException;
import org.sakaiproject.scorm.exceptions.ResourceNotDeletedException;
import org.sakaiproject.scorm.exceptions.ResourceStorageException;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.service.impl.AbstractResourceService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Validator;

import static org.sakaiproject.scorm.api.ScormConstants.ROOT_DIRECTORY;

@Slf4j
public abstract class SakaiResourceService extends AbstractResourceService
{
	private static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;
	private static final String FILE_UPLOAD_MAX_SIZE_CONFIG_KEY = "content.upload.max";
	private static final String ROOT_DIRECTORY_RESOURCE = "/content" + ROOT_DIRECTORY;

	protected abstract ServerConfigurationService configurationService();
	protected abstract ContentHostingService contentService();
	protected abstract SecurityService securityService();
	protected abstract SiteService siteService();
	protected abstract ToolManager toolManager();

	@Override
	public String convertArchive(String resourceId, String title) throws InvalidArchiveException, ResourceStorageException
	{
		String uuid = null;
		uuid = super.convertArchive(resourceId, title);
		int packageCount = countExistingContentPackages(title);

		if (packageCount > 1)
		{
			title = new StringBuilder(title).append(" (").append(packageCount).append(")").toString();
		}

		ContentCollectionEdit collection = null;
		try
		{
			collection = this.contentService().editCollection(getContentPackageDirectoryPath(uuid));
			collection.setHidden();

			ResourcePropertiesEdit props = collection.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, title);
			this.contentService().commitCollection(collection);
		} 
		catch (Exception e)
		{
			log.warn("Unable to rename the root collection for {} to {}", uuid, title);

			try
			{
				if (collection != null)
				{
					this.contentService().cancelCollection(collection);
				}
			} 
			catch (Exception ex)
			{
				log.warn("Failed to cancel collection edit for {}", uuid);
			}

			throw new ResourceStorageException("Unable to rename the root collection for " + uuid + " to " + title + ", reason: " + e.getMessage(), e);
		}

		return uuid;
	}

	private int countExistingContentPackages(String title) throws ResourceStorageException
	{
		int count = 1;
		String rootCollectionId = getRootDirectoryPath();

		try
		{
			ContentCollection collection = this.contentService().getCollection(rootCollectionId);
			List<ContentEntity> members = collection.getMemberResources();
			Pattern p = Pattern.compile(title + "\\s*\\(?\\d*\\)?");

			for (ContentEntity entity : members)
			{
				if (entity.isCollection())
				{
					ResourceProperties props = entity.getProperties();
					String name = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);

					if (name != null)
					{
						Matcher m = p.matcher(name);
						if (m.matches())
						{
							count++;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			log.warn("Unable to find existing content packages with title {}", title);
			throw new ResourceStorageException("Unable to find existing content packages with title " + title + ", reason: " + e.getMessage(), e);
		}

		return count;
	}

	private boolean ensureCollection(String collectionId, String name) throws ResourceStorageException
	{
		boolean created = false;
		SecurityAdvisor advisor = new SecurityAdvisor()
		{
			@Override
			public SecurityAdvisor.SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				return ContentHostingService.AUTH_RESOURCE_READ.equals(function) ? SecurityAdvisor.SecurityAdvice.ALLOWED : SecurityAdvisor.SecurityAdvice.PASS;
			}
		};

		try
		{
			securityService().pushAdvisor(advisor);
			contentService().checkCollection(collectionId);
		}
		catch (IdUnusedException e)
		{
			forceCreateCollection(collectionId, name);
			created = true;
		}
		catch (Exception e)
		{
			log.error("Unexpected exception while checking for collection", e);
			throw new ResourceStorageException("Unexpected exception while checking for collection, reason: " + e.getMessage(), e);
		}
		finally
		{
			securityService().popAdvisor(advisor);
		}

		return created;
	}

	private List<Archive> findUnvalidatedArchives(String collectionId) throws ResourceStorageException
	{
		List<Archive> archives = new LinkedList<>();
		try
		{
			ContentCollection collection = this.contentService().getCollection(collectionId);
			List<ContentEntity> members = collection.getMemberResources();

			for (ContentEntity member : members)
			{
				if (member.isResource())
				{
					String mimeType = ((ContentResource) member).getContentType();
					if (isValidArchive(mimeType))
					{
						ResourceProperties props = member.getProperties();
						String title = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
						archives.add(new Archive(member.getId(), title));
					}
				}
				else if (member.isCollection() && member.getVirtualContentEntity() == null && member.getContentHandler() == null)
				{
					archives.addAll(findUnvalidatedArchives(member.getId()));
				}
			}
		} 
		catch (Exception e)
		{
			log.error("Caught an exception looking for content packages", e);
			throw new ResourceStorageException("Caught an exception looking for content packages, reason: " + e.getMessage(), e);
		}

		return archives;
	}

	private void forceCreateCollection(String collectionId, String name) throws ResourceStorageException 
	{
		SecurityAdvisor advisor = new SecurityAdvisor()
		{
			@Override
			public SecurityAdvisor.SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				return ContentHostingService.AUTH_RESOURCE_ADD.equals(function) ? SecurityAdvisor.SecurityAdvice.ALLOWED : SecurityAdvisor.SecurityAdvice.PASS;
			}
		};

		try
		{
			securityService().pushAdvisor(advisor);
			ContentCollectionEdit collection = contentService().addCollection(collectionId);
			ResourcePropertiesEdit properties = collection.getPropertiesEdit();
			properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
			contentService().commitCollection(collection);
		}
		catch (Exception e)
		{
			log.error("Unexpected exception while creating collection", e);
			throw new ResourceStorageException("Unexpected exception while creating collection, reason: " + e.getMessage(), e);
		}
		finally
		{
			securityService().popAdvisor(advisor);
		}
	}

	@Override
	public Archive getArchive(String resourceId) throws ResourceStorageException
	{
		Archive archive = null;
		try
		{
			ContentResource resource = this.contentService().getResource(resourceId);
			ResourceProperties props = resource.getProperties();
			String title = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);

			archive = new Archive(resourceId, title);
			archive.setHidden(resource.isHidden());
			archive.setMimeType(resource.getContentType());
			archive.setPath(resourceId);
		}
		catch (Exception e)
		{
			log.error("Failed to retrieve resource from content hosting ", e);
			throw new ResourceStorageException("Failed to retrieve resource from content hosting, reason: " + e.getMessage(), e);
		}

		return archive;
	}

	@Override
	public InputStream getArchiveStream(String resourceId) throws ResourceStorageException
	{
		try
		{
			ContentResource resource = this.contentService().getResource(resourceId);
			return resource.streamContent();
		}
		catch (Exception e) {
			log.error("Failed to retrieve resource from content hosting ", e);
			throw new ResourceStorageException("Failed to retrieve resource from content hosting, reason: " + e.getMessage(), e);
		}
	}

	@Override
	protected String getContentPackageDirectoryPath(String uuid)
	{
		return new StringBuilder(getRootDirectoryPath()).append(uuid).append("/").toString();
	}

	protected List<ContentPackageResource> getContentResourcesRecursive(String collectionId, String uuid, String path) throws ResourceStorageException
	{
		List<ContentPackageResource> resources = new LinkedList<>();
		try
		{
			ContentCollection collection = this.contentService().getCollection(collectionId);
			List<ContentEntity> members = collection.getMemberResources();

			for (ContentEntity member : members)
			{
				String id = member.getId();
				String[] tokens = id.split("/");
				String filename = tokens[tokens.length - 1];
				StringBuilder launchLineBuilder = new StringBuilder(path);

				if (path.isEmpty())
				{
					launchLineBuilder.append(filename);
				}
				else if (!path.endsWith("/"))
				{
					launchLineBuilder.append("/").append(filename);
				}
				else
				{
					launchLineBuilder.append(filename);
				}

				if (member.isCollection())
				{
					launchLineBuilder.append("/");
				}

				String launchline = launchLineBuilder.toString();
				String resourcePath = getResourcePath(uuid, launchline);

				if (member.isResource() && member instanceof ContentResource)
				{
					resources.add(new ContentPackageSakaiResource(resourcePath, ((ContentResource) member)));
				}
				else if (member.isCollection() && member instanceof ContentCollection)
				{
					resources.addAll(getContentResourcesRecursive(member.getId(), uuid, launchline));
				}
			}
		}
		catch (Exception e)
		{
			log.error("Caught an exception looking for content packages", e);
			throw new ResourceStorageException("Caught an exception looking for content packages, reason: " + e.getMessage(), e);
		}

		return resources;
	}

	private String getDisplayName(String name)
	{
		String[] parts = name.split("/");
		return parts.length == 0 ? "Unknown" : parts[parts.length - 1];
	}

	@Override
	public int getMaximumUploadFileSize()
	{
		String maxSize = null;
		int megaBytes = 1;
		try
		{
			maxSize = configurationService().getString(FILE_UPLOAD_MAX_SIZE_CONFIG_KEY, "1");
			if (null == maxSize)
			{
				log.warn("The sakai property '{}' is not set!", FILE_UPLOAD_MAX_SIZE_CONFIG_KEY);
			}
			else
			{
				megaBytes = Integer.parseInt(maxSize);
			}
		}
		catch (NumberFormatException nfe)
		{
			log.error("Failed to parse {} as an integer!", maxSize, nfe);
		}

		return megaBytes;
	}

	@Override
	public String getResourcePath(String resourceId, String launchLine)
	{
		StringBuilder pathBuilder = new StringBuilder();
		if (launchLine.startsWith("/"))
		{
			launchLine = launchLine.substring(1);
		}

		pathBuilder.append(getContentPackageDirectoryPath(resourceId));
		pathBuilder.append(launchLine);
		return pathBuilder.toString(); // .replace(" ", "%20");
	}

	@Override
	public List<ContentPackageResource> getResources(String uuid) throws ResourceStorageException
	{
		String contentPackageDirectoryId = getContentPackageDirectoryPath(uuid);
		return getContentResourcesRecursive(contentPackageDirectoryId, uuid, "");
	}

	@Override
	protected String getRootDirectoryPath()
	{
		return ROOT_DIRECTORY;
	}

	@Override
	public List<Archive> getUnvalidatedArchives() throws ResourceStorageException
	{
		String siteCollectionId = getRootDirectoryPath();
		return findUnvalidatedArchives(siteCollectionId);
	}

	public void init() throws ResourceStorageException
	{
		boolean created = ensureCollection(ROOT_DIRECTORY, "scorm");
		if (created)
		{
			SecurityAdvisor advisor = new SecurityAdvisor()
			{
				@Override
				public SecurityAdvice isAllowed(String userId, String function, String reference)
				{
					if (ROOT_DIRECTORY_RESOURCE.equals(reference) && (ContentHostingService.AUTH_RESOURCE_WRITE_ANY.equals(function) 
							|| ContentHostingService.AUTH_RESOURCE_READ.equals(function)
							|| ContentHostingService.AUTH_RESOURCE_HIDDEN.equals(function)))
					{
						return SecurityAdvice.ALLOWED;
					}
					else
					{
						return SecurityAdvice.PASS;
					}
				}
			};

			try
			{
				securityService().pushAdvisor(advisor);
				ContentCollectionEdit rootCollection = contentService().editCollection(ROOT_DIRECTORY);
				ResourcePropertiesEdit props = rootCollection.getPropertiesEdit();
				props.addProperty(ResourceProperties.PROP_CREATOR, "admin");
				props.addProperty(ResourceProperties.PROP_MODIFIED_BY, "admin");
				contentService().commitCollection(rootCollection);
			}
			catch (Exception e)
			{
				log.warn("Couldn't configure scorm root colection.", e);
			}
			finally
			{
				securityService().popAdvisor(advisor);
			}
		}
	}

	@Override
	protected String newFolder(String uuid, ZipEntry entry) throws ResourceStorageException
	{
		String entryName = entry.getName();
		if (entryName.indexOf('\\') != -1)
		{
			entryName = entryName.replace('\\', '/');
		}

		ContentCollectionEdit collection = null;
		String collectionId = getResourcePath(uuid, entryName);

		log.debug("Adding a folder with collection id: {}", collectionId);

		try
		{
			collection = this.contentService().addCollection(collectionId);
			String displayName = getDisplayName(entryName);

			ResourcePropertiesEdit props = collection.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);

			this.contentService().commitCollection(collection);
		}
		catch (IdUsedException e)
		{
			// Well, if it's used, then we'll go ahead and use it again, I guess
		} 
		catch (Exception e)
		{
			log.error("Failed to add a folder with id {}", collectionId);

			if (collection != null)
			{
				this.contentService().cancelCollection(collection);
			}

			throw new ResourceStorageException("Failed to add a folder with id " + collectionId + ", reason: " + e.getMessage(), e);
		}

		return collectionId;
	}

	@Override
	protected String newItem(String uuid, ZipInputStream zipStream, ZipEntry entry) throws ResourceStorageException
	{
		String entryName = entry.getName();
		if (entryName.indexOf('\\') != -1)
		{
			entryName = entryName.replace('\\', '/');
		}

		String resourceId = getResourcePath(uuid, entryName);
		ContentResourceEdit resource = null;

		try
		{
			resource = this.contentService().addResource(resourceId);
			byte[] buffer = new byte[1024];
			int length;
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();

			while ((length = zipStream.read(buffer)) > 0)
			{
				outStream.write(buffer, 0, length);
			}

			outStream.close();

			resource.setContent(outStream.toByteArray());
			resource.setContentType(getMimeType(entry.getName()));

			ResourcePropertiesEdit props = resource.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, getDisplayName(entry.getName()));
			props.addProperty(ResourceProperties.PROP_CONTENT_ENCODING, "UTF-8");

			this.contentService().commitResource(resource, NotificationService.NOTI_NONE);

		}
		catch (Exception e)
		{
			log.error("Failed to create new resource with id {}", resourceId, e);

			if (resource != null)
			{
				this.contentService().cancelResource(resource);
			}

			throw new ResourceStorageException(e);
		}

		return resourceId;
	}

	@Override
	public String putArchive(InputStream stream, String name, String mimeType, boolean isHidden, int priority) throws PermissionException, IdUniquenessException, IdLengthException,
                                                                                                                IdInvalidException, IdUnusedException, OverQuotaException, ServerOverloadException
	{
		String collectionId = getRootDirectoryPath();
		String fileName = name;
		int extIndex = fileName.lastIndexOf('.');
		String basename = fileName.substring(0, extIndex);
		String extension = fileName.substring(extIndex);

		ContentResourceEdit edit = null;
		try
		{
			edit = this.contentService().addResource(collectionId, Validator.escapeResourceName(basename), Validator.escapeResourceName(extension), MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
			edit.setContent(stream);
			edit.setContentType(mimeType);

			if (isHidden)
			{
				edit.setHidden();
			}

			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
			this.contentService().commitResource(edit, priority);

			return edit.getId();
		}
		catch (Exception e)
		{
			if (edit != null)
			{
				this.contentService().cancelResource(edit);
				log.error("Failed to place resources in Sakai content repository", e);
			}

			throw e;
		}
	}

	@Override
	public void removeResources(String uuid) throws ResourceNotDeletedException
	{
		String contentPackageDirectoryId = "";
		try
		{
			contentPackageDirectoryId = getContentPackageDirectoryPath(uuid);
			removeResourcesRecursive(contentPackageDirectoryId);

			log.debug("Removing collection {}", contentPackageDirectoryId);
		}
		catch (IdUnusedException iuue)
		{
			// I think this could be a bug with the BaseContentService, possibly related to caching...
			// Trap this. We can keep going; this isn't really a case that we want to give up on the whole operation
			log.warn("An underlying collection or resource was not properly removed, causing this collection remove to fail: " + contentPackageDirectoryId, iuue);
		}
		catch (Exception e)
		{
			log.error("Unable to remove archive: {}", contentPackageDirectoryId, e);
			throw new ResourceNotDeletedException(e.getMessage());
		}
	}

	private void removeResourcesRecursive(String collectionId) throws IdUnusedException, InUseException, PermissionException, ServerOverloadException, TypeException
	{
		ContentCollection collection = this.contentService().getCollection(collectionId);
		List<ContentEntity> members = collection.getMemberResources();

		for (ContentEntity member : members)
		{
			if (member.isResource() && member instanceof ContentResource)
			{
				log.debug("Removing resource {}", member.getId());

				try
				{
					this.contentService().removeResource(member.getId());
				}
				catch (IdUnusedException iuue)
				{
					// I think this could be a bug with the BaseContentService, possibly related to caching...
					// Trap this. We can keep going; this isn't really a case that we want to give up on the whole operation
					log.warn("Could not find this resource to remove: {}", member.getId());
				}
			}
			else if (member.isCollection() && member instanceof ContentCollection)
			{
				removeResourcesRecursive(member.getId());
			}
		}

		log.debug("Removing collection {}", collectionId);

		try
		{
			this.contentService().removeCollection(collectionId);
		}
		catch (IdUnusedException iuue)
		{
			// I think this could be a bug with the BaseContentService, possibly related to caching...
			// Trap this. We can keep going; this isn't really a case that we want to give up on the whole operation
			log.warn("Could not find this collection to remove: {}", collectionId);
		}
	}
}
