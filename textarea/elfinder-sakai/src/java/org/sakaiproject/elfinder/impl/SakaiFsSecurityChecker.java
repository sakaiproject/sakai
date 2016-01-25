package org.sakaiproject.elfinder.impl;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsSecurityChecker;
import cn.bluejoe.elfinder.service.FsService;
import org.sakaiproject.elfinder.sakai.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.elfinder.sakai.SiteVolume;
import org.sakaiproject.site.api.Site;

import java.io.IOException;
import java.util.Random;

/**
 * This just deals with if a item is writable by basically delegating through to the volume.
 * @see SiteVolume#isWriteable(FsItem)
 */
public class SakaiFsSecurityChecker implements FsSecurityChecker {

    @Override
    public boolean isLocked(FsService fsService, FsItem fsi) throws IOException {
        return false;
    }

    @Override
    public boolean isReadable(FsService fsService, FsItem fsi) throws IOException {
        // Filtering should be done by the volumes.
        return true;
    }

    @Override
    public boolean isWritable(FsService fsService, FsItem fsi) throws IOException {
        if (fsi.getVolume() instanceof ReadOnlyFsVolume) {
            return false;
        } else {
            if (fsi.getVolume() instanceof SiteVolume) {
                SiteVolume siteVolume = (SiteVolume) fsi.getVolume();
                return siteVolume.isWriteable(fsi);
            }
           return true;
        }
    }
}
