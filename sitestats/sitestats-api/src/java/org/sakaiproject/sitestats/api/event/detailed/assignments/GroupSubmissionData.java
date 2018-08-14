package org.sakaiproject.sitestats.api.event.detailed.assignments;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Data for a group assignment submission
 * @author plukasew
 */
public class GroupSubmissionData implements AssignmentsData
{
	public final AssignmentData asn;
	public final String group;
	public final Optional<String> submitterId;
	public final boolean byInstructor;

	/**
	 * Constructor
	 * @param asn the assignment
	 * @param group the id of the group submitting the assignment
	 * @param submitterId the id of the user making the actual submission
	 * @param byInstructor whether the submission was made by the instructor on behalf of the group
	 */
	public GroupSubmissionData(AssignmentData asn, String group, String submitterId, boolean byInstructor)
	{
		this.asn = asn;
		this.group = group;
		this.submitterId = Optional.ofNullable(StringUtils.trimToNull(submitterId));
		this.byInstructor = byInstructor;
	}
}
