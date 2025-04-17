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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author David P. Bauer [dbauer1 (at) udayton (dot) edu]
 */
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class GradingRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private AttendanceSite attendanceSite;
    private Status status;
    private Integer startRange;
    private Integer endRange;
    private Double points;

    public GradingRule(AttendanceSite attendanceSite) {
        this.attendanceSite = attendanceSite;
    }

}
