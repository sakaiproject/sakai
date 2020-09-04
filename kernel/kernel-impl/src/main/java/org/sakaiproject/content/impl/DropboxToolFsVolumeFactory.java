/**
 * Copyright (c) 2003-2015 The Apereo Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/ecl2
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.content.impl;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.elfinder.FsType;
import org.sakaiproject.elfinder.SakaiFsItem;
import org.sakaiproject.elfinder.SakaiFsService;
import org.sakaiproject.elfinder.ToolFsVolume;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.SakaiException;

import lombok.extern.slf4j.Slf4j;

/**
 * This is the creator of ContentHosting FsVolumes.
 */
@Slf4j
public class DropboxToolFsVolumeFactory extends ContentToolFsVolumeFactory {

    @Override
    public String getPrefix() {
        return FsType.DROPBOX.toString();
    }

    @Override
    public ToolFsVolume getVolume(String siteId) {
        return new DropboxToolFsVolume(siteId, sakaiFsService);
    }

    @Override
    public String getToolId() {
        return "sakai.dropbox";
    }

    public class DropboxToolFsVolume extends ContentToolFsVolume {

        public DropboxToolFsVolume(String siteId, SakaiFsService service) {
            super(siteId, service);
        }

        public SakaiFsItem getRoot() {
            String id = contentHostingService.getDropboxCollection(siteId);
            return new SakaiFsItem(id, this, FsType.DROPBOX);
        }


        public String getName(SakaiFsItem fsi) {
            String rootId = getRoot().getId();
            String id = fsi.getId();
            if (rootId.equals(id)) {
                // Todo this needs i18n
                return "Dropbox";
            }
            try {
                // ask ContentHostingService for name
                ContentEntity contentEntity;
                if (contentHostingService.isCollection(id)) {
                    contentEntity = contentHostingService.getCollection(id);
                } else {
                    contentEntity = contentHostingService.getResource(id);
                }
                return contentEntity.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
            } catch (SakaiException se) {
                log.warn("Failed to get name for: {}", id, se);
            }
            return id;
        }
    }
}
