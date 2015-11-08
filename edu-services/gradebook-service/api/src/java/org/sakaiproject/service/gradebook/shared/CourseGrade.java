package org.sakaiproject.service.gradebook.shared;

import java.io.Serializable;

/**
 * Wrapper for the course grade that contains the the calculated grade (ie 46.67), the mapped grade (ie F) and any entered grade override (ie D-).
 */
public class CourseGrade implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String enteredGrade;
	private String calculatedGrade;
	private String mappedGrade;
	
	public CourseGrade() {}

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
	
	
	
}
