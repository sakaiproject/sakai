/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.service.gradebook.shared;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JavaBean to hold data associated with a Gradebook assignment. The Course Grade is not considered an assignment.
 */
@NoArgsConstructor
public class Assignment implements Serializable, Comparable<Assignment> {
	private static final long serialVersionUID = 1L;

	/**
	 * the id of the assignment in the gradebook
	 */
	@Getter
	@Setter
	private Long id;

	/**
	 * the assignment name is unique among currently defined assignments. However, it is not a safe UID for persistance, since an assignment
	 * can be renamed. Also, an assignment can be deleted and a new assignment can be created re-using the old name.
	 */
	@Getter
	@Setter
	private String name;

	/**
	 * the total points the assignment is worth.
	 */
	@Getter
	@Setter
	private Double points;

	/**
	 * the due date for the assignment, or null if none is defined.
	 */
	@Getter
	@Setter
	private Date dueDate;

	/**
	 * true if the assignment is maintained by some software other than the Gradebook itself.
	 */
	@Getter
	@Setter
	private boolean externallyMaintained;

	/**
	 * the external id, or null if the assignment is maintained by the Gradebook
	 */
	@Getter
	@Setter
	private String externalId;

	/**
	 * the external app name, or null if the assignment is maintained by the Gradebook
	 */
	@Getter
	@Setter
	private String externalAppName;

	/**
	 * the external data, or null if the assignment is maintained by the Gradebook
	 */
	@Getter
	@Setter
	private String externalData;

	/**
	 * true if the assignment has been released for view to students
	 */
	@Getter
	@Setter
	private boolean released;

	/**
	 * Note that any calls setSortOrder will not be persisted, if you want to change the sort order of an assignment you must call
	 * GradebookService.updateAssignmentOrder as that method properly handles the reordering of all other assignments for the gradebook.
	 */
	@Getter
	@Setter
	private Integer sortOrder;

	@Getter
	@Setter
	private boolean counted;

	@Getter
	@Setter
	private String categoryName;

	@Getter
	@Setter
	private Double weight;

	@Getter
	@Setter
	private boolean ungraded;

	@Getter
	@Setter
	private boolean extraCredit;

	@Getter
	@Setter
	private boolean categoryExtraCredit;

	@Getter
	@Setter
	private Long categoryId;

	@Getter
	@Setter
	private Integer categoryOrder;

	@Getter
	@Setter
	private Integer categorizedSortOrder;

	@Getter
	@Setter
	private boolean scaleGrades;

	@Override
	public int compareTo(final Assignment o) {
		return new CompareToBuilder()
				.append(this.id, o.id)
				.toComparison();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
