package org.sakaiproject.gradebookng.business;

/**
 * Represents the roles used in the gradebook. Users are categorised to one of these.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public enum GbRole {

	STUDENT("section.role.student"),
	TA("section.role.ta"),
	INSTRUCTOR("section.role.instructor");

	private String value;

	GbRole(final String value) {
		this.value = value;
	}

	/**
	 * Get the actual name of the role
	 *
	 * @return
	 */
	public String getValue() {
		return this.value;
	}

}
