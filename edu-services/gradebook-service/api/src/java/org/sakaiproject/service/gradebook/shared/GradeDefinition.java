/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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

package org.sakaiproject.service.gradebook.shared;

import java.util.Date;

/**
 *
 */
public class GradeDefinition {
	private String studentUid;
	private String graderUid;
	private Date dateRecorded;
	private String grade;
	private String gradeComment;
	private int gradeEntryType;
	private boolean gradeReleased;
	
	public String getStudentUid() {
		return studentUid;
	}
	public void setStudentUid(String studentUid) {
		this.studentUid = studentUid;
	}
	
	public String getGraderUid() {
		return graderUid;
	}
	public void setGraderUid(String graderUid) {
		this.graderUid = graderUid;
	}
	
	public Date getDateRecorded() {
		return dateRecorded;
	}
	public void setDateRecorded(Date dateRecorded) {
		this.dateRecorded = dateRecorded;
	}
	
	/**
	 * 
	 * @return current grade for this student in the format according to
	 * the gradebook's grade entry type ie %, letter, points
	 */
	public String getGrade() {
		return grade;
	}
	
	/**
	 * current grade for this student in the format according to
	 * the gradebook's grade entry type ie %, letter, points
	 * @param grade
	 */
	public void setGrade(String grade) {
		this.grade = grade;
	}
	
	public String getGradeComment()
	{
		return gradeComment;
	}
	
	public void setGradeComment(String gradeComment)
	{
		this.gradeComment = gradeComment;
	}
	
	/**
	 * 
	 * @return constant equivalent to the grade entry type for the gb -
	 * %, letter, points, etc. lets you know what format the given grade will
	 * be in
	 */
	public int getGradeEntryType() {
		return gradeEntryType;
	}
	
	/**
	 * constant equivalent to the grade entry type for the gb -
	 * %, letter, points, etc.  lets you know what format the given grade will
	 * be in
	 * @param gradeEntryType
	 */
	public void setGradeEntryType(int gradeEntryType) {
		this.gradeEntryType = gradeEntryType;
	}
	
	/**
	 * 
	 * @return true if this grade has been released to the student
	 */
	public boolean isGradeReleased() {
		return gradeReleased;
	}
	
	/**
	 * true if this grade has been released to the student
	 * @param gradeReleased
	 */
	public void setGradeReleased(boolean gradeReleased) {
		this.gradeReleased = gradeReleased;
	}
	
	
}
