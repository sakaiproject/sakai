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

/**
 * JavaBean to hold data associated with a Gradebook assignment. The Course Grade is not considered an assignment.
 */
public class Assignment implements Serializable, Comparable<Assignment> {
	private static final long serialVersionUID = 1L;

	private String name;
	private Long id;
	private Double points;
	private Date dueDate;
	private boolean counted;
	private boolean externallyMaintained;
	private String externalId;
	private String externalAppName;
	private boolean released;
	private String categoryName;
	private Double weight;
	private boolean ungraded;
	private boolean extraCredit;
	// Needed for transfer
	private boolean categoryExtraCredit;
	private Integer sortOrder;
	private Integer categorizedSortOrder;
	private Long categoryId;
	private Integer categoryOrder;

	/**
	 * For editing. Not persisted.
	 */
	private boolean scaleGrades;

	public Assignment() {
	}

	/**
	 * @return Returns the name of the assignment. The assignment name is unique among currently defined assignments. However, it is not a
	 *         safe UID for persistance, since an assignment can be renamed. Also, an assignment can be deleted and a new assignment can be
	 *         created re-using the old name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 *
	 * @return Returns the ID of the assignment in the gradebook
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * @return Returns the total points the assignment is worth.
	 */
	public Double getPoints() {
		return this.points;
	}

	/**
	 * @return Returns the due date for the assignment, or null if none is defined.
	 */
	public Date getDueDate() {
		return this.dueDate;
	}

	/**
	 * @return Returns true if the assignment is maintained by some software other than the Gradebook itself.
	 */
	public boolean isExternallyMaintained() {
		return this.externallyMaintained;
	}

	/**
	 *
	 * @return true if the assignment has been released for view to students
	 */
	public boolean isReleased() {
		return this.released;
	}

	/**
	 *
	 * @return Returns the externalAppName, or null if the assignment is maintained by the Gradebook
	 */
	public String getExternalAppName() {
		return this.externalAppName;
	}

	/**
	 *
	 * @return Returns the external Id, or null if the assignment is maintained by the Gradebook
	 */
	public String getExternalId() {
		return this.externalId;
	}

	public boolean isCounted() {
		return this.counted;
	}

	public void setCounted(final boolean notCounted) {
		this.counted = notCounted;
	}

	public void setPoints(final Double points) {
		this.points = points;
	}

	public void setDueDate(final Date dueDate) {
		this.dueDate = dueDate;
	}

	public void setExternalAppName(final String externalAppName) {
		this.externalAppName = externalAppName;
	}

	public void setExternalId(final String externalId) {
		this.externalId = externalId;
	}

	public void setExternallyMaintained(final boolean externallyMaintained) {
		this.externallyMaintained = externallyMaintained;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setReleased(final boolean released) {
		this.released = released;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getCategoryName() {
		return this.categoryName;
	}

	public void setCategoryName(final String categoryName) {
		this.categoryName = categoryName;
	}

	public Double getWeight() {
		return this.weight;
	}

	public void setWeight(final Double weight) {
		this.weight = weight;
	}

	public boolean getUngraded() {
		return this.ungraded;
	}

	public void setUngraded(final boolean ungraded) {
		this.ungraded = ungraded;
	}

	// Extra credit columns
	public void setExtraCredit(final boolean extraCredit) {
		this.extraCredit = extraCredit;
	}

	public boolean isExtraCredit() {
		return this.extraCredit;
	}

	public boolean getExtraCredit() {
		return isExtraCredit();
	}

	// Needed for cateogry transfer
	public void setCategoryExtraCredit(final boolean categoryExtraCredit) {
		this.categoryExtraCredit = categoryExtraCredit;
	}

	public boolean isCategoryExtraCredit() {
		return this.categoryExtraCredit;
	}

	public Integer getSortOrder() {
		return this.sortOrder;
	}

	/**
	 * Note that any calls setSortOrder will not be persisted. If you want to change the sort order of an assignment you must call
	 * GradebookService.updateAssignmentOrder as that method properly handles the reordering of all other assignments for the gradebook.
	 *
	 * @return
	 */
	public void setSortOrder(final Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	public Long getCategoryId() {
		return this.categoryId;
	}

	public void setCategoryId(final Long categoryId) {
		this.categoryId = categoryId;
	}

	public Integer getCategoryOrder() {
		return this.categoryOrder;
	}

	public void setCategoryOrder(final Integer categoryOrder) {
		this.categoryOrder = categoryOrder;
	}

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

	public Integer getCategorizedSortOrder() {
		return this.categorizedSortOrder;
	}

	public void setCategorizedSortOrder(final Integer categorizedSortOrder) {
		this.categorizedSortOrder = categorizedSortOrder;
	}

	public boolean isScaleGrades() {
		return scaleGrades;
	}

	public void setScaleGrades(boolean scaleGrades) {
		this.scaleGrades = scaleGrades;
	}
}
