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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.scorm.exceptions.ResourceNotFoundException;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.time.api.Time;

public class ContentPackageSakaiResource extends ContentPackageResource {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(ContentPackageSakaiResource.class);

	String contentResourceId;

	public ContentPackageSakaiResource(String path, ContentResource contentResource) {
		this(path, contentResource.getId(), contentResource.getContentLength(), contentResource.getContentType());
	}

	public ContentPackageSakaiResource(String path, String contentResourceId, long contentLength, String mimeType) {
		super(path);
		this.contentResourceId = contentResourceId;
	}

	@Override
	public InputStream getInputStream() throws ResourceNotFoundException {
		try {
			ContentResource resource = ContentHostingService.getResource(contentResourceId);
			return resource.streamContent();
		} catch (IdUnusedException e) {
			log.error("Could not stream content from this path: " + getPath(), e);
			throw new ResourceNotFoundException(getPath());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLastModified() {
		try {
			ContentResource resource = ContentHostingService.getResource(contentResourceId);
			Time modiefied = resource.getProperties().getTimeProperty(resource.getProperties().getNamePropModifiedDate());
			if (modiefied != null) {
				return modiefied.getTime();
			} else {
				Time created = resource.getProperties().getTimeProperty(resource.getProperties().getNamePropCreationDate());
				if (created != null) {
					return created.getTime();
				}
			}
		} catch (EntityPropertyNotDefinedException e) {
			//  ignore
		} catch (EntityPropertyTypeException e) {
			// ignore
		} catch (PermissionException e) {
			// ignore
		} catch (IdUnusedException e) {
			// ignore
		} catch (TypeException e) {
			// ignore
		}
		return System.currentTimeMillis();
	}

	@Override
	public long getLength() {
		try {
			ContentResource resource = ContentHostingService.getResource(contentResourceId);
			return resource.getContentLength();
		} catch (PermissionException e) {
			//  ignore
		} catch (IdUnusedException e) {
			//  ignore
		} catch (TypeException e) {
			//  ignore
		}
		return -1;
	}

	@Override
	public String getMimeType() {
		try {
			ContentResource resource = ContentHostingService.getResource(contentResourceId);
			return resource.getContentType();
		} catch (PermissionException e) {
			//  ignore
		} catch (IdUnusedException e) {
			//  ignore
		} catch (TypeException e) {
			//  ignore
		}
		return "";
	}

}
