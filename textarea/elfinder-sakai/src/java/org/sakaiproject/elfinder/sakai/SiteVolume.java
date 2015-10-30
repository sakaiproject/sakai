package org.sakaiproject.elfinder.sakai;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;

/**
 * This is a FsVolume that's aware of the site it's in.
 */
public interface SiteVolume extends FsVolume {

    /**
     * The SiteId that this FsVolume is for.
     * @return A String site ID.
     */
    String getSiteId();

    /**
     * The SiteVolumeFactory that created this SiteVolume.
     * This is mainly used to discover the prefix.
     * @return The SiteVolumeFactory.
     */
    SiteVolumeFactory getSiteVolumeFactory();

    /**
     * Is the item writeable by the current user.
     * @param item The FsItem.
     * @return <code>true</code> if the passed item is writeable by the current user.
     */
    boolean isWriteable(FsItem item);

}
