package org.sakaiproject.elfinder.sakai.assignment;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;

/**
 * Created by neelam on 11-Jan-16.
 */
public class AssignmentFsItem implements FsItem {
    private final FsVolume fsVolume;
    private final String id;
    private final String title;
    
    public AssignmentFsItem(FsVolume fsVolume, String id, String title){
        this.id = id;
        this.fsVolume = fsVolume;
        this.title = title;
    }
    @Override
    public FsVolume getVolume() {
        return fsVolume;
    }
    public String getId() {
        return id;
    }
    
    public String getTitle(){
        return title;
    }
    
    @Override
    public boolean equals(Object object){
        if(this == object){
            return true;
        }
        if(object == null || getClass() != object.getClass()){
            return false;
        }
        AssignmentFsItem second = (AssignmentFsItem)object;
        if(id != null ? !id.equals(second.getId()) : second.getId()!= null) return false;
        return !(fsVolume != null ? !fsVolume.equals(second.fsVolume) : second.fsVolume!= null);
    }
    
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (fsVolume != null ? fsVolume.hashCode() : 0);
        return result;
    }
}
