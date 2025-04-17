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

import java.io.Serializable;

/**
 * The AttendanceGrade earned for the all AttendanceItems
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]au)
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceGrade implements Serializable {
	private static final 	long 		serialVersionUID = 1L;

	private 				Long 			id;
	private					Double			grade;
	private					String			userID;
	private					Boolean			override;

	private 				AttendanceSite 	attendanceSite;

	public AttendanceGrade(AttendanceSite aS, String userId){
		this.attendanceSite = aS;
		this.userID 		= userId;
	}
}
