/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.elfinder.sakai.site;

import cn.bluejoe.elfinder.service.FsItem;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.elfinder.sakai.SiteVolumeFactory;
import org.sakaiproject.elfinder.sakai.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the top-level volume. It contains the volumes for the site.
 * At the moment it will just contain the Resources Volume.
 * We'll have pre-hashed paths of /siteId/content/ /siteId/forums/ /siteId/
 *
 */
public class SiteFsVolume extends ReadOnlyFsVolume {

    public String getSiteId() {
        return siteId;
    }

    private String siteId;
    private SiteService siteService;
    private SakaiFsService service;

    public SiteFsVolume(String siteId, SakaiFsService service) {
        this.siteId = siteId;
        this.service = service;
        this.siteService = service.getSiteService();
    }

    @Override
    public boolean exists(FsItem newFile) {
        return false;
    }

    @Override
    public FsItem fromPath(String relativePath) {
        return null;
    }

    @Override
    public String getDimensions(FsItem fsi) {
        return null;
    }

    @Override
    public long getLastModified(FsItem fsi) {
        return 0;
    }

    @Override
    public String getMimeType(FsItem fsi) {
        return "directory";
    }

    @Override
    public String getName() {
        // We may want to cache this in the volumne
        String title = "";
        try {
            title = siteService.getSite(siteId).getTitle();
        } catch (IdUnusedException e) {
            // Ignore
        }
        return title;
    }

    @Override
    public String getName(FsItem fsi) {
        return null;
    }

    @Override
    public FsItem getParent(FsItem fsi) {
        return null;
    }

    @Override
    public String getPath(FsItem fsi) throws IOException {
        return null;
    }

    @Override
    public FsItem getRoot() {
        return new SiteFsItem(this, siteId);
    }

    @Override
    public long getSize(FsItem fsi) {
        return 0;
    }

    @Override
    public String getThumbnailFileName(FsItem fsi) {
        return null;
    }

    @Override
    public boolean hasChildFolder(FsItem fsi) {
        return true;
    }

    @Override
    public boolean isFolder(FsItem fsi) {
        // All items here are directories.
        return true;
    }

    @Override
    public boolean isRoot(FsItem fsi) {
        return true;
    }

    @Override
    public FsItem[] listChildren(FsItem fsi) {
        List<FsItem> children = new ArrayList<>();
        String siteId = ((SiteFsItem)fsi).getId();
        try {
            Site site = siteService.getSiteVisit(siteId);
            for (Map.Entry<String, SiteVolumeFactory> factory : service.getToolVolume().entrySet()) {
                // Check that the tool is present in the site.
                if (site.getToolForCommonId(factory.getValue().getToolId()) != null) {
                    FsItem root = factory.getValue().getVolume(service, siteId).getRoot();
                    if (root != null) {
                        children.add(root);
                    }
                }
            }
        } catch (PermissionException pe) {
            throw new IllegalArgumentException("The current user doesn't have access to: "+ siteId);
        } catch (IdUnusedException iue) {
            throw new IllegalArgumentException("There is no site with ID: "+ siteId);
        }
        return children.toArray(new FsItem[0]);
    }

    @Override
    public InputStream openInputStream(FsItem fsi) throws IOException {
        return null;
    }

    @Override
    public String getURL(FsItem f) {
        return null;
    }

}
