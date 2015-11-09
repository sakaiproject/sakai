package org.sakaiproject.elfinder.sakai.site;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;

/**
 * A FsItem for a Site.
 */
public class SiteFsItem implements FsItem {

    private final FsVolume fsVolume;
    private String id;

    public SiteFsItem(FsVolume fsVolume, String id) {
        this.fsVolume = fsVolume;
        this.id = id;
    }

    @Override
    public FsVolume getVolume() {
        return fsVolume;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
