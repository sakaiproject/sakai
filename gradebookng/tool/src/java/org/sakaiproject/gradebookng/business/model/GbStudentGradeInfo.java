package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.entity.api.ResourceProperties;
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
	private Map<String, String> studentExtraProperties;

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
		this.studentUuid = u.getId();
		this.studentEid = u.getEid();
		this.studentFirstName = u.getFirstName();
		this.studentLastName = u.getLastName();
		this.studentDisplayName = u.getDisplayName();
		this.studentExtraProperties = flattenPropertiesToMap(u.getProperties());
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

	/**
	 * Helper to convert ResourceProperties of user properties to a simple map.
	 * @param rp
	 * @return Map<String, String>
	 */
	private Map<String, String> flattenPropertiesToMap(ResourceProperties rp) {
		Map<String, String> map = new HashMap<>();

		for (Iterator<String> i = rp.getPropertyNames(); i.hasNext();) {
			String propName = i.next();
			String propVal = rp.getPropertyFormatted(propName);
			map.put(propName, propVal);
		}
		return map;
	}

}
