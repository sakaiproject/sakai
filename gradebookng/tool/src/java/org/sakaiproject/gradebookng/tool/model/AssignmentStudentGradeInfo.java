/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.gradebookng.business.model.GbGradeInfo;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by chmaurer on 1/29/15.
 */
// @Data
public class AssignmentStudentGradeInfo implements Serializable {

	@Getter
	@Setter
	private Long assignmemtId;
	// private String studentId;
	// private GradeInfo gradeInfo;

	@Getter
	private final Map<String, GbGradeInfo> studentGrades;

	public AssignmentStudentGradeInfo() {
		this.studentGrades = new HashMap<String, GbGradeInfo>();
	}

	/**
	 * Helper to add a grade to the map
	 *
	 * @param studentId
	 * @param gradeInfo
	 */
	public void addGrade(final String studentId, final GbGradeInfo gradeInfo) {
		this.studentGrades.put(studentId, gradeInfo);
	}

}
