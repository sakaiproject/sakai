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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Created by james on 7/11/17.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceUserGroupStats {
    private static final    long            serialVersionUID    = 1L;

    private                 String          userID;
    private                 String          groupId;
    private                 AttendanceSite  attendanceSite;


    public AttendanceUserGroupStats(String userID, AttendanceSite attendanceSite) {
        this.userID = userID;
        this.attendanceSite = attendanceSite;
    }

    @Override
    public boolean equals (final Object obj) {
        if(obj == null) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        if(obj.getClass() != getClass()) {
            return false;
        }

        final AttendanceUserGroupStats other = (AttendanceUserGroupStats) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.userID, other.userID)
                .append(this.groupId, other.groupId)
                .append(this.attendanceSite, other.attendanceSite)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.userID)
                .append(this.groupId)
                .append(this.attendanceSite.getId())
                .toHashCode();
    }
}
