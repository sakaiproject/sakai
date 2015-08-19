package org.sakaiproject.service.gradebook.shared;

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

}
