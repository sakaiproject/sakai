package org.sakaiproject.tool.gradebook.ui.helpers.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Set;

import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.EventTrackingService;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;


import uk.org.ponder.beanutil.entity.EntityBeanLocator;
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
			
	    private Map<String, AssignmentGradeRecord> OTPMap;
		@SuppressWarnings("unchecked")
		public void setAssignmentGradeRecordEntityBeanLocator(EntityBeanLocator entityBeanLocator) {
			this.OTPMap = entityBeanLocator.getDeliveredBeans();
		}
		
		private Map<String, Comment> CommentOTPMap;
		public void setCommentEntityBeanLocator(EntityBeanLocator entityBeanLocator) {
			this.CommentOTPMap = entityBeanLocator.getDeliveredBeans();
		}

		private MessageLocator messageLocator;
		public void setMessageLocator (MessageLocator messageLocator) {
			this.messageLocator = messageLocator;
		}
		
		private GradebookManager gradebookManager;
	    public void setGradebookManager(GradebookManager gradebookManager) {
	    	this.gradebookManager = gradebookManager;
	    }
	    
	    private EventTrackingService eventTrackingService;
	    public EventTrackingService getEventTrackingService() {
	        return eventTrackingService;
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
		
		private String comment;
		public void setComment(String comment){
			this.comment = comment;
		}
		
		public String processActionSubmitGrade(){
			if (this.assignmentId == null || this.studentId == null || this.gradebookId == null){
				return FAILURE;
			}
			Assignment assignment = gradebookManager.getAssignment(this.assignmentId);
			Comment comment = new Comment();
			AssignmentGradeRecord agr = new AssignmentGradeRecord();
			List gradeRecords = new ArrayList();
			for (String key : OTPMap.keySet()) {
				agr = OTPMap.get(key);
				
				agr.setStudentId(this.studentId);
				agr.setDateRecorded(new Date());
				agr.setGradableObject(assignment);
				
				gradeRecords.add(agr);
			}
			List comments = new ArrayList();
			for (String key : CommentOTPMap.keySet()) {
				comment = CommentOTPMap.get(key);
				comment.setDateRecorded(new Date());
				comment.setGradableObject(assignment);
				comment.setStudentId(this.studentId);
				
				comments.add(comment);
			}
			Set excessiveScores = gradebookManager.updateAssignmentGradesAndComments(assignment, gradeRecords, comments);
			
			/**
			eventTrackingService.postEvent("gradebook.updateItemScores", "/gradebook/" + this.gradebookId + "/1/" + getAuthzLevel());
			
			eventTrackingService.postEvent("gradebook.comment", "/gradebook/" + this.gradebookId + "/1/" + getAuthzLevel());
			**/
			return SUBMIT;
		}
		
		public String processActionCancel(){
			
			return CANCEL;
		}
		
	
		public AssignmentGradeRecord getAssignmentGradeRecordById(Long assignmentGradeRecordId){
			return gradebookManager.getAssignmentGradeRecordById(assignmentGradeRecordId);
		}
		
		public Comment getCommentById(Long commentId){
			return gradebookManager.getCommentById(commentId);
		}
		
		public String getAuthzLevel(){
			
	         return (gradebookService.currentUserHasGradeAllPerm(gradebookManager.getGradebook(this.gradebookId).getUid()) ?"instructor" : "TA");
	    }
}
