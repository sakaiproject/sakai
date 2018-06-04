/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.user.api.User;

/**
 * Model for storing the grade info for a student
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbStudentGradeInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Getter
	private String studentUuid;

	@Getter
	private String studentDisplayId;

	@Getter
	private String studentDisplayName;

	@Getter
	private String studentFirstName;

	@Getter
	private String studentLastName;

	@Getter
	private String studentEid;
	
	@Getter
	private String studentNumber;

	@Getter
	@Setter
	private GbCourseGrade courseGrade;

	@Getter
	private Map<Long, GbGradeInfo> grades;

	@Getter
	private Map<Long, Double> categoryAverages;

	public GbStudentGradeInfo() {
	}

	public GbStudentGradeInfo(final User u) {
		this(u, "");
	}

	public GbStudentGradeInfo(final GbUser u) {
		studentUuid = u.getUserUuid();
		studentEid = u.getDisplayId();
		studentDisplayName = u.getDisplayName();
		this.studentFirstName = u.getFirstName();
		this.studentLastName = u.getLastName();
		studentNumber = u.getStudentNumber();
		grades = new HashMap<>();
		categoryAverages = new HashMap<>();
	}

	public GbStudentGradeInfo(final User u, final String studentNumber) {
		this.studentUuid = u.getId();
		this.studentEid = u.getEid();
		this.studentDisplayId = u.getDisplayId();
		this.studentFirstName = u.getFirstName();
		this.studentLastName = u.getLastName();
		this.studentDisplayName = u.getDisplayName();
		this.studentNumber = studentNumber;
		this.grades = new HashMap<>();
		this.categoryAverages = new HashMap<>();
	}

	/**
	 * Helper to add an assignment grade to the map
	 *
	 * @param assignmentId
	 * @param gradeInfo
	 */
	public void addGrade(final Long assignmentId, final GbGradeInfo gradeInfo) {
		this.grades.put(assignmentId, gradeInfo);
	}

	/**
	 * Helper to add a category average to the map
	 *
	 * @param categoryId
	 * @param score
	 */
	public void addCategoryAverage(final Long categoryId, final Double score) {
		this.categoryAverages.put(categoryId, score);
	}
}
