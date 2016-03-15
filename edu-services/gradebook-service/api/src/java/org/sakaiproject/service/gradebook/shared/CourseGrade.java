package org.sakaiproject.service.gradebook.shared;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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
	
	public CourseGrade() {}

	/**
	 * ID of this course grade record. This will be null if the course grade is calculated, and non null if we have an override (as it then refers to the course grade assignment id).
	 * @return 
	 */
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEnteredGrade() {
		return enteredGrade;
	}

	public void setEnteredGrade(String enteredGrade) {
		this.enteredGrade = enteredGrade;
	}

	public String getCalculatedGrade() {
		return calculatedGrade;
	}

	public void setCalculatedGrade(String calculatedGrade) {
		this.calculatedGrade = calculatedGrade;
	}

	public String getMappedGrade() {
		return mappedGrade;
	}

	public void setMappedGrade(String mappedGrade) {
		this.mappedGrade = mappedGrade;
	}
	
	public Double getPointsEarned() {
		return pointsEarned;
	}

	public void setPointsEarned(Double pointsEarned) {
		this.pointsEarned = pointsEarned;
	}

	/**
	 * This value is only accurate when there are no weighted categories.
	 * If weighting is enabled, this value will not be what you expect.
	 * For this reason, this value should not be used when weighted categories are enabled.
	 * @return Double representing the total points possible, see caveat.
	 */
	public Double getTotalPointsPossible() {
		return totalPointsPossible;
	}

	public void setTotalPointsPossible(Double totalPointsPossible) {
		this.totalPointsPossible = totalPointsPossible;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
