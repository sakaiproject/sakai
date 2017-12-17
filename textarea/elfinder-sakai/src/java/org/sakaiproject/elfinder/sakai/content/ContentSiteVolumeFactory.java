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
package org.sakaiproject.elfinder.sakai.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import cn.bluejoe.elfinder.controller.ErrorException;
import cn.bluejoe.elfinder.service.FsItem;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.*;
import org.sakaiproject.elfinder.sakai.SiteVolumeFactory;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.elfinder.sakai.SiteVolume;
import org.sakaiproject.elfinder.sakai.site.SiteFsItem;
import org.sakaiproject.elfinder.sakai.site.SiteFsVolume;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * This is the creator of ContentHosting FsVolumes.
 */
@Slf4j
public class ContentSiteVolumeFactory implements SiteVolumeFactory {

    protected ContentHostingService contentHostingService;
    protected SiteService siteService;
    protected SecurityService securityService;
    protected UserDirectoryService userDirectoryService;
    protected ThreadLocalManager threadLocalManager;

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
        this.threadLocalManager = threadLocalManager;
    }

    @Override
    public String getPrefix() {
        return "content";
    }

    @Override
    public SiteVolume getVolume(SakaiFsService sakaiFsService, String siteId) {
        return new ContentSiteVolume(siteId, sakaiFsService);
    }

    @Override
    public String getToolId() {
        return "sakai.resources";
    }

    /**
     * Volume is a container for a set of files and folder. In Sakai this will typically be the contents of a resources
     * tool or the resources for a tool in a site.
     */
    public class ContentSiteVolume implements SiteVolume {

        private SakaiFsService service;

        @Override
        public String getSiteId() {
            return siteId;
        }

        @Override
        public SiteVolumeFactory getSiteVolumeFactory() {
            return ContentSiteVolumeFactory.this;
        }


        @Override
        public boolean isWriteable(FsItem item) {
            String id = asId(item);
            if (contentHostingService.isCollection(id)) {
                // Sakai has more fine grain permissions that elfinder so we allow on either of these and then
                // if the end user can't perform one of the actions later on if will fail.
                return contentHostingService.allowAddResource(id + "dummy") ||
                    contentHostingService.allowUpdateCollection(id);
            } else {
                return contentHostingService.allowUpdateResource(id);
            }
        }

        // This is the ID of a site.
        // TODO What when we're not in a site?
        protected String siteId;

        public ContentSiteVolume(String siteId, SakaiFsService service) {
            this.siteId = siteId;
            this.service = service;
        }

        public void createFile(FsItem fsi) throws IOException {
            String id = asId(fsi);
            try {
                String filename = lastPathSegment(id);
                String name = filename, ext = "";
                int index = filename.lastIndexOf(".");
                if (index >= 0) {
                    name = filename.substring(0, index);
                    ext = filename.substring(index + 1);
                }
                ContentResourceEdit cre = contentHostingService.addResource(asId(getParent(fsi)), name, ext, 999);
                contentHostingService.commitResource(cre, org.sakaiproject.event.api.NotificationService.NOTI_NONE);
                //update saved ID incase it wasn't the same
                ((ContentFsItem) fsi).setId(cre.getId());
                // This is because the user might not have permission to update the file and elfinder does the upload
                // in 2 steps. This will get removed at the end of the request.
                SecurityAdvisor advisor = (userId, function, reference) -> {
                    // Check userId so event publication doesn't get confused
                    if (userDirectoryService.getCurrentUser().getId().equals(userId) &&
                            reference.equals(cre.getReference()) && function.startsWith("content.")) {
                        return SecurityAdvisor.SecurityAdvice.ALLOWED;
                    }
                    return SecurityAdvisor.SecurityAdvice.PASS;
                };
                securityService.pushAdvisor(advisor);
                // We put this on a thead local so we can correctly remove it in the write stream if we get called.
                // Otherwise it will get removed when the request ends.
                threadLocalManager.set(getClass().getName()+":advisor", advisor);
            } catch (SakaiException se) {
                throw new IOException("Failed to create new file: " + id, se);
            }

        }

        public void createFolder(FsItem fsi) throws IOException {
            String id = asId(fsi);
            try {
                if (fsi instanceof ContentFsItem) {
                    ContentFsItem cfsi = (ContentFsItem)fsi;
                    String collectionId = asId(getParent(cfsi));
                    String path = asId(cfsi);
                    String name = lastPathSegment(path);
                    ContentCollectionEdit edit = contentHostingService.addCollection(collectionId, name);
                    ResourcePropertiesEdit props = edit.getPropertiesEdit();
                    props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
                    contentHostingService.commitCollection(edit);
                    // Directories always end with a trailing slash
                    // The creation appends this if missing, so make sure the item is in sync
                    cfsi.setId(edit.getId());
                } else {
                    throw new IllegalArgumentException("Can only pass ContentFsItem");
                }
            } catch (SakaiException se) {
                throw new IOException("Failed to create new folder: " + id, se);
            }
        }

        /**
         * This extracts the last name from the path.
         *
         * @param path The path, can end with a '/' or the filename.
         * @return The name of the last name in the path with no slashes.
         */
        private String lastPathSegment(String path) {
            int start = path.lastIndexOf("/");
            int stop = path.length();
            if (start == path.length() - 1) {
                stop = start;
                start = path.lastIndexOf("/", start - 1);
            }
            return path.substring(start + 1, stop);
        }

        public void deleteFile(FsItem fsi) throws IOException {
            String id = asId(fsi);
            try {
                contentHostingService.removeResource(id);
            } catch (SakaiException se) {
                throw new IOException("Failed to remove file: " + id, se);
            }
        }

        public void deleteFolder(FsItem fsi) throws IOException {
            String id = asId(fsi);
            try {
                contentHostingService.removeCollection(id);
            } catch (SakaiException se) {
                throw new IOException("Failed to remove folder: " + id, se);
            }

        }

        public boolean exists(FsItem newFile) {
            try {
                String id = asId(newFile);
                if (contentHostingService.isCollection(id)) {
                    contentHostingService.getCollection(id);
                } else {
                    contentHostingService.getResource(id);
                }
                return true;
            } catch (IdUnusedException iue) {
                return false; // This one we expect.
            } catch (SakaiException se) {
                return false;
            }
        }

        public FsItem fromPath(String path) {
            // The path is relative to the site's top level folder.
            if (path == null) {
                return getRoot();
            } else {
                return new ContentFsItem(this, path);
            }
        }

        public String getDimensions(FsItem fsi) {
            return null;
        }

        public long getLastModified(FsItem fsi) {
            String id = asId(fsi);
            try {
                ContentEntity contentEntity;
                if (contentHostingService.isCollection(id)) {
                    contentEntity = contentHostingService.getCollection(id);
                } else {
                    contentEntity = contentHostingService.getResource(id);
                }
                Date date = contentEntity.getProperties().getDateProperty(ResourceProperties.PROP_MODIFIED_DATE);
                return date.getTime() / 1000;
            } catch (IdUnusedException iue) {
                log.debug("Failed to find item to get last modified date for: "+ id);
            } catch (SakaiException se) {
                log.warn("Failed to get last modified date for: " + id, se);
            } catch (EntityPropertyTypeException e) {
                log.warn("Property isn't date on :" + id, e);
            } catch (EntityPropertyNotDefinedException e) {
                // This isn't too much of a problem.
                log.debug("No modified date set on: " + id, e);
            }
            return 0;
        }

        public String getMimeType(FsItem fsi) {
            String id = asId(fsi);
            if (contentHostingService.isCollection(id)) {
                return "directory";
            } else {
                try {
                    ContentResource resource = contentHostingService.getResource(id);
                    return resource.getContentType();
                } catch (SakaiException se) {
                    return "";
                }
            }
        }

        public String getName() {
            try {
                return siteService.getSite(siteId).getTitle() + " Resources";
            } catch (SakaiException se) {
                return "unknown";
            }
        }

        public String getName(FsItem fsi) {
            String rootId = asId(getRoot());
            String id = asId(fsi);
            if (rootId.equals(id)) {
                // Todo this needs i18n
                return "Resources";
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
            } catch (IdUnusedException iue) {
                log.debug("Failed to find item to get name: "+ id);
            } catch (SakaiException se) {
                log.warn("Failed to get name for: " + id, se);
            }
            return lastPathSegment(id);
        }

        public FsItem getParent(FsItem fsi) {
            String rootId = asId(getRoot());
            String id = asId(fsi);
            if (id.startsWith(rootId) && !rootId.equals(id))  {
                String parentId = contentHostingService.getContainingCollectionId(id);
                return fromPath(parentId);
            } else {
                return service.getSiteVolume(siteId).getRoot();
            }
        }

        public String getPath(FsItem fsi) throws IOException {
            String id = asId(fsi);
            // This is need because FsItemEx enforces the slash between directory and file
            // and Sakai directories always have trailing /
            return id;
    //    	int lastSlash = id.lastIndexOf("/");
    //        if(lastSlash < 0) return id;
    //        return id.substring(0, lastSlash);
        }

        public FsItem getRoot() {
            String id = contentHostingService.getSiteCollection(siteId);
            try {
                contentHostingService.getCollection(id);
                return fromPath(id);
            } catch (IdUnusedException | PermissionException ignored) {
            } catch (TypeException e) {
                log.warn("Unexpected error getting root.", e);
            }
            return null;
        }

        public long getSize(FsItem fsi) {
            String id = asId(fsi);
            try {
                if (contentHostingService.isCollection(id)) {
                    return contentHostingService.getCollectionSize(id);
                } else {
                    return contentHostingService.getResource(id).getContentLength();
                }
            } catch (IdUnusedException uie) {
                log.debug("Failed to file size as item can't be found: "+ id);
            } catch (SakaiException se) {
                log.warn("Failed to get size for: " + id, se);
            }
            return 0;
        }

        public String getThumbnailFileName(FsItem fsi) {
            return null;
        }

        public boolean hasChildFolder(FsItem fsi) {
            String id = asId(fsi);
            try {
                // For sites that don't have a root folder yet this will fail.
                ContentCollection collection = contentHostingService.getCollection(id);
                // Just need to check if any of them are collections
                for (String member : collection.getMembers()) {
                    if (contentHostingService.isCollection(member)) {
                        // Shortcut out on the first one we find
                        return true;
                    }
                }
            } catch (IdUnusedException iue) {
                log.debug("Couldn't find resource to look for child folders: "+ id);
            } catch (SakaiException se) {
                log.warn("Couldn't see if there are child folders: " + id, se);
            }

            return false;
        }

        public boolean isFolder(FsItem fsi) {
            String id = asId(fsi);
            return contentHostingService.isCollection(id);
        }

        /**
         * For a SubVolume this must always be false so it walks back up the hierarchy.
         */
        public boolean isRoot(FsItem fsi) {
            return false;
        }

        public FsItem[] listChildren(FsItem fsi) {
            String id = asId(fsi);
            try {
                ContentCollection collection = contentHostingService.getCollection(id);
                List<FsItem> items = new ArrayList<>();
                for (String member : collection.getMembers()) {
                    items.add(fromPath(member));
                }
                return items.toArray(new FsItem[items.size()]);
            } catch (IdUnusedException iue) {
                log.debug("Failed to list children as item can't be found for: "+ id);
            } catch (PermissionException pe) {
                throw new ErrorException("errPerm");
            } catch (SakaiException se) {
                log.warn("Failed to find children of: " + id, se);
            }
            return new FsItem[0];
        }

        public InputStream openInputStream(FsItem fsi) throws IOException {
            String id = asId(fsi);
            try {
                ContentResource resource = contentHostingService.getResource(id);
                return resource.streamContent();
            } catch (SakaiException se) {
                throw new IOException("Failed to open input stream for: " + id, se);
            }
        }

        public void writeStream(final FsItem fsi, InputStream is) throws IOException {
            String id = asId(fsi);
            try {
                ContentResourceEdit resource = contentHostingService.editResource(id);
                resource.setContent(is);
                contentHostingService.commitResource(resource, org.sakaiproject.event.api.NotificationService.NOTI_NONE);
                Object advisor = threadLocalManager.get(getClass().getName()+":advisor");
                if (advisor instanceof SecurityAdvisor) {
                    securityService.popAdvisor((SecurityAdvisor) advisor);
                }
            } catch (SakaiException se) {
                throw new IOException("Failed to open input stream for: " + id, se);
            }
        }

        public void rename(FsItem src, FsItem dst) throws IOException {
            String srcId = asId(src);
            String dstName = getName(dst);

            try {
                if (contentHostingService.isCollection(srcId)) {
                    ContentCollectionEdit edit = contentHostingService.editCollection(srcId);
                    ResourcePropertiesEdit props = edit.getPropertiesEdit();
                    props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, dstName);

                    contentHostingService.commitCollection(edit);
                } else {
                    ContentResourceEdit edit = contentHostingService.editResource(srcId);
                    ResourcePropertiesEdit props = edit.getPropertiesEdit();
                    props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, dstName);

                    contentHostingService.commitResource(edit, org.sakaiproject.event.api.NotificationService.NOTI_NONE);
                }
            } catch (SakaiException se) {
                throw new IOException("Failed to rename file: " + srcId + " to " + dstName, se);
            }
        }

        public String asId(FsItem fsItem) {
            if (fsItem instanceof ContentFsItem) {
                return ((ContentFsItem) fsItem).getId();
            } else {
                throw new IllegalArgumentException("Passed FsItem must be a SakaiFsItem.");
            }
        }

        @Override
        public String getURL(FsItem fsItem) {
            String id = asId(fsItem);
            return contentHostingService.getUrl(id);
        }

        @Override
        public void filterOptions(FsItem fsItem, Map<String, Object> map) {
            // The preview isn't working properly
            map.put("disabled", Arrays.asList(new String[]{"search", "zipdl"}));
            // Disabled chunked uploads
            map.put("uploadMaxConn", "-1");
        }
    }
}
