/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.roster;

import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.user.api.User;

/**
 * Wraps a participant with enrollment information.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">jholtzman@berkeley.edu</a>
 *
 */
public class EnrolledParticipant implements Participant {
	
	public static final String SORT_BY_STATUS = "status";
	public static final String SORT_BY_CREDITS = "credits";

	protected Participant participant;
	
	protected String enrollmentStatus;
	protected String enrollmentCredits;

	/**
	 * Decorate a Participant with enrollment status and credits.
	 * 
	 * @param participant
	 * @param enrollmentStatus
	 * @param enrollmentCredits
	 */
	public EnrolledParticipant(Participant participant, String enrollmentStatus, String enrollmentCredits) {
		this.participant = participant;
		this.enrollmentStatus = enrollmentStatus;
		this.enrollmentCredits = enrollmentCredits;
	}
	
	public String getEnrollmentCredits() {
		return enrollmentCredits;
	}
	public void setEnrollmentCredits(String enrollmentCredits) {
		this.enrollmentCredits = enrollmentCredits;
	}
	public String getEnrollmentStatus() {
		return enrollmentStatus;
	}
	public void setEnrollmentStatus(String enrollmentStatus) {
		this.enrollmentStatus = enrollmentStatus;
	}
	
	// Delegate methods to the participant impl
	public Profile getProfile() {
		return participant.getProfile();
	}
	public String getRoleTitle() {
		return participant.getRoleTitle();
	}
	public User getUser() {
		return participant.getUser();
	}

	public boolean isOfficialPhotoPublicAndPreferred() {
		return participant.isOfficialPhotoPublicAndPreferred();
	}

	public boolean isProfilePhotoPublic() {
		return participant.isProfilePhotoPublic();
	}

	public boolean isOfficialPhotoPreferred() {
		return participant.isOfficialPhotoPreferred();
	}
	
	public String getGroupsString() {
		return participant.getGroupsString();
	}
}
