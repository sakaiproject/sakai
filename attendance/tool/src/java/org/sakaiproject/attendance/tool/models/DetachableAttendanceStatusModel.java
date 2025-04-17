/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.tool.models;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.model.AttendanceStatus;

/**
 * DetachableAttendanceStatusModel
 *
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class DetachableAttendanceStatusModel extends LoadableDetachableModel<AttendanceStatus>{
    @SpringBean(name="org.sakaiproject.attendance.logic.AttendanceLogic")
    protected AttendanceLogic attendanceLogic;

    private static final long serialVersionUID = 1L;
    private final long id;

    /**
     * Constructor with provided AttendanceStatus
     *
     * @param t, the AttendanceStatus
     */
    public DetachableAttendanceStatusModel(AttendanceStatus t){
        this.id = t.getId();
    }

    /**
     * Constructor with an AttendanceStatus id provided
     *
     * @param id, an AttendanceStatus id
     */
    public DetachableAttendanceStatusModel(long id){
        this.id = id;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }

    /**
     * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
     *
     * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(final Object obj){
        if (obj == this){
            return true;
        }
        else if (obj == null){
            return false;
        }
        else if (obj instanceof DetachableAttendanceStatusModel) {
            DetachableAttendanceStatusModel other = (DetachableAttendanceStatusModel)obj;
            return other.id == id;
        }
        return false;
    }

    /**
     * @see org.apache.wicket.model.LoadableDetachableModel#load()
     */
    protected AttendanceStatus load(){
        Injector.get().inject(this);
        // get the thing
        return attendanceLogic.getAttendanceStatusById(id);
    }
}
