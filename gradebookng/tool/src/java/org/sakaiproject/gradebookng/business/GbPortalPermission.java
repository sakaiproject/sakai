package org.sakaiproject.gradebookng.business;

/**
 * Represents the permissions used in the gradebook. The original String constants are not accessible so they are provided here for
 * convenience.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public enum GbPortalPermission {

	GRADE_ALL("gradebook.gradeAll"),
	GRADE_SECTION("gradebook.gradeSection"),
	EDIT_ASSIGNMENTS("gradebook.editAssignments"),
	VIEW_OWN_GRADES("gradebook.viewOwnGrades");

	private String value;

	GbPortalPermission(final String value) {
		this.value = value;
	}

	/**
	 * Get the actual name of the permission
	 * 
	 * @return
	 */
	public String getValue() {
		return this.value;
	}

}
