package org.sakaiproject.elfinder.sakai.msgcntr;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;

/**
 * Created by buckett on 10/08/15.
 */
public class MsgCntrFsItem  implements FsItem{


    private final String id;
    private final FsVolume volume;

    public MsgCntrFsItem(String id, FsVolume volume) {
        this.id = id;
        this.volume = volume;
    }

    public String getId() {
        return id;
    }

    @Override
    public FsVolume getVolume() {
        return volume;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MsgCntrFsItem that = (MsgCntrFsItem) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return !(volume != null ? !volume.equals(that.volume) : that.volume != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (volume != null ? volume.hashCode() : 0);
        return result;
    }
}
