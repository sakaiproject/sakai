package org.sakaiproject.tool.gradebook.ui.helpers.entity;

import java.util.Date;

import lombok.Data;

import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;

@Data
public class GradeAssignmentItemDetail extends GradeAssignmentItem {

	protected String graderUserId;
	protected Date dateRecorded;
	protected String comment;

	public GradeAssignmentItemDetail() {
	}

	public GradeAssignmentItemDetail(Assignment assignment, CommentDefinition cd) {
		super(assignment);
		if (cd != null) {
			comment = cd.getCommentText();
			dateRecorded = cd.getDateRecorded();
			graderUserId = cd.getGraderUid();
		}

	}

}
