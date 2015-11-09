package org.sakaiproject.elfinder.sakai.content;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;

/**
 * Created by buckett on 08/07/15.
 */
public class ContentFsItem implements FsItem {

    private final FsVolume fsVolume;
    private String id;

    public ContentFsItem(FsVolume fsVolume, String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID can't be null");
        }
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
