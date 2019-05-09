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
	 * @param anonymous whether the assignment is anonymous
	 * @param deleted whether the assignment has been deleted
	 */
	public AssignmentData(String title, boolean anonymous, boolean deleted)
	{
		this.title = title;
		this.anonymous = anonymous;
		this.deleted = deleted;
	}
}
