/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
