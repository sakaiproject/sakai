/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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

import java.util.Date;

/**
 *
 */
public class CommentDefinition {
	private String studentUid;
	private String graderUid;
	private Date dateRecorded;
	private String commentText;
	private String assignmentName;
	
	public String getAssignmentName() {
		return assignmentName;
	}
	public void setAssignmentName(String assignmentName) {
		this.assignmentName = assignmentName;
	}
	public String getCommentText() {
		return commentText;
	}
	public void setCommentText(String commentText) {
		this.commentText = commentText;
	}
	public Date getDateRecorded() {
		return dateRecorded;
	}
	public void setDateRecorded(Date dateRecorded) {
		this.dateRecorded = dateRecorded;
	}
	public String getGraderUid() {
		return graderUid;
	}
	public void setGraderUid(String graderUid) {
		this.graderUid = graderUid;
	}
	public String getStudentUid() {
		return studentUid;
	}
	public void setStudentUid(String studentUid) {
		this.studentUid = studentUid;
	}
}
