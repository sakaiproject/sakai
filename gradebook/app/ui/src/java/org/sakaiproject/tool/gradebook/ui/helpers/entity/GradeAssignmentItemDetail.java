package org.sakaiproject.tool.gradebook.ui.helpers.entity;

import java.util.Date;

import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;

import lombok.Getter;
import lombok.Setter;

public class GradeAssignmentItemDetail extends GradeAssignmentItem {

	@Getter
	@Setter
	private String graderUserId;
	
	@Getter
	@Setter
	private Date dateRecorded;
	
	@Getter
	@Setter
	private String comment;

	public GradeAssignmentItemDetail() {
	}

	public GradeAssignmentItemDetail(Assignment assignment, CommentDefinition cd) {
		super(assignment);
		if (cd != null) {
			this.comment = cd.getCommentText();
			this.dateRecorded = cd.getDateRecorded();
			this.graderUserId = cd.getGraderUid();
		}

	}

}
