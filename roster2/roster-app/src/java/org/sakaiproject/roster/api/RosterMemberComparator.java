/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.roster.api;

import java.util.Comparator;

import lombok.RequiredArgsConstructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>Comparator</code> for <code>RosterMember</code>s.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
@RequiredArgsConstructor
public class RosterMemberComparator implements Comparator<RosterMember> {

	private static final Log log = LogFactory.getLog(RosterMemberComparator.class);
	
	// sort fields
	public final static String SORT_NAME		= "sortName";
	public final static String SORT_DISPLAY_ID	= "displayId";
	public final static String SORT_EMAIL		= "email";
	public final static String SORT_ROLE		= "role";
	public final static String SORT_STATUS		= "status";
	public final static String SORT_CREDITS		= "credits";
	// sort directions
	public final static int SORT_ASCENDING		= 0;
	public final static int SORT_DESCENDING		= 1;

	private final String sortField;
	private final int sortDirection;
	private final boolean firstNameLastName;
	
	/**
	 * Compares two <code>RosterMember</code> objects according to the sorting
	 * order configured in this instance of <code>RosterMemberComparator</code>.
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(RosterMember a, RosterMember b) {

		RosterMember member1;
		RosterMember member2;

		if (SORT_DESCENDING == sortDirection) {
			member1 = b;
			member2 = a;
		}
		// just sort ascending by default
		else {
			member1 = a;
			member2 = b;
		}

		if (SORT_NAME.equals(sortField)) {
			if (firstNameLastName) {
				return member1.getDisplayName().compareToIgnoreCase(
						member2.getDisplayName());
			} else {
				return member1.getSortName().compareToIgnoreCase(
						member2.getSortName());
			}
		} else if (SORT_DISPLAY_ID.equals(sortField)) {
			return member1.getDisplayId().compareToIgnoreCase(
					member2.getDisplayId());
		} else if (SORT_EMAIL.equals(sortField)) {
			return member1.getEmail().compareToIgnoreCase(
					member2.getEmail());
		} else if (SORT_ROLE.equals(sortField)) {
			return member1.getRole().compareToIgnoreCase(member2.getRole());
		} else if (SORT_STATUS.equals(sortField)) {
			return member1.getEnrollmentStatus().compareToIgnoreCase(
					member2.getEnrollmentStatus());
		} else if (SORT_CREDITS.equals(sortField)) {
			return member1.getCredits().compareToIgnoreCase(
					member2.getCredits());
		}

		log.warn("members not sorted");

		return 0;
	}
	
}