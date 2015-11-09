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
