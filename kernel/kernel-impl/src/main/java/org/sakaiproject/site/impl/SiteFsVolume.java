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
package org.sakaiproject.site.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sakaiproject.elfinder.FsType;
import org.sakaiproject.elfinder.SakaiFsItem;
import org.sakaiproject.elfinder.SakaiFsService;
import org.sakaiproject.elfinder.ToolFsVolumeFactory;
import org.sakaiproject.elfinder.ReadOnlyFsVolume;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import lombok.extern.slf4j.Slf4j;

/**
 * This is the top-level volume. It contains the volumes for the site.
 * At the moment it will just contain the Resources Volume.
 * We'll have pre-hashed paths of /siteId/content/ /siteId/forums/ /siteId/
 *
 */
@Slf4j
public class SiteFsVolume extends ReadOnlyFsVolume {

    private SakaiFsService sakaiFsService;
    private String siteId;
    private SiteService siteService;

    public String getSiteId() {
        return siteId;
    }

    public SiteFsVolume(String siteId, SakaiFsService sakaiFsService, SiteService siteService) {

        try {
            this.siteId = URLDecoder.decode(siteId, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            log.error("Failed to decode siteId as UTF-8. Original siteId will be used.");
            this.siteId = siteId;
        }
        this.sakaiFsService = sakaiFsService;
        this.siteService = siteService;
    }

    @Override
    public boolean exists(SakaiFsItem newFile) {
        return false;
    }

    @Override
    public SakaiFsItem fromPath(String relativePath) {
        return null;
    }

    @Override
    public String getDimensions(SakaiFsItem fsi) {
        return null;
    }

    @Override
    public long getLastModified(SakaiFsItem fsi) {
        return 0;
    }

    @Override
    public String getMimeType(SakaiFsItem fsi) {
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
    public String getName(SakaiFsItem fsi) {
        return null;
    }

    @Override
    public SakaiFsItem getParent(SakaiFsItem fsi) {
        return null;
    }

    @Override
    public String getPath(SakaiFsItem fsi) throws IOException {
        return null;
    }

    @Override
    public SakaiFsItem getRoot() {
        return new SakaiFsItem(siteId, this, FsType.SITE);
    }

    @Override
    public long getSize(SakaiFsItem fsi) {
        return 0;
    }

    @Override
    public String getThumbnailFileName(SakaiFsItem fsi) {
        return null;
    }

    @Override
    public boolean hasChildFolder(SakaiFsItem fsi) {
        return true;
    }

    @Override
    public boolean isFolder(SakaiFsItem fsi) {
        // All items here are directories.
        return true;
    }

    @Override
    public boolean isRoot(SakaiFsItem fsi) {
        return true;
    }

    @Override
    public SakaiFsItem[] listChildren(SakaiFsItem fsi) {
        List<SakaiFsItem> children = new ArrayList<>();
        String siteId = fsi.getId();
        try {
            Site site = siteService.getSiteVisit(siteId);
            for (Map.Entry<String, ToolFsVolumeFactory> factory : sakaiFsService.getToolVolumes().entrySet()) {
                // Check that the tool is present in the site.
                if (site.getToolForCommonId(factory.getValue().getToolId()) != null) {
                    SakaiFsItem root = factory.getValue().getVolume(siteId).getRoot();
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
        return children.toArray(new SakaiFsItem[0]);
    }

    @Override
    public InputStream openInputStream(SakaiFsItem fsi) throws IOException {
        return null;
    }

    @Override
    public String getURL(SakaiFsItem f) {
        return null;
    }

}
