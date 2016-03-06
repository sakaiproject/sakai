package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.user.api.User;

import lombok.Getter;
import lombok.Setter;

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
	private String studentDisplayName;

	@Getter
	private String studentFirstName;

	@Getter
	private String studentLastName;

	@Getter
	private String studentEid;

	@Getter
	@Setter
	private CourseGrade courseGrade;

	@Getter
	private Map<Long, GbGradeInfo> grades;

	@Getter
	private Map<Long, Double> categoryAverages;

	public GbStudentGradeInfo() {
	}

	public GbStudentGradeInfo(final User u) {
		this.studentUuid = u.getId();
		this.studentEid = u.getEid();
		this.studentFirstName = u.getFirstName();
		this.studentLastName = u.getLastName();
		this.studentDisplayName = u.getDisplayName();
		this.grades = new HashMap<Long, GbGradeInfo>();
		this.categoryAverages = new HashMap<Long, Double>();
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
