/**
 * Copyright (c) 2006-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
