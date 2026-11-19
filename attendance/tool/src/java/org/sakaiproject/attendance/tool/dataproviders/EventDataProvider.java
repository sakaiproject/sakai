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

package org.sakaiproject.attendance.tool.dataproviders;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.tool.models.DetachableEventModel;

import java.util.Collections;
import java.util.List;

/**
 * An EventDataProvider
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class EventDataProvider extends BaseProvider<AttendanceEvent> {
    public EventDataProvider() {
        super();
    }

    /**
     * Constructor with the data provided
     * @param data, a List of AttendanceEvents
     */
    public EventDataProvider(List<AttendanceEvent> data) {
        super();
        if(data != null && !data.isEmpty()) {
            this.list = data;
        }
    }

    protected List<AttendanceEvent> getData() {
        if(this.list == null) {
            this.list = attendanceLogic.getAttendanceEventsForCurrentSite();
            Collections.reverse(this.list);
        }
        return this.list;
    }

    @Override
    public IModel<AttendanceEvent> model(AttendanceEvent object){
        return Model.of(object);
    }
}
