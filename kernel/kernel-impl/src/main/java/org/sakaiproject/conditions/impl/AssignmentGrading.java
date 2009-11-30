package org.sakaiproject.conditions.impl;

/**
 * @author Zach A. Thomas
 *
 */
public class AssignmentGrading {
	private String userId;
	private Double score;
	private String assignmentName;
	private boolean isReleased;
	private boolean pastDue;
	private String assignmentSource;
	
	public boolean isScoreBlank() {
		return (score == null);
	}
	
	public boolean isScoreNonBlank() {
		return (score != null);
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	public String getAssignmentName() {
		return assignmentName;
	}
	public void setAssignmentName(String assignmentName) {
		this.assignmentName = assignmentName;
	}
	public boolean isReleased() {
		return isReleased;
	}
	public void setReleased(boolean isReleased) {
		this.isReleased = isReleased;
	}
	public boolean isPastDue() {
		return pastDue;
	}
	public void setPastDue(boolean pastDue) {
		this.pastDue = pastDue;
	}
	public String getAssignmentSource() {
		return assignmentSource;
	}
	public void setAssignmentSource(String assignmentSource) {
		this.assignmentSource = assignmentSource;
	}

}
