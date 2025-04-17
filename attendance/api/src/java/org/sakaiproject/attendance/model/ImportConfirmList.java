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

package org.sakaiproject.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Holds the records that are being changed by the Import function.
 *
 * Created by james on 6/9/17.
 */
@NoArgsConstructor
@AllArgsConstructor
public class ImportConfirmList implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter @Setter private Long                id;
    @Getter @Setter private AttendanceEvent     attendanceEvent;
    @Getter @Setter private AttendanceRecord    attendanceRecord;
    @Getter @Setter private AttendanceSite      attendanceSite;
    @Getter @Setter private String              userID;
    @Getter @Setter private Status              status;
    @Getter @Setter private Status              oldStatus;
    @Getter @Setter private String              comment;
    @Getter @Setter private String              oldComment;
    @Getter @Setter private String              eventName;
    @Getter @Setter private String              eventDate;


    public ImportConfirmList(AttendanceEvent e, String uId, Status newStatus) {
        this.attendanceEvent    = e;
        this.userID             = uId;
        this.status             = newStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final org.sakaiproject.attendance.model.ImportConfirmList that = (org.sakaiproject.attendance.model.ImportConfirmList) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(attendanceEvent, that.attendanceEvent) &&
                Objects.equals(userID, that.userID) &&
                status == that.status &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(oldComment, that.oldComment) &&
                Objects.equals(oldStatus, that.oldStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
