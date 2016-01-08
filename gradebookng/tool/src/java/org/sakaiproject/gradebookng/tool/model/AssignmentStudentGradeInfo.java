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
