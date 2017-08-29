/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.elfinder.sakai.content;

import cn.bluejoe.elfinder.service.FsItem;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.elfinder.sakai.SiteVolumeFactory;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.elfinder.sakai.SiteVolume;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.site.api.SiteService;

/**
 * This is the creator of ContentHosting FsVolumes.
 */
public class DropboxSiteVolumeFactory extends ContentSiteVolumeFactory{

    @Override
    public String getPrefix() {
        return "dropbox";
    }

    @Override
    public SiteVolume getVolume(SakaiFsService sakaiFsService, String siteId) {
        return new DropboxSiteVolume(siteId, sakaiFsService);
    }

    @Override
    public String getToolId() {
        return "sakai.dropbox";
    }

    /**
     * Created by buckett on 05/08/15.
     */
    public class DropboxSiteVolume extends ContentSiteVolumeFactory.ContentSiteVolume {

        public DropboxSiteVolume(String siteId, SakaiFsService service ) {
            super(siteId, service);
        }

        public FsItem getRoot() {
            String id = contentHostingService.getDropboxCollection(siteId);
            return new ContentFsItem(this, id);
        }


        public String getName(FsItem fsi) {
            String rootId = asId(getRoot());
            String id = asId(fsi);
            if (rootId.equals(id)) {
                // Todo this needs i18n
                return "Dropbox";
            }
            try {
                //ask ContentHostingService for name
                ContentEntity contentEntity;
                if (contentHostingService.isCollection(id)) {
                    contentEntity = contentHostingService.getCollection(id);
                } else {
                    contentEntity = contentHostingService.getResource(id);
                }
                return contentEntity.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
            } catch (SakaiException se) {
                //LOG.warn("Failed to get name for: " + id, se);
            }
            return id;
        }
    }
}
