/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
