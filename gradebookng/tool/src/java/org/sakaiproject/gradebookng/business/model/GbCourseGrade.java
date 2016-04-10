package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.service.gradebook.shared.CourseGrade;

import lombok.Getter;

/**
 * Wraps a {@link CourseGrade} and provides a display string
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbCourseGrade implements Serializable, Comparable<GbCourseGrade> {

	private static final long serialVersionUID = 1L;

	@Getter
	private final CourseGrade courseGrade;

	@Getter
	private String displayString;

	/**
	 * Constructor. Takes a {@link CourseGrade}. Display string is set
	 * afterwards.
	 *
	 * @param courseGrade
	 *            CourseGrade object
	 */
	public GbCourseGrade(final CourseGrade courseGrade) {
		this.courseGrade = courseGrade;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Only compares grades
	 */
	@Override
	public int compareTo(final GbCourseGrade o) {
		return new CompareToBuilder().append(this.displayString, o.getDisplayString()).toComparison();

	}

}
