/**
 * Copyright (c) 2003-2008 The Apereo Foundation
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
package org.sakaiproject.tool.gradebook.ui.helpers.beans;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.ui.helpers.producers.AuthorizationFailedProducer;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;


public class AssignmentGradeRecordBean {
	
		private static final String CANCEL = "cancel";
		private static final String SUBMIT = "submit";
		private static final String FAILURE = "failure";
		
		
		private TargettedMessageList messages;
	    public void setMessages(TargettedMessageList messages) {
	    	this.messages = messages;
	    }

		private MessageLocator messageLocator;
		public void setMessageLocator (MessageLocator messageLocator) {
			this.messageLocator = messageLocator;
		}
		
		private GradebookManager gradebookManager;
	    public void setGradebookManager(GradebookManager gradebookManager) {
	    	this.gradebookManager = gradebookManager;
	    }
	    
	  	private GradebookService gradebookService;
	    public void setGradebookService(GradebookService gradebookService) {
	    	this.gradebookService = gradebookService;
	    }

	    private Long gradebookId;
	    public void setGradebookId(Long gradebookId){
	        this.gradebookId = gradebookId;
	    }

	    private Long assignmentId;
	    public void setAssignmentId(Long assignmentId){
	        this.assignmentId = assignmentId;
	    }

	    private String studentId;
	    public void setStudentId(String studentId){
	        this.studentId = studentId;
	    }

	    private String commentText;
	    public void setCommentText(String commentText){
	        this.commentText = commentText;
	    }

	    private String enteredGrade;
	    public void setEnteredGrade(String enteredGrade) {
	        this.enteredGrade = enteredGrade;
	    }

	    public String processActionSubmitGrade(){
	        if (this.assignmentId == null || this.studentId == null || this.gradebookId == null){
	            return FAILURE;
	        }

	        Gradebook gradebook = gradebookManager.getGradebook(this.gradebookId);
	        GradebookAssignment assignment = gradebookManager.getAssignment(this.assignmentId);

	        if (!gradebookService.isUserAbleToGradeItemForStudent(gradebook.getUid(), this.assignmentId, this.studentId)) {
	            return AuthorizationFailedProducer.VIEW_ID;
	        }
	        
	        boolean errorFound = false;

	        if (!gradebookService.isGradeValid(gradebook.getUid(), enteredGrade)) {
	            errorFound = true;
	        }

	        if (errorFound) {
	            if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
	                messages.addMessage(new TargettedMessage("gradebook.grade-gradebook-item.letter_error",
	                        new Object[] {assignment.getName() }, TargettedMessage.SEVERITY_ERROR));

	            } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
	                messages.addMessage(new TargettedMessage("gradebook.grade-gradebook-item.percent_error",
	                        new Object[] {assignment.getName() }, TargettedMessage.SEVERITY_ERROR));
	            } else {
	                messages.addMessage(new TargettedMessage("gradebook.grade-gradebook-item.points_error",
	                        new Object[] {assignment.getName() }, TargettedMessage.SEVERITY_ERROR));
	            }
	        }

	        if (errorFound) {
	            return FAILURE;
	        }

	        gradebookService.saveGradeAndCommentForStudent(gradebook.getUid(), 
	                assignment.getId(), studentId, enteredGrade, commentText);
			
			return SUBMIT;
		}
		
		public String processActionCancel(){
			
			return CANCEL;
		}

}
