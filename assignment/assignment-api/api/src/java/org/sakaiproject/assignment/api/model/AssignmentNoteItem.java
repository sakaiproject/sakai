/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.assignment.api.model;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * The AssignmentSupplementItem is to store additional information for the assignment. Candidates include model answers, instructor notes, grading guidelines, etc.
 * @author zqian
 *
 */
public class AssignmentNoteItem {

	/*********** constructors ******************/
	public AssignmentNoteItem() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public AssignmentNoteItem(Long id, String assignmentId, String note,
			String creatorId, int shareWith) {
		super();
		this.id = id;
		this.assignmentId = assignmentId;
		this.note = note;
		this.creatorId = creatorId;
		this.shareWith = shareWith;
	}
	
	/************** attributes and methods ****************/
	/** id in db **/
	private Long id;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/** assignment id **/
	private String assignmentId;

	public String getAssignmentId()
	{
		return assignmentId;
	}
	public void setAssignmentId(String assignmentId)
	{
		this.assignmentId = assignmentId;
	}
	
	/** note **/
	public String note;
	public String getNote()
	{
		return note;
	}
	public void setNote(String note)
	{
		this.note = note;
	}
	
	/** when to show the model answer to student **/
	private int shareWith;

	public int getShareWith() {
		return shareWith;
	}
	public void setShareWith(int shareWith) {
		this.shareWith = shareWith;
	}
	
	/** who created this note item **/
	private String creatorId;
	
	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}
	
	
}
