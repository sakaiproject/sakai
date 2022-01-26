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
package org.sakaiproject.service.gradebook.shared;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Wrapper for the course grade that contains the the calculated grade (ie 46.67), the mapped grade (ie F) and any entered grade override (ie D-).
 */
public class CourseGrade implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String enteredGrade;
	private String calculatedGrade;
	private String mappedGrade;
	private Double pointsEarned;
	private Double totalPointsPossible;
	private Date dateRecorded;

	public CourseGrade() {
		//
	}

	/**
	 * ID of this course grade record. This will be null if the course grade is calculated, and non null if we have an override (as it then refers to the course grade assignment id).
	 * @return
	 */
	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getEnteredGrade() {
		return this.enteredGrade;
	}

	public void setEnteredGrade(final String enteredGrade) {
		this.enteredGrade = enteredGrade;
	}

	public String getCalculatedGrade() {
		return this.calculatedGrade;
	}

	public void setCalculatedGrade(final String calculatedGrade) {
		this.calculatedGrade = calculatedGrade;
	}

	public String getMappedGrade() {
		return this.mappedGrade;
	}

	public void setMappedGrade(final String mappedGrade) {
		this.mappedGrade = mappedGrade;
	}

	public Double getPointsEarned() {
		return this.pointsEarned;
	}

	public void setPointsEarned(final Double pointsEarned) {
		this.pointsEarned = pointsEarned;
	}

	/**
	 * This value is only accurate when there are no weighted categories.
	 * If weighting is enabled, this value will not be what you expect.
	 * For this reason, this value should not be used when weighted categories are enabled.
	 * @return Double representing the total points possible, see caveat.
	 */
	public Double getTotalPointsPossible() {
		return this.totalPointsPossible;
	}

	public void setTotalPointsPossible(final Double totalPointsPossible) {
		this.totalPointsPossible = totalPointsPossible;
	}

	public Date getDateRecorded() {
		return this.dateRecorded;
	}

	public void setDateRecorded(final Date dateRecorded) {
		this.dateRecorded = dateRecorded;
	}

	/**
	 * Helper to get a grade override preferentially, or fallback to the standard mapped grade.
	 * @return
	 */
	public String getDisplayGrade() {
		return (StringUtils.isNotBlank(getEnteredGrade()) ? getEnteredGrade() : getMappedGrade());
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
