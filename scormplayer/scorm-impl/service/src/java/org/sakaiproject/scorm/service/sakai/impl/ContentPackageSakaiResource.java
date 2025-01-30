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

import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.scorm.exceptions.ResourceNotFoundException;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.time.api.Time;

@Slf4j
public class ContentPackageSakaiResource extends ContentPackageResource
{
	private static final long serialVersionUID = 1L;

	private static final ContentHostingService contentHostingService = (ContentHostingService) ComponentManager.get(ContentHostingService.class);

	String contentResourceId;

	public ContentPackageSakaiResource(String path, ContentResource contentResource)
	{
		this(path, contentResource.getId(), contentResource.getContentLength(), contentResource.getContentType());
	}

	public ContentPackageSakaiResource(String path, String contentResourceId, long contentLength, String mimeType)
	{
		super(path);
		this.contentResourceId = contentResourceId;
	}

	@Override
	public InputStream getInputStream() throws ResourceNotFoundException
	{
		try
		{
			ContentResource resource = contentHostingService.getResource(contentResourceId);
			return resource.streamContent();
		}
		catch (IdUnusedException e)
		{
			log.error("Could not stream content from this path: {}", getPath(), e);
			throw new ResourceNotFoundException(getPath());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLastModified()
	{
		try
		{
			ContentResource resource = contentHostingService.getResource(contentResourceId);
			Time modiefied = resource.getProperties().getTimeProperty(resource.getProperties().getNamePropModifiedDate());
			if (modiefied != null)
			{
				return modiefied.getTime();
			}
			else
			{
				Time created = resource.getProperties().getTimeProperty(resource.getProperties().getNamePropCreationDate());
				if (created != null)
				{
					return created.getTime();
				}
			}
		}
		catch (EntityPropertyNotDefinedException | EntityPropertyTypeException | PermissionException | IdUnusedException | TypeException e)
		{
			// ignore
		}

		return System.currentTimeMillis();
	}

	@Override
	public long getLength()
	{
		try
		{
			ContentResource resource = contentHostingService.getResource(contentResourceId);
			return resource.getContentLength();
		}
		catch (PermissionException | IdUnusedException | TypeException e)
		{
			// ignore
		}
		return -1;
	}

	@Override
	public String getMimeType()
	{
		try
		{
			ContentResource resource = contentHostingService.getResource(contentResourceId);
			return resource.getContentType();
		}
		catch (PermissionException | IdUnusedException | TypeException e)
		{
			// ignore
		}

		return "";
	}
}
