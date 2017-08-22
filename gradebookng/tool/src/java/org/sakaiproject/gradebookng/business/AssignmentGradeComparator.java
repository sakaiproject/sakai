/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.gradebookng.business;

import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.math.NumberUtils;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;

/**
 * Comparator class for sorting an assignment by the grades.
 *
 * Note that this must have the assignmentId set into it so we can extract the appropriate grade entry from the map that each student has.
 *
 */
public class AssignmentGradeComparator implements Comparator<GbStudentGradeInfo> {

	private final long assignmentId;

	public AssignmentGradeComparator(final long assignmentId) {
		this.assignmentId = assignmentId;
	}

	@Override
	public int compare(final GbStudentGradeInfo g1, final GbStudentGradeInfo g2) {

		final GbGradeInfo info1 = g1.getGrades().get(this.assignmentId);
		final GbGradeInfo info2 = g2.getGrades().get(this.assignmentId);

		// for proper number ordering, these have to be numerical
		final Double grade1 = (info1 != null) ? NumberUtils.toDouble(info1.getGrade()) : null;
		final Double grade2 = (info2 != null) ? NumberUtils.toDouble(info2.getGrade()) : null;

		return new CompareToBuilder().append(grade1, grade2).toComparison();

	}

}
