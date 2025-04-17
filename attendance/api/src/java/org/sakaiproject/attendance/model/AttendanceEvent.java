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

import lombok.*;

import java.io.Serializable;
import java.util.*;

/**
 * Represents an AttendanceEvent, such as a class meeting or seminar
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu])
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude={"records","stats"})
public class AttendanceEvent implements Serializable {
	private static final 	long 		serialVersionUID = 1L;

	@Getter	@Setter	private 				Long 					id;
	@Getter @Setter private 				String 					name;
	@Getter @Setter private 				Date 					startDateTime;
	@Getter @Setter private 				Date	 				endDateTime;
	@Getter @Setter private 				Boolean 				isReoccurring;
	@Getter @Setter private 				Long 					reoccurringID;
	@Getter @Setter private 				Boolean 				isRequired;
	@Getter @Setter private 				String					releasedTo;
	@Getter @Setter private 				AttendanceSite 			attendanceSite;
	@Getter @Setter private 				String 					location;
	@Getter @Setter private 				Set<AttendanceRecord> 	records = new HashSet<AttendanceRecord>(0);
	@Getter @Setter private					AttendanceItemStats 	stats;

	// Copy constructor
	public AttendanceEvent(AttendanceEvent attendanceEvent){
		this.name 			= attendanceEvent.name;
		this.startDateTime 	= attendanceEvent.startDateTime;
		this.endDateTime 	= attendanceEvent.endDateTime;
		this.isReoccurring 	= attendanceEvent.isReoccurring;
		this.reoccurringID 	= attendanceEvent.reoccurringID;
		this.isRequired 	= attendanceEvent.isRequired;
		this.releasedTo 	= attendanceEvent.releasedTo;
		this.attendanceSite = attendanceEvent.attendanceSite;
		this.location 		= attendanceEvent.location;
	}

}
