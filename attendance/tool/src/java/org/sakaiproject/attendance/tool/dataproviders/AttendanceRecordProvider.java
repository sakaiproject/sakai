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
import org.sakaiproject.attendance.model.AttendanceRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * An AttendanceRecord Provider
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class AttendanceRecordProvider extends BaseProvider<AttendanceRecord> {

    private AttendanceEvent aE;
    private String groupId;
    private String id; // User id

    public AttendanceRecordProvider() {
        super();
    }

    /**
     * A Constructor for a User's AttendanceRecords in the Current Site
     *
     * @param id, the User ID
     */
    public AttendanceRecordProvider(String id) {
        super();
        this.aE = null;
        this.groupId = null;
        this.id = id;
    }

    /**
     * A Construct for AttendanceRecords from the provided AttendanceEvent
     *
     * @param aE, the AttendanceEvent
     */
    public AttendanceRecordProvider(AttendanceEvent aE) {
        super();
        this.aE = aE;
        this.groupId = null;
        this.id = null;
    }

    /**
     * Constructor for AttendanceRecords for an AttendanceEvent for a specific group
     *
     * @param aE, the Attendance Event
     * @param groupId, the Group ID
     */
    public AttendanceRecordProvider(AttendanceEvent aE, String groupId) {
        super();
        this.aE = aE;
        this.groupId = groupId;
        this.id = null;
    }

    protected List<AttendanceRecord> getData() {
        if(this.aE != null) {
            List<String> currentStudentIds;
            if(this.groupId == null) {
                currentStudentIds = sakaiProxy.getCurrentSiteMembershipIds();
            } else {
                currentStudentIds = sakaiProxy.getGroupMembershipIdsForCurrentSite(groupId);
            }
            this.list = new ArrayList<>();
            for(AttendanceRecord record: aE.getRecords()) {
                if(currentStudentIds.contains(record.getUserID())) {
                    this.list.add(record);
                }
            }
        } else if (id != null) {
            List<AttendanceRecord> records = attendanceLogic.getAttendanceRecordsForUser(id);
            if(!records.isEmpty()) {
                // don't think records will ever be empty
                this.list = records;
            }
        }

        if(this.list == null) {
            this.list = new ArrayList<>();
        }

        this.list.sort(Comparator.comparing(
                AttendanceRecord::getUserID, Comparator.comparing(u -> sakaiProxy.getUser(u).getSortName())
        ));

        return this.list;
    }

    @Override
    public IModel<AttendanceRecord> model(AttendanceRecord object){
        if(object.getId() == null) {
            return new Model<>(object);
        }
        return Model.of(object);
    }
}
