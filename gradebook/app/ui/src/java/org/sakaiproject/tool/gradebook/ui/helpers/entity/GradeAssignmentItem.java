package org.sakaiproject.tool.gradebook.ui.helpers.entity;

import org.sakaiproject.service.gradebook.shared.Assignment;

import lombok.Getter;
import lombok.Setter;


public class GradeAssignmentItem {
	
	@Getter
	@Setter
	private String userId;
	
	@Getter
	@Setter
	private String userName;
	
	@Getter
	@Setter
	private String itemName;
	
	@Getter
	@Setter
	private Double points;
	
	@Getter
	@Setter
	private String grade;

	public GradeAssignmentItem() {
	}

	public GradeAssignmentItem(Assignment assignment) {
		this.itemName = assignment.getName();
		this.points = assignment.getPoints();
	}
}
