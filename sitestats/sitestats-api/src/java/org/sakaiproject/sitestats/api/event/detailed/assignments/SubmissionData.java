package org.sakaiproject.sitestats.api.event.detailed.assignments;

/**
 * Data for an individual (non-group) submission
 * @author plukasew
 */
public class SubmissionData implements AssignmentsData
{
	public final AssignmentData asn;
	public final String submitterId;
	public final boolean byInstructor;

	/**
	 * Constructor
	 * @param asn the assignment
	 * @param submitterId id of the user submitting the assignment
	 * @param byInstructor whether the submission was made by the instructor on behalf of the student
	 */
	public SubmissionData(AssignmentData asn, String submitterId, boolean byInstructor)
	{
		this.asn = asn;
		this.submitterId = submitterId;
		this.byInstructor = byInstructor;
	}
}
