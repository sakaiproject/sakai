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
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.models.DetachableAttendanceStatusModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An AttendanceStatus Provider
 *
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class AttendanceStatusProvider extends BaseProvider<AttendanceStatus> {
    // AttendanceStatus Filters
    public static final String ALL = "allStatuses"; // Provides all statuses including "UNKNOWN"
    public static final String ALL_ACTIVE = "allActiveStatuses"; // Provides only the active statuses including "UNKNOWN"
    public static final String ACTIVE = "activeStatuses"; // Provides only the active statuses but doesn't include "UNKOWN"
    public static final String DISPLAY = "displayStatuses"; // Provides active and inactive but doesn't include "UNKNOWN"

    public AttendanceStatusProvider() {
        super();
    }

    /**
     * Constructor for AttendanceStatuses for a Site with a specific Filter
     *
     * @param siteId, the Sakai SiteID
     * @param filter, the AttendanceStatusProvider filter (public strings of this class: ALL, ALL_ACTIVE, ACTIVE, and DISPLAY)
     */
    public AttendanceStatusProvider(String siteId, String filter) {
        super();
        AttendanceSite attendanceSite = attendanceLogic.getAttendanceSite(siteId);
        if(attendanceSite != null) {
            List<AttendanceStatus> attendanceStatuses = attendanceLogic.getAllStatusesForSite(attendanceSite);
            this.list = new ArrayList<AttendanceStatus>();
            filterStatuses(attendanceStatuses, filter);
        }
    }

    /**
     * Constructor for AttendanceStatuses for the provided AttendanceSite with a specific Filter
     *
     * @param attendanceSite, the AttendanceSite
     * @param filter, the AttendanceStatusProvider filter (public strings of this class: ALL, ALL_ACTIVE, ACTIVE, and DISPLAY)
     */
    public AttendanceStatusProvider(AttendanceSite attendanceSite, String filter) {
        super();
        if(attendanceSite != null) {
            List<AttendanceStatus> attendanceStatuses = attendanceLogic.getAllStatusesForSite(attendanceSite);
            this.list = new ArrayList<AttendanceStatus>();
            filterStatuses(attendanceStatuses, filter);
        }
    }

    @Override
    List<AttendanceStatus> getData() {
        if(this.list == null) {
            this.list = new ArrayList<AttendanceStatus>();
        }

        Collections.sort(this.list, new Comparator<AttendanceStatus>() {
            @Override
            public int compare(AttendanceStatus o1, AttendanceStatus o2) {
                return o1.getSortOrder() - o2.getSortOrder();
            }
        });

        return this.list;
    }

    @Override
    public IModel<AttendanceStatus> model(AttendanceStatus attendanceStatus) {
        return Model.of(attendanceStatus);
    }

    private void filterStatuses(List<AttendanceStatus> attendanceStatuses, String filter) {
        for(AttendanceStatus attendanceStatus : attendanceStatuses) {
            if(ALL.equals(filter)) {
                list.add(attendanceStatus);
            } else if(ALL_ACTIVE.equals(filter) && attendanceStatus.getIsActive()) {
                list.add(attendanceStatus);
            } else if(ACTIVE.equals(filter) && attendanceStatus.getIsActive() && attendanceStatus.getStatus() != Status.UNKNOWN) {
                list.add(attendanceStatus);
            } else if(DISPLAY.equals(filter) && attendanceStatus.getStatus() != Status.UNKNOWN) {
                list.add(attendanceStatus);
            }
        }
    }
}
