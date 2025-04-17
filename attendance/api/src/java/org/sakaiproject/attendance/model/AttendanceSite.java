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
import org.sakaiproject.attendance.util.AttendanceConstants;

import java.io.Serializable;
import java.util.*;

/**
 * An AttendanceSite represents all the Attendance related data for a specific Sakai Site.
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude="attendanceStatuses")
public class AttendanceSite implements Serializable {
	private static final 					long 					serialVersionUID 	= 1L;

	@Getter	@Setter	private 				Long 					id;
	@Getter	@Setter	private 				String 					siteID;
	@Getter	@Setter	private 				Status 					defaultStatus;
	@Getter @Setter private					Double					maximumGrade;
	@Getter @Setter private					Boolean					isGradeShown;
			@Setter private					Boolean					sendToGradebook;
			@Setter private					Boolean					useAutoGrading;
			@Setter private					Boolean					autoGradeBySubtraction;
	@Getter @Setter private					String					gradebookItemName;
	@Getter @Setter private					Boolean					showCommentsToStudents;
			@Setter private					Boolean					isSyncing;
	@Getter @Setter private					Date					syncTime;
	@Getter	@Setter	private 				Set<AttendanceStatus>	attendanceStatuses	= new HashSet<>(0);

	public AttendanceSite(String siteID){
		this.siteID 				= siteID;
		this.defaultStatus 			= Status.UNKNOWN;
		this.isGradeShown 			= false;
		this.sendToGradebook 		= false;
		this.useAutoGrading			= false;
		this.autoGradeBySubtraction = true;
		this.gradebookItemName 		= AttendanceConstants.GRADEBOOK_ITEM_NAME;
		this.showCommentsToStudents = false;
		this.isSyncing				= false;
	}

	public Boolean getSendToGradebook() {
		if(this.sendToGradebook == null) {
			return false;
		}

		return this.sendToGradebook;
	}

	public Boolean getIsSyncing() {
		if(this.isSyncing == null) {
			return false;
		}

		return this.isSyncing;
	}

	public Boolean getUseAutoGrading() {
		if(this.useAutoGrading == null) {
			return false;
		}

		return this.useAutoGrading;
	}

	public Boolean getAutoGradeBySubtraction() {
		if(this.autoGradeBySubtraction == null) {
			return true;
		}

		return this.autoGradeBySubtraction;
	}

}
