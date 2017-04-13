/**
 * Copyright (c) 2016 Apereo Foundation
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
package org.sakaiproject.elfinder.sakai.samigo;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;

/**
 * Created by ddelblanco on 03/16.
 */
public class SamFsItem  implements FsItem{


    private final String id;
    private final FsVolume fsVolume;
    private PublishedAssessmentFacade assessment;

    public SamFsItem(String id, FsVolume fsVolume) {
        this.id = id;
        this.fsVolume = fsVolume;
    }

    public SamFsItem(PublishedAssessmentFacade assessment, String id, FsVolume fsVolume ) {
        this(id, fsVolume);
        this.assessment = assessment;
    }

    public String getId() {
        return id;
    }

    public PublishedAssessmentFacade getAssessment() {
        return assessment;
    }

    @Override
    public FsVolume getVolume() {
        return fsVolume;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object){
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        SamFsItem second = (SamFsItem) object;

        if (id != null ? !id.equals(second.id) : second.id != null) return false;
        return !(fsVolume != null ? !fsVolume.equals(second.fsVolume) : second.fsVolume != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (fsVolume != null ? fsVolume.hashCode() : 0);
        return result;
    }
}
