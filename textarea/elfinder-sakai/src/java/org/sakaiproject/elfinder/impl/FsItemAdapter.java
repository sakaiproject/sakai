package org.sakaiproject.elfinder.impl;

import org.sakaiproject.elfinder.SakaiFsItem;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;
import lombok.Getter;

public class FsItemAdapter implements FsItem {

    @Getter private final SakaiFsItem sakaiFsItem;

    public FsItemAdapter(SakaiFsItem sakaiFsItem) {
        this.sakaiFsItem = sakaiFsItem;
    }

    @Override
    public FsVolume getVolume() {
        return (sakaiFsItem == null) ? null : new FsVolumeAdapter(sakaiFsItem.getVolume());
    }

    public String getId() {
        return sakaiFsItem.getId();
    }

    public void setId(String id) {
        sakaiFsItem.setId(id);
    }

    public String getTitle() {
        return sakaiFsItem.getTitle();
    }
}
