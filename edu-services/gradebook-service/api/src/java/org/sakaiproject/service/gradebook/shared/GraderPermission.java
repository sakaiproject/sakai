package org.sakaiproject.service.gradebook.shared;

import java.util.ArrayList;
import java.util.List;

/**
 * The list of permissions that can be assigned to a grader
 */
public enum GraderPermission {

	VIEW,
	GRADE,
	VIEW_COURSE_GRADE;
	
	/**
	 * Return a lowercase version of the enum
	 */
	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
	
	/**
	 * Helper to get the view and grade permissions as a list
	 * Used in a few places
	 * @return
	 */
	public static List<String> getStandardPermissions() {
		List<String> rval = new ArrayList<>();
		rval.add(GraderPermission.VIEW.toString());
		rval.add(GraderPermission.GRADE.toString());
		return rval;
	}

}
