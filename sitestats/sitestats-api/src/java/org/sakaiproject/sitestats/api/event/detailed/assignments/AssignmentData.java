package org.sakaiproject.sitestats.api.event.detailed.assignments;

/**
 * Data for an assignment
 * @author plukasew
 */
public class AssignmentData implements AssignmentsData
{
	public final String title;
	public final boolean anonymous;
	public final boolean deleted;

	/**
	 * Constructor
	 * @param title the title of the assignment
	 * @param anon whether the assignment is anonymous
	 * @param deleted whether the assignment has been deleted
	 */
	public AssignmentData(String title, boolean anon, boolean deleted)
	{
		this.title = title;
		anonymous = anon;
		this.deleted = deleted;
	}
}
