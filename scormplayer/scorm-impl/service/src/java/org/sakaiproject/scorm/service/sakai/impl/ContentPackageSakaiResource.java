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
public class ContentPackageSakaiResource extends ContentPackageResource {
	private static final long serialVersionUID = 1L;

	private static final ContentHostingService contentHostingService = ComponentManager.get(ContentHostingService.class);

	String contentResourceId;
	ContentResource contentResource;

    public ContentPackageSakaiResource(String path, ContentResource contentResource) {
        super(path);
        this.contentResource = contentResource;
        this.contentResourceId = contentResource.getId();
    }

    public ContentPackageSakaiResource(String path, String contentResourceId) {
        super(path);
        this.contentResourceId = contentResourceId;
        try {
            this.contentResource = contentHostingService.getResource(contentResourceId);
        } catch (PermissionException e) {
            log.warn("User lacks permission to access CHS resource [{}], {}", contentResourceId, e.toString());
        } catch (IdUnusedException e) {
            log.warn("CHS resource not found with id [{}], {}", contentResourceId, e.toString());
        } catch (TypeException e) {
            log.warn("CHS resource is a collection [{}], {}", contentResourceId, e.toString());
        }
    }

    @Override
    public InputStream getInputStream() throws ResourceNotFoundException {
        if (contentResource == null) throw new ResourceNotFoundException(contentResourceId);
        try {
            return contentResource.streamContent();
        } catch (Exception e) {
            throw new ResourceNotFoundException(contentResourceId);
        }
    }

    @Override
    public long getLastModified() {
        if (contentResource != null && contentResource.getProperties() != null) {
            try {
                Time modified = contentResource.getProperties().getTimeProperty(contentResource.getProperties().getNamePropModifiedDate());
                if (modified != null) {
                    return modified.getTime();
                }

                Time created = contentResource.getProperties().getTimeProperty(contentResource.getProperties().getNamePropCreationDate());
                if (created != null) {
                    return created.getTime();
                }
            } catch (EntityPropertyNotDefinedException | EntityPropertyTypeException e) {
                log.debug("Creation/Modified time property not found for resource [{}], {}", contentResourceId, e.toString());
            }
        }
        return System.currentTimeMillis();
    }

    @Override
    public long getLength() {
        return contentResource != null ? Math.max(0, contentResource.getContentLength()) : 0;
    }

    @Override
    public String getMimeType() {
        return contentResource != null ? contentResource.getContentType() : "application/octet-stream";
    }

    public ContentResource getContentResource() {
        return contentResource;
    }
}
